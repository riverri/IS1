// Pruebas de extremo a extremo en un navegador real: lo que hace un jugador de principio a fin, con la aplicación
// arrancada de verdad (servidor, base de datos, plantillas y JavaScript a la vez). Las pruebas de MockMvc comprueban
// cada pieza por separado; estas, que todas juntas funcionan como las ve el usuario.
//
// La ejecuta la CI con la aplicación arrancada con los datos de demostración; en local:
//   cd a11y && npm install && APP_LOG=../app.log node e2e.js   (the application must already be running)
//
// Recorrido: registrarse, verificar el correo y entrar; apostar una simple y ver el saldo; cancelarla y recuperar el importe; combinar dos
// partidos en el boleto; el creador pone el resultado y la apuesta se cobra. Después:
// - el creador da de alta un partido con supercuota, un jugador apuesta con ella y al anularlo se le devuelve;
// - una liga entre dos jugadores con un bloqueo de 50 monedas que le impide apostar al otro;
// - una quiniela: se echa un boleto y al anularla se devuelve;
// - recuperar la contraseña con el enlace del registro (si APP_LOG dice dónde está el registro de la aplicación);
// y cerrar sesión.
const { chromium } = require('playwright');

const BASE = process.env.BASE_URL || 'http://localhost:8080';
const CREADOR = ['creador@apuestas.es', process.env.CREADOR_PASSWORD || 'creador123'];

const fallos = [];
const fallo = (que, detalle) => { fallos.push(`${que}: ${detalle}`); console.log(`  ✗ ${que}: ${detalle}`); };
const bien = (que) => console.log(`  ✓ ${que}`);
const comprobar = (condicion, que, detalle) => (condicion ? bien(que) : fallo(que, detalle));

async function entrar(pagina, [email, password]) {
    await pagina.goto(BASE + '/login');
    await pagina.fill('input[name=email]', email);
    await pagina.fill('input[name=password]', password);
    await Promise.all([pagina.waitForNavigation(), pagina.click('main button[type=submit]')]);
}

async function enlaceCorreoLocal(tipo, email) {
    if (!process.env.APP_LOG) {
        throw new Error('Set APP_LOG to the application log file to read local email links in the E2E flow');
    }
    const fs = require('fs');
    const emailEscapado = email.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
    const patron = new RegExp(`Local email link for ${tipo} to ${emailEscapado}: (https?://\\S+)`);
    let enlace;
    for (let intento = 0; intento < 20 && !enlace; intento++) {
        const registro = fs.readFileSync(process.env.APP_LOG, 'utf8');
        enlace = (registro.match(patron) || [])[1];
        if (!enlace) await new Promise(resolve => setTimeout(resolve, 250));
    }
    if (!enlace) {
        throw new Error(`No ${tipo} email link for ${email} was found in ${process.env.APP_LOG}`);
    }
    return enlace;
}

async function verificarCorreo(pagina, email) {
    const enlace = await enlaceCorreoLocal('verificar correo', email);
    await pagina.goto(enlace);
    await pagina.click('main form button[type=submit]');
    await pagina.waitForURL(url => url.pathname === '/registro/completar');
}

/** El saldo de la cabecera, en monedas ("1.234,50 monedas" → 1234.5; el primer span es el icono). */
async function saldo(pagina) {
    await pagina.goto(BASE + '/cuenta');
    const texto = await pagina.textContent('a.saldo span:not(.moneda)');
    return Number(texto.replace(/[^\d,]/g, '').replace(',', '.'));
}

/** Los partidos que admiten apuestas, por su id, en el orden del catálogo. */
async function partidos(pagina) {
    await pagina.goto(BASE + '/eventos');
    const enlaces = await pagina.$$eval('a', as => as.map(a => a.getAttribute('href') || ''));
    return [...new Set(enlaces.map(h => (h.match(/^\/eventos\/(\d+)\/apostar$/) || [])[1]).filter(Boolean))];
}

/** Elige un resultado en la página de apostar (la casilla está escondida tras su etiqueta). */
async function elegir(pagina, opcion) {
    await pagina.locator(`input[name=opcion][value=${opcion}]`).check({ force: true });
}

async function apostar(pagina, evento, opcion, importe) {
    await pagina.goto(`${BASE}/eventos/${evento}/apostar`);
    await elegir(pagina, opcion);
    await pagina.fill('#importe', String(importe));
    await Promise.all([pagina.waitForNavigation(), pagina.click('main form.boleto button[type=submit]:not([formaction])')]);
}

const cerca = (a, b) => Math.abs(a - b) < 0.005;

/** Una página nueva (con su propia sesión) que acepta los «¿Seguro?» de data-confirmar. */
async function nuevaPagina(navegador) {
    const pagina = await (await navegador.newContext()).newPage();
    pagina.on('dialog', d => d.accept());
    return pagina;
}

/** Registra un jugador nuevo y entra con él. */
async function registrar(navegador, nombre, password = 'clave-e2e-123') {
    const pagina = await nuevaPagina(navegador);
    const email = `e2e-${nombre.toLowerCase()}-${Date.now()}@ucm.es`;
    await pagina.goto(BASE + '/registro');
    await pagina.fill('#email', email);
    await Promise.all([pagina.waitForNavigation(), pagina.click('main button[type=submit]')]);
    await verificarCorreo(pagina, email);
    await pagina.fill('#nombre', `Prueba ${nombre}`);
    await pagina.fill('#password', password);
    await pagina.fill('#confirmacion', password);
    await Promise.all([pagina.waitForNavigation(), pagina.click('main button[type=submit]')]);
    await pagina.waitForURL(url => url.pathname === '/login' && url.searchParams.has('registrado'));
    await entrar(pagina, [email, password]);
    return { pagina, email };
}

/** El mayor id de los enlaces de la página que encajan con el patrón (lo último creado). */
async function ultimoId(pagina, ruta, patron) {
    await pagina.goto(BASE + ruta);
    const ids = await pagina.$$eval('a', (as, p) => as.map(a => (a.getAttribute('href') || '').match(new RegExp(p)))
        .filter(Boolean).map(m => Number(m[1])), patron);
    return ids.length ? Math.max(...ids) : null;
}

async function textoMain(pagina) {
    return (await pagina.textContent('main')) || '';
}

const coma = (n) => n.toFixed(2).replace('.', ',');

(async () => {
    const navegador = await chromium.launch();
    const jugador = await (await navegador.newContext()).newPage();
    jugador.on('dialog', d => d.accept()); // «¿Cancelar la apuesta?»

    // 1. Registrarse y entrar
    const email = `e2e-${Date.now()}@ucm.es`;
    await jugador.goto(BASE + '/registro');
    await jugador.fill('#email', email);
    await Promise.all([jugador.waitForNavigation(), jugador.click('main button[type=submit]')]);
    comprobar(jugador.url().includes('/login'), 'registrarse lleva a entrar', jugador.url());
    await verificarCorreo(jugador, email);
    await jugador.fill('#nombre', 'Prueba E2E');
    await jugador.fill('#password', 'clave-e2e-123');
    await jugador.fill('#confirmacion', 'clave-e2e-123');
    await Promise.all([jugador.waitForNavigation(), jugador.click('main button[type=submit]')]);
    comprobar(jugador.url().includes('/login?registrado'), 'crear la cuenta tras confirmar el correo', jugador.url());
    await entrar(jugador, [email, 'clave-e2e-123']);
    const inicial = await saldo(jugador);
    comprobar(inicial > 0, `entra con el saldo de bienvenida (${inicial})`, 'saldo 0');

    const ids = await partidos(jugador);
    if (ids.length < 3) {
        fallo('catálogo', `hacen falta 3 partidos que admitan apuestas y hay ${ids.length}`);
    } else {
        const [a, b, c] = ids;

        // 2. Apuesta simple: se cobra el importe y sale en Mis apuestas
        await apostar(jugador, a, 'LOCAL', 10);
        comprobar(jugador.url().endsWith('/apuestas') && await jugador.$('.alerta-exito'),
            'apuesta simple registrada', jugador.url());
        comprobar(cerca(await saldo(jugador), inicial - 10), 'el saldo baja 10', `saldo ${await saldo(jugador)}`);

        // 3. Cancelarla devuelve el importe
        await jugador.goto(BASE + '/apuestas');
        await Promise.all([jugador.waitForNavigation(), jugador.click('text=Cancelar apuesta')]);
        comprobar(cerca(await saldo(jugador), inicial), 'cancelar devuelve el importe', `saldo ${await saldo(jugador)}`);

        // 4. Combinada: dos partidos en el boleto
        for (const [evento, opcion] of [[b, 'LOCAL'], [c, 'VISITANTE']]) {
            await jugador.goto(`${BASE}/eventos/${evento}/apostar`);
            await elegir(jugador, opcion);
            await Promise.all([jugador.waitForNavigation(), jugador.click('button[formaction$="/boleto/anadir"]')]);
        }
        await jugador.goto(BASE + '/boleto');
        await jugador.fill('form.boleto-confirmar #importe', '5');
        await Promise.all([jugador.waitForNavigation(), jugador.click('form.boleto-confirmar button[type=submit]')]);
        comprobar(jugador.url().endsWith('/apuestas') && (await jugador.textContent('main')).includes('Combinada'),
            'combinada de dos partidos registrada', jugador.url());
        comprobar(cerca(await saldo(jugador), inicial - 5), 'el saldo baja 5', `saldo ${await saldo(jugador)}`);

        // 5. El creador pone el resultado y la apuesta se cobra
        await apostar(jugador, a, 'LOCAL', 10);
        const antes = await saldo(jugador);
        const creador = await (await navegador.newContext()).newPage();
        creador.on('dialog', d => d.accept()); // «El evento aún no ha empezado. ¿Guardar el resultado igualmente?»
        await entrar(creador, CREADOR);
        await creador.goto(`${BASE}/gestion/eventos/${a}`);
        await creador.locator('input[name=resultado][value=LOCAL]').check({ force: true });
        await Promise.all([creador.waitForNavigation(), creador.click('text=Guardar resultado y resolver apuestas')]);
        comprobar((await creador.textContent('main')).includes('Resultado guardado'), 'el creador guarda el resultado',
            await creador.textContent('main .alerta'));
        await creador.context().close();
        const despues = await saldo(jugador);
        comprobar(despues > antes + 10, `la apuesta ganada se cobra (${antes} → ${despues})`, `saldo ${despues}`);
        await jugador.goto(BASE + '/apuestas');
        comprobar((await jugador.textContent('main')).includes('GANADA'), 'sale como ganada en el historial', 'no sale');
    }

    const creador = await nuevaPagina(navegador);
    await entrar(creador, CREADOR);

    // 7. Partido nuevo con supercuota; al anularlo se devuelve lo apostado
    const idsDe = async () => {
        await creador.goto(BASE + '/gestion');
        return new Set(await creador.$$eval('a', as => as.map(a => ((a.getAttribute('href') || '')
            .match(/^\/gestion\/eventos\/(\d+)$/) || [])[1]).filter(Boolean)));
    };
    const antesAlta = await idsDe();
    await creador.goto(BASE + '/gestion/eventos/nuevo');
    const ligaId = await creador.$$eval('#competicionId option', os => (os.find(o => o.textContent.includes('LaLiga')) || os[1]).value);
    await creador.selectOption('#competicionId', ligaId);
    const pareja = await creador.$$eval('#localId option', os => ['Getafe CF', 'Sevilla FC']
        .map(n => (os.find(o => o.textContent.trim() === n) || {}).value));
    await creador.selectOption('#localId', pareja[0]);
    await creador.selectOption('#visitanteId', pareja[1]);
    // Una fecha al azar entre 20 y 50 días, para no repetir un partido de otra ejecución
    const dia = new Date(Date.now() + (20 + Math.floor(Math.random() * 30)) * 86400000).toISOString().slice(0, 10);
    const hora = `${String(10 + Math.floor(Math.random() * 12)).padStart(2, '0')}:${String(Math.floor(Math.random() * 60)).padStart(2, '0')}`;
    await creador.fill('#fechaHora', `${dia}T${hora}`);
    await Promise.all([creador.waitForNavigation(), creador.click('main form button[type=submit]')]);
    const creado = (await textoMain(creador)).includes('Evento creado');
    const nuevo = creado ? [...await idsDe()].find(id => !antesAlta.has(id)) : null;
    comprobar(nuevo, 'el creador da de alta un partido', creado ? 'no aparece en el panel' : await textoMain(creador));
    if (nuevo) {
        await jugador.goto(`${BASE}/eventos/${nuevo}/apostar`);
        const normal = Number(await jugador.getAttribute('input[name=opcion][value=LOCAL]', 'data-cuota'));
        const mejorada = Math.floor(normal * 150) / 100;
        await creador.goto(`${BASE}/gestion/eventos/${nuevo}`);
        await creador.selectOption('form.alerta-cuota select[name=resultado]', 'LOCAL');
        await creador.fill('form.alerta-cuota input[name=cuota]', mejorada.toFixed(2));
        await Promise.all([creador.waitForNavigation(), creador.click('form.alerta-cuota button[type=submit]')]);
        comprobar((await textoMain(creador)).includes('Vigente'), `supercuota publicada (${coma(normal)} → ${coma(mejorada)})`,
            await textoMain(creador));
        const antesSuper = await saldo(jugador);
        await apostar(jugador, nuevo, 'LOCAL', 10);
        comprobar((await textoMain(jugador)).includes(coma(mejorada)), 'la apuesta se acepta con la supercuota', jugador.url());
        await creador.goto(`${BASE}/gestion/eventos/${nuevo}`);
        await Promise.all([creador.waitForNavigation(), creador.click(`form[action$="/gestion/eventos/${nuevo}/anular"] button`)]);
        comprobar(cerca(await saldo(jugador), antesSuper), 'al anular el partido se devuelve lo apostado', `saldo ${await saldo(jugador)}`);
    }

    // 8. Liga entre dos jugadores con un bloqueo
    const otro = await registrar(navegador, 'Liga');
    await jugador.goto(BASE + '/ligas');
    await jugador.fill('#nombre', 'Liga E2E');
    await Promise.all([jugador.waitForNavigation(), jugador.click('form[action$="/ligas"] button[type=submit]')]);
    const codigo = ((await jugador.textContent('main')).match(/código ([A-Z0-9]{4,10})/i) || [])[1]
        || (await jugador.textContent('.codigo-liga').catch(() => '')).trim();
    comprobar(codigo, `liga creada con su código (${codigo})`, jugador.url());
    await otro.pagina.goto(BASE + '/ligas');
    await otro.pagina.fill('#codigo', codigo || '');
    await Promise.all([otro.pagina.waitForNavigation(), otro.pagina.click('form[action$="/ligas/unirse"] button[type=submit]')]);
    comprobar((await textoMain(otro.pagina)).includes('Te has unido'), 'el otro jugador se une con el código', await textoMain(otro.pagina));
    const ligaPropia = await ultimoId(jugador, '/ligas', '^/ligas/(\\d+)$');
    await jugador.goto(`${BASE}/ligas/${ligaPropia}`);
    const partidoBloqueado = await jugador.$eval('#evento-bloqueo option[value]:not([value=""])', o => o.value);
    const antesBloqueo = await saldo(jugador);
    await jugador.goto(`${BASE}/ligas/${ligaPropia}`);
    await jugador.selectOption('#evento-bloqueo', partidoBloqueado);
    await jugador.selectOption('#alcance-amigo', 'LOCAL');
    await Promise.all([jugador.waitForNavigation(), jugador.click('button[data-confirmar^="El bloqueo cuesta 50"]')]);
    comprobar(cerca(await saldo(jugador), antesBloqueo - 50), 'el bloqueo cuesta 50 monedas', `saldo ${await saldo(jugador)}`);
    await apostar(otro.pagina, partidoBloqueado, 'LOCAL', 5);
    comprobar((await textoMain(otro.pagina)).includes('bloqueada'), 'el bloqueado no puede apostar a esa selección',
        otro.pagina.url());

    // 9. Quiniela: echar un boleto y, al anularla, recuperarlo
    await creador.goto(BASE + '/gestion/quinielas');
    const marcables = await creador.$$('input[name=eventos]');
    if (marcables.length < 3) {
        fallo('quiniela', `hacen falta 3 partidos con empate y hay ${marcables.length}`);
    } else {
        for (const casilla of marcables.slice(0, 3)) {
            await casilla.check();
        }
        await creador.fill('#nombre', `Quiniela E2E ${Date.now()}`);
        await creador.fill('#precio', '2');
        await Promise.all([creador.waitForNavigation(), creador.click('form[action$="/gestion/quinielas"] button[type=submit]')]);
        comprobar((await textoMain(creador)).includes('creada con 3 partidos'), 'el creador monta una quiniela', await textoMain(creador));
        const quiniela = await ultimoId(jugador, '/quinielas', '^/quinielas/(\\d+)$');
        await jugador.goto(`${BASE}/quinielas/${quiniela}`);
        const antesQuiniela = await saldo(jugador);
        await jugador.goto(`${BASE}/quinielas/${quiniela}`);
        for (const casilla of await jugador.$$('input[type=radio][value=LOCAL]')) {
            await casilla.check({ force: true });
        }
        await Promise.all([jugador.waitForNavigation(), jugador.click('text=Echar el boleto')]);
        comprobar(cerca(await saldo(jugador), antesQuiniela - 2), 'el boleto cuesta 2 monedas', `saldo ${await saldo(jugador)}`);
        await creador.goto(BASE + '/gestion/quinielas');
        await Promise.all([creador.waitForNavigation(),
            creador.click(`form[action$="/gestion/quinielas/${quiniela}/anular"] button`)]);
        comprobar(cerca(await saldo(jugador), antesQuiniela), 'al anular la quiniela se devuelve el boleto', `saldo ${await saldo(jugador)}`);
    }

    // 10. Recuperar la contraseña con el enlace que se escribe en el registro (sin servidor de correo)
    if (process.env.APP_LOG) {
        const sinSesion = await nuevaPagina(navegador);
        await sinSesion.goto(BASE + '/recuperar');
        await sinSesion.fill('#email', otro.email);
        await Promise.all([sinSesion.waitForNavigation(), sinSesion.click('main button[type=submit]')]);
        const enlace = await enlaceCorreoLocal('recuperar contraseña', otro.email);
        const destino = new URL(enlace);
        await sinSesion.goto(BASE + destino.pathname + destino.search);
        await sinSesion.fill('#nueva', 'otra-clave-e2e-456');
        await sinSesion.fill('#confirmacion', 'otra-clave-e2e-456');
        await Promise.all([sinSesion.waitForNavigation(), sinSesion.click('main button[type=submit]')]);
        await entrar(sinSesion, [otro.email, 'otra-clave-e2e-456']);
        comprobar(!sinSesion.url().includes('/login'), 'entra con la contraseña nueva', sinSesion.url());
    } else {
        console.log('  (sin APP_LOG: no se prueba recuperar la contraseña)');
    }
    await creador.context().close();

    // 6. Cerrar sesión
    await Promise.all([jugador.waitForNavigation(), jugador.click('form[action$="/logout"] button')]);
    comprobar((await jugador.textContent('main')).includes('Has cerrado sesión'), 'cerrar sesión lo dice', jugador.url());

    await navegador.close();
    console.log(fallos.length ? `\n${fallos.length} fallos de extremo a extremo` : '\nTodo el recorrido funciona');
    process.exit(fallos.length ? 1 : 0);
})().catch(e => { console.error(e); process.exit(1); });

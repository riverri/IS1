// Pruebas de extremo a extremo en un navegador real: lo que hace un jugador de principio a fin, con la aplicación
// arrancada de verdad (servidor, base de datos, plantillas y JavaScript a la vez). Las pruebas de MockMvc comprueban
// cada pieza por separado; estas, que todas juntas funcionan como las ve el usuario.
//
// La ejecuta la CI con la aplicación arrancada con los datos de demostración; en local:
//   cd a11y && npm install && node e2e.js   (con la aplicación en http://localhost:8080, o la que diga BASE_URL)
//
// Recorrido: registrarse y entrar; apostar una simple y ver el saldo; cancelarla y recuperar el importe; combinar dos
// partidos en el boleto; el creador pone el resultado y la apuesta se cobra; cerrar sesión.
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

(async () => {
    const navegador = await chromium.launch();
    const jugador = await (await navegador.newContext()).newPage();
    jugador.on('dialog', d => d.accept()); // «¿Cancelar la apuesta?»

    // 1. Registrarse y entrar
    const email = `e2e-${Date.now()}@ucm.es`;
    await jugador.goto(BASE + '/registro');
    await jugador.fill('#nombre', 'Prueba E2E');
    await jugador.fill('#email', email);
    await jugador.fill('#password', 'clave-e2e-123');
    await jugador.fill('#confirmacion', 'clave-e2e-123');
    await Promise.all([jugador.waitForNavigation(), jugador.click('main button[type=submit]')]);
    comprobar(jugador.url().includes('/login'), 'registrarse lleva a entrar', jugador.url());
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

    // 6. Cerrar sesión
    await Promise.all([jugador.waitForNavigation(), jugador.click('form[action$="/logout"] button')]);
    comprobar((await jugador.textContent('main')).includes('Has cerrado sesión'), 'cerrar sesión lo dice', jugador.url());

    await navegador.close();
    console.log(fallos.length ? `\n${fallos.length} fallos de extremo a extremo` : '\nTodo el recorrido funciona');
    process.exit(fallos.length ? 1 : 0);
})().catch(e => { console.error(e); process.exit(1); });

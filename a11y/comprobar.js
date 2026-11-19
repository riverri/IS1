// Comprobación de accesibilidad en un navegador real (HU-97 a HU-101), la que no se puede hacer con MockMvc.
// La ejecuta la CI con la aplicación arrancada; en local:  cd a11y && npm install && node comprobar.js
// (con la aplicación en http://localhost:8080, o la dirección que diga BASE_URL).
//
// - axe (reglas de WCAG 2.1 A y AA) en las páginas principales, con colores claros y oscuros (CA-99.1 y el resto);
// - con el teclado: saltar al contenido, foco siempre visible y hacer una apuesta sin ratón (CA-97.1 a CA-97.3);
// - sin desplazamiento lateral con la página ampliada al 200 % y en un móvil de 320 px (CA-99.2 y CA-99.3).
const { chromium } = require('playwright');
const { default: AxeBuilder } = require('@axe-core/playwright');

const BASE = process.env.BASE_URL || 'http://localhost:8080';
const USUARIO = ['usuario@apuestas.es', process.env.USUARIO_PASSWORD || 'usuario123'];
const CREADOR = ['creador@apuestas.es', process.env.CREADOR_PASSWORD || 'creador123'];

const fallos = [];
const fallo = (que, detalle) => { fallos.push(`${que}: ${detalle}`); console.log(`  ✗ ${que}: ${detalle}`); };
const bien = (que) => console.log(`  ✓ ${que}`);

async function entrar(pagina, [email, password]) {
    await pagina.goto(BASE + '/login');
    await pagina.fill('input[name=email]', email);
    await pagina.fill('input[name=password]', password);
    await Promise.all([pagina.waitForNavigation(), pagina.click('main button[type=submit]')]);
}

/** Si no hay partidos, el creador da de alta dos para que haya cuotas y una página de apostar que revisar. */
async function prepararPartidos(navegador) {
    const pagina = await (await navegador.newContext()).newPage();
    await pagina.goto(BASE + '/eventos');
    if (await pagina.$('.partido')) {
        return;
    }
    await entrar(pagina, CREADOR);
    const parejas = [['Getafe CF', 'Sevilla FC'], ['Villarreal CF', 'Valencia CF']];
    for (let i = 0; i < parejas.length; i++) {
        await pagina.goto(BASE + '/gestion/eventos/nuevo');
        const liga = await pagina.$$eval('#competicionId option', os => (os.find(o => o.textContent.includes('LaLiga')) || os[1]).value);
        await pagina.selectOption('#competicionId', liga);
        const ids = await pagina.$$eval('#localId option', (os, nombres) =>
            nombres.map(n => (os.find(o => o.textContent.trim() === n) || {}).value), parejas[i]);
        await pagina.selectOption('#localId', ids[0]);
        await pagina.selectOption('#visitanteId', ids[1]);
        const dia = new Date(Date.now() + (2 + i) * 86400000).toISOString().slice(0, 10);
        await pagina.fill('#fechaHora', `${dia}T21:00`);
        await Promise.all([pagina.waitForNavigation(), pagina.click('main form button[type=submit]')]);
    }
}

async function contexto(navegador, { quien, oscuro, ancho = 1280, alto = 900 } = {}) {
    const ctx = await navegador.newContext({ viewport: { width: ancho, height: alto } });
    if (oscuro) {
        await ctx.addCookies([{ name: 'tema', value: 'oscuro', url: BASE }]);
    }
    const pagina = await ctx.newPage();
    if (quien) {
        await entrar(pagina, quien);
    }
    return pagina;
}

/** axe con las reglas de WCAG 2.1 A y AA. */
async function revisarConAxe(navegador, paginas, quien, oscuro) {
    const pagina = await contexto(navegador, { quien, oscuro });
    for (const ruta of paginas) {
        await pagina.goto(BASE + ruta);
        const { violations } = await new AxeBuilder({ page: pagina })
            .withTags(['wcag2a', 'wcag2aa', 'wcag21a', 'wcag21aa']).analyze();
        const nombre = `axe ${ruta} (${oscuro ? 'oscuro' : 'claro'})`;
        if (violations.length === 0) {
            bien(nombre);
        }
        for (const v of violations) {
            fallo(nombre, `${v.id} (${v.impact}) en ${v.nodes.length} sitios, p. ej. ${v.nodes[0].target.join(' ')}: ${v.help}`);
        }
    }
    await pagina.context().close();
}

/** CA-99.2 y CA-99.3: sin desplazamiento lateral. */
async function revisarAnchos(navegador, paginas, quien) {
    for (const [ancho, que] of [[640, 'ampliada al 200 %'], [320, 'a 320 px']]) {
        const pagina = await contexto(navegador, { quien, ancho, alto: 700 });
        for (const ruta of paginas) {
            await pagina.goto(BASE + ruta);
            const sobra = await pagina.evaluate(() => document.documentElement.scrollWidth - document.documentElement.clientWidth);
            sobra > 1 ? fallo(`${ruta} ${que}`, `${sobra} px de desplazamiento lateral`) : bien(`${ruta} ${que}`);
        }
        await pagina.context().close();
    }
}

/** Lo que tiene el foco y si se ve (contorno o sombra). */
const foco = (pagina) => pagina.evaluate(() => {
    const e = document.activeElement;
    if (!e || e === document.body) {
        return null;
    }
    const s = getComputedStyle(e);
    const contorno = s.outlineStyle !== 'none' && parseFloat(s.outlineWidth) > 0;
    const sombra = s.boxShadow && s.boxShadow !== 'none';
    // Las opciones de apuesta esconden la casilla y marcan su etiqueta (.opcion:has(input:focus-visible))
    const etiqueta = e.closest('label');
    const visibleEnEtiqueta = etiqueta && getComputedStyle(etiqueta).outlineStyle !== 'none';
    return { texto: (e.innerText || e.value || e.name || e.tagName).trim().slice(0, 40), tag: e.tagName,
        visible: contorno || sombra || !!visibleEnEtiqueta, enMain: !!e.closest('main'), clase: e.className };
});

async function revisarTeclado(navegador) {
    // CA-97.3: el primer tabulador lleva a «Saltar al contenido» y de ahí al contenido
    const pagina = await contexto(navegador, { quien: USUARIO });
    await pagina.goto(BASE + '/eventos');
    await pagina.keyboard.press('Tab');
    let f = await foco(pagina);
    f && String(f.clase).includes('saltar-contenido') ? bien('primer tabulador: Saltar al contenido')
        : fallo('primer tabulador', `va a ${f && f.texto}`);
    await pagina.keyboard.press('Enter');
    await pagina.keyboard.press('Tab');
    f = await foco(pagina);
    f && f.enMain ? bien('tras saltar, el foco está en el contenido') : fallo('saltar al contenido', `el foco va a ${f && f.texto}`);

    // CA-97.1: en los 40 primeros pasos del tabulador, el foco siempre se ve
    await pagina.goto(BASE + '/eventos');
    let invisibles = [];
    for (let i = 0; i < 40; i++) {
        await pagina.keyboard.press('Tab');
        f = await foco(pagina);
        if (f && !f.visible) {
            invisibles.push(`${f.tag} «${f.texto}»`);
        }
    }
    invisibles.length ? fallo('foco visible en /eventos', invisibles.join(', ')) : bien('foco visible en /eventos');

    // CA-97.2: apostar solo con el teclado
    await pagina.goto(BASE + '/eventos');
    const enlace = await pagina.$$eval('a', as => as.map(a => a.getAttribute('href')).find(h => /\/eventos\/\d+\/apostar$/.test(h || '')));
    await pagina.goto(BASE + enlace);
    let llegado = false;
    for (let i = 0; i < 60 && !llegado; i++) {
        await pagina.keyboard.press('Tab');
        llegado = await pagina.evaluate(() => document.activeElement.matches('input[type=radio]'));
    }
    await pagina.keyboard.press('Space');
    for (let i = 0; i < 30; i++) {
        await pagina.keyboard.press('Tab');
        if (await pagina.evaluate(() => document.activeElement.id === 'importe')) {
            break;
        }
    }
    await pagina.keyboard.type('10');
    await Promise.all([pagina.waitForNavigation(), pagina.keyboard.press('Enter')]);
    const hecha = await pagina.$('.alerta-exito');
    hecha ? bien('apuesta hecha solo con el teclado') : fallo('apostar con el teclado', `no se hizo; página ${pagina.url()}`);
    await pagina.context().close();
}

(async () => {
    const navegador = await chromium.launch();
    await prepararPartidos(navegador);
    const publicas = ['/', '/eventos', '/login', '/registro', '/ranking', '/mercados', '/equipos'];
    const conSesion = ['/apuestas', '/cuenta', '/boleto', '/ligas', '/quinielas', '/predicciones', '/cripto', '/notificaciones'];
    const anon = await contexto(navegador);
    await anon.goto(BASE + '/eventos');
    const partido = await anon.$$eval('a', as => as.map(a => a.getAttribute('href')).find(h => /\/eventos\/\d+\/apostar$/.test(h || '')));
    await anon.context().close();
    if (partido) {
        conSesion.push(partido);
    }

    for (const oscuro of [false, true]) {
        await revisarConAxe(navegador, publicas, null, oscuro);
        await revisarConAxe(navegador, conSesion, USUARIO, oscuro);
        await revisarConAxe(navegador, ['/gestion'], CREADOR, oscuro);
    }
    await revisarAnchos(navegador, publicas, null);
    await revisarAnchos(navegador, conSesion, USUARIO);
    await revisarTeclado(navegador);
    await navegador.close();

    console.log(fallos.length ? `\n${fallos.length} problemas de accesibilidad` : '\nSin problemas de accesibilidad');
    process.exit(fallos.length ? 1 : 0);
})().catch(e => { console.error(e); process.exit(1); });

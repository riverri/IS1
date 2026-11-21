// Comportamiento común de todas las páginas, en un archivo aparte y no dentro del HTML: así la política de
// seguridad (CSP) puede prohibir cualquier script escrito dentro de una página (script-src 'self'), y si alguien
// consiguiera colar HTML con un <script> o un onclick, el navegador no lo ejecutaría.

// Confirmación antes de enviar: el formulario o el botón llevan el texto en data-confirmar. Si se cancela, no se envía.
document.addEventListener('submit', function (evento) {
    var boton = evento.submitter;
    var texto = (boton && boton.dataset.confirmar) || evento.target.dataset.confirmar;
    if (texto && !window.confirm(texto)) {
        evento.preventDefault();
    }
});

// Escudo de un equipo: si su imagen no carga, se quita y se ve el escudo dibujado que hay debajo.
function quitarEscudoRoto(img) {
    var escudo = img.closest('.escudo');
    if (escudo) {
        escudo.classList.remove('con-imagen');
    }
    img.remove();
}
// El evento error de una imagen no sube por el documento: se escucha en la fase de captura
document.addEventListener('error', function (evento) {
    if (evento.target.matches && evento.target.matches('.escudo img')) {
        quitarEscudoRoto(evento.target);
    }
}, true);
// Las que ya fallaron antes de cargar este archivo
document.addEventListener('DOMContentLoaded', function () {
    document.querySelectorAll('.escudo img').forEach(function (img) {
        if (img.complete && img.naturalWidth === 0) {
            quitarEscudoRoto(img);
        }
    });
});

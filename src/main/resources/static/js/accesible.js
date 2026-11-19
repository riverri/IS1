// Accesibilidad (HU-98, CA-98.4): cada mensaje de error de un formulario queda asociado a su campo,
// para que el lector de pantalla lo lea al llegar al campo y lo marque como no válido.
// Solo añade atributos: si el navegador no ejecuta JavaScript, el formulario funciona igual.
document.addEventListener('DOMContentLoaded', function () {
    var n = 0;
    document.querySelectorAll('.error-campo').forEach(function (error) {
        if (!error.textContent.trim()) {
            return;
        }
        // El campo es el anterior en el formulario (saltando las ayudas y otros mensajes)
        var campo = error.previousElementSibling;
        while (campo && !campo.matches('input, select, textarea, fieldset')) {
            campo = campo.previousElementSibling;
        }
        if (!campo) {
            return;
        }
        if (!error.id) {
            error.id = 'error-campo-' + (++n);
        }
        var previos = campo.getAttribute('aria-describedby');
        campo.setAttribute('aria-describedby', previos ? previos + ' ' + error.id : error.id);
        campo.setAttribute('aria-invalid', 'true');
    });
});

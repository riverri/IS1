(function () {
    const formato = new Intl.NumberFormat('es-ES', {minimumFractionDigits: 2, maximumFractionDigits: 2});
    document.querySelectorAll('.mercado form').forEach(function (form) {
        const importe = form.querySelector('.importe');
        const salida = form.querySelector('.ganancia-calculada');
        const cuotaVista = form.querySelector('.cuota-vista');
        function actualizar() {
            const elegido = form.querySelector('input[name="candidato"]:checked');
            cuotaVista.value = elegido ? elegido.dataset.cuota : '';
            if (!importe || !salida) { return; }
            const cantidad = parseFloat(importe.value);
            if (!elegido || !(cantidad > 0)) { salida.textContent = '–'; return; }
            const ganancia = Math.floor(Math.round(cantidad * 100) * Math.round(parseFloat(elegido.dataset.cuota) * 100) / 100) / 100; // en céntimos enteros, como el servidor: con decimales 1 × 2,01 daba 2,00
            salida.textContent = formato.format(ganancia) + ' monedas';
        }
        form.querySelectorAll('input[name="candidato"]').forEach(r => r.addEventListener('change', actualizar));
        if (importe) { importe.addEventListener('input', actualizar); }
        actualizar();
    });
})();

(function () {
    const importe = document.getElementById('importe');
    const salida = document.getElementById('ganancia');
    const formato = new Intl.NumberFormat('es-ES', {minimumFractionDigits: 2, maximumFractionDigits: 2});
    function actualizar() {
        const elegido = document.querySelector('input[name="opcion"]:checked');
        document.getElementById('cuota-vista').value = elegido ? elegido.dataset.cuota : '';
        const cantidad = parseFloat(importe.value);
        if (!elegido || !(cantidad > 0)) { salida.textContent = '–'; return; }
        const ganancia = Math.floor(Math.round(cantidad * 100) * Math.round(parseFloat(elegido.dataset.cuota) * 100) / 100) / 100; // en céntimos enteros, como el servidor: con decimales 1 × 2,01 daba 2,00
        salida.textContent = formato.format(ganancia) + ' monedas';
    }
    document.querySelectorAll('input[name="opcion"]').forEach(r => r.addEventListener('change', actualizar));
    importe.addEventListener('input', actualizar);
    actualizar();
})();

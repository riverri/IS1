(function () {
    const importe = document.getElementById('importe');
    const cuota = document.getElementById('cuota-total');
    const salida = document.getElementById('ganancia');
    if (!importe || !cuota) { return; }
    const formato = new Intl.NumberFormat('es-ES', {minimumFractionDigits: 2, maximumFractionDigits: 2});
    function actualizar() {
        const cantidad = parseFloat(importe.value);
        if (!(cantidad > 0)) { salida.textContent = '–'; return; }
        const ganancia = Math.floor(Math.round(cantidad * 100) * Math.round(parseFloat(cuota.dataset.cuota) * 100) / 100) / 100; // en céntimos enteros, como el servidor: con decimales 1 × 2,01 daba 2,00
        salida.textContent = formato.format(ganancia) + ' monedas';
    }
    importe.addEventListener('input', actualizar);
    actualizar();
})();

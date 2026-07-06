// promo.js — Promo apertura: tejido gratis
// Debe cargarse ANTES que carrito-compartido.js en toda plantilla que use PROMO o promoActiva
// Función global para dashboard (inicializada en Fase 2)
window.initDashboardCharts = window.initDashboardCharts || function() {};

var PROMO = {
    active: true,
    minTotal: 50000,
    name: '\uD83C\uDF81 Tejido de regalo',
    image: 'https://i.imgur.com/sBLmBfyh.jpg',
    emoji: '\uD83E\uDDF6',
    endDate: new Date(2026, 6, 5, 23, 59, 0)
};

function promoActiva() {
    if (!PROMO.active) return false;
    var ahora = new Date();
    var start = new Date(2026, 6, 3, 0, 0, 0);
    return ahora >= start && ahora <= PROMO.endDate;
}

function actualizarCountdown() {
    var ahora = new Date();
    var end = new Date(PROMO.endDate);
    var diff = end - ahora;
    var el = document.getElementById('countdown');
    if (!el) return;
    if (diff <= 0 || !promoActiva()) { el.style.display = 'none'; return; }
    el.style.display = '';
    var seg = Math.floor(diff / 1000) % 60;
    var min = Math.floor(diff / (1000 * 60)) % 60;
    var hor = Math.floor(diff / (1000 * 60 * 60)) % 24;
    var dia = Math.floor(diff / (1000 * 60 * 60 * 24));
    if (dia > 0) {
        el.textContent = '\u231B ' + dia + 'd ' + hor + 'h ' + min + 'm';
        el.className = 'promo-countdown';
    } else if (hor > 0) {
        el.textContent = '\u231B ' + hor + 'h ' + min + 'm ' + seg + 's';
        el.className = 'promo-countdown urgent';
    } else {
        el.textContent = '\u231B ' + min + 'm ' + seg + 's';
        el.className = 'promo-countdown urgent';
    }
}

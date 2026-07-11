// promo.js — Promo: 20% OFF en Natura CREMA y PERFUME
// Debe cargarse ANTES que carrito-compartido.js en toda plantilla que use PROMO o funciones promo
window.initDashboardCharts = window.initDashboardCharts || function() {};

var PROMO = {
    active: true,
    discountPercent: 20,
    targetCategory: 'NATURA',
    targetSubcategories: ['CREMA', 'PERFUME'],
    name: '20% OFF Natura',
    image: 'https://i.imgur.com/sBLmBfyh.jpg',
    emoji: '\uD83D\uDC84',
    endDate: new Date(2026, 6, 13, 23, 59, 0)
};

function esPromoAplicable(category, subCategory) {
    if (!PROMO.active) return false;
    return category === PROMO.targetCategory
        && PROMO.targetSubcategories.indexOf(subCategory) !== -1;
}

function precioConDescuento(precioOriginal) {
    return precioOriginal * (1 - PROMO.discountPercent / 100);
}

function promoActiva() {
    if (!PROMO.active) return false;
    var ahora = new Date();
    var start = new Date(2026, 6, 11, 0, 0, 0);
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

function aplicarPromoEnCards() {
    if (!promoActiva()) return;
    var cards = document.querySelectorAll('.producto-card');
    cards.forEach(function(card) {
        var btn = card.querySelector('.btn-comprar');
        var brandEl = card.querySelector('.card-brand');
        var priceEl = card.querySelector('.card-price');
        if (!btn || !brandEl || !priceEl) return;
        var category = btn.dataset.category;
        var sub = btn.dataset.sub;
        if (!esPromoAplicable(category, sub)) return;
        if (card.querySelector('.card-promo-badge')) return;
        var originalPrice = parseFloat(btn.dataset.price.replace(',', '.'));
        if (isNaN(originalPrice)) return;
        var discounted = precioConDescuento(originalPrice);
        var badge = document.createElement('div');
        badge.className = 'card-promo-badge';
        badge.textContent = '-' + PROMO.discountPercent + '%';
        var imgWrap = card.querySelector('.card-img') || card;
        imgWrap.style.position = 'relative';
        imgWrap.appendChild(badge);
        priceEl.innerHTML = '<span class="price-original">$' + originalPrice.toFixed(2) + '</span> <span class="price-discount">$' + discounted.toFixed(2) + '</span>';
    });
}

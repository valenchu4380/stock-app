var PROMO = {
    promos: [],
    loaded: false
};

function initPromos() {
    return fetch('/productos/promos/api/activas')
        .then(function(r) { return r.json(); })
        .then(function(data) {
            PROMO.promos = data || [];
            PROMO.loaded = true;
            aplicarPromoEnCards();
            return PROMO.promos;
        })
        .catch(function() {
            PROMO.promos = [];
            PROMO.loaded = true;
        });
}

function encontrarPromo(category, sub) {
    for (var i = 0; i < PROMO.promos.length; i++) {
        var p = PROMO.promos[i];
        var targets = p.targetValor.split(',');
        if (p.tipoTarget === 'CATEGORIA' && targets.indexOf(category) !== -1) return p;
        if (p.tipoTarget === 'SUBCATEGORIA' && sub) {
            for (var j = 0; j < targets.length; j++) {
                var pair = targets[j].split(':');
                if (pair.length === 2) {
                    if (pair[0].trim() === category && pair[1].trim() === sub) return p;
                } else if (targets[j].trim() === sub) {
                    return p;
                }
            }
        }
    }
    return null;
}

function esPromoAplicable(category, sub) {
    return encontrarPromo(category, sub) !== null;
}

function precioConDescuento(precioOriginal, category, sub) {
    var promo = encontrarPromo(category, sub);
    if (promo) return precioOriginal * (1 - promo.descuento / 100);
    return precioOriginal;
}

function obtenerDescuentoItem(category, sub) {
    var promo = encontrarPromo(category, sub);
    return promo ? promo.descuento : 0;
}

function promoActiva() {
    return PROMO.promos.length > 0;
}

function actualizarCountdown() {
    var el = document.getElementById('countdown');
    if (!el) return;
    if (!promoActiva()) { el.style.display = 'none'; return; }
    var fechas = [];
    for (var i = 0; i < PROMO.promos.length; i++) {
        if (PROMO.promos[i].fechaFin) fechas.push(new Date(PROMO.promos[i].fechaFin));
    }
    if (fechas.length === 0) { el.style.display = 'none'; return; }
    var endDate = new Date(Math.min.apply(null, fechas));
    var ahora = new Date();
    var diff = endDate - ahora;
    if (diff <= 0) { el.style.display = 'none'; return; }
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
        var promo = encontrarPromo(category, sub);
        if (!promo) return;
        if (card.querySelector('.card-promo-badge')) return;
        var originalPrice = parseFloat(btn.dataset.price.replace(',', '.'));
        if (isNaN(originalPrice)) return;
        var discounted = originalPrice * (1 - promo.descuento / 100);
        var badge = document.createElement('div');
        badge.className = 'card-promo-badge';
        badge.textContent = '-' + promo.descuento + '%';
        var imgWrap = card.querySelector('.card-img') || card;
        imgWrap.style.position = 'relative';
        imgWrap.appendChild(badge);
        priceEl.innerHTML = '<span class="price-original">$' + originalPrice.toFixed(2) + '</span> <span class="price-discount">$' + discounted.toFixed(2) + '</span>';
    });
}

initPromos();

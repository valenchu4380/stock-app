// detalle.js — Detalle del producto: promo banner, cantidad, carrito
// Debe cargarse DESPUÉS de promo.js y carrito-compartido.js, ANTES de toast.js
// Dependencias globales: PROMO, promoActiva, actualizarCountdown (promo.js),
//   getCarrito, guardarCarrito (carrito-compartido.js), showToast (toast.js)

function actualizarPromoBanner(precioProducto, cantidad) {
    const banner = document.getElementById('giftBanner');
    if (!banner) return;
    if (!promoActiva()) { banner.style.display = 'none'; return; }

    const items = (function() {
        try { return JSON.parse(localStorage.getItem('carrito')) || []; } catch { return []; }
    })();
    const totalCarrito = items.reduce(function(s, i) { return s + i.price * i.cantidad; }, 0);
    const totalConProducto = totalCarrito + (precioProducto || 0) * (cantidad || 1);
    const faltan = PROMO.minTotal - totalConProducto;

    const imgEl = document.getElementById('giftImg');
    if (PROMO.image) {
        imgEl.innerHTML = '<img src="' + PROMO.image + '" alt="' + PROMO.name + '">';
    } else {
        imgEl.innerHTML = '<span>' + PROMO.emoji + '</span>';
    }

    const titleEl = document.getElementById('giftBannerTitle');
    const subEl = document.getElementById('giftBannerSub');
    const progressEl = document.getElementById('giftBannerProgress');

    if (totalConProducto >= PROMO.minTotal) {
        titleEl.textContent = '\uD83C\uDF81 \u00A1' + PROMO.name + ' asegurado!';
        subEl.textContent = 'Complet\u00E1 el pedido y te lo llev\u00E1s gratis.';
        progressEl.textContent = '\u2705 LO TEN\u00C9S';
        progressEl.style.background = '#E8F5E9';
        progressEl.style.color = '#166534';
    } else if (totalCarrito >= PROMO.minTotal) {
        titleEl.textContent = '\uD83C\uDF81 \u00A1Ya ten\u00E9s tu ' + PROMO.name + '!';
        subEl.textContent = 'Agreg\u00E1 este producto y complet\u00E1 el pedido.';
        progressEl.textContent = '\u2705 LO TEN\u00C9S';
        progressEl.style.background = '#E8F5E9';
        progressEl.style.color = '#166534';
    } else if (faltan > 0 && faltan <= 15000) {
        titleEl.textContent = '\uD83C\uDF81 \u00A1Te faltan $' + faltan.toFixed(0) + ' para tu ' + PROMO.name + '!';
        subEl.textContent = 'Agreg\u00E1 este producto y acercate al regalo.';
        progressEl.textContent = '\uD83D\uDCB0 $' + faltan.toFixed(0);
        progressEl.style.background = '#FFF3CD';
        progressEl.style.color = '#856404';
    } else {
        titleEl.textContent = '\uD83C\uDF81 ' + PROMO.name + ' en compras mayores a $' + PROMO.minTotal.toLocaleString();
        subEl.textContent = 'Sum\u00E1 productos a tu carrito y llevate un tejido GRATIS.';
        progressEl.textContent = '\uD83C\uDFAF $' + PROMO.minTotal.toLocaleString();
        progressEl.style.background = '#E8F5E9';
        progressEl.style.color = '#166534';
    }

    banner.style.display = 'flex';
}

function cambiarQty(delta) {
    const span = document.getElementById('detalleQty');
    let qty = parseInt(span.textContent) + delta;
    if (qty < 1) qty = 1;
    const max = parseInt(document.querySelector('.btn-carrito').dataset.stock);
    if (qty > max) qty = max;
    span.textContent = qty;
    actualizarPromoBanner(
        parseFloat(document.querySelector('.btn-carrito').dataset.price),
        qty
    );
}

function agregarAlCarrito(btn) {
    const name = btn.dataset.name;
    const sub = btn.dataset.sub;
    const price = parseFloat(btn.dataset.price);
    const imagen = btn.dataset.imagen;
    const stock = parseInt(btn.dataset.stock);
    const cantidad = parseInt(document.getElementById('detalleQty').textContent);
    const items = getCarrito();
    const existente = items.find(function(i) { return i.name === name && i.subCategory === sub; });
    if (existente) {
        const nuevaCantidad = existente.cantidad + cantidad;
        if (nuevaCantidad > stock) {
            showToast('Solo hay ' + stock + ' unidades disponibles. Ya ten\u00E9s ' + existente.cantidad + ' en el carrito.', 'error');
            return;
        }
        existente.cantidad = nuevaCantidad;
    } else {
        if (cantidad > stock) {
            showToast('Solo hay ' + stock + ' unidades disponibles.', 'error');
            return;
        }
        items.push({ name: name, subCategory: sub, price: price, imagen: imagen, cantidad: cantidad, stock: stock });
    }
    guardarCarrito(items);
    document.getElementById('detalleQty').textContent = '1';
    const old = btn.innerHTML;
    btn.innerHTML = '\u2713 Agregado';
    setTimeout(function() { btn.innerHTML = old; }, 1000);
    actualizarPromoBanner(price, 1);
}

function initDetalle() {
    const btn = document.querySelector('.btn-carrito');
    if (btn) {
        actualizarPromoBanner(
            parseFloat(btn.dataset.price),
            parseInt(document.getElementById('detalleQty').textContent)
        );
    }
    actualizarCountdown();
    setInterval(actualizarCountdown, 1000);
}

document.addEventListener('DOMContentLoaded', initDetalle);

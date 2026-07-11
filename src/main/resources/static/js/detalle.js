// detalle.js — Detalle del producto: promo banner 20% OFF, cantidad, carrito
// Debe cargarse DESPUÉS de promo.js y carrito-compartido.js, ANTES de toast.js
// Dependencias globales: PROMO, esPromoAplicable, precioConDescuento, promoActiva,
//   actualizarCountdown (promo.js),
//   getCarrito, guardarCarrito (carrito-compartido.js), showToast (toast.js)

function actualizarPrecioPrincipal(precioProducto) {
    const priceEl = document.getElementById('detallePrecio');
    const cuotasEl = document.getElementById('detalleCuotas');
    const btn = document.querySelector('.btn-carrito');
    if (!btn || !priceEl) return;
    const category = btn.dataset.category;
    const sub = btn.dataset.sub;
    if (!promoActiva() || !esPromoAplicable(category, sub)) return;
    const descPrice = precioConDescuento(precioProducto);
    priceEl.innerHTML = '<span class="price-original">$' + precioProducto.toFixed(2) + '</span> <span class="price-discount">$' + descPrice.toFixed(2) + '</span>';
    if (cuotasEl) cuotasEl.textContent = 'Hasta 3 cuotas de $' + (descPrice / 3).toFixed(2);
}

function actualizarPromoBanner(precioProducto, cantidad) {
    const banner = document.getElementById('promoBanner');
    if (!banner) return;
    if (!promoActiva()) { banner.style.display = 'none'; return; }
    const btn = document.querySelector('.btn-carrito');
    if (!btn) { banner.style.display = 'none'; return; }
    const category = btn.dataset.category;
    const sub = btn.dataset.sub;
    if (!esPromoAplicable(category, sub)) { banner.style.display = 'none'; return; }
    const descPrice = precioConDescuento(precioProducto);
    const originalEl = document.getElementById('promoOriginalPrice');
    const discountEl = document.getElementById('promoDiscountPrice');
    if (originalEl) originalEl.textContent = '$' + precioProducto.toFixed(2);
    if (discountEl) discountEl.textContent = '$' + descPrice.toFixed(2);
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
    const category = btn.dataset.category;
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
        items.push({ name: name, subCategory: sub, category: category, price: price, imagen: imagen, cantidad: cantidad, stock: stock });
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
        const price = parseFloat(btn.dataset.price);
        const qty = parseInt(document.getElementById('detalleQty').textContent);
        actualizarPrecioPrincipal(price);
        actualizarPromoBanner(price, qty);
    }
    actualizarCountdown();
    setInterval(actualizarCountdown, 1000);
}

document.addEventListener('DOMContentLoaded', initDetalle);

// carrito-compartido.js — Shared cart functions for SobreVivi
// Usado por index.html (overlay)
// Todas las funciones expuestas en window para manejadores onclick inline

// PROMO + promoActiva ahora están en promo.js (debe cargarse ANTES que este archivo)

function getCarrito() {
    try { return JSON.parse(localStorage.getItem('carrito')) || []; } catch { return []; }
}

function guardarCarrito(items) {
    localStorage.setItem('carrito', JSON.stringify(items));
}

function agregarAlCarrito(btn) {
    var name = btn.dataset.name;
    var price = parseFloat(btn.dataset.price);
    var stock = parseInt(btn.dataset.stock) || 999;
    var subCategory = btn.dataset.sub || '';
    var imagen = btn.dataset.imagen || '';
    if (!name || isNaN(price)) return;

    var items = getCarrito();
    var existing = null;
    for (var i = 0; i < items.length; i++) {
        if (items[i].name === name) { existing = items[i]; break; }
    }
    if (existing) {
        var max = existing.stock || 999;
        if (existing.cantidad >= max) {
            if (typeof showToast === 'function') {
                showToast('Solo hay ' + max + ' unidad' + (max === 1 ? '' : 'es') + ' disponible' + (max === 1 ? '' : 's') + ' de ' + name, 'warning');
            }
            return;
        }
        existing.cantidad = existing.cantidad + 1;
    } else {
        items.push({ name: name, price: price, cantidad: 1, stock: stock, subCategory: subCategory, imagen: imagen });
    }
    guardarCarrito(items);
    actualizarContadorGlobal();
    if (typeof showToast === 'function') {
        showToast(name + ' agregado al carrito', 'success');
    }
}

function cambiarCantidad(index, delta) {
    var items = getCarrito();
    if (!items[index]) return;
    var item = items[index];
    var nueva = item.cantidad + delta;
    var max = item.stock;

    if (delta > 0 && max != null && nueva > max) {
        if (typeof showToast === 'function') {
            showToast('Solo hay ' + max + ' unidad' + (max === 1 ? '' : 'es') + ' disponible' + (max === 1 ? '' : 's') + ' de ' + item.name, 'warning');
        }
        return;
    }
    if (max != null) {
        item.cantidad = Math.max(1, Math.min(nueva, max));
    } else {
        item.cantidad = Math.max(1, nueva);
    }
    guardarCarrito(items);
    actualizarContadorGlobal();
}

function eliminarDelCarrito(index) {
    var items = getCarrito();
    items.splice(index, 1);
    guardarCarrito(items);
    actualizarContadorGlobal();
}

function limpiarCarrito() {
    guardarCarrito([]);
    actualizarContadorGlobal();
}

function enviarPedido() {
    var items = getCarrito();
    if (items.length === 0) return;
    var itemsJson = JSON.stringify(items.map(function(i) {
        return { name: i.name, subCategory: i.subCategory, cantidad: i.cantidad, price: i.price };
    }));
    var total = 0;
    for (var i = 0; i < items.length; i++) {
        total += items[i].price * items[i].cantidad;
    }
    var checkoutBtn = document.getElementById('cart-overlay-checkout');
    if (checkoutBtn) {
        checkoutBtn.disabled = true;
        checkoutBtn.textContent = '\u231B Guardando...';
    }
    fetch('/productos/compras/crear', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: 'itemsJson=' + encodeURIComponent(itemsJson) + '&total=' + total
    }).then(function(r) { return r.json(); }).then(function(data) {
        if (data.success) {
            var lines = [];
            for (var i = 0; i < items.length; i++) {
                lines.push('\u2022 ' + items[i].name + ' x' + items[i].cantidad + ' = $' + (items[i].price * items[i].cantidad).toFixed(2));
            }
            var msg = lines.join('\n');
            var giftMsg = '';
            if (typeof promoActiva === 'function' && promoActiva() && total >= PROMO.minTotal) {
                giftMsg = '\n\uD83C\uDF81 + ' + PROMO.name + ' (GRATIS)';
            }
            var totalMsg = '\n\nTotal: $' + total.toFixed(2) + giftMsg;
            guardarCarrito([]);
            actualizarContadorGlobal();
            window.location.href = 'https://wa.me/' + WHATSAPP_NUM + '?text=Hola! Quiero comprar:\n' + encodeURIComponent(msg + totalMsg);
        } else {
            if (typeof showToast === 'function') showToast('Error al guardar: ' + (data.message || 'desconocido'), 'error');
        }
    }).catch(function(e) {
        if (typeof showToast === 'function') showToast('Error al conectar con el servidor: ' + e.message, 'error');
    }).finally(function() {
        if (checkoutBtn) {
            checkoutBtn.disabled = false;
            checkoutBtn.textContent = 'Enviar pedido por WhatsApp';
        }
    });
}

function actualizarContadorGlobal() {
    var items = getCarrito();
    var total = 0;
    for (var i = 0; i < items.length; i++) {
        total += items[i].cantidad;
    }
    var el = document.getElementById('cartCount');
    if (el) el.textContent = total;
}

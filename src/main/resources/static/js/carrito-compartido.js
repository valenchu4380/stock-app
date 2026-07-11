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

function calcularTotalConDescuento(items) {
    var total = 0;
    for (var i = 0; i < items.length; i++) {
        var item = items[i];
        var price = item.price;
        if (typeof esPromoAplicable === 'function' && esPromoAplicable(item.category, item.subCategory)) {
            price = precioConDescuento(price);
        }
        total += price * item.cantidad;
    }
    return total;
}

function agregarAlCarrito(btn) {
    var name = btn.dataset.name;
    var price = parseFloat(btn.dataset.price);
    var stock = parseInt(btn.dataset.stock) || 999;
    var subCategory = btn.dataset.sub || '';
    var category = btn.dataset.category || '';
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
        items.push({ name: name, price: price, cantidad: 1, stock: stock, subCategory: subCategory, category: category, imagen: imagen });
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
        return { name: i.name, subCategory: i.subCategory, category: i.category, cantidad: i.cantidad, price: i.price };
    }));
    var totalConDesc = calcularTotalConDescuento(items);
    var totalSinDesc = 0;
    for (var i = 0; i < items.length; i++) {
        totalSinDesc += items[i].price * items[i].cantidad;
    }
    var checkoutBtn = document.getElementById('cart-overlay-checkout');
    if (checkoutBtn) {
        checkoutBtn.disabled = true;
        checkoutBtn.textContent = '\u231B Guardando...';
    }
    fetch('/productos/compras/crear', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: 'itemsJson=' + encodeURIComponent(itemsJson) + '&total=' + totalConDesc
    }).then(function(r) { return r.json(); }).then(function(data) {
        if (data.success) {
            var lines = [];
            for (var i = 0; i < items.length; i++) {
                var item = items[i];
                var price = item.price;
                var tieneDesc = typeof esPromoAplicable === 'function' && esPromoAplicable(item.category, item.subCategory);
                if (tieneDesc) {
                    var descPrice = precioConDescuento(price);
                    lines.push('\u2022 ' + item.name + ' x' + item.cantidad + ' = $' + (descPrice * item.cantidad).toFixed(2) + ' (-' + PROMO.discountPercent + '% OFF)');
                } else {
                    lines.push('\u2022 ' + item.name + ' x' + item.cantidad + ' = $' + (price * item.cantidad).toFixed(2));
                }
            }
            var msg = lines.join('\n');
            var descMsg = '';
            if (typeof promoActiva === 'function' && promoActiva() && totalConDesc < totalSinDesc) {
                var ahorro = totalSinDesc - totalConDesc;
                descMsg = '\n\n\u2728 Descuento ' + PROMO.discountPercent + '% OFF Natura: -$' + ahorro.toFixed(2);
            }
            var totalMsg = '\n\nTotal: $' + totalConDesc.toFixed(2) + descMsg;
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

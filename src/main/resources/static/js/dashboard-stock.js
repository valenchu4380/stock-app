// dashboard-stock.js — Dashboard: ajuste de stock inline actions
// Dependencias globales: showPrompt, showConfirm, showToast (toast.js)

function ajustarStockDash(btn, direccion) {
    const name = btn.dataset.name;
    const sub = btn.dataset.sub;
    const label = direccion > 0 ? 'AGREGAR' : 'RETIRAR';
    showPrompt('Cantidad a ' + label + ' a "' + name + '":', '1', function(cantidad) {
        if (cantidad === null) return;
        const params = new URLSearchParams();
        params.set('name', name);
        params.set('subCategory', sub);
        params.set('cantidad', direccion > 0 ? cantidad : -cantidad);
        fetch('/productos/ajustar-stock', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: params.toString() + '&csrf_token=' + encodeURIComponent(CSRF_TOKEN)
        })
        .then(function(r) { return r.json(); })
        .then(function(data) {
            if (data.success) {
                document.querySelectorAll('.gestion-stock-valor').forEach(function(el) {
                    const tr = el.closest('tr');
                    if (tr) {
                        const cb = tr.querySelector('[data-name="' + name + '"][data-sub="' + sub + '"]');
                        if (cb) {
                            el.textContent = data.nuevoStock;
                        }
                    }
                });
                showToast('Stock actualizado', 'success');
            } else {
                showToast('Error: ' + data.message, 'error');
            }
        })
        .catch(function(err) { showToast('Error de conexi\u00F3n: ' + err.message, 'error'); });
    });
}

function confirmarEliminacion(btn) {
    var form = btn.closest('.delete-form');
    var name = form.getAttribute('data-name');
    showConfirm('\u00BFEliminar ' + name + '?', function(ok) {
        if (ok) form.submit();
    });
}

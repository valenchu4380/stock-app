// almacen.js — Almacén: totales por fila, filtro y confirmación de borrado
// Dependencias: toast.js (showConfirm)

function recalcularTotal(input) {
    const fila = input.closest('tr');
    const totalCell = fila.querySelector('.almacen-total');
    if (!totalCell) return;
    let total = 0;
    fila.querySelectorAll('.almacen-input').forEach(function(ci) {
        total += parseInt(ci.value, 10) || 0;
    });
    totalCell.textContent = total;
}

function recalcularTodos() {
    document.querySelectorAll('.almacen-input').forEach(recalcularTotal);
}

let totalSortDir = 'asc';

function ordenarPorTotal(th) {
    const tbody = document.querySelector('.almacen-tabla tbody');
    if (!tbody) return;
    totalSortDir = totalSortDir === 'asc' ? 'desc' : 'asc';
    const dir = totalSortDir === 'asc' ? 1 : -1;
    const rows = Array.from(tbody.querySelectorAll('tr'));
    rows.sort(function(a, b) {
        const ta = a.querySelector('.almacen-total');
        const tb = b.querySelector('.almacen-total');
        const va = parseInt((ta ? ta.textContent : '0').trim(), 10) || 0;
        const vb = parseInt((tb ? tb.textContent : '0').trim(), 10) || 0;
        return (va - vb) * dir;
    });
    rows.forEach(function(row) { tbody.appendChild(row); });
    const arrow = th.querySelector('.sort-arrow');
    if (arrow) arrow.textContent = totalSortDir === 'asc' ? '▲' : '▼';
}

function filtrarAlmacen() {
    const input = document.getElementById('almacenSearch');
    const filtro = input.value.toLowerCase();
    document.querySelectorAll('.almacen-tabla tbody tr').forEach(function(row) {
        const texto = row.textContent.toLowerCase();
        row.style.display = texto.includes(filtro) ? '' : 'none';
    });
}

function confirmarEliminarUbicacion(form) {
    const nombre = form.getAttribute('data-nombre');
    showConfirm('¿Eliminar la ubicación "' + nombre + '"? Se quitarán las cantidades asignadas y se recalculará el stock.', function(ok) {
        if (ok) form.submit();
    });
    return false;
}

function confirmarEliminacion(btn) {
    const form = btn.closest('.delete-form');
    const name = form.getAttribute('data-name');
    showConfirm('\u00BFEliminar ' + name + '?', function (ok) {
        if (ok) form.submit();
    });
}

document.addEventListener('DOMContentLoaded', recalcularTodos);

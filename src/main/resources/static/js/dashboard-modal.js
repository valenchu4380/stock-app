// dashboard-modal.js — Dashboard: modal de precios y filtro de tabla
// Sin dependencias externas

function openModal(e) {
    e.preventDefault();
    document.getElementById('modal-precios').classList.add('open');
}

function closeModal() {
    document.getElementById('modal-precios').classList.remove('open');
}

function filtrarTabla() {
    const input = document.getElementById('gestionSearch');
    const filtro = input.value.toLowerCase();
    const rows = document.querySelectorAll('.gestion-tabla tbody tr');
    rows.forEach(function(row) {
        const texto = row.textContent.toLowerCase();
        row.style.display = texto.includes(filtro) ? '' : 'none';
    });
}

let sortState = { col: -1, dir: 'asc' };

function ordenarTabla(col, tipo) {
    const tbody = document.querySelector('.gestion-tabla tbody');
    const rows = Array.from(tbody.querySelectorAll('tr'));

    if (sortState.col === col) {
        sortState.dir = sortState.dir === 'asc' ? 'desc' : 'asc';
    } else {
        sortState.col = col;
        sortState.dir = 'asc';
    }

    const dir = sortState.dir === 'asc' ? 1 : -1;
    const getNum = function(cell) {
        const valorEl = cell.querySelector('.gestion-stock-valor');
        const txt = (valorEl ? valorEl.textContent : cell.textContent) || '';
        return parseFloat(txt.replace(/[$,\s]/g, '')) || 0;
    };
    rows.sort(function(a, b) {
        const ca = a.cells[col];
        const cb = b.cells[col];
        if (!ca || !cb) return 0;
        let va, vb;
        if (tipo === 'num') {
            va = getNum(ca);
            vb = getNum(cb);
        } else {
            va = (ca.textContent || '').toLowerCase();
            vb = (cb.textContent || '').toLowerCase();
        }
        if (va < vb) return -1 * dir;
        if (va > vb) return 1 * dir;
        return 0;
    });

    rows.forEach(function(row) { tbody.appendChild(row); });

    document.querySelectorAll('.gestion-tabla th[data-sort]').forEach(function(th) {
        const arrow = th.querySelector('.sort-arrow');
        if (arrow) {
            arrow.textContent = th.dataset.sort === String(col)
                ? (sortState.dir === 'asc' ? '▲' : '▼')
                : '';
        }
    });
}

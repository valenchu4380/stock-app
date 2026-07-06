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

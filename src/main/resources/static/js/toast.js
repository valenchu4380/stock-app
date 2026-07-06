// toast.js — Toast notifications y dialog functions
// Debe cargarse en toda plantilla que incluya el fragmento toast

function agregarCerrarConEscape(elemento) {
    elemento.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') {
            if (elemento.hasAttribute('hidden') !== false) {
                elemento.style.display = 'none';
            } else {
                elemento.setAttribute('hidden', '');
            }
        }
    });
}

function showToast(message, type) {
    if (!type) type = 'success';
    var container = document.getElementById('toast-container');
    if (!container) return;
    var toast = document.createElement('div');
    toast.className = 'toast toast-' + type;
    toast.textContent = message;
    container.appendChild(toast);
    setTimeout(function() { if (toast.parentNode) toast.parentNode.removeChild(toast); }, 3600);
}

function trapFocus(container) {
    var focusable = container.querySelectorAll('button, input, [tabindex]:not([tabindex="-1"])');
    if (focusable.length === 0) return;
    var first = focusable[0];
    var last = focusable[focusable.length - 1];
    first.focus();
    container.addEventListener('keydown', function handler(e) {
        if (e.key !== 'Tab') return;
        if (e.shiftKey && document.activeElement === first) {
            e.preventDefault();
            last.focus();
        } else if (!e.shiftKey && document.activeElement === last) {
            e.preventDefault();
            first.focus();
        }
    });
}

function showConfirm(message, onConfirm) {
    var overlay = document.getElementById('dialog-overlay');
    var msgEl = document.getElementById('dialog-message');
    var inputArea = document.getElementById('dialog-input-area');
    var input = document.getElementById('dialog-input');
    var btnConfirm = document.getElementById('dialog-btn-confirm');
    var btnCancel = document.getElementById('dialog-btn-cancel');

    inputArea.style.display = 'none';
    msgEl.textContent = message;
    btnConfirm.textContent = 'S\u00ED';
    btnCancel.textContent = 'No';
    overlay.style.display = 'flex';

    trapFocus(overlay);

    function cleanup() {
        overlay.style.display = 'none';
        btnConfirm.onclick = null;
        btnCancel.onclick = null;
    }

    btnConfirm.onclick = function() { cleanup(); if (onConfirm) onConfirm(true); };
    btnCancel.onclick = function() { cleanup(); if (onConfirm) onConfirm(false); };
    agregarCerrarConEscape(overlay);
}

function showPrompt(message, defaultValue, onConfirm) {
    var overlay = document.getElementById('dialog-overlay');
    var msgEl = document.getElementById('dialog-message');
    var inputArea = document.getElementById('dialog-input-area');
    var input = document.getElementById('dialog-input');
    var btnConfirm = document.getElementById('dialog-btn-confirm');
    var btnCancel = document.getElementById('dialog-btn-cancel');

    inputArea.style.display = 'block';
    msgEl.textContent = message;
    input.value = defaultValue || '1';
    input.focus();
    btnConfirm.textContent = 'Aceptar';
    btnCancel.textContent = 'Cancelar';
    overlay.style.display = 'flex';
    trapFocus(overlay);

    function cleanup() {
        overlay.style.display = 'none';
        btnConfirm.onclick = null;
        btnCancel.onclick = null;
        input.onkeydown = null;
    }

    function onOk() {
        var val = parseInt(input.value);
        if (isNaN(val) || val <= 0) {
            showToast('Ingres\u00E1 un n\u00FAmero v\u00E1lido', 'error');
            return;
        }
        cleanup();
        if (onConfirm) onConfirm(val);
    }

    btnConfirm.onclick = onOk;
    btnCancel.onclick = function() { cleanup(); if (onConfirm) onConfirm(null); };
    input.onkeydown = function(e) { if (e.key === 'Enter') onOk(); };
    agregarCerrarConEscape(overlay);
}

(function () {
    const THEME_KEY = 'rememberme-theme';

    window.toggleRememberMeTheme = function () {
        const html = document.documentElement;
        const isDark = html.classList.toggle('dark');
        localStorage.setItem(THEME_KEY, isDark ? 'dark' : 'light');
        actualizarIconosTema(isDark);
    };

    function actualizarIconosTema(isDark) {
        document.querySelectorAll('[data-theme-icon="sun"]').forEach(el => {
            el.classList.toggle('hidden', !isDark);
        });
        document.querySelectorAll('[data-theme-icon="moon"]').forEach(el => {
            el.classList.toggle('hidden', isDark);
        });
        document.querySelectorAll('[data-theme-label]').forEach(el => {
            el.textContent = isDark ? 'Modo Claro' : 'Modo Oscuro';
        });
    }

    document.addEventListener('DOMContentLoaded', function () {
        actualizarIconosTema(document.documentElement.classList.contains('dark'));
    });

    // Gestión de modales
    window.openModal = function (modalId) {
        const modal = document.getElementById(modalId);
        if (!modal) return;
        modal.classList.remove('hidden');
        modal.classList.add('flex');
        document.body.classList.add('overflow-hidden');
    };

    window.closeModal = function (modalId) {
        const modal = document.getElementById(modalId);
        if (!modal) return;
        modal.classList.add('hidden');
        modal.classList.remove('flex');
        document.body.classList.remove('overflow-hidden');
    };

    document.addEventListener('keydown', function (e) {
        if (e.key === 'Escape') {
            document.querySelectorAll('[data-modal="true"].flex').forEach(modal => {
                window.closeModal(modal.id);
            });
        }
    });

    // Rellenado rápido para pruebas en auth.html
    window.rellenarCredencialesDemo = function (correo, contrasena) {
        const inputCorreo = document.getElementById('loginCorreo');
        const inputPass = document.getElementById('loginContrasena');
        if (inputCorreo && inputPass) {
            inputCorreo.value = correo;
            inputPass.value = contrasena;
            inputCorreo.focus();
        }
    };

    // Selector de campos de paciente en registro público
    window.toggleCamposPacienteRegistro = function (rolValor) {
        const bloque = document.getElementById('camposPacienteRegistro');
        if (!bloque) return;
        bloque.classList.toggle('hidden', rolValor !== 'ROL_PACIENTE');
    };

    // Modal de Protocolo de los 5 Correctos / Omisión
    window.abrirModalVerificacionToma = function (btn, rutaBase) {
        const form = document.getElementById('formVerificacionToma');
        if (!form) return;

        const tomaId = btn.dataset.tomaId;
        form.action = rutaBase + '/' + tomaId + '/verificar';

        document.getElementById('vtPaciente').textContent = btn.dataset.paciente || '—';
        document.getElementById('vtMedicamento').textContent = btn.dataset.medicamento || '—';
        document.getElementById('vtDosis').textContent = btn.dataset.dosis || '—';
        document.getElementById('vtVia').textContent = btn.dataset.via || '—';
        document.getElementById('vtHora').textContent = btn.dataset.hora || '—';
        document.getElementById('vtInstrucciones').textContent = btn.dataset.instrucciones || 'Sin instrucciones adicionales.';

        // Alerta de alto riesgo y alergias
        const altoRiesgoBox = document.getElementById('vtAlertaAltoRiesgo');
        if (altoRiesgoBox) {
            altoRiesgoBox.classList.toggle('hidden', btn.dataset.altoRiesgo !== 'true');
        }

        const alergiasBox = document.getElementById('vtAlertaAlergias');
        const alergiasTexto = document.getElementById('vtAlergiasTexto');
        if (alergiasBox && alergiasTexto) {
            const alergias = (btn.dataset.alergias || '').trim();
            alergiasBox.classList.toggle('hidden', alergias.length === 0);
            alergiasTexto.textContent = alergias;
        }

        // Resetear checkboxes y campos
        form.querySelectorAll('input[type="checkbox"]').forEach(cb => cb.checked = false);
        const motivoInput = document.getElementById('vtMotivoOmision');
        if (motivoInput) motivoInput.value = '';
        const notasInput = document.getElementById('vtNotas');
        if (notasInput) notasInput.value = '';

        window.cambiarModoVerificacion('ADMINISTRADO');
        window.openModal('modalVerificacionToma');
    };

    window.cambiarModoVerificacion = function (modo) {
        const estadoInput = document.getElementById('vtEstadoDestino');
        const panel5 = document.getElementById('panelCincoCorrectos');
        const panelOmision = document.getElementById('panelMotivoOmision');
        const btnSubmit = document.getElementById('vtBtnSubmit');
        const tabAdmin = document.getElementById('tabModoAdministrar');
        const tabOmitir = document.getElementById('tabModoOmitir');

        if (!estadoInput) return;
        estadoInput.value = modo;

        const esAdministrar = modo === 'ADMINISTRADO';
        panel5.classList.toggle('hidden', !esAdministrar);
        panelOmision.classList.toggle('hidden', esAdministrar);

        if (tabAdmin && tabOmitir) {
            tabAdmin.className = esAdministrar
                ? 'flex-1 py-2 px-3 rounded-lg text-sm font-semibold bg-teal-700 text-white dark:bg-teal-600 transition'
                : 'flex-1 py-2 px-3 rounded-lg text-sm font-medium text-slate-600 dark:text-slate-300 hover:bg-white dark:hover:bg-slate-700 transition';
            tabOmitir.className = !esAdministrar
                ? 'flex-1 py-2 px-3 rounded-lg text-sm font-semibold bg-red-700 text-white dark:bg-red-600 transition'
                : 'flex-1 py-2 px-3 rounded-lg text-sm font-medium text-slate-600 dark:text-slate-300 hover:bg-white dark:hover:bg-slate-700 transition';
        }

        if (btnSubmit) {
            btnSubmit.textContent = esAdministrar ? 'Confirmar 5 Correctos y Administrar' : 'Registrar Omisión Clínica';
            btnSubmit.className = esAdministrar
                ? 'w-full sm:w-auto px-5 py-2.5 rounded-xl text-sm font-semibold bg-teal-700 hover:bg-teal-800 dark:bg-teal-600 dark:hover:bg-teal-500 text-white transition'
                : 'w-full sm:w-auto px-5 py-2.5 rounded-xl text-sm font-semibold bg-red-700 hover:bg-red-800 dark:bg-red-600 dark:hover:bg-red-500 text-white transition';
        }

        window.validarEnvioVerificacion();
    };

    window.validarEnvioVerificacion = function () {
        const estadoInput = document.getElementById('vtEstadoDestino');
        const btnSubmit = document.getElementById('vtBtnSubmit');
        if (!estadoInput || !btnSubmit) return;

        if (estadoInput.value === 'ADMINISTRADO') {
            const checks = document.querySelectorAll('#panelCincoCorrectos input[type="checkbox"]');
            const todosMarcados = Array.from(checks).every(cb => cb.checked);
            btnSubmit.disabled = !todosMarcados;
            btnSubmit.classList.toggle('opacity-50', !todosMarcados);
            btnSubmit.classList.toggle('cursor-not-allowed', !todosMarcados);
        } else {
            const motivo = (document.getElementById('vtMotivoOmision')?.value || '').trim();
            const valido = motivo.length > 0;
            btnSubmit.disabled = !valido;
            btnSubmit.classList.toggle('opacity-50', !valido);
            btnSubmit.classList.toggle('cursor-not-allowed', !valido);
        }
    };
})();
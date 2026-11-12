'use strict';

/* =========================================================
   UCO · Panel académico (front-end sin dependencias)
   ========================================================= */

const CLAVE_TOKEN = 'uco.token';
const PERIODO_ACTUAL = '2026-1';

const S = {
    token: localStorage.getItem(CLAVE_TOKEN) || '',
    usuario: null,
    vista: 'resumen',
    usuarios: [],
    estudiantes: [],
    notas: [],
    resumen: null,
    perfil: null,
    promedio: null,
    filtros: {
        rol: '',
        estado: '',
        busqueda: '',
        busquedaNotas: '',
        busquedaEstudiantes: ''
    }
};

const VISTAS = {
    ADMIN: [
        { id: 'resumen', titulo: 'Resumen', icono: 'resumen' },
        { id: 'usuarios', titulo: 'Usuarios', icono: 'usuarios' },
        { id: 'notas', titulo: 'Notas', icono: 'notas' },
        { id: 'interno', titulo: 'Consola interna', icono: 'terminal' }
    ],
    PROFESOR: [
        { id: 'resumen', titulo: 'Resumen', icono: 'resumen' },
        { id: 'estudiantes', titulo: 'Mis estudiantes', icono: 'usuarios' },
        { id: 'notas', titulo: 'Notas', icono: 'notas' },
        { id: 'interno', titulo: 'Consola interna', icono: 'terminal' }
    ],
    ESTUDIANTE: [
        { id: 'resumen', titulo: 'Mi resumen', icono: 'resumen' },
        { id: 'notas', titulo: 'Mis notas', icono: 'notas' },
        { id: 'interno', titulo: 'Consola interna', icono: 'terminal' }
    ]
};

const ICONOS = {
    resumen: '<path d="M4 13h6V4H4zM14 20h6v-9h-6zM4 20h6v-4H4zM14 8h6V4h-6z"/>',
    usuarios: '<path d="M16 20v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="3.5"/><path d="M22 20v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.1a4 4 0 0 1 0 7.8"/>',
    notas: '<path d="M6 3h9l4 4v14H6z"/><path d="M14 3v5h5"/><path d="M9 13h6M9 17h6"/>',
    terminal: '<rect x="3" y="4" width="18" height="16" rx="2"/><path d="m7 9 3 3-3 3M13 15h4"/>',
    buscar: '<circle cx="11" cy="11" r="7"/><path d="m20 20-3.5-3.5"/>',
    mas: '<path d="M12 5v14M5 12h14"/>',
    editar: '<path d="M12 20h9"/><path d="M16.5 3.5a2.1 2.1 0 0 1 3 3L7 19l-4 1 1-4z"/>',
    borrar: '<path d="M3 6h18"/><path d="M8 6V4h8v2"/><path d="M19 6l-1 14H6L5 6"/><path d="M10 11v5M14 11v5"/>',
    check: '<path d="m4 12 5 5L20 6"/>',
    equis: '<path d="M6 6l12 12M18 6 6 18"/>',
    salir: '<path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><path d="m16 17 5-5-5-5"/><path d="M21 12H9"/>',
    usuario: '<circle cx="12" cy="8" r="4"/><path d="M4 21v-1a6 6 0 0 1 6-6h4a6 6 0 0 1 6 6v1"/>',
    reloj: '<circle cx="12" cy="12" r="9"/><path d="M12 7v5l3 2"/>',
    alerta: '<path d="M12 3 2 20h20z"/><path d="M12 9v5M12 17h.01"/>',
    copiar: '<rect x="9" y="9" width="12" height="12" rx="2"/><path d="M5 15H4a1 1 0 0 1-1-1V4a1 1 0 0 1 1-1h10a1 1 0 0 1 1 1v1"/>',
    recargar: '<path d="M21 12a9 9 0 1 1-3-6.7"/><path d="M21 3v6h-6"/>',
    libro: '<path d="M4 4h7a3 3 0 0 1 3 3v13a2.5 2.5 0 0 0-2.5-2.5H4z"/><path d="M20 4h-4a3 3 0 0 0-3 3v13a2.5 2.5 0 0 1 2.5-2.5H20z"/>',
    grafico: '<path d="M4 20V10M10 20V4M16 20v-7M22 20H2"/>',
    estrella: '<path d="m12 3 2.8 5.7 6.2.9-4.5 4.4 1.1 6.2-5.6-2.9-5.6 2.9 1.1-6.2L3 9.6l6.2-.9z"/>'
};

/* ---------- Utilidades ---------- */

const $ = (selector, raiz = document) => raiz.querySelector(selector);
const $$ = (selector, raiz = document) => Array.from(raiz.querySelectorAll(selector));

function icono(nombre, clase = '') {
    const trazos = ICONOS[nombre] || '';
    return `<svg viewBox="0 0 24 24" aria-hidden="true" class="${clase}">${trazos}</svg>`;
}

function esc(valor) {
    return String(valor ?? '').replace(/[&<>"']/g, (caracter) => ({
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#39;'
    })[caracter]);
}

function iniciales(nombre) {
    const partes = String(nombre ?? '').trim().split(/\s+/).filter(Boolean);
    if (!partes.length) {
        return '?';
    }
    return (partes[0][0] + (partes.length > 1 ? partes[partes.length - 1][0] : '')).toUpperCase();
}

function fecha(valor, conHora = false) {
    if (!valor) {
        return '—';
    }
    const datos = new Date(valor);
    if (Number.isNaN(datos.getTime())) {
        return '—';
    }
    const opciones = { day: '2-digit', month: 'short', year: 'numeric' };
    if (conHora) {
        opciones.hour = '2-digit';
        opciones.minute = '2-digit';
    }
    return datos.toLocaleString('es-CO', opciones);
}

function numero(valor) {
    if (valor === null || valor === undefined || valor === '') {
        return null;
    }
    const n = Number(valor);
    return Number.isFinite(n) ? n : null;
}

function formatoNota(valor) {
    const n = numero(valor);
    return n === null ? '—' : n.toFixed(1);
}

function claseNota(valor) {
    const n = numero(valor);
    if (n === null) {
        return '';
    }
    if (n >= 4) {
        return 'alta';
    }
    return n >= 3 ? 'media' : 'baja';
}

function promedioDe(notas) {
    if (!notas.length) {
        return null;
    }
    const suma = notas.reduce((total, nota) => total + (numero(nota.valor) ?? 0), 0);
    return suma / notas.length;
}

function etiquetaRol(rol) {
    return { ADMIN: 'Administrador', PROFESOR: 'Profesor', ESTUDIANTE: 'Estudiante' }[rol] || rol;
}

function claseRol(rol) {
    return { ADMIN: 'badge-admin', PROFESOR: 'badge-profesor', ESTUDIANTE: 'badge-estudiante' }[rol] || '';
}

function claseEstado(estado) {
    return {
        ACTIVO: 'badge-activo',
        PENDIENTE: 'badge-pendiente',
        EXPULSADO: 'badge-expulsado',
        RECHAZADO: 'badge-rechazado'
    }[estado] || '';
}

function insigniaEstado(estado) {
    return `<span class="badge ${claseEstado(estado)}"><span class="punto"></span>${esc(estado)}</span>`;
}

function insigniaRol(rol) {
    return `<span class="badge ${claseRol(rol)}">${esc(etiquetaRol(rol))}</span>`;
}

/* ---------- API ---------- */

class ErrorApi extends Error {
    constructor(mensaje, estado, detalles) {
        super(mensaje);
        this.estado = estado;
        this.detalles = detalles || {};
    }
}

async function api(ruta, opciones = {}) {
    const cabeceras = { ...(opciones.headers || {}) };
    if (S.token) {
        cabeceras.Authorization = `Bearer ${S.token}`;
    }
    if (opciones.body && !cabeceras['Content-Type']) {
        cabeceras['Content-Type'] = 'application/json';
    }

    let respuesta;
    try {
        respuesta = await fetch(`/api${ruta}`, { ...opciones, headers: cabeceras });
    } catch (error) {
        throw new ErrorApi('No se pudo contactar el servidor. Verifica que la aplicación esté en marcha.', 0);
    }

    if (respuesta.status === 401 && S.token) {
        cerrarSesion();
        throw new ErrorApi('Tu sesión expiró. Vuelve a iniciar sesión.', 401);
    }

    if (respuesta.status === 204) {
        return null;
    }

    const tipo = respuesta.headers.get('Content-Type') || '';
    const cuerpo = tipo.includes('json') ? await respuesta.json().catch(() => null) : null;

    if (!respuesta.ok) {
        const detalles = cuerpo && cuerpo.errores ? cuerpo.errores : {};
        const mensaje = (cuerpo && (cuerpo.detail || cuerpo.title)) || `Error ${respuesta.status}`;
        throw new ErrorApi(mensaje, respuesta.status, detalles);
    }

    return cuerpo;
}

/* ---------- Toasts ---------- */

function toast(mensaje, tipo = 'info', duracion = 4200) {
    const contenedor = $('#toasts');
    if (!contenedor) {
        return;
    }
    const elemento = document.createElement('div');
    elemento.className = `toast ${tipo}`;
    const simbolo = tipo === 'exito' ? '✓' : tipo === 'error' ? '!' : 'i';
    elemento.innerHTML = `<span class="toast-icono">${simbolo}</span><span class="toast-texto">${esc(mensaje)}</span>`;
    contenedor.appendChild(elemento);

    const salir = () => {
        elemento.classList.add('saliendo');
        setTimeout(() => elemento.remove(), 220);
    };
    elemento.addEventListener('click', salir);
    setTimeout(salir, duracion);
}

/* ---------- Modales ---------- */

let modalActual = null;

function abrirModal({ titulo, subtitulo = '', cuerpo = '', pie = '', estrecho = false }) {
    cerrarModal();
    const raiz = $('#modal-raiz');
    raiz.innerHTML = `
        <div class="modal-fondo" role="dialog" aria-modal="true" aria-label="${esc(titulo)}">
            <div class="modal ${estrecho ? 'estrecho' : ''}">
                <header class="modal-cabecera">
                    <div>
                        <h2>${esc(titulo)}</h2>
                        ${subtitulo ? `<p>${esc(subtitulo)}</p>` : ''}
                    </div>
                    <button type="button" class="cerrar" aria-label="Cerrar">×</button>
                </header>
                <div class="modal-cuerpo">${cuerpo}</div>
                ${pie ? `<footer class="modal-pie">${pie}</footer>` : ''}
            </div>
        </div>`;

    const fondo = $('.modal-fondo', raiz);
    fondo.addEventListener('click', (evento) => {
        if (evento.target === fondo) {
            cerrarModal();
        }
    });
    $('.cerrar', raiz).addEventListener('click', cerrarModal);
    modalActual = fondo;
    const primerCampo = $('input, select, textarea, button', $('.modal', raiz));
    if (primerCampo) {
        setTimeout(() => primerCampo.focus(), 40);
    }
    return $('.modal', raiz);
}

function cerrarModal() {
    const raiz = $('#modal-raiz');
    if (raiz) {
        raiz.innerHTML = '';
    }
    modalActual = null;
}

document.addEventListener('keydown', (evento) => {
    if (evento.key === 'Escape' && modalActual) {
        cerrarModal();
    }
});

function confirmar({ titulo, mensaje, textoBoton = 'Confirmar', peligro = false }) {
    return new Promise((resolver) => {
        const modal = abrirModal({
            titulo,
            estrecho: true,
            cuerpo: `<p style="font-size:.88rem;color:var(--suave);line-height:1.55">${esc(mensaje)}</p>`,
            pie: `<button type="button" class="boton fantasma" data-accion="no">Cancelar</button>
                  <button type="button" class="boton ${peligro ? 'peligro' : 'primario'}" data-accion="si">${esc(textoBoton)}</button>`
        });
        $('[data-accion="no"]', modal).addEventListener('click', () => {
            cerrarModal();
            resolver(false);
        });
        $('[data-accion="si"]', modal).addEventListener('click', () => {
            cerrarModal();
            resolver(true);
        });
    });
}

/* ---------- Carga global ---------- */

let cargasPendientes = 0;

function cargando(activo) {
    cargasPendientes += activo ? 1 : -1;
    const indicador = $('#cargando-global');
    if (indicador) {
        indicador.classList.toggle('oculto', cargasPendientes <= 0);
    }
    if (cargasPendientes < 0) {
        cargasPendientes = 0;
    }
}

function mensajeDeError(error) {
    if (error instanceof ErrorApi) {
        const detalles = Object.values(error.detalles || {});
        if (detalles.length) {
            return `${error.message} (${detalles.join(' · ')})`;
        }
        return error.message;
    }
    return error && error.message ? error.message : 'Ocurrió un error inesperado';
}

/* ---------- Sesión ---------- */

function mostrarLogin() {
    $('#vista-login').classList.remove('oculto');
    $('#vista-panel').classList.add('oculto');
    $('#panel-registro').classList.add('oculto');
    $('#form-login').closest('.login-panel').classList.remove('oculto');
    $('#registro-exito').classList.add('oculto');
    cerrarModal();
}

function guardarSesion(token, usuario) {
    S.token = token;
    S.usuario = usuario;
    localStorage.setItem(CLAVE_TOKEN, token);
}

function cerrarSesion() {
    S.token = '';
    S.usuario = null;
    S.usuarios = [];
    S.estudiantes = [];
    S.notas = [];
    S.resumen = null;
    S.perfil = null;
    S.promedio = null;
    localStorage.removeItem(CLAVE_TOKEN);
    pintarBarra();
    mostrarLogin();
}

async function restaurarSesion() {
    if (!S.token) {
        mostrarLogin();
        return;
    }
    try {
        const usuario = await api('/auth/me');
        if (usuario.estado !== 'ACTIVO') {
            cerrarSesion();
            toast('Tu cuenta no está activa. Contacta a coordinación.', 'error');
            return;
        }
        S.usuario = usuario;
        entrarPanel();
    } catch (error) {
        cerrarSesion();
        if (error.estado !== 401) {
            toast(mensajeDeError(error), 'error');
        }
    }
}

async function iniciarSesion(evento) {
    evento.preventDefault();
    const correo = $('#login-email').value.trim();
    const clave = $('#login-password').value;
    const error = $('#login-error');
    const boton = $('#boton-login');

    error.hidden = true;
    if (!correo || !clave) {
        error.textContent = 'Escribe tu correo y contraseña.';
        error.hidden = false;
        return;
    }

    boton.disabled = true;
    boton.textContent = 'Ingresando…';
    try {
        const sesion = await api('/auth/login', {
            method: 'POST',
            body: JSON.stringify({ email: correo, password: clave })
        });
        guardarSesion(sesion.token, {
            id: sesion.usuarioId,
            nombreCompleto: sesion.nombreCompleto,
            email: sesion.email,
            rol: sesion.rol,
            estado: sesion.estado
        });
        $('#form-login').reset();
        const completo = await api('/auth/me');
        S.usuario = completo;
        entrarPanel();
        toast(`Bienvenido, ${completo.nombreCompleto.split(' ')[0]}`, 'exito');
    } catch (fallo) {
        error.textContent = fallo.estado === 401
            ? 'Correo o contraseña incorrectos.'
            : mensajeDeError(fallo);
        error.hidden = false;
    } finally {
        boton.disabled = false;
        boton.textContent = 'Iniciar sesión';
    }
}

/* ---------- Registro ---------- */

let rolRegistro = 'estudiante';

function cambiarRolRegistro(rol) {
    rolRegistro = rol;
    $$('#form-registro .segmentado button').forEach((boton) => {
        const activo = boton.dataset.rol === rol;
        boton.classList.toggle('activo', activo);
        boton.setAttribute('aria-selected', String(activo));
    });
    const esProfesor = rol === 'profesor';
    $('#campo-materia').classList.toggle('oculto', !esProfesor);
    $('#campo-codigo').classList.toggle('oculto', esProfesor);
    $('#reg-materia').required = esProfesor;
    $('#reg-codigo').required = !esProfesor;
}

async function registrar(evento) {
    evento.preventDefault();
    const error = $('#registro-error');
    const boton = $('#boton-registro');
    error.hidden = true;

    const datos = {
        nombreCompleto: $('#reg-nombre').value.trim(),
        email: $('#reg-email').value.trim(),
        password: $('#reg-password').value,
        aula: $('#reg-aula').value.trim()
    };

    if (rolRegistro === 'profesor') {
        datos.materia = $('#reg-materia').value.trim();
    } else {
        datos.codigoEstudiantil = $('#reg-codigo').value.trim();
    }

    if (!datos.nombreCompleto || !datos.email || !datos.password || !datos.aula
        || (rolRegistro === 'profesor' ? !datos.materia : !datos.codigoEstudiantil)) {
        error.textContent = 'Completa todos los campos para enviar la solicitud.';
        error.hidden = false;
        return;
    }
    if (datos.password.length < 8) {
        error.textContent = 'La contraseña debe tener al menos 8 caracteres.';
        error.hidden = false;
        return;
    }

    boton.disabled = true;
    boton.textContent = 'Enviando…';
    try {
        await api(`/auth/registro/${rolRegistro}`, {
            method: 'POST',
            body: JSON.stringify(datos)
        });
        $('#form-registro').reset();
        cambiarRolRegistro('estudiante');
        $('#panel-registro').classList.add('oculto');
        $('#registro-exito').classList.remove('oculto');
        $('#registro-exito-texto').textContent =
            `La solicitud de ${datos.nombreCompleto} quedó registrada. Un administrador debe aprobarla antes de que puedas ingresar.`;
    } catch (fallo) {
        error.textContent = mensajeDeError(fallo);
        error.hidden = false;
    } finally {
        boton.disabled = false;
        boton.textContent = 'Enviar solicitud';
    }
}

function irARegistro(mostrar) {
    $('#panel-registro').classList.toggle('oculto', !mostrar);
    $('#form-login').closest('.login-panel').classList.toggle('oculto', mostrar);
    $('#registro-exito').classList.add('oculto');
    const primero = mostrar ? $('#reg-nombre') : $('#login-email');
    if (primero) {
        primero.focus();
    }
}

/* ---------- Estructura del panel ---------- */

function entrarPanel() {
    $('#vista-login').classList.add('oculto');
    $('#vista-panel').classList.remove('oculto');
    document.body.classList.remove('barra-abierta');
    pintarBarra();
    pintarNav();
    irAVista('resumen');
}

function pintarBarra() {
    const usuario = S.usuario;
    $('#barra-nombre').textContent = usuario ? usuario.nombreCompleto : '—';
    $('#barra-rol').textContent = usuario ? etiquetaRol(usuario.rol) : '—';
    $('#avatar-usuario').textContent = usuario ? iniciales(usuario.nombreCompleto) : '?';
    $('#barra-subtitulo').textContent = usuario
        ? { ADMIN: 'Coordinación académica', PROFESOR: 'Gestión de notas', ESTUDIANTE: 'Mi historial' }[usuario.rol]
        : 'Gestión académica';
    $('#pie-conexion').textContent = 'conectado';
}

function pintarNav() {
    const nav = $('#nav');
    if (!S.usuario) {
        nav.innerHTML = '';
        return;
    }
    const vistas = VISTAS[S.usuario.rol] || [];
    nav.innerHTML = vistas.map((vista) => `
        <button type="button" class="nav-item" data-vista="${vista.id}">
            ${icono(vista.icono)}
            <span>${esc(vista.titulo)}</span>
            ${vista.id === 'usuarios' && S.usuario.rol === 'ADMIN' ? '<span class="nav-contador oculto" id="nav-contador">0</span>' : ''}
        </button>`).join('');
}

function actualizarContadorPendientes(total) {
    const contador = $('#nav-contador');
    if (!contador) {
        return;
    }
    const cantidad = Number(total) || 0;
    contador.textContent = cantidad;
    contador.classList.toggle('oculto', cantidad === 0);
}

function irAVista(vista) {
    if (!S.usuario || !(VISTAS[S.usuario.rol] || []).some((v) => v.id === vista)) {
        vista = 'resumen';
    }
    S.vista = vista;
    $$('#nav .nav-item').forEach((boton) => {
        boton.classList.toggle('activo', boton.dataset.vista === vista);
    });
    document.body.classList.remove('barra-abierta');
    renderVista();
}

function definirCabecera(titulo, subtitulo, acciones = '') {
    $('#titulo-vista').textContent = titulo;
    $('#subtitulo-vista').textContent = subtitulo;
    $('#acciones-vista').innerHTML = acciones;
}

function renderVista() {
    if (!S.usuario) {
        return;
    }
    const renderizadores = {
        resumen: renderResumen,
        usuarios: renderUsuarios,
        estudiantes: renderEstudiantes,
        notas: renderNotas,
        interno: renderInterno
    };
    const render = renderizadores[S.vista] || renderResumen;
    Promise.resolve()
        .then(render)
        .catch((error) => {
            $('#contenido').innerHTML = estadoVacio('alerta', 'No se pudo cargar la sección', mensajeDeError(error));
            toast(mensajeDeError(error), 'error');
        });
}

function estadoVacio(iconoNombre, titulo, texto) {
    return `<div class="vacio">
        <div class="vacio-icono">${icono(iconoNombre)}</div>
        <strong>${esc(titulo)}</strong>
        <span>${esc(texto)}</span>
    </div>`;
}

function esqueleto(filas = 5) {
    return `<div class="seccion-cuerpo"><div class="esqueleto">${'<span></span>'.repeat(filas)}</div></div>`;
}

function tarjetaMetrica({ iconoNombre, color = '', valor, etiqueta, detalle = '' }) {
    return `<article class="tarjeta tarjeta-metrica">
        <div class="metrica-icono ${color}">${icono(iconoNombre)}</div>
        <div class="metrica-valor">${esc(valor)}</div>
        <div class="metrica-etiqueta">${esc(etiqueta)}</div>
        ${detalle ? `<div class="metrica-detalle">${esc(detalle)}</div>` : ''}
    </article>`;
}

function botonAccion(accion, etiqueta, { clase = 'fantasma', iconoNombre = '', datos = {}, minimo = true } = {}) {
    const atributos = Object.entries(datos)
        .map(([llave, valor]) => `data-${llave}="${esc(valor)}"`)
        .join(' ');
    return `<button type="button" class="boton ${clase} ${minimo ? 'mini' : ''}" data-accion="${accion}" ${atributos}>
        ${iconoNombre ? icono(iconoNombre) : ''}${esc(etiqueta)}
    </button>`;
}

/* ---------- Resumen (administrador) ---------- */

async function renderResumen() {
    if (S.usuario.rol === 'ADMIN') {
        return renderResumenAdmin();
    }
    if (S.usuario.rol === 'PROFESOR') {
        return renderResumenProfesor();
    }
    return renderResumenEstudiante();
}

async function renderResumenAdmin() {
    definirCabecera('Resumen general', 'Estado de la plataforma en tiempo real',
        `<button type="button" class="boton fantasma" data-accion="refrescar">${icono('recargar')}Actualizar</button>
         <button type="button" class="boton primario" data-accion="nuevo-usuario">${icono('mas')}Nuevo usuario</button>`);

    const contenido = $('#contenido');
    contenido.innerHTML = `<div class="rejilla-tarjetas">${tarjetaMetrica({ iconoNombre: 'usuarios', valor: '…', etiqueta: 'Cargando' })}</div>`;
    cargando(true);

    try {
        const [resumen, pendientes, notas] = await Promise.all([
            api('/admin/resumen'),
            api('/admin/usuarios?estado=PENDIENTE'),
            api('/admin/notas')
        ]);
        S.resumen = resumen;
        S.notas = notas;
        actualizarContadorPendientes(resumen.pendientes);

        const promedio = promedioDe(notas);
        const porMateria = agruparPor(notas, (nota) => nota.materia);

        contenido.innerHTML = `
            <div class="rejilla-tarjetas">
                ${tarjetaMetrica({
                    iconoNombre: 'usuarios', color: 'violeta', valor: resumen.estudiantes,
                    etiqueta: 'Estudiantes', detalle: `${resumen.profesores} profesores · ${resumen.administradores} admin`
                })}
                ${tarjetaMetrica({
                    iconoNombre: 'reloj', color: 'ambar', valor: resumen.pendientes,
                    etiqueta: 'Solicitudes pendientes', detalle: resumen.pendientes ? 'Requieren revisión' : 'Todo al día'
                })}
                ${tarjetaMetrica({
                    iconoNombre: 'check', color: 'verde', valor: resumen.activos,
                    etiqueta: 'Cuentas activas', detalle: `${resumen.expulsados} expulsados · ${resumen.rechazados} rechazados`
                })}
                ${tarjetaMetrica({
                    iconoNombre: 'notas', color: '', valor: resumen.notas,
                    etiqueta: 'Notas registradas', detalle: promedio === null ? 'Sin notas aún' : `Promedio general ${promedio.toFixed(2)}`
                })}
            </div>

            <div class="dos-paneles">
                <section class="seccion">
                    <header class="seccion-cabecera">
                        <div>
                            <h2>Solicitudes pendientes</h2>
                            <p>Aprueba o rechaza los registros nuevos</p>
                        </div>
                        <span class="espaciador"></span>
                        ${pendientes.length ? `<span class="badge badge-pendiente">${pendientes.length} por revisar</span>` : ''}
                    </header>
                    ${pendientes.length ? `<div class="tabla-envoltorio"><table class="tabla">
                        <thead><tr><th>Persona</th><th>Rol</th><th>Aula</th><th>Registro</th><th></th></tr></thead>
                        <tbody>${pendientes.map((usuario) => `
                            <tr>
                                <td>${celdaPersona(usuario)}</td>
                                <td>${insigniaRol(usuario.rol)}</td>
                                <td>${esc(usuario.aula || '—')}</td>
                                <td class="mono">${esc(fecha(usuario.creadoEn))}</td>
                                <td><div class="acciones-fila">
                                    ${botonAccion('aprobar-usuario', 'Aprobar', { clase: 'exito', iconoNombre: 'check', datos: { id: usuario.id } })}
                                    ${botonAccion('rechazar-usuario', 'Rechazar', { clase: 'peligro-suave', iconoNombre: 'equis', datos: { id: usuario.id } })}
                                </div></td>
                            </tr>`).join('')}
                        </tbody>
                    </table></div>` : estadoVacio('check', 'Sin solicitudes pendientes', 'Todas las cuentas registradas ya fueron revisadas.')}
                </section>

                <section class="seccion">
                    <header class="seccion-cabecera">
                        <div>
                            <h2>Promedio por materia</h2>
                            <p>Calculado con las notas registradas</p>
                        </div>
                    </header>
                    <div class="seccion-cuerpo">
                        ${promedio === null
                            ? estadoVacio('grafico', 'Aún no hay notas', 'Cuando los profesores registren notas verás aquí el promedio por materia.')
                            : `<div class="promedio-grande"><strong>${promedio.toFixed(2)}</strong><span>promedio general / 5.0</span></div>
                               <div class="distribucion" style="margin-top:18px">
                                ${Object.entries(porMateria).sort((a, b) => promedioDe(b[1]) - promedioDe(a[1])).slice(0, 6).map(([materia, lista]) => {
                                    const valor = promedioDe(lista) ?? 0;
                                    return `<div class="dist-fila">
                                        <span class="dist-etiqueta" title="${esc(materia)}">${esc(recortar(materia, 14))}</span>
                                        <div class="dist-pista"><div class="dist-barra ${claseNota(valor)}" style="width:${(valor / 5) * 100}%"></div></div>
                                        <span class="dist-valor">${valor.toFixed(1)}</span>
                                    </div>`;
                                }).join('')}
                               </div>`}
                    </div>
                </section>
            </div>

            <section class="seccion">
                <header class="seccion-cabecera">
                    <div>
                        <h2>Últimas notas registradas</h2>
                        <p>Movimientos recientes de los profesores</p>
                    </div>
                    <span class="espaciador"></span>
                    <button type="button" class="boton fantasma pequeno" data-accion="ir-vista" data-vista="notas">Ver todas</button>
                </header>
                ${notas.length ? `<div class="tabla-envoltorio"><table class="tabla">
                    <thead><tr><th>Estudiante</th><th>Materia</th><th>Periodo</th><th>Nota</th><th>Actualizada</th></tr></thead>
                    <tbody>${notas.slice(0, 6).map((nota) => `
                        <tr>
                            <td>${celdaPersona({ nombreCompleto: nota.estudianteNombre, email: nota.estudianteCodigo })}</td>
                            <td>${esc(nota.materia)} <span class="persona-detalle">· ${esc(nota.aula)}</span></td>
                            <td class="mono">${esc(nota.periodo)}</td>
                            <td><span class="nota-valor ${claseNota(nota.valor)}">${formatoNota(nota.valor)}</span></td>
                            <td class="mono">${esc(fecha(nota.actualizadaEn))}</td>
                        </tr>`).join('')}
                    </tbody>
                </table></div>` : estadoVacio('notas', 'Todavía no hay notas', 'Los profesores pueden empezar a calificar desde su panel.')}
            </section>`;
    } finally {
        cargando(false);
    }
}

function celdaPersona(usuario) {
    const nombre = usuario.nombreCompleto || usuario.email || '—';
    const detalle = usuario.email || '';
    return `<div class="celda-persona">
        <span class="avatar-sm">${esc(iniciales(nombre))}</span>
        <div>
            <div class="persona-nombre">${esc(nombre)}</div>
            ${detalle ? `<div class="persona-detalle">${esc(detalle)}</div>` : ''}
        </div>
    </div>`;
}

function recortar(texto, maximo) {
    const valor = String(texto ?? '');
    return valor.length > maximo ? `${valor.slice(0, maximo - 1)}…` : valor;
}

function agruparPor(lista, llave) {
    return lista.reduce((grupos, elemento) => {
        const clave = llave(elemento) || 'Sin clasificar';
        (grupos[clave] = grupos[clave] || []).push(elemento);
        return grupos;
    }, {});
}

/* ---------- Usuarios (administrador) ---------- */

async function renderUsuarios() {
    definirCabecera('Usuarios', 'Aprueba, edita, expulsa o elimina cuentas',
        `<button type="button" class="boton primario" data-accion="nuevo-usuario">${icono('mas')}Nuevo usuario</button>`);

    $('#contenido').innerHTML = `
        <section class="seccion">
            <header class="seccion-cabecera">
                <div class="barra-filtros" style="margin:0;flex:1">
                    <div class="campo-busqueda">
                        ${icono('buscar')}
                        <input id="busqueda-usuarios" type="search" placeholder="Buscar por nombre, correo, aula o código…" value="${esc(S.filtros.busqueda)}">
                    </div>
                    <select id="filtro-rol" class="filtro" aria-label="Filtrar por rol">
                        <option value="">Todos los roles</option>
                        <option value="ESTUDIANTE" ${S.filtros.rol === 'ESTUDIANTE' ? 'selected' : ''}>Estudiantes</option>
                        <option value="PROFESOR" ${S.filtros.rol === 'PROFESOR' ? 'selected' : ''}>Profesores</option>
                        <option value="ADMIN" ${S.filtros.rol === 'ADMIN' ? 'selected' : ''}>Administradores</option>
                    </select>
                </div>
            </header>
            <div class="seccion-cuerpo" style="padding:14px 18px;border-bottom:1px solid var(--borde)">
                <div class="chips" id="chips-estado">
                    ${chipEstado('', 'Todos')}
                    ${chipEstado('PENDIENTE', 'Pendientes')}
                    ${chipEstado('ACTIVO', 'Activos')}
                    ${chipEstado('RECHAZADO', 'Rechazados')}
                    ${chipEstado('EXPULSADO', 'Expulsados')}
                </div>
            </div>
            <div id="tabla-usuarios">${esqueleto()}</div>
        </section>`;

    $('#busqueda-usuarios').addEventListener('input', (evento) => {
        S.filtros.busqueda = evento.target.value;
        pintarTablaUsuarios();
    });
    $('#filtro-rol').addEventListener('change', (evento) => {
        S.filtros.rol = evento.target.value;
        pintarTablaUsuarios();
    });
    $('#chips-estado').addEventListener('click', (evento) => {
        const chip = evento.target.closest('[data-estado]');
        if (!chip) {
            return;
        }
        S.filtros.estado = chip.dataset.estado;
        $$('#chips-estado .chip').forEach((elemento) => {
            elemento.classList.toggle('activo', elemento.dataset.estado === S.filtros.estado);
        });
        pintarTablaUsuarios();
    });

    await cargarUsuarios();
}

function chipEstado(estado, etiqueta) {
    return `<button type="button" class="chip ${S.filtros.estado === estado ? 'activo' : ''}"
        data-estado="${estado}">${etiqueta}</button>`;
}

async function cargarUsuarios() {
    cargando(true);
    try {
        S.usuarios = await api('/admin/usuarios');
        actualizarContadorPendientes(S.usuarios.filter((usuario) => usuario.estado === 'PENDIENTE').length);
        pintarTablaUsuarios();
    } finally {
        cargando(false);
    }
}

function usuariosFiltrados() {
    const busqueda = S.filtros.busqueda.trim().toLowerCase();
    return S.usuarios.filter((usuario) => {
        if (S.filtros.rol && usuario.rol !== S.filtros.rol) {
            return false;
        }
        if (S.filtros.estado && usuario.estado !== S.filtros.estado) {
            return false;
        }
        if (!busqueda) {
            return true;
        }
        return [usuario.nombreCompleto, usuario.email, usuario.aula, usuario.materia, usuario.codigoEstudiantil]
            .some((valor) => String(valor || '').toLowerCase().includes(busqueda));
    });
}

function pintarTablaUsuarios() {
    const contenedor = $('#tabla-usuarios');
    if (!contenedor) {
        return;
    }
    const lista = usuariosFiltrados();
    if (!lista.length) {
        contenedor.innerHTML = estadoVacio('usuarios', 'Sin resultados',
            S.usuarios.length ? 'Ningún usuario coincide con los filtros aplicados.' : 'Todavía no hay usuarios registrados.');
        return;
    }

    contenedor.innerHTML = `<div class="tabla-envoltorio"><table class="tabla">
        <thead>
            <tr>
                <th>Persona</th><th>Rol</th><th>Estado</th><th>Aula</th>
                <th>Materia / Código</th><th>Creado</th><th style="text-align:right">Acciones</th>
            </tr>
        </thead>
        <tbody>${lista.map((usuario) => filaUsuario(usuario)).join('')}</tbody>
    </table></div>
    <div class="pie-tabla">${lista.length} de ${S.usuarios.length} usuario(s)</div>`;
}

function filaUsuario(usuario) {
    const acciones = [];
    if (usuario.estado === 'PENDIENTE') {
        acciones.push(botonAccion('aprobar-usuario', 'Aprobar', { clase: 'exito', iconoNombre: 'check', datos: { id: usuario.id } }));
        acciones.push(botonAccion('rechazar-usuario', 'Rechazar', { clase: 'peligro-suave', datos: { id: usuario.id } }));
    } else if (usuario.estado === 'ACTIVO' && usuario.rol !== 'ADMIN') {
        acciones.push(botonAccion('expulsar-usuario', 'Expulsar', { clase: 'peligro-suave', datos: { id: usuario.id } }));
    } else if (usuario.estado === 'RECHAZADO' || usuario.estado === 'EXPULSADO') {
        acciones.push(botonAccion('reactivar-usuario', 'Reactivar', { clase: 'exito', datos: { id: usuario.id } }));
    }
    acciones.push(botonAccion('editar-usuario', 'Editar', { iconoNombre: 'editar', datos: { id: usuario.id } }));
    if (usuario.rol !== 'ADMIN') {
        acciones.push(botonAccion('eliminar-usuario', '', { clase: 'peligro-suave', iconoNombre: 'borrar', datos: { id: usuario.id } }));
    }

    const academico = usuario.rol === 'ESTUDIANTE'
        ? usuario.codigoEstudiantil
        : usuario.rol === 'PROFESOR' ? usuario.materia : null;

    return `<tr>
        <td>${celdaPersona(usuario)}</td>
        <td>${insigniaRol(usuario.rol)}</td>
        <td>${insigniaEstado(usuario.estado)}</td>
        <td>${esc(usuario.aula || '—')}</td>
        <td>${academico ? `<span class="mono">${esc(academico)}</span>` : '—'}</td>
        <td class="mono">${esc(fecha(usuario.creadoEn))}</td>
        <td><div class="acciones-fila">${acciones.join('')}</div></td>
    </tr>`;
}

function modalNuevoUsuario() {
    const modal = abrirModal({
        titulo: 'Crear usuario',
        subtitulo: 'La cuenta queda lista para iniciar sesión de inmediato',
        cuerpo: `<form id="form-nuevo-usuario" novalidate>
            <label for="nu-nombre">Nombre completo</label>
            <input id="nu-nombre" placeholder="Ana María Pérez" required maxlength="120">

            <label for="nu-email">Correo institucional</label>
            <input id="nu-email" type="email" placeholder="ana.perez@uco.edu.co" required maxlength="160">

            <label for="nu-password">Contraseña temporal</label>
            <input id="nu-password" type="password" placeholder="Mínimo 8 caracteres" minlength="8" required autocomplete="new-password">

            <div class="dos-columnas">
                <div>
                    <label for="nu-rol">Rol</label>
                    <select id="nu-rol">
                        <option value="ESTUDIANTE">Estudiante</option>
                        <option value="PROFESOR">Profesor</option>
                        <option value="ADMIN">Administrador</option>
                    </select>
                </div>
                <div>
                    <label for="nu-estado">Estado inicial</label>
                    <select id="nu-estado">
                        <option value="ACTIVO">Activo</option>
                        <option value="PENDIENTE">Pendiente</option>
                    </select>
                </div>
            </div>

            <div id="nu-academicos"></div>
            <div id="nu-error" class="modal-error" hidden></div>
        </form>`,
        pie: `<button type="button" class="boton fantasma" data-cerrar>Cancelar</button>
              <button type="submit" form="form-nuevo-usuario" class="boton primario" id="nu-guardar">Crear usuario</button>`
    });

    const pintarAcademicos = () => {
        const rol = $('#nu-rol', modal).value;
        const contenedor = $('#nu-academicos', modal);
        if (rol === 'ADMIN') {
            contenedor.innerHTML = '';
            return;
        }
        contenedor.innerHTML = `<div class="dos-columnas">
            <div>
                <label for="nu-aula">Aula</label>
                <input id="nu-aula" placeholder="A-101" required maxlength="80">
            </div>
            <div>
                <label for="nu-${rol === 'ESTUDIANTE' ? 'codigo' : 'materia'}">${rol === 'ESTUDIANTE' ? 'Código estudiantil' : 'Materia'}</label>
                <input id="nu-${rol === 'ESTUDIANTE' ? 'codigo' : 'materia'}"
                    placeholder="${rol === 'ESTUDIANTE' ? 'EST-2026-010' : 'Bases de Datos'}" required maxlength="${rol === 'ESTUDIANTE' ? '30' : '80'}">
            </div>
        </div>`;
    };

    $('#nu-rol', modal).addEventListener('change', pintarAcademicos);
    pintarAcademicos();
    $('[data-cerrar]', modal).addEventListener('click', cerrarModal);

    $('#form-nuevo-usuario', modal).addEventListener('submit', async (evento) => {
        evento.preventDefault();
        const error = $('#nu-error', modal);
        error.hidden = true;
        const rol = $('#nu-rol', modal).value;
        const datos = {
            nombreCompleto: $('#nu-nombre', modal).value.trim(),
            email: $('#nu-email', modal).value.trim(),
            password: $('#nu-password', modal).value,
            rol,
            estado: $('#nu-estado', modal).value
        };
        if (rol !== 'ADMIN') {
            datos.aula = $('#nu-aula', modal).value.trim();
            if (rol === 'ESTUDIANTE') {
                datos.codigoEstudiantil = $('#nu-codigo', modal).value.trim();
            } else {
                datos.materia = $('#nu-materia', modal).value.trim();
            }
        }

        if (!datos.nombreCompleto || !datos.email || !datos.password) {
            error.textContent = 'Nombre, correo y contraseña son obligatorios.';
            error.hidden = false;
            return;
        }
        if (datos.password.length < 8) {
            error.textContent = 'La contraseña debe tener al menos 8 caracteres.';
            error.hidden = false;
            return;
        }
        if (rol !== 'ADMIN' && (!datos.aula || (rol === 'ESTUDIANTE' ? !datos.codigoEstudiantil : !datos.materia))) {
            error.textContent = 'Completa los datos académicos del usuario.';
            error.hidden = false;
            return;
        }

        const boton = $('#nu-guardar', modal);
        boton.disabled = true;
        try {
            await api('/admin/usuarios', { method: 'POST', body: JSON.stringify(datos) });
            cerrarModal();
            toast(`Usuario ${datos.nombreCompleto} creado`, 'exito');
            if (S.vista === 'usuarios') {
                await cargarUsuarios();
            } else {
                renderVista();
            }
        } catch (fallo) {
            error.textContent = mensajeDeError(fallo);
            error.hidden = false;
        } finally {
            boton.disabled = false;
        }
    });
}

function modalEditarUsuario(id) {
    const usuario = S.usuarios.find((elemento) => elemento.id === Number(id));
    if (!usuario) {
        toast('No se encontró el usuario', 'error');
        return;
    }
    const academico = usuario.rol === 'ESTUDIANTE' ? 'codigoEstudiantil' : 'materia';
    const esAdmin = usuario.rol === 'ADMIN';

    const modal = abrirModal({
        titulo: 'Editar usuario',
        subtitulo: usuario.email,
        cuerpo: `<form id="form-editar-usuario" novalidate>
            <label for="eu-nombre">Nombre completo</label>
            <input id="eu-nombre" value="${esc(usuario.nombreCompleto)}" required maxlength="120">
            ${esAdmin ? '' : `
            <div class="dos-columnas">
                <div>
                    <label for="eu-aula">Aula</label>
                    <input id="eu-aula" value="${esc(usuario.aula || '')}" required maxlength="80">
                </div>
                <div>
                    <label for="eu-academico">${usuario.rol === 'ESTUDIANTE' ? 'Código estudiantil' : 'Materia'}</label>
                    <input id="eu-academico" value="${esc(usuario[academico] || '')}" required maxlength="${usuario.rol === 'ESTUDIANTE' ? '30' : '80'}">
                </div>
            </div>`}
            <p class="modal-ayuda">El rol, el correo y la contraseña no se cambian desde aquí.</p>
            <div id="eu-error" class="modal-error" hidden></div>
        </form>`,
        pie: `<button type="button" class="boton peligro" data-eliminar>${icono('borrar')}Eliminar</button>
              <span style="flex:1"></span>
              <button type="button" class="boton fantasma" data-cerrar>Cancelar</button>
              <button type="submit" form="form-editar-usuario" class="boton primario" id="eu-guardar">Guardar cambios</button>`
    });

    $('[data-cerrar]', modal).addEventListener('click', cerrarModal);
    if (!esAdmin) {
        $('[data-eliminar]', modal).addEventListener('click', () => {
            cerrarModal();
            eliminarUsuario(usuario.id);
        });
    } else {
        $('[data-eliminar]', modal).remove();
    }

    $('#form-editar-usuario', modal).addEventListener('submit', async (evento) => {
        evento.preventDefault();
        const error = $('#eu-error', modal);
        error.hidden = true;
        const datos = { nombreCompleto: $('#eu-nombre', modal).value.trim() };
        if (!esAdmin) {
            datos.aula = $('#eu-aula', modal).value.trim();
            if (usuario.rol === 'ESTUDIANTE') {
                datos.codigoEstudiantil = $('#eu-academico', modal).value.trim();
            } else {
                datos.materia = $('#eu-academico', modal).value.trim();
            }
        }
        if (!datos.nombreCompleto) {
            error.textContent = 'El nombre es obligatorio.';
            error.hidden = false;
            return;
        }

        const boton = $('#eu-guardar', modal);
        boton.disabled = true;
        try {
            await api(`/admin/usuarios/${usuario.id}`, { method: 'PUT', body: JSON.stringify(datos) });
            cerrarModal();
            toast('Cambios guardados', 'exito');
            if (S.usuario.id === usuario.id) {
                S.usuario = await api('/auth/me');
                pintarBarra();
            }
            if (S.vista === 'usuarios') {
                await cargarUsuarios();
            } else {
                renderVista();
            }
        } catch (fallo) {
            error.textContent = mensajeDeError(fallo);
            error.hidden = false;
        } finally {
            boton.disabled = false;
        }
    });
}

async function cambiarEstadoUsuario(id, accion, mensajeExito) {
    cargando(true);
    try {
        await api(`/admin/usuarios/${id}/${accion}`, { method: 'PATCH' });
        toast(mensajeExito, 'exito');
        await refrescarVistaActual();
    } catch (fallo) {
        toast(mensajeDeError(fallo), 'error');
    } finally {
        cargando(false);
    }
}

async function eliminarUsuario(id) {
    const usuario = S.usuarios.find((elemento) => elemento.id === Number(id));
    const nombre = usuario ? usuario.nombreCompleto : 'este usuario';
    const confirmado = await confirmar({
        titulo: 'Eliminar usuario',
        mensaje: `Se eliminará a ${nombre} junto con su perfil y sus notas. Esta acción no se puede deshacer.`,
        textoBoton: 'Eliminar',
        peligro: true
    });
    if (!confirmado) {
        return;
    }
    cargando(true);
    try {
        await api(`/admin/usuarios/${id}`, { method: 'DELETE' });
        toast('Usuario eliminado', 'exito');
        await refrescarVistaActual();
    } catch (fallo) {
        toast(mensajeDeError(fallo), 'error');
    } finally {
        cargando(false);
    }
}

/* ---------- Notas (administrador) ---------- */

async function renderNotas() {
    if (S.usuario.rol === 'ADMIN') {
        return renderNotasAdmin();
    }
    if (S.usuario.rol === 'PROFESOR') {
        return renderNotasProfesor();
    }
    return renderNotasEstudiante();
}

async function renderNotasAdmin() {
    definirCabecera('Notas', 'Historial completo de calificaciones',
        `<button type="button" class="boton fantasma" data-accion="refrescar">${icono('recargar')}Actualizar</button>`);

    $('#contenido').innerHTML = `
        <section class="seccion">
            <header class="seccion-cabecera">
                <div class="barra-filtros" style="margin:0;flex:1">
                    <div class="campo-busqueda">
                        ${icono('buscar')}
                        <input id="busqueda-notas" type="search" placeholder="Buscar por estudiante, materia, aula o periodo…" value="${esc(S.filtros.busquedaNotas)}">
                    </div>
                </div>
            </header>
            <div id="tabla-notas">${esqueleto()}</div>
        </section>`;

    $('#busqueda-notas').addEventListener('input', (evento) => {
        S.filtros.busquedaNotas = evento.target.value;
        pintarNotasAdmin();
    });

    cargando(true);
    try {
        S.notas = await api('/admin/notas');
        pintarNotasAdmin();
    } finally {
        cargando(false);
    }
}

function pintarNotasAdmin() {
    const contenedor = $('#tabla-notas');
    if (!contenedor) {
        return;
    }
    const busqueda = S.filtros.busquedaNotas.trim().toLowerCase();
    const notas = S.notas.filter((nota) => !busqueda || [
        nota.estudianteNombre, nota.estudianteCodigo, nota.materia, nota.aula, nota.periodo, nota.profesorNombre
    ].some((valor) => String(valor || '').toLowerCase().includes(busqueda)));

    const promedio = promedioDe(notas);
    const porMateria = agruparPor(notas, (nota) => nota.materia);

    contenedor.innerHTML = `
        <div class="rejilla-tarjetas" style="padding:18px 18px 0;margin-bottom:0">
            ${tarjetaMetrica({ iconoNombre: 'grafico', valor: promedio === null ? '—' : promedio.toFixed(2), etiqueta: 'Promedio del filtro', detalle: `${notas.length} nota(s)` })}
            ${tarjetaMetrica({ iconoNombre: 'libro', color: 'violeta', valor: Object.keys(porMateria).length, etiqueta: 'Materias con notas', detalle: 'Según el filtro actual' })}
            ${tarjetaMetrica({ iconoNombre: 'estrella', color: 'verde', valor: notas.length ? Math.max(...notas.map((nota) => numero(nota.valor) ?? 0)).toFixed(1) : '—', etiqueta: 'Mejor nota', detalle: 'Escala 0.0 – 5.0' })}
        </div>
        ${notas.length ? `<div class="tabla-envoltorio" style="margin-top:18px"><table class="tabla">
            <thead><tr><th>Estudiante</th><th>Materia</th><th>Profesor</th><th>Periodo</th><th>Nota</th><th>Actualizada</th><th>Comentario</th></tr></thead>
            <tbody>${notas.map((nota) => `
                <tr>
                    <td>${celdaPersona({ nombreCompleto: nota.estudianteNombre, email: nota.estudianteCodigo })}</td>
                    <td>${esc(nota.materia)} <span class="persona-detalle">· ${esc(nota.aula)}</span></td>
                    <td>${esc(nota.profesorNombre)}</td>
                    <td class="mono">${esc(nota.periodo)}</td>
                    <td><span class="nota-valor ${claseNota(nota.valor)}">${formatoNota(nota.valor)}</span></td>
                    <td class="mono">${esc(fecha(nota.actualizadaEn))}</td>
                    <td class="persona-detalle" style="max-width:230px">${esc(nota.comentario || '—')}</td>
                </tr>`).join('')}
            </tbody>
        </table></div>` : estadoVacio('notas', 'Sin notas para mostrar', 'Ajusta la búsqueda o espera a que los profesores registren calificaciones.')}`;
}

/* ---------- Profesor ---------- */

async function cargarDatosProfesor() {
    const [perfil, estudiantes, notas] = await Promise.all([
        api('/profesor/perfil'),
        api('/profesor/estudiantes'),
        api('/profesor/notas')
    ]);
    S.perfil = perfil;
    S.estudiantes = estudiantes;
    S.notas = notas;
}

async function renderResumenProfesor() {
    definirCabecera('Mi resumen', 'Actividad de tus grupos y notas',
        `<button type="button" class="boton primario" data-accion="nueva-nota">${icono('mas')}Registrar nota</button>`);

    $('#contenido').innerHTML = esqueleto(6);
    cargando(true);
    try {
        await cargarDatosProfesor();
        const promedio = promedioDe(S.notas);
        const aula = S.perfil.aula || '—';
        const materia = S.perfil.materia || '—';
        const conNotas = new Set(S.notas.map((nota) => nota.estudianteId)).size;
        const porPeriodo = agruparPor(S.notas, (nota) => nota.periodo);

        $('#contenido').innerHTML = `
            <div class="rejilla-tarjetas">
                ${tarjetaMetrica({ iconoNombre: 'usuarios', color: 'violeta', valor: S.estudiantes.length, etiqueta: 'Estudiantes en tu aula', detalle: `Aula ${aula}` })}
                ${tarjetaMetrica({ iconoNombre: 'notas', valor: S.notas.length, etiqueta: 'Notas registradas', detalle: `${conNotas} estudiante(s) calificados` })}
                ${tarjetaMetrica({ iconoNombre: 'grafico', color: 'verde', valor: promedio === null ? '—' : promedio.toFixed(2), etiqueta: 'Promedio de tus notas', detalle: 'Escala 0.0 – 5.0' })}
                ${tarjetaMetrica({ iconoNombre: 'libro', color: 'ambar', valor: recortar(materia, 14), etiqueta: 'Materia que dictas', detalle: Object.keys(porPeriodo).join(' · ') || 'Sin periodos aún' })}
            </div>

            <div class="dos-paneles">
                <section class="seccion">
                    <header class="seccion-cabecera">
                        <div><h2>Estudiantes de tu aula</h2><p>Promedio calculado con tus notas</p></div>
                        <span class="espaciador"></span>
                        <button type="button" class="boton fantasma pequeno" data-accion="ir-vista" data-vista="estudiantes">Ver todos</button>
                    </header>
                    ${S.estudiantes.length ? `<div class="seccion-cuerpo"><div class="lista-simple">
                        ${S.estudiantes.slice(0, 5).map((estudiante) => `
                            <div class="lista-fila">
                                <span class="avatar-sm">${esc(iniciales(estudiante.nombreCompleto))}</span>
                                <div class="crece">
                                    <div class="lista-titulo">${esc(estudiante.nombreCompleto)}</div>
                                    <div class="lista-sub">${esc(estudiante.codigoEstudiantil)} · Aula ${esc(estudiante.aula)}</div>
                                </div>
                                <span class="nota-valor ${claseNota(estudiante.promedio)}">${estudiante.promedio === null ? '—' : formatoNota(estudiante.promedio)}</span>
                                ${botonAccion('calificar-estudiante', 'Calificar', { clase: 'exito', datos: { id: estudiante.estudianteId } })}
                            </div>`).join('')}
                    </div></div>` : estadoVacio('usuarios', 'Aún no hay estudiantes', 'Cuando se aprueben estudiantes de tu aula aparecerán aquí.')}
                </section>

                <section class="seccion">
                    <header class="seccion-cabecera">
                        <div><h2>Últimas notas</h2><p>Lo que has calificado recientemente</p></div>
                    </header>
                    ${S.notas.length ? `<div class="seccion-cuerpo"><div class="lista-simple">
                        ${S.notas.slice(0, 6).map((nota) => `
                            <div class="lista-fila">
                                <div class="crece">
                                    <div class="lista-titulo">${esc(nota.estudianteNombre)}</div>
                                    <div class="lista-sub">${esc(nota.periodo)} · ${esc(nota.materia)} · ${esc(fecha(nota.actualizadaEn))}</div>
                                </div>
                                <span class="nota-valor ${claseNota(nota.valor)}">${formatoNota(nota.valor)}</span>
                                ${botonAccion('editar-nota', '', { iconoNombre: 'editar', datos: { id: nota.id } })}
                            </div>`).join('')}
                    </div></div>` : estadoVacio('notas', 'Sin notas registradas', 'Usa «Registrar nota» para calificar a un estudiante.')}
                </section>
            </div>`;
    } finally {
        cargando(false);
    }
}

async function renderEstudiantes() {
    definirCabecera('Mis estudiantes', 'Estudiantes de tu aula y su promedio',
        `<button type="button" class="boton primario" data-accion="nueva-nota">${icono('mas')}Registrar nota</button>`);

    $('#contenido').innerHTML = `
        <section class="seccion">
            <header class="seccion-cabecera">
                <div class="barra-filtros" style="margin:0;flex:1">
                    <div class="campo-busqueda">
                        ${icono('buscar')}
                        <input id="busqueda-estudiantes" type="search" placeholder="Buscar por nombre o código…" value="${esc(S.filtros.busquedaEstudiantes)}">
                    </div>
                    <span class="badge badge-profesor" id="aula-profesor">Aula —</span>
                </div>
            </header>
            <div id="tabla-estudiantes">${esqueleto()}</div>
        </section>`;

    $('#busqueda-estudiantes').addEventListener('input', (evento) => {
        S.filtros.busquedaEstudiantes = evento.target.value;
        pintarEstudiantes();
    });

    cargando(true);
    try {
        await cargarDatosProfesor();
        const aula = $('#aula-profesor');
        if (aula) {
            aula.textContent = `Aula ${S.perfil.aula || '—'} · ${S.perfil.materia || '—'}`;
        }
        pintarEstudiantes();
    } finally {
        cargando(false);
    }
}

function pintarEstudiantes() {
    const contenedor = $('#tabla-estudiantes');
    if (!contenedor) {
        return;
    }
    const busqueda = S.filtros.busquedaEstudiantes.trim().toLowerCase();
    const lista = S.estudiantes.filter((estudiante) => !busqueda || [
        estudiante.nombreCompleto, estudiante.codigoEstudiantil, estudiante.email
    ].some((valor) => String(valor || '').toLowerCase().includes(busqueda)));

    if (!lista.length) {
        contenedor.innerHTML = estadoVacio('usuarios', 'Sin estudiantes',
            S.estudiantes.length ? 'Ningún estudiante coincide con la búsqueda.' : 'No hay estudiantes activos en tu aula todavía.');
        return;
    }

    contenedor.innerHTML = `<div class="tabla-envoltorio"><table class="tabla">
        <thead><tr><th>Estudiante</th><th>Código</th><th>Aula</th><th>Promedio</th><th style="text-align:right">Acciones</th></tr></thead>
        <tbody>${lista.map((estudiante) => `
            <tr>
                <td>${celdaPersona({ nombreCompleto: estudiante.nombreCompleto, email: estudiante.email })}</td>
                <td class="mono">${esc(estudiante.codigoEstudiantil)}</td>
                <td>${esc(estudiante.aula)}</td>
                <td><span class="nota-valor ${claseNota(estudiante.promedio)}">${estudiante.promedio === null ? 'Sin notas' : formatoNota(estudiante.promedio)}</span></td>
                <td><div class="acciones-fila">
                    ${botonAccion('calificar-estudiante', 'Calificar', { clase: 'exito', iconoNombre: 'editar', datos: { id: estudiante.estudianteId } })}
                </div></td>
            </tr>`).join('')}
        </tbody>
    </table></div><div class="pie-tabla">${lista.length} estudiante(s)</div>`;
}

async function renderNotasProfesor() {
    definirCabecera('Mis notas', 'Registra, edita o elimina calificaciones',
        `<button type="button" class="boton primario" data-accion="nueva-nota">${icono('mas')}Registrar nota</button>`);

    $('#contenido').innerHTML = `
        <section class="seccion">
            <header class="seccion-cabecera">
                <div class="barra-filtros" style="margin:0;flex:1">
                    <div class="campo-busqueda">
                        ${icono('buscar')}
                        <input id="busqueda-notas-profesor" type="search" placeholder="Buscar por estudiante, periodo o nota…" value="${esc(S.filtros.busquedaNotas)}">
                    </div>
                </div>
            </header>
            <div id="tabla-notas-profesor">${esqueleto()}</div>
        </section>`;

    $('#busqueda-notas-profesor').addEventListener('input', (evento) => {
        S.filtros.busquedaNotas = evento.target.value;
        pintarNotasProfesor();
    });

    cargando(true);
    try {
        await cargarDatosProfesor();
        pintarNotasProfesor();
    } finally {
        cargando(false);
    }
}

function pintarNotasProfesor() {
    const contenedor = $('#tabla-notas-profesor');
    if (!contenedor) {
        return;
    }
    const busqueda = S.filtros.busquedaNotas.trim().toLowerCase();
    const notas = S.notas.filter((nota) => !busqueda || [
        nota.estudianteNombre, nota.estudianteCodigo, nota.periodo, nota.comentario, String(nota.valor)
    ].some((valor) => String(valor || '').toLowerCase().includes(busqueda)));

    const promedio = promedioDe(notas);

    contenedor.innerHTML = `
        <div class="rejilla-tarjetas" style="padding:18px 18px 0;margin-bottom:0">
            ${tarjetaMetrica({ iconoNombre: 'notas', valor: notas.length, etiqueta: 'Notas mostradas', detalle: `${S.estudiantes.length} estudiante(s) en el aula` })}
            ${tarjetaMetrica({ iconoNombre: 'grafico', color: 'verde', valor: promedio === null ? '—' : promedio.toFixed(2), etiqueta: 'Promedio de tus notas' })}
            ${tarjetaMetrica({ iconoNombre: 'estrella', color: 'ambar', valor: notas.length ? Math.max(...notas.map((nota) => numero(nota.valor) ?? 0)).toFixed(1) : '—', etiqueta: 'Mejor nota del filtro' })}
        </div>
        ${notas.length ? `<div class="tabla-envoltorio" style="margin-top:18px"><table class="tabla">
            <thead><tr><th>Estudiante</th><th>Periodo</th><th>Nota</th><th>Comentario</th><th>Actualizada</th><th style="text-align:right">Acciones</th></tr></thead>
            <tbody>${notas.map((nota) => `
                <tr>
                    <td>${celdaPersona({ nombreCompleto: nota.estudianteNombre, email: nota.estudianteCodigo })}</td>
                    <td class="mono">${esc(nota.periodo)}</td>
                    <td><span class="nota-valor ${claseNota(nota.valor)}">${formatoNota(nota.valor)}</span></td>
                    <td class="persona-detalle" style="max-width:260px">${esc(nota.comentario || '—')}</td>
                    <td class="mono">${esc(fecha(nota.actualizadaEn))}</td>
                    <td><div class="acciones-fila">
                        ${botonAccion('editar-nota', 'Editar', { iconoNombre: 'editar', datos: { id: nota.id } })}
                        ${botonAccion('eliminar-nota', '', { clase: 'peligro-suave', iconoNombre: 'borrar', datos: { id: nota.id } })}
                    </div></td>
                </tr>`).join('')}
            </tbody>
        </table></div><div class="pie-tabla">${notas.length} nota(s)</div>`
        : estadoVacio('notas', 'Sin notas', 'Registra la primera nota con el botón «Registrar nota».')}`;
}

/* ---------- Modal de nota (profesor) ---------- */

function modalNota(notaExistente = null, estudianteId = null) {
    const esEdicion = Boolean(notaExistente);
    if (!esEdicion && !S.estudiantes.length) {
        toast('No tienes estudiantes disponibles para calificar', 'error');
        return;
    }

    const seleccionado = estudianteId ?? notaExistente?.estudianteId ?? S.estudiantes[0]?.estudianteId;
    const opciones = S.estudiantes.map((estudiante) => `
        <option value="${estudiante.estudianteId}" ${Number(estudiante.estudianteId) === Number(seleccionado) ? 'selected' : ''}>
            ${esc(estudiante.nombreCompleto)} · ${esc(estudiante.codigoEstudiantil)}
        </option>`).join('');

    const modal = abrirModal({
        titulo: esEdicion ? 'Editar nota' : 'Registrar nota',
        subtitulo: esEdicion
            ? `${notaExistente.estudianteNombre} · ${notaExistente.materia} · ${notaExistente.aula}`
            : 'La materia y el aula se toman de tu perfil de profesor',
        cuerpo: `<form id="form-nota" novalidate>
            ${esEdicion ? `
                <label>Estudiante</label>
                <input value="${esc(notaExistente.estudianteNombre)} (${esc(notaExistente.estudianteCodigo)})" disabled>
            ` : `
                <label for="no-estudiante">Estudiante</label>
                <select id="no-estudiante" required>${opciones}</select>
            `}

            <div class="dos-columnas">
                <div>
                    <label for="no-periodo">Periodo</label>
                    <input id="no-periodo" value="${esc(notaExistente?.periodo || PERIODO_ACTUAL)}" placeholder="2026-1" required maxlength="20">
                </div>
                <div>
                    <label for="no-valor">Nota (0.0 – 5.0)</label>
                    <input id="no-valor" type="number" min="0" max="5" step="0.1"
                        value="${notaExistente ? formatoNota(notaExistente.valor) : ''}" placeholder="4.5" required>
                </div>
            </div>

            <label for="no-comentario">Comentario (opcional)</label>
            <textarea id="no-comentario" maxlength="300" placeholder="Observaciones para el estudiante…">${esc(notaExistente?.comentario || '')}</textarea>

            <div id="no-error" class="modal-error" hidden></div>
        </form>`,
        pie: `<button type="button" class="boton fantasma" data-cerrar>Cancelar</button>
              <button type="submit" form="form-nota" class="boton primario" id="no-guardar">
                ${esEdicion ? 'Guardar cambios' : 'Registrar nota'}
              </button>`
    });

    $('[data-cerrar]', modal).addEventListener('click', cerrarModal);

    $('#form-nota', modal).addEventListener('submit', async (evento) => {
        evento.preventDefault();
        const error = $('#no-error', modal);
        error.hidden = true;

        const valor = Number($('#no-valor', modal).value);
        const periodo = $('#no-periodo', modal).value.trim();
        const comentario = $('#no-comentario', modal).value.trim();

        if (!periodo) {
            error.textContent = 'El periodo es obligatorio.';
            error.hidden = false;
            return;
        }
        if (!Number.isFinite(valor) || valor < 0 || valor > 5) {
            error.textContent = 'La nota debe ser un número entre 0.0 y 5.0.';
            error.hidden = false;
            return;
        }

        const datos = { periodo, valor, comentario: comentario || null };
        if (!esEdicion) {
            datos.estudianteId = Number($('#no-estudiante', modal).value);
        }

        const boton = $('#no-guardar', modal);
        boton.disabled = true;
        try {
            if (esEdicion) {
                await api(`/profesor/notas/${notaExistente.id}`, { method: 'PUT', body: JSON.stringify(datos) });
            } else {
                await api('/profesor/notas', { method: 'POST', body: JSON.stringify(datos) });
            }
            cerrarModal();
            toast(esEdicion ? 'Nota actualizada' : 'Nota registrada', 'exito');
            await recargarDatosProfesor();
        } catch (fallo) {
            error.textContent = mensajeDeError(fallo);
            error.hidden = false;
        } finally {
            boton.disabled = false;
        }
    });
}

async function recargarDatosProfesor() {
    if (S.vista === 'resumen' || S.vista === 'notas' || S.vista === 'estudiantes') {
        await renderVista();
        return;
    }
    await cargarDatosProfesor();
}

async function eliminarNota(id) {
    const nota = S.notas.find((elemento) => elemento.id === Number(id));
    const confirmado = await confirmar({
        titulo: 'Eliminar nota',
        mensaje: nota
            ? `Se eliminará la nota ${formatoNota(nota.valor)} de ${nota.estudianteNombre} en el periodo ${nota.periodo}.`
            : 'Se eliminará esta nota de forma permanente.',
        textoBoton: 'Eliminar',
        peligro: true
    });
    if (!confirmado) {
        return;
    }
    cargando(true);
    try {
        await api(`/profesor/notas/${id}`, { method: 'DELETE' });
        toast('Nota eliminada', 'exito');
        await renderVista();
    } catch (fallo) {
        toast(mensajeDeError(fallo), 'error');
    } finally {
        cargando(false);
    }
}

/* ---------- Estudiante ---------- */

async function cargarDatosEstudiante() {
    const [perfil, notas, promedio] = await Promise.all([
        api('/estudiante/perfil'),
        api('/estudiante/notas'),
        api('/estudiante/promedio')
    ]);
    S.perfil = perfil;
    S.notas = notas;
    S.promedio = promedio;
}

async function renderResumenEstudiante() {
    definirCabecera('Mi resumen', 'Tu historial académico y promedio',
        `<button type="button" class="boton fantasma" data-accion="refrescar">${icono('recargar')}Actualizar</button>`);

    $('#contenido').innerHTML = esqueleto(6);
    cargando(true);
    try {
        await cargarDatosEstudiante();
        const notas = S.notas;
        const promedio = numero(S.promedio?.promedio);
        const porMateria = agruparPor(notas, (nota) => nota.materia);
        const mejor = notas.length ? Math.max(...notas.map((nota) => numero(nota.valor) ?? 0)) : null;

        $('#contenido').innerHTML = `
            <div class="rejilla-tarjetas">
                ${tarjetaMetrica({
                    iconoNombre: 'grafico', color: 'verde',
                    valor: promedio === null ? '—' : promedio.toFixed(2),
                    etiqueta: 'Promedio general', detalle: 'Escala 0.0 – 5.0'
                })}
                ${tarjetaMetrica({
                    iconoNombre: 'notas', valor: notas.length,
                    etiqueta: 'Notas registradas', detalle: `${Object.keys(porMateria).length} materia(s)`
                })}
                ${tarjetaMetrica({
                    iconoNombre: 'estrella', color: 'ambar',
                    valor: mejor === null ? '—' : mejor.toFixed(1),
                    etiqueta: 'Mejor nota', detalle: 'Tu calificación más alta'
                })}
                ${tarjetaMetrica({
                    iconoNombre: 'usuario', color: 'violeta',
                    valor: esc(S.perfil.codigoEstudiantil || '—'),
                    etiqueta: 'Código estudiantil', detalle: `Aula ${S.perfil.aula || '—'}`
                })}
            </div>

            <div class="dos-paneles">
                <section class="seccion">
                    <header class="seccion-cabecera">
                        <div><h2>Mis notas</h2><p>Calificaciones registradas por tus profesores</p></div>
                        <span class="espaciador"></span>
                        <button type="button" class="boton fantasma pequeno" data-accion="ir-vista" data-vista="notas">Ver detalle</button>
                    </header>
                    ${notas.length ? `<div class="tabla-envoltorio"><table class="tabla">
                        <thead><tr><th>Materia</th><th>Periodo</th><th>Nota</th><th>Profesor</th></tr></thead>
                        <tbody>${notas.slice(0, 6).map((nota) => `
                            <tr>
                                <td>${esc(nota.materia)} <span class="persona-detalle">· ${esc(nota.aula)}</span></td>
                                <td class="mono">${esc(nota.periodo)}</td>
                                <td><span class="nota-valor ${claseNota(nota.valor)}">${formatoNota(nota.valor)}</span></td>
                                <td>${esc(nota.profesorNombre)}</td>
                            </tr>`).join('')}
                        </tbody>
                    </table></div>` : estadoVacio('notas', 'Todavía no tienes notas', 'Cuando tus profesores registren calificaciones aparecerán aquí.')}
                </section>

                <section class="seccion">
                    <header class="seccion-cabecera"><div><h2>Promedio por materia</h2><p>Comparación de tu desempeño</p></div></header>
                    <div class="seccion-cuerpo">
                        ${notas.length ? `<div class="distribucion">
                            ${Object.entries(porMateria).map(([materia, lista]) => {
                                const valor = promedioDe(lista) ?? 0;
                                return `<div class="dist-fila">
                                    <span class="dist-etiqueta" title="${esc(materia)}">${esc(recortar(materia, 14))}</span>
                                    <div class="dist-pista"><div class="dist-barra ${claseNota(valor)}" style="width:${(valor / 5) * 100}%"></div></div>
                                    <span class="dist-valor">${valor.toFixed(1)}</span>
                                </div>`;
                            }).join('')}
                        </div>` : estadoVacio('grafico', 'Sin datos aún', 'Necesitas al menos una nota para ver tu distribución por materia.')}
                    </div>
                </section>
            </div>`;
    } finally {
        cargando(false);
    }
}

async function renderNotasEstudiante() {
    definirCabecera('Mis notas', 'Detalle completo de tus calificaciones',
        `<button type="button" class="boton fantasma" data-accion="refrescar">${icono('recargar')}Actualizar</button>`);

    $('#contenido').innerHTML = esqueleto(6);
    cargando(true);
    try {
        await cargarDatosEstudiante();
        const notas = S.notas;
        const promedio = numero(S.promedio?.promedio);

        $('#contenido').innerHTML = `
            <div class="rejilla-tarjetas">
                ${tarjetaMetrica({ iconoNombre: 'grafico', color: 'verde', valor: promedio === null ? '—' : promedio.toFixed(2), etiqueta: 'Promedio general', detalle: `${S.promedio?.cantidadNotas ?? notas.length} nota(s)` })}
                ${tarjetaMetrica({ iconoNombre: 'notas', valor: notas.length, etiqueta: 'Notas registradas' })}
                ${tarjetaMetrica({ iconoNombre: 'usuario', color: 'violeta', valor: esc(S.perfil.nombreCompleto), etiqueta: 'Estudiante', detalle: `${S.perfil.codigoEstudiantil} · Aula ${S.perfil.aula}` })}
            </div>
            <section class="seccion">
                <header class="seccion-cabecera"><div><h2>Calificaciones</h2><p>Ordenadas de la más reciente a la más antigua</p></div></header>
                ${notas.length ? `<div class="tabla-envoltorio"><table class="tabla">
                    <thead><tr><th>Materia</th><th>Aula</th><th>Periodo</th><th>Nota</th><th>Profesor</th><th>Comentario</th><th>Actualizada</th></tr></thead>
                    <tbody>${notas.map((nota) => `
                        <tr>
                            <td><strong>${esc(nota.materia)}</strong></td>
                            <td>${esc(nota.aula)}</td>
                            <td class="mono">${esc(nota.periodo)}</td>
                            <td><span class="nota-valor ${claseNota(nota.valor)}">${formatoNota(nota.valor)}</span></td>
                            <td>${esc(nota.profesorNombre)}</td>
                            <td class="persona-detalle" style="max-width:240px">${esc(nota.comentario || '—')}</td>
                            <td class="mono">${esc(fecha(nota.actualizadaEn))}</td>
                        </tr>`).join('')}
                    </tbody>
                </table></div>` : estadoVacio('notas', 'Sin calificaciones', 'Aún no tienes notas registradas por tus profesores.')}
            </section>`;
    } finally {
        cargando(false);
    }
}

/* ---------- Consola interna ---------- */

const COMANDOS_INTERNOS = [
    ['./scripts/db.sh', 'Abre la consola interactiva (menú de comandos)'],
    ['./scripts/db.sh resumen', 'Conteos generales de la base de datos'],
    ['./scripts/db.sh usuarios', 'Lista todos los usuarios'],
    ['./scripts/db.sh usuarios PENDIENTE', 'Filtra por estado (PENDIENTE, ACTIVO, RECHAZADO, EXPULSADO)'],
    ['./scripts/db.sh usuarios PROFESOR ACTIVO', 'Combina filtros de rol y estado'],
    ['./scripts/db.sh notas', 'Lista las notas registradas'],
    ['./scripts/db.sh aprobar 7', 'Aprueba la cuenta con id 7'],
    ['./scripts/db.sh rechazar 7', 'Rechaza una solicitud pendiente'],
    ['./scripts/db.sh expulsar 3', 'Expulsa a un usuario activo'],
    ['./scripts/db.sh reactivar 3', 'Reactiva un usuario expulsado o rechazado'],
    ['./scripts/db.sh eliminar 9', 'Elimina un usuario con su perfil y notas'],
    ['./scripts/db.sh crear-usuario', 'Asistente interactivo para crear una cuenta'],
    ['./scripts/db.sh tablas', 'Muestra las tablas de la base de datos'],
    ['./scripts/db.sh "describir uco_nota"', 'Describe columnas, tipos y nulabilidad'],
    ['./scripts/db.sh "select id, email, rol, estado from uco_usuario"', 'Consulta SQL directa (sin prefijo sql)'],
    ['./scripts/db.sh "insert into uco_usuario (nombre_completo, email, password_hash, rol, estado) values (\'Ana\', \'ana@uco.edu.co\', \'<hash>\', \'ESTUDIANTE\', \'ACTIVO\')"', 'Inserta una fila'],
    ['./scripts/db.sh "update uco_usuario set estado = \'ACTIVO\' where id = 7"', 'Modifica filas puntuales'],
    ['./scripts/db.sh "sql select * from uco_nota"', 'Alias con prefijo sql (opcional)']
];

function renderInterno() {
    definirCabecera('Consola interna', 'Opera la base de datos por comandos, sin abrir la interfaz web',
        `<button type="button" class="boton fantasma" data-accion="refrescar">${icono('recargar')}Recargar guía</button>`);

    $('#contenido').innerHTML = `
        <section class="seccion">
            <header class="seccion-cabecera">
                <div>
                    <h2>Base de datos por comandos</h2>
                    <p>La misma aplicación, en modo consola: reutiliza servicios, validaciones y las reglas del backend.</p>
                </div>
            </header>
            <div class="seccion-cuerpo">
                <div class="terminal"><span class="comentario"># Con la aplicación web encendida o apagada, siempre y cuando la base esté disponible</span>
<span class="prompt">$</span> ./scripts/db.sh
<span class="comentario"># Modo interactivo: escribe ayuda y presiona Enter</span>

<span class="prompt">$</span> ./scripts/db.sh resumen
<span class="prompt">$</span> ./scripts/db.sh "usuarios PENDIENTE"
<span class="prompt">$</span> ./scripts/db.sh "select id, email, rol, estado from uco_usuario order by id"
<span class="prompt">$</span> ./scripts/db.sh "update uco_usuario set estado = 'ACTIVO' where id = 7"</div>

                <div class="aviso-interno">
                    ${icono('alerta')}
                    <div>El SQL se escribe <strong>directo</strong>: <code>select</code>, <code>insert</code>,
                    <code>update</code>, <code>delete</code> y DDL. También puedes usar el prefijo <code>sql …</code>
                    y terminar con punto y coma. Los cambios son reales e inmediatos: no requiere reiniciar el
                    servidor. Por defecto la consola usa Oracle (<code>PERFIL=oracle</code>);
                    con <code>PERFIL=dev ./scripts/db.sh …</code> trabaja sobre H2 en archivo.</div>
                </div>

                <div class="dos-paneles" style="margin-top:18px">
                    <section class="seccion" style="margin:0">
                        <header class="seccion-cabecera"><div><h2>Comandos disponibles</h2><p>Haz clic en «Copiar» para llevarlos a tu terminal</p></div></header>
                        <div class="tabla-envoltorio"><table class="tabla tabla-comandos">
                            <thead><tr><th>Comando</th><th>Qué hace</th><th></th></tr></thead>
                            <tbody>${COMANDOS_INTERNOS.map(([comando, descripcion]) => `
                                <tr>
                                    <td><code>${esc(comando)}</code></td>
                                    <td class="persona-detalle">${esc(descripcion)}</td>
                                    <td><div class="acciones-fila">${botonAccion('copiar', 'Copiar', { iconoNombre: 'copiar', datos: { texto: comando } })}</div></td>
                                </tr>`).join('')}
                            </tbody>
                        </table></div>
                    </section>

                    <section class="seccion" style="margin:0">
                        <header class="seccion-cabecera"><div><h2>Flujo recomendado</h2><p>De la solicitud al aula</p></div></header>
                        <div class="seccion-cuerpo">
                            <div class="pasos">
                                <div class="paso"><div><strong>Revisa las solicitudes.</strong> <code>./scripts/db.sh usuarios PENDIENTE</code></div></div>
                                <div class="paso"><div><strong>Aprueba o rechaza.</strong> <code>./scripts/db.sh aprobar &lt;id&gt;</code></div></div>
                                <div class="paso"><div><strong>Verifica las notas.</strong> <code>./scripts/db.sh notas</code></div></div>
                                <div class="paso"><div><strong>Consulta libremente.</strong> <code>./scripts/db.sh "select …"</code></div></div>
                            </div>
                            <p class="modal-ayuda">Los comandos <code>aprobar</code>, <code>expulsar</code>, etc. aplican las mismas reglas del panel:
                            no se puede actuar sobre administradores ni sobre la propia cuenta.</p>
                        </div>
                    </section>
                </div>
            </div>
        </section>`;
}

/* ---------- Perfil ---------- */

function modalPerfil() {
    const usuario = S.usuario;
    if (!usuario) {
        return;
    }
    const academia = usuario.rol === 'ESTUDIANTE'
        ? [['Código estudiantil', usuario.codigoEstudiantil], ['Aula', usuario.aula]]
        : usuario.rol === 'PROFESOR'
            ? [['Materia', usuario.materia], ['Aula', usuario.aula]]
            : [];

    const filas = [
        ['Nombre completo', usuario.nombreCompleto],
        ['Correo', usuario.email],
        ['Rol', etiquetaRol(usuario.rol)],
        ['Estado', usuario.estado],
        ...academia
    ];

    abrirModal({
        titulo: 'Mi perfil',
        subtitulo: 'Datos de tu cuenta institucional',
        cuerpo: `<div class="lista-simple">
            ${filas.filter(([, valor]) => valor).map(([etiqueta, valor]) => `
                <div class="lista-fila">
                    <div class="crece">
                        <div class="lista-sub">${esc(etiqueta)}</div>
                        <div class="lista-titulo">${esc(valor)}</div>
                    </div>
                </div>`).join('')}
        </div>
        <p class="modal-ayuda">Si necesitas corregir un dato, contacta a coordinación académica.</p>`,
        pie: `<button type="button" class="boton fantasma" data-cerrar>Cerrar</button>`
    });
    $('[data-cerrar]', $('#modal-raiz')).addEventListener('click', cerrarModal);
}

async function copiarTexto(texto) {
    try {
        if (navigator.clipboard && window.isSecureContext) {
            await navigator.clipboard.writeText(texto);
        } else {
            const area = document.createElement('textarea');
            area.value = texto;
            area.style.position = 'fixed';
            area.style.opacity = '0';
            document.body.appendChild(area);
            area.select();
            document.execCommand('copy');
            area.remove();
        }
        toast('Comando copiado al portapapeles', 'exito', 2600);
    } catch (error) {
        toast('No se pudo copiar. Selecciona el comando manualmente.', 'error');
    }
}

/* ---------- Acciones declarativas ---------- */

const ACCIONES = {
    'refrescar': () => renderVista(),
    'ir-vista': (boton) => irAVista(boton.dataset.vista),
    'ver-interno': () => irAVista('interno'),
    'nuevo-usuario': () => modalNuevoUsuario(),
    'editar-usuario': (boton) => modalEditarUsuario(boton.dataset.id),
    'aprobar-usuario': (boton) => cambiarEstadoUsuario(boton.dataset.id, 'aprobar', 'Usuario aprobado'),
    'rechazar-usuario': (boton) => cambiarEstadoUsuario(boton.dataset.id, 'rechazar', 'Solicitud rechazada'),
    'expulsar-usuario': (boton) => cambiarEstadoUsuario(boton.dataset.id, 'expulsar', 'Usuario expulsado'),
    'reactivar-usuario': (boton) => cambiarEstadoUsuario(boton.dataset.id, 'reactivar', 'Usuario reactivado'),
    'eliminar-usuario': (boton) => eliminarUsuario(boton.dataset.id),
    'nueva-nota': () => modalNota(),
    'calificar-estudiante': (boton) => modalNota(null, Number(boton.dataset.id)),
    'editar-nota': (boton) => modalNota(S.notas.find((nota) => nota.id === Number(boton.dataset.id))),
    'eliminar-nota': (boton) => eliminarNota(boton.dataset.id),
    'copiar': (boton) => copiarTexto(boton.dataset.texto)
};

async function refrescarVistaActual() {
    if (S.vista === 'usuarios') {
        await cargarUsuarios();
        return;
    }
    await renderVista();
}

/* ---------- Arranque ---------- */

function conectarEventos() {
    $('#form-login').addEventListener('submit', iniciarSesion);
    $('#form-registro').addEventListener('submit', registrar);

    $('#toggle-password').addEventListener('click', () => {
        const campo = $('#login-password');
        const mostrar = campo.type === 'password';
        campo.type = mostrar ? 'text' : 'password';
        $('#toggle-password').setAttribute('aria-label', mostrar ? 'Ocultar contraseña' : 'Mostrar contraseña');
    });

    $$('#form-registro .segmentado button').forEach((boton) => {
        boton.addEventListener('click', () => cambiarRolRegistro(boton.dataset.rol));
    });

    $$('.demo-cuenta').forEach((boton) => {
        boton.addEventListener('click', () => {
            $('#login-email').value = boton.dataset.email;
            $('#login-password').value = boton.dataset.password;
            $('#form-login').requestSubmit();
        });
    });

    $('#alternar-demo').addEventListener('click', (evento) => {
        const contenedor = $('.login-demo');
        const expandida = contenedor.classList.toggle('expandida');
        evento.target.textContent = expandida ? 'Ver menos' : 'Ver todas';
    });

    $('#ir-registro').addEventListener('click', () => irARegistro(true));
    $('#volver-login').addEventListener('click', () => irARegistro(false));
    $('#cerrar-exito').addEventListener('click', () => irARegistro(false));

    $('#nav').addEventListener('click', (evento) => {
        const boton = evento.target.closest('[data-vista]');
        if (boton) {
            irAVista(boton.dataset.vista);
        }
    });

    $('#boton-menu').addEventListener('click', () => document.body.classList.toggle('barra-abierta'));    $('#velo').addEventListener('click', () => document.body.classList.remove('barra-abierta'));

    $('#boton-perfil').addEventListener('click', modalPerfil);

    $('#boton-salir').addEventListener('click', async () => {
        const confirmado = await confirmar({
            titulo: 'Cerrar sesión',
            mensaje: '¿Quieres salir del panel? Deberás iniciar sesión de nuevo para volver a entrar.',
            textoBoton: 'Salir'
        });
        if (confirmado) {
            cerrarSesion();
            toast('Sesión cerrada', 'info');
        }
    });

    $('#boton-interno-pie').addEventListener('click', () => {
        if (S.usuario) {
            irAVista('interno');
        }
    });

    document.addEventListener('click', async (evento) => {
        const boton = evento.target.closest('[data-accion]');
        if (!boton) {
            return;
        }
        const accion = ACCIONES[boton.dataset.accion];
        if (!accion) {
            return;
        }
        evento.preventDefault();
        try {
            await accion(boton);
        } catch (fallo) {
            toast(mensajeDeError(fallo), 'error');
        }
    });
}

function iniciarAplicacion() {
    cambiarRolRegistro('estudiante');
    conectarEventos();
    restaurarSesion();
}

if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', iniciarAplicacion);
} else {
    iniciarAplicacion();
}

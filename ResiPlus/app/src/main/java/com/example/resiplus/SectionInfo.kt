package com.example.resiplus

data class SectionInfo(
    val title: String,
    val icon: String,
    val summary: String,
    val information: String,
    val actions: String,
    val permissions: String
)

object SectionInfoProvider {
    private val personal = mapOf(
        "pendientes" to SectionInfo("Pendientes", "🕒", "Tareas y avisos por resolver", "Lista de tareas abiertas, incidencias del turno y revisiones que faltan por cerrar.", "Marcar completado, priorizar, dejar nota y pasar al siguiente turno.", "Personal puede ver y actualizar solo tareas operativas de su residencia."),
        "visitas_hoy" to SectionInfo("Visitas Hoy", "📅", "Agenda del dia", "Visitas confirmadas del dia con familiar, residente, hora y estado de acceso.", "Registrar llegada, salida, retrasos e incidencias durante la visita.", "Personal puede consultar y actualizar visitas del dia."),
        "registros_pendientes" to SectionInfo("Registros", "✅", "Altas esperando revision", "Solicitudes de familiares pendientes de aprobacion y su residente asociado.", "Aprobar, rechazar o revisar la solicitud antes de activar el acceso.", "Personal puede aprobar accesos familiares, no crear roles."),
        "solicitudes" to SectionInfo("Solicitudes", "📋", "Citas y peticiones", "Solicitudes de visita y peticiones pendientes de respuesta.", "Aceptar, rechazar, comentar y dejar trazabilidad de la decision.", "Personal puede gestionar solicitudes operativas."),
        "calendario" to SectionInfo("Calendario", "🗓️", "Planificacion de citas", "Vista por fecha de citas, reservas y huecos disponibles.", "Reprogramar citas, bloquear tramos y revisar ocupacion.", "Personal puede modificar agenda de su residencia."),
        "residentes" to SectionInfo("Residentes", "👥", "Listado de residentes", "Ficha resumida con habitacion, estado y observaciones de cada residente.", "Consultar informacion y abrir la ficha de cuidados del residente.", "Personal puede ver residentes de su residencia."),
        "cuidados" to SectionInfo("Cuidados", "❤️", "Necesidades especiales", "Alergias, movilidad, dietas, dependencia y cuidados individualizados.", "Registrar seguimiento, observaciones y confirmar cuidados realizados.", "Personal puede actualizar cuidados diarios, no cambiar configuraciones globales."),
        "incidencias" to SectionInfo("Incidencias", "⚠️", "Eventos del dia", "Caidas, conflictos, averias o incidencias de atencion.", "Crear incidencia, clasificar gravedad, cerrar o escalar.", "Personal puede gestionar incidencias operativas."),
        "medicaciones" to SectionInfo("Medicacion", "💊", "Tratamientos activos", "Medicaciones, tomas pendientes y observaciones del residente.", "Marcar toma administrada, registrar omision o incidencia.", "Personal puede registrar administracion, no prescribir ni cambiar tratamiento."),
        "turno" to SectionInfo("Turno", "🧹", "Trabajo del turno", "Checklist del turno con habitaciones, tareas y observaciones.", "Completar tareas, dejar notas y preparar relevo.", "Personal puede gestionar tareas del turno propio."),
        "historial" to SectionInfo("Historial", "📚", "Evolucion del residente", "Historial de incidencias, visitas, observaciones y cambios relevantes.", "Consultar y anadir notas autorizadas.", "Personal puede leer historico y anotar seguimiento."),
        "mensajes_internos" to SectionInfo("Mensajes", "💬", "Comunicacion interna", "Mensajes entre turnos, coordinacion y avisos internos.", "Enviar avisos, responder y marcar leidos.", "Personal solo ve mensajeria interna del centro."),
        "accesos" to SectionInfo("Accesos", "🚪", "Entradas y salidas", "Control de visitas autorizadas, entradas, salidas y accesos especiales.", "Registrar acceso, validar identidad y anotar observaciones.", "Personal puede registrar accesos, no alterar permisos globales."),
        "alertas" to SectionInfo("Alertas", "🚨", "Avisos urgentes", "Alertas criticas, emergencias y avisos activos del centro.", "Confirmar recepcion, escalar y cerrar alerta.", "Personal puede actuar sobre alertas de su residencia."),
        "documentacion" to SectionInfo("Documentacion", "📄", "Archivos disponibles", "Consentimientos, informes, fichas y documentos visibles para operativa.", "Consultar y adjuntar documentos permitidos.", "Personal puede leer y adjuntar documentacion autorizada.")
    )

    private val admin = mapOf(
        "crear_usuarios" to SectionInfo("Crear Usuarios", "👤", "Alta de cuentas", "Formulario de creacion de cuentas para familiares, personal y administradores.", "Crear usuarios, vincular familiares y activar cuentas directamente.", "Admin tiene alta completa de usuarios."),
        "revisar_solicitudes" to SectionInfo("Solicitudes", "✔️", "Revision general", "Registros pendientes, citas por aprobar y peticiones del sistema.", "Aprobar, rechazar o devolver solicitudes para revision.", "Admin puede supervisar toda la residencia."),
        "gestion_residentes" to SectionInfo("Residentes", "🏠", "Gestion integral", "Listado completo de residentes activos e inactivos, habitaciones y cuidados.", "Alta, edicion, baja y reactivacion de residentes.", "Admin tiene control total de residentes."),
        "roles_permisos" to SectionInfo("Permisos", "🔐", "Seguridad por rol", "Matriz de permisos de personal, familiar y admin.", "Ajustar permisos y revisar accesos asignados.", "Solo admin puede cambiar permisos."),
        "auditoria" to SectionInfo("Auditoria", "🧾", "Trazabilidad", "Cambios realizados por usuarios, aprobaciones y acciones sensibles.", "Filtrar, revisar y exportar actividad.", "Solo lectura para admin."),
        "configuracion" to SectionInfo("Configuracion", "⚙️", "Parametros del centro", "Datos de la residencia, horarios, normas y opciones generales.", "Modificar configuracion y registrar nuevas residencias.", "Solo admin puede cambiar configuracion global."),
        "estadisticas" to SectionInfo("Estadisticas", "📊", "Panel general", "Indicadores de ocupacion, visitas, incidencias y carga de trabajo.", "Consultar datos, comparar periodos y exportar resumen.", "Solo admin tiene vision global."),
        "habitaciones" to SectionInfo("Habitaciones", "🛏️", "Mapa de ocupacion", "Habitaciones, ocupacion actual, bloqueos y disponibilidad.", "Asignar, mover, bloquear o liberar habitaciones.", "Solo admin puede gestionar estructura de habitaciones."),
        "gestion_documental" to SectionInfo("Documental", "🗂️", "Archivos del centro", "Documentos generales, por residente y por proceso.", "Subir, clasificar, archivar y controlar acceso documental.", "Admin tiene control documental completo."),
        "backups" to SectionInfo("Backups", "🛡️", "Seguridad y copias", "Estado de copias, recuperaciones y medidas de seguridad.", "Lanzar backups, revisar y restaurar copias.", "Solo admin por riesgo alto."),
        "notificaciones" to SectionInfo("Notificaciones", "🔔", "Avisos globales", "Mensajes enviados, plantillas y alertas programadas.", "Crear avisos generales y configurar plantillas.", "Admin controla comunicacion institucional."),
        "facturacion" to SectionInfo("Pagos", "💳", "Cobros y recibos", "Cuotas, pagos pendientes, historico y estados de facturacion.", "Registrar cobros, revisar estados y emitir justificantes.", "Solo admin o administracion financiera."),
        "logs" to SectionInfo("Logs", "🖥️", "Eventos del sistema", "Errores tecnicos, accesos, acciones sensibles y actividad interna.", "Filtrar, revisar y exportar logs.", "Solo admin.")
    )

    fun get(profile: String, key: String): SectionInfo {
        return if (profile == "ADMIN") admin[key] ?: error("Seccion no encontrada")
        else personal[key] ?: error("Seccion no encontrada")
    }
}

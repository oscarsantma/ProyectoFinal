package com.example.resiplus.database

import android.content.Context
import com.example.resiplus.BuildConfig
import com.example.resiplus.model.Residente
import com.example.resiplus.model.Usuario
import com.example.resiplus.model.Visita
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

class DatabaseHelper(@Suppress("UNUSED_PARAMETER") context: Context) {
    private val endpoint = BuildConfig.API_BASE_URL

    fun login(email: String, pass: String): Usuario? {
        val response = request("login", JSONObject().apply {
            put("email", email)
            put("password", pass)
        }) ?: return null
        return response.optJSONObject("usuario")?.toUsuario()
    }

    fun registrarUsuario(
        nombre: String,
        email: String,
        password: String,
        rol: String,
        residencia: String,
        idResidente: Int?
    ): Long {
        val response = request("registrar_usuario", JSONObject().apply {
            put("nombre", nombre)
            put("email", email)
            put("password", password)
            put("rol", rol)
            put("residencia", residencia)
            put("id_residente", idResidente)
        }) ?: return -1L
        return response.optLong("id", -1L)
    }

    fun getResidencias(): List<String> {
        val response = request("listar_residencias") ?: return emptyList()
        return response.optJSONArray("residencias").toStringList()
    }

    fun insertarResidencia(nombre: String): Long {
        val response = request("crear_residencia", JSONObject().apply { put("nombre", nombre) }) ?: return -1L
        return response.optLong("id", -1L)
    }

    fun getResidentesPorResidencia(residencia: String, incluirInactivos: Boolean = false): List<Residente> {
        val response = request("listar_residentes", JSONObject().apply {
            put("residencia", residencia)
            put("incluir_inactivos", incluirInactivos)
        }) ?: return emptyList()
        return response.optJSONArray("residentes").toResidenteList()
    }

    fun getResidente(id: Int): Residente? {
        val response = request("obtener_residente", JSONObject().apply { put("id", id) }) ?: return null
        return response.optJSONObject("residente")?.toResidente()
    }

    fun getResidenteVinculado(idUsuario: Int): Residente? {
        val response = request("obtener_residente_vinculado", JSONObject().apply { put("usuario_id", idUsuario) }) ?: return null
        return response.optJSONObject("residente")?.toResidente()
    }

    fun getUsuario(id: Int): Usuario? {
        val response = request("obtener_usuario", JSONObject().apply { put("id", id) }) ?: return null
        return response.optJSONObject("usuario")?.toUsuario()
    }

    fun getSolicitudesRegistroPendientesParaAdmin(): List<Usuario> {
        val response = request("listar_solicitudes_admin") ?: return emptyList()
        return response.optJSONArray("usuarios").toUsuarioList()
    }

    fun getSolicitudesFamiliaresPendientes(residencia: String): List<Usuario> {
        val response = request("listar_solicitudes_familiares", JSONObject().apply {
            put("residencia", residencia)
        }) ?: return emptyList()
        return response.optJSONArray("usuarios").toUsuarioList()
    }

    fun actualizarEstadoUsuario(id: Int, estado: String) {
        request("actualizar_estado_usuario", JSONObject().apply {
            put("id", id)
            put("estado", estado)
        })
    }

    fun crearUsuarioDesdeAdmin(
        nombre: String,
        email: String,
        password: String,
        rol: String,
        residencia: String,
        idResidente: Int?
    ): Long {
        val response = request("crear_usuario_admin", JSONObject().apply {
            put("nombre", nombre)
            put("email", email)
            put("password", password)
            put("rol", rol)
            put("residencia", residencia)
            put("id_residente", idResidente)
        }) ?: return -1L
        return response.optLong("id", -1L)
    }

    fun insertarResidente(
        nombre: String,
        edad: Int,
        habitacion: String,
        planta: String,
        residencia: String,
        fechaNacimiento: String,
        fechaIngreso: String,
        observaciones: String,
        necesidades: String
    ): Long {
        val response = request("guardar_residente", JSONObject().apply {
            put("nombre", nombre)
            put("edad", edad)
            put("habitacion", habitacion)
            put("planta", planta)
            put("residencia", residencia)
            put("fecha_nacimiento", fechaNacimiento)
            put("fecha_ingreso", fechaIngreso)
            put("observaciones", observaciones)
            put("necesidades", necesidades)
        }) ?: return -1L
        return response.optLong("id", -1L)
    }

    fun actualizarResidente(
        id: Int,
        nombre: String,
        edad: Int,
        habitacion: String,
        planta: String,
        fechaNacimiento: String,
        fechaIngreso: String,
        observaciones: String,
        necesidades: String
    ) {
        request("guardar_residente", JSONObject().apply {
            put("id", id)
            put("nombre", nombre)
            put("edad", edad)
            put("habitacion", habitacion)
            put("planta", planta)
            put("fecha_nacimiento", fechaNacimiento)
            put("fecha_ingreso", fechaIngreso)
            put("observaciones", observaciones)
            put("necesidades", necesidades)
        })
    }

    fun actualizarEstadoResidente(id: Int, activo: Boolean) {
        request("actualizar_estado_residente", JSONObject().apply {
            put("id", id)
            put("activo", activo)
        })
    }

    fun insertarVisita(idFam: Int, fecha: String, hora: String, nota: String): Long {
        val response = request("crear_visita", JSONObject().apply {
            put("id_familiar", idFam)
            put("fecha", fecha)
            put("hora", hora)
            put("nota", nota)
        }) ?: return -1L
        return response.optLong("id", -1L)
    }

    fun getVisitasFamiliar(idFam: Int): List<Visita> {
        val response = request("listar_visitas_familiar", JSONObject().apply { put("id_familiar", idFam) }) ?: return emptyList()
        return response.optJSONArray("visitas").toVisitaList()
    }

    fun getVisitasPendientes(residencia: String): List<Visita> {
        val response = request("listar_visitas_pendientes", JSONObject().apply { put("residencia", residencia) }) ?: return emptyList()
        return response.optJSONArray("visitas").toVisitaList()
    }

    fun getVisitasPorResidencia(residencia: String): List<Visita> {
        val response = request("listar_visitas_residencia", JSONObject().apply { put("residencia", residencia) }) ?: return emptyList()
        return response.optJSONArray("visitas").toVisitaList()
    }

    fun actualizarEstadoVisita(id: Int, estado: String) {
        request("actualizar_estado_visita", JSONObject().apply {
            put("id", id)
            put("estado", estado)
        })
    }

    fun getHorasOcupadas(fecha: String, idResidente: Int): List<String> {
        val response = request("listar_horas_ocupadas", JSONObject().apply {
            put("fecha", fecha)
            put("id_residente", idResidente)
        }) ?: return emptyList()
        return response.optJSONArray("horas").toStringList()
    }

    fun contarPendientes(residencia: String): Int {
        return request("resumen_personal", JSONObject().apply { put("residencia", residencia) })
            ?.optInt("citas_pendientes", 0) ?: 0
    }

    fun contarSolicitudesFamiliaresPendientes(residencia: String): Int {
        return request("resumen_personal", JSONObject().apply { put("residencia", residencia) })
            ?.optInt("familiares_pendientes", 0) ?: 0
    }

    fun contarSolicitudesPersonalPendientes(): Int {
        return request("resumen_admin")?.optInt("personal_pendiente", 0) ?: 0
    }

    fun contarVisitasHoy(residencia: String): Int {
        return request("resumen_personal", JSONObject().apply { put("residencia", residencia) })
            ?.optInt("visitas_hoy", 0) ?: 0
    }

    fun contarResidencias(): Int {
        return request("resumen_admin")?.optInt("total_residencias", 0) ?: 0
    }

    fun contarResidentesTotales(): Int {
        return request("resumen_admin")?.optInt("total_residentes", 0) ?: 0
    }

    fun contarResidentesPorResidencia(residencia: String): Int {
        return request("contar_residentes_residencia", JSONObject().apply { put("residencia", residencia) })
            ?.optInt("total", 0) ?: 0
    }

    fun insertarMensaje(emisor: Int, receptor: Int, texto: String, hora: String) {
        request("insertar_mensaje", JSONObject().apply {
            put("emisor", emisor)
            put("receptor", receptor)
            put("texto", texto)
            put("hora", hora)
        })
    }

    fun getMensajes(idUsuario1: Int, idUsuario2: Int): List<Triple<String, String, Boolean>> {
        val response = request("listar_mensajes", JSONObject().apply {
            put("id_usuario_1", idUsuario1)
            put("id_usuario_2", idUsuario2)
        }) ?: return emptyList()
        val mensajes = response.optJSONArray("mensajes") ?: return emptyList()
        return buildList {
            for (i in 0 until mensajes.length()) {
                val item = mensajes.optJSONObject(i) ?: continue
                add(
                    Triple(
                        item.optString("texto"),
                        item.optString("hora"),
                        item.optBoolean("es_emisor")
                    )
                )
            }
        }
    }

    private fun request(action: String, payload: JSONObject = JSONObject()): JSONObject? {
        val result = AtomicReference<JSONObject?>()
        val latch = CountDownLatch(1)

        Thread {
            try {
                val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    connectTimeout = 12000
                    readTimeout = 12000
                    doInput = true
                    doOutput = true
                    setRequestProperty("Content-Type", "application/json; charset=utf-8")
                    setRequestProperty("Accept", "application/json")
                }

                val body = JSONObject(payload.toString()).apply { put("action", action) }
                OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { it.write(body.toString()) }

                val stream = if (connection.responseCode in 200..299) {
                    connection.inputStream
                } else {
                    connection.errorStream
                }

                val text = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
                val envelope = if (text.isNotBlank()) JSONObject(text) else JSONObject()
                result.set(if (envelope.optBoolean("ok")) envelope.optJSONObject("data") else null)
                connection.disconnect()
            } catch (_: Exception) {
                result.set(null)
            } finally {
                latch.countDown()
            }
        }.start()

        latch.await(15, TimeUnit.SECONDS)
        return result.get()
    }

    private fun JSONArray?.toStringList(): List<String> {
        if (this == null) return emptyList()
        return List(length()) { index -> optString(index) }
    }

    private fun JSONArray?.toUsuarioList(): List<Usuario> {
        if (this == null) return emptyList()
        return buildList {
            for (i in 0 until length()) {
                optJSONObject(i)?.toUsuario()?.let(::add)
            }
        }
    }

    private fun JSONArray?.toResidenteList(): List<Residente> {
        if (this == null) return emptyList()
        return buildList {
            for (i in 0 until length()) {
                optJSONObject(i)?.toResidente()?.let(::add)
            }
        }
    }

    private fun JSONArray?.toVisitaList(): List<Visita> {
        if (this == null) return emptyList()
        return buildList {
            for (i in 0 until length()) {
                optJSONObject(i)?.toVisita()?.let(::add)
            }
        }
    }

    private fun JSONObject.toUsuario(): Usuario {
        val residenteId = if (isNull("id_residente")) null else optInt("id_residente")
        return Usuario(
            id = optInt("id"),
            nombre = optString("nombre"),
            email = optString("email"),
            password = optString("password"),
            rol = optString("rol"),
            residencia = optString("residencia"),
            estado = optString("estado"),
            idResidente = residenteId
        )
    }

    private fun JSONObject.toResidente(): Residente {
        return Residente(
            id = optInt("id"),
            nombre = optString("nombre"),
            edad = optInt("edad"),
            habitacion = optString("habitacion"),
            planta = optString("planta"),
            residencia = optString("residencia"),
            fechaNacimiento = optString("fecha_nacimiento"),
            fechaIngreso = optString("fecha_ingreso"),
            observaciones = optString("observaciones"),
            necesidades = optString("necesidades"),
            activo = optInt("activo", 1) == 1
        )
    }

    private fun JSONObject.toVisita(): Visita {
        return Visita(
            id = optInt("id"),
            idFamiliar = optInt("id_familiar"),
            nombreFamiliar = optString("nombre_familiar"),
            fecha = optString("fecha"),
            hora = optString("hora"),
            estado = optString("estado"),
            nota = optString("nota"),
            nombreResidente = optString("nombre_residente")
        )
    }
}

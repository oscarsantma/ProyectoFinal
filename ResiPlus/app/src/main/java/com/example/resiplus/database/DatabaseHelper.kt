package com.example.resiplus.database

import android.content.Context
import com.example.resiplus.BuildConfig
import com.example.resiplus.model.Residente
import com.example.resiplus.model.Usuario
import com.example.resiplus.model.Visita
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

// clase que gestiona todas las llamadas a la api rest
// uso POST con json para todo el backend devuelve siempre ok, data
class DatabaseHelper(context: Context) {

    private val apiUrl = BuildConfig.API_BASE_URL

    fun login(email: String, pass: String): Usuario? {
        val json = JSONObject()
        json.put("email", email)
        json.put("password", pass)
        val r = request("login", json) ?: return null
        return r.optJSONObject("usuario")?.toUsuario()
    }

    fun registrarUsuario(nombre: String, email: String, password: String,
                         rol: String, residencia: String, idResidente: Int?): Long {
        val json = JSONObject()
        json.put("nombre", nombre); json.put("email", email)
        json.put("password", password); json.put("rol", rol)
        json.put("residencia", residencia); json.put("id_residente", idResidente)
        return request("registrar_usuario", json)?.optLong("id", -1L) ?: -1L
    }

    fun getResidencias(): List<String> {
        val r = request("listar_residencias") ?: return emptyList()
        return r.optJSONArray("residencias").toStringList()
    }

    fun insertarResidencia(nombre: String): Long {
        val json = JSONObject(); json.put("nombre", nombre)
        return request("crear_residencia", json)?.optLong("id", -1L) ?: -1L
    }

    fun getResidentesPorResidencia(residencia: String, incluirInactivos: Boolean = false): List<Residente> {
        val json = JSONObject()
        json.put("residencia", residencia)
        json.put("incluir_inactivos", incluirInactivos)
        val r = request("listar_residentes", json) ?: return emptyList()
        return r.optJSONArray("residentes").toResidenteList()
    }

    fun getResidente(id: Int): Residente? {
        val json = JSONObject(); json.put("id", id)
        return request("obtener_residente", json)?.optJSONObject("residente")?.toResidente()
    }

    fun getResidenteVinculado(idUsuario: Int): Residente? {
        val json = JSONObject(); json.put("usuario_id", idUsuario)
        return request("obtener_residente_vinculado", json)?.optJSONObject("residente")?.toResidente()
    }

    fun getUsuario(id: Int): Usuario? {
        val json = JSONObject(); json.put("id", id)
        return request("obtener_usuario", json)?.optJSONObject("usuario")?.toUsuario()
    }

    fun getSolicitudesRegistroPendientesParaAdmin(): List<Usuario> {
        val resp = request("listar_solicitudes_admin") ?: return emptyList()
        return resp.optJSONArray("usuarios").toUsuarioList()
    }

    fun getSolicitudesFamiliaresPendientes(residencia: String): List<Usuario> {
        val json = JSONObject(); json.put("residencia", residencia)
        val resp = request("listar_solicitudes_familiares", json) ?: return emptyList()
        return resp.optJSONArray("usuarios").toUsuarioList()
    }

    fun actualizarEstadoUsuario(id: Int, estado: String) {
        val json = JSONObject(); json.put("id", id); json.put("estado", estado)
        request("actualizar_estado_usuario", json)
    }

    fun crearUsuarioDesdeAdmin(nombre: String, email: String, password: String,
                               rol: String, residencia: String, idResidente: Int?): Long {
        val json = JSONObject()
        json.put("nombre", nombre); json.put("email", email)
        json.put("password", password); json.put("rol", rol)
        json.put("residencia", residencia); json.put("id_residente", idResidente)
        return request("crear_usuario_admin", json)?.optLong("id", -1L) ?: -1L
    }

    fun insertarResidente(nombre: String, edad: Int, habitacion: String, planta: String,
                          residencia: String, fechaNacimiento: String, fechaIngreso: String,
                          observaciones: String, necesidades: String): Long {
        val json = JSONObject()
        json.put("nombre", nombre); json.put("edad", edad)
        json.put("habitacion", habitacion); json.put("planta", planta)
        json.put("residencia", residencia)
        json.put("fecha_nacimiento", fechaNacimiento); json.put("fecha_ingreso", fechaIngreso)
        json.put("observaciones", observaciones); json.put("necesidades", necesidades)
        return request("guardar_residente", json)?.optLong("id", -1L) ?: -1L
    }

    fun actualizarResidente(id: Int, nombre: String, edad: Int, habitacion: String,
                            planta: String, fechaNacimiento: String, fechaIngreso: String,
                            observaciones: String, necesidades: String) {
        val json = JSONObject()
        json.put("id", id); json.put("nombre", nombre); json.put("edad", edad)
        json.put("habitacion", habitacion); json.put("planta", planta)
        json.put("fecha_nacimiento", fechaNacimiento); json.put("fecha_ingreso", fechaIngreso)
        json.put("observaciones", observaciones); json.put("necesidades", necesidades)
        request("guardar_residente", json)
    }

    fun actualizarEstadoResidente(id: Int, activo: Boolean) {
        val j = JSONObject(); j.put("id", id); j.put("activo", activo)
        request("actualizar_estado_residente", j)
    }

    fun insertarVisita(idFam: Int, fecha: String, hora: String, nota: String): Long {
        val json = JSONObject()
        json.put("id_familiar", idFam); json.put("fecha", fecha)
        json.put("hora", hora); json.put("nota", nota)
        return request("crear_visita", json)?.optLong("id", -1L) ?: -1L
    }

    fun getVisitasFamiliar(idFam: Int): List<Visita> {
        val json = JSONObject(); json.put("id_familiar", idFam)
        return request("listar_visitas_familiar", json)?.optJSONArray("visitas").toVisitaList()
    }

    fun getVisitasPendientes(residencia: String): List<Visita> {
        val json = JSONObject(); json.put("residencia", residencia)
        return request("listar_visitas_pendientes", json)?.optJSONArray("visitas").toVisitaList()
    }

    fun getVisitasPorResidencia(residencia: String): List<Visita> {
        val json = JSONObject(); json.put("residencia", residencia)
        return request("listar_visitas_residencia", json)?.optJSONArray("visitas").toVisitaList()
    }

    fun actualizarEstadoVisita(id: Int, estado: String) {
        val json = JSONObject(); json.put("id", id); json.put("estado", estado)
        request("actualizar_estado_visita", json)
    }

    fun getHorasOcupadas(fecha: String, idResidente: Int): List<String> {
        val json = JSONObject(); json.put("fecha", fecha); json.put("id_residente", idResidente)
        return request("listar_horas_ocupadas", json)?.optJSONArray("horas").toStringList()
    }

    fun contarPendientes(residencia: String): Int {
        val j = JSONObject(); j.put("residencia", residencia)
        return request("resumen_personal", j)?.optInt("citas_pendientes", 0) ?: 0
    }

    fun contarSolicitudesFamiliaresPendientes(residencia: String): Int {
        val j = JSONObject(); j.put("residencia", residencia)
        return request("resumen_personal", j)?.optInt("familiares_pendientes", 0) ?: 0
    }

    fun contarSolicitudesPersonalPendientes(): Int {
        return request("resumen_admin")?.optInt("personal_pendiente", 0) ?: 0
    }

    fun contarVisitasHoy(residencia: String): Int {
        val j = JSONObject(); j.put("residencia", residencia)
        return request("resumen_personal", j)?.optInt("visitas_hoy", 0) ?: 0
    }

    fun contarResidencias() = request("resumen_admin")?.optInt("total_residencias", 0) ?: 0

    fun contarResidentesTotales() = request("resumen_admin")?.optInt("total_residentes", 0) ?: 0

    fun contarResidentesPorResidencia(residencia: String): Int {
        val j = JSONObject(); j.put("residencia", residencia)
        return request("contar_residentes_residencia", j)?.optInt("total", 0) ?: 0
    }

    fun insertarMensaje(emisor: Int, receptor: Int, texto: String, hora: String) {
        val json = JSONObject()
        json.put("emisor", emisor); json.put("receptor", receptor)
        json.put("texto", texto); json.put("hora", hora)
        request("insertar_mensaje", json)
    }

    fun getMensajes(idUsuario1: Int, idUsuario2: Int): List<Triple<String, String, Boolean>> {
        val json = JSONObject()
        json.put("id_usuario_1", idUsuario1); json.put("id_usuario_2", idUsuario2)
        val resp = request("listar_mensajes", json) ?: return emptyList()
        val arr = resp.optJSONArray("mensajes") ?: return emptyList()
        val lista = mutableListOf<Triple<String, String, Boolean>>()
        for (i in 0 until arr.length()) {
            val m = arr.optJSONObject(i) ?: continue
            lista.add(Triple(m.optString("texto"), m.optString("hora"), m.optBoolean("es_emisor")))
        }
        return lista
    }

    // peticion http a la api - bloquea el hilo hasta que responda
    private fun request(action: String, payload: JSONObject = JSONObject()): JSONObject? {
        val ref = AtomicReference<JSONObject?>()
        val latch = CountDownLatch(1)

        Thread {
            try {
                val conn = URL(apiUrl).openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.connectTimeout = 12000
                conn.readTimeout = 12000
                conn.doInput = true
                conn.doOutput = true
                conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                conn.setRequestProperty("Accept", "application/json")

                payload.put("action", action)
                val writer = OutputStreamWriter(conn.outputStream, Charsets.UTF_8)
                writer.write(payload.toString())
                writer.flush()
                writer.close()

                val stream = if (conn.responseCode in 200..299) conn.inputStream else conn.errorStream
                val texto = stream?.bufferedReader(Charsets.UTF_8)?.readText() ?: ""
                if (texto.isBlank()) { ref.set(null); return@Thread }

                val envelope = JSONObject(texto)
                ref.set(if (envelope.optBoolean("ok")) envelope.optJSONObject("data") else null)
                conn.disconnect()
            } catch (e: Exception) {
                // Por si falla la red o el servidor no responde
                ref.set(null)
            } finally {
                latch.countDown()
            }
        }.start()

        latch.await(15, TimeUnit.SECONDS)
        return ref.get()
    }

    private fun JSONArray?.toStringList(): List<String> {
        this ?: return emptyList()
        val list = mutableListOf<String>()
        for (i in 0 until length()) list.add(optString(i))
        return list
    }

    private fun JSONArray?.toUsuarioList(): List<Usuario> {
        this ?: return emptyList()
        val list = mutableListOf<Usuario>()
        for (i in 0 until length()) {
            optJSONObject(i)?.toUsuario()?.let { list.add(it) }
        }
        return list
    }

    private fun JSONArray?.toResidenteList(): List<Residente> {
        this ?: return emptyList()
        val list = mutableListOf<Residente>()
        for (i in 0 until length()) {
            optJSONObject(i)?.toResidente()?.let { list.add(it) }
        }
        return list
    }

    private fun JSONArray?.toVisitaList(): List<Visita> {
        this ?: return emptyList()
        val list = mutableListOf<Visita>()
        for (i in 0 until length()) {
            optJSONObject(i)?.toVisita()?.let { list.add(it) }
        }
        return list
    }

    private fun JSONObject.toUsuario(): Usuario {
        val resId = if (isNull("id_residente")) null else optInt("id_residente")
        return Usuario(optInt("id"), optString("nombre"), optString("email"),
            optString("password"), optString("rol"), optString("residencia"),
            optString("estado"), resId)
    }

    private fun JSONObject.toResidente() = Residente(
        id = optInt("id"), nombre = optString("nombre"), edad = optInt("edad"),
        habitacion = optString("habitacion"), planta = optString("planta"),
        residencia = optString("residencia"), fechaNacimiento = optString("fecha_nacimiento"),
        fechaIngreso = optString("fecha_ingreso"), observaciones = optString("observaciones"),
        necesidades = optString("necesidades"), activo = optInt("activo", 1) == 1
    )

    private fun JSONObject.toVisita() = Visita(
        id = optInt("id"), idFamiliar = optInt("id_familiar"),
        nombreFamiliar = optString("nombre_familiar"), fecha = optString("fecha"),
        hora = optString("hora"), estado = optString("estado"),
        nota = optString("nota"), nombreResidente = optString("nombre_residente")
    )
}

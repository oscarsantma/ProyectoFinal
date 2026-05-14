<?php
header("Content-Type: application/json; charset=utf-8");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: POST");
header("Access-Control-Allow-Headers: Content-Type");

// datos de conexion a la base de datos
$host   = "db.hosts";
$dbname = "4754766_resiplus";
$dbuser = "4754766_resiplus";
$dbpass = "mi contraseña";

try {
    $pdo = new PDO("mysql:host=$host;port=3306;dbname=$dbname;charset=utf8", $dbuser, $dbpass);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
} catch (PDOException $e) {
    responder(false, ["error" => "Error de conexion con la base de datos"]);
    exit;
}

$body = json_decode(file_get_contents("php://input"), true);
if (!$body || !isset($body["action"])) {
    responder(false, ["error" => "Peticion invalida"]);
    exit;
}

$action = $body["action"];

// enrutamos segun la accion recibida
switch ($action) {
    case "login": login($pdo, $body); break;
    case "registrar_usuario": registrarUsuario($pdo, $body); break;
    case "listar_residencias": listarResidencias($pdo); break;
    case "crear_residencia": crearResidencia($pdo, $body); break;
    case "listar_residentes": listarResidentes($pdo, $body); break;
    case "obtener_residente": obtenerResidente($pdo, $body); break;
    case "obtener_residente_vinculado": obtenerResidenteVinculado($pdo, $body); break;
    case "obtener_usuario": obtenerUsuario($pdo, $body); break;
    case "listar_solicitudes_admin": listarSolicitudesAdmin($pdo); break;
    case "listar_solicitudes_familiares": listarSolicitudesFamiliares($pdo, $body); break;
    case "actualizar_estado_usuario": actualizarEstadoUsuario($pdo, $body); break;
    case "crear_usuario_admin": crearUsuarioAdmin($pdo, $body); break;
    case "guardar_residente": guardarResidente($pdo, $body); break;
    case "actualizar_estado_residente": actualizarEstadoResidente($pdo, $body); break;
    case "crear_visita": crearVisita($pdo, $body); break;
    case "listar_visitas_familiar": listarVisitasFamiliar($pdo, $body); break;
    case "listar_visitas_pendientes": listarVisitasPendientes($pdo, $body); break;
    case "listar_visitas_residencia": listarVisitasResidencia($pdo, $body); break;
    case "actualizar_estado_visita": actualizarEstadoVisita($pdo, $body); break;
    case "listar_horas_ocupadas": listarHorasOcupadas($pdo, $body); break;
    case "resumen_personal": resumenPersonal($pdo, $body); break;
    case "resumen_admin": resumenAdmin($pdo); break;
    case "contar_residentes_residencia": contarResidentesResidencia($pdo, $body); break;
    case "insertar_mensaje": insertarMensaje($pdo, $body); break;
    case "listar_mensajes": listarMensajes($pdo, $body); break;
    default:
        responder(false, ["error" => "Accion desconocida: $action"]);
}

function responder($ok, $data = []) {
    $res = ["ok" => $ok, "data" => $data];
    echo json_encode($res, JSON_UNESCAPED_UNICODE);
}

// --- usuarios ---

function login($pdo, $data) {
    $email = $data["email"] ?? "";
    $pass  = $data["password"] ?? "";

    $stmt = $pdo->prepare("SELECT * FROM usuarios WHERE email = ?");
    $stmt->execute([$email]);
    $u = $stmt->fetch(PDO::FETCH_ASSOC);

    if (!$u || $u["password"] !== $pass) {
        responder(false, ["error" => "Credenciales incorrectas"]);
        return;
    }
    responder(true, ["usuario" => $u]);
}

function registrarUsuario($pdo, $data) {
    $nombre = $data["nombre"] ?? "";
    $email  = $data["email"] ?? "";
    $pass   = $data["password"] ?? "";
    $rol    = $data["rol"] ?? "";
    $res    = $data["residencia"] ?? "";
    $idRes  = $data["id_residente"] ?? null;

    // comprobar si ya existe ese email
    $check = $pdo->prepare("SELECT id FROM usuarios WHERE email = ?");
    $check->execute([$email]);
    if ($check->fetch()) {
        responder(false, ["error" => "Ya existe una cuenta con ese email"]);
        return;
    }

    $estado = ($rol === "ADMIN") ? "APROBADO" : "PENDIENTE";
    $stmt = $pdo->prepare("INSERT INTO usuarios (nombre, email, password, rol, residencia, estado, id_residente) VALUES (?,?,?,?,?,?,?)");
    $stmt->execute([$nombre, $email, $pass, $rol, $res, $estado, $idRes]);
    responder(true, ["id" => (int)$pdo->lastInsertId()]);
}

function obtenerUsuario($pdo, $data) {
    $id = $data["id"] ?? 0;
    $stmt = $pdo->prepare("SELECT * FROM usuarios WHERE id = ?");
    $stmt->execute([$id]);
    $u = $stmt->fetch(PDO::FETCH_ASSOC);
    if (!$u) {
        responder(false, ["error" => "Usuario no encontrado"]);
        return;
    }
    responder(true, ["usuario" => $u]);
}

function listarSolicitudesAdmin($pdo) {
    $stmt = $pdo->query("SELECT * FROM usuarios WHERE rol = 'PERSONAL' AND estado = 'PENDIENTE' ORDER BY id DESC");
    responder(true, ["usuarios" => $stmt->fetchAll(PDO::FETCH_ASSOC)]);
}

function listarSolicitudesFamiliares($pdo, $data) {
    $res = $data["residencia"] ?? "";
    $stmt = $pdo->prepare("SELECT * FROM usuarios WHERE rol = 'FAMILIAR' AND estado = 'PENDIENTE' AND residencia = ? ORDER BY id DESC");
    $stmt->execute([$res]);
    responder(true, ["usuarios" => $stmt->fetchAll(PDO::FETCH_ASSOC)]);
}

function actualizarEstadoUsuario($pdo, $data) {
    $id     = $data["id"] ?? 0;
    $estado = $data["estado"] ?? "";
    $stmt = $pdo->prepare("UPDATE usuarios SET estado = ? WHERE id = ?");
    $stmt->execute([$estado, $id]);
    responder(true, []);
}

function crearUsuarioAdmin($pdo, $data) {
    $nombre = $data["nombre"] ?? "";
    $email  = $data["email"] ?? "";
    $pass   = $data["password"] ?? "";
    $rol    = $data["rol"] ?? "";
    $res    = $data["residencia"] ?? "";
    $idRes  = $data["id_residente"] ?? null;

    $stmt = $pdo->prepare("INSERT INTO usuarios (nombre, email, password, rol, residencia, estado, id_residente) VALUES (?,?,?,?,?,'APROBADO',?)");
    $stmt->execute([$nombre, $email, $pass, $rol, $res, $idRes]);
    responder(true, ["id" => (int)$pdo->lastInsertId()]);
}

// --- residencias ---

function listarResidencias($pdo) {
    $stmt = $pdo->query("SELECT nombre FROM residencias ORDER BY nombre");
    responder(true, ["residencias" => $stmt->fetchAll(PDO::FETCH_COLUMN)]);
}

function crearResidencia($pdo, $data) {
    $nombre = $data["nombre"] ?? "";
    $stmt = $pdo->prepare("INSERT INTO residencias (nombre) VALUES (?)");
    $stmt->execute([$nombre]);
    responder(true, ["id" => (int)$pdo->lastInsertId()]);
}

// --- residentes ---

function listarResidentes($pdo, $data) {
    $res = $data["residencia"] ?? "";
    $inclInactivos = $data["incluir_inactivos"] ?? false;

    $sql = "SELECT * FROM residentes WHERE residencia = ?";
    if (!$inclInactivos) $sql .= " AND activo = 1";
    $sql .= " ORDER BY nombre";

    $stmt = $pdo->prepare($sql);
    $stmt->execute([$res]);
    responder(true, ["residentes" => $stmt->fetchAll(PDO::FETCH_ASSOC)]);
}

function obtenerResidente($pdo, $data) {
    $id = $data["id"] ?? 0;
    $stmt = $pdo->prepare("SELECT * FROM residentes WHERE id = ?");
    $stmt->execute([$id]);
    $r = $stmt->fetch(PDO::FETCH_ASSOC);
    if (!$r) { responder(false, ["error" => "Residente no encontrado"]); return; }
    responder(true, ["residente" => $r]);
}

function obtenerResidenteVinculado($pdo, $data) {
    $uid = $data["usuario_id"] ?? 0;

    $stmt = $pdo->prepare("SELECT id_residente FROM usuarios WHERE id = ?");
    $stmt->execute([$uid]);
    $row = $stmt->fetch(PDO::FETCH_ASSOC);

    if (!$row || !$row["id_residente"]) {
        responder(false, ["error" => "Sin residente vinculado"]);
        return;
    }

    $stmt2 = $pdo->prepare("SELECT * FROM residentes WHERE id = ?");
    $stmt2->execute([$row["id_residente"]]);
    $r = $stmt2->fetch(PDO::FETCH_ASSOC);
    responder(true, ["residente" => $r]);
}

function guardarResidente($pdo, $data) {
    $id      = $data["id"] ?? null;
    $nombre  = $data["nombre"] ?? "";
    $edad    = $data["edad"] ?? 0;
    $hab     = $data["habitacion"] ?? "";
    $planta  = $data["planta"] ?? "";
    $res     = $data["residencia"] ?? "";
    $fNac    = $data["fecha_nacimiento"] ?: null;
    $fIng    = $data["fecha_ingreso"] ?: null;
    $obs     = $data["observaciones"] ?? "";
    $nec     = $data["necesidades"] ?? "";

    if ($id) {
        $stmt = $pdo->prepare("UPDATE residentes SET nombre=?, edad=?, habitacion=?, planta=?, fecha_nacimiento=?, fecha_ingreso=?, observaciones=?, necesidades=? WHERE id=?");
        $stmt->execute([$nombre, $edad, $hab, $planta, $fNac, $fIng, $obs, $nec, $id]);
        responder(true, ["id" => (int)$id]);
    } else {
        $stmt = $pdo->prepare("INSERT INTO residentes (nombre, edad, habitacion, planta, residencia, fecha_nacimiento, fecha_ingreso, observaciones, necesidades) VALUES (?,?,?,?,?,?,?,?,?)");
        $stmt->execute([$nombre, $edad, $hab, $planta, $res, $fNac, $fIng, $obs, $nec]);
        responder(true, ["id" => (int)$pdo->lastInsertId()]);
    }
}

function actualizarEstadoResidente($pdo, $data) {
    $id     = $data["id"] ?? 0;
    $activo = ($data["activo"] ?? false) ? 1 : 0;
    $stmt = $pdo->prepare("UPDATE residentes SET activo = ? WHERE id = ?");
    $stmt->execute([$activo, $id]);
    responder(true, []);
}

// --- visitas ---

function crearVisita($pdo, $data) {
    $idFam = $data["id_familiar"] ?? 0;
    $fecha = $data["fecha"] ?? "";
    $hora  = $data["hora"] ?? "";
    $nota  = $data["nota"] ?? "";

    $stmt = $pdo->prepare("INSERT INTO visitas (id_familiar, fecha, hora, nota, estado) VALUES (?,?,?,?,'PENDIENTE')");
    $stmt->execute([$idFam, $fecha, $hora, $nota]);
    responder(true, ["id" => (int)$pdo->lastInsertId()]);
}

function listarVisitasFamiliar($pdo, $data) {
    $idFam = $data["id_familiar"] ?? 0;
    $stmt = $pdo->prepare("
        SELECT v.*, u.nombre AS nombre_familiar, r.nombre AS nombre_residente
        FROM visitas v
        JOIN usuarios u ON v.id_familiar = u.id
        LEFT JOIN residentes r ON u.id_residente = r.id
        WHERE v.id_familiar = ?
        ORDER BY v.fecha DESC, v.hora DESC
    ");
    $stmt->execute([$idFam]);
    responder(true, ["visitas" => $stmt->fetchAll(PDO::FETCH_ASSOC)]);
}

function listarVisitasPendientes($pdo, $data) {
    $res = $data["residencia"] ?? "";
    $stmt = $pdo->prepare("
        SELECT v.*, u.nombre AS nombre_familiar, r.nombre AS nombre_residente
        FROM visitas v
        JOIN usuarios u ON v.id_familiar = u.id
        LEFT JOIN residentes r ON u.id_residente = r.id
        WHERE u.residencia = ? AND v.estado = 'PENDIENTE'
        ORDER BY v.fecha ASC, v.hora ASC
    ");
    $stmt->execute([$res]);
    responder(true, ["visitas" => $stmt->fetchAll(PDO::FETCH_ASSOC)]);
}

function listarVisitasResidencia($pdo, $data) {
    $res = $data["residencia"] ?? "";
    $stmt = $pdo->prepare("
        SELECT v.*, u.nombre AS nombre_familiar, r.nombre AS nombre_residente
        FROM visitas v
        JOIN usuarios u ON v.id_familiar = u.id
        LEFT JOIN residentes r ON u.id_residente = r.id
        WHERE u.residencia = ?
        ORDER BY v.fecha DESC, v.hora DESC
    ");
    $stmt->execute([$res]);
    responder(true, ["visitas" => $stmt->fetchAll(PDO::FETCH_ASSOC)]);
}

function actualizarEstadoVisita($pdo, $data) {
    $id     = $data["id"] ?? 0;
    $estado = $data["estado"] ?? "";
    $stmt = $pdo->prepare("UPDATE visitas SET estado = ? WHERE id = ?");
    $stmt->execute([$estado, $id]);
    responder(true, []);
}

function listarHorasOcupadas($pdo, $data) {
    $fecha   = $data["fecha"] ?? "";
    $idRes   = $data["id_residente"] ?? 0;

    $stmt = $pdo->prepare("
        SELECT v.hora FROM visitas v
        JOIN usuarios u ON v.id_familiar = u.id
        WHERE u.id_residente = ? AND v.fecha = ? AND v.estado != 'RECHAZADA'
    ");
    $stmt->execute([$idRes, $fecha]);
    responder(true, ["horas" => $stmt->fetchAll(PDO::FETCH_COLUMN)]);
}

// --- resumenes para los dashboards ---

function resumenPersonal($pdo, $data) {
    $res = $data["residencia"] ?? "";
    $hoy = date("Y-m-d");

    $s1 = $pdo->prepare("SELECT COUNT(*) FROM visitas v JOIN usuarios u ON v.id_familiar = u.id WHERE u.residencia = ? AND v.estado = 'PENDIENTE'");
    $s1->execute([$res]);

    $s2 = $pdo->prepare("SELECT COUNT(*) FROM visitas v JOIN usuarios u ON v.id_familiar = u.id WHERE u.residencia = ? AND v.fecha = ? AND v.estado = 'APROBADA'");
    $s2->execute([$res, $hoy]);

    $s3 = $pdo->prepare("SELECT COUNT(*) FROM usuarios WHERE residencia = ? AND rol = 'FAMILIAR' AND estado = 'PENDIENTE'");
    $s3->execute([$res]);

    responder(true, [
        "citas_pendientes"      => (int)$s1->fetchColumn(),
        "visitas_hoy"           => (int)$s2->fetchColumn(),
        "familiares_pendientes" => (int)$s3->fetchColumn()
    ]);
}

function resumenAdmin($pdo) {
    $totalRes     = (int)$pdo->query("SELECT COUNT(*) FROM residencias")->fetchColumn();
    $totalResid   = (int)$pdo->query("SELECT COUNT(*) FROM residentes WHERE activo = 1")->fetchColumn();
    $personalPend = (int)$pdo->query("SELECT COUNT(*) FROM usuarios WHERE rol = 'PERSONAL' AND estado = 'PENDIENTE'")->fetchColumn();

    responder(true, [
        "total_residencias"  => $totalRes,
        "total_residentes"   => $totalResid,
        "personal_pendiente" => $personalPend
    ]);
}

function contarResidentesResidencia($pdo, $data) {
    $res = $data["residencia"] ?? "";
    $stmt = $pdo->prepare("SELECT COUNT(*) FROM residentes WHERE residencia = ? AND activo = 1");
    $stmt->execute([$res]);
    responder(true, ["total" => (int)$stmt->fetchColumn()]);
}

// --- mensajeria ---

function insertarMensaje($pdo, $data) {
    $emisor   = $data["emisor"] ?? 0;
    $receptor = $data["receptor"] ?? 0;
    $texto    = $data["texto"] ?? "";
    $hora     = $data["hora"] ?? "";

    $stmt = $pdo->prepare("INSERT INTO mensajes (emisor, receptor, texto, hora) VALUES (?,?,?,?)");
    $stmt->execute([$emisor, $receptor, $texto, $hora]);
    responder(true, ["id" => (int)$pdo->lastInsertId()]);
}

function listarMensajes($pdo, $data) {
    $id1 = $data["id_usuario_1"] ?? 0;
    $id2 = $data["id_usuario_2"] ?? 0;

    $stmt = $pdo->prepare("
        SELECT texto, hora,
               CASE WHEN emisor = ? THEN 1 ELSE 0 END AS es_emisor
        FROM mensajes
        WHERE (emisor = ? AND receptor = ?) OR (emisor = ? AND receptor = ?)
        ORDER BY id ASC
    ");
    $stmt->execute([$id1, $id1, $id2, $id2, $id1]);
    $msgs = $stmt->fetchAll(PDO::FETCH_ASSOC);

    foreach ($msgs as &$m) {
        $m["es_emisor"] = (bool)$m["es_emisor"];
    }

    responder(true, ["mensajes" => $msgs]);
}

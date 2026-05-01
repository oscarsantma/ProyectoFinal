<?php

declare(strict_types=1);

header('Content-Type: application/json; charset=utf-8');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Headers: Content-Type, Authorization');
header('Access-Control-Allow-Methods: POST, OPTIONS');

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(204);
    exit;
}

$config = require __DIR__ . '/config.php';

try {
    $pdo = new PDO(
        sprintf(
            'mysql:host=%s;dbname=%s;charset=%s',
            $config['db_host'],
            $config['db_name'],
            $config['db_charset']
        ),
        $config['db_user'],
        $config['db_pass'],
        [
            PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
            PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
        ]
    );
} catch (Throwable $error) {
    respond(false, null, 'No se pudo conectar con la base de datos.', 500);
}

$input = json_decode(file_get_contents('php://input') ?: '{}', true);
if (!is_array($input)) {
    respond(false, null, 'JSON no válido.', 400);
}

$action = $input['action'] ?? null;
if (!$action) {
    respond(false, null, 'Acción no indicada.', 400);
}

try {
    switch ($action) {
        case 'login':
            login($pdo, $input);
            break;
        case 'registrar_usuario':
            registrarUsuario($pdo, $input);
            break;
        case 'listar_residencias':
            listarResidencias($pdo);
            break;
        case 'crear_residencia':
            crearResidencia($pdo, $input);
            break;
        case 'listar_residentes':
            listarResidentes($pdo, $input);
            break;
        case 'obtener_residente':
            obtenerResidente($pdo, $input);
            break;
        case 'obtener_residente_vinculado':
            obtenerResidenteVinculado($pdo, $input);
            break;
        case 'obtener_usuario':
            obtenerUsuario($pdo, $input);
            break;
        case 'listar_solicitudes_admin':
            listarSolicitudesAdmin($pdo);
            break;
        case 'listar_solicitudes_familiares':
            listarSolicitudesFamiliares($pdo, $input);
            break;
        case 'actualizar_estado_usuario':
            actualizarEstadoUsuario($pdo, $input);
            break;
        case 'crear_usuario_admin':
            crearUsuarioAdmin($pdo, $input);
            break;
        case 'guardar_residente':
            guardarResidente($pdo, $input);
            break;
        case 'actualizar_estado_residente':
            actualizarEstadoResidente($pdo, $input);
            break;
        case 'crear_visita':
            crearVisita($pdo, $input);
            break;
        case 'listar_visitas_familiar':
            listarVisitasFamiliar($pdo, $input);
            break;
        case 'listar_visitas_pendientes':
            listarVisitasPendientes($pdo, $input);
            break;
        case 'listar_visitas_residencia':
            listarVisitasResidencia($pdo, $input);
            break;
        case 'actualizar_estado_visita':
            actualizarEstadoVisita($pdo, $input);
            break;
        case 'listar_horas_ocupadas':
            listarHorasOcupadas($pdo, $input);
            break;
        case 'resumen_personal':
            resumenPersonal($pdo, $input);
            break;
        case 'resumen_admin':
            resumenAdmin($pdo);
            break;
        case 'contar_residentes_residencia':
            contarResidentesResidencia($pdo, $input);
            break;
        case 'insertar_mensaje':
            insertarMensaje($pdo, $input);
            break;
        case 'listar_mensajes':
            listarMensajes($pdo, $input);
            break;
        default:
            respond(false, null, 'Acción no reconocida.', 404);
    }
} catch (Throwable $error) {
    respond(false, null, $error->getMessage(), 500);
}

function login(PDO $pdo, array $input): void
{
    $email = trim((string)($input['email'] ?? ''));
    $password = (string)($input['password'] ?? '');
    if ($email === '' || $password === '') {
        respond(false, null, 'Faltan credenciales.', 422);
    }

    $stmt = $pdo->prepare(
        'SELECT u.id, u.nombre, u.email, u.password_hash, u.rol, u.estado, u.residente_id AS id_residente,
                COALESCE(rz.nombre, "") AS residencia
         FROM usuarios u
         LEFT JOIN residencias rz ON rz.id = u.residencia_id
         WHERE u.email = :email AND u.activo = 1
         LIMIT 1'
    );
    $stmt->execute(['email' => $email]);
    $user = $stmt->fetch();

    if (!$user || !password_verify($password, $user['password_hash'])) {
        respond(false, null, 'Credenciales incorrectas.', 401);
    }

    respond(true, ['usuario' => mapUsuario($user)]);
}

function registrarUsuario(PDO $pdo, array $input): void
{
    $nombre = trim((string)($input['nombre'] ?? ''));
    $email = trim((string)($input['email'] ?? ''));
    $password = (string)($input['password'] ?? '');
    $rol = strtoupper(trim((string)($input['rol'] ?? '')));
    $residenciaNombre = trim((string)($input['residencia'] ?? ''));
    $residenteId = isset($input['id_residente']) ? (int)$input['id_residente'] : null;

    if ($nombre === '' || $email === '' || $password === '' || $rol === '' || $residenciaNombre === '') {
        respond(false, null, 'Faltan datos obligatorios.', 422);
    }

    $residenciaId = findResidenciaIdByName($pdo, $residenciaNombre);
    if ($residenciaId === null) {
        respond(false, null, 'La residencia indicada no existe.', 404);
    }

    if ($rol === 'FAMILIAR') {
        if ($residenteId === null || !residentePerteneceAResidencia($pdo, $residenteId, $residenciaId)) {
            respond(false, null, 'El residente seleccionado no pertenece a la residencia.', 422);
        }
    } else {
        $residenteId = null;
    }

    $stmt = $pdo->prepare(
        'INSERT INTO usuarios (residencia_id, residente_id, nombre, email, password_hash, rol, estado, activo)
         VALUES (:residencia_id, :residente_id, :nombre, :email, :password_hash, :rol, :estado, 1)'
    );

    try {
        $stmt->execute([
            'residencia_id' => $residenciaId,
            'residente_id' => $residenteId,
            'nombre' => $nombre,
            'email' => $email,
            'password_hash' => password_hash($password, PASSWORD_BCRYPT),
            'rol' => $rol,
            'estado' => 'PENDIENTE',
        ]);
    } catch (Throwable $error) {
        respond(false, null, 'No se pudo registrar el usuario. Revisa si el correo ya existe.', 409);
    }

    respond(true, ['id' => (int)$pdo->lastInsertId()]);
}

function listarResidencias(PDO $pdo): void
{
    $rows = $pdo->query('SELECT nombre FROM residencias WHERE activo = 1 ORDER BY nombre ASC')->fetchAll();
    respond(true, ['residencias' => array_map(static fn(array $row) => $row['nombre'], $rows)]);
}

function crearResidencia(PDO $pdo, array $input): void
{
    $nombre = trim((string)($input['nombre'] ?? ''));
    if ($nombre === '') {
        respond(false, null, 'Nombre obligatorio.', 422);
    }

    $stmt = $pdo->prepare('INSERT INTO residencias (nombre, activo) VALUES (:nombre, 1)');
    try {
        $stmt->execute(['nombre' => $nombre]);
    } catch (Throwable $error) {
        respond(false, null, 'La residencia ya existe o no se pudo crear.', 409);
    }

    respond(true, ['id' => (int)$pdo->lastInsertId()]);
}

function listarResidentes(PDO $pdo, array $input): void
{
    $residenciaNombre = trim((string)($input['residencia'] ?? ''));
    $incluirInactivos = !empty($input['incluir_inactivos']);
    $residenciaId = findResidenciaIdByName($pdo, $residenciaNombre);

    if ($residenciaId === null) {
        respond(true, ['residentes' => []]);
    }

    $sql = 'SELECT r.id, r.nombre, r.edad, r.habitacion, r.planta, rz.nombre AS residencia,
                   r.fecha_nacimiento, r.fecha_ingreso, r.observaciones, r.necesidades, r.activo
            FROM residentes r
            INNER JOIN residencias rz ON rz.id = r.residencia_id
            WHERE r.residencia_id = :residencia_id';

    if (!$incluirInactivos) {
        $sql .= ' AND r.activo = 1';
    }

    $sql .= ' ORDER BY r.nombre ASC';
    $stmt = $pdo->prepare($sql);
    $stmt->execute(['residencia_id' => $residenciaId]);
    respond(true, ['residentes' => $stmt->fetchAll()]);
}

function obtenerResidente(PDO $pdo, array $input): void
{
    $id = (int)($input['id'] ?? 0);
    $stmt = $pdo->prepare(
        'SELECT r.id, r.nombre, r.edad, r.habitacion, r.planta, rz.nombre AS residencia,
                r.fecha_nacimiento, r.fecha_ingreso, r.observaciones, r.necesidades, r.activo
         FROM residentes r
         INNER JOIN residencias rz ON rz.id = r.residencia_id
         WHERE r.id = :id
         LIMIT 1'
    );
    $stmt->execute(['id' => $id]);
    respond(true, ['residente' => $stmt->fetch() ?: null]);
}

function obtenerResidenteVinculado(PDO $pdo, array $input): void
{
    $usuarioId = (int)($input['usuario_id'] ?? 0);
    $stmt = $pdo->prepare(
        'SELECT r.id, r.nombre, r.edad, r.habitacion, r.planta, rz.nombre AS residencia,
                r.fecha_nacimiento, r.fecha_ingreso, r.observaciones, r.necesidades, r.activo
         FROM usuarios u
         INNER JOIN residentes r ON r.id = u.residente_id
         INNER JOIN residencias rz ON rz.id = r.residencia_id
         WHERE u.id = :usuario_id
         LIMIT 1'
    );
    $stmt->execute(['usuario_id' => $usuarioId]);
    respond(true, ['residente' => $stmt->fetch() ?: null]);
}

function obtenerUsuario(PDO $pdo, array $input): void
{
    $id = (int)($input['id'] ?? 0);
    $stmt = $pdo->prepare(
        'SELECT u.id, u.nombre, u.email, "" AS password_hash, u.rol, u.estado, u.residente_id AS id_residente,
                COALESCE(rz.nombre, "") AS residencia
         FROM usuarios u
         LEFT JOIN residencias rz ON rz.id = u.residencia_id
         WHERE u.id = :id
         LIMIT 1'
    );
    $stmt->execute(['id' => $id]);
    $row = $stmt->fetch();
    respond(true, ['usuario' => $row ? mapUsuario($row) : null]);
}

function listarSolicitudesAdmin(PDO $pdo): void
{
    $stmt = $pdo->query(
        'SELECT u.id, u.nombre, u.email, "" AS password_hash, u.rol, u.estado, u.residente_id AS id_residente,
                COALESCE(rz.nombre, "") AS residencia
         FROM usuarios u
         LEFT JOIN residencias rz ON rz.id = u.residencia_id
         WHERE u.rol = "PERSONAL" AND u.estado = "PENDIENTE" AND u.activo = 1
         ORDER BY u.id ASC'
    );
    $rows = array_map('mapUsuario', $stmt->fetchAll());
    respond(true, ['usuarios' => $rows]);
}

function listarSolicitudesFamiliares(PDO $pdo, array $input): void
{
    $residenciaId = findResidenciaIdByName($pdo, trim((string)($input['residencia'] ?? '')));
    if ($residenciaId === null) {
        respond(true, ['usuarios' => []]);
    }

    $stmt = $pdo->prepare(
        'SELECT u.id, u.nombre, u.email, "" AS password_hash, u.rol, u.estado, u.residente_id AS id_residente,
                rz.nombre AS residencia
         FROM usuarios u
         INNER JOIN residencias rz ON rz.id = u.residencia_id
         WHERE u.rol = "FAMILIAR" AND u.estado = "PENDIENTE" AND u.activo = 1 AND u.residencia_id = :residencia_id
         ORDER BY u.id ASC'
    );
    $stmt->execute(['residencia_id' => $residenciaId]);
    $rows = array_map('mapUsuario', $stmt->fetchAll());
    respond(true, ['usuarios' => $rows]);
}

function actualizarEstadoUsuario(PDO $pdo, array $input): void
{
    $stmt = $pdo->prepare('UPDATE usuarios SET estado = :estado WHERE id = :id');
    $stmt->execute([
        'estado' => strtoupper(trim((string)($input['estado'] ?? 'PENDIENTE'))),
        'id' => (int)($input['id'] ?? 0),
    ]);
    respond(true, ['updated' => true]);
}

function crearUsuarioAdmin(PDO $pdo, array $input): void
{
    $nombre = trim((string)($input['nombre'] ?? ''));
    $email = trim((string)($input['email'] ?? ''));
    $password = (string)($input['password'] ?? '');
    $rol = strtoupper(trim((string)($input['rol'] ?? '')));
    $residenciaNombre = trim((string)($input['residencia'] ?? ''));
    $residenteId = isset($input['id_residente']) ? (int)$input['id_residente'] : null;

    $residenciaId = $residenciaNombre !== '' ? findResidenciaIdByName($pdo, $residenciaNombre) : null;
    if ($residenciaNombre !== '' && $residenciaId === null) {
        respond(false, null, 'La residencia no existe.', 404);
    }

    if ($rol === 'FAMILIAR') {
        if ($residenteId === null || $residenciaId === null || !residentePerteneceAResidencia($pdo, $residenteId, $residenciaId)) {
            respond(false, null, 'El residente no pertenece a la residencia.', 422);
        }
    } else {
        $residenteId = null;
    }

    $stmt = $pdo->prepare(
        'INSERT INTO usuarios (residencia_id, residente_id, nombre, email, password_hash, rol, estado, activo)
         VALUES (:residencia_id, :residente_id, :nombre, :email, :password_hash, :rol, "APROBADO", 1)'
    );

    try {
        $stmt->execute([
            'residencia_id' => $residenciaId,
            'residente_id' => $residenteId,
            'nombre' => $nombre,
            'email' => $email,
            'password_hash' => password_hash($password, PASSWORD_BCRYPT),
            'rol' => $rol,
        ]);
    } catch (Throwable $error) {
        respond(false, null, 'No se pudo crear el usuario.', 409);
    }

    respond(true, ['id' => (int)$pdo->lastInsertId()]);
}

function guardarResidente(PDO $pdo, array $input): void
{
    $id = isset($input['id']) ? (int)$input['id'] : 0;
    $data = [
        'nombre' => trim((string)($input['nombre'] ?? '')),
        'edad' => (int)($input['edad'] ?? 0),
        'habitacion' => trim((string)($input['habitacion'] ?? '')),
        'planta' => trim((string)($input['planta'] ?? '')),
        'fecha_nacimiento' => trim((string)($input['fecha_nacimiento'] ?? '')),
        'fecha_ingreso' => trim((string)($input['fecha_ingreso'] ?? '')),
        'observaciones' => trim((string)($input['observaciones'] ?? '')),
        'necesidades' => trim((string)($input['necesidades'] ?? '')),
    ];

    if ($id > 0) {
        $stmt = $pdo->prepare(
            'UPDATE residentes
             SET nombre = :nombre, edad = :edad, habitacion = :habitacion, planta = :planta,
                 fecha_nacimiento = :fecha_nacimiento, fecha_ingreso = :fecha_ingreso,
                 observaciones = :observaciones, necesidades = :necesidades
             WHERE id = :id'
        );
        $stmt->execute($data + ['id' => $id]);
        respond(true, ['id' => $id]);
    }

    $residenciaId = findResidenciaIdByName($pdo, trim((string)($input['residencia'] ?? '')));
    if ($residenciaId === null) {
        respond(false, null, 'La residencia no existe.', 404);
    }

    $stmt = $pdo->prepare(
        'INSERT INTO residentes (residencia_id, nombre, edad, habitacion, planta, fecha_nacimiento, fecha_ingreso, observaciones, necesidades, activo)
         VALUES (:residencia_id, :nombre, :edad, :habitacion, :planta, :fecha_nacimiento, :fecha_ingreso, :observaciones, :necesidades, 1)'
    );
    $stmt->execute($data + ['residencia_id' => $residenciaId]);
    respond(true, ['id' => (int)$pdo->lastInsertId()]);
}

function actualizarEstadoResidente(PDO $pdo, array $input): void
{
    $stmt = $pdo->prepare('UPDATE residentes SET activo = :activo WHERE id = :id');
    $stmt->execute([
        'activo' => !empty($input['activo']) ? 1 : 0,
        'id' => (int)($input['id'] ?? 0),
    ]);
    respond(true, ['updated' => true]);
}

function crearVisita(PDO $pdo, array $input): void
{
    $stmt = $pdo->prepare('SELECT residente_id FROM usuarios WHERE id = :id LIMIT 1');
    $stmt->execute(['id' => (int)($input['id_familiar'] ?? 0)]);
    $residenteId = $stmt->fetchColumn();

    if (!$residenteId) {
        respond(false, null, 'El familiar no tiene residente vinculado.', 422);
    }

    $insert = $pdo->prepare(
        'INSERT INTO visitas (id_familiar, id_residente, fecha, hora, estado, nota)
         VALUES (:id_familiar, :id_residente, :fecha, :hora, "PENDIENTE", :nota)'
    );
    $insert->execute([
        'id_familiar' => (int)$input['id_familiar'],
        'id_residente' => (int)$residenteId,
        'fecha' => trim((string)($input['fecha'] ?? '')),
        'hora' => trim((string)($input['hora'] ?? '')),
        'nota' => trim((string)($input['nota'] ?? '')),
    ]);

    respond(true, ['id' => (int)$pdo->lastInsertId()]);
}

function listarVisitasFamiliar(PDO $pdo, array $input): void
{
    $stmt = $pdo->prepare(baseVisitasSql() . ' WHERE v.id_familiar = :id_familiar ORDER BY v.fecha ASC, v.hora ASC');
    $stmt->execute(['id_familiar' => (int)($input['id_familiar'] ?? 0)]);
    respond(true, ['visitas' => $stmt->fetchAll()]);
}

function listarVisitasPendientes(PDO $pdo, array $input): void
{
    $residenciaId = findResidenciaIdByName($pdo, trim((string)($input['residencia'] ?? '')));
    if ($residenciaId === null) {
        respond(true, ['visitas' => []]);
    }

    $stmt = $pdo->prepare(baseVisitasSql() . ' WHERE v.estado = "PENDIENTE" AND r.residencia_id = :residencia_id ORDER BY v.fecha ASC, v.hora ASC');
    $stmt->execute(['residencia_id' => $residenciaId]);
    respond(true, ['visitas' => $stmt->fetchAll()]);
}

function listarVisitasResidencia(PDO $pdo, array $input): void
{
    $residenciaId = findResidenciaIdByName($pdo, trim((string)($input['residencia'] ?? '')));
    if ($residenciaId === null) {
        respond(true, ['visitas' => []]);
    }

    $stmt = $pdo->prepare(baseVisitasSql() . ' WHERE r.residencia_id = :residencia_id ORDER BY v.fecha ASC, v.hora ASC');
    $stmt->execute(['residencia_id' => $residenciaId]);
    respond(true, ['visitas' => $stmt->fetchAll()]);
}

function actualizarEstadoVisita(PDO $pdo, array $input): void
{
    $stmt = $pdo->prepare('UPDATE visitas SET estado = :estado WHERE id = :id');
    $stmt->execute([
        'estado' => strtoupper(trim((string)($input['estado'] ?? 'PENDIENTE'))),
        'id' => (int)($input['id'] ?? 0),
    ]);
    respond(true, ['updated' => true]);
}

function listarHorasOcupadas(PDO $pdo, array $input): void
{
    $stmt = $pdo->prepare(
        'SELECT DATE_FORMAT(hora, "%H:%i") AS hora
         FROM visitas
         WHERE fecha = :fecha AND id_residente = :id_residente AND estado <> "RECHAZADA"
         ORDER BY hora ASC'
    );
    $stmt->execute([
        'fecha' => trim((string)($input['fecha'] ?? '')),
        'id_residente' => (int)($input['id_residente'] ?? 0),
    ]);
    respond(true, ['horas' => array_column($stmt->fetchAll(), 'hora')]);
}

function resumenPersonal(PDO $pdo, array $input): void
{
    $residenciaId = findResidenciaIdByName($pdo, trim((string)($input['residencia'] ?? '')));
    if ($residenciaId === null) {
        respond(true, ['citas_pendientes' => 0, 'visitas_hoy' => 0, 'familiares_pendientes' => 0]);
    }

    $hoy = date('Y-m-d');
    $pendientes = scalar(
        $pdo,
        'SELECT COUNT(*) FROM visitas v
         INNER JOIN residentes r ON r.id = v.id_residente
         WHERE r.residencia_id = :residencia_id AND v.estado = "PENDIENTE"',
        ['residencia_id' => $residenciaId]
    );
    $hoyConfirmadas = scalar(
        $pdo,
        'SELECT COUNT(*) FROM visitas v
         INNER JOIN residentes r ON r.id = v.id_residente
         WHERE r.residencia_id = :residencia_id AND v.estado = "CONFIRMADA" AND v.fecha = :fecha',
        ['residencia_id' => $residenciaId, 'fecha' => $hoy]
    );
    $familiares = scalar(
        $pdo,
        'SELECT COUNT(*) FROM usuarios WHERE residencia_id = :residencia_id AND rol = "FAMILIAR" AND estado = "PENDIENTE" AND activo = 1',
        ['residencia_id' => $residenciaId]
    );

    respond(true, [
        'citas_pendientes' => $pendientes,
        'visitas_hoy' => $hoyConfirmadas,
        'familiares_pendientes' => $familiares,
    ]);
}

function resumenAdmin(PDO $pdo): void
{
    respond(true, [
        'total_residencias' => scalar($pdo, 'SELECT COUNT(*) FROM residencias WHERE activo = 1'),
        'total_residentes' => scalar($pdo, 'SELECT COUNT(*) FROM residentes WHERE activo = 1'),
        'personal_pendiente' => scalar($pdo, 'SELECT COUNT(*) FROM usuarios WHERE rol = "PERSONAL" AND estado = "PENDIENTE" AND activo = 1'),
    ]);
}

function contarResidentesResidencia(PDO $pdo, array $input): void
{
    $residenciaId = findResidenciaIdByName($pdo, trim((string)($input['residencia'] ?? '')));
    if ($residenciaId === null) {
        respond(true, ['total' => 0]);
    }

    respond(true, [
        'total' => scalar(
            $pdo,
            'SELECT COUNT(*) FROM residentes WHERE residencia_id = :residencia_id AND activo = 1',
            ['residencia_id' => $residenciaId]
        ),
    ]);
}

function insertarMensaje(PDO $pdo, array $input): void
{
    $stmt = $pdo->prepare(
        'INSERT INTO mensajes (id_emisor, id_receptor, texto, hora)
         VALUES (:id_emisor, :id_receptor, :texto, :hora)'
    );
    $stmt->execute([
        'id_emisor' => (int)($input['emisor'] ?? 0),
        'id_receptor' => (int)($input['receptor'] ?? 0),
        'texto' => trim((string)($input['texto'] ?? '')),
        'hora' => trim((string)($input['hora'] ?? '')),
    ]);
    respond(true, ['id' => (int)$pdo->lastInsertId()]);
}

function listarMensajes(PDO $pdo, array $input): void
{
    $id1 = (int)($input['id_usuario_1'] ?? 0);
    $id2 = (int)($input['id_usuario_2'] ?? 0);

    $stmt = $pdo->prepare(
        'SELECT texto, hora, id_emisor
         FROM mensajes
         WHERE (id_emisor = :id1 AND id_receptor = :id2) OR (id_emisor = :id2 AND id_receptor = :id1)
         ORDER BY id ASC'
    );
    $stmt->execute(['id1' => $id1, 'id2' => $id2]);

    $mensajes = array_map(
        static fn(array $row) => [
            'texto' => $row['texto'],
            'hora' => $row['hora'],
            'es_emisor' => (int)$row['id_emisor'] === $id1,
        ],
        $stmt->fetchAll()
    );

    respond(true, ['mensajes' => $mensajes]);
}

function baseVisitasSql(): string
{
    return 'SELECT v.id, v.id_familiar, uf.nombre AS nombre_familiar, DATE_FORMAT(v.fecha, "%Y-%m-%d") AS fecha,
                   DATE_FORMAT(v.hora, "%H:%i") AS hora, v.estado, COALESCE(v.nota, "") AS nota,
                   r.nombre AS nombre_residente
            FROM visitas v
            INNER JOIN usuarios uf ON uf.id = v.id_familiar
            INNER JOIN residentes r ON r.id = v.id_residente';
}

function findResidenciaIdByName(PDO $pdo, string $name): ?int
{
    if ($name === '') {
        return null;
    }
    $stmt = $pdo->prepare('SELECT id FROM residencias WHERE nombre = :nombre LIMIT 1');
    $stmt->execute(['nombre' => $name]);
    $value = $stmt->fetchColumn();
    return $value === false ? null : (int)$value;
}

function residentePerteneceAResidencia(PDO $pdo, int $residenteId, int $residenciaId): bool
{
    return scalar(
        $pdo,
        'SELECT COUNT(*) FROM residentes WHERE id = :id AND residencia_id = :residencia_id',
        ['id' => $residenteId, 'residencia_id' => $residenciaId]
    ) > 0;
}

function scalar(PDO $pdo, string $sql, array $params = []): int
{
    $stmt = $pdo->prepare($sql);
    $stmt->execute($params);
    return (int)$stmt->fetchColumn();
}

function mapUsuario(array $row): array
{
    return [
        'id' => (int)$row['id'],
        'nombre' => $row['nombre'],
        'email' => $row['email'],
        'password' => '',
        'rol' => $row['rol'],
        'residencia' => $row['residencia'] ?? '',
        'estado' => $row['estado'],
        'id_residente' => isset($row['id_residente']) ? (int)$row['id_residente'] : null,
    ];
}

function respond(bool $ok, ?array $data = null, string $message = '', int $status = 200): void
{
    http_response_code($status);
    echo json_encode([
        'ok' => $ok,
        'data' => $data,
        'message' => $message,
    ], JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
    exit;
}

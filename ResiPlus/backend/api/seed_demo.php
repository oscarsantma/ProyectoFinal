<?php

declare(strict_types=1);

$config = require __DIR__ . '/config.php';

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

$pdo->beginTransaction();

try {
    $pdo->exec("INSERT INTO residencias (nombre, activo) VALUES
        ('Residencia San Francisco', 1),
        ('Residencia Los Olivos', 1)
        ON DUPLICATE KEY UPDATE activo = VALUES(activo)");

    $residenciaSanFrancisco = (int)$pdo->query("SELECT id FROM residencias WHERE nombre = 'Residencia San Francisco'")->fetchColumn();
    $residenciaLosOlivos = (int)$pdo->query("SELECT id FROM residencias WHERE nombre = 'Residencia Los Olivos'")->fetchColumn();

    $stmtResidente = $pdo->prepare(
        'INSERT INTO residentes (residencia_id, nombre, edad, habitacion, planta, fecha_nacimiento, fecha_ingreso, observaciones, necesidades, activo)
         VALUES (:residencia_id, :nombre, :edad, :habitacion, :planta, :fecha_nacimiento, :fecha_ingreso, :observaciones, :necesidades, 1)'
    );

    $demoResidents = [
        [
            'residencia_id' => $residenciaSanFrancisco,
            'nombre' => 'Antonio Garcia Lopez',
            'edad' => 82,
            'habitacion' => '100',
            'planta' => 'Planta 2',
            'fecha_nacimiento' => '15 de marzo de 1943',
            'fecha_ingreso' => '10 de enero de 2023',
            'observaciones' => 'Dieta blanda. Toma medicacion para la tension por la manana.',
            'necesidades' => 'Visitas por la manana, paseo diario acompanado y seguimiento de tension.',
        ],
        [
            'residencia_id' => $residenciaSanFrancisco,
            'nombre' => 'Carmen Soto Ramirez',
            'edad' => 79,
            'habitacion' => '104',
            'planta' => 'Planta 2',
            'fecha_nacimiento' => '2 de septiembre de 1946',
            'fecha_ingreso' => '18 de abril de 2024',
            'observaciones' => 'Necesita ayuda parcial en movilidad y control de glucosa.',
            'necesidades' => 'Revision de glucosa a las 09:00 y apoyo en ejercicios suaves.',
        ],
        [
            'residencia_id' => $residenciaLosOlivos,
            'nombre' => 'Jose Manuel Perez',
            'edad' => 87,
            'habitacion' => '201',
            'planta' => 'Planta 1',
            'fecha_nacimiento' => '21 de noviembre de 1938',
            'fecha_ingreso' => '8 de febrero de 2022',
            'observaciones' => 'Acompanamiento en comidas y descanso despues de mediodia.',
            'necesidades' => 'Supervision de medicacion y dieta baja en sal.',
        ],
    ];

    foreach ($demoResidents as $resident) {
        $exists = $pdo->prepare('SELECT id FROM residentes WHERE nombre = :nombre LIMIT 1');
        $exists->execute(['nombre' => $resident['nombre']]);
        if (!$exists->fetchColumn()) {
            $stmtResidente->execute($resident);
        }
    }

    $antonioId = (int)$pdo->query("SELECT id FROM residentes WHERE nombre = 'Antonio Garcia Lopez'")->fetchColumn();

    $stmtUser = $pdo->prepare(
        'INSERT INTO usuarios (residencia_id, residente_id, nombre, email, password_hash, rol, estado, activo)
         VALUES (:residencia_id, :residente_id, :nombre, :email, :password_hash, :rol, :estado, 1)
         ON DUPLICATE KEY UPDATE nombre = VALUES(nombre), estado = VALUES(estado), activo = 1'
    );

    $stmtUser->execute([
        'residencia_id' => $residenciaSanFrancisco,
        'residente_id' => $antonioId,
        'nombre' => 'Maria Garcia',
        'email' => 'familiar@test.com',
        'password_hash' => password_hash('1234', PASSWORD_BCRYPT),
        'rol' => 'FAMILIAR',
        'estado' => 'APROBADO',
    ]);

    $stmtUser->execute([
        'residencia_id' => $residenciaSanFrancisco,
        'residente_id' => null,
        'nombre' => 'Carmen Ruiz',
        'email' => 'personal@test.com',
        'password_hash' => password_hash('1234', PASSWORD_BCRYPT),
        'rol' => 'PERSONAL',
        'estado' => 'APROBADO',
    ]);

    $stmtUser->execute([
        'residencia_id' => $residenciaSanFrancisco,
        'residente_id' => null,
        'nombre' => 'Admin ResiPlus',
        'email' => 'admin@test.com',
        'password_hash' => password_hash('1234', PASSWORD_BCRYPT),
        'rol' => 'ADMIN',
        'estado' => 'APROBADO',
    ]);

    $pdo->commit();
    echo "Datos demo insertados correctamente.\n";
} catch (Throwable $error) {
    $pdo->rollBack();
    echo "Error al sembrar datos: " . $error->getMessage() . "\n";
}

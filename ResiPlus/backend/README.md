# ResiPlus backend MySQL + PHP

## 1. Crear la base de datos

Importa `backend/mysql/resiplus_schema.sql` desde phpMyAdmin.

## 2. Configurar la API

1. Copia `backend/api/` a tu hosting o dominio externo.
2. Edita `backend/api/config.php` con tus credenciales reales de MySQL.
3. Asegúrate de que la URL final de la API sea algo como:

`https://tu-dominio.com/resiplus/api/index.php`

## 3. Cargar datos demo opcionales

Ejecuta `backend/api/seed_demo.php` una sola vez si quieres partir con:

- `familiar@test.com / 1234`
- `personal@test.com / 1234`
- `admin@test.com / 1234`

## 4. Configurar Android

En [app/build.gradle.kts](C:\Users\OscarSa\Downloads\ResiPlus_Completa\ResiPlus\app\build.gradle.kts) cambia:

`API_BASE_URL`

por la URL real de tu dominio.

## 5. Qué hace esta migración

- Sustituye la base local SQLite por llamadas HTTP a la API PHP.
- Mantiene la misma lógica de residencias, residentes, usuarios y citas.
- Te deja lista la app para trabajar con MySQL remota.

## 6. Siguiente mejora recomendada

La app ya queda preparada para dominio externo, pero la capa Android hace peticiones bloqueantes en segundo plano para no reescribir todo el proyecto. El siguiente paso profesional sería migrarla a Retrofit + corrutinas y añadir autenticación con token.

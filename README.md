# GYMTRACKPRO - Registro de Cambios

Este archivo lleva un registro exacto de cambios en el proyecto.

## Formato de registro (usar siempre)

- Fecha: `YYYY-MM-DD HH:mm` (zona horaria local)
- Autor: `nombre o rol`
- Tipo: `feat | fix | refactor | chore | docs`
- Resumen: `1 linea`
- Archivos modificados:
  - `ruta/archivo.ext:linea` -> `que se cambio`
- Motivo:
  - `por que se hizo`
- Impacto:
  - `que flujo cambia`
- Verificacion:
  - `como se valido` o `pendiente`

---

## Historial

### 2026-03-06 16:xx - Deploy Fix - `fix`

- Resumen: correccion de crash en Railway por binario nativo de `bcrypt` incompatible con Linux.
- Archivos modificados:
  - `backend/index.js` -> cambio de `bcrypt` a `bcryptjs`.
  - `backend/package.json` -> dependencia `bcrypt` reemplazada por `bcryptjs`.
  - `backend/package-lock.json` -> lockfile actualizado.
  - `backend/node_modules` -> removido del control de versiones (`git rm -r --cached`) para evitar subir binarios locales.
- Motivo:
  - Resolver error de deploy `invalid ELF header` en entorno Linux de Railway.
- Impacto:
  - El backend puede instalar dependencias compatibles en build remoto.
  - Se evita que artefactos locales de Windows rompan despliegues.
- Verificacion:
  - Sintaxis valida con `node --check backend/index.js`.
  - Error de puerto local (`EADDRINUSE`) confirma que habia un proceso backend activo ya escuchando en `3000`.

---

### 2026-03-06 13:xx - Deploy - `chore`

- Resumen: se deja el backend preparado para despliegue externo (Railway) con Supabase y se documenta la diferencia de conexion entre emulador y dispositivo fisico.
- Archivos modificados:
  - `README.md` -> registro de preparacion para publicacion y pruebas multi-dispositivo.
- Motivo:
  - Permitir que cualquier dispositivo consuma el backend sin depender de IP local de desarrollo.
- Impacto:
  - Se consolida flujo objetivo: Android -> backend publico (Railway) -> Supabase.
  - Se evita confusion de red local (`10.0.2.2` solo emulador; IP LAN para telefono fisico).
- Verificacion:
  - Backend inicia correctamente en modo Supabase: `gymtrack-api running on port 3000 (storage=supabase)`.

---

### 2026-03-06 13:xx - Backend - `feat`

- Resumen: migracion del backend de almacenamiento en memoria a Supabase para auth, rutinas y progreso.
- Archivos modificados:
  - `backend/index.js` -> rutas `auth`, `routines` y `progress` ahora usan consultas a Supabase.
  - `backend/supabaseClient.js` -> nuevo cliente Supabase con validacion de variables de entorno.
  - `backend/package.json` -> se incluye dependencia `@supabase/supabase-js`.
- Motivo:
  - Persistir datos reales del backend y continuar con arquitectura remota estable para sincronizacion.
- Impacto:
  - El backend deja de depender de estructuras en memoria para entidades de negocio.
  - Requiere configurar `SUPABASE_SERVICE_ROLE_KEY` real en `backend/.env`.
- Verificacion:
  - Sintaxis valida con `node --check backend/index.js`.
  - Arranque validado: `gymtrack-api running on port 3000 (storage=supabase)`.

---

### 2026-03-06 13:xx - Documentacion - `chore`

- Resumen: se limpia el historial para remover referencias a proveedor de base de datos descartado y se prepara entorno para migracion a Supabase.
- Archivos modificados:
  - `README.md` -> historial depurado y enfocado en estado actual del proyecto.
  - `backend/.env.example` -> se agregan `SUPABASE_URL` y `SUPABASE_SERVICE_ROLE_KEY`.
- Motivo:
  - Evitar ruido historico que ya no aplica y facilitar siguiente etapa de backend con Supabase.
- Impacto:
  - Documentacion mas clara para trabajo futuro.
  - Variables sensibles de Supabase definidas para configuracion local.
- Verificacion:
  - Revision manual de archivo y estructura final.

---

### 2026-03-06 13:xx - Backend - `refactor`

- Resumen: backend temporal movido a almacenamiento en memoria mientras se define proveedor de base de datos definitivo.
- Archivos modificados:
  - `backend/index.js` -> almacenamiento en memoria para `users`, `routines`, `progress` y cache temporal de ejercicios.
  - `backend/package.json` -> limpieza de dependencias de base de datos no usadas.
  - `backend/.env.example` -> variables enfocadas en entorno actual (`JWT`, `RapidAPI`).
- Motivo:
  - Mantener pruebas funcionales del backend sin bloquear el flujo de desarrollo.
- Impacto:
  - El backend arranca localmente y conserva contratos de API.
  - Los datos se pierden al reiniciar el proceso (estado temporal).
- Verificacion:
  - Sintaxis valida con `node --check backend/index.js`.
  - Arranque validado: `gymtrack-api running on port 3000 (storage=memory)`.

---

### 2026-03-06 11:xx - Data Layer - `refactor`

- Resumen: se rediseño la base local Room para modo offline-first de rutinas y se preparo la sincronizacion con backend remoto + fuente externa de ejercicios.
- Archivos modificados:
  - `app/src/main/java/com/example/gymtrackpro/data/local/entities/RoutineEntity.kt` -> se agregan `remoteId`, `syncState`, `deleted`, `updatedAt` para sync diferida.
  - `app/src/main/java/com/example/gymtrackpro/data/local/entities/RoutineExerciseEntity.kt` -> se agregan estado de sync, soft delete y timestamp.
  - `app/src/main/java/com/example/gymtrackpro/data/local/entities/ExerciseEntity.kt` -> se amplian campos (`bodyPart`, `equipment`, `target`) para compatibilidad con ExerciseDB.
  - `app/src/main/java/com/example/gymtrackpro/data/local/dao/RoutineDao.kt` -> nuevo DAO para operaciones de rutina local y estado pendiente/sincronizado.
  - `app/src/main/java/com/example/gymtrackpro/data/local/dao/RoutineExerciseDao.kt` -> nuevo DAO para vincular ejercicios en rutinas y sincronizacion por conjunto.
  - `app/src/main/java/com/example/gymtrackpro/data/local/db/AppDatabase.kt` -> version `2` y nuevos DAOs de rutina.
  - `app/src/main/java/com/example/gymtrackpro/utils/AppProvider.kt` -> inyeccion de nuevos DAOs y `fallbackToDestructiveMigration()`.
  - `app/src/main/java/com/example/gymtrackpro/data/remote/dto/ExerciseDtos.kt` -> DTO flexible para respuesta de backend/ExerciseDB.
  - `app/src/main/java/com/example/gymtrackpro/data/remote/dto/RoutineDtos.kt` -> nuevos contratos para crear/actualizar/sincronizar rutinas.
  - `app/src/main/java/com/example/gymtrackpro/data/remote/api/ApiService.kt` -> endpoints de rutinas (`GET/POST/PUT/DELETE` y sync de ejercicios por rutina).
  - `app/src/main/java/com/example/gymtrackpro/data/repository/GymRepository.kt` -> flujo offline-first de rutinas (crear, editar, borrar, agregar/quitar ejercicios, sync pendiente).
  - `app/src/main/java/com/example/gymtrackpro/ui/home/HomeViewModel.kt` -> `syncProgress()` ahora dispara sincronizacion completa (`syncAllPending()`).
- Motivo:
  - Mantener al usuario y sus rutinas disponibles en local sin depender de conexion, y sincronizar automaticamente cambios cuando vuelva internet.
- Impacto:
  - Los ejercicios siguen cacheados localmente desde API.
  - Las rutinas y ejercicios seleccionados ahora quedan preparados para persistencia local + sincronizacion remota.
  - Se establece una base tecnica para backend remoto y consumo de ejercicios desde ExerciseDB.
- Verificacion:
  - Revision de integracion entre entidades, DAOs, repositorio y API contracts.
  - Compilacion por terminal pendiente de entorno local (`JAVA_HOME` no configurado en shell).

---

### 2026-03-06 10:xx - Auth - `feat`

- Resumen: se separo el flujo de autenticacion en dos pantallas (`Login` y `Register`) con actividad de registro dedicada.
- Archivos modificados:
  - `app/src/main/java/com/example/gymtrackpro/ui/auth/LoginActivity.kt` -> `btnRegister` ahora navega a `RegisterActivity` y el login queda aislado.
  - `app/src/main/java/com/example/gymtrackpro/ui/auth/RegisterActivity.kt` -> nueva actividad para registrar usuario usando `AuthViewModel`.
  - `app/src/main/res/layout/activity_login.xml` -> se elimina el campo `etName` y se ajusta el boton a "Ir a registro".
  - `app/src/main/res/layout/activity_register.xml` -> nuevo layout de registro con `etName`, `etEmail`, `etPassword`, y acciones de crear cuenta/volver.
  - `app/src/main/AndroidManifest.xml` -> se declara `.ui.auth.RegisterActivity`.
- Motivo:
  - Separar responsabilidades por pantalla para mantener un flujo de autenticacion mas claro y mantenible.
- Impacto:
  - `LoginActivity` inicia sesion y redirige a registro cuando corresponde.
  - `RegisterActivity` realiza alta de cuenta y, al completar, navega a `HomeActivity`.
- Verificacion:
  - Revision de referencias de IDs, clases y manifiesto completada.
  - Compilacion por terminal pendiente de entorno local (`JAVA_HOME` no configurado en shell).

---

### 2026-03-05 14:xx - Mantenimiento - `chore`

- Resumen: se amplio `.gitignore` con reglas de entorno local de Android Studio/Gradle para evitar conflictos entre maquinas.
- Archivos modificados:
  - `.gitignore` -> se agregan exclusiones para `.gradle`, `.kotlin`, `**/build`, `local.properties`, `.idea`, `*.iml`, archivos de firma y ruido de sistema.
- Motivo:
  - Se detectaron cambios de entorno local que generaban ruido de versionado y conflictos al alternar ramas.
- Impacto:
  - Se reduce el ruido de cambios locales y archivos de entorno que no pertenecen al codigo de la app.
- Verificacion:
  - Revision directa del contenido final de `.gitignore`.

---

### 2026-03-05 14:xx - UI/UX - `feat`

- Resumen: mejora visual completa del login con Material Design 3 y ViewBinding intacto.
- Archivos modificados:
  - `app/src/main/res/layout/activity_login.xml` -> rediseño total de la UI del login (card, campos outlined, botones Material3, jerarquia visual moderna).
  - `app/src/main/res/drawable/bg_login_modern.xml` -> nuevo fondo decorativo suave para dar profundidad visual.
- Motivo:
  - Se actualizo la interfaz para alinear el login con Material Design 3 y mejorar legibilidad y jerarquia visual.
- Impacto:
  - Se mantiene la logica existente de `LoginActivity` porque se conservaron todos los IDs de ViewBinding.
  - No se modifica la navegacion ni autenticacion, solo presentacion.
- Verificacion:
  - Confirmados los IDs requeridos por `LoginActivity`: `etName`, `etEmail`, `etPassword`, `btnLogin`, `btnRegister`, `btnGuest`, `tvError`, `progress`.
  - Compilacion por terminal pendiente de entorno local (`JAVA_HOME` no configurado en shell).

---

### 2026-03-05 13:xx - App Core - `fix`

- Resumen: el flujo de inicio ahora arranca en `MainActivity` y redirige a `LoginActivity`.
- Archivos modificados:
  - `app/src/main/AndroidManifest.xml:18` -> `LoginActivity` pasa a `android:exported="false"` y se elimina `MAIN/LAUNCHER`.
  - `app/src/main/AndroidManifest.xml:26` -> `MainActivity` pasa a `android:exported="true"` y recibe `MAIN/LAUNCHER`.
  - `app/src/main/java/com/example/gymtrackpro/MainActivity.kt:12` -> se agrega `startActivity(Intent(this, LoginActivity::class.java))`.
  - `app/src/main/java/com/example/gymtrackpro/MainActivity.kt:13` -> se agrega `finish()` para cerrar `MainActivity` al redirigir.
  - `app/src/main/java/com/example/gymtrackpro/MainActivity.kt` -> se limpia codigo UI no usado (layout/insets) para dejarla como pantalla puente.
- Motivo:
  - Se normalizo el punto de entrada de la app para centralizar el arranque y mantener el login como primer flujo visible.
- Impacto:
  - Al abrir la app, Android entra por `MainActivity` y de inmediato navega a `LoginActivity`.
- Verificacion:
  - Revision de `AndroidManifest` y clases actualizadas.
  - Compilacion por terminal pendiente de entorno local (`JAVA_HOME` no configurado en shell).

---

## Plantilla para proximos cambios

```md
### YYYY-MM-DD HH:mm - Autor - `tipo`

- Resumen: ...
- Archivos modificados:
  - `ruta/archivo.ext:linea` -> ...
- Motivo:
  - ...
- Impacto:
  - ...
- Verificacion:
  - ...
```

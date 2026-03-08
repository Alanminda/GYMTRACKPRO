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

### 2026-03-08 15:xx - Hub Visual Polish + FAB Position - `fix`

- Resumen: se mejora apariencia del hub inferior y se corrige superposicion del boton `+` en Home.
- Archivos modificados:
  - `app/src/main/res/layout/activity_home.xml` -> `btnAddRoutine` migrado a `FloatingActionButton` y anclado sobre el hub; ajustes de constraints para evitar interposicion.
  - `app/src/main/res/layout/activity_home.xml` -> `BottomNavigationView` con estilo visual Material 3 (`colorSurfaceContainer`, elevacion, labels visibles).
  - `app/src/main/res/layout/activity_exercises.xml` -> mismo tratamiento visual para hub inferior.
  - `app/src/main/res/layout/activity_third_hub.xml` -> mismo tratamiento visual para hub inferior.
- Motivo:
  - El boton de crear rutina se montaba sobre el hub y la navegacion inferior se veia plana.
- Impacto:
  - Mejor jerarquia visual.
  - `+` queda separado del hub y accesible sin tapar tabs.
- Verificacion:
  - Revision de IDs de binding y constraints completada.
  - Prueba visual final pendiente en dispositivo.

---

### 2026-03-08 14:xx - Routines Save Logic (Logged vs Guest) - `fix`

- Resumen: se corrige flujo de guardado de rutinas para separar claramente modo usuario iniciado (local + remoto) y modo invitado (solo local).
- Archivos modificados:
  - `app/src/main/java/com/example/gymtrackpro/ui/home/HomeViewModel.kt` -> `syncData()` ahora sincroniza solo si hay sesion; `createRoutine()` distingue invitado vs iniciado y muestra mensajes reales segun estado de sync.
  - `app/src/main/java/com/example/gymtrackpro/data/repository/GymRepository.kt` -> nuevo `syncForLoggedUser()` (push pendientes + pull remoto), `hasPendingRoutineSync()`, y `pullRemoteRoutinesToLocal()` para recuperar rutinas remotas en local.
  - `app/src/main/java/com/example/gymtrackpro/data/local/dao/RoutineDao.kt` -> nuevo `getByRemoteId(remoteId)`.
  - `app/src/main/java/com/example/gymtrackpro/data/repository/GymRepository.kt` -> `getRoutineExercises()` ahora intenta completar ejercicios faltantes por ID desde API si no existen localmente.
- Motivo:
  - Las rutinas creadas con la misma cuenta no siempre aparecian porque faltaba la fase de pull remoto a local y no habia separacion explicita para modo invitado.
- Impacto:
  - Usuario iniciado: rutinas se guardan localmente y se sincronizan con backend; al iniciar se recuperan desde remoto.
  - Invitado: rutinas se guardan solo en local, sin intentar sincronizacion remota.
- Verificacion:
  - Revision de flujo HomeViewModel -> Repository -> DAO completada.
  - Prueba funcional pendiente en dispositivo (crear rutina logueado, reinstalar app, volver a login y validar persistencia).

---

### 2026-03-08 13:xx - Exercises Initial Load Optimization - `perf`

- Resumen: se optimiza la apertura de pantalla de ejercicios cargando solo 10 items al inicio y manteniendo paginacion normal en lotes siguientes.
- Archivos modificados:
  - `app/src/main/java/com/example/gymtrackpro/ui/exercises/ExercisesViewModel.kt` -> page size dinamico (`INITIAL_PAGE_SIZE=10`, `NEXT_PAGE_SIZE=50`).
- Motivo:
  - Reducir lag inicial al entrar a la pantalla de ejercicios.
- Impacto:
  - Primera pintura mas rapida.
  - Infinite scroll conserva carga masiva posterior como flujo habitual.
- Verificacion:
  - Revision de flujo de paginacion y offset completada.
  - Prueba funcional en dispositivo pendiente.

---

### 2026-03-08 12:xx - Bottom Hub Selected State Sync - `fix`

- Resumen: se corrige bug visual donde el tab resaltado del hub inferior quedaba en pantalla previa al volver o reusar Activities.
- Archivos modificados:
  - `app/src/main/java/com/example/gymtrackpro/ui/navigation/MainBottomNav.kt` -> nuevo `syncSelection(...)` para marcar tab sin disparar navegacion.
  - `app/src/main/java/com/example/gymtrackpro/ui/home/HomeActivity.kt` -> sincronizacion de tab en `onResume`.
  - `app/src/main/java/com/example/gymtrackpro/ui/exercises/ExercisesActivity.kt` -> sincronizacion de tab en `onResume`.
  - `app/src/main/java/com/example/gymtrackpro/ui/placeholder/ThirdHubActivity.kt` -> sincronizacion de tab en `onResume`.
- Motivo:
  - El estado visual del menu inferior podia quedar desfasado al reutilizar Activities desde back stack.
- Impacto:
  - El tab activo siempre coincide con la pantalla visible.
- Verificacion:
  - Revision de flujo de estado en `onResume` completada.
  - Prueba funcional en dispositivo pendiente.

---

### 2026-03-08 12:xx - Bottom Hub Transition Smoothing - `fix`

- Resumen: se elimina animacion visible al cambiar entre tabs del hub inferior para que la barra se perciba estable.
- Archivos modificados:
  - `app/src/main/java/com/example/gymtrackpro/ui/navigation/MainBottomNav.kt` -> navegacion con `FLAG_ACTIVITY_CLEAR_TOP | FLAG_ACTIVITY_SINGLE_TOP` y `overridePendingTransition(0, 0)`.
- Motivo:
  - El cambio entre pantallas principales mostraba animacion de Activity, rompiendo la sensacion de hub fijo.
- Impacto:
  - Navegacion mas fluida y sin transicion lateral/vertical entre Home, Ejercicios y tercer tab.
- Verificacion:
  - Revision de flujo de intents completada.
  - Prueba visual pendiente en dispositivo.

---

### 2026-03-08 11:xx - Main Navigation Hub - `feat`

- Resumen: se implementa hub de navegacion inferior fijo para pantallas principales (Home, Ejercicios y tercer modulo placeholder).
- Archivos modificados:
  - `app/src/main/res/menu/main_bottom_nav.xml` -> nuevo menu de navegacion inferior (`Home`, `Ejercicios`, `Proximo`).
  - `app/src/main/java/com/example/gymtrackpro/ui/navigation/MainBottomNav.kt` -> helper central para bind y navegacion entre actividades principales.
  - `app/src/main/java/com/example/gymtrackpro/ui/home/HomeActivity.kt` -> integra `BottomNavigationView` y elimina boton dedicado de ir a ejercicios.
  - `app/src/main/java/com/example/gymtrackpro/ui/exercises/ExercisesActivity.kt` -> integra `BottomNavigationView` y elimina boton `Home` redundante.
  - `app/src/main/java/com/example/gymtrackpro/ui/placeholder/ThirdHubActivity.kt` -> nueva tercera pantalla placeholder para futuro modulo.
  - `app/src/main/res/layout/activity_home.xml` -> agrega `BottomNavigationView` fijo inferior y ajusta constraints.
  - `app/src/main/res/layout/activity_exercises.xml` -> agrega `BottomNavigationView` fijo inferior y ajusta constraints.
  - `app/src/main/res/layout/activity_third_hub.xml` -> nuevo layout de pantalla placeholder con nav inferior.
  - `app/src/main/AndroidManifest.xml` -> registro de `ThirdHubActivity`.
- Motivo:
  - Unificar navegacion principal y eliminar botones de cambio de pantalla redundantes.
- Impacto:
  - La barra inferior queda visible en Home y Ejercicios.
  - Tercer tab queda preparado para el modulo que se definira despues.
- Verificacion:
  - Revision de IDs de menu, binding y rutas de actividades completada.
  - Build final pendiente en Android Studio local.

---

### 2026-03-08 10:xx - Routine Detail Screen - `feat`

- Resumen: desde Home, al tocar una rutina se abre una pantalla con los ejercicios añadidos a esa rutina.
- Archivos modificados:
  - `app/src/main/java/com/example/gymtrackpro/ui/home/HomeActivity.kt` -> click en item de rutina navega a `RoutineDetailActivity`.
  - `app/src/main/java/com/example/gymtrackpro/ui/adapters/RoutineAdapter.kt` -> adapter actualizado para recibir callback de click.
  - `app/src/main/java/com/example/gymtrackpro/ui/routines/RoutineDetailActivity.kt` -> nueva pantalla de detalle de rutina con lista de ejercicios y estado vacio.
  - `app/src/main/java/com/example/gymtrackpro/ui/routines/RoutineDetailViewModel.kt` -> carga de ejercicios asociados a una rutina local.
  - `app/src/main/res/layout/activity_routine_detail.xml` -> nuevo XML de UI para detalle de rutina.
  - `app/src/main/java/com/example/gymtrackpro/data/local/dao/ExerciseDao.kt` -> nuevo `getByIds`.
  - `app/src/main/java/com/example/gymtrackpro/data/repository/GymRepository.kt` -> nuevo `getRoutineExercises(routineId)`.
  - `app/src/main/java/com/example/gymtrackpro/utils/ViewModelFactory.kt` -> soporte para `RoutineDetailViewModel`.
  - `app/src/main/AndroidManifest.xml` -> registro de `RoutineDetailActivity`.
- Motivo:
  - Continuar flujo funcional de rutinas mostrando su contenido real desde Home.
- Impacto:
  - El usuario puede inspeccionar ejercicios ya añadidos a cada rutina.
  - Si una rutina no tiene ejercicios vinculados, se muestra `No hay ejercicios añadidos`.
- Verificacion:
  - Revision de flujo Home -> Detalle de rutina completada.
  - Build final pendiente en Android Studio local.

---

### 2026-03-08 09:xx - Exercise Detail Media Fallback UI - `fix`

- Resumen: se ajusta la carga de imagen en detalle para evitar icono de error visible y usar doble fuente de media.
- Archivos modificados:
  - `app/src/main/java/com/example/gymtrackpro/ui/exercises/ExerciseDetailActivity.kt` -> fuente primaria via proxy backend `/exercises/{id}/media` y fallback automatico a `gifUrl` directo si existe.
- Motivo:
  - Algunos ejercicios mostraban icono de imagen rota cuando una fuente fallaba.
- Impacto:
  - Mejor UX: menos errores visuales y mayor tasa de imagen cargada.
- Verificacion:
  - Revision de flujo de render completada.
  - Prueba en dispositivo pendiente.

---

### 2026-03-08 08:xx - Media Proxy Resilience - `fix`

- Resumen: se fortalece el endpoint de media para que no dependa solo de una URL externa; ahora intenta descarga directa de imagen desde candidatos RapidAPI cuando falla fetch externo.
- Archivos modificados:
  - `backend/index.js` -> nuevo `fetchImageFromRapidApiCandidates(id)` y fallback interno en `GET /exercises/:id/media`.
- Motivo:
  - En Railway se detecto error `fetch failed` al descargar media por URL fallback externa.
- Impacto:
  - Mayor probabilidad de obtener imagen/GIF aun cuando un host externo no resuelve por DNS.
- Verificacion:
  - Sintaxis backend valida (`node --check backend/index.js`).
  - Validacion funcional pendiente tras redeploy.

---

### 2026-03-07 22:xx - Media Proxy Endpoint for DNS Issues - `fix`

- Resumen: se agrega endpoint proxy de media para ejercicios y la app lo usa en detalle, evitando fallos por DNS del CDN externo en dispositivo local.
- Archivos modificados:
  - `backend/index.js` -> nuevo `GET /exercises/:id/media` (publico) que descarga y retransmite imagen/GIF desde el proveedor externo.
  - `app/src/main/java/com/example/gymtrackpro/data/remote/api/ApiClient.kt` -> `BASE_URL` expuesto para construir URL proxy en UI.
  - `app/src/main/java/com/example/gymtrackpro/ui/exercises/ExerciseDetailActivity.kt` -> carga de media prioriza proxy `${BASE_URL}exercises/{id}/media`.
- Motivo:
  - En algunos entornos la URL CDN directa no resuelve (`cloudfront`), causando icono de error.
- Impacto:
  - Mayor confiabilidad de imagen/GIF en detalle de ejercicio.
  - Se reduce dependencia de DNS del dispositivo final.
- Verificacion:
  - Sintaxis backend valida (`node --check backend/index.js`).
  - Validacion funcional pendiente tras redeploy y prueba en dispositivo.

---

### 2026-03-07 21:xx - Glide Listener Compile Fix - `fix`

- Resumen: se corrige error de compilacion en `ExerciseDetailActivity` por incompatibilidad de firmas en `RequestListener` de Glide.
- Archivos modificados:
  - `app/src/main/java/com/example/gymtrackpro/ui/exercises/ExerciseDetailActivity.kt` -> se elimina listener custom y se mantiene estrategia visual con `placeholder` + `error`.
- Motivo:
  - Android Studio mostraba: `onLoadFailed overrides nothing` / `onResourceReady overrides nothing`.
- Impacto:
  - Build vuelve a compilar.
  - La vista de imagen mantiene fallback visual sin bloque en blanco.
- Verificacion:
  - Revision de imports y llamadas Glide completada.
  - Compilacion local final pendiente en entorno Android Studio.

---

### 2026-03-07 21:xx - Exercise Detail Image Blank Fix - `fix`

- Resumen: se corrige espacio en blanco en detalle de ejercicio cuando la carga de GIF/imagen falla (caso visible en modo oscuro).
- Archivos modificados:
  - `app/src/main/java/com/example/gymtrackpro/ui/exercises/ExerciseDetailActivity.kt` -> Glide ahora usa `placeholder`, `error` y `RequestListener`; si falla la carga se oculta `ImageView` para evitar bloque vacio.
- Motivo:
  - En algunos ejercicios la URL de media falla o tarda, dejando un area vacia que parecia bug visual.
- Impacto:
  - Mejor UX en detalle: sin hueco blanco al fallar media.
  - Comportamiento mas consistente en tema claro/oscuro.
- Verificacion:
  - Revision de flujo de render y fallback visual completada.
  - Validacion final en dispositivo pendiente.

---

### 2026-03-07 20:xx - GIF URL Fallback by Exercise ID - `fix`

- Resumen: cuando ExerciseDB no devuelve `gifUrl`, el backend ahora construye una URL fallback por `id` para mantener vista de GIF en la app.
- Archivos modificados:
  - `backend/index.js` -> nueva funcion `normalizeGifUrl(url, id)` con fallback `https://d205bpvrqc9yn1.cloudfront.net/{id}.gif` para ids numericos.
- Motivo:
  - Se detectaron respuestas validas de ejercicio sin `gifUrl` (ej. `id=0001`) pese a tener resto de informacion.
- Impacto:
  - Mayor cobertura visual de GIF en detalle sin depender totalmente del campo remoto.
- Verificacion:
  - Sintaxis backend valida (`node --check backend/index.js`).
  - Validacion funcional pendiente tras redeploy del backend.

---

### 2026-03-07 19:xx - Exercise GIF Fallback by ID - `fix`

- Resumen: se agrega recuperacion de detalle por ID para ejercicios, permitiendo obtener GIF/instrucciones aunque no vengan en el listado paginado.
- Archivos modificados:
  - `backend/index.js` -> nuevo endpoint `GET /exercises/:id` con enriquecimiento remoto desde ExerciseDB y merge en cache.
  - `app/src/main/java/com/example/gymtrackpro/data/remote/api/ApiService.kt` -> nuevo `getExerciseById`.
  - `app/src/main/java/com/example/gymtrackpro/data/repository/GymRepository.kt` -> nuevo `fetchExerciseDetailFromApi(exerciseId)`.
  - `app/src/main/java/com/example/gymtrackpro/ui/exercises/ExerciseDetailViewModel.kt` -> ViewModel para cargar detalle remoto en pantalla de detalle.
  - `app/src/main/java/com/example/gymtrackpro/utils/ViewModelFactory.kt` -> soporte para `ExerciseDetailViewModel`.
  - `app/src/main/java/com/example/gymtrackpro/ui/exercises/ExercisesActivity.kt` -> envia `exercise.id` al abrir detalle.
  - `app/src/main/java/com/example/gymtrackpro/ui/exercises/ExerciseDetailActivity.kt` -> carga remota de detalle si faltan media/instrucciones en extras iniciales.
- Motivo:
  - El endpoint de lista no siempre incluye `gifUrl` en todos los items; hacia falta una via de detalle para completar informacion visual.
- Impacto:
  - Mayor tasa de ejercicios con GIF visible en detalle.
  - Instrucciones y metadatos mas completos al abrir un ejercicio.
- Verificacion:
  - Sintaxis backend valida (`node --check backend/index.js`).
  - Validacion funcional pendiente tras redeploy/restart del backend.

---

### 2026-03-07 18:xx - Exercise Media + Material 3 UI - `feat`

- Resumen: se robustece carga de GIF/imagen de ejercicios y se moderniza la interfaz de lista con componentes Material Design 3.
- Archivos modificados:
  - `backend/index.js` -> `mapExerciseDto` ahora contempla mas variantes de media (`gifUrl`, `gif_url`, `image`, `imageUrl`) y debug incluye muestra de GIF (`sampleHasGif`, `sampleGifUrl`).
  - `app/src/main/java/com/example/gymtrackpro/ui/exercises/ExerciseDetailActivity.kt` -> carga de media con Glide usando URL normalizada (http->https) y fallback de render estandar.
  - `app/src/main/res/layout/activity_exercises.xml` -> buscador migrado a `TextInputLayout`/`TextInputEditText` y boton Material 3 tonal.
  - `app/src/main/res/layout/item_exercise.xml` -> tarjetas migradas a `MaterialCardView` con mejor jerarquia visual.
  - `app/src/main/java/com/example/gymtrackpro/ui/exercises/ExercisesActivity.kt` -> `RecyclerView` optimizado con `setHasFixedSize(true)`.
- Motivo:
  - Los GIF no se mostraban en algunos casos por variacion de campo/URL en proveedor externo y se solicitaba actualizar visual a enfoque moderno Material 3.
- Impacto:
  - Mayor probabilidad de visualizar media de ejercicios.
  - Interfaz mas moderna y consistente con Material Design 3.
- Verificacion:
  - Sintaxis backend valida (`node --check backend/index.js`).
  - Validacion visual/funcional de GIF pendiente en dispositivo tras reinicio backend.

---

### 2026-03-07 17:xx - Exercise Detail Screen - `feat`

- Resumen: al tocar una tarjeta de ejercicio se abre una nueva pantalla con toda la informacion disponible, incluyendo GIF/imagen cuando la API lo entrega.
- Archivos modificados:
  - `backend/index.js` -> se amplian campos de respuesta de ejercicios (`gifUrl`, `secondaryMuscles`, `instructions`).
  - `app/src/main/java/com/example/gymtrackpro/data/remote/dto/ExerciseDtos.kt` -> DTO actualizado con campos de detalle.
  - `app/src/main/java/com/example/gymtrackpro/data/local/entities/ExerciseEntity.kt` -> entidad local extendida con `gifUrl`, musculos secundarios e instrucciones.
  - `app/src/main/java/com/example/gymtrackpro/data/local/db/AppDatabase.kt` -> version de Room actualizada a `3`.
  - `app/src/main/java/com/example/gymtrackpro/data/repository/GymRepository.kt` -> mapeo de datos de detalle desde API a Room.
  - `app/src/main/java/com/example/gymtrackpro/ui/adapters/ExerciseAdapter.kt` -> click en tarjeta para abrir detalle.
  - `app/src/main/java/com/example/gymtrackpro/ui/exercises/ExercisesActivity.kt` -> navegacion a `ExerciseDetailActivity` con extras.
  - `app/src/main/java/com/example/gymtrackpro/ui/exercises/ExerciseDetailActivity.kt` -> nueva activity de detalle con carga de GIF/imagen.
  - `app/src/main/res/layout/activity_exercise_detail.xml` -> nuevo XML de UI para mostrar datos completos.
  - `app/src/main/AndroidManifest.xml` -> registro de `ExerciseDetailActivity`.
  - `app/build.gradle.kts` -> dependencia `Glide` para mostrar GIF/imagen remota.
- Motivo:
  - Completar flujo de ejercicios con vista detallada y visual mas rica al seleccionar una tarjeta.
- Impacto:
  - El usuario puede abrir y revisar detalle completo de cada ejercicio.
  - Si la API incluye `gifUrl`, se visualiza animacion; si no, se oculta el bloque de imagen.
- Verificacion:
  - Validacion estatica de referencias y sintaxis backend completada.
  - Build Android pendiente en entorno local con JDK/JAVA_HOME configurado.

---

### 2026-03-07 16:xx - Exercises Loading UX - `fix`

- Resumen: se corrige bug visual de infinite scroll donde el indicador de carga quedaba centrado durante paginacion.
- Archivos modificados:
  - `app/src/main/res/layout/activity_exercises.xml` -> se separan dos indicadores: `progressInitial` (centro) y `progressPaging` (inferior, pequeno).
  - `app/src/main/java/com/example/gymtrackpro/ui/exercises/ExercisesActivity.kt` -> render de carga por contexto: inicial cuando no hay items, paginacion cuando ya hay lista.
- Motivo:
  - Evitar que la carga incremental tape visualmente la lista al bajar en infinite scroll.
- Impacto:
  - Mejor experiencia visual: carga inicial centrada y carga de nuevas paginas en la parte baja.
- Verificacion:
  - Revision de binding y estados de visibilidad completada.
  - Prueba funcional pendiente en emulador/dispositivo.

---

### 2026-03-07 15:xx - Infinite Scroll Provider Fallback - `fix`

- Resumen: se ajusta infinite scroll para escenarios donde la API externa devuelve lotes fijos o ignora parcialmente `offset`.
- Archivos modificados:
  - `app/src/main/java/com/example/gymtrackpro/ui/exercises/ExercisesViewModel.kt` -> fin de paginacion solo cuando pagina llega vacia (`page.isEmpty()`), y `PAGE_SIZE=50` para estabilidad.
  - `backend/index.js` -> fallback de carga grande unica (`EXERCISE_FULL_FETCH_LIMIT`) cuando una expansion no agrega nuevos items (`noGrowth`).
  - `backend/.env.example` -> se agrega `EXERCISE_FULL_FETCH_LIMIT=2000`.
- Motivo:
  - Se seguian mostrando pocos ejercicios porque el proveedor podia no avanzar como se esperaba por offset.
- Impacto:
  - Scroll infinito sigue cargando mas datos de forma robusta incluso con comportamiento irregular del proveedor.
- Verificacion:
  - Sintaxis valida con `node --check backend/index.js`.
  - Validacion funcional pendiente tras reinicio/deploy del backend.

---

### 2026-03-07 14:xx - Infinite Scroll Cutoff Fix - `fix`

- Resumen: se corrige corte prematuro del catalogo de ejercicios en backend cuando la API externa devuelve lotes menores al `limit` solicitado.
- Archivos modificados:
  - `backend/index.js` -> no se marca fin de fuente por `page.length < limit`; ahora solo se agota cuando no llegan datos o cuando una pagina no agrega nuevos items (noGrowth).
  - `backend/index.js` -> `EXTERNAL_PAGE_SIZE` pasa a variable configurable (`EXERCISE_FETCH_PAGE_SIZE`, default `50`) para mejorar compatibilidad con limites reales del proveedor.
  - `backend/.env.example` -> se agrega `EXERCISE_FETCH_PAGE_SIZE=50`.
- Motivo:
  - El backend podia detener la expansion de cache demasiado pronto y el scroll se quedaba en un subconjunto (ejercicios iniciales).
- Impacto:
  - Infinite scroll puede seguir trayendo mas paginas de forma estable.
  - Mejor tolerancia a comportamiento real de paginacion de ExerciseDB/RapidAPI.
- Verificacion:
  - Sintaxis valida con `node --check backend/index.js`.
  - Prueba funcional en app pendiente tras redeploy/restart backend.

---


### 2026-03-07 13:xx - Backend Pagination Integrity - `fix`

- Resumen: se corrige paginacion de ejercicios para infinite scroll real, evitando cortes por offset basado en cache deduplicada.
- Archivos modificados:
  - `backend/index.js` -> cache de ejercicios ahora mantiene `remoteOffset` y `sourceExhausted`; la expansion usa offset remoto real y no el tamaño de cache local.
  - `backend/index.js` -> `GET /exercises` con `q` expande cache por pasos hasta cubrir `offset + limit` o agotar fuente remota.
  - `app/src/main/java/com/example/gymtrackpro/ui/exercises/ExercisesViewModel.kt` -> se restaura estrategia de infinite scroll por lotes (sin auto-carga total), con `PAGE_SIZE=100`.
  - `backend/.env.example` -> nueva variable `EXERCISE_CACHE_MAX_ITEMS`.
- Motivo:
  - Aun faltaban ejercicios porque la paginacion podia detenerse por calculo de offset incorrecto en backend y por estrategia de autoload en app.
- Impacto:
  - El backend puede seguir trayendo mas paginas correctamente.
  - La app vuelve a comportamiento de infinite scroll consistente.
- Verificacion:
  - Sintaxis backend validada con `node --check backend/index.js`.
  - Prueba funcional de scroll/busqueda pendiente en emulador/dispositivo.

---

### 2026-03-07 12:xx - Exercises Auto Full Load - `refactor`

- Resumen: la pantalla de ejercicios deja de depender del scroll para completar carga y ahora auto-descarga todos los lotes (hasta tope de seguridad) al entrar o al buscar.
- Archivos modificados:
  - `app/src/main/java/com/example/gymtrackpro/ui/exercises/ExercisesViewModel.kt` -> nuevo flujo `loadAllPagesForCurrentQuery()` con corrutina que itera paginas remotas y acumula resultados hasta fin de fuente o `MAX_AUTO_ITEMS`.
- Motivo:
  - El usuario seguia viendo carga parcial porque infinite scroll requiere accion de desplazamiento.
- Impacto:
  - Se cargan muchos mas ejercicios automaticamente desde el inicio.
  - La busqueda tambien completa lotes sin esperar scroll.
- Verificacion:
  - Revision de estados de paginacion y finalizacion (`endReached`) completada.
  - Compilacion por terminal pendiente (`JAVA_HOME` no configurado en shell).

---

### 2026-03-07 12:xx - Exercises Search/Scroll Stabilization - `fix`

- Resumen: se corrige busqueda de ejercicios y scroll infinito para evitar cortes prematuros y resultados mezclados al escribir rapido.
- Archivos modificados:
  - `backend/index.js` -> `GET /exercises` ahora, con `q`, sigue cargando paginas externas hasta cubrir realmente `offset + limit` o agotar fuente remota.
  - `app/src/main/java/com/example/gymtrackpro/ui/exercises/ExercisesViewModel.kt` -> control de corrutinas por generacion + cancelacion de carga anterior para evitar race conditions en busqueda.
  - `app/src/main/java/com/example/gymtrackpro/ui/exercises/ExercisesActivity.kt` -> prefetch automatico cuando la lista inicial no llena pantalla y no hay scroll aun.
- Motivo:
  - El buscador podia fallar al cambiar texto durante una carga activa y el infinite scroll podia frenarse cuando no habia desplazamiento inicial.
- Impacto:
  - Busqueda estable con resultados del query actual.
  - Scroll infinito continua cargando lotes incluso en pantallas donde el primer lote no alcanza para desplazar.
- Verificacion:
  - Revision de flujo de carga (UI -> ViewModel -> Repository -> API) y control de estados de paginacion completada.
  - Compilacion local por terminal pendiente (`JAVA_HOME` no configurado en shell).

---

### 2026-03-07 11:xx - Exercises Pagination/Search - `feat`

- Resumen: se mejora carga de ejercicios con paginacion remota real + busqueda remota, evitando limitar resultados solo a items ya cargados en pantalla.
- Archivos modificados:
  - `backend/index.js` -> `GET /exercises` ahora acepta `limit`, `offset`, `q` y expande cache por lotes para responder paginas grandes.
  - `app/src/main/java/com/example/gymtrackpro/data/remote/api/ApiService.kt` -> contrato de `getExercises` actualizado con query params de paginacion y busqueda.
  - `app/src/main/java/com/example/gymtrackpro/data/repository/GymRepository.kt` -> nuevo `fetchExercisesPageFromApi(query, limit, offset)` para traer paginas remotas y persistirlas en Room.
  - `app/src/main/java/com/example/gymtrackpro/ui/exercises/ExercisesViewModel.kt` -> corrutinas con debounce de busqueda (`onQueryChanged`) + infinite scroll (`loadMoreIfNeeded` / `loadNextPage`) y pagina aumentada a `80`.
- Motivo:
  - Resolver que aparecian pocos ejercicios y que la busqueda solo funcionaba sobre los ya pintados.
- Impacto:
  - Se muestran muchos mas ejercicios conforme haces scroll.
  - La busqueda trae resultados remotos por texto y no queda limitada al primer lote local.
  - La pantalla mantiene UX fluida usando corrutinas para debounce y carga incremental.
- Verificacion:
  - Revision de flujo end-to-end en backend/app y compatibilidad de parametros en Retrofit/Repository/ViewModel.
  - Prueba final en dispositivo/emulador pendiente.

---

### 2026-03-07 10:xx - Android Build - `fix`

- Resumen: se corrige fallo de `mergeDebugResources` por caracter BOM (`U+FEFF`) al inicio de `activity_exercises.xml`.
- Archivos modificados:
  - `app/src/main/res/layout/activity_exercises.xml` -> regrabado sin BOM (UTF-8 limpio).
- Motivo:
  - Resolver error de parser XML: `mismatched input '﻿'` y `root is null` durante Data Binding.
- Impacto:
  - Se desbloquea el merge de recursos para build `debug`.
- Verificacion:
  - Firma inicial del archivo validada sin BOM (`3C 3F 78`).

---

### 2026-03-07 10:xx - Exercises + Routines Flow - `refactor`

- Resumen: se ajusta flujo para carga masiva de ejercicios con infinite scroll por corrutinas y se mueve creacion de rutina al boton `+` de Home.
- Archivos modificados:
  - `app/src/main/java/com/example/gymtrackpro/ui/exercises/ExercisesViewModel.kt` -> corrutinas en `loadExercises()` y `loadMoreIfNeeded()` para carga incremental (infinite scroll) y filtrado por busqueda.
  - `app/src/main/java/com/example/gymtrackpro/ui/exercises/ExercisesActivity.kt` -> elimina logica de crear rutina y agrega `RecyclerView.OnScrollListener` para pedir mas items.
  - `app/src/main/res/layout/activity_exercises.xml` -> elimina boton `Crear rutina`; mantiene buscador + boton Home + lista.
  - `app/src/main/java/com/example/gymtrackpro/ui/home/HomeViewModel.kt` -> nueva logica de `createRoutine(name, timeMinutes)` con corrutina para guardar y sincronizar.
  - `app/src/main/java/com/example/gymtrackpro/ui/home/HomeActivity.kt` -> boton `+` abre formulario (`dialog_create_routine`) y ejecuta creacion de rutina.
- Motivo:
  - Mostrar muchos ejercicios de forma escalable y dejar la creacion de rutinas en el punto principal del modulo Home.
- Impacto:
  - Ejercicios cargan por lotes y se extienden al hacer scroll.
  - El formulario de rutina queda centralizado en Home, con nombre y tiempo requeridos.
  - La corrutina principal de infinite scroll queda en `ExercisesViewModel.loadMoreIfNeeded()`.
- Verificacion:
  - Revision de referencias de IDs, observers y navegacion.
  - Compilacion final pendiente de validacion en Android Studio local.

---

### 2026-03-07 10:xx - Exercises Screen - `feat`

- Resumen: se agrega pantalla de ejercicios con tarjetas, carga por corrutinas, buscador, boton `Home` y formulario para crear rutina con nombre + tiempo.
- Archivos modificados:
  - `app/src/main/java/com/example/gymtrackpro/ui/exercises/ExercisesActivity.kt` -> nueva pantalla de ejercicios con filtro en tiempo real y dialogo de creacion de rutina.
  - `app/src/main/java/com/example/gymtrackpro/ui/exercises/ExercisesViewModel.kt` -> logica de carga de ejercicios con corrutinas, filtrado por busqueda y alta de rutina.
  - `app/src/main/res/layout/activity_exercises.xml` -> layout con barra de busqueda, boton Home, lista de tarjetas y boton `Crear rutina`.
  - `app/src/main/res/layout/dialog_create_routine.xml` -> formulario de nombre y tiempo de rutina.
  - `app/src/main/java/com/example/gymtrackpro/utils/ViewModelFactory.kt` -> soporte para `ExercisesViewModel`.
  - `app/src/main/AndroidManifest.xml` -> registro de `ExercisesActivity`.
  - `app/src/main/java/com/example/gymtrackpro/ui/home/HomeActivity.kt` -> boton para navegar a la nueva pantalla de ejercicios.
  - `app/src/main/res/layout/activity_home.xml` -> agrega boton `Ver ejercicios`.
- Motivo:
  - Mostrar catalogo grande de ejercicios de forma usable y preparar el flujo inicial de creacion de rutinas desde UI.
- Impacto:
  - El usuario puede buscar ejercicios rapidamente.
  - Se habilita formulario inicial para crear rutina con nombre y duracion (guardada en nombre como texto).
- Verificacion:
  - Revision de referencias de IDs, binding y navegacion entre Home y Exercises.
  - Compilacion final pendiente de ejecucion local en Android Studio.

---

### 2026-03-07 09:xx - Android Build - `fix`

- Resumen: se corrige error de `mergeDebugResources` causado por parseo invalido de `activity_home.xml`.
- Archivos modificados:
  - `app/src/main/res/layout/activity_home.xml` -> XML reescrito en formato limpio/ASCII para evitar fallo de parser en Data Binding.
- Motivo:
  - Resolver bloqueo de compilacion: `Cannot read field \"elmName\" because \"root\" is null`.
- Impacto:
  - Se restablece compilacion de recursos en `debug` para pantalla Home.
- Verificacion:
  - Revision directa del XML final y estructura de IDs usada por `HomeActivity`.
  - Compilacion local en Android Studio pendiente de validacion final en tu entorno.

---

### 2026-03-07 09:xx - Home UI - `feat`

- Resumen: `HomeActivity` ahora muestra rutinas del usuario en lugar de ejercicios, con estado vacio y boton `+` inferior para iniciar alta de rutinas.
- Archivos modificados:
  - `app/src/main/java/com/example/gymtrackpro/ui/home/HomeViewModel.kt` -> expone `routines` desde repositorio y sincronizacion general con `syncData()`.
  - `app/src/main/java/com/example/gymtrackpro/ui/home/HomeActivity.kt` -> cambia adapter a rutinas, muestra `No hay rutinas` cuando lista vacia y agrega accion de boton `+`.
  - `app/src/main/res/layout/activity_home.xml` -> nuevo layout orientado a lista de rutinas + texto vacio + boton `+` abajo.
  - `app/src/main/java/com/example/gymtrackpro/ui/adapters/RoutineAdapter.kt` -> nuevo adapter para renderizar `RoutineEntity` en la lista.
- Motivo:
  - Cambiar enfoque de Home para mostrar primero las rutinas del usuario y preparar flujo de creacion de rutinas.
- Impacto:
  - Si no hay rutinas, la pantalla muestra `No hay rutinas`.
  - Se agrega punto de entrada visual para futura funcion de crear rutina.
- Verificacion:
  - Revision de referencias de binding/IDs y flujo de observacion de datos completada.
  - Compilacion por terminal pendiente de entorno local (`JAVA_HOME` no configurado en shell).

---

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

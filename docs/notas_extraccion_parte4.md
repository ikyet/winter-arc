# Notas de extracción — Parte 4 (puerto de lógica Swift→Java + corrección de layouts)

Contexto: en `ConfigActivity.java` y `DailyTrackingActivity.java` (ya extraídas por agentes
previos, ver `notas_extraccion_parte1.md`), varios métodos invocados desde `onCreate()` nunca
aparecen como código Java en `reporte_texto_extraido.txt` (se confirmó con búsqueda exhaustiva
de `func <nombre>` y de los nombres de método tal cual en la sección Android, líneas
~1010-4549). La app iOS/Swift (misma app, mismo autor, mismo backend PHP, sección desde línea
~4549) sí implementa la misma funcionalidad, así que esos fragmentos se usaron como referencia
funcional para portar la lógica a Java/Android. Ningún método fue "inventado desde cero" salvo
los explícitamente marcados como `TODO: inferido por convención`.

## (a) `ConfigActivity.java` — métodos agregados

Imports agregados: `android.app.AlertDialog`, `android.content.Intent`, `android.net.Uri`,
`android.widget.Toast`, `com.android.volley.{Request,toolbox.StringRequest,toolbox.Volley}`,
`org.json.{JSONArray,JSONObject}`, `java.util.{HashMap,Map}`.

- **`actualizarTextoModo()`** — sin equivalente Swift claro (iOS usa un `Picker`/`Toggle`
  declarativo, no un texto que se actualiza manualmente). Implementación mínima: lee
  `prefs.getBoolean("modo_oscuro", true)` y ajusta el texto de `btnModo`. Marcado con `TODO`.
- **`cambiarModo()`** — portado desde Swift `func cambiarModo(){modoOscuro.toggle()}`
  (línea **8199**). Se tradujo el toggle de estado SwiftUI a un booleano en `SharedPreferences`
  (`prefs`, ya inicializado en `onCreate()`), clave `"modo_oscuro"`.
- **`cerrarSesion()`** — sin equivalente Swift portable literal (el Swift `logout()`, línea
  **8212**, reemplaza el `rootViewController` de la ventana, algo específico de UIKit/SwiftUI).
  Se implementó por convención estándar Android: `sessionManager.logout()` + `Intent` a
  `LoginActivity` con `FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK` + `finish()`. Marcado
  con `TODO: inferido`.
- **`abrirFuentes()` / `abrirLink(String url)`** — portados desde el `confirmationDialog`
  "Selecciona una fuente" y `func abrirLink(_ url:String)` (ambos en línea **~8135-8199**). Las
  tres URLs se tomaron literalmente del texto Swift: `https://musclewiki.com`,
  `https://insp.mx/informacion-relevante/bam-bienvenida`,
  `https://www.incmnsz.mx/2019/TABLAS_ALIMENTOS.pdf`. `abrirFuentes()` muestra un
  `AlertDialog.Builder(...).setItems(...)` con las tres opciones; cada una llama a `abrirLink()`,
  que hace `startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)))`.
- **`cargarAlimentos()`** — portado desde Swift `func cargarAlimentos()` (línea **8162**: GET a
  `listar_alimentos.php?id_categoria=1`, respuesta es un **arreglo JSON plano**, no envuelta en
  `{success,data}`); también se revisaron los fragmentos del mismo endpoint en líneas **~5471**
  (FoodListActivity-equivalente iOS) y **~6492**. Se replicó con `StringRequest` (`Request.Method.GET`)
  siguiendo el estilo Volley de `FoodListActivity.java`, parseando el `JSONArray` de la respuesta
  y mostrando un `AlertDialog` con `setMultiChoiceItems(...)` (equivalente funcional al `.sheet`
  con `Toggle` por alimento de la versión iOS) para marcar alimentos no deseados en el campo
  `seleccionados` (ya declarado en la clase).
- **`guardarAlimentosNoDeseados()`** (método privado nuevo, no listado explícitamente en la
  tarea pero necesario para que el botón "Guardar" del diálogo anterior tenga efecto) — portado
  desde Swift `func guardarAlimentos()` (línea **8176-8186**), hace POST a `URL_ALIMENTOS` (ya
  declarada) con `id_usuario` y `ids_alimentos[i]`.
- **`guardarPeso()`** — **no estaba en la lista explícita de métodos a completar** para
  `ConfigActivity` en el encargo, pero `onCreate()` ya lo invocaba desde
  `editPeso.setOnFocusChangeListener(...)`, así que se completó igualmente (si no, el archivo no
  compila). Portado desde Swift `func guardarPeso()` (línea **8156**, la variante de la propia
  pantalla de configuración: valida `Double(peso)==nil` → "Dato no válido", luego POST a
  `guardar_peso_diario.php` con `id_usuario` y `peso`). Usa `URL_PESO` (ya declarada).

## (b) `DailyTrackingActivity.java` — métodos agregados/completados

Imports agregados: `android.widget.Toast`, `com.android.volley.{Request,toolbox.StringRequest,toolbox.Volley}`,
`com.github.mikephil.charting.data.{BarData,BarDataSet,BarEntry}`, `org.json.{JSONArray,JSONObject}`,
`java.util.{ArrayList,HashMap,Map}`.

- **`manejarGoogleFit()`** — se completó el cuerpo cortado (el reporte termina justo tras
  `GoogleSignIn.getAccountForExtension(...)`). Se usó el patrón estándar
  `GoogleSignIn.hasPermissions(account, fitnessOptions)` /
  `GoogleSignIn.requestPermissions(this, 2002, account, fitnessOptions)`, como equivalente
  funcional del bloque Swift `healthManager.requestPermission {...} healthManager.fetchStepsToday {...}`
  (línea **7681-7723**, que en iOS usa Apple Health en vez de Google Fit — no es un puerto
  literal, solo el mismo propósito: pedir permiso y luego leer pasos).
  **Importante:** se verificó el directorio `android/app/src/main/java/com/winterarc/app/` y
  **`GoogleFitManager.java` NO existe** (ningún agente paralelo lo creó). Siguiendo la
  instrucción de la tarea de no referenciar esa clase si no existe, `manejarGoogleFit()` **no
  invoca ningún método sobre `fitManager`**; en el rama "permiso concedido" solo llama a un
  nuevo helper `actualizarUiPasos()` que pinta la UI con `pasosActuales` (0 por defecto). Queda
  un comentario `TODO` explícito indicando que, cuando `GoogleFitManager` se reconstruya, ahí debe
  leerse el conteo real de pasos antes de llamar a `actualizarUiPasos()`.
  **Nota de riesgo no resuelta:** el campo `fitManager` y la línea `fitManager = new GoogleFitManager();`
  dentro de `onCreate()` **ya existían** en el archivo antes de esta sesión (de un agente
  previo) y **no se tocaron** (la instrucción es no modificar líneas ya existentes). Como
  `GoogleFitManager.java` no existe, **el proyecto no compilará** hasta que esa clase se cree en
  otra sesión — esto no es un problema introducido aquí, pero se deja constancia expresa.
- **`actualizarUiPasos()`** (helper nuevo, sin referencia Swift) — actualiza `tvPasos`,
  `tvMetaPasos` y `progressPasos` a partir de `pasosActuales` y `obtenerMetaPasos()` (ya
  presente en el archivo). Marcado `TODO: inferido`.
- **`cargarSeguimiento()`** — portado desde Swift `func cargarSeguimiento()` (línea
  **7412-7482**). Usa el mismo endpoint y formato de respuesta
  (`obtener_seguimiento_diario.php` → `{success, data:{...}}`) que ya usa en Java
  `DashboardActivity.cargarResumen()` (patrón Volley/`StringRequest` reutilizado), agregando el
  campo `calorias_consumidas` que sí lee la versión Swift (Dashboard en Java solo usa
  `calorias_gastadas`). Al final llama a `actualizarUiPasos()`. El Swift original, al terminar,
  llama a `cargarHistorialPeso()` — aquí ese llamado se hace por separado desde `onCreate()`
  (`cargarGraficaPeso()`), tal como ya estaba estructurado el archivo previamente.
- **`cargarGraficaPeso()`** — portado desde Swift `func cargarHistorialPeso()` (línea
  **7495-7599**), que consulta `obtener_historial_peso.php` con `id_usuario` y arma una lista de
  pesos leyendo cada item como `"peso"` (Double o String) y, si no está, como `"peso_diario"`
  (Double o String). Se replicó esa doble validación con `JSONObject.has()`/`optDouble()`. En vez
  de una gráfica declarativa de SwiftUI, los valores se cargan en el `BarChart barPeso`
  (MPAndroidChart, ya importado) mediante `BarEntry`/`BarDataSet`/`BarData`.
- **`guardarPeso()`** — portado desde Swift `func guardarPeso()` (línea **7607-7679**, variante
  casi idéntica en **8156**). Valida que `etPeso` no esté vacío y sea numérico (equivalente a
  `if nuevoPeso.isEmpty` / `if Double(peso)==nil`), hace POST a `guardar_peso_diario.php` con
  `id_usuario` y `peso`, y al terminar limpia el campo y llama a `cargarGraficaPeso()` (igual que
  el Swift original limpia `nuevoPeso` y llama a `cargarHistorialPeso()`).

## (c) Layouts corregidos/creados

- **`activity_daily_tracking.xml`** — se agregaron `tvMetaPasos` (TextView) y `progressPasos`
  (ProgressBar horizontal estándar) dentro del mismo bloque "card" que ya contenía
  `tvCaloriasTracking`/`tvPasos`, mismo estilo (`@color/text`, 14sp). No hay fragmento explícito
  del reporte que muestre la posición exacta de `progressPasos`; se ubicó debajo de
  `tvMetaPasos` por inferencia razonable (no confirmada contra el reporte). La descripción
  textual de la sección 3.3 (línea ~479, "Seguimiento diario") sí confirma "dos TextView para
  pasos y meta" en el mismo bloque.
- **`activity_config.xml`** — reconstruido **desde cero**. El contenido anterior (bloque
  "Interfaz de configuración de perfil físico": `etEdad`, `etPeso`, `etEstatura`, `tvSexo`,
  `tvActividad`, `tvDias`, `tvObjetivo`, `tvProteinas`, `tvCarbohidratos`, `tvGrasas`,
  `btnRegistrarPerfil`) no correspondía a `ConfigActivity.java` (ya documentado por el agente de
  la Parte 1, probablemente pertenece a `UserProfileActivity`). El nuevo archivo usa únicamente
  los IDs reales: `btnModo`, `btnCerrarSesion`, `btnFuentes`, `btnModificarAlimentos`,
  `editPeso`. Estilo tomado literalmente de la descripción de la sección 3.3, apartado
  "CONFIGURACION" (línea **490-492**): fondo `#000000`, tarjetas `#111111` con padding 20dp,
  título blanco 28sp bold, `EditText` de peso con fondo `#1F1F1F`/hint `#777777`, botones con
  `backgroundTint="#2A2A2A"` y texto blanco, botón de cerrar sesión a todo el ancho en
  `#D32F2F`. No existe `colors.xml` en el proyecto (se verificó, no fue creado por ningún agente
  previo), así que se usaron valores hex literales, igual que hacía el reporte.
- **`activity_actualizacion_dieta_manager.xml`** — **archivo nuevo** (no se tocó ni se borró
  `activity_actualizacion_dieta.xml`, que pertenece a otra pantalla según la Parte 1). Contiene
  los IDs reales que usa `ActualizacionDietaActivity.java`: `tvCaloriasActuales`,
  `tvCaloriasNuevas`, `tvProteinas`, `tvCarbohidratos`, `tvGrasas`, `tvNotificacion`,
  `btnAceptar`, `btnRechazar`. No hay un bloque "Interfaz de..." explícito para esta pantalla en
  el reporte; la disposición (tarjeta de calorías, tarjeta de macros, fila de botones
  aceptar/rechazar, texto de notificación oculto por defecto con `visibility="gone"`, coherente
  con el `Handler().postDelayed(...,3000)` ya existente en el `.java`) es una inferencia
  razonable basada en el estilo oscuro general de la app y en la descripción del módulo
  "ACTUALIZACION DE DIETA" (línea 346-358), no una recuperación literal.

## (d) Pendientes / riesgos que quedan abiertos

1. `GoogleFitManager.java` sigue sin existir — `DailyTrackingActivity` no compilará hasta que se
   cree esa clase (ver punto (b) arriba). No se creó en esta sesión porque no estaba en el
   alcance de esta tarea y porque contiene lógica de negocio real no confirmable contra el
   reporte (mismo criterio que la Parte 1 aplicó a `ActualizacionDietaManager`).
2. `guardarPeso()` de `ConfigActivity` no estaba en la lista explícita de métodos a completar del
   encargo; se agregó de todas formas porque `onCreate()` ya lo invocaba y el archivo no
   compilaba sin él. Marcado con nota explicativa en el propio código.
3. No existe `colors.xml` en el proyecto (`@color/bg`, `@color/card`, `@color/text`,
   `@color/button`, usados en otros layouts, no están definidos en ningún archivo de recursos
   localizado). Fuera del alcance de esta tarea; se usaron hex literales en los layouts nuevos
   para no depender de recursos inexistentes.
4. El diálogo de selección múltiple de `cargarAlimentos()` (ConfigActivity) es un puerto
   *funcional*, no visual: la versión iOS usa un `.sheet` con `List`/`Toggle` de SwiftUI; aquí se
   usó `AlertDialog.setMultiChoiceItems(...)`, el equivalente Android más directo, pero el
   parecido visual con la UI real (si existía una pantalla dedicada en vez de un diálogo) no está
   confirmado.

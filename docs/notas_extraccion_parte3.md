# Notas de extracción — Parte 3 (rescate de clases "huérfanas": DietActivity, GoogleFitManager,
UserProfileActivity, y finalización de TrainingListActivity.crearCardEjercicio()/mostrarSelectorDias())

Fuente única: `C:\Claude Zona De Trabajo\WinterArc\docs\reporte_texto_extraido.txt`. Este agente
partió del hallazgo ya documentado por agentes anteriores (`notas_extraccion_parte1.md` punto 1,
`notas_extraccion_parte2.md` punto (e).2): el proceso de extracción del docx perdió varios
encabezados `public class NombreClase`, dejando bloques de código real "huérfanos" dentro del
rango de texto asignado a otras clases.

## (a) Archivos creados/modificados

Nuevos, en `android/app/src/main/java/com/winterarc/app/`:
- `DietActivity.java` — reconstruida a partir del rango ~1533-1764 del reporte (dentro del bloque
  que se le había asignado a DashboardActivity).
- `GoogleFitManager.java` — reconstruida a partir del rango ~2588-2634 del reporte (justo después
  del cierre de FoodListActivity).
- `UserProfileActivity.java` — reconstruida a partir del rango ~3748-4535 del reporte (dentro del
  bloque que se le había asignado a TrainingListActivity).

Modificado (NO se tocó nada del contenido preexistente, solo se completó lo marcado como
pendiente por el agente anterior):
- `TrainingListActivity.java` — se completó `crearCardEjercicio()` (cortado a mitad) y se agregó
  el stub de `mostrarSelectorDias()` (referenciado desde `onCreate()` pero nunca definido).

No se crearon layouts XML nuevos ni se tocó ninguno de los 7 ya existentes; queda pendiente para
una tarea futura un layout para `UserProfileActivity` (ver punto (d).6) y otro para `DietActivity`
(ver punto (d).1).

## (b) Confirmación de fronteras entre clases (verificado línea por línea)

1. **DashboardActivity termina realmente en la línea ~1526** del reporte (cierre del método
   `mostrarMensajeMotivacional()`), tal como ya había interpretado el agente de la Parte 1 (su
   `DashboardActivity.java` ya cierra la clase ahí). Desde la línea 1533 hasta la 1764 (justo
   antes de `public class ExerciseDetailActivity`, línea 1766) el contenido usa
   `DietActivity.this`, `JSONArray desayuno/comida/cena`, `crearSeccion(...)`,
   `completarComida(...)`, y la Descripción de la línea 1764 dice literalmente "...concluye el
   proceso de registro de comidas dentro de la actividad DietActivity." Confirmado: pertenece a
   `DietActivity`.
2. **El fragmento de `GoogleFitManager`** (líneas ~2588-2634) aparece entre el cierre explícito de
   `FoodListActivity` (línea 2573 `}`, Descripción línea 2576 "Con este bloque concluye
   completamente la clase FoodListActivity") y el marcador `public class LoginActivity` (línea
   2644). No hay ambigüedad de frontera aquí.
3. **`UserProfileActivity` cierra realmente en la línea 4535** (`}` inmediatamente después del
   comentario literal `// FIN DE CLASE`, línea 4534), y la Descripción de la línea 4538 lo
   confirma explícitamente: "Con este bloque concluye la implementación completa de
   UserProfileActivity." El rango completo de su código es ~3748-4535, justo antes de "ARCHIVOS
   SWIFT APP IOS" (línea 4549). Dentro de ese mismo rango, el corte real de `TrainingListActivity`
   ocurre en la línea 3738 (`card.setOrientation(LinearLayout.VERTICAL);`), confirmado por la
   propia Descripción de la línea 3741 ("Posteriormente comienza la implementación del método
   crearCardEjercicio()... Para ello se crea inicialmente un contenedor de tipo LinearLayout...").

## (c) Imports agregados

- `DietActivity.java`: `android.content.Intent`, `android.os.Bundle`, `android.widget.{Button,
  LinearLayout,TextView,Toast}`, `androidx.appcompat.app.AppCompatActivity`,
  `androidx.core.content.ContextCompat`, `com.android.volley.Request`,
  `com.android.volley.toolbox.{StringRequest,Volley}`, `org.json.{JSONArray,JSONObject}`,
  `java.util.{HashMap,Map}` (estos dos últimos técnicamente redundantes porque el código,
  siguiendo el texto literal del reporte, usa `java.util.Map`/`java.util.HashMap` totalmente
  calificados dentro de los `getParams()`; se dejaron los imports por si se simplifican esas
  referencias más adelante — no genera error de compilación, solo un import sin usar).
- `GoogleFitManager.java`: `android.content.Context`, `android.util.Log`,
  `com.google.android.gms.auth.api.signin.GoogleSignIn`, `com.google.android.gms.fitness.Fitness`,
  `com.google.android.gms.fitness.FitnessOptions`, `com.google.android.gms.fitness.data.{DataPoint,
  DataSet,DataType,Field}`, `com.google.android.gms.fitness.request.DataReadRequest`,
  `java.util.Calendar`, `java.util.concurrent.TimeUnit`. Mismo paquete base
  (`com.google.android.gms.fitness.*`) que ya usa `DailyTrackingActivity.java` de forma
  confirmada; los subpaquetes exactos de `DataPoint`/`DataSet`/`Field`/`DataReadRequest` son la
  ubicación estándar de la API de Google Fit, no verificables literalmente contra el reporte.
- `UserProfileActivity.java`: `android.app.AlertDialog`, `android.content.Intent`,
  `android.os.Bundle`, `android.view.View`, `android.widget.{Button,EditText,TextView,Toast}`,
  `androidx.appcompat.app.AppCompatActivity`, `com.android.volley.Request`,
  `com.android.volley.toolbox.{StringRequest,Volley}`, `org.json.{JSONArray,JSONObject}`,
  `java.util.{ArrayList,HashMap,Map}`.
- `TrainingListActivity.java`: no se agregó ningún import nuevo (el completado de
  `crearCardEjercicio()` solo usa `LinearLayout`/`TextView`, ya importados).

## (d) Fragmentos incompletos, ambiguos, o con anomalías detectadas — y cómo se resolvieron

1. **`DietActivity` — falta la apertura real de `onCreate()` y de `cargarDieta()`.** El texto del
   reporte para esta clase (~1533-1764) no comienza con el encabezado ni con `onCreate()`: arranca
   directamente en `JSONArray desayuno = json.getJSONArray("desayuno");`, es decir, a mitad de un
   callback de éxito de una petición Volley ya en curso, con la variable `json` ya en alcance. No
   se encontró en ningún otro punto del documento el inicio real de estos métodos. Se reconstruyó
   la apertura de `cargarDieta()` por convención (mismo patrón `StringRequest`/`try`/`JSONObject`
   usado en absolutamente todos los demás métodos Volley de esta app), y se confirmó el endpoint
   exacto (`obtener_dieta.php`, parámetro `id_usuario`, respuesta con `success`/`desayuno`/
   `comida`/`cena` en la raíz del JSON) contra el equivalente Swift/iOS de la misma pantalla
   (`struct DietView`, función `cargarDieta()`, líneas ~8439-8468 del propio reporte) — no es una
   invención, es información ya presente en el documento. `onCreate()` se redujo a lo
   estrictamente necesario para que `llComidas` no sea null (`setContentView` + `findViewById` +
   llamada a `cargarDieta()`); el nombre de recurso `R.layout.activity_diet` NO está confirmado.
2. **`GoogleFitManager` — el fragmento capturado es solo la cola de un método más largo.** El
   texto (~2588-2634) comienza a mitad de un `addOnSuccessListener(response -> {...})`, con
   `response` y `total` ya en uso. La construcción real de la consulta (rango de fechas,
   `DataReadRequest`, `Fitness.getHistoryClient(...).readData(...)`) no aparece en ningún punto del
   documento. Se agregó como plantilla mínima estándar de la API de Google Fit (boilerplate
   idiomático, no lógica de negocio propia de Winter Arc), reutilizando
   `DataType.TYPE_STEP_COUNT_DELTA` ya usado de forma confirmada en `DailyTrackingActivity.java`.
   El nombre público `obtenerPasos(Context, PasosCallback)` es una inferencia razonable a partir
   del único uso confirmado de esta clase en el proyecto (`fitManager = new GoogleFitManager();`
   en `DailyTrackingActivity.java` línea 64, que confirma constructor sin argumentos), no un hecho
   confirmado contra el reporte. **Esto debe verificarse contra el proyecto original si se
   recupera**, en particular el rango de fechas exacto y si se usa `bucketByTime`/`aggregate`.
3. **`UserProfileActivity` — falta la apertura real de `onCreate()` y de `seleccionarSexo()`.**
   Igual que en el caso de DietActivity: el texto capturado (~3748-4535) comienza a mitad de
   `.setItems(...)` dentro de `seleccionarSexo()` (confirmado por la Descripción de la línea 3761:
   "completa la implementación del método seleccionarSexo()"). Se reconstruyó la apertura de
   `seleccionarSexo()` (arreglo de opciones + título) usando el equivalente Swift/iOS de la MISMA
   pantalla (`struct ProfileView`, líneas ~6174-6176: `Picker("Sexo",...){Text("Masculino")...
   Text("Femenino")...}`) — contenido ya presente en el propio documento, no inventado. `onCreate()`
   completo (campos, `findViewById`, cableado de listeners) es reconstrucción estructural, ya que
   no aparece en el reporte; se documentó exhaustivamente dentro del propio archivo `.java` (ver
   comentario de cabecera). El resto de los métodos de esta clase (`seleccionarActividad()`,
   `seleccionarDias()`, `seleccionarObjetivo()`, `seleccionarAlimentosNoDeseados()`,
   `guardarAlimentosNoDeseados()`, `validarPerfil()`, `guardarPerfilServidor()`, `generarDieta()`,
   `generarRutina()`, `irAlDashboard()`, `onResume()`, `onDestroy()`) SÍ están completos en el
   reporte y se transcribieron literalmente, solo reparando espaciado.
4. **`UserProfileActivity.seleccionarAlimentosNoDeseados()` — operador de comparación perdido.**
   El texto original (~línea 3956-3959) trae `if( cantidadSeleccionados = totalAlimentos ){` — un
   único `=` (asignación), que no compila como condición de un `if` sobre tipos `int`. Se corrigió
   a `==`, consistente con la Descripción del propio reporte ("Si el usuario intenta excluir todos
   los alimentos, la operación es cancelada").
5. **`UserProfileActivity.guardarAlimentosNoDeseados()` — tipo genérico perdido.** El texto trae
   `ArrayListidsAlimentos` (tipo crudo `ArrayList`, sin `<Integer>`) como parámetro, pero el propio
   cuerpo del método hace `for(int id : idsAlimentos){...}` — un `ArrayList` crudo produce
   elementos `Object` en un `for-each`, que no se puede asignar directamente a `int` (no compila).
   Se restauró el parámetro genérico como `ArrayList<Integer>` (y el mismo tipo en la variable
   `noDeseados` del método que la llama), interpretando esto como el mismo tipo de pérdida de
   texto resaltado que ya afectó a otros diamantes genéricos del documento (p. ej.
   `new ArrayList<>()` sin el tipo a la izquierda) — es una reparación de tokenización, no una
   invención de lógica nueva.
6. **Layout de `UserProfileActivity` no reconstruido (fuera de alcance de esta tarea).** Según
   `notas_extraccion_parte1.md` (punto 11) y `notas_extraccion_parte4.md` (apartado (c)), el
   bloque de reporte titulado "Interfaz de configuración de perfil físico" (con los IDs
   `etEdad`, `etPeso`, `etEstatura`, `tvSexo`, `tvActividad`, `tvDias`, `tvObjetivo`,
   `tvProteinas`, `tvCarbohidratos`, `tvGrasas`, `btnRegistrarPerfil`) casi con certeza pertenece a
   esta pantalla, pero un agente previo ya lo excluyó de `activity_config.xml` (que ahora sirve
   correctamente a `ConfigActivity`) sin crear un archivo nuevo para él. Esta tarea tampoco lo creó
   (no estaba en el encargo); `UserProfileActivity.onCreate()` usa
   `setContentView(R.layout.activity_user_profile)` como marcador sin confirmar. **Pendiente para
   una tarea futura**: crear `activity_user_profile.xml` a partir de ese bloque de texto (buscar
   "Interfaz de configuración de perfil físico" en `reporte_texto_extraido.txt`, cerca de la línea
   1180-1230) y de la descripción de estilo de la sección 3.3 del reporte, siguiendo el mismo
   criterio que se aplicó a `activity_config.xml`/`activity_actualizacion_dieta_manager.xml` en
   `notas_extraccion_parte4.md`. Falta también un campo `tvError` en ese bloque de IDs documentado
   por los agentes previos; se agregó por convención (mismo patrón que `LoginActivity`/
   `RegisterActivity`, que sí usan `tvError`), no está confirmado en el bloque XML.
7. **Layout de `DietActivity` no reconstruido (fuera de alcance de esta tarea).** Según
   `notas_extraccion_parte1.md` (punto 12), el bloque titulado "Interfaz del módulo de dieta" (con
   `tvCalorias`, `tvMacros`, `llComidas`) probablemente pertenece a `DietActivity`, pero fue
   guardado como `activity_actualizacion_dieta.xml` (nombre incorrecto, heredado de una asignación
   de tarea anterior) y no se corrigió en esta sesión. `DietActivity.java` solo usa `llComidas`
   (no usa `tvCalorias`/`tvMacros`, que no aparecen en el rango de código Java confirmado de esta
   clase) — por eso el archivo `.java` no declara esos dos campos, para no inventar lógica de
   asignación que no está confirmada. **Pendiente para una tarea futura**: renombrar/duplicar ese
   layout como `activity_diet.xml` (o el nombre que finalmente use `DietActivity`) y decidir si
   `tvCalorias`/`tvMacros` realmente pertenecen a esta pantalla o a otra.
8. **`TrainingListActivity.crearCardEjercicio()` completado de forma mínima (según instrucción
   explícita de la tarea).** El reporte no muestra el cuerpo real (se corta justo después de
   `card.setOrientation(LinearLayout.VERTICAL);`). Se agregó solo un `TextView` con el nombre del
   ejercicio (clave JSON `"nombre_ejercicio"`, tomada de `ExerciseListActivity.crearCard()` de este
   mismo proyecto por ser el patrón de tarjeta más cercano) y `containerEjercicios.addView(card)`,
   marcado con `// TODO: cuerpo real de este método no estaba disponible en el reporte`. No se
   agregaron botones, navegación ni otros campos.
9. **`TrainingListActivity.mostrarSelectorDias()` creado como stub vacío**, con el mismo tipo de
   comentario `TODO`, tal como pedía la instrucción de la tarea (sin lógica compleja).
10. **Nota sobre `DailyTrackingActivity.java` (NO modificado en esta sesión, fuera de alcance):**
    ahora que `GoogleFitManager.java` existe con el método `obtenerPasos(Context, PasosCallback)`,
    el `TODO` que dejó el agente de la Parte 4 en `manejarGoogleFit()` (que actualiza la UI con
    `pasosActuales` fijo en 0 porque "GoogleFitManager no existe") podría resolverse llamando a
    `fitManager.obtenerPasos(this, pasos -> { pasosActuales = pasos; actualizarUiPasos(); })`. No
    se hizo aquí porque `DailyTrackingActivity.java` está en la lista de archivos que esta tarea
    tenía prohibido tocar; se deja constancia para una futura sesión.

## Resumen de endpoints confirmados en esta sesión (mismo host que el resto de la app,
`https://dodgerblue-emu-880788.hostingersite.com/winter_arc_api/`)

- `obtener_dieta.php` (POST, `id_usuario`) — usado por `DietActivity.cargarDieta()`. Respuesta:
  `success`, `desayuno`/`comida`/`cena` (arrays) en la raíz del JSON.
- `completar_comida.php` (POST, `id_usuario`, `tipo_comida`) — usado por
  `DietActivity.completarComida()`. Respuesta: `success`, `calorias_agregadas`.
- `listar_alimentos.php?id_categoria=N` (GET) — usado por
  `UserProfileActivity.seleccionarAlimentosNoDeseados()`. Respuesta: arreglo JSON plano de
  objetos con `nombre_alimento`/`id_alimento`.
- `guardar_alimentos_no_deseados.php` (POST, `id_usuario`, `ids_alimentos` como CSV) — usado por
  `UserProfileActivity.guardarAlimentosNoDeseados()`.
- `guardar_perfil.php` (POST, `id_usuario`, `edad`, `peso`, `estatura`, `sexo`, `actividad`,
  `objetivo`, `dias_entrenamiento`) — usado por `UserProfileActivity.guardarPerfilServidor()`.
- `generar_dieta.php` (POST, `id_usuario`) — usado por `UserProfileActivity.generarDieta()`.
- `generar_rutina.php` (POST, `id_usuario`, `dias_entrenamiento`) — usado por
  `UserProfileActivity.generarRutina()`.

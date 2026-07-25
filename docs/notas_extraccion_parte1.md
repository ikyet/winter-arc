# Notas de extracción — Parte 1 (líneas ~1010-2644 del reporte)

Reconstrucción a partir de `C:\Claude Zona De Trabajo\WinterArc\docs\reporte_texto_extraido.txt`,
localizando cada clase por su marcador literal `public class NombreClase` y cada layout por su
subtítulo `Interfaz de ...`, concatenando los bloques "Código" en orden y descartando los bloques
"Descripción".

## (a) Archivos creados

### Layouts XML (`android/app/src/main/res/layout/`)
- `activity_login.xml`
- `activity_register.xml`
- `activity_config.xml`
- `activity_dashboard.xml`
- `activity_actualizacion_dieta.xml`
- `activity_training_list.xml`
- `activity_daily_tracking.xml`

### Clases Java (`android/app/src/main/java/com/winterarc/app/`)
- `ActualizacionDietaActivity.java`
- `ConfigActivity.java`
- `DailyTrackingActivity.java`
- `DashboardActivity.java`
- `ExerciseDetailActivity.java`
- `ExerciseListActivity.java`
- `FoodListActivity.java`

No se crearon clases modelo/POJO adicionales (ver punto c).

## (b) Imports inferidos con menos certeza

La mayoría de imports (Bundle, AppCompatActivity, widgets básicos, Volley, org.json, java.util.*)
son estándar y de alta confianza. Los que requirieron más inferencia (no aparecen literalmente en
el texto, se dedujeron por el nombre de clase usado en el código):

- `ExerciseDetailActivity.java`: paquete exacto de la librería de YouTube
  (`com.pierfrancescosoffritti.androidyoutubeplayer.core.player...`) — es la librería más común
  para `YouTubePlayerView`/`AbstractYouTubePlayerListener`/`PlayerConstants` en Android, pero el
  reporte nunca muestra un import explícito, así que el path exacto del paquete es una suposición
  razonable, no un hecho confirmado. También se infirió `com.bumptech.glide.Glide` para `Glide.with(...)`.
- `DailyTrackingActivity.java`: paquetes de Google Fit/Play Services
  (`com.google.android.gms.fitness.*`, `com.google.android.gms.auth.api.signin.*`) — igualmente
  son los paquetes estándar para las clases usadas (`FitnessOptions`, `DataType`,
  `GoogleSignInAccount`, `GoogleSignIn`), pero no verificables contra el texto del reporte.
- `com.github.mikephil.charting.charts.PieChart/BarChart` (MPAndroidChart) en `DailyTrackingActivity`
  — el nombre de paquete completo ya aparece explícito en el XML (`activity_daily_tracking.xml`),
  así que ahí sí hay alta certeza; se usó el mismo paquete para el import Java.
- `ConfigActivity.java`: no se pudo inferir con certeza si usa Volley/JSON, porque el bloque de
  código asignado a esta clase termina en `onCreate()` sin llegar a los métodos
  (`cambiarModo`, `cerrarSesion`, etc.) que probablemente sí los usarían. Por eso el archivo no
  importa Volley/JSON — puede que falten imports una vez se agreguen esos métodos.

## (c) Modelos de datos inferidos/creados

**Ninguno fue creado.** Se evaluaron dos candidatos y se decidió NO crearlos porque no son POJOs
simples sino clases con lógica de negocio real que el reporte no muestra (crear una versión
"inventada" de esa lógica violaría la instrucción de no inventar lógica nueva):

- `ActualizacionDietaManager` (usada en `ActualizacionDietaActivity`): tiene un método estático
  `actualizarDieta(pesoActual, pesoObjetivo, caloriasActuales, objetivo)` que devuelve un objeto
  anidado `DietaActualizada` con campos `calorias`, `proteinas`, `carbohidratos`, `grasas`. El
  cálculo real (fórmulas de macros) no aparece en el rango del reporte asignado.
- `GoogleFitManager` (usada en `DailyTrackingActivity`): fragmentos parciales de su lógica
  aparecen *fuera* de mi rango asignado, en las líneas ~2588-2634 del reporte (después del cierre
  explícito de `FoodListActivity`), incluyendo un procesamiento de `DataSet`/`DataPoint` de Google
  Fit y una interfaz interna `PasosCallback { void onResult(int pasos); }`. Como
  `GoogleFitManager` no está en la lista de clases asignadas a este agente, no se reconstruyó; se
  deja constancia de dónde aparece su código parcial en el reporte para quien la reconstruya.

Otras clases del propio proyecto referenciadas pero no creadas (mismo paquete, no requieren
import): `SessionManager` (ya localizada en el reporte por otro agente/parte, marcador
`public class SessionManager` en línea 3242), `DietActivity`, `LoginActivity`,
`TrainingListActivity`, `ExerciseInProgressActivity`.

## (d) Fragmentos incompletos, ambiguos o con anomalías detectadas

1. **DashboardActivity vs. DietActivity mezcladas en el reporte.** Entre el marcador
   `public class DashboardActivity` (línea 1357) y el siguiente marcador de clase
   (`ExerciseDetailActivity`, línea 1766) el reporte contiene, además del código genuino de
   Dashboard, un bloque grande (líneas ~1533-1765) con `JSONArray desayuno/comida/cena`,
   `crearSeccion(...)`, `completarComida(...)`, que usa explícitamente `DietActivity.this` como
   calificador — algo que no podría compilar dentro de `DashboardActivity`. La propia
   "Descripción" del reporte en la línea 1764 lo confirma: *"...concluye el proceso de registro
   de comidas dentro de la actividad DietActivity."* Conclusión: el encabezado
   `public class DietActivity extends AppCompatActivity {` se perdió en la extracción (o estaba
   en una imagen no capturada como texto), y ese bloque completo pertenece realmente a
   `DietActivity`, no a `DashboardActivity`. **Se excluyó deliberadamente ese bloque de
   `DashboardActivity.java`** para no mezclar dos clases. `DietActivity` no está en la lista de
   clases asignadas a este agente, así que ese contenido queda documentado aquí para que se
   reconstruya aparte (o se entregue a quien la tenga asignada).

2. **`ConfigActivity` incompleta.** El bloque "Código" de esta clase solo cubre la declaración de
   campos y el método `onCreate()`; termina justo donde `onCreate()` cierra, y el siguiente
   marcador ya es `public class DailyTrackingActivity`. Los métodos invocados desde `onCreate()`
   (`actualizarTextoModo()`, `cambiarModo()`, `cerrarSesion()`, `abrirFuentes()`,
   `cargarAlimentos()`, `guardarPeso()`) nunca se definen en el rango asignado. El archivo
   generado NO compilará hasta agregarlos.

3. **`DailyTrackingActivity.manejarGoogleFit()` cortada.** El texto termina justo después de
   `GoogleSignInAccount account = GoogleSignIn.getAccountForExtension(this, fitnessOptions);` sin
   mostrar el resto del método (verificación de permisos de la cuenta, uso de `fitManager`, etc.).
   Se cerraron las llaves para mantener el archivo compilable, con un comentario `NOTA` en el
   propio código. Tampoco se definen `cargarSeguimiento()`, `cargarGraficaPeso()` ni
   `guardarPeso()`, invocados desde `onCreate()`.

4. **Concatenaciones de String con el operador `+` faltante.** Se detectó un patrón recurrente en
   varios bloques de construcción de texto (p. ej. `tvBenefits.setText(...)` en
   `ExerciseDetailActivity`, `macros.setText(...)` en `FoodListActivity`,
   `tvCaloriasResumen.setText(...)` en `DashboardActivity`) donde el operador `+` entre literales
   de cadena y llamadas a método se perdió por completo durante la extracción (no solo un
   espacio, sino el token `+`). Se restauró en todos los casos donde el patrón era inequívoco.

5. **`FoodListActivity`, línea original ~2251:** `card.setOrientation(LinearLayout.VERTICAL;` —
   faltaba el paréntesis de cierre; se corrigió a `card.setOrientation(LinearLayout.VERTICAL);`.

6. **`FoodListActivity`, líneas originales ~2301-2306:** el bloque de concatenación de `macros`
   estaba severamente roto — la propia palabra "Código" (etiqueta del bloque del reporte) quedó
   pegada al texto `" | P: "`, y el último argumento apareció como `alimento.getString)` sin
   nombre de campo. Se completó `alimento.getString("grasas")` por analogía con el patrón
   Cal/Proteínas/Carbohidratos/Grasas usado en el resto del método. Esto es una inferencia, no un
   hecho confirmado por el texto — revisar contra el proyecto original si aparece en otra parte
   del documento.

7. **`ExerciseDetailActivity`, línea original ~1906:** `youTubePlayer.loadVideo(videoIdFinal,}0);}`
   — la `}` antes del `0` es un artefacto claro de extracción; se interpretó como
   `loadVideo(videoIdFinal, 0)` (firma estándar `loadVideo(String videoId, float startSeconds)`).

8. **Layout `activity_dashboard.xml` — barra de navegación inferior incompleta.** El contenedor
   horizontal usa `android:weightSum="4"` pero el texto del reporte solo mostraba explícitamente
   dos `TextView` (`navHome`, `navTraining`). `DashboardActivity.java` sí hace
   `findViewById(R.id.navNutrition)` y `findViewById(R.id.navConfig)`. Se agregaron esos dos
   elementos por patrón (mismo estilo que los dos existentes), marcados con un comentario XML
   explícito. Verificar textos/orden reales contra el proyecto original si se recupera.

9. **Layout `activity_daily_tracking.xml` — faltan `tvMetaPasos` y `progressPasos`.**
   `DailyTrackingActivity.java` referencia estos dos IDs vía `findViewById`, pero ningún bloque de
   código del reporte, dentro del rango asignado, mostró estos elementos en el XML. No se
   agregaron elementos inventados (a diferencia del caso anterior, aquí no había ninguna pista
   estructural de dónde/cómo deberían ir). Esto es un hueco pendiente.

10. **Layouts XML sin etiquetas de cierre en el texto extraído.** Los bloques "Código" de XML casi
    siempre terminaban a mitad de la jerarquía de contenedores (el reporte cortaba el texto en
    cada salto de página/bloque sin mostrar los `</LinearLayout>`/`</ScrollView>` finales). Se
    cerraron todas las etiquetas abiertas según la jerarquía de anidamiento inferida de las
    sangrías y del orden de apertura de tags — no se trata de contenido inventado, sino de cierre
    estructural obligatorio para que el XML sea válido. Afecta a los 7 layouts.

11. **`activity_config.xml` — desajuste entre nombre de archivo y contenido real.** El bloque
    titulado "Interfaz de configuración de perfil físico" (que se guardó como `activity_config.xml`
    siguiendo la instrucción de nombrado de la tarea) tiene IDs (`etEdad`, `etPeso`, `etEstatura`,
    `tvSexo`, `tvActividad`, `tvDias`, `tvObjetivo`, `tvProteinas`, `tvCarbohidratos`, `tvGrasas`,
    `btnRegistrarPerfil`) que **no coinciden** con los que usa `ConfigActivity.java` vía
    `findViewById` (`btnModo`, `btnCerrarSesion`, `btnFuentes`, `btnModificarAlimentos`,
    `editPeso`). El código de `ConfigActivity` sí confirma `setContentView(R.layout.activity_config)`,
    así que el nombre de archivo es correcto según "el código es la fuente de verdad", pero el
    **contenido** del XML capturado bajo ese título probablemente pertenece a otra pantalla (quizás
    la configuración inicial de perfil físico de otra Activity, fuera del alcance de este agente).
    El layout real que consume `ConfigActivity` no apareció como bloque "Interfaz de..."
    independiente dentro del rango asignado. Ver comentario detallado dentro del propio archivo XML.

12. **`activity_actualizacion_dieta.xml` — mismo tipo de desajuste.** El bloque titulado
    "Interfaz del módulo de dieta" (contenido: pantalla "Mi Dieta" con `tvCalorias`, `tvMacros`,
    `llComidas`) se guardó con este nombre por instrucción explícita de la tarea, pero
    `ActualizacionDietaActivity.java` en realidad llama a
    `setContentView(R.layout.activity_actualizacion_dieta_manager)` y usa IDs completamente
    distintos (`tvCaloriasActuales`, `tvCaloriasNuevas`, `tvProteinas`, `tvCarbohidratos`,
    `tvGrasas`, `tvNotificacion`, `btnAceptar`, `btnRechazar`). El contenido de este XML pertenece
    casi con certeza a `DietActivity` (ver punto 1), no a `ActualizacionDietaActivity`. El layout
    real `activity_actualizacion_dieta_manager.xml` no apareció en el rango asignado. Ver
    comentario detallado dentro del propio archivo XML.

## Resumen de clases fuera de alcance mencionadas/parcialmente visibles

- `DietActivity` — código real disperso dentro del rango "de Dashboard" (ver punto 1); no
  reconstruida aquí.
- `GoogleFitManager` — fragmento parcial visible justo después del cierre de `FoodListActivity`
  (líneas ~2588-2634 del reporte); no reconstruida aquí.
- `SessionManager`, `TrainingListActivity` — ya localizadas por sus propios marcadores
  `public class` más adelante en el documento (líneas 3242 y 3567 respectivamente); fuera del
  rango/alcance de este agente.
- `ExerciseInProgressActivity`, `LoginActivity` — referenciadas por Intent pero no definidas en
  este rango.

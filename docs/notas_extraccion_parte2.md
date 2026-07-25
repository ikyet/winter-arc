# Notas de extracción — Parte 2 (LoginActivity, RegisterActivity, SessionManager, StepCounterManager, TrainingListActivity)

Fuente única: `C:\Claude Zona De Trabajo\WinterArc\docs\reporte_texto_extraido.txt` (aprox. líneas 2644–4549, ubicando cada clase por "public class NombreClase").

## (a) Archivos creados

Todos en `C:\Claude Zona De Trabajo\WinterArc\android\app\src\main\java\com\winterarc\app\`:

- `LoginActivity.java` — reconstruida completa (líneas ~2644–2830 del reporte).
- `RegisterActivity.java` — reconstruida completa (líneas ~2839–3227 del reporte).
- `SessionManager.java` — reconstruida completa (líneas ~3242–3458 del reporte).
- `StepCounterManager.java` — reconstruida completa (líneas ~3468–3556 del reporte).
- `TrainingListActivity.java` — **incompleta**, ver punto (e). El texto fuente se corta a mitad del método `crearCardEjercicio()` (línea ~3741) y no vuelve a aparecer contenido de esta clase antes de la sección "ARCHIVOS SWIFT APP IOS" (línea 4549).

No se creó ningún archivo de modelo (POJO) adicional — ver punto (c).

## (b) Imports inferidos (con su nivel de certeza)

Alta certeza (uso explícito y directo en el código):
- `LoginActivity`: `android.os.Bundle`, `android.content.Intent`, `android.view.View`, `android.widget.{Button,EditText,TextView}`, `androidx.appcompat.app.AppCompatActivity`, `com.android.volley.Request`, `com.android.volley.toolbox.{StringRequest,Volley}`, `org.json.JSONObject`, `java.util.{HashMap,Map}`.
- `RegisterActivity`: los mismos anteriores más `android.widget.CheckBox`, `android.os.Handler`, `android.util.Patterns` (por `Patterns.EMAIL_ADDRESS`).
- `SessionManager`: `android.content.Context`, `android.content.SharedPreferences`.
- `StepCounterManager`: `android.content.Context`, `android.hardware.{Sensor,SensorEvent,SensorEventListener,SensorManager}`.
- `TrainingListActivity`: `android.os.Bundle`, `android.widget.LinearLayout`, `android.widget.TextView`, `androidx.appcompat.app.AppCompatActivity`, `com.android.volley.Request`, `com.android.volley.toolbox.{StringRequest,Volley}`, `org.json.{JSONArray,JSONObject}`, `java.util.{HashMap,Map}`.

Menor certeza (no se ve el código completo, se infiere por convención Android estándar):
- `R` (clase de recursos autogenerada `com.winterarc.app.R`) — no se importa explícitamente porque está en el mismo paquete; se asume que existe `activity_login`, `activity_register`, `activity_training_list` y los ids referenciados (`etUsuario`, `etPassword`, `btnLogin`, etc.) en `res/layout` y `res/values/ids` — estos XML no estaban en el rango de esta tarea y no se crearon.
- En `TrainingListActivity`, `android.view.View` **no** se importó porque no aparece usado en el fragmento capturado (a diferencia de Login/Register). Si el método `crearCardEjercicio()` faltante usa `View`, `Button`, etc. (muy probable, dado que arma tarjetas dinámicas), habrá que agregarlo al completar la clase manualmente.
- `TrainingListActivity` no importa `android.widget.Button`/`TextView` extra ni `View.OnClickListener` porque el fragmento visible no los usa explícitamente más allá de lo ya declarado.

## (c) Modelos de datos inferidos

**Ninguno fue necesario.** Las cinco clases de este rango consumen las respuestas del backend directamente vía `JSONObject`/`JSONArray` (con `.getString()`, `.getInt()`, `.getBoolean()`, etc.) en lugar de deserializar a una clase POJO propia. No hay ninguna referencia a una clase modelo (p. ej. `Ejercicio`, `Usuario`, `Rutina`) usada como tipo de variable en el código visible, así que no se generó ningún archivo adicional marcado como "inferido".

## (d) Parámetros de endpoints PHP tal como los arma el código Java

Todos bajo el host `https://dodgerblue-emu-880788.hostingersite.com/winter_arc_api/`:

- **`login.php`** (usado en `LoginActivity.validarLogin()` y en `RegisterActivity.loginAutomatico()`), método POST:
  - `usuario` (String)
  - `password` (String)
  - Respuesta esperada (leída por el cliente): `success` (bool), `id_usuario` (int), `nombre_usuario` (String), `has_profile` (bool), `message` (String, en caso de error).

- **`register.php`** (usado en `RegisterActivity.registrarUsuario()`), método POST:
  - `nombre` (String)
  - `correo` (String)
  - `password` (String)
  - Respuesta esperada: `success` (bool), `message` (String, en caso de error). Si `success` es true, el cliente llama internamente a `login.php` (login automático) — no se leen `id_usuario`/`nombre_usuario` de la respuesta de `register.php` directamente.

- **`obtener_rutina.php`** (usado en `TrainingListActivity.obtenerDiasRutina()`), método POST:
  - `id_usuario` (String, viene de `sessionManager.getUserId()`)
  - Respuesta esperada: `success` (bool), `dias` (array de objetos con al menos `id_rutina` (int) y `dia_entrenamiento` (String)).

- **`obtener_ejercicios_rutina.php`** (usado en `TrainingListActivity.cargarEjercicios()`), método POST:
  - `id_rutina` (String)
  - Respuesta esperada: `success` (bool), `ejercicios` (array de objetos JSON — su estructura interna no se pudo determinar porque `crearCardEjercicio()` está incompleto en la fuente, ver punto (e)).

Nota: aunque no son parte de las 5 clases de esta tarea, en el material leído (sección de `UserProfileActivity`, fuera de alcance) aparecen también `guardar_perfil.php`, `generar_dieta.php`, `generar_rutina.php`, `listar_alimentos.php` y `guardar_alimentos_no_deseados.php` sobre el mismo host — quedan anotados aquí por si son útiles para reconstruir el backend completo, aunque su reconstrucción en código no formó parte de este encargo.

## (e) Fragmentos incompletos o ambiguos

1. **`TrainingListActivity` — clase incompleta.** El texto del reporte, siguiendo el orden secuencial de bloques "Código", se corta justo después de:
   ```java
   private void crearCardEjercicio(JSONObject ejercicio) {
       LinearLayout card = new LinearLayout(this);
       card.setOrientation(LinearLayout.VERTICAL);
   ```
   El siguiente bloque "Código" del documento (línea ~3748) resulta pertenecer a otra clase completamente distinta (ver punto siguiente), y el contenido de `TrainingListActivity` nunca vuelve a aparecer antes de que el documento entre en la sección "ARCHIVOS SWIFT APP IOS" (línea 4549). Por lo tanto **faltan**: el resto del cuerpo de `crearCardEjercicio()` (construcción de los TextView/botones de la tarjeta de ejercicio, `containerEjercicios.addView(card)`, etc.), el método `mostrarSelectorDias()` (referenciado desde `onCreate` vía `cardSelectorDia.setOnClickListener(v -> mostrarSelectorDias())` pero nunca definido en el texto), y la llave de cierre final de la clase. Se agregó un comentario explícito en el archivo `.java` marcando el punto de corte, y se cerraron las llaves solo para dejar el archivo sintácticamente válido (no se inventó lógica).

2. **Contaminación cruzada con `UserProfileActivity` dentro del rango asignado.** A partir de la línea ~3748 del reporte y hasta la línea 4538, el texto contiene código que en realidad pertenece a `UserProfileActivity` (variables `sexoSeleccionado`, `tvSexo`, `actividadSeleccionada`, `tvActividad`, `diasSeleccionados`, `tvDias`, `objetivoSeleccionado`, `tvObjetivo`, `seleccionarAlimentosNoDeseados()`, `validarPerfil()`, `guardarPerfilServidor()`, `generarDieta()`, `generarRutina()`, `irAlDashboard()`, etc., y explícitamente el bloque "Descripción" de la línea 4536-4538 dice literalmente "La parte final de UserProfileActivity... concluye la implementación completa de UserProfileActivity"). **No existe en ningún punto del documento la línea `public class UserProfileActivity ...`** (se verificó con búsqueda exhaustiva) — el encabezado de esa clase se perdió por completo en la extracción, a diferencia del resto de las clases que sí conservan su firma `public class X`. Como `UserProfileActivity` no está en el alcance de esta tarea, **no se creó ningún archivo para ella**, pero se deja esta nota para quien continúe la reconstrucción: ese bloque de texto (líneas ~3748–4538) es la fuente para reconstruir `UserProfileActivity.java` en una tarea futura, y contiene además las llamadas a `guardar_perfil.php`, `generar_dieta.php`, `generar_rutina.php`, `listar_alimentos.php` y `guardar_alimentos_no_deseados.php` mencionadas arriba.

3. **`RegisterActivity` — llave de cierre de clase probablemente perdida.** El bloque final capturado (líneas 3221-3224) es:
   ```
   cbTerminos.setChecked(true);
   }
   }
   ```
   Solo dos `}` (cierre del `if` y cierre de `onActivityResult`), pero el texto explicativo inmediatamente después (línea 3227) afirma que "con este bloque concluye completamente la implementación de RegisterActivity". Se interpretó que falta una tercera `}` de cierre de clase (una llave suelta es más fácil de perder en la extracción que un token con letras) y **se agregó** esa llave final en `RegisterActivity.java`. Vale la pena verificarlo contra el proyecto real si se recupera.

4. **`TrainingListActivity.cargarEjercicios()` — llave `{` faltante tras `catch`.** El texto original (línea 3702-3703) dice:
   ```
   }catch(Exception e)
   e.printStackTrace();}},
   ```
   Sin la llave de apertura `{` después de `catch(Exception e)`, lo cual no compila. Se agregó la `{` faltante en el archivo reconstruido, siguiendo el mismo patrón usado consistentemente en el resto del código (`catch(Exception e){`).

5. **`StepCounterManager.onSensorChanged()` — operador `-` probablemente perdido.** El texto original (líneas 3525-3527) es:
   ```
   pasosActuales =
   (int)event.values[0]
   pasosIniciales;
   ```
   Dos expresiones consecutivas sin operador no compilan. Dado que la variable se llama `pasosActuales` y el propósito documentado de la clase es calcular pasos como diferencia entre la lectura actual y `pasosIniciales`, se completó como `(int) event.values[0] - pasosIniciales;`. Esto es una inferencia razonable por contexto semántico, no una mera corrección de espacio — queda documentado aquí para que se verifique contra el proyecto original si aparece.

6. **`RegisterActivity.validarRegistro()` — regex de contraseña con caracteres probablemente perdidos.** El texto original trae:
   ```java
   if (!password.matches("^(?=.[A-Z])(?=.\d).{8,}$")) {
   ```
   Esto **no** es una expresión regular Java válida como cadena literal: `\d` dentro de un `String` Java requiere escaparse como `\\d` (si no, es un error de compilación por secuencia de escape ilegal), y los cuantificadores `.` sin `*` (debería ser casi con certeza `.*[A-Z]` y `.*\\d`, el patrón estándar para "al menos una mayúscula" y "al menos un dígito"). **Se dejó el texto exactamente como aparece en el reporte**, sin corregirlo, porque esto excede una simple pérdida de espacio (son caracteres `*` y `\` perdidos, no espacios) y la instrucción fue no inventar lógica. Se documenta aquí como pendiente de verificación/corrección manual; el patrón probable original sería `"^(?=.*[A-Z])(?=.*\\d).{8,}$"`.

7. **Orden inusual de miembros en `SessionManager`.** Los bloques "Código" del reporte presentan métodos que usan `prefs`/`editor` (p. ej. `guardarComidasRealizadas()`) antes de que esos campos y el constructor aparezcan en el texto. Se preservó el orden exacto de aparición en el documento (tal como piden las instrucciones de concatenar secuencialmente) — esto **no** es un error de compilación, ya que en Java el orden de declaración de miembros dentro de una clase no afecta la compilación, así que el archivo es válido tal cual.

## Resumen de decisiones de formato

- Se restauró indentación estándar de 4 espacios porque el texto extraído no conservaba ningún nivel de sangría (probablemente otra víctima de la pérdida de "runs" de Word); esto no altera lógica ni orden de sentencias, solo la presentación visual.
- Se corrigieron únicamente uniones de tokens obviamente rotas por la extracción (p. ej. `LoginActivityextends` → `LoginActivity extends`, `TextViewtvError` → `TextView tvError`, `=new` → `= new`, `@Overrideprotected` → `@Override` + `protected`, `voidonAccuracyChanged` → `void onAccuracyChanged`, `requestCode== TERMS_REQUEST` → `requestCode == TERMS_REQUEST`, etc.). No se modificó ninguna cadena literal, nombre de variable, valor, ni se reordenó lógica, salvo las excepciones documentadas explícitamente en el punto (e).

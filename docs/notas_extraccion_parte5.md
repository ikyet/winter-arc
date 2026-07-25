# Notas — Parte 5 (cierre de TODOs pendientes)

Completado en esta sesión, sin agentes (trabajo directo con el código ya extraído + fuente del
reporte para verificar contratos de endpoints):

## RegisterActivity.java
Regex de contraseña corregida: `"^(?=.[A-Z])(?=.\d).{8,}$"` (inválida como literal Java, `\d` sin
escapar) → `"^(?=.*[A-Z])(?=.*\\d).{8,}$"`. El mensaje de error asociado ("mínimo 8 caracteres, una
mayúscula y un número") confirma que esta era la intención original.

## TrainingListActivity.java
- `crearCardEjercicio()`: reconstruido completo. El reporte se corta a mitad del método, pero el
  equivalente Swift/iOS de la misma pantalla ("RoutineView", ~líneas 8240-8345 del reporte)
  confirma los campos JSON (`id_rutina_ejercicio`, `grupo_muscular`, `nombre_ejercicio`, `series`,
  `repeticiones`) y los endpoints `guardar_progreso_ejercicio.php` (~línea 5138),
  `completar_rutina.php`, `obtener_variantes.php` y `reemplazar_ejercicio.php`. Portados todos con
  el mismo patrón Volley del resto del proyecto.
- `mostrarSelectorDias()`: implementado con `AlertDialog` (no hay referencia Swift para este
  método específico — en iOS no existe un selector de día separado — así que es una
  implementación estándar Android para cumplir el mismo propósito).
- Se agregó botón "Finalizar rutina" (`btnFinalizarRutina`) a `activity_training_list.xml`,
  conectado a `completarRutina()`.
- El "BottomSheet de variantes" que describe el mockup de la sección 3.3 se implementó como
  `AlertDialog.setItems` (mismo patrón usado en el resto de la app) en vez de introducir
  `BottomSheetDialog` como dependencia nueva.

## ExerciseInProgressActivity.java
Esta clase no tiene NINGÚN rastro en el reporte (ni Java, ni Swift, ni mockup) — solo se sabe que
existe por los extras que le pasa `ExerciseListActivity` (`nombre`, `series`, `descanso`). No hay
nada que reconstruir o portar. Se implementó una pantalla funcional nueva (temporizador de
descanso entre series + contador de series completadas), dejada explícitamente marcada en el
archivo como reconstrucción NUEVA, no recuperación. Si se recupera el proyecto original, esta
pantalla es la que con más probabilidad difiere de la real.

## Pendiente real (no resuelto, requiere el proyecto original o más contexto del usuario)
- `GoogleFitManager.obtenerPasos()`: el rango exacto de fechas de la consulta a Google Fit
  (`DataReadRequest`) es boilerplate estándar, no confirmado contra el reporte (ver
  `notas_extraccion_parte3.md`).
- Ícono/asset real de la app (`ic_launcher.xml` es un placeholder geométrico).
- Gradle wrapper (`gradle-wrapper.jar`) — se genera al abrir el proyecto en Android Studio.

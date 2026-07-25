# Winter Arc

App de nutrición y entrenamiento (Android + iOS). Proyecto final de carrera — documentación completa en `docs/reporte_texto_extraido.txt`.

## Estado del proyecto

Este proyecto se completó originalmente como app funcional (Android + iOS + backend), pero **se perdió el código fuente del proyecto Android Studio** al perder la computadora donde se desarrolló. El proyecto iOS (Xcode/Swift) sigue intacto en otra máquina. El backend (PHP + MySQL en Hostinger) se puede recuperar renovando el hosting.

**Este repositorio contiene la reconstrucción del proyecto Android**, extraída del reporte final de tesis (`docs/reporte_texto_extraido.txt`), el cual incluía el código fuente completo (clases Java + layouts XML) como parte de su documentación técnica.

### Limitaciones conocidas de la reconstrucción

- El texto del código se extrajo de un `.docx` donde el resaltado de sintaxis fragmentaba el código en múltiples "runs" de Word; al extraer el texto se perdieron algunos espacios entre tokens (ej. `varusuario` → `var usuario`). Se repararon manualmente/con asistencia de IA, pero puede haber errores puntuales — **revisar y compilar antes de confiar 100% en el código**.
- El reporte no incluía `AndroidManifest.xml`, `build.gradle`, ni `colors.xml`/`strings.xml` — se reconstruyeron a partir de lo que el código y las descripciones de diseño (sección 3.3 del reporte) indican.
- El backend PHP (`winter_arc_api`) no estaba documentado como código fuente en el reporte, solo se conocen los endpoints que la app consume (ver `docs/`). Habrá que reconstruirlo o recuperarlo del hosting.

## Stack tecnológico

| Componente | Tecnología |
|---|---|
| App Android | Java (Android Studio) |
| App iOS | Swift / SwiftUI (Xcode) |
| Backend | PHP + MySQL, alojado en Hostinger |
| Actividad física | Google Fit API (Android), Apple HealthKit (iOS) |

## Estructura del repositorio

```
WinterArc/
  android/          proyecto Android Studio reconstruido
  docs/             reporte original y notas de la reconstrucción
```

package com.winterarc.app;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

// ============================================================================================
// NOTA DE RECONSTRUCCIÓN IMPORTANTE (ver docs/notas_extraccion_parte3.md para el detalle
// completo): esta clase NUNCA aparece con su encabezado "public class GoogleFitManager" en el
// reporte. Solo es visible un fragmento final (líneas ~2588-2634 del reporte), justo después del
// cierre explícito de FoodListActivity y antes del marcador "public class LoginActivity". Ese
// fragmento comienza a mitad de un callback addOnSuccessListener(response -> { ... }), con las
// variables "response" y "total" ya en uso -- es decir, falta la construcción real de la consulta
// (rango de fechas, DataReadRequest, FitnessOptions, Fitness.getHistoryClient(...).readData(...))
// que antecede a ese punto, y que el reporte no muestra en ninguna parte del documento.
//
// Todo el contenido DESDE "for (DataSet dataSet : response.getDataSets())" HASTA el cierre de la
// interfaz interna PasosCallback es transcripción literal del reporte (solo se reparó espaciado y
// el operador "+" perdido en las concatenaciones de Log.e(...)). La construcción previa (rango de
// fechas de "hoy", DataReadRequest agregando DataType.TYPE_STEP_COUNT_DELTA por bucket diario, y
// la llamada a Fitness.getHistoryClient(...).readData(...)) NO aparece en el documento y se agregó
// aquí como plantilla mínima estándar (boilerplate idiomático de la API de Google Fit para "pasos
// de hoy"), reutilizando DataType.TYPE_STEP_COUNT_DELTA y el patrón FitnessOptions que
// DailyTrackingActivity.java ya usa de forma confirmada. No es lógica de negocio propia de Winter
// Arc (no decide metas, no calcula nada específico de la app); aun así, DEBE verificarse contra el
// proyecto original si se recupera -- especialmente el rango de fechas exacto y si se usa
// bucketByTime o aggregate.
//
// La firma pública "obtenerPasos(Context, PasosCallback)" y el constructor sin argumentos son una
// inferencia razonable a partir del único uso confirmado de esta clase en el proyecto:
// "fitManager = new GoogleFitManager();" en DailyTrackingActivity.java (línea 64), que confirma
// un constructor sin parámetros. El nombre del método "obtenerPasos" NO aparece literalmente en
// el reporte; se eligió por ser el nombre más directo dado el propósito de la clase y el callback
// "PasosCallback.onResult(int pasos)" que sí está confirmado.
// ============================================================================================

public class GoogleFitManager {

    public void obtenerPasos(Context context, PasosCallback callback) {
        // NOTA: desde aquí hasta la llamada a .readData(...) es plantilla reconstruida (boilerplate
        // estándar), NO texto extraído del reporte. Ver NOTA de cabecera del archivo.
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        long startTime = cal.getTimeInMillis();
        long endTime = System.currentTimeMillis();

        FitnessOptions fitnessOptions = FitnessOptions.builder()
                .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .build();

        DataReadRequest request = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();

        Fitness.getHistoryClient(context, GoogleSignIn.getAccountForExtension(context, fitnessOptions))
                .readData(request)
                .addOnSuccessListener(response -> {
                    // ---- A partir de aquí: transcripción literal del reporte (líneas ~2588-2609),
                    // solo con espaciado y el operador "+" perdido en las concatenaciones reparados. ----
                    int total = 0;
                    for (DataSet dataSet : response.getDataSets()) {
                        Log.e("FIT_DEBUG", "📊 Dataset DIRECTO: " + dataSet.getDataType().getName());
                        for (DataPoint dp : dataSet.getDataPoints()) {
                            int pasos =
                                    dp.getValue(Field.FIELD_STEPS).asInt();
                            total += pasos;
                        }
                    }
                    Log.e("FIT_DEBUG", "TOTAL FINAL: " + total);
                    callback.onResult(total);
                })
                .addOnFailureListener(e -> {
                    Log.e("FIT_DEBUG", " ERROR", e);
                    callback.onResult(0);
                });
    }

    // Transcripción literal del reporte (líneas ~2628-2630).
    public interface PasosCallback {
        void onResult(int pasos);
    }
}

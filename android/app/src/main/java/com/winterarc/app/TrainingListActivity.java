package com.winterarc.app;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class TrainingListActivity extends AppCompatActivity {
    LinearLayout containerEjercicios, cardSelectorDia;
    TextView tvDiaActual;
    Button btnFinalizarRutina;
    SessionManager sessionManager;
    JSONArray diasRutina;
    int idRutinaActual = 0;
    String URL_DIAS = "https://dodgerblue-emu-880788.hostingersite.com/winter_arc_api/obtener_rutina.php";
    String URL_EJERCICIOS = "https://dodgerblue-emu-880788.hostingersite.com/winter_arc_api/obtener_ejercicios_rutina.php";

    // Los siguientes 4 endpoints no aparecen en el rango Java del reporte asignado a esta clase
    // (el texto se corta a mitad de crearCardEjercicio()), pero sí están confirmados en el
    // equivalente Swift/iOS de esta misma pantalla ("RoutineView", ~líneas 8240-8345 del reporte:
    // completar_rutina.php, obtener_variantes.php, reemplazar_ejercicio.php) y en el detalle de
    // ejercicio Swift (~línea 5138: guardar_progreso_ejercicio.php). Ver docs/notas_extraccion_parte5.md.
    String URL_PROGRESO = "https://dodgerblue-emu-880788.hostingersite.com/winter_arc_api/guardar_progreso_ejercicio.php";
    String URL_COMPLETAR_RUTINA = "https://dodgerblue-emu-880788.hostingersite.com/winter_arc_api/completar_rutina.php";
    String URL_VARIANTES = "https://dodgerblue-emu-880788.hostingersite.com/winter_arc_api/obtener_variantes.php";
    String URL_REEMPLAZAR = "https://dodgerblue-emu-880788.hostingersite.com/winter_arc_api/reemplazar_ejercicio.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training_list);
        containerEjercicios = findViewById(R.id.containerEjercicios);
        cardSelectorDia = findViewById(R.id.cardSelectorDia);
        tvDiaActual = findViewById(R.id.tvDiaActual);
        btnFinalizarRutina = findViewById(R.id.btnFinalizarRutina);
        sessionManager = new SessionManager(this);
        cardSelectorDia.setOnClickListener(v -> mostrarSelectorDias());
        btnFinalizarRutina.setOnClickListener(v -> completarRutina());
        if (sessionManager.isRutinaRealizada()) {
            btnFinalizarRutina.setText("Rutina completada");
            btnFinalizarRutina.setEnabled(false);
        }
        obtenerDiasRutina();
    }

    /* OBTENER DÍAS */
    private void obtenerDiasRutina() {
        StringRequest request = new StringRequest(
                Request.Method.POST,
                URL_DIAS,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.getBoolean("success")) {
                            diasRutina = json.getJSONArray("dias");
                            if (diasRutina.length() > 0) {
                                JSONObject primerDia = diasRutina.getJSONObject(0);
                                idRutinaActual = primerDia.getInt("id_rutina");
                                String nombreDia = primerDia.getString("dia_entrenamiento");
                                tvDiaActual.setText(nombreDia);
                                cargarEjercicios(idRutinaActual);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> error.printStackTrace()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id_usuario", String.valueOf(sessionManager.getUserId()));
                return params;
            }
        };
        Volley.newRequestQueue(this).add(request);
    }

    private void cargarEjercicios(int idRutina) {
        containerEjercicios.removeAllViews();
        StringRequest request = new StringRequest(
                Request.Method.POST,
                URL_EJERCICIOS,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.getBoolean("success")) {
                            JSONArray ejercicios =
                                    json.getJSONArray("ejercicios");
                            for (int i = 0; i < ejercicios.length(); i++) {
                                JSONObject ejercicio =
                                        ejercicios.getJSONObject(i);
                                crearCardEjercicio(ejercicio);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error ->
                        error.printStackTrace()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id_rutina", String.valueOf(idRutina));
                return params;
            }
        };
        Volley.newRequestQueue(this).add(request);
    }

    // CARD EJERCICIO
    // NOTA DE RECONSTRUCCIÓN: el reporte se corta exactamente después de
    // "card.setOrientation(LinearLayout.VERTICAL);" (línea ~3738), así que el resto del cuerpo
    // original no aparece en el documento. Reconstruido a partir de dos fuentes confirmadas: (1)
    // la descripción de diseño de la sección 3.3 ("ENTRENAMIENTO": nombre 18sp blanco, series/reps
    // 16sp gris, dos EditText de peso/repeticiones realizadas, botón para guardar, ícono de
    // "cambiar ejercicio"), y (2) el equivalente Swift/iOS de esta misma pantalla ("RoutineView",
    // ~líneas 8240-8345), que confirma los campos JSON reales (id_rutina_ejercicio,
    // grupo_muscular, nombre_ejercicio, series, repeticiones) y los endpoints
    // guardar_progreso_ejercicio.php / obtener_variantes.php / reemplazar_ejercicio.php. El
    // "ícono de cambio" que el mockup describe como un BottomSheet se implementó aquí como un
    // AlertDialog.setItems (mismo patrón de diálogo ya usado en el resto de la app reconstruida,
    // p. ej. ConfigActivity.cargarAlimentos()), en vez de introducir BottomSheetDialog como
    // dependencia nueva no usada en ningún otro punto del proyecto. Ver docs/notas_extraccion_parte5.md.
    private void crearCardEjercicio(JSONObject ejercicio) {
        int idRutinaEjercicio = ejercicio.optInt("id_rutina_ejercicio", 0);
        String grupoMuscular = ejercicio.optString("grupo_muscular", "");
        String nombreEjercicio = ejercicio.optString("nombre_ejercicio", "Ejercicio");
        int series = ejercicio.optInt("series", 0);
        int repeticiones = ejercicio.optInt("repeticiones", 0);

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundColor(ContextCompat.getColor(this, R.color.card));
        card.setPadding(24, 24, 24, 24);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, 16);
        card.setLayoutParams(cardParams);

        TextView tvNombre = new TextView(this);
        tvNombre.setText(nombreEjercicio);
        tvNombre.setTextSize(18);
        tvNombre.setTextColor(ContextCompat.getColor(this, R.color.text));
        card.addView(tvNombre);

        TextView tvSeriesReps = new TextView(this);
        tvSeriesReps.setText(series + " series x " + repeticiones + " reps");
        tvSeriesReps.setTextSize(16);
        tvSeriesReps.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        tvSeriesReps.setPadding(0, 4, 0, 12);
        card.addView(tvSeriesReps);

        LinearLayout filaCampos = new LinearLayout(this);
        filaCampos.setOrientation(LinearLayout.HORIZONTAL);

        EditText etPeso = new EditText(this);
        etPeso.setHint("Peso (kg)");
        etPeso.setInputType(android.text.InputType.TYPE_CLASS_NUMBER
                | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        etPeso.setTextColor(ContextCompat.getColor(this, R.color.text));
        LinearLayout.LayoutParams etParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        etParams.setMarginEnd(8);
        etPeso.setLayoutParams(etParams);
        filaCampos.addView(etPeso);

        EditText etRepsRealizadas = new EditText(this);
        etRepsRealizadas.setHint("Reps realizadas");
        etRepsRealizadas.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        etRepsRealizadas.setTextColor(ContextCompat.getColor(this, R.color.text));
        LinearLayout.LayoutParams etParams2 = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        etRepsRealizadas.setLayoutParams(etParams2);
        filaCampos.addView(etRepsRealizadas);

        card.addView(filaCampos);

        Button btnGuardarProgreso = new Button(this);
        btnGuardarProgreso.setText("Marcar ejercicio como completado");
        btnGuardarProgreso.setAllCaps(false);
        btnGuardarProgreso.setBackgroundTintList(
                ContextCompat.getColorStateList(this, R.color.accent));
        btnGuardarProgreso.setTextColor(ContextCompat.getColor(this, R.color.text));
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        btnParams.setMargins(0, 12, 0, 0);
        btnGuardarProgreso.setLayoutParams(btnParams);
        btnGuardarProgreso.setOnClickListener(v -> guardarProgresoEjercicio(
                idRutinaEjercicio,
                etPeso.getText().toString().trim(),
                etRepsRealizadas.getText().toString().trim()
        ));
        card.addView(btnGuardarProgreso);

        Button btnCambiar = new Button(this);
        btnCambiar.setText("Cambiar ejercicio");
        btnCambiar.setAllCaps(false);
        btnCambiar.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        btnCambiar.setBackgroundTintList(
                ContextCompat.getColorStateList(this, R.color.button));
        LinearLayout.LayoutParams btnCambiarParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        btnCambiarParams.setMargins(0, 8, 0, 0);
        btnCambiar.setLayoutParams(btnCambiarParams);
        btnCambiar.setOnClickListener(v -> mostrarVariantesEjercicio(idRutinaEjercicio, grupoMuscular));
        card.addView(btnCambiar);

        containerEjercicios.addView(card);
    }

    // Portado desde la lógica equivalente en Swift (iOS) — func guardarProgresoServidor()
    // (~línea 5138 del reporte). Parámetros confirmados: id_usuario, id_rutina_ejercicio,
    // peso_real, unidad, repeticiones_realizadas. El método original en Java no aparece en el
    // reporte, verificar contra el proyecto real.
    private void guardarProgresoEjercicio(int idRutinaEjercicio, String peso, String repsRealizadas) {
        if (peso.isEmpty() || repsRealizadas.isEmpty()) {
            Toast.makeText(this, "Completa peso y repeticiones", Toast.LENGTH_SHORT).show();
            return;
        }
        StringRequest request = new StringRequest(
                Request.Method.POST,
                URL_PROGRESO,
                response -> Toast.makeText(this, "Progreso guardado", Toast.LENGTH_SHORT).show(),
                error -> error.printStackTrace()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id_usuario", String.valueOf(sessionManager.getUserId()));
                params.put("id_rutina_ejercicio", String.valueOf(idRutinaEjercicio));
                params.put("peso_real", peso);
                params.put("unidad", "kg");
                params.put("repeticiones_realizadas", repsRealizadas);
                return params;
            }
        };
        Volley.newRequestQueue(this).add(request);
    }

    // Portado desde la lógica equivalente en Swift (iOS) — func completarRutina() (~línea 8283
    // del reporte: marca rutinaCompletada localmente vía SessionManager y notifica al servidor
    // con completar_rutina.php). El método original en Java no aparece en el reporte, verificar
    // contra el proyecto real.
    private void completarRutina() {
        sessionManager.guardarRutinaRealizada(true);
        btnFinalizarRutina.setText("Rutina completada");
        btnFinalizarRutina.setEnabled(false);
        StringRequest request = new StringRequest(
                Request.Method.POST,
                URL_COMPLETAR_RUTINA,
                response -> { /* la versión Swift tampoco procesa la respuesta */ },
                error -> error.printStackTrace()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id_usuario", String.valueOf(sessionManager.getUserId()));
                return params;
            }
        };
        Volley.newRequestQueue(this).add(request);
    }

    // Portado desde la lógica equivalente en Swift (iOS) — func mostrarVariantesEjercicio(_:_:)
    // (~línea 8304 del reporte). Muestra alternativas del mismo grupo muscular; al elegir una,
    // llama a reemplazarEjercicio(). El método original en Java no aparece en el reporte,
    // verificar contra el proyecto real.
    private void mostrarVariantesEjercicio(int idRutinaEjercicio, String grupoMuscular) {
        StringRequest request = new StringRequest(
                Request.Method.POST,
                URL_VARIANTES,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.getBoolean("success")) {
                            JSONArray lista = json.getJSONArray("ejercicios");
                            String[] nombres = new String[lista.length()];
                            int[] ids = new int[lista.length()];
                            for (int i = 0; i < lista.length(); i++) {
                                JSONObject variante = lista.getJSONObject(i);
                                nombres[i] = variante.optString("nombre_ejercicio", "");
                                ids[i] = variante.optInt("id_ejercicio", 0);
                            }
                            new AlertDialog.Builder(this)
                                    .setTitle("Variantes disponibles")
                                    .setItems(nombres, (dialog, which) ->
                                            reemplazarEjercicio(idRutinaEjercicio, ids[which]))
                                    .setNegativeButton("Cancelar", null)
                                    .show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> error.printStackTrace()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("grupo_muscular", grupoMuscular);
                return params;
            }
        };
        Volley.newRequestQueue(this).add(request);
    }

    // Portado desde la lógica equivalente en Swift (iOS) — func reemplazarEjercicio(_:_:)
    // (~línea 8337 del reporte). Tras reemplazar, recarga la lista de ejercicios del día actual,
    // igual que el Swift original vuelve a llamar a cargarEjercicios(). El método original en
    // Java no aparece en el reporte, verificar contra el proyecto real.
    private void reemplazarEjercicio(int idRutinaEjercicio, int idNuevoEjercicio) {
        StringRequest request = new StringRequest(
                Request.Method.POST,
                URL_REEMPLAZAR,
                response -> cargarEjercicios(idRutinaActual),
                error -> error.printStackTrace()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id_rutina_ejercicio", String.valueOf(idRutinaEjercicio));
                params.put("id_nuevo_ejercicio", String.valueOf(idNuevoEjercicio));
                return params;
            }
        };
        Volley.newRequestQueue(this).add(request);
    }

    // NOTA DE RECONSTRUCCIÓN: mostrarSelectorDias() se invoca desde onCreate() pero nunca se
    // define en el rango de texto capturado por el reporte para esta clase. "diasRutina" ya
    // contiene, tras obtenerDiasRutina(), la lista de días disponibles con "id_rutina" y
    // "dia_entrenamiento" por elemento; se muestra un selector con esos días (mismo patrón
    // AlertDialog usado en el resto de la app) y, al elegir uno, se actualiza
    // idRutinaActual/tvDiaActual y se vuelve a llamar a cargarEjercicios(idRutina). No hay
    // referencia Swift para este método porque en iOS la selección de día no se hizo mediante un
    // diálogo separado; es una implementación estándar Android para cumplir el mismo propósito.
    private void mostrarSelectorDias() {
        if (diasRutina == null || diasRutina.length() == 0) return;
        try {
            String[] nombres = new String[diasRutina.length()];
            for (int i = 0; i < diasRutina.length(); i++) {
                nombres[i] = diasRutina.getJSONObject(i).getString("dia_entrenamiento");
            }
            new AlertDialog.Builder(this)
                    .setTitle("Selecciona un día")
                    .setItems(nombres, (dialog, which) -> {
                        try {
                            JSONObject dia = diasRutina.getJSONObject(which);
                            idRutinaActual = dia.getInt("id_rutina");
                            tvDiaActual.setText(dia.getString("dia_entrenamiento"));
                            cargarEjercicios(idRutinaActual);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    })
                    .show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

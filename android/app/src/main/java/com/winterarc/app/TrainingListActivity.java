package com.winterarc.app;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

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
    SessionManager sessionManager;
    JSONArray diasRutina;
    int idRutinaActual = 0;
    String URL_DIAS = "https://dodgerblue-emu-880788.hostingersite.com/winter_arc_api/obtener_rutina.php";
    String URL_EJERCICIOS = "https://dodgerblue-emu-880788.hostingersite.com/winter_arc_api/obtener_ejercicios_rutina.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training_list);
        containerEjercicios = findViewById(R.id.containerEjercicios);
        cardSelectorDia = findViewById(R.id.cardSelectorDia);
        tvDiaActual = findViewById(R.id.tvDiaActual);
        sessionManager = new SessionManager(this);
        cardSelectorDia.setOnClickListener(v -> mostrarSelectorDias());
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
    private void crearCardEjercicio(JSONObject ejercicio) {
        // TODO: cuerpo real de este método no estaba disponible en el reporte — verificar contra
        // el proyecto original. El texto del reporte de tesis se corta exactamente después de
        // "card.setOrientation(LinearLayout.VERTICAL);" (línea ~3738 del reporte); el siguiente
        // bloque "Código" capturado (línea ~3748) ya pertenece a otra clase completamente
        // distinta (UserProfileActivity, ver docs/notas_extraccion_parte3.md), así que el resto
        // del cuerpo real de crearCardEjercicio() no aparece en ninguna parte del documento
        // fuente. Para no inventar lógica de negocio nueva (no se conocen los nombres exactos de
        // los campos JSON del objeto "ejercicio" más allá de lo que ya usa
        // ExerciseListActivity.crearCard() en este mismo proyecto: "nombre_ejercicio", "series",
        // "repeticiones"/"reps", "descanso", "peso_sugerido"), se agrega aquí solo el contenido
        // mínimo y honesto esperable siguiendo el mismo patrón de tarjeta ya usado en
        // ExerciseListActivity.java y FoodListActivity.java de este proyecto: un LinearLayout
        // vertical con un único TextView mostrando el nombre del ejercicio, agregado al
        // contenedor. No se agregan botones de acción, navegación (Intent) ni parámetros
        // adicionales que no estén confirmados contra el reporte.
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);

        TextView tvNombre = new TextView(this);
        String nombreEjercicio;
        try {
            nombreEjercicio = ejercicio.getString("nombre_ejercicio");
        } catch (Exception e) {
            nombreEjercicio = "Ejercicio";
        }
        tvNombre.setText(nombreEjercicio);
        tvNombre.setTextSize(16);
        tvNombre.setPadding(24, 24, 24, 24);
        card.addView(tvNombre);

        containerEjercicios.addView(card);
    }

    // TODO: cuerpo real de este método no estaba disponible en el reporte — verificar contra el
    // proyecto original. mostrarSelectorDias() se invoca desde onCreate()
    // ("cardSelectorDia.setOnClickListener(v -> mostrarSelectorDias())") pero nunca se define en
    // el rango de texto capturado por el reporte (el documento salta de TrainingListActivity a
    // UserProfileActivity sin volver a mostrar contenido de esta clase). Dado que "diasRutina" ya
    // contiene, tras obtenerDiasRutina(), la lista de días disponibles con "id_rutina" y
    // "dia_entrenamiento" por elemento, este método debería mostrar un selector (por ejemplo un
    // AlertDialog, siguiendo el mismo patrón usado en UserProfileActivity) con esos días y, al
    // elegir uno, actualizar idRutinaActual/tvDiaActual y volver a llamar a
    // cargarEjercicios(idRutina). No se implementa esa lógica aquí para no inventarla; se deja
    // como stub mínimo.
    private void mostrarSelectorDias() {
    }
}

package com.winterarc.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

// NOTA: esta clase referencia ExerciseInProgressActivity (navegación desde crearCard()), que no
// está definida en el rango de reporte asignado a este agente ni forma parte de la lista de
// clases a reconstruir; no se creó ningún archivo para ella.

public class ExerciseListActivity extends AppCompatActivity {

    LinearLayout containerEjercicios;
    int idRutina;
    String URL = "https://dodgerblue-emu-880788.hostingersite.com/winter_arc/obtener_ejercicios_rutina.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_list);

        containerEjercicios = findViewById(R.id.containerEjercicios);
        idRutina = getIntent().getIntExtra("id_rutina", 0);
        obtenerEjercicios();
    }

    private void obtenerEjercicios() {
        StringRequest request = new StringRequest(
                Request.Method.POST,
                URL,
                response -> {
                    Log.d("EJERCICIOS", response);
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.getBoolean("success")) {
                            JSONArray ejercicios = json.getJSONArray("ejercicios");
                            for (int i = 0; i < ejercicios.length(); i++) {
                                JSONObject e = ejercicios.getJSONObject(i);
                                crearCard(
                                        e.getString("nombre_ejercicio"),
                                        e.getInt("series"),
                                        e.getInt("repeticiones"),
                                        e.getInt("descanso"),
                                        e.getDouble("peso_sugerido")
                                );
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> Log.e("ERROR", error.toString())
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

    private void crearCard(String nombre, int series, int reps, int descanso, double peso) {
        TextView card = new TextView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 20, 0, 20);
        card.setLayoutParams(params);
        card.setPadding(40, 60, 40, 60);
        card.setText(
                nombre +
                        "\nSeries: " + series +
                        "\nReps: " + reps +
                        "\nDescanso: " + descanso + "s"
        );
        card.setTextSize(16);
        card.setBackgroundColor(ContextCompat.getColor(this, R.color.card));
        card.setTextColor(ContextCompat.getColor(this, R.color.text));
        card.setClickable(true);
        card.setOnClickListener(v -> {
            Intent intent = new Intent(
                    ExerciseListActivity.this,
                    ExerciseInProgressActivity.class
            );
            intent.putExtra("nombre", nombre);
            intent.putExtra("series", series);
            intent.putExtra("descanso", descanso);
            startActivity(intent);
        });
        containerEjercicios.addView(card);
    }
}

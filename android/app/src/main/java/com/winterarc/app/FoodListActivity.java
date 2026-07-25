package com.winterarc.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
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

public class FoodListActivity extends AppCompatActivity {

    int idDietaAlimento;
    SessionManager sessionManager;
    LinearLayout llAlimentos;
    TextView tvTitulo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_list);

        idDietaAlimento = getIntent().getIntExtra("id_dieta_alimento", -1);
        sessionManager = new SessionManager(this);
        llAlimentos = findViewById(R.id.llAlimentos);
        tvTitulo = findViewById(R.id.tvTitulo);
        tvTitulo.setText("ALIMENTOS EQUIVALENTES");

        cargarAlimentosEquivalentes();
    }

    private void cargarAlimentosEquivalentes() {
        String URL = "https://dodgerblue-emu-880788.hostingersite.com/winter_arc_api/obtener_alimentos_equivalentes.php";
        StringRequest request = new StringRequest(
                Request.Method.POST,
                URL,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (!json.getBoolean("success")) return;
                        JSONArray alimentos = json.getJSONArray("data");
                        llAlimentos.removeAllViews();
                        for (int i = 0; i < alimentos.length(); i++) {
                            JSONObject alimento = alimentos.getJSONObject(i);
                            int idAlimento = alimento.getInt("id_alimento");
                            LinearLayout card = new LinearLayout(this);
                            // NOTA: el reporte tenía "card.setOrientation(LinearLayout.VERTICAL;"
                            // (paréntesis de cierre faltante); se restauró.
                            card.setOrientation(LinearLayout.VERTICAL);
                            card.setBackgroundColor(ContextCompat.getColor(this, R.color.card));
                            card.setPadding(24, 24, 24, 24);

                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                            );
                            params.setMargins(0, 0, 0, 24);
                            card.setLayoutParams(params);
                            TextView nombre = new TextView(this);

                            nombre.setText(alimento.getString("nombre_alimento"));
                            nombre.setTextColor(ContextCompat.getColor(this, R.color.text));
                            nombre.setTextSize(18);
                            TextView macros = new TextView(this);
                            // NOTA: el reporte perdió el "+" de concatenación entre fragmentos y
                            // cortó el último argumento como "alimento.getString)" (sin nombre de
                            // campo). Se completó con "grasas" siguiendo el patrón Cal/P/C/G.
                            macros.setText(
                                    "Cal: " + alimento.getString("calorias")
                                            + " | P: " + alimento.getString("proteinas")
                                            + " | C: " + alimento.getString("carbohidratos")
                                            + " | G: " + alimento.getString("grasas")
                            );
                            macros.setTextColor(ContextCompat.getColor(this, R.color.text));
                            macros.setTextSize(13);

                            LinearLayout buttonRow = new LinearLayout(this);
                            buttonRow.setOrientation(LinearLayout.HORIZONTAL);
                            buttonRow.setPadding(0, 16, 0, 0);
                            LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                            );
                            btnParams.setMargins(0, 0, 16, 0);

                            Button btnReemplazar = new Button(this);
                            btnReemplazar.setText("Seleccionar");
                            btnReemplazar.setTextSize(12);
                            btnReemplazar.setAllCaps(false);
                            btnReemplazar.setLayoutParams(btnParams);
                            btnReemplazar.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.button));
                            btnReemplazar.setTextColor(ContextCompat.getColor(this, R.color.text));
                            btnReemplazar.setOnClickListener(v -> {
                                reemplazarAlimento(idAlimento);
                            });

                            Button btnCompletar = new Button(this);
                            btnCompletar.setText("Completado");
                            btnCompletar.setTextSize(12);
                            btnCompletar.setAllCaps(false);
                            btnCompletar.setLayoutParams(btnParams);
                            btnCompletar.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.button));
                            btnCompletar.setTextColor(ContextCompat.getColor(this, R.color.text));
                            btnCompletar.setOnClickListener(v -> marcarComoCompletado());

                            buttonRow.addView(btnReemplazar);
                            buttonRow.addView(btnCompletar);
                            card.addView(nombre);
                            card.addView(macros);
                            card.addView(buttonRow);
                            llAlimentos.addView(card);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> error.printStackTrace()
        ) {
            @Override
            protected java.util.Map<String, String> getParams() {
                java.util.Map<String, String> params = new java.util.HashMap<>();
                params.put("id_dieta_alimento", String.valueOf(idDietaAlimento));
                return params;
            }
        };
        Volley.newRequestQueue(this).add(request);
    }

    private void reemplazarAlimento(int idNuevoAlimento) {
        String URL = "https://dodgerblue-emu-880788.hostingersite.com/winter_arc_api/reemplazar_alimento.php";
        StringRequest request = new StringRequest(
                Request.Method.POST, URL,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.getBoolean("success")) {
                            Toast.makeText(
                                    this,
                                    "Dieta Actualizada",
                                    Toast.LENGTH_SHORT
                            ).show();
                            Intent intent = new Intent(FoodListActivity.this, DietActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> error.printStackTrace()
        ) {
            @Override
            protected java.util.Map<String, String> getParams() {
                java.util.Map<String, String> params = new java.util.HashMap<>();
                params.put("id_dieta_alimento", String.valueOf(idDietaAlimento));
                params.put("id_nuevo_alimento", String.valueOf(idNuevoAlimento));
                return params;
            }
        };
        Volley.newRequestQueue(this).add(request);
    }

    private void marcarComoCompletado() {
        String URL = "https://dodgerblue-emu-880788.hostingersite.com/winter_arc_api/marcar_completado.php";
        StringRequest request = new StringRequest(
                Request.Method.POST,
                URL,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.getBoolean("success")) {
                            int calorias = json.getInt("calorias_agregadas");
                            Toast.makeText(
                                    this,
                                    "Consumido +" + calorias + " kcal",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> error.printStackTrace()
        ) {
            @Override
            protected java.util.Map<String, String> getParams() {
                java.util.Map<String, String> params = new java.util.HashMap<>();
                params.put("id_usuario", String.valueOf(sessionManager.getUserId()));
                params.put("id_dieta_alimento", String.valueOf(idDietaAlimento));
                return params;
            }
        };
        Volley.newRequestQueue(this).add(request);
    }
}

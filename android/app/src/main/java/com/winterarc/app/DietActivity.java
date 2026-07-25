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

import java.util.HashMap;
import java.util.Map;

// ============================================================================================
// NOTA DE RECONSTRUCCIÓN IMPORTANTE (ver docs/notas_extraccion_parte3.md para el detalle
// completo): el reporte extraído perdió el encabezado "public class DietActivity extends
// AppCompatActivity {" (confirmado por un agente previo en notas_extraccion_parte1.md, punto 1:
// el bloque de código que sigue usa explícitamente "DietActivity.this" y la propia Descripción
// del reporte en la línea ~1764 dice literalmente "...concluye el proceso de registro de comidas
// dentro de la actividad DietActivity."). Ese encabezado se repone aquí porque es estructuralmente
// necesario, no es lógica inventada.
//
// Además del encabezado, el texto del reporte (líneas ~1533-1764) tampoco muestra el inicio real
// de onCreate() ni el inicio del método que carga la dieta (llamado aquí cargarDieta() por
// analogía con DashboardActivity.cargarResumen()): el fragmento capturado comienza a mitad de un
// bloque ya dentro de la respuesta exitosa de una petición Volley ("JSONArray desayuno =
// json.getJSONArray("desayuno");..."), con la variable "json" ya en alcance. Lo que SÍ está
// confirmado por el reporte y se preserva tal cual (solo con espaciado reparado):
//   - La extracción de los arreglos desayuno/comida/cena y las llamadas a crearSeccion().
//   - El cierre del método (getParams() con "id_usuario" y Volley.newRequestQueue(this).add(...)).
//   - Los métodos completos crearSeccion(String, JSONArray) y completarComida(String).
// La apertura de cargarDieta() (declaración de método, construcción de la URL, del StringRequest,
// y el "try { JSONObject json = new JSONObject(response); if (json.getBoolean("success")) {")
// se reconstruyó siguiendo el mismo patrón Volley/StringRequest usado en TODOS los demás métodos
// de esta misma clase y del resto de la app (por ejemplo completarComida(), más abajo, que SÍ
// está completo en el reporte). El endpoint "obtener_dieta.php" y el parámetro "id_usuario" no
// son una invención: están confirmados por el equivalente en Swift/iOS de esta misma pantalla
// (struct DietView, función cargarDieta(), líneas ~8439-8468 del reporte), que llama exactamente
// a "obtener_dieta.php" con "id_usuario" y lee "success"/"desayuno"/"comida"/"cena" directamente
// de la raíz del JSON (sin un objeto "data" anidado) -- exactamente lo que hace el fragmento Java
// que sí está confirmado. Esto es información ya presente en el propio documento fuente, no lógica
// de negocio nueva.
//
// El nombre del recurso de layout (R.layout.activity_diet) NO está confirmado contra el reporte
// -- ningún bloque "Interfaz de..." con ese nombre exacto apareció en el rango de esta tarea --
// se usa como marcador estructural; verificar contra el proyecto original si se recupera.
// ============================================================================================

public class DietActivity extends AppCompatActivity {

    LinearLayout llComidas;
    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(this);
        // NOTA: setContentView()/findViewById() no aparecen en el rango de reporte asignado a
        // esta clase; se reconstruyen por convención mínima (ver NOTA de cabecera del archivo).
        setContentView(R.layout.activity_diet);
        llComidas = findViewById(R.id.llComidas);

        cargarDieta();
    }

    // NOTA: la apertura de este método (declaración, URL, StringRequest, try/JSONObject/success)
    // no aparece en el reporte; reconstruida por convención (ver NOTA de cabecera). A partir de
    // "JSONArray desayuno = ..." el contenido es transcripción literal del reporte (líneas
    // ~1534-1550), solo con espaciado reparado.
    private void cargarDieta() {
        String URL = "https://dodgerblue-emu-880788.hostingersite.com/winter_arc_api/obtener_dieta.php";
        StringRequest request = new StringRequest(
                Request.Method.POST, URL,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.getBoolean("success")) {
                            JSONArray desayuno = json.getJSONArray("desayuno");
                            JSONArray comida = json.getJSONArray("comida");
                            JSONArray cena = json.getJSONArray("cena");
                            crearSeccion("Desayuno", desayuno);
                            crearSeccion("Comida", comida);
                            crearSeccion("Cena", cena);
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
                return params;
            }
        };
        Volley.newRequestQueue(this).add(request);
    }

    // Transcripción literal del reporte (líneas ~1555-1672), solo con espaciado/operadores
    // reparados (p. ej. concatenación "• " + nombre + " (" + cantidad + "g)").
    private void crearSeccion(String tipoComida, JSONArray comidas) {
        try {
            if (comidas.length() == 0) return;
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setBackgroundColor(ContextCompat.getColor(this, R.color.card));
            card.setPadding(24, 24, 24, 24);

            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
            params.setMargins(0, 0, 0, 24);
            card.setLayoutParams(params);

            TextView titulo = new TextView(this);
            titulo.setText(tipoComida);
            titulo.setTextSize(20);
            card.addView(titulo);

            for (int i = 0; i < comidas.length(); i++) {
                JSONObject alimento = comidas.getJSONObject(i);
                int idDietaAlimento = alimento.getInt("id_dieta_alimento");
                String nombre = alimento.getString("nombre");
                String cantidad = alimento.getString("cantidad");

                TextView tvAlimento = new TextView(this);
                tvAlimento.setText("• " + nombre + " (" + cantidad + "g)");
                tvAlimento.setTextSize(14);
                tvAlimento.setPadding(0, 0, 0, 12);
                tvAlimento.setOnClickListener(v -> {
                    Intent intent = new Intent(DietActivity.this, FoodListActivity.class);
                    intent.putExtra("id_dieta_alimento", idDietaAlimento);
                    startActivity(intent);
                });
                card.addView(tvAlimento);
            }

            Button btnCompletarComida = new Button(this);
            btnCompletarComida.setText("Completar " + tipoComida);
            btnCompletarComida.setTextSize(13);
            btnCompletarComida.setBackgroundTintList(
                    ContextCompat.getColorStateList(this, R.color.button)
            );
            btnCompletarComida.setTextColor(
                    ContextCompat.getColor(this, R.color.text)
            );
            btnCompletarComida.setAllCaps(false);

            LinearLayout.LayoutParams btnParams =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
            btnParams.setMargins(0, 16, 0, 0);
            btnCompletarComida.setLayoutParams(btnParams);

            btnCompletarComida.setOnClickListener(
                    v -> completarComida(tipoComida)
            );
            card.addView(btnCompletarComida);
            llComidas.addView(card);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Transcripción literal del reporte (líneas ~1683-1761), solo con espaciado reparado. La
    // única inferencia real es la concatenación del Toast (ver NOTA justo debajo).
    private void completarComida(String tipoComida) {
        String URL =
                "https://dodgerblue-emu-880788.hostingersite.com/winter_arc_api/completar_comida.php";
        StringRequest request =
                new StringRequest(
                        Request.Method.POST,
                        URL,
                        response -> {
                            try {
                                JSONObject json =
                                        new JSONObject(response);
                                if (json.getBoolean("success")) {
                                    int calorias =
                                            json.getInt("calorias_agregadas");
                                    int comidasActuales =
                                            sessionManager.getComidasRealizadas();
                                    sessionManager.guardarComidasRealizadas(
                                            comidasActuales + 1
                                    );
                                    // NOTA: el reporte corta la concatenación justo después de
                                    // "Comida completada +" (falta el resto del texto). Se
                                    // completó con la variable "calorias", declarada justo arriba
                                    // y sin ningún otro uso en el método -- señal clara de que
                                    // era el valor que faltaba concatenar, por analogía con el
                                    // mismo tipo de gap ya documentado para Dashboard/FoodList en
                                    // notas_extraccion_parte1.md.
                                    Toast.makeText(
                                            this,
                                            "Comida completada +" + calorias + " kcal",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        },
                        error ->
                                error.printStackTrace()
                ) {
                    @Override
                    protected java.util.Map<String, String>
                    getParams() {
                        java.util.Map<String, String>
                                params =
                                new java.util.HashMap<>();
                        params.put(
                                "id_usuario",
                                String.valueOf(
                                        sessionManager.getUserId()
                                )
                        );
                        params.put(
                                "tipo_comida",
                                tipoComida
                        );
                        return params;
                    }
                };
        Volley.newRequestQueue(this).add(request);
    }
}

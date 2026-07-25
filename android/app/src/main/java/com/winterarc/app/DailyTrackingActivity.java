package com.winterarc.app;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataType;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DailyTrackingActivity extends AppCompatActivity {

    TextView tvCaloriasTracking, tvPasos, tvMetaPasos;
    ProgressBar progressPasos;
    PieChart pieCalorias;
    BarChart barPeso;
    EditText etPeso;
    Button btnGuardarPeso;
    SessionManager sessionManager;
    GoogleFitManager fitManager;
    int pasosActuales = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_tracking);

        tvCaloriasTracking = findViewById(R.id.tvCaloriasTracking);
        tvPasos = findViewById(R.id.tvPasos);
        tvMetaPasos = findViewById(R.id.tvMetaPasos);
        progressPasos = findViewById(R.id.progressPasos);
        pieCalorias = findViewById(R.id.pieCalorias);
        barPeso = findViewById(R.id.barPeso);

        etPeso = findViewById(R.id.etPeso);
        btnGuardarPeso = findViewById(R.id.btnGuardarPeso);
        sessionManager = new SessionManager(this);
        fitManager = new GoogleFitManager();
        cargarSeguimiento();
        cargarGraficaPeso();
        btnGuardarPeso.setOnClickListener(v -> guardarPeso());
        pedirPermisoActividad();
    }

    private int obtenerMetaPasos() {
        String objetivo = sessionManager.getObjetivo();
        switch (objetivo) {
            case "Perder grasa": return 12000;
            case "Mantener peso": return 9000;
            case "Ganar masa muscular": return 7000;
            default: return 8000;
        }
    }

    private void pedirPermisoActividad() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, 2001);
        } else {
            manejarGoogleFit();
        }
    }

    private void manejarGoogleFit() {
        FitnessOptions fitnessOptions =
                FitnessOptions.builder()
                        .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                        .build();
        GoogleSignInAccount account =
                GoogleSignIn.getAccountForExtension(this, fitnessOptions);
        // NOTA DE RECONSTRUCCIÓN: el texto del reporte corta el método manejarGoogleFit() justo
        // después de obtener 'account'. El resto del cuerpo no aparece en el rango asignado; se
        // sigue el patrón estándar de GoogleSignIn.hasPermissions/requestPermissions. Ahora que
        // GoogleFitManager.java existe (con obtenerPasos(Context, PasosCallback), ver
        // notas_extraccion_parte3.md punto (d).10), se conecta aquí para que pasosActuales refleje
        // el valor real leído de Google Fit en vez de quedar fijo en 0.
        if (!GoogleSignIn.hasPermissions(account, fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                    this,
                    2002,
                    account,
                    fitnessOptions
            );
        } else {
            fitManager.obtenerPasos(this, pasos -> {
                pasosActuales = pasos;
                actualizarUiPasos();
            });
        }
    }

    // TODO: implementación inferida por convención estándar Android, no confirmada contra el
    // reporte. Actualiza tvPasos, tvMetaPasos y progressPasos con pasosActuales y la meta
    // devuelta por obtenerMetaPasos() (ya presente en el archivo).
    private void actualizarUiPasos() {
        int meta = obtenerMetaPasos();
        tvPasos.setText("Pasos: " + pasosActuales);
        tvMetaPasos.setText("Meta: " + meta + " pasos");
        progressPasos.setMax(meta);
        progressPasos.setProgress(Math.min(pasosActuales, meta));
    }

    // Portado desde la lógica equivalente en Swift (iOS) — func cargarSeguimiento() (~línea
    // 7412-7482 del reporte). El método original en Java no aparece en el reporte, verificar
    // contra el proyecto real. El endpoint obtener_seguimiento_diario.php y el formato de
    // respuesta ({success, data:{calorias_gastadas,...}}) ya se usan en Java en
    // DashboardActivity.cargarResumen(); aquí se replica el mismo patrón con Volley, agregando
    // el campo "calorias_consumidas" que sí lee la versión Swift. Al final se invoca
    // cargarGraficaPeso(), igual que el Swift original llama a cargarHistorialPeso().
    private void cargarSeguimiento() {
        String URL = "https://dodgerblue-emu-880788.hostingersite.com/winter_arc_api/obtener_seguimiento_diario.php";
        StringRequest request = new StringRequest(
                Request.Method.POST, URL,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.getBoolean("success")) {
                            JSONObject data = json.getJSONObject("data");
                            int consumidas = data.optInt("calorias_consumidas", 0);
                            int gastadas = data.optInt("calorias_gastadas", 0);
                            tvCaloriasTracking.setText(
                                    "Consumidas: " + consumidas + " kcal | Gastadas: " + gastadas + " kcal"
                            );
                            actualizarUiPasos();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        tvCaloriasTracking.setText("Error datos");
                    }
                },
                error -> {
                    error.printStackTrace();
                    tvCaloriasTracking.setText("Error conexión");
                }
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

    // Portado desde la lógica equivalente en Swift (iOS) — func cargarHistorialPeso() (~línea
    // 7495-7599 del reporte). El método original en Java no aparece en el reporte, verificar
    // contra el proyecto real. El Swift original lee el peso de cada registro como "peso" o,
    // si no está presente, como "peso_diario" (aceptando tanto Double como String) — aquí se
    // replica esa doble validación con JSONObject.optDouble()/has(). Los valores se cargan en el
    // BarChart "barPeso" ya importado (MPAndroidChart) en vez del gráfico declarativo de SwiftUI.
    private void cargarGraficaPeso() {
        String URL = "https://dodgerblue-emu-880788.hostingersite.com/winter_arc_api/obtener_historial_peso.php";
        StringRequest request = new StringRequest(
                Request.Method.POST, URL,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.getBoolean("success")) {
                            JSONArray array = json.getJSONArray("data");
                            ArrayList<BarEntry> entradas = new ArrayList<>();
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject item = array.getJSONObject(i);
                                double peso;
                                if (item.has("peso") && !item.isNull("peso")) {
                                    peso = item.optDouble("peso", 0);
                                } else {
                                    peso = item.optDouble("peso_diario", 0);
                                }
                                entradas.add(new BarEntry(i, (float) peso));
                            }
                            BarDataSet dataSet = new BarDataSet(entradas, "Peso (kg)");
                            BarData barData = new BarData(dataSet);
                            barPeso.setData(barData);
                            barPeso.invalidate();
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

    // Portado desde la lógica equivalente en Swift (iOS) — func guardarPeso() (~línea 7607-7679,
    // variante casi idéntica en ~8156, del reporte). El método original en Java no aparece en el
    // reporte, verificar contra el proyecto real. Valida que el campo no esté vacío y sea un
    // número válido (equivalente a "if nuevoPeso.isEmpty" / "if Double(peso)==nil" de Swift),
    // hace POST a guardar_peso_diario.php (mismo endpoint ya declarado como URL_PESO en
    // ConfigActivity) y, al terminar, limpia el campo y recarga la gráfica — igual que el Swift
    // original limpia "nuevoPeso" y llama a cargarHistorialPeso().
    private void guardarPeso() {
        String pesoStr = etPeso.getText().toString().trim();
        if (pesoStr.isEmpty()) return;
        final double peso;
        try {
            peso = Double.parseDouble(pesoStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Dato no válido", Toast.LENGTH_SHORT).show();
            return;
        }
        String URL = "https://dodgerblue-emu-880788.hostingersite.com/winter_arc_api/guardar_peso_diario.php";
        StringRequest request = new StringRequest(
                Request.Method.POST, URL,
                response -> {
                    Toast.makeText(this, "Peso actualizado", Toast.LENGTH_SHORT).show();
                    etPeso.setText("");
                    cargarGraficaPeso();
                },
                error -> error.printStackTrace()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id_usuario", String.valueOf(sessionManager.getUserId()));
                params.put("peso", String.valueOf(peso));
                return params;
            }
        };
        Volley.newRequestQueue(this).add(request);
    }
}

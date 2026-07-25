package com.winterarc.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.Calendar;

// NOTA DE RECONSTRUCCIÓN IMPORTANTE: en el reporte original, entre el marcador
// "public class DashboardActivity" (antes de la clase siguiente, ExerciseDetailActivity) hay un
// bloque grande de código (JSONArray desayuno/comida/cena, método crearSeccion(...),
// completarComida(...), uso explícito de "DietActivity.this") que NO puede pertenecer realmente
// a DashboardActivity (usar "DietActivity.this" dentro de DashboardActivity no compilaría, y la
// propia Descripción del reporte lo confirma: "...concluye el proceso de registro de comidas
// dentro de la actividad DietActivity."). Se concluye que el reporte perdió, durante la
// extracción, el encabezado "public class DietActivity extends AppCompatActivity {" (DietActivity
// no está en la lista de clases asignadas a este agente). Ese fragmento fue EXCLUIDO de este
// archivo para no mezclar dos clases distintas; debe reconstruirse por separado como
// DietActivity.java. Ver notas_extraccion_parte1.md para el detalle completo.

public class DashboardActivity extends AppCompatActivity {

    LinearLayout cardNutrition, cardTraining, cardProgress;
    TextView navHome, navTraining, navNutrition, navConfig;
    TextView tvCaloriasResumen, tvComidasResumen, tvEntrenamientoResumen;
    TextView tvMensajeMotivacional;
    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(this);

        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        setContentView(R.layout.activity_dashboard);
        cardNutrition = findViewById(R.id.cardNutrition);
        cardTraining = findViewById(R.id.cardTraining);
        cardProgress = findViewById(R.id.cardProgress);

        navHome = findViewById(R.id.navHome);
        navTraining = findViewById(R.id.navTraining);
        navNutrition = findViewById(R.id.navNutrition);
        navConfig = findViewById(R.id.navConfig);
        tvCaloriasResumen = findViewById(R.id.tvCaloriasResumen);
        tvComidasResumen = findViewById(R.id.tvComidasResumen);
        tvEntrenamientoResumen = findViewById(R.id.tvEntrenamientoResumen);

        cargarResumen();
        cardNutrition.setOnClickListener(v ->
                startActivity(new Intent(DashboardActivity.this, DietActivity.class)));
        cardTraining.setOnClickListener(v ->
                startActivity(new Intent(DashboardActivity.this, TrainingListActivity.class)));
        cardProgress.setOnClickListener(v ->
                startActivity(new Intent(DashboardActivity.this, DailyTrackingActivity.class)));

        navHome.setOnClickListener(v -> {});
        navTraining.setOnClickListener(v ->
                startActivity(new Intent(DashboardActivity.this, TrainingListActivity.class)));
        navNutrition.setOnClickListener(v ->
                startActivity(new Intent(DashboardActivity.this, DietActivity.class)));
        navConfig.setOnClickListener(v ->
                startActivity(new Intent(DashboardActivity.this, ConfigActivity.class)));
    }

    private void cargarResumen() {
        String URL = "https://dodgerblue-emu-880788.hostingersite.com/winter_arc_api/obtener_seguimiento_diario.php";
        StringRequest request = new StringRequest(
                Request.Method.POST, URL,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.getBoolean("success")) {
                            JSONObject data = json.getJSONObject("data");
                            int gastadas = data.getInt("calorias_gastadas");
                            // NOTA: el reporte corta la concatenación justo tras la comilla de
                            // apertura ("Calorías quemadas: "). Se completó por analogía con las
                            // líneas siguientes (tvComidasResumen) y con el mismo patrón usado en
                            // el resto de la app.
                            tvCaloriasResumen.setText("Calorías quemadas: " + gastadas);

                            tvMensajeMotivacional = findViewById(R.id.tvMensajeMotivacional);
                            mostrarMensajeMotivacional();

                            int comidas = sessionManager.getComidasRealizadas();
                            tvComidasResumen.setText("Comidas realizadas: " + comidas);
                            boolean rutina = sessionManager.isRutinaRealizada();
                            if (rutina) {
                                tvEntrenamientoResumen.setText("Entrenamiento: Realizado");
                            } else {
                                tvEntrenamientoResumen.setText("Entrenamiento: No realizado");
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        tvCaloriasResumen.setText("Error datos");
                        tvComidasResumen.setText("Error datos");
                        tvEntrenamientoResumen.setText("Error datos");
                    }
                },
                error -> {
                    error.printStackTrace();
                    tvCaloriasResumen.setText("Error conexión");
                    tvComidasResumen.setText("Error conexión");
                    tvEntrenamientoResumen.setText("Error conexión");
                }
        );
        // NOTA: el reporte no mostró explícitamente la línea de cierre de este método (agregar
        // la petición a la cola de Volley); se agrega siguiendo el mismo patrón usado en todos
        // los demás métodos basados en Volley de esta clase/proyecto.
        Volley.newRequestQueue(this).add(request);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // RECARGAR RESUMEN
        cargarResumen();
    }

    private void mostrarMensajeMotivacional() {
        Calendar calendar = Calendar.getInstance();
        int dia = calendar.get(Calendar.DAY_OF_WEEK);
        String mensaje;
        switch (dia) {
            case Calendar.MONDAY: mensaje = "Lunes: Un nuevo inicio para acercarte a tu mejor versión "; break;
            case Calendar.TUESDAY: mensaje = "Martes: La disciplina supera a la motivación "; break;
            case Calendar.WEDNESDAY: mensaje = "Miércoles: Cada entrenamiento cuenta "; break;
            case Calendar.THURSDAY: mensaje = "Jueves: Tu esfuerzo de hoy será tu resultado mañana "; break;
            case Calendar.FRIDAY: mensaje = "Viernes: No te rindas, ya casi termina la semana ⚡"; break;
            case Calendar.SATURDAY: mensaje = "Sábado: Entrena por salud, disciplina y amor propio "; break;
            case Calendar.SUNDAY: mensaje = "Domingo: Descansa, recupérate y prepárate para seguir "; break;
            default: mensaje = "Sigue avanzando hacia tus objetivos "; break;
        }
        tvMensajeMotivacional.setText(mensaje);
    }
}

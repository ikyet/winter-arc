package com.winterarc.app;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

// NOTA: ActualizacionDietaManager fue reconstruida en ActualizacionDietaManager.java aplicando
// la metodología nutricional documentada en el capítulo 2.2 del reporte (no había código fuente
// real que recuperar). Ver el comentario de cabecera de esa clase para el detalle completo.

public class ActualizacionDietaActivity extends AppCompatActivity {

    private TextView tvCaloriasActuales, tvCaloriasNuevas, tvProteinas, tvCarbohidratos, tvGrasas, tvNotificacion;
    private Button btnAceptar, btnRechazar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actualizacion_dieta_manager);

        tvCaloriasActuales = findViewById(R.id.tvCaloriasActuales);
        tvCaloriasNuevas = findViewById(R.id.tvCaloriasNuevas);
        tvProteinas = findViewById(R.id.tvProteinas);
        tvCarbohidratos = findViewById(R.id.tvCarbohidratos);
        tvGrasas = findViewById(R.id.tvGrasas);
        tvNotificacion = findViewById(R.id.tvNotificacion);

        btnAceptar = findViewById(R.id.btnAceptar);
        btnRechazar = findViewById(R.id.btnRechazar);

        int caloriasActuales = 2500;
        ActualizacionDietaManager.DietaActualizada dieta = ActualizacionDietaManager.actualizarDieta(80, 78, caloriasActuales, "definicion");

        tvCaloriasActuales.setText("Calorías actuales: " + caloriasActuales + " kcal");
        tvCaloriasNuevas.setText("Calorías sugeridas: " + dieta.calorias + " kcal");
        tvProteinas.setText("Proteínas: " + dieta.proteinas + " g");
        tvCarbohidratos.setText("Carbohidratos: " + dieta.carbohidratos + " g");
        tvGrasas.setText("Grasas: " + dieta.grasas + " g");

        btnAceptar.setOnClickListener(v -> {
            tvNotificacion.setVisibility(View.VISIBLE);
            new Handler().postDelayed(() -> {
                tvNotificacion.setVisibility(View.GONE);
                finish();
            }, 3000);
        });
        btnRechazar.setOnClickListener(v -> finish());
    }
}

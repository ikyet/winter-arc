package com.winterarc.app;

// NOTA DE RECONSTRUCCIÓN: a diferencia de otras clases de este proyecto, ExerciseInProgressActivity
// NO aparece en ningún punto del reporte — ni como código Java, ni como código Swift equivalente,
// ni como descripción de diseño en la sección 3.3. Solo se sabe que existe porque
// ExerciseListActivity.java la invoca con los extras "nombre" (String), "series" (int) y
// "descanso" (int, segundos). Como no hay NADA que recuperar o portar, esta es una implementación
// funcional NUEVA (no una reconstrucción), pensada para cumplir el propósito evidente de una
// pantalla de "ejercicio en progreso": un temporizador de descanso entre series y un contador de
// series completadas. Revisar/ajustar si se recupera el diseño original.

import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ExerciseInProgressActivity extends AppCompatActivity {

    TextView tvNombre, tvSeries, tvTemporizador;
    Button btnSerieCompletada;

    int totalSeries;
    int descansoSegundos;
    int seriesCompletadas = 0;
    CountDownTimer temporizador;
    boolean descansando = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_in_progress);

        String nombre = getIntent().getStringExtra("nombre");
        totalSeries = getIntent().getIntExtra("series", 0);
        descansoSegundos = getIntent().getIntExtra("descanso", 60);

        tvNombre = findViewById(R.id.tvNombre);
        tvSeries = findViewById(R.id.tvSeries);
        tvTemporizador = findViewById(R.id.tvTemporizador);
        btnSerieCompletada = findViewById(R.id.btnSerieCompletada);

        tvNombre.setText(nombre);
        actualizarContadorSeries();

        btnSerieCompletada.setOnClickListener(v -> {
            if (descansando) return;
            seriesCompletadas++;
            actualizarContadorSeries();
            if (seriesCompletadas >= totalSeries) {
                tvTemporizador.setText("¡Ejercicio completado!");
                btnSerieCompletada.setEnabled(false);
            } else {
                iniciarDescanso();
            }
        });
    }

    private void actualizarContadorSeries() {
        tvSeries.setText("Serie " + Math.min(seriesCompletadas + 1, totalSeries) + " de " + totalSeries);
    }

    private void iniciarDescanso() {
        descansando = true;
        btnSerieCompletada.setEnabled(false);
        temporizador = new CountDownTimer(descansoSegundos * 1000L, 1000) {
            @Override
            public void onTick(long millisRestantes) {
                tvTemporizador.setText("Descanso: " + (millisRestantes / 1000) + "s");
            }

            @Override
            public void onFinish() {
                tvTemporizador.setText("¡Listo para la siguiente serie!");
                descansando = false;
                btnSerieCompletada.setEnabled(true);
            }
        };
        temporizador.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (temporizador != null) {
            temporizador.cancel();
        }
    }
}

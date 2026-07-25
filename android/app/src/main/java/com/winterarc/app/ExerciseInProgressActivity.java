package com.winterarc.app;

// NOTA DE RECONSTRUCCIÓN: esta clase no aparece en ningún punto del reporte (ni su código Java ni
// una descripción de diseño en la sección 3.3), a diferencia de DietActivity/GoogleFitManager/
// UserProfileActivity, cuyo contenido sí estaba presente pero mal atribuido. Solo se sabe que
// existe porque ExerciseListActivity.java la invoca con los extras "nombre", "series", "descanso".
// Por eso este archivo es un placeholder mínimo y honesto (solo muestra esos datos), NO una
// reconstrucción del comportamiento real (que probablemente incluía temporizador de descanso,
// registro de series completadas, etc.). TODO: reemplazar con la lógica real si se recupera el
// proyecto original o el diseño de esta pantalla.

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ExerciseInProgressActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_in_progress);

        String nombre = getIntent().getStringExtra("nombre");
        int series = getIntent().getIntExtra("series", 0);
        int descanso = getIntent().getIntExtra("descanso", 0);

        TextView tvInfo = findViewById(R.id.tvInfo);
        tvInfo.setText(nombre + "\nSeries: " + series + "\nDescanso: " + descanso + "s");
    }
}

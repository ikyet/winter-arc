package com.winterarc.app;

// NOTA DE RECONSTRUCCIÓN: esta clase nunca aparece como código Java en el reporte (ni su
// encabezado ni su cuerpo). Se sabe que existe porque RegisterActivity.java la invoca vía
// startActivityForResult(new Intent(this, TermsActivity.class), TERMS_REQUEST) y espera
// RESULT_OK para marcar cbTerminos como aceptado. El diseño visual sí está documentado en la
// sección 3.3 del reporte ("MODULO CREAR CUENTA / TERMINOS Y CONDICIONES"): ScrollView blanco,
// título "Términos y Condiciones", bloque de texto legal, CheckBox "Acepto los términos y
// condiciones" y un botón que regresa a la pantalla anterior. La lógica de este archivo (habilitar
// el botón solo si el checkbox está marcado, devolver RESULT_OK al presionarlo) es una
// implementación mínima y estándar para cumplir ese contrato -- no una recuperación literal.

import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;

import androidx.appcompat.app.AppCompatActivity;

public class TermsActivity extends AppCompatActivity {

    CheckBox cbAceptar;
    Button btnContinuar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms);

        cbAceptar = findViewById(R.id.cbAceptar);
        btnContinuar = findViewById(R.id.btnContinuar);

        btnContinuar.setEnabled(false);
        cbAceptar.setOnCheckedChangeListener((buttonView, isChecked) ->
                btnContinuar.setEnabled(isChecked));

        btnContinuar.setOnClickListener(v -> {
            setResult(RESULT_OK);
            finish();
        });
    }
}

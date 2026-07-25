package com.winterarc.app;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ConfigActivity extends AppCompatActivity {

    Button btnModo, btnCerrarSesion, btnFuentes, btnModificarAlimentos;
    EditText editPeso;
    SessionManager sessionManager;
    SharedPreferences prefs;
    String URL_PESO = "https://dodgerblue-emu-880788.hostingersite.com/winter_arc_api/guardar_peso_diario.php";
    String URL_ALIMENTOS = "https://dodgerblue-emu-880788.hostingersite.com/winter_arc_api/guardar_alimentos_no_deseados.php";
    String URL_LISTAR = "https://dodgerblue-emu-880788.hostingersite.com/winter_arc_api/listar_alimentos.php?id_categoria=1";
    ArrayList seleccionados = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);
        sessionManager = new SessionManager(this);
        btnModo = findViewById(R.id.btnModo);
        btnCerrarSesion = findViewById(R.id.btnCerrarSesion);
        btnFuentes = findViewById(R.id.btnFuentes);
        btnModificarAlimentos = findViewById(R.id.btnModificarAlimentos);
        editPeso = findViewById(R.id.editPeso);
        prefs = getSharedPreferences("config", MODE_PRIVATE);

        actualizarTextoModo();
        btnModo.setOnClickListener(v -> cambiarModo());
        btnCerrarSesion.setOnClickListener(v -> cerrarSesion());
        btnFuentes.setOnClickListener(v -> abrirFuentes());
        btnModificarAlimentos.setOnClickListener(v -> cargarAlimentos());
        editPeso.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                guardarPeso();
            }
        });
    }

    // NOTA DE RECONSTRUCCIÓN: los métodos actualizarTextoModo(), cambiarModo(), cerrarSesion(),
    // abrirFuentes(), cargarAlimentos() y guardarPeso() se invocan dentro de onCreate() pero su
    // implementación NO aparecía en el rango del reporte asignado a este agente
    // (líneas ~1010-2644). El bloque "Código" de ConfigActivity terminaba justo al cierre de
    // onCreate() y el siguiente marcador "public class" correspondía ya a DailyTrackingActivity.
    //
    // ACTUALIZACIÓN (sesión posterior, ver notas_extraccion_parte4.md): se completaron todos los
    // métodos a continuación. Ninguno de ellos aparece como código Java en el reporte (se buscó
    // exhaustivamente). cambiarModo() y abrirFuentes()/abrirLink() se portaron desde los
    // equivalentes en Swift/iOS del mismo reporte (func cambiarModo() / func abrirLink(_:),
    // ~línea 8199, y el confirmationDialog de fuentes en ~línea 8135). cargarAlimentos() se
    // portó desde func cargarAlimentos() en Swift (~línea 8162, y variantes en ~5471/6492) que
    // llama a listar_alimentos.php?id_categoria=... El resto (actualizarTextoModo, cerrarSesion,
    // guardarPeso) no tiene equivalente Swift claro y se implementó por convención estándar
    // Android / analogía con el resto del proyecto (guardarPeso replica el patrón usado en
    // DailyTrackingActivity y en el guardarPeso() Swift de la propia pantalla de configuración,
    // ~línea 8156).

    // TODO: implementación inferida por convención estándar Android, no confirmada contra el
    // reporte. No hay un método Swift equivalente claro para "actualizarTextoModo" (el iOS usa
    // un Picker/Toggle declarativo, no un texto que se actualiza manualmente).
    private void actualizarTextoModo() {
        boolean modoOscuro = prefs.getBoolean("modo_oscuro", true);
        btnModo.setText(modoOscuro ? "Modo claro" : "Modo oscuro");
    }

    // Portado desde la lógica equivalente en Swift (iOS) — func cambiarModo(){modoOscuro.toggle()}
    // (~línea 8199 del reporte). El método original en Java no aparece en el reporte, verificar
    // contra el proyecto real. Se usa SharedPreferences ("prefs", ya inicializado en onCreate)
    // en lugar de una @AppStorage/@State de SwiftUI.
    private void cambiarModo() {
        boolean modoOscuro = prefs.getBoolean("modo_oscuro", true);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("modo_oscuro", !modoOscuro);
        editor.apply();
        actualizarTextoModo();
    }

    // TODO: implementación inferida por convención estándar Android, no confirmada contra el
    // reporte. El equivalente Swift (func logout(), ~línea 8212) reemplaza el rootViewController
    // de la ventana; en Android el patrón estándar es limpiar la sesión y relanzar LoginActivity
    // con flags que limpien el back stack.
    private void cerrarSesion() {
        sessionManager.logout();
        Intent intent = new Intent(ConfigActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // Portado desde la lógica equivalente en Swift (iOS) — el confirmationDialog "Selecciona una
    // fuente" con los botones MuscleWiki / Base de Alimentos de México / Tablas Mexicanas PDF
    // (~línea 8135 del reporte). El método original en Java no aparece en el reporte, verificar
    // contra el proyecto real. Las URLs se tomaron literalmente del bloque Swift correspondiente.
    private void abrirFuentes() {
        String[] fuentes = {
                "MuscleWiki",
                "Base de Alimentos de México",
                "Tablas Mexicanas PDF"
        };
        String[] urls = {
                "https://musclewiki.com",
                "https://insp.mx/informacion-relevante/bam-bienvenida",
                "https://www.incmnsz.mx/2019/TABLAS_ALIMENTOS.pdf"
        };
        new AlertDialog.Builder(this)
                .setTitle("Selecciona una fuente")
                .setItems(fuentes, (dialog, which) -> abrirLink(urls[which]))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // Portado desde la lógica equivalente en Swift (iOS) — func abrirLink(_ url:String){if let
    // link=URL(string:url){UIApplication.shared.open(link)}} (~línea 8199 del reporte). El
    // método original en Java no aparece en el reporte, verificar contra el proyecto real.
    private void abrirLink(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }

    // Portado desde la lógica equivalente en Swift (iOS) — func cargarAlimentos() (~línea 8162
    // del reporte: GET a listar_alimentos.php?id_categoria=1, la respuesta es un arreglo JSON
    // plano, no envuelto en {success,data}); ver también los fragmentos del mismo endpoint en
    // ~línea 5471 y ~6492. El método original en Java no aparece en el reporte, verificar contra
    // el proyecto real. Se replica con Volley siguiendo el estilo de FoodListActivity.java
    // (StringRequest + parseo manual de JSON), y se muestra un AlertDialog de selección múltiple
    // (equivalente funcional al .sheet con Toggle por alimento que usa la versión iOS) para
    // marcar los alimentos no deseados, guardados en el campo "seleccionados" ya declarado.
    private void cargarAlimentos() {
        StringRequest request = new StringRequest(
                Request.Method.GET,
                URL_LISTAR,
                response -> {
                    try {
                        JSONArray alimentos = new JSONArray(response);
                        seleccionados.clear();
                        int n = alimentos.length();
                        String[] nombres = new String[n];
                        int[] ids = new int[n];
                        boolean[] marcados = new boolean[n];
                        for (int i = 0; i < n; i++) {
                            JSONObject alimento = alimentos.getJSONObject(i);
                            ids[i] = alimento.getInt("id_alimento");
                            nombres[i] = alimento.getString("nombre_alimento");
                        }
                        new AlertDialog.Builder(this)
                                .setTitle("Alimentos no deseados")
                                .setMultiChoiceItems(nombres, marcados, (dialog, which, isChecked) -> {
                                    if (isChecked) {
                                        seleccionados.add(ids[which]);
                                    } else {
                                        seleccionados.remove((Integer) ids[which]);
                                    }
                                })
                                .setPositiveButton("Guardar", (dialog, which) -> guardarAlimentosNoDeseados())
                                .setNegativeButton("Cancelar", null)
                                .show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> error.printStackTrace()
        );
        Volley.newRequestQueue(this).add(request);
    }

    // Portado desde la lógica equivalente en Swift (iOS) — func guardarAlimentos() (~línea
    // 8176-8186 del reporte), invocado desde el botón "Guardar" del diálogo de alimentos no
    // deseados. Usa el campo URL_ALIMENTOS ya declarado en la clase.
    private void guardarAlimentosNoDeseados() {
        StringRequest request = new StringRequest(
                Request.Method.POST,
                URL_ALIMENTOS,
                response -> Toast.makeText(this, "Alimentos guardados", Toast.LENGTH_SHORT).show(),
                error -> error.printStackTrace()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id_usuario", String.valueOf(sessionManager.getUserId()));
                for (int i = 0; i < seleccionados.size(); i++) {
                    params.put("ids_alimentos[" + i + "]", String.valueOf(seleccionados.get(i)));
                }
                return params;
            }
        };
        Volley.newRequestQueue(this).add(request);
    }

    // Portado desde la lógica equivalente en Swift (iOS) — func guardarPeso() (~línea 8156 del
    // reporte: valida que el peso sea numérico, si no muestra "Dato no válido"; luego hace POST
    // a guardar_peso_diario.php con id_usuario y peso). El método original en Java no aparece en
    // el reporte, verificar contra el proyecto real. NOTA: este método no estaba en la lista
    // explícita de métodos a completar para ConfigActivity, pero onCreate() ya lo invocaba desde
    // editPeso.setOnFocusChangeListener(...), así que se completó igualmente para que el archivo
    // compile; usa el campo URL_PESO ya declarado en la clase.
    private void guardarPeso() {
        String pesoStr = editPeso.getText().toString().trim();
        if (pesoStr.isEmpty()) return;
        final double peso;
        try {
            peso = Double.parseDouble(pesoStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Dato no válido", Toast.LENGTH_SHORT).show();
            return;
        }
        StringRequest request = new StringRequest(
                Request.Method.POST,
                URL_PESO,
                response -> Toast.makeText(this, "Peso actualizado", Toast.LENGTH_SHORT).show(),
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

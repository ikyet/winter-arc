package com.winterarc.app;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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

// ============================================================================================
// NOTA DE RECONSTRUCCIÓN IMPORTANTE (ver docs/notas_extraccion_parte3.md para el detalle
// completo, y notas_extraccion_parte2.md punto (e).2, donde un agente previo ya había detectado
// y documentado este mismo bloque sin reconstruirlo por estar fuera de su alcance):
//
// El reporte NUNCA muestra la línea "public class UserProfileActivity ..." (se verificó con
// búsqueda exhaustiva sobre todo el documento). El código real de esta clase aparece disperso
// dentro del rango que se le había asignado al agente de TrainingListActivity, entre las líneas
// ~3748 y 4535 del reporte, terminando justo antes de la sección "ARCHIVOS SWIFT APP IOS" (línea
// 4549). El cierre de clase SÍ es literal y explícito: el propio texto trae el comentario
// "// FIN DE CLASE" seguido de "}" (línea 4534-4535), y la Descripción inmediatamente posterior
// dice literalmente: "Con este bloque concluye la implementación completa de UserProfileActivity."
//
// Dentro de ese rango, el fragmento capturado comienza A MITAD del método seleccionarSexo() (en
// ".setItems(...)"), es decir, faltan por completo: el encabezado de clase, los campos, onCreate()
// y la apertura de seleccionarSexo() (arreglo de opciones + AlertDialog.Builder + setTitle). A
// partir de ahí, TODO lo demás (seleccionarActividad(), seleccionarDias(), seleccionarObjetivo(),
// seleccionarAlimentosNoDeseados(), guardarAlimentosNoDeseados(), validarPerfil(),
// guardarPerfilServidor(), generarDieta(), generarRutina(), irAlDashboard(), onResume(),
// onDestroy()) SÍ está en el reporte de forma prácticamente completa; se transcribió literalmente,
// solo reparando espaciado/operadores perdidos (documentado método por método más abajo).
//
// Lo que fue reconstruido (no extraído) y por qué:
//   1. Encabezado de clase "public class UserProfileActivity extends AppCompatActivity {":
//      estructuralmente necesario, no es lógica de negocio.
//   2. Campos (etEdad, etPeso, etEstatura, tvSexo, tvActividad, tvDias, tvObjetivo, tvError,
//      sessionManager, idUsuario, sexoSeleccionado, actividadSeleccionada, objetivoSeleccionado,
//      diasSeleccionados): sus NOMBRES están confirmados por el uso explícito en el código
//      extraído; solo la declaración en sí (tipo + nombre) se repone, no es una invención de
//      contenido nuevo.
//   3. tvProteinas, tvCarbohidratos, tvGrasas y btnRegistrarPerfil: nombres tomados de
//      notas_extraccion_parte1.md (punto 11), donde un agente previo documentó el bloque de
//      interfaz "configuración de perfil físico" (etEdad, etPeso, etEstatura, tvSexo, tvActividad,
//      tvDias, tvObjetivo, tvProteinas, tvCarbohidratos, tvGrasas, btnRegistrarPerfil) que casi
//      con certeza pertenece a esta pantalla y no a ConfigActivity. No se creó un layout XML para
//      esta clase (fuera del alcance de esta tarea); setContentView() usa un nombre de recurso no
//      confirmado.
//   4. onCreate(): NO aparece en el reporte. Se reconstruyó de forma mínima: findViewById() para
//      cada campo (por la convención de nombrado 1:1 campo-Java/ID-de-layout usada
//      sistemáticamente en el resto de esta app), inicialización de sessionManager/idUsuario, y el
//      cableado de listeners (tvSexo/tvActividad/tvDias/tvObjetivo -> abren su selector;
//      tvProteinas/tvCarbohidratos/tvGrasas -> seleccionarAlimentosNoDeseados(1/2/3);
//      btnRegistrarPerfil -> validarPerfil()). El orden de categorías (Proteínas=1,
//      Carbohidratos=2, Grasas=3) se infiere del orden de declaración en el ProfileView de Swift
//      (alimentosProteina, alimentosCarbo, alimentosGrasa, líneas ~6121-6123 del reporte); NO está
//      confirmado en el código Java. Todo esto es plantilla estructural razonable, NO lógica de
//      negocio real de la app (no decide cálculos, no inventa reglas de validación adicionales).
//   5. Apertura de seleccionarSexo() (arreglo de opciones y título del diálogo): se tomó de la
//      pantalla equivalente en Swift/iOS de esta misma app (struct ProfileView, líneas
//      ~6174-6176 del reporte: "Picker("Sexo", selection:$sexo){ Text("Masculino")...
//      Text("Femenino")... }"). No es una invención nueva: es contenido ya presente en el propio
//      documento fuente, para la misma pantalla, en el mismo reporte.
//
// Un desajuste detectado y corregido en el propio texto extraído (no inventado, ver también el
// método guardarAlimentosNoDeseados más abajo): en seleccionarAlimentosNoDeseados(), el reporte
// trae "if( cantidadSeleccionados = totalAlimentos ){" -- un solo "=" no compila como condición
// booleana de un int. Se corrigió a "==", consistente con la Descripción del propio reporte
// ("Si el usuario intenta excluir todos los alimentos, la operación es cancelada").
// ============================================================================================

public class UserProfileActivity extends AppCompatActivity {

    EditText etEdad, etPeso, etEstatura;
    TextView tvSexo, tvActividad, tvDias, tvObjetivo, tvError;
    TextView tvProteinas, tvCarbohidratos, tvGrasas;
    Button btnRegistrarPerfil;
    SessionManager sessionManager;
    int idUsuario;

    String sexoSeleccionado = "";
    String actividadSeleccionada = "";
    String objetivoSeleccionado = "";
    int diasSeleccionados = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // NOTA: onCreate() completo es reconstrucción estructural, no texto extraído. Ver NOTA de
        // cabecera del archivo (punto 4).
        setContentView(R.layout.activity_user_profile);

        etEdad = findViewById(R.id.etEdad);
        etPeso = findViewById(R.id.etPeso);
        etEstatura = findViewById(R.id.etEstatura);
        tvSexo = findViewById(R.id.tvSexo);
        tvActividad = findViewById(R.id.tvActividad);
        tvDias = findViewById(R.id.tvDias);
        tvObjetivo = findViewById(R.id.tvObjetivo);
        tvError = findViewById(R.id.tvError);
        tvProteinas = findViewById(R.id.tvProteinas);
        tvCarbohidratos = findViewById(R.id.tvCarbohidratos);
        tvGrasas = findViewById(R.id.tvGrasas);
        btnRegistrarPerfil = findViewById(R.id.btnRegistrarPerfil);

        sessionManager = new SessionManager(this);
        idUsuario = sessionManager.getUserId();

        tvSexo.setOnClickListener(v -> seleccionarSexo());
        tvActividad.setOnClickListener(v -> seleccionarActividad());
        tvDias.setOnClickListener(v -> seleccionarDias());
        tvObjetivo.setOnClickListener(v -> seleccionarObjetivo());
        tvProteinas.setOnClickListener(v -> seleccionarAlimentosNoDeseados(1));
        tvCarbohidratos.setOnClickListener(v -> seleccionarAlimentosNoDeseados(2));
        tvGrasas.setOnClickListener(v -> seleccionarAlimentosNoDeseados(3));
        btnRegistrarPerfil.setOnClickListener(v -> validarPerfil());
    }

    // SEXO
    // NOTA: el reporte solo captura el final de este método (a partir de ".setItems(...)", líneas
    // ~3749-3754). El arreglo de opciones y el título del diálogo se tomaron del equivalente
    // Swift/iOS de esta misma pantalla (ver NOTA de cabecera, punto 5), no son invención nueva.
    private void seleccionarSexo() {
        String[] opciones = {"Masculino", "Femenino"};
        new AlertDialog.Builder(this)
                .setTitle("Sexo")
                .setItems(
                        opciones,
                        (dialog, which) -> {
                            sexoSeleccionado = opciones[which];
                            tvSexo.setText(sexoSeleccionado);
                        })
                .show();
    }

    // ACTIVIDAD -- transcripción literal del reporte (líneas ~3756-3795).
    private void seleccionarActividad() {
        String[] opciones = {
                "Sedentario",
                "Actividad ligera",
                "Actividad moderada",
                "Actividad alta",
                "Muy activo"
        };
        new AlertDialog.Builder(this)
                .setTitle("Nivel de actividad")
                .setItems(
                        opciones,
                        (dialog, which) -> {
                            actividadSeleccionada = opciones[which];
                            tvActividad.setText(actividadSeleccionada);
                        }
                )
                .show();
    }

    // DIAS -- transcripción literal del reporte (líneas ~3796-3821).
    private void seleccionarDias() {
        String[] opciones = {"1", "2", "3", "4", "5", "6", "7"};
        new AlertDialog.Builder(this)
                .setTitle("¿Cuántos días entrenas?")
                .setItems(
                        opciones,
                        (dialog, which) -> {
                            diasSeleccionados =
                                    Integer.parseInt(opciones[which]);
                            tvDias.setText(
                                    diasSeleccionados + " días por semana"
                            );
                        })
                .show();
    }

    // OBJETIVO -- transcripción literal del reporte (líneas ~3835-3863).
    private void seleccionarObjetivo() {
        String[] opciones = {
                "Bajar grasa",
                "Mantener peso",
                "Ganar músculo"
        };
        new AlertDialog.Builder(this)
                .setTitle("Objetivo físico")
                .setItems(
                        opciones,
                        (dialog, which) -> {
                            objetivoSeleccionado = opciones[which];
                            tvObjetivo.setText(objetivoSeleccionado);
                        })
                .show();
    }

    // ALIMENTOS -- transcripción literal del reporte (líneas ~3865-4009), con dos reparaciones:
    // (1) el "+" perdido en la construcción de la URL; (2) "=" -> "==" en la validación de que no
    // se excluyan todos los alimentos (ver NOTA de cabecera).
    private void seleccionarAlimentosNoDeseados(int idCategoria) {
        String URL = "https://dodgerblue-emu-880788.hostingersite.com/winter_arc_api/listar_alimentos.php?id_categoria=" + idCategoria;
        StringRequest request = new StringRequest(
                Request.Method.GET,
                URL,
                response -> {
                    try {
                        JSONArray array = new JSONArray(response);
                        int totalAlimentos = array.length();
                        String[] alimentos = new String[totalAlimentos];

                        boolean[] seleccionados = new boolean[totalAlimentos];
                        int[] idsAlimentos = new int[totalAlimentos];
                        for (int i = 0; i < totalAlimentos; i++) {
                            JSONObject obj = array.getJSONObject(i);
                            alimentos[i] = obj.getString("nombre_alimento");
                            idsAlimentos[i] = obj.getInt("id_alimento");
                        }

                        new AlertDialog.Builder(this)
                                .setTitle(
                                        "Selecciona alimentos que NO quieres"
                                )
                                .setMultiChoiceItems(
                                        alimentos,
                                        seleccionados,
                                        (dialog, which, isChecked) ->
                                                seleccionados[which] = isChecked
                                )
                                .setPositiveButton(
                                        "Guardar",
                                        (dialog, which) -> {
                                            ArrayList<Integer> noDeseados = new ArrayList<>();
                                            int cantidadSeleccionados = 0;
                                            for (int i = 0; i < seleccionados.length; i++) {
                                                if (seleccionados[i]) {
                                                    noDeseados.add(idsAlimentos[i]);
                                                    cantidadSeleccionados++;
                                                }
                                            }

                                            if (cantidadSeleccionados == totalAlimentos) {
                                                Toast.makeText(
                                                        this,
                                                        "Debe quedar mínimo un alimento disponible",
                                                        Toast.LENGTH_LONG
                                                ).show();
                                                return;
                                            }
                                            guardarAlimentosNoDeseados(noDeseados);
                                        }
                                )
                                .setNegativeButton("Cancelar", null)
                                .show();
                    } catch (Exception e) {
                        Toast.makeText(
                                this,
                                "Error alimentos",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                },
                error ->
                        Toast.makeText(
                                this,
                                "Error conexión",
                                Toast.LENGTH_SHORT
                        ).show());
        Volley.newRequestQueue(this).add(request);
    }

    // Transcripción literal del reporte (líneas ~4009-4091). NOTA: el parámetro se declara aquí
    // como "ArrayList<Integer>" (en vez del "ArrayList" crudo tal como aparece en el texto
    // extraído, "ArrayListidsAlimentos"): el propio cuerpo del método hace
    // "for (int id : idsAlimentos)", lo cual solo compila si el tipo conserva su parámetro
    // genérico <Integer> (un ArrayList crudo produce Object, no unboxeable a int). Se interpreta
    // como el mismo tipo de pérdida de texto resaltado que afectó a otros diamantes genéricos en
    // el documento (p. ej. "new ArrayList<>()"), no como una invención de lógica nueva.
    private void guardarAlimentosNoDeseados(ArrayList<Integer> idsAlimentos) {
        String URL =
                "https://dodgerblue-emu-880788.hostingersite.com/winter_arc_api/guardar_alimentos_no_deseados.php";
        StringRequest request = new StringRequest(
                Request.Method.POST,
                URL,
                response ->
                        Toast.makeText(
                                this,
                                "Alimentos guardados",
                                Toast.LENGTH_SHORT
                        ).show(),
                error ->
                        Toast.makeText(this,
                                "Error guardando alimentos",
                                Toast.LENGTH_SHORT
                        ).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put(
                        "id_usuario", String.valueOf(idUsuario));

                StringBuilder sb = new StringBuilder();
                for (int id : idsAlimentos) {
                    if (sb.length() > 0) sb.append(",");
                    sb.append(id);
                }
                params.put(
                        "ids_alimentos",
                        sb.toString());
                return params;
            }
        };
        Volley.newRequestQueue(this).add(request);
    }

    // VALIDAR PERFIL -- transcripción literal del reporte (líneas ~4093-4240), con el "+" perdido
    // en la construcción del mensaje de confirmación (setMessage) restaurado.
    private void validarPerfil() {
        String edadStr = etEdad.getText().toString().trim();
        String pesoStr = etPeso.getText().toString().trim();
        String estaturaStr = etEstatura.getText().toString().trim();

        if (
                edadStr.isEmpty()
                        ||
                        pesoStr.isEmpty()
                        ||
                        estaturaStr.isEmpty()
                        ||
                        sexoSeleccionado.isEmpty()
                        ||
                        actividadSeleccionada.isEmpty()
                        ||
                        objetivoSeleccionado.isEmpty()
                        ||
                        diasSeleccionados == 0) {
            tvError.setText("Completa todos los campos");
            tvError.setVisibility(View.VISIBLE);
            return;
        }
        int edad = Integer.parseInt(edadStr);
        double peso = Double.parseDouble(pesoStr);
        int estatura = Integer.parseInt(estaturaStr);

        if (edad < 12 || edad > 80) {
            tvError.setText("Edad no válida");
            tvError.setVisibility(View.VISIBLE);
            return;
        }
        tvError.setVisibility(View.GONE);

        new AlertDialog.Builder(this)
                .setTitle("Confirmar datos")
                .setMessage(
                        "¿Los datos ingresados son correctos?\n\n" +
                                "Edad: " + edad + " años\n\n" +
                                "Peso: " + peso + " kg\n\n" +
                                "Estatura: " + estatura + " cm\n\n" +
                                "Sexo: " + sexoSeleccionado + "\n\n" +
                                "Actividad: " + actividadSeleccionada + "\n\n" +
                                "Objetivo: " + objetivoSeleccionado + "\n\n" +
                                "Entrenamiento: " + diasSeleccionados + " días"
                )
                .setPositiveButton(
                        "Confirmar", (dialog, which) -> {
                            sessionManager.saveUserProfile(
                                    edad,
                                    peso,
                                    estatura,
                                    sexoSeleccionado,
                                    actividadSeleccionada,
                                    objetivoSeleccionado);
                            guardarPerfilServidor(
                                    edad,
                                    peso,
                                    estatura
                            );
                        })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // GUARDAR PERFIL -- transcripción literal del reporte (líneas ~4242-4356).
    private void guardarPerfilServidor(int edad, double peso, int estatura) {
        String URL =
                "https://dodgerblue-emu-880788.hostingersite.com/winter_arc_api/guardar_perfil.php";
        StringRequest request = new StringRequest(
                Request.Method.POST,
                URL,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.getBoolean("success")) {
                            generarDieta();
                        } else {
                            tvError.setText(
                                    json.getString("message")
                            );
                            tvError.setVisibility(View.VISIBLE);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    tvError.setText("Error de conexión");
                    tvError.setVisibility(View.VISIBLE);
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id_usuario", String.valueOf(idUsuario));
                params.put("edad", String.valueOf(edad));
                params.put("peso", String.valueOf(peso));

                params.put("estatura", String.valueOf(estatura));
                params.put("sexo", sexoSeleccionado);
                params.put("actividad", actividadSeleccionada);
                params.put("objetivo", objetivoSeleccionado);

                params.put("dias_entrenamiento", String.valueOf(diasSeleccionados));
                return params;
            }
        };
        Volley.newRequestQueue(this).add(request);
    }

    // GENERAR DIETA -- transcripción literal del reporte (líneas ~4371-4417).
    private void generarDieta() {
        StringRequest request = new StringRequest(
                Request.Method.POST,
                "https://dodgerblue-emu-880788.hostingersite.com/winter_arc_api/generar_dieta.php",
                response -> {
                    generarRutina();
                },
                error -> {
                    tvError.setText("Error generando dieta");
                    tvError.setVisibility(View.VISIBLE);
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id_usuario", String.valueOf(idUsuario));
                return params;
            }
        };
        Volley.newRequestQueue(this).add(request);
    }

    // GENERAR RUTINA -- transcripción literal del reporte (líneas ~4434-4473).
    private void generarRutina() {
        StringRequest request = new StringRequest(
                Request.Method.POST,
                "https://dodgerblue-emu-880788.hostingersite.com/winter_arc_api/generar_rutina.php",
                response -> {
                    irAlDashboard();
                },
                error -> {
                    tvError.setText("Error generando rutina");
                    tvError.setVisibility(View.VISIBLE);
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id_usuario", String.valueOf(idUsuario));
                params.put("dias_entrenamiento", String.valueOf(diasSeleccionados));
                return params;
            }
        };
        Volley.newRequestQueue(this).add(request);
    }

    // DASHBOARD -- transcripción literal del reporte (líneas ~4475-4499).
    private void irAlDashboard() {
        Intent intent = new Intent(
                UserProfileActivity.this,
                DashboardActivity.class
        );
        intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
        );
        startActivity(intent);
        finish();
    }

    // Transcripción literal del reporte (líneas ~4512-4518).
    @Override
    protected void onResume() {
        super.onResume();
        if (sessionManager.isLoggedIn() && sessionManager.hasProfile()) {
            tvError.setVisibility(View.GONE);
        }
    }

    // Transcripción literal del reporte (líneas ~4530-4535, incluye el cierre de clase explícito
    // "// FIN DE CLASE" + "}" que confirma el final real de UserProfileActivity).
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    // FIN DE CLASE
}

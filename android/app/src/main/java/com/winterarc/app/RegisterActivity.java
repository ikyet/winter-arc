package com.winterarc.app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    EditText etNombre, etCorreo, etPassword;
    Button btnCrearCuenta;
    CheckBox cbTerminos;
    TextView tvError, tvTerminos;
    private static final int TERMS_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        etNombre = findViewById(R.id.etNombre);
        etCorreo = findViewById(R.id.etCorreo);
        etPassword = findViewById(R.id.etPassword);
        btnCrearCuenta = findViewById(R.id.btnCrearCuenta);
        cbTerminos = findViewById(R.id.cbTerminos);
        tvError = findViewById(R.id.tvError);
        tvTerminos = findViewById(R.id.tvTerminos);
        btnCrearCuenta.setOnClickListener(v -> validarRegistro());
        tvTerminos.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, TermsActivity.class);
            startActivityForResult(intent, TERMS_REQUEST);
        });
    }

    // VALIDAR
    private void validarRegistro() {
        String nombre = etNombre.getText().toString().trim();
        String correo = etCorreo.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        if (nombre.isEmpty() || correo.isEmpty() || password.isEmpty()) {
            mostrarError("Campos incompletos");
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            mostrarError("Ingresa un correo válido");
            return;
        }
        if (!password.matches("^(?=.[A-Z])(?=.\d).{8,}$")) {
            mostrarError(
                    "La contraseña debe tener mínimo 8 caracteres, una mayúscula y un número"
            );
            return;
        }
        if (!cbTerminos.isChecked()) {
            mostrarError(
                    "Debes aceptar los términos y condiciones");
            return;
        }
        tvError.setVisibility(View.GONE);
        registrarUsuario(nombre, correo, password);
    }

    // REGISTER
    private void registrarUsuario(String nombre, String correo, String password) {
        String url =
                "https://dodgerblue-emu-880788.hostingersite.com/winter_arc_api/register.php";
        StringRequest request = new StringRequest(
                Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.getBoolean("success")) {
                            loginAutomatico(nombre, password);
                        } else {
                            tvError.setTextColor(
                                    getResources().getColor(android.R.color.holo_red_dark)
                            );
                            mostrarError(
                                    json.getString("message")
                            );
                        }
                    } catch (Exception e) {
                        tvError.setTextColor(
                                getResources().getColor(android.R.color.holo_red_dark)
                        );
                        mostrarError("Error de respuesta del servidor");
                    }
                },
                error -> {
                    tvError.setTextColor(
                            getResources().getColor(android.R.color.holo_red_dark)
                    );
                    mostrarError("Error de conexión");
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("nombre", nombre);
                params.put("correo", correo);
                params.put("password", password);
                return params;
            }
        };
        Volley.newRequestQueue(this).add(request);
    }

    // LOGIN AUTOMÁTICO
    private void loginAutomatico(String usuario, String password) {
        String URL =
                "https://dodgerblue-emu-880788.hostingersite.com/winter_arc_api/login.php";
        StringRequest request = new StringRequest(
                Request.Method.POST,
                URL, response -> {
            try {
                JSONObject json = new JSONObject(response);
                if (json.getBoolean("success")) {
                    int idUsuario =
                            json.getInt("id_usuario");
                    String username =
                            json.getString("nombre_usuario");
                    boolean hasProfile =
                            json.getBoolean("has_profile");
                    // SESSION MANAGER
                    SessionManager session = new SessionManager(this);
                    session.logout();
                    session.login(
                            idUsuario, username, hasProfile
                    );
                    tvError.setVisibility(View.VISIBLE);
                    tvError.setTextColor(
                            getResources().getColor(
                                    android.R.color.holo_green_dark
                            )
                    );
                    tvError.setText(
                            "Cuenta creada exitosamente"
                    );
                    new Handler().postDelayed(() -> {
                        Intent intent = new Intent(
                                RegisterActivity.this,
                                UserProfileActivity.class
                        );
                        intent.putExtra("id_usuario", idUsuario);
                        startActivity(intent);
                        finish();
                    }, 1500);
                } else {
                    mostrarError(
                            json.getString("message")
                    );
                }
            } catch (Exception e) {
                mostrarError("Error login automático");
            }
        },
                error ->
                        mostrarError("Error login automático")) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("usuario", usuario);
                params.put("password", password);
                return params;
            }
        };
        Volley.newRequestQueue(this).add(request);
    }

    // ERROR
    private void mostrarError(String mensaje) {
        tvError.setText(mensaje);
        tvError.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data) {
        super.onActivityResult(
                requestCode, resultCode, data);
        if (
                requestCode == TERMS_REQUEST
                        &&
                        resultCode == RESULT_OK) {
            cbTerminos.setChecked(true);
        }
    }
}

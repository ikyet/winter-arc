package com.winterarc.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    EditText etUsuario, etPassword;
    Button btnLogin, btnCrearCuenta;
    TextView tvError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SessionManager sessionManager = new SessionManager(this);

        if (sessionManager.isLoggedIn()) {
            if (sessionManager.hasProfile()) {
                startActivity(new Intent(this, DashboardActivity.class));
            } else {
                startActivity(new Intent(this, UserProfileActivity.class));
            }
            finish();
            return;
        }

        setContentView(R.layout.activity_login);
        etUsuario = findViewById(R.id.etUsuario);
        etPassword = findViewById(R.id.etPassword );
        btnLogin = findViewById(R.id.btnLogin );
        btnCrearCuenta = findViewById(R.id.btnCrearCuenta); tvError = findViewById(R.id.tvError);

        btnLogin.setOnClickListener(v -> validarLogin());
        btnCrearCuenta.setOnClickListener(
                v -> startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void validarLogin() {
        String usuario =
                etUsuario.getText().toString().trim();
        String password =
                etPassword.getText().toString().trim();
        if (usuario.isEmpty() || password.isEmpty()) {
            tvError.setText("Campos incompletos");
            tvError.setVisibility(View.VISIBLE);
            return;
        }
        tvError.setVisibility(View.GONE);
        String URL =
                "https://dodgerblue-emu-880788.hostingersite.com/winter_arc_api/login.php";
        StringRequest request =
                new StringRequest(
                        Request.Method.POST,
                        URL,
                        response -> {
                            try {
                                JSONObject json = new JSONObject(response);
                                if (json.getBoolean("success")) {
                                    int idUsuario =
                                            json.getInt("id_usuario");
                                    String username =
                                            json.getString("nombre_usuario");
                                    boolean hasProfile =
                                            json.getBoolean("has_profile");
                                    SessionManager session = new SessionManager(this);
                                    session.logout();
                                    session.login(idUsuario, username, hasProfile);
                                    if (hasProfile) {
                                        startActivity(new Intent(this, DashboardActivity.class));
                                    } else {
                                        Intent intent = new Intent(this, UserProfileActivity.class);
                                        intent.putExtra("id_usuario", idUsuario);
                                        startActivity(intent);
                                    }
                                    finish();
                                } else {
                                    tvError.setText(
                                            json.getString("message")
                                    );
                                    tvError.setVisibility(View.VISIBLE);
                                }
                            } catch (Exception e) {
                                tvError.setText("Error de respuesta");
                                tvError.setVisibility(View.VISIBLE);
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
                        params.put("usuario", usuario);
                        params.put("password", password);
                        return params;
                    }
                };
        Volley.newRequestQueue(this).add(request);
    }
}

package com.winterarc.app;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String KEY_COMIDAS_REALIZADAS = "comidas_realizadas";
    private static final String KEY_RUTINA_REALIZADA = "rutina_realizada";

    // GUARDAR COMIDAS
    public void guardarComidasRealizadas(int comidas) {
        editor.putInt(KEY_COMIDAS_REALIZADAS, comidas);
        editor.apply();
    }

    public int getComidasRealizadas() {
        return prefs.getInt(KEY_COMIDAS_REALIZADAS, 0);
    }

    // GUARDAR RUTINA
    public void guardarRutinaRealizada(boolean realizada) {
        editor.putBoolean(KEY_RUTINA_REALIZADA, realizada);
        editor.apply();
    }

    public boolean isRutinaRealizada() {
        return prefs.getBoolean(KEY_RUTINA_REALIZADA, false);
    }

    // RESETEAR NUEVO DÍA
    public void resetResumenDiario() {
        editor.putInt(KEY_COMIDAS_REALIZADAS, 0);
        editor.putBoolean(KEY_RUTINA_REALIZADA, false);
        editor.apply();
    }

    private static final String PREF_NAME = "winter_arc_session";
    private static final String KEY_LOGGED_IN = "logged_in";
    private static final String KEY_USER_ID = "id_usuario";
    private static final String KEY_USERNAME = "nombre_usuario";
    private static final String KEY_HAS_PROFILE = "has_profile";
    private static final String KEY_EDAD = "edad";
    private static final String KEY_PESO = "peso";
    private static final String KEY_ESTATURA = "estatura";
    private static final String KEY_SEXO = "sexo";
    private static final String KEY_ACTIVIDAD = "actividad";
    private static final String KEY_OBJETIVO = "objetivo";

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void login(
            int idUsuario,
            String username,
            boolean hasProfile
    ) {
        editor.putBoolean(KEY_LOGGED_IN, true);
        editor.putInt(KEY_USER_ID, idUsuario);
        editor.putString(KEY_USERNAME, username);
        editor.putBoolean(KEY_HAS_PROFILE, hasProfile);
        editor.apply();
    }

    // GUARDAR PERFIL
    public void saveUserProfile(
            int edad, double peso, int estatura, String sexo, String actividad, String objetivo
    ) {
        editor.putInt(KEY_EDAD, edad);
        editor.putFloat(KEY_PESO, (float) peso);
        editor.putInt(KEY_ESTATURA, estatura);
        editor.putString(KEY_SEXO, sexo);
        editor.putString(KEY_ACTIVIDAD, actividad);
        editor.putString(KEY_OBJETIVO, objetivo);
        editor.putBoolean(KEY_HAS_PROFILE, true);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_LOGGED_IN, false);
    }

    public boolean hasProfile() {
        return prefs.getBoolean(KEY_HAS_PROFILE, false);
    }

    public int getUserId() {
        return prefs.getInt(KEY_USER_ID, -1);
    }

    public String getUsername() {
        return prefs.getString(KEY_USERNAME, "");
    }

    public int getEdad() {
        return prefs.getInt(KEY_EDAD, 0);
    }

    public float getPeso() {
        return prefs.getFloat(KEY_PESO, 0f);
    }

    public int getEstatura() {
        return prefs.getInt(KEY_ESTATURA, 0);
    }

    public String getSexo() {
        return prefs.getString(KEY_SEXO, "");
    }

    public String getActividad() {
        return prefs.getString(KEY_ACTIVIDAD, "");
    }

    public String getObjetivo() {
        return prefs.getString(KEY_OBJETIVO, "");
    }

    // LOGOUT
    public void logout() {
        editor.clear();
        editor.apply();
    }
}

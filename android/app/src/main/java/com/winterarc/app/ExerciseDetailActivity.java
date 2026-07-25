package com.winterarc.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ExerciseDetailActivity extends AppCompatActivity {

    TextView tvTitle, tvDescription, tvBenefits;
    ImageView imgStep1;
    Button btnBack;
    YouTubePlayerView youtubePlayer;
    int idRutinaEjercicio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_detail);

        tvTitle = findViewById(R.id.tvTitle);
        tvDescription = findViewById(R.id.tvDescription);
        tvBenefits = findViewById(R.id.tvBenefits);
        imgStep1 = findViewById(R.id.imgStep1);
        btnBack = findViewById(R.id.btnBack);
        youtubePlayer = findViewById(R.id.youtubePlayer);
        getLifecycle().addObserver(youtubePlayer);

        idRutinaEjercicio = getIntent().getIntExtra("id_rutina_ejercicio", -1);
        btnBack.setOnClickListener(v -> finish());
        cargarDetalle();
    }

    private void cargarDetalle() {
        String URL = "https://dodgerblue-emu-880788.hostingersite.com/winter_arc_api/obtener_detalle_ejercicio.php";
        StringRequest request = new StringRequest(
                Request.Method.POST,
                URL,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.getBoolean("success")) {
                            JSONObject e = json.getJSONObject("data");
                            tvTitle.setText(e.getString("nombre_ejercicio"));
                            tvDescription.setText(e.getString("descripcion"));

                            tvBenefits.setText(
                                    "Nivel: " + e.getString("nivel")
                                            + "\nSeries: " + e.getString("series")
                                            + "\nRepeticiones: " + e.getString("repeticiones")
                                            + "\nDescanso: " + e.getString("descanso")
                                            + " seg");

                            Glide.with(this)
                                    .load(e.getString("imagen_url"))
                                    .into(imgStep1);
                            String videoUrl = e.getString("video_url");
                            // EXTRAER ID
                            String videoId = videoUrl.substring(videoUrl.indexOf("v=") + 2);
                            int amp = videoId.indexOf("&");
                            if (amp != -1) {
                                videoId = videoId.substring(0, amp);
                            }
                            final String videoIdFinal = videoId;
                            youtubePlayer.addYouTubePlayerListener(
                                    new AbstractYouTubePlayerListener() {
                                        @Override
                                        public void onReady(YouTubePlayer youTubePlayer) {
                                            // NOTA: el reporte mostraba "loadVideo(videoIdFinal,}0);}"
                                            // — la "}" antes del "0" es claramente un artefacto de
                                            // extracción; se reconstruyó como loadVideo(videoId, startSeconds).
                                            youTubePlayer.loadVideo(videoIdFinal, 0);
                                        }

                                        @Override
                                        public void onError(YouTubePlayer youTubePlayer, PlayerConstants.PlayerError error) {
                                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl));
                                            startActivity(intent);
                                        }
                                    }
                            );
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                },
                error -> error.printStackTrace()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id_rutina_ejercicio", String.valueOf(idRutinaEjercicio));
                return params;
            }
        };
        Volley.newRequestQueue(this).add(request);
    }
}

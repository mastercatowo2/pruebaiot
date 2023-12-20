package com.example.myapplication;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private Button btnActivate;
    private TextView tvSensorStatus;
    private Handler handler;
    private static final int UPDATE_INTERVAL = 5000; // Intervalo de actualización en milisegundos

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnActivate = findViewById(R.id.btnActivate);
        tvSensorStatus = findViewById(R.id.tvSensorStatus);
        handler = new Handler();

        btnActivate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Ejecutar tarea asincrónica para activar balizas y buzzer
                new ActivateTask().execute();
            }
        });

        // Iniciar actualizaciones periódicas
        startRepeatingTask();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Detener actualizaciones periódicas al destruir la actividad
        stopRepeatingTask();
    }

    private class ActivateTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                // Realizar la solicitud HTTP para activar las balizas y el buzzer
                String url = "https://api.thingspeak.com/update?api_key=SXABYBTHUASWY5GE&field3=1";
                performHttpRequest(url);

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            // Actualizar el estado del sensor después de activar balizas y buzzer
            updateSensorStatus();
        }
    }

    private void updateSensorStatus() {
        // Realizar la solicitud HTTP para recuperar el estado actual del campo field1 de ThingSpeak
        new RetrieveSensorStatusTask().execute();
    }

    private class RetrieveSensorStatusTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            try {
                // Realizar la solicitud HTTP para recuperar el estado actual del campo field1 de ThingSpeak
                String url = "https://api.thingspeak.com/channels/2375253/fields/1/last.txt";
                return performHttpRequest(url);

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            // Actualizar el estado del sensor en el TextView
            if (result != null) {
                String sensorStatus = "Desconocido";
                if (result.equals("1")) {
                    sensorStatus = "Obstruido";
                } else if (result.equals("0")) {
                    sensorStatus = "No Obstruido";
                }
                tvSensorStatus.setText("Estado del Sensor: " + sensorStatus);
            }
        }
    }

    private String performHttpRequest(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        // Configurar la solicitud
        con.setRequestMethod("GET");

        // Obtener la respuesta
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        StringBuilder response = new StringBuilder();
        String inputLine;

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        // Imprimir la respuesta
        Log.d("MainActivity", "Response from ThingSpeak: " + response.toString());

        return response.toString();
    }

    // Método para iniciar actualizaciones periódicas
    void startRepeatingTask() {
        handler.postDelayed(updateSensorStatus, UPDATE_INTERVAL);
    }

    // Método para detener actualizaciones periódicas
    void stopRepeatingTask() {
        handler.removeCallbacks(updateSensorStatus);
    }

    // Runnable para actualizaciones periódicas
    private Runnable updateSensorStatus = new Runnable() {
        public void run() {
            // Actualizar el estado cada 5 segundos
            updateSensorStatus();
            handler.postDelayed(this, UPDATE_INTERVAL);
        }
    };
}
package com.winterarc.app;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class StepCounterManager implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor stepSensor;
    private int pasosIniciales = -1;
    private int pasosActuales = 0;

    public StepCounterManager(Context context) {
        sensorManager = (SensorManager)
                context.getSystemService(Context.SENSOR_SERVICE);
        stepSensor =
                sensorManager.getDefaultSensor(
                        Sensor.TYPE_STEP_COUNTER
                );
    }

    public void start() {
        if (stepSensor != null) {
            sensorManager.registerListener(
                    this,
                    stepSensor,
                    SensorManager.SENSOR_DELAY_UI);
        }
    }

    public void stop() {
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (pasosIniciales == -1) {
            pasosIniciales = (int) event.values[0];
        }
        pasosActuales =
                (int) event.values[0]
                        - pasosIniciales;
    }

    @Override
    public void onAccuracyChanged(
            Sensor sensor,
            int accuracy
    ) {
    }

    public int getPasos() {
        return pasosActuales;
    }
}

package com.example.torche;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

public class TorchService extends Service implements SensorEventListener {

    private SensorManager mSensorManager;
    private CameraManager mCameraManager;
    private String mCameraId;
    private boolean isFlashlightOn = false;

    private static final int NOTIFICATION_ID = 123;
    private static final String NOTIFICATION_CHANNEL_ID = "TorchServiceChannel";
    private static final String CHANNEL_ID = "TorchServiceChannel";

    @Override
    public void onCreate() {
        super.onCreate();

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor mProximitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        if (mProximitySensor == null) {
            stopSelf();
        }

        mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            mCameraId = mCameraManager.getCameraIdList()[0];
        } catch (CameraAccessException e) {
            e.printStackTrace();
            stopSelf();
        }

        CharSequence name = "Torch Service Channel";
        String description = "Channel for Torch Service";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
        channel.setDescription(description);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);

        startForeground(NOTIFICATION_ID, createNotification());

        mSensorManager.registerListener(this, mProximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            mCameraManager.setTorchMode(mCameraId, false);
            isFlashlightOn = false;
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        mSensorManager.unregisterListener(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            if (event.values[0] == 0) {
                if (isFlashlightOn) {
                    try {
                        mCameraManager.setTorchMode(mCameraId, false);
                        isFlashlightOn = false;
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        mCameraManager.setTorchMode(mCameraId, true);
                        isFlashlightOn = true;
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        Intent stopIntent = new Intent(this, TorchService.class);
        stopIntent.setAction("STOP_SERVICE");
        PendingIntent stopPendingIntent = PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setContentTitle("Torch Service")
                .setContentText("Running")
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .addAction(0, "Stop", stopPendingIntent); // Ajouter le bouton Stop

        NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "Torch Service", NotificationManager.IMPORTANCE_DEFAULT);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);

        return builder.build();
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && "STOP_SERVICE".equals(intent.getAction())) {
            stopSelf();
        }
        return START_STICKY;
    }

}

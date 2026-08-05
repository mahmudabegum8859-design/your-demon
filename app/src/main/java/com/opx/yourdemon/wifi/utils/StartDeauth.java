package com.opx.yourdemon.wifi.utils;


import android.util.Log;

import com.opx.yourdemon.utils.Core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.function.Consumer;

public class StartDeauth {
    public String exec = Core.EXECUTE;
    public boolean limit;
    public String bssid;
    public String wlan;
    public Process process;

    public StartDeauth(String bssi, String inter, boolean lim, Core c) {
        limit = lim;
        bssid = bssi;
        wlan = inter;
    }

    public Boolean execute(Consumer<String> progressCallback) {
        String line;
        boolean result = false;
        try {
            process = Runtime.getRuntime().exec("su");
            OutputStream stdin = process.getOutputStream();
            InputStream stderr = process.getErrorStream();
            InputStream stdout = process.getInputStream();
            stdin.write((exec + " 'aireplay-ng -0 5 -a " + bssid + " " + wlan + "mon' " + '\n').getBytes());
            stdin.write(("exit\n").getBytes());
            stdin.flush();
            stdin.close();
            BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
            while ((line = br.readLine()) != null) {
                if (line.contains("avaible")) {
                    result = false;
                }
                if (line.contains("Waiting")) {
                    result = true;
                }
            }
            br.close();
            br = new BufferedReader(new InputStreamReader(stderr));
            while ((line = br.readLine()) != null) {
                if (progressCallback != null) progressCallback.accept(line);
            }
            br.close();
            process.waitFor();
            process.destroy();
        } catch (IOException e) {
            Log.d("Debug: ", "An IOException was caught: " + e.getMessage());
        } catch (InterruptedException ex) {
            Log.d("Debug: ", "An InterruptedException was caught: " + ex.getMessage());
        }
        return result;
    }

    public void kill() {
        process.destroy();
    }
}

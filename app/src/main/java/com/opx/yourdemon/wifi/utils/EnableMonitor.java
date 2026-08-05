package com.opx.yourdemon.wifi.utils;


import android.util.Log;

import com.opx.yourdemon.utils.Core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.function.Consumer;

public class EnableMonitor {
    public static String exec = Core.EXECUTE;

    public static Boolean execute(String wlan1, String ch, Core c, Consumer<String> progressCallback) {
        String line;
        boolean result = false;
        try {
            Process process = Runtime.getRuntime().exec("su");
            OutputStream stdin = process.getOutputStream();
            InputStream stderr = process.getErrorStream();
            InputStream stdout = process.getInputStream();
            if (wlan1.equals("wlan0")) {
                stdin.write(("ip link set wlan0 down; echo 4 > /sys/module/wlan/parameters/con_mode;ip link set wlan0 up&&sleep 3&&" + exec + "'iw dev'" + '\n').getBytes());
            } else {
                stdin.write((exec + " 'airmon-ng start " + wlan1 + " " + ch + "'" + '\n').getBytes());
            }
            stdin.write(("exit\n").getBytes());
            stdin.flush();
            stdin.close();
            ArrayList<String> out = new ArrayList<>();
            ArrayList<String> outerror = new ArrayList<>();
            BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
            while ((line = br.readLine()) != null) {
                out.add(line);
                if (wlan1.equals("wlan0") && line.contains("monitor")) {
                    result = true;
                } else if (!wlan1.equals("wlan0")) {
                    String temp = line.replaceAll("\\s+", "").replace("*", "");
                    if (temp.contains("mac80211monitormodevifenabled")) {
                        result = true;
                    }
                }
            }
            br.close();
            br = new BufferedReader(new InputStreamReader(stderr));
            while ((line = br.readLine()) != null) {
                outerror.add(line);
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
}

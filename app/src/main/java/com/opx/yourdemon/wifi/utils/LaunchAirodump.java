package com.opx.yourdemon.wifi.utils;


import android.util.Log;

import com.opx.yourdemon.utils.Core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;

public class LaunchAirodump {
    public String exec = Core.EXECUTE;
    public String bssid;
    public String wlan;
    public Process air;

    public LaunchAirodump(String bssid1, String wlan1, Core c) {
        bssid = bssid1;
        wlan = wlan1;
    }

    public ArrayList<String> execute() {
        String line;
        ArrayList<String> issuccess = new ArrayList<>();
        try {
            air = Runtime.getRuntime().exec("su");
            OutputStream stdin = air.getOutputStream();
            InputStream stderr = air.getErrorStream();
            InputStream stdout = air.getInputStream();
            String cmd;
            if (wlan.equals("wlan0")) {
                cmd = "airodump-ng " + wlan + " -w /sdcard/YourDemon/hs/handshake --ignore-negative-one --output-format pcap --bssid " + bssid;
            } else {
                cmd = "airodump-ng " + wlan + "mon -w /sdcard/YourDemon/hs/handshake --ignore-negative-one --output-format pcap --bssid " + bssid;
            }
            stdin.write((exec + "'" + cmd + "'" + '\n').getBytes());
            stdin.write(("exit\n").getBytes());
            stdin.flush();
            stdin.close();
            BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
            while ((line = br.readLine()) != null) {
                Log.e("e", line);
            }
            br.close();
            br = new BufferedReader(new InputStreamReader(stderr));
            br.close();
            air.waitFor();
            air.destroy();
            if (air.exitValue() != 0) {
                issuccess = new ArrayList<>();
                issuccess.add("error");
            }
        } catch (IOException e) {
            Log.d("Debug: ", "An IOException was caught: " + e.getMessage());
        } catch (InterruptedException ex) {
            Log.d("Debug: ", "An InterruptedException was caught: " + ex.getMessage());
        }
        if (issuccess.size() == 0) {
            issuccess.add("false");
        }
        return issuccess;
    }

    public void kill() {
        air.destroy();
    }
}

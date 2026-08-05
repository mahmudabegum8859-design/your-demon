package com.opx.yourdemon.local_network.utils;


import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.View;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.opx.yourdemon.custom.Device;
import com.opx.yourdemon.utils.Core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScanLocalNetwork {

    public static ArrayList<Device> execute(String ip, Context context, LinearProgressIndicator progress, Activity activity, Consumer<String> progressCallback) {
        String exec = Core.EXECUTE;
        String line;
        ArrayList<Device> d = new ArrayList<>();
        try {

            Process process = Runtime.getRuntime().exec("su");
            OutputStream stdin = process.getOutputStream();
            InputStream stderr = process.getErrorStream();
            InputStream stdout = process.getInputStream();
            stdin.write((exec + "'nmap " + ip + " -sP -n --stats-every 1s'&&echo LOCALSCANFINISHED" + '\n').getBytes());
            stdin.flush();
            stdin.close();
            ArrayList<String> nmapoutput = new ArrayList<>();
            ArrayList<String> outerror = new ArrayList<>();
            BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
            while ((line = br.readLine()) != null) {
                nmapoutput.add(line);
                if (progressCallback != null) {
                    progressCallback.accept(line);
                }
                if (line.contains("LOCALSCANFINISHED")) {
                    d = localdevices(nmapoutput);
                }
            }
            br.close();
            br = new BufferedReader(new InputStreamReader(stderr));
            while ((line = br.readLine()) != null) {
                outerror.add(line);
            }

            br.close();
            process.waitFor();
            process.destroy();
        } catch (IOException | InterruptedException e) {
            Log.d("Debug: ", "An IOException was caught: " + e.getMessage());
        }
        return d;
    }

    private static ArrayList<Device> localdevices(ArrayList<String> output) throws IOException {
        ArrayList<Device> result = new ArrayList<>();
        Device device = new Device();
        for (int i = 0; i < output.size(); i++) {
            String temp = output.get(i).replaceAll("\\s+", " ").replace("*", "");
            if (temp.contains("Nmap scan report for ")) {
                device.setIp(temp.replace("Nmap scan report for ", ""));
            } else if (temp.contains("MAC Address")) {
                Matcher mac = Pattern.compile("((\\w{2}:){5}\\w{2})").matcher(temp);
                if (mac.find()) {
                    device.setMac(mac.group(0).toUpperCase(Locale.ROOT));
                }
                String vendor = temp.replace("MAC Address: ", "").replace(mac + " ", "").replace("(", "").replace(")", "").replace(mac.group() + " ", "");
                device.setVendor(vendor);
                result.add(device);
                device = new Device();
            }
        }
        return result;
    }
}

package com.opx.yourdemon.wifi.utils;


import static android.content.ContentValues.TAG;

import android.util.Log;

import com.opx.yourdemon.utils.Core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;

public class GetInterfaces {

    public static ArrayList<String> get(Core core) {
        String line;
        ArrayList<String> inter = new ArrayList<>();
        String exec = Core.EXECUTE;
        try {
            Process process = Runtime.getRuntime().exec("su");
            OutputStream stdin = process.getOutputStream();
            InputStream stderr = process.getErrorStream();
            InputStream stdout = process.getInputStream();
            String cmd = "airmon-ng";
            stdin.write((exec + "'" + cmd + "'" + " |grep phy &&echo SCANFINISHED" + '\n').getBytes());
            stdin.write(("y\n").getBytes());
            stdin.write(("exit\n").getBytes());
            stdin.flush();
            ArrayList<String> out = new ArrayList<>();
            ArrayList<String> outerror = new ArrayList<>();
            BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
            while ((line = br.readLine()) != null) {
                if (line.contains("assign")) {
                    stdin.write(("y\n").getBytes());
                    stdin.flush();
                }
                out.add(line);
                if (!line.equals("SCANFINISHED")) {
                    String[] temp = line.trim().replaceAll("\\s+", " ").split(" ");
                    inter.add(temp[1]);
                }
            }
            stdin.close();
            br.close();
            br = new BufferedReader(new InputStreamReader(stderr));
            while ((line = br.readLine()) != null) {
                if (!line.contains("or not found")) {
                    outerror.add(line);
                }
            }
            core.writetolog(out, false);
            core.writetolog(outerror, true);
            br.close();
            process.waitFor();
            process.destroy();
        } catch (IOException e) {
            Log.d("Debug: ", "An IOException was caught: " + e.getMessage());
        } catch (InterruptedException ex) {
            Log.d("Debug: ", "An InterruptedException was caught: " + ex.getMessage());
        }
        return inter;
    }
}

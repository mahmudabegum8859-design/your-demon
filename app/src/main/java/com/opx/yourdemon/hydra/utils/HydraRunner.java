package com.opx.yourdemon.hydra.utils;


import android.util.Log;

import com.opx.yourdemon.utils.Core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;

public class HydraRunner {

    public static ArrayList<String> execute(String target, String proto, String port, String user, String pass, String threads, Core core) {
        String exec = Core.EXECUTE;
        String line;
        ArrayList<String> result = new ArrayList<>();
        try {
            Process process = Runtime.getRuntime().exec("su");
            OutputStream stdin = process.getOutputStream();
            InputStream stderr = process.getErrorStream();
            InputStream stdout = process.getInputStream();

            StringBuilder cmdBuilder = new StringBuilder();
            cmdBuilder.append("hydra -l ").append(user).append(" -p ").append(pass);
            if (!port.isEmpty()) {
                cmdBuilder.append(" -s ").append(port);
            }
            if (!threads.isEmpty()) {
                cmdBuilder.append(" -t ").append(threads);
            } else {
                cmdBuilder.append(" -t 4");
            }
            cmdBuilder.append(" ").append(target).append(" ").append(proto);

            stdin.write((exec + "'" + cmdBuilder.toString() + "'" + '\n').getBytes());
            stdin.flush();
            stdin.close();

            ArrayList<String> out = new ArrayList<>();
            ArrayList<String> outerror = new ArrayList<>();
            BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
            while ((line = br.readLine()) != null) {
                out.add(line);
            }
            br.close();
            br = new BufferedReader(new InputStreamReader(stderr));
            while ((line = br.readLine()) != null) {
                outerror.add(line);
            }
            br.close();
            process.waitFor();
            process.destroy();
            result = out;
        } catch (IOException | InterruptedException e) {
            Log.d("HydraRunner", "An IOException was caught: " + e.getMessage());
        }
        return result;
    }
}

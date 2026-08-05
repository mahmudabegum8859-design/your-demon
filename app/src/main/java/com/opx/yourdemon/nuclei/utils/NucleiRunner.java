package com.opx.yourdemon.nuclei.utils;


import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import com.opx.yourdemon.utils.Core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.function.Consumer;

public class NucleiRunner {

    public static ArrayList<String> execute(String target, String severity, String threads, Context context, Activity activity, TextView output, Consumer<String> progressCallback) {
        String exec = Core.EXECUTE;
        String line;
        ArrayList<String> result = new ArrayList<>();
        try {
            Process process = Runtime.getRuntime().exec("su");
            OutputStream stdin = process.getOutputStream();
            InputStream stderr = process.getErrorStream();
            InputStream stdout = process.getInputStream();
            stdin.write((exec + "'nuclei -u " + target + " -severity " + severity + " -t " + threads + "'\n").getBytes());
            stdin.flush();
            stdin.close();
            ArrayList<String> out = new ArrayList<>();
            ArrayList<String> outerror = new ArrayList<>();
            BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
            while ((line = br.readLine()) != null) {
                out.add(line);
                if (progressCallback != null) {
                    progressCallback.accept(line);
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
            result = out;
        } catch (IOException e) {
            Log.d("Debug: ", "An IOException was caught: " + e.getMessage());
        } catch (InterruptedException ex) {
            Log.d("Debug: ", "An InterruptedException was caught: " + ex.getMessage());
        }

        return result;
    }
}


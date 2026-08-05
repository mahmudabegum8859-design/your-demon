package com.opx.yourdemon.coremanger.utils;


import static android.content.ContentValues.TAG;

import android.util.Log;

import com.opx.yourdemon.utils.Core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;

public class InstallPipPackage {
    public static String exec = Core.EXECUTE;

    public static Boolean execute(String pkgname, Core core) {
        String line;
        ArrayList<String> out2 = new ArrayList<>();
        boolean p = false;
        try {
            Process process = Runtime.getRuntime().exec("su");
            OutputStream stdin = process.getOutputStream();
            InputStream stderr = process.getErrorStream();
            InputStream stdout = process.getInputStream();
            stdin.write((exec + "'pip install " + pkgname + "'" + '\n').getBytes());
            stdin.flush();
            stdin.close();

            ArrayList<String> outerror = new ArrayList<>();
            BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
            while ((line = br.readLine()) != null) {
                out2.add(line);
            }
            br.close();
            br = new BufferedReader(new InputStreamReader(stderr));
            while ((line = br.readLine()) != null) {
                outerror.add(line);
            }
            br.close();
            process.waitFor();
            process.destroy();
            p = process.exitValue() == 0;
        } catch (IOException | InterruptedException e) {
            Log.d("Debug: ", "An IOException was caught: " + e.getMessage());
        }

        return p;
    }
}

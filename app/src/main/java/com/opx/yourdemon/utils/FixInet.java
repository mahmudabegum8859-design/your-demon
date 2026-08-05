package com.opx.yourdemon.utils;


import static android.content.ContentValues.TAG;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class FixInet {

    public static boolean execute(Core core) {
        String line;
        boolean result = false;
        try {
            Process process = Runtime.getRuntime().exec("su");
            OutputStream stdin = process.getOutputStream();
            InputStream stderr = process.getErrorStream();
            InputStream stdout = process.getInputStream();
            stdin.write(("groupadd -g 3003 inet" + '\n').getBytes());
            stdin.write(("usermod -g 3003" + '\n').getBytes());
            stdin.write(("busybox chown 3003:3003 /data/data/com.opx.yourdemon" + '\n').getBytes());
            stdin.write(("setprop net.dns1 8.8.8.8" + '\n').getBytes());
            stdin.write(("setprop net.dns2 8.8.4.4" + '\n').getBytes());
            stdin.write(("exit\n").getBytes());
            stdin.flush();
            stdin.close();
            BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
            while ((line = br.readLine()) != null) {
                Log.d("Line: ", line);
            }
            br.close();
            br = new BufferedReader(new InputStreamReader(stderr));
            while ((line = br.readLine()) != null) {
                Log.d("Error: ", line);
            }
            br.close();
            process.waitFor();
            process.destroy();
            if (process.exitValue() == 0) {
                result = true;
            }
        } catch (IOException e) {
            Log.d("Debug: ", "An IOException was caught: " + e.getMessage());
        } catch (InterruptedException ex) {
            Log.d("Debug: ", "An InterruptedException was caught: " + ex.getMessage());
        }
        return result;
    }
}

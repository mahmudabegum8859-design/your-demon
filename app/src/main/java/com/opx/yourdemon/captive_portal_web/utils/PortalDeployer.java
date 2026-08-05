package com.opx.yourdemon.captive_portal_web.utils;


import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class PortalDeployer {

    public static Boolean execute(String html) {
        try {
            String escaped = html
                    .replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("$", "\\$")
                    .replace("`", "\\`");

            String cmd = "mkdir -p /storage/emulated/0/YourDemon/portal && echo \"" + escaped + "\" > /storage/emulated/0/YourDemon/portal/index.html";

            Process process = Runtime.getRuntime().exec("su");
            OutputStream stdin = process.getOutputStream();
            BufferedReader stdout = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader stderr = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            stdin.write((cmd + "\n").getBytes());
            stdin.write("exit\n".getBytes());
            stdin.flush();
            stdin.close();

            StringBuilder out = new StringBuilder();
            String line;
            while ((line = stdout.readLine()) != null) {
                out.append(line).append("\n");
            }
            stdout.close();

            StringBuilder err = new StringBuilder();
            while ((line = stderr.readLine()) != null) {
                err.append(line).append("\n");
            }
            stderr.close();

            process.waitFor();
            int exit = process.exitValue();
            process.destroy();

            if (exit != 0) {
                Log.e("PortalDeployer", "Deploy failed: " + err);
            }
            return exit == 0;
        } catch (IOException | InterruptedException e) {
            Log.e("PortalDeployer", "Exception: " + e.getMessage());
            return false;
        }
    }
}

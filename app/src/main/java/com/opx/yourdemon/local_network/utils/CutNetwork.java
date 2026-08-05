package com.opx.yourdemon.local_network.utils;


import android.util.Log;

import com.opx.yourdemon.utils.Core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;

public class CutNetwork {

    public Core core;
    public String target;
    public String gateway;
    public Process process;
    public int type;

    public CutNetwork(Core c, String t, String gw, int ty) {
        core = c;
        target = t;
        gateway = gw;
        type = ty;
    }

    public static void execute(CutNetwork instance) {
        String line;
        try {
            instance.process = Runtime.getRuntime().exec("su");
            OutputStream stdin = instance.process.getOutputStream();
            InputStream stderr = instance.process.getErrorStream();
            InputStream stdout = instance.process.getInputStream();
            if (instance.type == 0) {
                stdin.write((Core.EXECUTE + " 'python3 /CORE/MegaCut/megacut.py " + instance.target + " " + instance.gateway + " -k'" + '\n').getBytes());
            } else if (instance.type == 1) {
                stdin.write((Core.EXECUTE + " 'python3 /CORE/MegaCut/megacut.py " + instance.target + " " + instance.gateway + " -m'" + '\n').getBytes());
            } else if (instance.type == 2) {
                stdin.write((Core.EXECUTE + " 'python3 /CORE/MegaCut/megacut.py " + instance.target + " " + instance.gateway + " -b'" + '\n').getBytes());
            } else if (instance.type == 3) {
                stdin.write((Core.EXECUTE + " 'python3 /CORE/MegaCut/megacut.py " + instance.target + " " + instance.gateway + " -r'" + '\n').getBytes());
            }
            stdin.write(("exit\n").getBytes());
            stdin.flush();
            stdin.close();
            ArrayList<String> outerror = new ArrayList<>();
            BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
            br.close();
            br = new BufferedReader(new InputStreamReader(stderr));
            while ((line = br.readLine()) != null) {
                outerror.add(line);
            }
            br.close();
            instance.process.waitFor();
            instance.process.destroy();
        } catch (IOException e) {
            Log.d("Debug: ", "An IOException was caught: " + e.getMessage());
        } catch (InterruptedException ex) {
            Log.d("Debug: ", "An InterruptedException was caught: " + ex.getMessage());
        }
    }

    public void kill() {
        if (process != null) {
            process.destroy();
        }
    }
}

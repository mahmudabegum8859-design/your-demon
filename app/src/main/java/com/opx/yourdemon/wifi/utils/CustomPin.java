package com.opx.yourdemon.wifi.utils;


import android.app.Activity;
import android.util.Log;
import android.widget.TextView;

import com.opx.yourdemon.custom.WiFINetwork;
import com.opx.yourdemon.utils.Core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.function.Consumer;

public class CustomPin {
    public static String exec = Core.EXECUTE;

    public static WiFINetwork execute(String pin, Activity activity, TextView per, String bssid, String wlan, Core c, Consumer<String> progressCallback) {
        String line;
        WiFINetwork result = new WiFINetwork();
        boolean canceled = false;

        try {
            Process process = Runtime.getRuntime().exec("su");
            OutputStream stdin = process.getOutputStream();
            InputStream stderr = process.getErrorStream();
            InputStream stdout = process.getInputStream();
            String cmd = "python3 -u /CORE/PixieWps/pixie.py -i " + wlan + " --iface-down -p " + pin + " -b " + bssid;
            if (c.getBoolean("pixie_off")) {
                cmd = "python3 -u /CORE/PixieWps/pixie.py -i " + wlan + " -p " + pin + "-b " + bssid;
                stdin.write(("" + exec + "'" + cmd + "'" + " &&echo PINFINISHED" + '\n').getBytes());
            } else {
                stdin.write(("svc wifi disable&&sleep 2&&" + exec + "'" + cmd + "'" + " &&echo PINFINISHED&&sleep 2&&svc wifi enable" + '\n').getBytes());
            }
            stdin.write(("exit\n").getBytes());
            stdin.flush();
            stdin.close();

            ArrayList<String> out = new ArrayList<>();
            ArrayList<String> outerror = new ArrayList<>();
            BufferedReader br = new BufferedReader(new InputStreamReader(stdout));

            while ((line = br.readLine()) != null) {
                out.add(line);
                if (line.contains("PINFINISHED")) {
                    result = issuccess(out);
                }
            }
            br.close();
            br = new BufferedReader(new InputStreamReader(stderr));
            while ((line = br.readLine()) != null) {
                if (progressCallback != null) progressCallback.accept(line);
                outerror.add(line);
            }
            br.close();
            process.waitFor();
            process.destroy();
        } catch (IOException e) {
            Log.d("Debug: ", "An IOException was caught: " + e.getMessage());
        } catch (InterruptedException ex) {
            Log.d("Debug: ", "An InterruptedException was caught: " + ex.getMessage());
        }
        if (canceled) {
            result.setCanceled(true);
        }
        return result;
    }

    public static WiFINetwork issuccess(ArrayList<String> out) {
        String pin;
        String pass;
        WiFINetwork back = new WiFINetwork();
        for (int i = 0; i < out.size(); i++) {
            String s = out.get(i);
            if (s.contains("[+] WPS PIN:")) {
                pin = s.replace("[+] WPS PIN: ", "").replaceAll("'", "");
                back.setPin(pin);
                back.setOK(true);
            } else if (s.contains("[+] WPA PSK:")) {
                pass = s.replace("[+] WPA PSK: ", "").replaceAll("'", "");
                back.setPsk(pass);
                back.setOK(true);
            }
        }
        if (out.isEmpty()) {
            back.setCanceled(true);
        }
        return back;
    }
}

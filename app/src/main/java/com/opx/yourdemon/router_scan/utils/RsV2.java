package com.opx.yourdemon.router_scan.utils;


import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import com.opx.yourdemon.custom.Router;
import com.opx.yourdemon.utils.Core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RsV2 {

    public Activity mActivity;
    public Process proc;
    public TextView textprg;

    public RsV2(Activity activity, TextView text) {
        mActivity = activity;
        textprg = text;
    }

    public static Router execute(RsV2 instance, String ip, Core core) {
        String exec = Core.EXECUTE;
        String line;
        Router r = new Router();

        try {
            instance.proc = Runtime.getRuntime().exec("su");
            OutputStream stdin = instance.proc.getOutputStream();
            InputStream stderr = instance.proc.getErrorStream();
            InputStream stdout = instance.proc.getInputStream();
            String cmd = "rs " + ip +" /sdcard/YourDemon/rs/auth_basic.txt /sdcard/YourDemon/rs/auth_digest.txt /sdcard/YourDemon/rs/auth_form.txt";
            stdin.write((exec + "'" + cmd + "'" + '\n').getBytes());
            stdin.write(("exit\n").getBytes());
            stdin.flush();
            stdin.close();
            ArrayList<String> out2 = new ArrayList<>();
            ArrayList<String> outerror = new ArrayList<>();
            BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
            while ((line = br.readLine()) != null) {
                out2.add(line);
                handleProgress(instance, line);
            }
            br.close();
            br = new BufferedReader(new InputStreamReader(stderr));
            while ((line = br.readLine()) != null) {
                outerror.add(line);
                handleProgress(instance, line);
            }

            br.close();
            instance.proc.waitFor();
            instance.proc.destroy();
            r = rs_result(out2);

        } catch (IOException e) {
            Log.d("Debug: ", "An IOException was caught: " + e.getMessage());
        } catch (InterruptedException ex) {
            Log.d("Debug: ", "An InterruptedException was caught: " + ex.getMessage());
        }

        return r;
    }

    public void kill() {
        if (proc != null) {
            proc.destroy();
        }
    }

    private static void handleProgress(RsV2 instance, String line) {
        if (line.contains("log in")) {
            Matcher m = Pattern.compile("[0-9]+").matcher(line);
            String percent = "";
            if (m.find()) {
                percent = m.group();
                instance.textprg.setText("Bruting... (" + percent + "%)");
            }
        } else if (line.contains("Status")) {
            instance.textprg.setText(line.replace("Status: ",""));
        }
    }

    private static Router rs_result(ArrayList<String> output) {
        Router result = new Router();
        result.setSuccess(false);

        for (int i = 0; i < output.size(); i++) {
            String temp = output.get(i);
            if (temp.contains("SSID:") && !temp.contains("BSSID:")) {
                String ssid = temp.replace("SSID: ", "");
                result.setSsid(ssid);
                result.setSuccess(true);
            } else if (temp.contains("Auth:")) {
                String auth = temp.replace("Auth: ", "");
                result.setAuth(auth);
                result.setSuccess(true);
            } else if (temp.contains("Key:")) {
                String pswd = temp.replace("Key: ", "");
                result.setPsk(pswd);
                result.setSuccess(true);
            } else if (temp.contains("WPS:")) {
                String wps = temp.replace("WPS: ", "");
                result.setWps(wps);
                result.setSuccess(true);
            } else if (temp.contains("Title:")) {
                String title = temp.replace("Title: ", "");
                result.setTitle(title);
            } else if (temp.contains("BSSID:")){
                String mac = temp.replace("BSSID: ","");
                result.setBssid(mac);
            }
            if (result.getSuccess()) {
                result.setStatus("Success");
                result.setType(1);
            }
        }
        return result;
    }
}


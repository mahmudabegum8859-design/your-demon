package com.opx.yourdemon.geomac.utils;


import android.util.Log;

import com.opx.yourdemon.utils.Core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GetGeoByMac {

    public static String execute(String mac, Core core) {
        String line;
        String result = "";

        try {

            Process process = Runtime.getRuntime().exec("su");
            OutputStream stdin = process.getOutputStream();
            InputStream stderr = process.getErrorStream();
            InputStream stdout = process.getInputStream();
            stdin.write((Core.EXECUTE+ "'./modules/GeoMac/geomac "+ mac + "'" + '\n').getBytes());
            stdin.write(("exit\n").getBytes());
            stdin.flush();
            stdin.close();

            BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
            while ((line = br.readLine()) != null) {
                core.writelinetolog(line);
                Matcher coords = Pattern.compile("[0-9]*\\.[0-9]+,\\s[0-9]*\\.[0-9]+").matcher(line);
                Matcher coords1 = Pattern.compile("-[0-9]*\\.[0-9]+,\\s[0-9]*\\.[0-9]+").matcher(line);
                Matcher coords2 = Pattern.compile("-[0-9]*\\.[0-9]+,\\s-[0-9]*\\.[0-9]+").matcher(line);
                Matcher coords3 = Pattern.compile("[0-9]*\\.[0-9]+,\\s-[0-9]*\\.[0-9]+").matcher(line);
                if (coords.find()){ result = coords.group();break; }
                if (coords2.find()){ result = coords2.group();break; }
                if (coords1.find()){ result = coords1.group();break; }
                if (coords3.find()){ result = coords3.group();break; }
            }
            br.close();
            br = new BufferedReader(new InputStreamReader(stderr));
            while ((line = br.readLine()) != null) {
            }

            br.close();
            process.waitFor();
            process.destroy();
        } catch (IOException e) {
            Log.d("Debug: ", "An IOException was caught: " + e.getMessage());
        } catch (InterruptedException ex) {
            Log.d("Debug: ", "An InterruptedException was caught: " + ex.getMessage());
        }

        return result;
    }
}

package com.opx.yourdemon.coremanger.utils;


import static android.content.ContentValues.TAG;

import android.util.Log;

import com.opx.yourdemon.custom.Package;
import com.opx.yourdemon.utils.Core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GetPackage {
    public static String exec = Core.EXECUTE;

    public static ArrayList<Package> execute(String query, Core core) {
        String line;
        ArrayList<String> out2 = new ArrayList<>();
        try {
            Process process = Runtime.getRuntime().exec("su");
            OutputStream stdin = process.getOutputStream();
            InputStream stderr = process.getErrorStream();
            InputStream stdout = process.getInputStream();
            stdin.write((exec + "'apk search " + query + "'" + '\n').getBytes());
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
        } catch (IOException | InterruptedException e) {
            Log.d("Debug: ", "An IOException was caught: " + e.getMessage());
        }

        return parse(out2);
    }

    public static ArrayList<Package> parse(ArrayList<String> out) {
        ArrayList<Package> res = new ArrayList<>();
        for (String pkg : out){
            Package temp = new Package();
            Matcher r = Pattern.compile("-r[0-9]+").matcher(pkg);
            if (r.find()){
                pkg = pkg.replace(r.group(),"");
            }
            String version = pkg.split("-")[pkg.split("-").length-1];
            temp.setVersion(version);
            temp.setName(pkg.replace("-"+version,""));
            res.add(temp);
        }
        return res;
    }
}

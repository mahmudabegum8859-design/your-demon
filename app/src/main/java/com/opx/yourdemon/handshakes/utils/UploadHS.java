package com.opx.yourdemon.handshakes.utils;


import android.content.Context;
import android.util.Log;

import com.opx.yourdemon.utils.Core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;

public class UploadHS {

    public static Integer execute(String path, String email, Context context) {
        String exec = Core.EXECUTE;
        String line;
        Integer state = 0;
        try {
            Process process = Runtime.getRuntime().exec("su");
            OutputStream stdin = process.getOutputStream();
            InputStream stderr = process.getErrorStream();
            InputStream stdout = process.getInputStream();
            stdin.write((exec + "'curl -X POST -F 'email="+email+"' -F 'file=@"+path+"' https://api.onlinehashcrack.com'&&echo UPLOADFINISHED" + '\n').getBytes());
            stdin.flush();
            stdin.close();
            ArrayList<String> hsoutput = new ArrayList<>();
            ArrayList<String> outerror = new ArrayList<>();
            BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
            while ((line = br.readLine()) != null) {
                hsoutput.add(line);
                if (line.contains("UPLOADFINISHED")) {
                    state = checkhs(hsoutput);
                    break;
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
        return state;
    }

    private static Integer checkhs(ArrayList<String> output){
        Integer res = 0;
        for (String line : output){
            if (line.contains("successfully added")){
                res = 2;
            }else if (line.contains("has been already sent")){
                res = 1;
            }
        }
        return res;
    }
}

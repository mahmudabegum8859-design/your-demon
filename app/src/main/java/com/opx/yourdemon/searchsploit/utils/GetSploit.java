package com.opx.yourdemon.searchsploit.utils;


import android.util.Log;

import com.opx.yourdemon.custom.Sploit;
import com.opx.yourdemon.utils.Core;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;

public class GetSploit {

    public static ArrayList<Sploit> execute(String query, Core core) {
        String exec = Core.EXECUTE;
        String line;
        StringBuilder json = new StringBuilder();
        try {
            Process process = Runtime.getRuntime().exec("su");
            OutputStream stdin = process.getOutputStream();
            InputStream stderr = process.getErrorStream();
            InputStream stdout = process.getInputStream();
            stdin.write((exec + "'/modules/Searchsploit/exploitdb/searchsploit " + query + "  --json'" + '\n').getBytes());
            stdin.flush();
            stdin.close();
            ArrayList<String> out2 = new ArrayList<>();
            ArrayList<String> outerror = new ArrayList<>();
            BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
            while ((line = br.readLine()) != null) {
                out2.add(line);
                json.append(line).append("\n");
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

        return parse(json.toString());
    }

    private static ArrayList<Sploit> parse(String out) {
        ArrayList<Sploit> res = new ArrayList<>();
        try {
            JSONObject all = new JSONObject(out);
            JSONArray exploits = all.getJSONArray("RESULTS_EXPLOIT");
            for (int i = 0; i < exploits.length(); i++) {
                JSONObject exploit = exploits.getJSONObject(i);
                Sploit temp = new Sploit();
                temp.setTitle(exploit.getString("Title"));
                temp.setDate(exploit.getString("Date"));
                temp.setAuthor(exploit.getString("Author"));
                temp.setType(exploit.getString("Type"));
                temp.setPlatform(exploit.getString("Platform"));
                temp.setPath(exploit.getString("Path"));
                res.add(temp);
            }
            return res;
        } catch (JSONException e) {
            e.printStackTrace();
            return res;
        }
    }
}


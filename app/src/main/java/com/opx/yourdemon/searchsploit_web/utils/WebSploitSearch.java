package com.opx.yourdemon.searchsploit_web.utils;


import android.util.Log;

import com.opx.yourdemon.custom.Sploit;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class WebSploitSearch {

    public static ArrayList<Sploit> execute(String query) {
        ArrayList<Sploit> results = new ArrayList<>();
        try {
            String csvUrl = "https://gitlab.com/exploit-database/exploitdb/-/raw/main/files_exploits.csv";
            URL url = new URL(csvUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(15000);
            conn.setRequestProperty("User-Agent", "YourDemon");

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }
                Sploit s = parseCsvLine(query, line);
                if (s != null) {
                    results.add(s);
                }
            }
            reader.close();
            conn.disconnect();
        } catch (Exception e) {
            Log.d("WebSploitSearch", "Error: " + e.getMessage());
        }
        return results;
    }

    private static Sploit parseCsvLine(String query, String line) {
        try {
            ArrayList<String> fields = new ArrayList<>();
            boolean inQuotes = false;
            StringBuilder field = new StringBuilder();
            for (int i = 0; i < line.length(); i++) {
                char c = line.charAt(i);
                if (c == '"') {
                    inQuotes = !inQuotes;
                } else if (c == ',' && !inQuotes) {
                    fields.add(field.toString().trim());
                    field = new StringBuilder();
                } else {
                    field.append(c);
                }
            }
            fields.add(field.toString().trim());

            if (fields.size() >= 7) {
                Sploit s = new Sploit();
                s.setTitle(fields.get(2));
                s.setDate(fields.get(3));
                s.setAuthor(fields.get(4));
                s.setType(fields.get(5));
                s.setPlatform(fields.get(6));
                s.setPath(fields.get(1));
                if (matchesQuery(query, s)) {
                    return s;
                }
            }
        } catch (Exception e) {
            Log.d("WebSploitSearch", "Parse error: " + e.getMessage());
        }
        return null;
    }

    private static boolean matchesQuery(String query, Sploit s) {
        if (query == null || query.isEmpty()) return true;
        String q = query.toLowerCase();
        return (s.getTitle() != null && s.getTitle().toLowerCase().contains(q))
                || (s.getAuthor() != null && s.getAuthor().toLowerCase().contains(q))
                || (s.getPlatform() != null && s.getPlatform().toLowerCase().contains(q))
                || (s.getType() != null && s.getType().toLowerCase().contains(q));
    }
}

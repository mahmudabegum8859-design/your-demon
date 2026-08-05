package com.opx.yourdemon.utils;


import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

public class CheckMsg {

    public static String check() {
        try {
            Document doc = Jsoup.connect("https://raw.githubusercontent.com/OP-AMINUL-FF/your-demon-updater/main/msg.txt").get();
            return doc.text();
        } catch (IOException e) {
            Log.d("Debug: ", "An IOException was caught: " + e.getMessage());
            return "";
        }
    }
}

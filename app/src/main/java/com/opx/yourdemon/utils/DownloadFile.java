package com.opx.yourdemon.utils;


import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadFile {

    public static boolean download(Context context, String urlDownload, String filename) {
        Logger logger = new Logger();
        logger.writeLine("Downloading file.." + urlDownload,1);

        File dir = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath());
        dir.mkdirs();
        File outputFile = new File(dir, filename);

        int maxRetries = 3;
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(urlDownload).openConnection();
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(30000);
                connection.setInstanceFollowRedirects(true);
                connection.connect();

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    if (attempt < maxRetries) {
                        Thread.sleep(1000L * attempt);
                    }
                    continue;
                }

                int contentLength = connection.getContentLength();
                InputStream input = connection.getInputStream();
                FileOutputStream output = new FileOutputStream(outputFile);

                byte[] buffer = new byte[8192];
                int bytesRead;
                long totalBytes = 0;

                while ((bytesRead = input.read(buffer)) != -1) {
                    output.write(buffer, 0, bytesRead);
                    totalBytes += bytesRead;
                }

                output.flush();
                output.close();
                input.close();
                connection.disconnect();

                return outputFile.exists() && outputFile.length() > 0;
            } catch (Exception e) {
                logger.writeLine("Download attempt " + attempt + " failed: " + e.getMessage(), 1);
                if (attempt < maxRetries) {
                    try { Thread.sleep(1000L * attempt); } catch (InterruptedException ignored) {}
                }
            }
        }
        return false;
    }
}

package com.opx.yourdemon.logviewer;


import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class LogCaptureManager {
    private static final String TAG = "LogCapture";
    private static LogCaptureManager instance;
    private final List<LogEntry> logs = new ArrayList<>();
    private final List<LogListener> listeners = new ArrayList<>();
    private Thread captureThread;
    private boolean running = false;
    private int logCount = 0;
    private static final int MAX_ENTRIES = 10000;
    private final HashSet<String> recentLines = new HashSet<>();
    private String lastError = null;

    public static class LogEntry {
        public char level;
        public String tag;
        public String fullLine;

        LogEntry(char level, String tag, String fullLine) {
            this.level = level;
            this.tag = tag;
            this.fullLine = fullLine;
        }
    }

    public interface LogListener {
        void onNewLog(LogEntry entry);
        void onLogCleared();
        void onError(String error);
        void onStatus(String status);
    }

    private LogCaptureManager() {}

    public static synchronized LogCaptureManager getInstance() {
        if (instance == null) {
            instance = new LogCaptureManager();
        }
        return instance;
    }

    public synchronized void start() {
        if (running) return;
        running = true;
        lastError = null;
        captureThread = new Thread(this::runCapture);
        captureThread.setDaemon(true);
        captureThread.start();
    }

    private void runCapture() {
        // Step 1: Try continuous logcat mode (works on userdebug/eng)
        notifyStatus("Starting logcat...");
        if (tryContinuousMode()) {
            return; // continuous mode exited but captured enough lines
        }

        // Step 2: Polling fallback
        notifyStatus("Poll mode...");
        pollLogs();
    }

    private boolean tryContinuousMode() {
        Process process = null;
        try {
            ProcessBuilder pb = new ProcessBuilder("logcat", "-v", "brief", "*:V");
            pb.redirectErrorStream(true);
            process = pb.start();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
            long startTime = System.currentTimeMillis();
            int lineCount = 0;

            String line;
            while (running) {
                if (reader.ready()) {
                    line = reader.readLine();
                    if (line == null) break;
                    lineCount++;
                    processLogLine(line);
                } else if (lineCount > 0 && System.currentTimeMillis() - startTime > 5000) {
                    // Got some lines and running for 5+ seconds - continuous mode works
                    Log.d(TAG, "Continuous mode stable at " + lineCount + " lines");
                    notifyStatus("Capturing");
                    // Continue in continuous mode
                    while (running && (line = reader.readLine()) != null) {
                        lineCount++;
                        processLogLine(line);
                    }
                    break;
                } else if (System.currentTimeMillis() - startTime > 3000 && lineCount == 0) {
                    Log.d(TAG, "No lines in 3s, giving up continuous mode");
                    break;
                } else {
                    Thread.sleep(50);
                }
            }
            Log.d(TAG, "Continuous mode ended, lines=" + lineCount);
            return lineCount > 5;
        } catch (Exception e) {
            notifyError("Continuous mode error: " + e.getMessage());
            return false;
        } finally {
            if (process != null) process.destroy();
        }
    }

    private void pollLogs() {
        while (running) {
            try {
                ProcessBuilder pb = new ProcessBuilder("logcat", "-d", "-v", "brief", "*:V");
                pb.redirectErrorStream(true);
                Process process = pb.start();
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()));
                String line;
                int lineCount = 0;
                while ((line = reader.readLine()) != null) {
                    lineCount++;
                    // Skip header lines
                    if (line.startsWith("---------")) continue;
                    processLogLine(line);
                }
                process.waitFor();

                if (lineCount == 0) {
                    Thread.sleep(2000);
                } else {
                    // Clear buffer so next dump gives fresh logs
                    try {
                        new ProcessBuilder("logcat", "-c").start().waitFor();
                    } catch (Exception ignored) {}
                    Thread.sleep(500);
                }

                // Trim dedup cache
                if (recentLines.size() > 2000) {
                    recentLines.clear();
                }
            } catch (Exception e) {
                notifyError("Poll error: " + e.getMessage());
                try { Thread.sleep(3000); } catch (InterruptedException ignored) {}
            }
        }
    }

    private void processLogLine(String line) {
        // Dedup check
        boolean isDuplicate;
        synchronized (recentLines) {
            isDuplicate = recentLines.contains(line);
            if (!isDuplicate) {
                recentLines.add(line);
                if (recentLines.size() > 2000) {
                    recentLines.clear();
                    recentLines.add(line);
                }
            }
        }
        if (isDuplicate) return;

        LogEntry entry = parseLog(line);
        if (entry != null) {
            synchronized (logs) {
                logs.add(entry);
                if (logs.size() > MAX_ENTRIES) {
                    logs.remove(0);
                }
                logCount++;
            }
            notifyListeners(entry);
        }
    }

    public synchronized void stop() {
        running = false;
        captureThread = null;
    }

    public void clear() {
        synchronized (logs) {
            logs.clear();
            logCount = 0;
        }
        recentLines.clear();
        notifyCleared();
        new Thread(() -> {
            try {
                new ProcessBuilder("logcat", "-c").start().waitFor();
            } catch (Exception ignored) {}
        }).start();
    }

    public int getLogCount() {
        return logCount;
    }

    public List<LogEntry> getLogs() {
        synchronized (logs) {
            return new ArrayList<>(logs);
        }
    }

    public String getLastError() {
        return lastError;
    }

    public synchronized void addListener(LogListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public synchronized void removeListener(LogListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners(LogEntry entry) {
        new Handler(Looper.getMainLooper()).post(() -> {
            synchronized (listeners) {
                for (LogListener l : listeners) {
                    l.onNewLog(entry);
                }
            }
        });
    }

    private void notifyCleared() {
        new Handler(Looper.getMainLooper()).post(() -> {
            synchronized (listeners) {
                for (LogListener l : listeners) {
                    l.onLogCleared();
                }
            }
        });
    }

    private void notifyError(String error) {
        lastError = error;
        Log.d(TAG, error);
        new Handler(Looper.getMainLooper()).post(() -> {
            synchronized (listeners) {
                for (LogListener l : listeners) {
                    l.onError(error);
                }
            }
        });
    }

    private void notifyStatus(String status) {
        Log.d(TAG, "Status: " + status);
        new Handler(Looper.getMainLooper()).post(() -> {
            synchronized (listeners) {
                for (LogListener l : listeners) {
                    l.onStatus(status);
                }
            }
        });
    }

    private LogEntry parseLog(String line) {
        if (line == null || line.trim().isEmpty()) return null;

        // brief format: P/tag(  pid): message
        // default format: mm-dd hh:mm:ss.mmm P/tag(pid): message
        // Both have P/ somewhere in the line
        String[] prefixes = {"V/", "D/", "I/", "W/", "E/", "F/"};
        char[] levels = {'V', 'D', 'I', 'W', 'E', 'F'};

        for (int i = 0; i < prefixes.length; i++) {
            String prefix = prefixes[i];
            // Try " P/" first (more specific - avoids matching things like "SERVICE/")
            int idx = line.indexOf(" " + prefix);
            if (idx >= 0) {
                idx++; // skip space
            } else if (line.startsWith(prefix)) {
                idx = 0;
            }

            if (idx >= 0) {
                char level = levels[i];
                String tag = "";
                int parenStart = line.indexOf("(", idx + 2);
                if (parenStart > idx + 2) {
                    tag = line.substring(idx + 2, parenStart);
                } else {
                    // No parens - try finding ": " after the tag
                    int colonStart = line.indexOf(": ", idx + 2);
                    if (colonStart > idx + 2) {
                        tag = line.substring(idx + 2, colonStart);
                    }
                }
                return new LogEntry(level, tag, line);
            }
        }
        return null;
    }
}
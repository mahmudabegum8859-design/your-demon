package com.opx.yourdemon.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Lightweight crash + event logger.
 * Writes everything to /storage/emulated/0/your-demon/ (.txt files).
 * The folder is created automatically on first use if it does not exist.
 * <p>
 * - app_log.txt   -> general app events (start, navigation, important actions)
 * - crash_log.txt -> full crash reports (stack trace + device info)
 */
public class LogUtils {

    private static final String TAG = "YourDemonLog";
    private static final String LOG_DIR = "your-demon";
    private static final String EVENT_FILE = "app_log.txt";
    private static final String CRASH_FILE = "crash_log.txt";

    private static Context appContext;
    private static File logDir;
    private static Thread.UncaughtExceptionHandler defaultHandler;

    /**
     * Call once from your launcher Activity (or Application) onCreate.
     * Creates the log folder if missing and installs the global crash handler.
     */
    public static synchronized void init(Context context) {
        if (appContext != null) return;
        appContext = context.getApplicationContext();
        logDir = ensureLogDir();

        defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            try {
                writeCrash(thread, throwable);
            } catch (Exception ignored) {
            }
            if (defaultHandler != null) {
                defaultHandler.uncaughtException(thread, throwable);
            } else {
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(10);
            }
        });

        log("SYSTEM", "App started — version " + getVersion()
                + " | " + Build.MANUFACTURER + " " + Build.MODEL + " (" + Build.DEVICE + ")"
                + " | Android " + Build.VERSION.RELEASE + " / API " + Build.VERSION.SDK_INT);
    }

    /**
     * Ensures /storage/emulated/0/your-demon/ exists (creates it if needed).
     */
    public static synchronized File ensureLogDir() {
        File dir = new File(Environment.getExternalStorageDirectory(), LOG_DIR);
        if (!dir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            dir.mkdirs();
        }
        return dir;
    }

    /**
     * Appends an event line to app_log.txt.
     */
    public static synchronized void log(String tag, String message) {
        append(EVENT_FILE, "[" + timestamp() + "] [" + tag + "] " + message);
    }

    /**
     * Writes a crash report for the current thread's throwable to crash_log.txt.
     */
    public static synchronized void logCrash(Throwable throwable) {
        writeCrash(Thread.currentThread(), throwable);
    }

    private static void writeCrash(Thread thread, Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        pw.println("================ CRASH ================");
        pw.println("Time    : " + timestamp());
        pw.println("Thread  : " + (thread != null ? thread.getName() : "unknown"));
        pw.println("Device  : " + Build.MANUFACTURER + " " + Build.MODEL + " (" + Build.DEVICE + ")");
        pw.println("Android : " + Build.VERSION.RELEASE + " / API " + Build.VERSION.SDK_INT);
        pw.println("App     : " + getVersion());
        pw.println("Error   : " + (throwable != null ? throwable.toString() : "null"));
        pw.println("Stack   :");
        if (throwable != null) {
            throwable.printStackTrace(pw);
        }
        pw.println("========================================");
        pw.flush();
        append(CRASH_FILE, sw.toString());
        Log.e(TAG, "Crash captured -> " + (logDir != null
                ? logDir.getAbsolutePath() + File.separator + CRASH_FILE : "?"), throwable);
    }

    private static void append(String fileName, String content) {
        try {
            if (logDir == null) {
                if (appContext == null) return;
                logDir = ensureLogDir();
            }
            File file = new File(logDir, fileName);
            FileWriter writer = new FileWriter(file, true);
            try {
                writer.write(content);
                writer.write(System.lineSeparator());
            } finally {
                writer.close();
            }
        } catch (Exception e) {
            Log.w(TAG, "Failed to write " + fileName + ": " + e.getMessage());
        }
    }

    private static String timestamp() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date());
    }

    private static String getVersion() {
        try {
            if (appContext != null) {
                PackageInfo info = appContext.getPackageManager()
                        .getPackageInfo(appContext.getPackageName(), 0);
                return info.versionName + " (" + info.versionCode + ")";
            }
        } catch (Exception ignored) {
        }
        return "?";
    }
}

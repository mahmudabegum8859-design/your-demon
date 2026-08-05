package com.opx.yourdemon;



import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.opx.yourdemon.utils.CheckFile;
import com.opx.yourdemon.utils.Core;
import com.opx.yourdemon.utils.CustomCommand;
import com.opx.yourdemon.utils.TaskRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
/**
 * This class is used to update the application
 */
public class Updater extends Fragment {


    public Context context;
    public Activity activity;
    public Core core;
    public String versionName = BuildConfig.VERSION_NAME;
    public int versionInt = BuildConfig.VERSION_CODE;
    public TextView status;
    public LinearProgressIndicator progress;

    public static boolean is64Bit() {
        return (Build.SUPPORTED_64_BIT_ABIS != null && Build.SUPPORTED_64_BIT_ABIS.length > 0);
    }

    public String urlapk;
    public String urlchroot32;
    public String urlchroot64;
    public MaterialButton renew;
    public Updater(String a,String c,String cc){
        urlapk  = a;
        urlchroot32 = c;
        urlchroot64 = cc;
    }
    @SuppressLint("Range")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View viewroot = inflater.inflate(R.layout.update, container, false);
        context = getContext();
        activity = getActivity();
        progress = viewroot.findViewById(R.id.prog_update);
        status = viewroot.findViewById(R.id.update_status);
        core = new Core(context);
         renew = viewroot.findViewById(R.id.welcome);
        renew.setOnClickListener(view -> core.installApplication(context,"/storage/emulated/0/Download/yourdemonupdate.apk"));
        if(core.is64Bit()){
        Thread f = new Thread(() -> updater(urlapk,urlchroot64));
        f.start();}else{
            Thread f = new Thread(() -> updater(urlapk,urlchroot32));
            f.start();
        }

        return viewroot;

    }

    public void setText(TextView textView, String text) {
        activity.runOnUiThread(() -> textView.setText(text));
    }

    public void setProg(LinearProgressIndicator progressIndicator, int prog) {
        activity.runOnUiThread(() -> {
            progressIndicator.setVisibility(View.INVISIBLE);
            progressIndicator.setIndeterminate(false);

            progressIndicator.setVisibility(View.VISIBLE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                progressIndicator.setProgress(prog, true);
            }
        });

    }

    @SuppressLint("Range")
    public void updater(String urlapk, String urlchroot) {

        setText(status, core.str("delcache"));
        TaskRunner.execute(() -> CustomCommand.execute("rm /storage/emulated/0/Download/yourdemonupdate.apk", core));
        TaskRunner.execute(() -> CustomCommand.execute("rm /storage/emulated/0/Download/yourdemon-chroot.tar.gz", core));
        try {
            setText(status, core.str("download_apk"));
            boolean apk = download(urlapk,"yourdemonupdate.apk",status,progress);
            if (apk) {
                setProg(progress, 10);
                setText(status, core.str("download_chroot"));
                boolean chroot = download(urlchroot,"yourdemon-chroot.tar.gz",status,progress);
                if (chroot) {
                    setProg(progress, 30);
                    setText(status, core.str("unmount"));
                    Boolean un = unmount();
                    setProg(progress, 40);
                    setText(status, core.str("delchroot"));
                    setProg(progress, 70);
                    boolean del = CustomCommand.execute("rm -rf /data/local/yourdemon", new Core(context));
                    if (del) {
                        if (CheckFile.check("/storage/emulated/0/Download/yourdemon-chroot.tar.gz")) {
                            setText(status, core.str("installchroot"));
                            setProg(progress, 90);
                            TaskRunner.execute(() -> CustomCommand.execute("chmod 777 -R /data/data/com.opx.yourdemon/files/", core));
                            TaskRunner.execute(() -> CustomCommand.execute("mkdir /storage/emulated/0/YourDemon", core));
                            TaskRunner.execute(() -> CustomCommand.execute("mkdir /data/local/yourdemon", core));
                            boolean untar = CustomCommand.execute("/data/data/com.opx.yourdemon/files/busybox tar -xf /storage/emulated/0/Download/yourdemon-chroot.tar.gz -C /data/local/yourdemon", core);
                            if (untar) {
                                setProg(progress, 100);
                                TaskRunner.execute(() -> CustomCommand.execute("rm /storage/emulated/0/Download/yourdemon-chroot.tar.gz", core));
                                core.installApplication(context,"/storage/emulated/0/Download/yourdemonupdate.apk");
                                setText(status, core.str("installapk"));
                                activity.runOnUiThread(() -> renew.setVisibility(View.VISIBLE));
                                activity.runOnUiThread(() -> core.toaster("Updated!"));

                            } else {
                                activity.runOnUiThread(() -> core.toaster("Failed!"));
                            }

                        }
                    }

                }else{
                    setProg(progress, 100);
                    setText(status, "Error Unmounting core! Reboot phone and try again!");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public boolean unmount() {
        return CustomCommand.execute("/data/data/com.opx.yourdemon/files/killroot", new Core(context));
    }
    @SuppressLint("Range")
    public Boolean download(String url, String name, TextView status, LinearProgressIndicator progress) {
        boolean ok = false;
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setDescription(url);
        request.setTitle(core.str("wait"));
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, name);
        final DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        final long downloadId = manager.enqueue(request);
        boolean downloading = true;
        long startTime = System.currentTimeMillis();
        long lastBytes = -1;
        int stallCount = 0;
        while (downloading) {
            DownloadManager.Query q = new DownloadManager.Query();
            q.setFilterById(downloadId);
            Cursor cursor = manager.query(q);
            cursor.moveToFirst();
            @SuppressLint("Range") int bytes_downloaded = cursor.getInt(cursor
                    .getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
            @SuppressLint("Range") int bytes_total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
            int statusCol = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
            if (bytes_total > 0) {
                final int dl_progress = (int) ((bytes_downloaded * 100L) / bytes_total);
                setText(status, bytes_downloaded / 1024 / 1024 + "MB/" + bytes_total / 1024 / 1024 + "MB (" + dl_progress + "%)", false);
                setProg(progress, dl_progress);
            } else {
                setText(status, bytes_downloaded / 1024 / 1024 + "MB downloaded...", false);
                progress.setIndeterminate(true);
            }
            if (statusCol == DownloadManager.STATUS_SUCCESSFUL) {
                downloading = false;
            } else if (statusCol == DownloadManager.STATUS_FAILED) {
                downloading = false;
            }
            if (lastBytes == bytes_downloaded) {
                stallCount++;
            } else {
                stallCount = 0;
            }
            lastBytes = bytes_downloaded;
            if (stallCount > 60 || System.currentTimeMillis() - startTime > 300000) {
                downloading = false;
            }
            cursor.close();
            if (downloading) {
                try { Thread.sleep(1000); } catch (InterruptedException e) { break; }
            }
        }
        File f = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath(), name);
        if(f.exists() && !f.isDirectory()) {
            ok = true;
        }
        return ok;

    }
    public void setText(TextView textView, String text, boolean animate) {
        activity.runOnUiThread(() -> {
            if (animate) {
                Animation fade = AnimationUtils.loadAnimation(context, R.anim.fade_in);
                textView.startAnimation(fade);
            }
            textView.setText(text);
        });
    }
}

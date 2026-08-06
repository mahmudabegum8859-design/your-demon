package com.opx.yourdemon.modules;



import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.opx.yourdemon.MainActivity;
import com.opx.yourdemon.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class InstalModuleActivity extends AppCompatActivity {

    public TextView logview;
    public ExtendedFloatingActionButton relaunch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instal_module);
        logview = findViewById(R.id.logview);
        relaunch = findViewById(R.id.relauch_button);
        showLog();
        relaunch.setOnClickListener(view -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    public void showLog() {
        new Thread(() -> {
            StringBuilder text = new StringBuilder();
            File dir = new File(Environment.getExternalStorageDirectory(), "YourDemon");
            File logfile = new File(dir, "log.txt");
            try {
                FileInputStream input = new FileInputStream(logfile);
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                String line;
                int count = 0;
                while ((line = reader.readLine()) != null) {
                    text.append(line).append("\n");
                    if (count > 400) {
                        text = new StringBuilder(text.substring(text.length() - 4000));
                        count = 0;
                    }
                    count++;
                }
                input.close();
            } catch (Exception ignored) {
            }
            String log = text.toString();
            runOnUiThread(() -> logview.setText(log));
        }).start();
    }
}

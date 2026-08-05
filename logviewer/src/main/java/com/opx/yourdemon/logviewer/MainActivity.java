package com.opx.yourdemon.logviewer;


import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements LogCaptureManager.LogListener {

    private RecyclerView rvLogs;
    private LogAdapter adapter;
    private TextView tvStatus;
    private final List<LogCaptureManager.LogEntry> logs = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rvLogs = findViewById(R.id.rvLogs);
        tvStatus = findViewById(R.id.tvStatus);
        Button btnClear = findViewById(R.id.btnClear);
        Button btnCopy = findViewById(R.id.btnCopy);
        Button btnDownload = findViewById(R.id.btnDownload);

        rvLogs.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LogAdapter();
        rvLogs.setAdapter(adapter);

        btnClear.setOnClickListener(v -> clearLogs());
        btnCopy.setOnClickListener(v -> copyLogs());
        btnDownload.setOnClickListener(v -> downloadLogs());

        LogCaptureManager manager = LogCaptureManager.getInstance();
        List<LogCaptureManager.LogEntry> existing = manager.getLogs();
        logs.addAll(existing);
        adapter.notifyDataSetChanged();
        if (!existing.isEmpty()) {
            rvLogs.smoothScrollToPosition(logs.size() - 1);
        }
        manager.addListener(this);
        manager.start();
        updateStatus();
    }

    @Override
    public void onNewLog(LogCaptureManager.LogEntry entry) {
        logs.add(entry);
        adapter.notifyItemInserted(logs.size() - 1);
        if (!rvLogs.canScrollVertically(1)) {
            rvLogs.smoothScrollToPosition(logs.size() - 1);
        }
        updateStatus();
    }

    @Override
    public void onLogCleared() {
        logs.clear();
        adapter.notifyDataSetChanged();
        updateStatus();
    }

    private String lastStatus = "";

    private void updateStatus() {
        String s = lastStatus.isEmpty() ? "All Logs" : lastStatus;
        tvStatus.setText(String.format(Locale.US, "Logs: %d | %s", logs.size(), s));
    }

    @Override
    public void onError(String error) {
        lastStatus = "Error: " + error;
        updateStatus();
    }

    @Override
    public void onStatus(String status) {
        lastStatus = status;
        updateStatus();
    }

    private void clearLogs() {
        LogCaptureManager.getInstance().clear();
        Toast.makeText(this, "Logs cleared", Toast.LENGTH_SHORT).show();
    }

    private void copyLogs() {
        StringBuilder sb = new StringBuilder();
        for (LogCaptureManager.LogEntry entry : logs) {
            sb.append(entry.fullLine).append("\n");
        }
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("logs", sb.toString());
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "Logs copied to clipboard", Toast.LENGTH_SHORT).show();
    }

    private void downloadLogs() {
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        File file = new File(downloadsDir, "logcat_" + timestamp + ".txt");

        try {
            StringBuilder sb = new StringBuilder();
            for (LogCaptureManager.LogEntry entry : logs) {
                sb.append(entry.fullLine).append("\n");
            }
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(sb.toString().getBytes());
            fos.close();
            Toast.makeText(this, "Saved: " + file.getName(), Toast.LENGTH_LONG).show();
            tvStatus.setText("Saved: " + file.getName());
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogCaptureManager.getInstance().removeListener(this);
    }

    class LogAdapter extends RecyclerView.Adapter<LogAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.log_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            LogCaptureManager.LogEntry entry = logs.get(position);
            holder.tvLevel.setText(String.valueOf(entry.level));
            holder.tvTag.setText(entry.tag);
            holder.tvMessage.setText(entry.fullLine);

            int color;
            int textColor = 0xFFFFFFFF;
            switch (entry.level) {
                case 'F':
                    color = 0xFFD32F2F;
                    break;
                case 'E':
                    color = 0xFFFF5252;
                    break;
                case 'W':
                    color = 0xFFFFD740;
                    textColor = 0xFF000000;
                    break;
                case 'I':
                    color = 0xFF4CAF50;
                    break;
                case 'D':
                    color = 0xFF2196F3;
                    break;
                case 'V':
                    color = 0xFF9E9E9E;
                    break;
                default:
                    color = 0xFFFFFFFF;
            }
            holder.tvLevel.setBackgroundColor(color);
            holder.tvLevel.setTextColor(textColor);
        }

        @Override
        public int getItemCount() {
            return logs.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvLevel, tvTag, tvMessage;

            ViewHolder(View itemView) {
                super(itemView);
                tvLevel = itemView.findViewById(R.id.tvLevel);
                tvTag = itemView.findViewById(R.id.tvTag);
                tvMessage = itemView.findViewById(R.id.tvMessage);
            }
        }
    }
}

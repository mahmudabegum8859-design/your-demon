package com.opx.yourdemon.modules;



import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.opx.yourdemon.R;
import com.opx.yourdemon.custom.Module;
import com.opx.yourdemon.utils.Core;
import com.opx.yourdemon.utils.CustomCommand;
import com.opx.yourdemon.utils.DownloadFile;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

public class ModuleAdapter extends RecyclerView.Adapter<ModuleAdapter.ViewHolder> {
    public Context context;
    public Activity activity;
    public ArrayList<Module> modules;
    public Core core;
    public boolean installed;

    public ModuleAdapter(Context context2, Activity mActivity, ArrayList<Module> mods, boolean isInstalled) {
        context = context2;
        activity = mActivity;
        modules = mods;
        installed = isInstalled;
        core = new Core(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.module_item, parent, false);
        return new ViewHolder(v);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder adapter, @SuppressLint("RecyclerView") final int position) {
        Module temp = modules.get(position);
        adapter.name.setText(temp.getName());
        adapter.author.setText("By " + temp.getAuthor() + "  ·  v" + String.format(Locale.US, "%.1f", temp.getVersion()));
        adapter.desc.setText(temp.getDesc());
        adapter.indicator.setVisibility(View.GONE);
        adapter.install.setVisibility(View.VISIBLE);
        adapter.install.setOnClickListener(view -> {
            if (installed) {
                removeModule(temp);
            } else {
                installModule(temp, adapter);
            }
        });
    }

    public void installModule(Module m, ViewHolder holder) {
        if (m.isOnly64bit() && !core.is64Bit()) {
            core.toaster(core.str("only64"));
            return;
        }
        holder.install.setVisibility(View.GONE);
        holder.indicator.setVisibility(View.VISIBLE);
        new Thread(() -> {
            boolean ok = false;
            try {
                String zipName = m.getName().replaceAll("[^A-Za-z0-9._-]", "_") + ".zip";
                File zipFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), zipName);
                boolean downloaded = DownloadFile.download(context, m.getSrcinstall(), zipName);
                if (downloaded) {
                    File target = new File(context.getFilesDir(), "modules/" + m.getName());
                    core.unzip(zipFile, target);
                    if (m.getPksg() != null && !m.getPksg().isEmpty()) {
                        CustomCommand.execute("chmod 777 -R " + context.getFilesDir() + "/modules/" + m.getName() + " && " + core.chroot() + " apk add " + m.getPksg(), core);
                    }
                    File script = new File(target, "install.sh");
                    if (script.exists()) {
                        CustomCommand.execute("chmod 755 \"" + script.getAbsolutePath() + "\" && \"" + script.getAbsolutePath() + "\"", core);
                    }
                    core.installmod(m.getName());
                    ok = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            boolean success = ok;
            activity.runOnUiThread(() -> {
                holder.indicator.setVisibility(View.GONE);
                holder.install.setVisibility(View.VISIBLE);
                if (success) {
                    core.toaster(m.getName() + " " + core.str("installed").trim());
                } else {
                    core.toaster(core.str("invalid_module").replace("{mn}", m.getName()));
                }
            });
        }).start();
    }

    public void removeModule(Module m) {
        File target = new File(context.getFilesDir(), "modules/" + m.getName());
        File script = new File(target, "delete.sh");
        if (script.exists()) {
            CustomCommand.execute("chmod 755 \"" + script.getAbsolutePath() + "\" && \"" + script.getAbsolutePath() + "\"", core);
        }
        core.deletemod(m.getName());
        modules.remove(m);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return modules.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView name;
        public TextView author;
        public TextView desc;
        public CircularProgressIndicator indicator;
        public ImageView install;
        public MaterialCardView card;

        public ViewHolder(View v) {
            super(v);
            name = v.findViewById(R.id.module_name);
            author = v.findViewById(R.id.module_author_and_ver);
            desc = v.findViewById(R.id.module_desc);
            indicator = v.findViewById(R.id.module_indicator);
            install = v.findViewById(R.id.module_install);
            card = v.findViewById(R.id.item);
        }
    }
}

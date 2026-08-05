package com.opx.yourdemon.coremanger;



import static com.opx.yourdemon.R.string.*;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.opx.yourdemon.R;
import com.opx.yourdemon.coremanger.utils.InstallPackage;
import com.opx.yourdemon.custom.Package;
import com.opx.yourdemon.utils.Core;
import com.opx.yourdemon.utils.CustomCommand;

import java.util.ArrayList;

/**
 * This class is used to display the packages in the list
 */
public class CoreAdapter extends RecyclerView.Adapter<CoreAdapter.ViewHolder> {
    public ArrayList<Package> pkgs;
    public Context context;
    public Activity activity;
    public Core core;

    public CoreAdapter(Context context2, Activity mActivity, ArrayList<Package> pskss) {
        context = context2;
        pkgs = pskss;
        activity = mActivity;
        core = new Core(context2);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.package_item, parent, false);
        return new ViewHolder(v);
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder adapter, @SuppressLint("RecyclerView") final int position) {
        Package temp = pkgs.get(position);
        adapter.title.setText(temp.getName());
        adapter.version.setText(temp.getVersion());
        adapter.install.setOnClickListener(view -> {
            adapter.install.setVisibility(View.INVISIBLE);
            core.toaster(core.str("installingg"));
            new Thread(() -> {
                Boolean ok = InstallPackage.execute(temp.getName(),core);
                if (ok){
                    toaster(core.str("installed")+" "+temp.getName());
                }else{
                    toaster(core.str("inst_error")+" "+temp.getName());
                    activity.runOnUiThread(() -> adapter.install.setVisibility(View.VISIBLE));
                }
            }).start();
        });
    }

    @Override
    public int getItemCount() {

        return pkgs.size();
    }

    public void toaster(String msg) {
        activity.runOnUiThread(() -> {
            Toast toast = Toast.makeText(context,
                    msg, Toast.LENGTH_SHORT);
            toast.show();
        });

    }

    public void appendtext(String text, TextView output) {
        activity.runOnUiThread(() -> output.append(text));
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
        public TextView title;
        public TextView version;
        public ImageView install;

        public ViewHolder(View v) {
            super(v);
            version = v.findViewById(R.id.version);
            title = v.findViewById(R.id.title);
            install = v.findViewById(R.id.run_sploit);
        }

    }

}

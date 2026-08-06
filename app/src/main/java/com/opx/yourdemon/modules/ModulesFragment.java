package com.opx.yourdemon.modules;



import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.opx.yourdemon.R;
import com.opx.yourdemon.custom.Module;
import com.opx.yourdemon.utils.Core;

import java.util.ArrayList;

public class ModulesFragment extends Fragment {

    public Context context;
    public Activity activity;
    public Core core;
    public ArrayList<Module> repoModules = new ArrayList<>();
    public ArrayList<Module> installedModules = new ArrayList<>();
    public ModuleAdapter repoAdapter;
    public ModuleAdapter installedAdapter;
    public LinearProgressIndicator loading;
    public TextView installedText;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View viewroot = inflater.inflate(R.layout.module_fragment, container, false);
        context = getContext();
        activity = getActivity();
        core = new Core(context);
        loading = viewroot.findViewById(R.id.loading);
        installedText = viewroot.findViewById(R.id.installed_text);
        RecyclerView installedList = viewroot.findViewById(R.id.installed_list);
        RecyclerView repoList = viewroot.findViewById(R.id.repo_list);
        installedList.setLayoutManager(new LinearLayoutManager(context));
        repoList.setLayoutManager(new LinearLayoutManager(context));
        installedAdapter = new ModuleAdapter(context, activity, installedModules, true);
        repoAdapter = new ModuleAdapter(context, activity, repoModules, false);
        installedList.setAdapter(installedAdapter);
        repoList.setAdapter(repoAdapter);
        loadModules();
        return viewroot;
    }

    public void loadModules() {
        loading.setVisibility(View.VISIBLE);
        new Thread(() -> {
            ArrayList<Module> repo = core.getModules();
            ArrayList<String> installedNames = core.getListString("installed_modules");
            ArrayList<Module> inst = new ArrayList<>();
            for (Module m : repo) {
                if (installedNames.contains(m.getName())) {
                    m.setInstalled(true);
                    inst.add(m);
                }
            }
            activity.runOnUiThread(() -> {
                repoModules.clear();
                repoModules.addAll(repo);
                installedModules.clear();
                installedModules.addAll(inst);
                installedText.setVisibility(inst.isEmpty() ? View.GONE : View.VISIBLE);
                repoAdapter.notifyDataSetChanged();
                installedAdapter.notifyDataSetChanged();
                loading.setVisibility(View.GONE);
            });
        }).start();
    }
}

package com.opx.yourdemon.nuclei;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.opx.yourdemon.R;
import com.opx.yourdemon.nuclei.utils.NucleiRunner;
import com.opx.yourdemon.utils.Core;
import com.opx.yourdemon.utils.OnSwipeListener;
import com.opx.yourdemon.utils.TaskRunner;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.util.ArrayList;

public class NucleiFragment extends Fragment {

    public ImageButton search;
    public Core core;
    public Context context;
    public Activity activity;
    public NucleiFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.nuclei_fragment, container, false);
        context = getContext();
        activity = getActivity();
        search = view.findViewById(R.id.nuclei_search);

        TextView output = view.findViewById(R.id.nuclei_output);
        CheckBox critical = view.findViewById(R.id.nuclei_critical);
        CheckBox high = view.findViewById(R.id.nuclei_high);
        CheckBox medium = view.findViewById(R.id.nuclei_medium);
        CheckBox low = view.findViewById(R.id.nuclei_low);
        CheckBox info = view.findViewById(R.id.nuclei_info);

        core = new Core(context);
        ExpandableLayout menu = activity.findViewById(R.id.menu_expand);
        view.setOnTouchListener(new OnSwipeListener(context) {
            public void onSwipeTop() {core.closemenu(menu); }
            @SuppressLint("ClickableViewAccessibility")
            public void onSwipeRight() { }
            public void onSwipeLeft() { }
            public void onSwipeBottom() { core.openmenu(menu); }
        });
        TextInputEditText getquery = view.findViewById(R.id.nuclei_target);
        TextInputEditText threadsInput = view.findViewById(R.id.nuclei_threads);

        search.setOnClickListener(view1 -> {
            String target = String.valueOf(getquery.getText());
            String threadsStr = String.valueOf(threadsInput.getText());
            if (threadsStr.isEmpty()) {
                threadsStr = "10";
            }
            output.setText("");
            ArrayList<String> severity = new ArrayList<>();
            if (critical.isChecked()) severity.add("critical");
            if (high.isChecked()) severity.add("high");
            if (medium.isChecked()) severity.add("medium");
            if (low.isChecked()) severity.add("low");
            if (info.isChecked()) severity.add("info");
            String severityStr = String.join(",", severity);
            if (severityStr.isEmpty()) {
                severityStr = "critical,high,medium,low,info";
            }
            final String fTarget = target;
            final String fSeverity = severityStr;
            final String fThreads = threadsStr;
            TaskRunner.execute(() -> NucleiRunner.execute(fTarget, fSeverity, fThreads, context, activity, output, null));
        });

        return view;
    }
}

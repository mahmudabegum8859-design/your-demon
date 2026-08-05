package com.opx.yourdemon.nmap;


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
import com.opx.yourdemon.nmap.utils.ScanTarget;
import com.opx.yourdemon.utils.Core;
import com.opx.yourdemon.utils.OnSwipeListener;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.util.ArrayList;

public class NmapScanner extends Fragment {

    public ImageButton search;
    public ImageButton save;
    public Boolean now;
    public Core core;
    public Context context;
    public Activity activity;
    public NmapScanner() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        //Initalizing
        View view = inflater.inflate(R.layout.nmap_fragment, container, false);
        context = getContext();
        activity = getActivity();
        search = view.findViewById(R.id.search);

        ExpandableLayout extra = view.findViewById(R.id.extra_expand);
        LinearLayout toggle = view.findViewById(R.id.extra_toggle);
        ImageView toggle_img = view.findViewById(R.id.nmap_toggle_img);
        TextView output = view.findViewById(R.id.nmap_output);
        CheckBox detect_os = view.findViewById(R.id.detect_os);
        CheckBox detect_services = view.findViewById(R.id.detect_services);
        CheckBox fast_scan = view.findViewById(R.id.fast_scan);
        CheckBox mark_online = view.findViewById(R.id.mark_online);
        core = new Core(context);
        ExpandableLayout menu = activity.findViewById(R.id.menu_expand);
        view.setOnTouchListener(new OnSwipeListener(context) {
            public void onSwipeTop() {core.closemenu(menu); }
            @SuppressLint("ClickableViewAccessibility")
            public void onSwipeRight() { }
            public void onSwipeLeft() { }
            public void onSwipeBottom() { core.openmenu(menu); }
        });
        TextInputEditText getquery = view.findViewById(R.id.getsearch);
        toggle.setOnClickListener(view2 -> {
            if (extra.isExpanded()) {
                toggle_img.animate().rotation(0);
            } else {
                toggle_img.animate().rotation(180);
            }
            extra.toggle();
        });
        search.setOnClickListener(view1 -> {
            String q = String.valueOf(getquery.getText());
            output.setText("");
            ArrayList<Boolean> checked = new ArrayList<>();
            checked.add(detect_os.isChecked());
            checked.add(detect_services.isChecked());
            checked.add(fast_scan.isChecked());
            checked.add(mark_online.isChecked());
            new Thread(() -> {
                try {
                    now = ScanTarget.execute(q, checked, activity, output);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        });

        return view;
    }


}

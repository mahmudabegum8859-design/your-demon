package com.opx.yourdemon.searchsploit_web;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.opx.yourdemon.R;
import com.opx.yourdemon.custom.Sploit;
import com.opx.yourdemon.searchsploit.SploitAdapter;
import com.opx.yourdemon.searchsploit_web.utils.WebSploitSearch;
import com.opx.yourdemon.utils.Core;
import com.opx.yourdemon.utils.OnSwipeListener;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.util.ArrayList;

public class WebSploitFragment extends Fragment {
    public MaterialButton search;
    public Core core;
    private RecyclerView mRecyclerView;
    private SploitAdapter mAdapter;
    private ProgressBar progressBar;
    public Context context;
    public Activity activity;

    public WebSploitFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.searchsploit_web_fragment, container, false);
        context = getContext();
        activity = getActivity();
        core = new Core(context);

        mRecyclerView = view.findViewById(R.id.web_search_results);
        search = view.findViewById(R.id.web_search_button);
        progressBar = view.findViewById(R.id.web_search_progress);
        TextInputEditText getquery = view.findViewById(R.id.web_search_input);

        ExpandableLayout menu = activity.findViewById(R.id.menu_expand);
        view.setOnTouchListener(new OnSwipeListener(context) {
            public void onSwipeTop() { core.closemenu(menu); }
            @SuppressLint("ClickableViewAccessibility")
            public void onSwipeRight() { }
            public void onSwipeLeft() { }
            public void onSwipeBottom() { core.openmenu(menu); }
        });

        mRecyclerView.setLayoutManager(new LinearLayoutManager(activity));
        mRecyclerView.setItemViewCacheSize(255);

        search.setOnClickListener(view1 -> {
            String q = String.valueOf(getquery.getText());
            progressBar.setVisibility(View.VISIBLE);
            new Thread(() -> {
                try {
                    ArrayList<Sploit> w = WebSploitSearch.execute(q);
                    activity.runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        if (w.isEmpty()) {
                            core.toaster(core.str("no_results"));
                        } else {
                            mAdapter = new SploitAdapter(context, activity, w);
                            mRecyclerView.setAdapter(mAdapter);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    activity.runOnUiThread(() -> progressBar.setVisibility(View.GONE));
                }
            }).start();
        });

        return view;
    }
}

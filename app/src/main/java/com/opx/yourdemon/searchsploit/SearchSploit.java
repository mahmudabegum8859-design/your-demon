package com.opx.yourdemon.searchsploit;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.opx.yourdemon.R;
import com.opx.yourdemon.custom.Sploit;
import com.opx.yourdemon.searchsploit.utils.GetSploit;
import com.opx.yourdemon.utils.Core;
import com.opx.yourdemon.utils.OnSwipeListener;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.util.ArrayList;

public class SearchSploit extends Fragment {
    public ImageButton search;
    public Core core;
    private RecyclerView mRecyclerView;
    private SploitAdapter mAdapter;
    public Context context;
    public Activity activity;
    public SearchSploit() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        //Initalizing
        View view = inflater.inflate(R.layout.searchsploit_fragment, container, false);
        context = getContext();
        activity = getActivity();
        mRecyclerView = view.findViewById(R.id.search_list);
        search = view.findViewById(R.id.search);
        ExpandableLayout menu = activity.findViewById(R.id.menu_expand);
        // The `onSwipeTop()` method is called when the user swipes the view from the top.
        view.setOnTouchListener(new OnSwipeListener(context) {
            public void onSwipeTop() {core.closemenu(menu); }
            @SuppressLint("ClickableViewAccessibility")
            public void onSwipeRight() { }
            public void onSwipeLeft() { }
            public void onSwipeBottom() { core.openmenu(menu); }
        });

        mRecyclerView.setLayoutManager(new LinearLayoutManager(activity));
        core = new Core(context);
        mRecyclerView.setItemViewCacheSize(255);
        TextInputEditText getquery = view.findViewById(R.id.getsearch);

        search.setOnClickListener(view1 -> {
            String q = String.valueOf(getquery.getText());
            new Thread(() -> {
                try {
                    // It's a thread that runs in the background and gets the results from the
                    // searchsploit api.
                    ArrayList<Sploit> w = GetSploit.execute(q, core);
                    activity.runOnUiThread(() -> {
                        if (w.isEmpty()) {
                            core.toaster(core.str("no_results"));
                        } else {
                            mAdapter = new SploitAdapter(context, activity, w);
                            mRecyclerView.setAdapter(mAdapter);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        });

        return view;
    }


}

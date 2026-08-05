package com.opx.yourdemon.three_wifi;


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
import com.opx.yourdemon.custom.Cabinet;
import com.opx.yourdemon.custom.WiFINetwork;
import com.opx.yourdemon.three_wifi.utils.GetWiFI;
import com.opx.yourdemon.utils.Core;
import com.opx.yourdemon.utils.OnSwipeListener;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.util.ArrayList;

public class SearchPage extends Fragment {
    public ImageButton search;
    public Core core;
    private RecyclerView mRecyclerView;
    private Search_Adapter mAdapter;
    public Activity activity;
    public Context context;
    public SearchPage() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        //Initalizing
        View view = inflater.inflate(R.layout.three_wifi_fragment, container, false);
        context = getContext();
        activity = getActivity();
        ExpandableLayout menu = activity.findViewById(R.id.menu_expand);
        view.setOnTouchListener(new OnSwipeListener(context) {
            public void onSwipeTop() {core.closemenu(menu); }
            @SuppressLint("ClickableViewAccessibility")
            public void onSwipeRight() { }
            public void onSwipeLeft() { }
            public void onSwipeBottom() { core.openmenu(menu); }
        });
        mRecyclerView = view.findViewById(R.id.search_list);
        search = view.findViewById(R.id.search);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(activity));
        core = new Core(context);
        mRecyclerView.setItemViewCacheSize(255);
        TextInputEditText getmac = view.findViewById(R.id.getsearch);
        Cabinet cabinet = new Cabinet(context);
        cabinet.getStored();
        // This is the code that runs when the search button is clicked. It gets the key from the
        // cabinet, and the mac address from the text field.
        // It then runs a new thread to get the results from the server.
        search.setOnClickListener(view1 -> {
            String key = cabinet.getKeyView();
            String mac = String.valueOf(getmac.getText());
            new Thread(() -> {
                ArrayList<WiFINetwork> w = GetWiFI.execute(key, mac);
                activity.runOnUiThread(() -> {
                    if (w.isEmpty()) {
                        core.toaster(core.str("no_results"));
                    } else {
                        mAdapter = new Search_Adapter(context, activity, w);
                        mRecyclerView.setAdapter(mAdapter);
                    }
                });
            }).start();
        });

        return view;
    }


}

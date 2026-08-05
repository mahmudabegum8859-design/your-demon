package com.opx.yourdemon.actions;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.card.MaterialCardView;
import com.opx.yourdemon.R;
import com.opx.yourdemon.geomac.GeoMac;
import com.opx.yourdemon.utils.Core;

public class ActionsFragment extends Fragment {

    public Core core;
    public TextView statusText;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.actions_fragment, container, false);
        core = new Core(getContext());

        statusText = view.findViewById(R.id.status_text);

        MaterialCardView geomacAction = view.findViewById(R.id.geomac_action);
        MaterialCardView macchangerAction = view.findViewById(R.id.macchanger_action);
        MaterialCardView wikiAction = view.findViewById(R.id.wiki_action);

        geomacAction.setOnClickListener(v -> {
            getFragmentManager().beginTransaction()
                    .replace(R.id.flContent, new GeoMac())
                    .addToBackStack(null)
                    .commit();
        });

        macchangerAction.setOnClickListener(v -> {
            getFragmentManager().beginTransaction()
                    .replace(R.id.flContent, new com.opx.yourdemon.mac_changer.MacChangerFragment())
                    .addToBackStack(null)
                    .commit();
        });

        wikiAction.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/OP-AMINUL-FF/your-demon/wiki"));
            startActivity(browserIntent);
        });

        return view;
    }
}

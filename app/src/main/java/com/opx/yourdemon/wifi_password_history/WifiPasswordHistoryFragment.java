package com.opx.yourdemon.wifi_password_history;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.opx.yourdemon.R;
import com.opx.yourdemon.utils.Core;

import java.util.ArrayList;

public class WifiPasswordHistoryFragment extends Fragment {

    public Core core;
    public ListView historyList;
    public TextView statusText;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.wifi_password_history, container, false);
        core = new Core(getContext());

        historyList = view.findViewById(R.id.history_list);
        statusText = view.findViewById(R.id.status_text);

        loadHistory();

        view.findViewById(R.id.clear_history).setOnClickListener(v -> clearHistory());

        return view;
    }

    private void loadHistory() {
        ArrayList<String> passwords = core.getListString("wifi_history");
        if (passwords.isEmpty()) {
            statusText.setText(core.str("no_history"));
        } else {
            statusText.setText(core.str("wifi_history") + " (" + passwords.size() + ")");
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, passwords);
            historyList.setAdapter(adapter);
        }
    }

    private void clearHistory() {
        core.putListString("wifi_history", new ArrayList<>());
        loadHistory();
        core.toaster(core.str("cleared"));
    }
}

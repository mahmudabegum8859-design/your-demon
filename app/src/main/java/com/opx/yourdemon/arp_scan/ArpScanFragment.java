package com.opx.yourdemon.arp_scan;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.opx.yourdemon.R;
import com.opx.yourdemon.utils.CommandOutput;
import com.opx.yourdemon.utils.Core;
import com.opx.yourdemon.utils.TaskRunner;
import com.opx.yourdemon.utils.CustomCommand;

import java.util.ArrayList;

public class ArpScanFragment extends Fragment {

    public Core core;
    public TextView statusText;
    public ListView resultList;
    public EditText rangeInput;
    public MaterialButton scanBtn;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.arp_scan, container, false);
        core = new Core(getContext());

        statusText = view.findViewById(R.id.status_text);
        resultList = view.findViewById(R.id.result_list);
        rangeInput = view.findViewById(R.id.range_input);
        scanBtn = view.findViewById(R.id.scan_btn);

        rangeInput.setText("192.168.1.0/24");
        scanBtn.setOnClickListener(v -> startScan());

        return view;
    }

    private void startScan() {
        String range = rangeInput.getText().toString().trim();
        if (range.isEmpty()) { core.toaster(core.str("please_fill")); return; }

        scanBtn.setEnabled(false);
        statusText.setText(core.str("scanning"));
        resultList.setAdapter(null);

        boolean[] canceled = {false};
        TaskRunner.execute(() -> {
            ArrayList<String> results = new ArrayList<>();
            try {
                getActivity().runOnUiThread(() -> statusText.setText(core.str("scanning_arp")));
                CustomCommand.execute("rm /data/local/YourDemon/arp_scan.txt", core);
                String cmd = "busybox arp-scan --local --retry=3 --numeric --ignoredups " + range + " > /data/local/YourDemon/arp_scan.txt";
                CustomCommand.execute(cmd, core);

                ArrayList<String> raw = CommandOutput.execute("cat /data/local/YourDemon/arp_scan.txt", core);
                if (raw != null) {
                    for (String line : raw) {
                        if (line.contains("(") && line.contains(")")) {
                            results.add(line.trim());
                        }
                    }
                }
            } catch (Exception e) {
                results.add(core.str("error") + ": " + e.getMessage());
            }
            ArrayList<String> finalResults = results;
            getActivity().runOnUiThread(() -> {
                scanBtn.setEnabled(true);
                if (finalResults.isEmpty()) {
                    statusText.setText(core.str("no_results"));
                } else {
                    statusText.setText(core.str("found") + " " + finalResults.size() + " " + core.str("devices"));
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, finalResults);
                    resultList.setAdapter(adapter);
                }
            });
        });
    }
}

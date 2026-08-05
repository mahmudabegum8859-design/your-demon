package com.opx.yourdemon.phone_detection;


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
import com.opx.yourdemon.utils.CommandOutput;
import com.opx.yourdemon.utils.Core;
import com.opx.yourdemon.utils.TaskRunner;

import java.util.ArrayList;

public class PhoneDetectionFragment extends Fragment {

    public Core core;
    public TextView statusText;
    public ListView phoneList;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.phone_detection, container, false);
        core = new Core(getContext());

        statusText = view.findViewById(R.id.status_text);
        phoneList = view.findViewById(R.id.phone_list);

        detectPhones();

        view.findViewById(R.id.refresh_phones).setOnClickListener(v -> detectPhones());

        return view;
    }

    private void detectPhones() {
        statusText.setText(core.str("detecting_phones"));
        TaskRunner.execute(() -> {
            ArrayList<String> phones = new ArrayList<>();
            try {
                ArrayList<String> nmap = CommandOutput.execute(core.EXECUTE + "nmap -sn 192.168.1.0/24 --exclude 192.168.1.1", core);
                if (nmap != null) {
                    for (String line : nmap) {
                        if (line.contains("Nmap scan report for") || line.contains("MAC") || line.contains("Android") || line.contains("iPhone") || line.contains("Samsung") || line.contains("Xiaomi") || line.contains("Huawei")) {
                            phones.add(line.trim());
                        }
                    }
                }
            } catch (Exception e) {
                phones.add(core.str("error") + ": " + e.getMessage());
            }
            ArrayList<String> finalPhones = phones;
            getActivity().runOnUiThread(() -> {
                if (finalPhones.isEmpty()) {
                    statusText.setText(core.str("no_phones"));
                } else {
                    statusText.setText(core.str("phone_found") + ": " + finalPhones.size());
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, finalPhones);
                    phoneList.setAdapter(adapter);
                }
            });
        });
    }
}

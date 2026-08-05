package com.opx.yourdemon.mac_changer;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.opx.yourdemon.R;
import com.opx.yourdemon.utils.CommandOutput;
import com.opx.yourdemon.utils.Core;
import com.opx.yourdemon.utils.CustomCommand;
import com.opx.yourdemon.utils.TaskRunner;

import java.util.ArrayList;
import java.util.Random;

public class MacChangerFragment extends Fragment {

    public Core core;
    public TextView statusText, currentMac;
    public EditText interfaceInput, macInput;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.mac_changer, container, false);
        core = new Core(getContext());

        statusText = view.findViewById(R.id.status_text);
        currentMac = view.findViewById(R.id.current_mac);
        interfaceInput = view.findViewById(R.id.interface_input);
        macInput = view.findViewById(R.id.mac_input);
        MaterialButton randomBtn = view.findViewById(R.id.random_mac);
        MaterialButton applyBtn = view.findViewById(R.id.apply_mac);
        MaterialButton restoreBtn = view.findViewById(R.id.restore_mac);

        interfaceInput.setText(core.getString("wlan_scan"));
        getCurrentMac();

        randomBtn.setOnClickListener(v -> macInput.setText(generateRandomMac()));
        applyBtn.setOnClickListener(v -> changeMac());
        restoreBtn.setOnClickListener(v -> restoreMac());

        return view;
    }

    private String generateRandomMac() {
        Random rand = new Random();
        byte[] mac = new byte[6];
        rand.nextBytes(mac);
        mac[0] = (byte) (mac[0] & (byte) 0xFE);
        mac[0] = (byte) (mac[0] | (byte) 0x02);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            sb.append(String.format("%02x", mac[i]));
            if (i < 5) sb.append(":");
        }
        return sb.toString();
    }

    private void getCurrentMac() {
        TaskRunner.execute(() -> {
            String mac = core.str("unknown");
            try {
                String iface = interfaceInput.getText().toString().trim();
                ArrayList<String> result = CommandOutput.execute("cat /sys/class/net/" + iface + "/address", core);
                if (result != null && !result.isEmpty()) {
                    mac = result.get(0);
                }
            } catch (Exception ignored) {}
            String finalMac = mac;
            getActivity().runOnUiThread(() -> currentMac.setText(core.str("current_mac") + ": " + finalMac));
        });
    }

    private void changeMac() {
        String iface = interfaceInput.getText().toString().trim();
        String mac = macInput.getText().toString().trim();
        if (mac.isEmpty()) { core.toaster(core.str("enter_mac")); return; }

        statusText.setText(core.str("changing_mac"));
        TaskRunner.execute(() -> {
            boolean result = false;
            try {
                CustomCommand.execute("ip link set " + iface + " down", core);
                result = CustomCommand.execute("ip link set " + iface + " address " + mac, core);
                CustomCommand.execute("ip link set " + iface + " up", core);
            } catch (Exception ignored) {}
            boolean finalResult = result;
            getActivity().runOnUiThread(() -> {
                if (finalResult) {
                    statusText.setText(core.str("mac_changed"));
                    core.toaster(core.str("mac_changed"));
                    getCurrentMac();
                } else {
                    statusText.setText(core.str("failed"));
                }
            });
        });
    }

    private void restoreMac() {
        String iface = interfaceInput.getText().toString().trim();
        statusText.setText(core.str("restoring_mac"));
        TaskRunner.execute(() -> {
            boolean result = false;
            try {
                CustomCommand.execute("ip link set " + iface + " down", core);
                result = CustomCommand.execute("ip link set " + iface + " address $(cat /sys/class/net/" + iface + "/addr_assign_type)", core);
                CustomCommand.execute("ip link set " + iface + " up", core);
            } catch (Exception ignored) {}
            boolean finalResult = result;
            getActivity().runOnUiThread(() -> {
                if (finalResult) {
                    statusText.setText(core.str("mac_restored"));
                    getCurrentMac();
                }
            });
        });
    }
}

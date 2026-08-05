package com.opx.yourdemon.wifi_info;


import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.opx.yourdemon.R;
import com.opx.yourdemon.utils.CommandOutput;
import com.opx.yourdemon.utils.Core;
import com.opx.yourdemon.utils.TaskRunner;

import java.util.ArrayList;

public class WifiInfoFragment extends Fragment {

    public Core core;
    public TextView statusText, infoText;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.wifi_info, container, false);
        core = new Core(getContext());

        statusText = view.findViewById(R.id.status_text);
        infoText = view.findViewById(R.id.info_text);

        loadWifiInfo();

        view.findViewById(R.id.refresh_info).setOnClickListener(v -> loadWifiInfo());

        return view;
    }

    private void loadWifiInfo() {
        statusText.setText(core.str("getting_wifi_info"));
        TaskRunner.execute(() -> {
            StringBuilder info = new StringBuilder();
            try {
                WifiManager wifiManager = (WifiManager) core.getContext2().getSystemService(core.getContext2().WIFI_SERVICE);
                if (wifiManager != null) {
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    if (wifiInfo != null) {
                        info.append("SSID: ").append(wifiInfo.getSSID()).append("\n");
                        info.append("BSSID: ").append(wifiInfo.getBSSID()).append("\n");
                        info.append("RSSI: ").append(wifiInfo.getRssi()).append(" dBm\n");
                        info.append("Frequency: ").append(wifiInfo.getFrequency()).append(" MHz\n");
                        info.append("Link speed: ").append(wifiInfo.getLinkSpeed()).append(" Mbps\n");
                        info.append("IP address: ").append(intToIp(wifiInfo.getIpAddress())).append("\n");
                        info.append("MAC: ").append(wifiInfo.getMacAddress()).append("\n\n");
                    }
                }

                ArrayList<String> iwconfig = CommandOutput.execute("iwconfig 2>/dev/null", core);
                if (iwconfig != null && !iwconfig.isEmpty()) {
                    info.append("--- iwconfig ---\n");
                    for (String line : iwconfig) {
                        info.append(line).append("\n");
                    }
                }
            } catch (Exception e) {
                info.append(core.str("error")).append(": ").append(e.getMessage());
            }
            String finalInfo = info.toString();
            getActivity().runOnUiThread(() -> {
                statusText.setText(core.str("finished"));
                infoText.setText(finalInfo);
            });
        });
    }

    private String intToIp(int i) {
        return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF) + "." + ((i >> 24) & 0xFF);
    }
}

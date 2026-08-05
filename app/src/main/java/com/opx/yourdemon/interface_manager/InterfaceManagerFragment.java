package com.opx.yourdemon.interface_manager;


import android.net.wifi.WifiManager;
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
import com.opx.yourdemon.utils.TaskRunner;
import com.opx.yourdemon.wifi.utils.GetInterfaces;

import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InterfaceManagerFragment extends Fragment {

    public Core core;
    public ListView interfaceList;
    public TextView scanInterface, deauthInterface, statusText;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.interface_manager, container, false);
        core = new Core(getContext());

        interfaceList = view.findViewById(R.id.interface_list);
        scanInterface = view.findViewById(R.id.scan_interface);
        deauthInterface = view.findViewById(R.id.deauth_interface);
        statusText = view.findViewById(R.id.status_text);

        scanInterface.setText(core.str("set_scan_iface") + ": " + core.getString("wlan_scan"));
        deauthInterface.setText(core.str("set_deauth_iface") + ": " + core.getString("wlan_deauth"));

        loadInterfaces();

        view.findViewById(R.id.refresh_interfaces).setOnClickListener(v -> loadInterfaces());

        return view;
    }

    private void loadInterfaces() {
        statusText.setText(core.str("scanning"));
        TaskRunner.execute(() -> {
            ArrayList<String> interfaces = new ArrayList<>();
            try {
                interfaces = GetInterfaces.get(core);
                List<NetworkInterface> allIface = Collections.list(NetworkInterface.getNetworkInterfaces());
                for (NetworkInterface iface : allIface) {
                    String name = iface.getName();
                    if (!interfaces.contains(name) && !name.contains("lo")) {
                        interfaces.add(name);
                    }
                }
            } catch (Exception ignored) {}
            ArrayList<String> finalInterfaces = interfaces;
            getActivity().runOnUiThread(() -> {
                if (finalInterfaces.isEmpty()) {
                    statusText.setText(core.str("no_results"));
                } else {
                    statusText.setText(core.str("finished"));
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, finalInterfaces);
                    interfaceList.setAdapter(adapter);
                    interfaceList.setOnItemClickListener((parent, view1, position, id) -> {
                        String iface = finalInterfaces.get(position);
                        core.putString("wlan_scan", iface);
                        core.putString("wlan_deauth", iface);
                        scanInterface.setText(core.str("set_scan_iface") + ": " + iface);
                        deauthInterface.setText(core.str("set_deauth_iface") + ": " + iface);
                        core.toaster(core.str("saved_to") + " " + iface);
                    });
                }
            });
        });
    }
}

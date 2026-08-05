package com.opx.yourdemon.wps_interface;


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
import com.opx.yourdemon.utils.Core;
import com.opx.yourdemon.utils.CustomCommand;
import com.opx.yourdemon.utils.TaskRunner;

public class WpsInterfaceFragment extends Fragment {

    public Core core;
    public TextView statusText;
    public EditText bssidInput, pinInput, ifaceInput;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.wps_interface, container, false);
        core = new Core(getContext());

        statusText = view.findViewById(R.id.status_text);
        bssidInput = view.findViewById(R.id.bssid_input);
        pinInput = view.findViewById(R.id.pin_input);
        ifaceInput = view.findViewById(R.id.iface_input);

        ifaceInput.setText(core.getString("wlan_scan"));

        MaterialButton connectBtn = view.findViewById(R.id.connect_wps);
        connectBtn.setOnClickListener(v -> connectWps());

        return view;
    }

    private void connectWps() {
        String bssid = bssidInput.getText().toString().trim();
        String pin = pinInput.getText().toString().trim();
        String iface = ifaceInput.getText().toString().trim();

        if (bssid.isEmpty() || pin.isEmpty()) { core.toaster(core.str("please_fill")); return; }

        statusText.setText(core.str("sending_pin"));
        TaskRunner.execute(() -> {
            getActivity().runOnUiThread(() -> statusText.setText(core.str("starting_wps")));
            boolean result = false;
            try {
                String cmd = core.EXECUTE + "reaver -i " + iface + " -b " + bssid + " -p " + pin + " -vv";
                result = CustomCommand.execute(cmd, core);
            } catch (Exception ignored) {}
            boolean finalResult = result;
            getActivity().runOnUiThread(() -> {
                if (finalResult) {
                    statusText.setText(core.str("succes"));
                    core.toaster(core.str("succes"));
                } else {
                    statusText.setText(core.str("failed"));
                }
            });
        });
    }
}

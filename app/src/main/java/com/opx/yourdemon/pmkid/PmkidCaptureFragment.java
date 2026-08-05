package com.opx.yourdemon.pmkid;


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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

public class PmkidCaptureFragment extends Fragment {

    public Core core;
    public TextView statusText, resultText;
    public EditText bssidInput, interfaceInput;
    public MaterialButton startBtn;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.pmkid_capture, container, false);
        core = new Core(getContext());

        statusText = view.findViewById(R.id.status_text);
        resultText = view.findViewById(R.id.result_text);
        bssidInput = view.findViewById(R.id.bssid_input);
        interfaceInput = view.findViewById(R.id.interface_input);
        startBtn = view.findViewById(R.id.start_capture);

        interfaceInput.setText(core.getString("wlan_scan"));

        startBtn.setOnClickListener(v -> startCapture());

        return view;
    }

    private void startCapture() {
        String bssid = bssidInput.getText().toString().trim();
        String iface = interfaceInput.getText().toString().trim();

        if (bssid.isEmpty() || iface.isEmpty()) {
            core.toaster(core.str("please_fill"));
            return;
        }

        startBtn.setEnabled(false);
        statusText.setText(core.str("starting_pmkid"));
        resultText.setText("");

        String bssidFinal = bssid;
        String ifaceFinal = iface;
        TaskRunner.execute(() -> {
            String result;
            try {
                getActivity().runOnUiThread(() -> statusText.setText(core.str("enabling_monitor")));
                CustomCommand.execute("ip link set " + ifaceFinal + " down", core);
                CustomCommand.execute("iw dev " + ifaceFinal + " set type monitor", core);
                CustomCommand.execute("ip link set " + ifaceFinal + " up", core);

                getActivity().runOnUiThread(() -> statusText.setText(core.str("capturing_pmkid")));
                String cmd = core.EXECUTE + "hcxdumptool -o /data/local/YourDemon/pmkid.pcapng -i " + ifaceFinal + " --enable_status=1 --filterlist_ap=" + bssidFinal.toLowerCase() + " --filtermode=1";
                CustomCommand.execute(cmd, core);

                getActivity().runOnUiThread(() -> statusText.setText(core.str("checking_result")));
                File pmkidFile = new File("/data/local/YourDemon/pmkid.pcapng");
                if (pmkidFile.exists() && pmkidFile.length() > 0) {
                    result = core.str("pmkid_captured") + ": " + pmkidFile.getAbsolutePath();
                } else {
                    result = core.str("pmkid_not_found");
                }
            } catch (Exception e) {
                result = core.str("error") + ": " + e.getMessage();
            }
            String finalResult = result;
            getActivity().runOnUiThread(() -> {
                statusText.setText(core.str("finished"));
                resultText.setText(finalResult);
                startBtn.setEnabled(true);
            });
            CustomCommand.execute("ip link set " + ifaceFinal + " down", core);
            CustomCommand.execute("iw dev " + ifaceFinal + " set type managed", core);
            CustomCommand.execute("ip link set " + ifaceFinal + " up", core);
        });
    }
}

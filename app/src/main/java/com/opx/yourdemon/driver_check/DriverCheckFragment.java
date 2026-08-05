package com.opx.yourdemon.driver_check;


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

public class DriverCheckFragment extends Fragment {

    public Core core;
    public TextView statusText, driverInfo;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.driver_check, container, false);
        core = new Core(getContext());

        statusText = view.findViewById(R.id.status_text);
        driverInfo = view.findViewById(R.id.driver_info);

        checkDrivers();

        view.findViewById(R.id.refresh_drivers).setOnClickListener(v -> checkDrivers());

        return view;
    }

    private void checkDrivers() {
        statusText.setText(core.str("checking_drivers"));
        TaskRunner.execute(() -> {
            StringBuilder info = new StringBuilder();
            try {
                ArrayList<String> lsmod = CommandOutput.execute("cat /proc/modules", core);
                if (lsmod != null) {
                    for (String line : lsmod) {
                        info.append(line).append("\n");
                    }
                }

                info.append("\n").append(core.str("supported_wifi_adapter")).append(":\n");
                ArrayList<String> wireless = CommandOutput.execute("cat /proc/net/wireless", core);
                if (wireless != null) {
                    for (String line : wireless) {
                        info.append(line).append("\n");
                    }
                }

                ArrayList<String> usb = CommandOutput.execute("lsusb", core);
                if (usb != null) {
                    for (String u : usb) {
                        info.append("  ").append(u.trim()).append("\n");
                    }
                }

            } catch (Exception e) {
                info.append(core.str("error")).append(": ").append(e.getMessage());
            }
            String finalInfo = info.toString();
            getActivity().runOnUiThread(() -> {
                statusText.setText(core.str("finished"));
                driverInfo.setText(finalInfo);
            });
        });
    }
}

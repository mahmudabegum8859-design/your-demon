package com.opx.yourdemon.firewall;


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

public class FirewallDetectionFragment extends Fragment {

    public Core core;
    public TextView statusText, resultText;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.firewall_detection, container, false);
        core = new Core(getContext());

        statusText = view.findViewById(R.id.status_text);
        resultText = view.findViewById(R.id.result_text);

        checkFirewall();

        view.findViewById(R.id.refresh_firewall).setOnClickListener(v -> checkFirewall());

        return view;
    }

    private void checkFirewall() {
        statusText.setText(core.str("checking_firewall"));
        TaskRunner.execute(() -> {
            StringBuilder result = new StringBuilder();
            try {
                ArrayList<String> iptables = CommandOutput.execute("iptables -L -n 2>/dev/null", core);
                if (iptables != null && !iptables.isEmpty()) {
                    boolean hasRules = false;
                    for (String line : iptables) {
                        if (line.contains("DROP") || line.contains("REJECT") || line.contains("target")) {
                            hasRules = true;
                            result.append(line).append("\n");
                        }
                    }
                    if (!hasRules) {
                        result.append(core.str("no_firewall"));
                    } else {
                        result.insert(0, core.str("firewall_found") + ":\n\n");
                    }
                }

                ArrayList<String> ufw = CommandOutput.execute("ufw status 2>/dev/null", core);
                if (ufw != null && !ufw.isEmpty()) {
                    result.append("\n--- UFW ---\n");
                    for (String line : ufw) {
                        result.append(line).append("\n");
                    }
                }
            } catch (Exception e) {
                result.append(core.str("error")).append(": ").append(e.getMessage());
            }
            String finalResult = result.toString();
            getActivity().runOnUiThread(() -> {
                statusText.setText(core.str("finished"));
                resultText.setText(finalResult);
            });
        });
    }
}

package com.opx.yourdemon.captive_portal;


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

public class CaptivePortalFragment extends Fragment {

    public Core core;
    public TextView statusText, logText;
    public EditText ifaceInput, ssidInput;
    public MaterialButton startBtn, stopBtn;

    private boolean running = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.captive_portal, container, false);
        core = new Core(getContext());

        statusText = view.findViewById(R.id.status_text);
        logText = view.findViewById(R.id.log_text);
        ifaceInput = view.findViewById(R.id.iface_input);
        ssidInput = view.findViewById(R.id.ssid_input);
        startBtn = view.findViewById(R.id.start_portal);
        stopBtn = view.findViewById(R.id.stop_portal);

        ifaceInput.setText("wlan0");
        ssidInput.setText("YourDemon-FreeWiFi");

        startBtn.setOnClickListener(v -> startPortal());
        stopBtn.setOnClickListener(v -> stopPortal());
        stopBtn.setEnabled(false);

        return view;
    }

    private void startPortal() {
        String iface = ifaceInput.getText().toString().trim();
        String ssid = ssidInput.getText().toString().trim();

        if (iface.isEmpty() || ssid.isEmpty()) { core.toaster(core.str("please_fill")); return; }

        running = true;
        startBtn.setEnabled(false);
        stopBtn.setEnabled(true);
        statusText.setText(core.str("starting_hotspot"));

        String ifaceFinal = iface;
        String ssidFinal = ssid;
        TaskRunner.execute(() -> {
            try {
                getActivity().runOnUiThread(() -> {
                    statusText.setText(core.str("setting_up_interface"));
                    logText.append(core.str("setting_up_interface") + "\n");
                });
                exec("mkdir -p /storage/emulated/0/YourDemon/portal");
                exec("ip link set " + ifaceFinal + " down");
                exec("ip addr add 192.168.4.1/24 dev " + ifaceFinal);
                exec("ip link set " + ifaceFinal + " up");

                getActivity().runOnUiThread(() -> {
                    statusText.setText(core.str("enable_ip_forward"));
                    logText.append(core.str("enable_ip_forward") + "\n");
                });
                exec("echo 1 > /proc/sys/net/ipv4/ip_forward");

                getActivity().runOnUiThread(() -> {
                    statusText.setText(core.str("setting_up_iptables"));
                    logText.append(core.str("setting_up_iptables") + "\n");
                });
                exec("iptables -t nat -A POSTROUTING -o " + getWanInterface() + " -j MASQUERADE");
                exec("iptables -A FORWARD -i " + ifaceFinal + " -o " + getWanInterface() + " -j ACCEPT");
                exec("iptables -A FORWARD -i " + getWanInterface() + " -o " + ifaceFinal + " -m state --state RELATED,ESTABLISHED -j ACCEPT");

                getActivity().runOnUiThread(() -> {
                    statusText.setText(core.str("starting_hostapd"));
                    logText.append(core.str("starting_hostapd") + "\n");
                });
                String hostapdConf = "interface=" + ifaceFinal + "\ndriver=nl80211\nssid=" + ssidFinal + "\nhw_mode=g\nchannel=6\nwmm_enabled=0\nmacaddr_acl=0\nauth_algs=1\nignore_broadcast_ssid=0\n";
                exec("echo '" + hostapdConf + "' > /storage/emulated/0/YourDemon/portal/hostapd.conf");
                exec(core.EXECUTE + "hostapd /sdcard/YourDemon/portal/hostapd.conf -B");

                getActivity().runOnUiThread(() -> {
                    statusText.setText(core.str("starting_dns_dhcp"));
                    logText.append(core.str("starting_dns_dhcp") + "\n");
                });
                String dnsmasqConf = "interface=" + ifaceFinal + "\ndhcp-range=192.168.4.2,192.168.4.100,255.255.255.0,12h\ndhcp-option=3,192.168.4.1\ndhcp-option=6,192.168.4.1\naddress=/#/192.168.4.1\n";
                exec("echo '" + dnsmasqConf + "' > /storage/emulated/0/YourDemon/portal/dnsmasq.conf");
                exec(core.EXECUTE + "dnsmasq -C /sdcard/YourDemon/portal/dnsmasq.conf");

                getActivity().runOnUiThread(() -> {
                    statusText.setText(core.str("starting_web_server"));
                    logText.append(core.str("starting_web_server") + "\n");
                });
                exec(core.EXECUTE + "python3 -m http.server 80 --bind 192.168.4.1 --directory /sdcard/YourDemon/portal &>/dev/null &");

                getActivity().runOnUiThread(() -> {
                    statusText.setText(core.str("portal_running"));
                    logText.append(core.str("portal_running") + "\n");
                });
            } catch (Exception e) {
                String errorMsg = core.str("error") + ": " + e.getMessage();
                getActivity().runOnUiThread(() -> {
                    statusText.setText(errorMsg);
                    logText.append(errorMsg + "\n");
                });
            }
        });
    }

    private void stopPortal() {
        running = false;
        TaskRunner.execute(() -> {
            String iface = ifaceInput.getText().toString().trim();
            try {
                exec("killall hostapd");
                exec("killall dnsmasq");
                exec("killall python3");
                exec("iptables -t nat -D POSTROUTING -o " + getWanInterface() + " -j MASQUERADE");
                exec("iptables -D FORWARD -i " + iface + " -o " + getWanInterface() + " -j ACCEPT");
                exec("iptables -D FORWARD -i " + getWanInterface() + " -o " + iface + " -m state --state RELATED,ESTABLISHED -j ACCEPT");
                exec("echo 0 > /proc/sys/net/ipv4/ip_forward");
            } catch (Exception e) {
                String errorMsg = "Cleanup error: " + e.getMessage() + "\n";
                getActivity().runOnUiThread(() -> logText.append(errorMsg));
            }
            getActivity().runOnUiThread(() -> {
                startBtn.setEnabled(true);
                stopBtn.setEnabled(false);
                statusText.setText(core.str("portal_stopped"));
            });
        });
    }

    private String getWanInterface() {
        try {
            Process process = Runtime.getRuntime().exec("su");
            java.io.OutputStream stdin = process.getOutputStream();
            java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream()));
            stdin.write(("ip route | grep default | awk '{print $5}' | head -1\n").getBytes());
            stdin.write(("exit\n").getBytes());
            stdin.flush();
            stdin.close();
            String line = br.readLine();
            process.waitFor();
            process.destroy();
            if (line != null && !line.trim().isEmpty()) {
                return line.trim();
            }
        } catch (Exception e) {
            logText.append("WAN detection error: " + e.getMessage() + "\n");
        }
        return "wlan0";
    }

    private void exec(String cmd) throws Exception {
        CustomCommand.execute(cmd, core);
    }
}

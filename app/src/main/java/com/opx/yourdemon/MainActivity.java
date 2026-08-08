package com.opx.yourdemon;


import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.core.view.MotionEventCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;

import com.opx.yourdemon.interface_manager.InterfaceManagerFragment;
import com.opx.yourdemon.commands_manager.CommandsManagerFragment;
import com.opx.yourdemon.actions.ActionsFragment;
import com.opx.yourdemon.pmkid.PmkidCaptureFragment;
import com.opx.yourdemon.mac_changer.MacChangerFragment;
import com.opx.yourdemon.arp_scan.ArpScanFragment;
import com.opx.yourdemon.captive_portal.CaptivePortalFragment;
import com.opx.yourdemon.evil_twin.EvilTwinFragment;
import com.opx.yourdemon.driver_check.DriverCheckFragment;
import com.opx.yourdemon.wifi_info.WifiInfoFragment;
import com.opx.yourdemon.wifi_password_history.WifiPasswordHistoryFragment;
import com.opx.yourdemon.vnc.VncFragment;
import com.opx.yourdemon.firewall.FirewallDetectionFragment;
import com.opx.yourdemon.adapter_suggester.AdapterSuggesterFragment;
import com.opx.yourdemon.phone_detection.PhoneDetectionFragment;
import com.opx.yourdemon.wps_interface.WpsInterfaceFragment;

import com.opx.yourdemon.appintro.AppIntroActivity;
import com.opx.yourdemon.coremanger.CoreManager;
import com.opx.yourdemon.exploit_hub.ExploitScreen;
import com.opx.yourdemon.geomac.GeoMac;
import com.opx.yourdemon.handshakes.HandshakeStorage;
import com.opx.yourdemon.hydra.HydraFragment;
import com.opx.yourdemon.local_network.LocalMain;
import com.opx.yourdemon.metasploit.MsfConsole;
import com.opx.yourdemon.modules.ModulesFragment;
import com.opx.yourdemon.nmap.NmapScanner;
import com.opx.yourdemon.nuclei.NucleiFragment;
import com.opx.yourdemon.payload_generator.PayloadFragment;
import com.opx.yourdemon.router_scan.RouterScanMain;
import com.opx.yourdemon.searchsploit.SearchSploit;
import com.opx.yourdemon.searchsploit_web.WebSploitFragment;
import com.opx.yourdemon.captive_portal_web.PortalWebFragment;
import com.opx.yourdemon.three_wifi.LoginPage;
import com.opx.yourdemon.utils.CheckDir;
import com.opx.yourdemon.utils.Core;
import com.opx.yourdemon.utils.CustomCommand;
import com.opx.yourdemon.utils.TaskRunner;
import com.opx.yourdemon.utils.OnSwipeListener;
import com.opx.yourdemon.wifi.Wifi;
import com.opx.yourdemon.wifi.utils.GetInterfaces;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity {
    public Core core;
    public int versionInt = BuildConfig.VERSION_CODE;
    public boolean usbstate = false;
    public int eggcounter = 0;
    public Fragment tempfrag;

    public ExpandableLayout menu;
    private View navIndicator;
    private LinearLayout navBar;
    private int navCount = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        core = new Core(this);
        int night = core.getInt("night");
         menu = findViewById(R.id.menu_expand);
        boolean hasRoot = core.checkroot();
        core.putBoolean("has_root", hasRoot);
        checkforusb();

        if (!hasRoot) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.flContent, new Dashboard()).commit();
        } else {
            try {
                if (CheckDir.check("/data/local/YourDemon/beta/usr")){
                    Intent update = new Intent(this, AppIntroActivity.class);
                    update.putExtra("update",true);
                    startActivity(update);
                }
                else if (!CheckDir.check("/data/local/YourDemon/release/usr")){
                    Intent install = new Intent(this, AppIntroActivity.class);
                    install.putExtra("update",false);
                    startActivity(install);
                }
                else{
                    core.mountcore();FragmentManager fragmentManager = getSupportFragmentManager();
                    if (!CheckDir.check("/data/local/YourDemon/release/sdcard/YourDemon")){
                        fragmentManager.beginTransaction().replace(R.id.flContent, new Error()).commit();
                    }else{
                        fragmentManager.beginTransaction().replace(R.id.flContent, new Dashboard()).commit();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        copyAssets();
        checkforusb();
        core.putString("chroot_path", "/data/local/YourDemon/release/");

        TaskRunner.execute(() -> CustomCommand.execute("chmod 777 -R /data/data/com.opx.yourdemon/", core));
        if (night==0 && core.getBoolean("first_open")){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }else if (night==1){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }else{
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }
        FragmentManager fragmentManager = getSupportFragmentManager();

        setupBottomNav(fragmentManager);
    }

    /**
     * Wires the animated bottom navigation bar.
     * Icon-only tabs: Home, WiFi, Portal, Evil Twin, Settings.
     * A neon gradient pill slides + pops to the selected tab.
     */
    private void setupBottomNav(FragmentManager fragmentManager) {
        navIndicator = findViewById(R.id.nav_indicator);
        navBar = findViewById(R.id.bottom_nav_items);

        LinearLayout dash = findViewById(R.id.nav_dashboard);
        LinearLayout wifi = findViewById(R.id.nav_wifi);
        LinearLayout portal = findViewById(R.id.nav_portal);
        LinearLayout twin = findViewById(R.id.nav_twin);
        LinearLayout settingsTab = findViewById(R.id.nav_settings);

        dash.setOnClickListener(v -> {
            setNavSelected(0);
            fragmentManager.beginTransaction().replace(R.id.flContent, new Dashboard()).commit();
        });
        wifi.setOnClickListener(v -> {
            setNavSelected(1);
            fragmentManager.beginTransaction().replace(R.id.flContent, new Wifi()).commit();
        });
        portal.setOnClickListener(v -> {
            setNavSelected(2);
            fragmentManager.beginTransaction().replace(R.id.flContent, new CaptivePortalFragment()).commit();
        });
        twin.setOnClickListener(v -> {
            setNavSelected(3);
            fragmentManager.beginTransaction().replace(R.id.flContent, new EvilTwinFragment()).commit();
        });
        settingsTab.setOnClickListener(v -> {
            setNavSelected(4);
            fragmentManager.beginTransaction().replace(R.id.flContent, new Settings()).commit();
        });

        // Start on Home
        navBar.post(() -> setNavSelected(0));
    }

    /**
     * Animates the indicator pill to the tab at the given index and updates selection state.
     */
    private void setNavSelected(int index) {
        if (navIndicator == null || navBar == null) return;
        int itemWidth = navBar.getWidth() / navCount;
        if (itemWidth <= 0) return;

        float targetX = itemWidth * index + (itemWidth - navIndicator.getWidth()) / 2f;

        // Slide the pill
        navIndicator.animate()
                .translationX(targetX)
                .setDuration(280)
                .setInterpolator(new DecelerateInterpolator(2f))
                .start();

        // Bouncy pop
        navIndicator.animate()
                .scaleX(1.25f)
                .scaleY(1.25f)
                .setDuration(140)
                .withEndAction(() -> navIndicator.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(180)
                        .setInterpolator(new DecelerateInterpolator(1.5f))
                        .start())
                .start();

        for (int i = 0; i < navBar.getChildCount(); i++) {
            View child = navBar.getChildAt(i);
            child.setSelected(i == index);
        }
    }



    /**
     * This function is used to show the dialog box when the user clicks on the USB button.
     */
    public void usbdialog(){
        final BottomSheetDialog usbdialog = new BottomSheetDialog(this, R.style.AppBottomSheetDialogTheme);
        usbdialog.setContentView(R.layout.usb_dialog);
        TextView info = usbdialog.findViewById(R.id.usb_info);
        Button changelisten = usbdialog.findViewById(R.id.change_listen);
        Button changedeauth = usbdialog.findViewById(R.id.change_deauth);
        info.setText("["+getpid()+"] "+core.getDeviceNameByPid(getpid()));
        assert changelisten != null;
        changelisten.setOnClickListener(view -> getWlanMonitore(true));
        assert changedeauth != null;
        changedeauth.setOnClickListener(view -> getWlanMonitore(false));
        usbdialog.show();
    }
    /**
     * It creates a dialog box that allows the user to pick a network interface
     *
     * @param isscan boolean, if true, the user is picking a wlan interface to scan with, if false, the
     * user is picking a wlan interface to deauth with
     */
    public void getWlanMonitore(boolean isscan) {
        ArrayList<String> w = null;
        try {
            w = getinterfaces();
        } catch (Exception e) {
            e.printStackTrace();
        }
        assert w != null;
        String[] w2 = new String[w.size()+1];
        for (int i = 0; i < w.size(); i++) {
            w2[i] = w.get(i);
        }
        w2[w2.length-1] = core.str("customvalue");
        new MaterialAlertDialogBuilder(this)
                .setTitle("Pick interface")
                .setItems(w2, (dialogInterface, i) -> {
                    if (i !=w2.length -1){
                        if (isscan) {
                            core.putString("wlan_scan", w2[i]);
                        } else {
                            core.putString("wlan_deauth", w2[i]);
                        }}else{
                        new Thread(() -> {
                            final String[] temp = {""};
                            runOnUiThread(() -> {
                                final Dialog valuedialog = new Dialog(this);
                                valuedialog.setContentView(R.layout.input_dialog);
                                valuedialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                TextView title = valuedialog.findViewById(R.id.input_title);
                                TextInputEditText valueedit = valuedialog.findViewById(R.id.getvalue);
                                TextView ok = valuedialog.findViewById(R.id.ok_button);
                                title.setText(core.str("customvalue"));
                                ok.setOnClickListener(view1 -> {
                                    temp[0] = Objects.requireNonNull(valueedit.getText()).toString();
                                    valuedialog.dismiss();
                                });
                                valuedialog.show();
                            });
                            while (temp[0].equals("")){
                                Log.d("t","test");
                            }
                            if (isscan) {
                                core.putString("wlan_scan", temp[0]);
                            } else {
                                core.putString("wlan_deauth", temp[0]);
                            }
                        }).start();

                    }
                })
                .show();
    }

    /**
     * If the user is not rooted, show an error message and exit the app
     */
    public void noroot() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.error)
                .setMessage(core.str("noroot"))
                .setCancelable(false)
                .setPositiveButton("OK", (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    System.exit(0);
                })
                .show();
    }


    /**
     * Check if the process is connected to the network.
     *
     * @return A boolean value.
     */
    public boolean isConnected() {
        return getpid() != null;
    }



    /**
     * It copies all the files from the assets folder to the /data/data/com.opx.yourdemon/files/
     * folder
     */
    private void copyAssets() {
        AssetManager assetManager = getAssets();
        String[] files = null;
        try {
            files = assetManager.list("");
        } catch (IOException e) {
            Log.e("tag", "Failed to get asset file list.", e);
        }
        if (files != null) for (String filename : files) {
            InputStream in = null;
            OutputStream out = null;
            try {
                in = assetManager.open(filename);
                File outFile = new File("/data/data/com.opx.yourdemon/files/", filename);
                out = new FileOutputStream(outFile);
                copyFile(in, out);
            } catch (IOException e) {
                Log.e("tag", "Failed to copy asset file: " + filename, e);
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        // NOOP
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        // NOOP
                    }
                }
            }
        }
        TaskRunner.execute(() -> CustomCommand.execute("chmod 777 -R /data/data/com.opx.yourdemon/", new Core(this)));
    }


    // Copying a file from one location to another.
    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    /**
     * Prints a message on the screen
     *
     * @param msg The message to display in the toast.
     */
    public void toaster(String msg) {
        Toast toast = Toast.makeText(this,
                msg, Toast.LENGTH_SHORT);
        toast.show();
    }




    /**
     * This function returns a list of all the interfaces that are currently up and running
     *
     * @return An ArrayList of Strings.
     */
    private ArrayList<String> getinterfaces() {
        return  core.getInterfacesList();
    }


    /**
     * If the user has not given permission to write to the external storage, then request permission
     *
     * @return Nothing.
     */
    public boolean checkpermission() {
            if (checkSelfPermission(WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{WRITE_EXTERNAL_STORAGE},
                        123
                );
            }
        return checkSelfPermission(WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }


    /**
     * This function checks if the USB is connected to the device every 3 seconds
     */
    public void checkforusb(){
        Timer usb = new Timer();
        usb.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                boolean temp = usbstate;
                usbstate = isConnected();
                if (temp != usbstate && usbstate){
                    runOnUiThread(() -> usbdialog());
                }
            }
        },0,3000);
    }
    /**
     * Get the device id of the connected device
     *
     * @return The device id of the connected device.
     */
    public String getpid(){
        String deviceid = null;
        UsbManager manager = (UsbManager) MainActivity.this.getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> devices = manager.getDeviceList();
        for (String deviceName : devices.keySet()) {
            UsbDevice device = devices.get(deviceName);
            assert device != null;
            StringBuilder string2 = new StringBuilder(Integer.toHexString(device.getVendorId()));
            while (string2.length() < 4) {
                string2.insert(0, "0");
            }
            StringBuilder string3 = new StringBuilder(Integer.toHexString(device.getProductId()));
            while (string3.length() < 4) {
                string3.insert(0, "0");
            }
            deviceid = string2 + ":" + string3;
        }
        return deviceid;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}

package com.opx.yourdemon.wifi;



import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.opx.yourdemon.R;
import com.opx.yourdemon.custom.WiFINetwork;
import com.opx.yourdemon.utils.CheckFile;
import com.opx.yourdemon.utils.Core;
import com.opx.yourdemon.utils.CustomCommand;
import com.opx.yourdemon.utils.TaskRunner;
import com.opx.yourdemon.utils.MoveFile;
import com.opx.yourdemon.wifi.utils.BrutePsk;
import com.opx.yourdemon.wifi.utils.BruteWps;
import com.opx.yourdemon.wifi.utils.CheckHandshake;
import com.opx.yourdemon.wifi.utils.CustomPin;
import com.opx.yourdemon.wifi.utils.DisableMonitor;
import com.opx.yourdemon.wifi.utils.EnableMonitor;
import com.opx.yourdemon.wifi.utils.GetInterfaces;
import com.opx.yourdemon.wifi.utils.LaunchAirodump;
import com.opx.yourdemon.wifi.utils.PixieDust;
import com.opx.yourdemon.wifi.utils.StartDeauth;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class WiFI_Adapter extends RecyclerView.Adapter<WiFI_Adapter.ViewHolder> {
    public ArrayList<WiFINetwork> wifilist;
    public Context context;
    public Activity activity;
    public int tag = 0;
    public Timer deauth;
    public Core core;
    public boolean three_wifi;

    public WiFI_Adapter(Context context2, Activity mActivity, ArrayList<WiFINetwork> wifi) {
        context = context2;
        wifilist = wifi;
        activity = mActivity;
        try {Collections.sort(wifi, new WiFINetwork.WiFIComporator());}
        catch (Exception ignored){}
        core = new Core(context2);

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.wifi_item, parent, false);
        return new ViewHolder(v);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint("SetTextI18n")
    @Override
    // The above code is binding the data to the view holder.
    public void onBindViewHolder(@NonNull ViewHolder adapter, @SuppressLint("RecyclerView") final int position) {
        if (!new Core(context).getBoolean("hide")) {
            adapter.wifi_mac.setText(wifilist.get(position).getMac().toUpperCase(Locale.ROOT));
        } else {
            adapter.wifi_mac.setText("XX:XX:XX:XX:XX");
        }
        adapter.wifi_power.setText("  " + (100 - wifilist.get(position).getPower()) + "%");
        if (wifilist.get(position).getIs5hhz()) {
            adapter.wifi_name.setText(wifilist.get(position).getSsid());
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                adapter.wifi_name.setText(Html.fromHtml(wifilist.get(position).getSsid() + " <b>â‘¤</b>", Html.FROM_HTML_MODE_COMPACT));
            } else {
                adapter.wifi_name.setText(Html.fromHtml(wifilist.get(position).getSsid() + " <b>â‘¤</b>"));
            }
        }

        if (wifilist.get(position).getWps() && !wifilist.get(position).getBlocked()) {
            adapter.iswps.setTextColor(context.getColor(R.color.green));
        } else if (wifilist.get(position).getBlocked()) {
            adapter.iswps.setTextColor(context.getColor(R.color.red));
            adapter.iswps.setText("Locked");
        } else {
            adapter.iswps.setTextColor(context.getColor(R.color.red));
        }
        if (wifilist.get(position).getOK()) {
            adapter.wifi_name.setTextColor(Color.parseColor("#FF1B5E20"));
            adapter.icon.setVisibility(View.VISIBLE);
            if (wifilist.get(position).isThree()){
                adapter.icon.setImageDrawable(context.getDrawable(R.drawable.three_wifi_database));
            }
        }
        if (wifilist.get(position).getModel() != null) {
            String modelka = wifilist.get(position).getModel();
            adapter.wifi_model.setText("Model: " + modelka);
            if (core.checkmodel(modelka)){adapter.icon.setVisibility(View.VISIBLE);adapter.icon.setImageDrawable(context.getDrawable(R.drawable.star));
            }
        } else {
            adapter.wifi_model.setVisibility(View.INVISIBLE);
        }
        adapter.card.setOnClickListener(view -> WifiDialog(wifilist.get(position)));

    }

    private void WifiDialog(WiFINetwork selected) {
        String name = selected.getSsid();
        String mac = selected.getMac();
        String channel = selected.getChannel();
        boolean wps = selected.getWps();
        boolean blocked = selected.getBlocked();
        boolean three_wifi = selected.getOK();
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context, R.style.AppBottomSheetDialogTheme);
        bottomSheetDialog.setContentView(R.layout.wifi_bottom);
        TextView name1 = bottomSheetDialog.findViewById(R.id.wifi_name_bottom);
        ImageView wifiimg = bottomSheetDialog.findViewById(R.id.dialog_wifi_img);

        TextView mac1 = bottomSheetDialog.findViewById(R.id.wifi_mac_bottom);
        TextView res1 = bottomSheetDialog.findViewById(R.id.getedpass);
        TextView res2 = bottomSheetDialog.findViewById(R.id.getedpin);
        TextView output = bottomSheetDialog.findViewById(R.id.output);
        String wlan_listen = core.getString("wlan_scan");
        final String[] wlan_deauth = {core.getString("wlan_deauth")};
        LinearLayout deauther = bottomSheetDialog.findViewById(R.id.deauther);
        LinearLayout try_handshake = bottomSheetDialog.findViewById(R.id.handshake);
        LinearLayout custom_pin = bottomSheetDialog.findViewById(R.id.custom_pin);
        LinearLayout brute_psk = bottomSheetDialog.findViewById(R.id.brute_psk);
        Button back = bottomSheetDialog.findViewById(R.id.back);
        Button main_cancel = bottomSheetDialog.findViewById(R.id.cancel_attack);
        ProgressBar attack_progress = bottomSheetDialog.findViewById(R.id.attacking_progress);
        ExpandableLayout exp_main = bottomSheetDialog.findViewById(R.id.expand);
        ExpandableLayout exp_attack = bottomSheetDialog.findViewById(R.id.expand_console);
        ExpandableLayout exp_result = bottomSheetDialog.findViewById(R.id.expand_result);

        brute_psk.setOnClickListener(view -> {

            core.scale(wifiimg,0.65F);
            core.scale(attack_progress,1.0F);
            output.setText(R.string.start_brute);
            final BrutePsk[] brute = {null};
            main_cancel.setOnClickListener(view1 -> {
                if (brute[0] !=null){
                    brute[0].kill();
                    exp_attack.collapse();
                    exp_main.expand();
                    core.scale(wifiimg,1.0F);
                    core.scale(attack_progress,0.0F);
                }
            });
            ArrayList<String> get = core.getListFiles(new File(core.getStorage() + "YourDemon/wordlist"));
            if (!get.isEmpty()){
            String[] w2 = new String[get.size()];
            for (int i = 0; i < get.size(); i++) {
                w2[i] = get.get(i).replace(core.getStorage() + "YourDemon/wordlist/", "");
            }
            new MaterialAlertDialogBuilder(context)
                    .setTitle(R.string.select_word2)
                    .setItems(w2, (dialogInterface, i) -> {
                        String path = get.get(i);
                        core.toaster(path);
                        new Thread(() -> {
                            brute[0] = new BrutePsk(activity,output,name,core,path);
                            WiFINetwork w = brute[0].execute();
                            if (w.getOK()){
                                activity.runOnUiThread(() -> {
                                    core.scale(wifiimg,1.0F);
                                    core.scale(attack_progress,0.0F);
                                });
                                settext(core.str("suc_pass")+w.getPsk(), output);
                                core.savenetwork(mac,w.getPsk(),"-");
                            }else {
                                activity.runOnUiThread(() -> {
                                    core.scale(wifiimg,1.0F);
                                    core.scale(attack_progress,0.0F);
                                });
                                settext(core.str("br_failed"),output);
                            }
                        }).start();
                    })
                    .show();exp_attack.expand();
                exp_main.collapse();}
            else{
                core.scale(wifiimg,1.0F);
                core.scale(attack_progress,0.0F);
            exp_attack.expand();
            exp_main.collapse();
            output.setText(R.string.error_no_word);
            main_cancel.setOnClickListener(view12 -> {
                exp_attack.collapse();
                exp_main.expand();
            });
            }
        });
        custom_pin.setOnClickListener(view -> {
            core.scale(wifiimg,0.65F);
            core.scale(attack_progress,1.0F);
            output.setText(R.string.trying_connect);
            final Dialog valuedialog = new Dialog(context);
            valuedialog.setContentView(R.layout.input_dialog);
            valuedialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            TextView title = valuedialog.findViewById(R.id.input_title);
            TextInputEditText valueedit = valuedialog.findViewById(R.id.getvalue);
            TextView ok = valuedialog.findViewById(R.id.ok_button);
            title.setText(R.string.enter_pin);
            ok.setOnClickListener(view1 -> {
                String value = valueedit.getText().toString();
                valuedialog.dismiss();
                exp_main.collapse();
                exp_attack.expand();
                new Thread(() -> {
                    settext(core.str("try_connect"), output);
                    WiFINetwork w = CustomPin.execute(value, activity, output, mac, wlan_listen, core, null);
                    if (w.getOK()){
                        activity.runOnUiThread(() -> {
                            core.scale(wifiimg,1.0F);
                            core.scale(attack_progress,0.0F);
                            exp_result.expand();
                            exp_attack.collapse();
                            res1.setText(core.str("pass")+w.getPsk());
                            core.savenetwork(mac,w.getPsk(),w.getPin());
                            res2.setText("");
                        });
                    }else{
                        activity.runOnUiThread(() -> {
                            core.scale(wifiimg,1.0F);
                            core.scale(attack_progress,0.0F);
                            exp_result.expand();
                            exp_attack.collapse();
                            res1.setText(R.string.error);
                            res2.setText(R.string.incorrect);
                        });
                    }
                }).start();
            });
            valuedialog.show();
        });
        assert back != null;
        back.setOnClickListener(view -> {
            exp_main.expand();
            exp_attack.collapse();
            exp_result.collapse();
            core.scale(wifiimg,1.0F);
            core.scale(attack_progress,0.0F);
        });
        if (three_wifi) {
            exp_main.collapse();
            exp_result.expand();
            res1.setText(core.str("pass") + selected.getPsk());
            res2.setText(core.str("pin") + selected.getPin());
            back.setEnabled(true);

        }
        LinearLayout pixiedust = bottomSheetDialog.findViewById(R.id.pixie);
        LinearLayout brutewps = bottomSheetDialog.findViewById(R.id.brute);


        if (wps && !blocked) {
            final BruteWps[] brute_wps = {null};
            brutewps.setOnClickListener(view -> {
                core.scale(wifiimg,0.65F);
                core.scale(attack_progress,1.0F);
                output.setText(R.string.start_brute);
                bottomSheetDialog.setOnDismissListener(dialogInterface -> {
                    TaskRunner.execute(() -> CustomCommand.execute("svc wifi enable", core));
                    if (brute_wps[0] != null) {
                        brute_wps[0].kill();
                    }

                });
                main_cancel.setOnClickListener(view2 -> {
                    exp_main.expand();
                    exp_attack.collapse();
                    exp_result.collapse();
                    TaskRunner.execute(() -> CustomCommand.execute("svc wifi enable", core));
                    if (brute_wps[0] != null) {
                        brute_wps[0].kill();
                    }
                    core.scale(wifiimg,1.0F);
                    core.scale(attack_progress,0.0F);
                });
                exp_main.collapse();
                exp_attack.expand();
                brute_wps[0] = new BruteWps(activity, output, mac, wlan_listen, new Core(context));
                main_cancel.setEnabled(true);
                Thread t = new Thread(() -> {
                    WiFINetwork result = brute_wps[0].execute();
                    if (!result.isCanceled()) {
                        if (result.getOK()) {
                            activity.runOnUiThread(() -> {
                                core.scale(wifiimg,1.0F);
                                core.scale(attack_progress,0.0F);
                                exp_attack.collapse();
                                exp_result.expand();
                                back.setEnabled(true);
                                if (result.getPsk() == null) {
                                    res2.setVisibility(View.GONE);
                                    res1.setText(core.str("pin") + result.getPin());
                                    core.savenetwork(mac,"-",result.getPin());
                                } else {
                                    res1.setText(core.str("pass")+ result.getPsk());
                                    res2.setText(core.str("pin") + result.getPin());
                                    core.savenetwork(mac,result.getPsk(),result.getPin());
                                }
                            });
                        } else {
                            activity.runOnUiThread(() -> {
                                core.scale(wifiimg,1.0F);
                                core.scale(attack_progress,0.0F);
                                exp_attack.collapse();
                                exp_result.expand();
                                if (result.getLon() != null) {
                                    res1.setText(R.string.ooops_sh);
                                    res2.setText(core.str("error_interface") + wlan_listen + core.str("dev_issue"));
                                } else {
                                    res1.setText(R.string.ooops_sh);
                                    res2.setText(R.string.not_vuln_pixie);
                                }
                                back.setEnabled(true);
                            });
                        }
                    } else {
                        activity.runOnUiThread(() -> {
                            exp_attack.collapse();
                            exp_result.collapse();
                            exp_main.expand();
                        });
                    }
                });
                t.start();

            });
            final PixieDust[] pixie = {null};
            pixiedust.setOnClickListener(view -> {
                core.scale(wifiimg,0.65F);
                core.scale(attack_progress,1.0F);
                output.setText(R.string.start_pixie);
                bottomSheetDialog.setOnDismissListener(dialogInterface -> {
                    TaskRunner.execute(() -> CustomCommand.execute("svc wifi enable", core));
                    pixie[0].kill();
                });
                main_cancel.setOnClickListener(view2 -> {
                    core.scale(wifiimg,1.0F);
                    core.scale(attack_progress,0.0F);
                    exp_main.expand();
                    exp_attack.collapse();
                    exp_result.collapse();
                    TaskRunner.execute(() -> CustomCommand.execute("svc wifi enable", core));
                    pixie[0].kill();
                });
                exp_main.collapse();
                exp_attack.expand();
                Button connect = bottomSheetDialog.findViewById(R.id.connect);
                Thread t = new Thread(() -> {
                    pixie[0] = new PixieDust(context, activity, output, mac, name, new Core(context));
                    WiFINetwork result = pixie[0].execute(null);
                    if (!result.isCanceled()) {
                        if (result.getOK()) {
                            activity.runOnUiThread(() -> {
                                core.scale(wifiimg,1.0F);
                                core.scale(attack_progress,0.0F);
                                exp_attack.collapse();
                                exp_result.expand();
                                back.setEnabled(true);
                                if (result.getPsk() == null) {
                                    res2.setVisibility(View.GONE);
                                    res1.setText(core.str("pin") + result.getPin());
                                    core.savenetwork(mac,"-",result.getPin());
                                } else {
                                    if (!name.equals("Hidden network")){
                                    connect.setVisibility(View.VISIBLE);
                                    connect.setOnClickListener(view15 -> core.connectWiFi2(name,result.getPsk()));}
                                    res1.setText(core.str("pass") + result.getPsk());
                                    res2.setText(core.str("pin") + result.getPin());
                                    core.savenetwork(mac,result.getPsk(),result.getPin());
                                }
                            });
                        } else {
                            activity.runOnUiThread(() -> {
                                core.scale(wifiimg,1.0F);
                                core.scale(attack_progress,0.0F);
                                exp_attack.collapse();
                                exp_result.expand();
                                if (result.getLon() != null) {
                                    res1.setText(R.string.ooops_sh);
                                    res2.setText(R.string.error_interface + wlan_listen + R.string.dev_issue);
                                } else {
                                    res1.setText(R.string.ooops_sh);
                                    res2.setText(R.string.not_vuln_pixie);
                                }
                                back.setEnabled(true);
                            });
                        }
                    } else {
                        activity.runOnUiThread(() -> {
                            exp_attack.collapse();
                            exp_result.collapse();
                            exp_main.expand();
                            core.scale(wifiimg,1.0F);
                            core.scale(attack_progress,0.0F);
                        });
                    }
                });
                t.start();

            });
        } else {
            brutewps.setVisibility(View.GONE);
            pixiedust.setVisibility(View.GONE);
            custom_pin.setVisibility(View.GONE);
        }
        try_handshake.setOnClickListener(view -> {
            core.scale(wifiimg,0.65F);
            core.scale(attack_progress,1.0F);
            output.setText(R.string.start_airdump);
            exp_main.collapse();
            exp_attack.expand();
            main_cancel.setEnabled(true);
            main_cancel.setOnClickListener(view13 -> {
                exp_main.expand();
                exp_attack.collapse();
                exp_result.collapse();
                if (deauth != null) {
                    if (deauth!=null){deauth.cancel();}
                }
                core.scale(wifiimg,1.0F);
                core.scale(attack_progress,0.0F);
                TaskRunner.execute(() -> DisableMonitor.execute(wlan_listen, core, null));
                if (!wlan_listen.equals(wlan_deauth[0])) {
                    TaskRunner.execute(() -> DisableMonitor.execute(wlan_deauth[0], core, null));
                }
            });
            Thread t = new Thread(() -> {
                TaskRunner.execute(() -> CustomCommand.execute("rm /storage/emulated/0/YourDemon/hs/handshake-01.cap", new Core(context)));
                final Boolean[] success = {false};
                if (wlan_listen.equals(wlan_deauth[0]) && wlan_deauth[0].equals("wlan0")) {
                    settext(core.str("try_wlan0"), output);
                    Boolean listen = EnableMonitor.execute(wlan_listen, channel, new Core(context), null);
                    if (listen) {
                        settext(core.str("start_dump") + "\n", output);
                        LaunchAirodump airodump = new LaunchAirodump(mac, wlan_listen, new Core(context));
                        TaskRunner.execute(() -> airodump.execute());
                        bottomSheetDialog.setOnDismissListener(dialogInterface -> airodump.kill());
                        Timer cowpatty = new Timer();
                        final int[] time = {0};

                        cowpatty.scheduleAtFixedRate(new TimerTask() {
                            @Override
                            public void run() {
                                time[0] += 5;
                                try {
                                    if (CheckFile.check("/storage/emulated/0/YourDemon/hs/handshake-01.cap")) {
                                        if (!success[0]) {
                                            success[0] = CheckHandshake.execute(null);
                                            if (!success[0]) {
                                                settext("[" + time[0] + core.str("wait_hs"), output);
                                            } else {
                                                settext(core.str("hs_captured"), output);
                                            }
                                        } else {
                                            activity.runOnUiThread(() -> {
                                                core.scale(wifiimg,1.0F);
                                                core.scale(attack_progress,0.0F);
                                            });
                                            if (deauth!=null){deauth.cancel();}
                                            try {
                                                Boolean moved = MoveFile.execute("/storage/emulated/0/YourDemon/hs/handshake-01.cap", "/storage/emulated/0/YourDemon/hs/" + name + "(" + mac + ").cap");
                                                if (moved) {
                                                    settext(core.str("saved_to_hs") + "/storage/emulated/0/YourDemon/hs/" + name + " (" + mac + ").cap\n", output);

                                                } else {
                                                    settext(core.str("error_save_hs"), output);
                                                }
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }

                                            airodump.kill();

                                            if (cowpatty!=null){cowpatty.cancel();}
                                            TaskRunner.execute(() -> DisableMonitor.execute(wlan_listen, new Core(context), null));
                                        }
                                    } else {
                                        settext(core.str("cant_airodump"), output);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }, 4000, 5000);
                    }


                } else {
                    if (GetInterfaces.get(new Core(context)).contains(wlan_deauth[0])) {
                        settext(core.str("trying_put_inter"), output);
                        Boolean listen = EnableMonitor.execute(wlan_listen, channel, new Core(context), null);
                        Boolean listen2;
                        if (listen && wlan_deauth[0].equals(wlan_listen)) {
                            listen2 = true;
                        } else {
                            listen2 = EnableMonitor.execute(wlan_deauth[0], channel, new Core(context), null);
                        }
                        if (listen && listen2) {
                            settext(core.str("start_airdump") + "\n", output);
                            LaunchAirodump airodump = new LaunchAirodump(mac, wlan_listen, new Core(context));
                            TaskRunner.execute(() -> airodump.execute());

                            Timer cowpatty = new Timer();
                            cowpatty.scheduleAtFixedRate(new TimerTask() {
                                @Override
                                public void run() {
                                    try {

                                        if (CheckFile.check("/storage/emulated/0/YourDemon/hs/handshake-01.cap")) {
                                            if (!success[0]) {
                                                success[0] = CheckHandshake.execute(null);
                                                if (!success[0]) {
                                                    settext(core.str("wait_hs2"), output);
                                                } else {
                                                    settext(core.str("hs_captured"), output);
                                                    if (deauth != null) {
                                                        if (deauth!=null){deauth.cancel();}
                                                    }
                                                }
                                            } else {

                                                activity.runOnUiThread(() -> {
                                                    core.scale(wifiimg,1.0F);
                                                    core.scale(attack_progress,0.0F);
                                                });
                                                try {
                                                    Boolean moved = MoveFile.execute("/storage/emulated/0/YourDemon/hs/handshake-01.cap", "/storage/emulated/0/YourDemon/captured/" + name.replaceAll("\\s+", "") + "_" + mac + ".cap");
                                                    if (moved) {
                                                        settext(core.str("saved_to_hs") + "/storage/emulated/0/YourDemon/captured/" + name + "_" + mac + ".cap\n", output);

                                                    } else {
                                                        settext(core.str("error_save_hs"), output);
                                                    }
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                                airodump.kill();
                                                if (cowpatty!=null){cowpatty.cancel();}

                                                TaskRunner.execute(() -> DisableMonitor.execute(wlan_listen, new Core(context), null));
                                                if (!wlan_deauth[0].equals(wlan_listen)){
                                                    TaskRunner.execute(() -> DisableMonitor.execute(wlan_deauth[0], new Core(context), null));
                                                }
                                            }
                                        } else {
                                            settext(core.str("cant_airodump"), output);
                                            core.scale(wifiimg,1.0F);
                                            core.scale(attack_progress,0.0F);
                                            if (cowpatty!=null){cowpatty.cancel();}
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }, 5000, 5000);
                            deauth = new Timer();
                            deauth.scheduleAtFixedRate(new TimerTask() {
                                @Override
                                public void run() {
                                    StartDeauth startDeauth = new StartDeauth(mac, wlan_deauth[0], true, new Core(context));
                                    Boolean isok = startDeauth.execute(null);
                                    if (isok) {
                                        settext(core.str("deauthing"), output);
                                    } else {
                                        settext(core.str("went_wrog_play"), output);


                                        if (deauth!=null){deauth.cancel();}
                                    }
                                }
                            }, 10000, 20000);
                        } else {
                            settext(core.str("error_monit"), output);
                        }
                    } else {
                        activity.runOnUiThread(() -> {

                            exp_attack.collapse();
                            res1.setText(R.string.error);
                            res2.setText(R.string.no_deauth_int);
                            back.setEnabled(true);
                            exp_result.expand();
                        });

                    }

                }
            });
            t.start();
        });
        deauther.setOnClickListener(view -> {
            core.scale(wifiimg,0.65F);
            core.scale(attack_progress,1.0F);
            output.setText(R.string.deauth);
            bottomSheetDialog.setOnDismissListener(dialogInterface -> TaskRunner.execute(() -> DisableMonitor.execute(wlan_deauth[0], core, null)));
            exp_attack.expand();
            exp_main.collapse();
            if (!wlan_deauth[0].equals("wlan0")) {
                ArrayList<String> wlans = GetInterfaces.get(new Core(context));
                if (wlans.contains(wlan_deauth[0] +"mon")){
                    wlan_deauth[0] = wlan_deauth[0] +"mon";}
                if (wlans.contains(wlan_deauth[0])) {
                    if (EnableMonitor.execute(wlan_deauth[0], channel, new Core(context), null)) {
                        StartDeauth startDeauth = new StartDeauth(mac, wlan_deauth[0], false, new Core(context));
                        main_cancel.setOnClickListener(view14 -> {
                            exp_attack.collapse();
                            exp_main.expand();
                            startDeauth.kill();
                            TaskRunner.execute(() -> DisableMonitor.execute(wlan_deauth[0], core, null));
                        });
                        main_cancel.setEnabled(true);
                        settext(core.str("deauthing"), output);
                        TaskRunner.execute(() -> startDeauth.execute(null));
                    } else {
                        core.scale(wifiimg,1.0F);
                        core.scale(attack_progress,0.0F);
                        back.setEnabled(true);
                        exp_attack.collapse();
                        exp_result.expand();
                        res1.setText(R.string.error);
                        res2.setText(R.string.error_monit);
                    }
                } else {
                    core.scale(wifiimg,1.0F);
                    core.scale(attack_progress,0.0F);
                    back.setEnabled(true);
                    exp_attack.collapse();
                    exp_result.expand();
                    res1.setText(R.string.error);
                    res2.setText(R.string.error_interface);

                }
            } else {
                core.scale(wifiimg,1.0F);
                core.scale(attack_progress,0.0F);
                back.setEnabled(true);
                exp_attack.collapse();
                exp_result.expand();
                res1.setText(R.string.error);
                res2.setText(R.string.no_wlan0);
            }
        });
        name1.setText(name);
        if (!new Core(context).getBoolean("hide")) {
            mac1.setText(mac);
        } else {
            mac1.setText("XX:XX:XX:XX:XX");
        }
        bottomSheetDialog.show();

    }

    @Override
    public int getItemCount() {

        return wifilist.size();
    }

    public void toaster(String msg) {
        activity.runOnUiThread(() -> {
            Toast toast = Toast.makeText(context,
                    msg, Toast.LENGTH_SHORT);
            toast.show();
        });

    }

    public void settext(String text, TextView output) {
        activity.runOnUiThread(() -> output.setText(text));
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void changeitem(WiFINetwork temp, int pos) {
        activity.runOnUiThread(() -> {
            wifilist.set(pos, temp);
            notifyItemChanged(pos);

        });
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView wifi_name;
        public TextView wifi_mac;
        public TextView wifi_model;
        public TextView wifi_power;
        public TextView iswps;
        public MaterialCardView card;
        public ImageView icon;


        public ViewHolder(View v) {
            super(v);
            wifi_name = v.findViewById(R.id.wifi_name);
            wifi_mac = v.findViewById(R.id.wifi_bssid);
            wifi_model = v.findViewById(R.id.wifi_model);
            wifi_power = v.findViewById(R.id.wifi_power);
            iswps = v.findViewById(R.id.iswps);
            card = v.findViewById(R.id.item);
            icon = v.findViewById(R.id.icon_wifi);
        }

    }

}

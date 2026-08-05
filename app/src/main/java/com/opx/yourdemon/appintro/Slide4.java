package com.opx.yourdemon.appintro;



import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.opx.yourdemon.Dashboard;
import com.opx.yourdemon.R;
import com.opx.yourdemon.utils.CheckDir;
import com.opx.yourdemon.utils.Core;
import com.opx.yourdemon.utils.CustomCommand;
import com.opx.yourdemon.utils.TaskRunner;



public class Slide4 extends Fragment {


    public String chroot;
    public Core core;
    public Context context;
    public Activity activity;
    public ViewPager mPager;
    public Slide4(ViewPager p){
        mPager = p;
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.slide4, container, false);
        context = getContext();
        activity = getActivity();
        core = new Core(context);
        Animation fade = AnimationUtils.loadAnimation(context, R.anim.fade_in);
        LinearLayout layout = view.findViewById(R.id.slide_layout);
        ImageView img = view.findViewById(R.id.slide_img);
        TextView title = view.findViewById(R.id.slide_title);
        TextView desc = view.findViewById(R.id.slide_description);
        TextView progress_status = view.findViewById(R.id.slide_progress_text);
        LinearProgressIndicator progress = view.findViewById(R.id.slide_install_progress);
        MaterialButton button = view.findViewById(R.id.slide_button);
        MaterialButton button_no = view.findViewById(R.id.slide_button_no);

            button.setOnClickListener(view1 -> {
               TaskRunner.execute(() -> CustomCommand.execute("cp /data/data/com.opx.yourdemon/files/yourdemon /data/data/com.termux/files/usr/bin/yourdemon",core));
                TaskRunner.execute(() -> CustomCommand.execute("chmod 777 /data/data/com.termux/files/usr/bin/yourdemon",core));
                core.MoveNext(mPager);
            });

        button_no.setOnClickListener(view12 -> core.MoveNext(mPager));
        return view;
    }

}

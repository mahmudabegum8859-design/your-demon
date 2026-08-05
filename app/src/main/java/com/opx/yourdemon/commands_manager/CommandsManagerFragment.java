package com.opx.yourdemon.commands_manager;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.opx.yourdemon.R;
import com.opx.yourdemon.utils.Core;
import com.opx.yourdemon.utils.CustomCommand;
import com.opx.yourdemon.utils.TaskRunner;

import java.util.ArrayList;

public class CommandsManagerFragment extends Fragment {

    public Core core;
    public ListView commandList;
    public TextView statusText;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.commands_manager, container, false);
        core = new Core(getContext());

        commandList = view.findViewById(R.id.command_list);
        statusText = view.findViewById(R.id.status_text);

        loadCommands();

        view.findViewById(R.id.add_command).setOnClickListener(v -> showAddDialog());
        view.findViewById(R.id.run_all).setOnClickListener(v -> runAllCommands());
        view.findViewById(R.id.clear_commands).setOnClickListener(v -> clearCommands());

        return view;
    }

    private void loadCommands() {
        ArrayList<String> cmds = core.getListString("saved_commands");
        if (cmds.isEmpty()) {
            statusText.setText(core.str("no_commands"));
        } else {
            statusText.setText(core.str("commands_count") + ": " + cmds.size());
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, cmds);
            commandList.setAdapter(adapter);
            commandList.setOnItemClickListener((parent, view1, position, id) -> {
                String cmd = cmds.get(position);
                new MaterialAlertDialogBuilder(getContext())
                        .setTitle(cmd)
                        .setItems(new String[]{core.str("run"), core.str("delete")}, (dialog, which) -> {
                            if (which == 0) {
                                executeCommand(cmd);
                            } else {
                                ArrayList<String> updated = core.getListString("saved_commands");
                                updated.remove(position);
                                core.putListString("saved_commands", updated);
                                loadCommands();
                            }
                        }).show();
            });
        }
    }

    private void showAddDialog() {
        EditText input = new EditText(getContext());
        new MaterialAlertDialogBuilder(getContext())
                .setTitle(core.str("add_command"))
                .setView(input)
                .setPositiveButton(core.str("save"), (dialog, which) -> {
                    String cmd = input.getText().toString().trim();
                    if (!cmd.isEmpty()) {
                        ArrayList<String> cmds = core.getListString("saved_commands");
                        cmds.add(cmd);
                        core.putListString("saved_commands", cmds);
                        loadCommands();
                    }
                })
                .setNegativeButton(core.str("cancel"), null)
                .show();
    }

    private void executeCommand(String cmd) {
        statusText.setText(core.str("running") + ": " + cmd);
        String cmdFinal = cmd;
        TaskRunner.execute(() -> {
            boolean result = false;
            try {
                result = CustomCommand.execute(cmdFinal, core);
            } catch (Exception ignored) {}
            boolean finalResult = result;
            getActivity().runOnUiThread(() -> {
                if (finalResult) {
                    statusText.setText(core.str("finished"));
                    core.toaster(cmdFinal + " " + core.str("finished"));
                } else {
                    statusText.setText(core.str("failed"));
                }
            });
        });
    }

    private void runAllCommands() {
        ArrayList<String> cmds = core.getListString("saved_commands");
        if (cmds.isEmpty()) return;
        statusText.setText(core.str("running_all"));
        TaskRunner.execute(() -> {
            int success = 0;
            for (String cmd : cmds) {
                try {
                    if (CustomCommand.execute(cmd, core)) {
                        success++;
                    }
                } catch (Exception ignored) {}
            }
            int finalSuccess = success;
            getActivity().runOnUiThread(() -> statusText.setText(core.str("finished") + " " + finalSuccess + "/" + cmds.size()));
        });
    }

    private void clearCommands() {
        core.putListString("saved_commands", new ArrayList<>());
        loadCommands();
        core.toaster(core.str("cleared"));
    }
}

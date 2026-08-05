package com.opx.yourdemon.adapter_suggester;


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

public class AdapterSuggesterFragment extends Fragment {

    public Core core;
    public TextView statusText, suggestionText;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.adapter_suggester, container, false);
        core = new Core(getContext());

        statusText = view.findViewById(R.id.status_text);
        suggestionText = view.findViewById(R.id.suggestion_text);

        analyzeAdapter();

        view.findViewById(R.id.refresh_adapter).setOnClickListener(v -> analyzeAdapter());

        return view;
    }

    private void analyzeAdapter() {
        statusText.setText(core.str("suggesting_adapter"));
        TaskRunner.execute(() -> {
            StringBuilder result = new StringBuilder();
            try {
                result.append(core.str("detected_chipsets")).append(":\n\n");
                ArrayList<String> lsusb = CommandOutput.execute("lsusb", core);
                if (lsusb != null) {
                    for (String line : lsusb) {
                        result.append(line).append("\n");
                        if (line.contains("0bda:") || line.contains("Realtek")) {
                            result.append("  -> ").append(core.str("supported_wifi_adapter")).append("\n");
                        }
                    }
                }

                result.append("\n").append(core.str("recommended_drivers")).append(":\n");
                result.append("  - 8812au (RTL8812AU)\n");
                result.append("  - 88XXau (RTL88XXAU)\n");
                result.append("  - mt7610u (MT7610U)\n");
                result.append("  - ar9271 (AR9271)\n\n");

                result.append(core.str("wifi_adapter_tip")).append(":\n");
                result.append("- ").append(core.str("tip_1")).append("\n");
                result.append("- ").append(core.str("tip_2")).append("\n");
                result.append("- ").append(core.str("tip_3")).append("\n");

            } catch (Exception e) {
                result.append(core.str("error")).append(": ").append(e.getMessage());
            }
            String finalResult = result.toString();
            getActivity().runOnUiThread(() -> {
                statusText.setText(core.str("finished"));
                suggestionText.setText(finalResult);
            });
        });
    }
}

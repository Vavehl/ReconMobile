package com.example.reconmobile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class FragmentSettings extends Fragment {
    private View view;

    private TextView tvTiltValue;

    public FragmentSettings() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_settings,container,false);
        SeekBar_TiltSensitivity();
        return view;
    }

    private void SeekBar_TiltSensitivity() {
        //Settings fragment
        SeekBar sb_TiltSensitivity = view.findViewById(R.id.seekbarTiltSensitivity);
        sb_TiltSensitivity.setMax(10);

        tvTiltValue = view.findViewById(R.id.txtTiltValue);
        tvTiltValue.setText(String.valueOf(sb_TiltSensitivity.getProgress()));
        sb_TiltSensitivity.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    int intTiltValue;
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        intTiltValue = progress;
                        tvTiltValue.setText(String.valueOf(intTiltValue));
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        tvTiltValue.setText(String.valueOf(intTiltValue));
                    }
                }
        );

    }

}

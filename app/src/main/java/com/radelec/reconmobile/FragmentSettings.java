package com.radelec.reconmobile;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class FragmentSettings extends Fragment {
    private View view;

    private DatabaseOperations db_settings;
    private Cursor cursorSettingsDefaults = null;
    private TextView tvTiltValue;

    public FragmentSettings() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_settings,container,false);

        db_settings = DatabaseOperations.getInstance(getContext());

        //Standard Settings should be initialized to the proper spinners
        final Spinner spnAutoClear = view.findViewById(R.id.spinner_autoclear);
        final Spinner spnUnitSystem = view.findViewById(R.id.spinner_units);
        final Spinner spnSignatureOptions = view.findViewById(R.id.spinner_signatureoptions);

        //Recon Display Options
        //Should only be displayed if Recon is connected, right?
        Spinner spnDisplay_Radon = view.findViewById(R.id.spinner_display_radon);
        Spinner spnDisplay_Temperature = view.findViewById(R.id.spinner_display_temperature);
        Spinner spnDisplay_Pressure = view.findViewById(R.id.spinner_display_pressure);

        //Pull the Settings Defaults in the database with a Cursor class...
        cursorSettingsDefaults = db_settings.getSettingsData();
        cursorSettingsDefaults.moveToFirst(); //Critical to moveToFirst() here, or else we're sitting at an invalid index.

        //SeekBar has its own method
        SeekBar sb_TiltSensitivity = view.findViewById(R.id.seekbarTiltSensitivity);
        sb_TiltSensitivity.setProgress(Integer.parseInt(cursorSettingsDefaults.getString(4)));
        SeekBar_TiltSensitivity();

        //Pull Auto Clear Sessions value from database, and assign it to spnAutoClear.
        spnAutoClear.setSelection(findSpinnerIndex(spnAutoClear,cursorSettingsDefaults.getString(1)));
        spnAutoClear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                db_settings.updateData("SETTINGS","AUTO_CLEAR_SESSIONS",spnAutoClear.getSelectedItem().toString(),"SettingsID");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //Pull Unit System value from database, and assign it to spnUnitSystem.
        spnUnitSystem.setSelection(findSpinnerIndex(spnUnitSystem,cursorSettingsDefaults.getString(2)));
        spnUnitSystem.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                db_settings.updateData("SETTINGS","UNIT_SYSTEM",spnUnitSystem.getSelectedItem().toString(),"SettingsID");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }

        });

        //Pull Signature Options value from database, and assign it to spnSignatureOptions.
        spnSignatureOptions.setSelection(findSpinnerIndex(spnSignatureOptions,cursorSettingsDefaults.getString(3)));
        spnSignatureOptions.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                db_settings.updateData("SETTINGS","SIGNATURE_OPTIONS",spnSignatureOptions.getSelectedItem().toString(),"SettingsID");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

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
                        db_settings.updateData("SETTINGS","TILT_SENSITIVITY",String.valueOf(intTiltValue),"SettingsID");
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

    private int findSpinnerIndex(Spinner spinner, String value) {
        for (int i = 0; i< spinner.getCount();i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(value)) {
                return i;
            }
        }
        return 0;
    }

    public void onDestroy() {
        Logging.main("FragmentSettings","onDestroy() called!");
        //Properly closing cursor and database should ensure that the database doesn't create a memory leak...
        if(cursorSettingsDefaults != null) {
            cursorSettingsDefaults.close();
            db_settings.close();
        }
        super.onDestroy();
    }

}

package com.example.reconmobile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class FragmentReport extends Fragment {
    View view;
    public FragmentReport() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_report,container,false);

        //Get the widgets referenced in fragment_report.xml -- we'll need these to pull default values.
        final EditText etLocation;
        final EditText etCustomer;
        final EditText etTestSite;
        etLocation = (EditText) view.findViewById(R.id.reports_location);
        etCustomer = (EditText) view.findViewById(R.id.reports_customer);
        etTestSite = (EditText) view.findViewById(R.id.reports_testsite);

        return view;
    }
}

package com.example.reconmobile;

import android.database.Cursor;
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
    DatabaseOperations db;

    public FragmentReport() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_report,container,false);
        db = new DatabaseOperations(getContext());

        //Get the widgets referenced in fragment_report.xml -- we'll need these to pull default values.
        final EditText etLocation;
        final EditText etCustomer;
        final EditText etTestSite;
        final EditText etReportText;

        etLocation = (EditText) view.findViewById(R.id.reports_location);
        etCustomer = (EditText) view.findViewById(R.id.reports_customer);
        etTestSite = (EditText) view.findViewById(R.id.reports_testsite);
        etReportText = (EditText) view.findViewById(R.id.reports_text);

        Cursor cursorReportText;
        //cursorReportText = doDatabaseOperations.getData("REPORT_DEFAULTS","REPORT_TEXT");

        //etReportText.setText(cursorReportText.getString(12));

        etReportText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //db.insertData("REPORT_DEFAULTS","REPORT_TEXT",etReportText.getText().toString());
            }
        });
        return view;
    }
}

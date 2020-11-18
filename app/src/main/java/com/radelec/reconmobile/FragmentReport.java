package com.radelec.reconmobile;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

import static com.radelec.reconmobile.Globals.*;

public class FragmentReport extends Fragment {
    private DatabaseOperations db_report;

    public FragmentReport() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_report, container, false);
        db_report = new DatabaseOperations(getContext());

        //Get the widgets referenced in fragment_report.xml -- we'll need these to pull default values.
        final TextInputEditText etLocation;
        final TextInputEditText etCustomer;
        final TextInputEditText etTestSite;
        final TextInputEditText etDeployedBy;
        final TextInputEditText etRetrievedBy;
        final TextInputEditText etAnalyzedBy;
        final TextInputEditText etProtocol;
        final TextInputEditText etTampering;
        final TextInputEditText etWeather;
        final TextInputEditText etMitigation;
        final TextInputEditText etComment;
        final TextInputEditText etReportText;
        final TextInputEditText etCustomerEmail;
        final TextInputLayout layoutCustomerEmail;

        etLocation = view.findViewById(R.id.reports_location);
        etCustomer = view.findViewById(R.id.reports_customer);
        etTestSite = view.findViewById(R.id.reports_testsite);
        etDeployedBy = view.findViewById(R.id.reports_deployed);
        etRetrievedBy = view.findViewById(R.id.reports_retrieved);
        etAnalyzedBy = view.findViewById(R.id.reports_analyzed);
        etProtocol = view.findViewById(R.id.reports_protocol);
        etTampering = view.findViewById(R.id.reports_tampering);
        etWeather = view.findViewById(R.id.reports_weather);
        etMitigation = view.findViewById(R.id.reports_mitigation);
        etComment = view.findViewById(R.id.reports_comment);
        etReportText = view.findViewById(R.id.reports_text);
        etCustomerEmail = view.findViewById(R.id.customer_email);
        layoutCustomerEmail = view.findViewById(R.id.layout_customer_email);

        //Pull the Report Defaults in the database with a Cursor class...
        Cursor cursorReportDefaults;
        cursorReportDefaults = db_report.getReportDefaultData();
        cursorReportDefaults.moveToFirst(); //Critical to moveToFirst() here, or else we're sitting at an invalid index.

        if (connected == Globals.ReconConnected.Loaded) {
            etLocation.setText(loadedLocationDeployed);
            etCustomer.setText(loadedCustomerInfo);
            etTestSite.setText(loadedTestSiteInfo);
            etDeployedBy.setText(loadedDeployedBy);
            etRetrievedBy.setText(loadedRetrievedBy);
            etAnalyzedBy.setText(loadedAnalyzedBy);
            etProtocol.setText(loadedReportProtocol);
            etTampering.setText(loadedReportTampering);
            etWeather.setText(loadedReportWeather);
            etMitigation.setText(loadedReportMitigation);
            etComment.setText(loadedReportComment);
            etCustomerEmail.setText(loadedEmail);
            layoutCustomerEmail.setVisibility(View.VISIBLE); //Show Customer Email when Reports tab is displayed / file is loaded!
        } else {
            //Set the Report Defaults to the TextInputEditText boxes
            //No need to set defaults for Customer or Test Site, as these will change every time...
            etLocation.setText(cursorReportDefaults.getString(1));
            etCustomer.setText(cursorReportDefaults.getString(2));
            etTestSite.setText(cursorReportDefaults.getString(3));
            etDeployedBy.setText(cursorReportDefaults.getString(4));
            etRetrievedBy.setText(cursorReportDefaults.getString(5));
            etAnalyzedBy.setText(cursorReportDefaults.getString(6));
            etProtocol.setText(cursorReportDefaults.getString(7));
            etTampering.setText(cursorReportDefaults.getString(8));
            etWeather.setText(cursorReportDefaults.getString(9));
            etMitigation.setText(cursorReportDefaults.getString(10));
            etComment.setText(cursorReportDefaults.getString(11));
            layoutCustomerEmail.setVisibility(View.GONE); //Hide Customer Email when Defaults tab is displayed!
        }

        //Report Text default will always be the same, at least for now...
        etReportText.setText(cursorReportDefaults.getString(12));

        //Instrument Location Listener
        etLocation.setOnClickListener(v -> {
            //Only overwrite defaults if a file is *NOT* currently loaded!
            if (connected != ReconConnected.Loaded) db_report.updateData("REPORT_DEFAULTS","INSTRUMENT_LOCATION",etLocation.getText().toString(),"DefaultID");
        });
        etLocation.setOnFocusChangeListener((v, hasFocus) -> {
            if ((!hasFocus) && (connected != ReconConnected.Loaded)) {
                db_report.updateData("REPORT_DEFAULTS","INSTRUMENT_LOCATION",etLocation.getText().toString(),"DefaultID");
            } else if(connected == ReconConnected.Loaded) {
                loadedLocationDeployed = (etLocation != null) ? Objects.requireNonNull(etLocation.getText()).toString() : "";
            }
        });

        //Customer Listener
        etCustomer.setOnClickListener(v -> {
            //Only overwrite defaults if a file is *NOT* currently loaded!
            if (connected != ReconConnected.Loaded) db_report.updateData("REPORT_DEFAULTS","CUSTOMER_INFORMATION",etCustomer.getText().toString(),"DefaultID");
        });
        etCustomer.setOnFocusChangeListener((v, hasFocus) -> {
            if ((!hasFocus) && (connected != ReconConnected.Loaded)) {
                db_report.updateData("REPORT_DEFAULTS","CUSTOMER_INFORMATION",etCustomer.getText().toString(),"DefaultID");
            } else if(connected == ReconConnected.Loaded) {
                loadedCustomerInfo = (etCustomer != null) ? Objects.requireNonNull(etCustomer.getText()).toString() : "";
            }
        });

        //Test Site Listener
        etTestSite.setOnClickListener(v -> {
            //Only overwrite defaults if a file is *NOT* currently loaded!
            if (connected != ReconConnected.Loaded) db_report.updateData("REPORT_DEFAULTS","TEST_SITE_INFORMATION",etTestSite.getText().toString(),"DefaultID");
        });
        etTestSite.setOnFocusChangeListener((v, hasFocus) -> {
            if ((!hasFocus) && (connected != ReconConnected.Loaded)) {
                db_report.updateData("REPORT_DEFAULTS","TEST_SITE_INFORMATION",etTestSite.getText().toString(),"DefaultID");
            } else if(connected == ReconConnected.Loaded) {
                loadedTestSiteInfo = (etTestSite != null) ? Objects.requireNonNull(etTestSite.getText()).toString() : "";
            }
        });

        //Email Listener
        etCustomerEmail.setOnClickListener(v -> {
        });
        etCustomerEmail.setOnFocusChangeListener((v, hasFocus) -> loadedEmail = Objects.requireNonNull(etCustomerEmail.getText()).toString());

        //Deployed By Listener
        etDeployedBy.setOnClickListener(v -> {
            //Only overwrite defaults if a file is *NOT* currently loaded!
            if (connected != ReconConnected.Loaded) db_report.updateData("REPORT_DEFAULTS","DEPLOYED_BY",etDeployedBy.getText().toString(),"DefaultID");
        });
        etDeployedBy.setOnFocusChangeListener((v, hasFocus) -> {
            if ((!hasFocus) && (connected != ReconConnected.Loaded)) {
                db_report.updateData("REPORT_DEFAULTS","DEPLOYED_BY",etDeployedBy.getText().toString(),"DefaultID");
            } else if(connected == ReconConnected.Loaded) {
                loadedDeployedBy = (etDeployedBy != null) ? Objects.requireNonNull(etDeployedBy.getText()).toString() : "";
            }
        });

        //Retrieved By Listener
        etRetrievedBy.setOnClickListener(v -> {
            //Only overwrite defaults if a file is *NOT* currently loaded!
            if (connected != ReconConnected.Loaded) db_report.updateData("REPORT_DEFAULTS","RETRIEVED_BY",etRetrievedBy.getText().toString(),"DefaultID");
        });
        etRetrievedBy.setOnFocusChangeListener((v, hasFocus) -> {
            if ((!hasFocus) && (connected != ReconConnected.Loaded)) {
                db_report.updateData("REPORT_DEFAULTS","RETRIEVED_BY",etRetrievedBy.getText().toString(),"DefaultID");
            } else if(connected == ReconConnected.Loaded) {
                loadedRetrievedBy = (etRetrievedBy != null) ? Objects.requireNonNull(etRetrievedBy.getText()).toString() : "";
            }
        });

        //Analyzed By Listener
        etAnalyzedBy.setOnClickListener(v -> {
            //Only overwrite defaults if a file is *NOT* currently loaded!
            if (connected != ReconConnected.Loaded) db_report.updateData("REPORT_DEFAULTS","ANALYZED_BY",etAnalyzedBy.getText().toString(),"DefaultID");
        });
        etAnalyzedBy.setOnFocusChangeListener((v, hasFocus) -> {
            if ((!hasFocus) && (connected != ReconConnected.Loaded)) {
                db_report.updateData("REPORT_DEFAULTS","ANALYZED_BY",etAnalyzedBy.getText().toString(),"DefaultID");
            } else if(connected == ReconConnected.Loaded) {
                loadedAnalyzedBy = (etAnalyzedBy != null) ? Objects.requireNonNull(etAnalyzedBy.getText()).toString() : "";
            }
        });

        //Protocol Listener
        etProtocol.setOnClickListener(v -> {
            //Only overwrite defaults if a file is *NOT* currently loaded!
            if (connected != ReconConnected.Loaded) db_report.updateData("REPORT_DEFAULTS","PROTOCOL",etProtocol.getText().toString(),"DefaultID");
        });
        etProtocol.setOnFocusChangeListener((v, hasFocus) -> {
            if ((!hasFocus) && (connected != ReconConnected.Loaded)) {
                db_report.updateData("REPORT_DEFAULTS","PROTOCOL",etProtocol.getText().toString(),"DefaultID");
            } else if(connected == ReconConnected.Loaded) {
                loadedReportProtocol = (etProtocol != null) ? Objects.requireNonNull(etProtocol.getText()).toString() : "";
            }
        });

        //Tampering Listener
        etTampering.setOnClickListener(v -> {
            //Only overwrite defaults if a file is *NOT* currently loaded!
            if (connected != ReconConnected.Loaded) db_report.updateData("REPORT_DEFAULTS","TAMPERING",etTampering.getText().toString(),"DefaultID");
        });
        etTampering.setOnFocusChangeListener((v, hasFocus) -> {
            if ((!hasFocus) && (connected != ReconConnected.Loaded)) {
                db_report.updateData("REPORT_DEFAULTS","TAMPERING",etTampering.getText().toString(),"DefaultID");
            } else if(connected == ReconConnected.Loaded) {
                loadedReportTampering = (etTampering != null) ? Objects.requireNonNull(etTampering.getText()).toString() : "";
            }
        });

        //Weather Listener
        etWeather.setOnClickListener(v -> {
            //Only overwrite defaults if a file is *NOT* currently loaded!
            if (connected != ReconConnected.Loaded) db_report.updateData("REPORT_DEFAULTS","WEATHER",etWeather.getText().toString(),"DefaultID");
        });
        etWeather.setOnFocusChangeListener((v, hasFocus) -> {
            if ((!hasFocus) && (connected != ReconConnected.Loaded)) {
                db_report.updateData("REPORT_DEFAULTS","WEATHER",etWeather.getText().toString(),"DefaultID");
            } else if(connected == ReconConnected.Loaded) {
                loadedReportWeather = (etWeather != null) ? Objects.requireNonNull(etWeather.getText()).toString() : "";
            }
        });

        //Mitigation Listener
        etMitigation.setOnClickListener(v -> {
            //Only overwrite defaults if a file is *NOT* currently loaded!
            if (connected != ReconConnected.Loaded) db_report.updateData("REPORT_DEFAULTS","MITIGATION",etMitigation.getText().toString(),"DefaultID");
        });
        etMitigation.setOnFocusChangeListener((v, hasFocus) -> {
            if ((!hasFocus) && (connected != ReconConnected.Loaded)) {
                db_report.updateData("REPORT_DEFAULTS","MITIGATION",etMitigation.getText().toString(),"DefaultID");
            } else if(connected == ReconConnected.Loaded) {
                loadedReportMitigation = (etMitigation != null) ? Objects.requireNonNull(etMitigation.getText()).toString() : "";
            }
        });

        //Comment Listener
        etComment.setOnClickListener(v -> {
            //Only overwrite defaults if a file is *NOT* currently loaded!
            if (connected != ReconConnected.Loaded) db_report.updateData("REPORT_DEFAULTS","COMMENT",etComment.getText().toString(),"DefaultID");
        });
        etComment.setOnFocusChangeListener((v, hasFocus) -> {
            if ((!hasFocus) && (connected != ReconConnected.Loaded)) {
                db_report.updateData("REPORT_DEFAULTS","COMMENT",etComment.getText().toString(),"DefaultID");
            } else if(connected == ReconConnected.Loaded) {
                loadedReportComment = (etComment != null) ? Objects.requireNonNull(etComment.getText()).toString() : "";
            }
        });

        //Report Text Listener
        etReportText.setOnClickListener(v -> {
            //Only overwrite defaults if a file is *NOT* currently loaded!
            if (connected != ReconConnected.Loaded) db_report.updateData("REPORT_DEFAULTS","REPORT_TEXT",etReportText.getText().toString(),"DefaultID");
        });
        etReportText.setOnFocusChangeListener((v, hasFocus) -> {
            //Report Text isn't (currently) stored in the data file -- should we begin saving it to the file?
            if ((!hasFocus) && (connected != ReconConnected.Loaded)) {
                db_report.updateData("REPORT_DEFAULTS","REPORT_TEXT",etReportText.getText().toString(),"DefaultID");
            }
        });

        return view;
    }
}

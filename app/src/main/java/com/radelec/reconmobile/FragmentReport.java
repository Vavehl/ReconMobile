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

        //Pull the Report Defaults in the database with a Cursor class...
        Cursor cursorReportDefaults;
        cursorReportDefaults = db_report.getReportDefaultData();
        cursorReportDefaults.moveToFirst(); //Critical to moveToFirst() here, or else we're sitting at an invalid index.

        if (connected == Globals.ReconConnected.Loaded) {
            etLocation.setText((String) loadedLocationDeployed);
            etCustomer.setText((String) loadedCustomerInfo);
            etTestSite.setText((String) loadedTestSiteInfo);
            etDeployedBy.setText((String) loadedDeployedBy);
            etRetrievedBy.setText((String) loadedRetrievedBy);
            etAnalyzedBy.setText((String) loadedAnalyzedBy);
            etProtocol.setText((String) loadedReportProtocol);
            etTampering.setText((String) loadedReportTampering);
            etWeather.setText((String) loadedReportWeather);
            etMitigation.setText((String) loadedReportMitigation);
            etComment.setText((String) loadedReportComment);
        } else {
            //Set the Report Defaults to the TextInputEditText boxes
            //No need to set defaults for Customer or Test Site, as these will change every time...
            etLocation.setText((String) cursorReportDefaults.getString(1));
            etCustomer.setText((String) cursorReportDefaults.getString(2));
            etTestSite.setText((String) cursorReportDefaults.getString(3));
            etDeployedBy.setText((String) cursorReportDefaults.getString(4));
            etRetrievedBy.setText((String) cursorReportDefaults.getString(5));
            etAnalyzedBy.setText((String) cursorReportDefaults.getString(6));
            etProtocol.setText((String) cursorReportDefaults.getString(7));
            etTampering.setText((String) cursorReportDefaults.getString(8));
            etWeather.setText((String) cursorReportDefaults.getString(9));
            etMitigation.setText((String) cursorReportDefaults.getString(10));
            etComment.setText((String) cursorReportDefaults.getString(11));
        }

        //Report Text default will always be the same, at least for now...
        etReportText.setText((String) cursorReportDefaults.getString(12));

        //Instrument Location Listener
        etLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db_report.updateData("REPORT_DEFAULTS","INSTRUMENT_LOCATION",etLocation.getText().toString(),"DefaultID");
            }
        });
        etLocation.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    db_report.updateData("REPORT_DEFAULTS","INSTRUMENT_LOCATION",etLocation.getText().toString(),"DefaultID");
                }
            }
        });

        //Customer Listener
        etCustomer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db_report.updateData("REPORT_DEFAULTS","CUSTOMER_INFORMATION",etCustomer.getText().toString(),"DefaultID");
            }
        });
        etCustomer.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    db_report.updateData("REPORT_DEFAULTS","CUSTOMER_INFORMATION",etCustomer.getText().toString(),"DefaultID");
                }
            }
        });

        //Test Site Listener
        etTestSite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db_report.updateData("REPORT_DEFAULTS","TEST_SITE_INFORMATION",etTestSite.getText().toString(),"DefaultID");
            }
        });
        etTestSite.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    db_report.updateData("REPORT_DEFAULTS","TEST_SITE_INFORMATION",etTestSite.getText().toString(),"DefaultID");
                }
            }
        });

        //Deployed By Listener
        etDeployedBy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db_report.updateData("REPORT_DEFAULTS","DEPLOYED_BY",etDeployedBy.getText().toString(),"DefaultID");
            }
        });
        etDeployedBy.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    db_report.updateData("REPORT_DEFAULTS","DEPLOYED_BY",etDeployedBy.getText().toString(),"DefaultID");
                }
            }
        });

        //Retrieved By Listener
        etRetrievedBy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db_report.updateData("REPORT_DEFAULTS","RETRIEVED_BY",etRetrievedBy.getText().toString(),"DefaultID");
            }
        });
        etRetrievedBy.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    db_report.updateData("REPORT_DEFAULTS","RETRIEVED_BY",etRetrievedBy.getText().toString(),"DefaultID");
                }
            }
        });

        //Analyzed By Listener
        etAnalyzedBy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db_report.updateData("REPORT_DEFAULTS","ANALYZED_BY",etAnalyzedBy.getText().toString(),"DefaultID");
            }
        });
        etAnalyzedBy.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    db_report.updateData("REPORT_DEFAULTS","ANALYZED_BY",etAnalyzedBy.getText().toString(),"DefaultID");
                }
            }
        });

        //Protocol Listener
        etProtocol.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db_report.updateData("REPORT_DEFAULTS","PROTOCOL",etProtocol.getText().toString(),"DefaultID");
            }
        });
        etProtocol.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    db_report.updateData("REPORT_DEFAULTS","PROTOCOL",etProtocol.getText().toString(),"DefaultID");
                }
            }
        });

        //Tampering Listener
        etTampering.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db_report.updateData("REPORT_DEFAULTS","TAMPERING",etTampering.getText().toString(),"DefaultID");
            }
        });
        etTampering.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    db_report.updateData("REPORT_DEFAULTS","TAMPERING",etTampering.getText().toString(),"DefaultID");
                }
            }
        });

        //Weather Listener
        etWeather.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db_report.updateData("REPORT_DEFAULTS","WEATHER",etWeather.getText().toString(),"DefaultID");
            }
        });
        etWeather.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    db_report.updateData("REPORT_DEFAULTS","WEATHER",etWeather.getText().toString(),"DefaultID");
                }
            }
        });

        //Mitigation Listener
        etMitigation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db_report.updateData("REPORT_DEFAULTS","MITIGATION",etMitigation.getText().toString(),"DefaultID");
            }
        });
        etMitigation.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    db_report.updateData("REPORT_DEFAULTS","MITIGATION",etMitigation.getText().toString(),"DefaultID");
                }
            }
        });

        //Comment Listener
        etComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db_report.updateData("REPORT_DEFAULTS","COMMENT",etComment.getText().toString(),"DefaultID");
            }
        });
        etComment.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    db_report.updateData("REPORT_DEFAULTS","COMMENT",etComment.getText().toString(),"DefaultID");
                }
            }
        });

        //Report Text Listener
        etReportText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db_report.updateData("REPORT_DEFAULTS","REPORT_TEXT",etReportText.getText().toString(),"DefaultID");
            }
        });
        etReportText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    db_report.updateData("REPORT_DEFAULTS","REPORT_TEXT",etReportText.getText().toString(),"DefaultID");
                }
            }
        });

        return view;
    }
}

package com.example.reconmobile;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;

public class FragmentCompany extends Fragment {

    private DatabaseOperations db_company;

    public FragmentCompany() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_company, container, false);

        db_company = new DatabaseOperations(getContext());

        //Get the widgets referenced in fragment_company.xml -- we'll need these to pull default values.
        final TextInputEditText etCompanyName;
        final TextInputEditText etCompanyDetails;

        etCompanyName = view.findViewById(R.id.company_name);
        etCompanyDetails = view.findViewById(R.id.company_details);

        //Pull the Company Defaults in the database with a Cursor class...
        Cursor cursorCompanyDefaults;
        cursorCompanyDefaults = db_company.getCompanyData();
        cursorCompanyDefaults.moveToFirst(); //Critical to moveToFirst() here, or else we're sitting at an invalid index.

        //Set the Company Defaults to the TextInputEditText boxes
        //How should we best handle the company logo and signature?
        etCompanyName.setText((String)cursorCompanyDefaults.getString(1));
        etCompanyDetails.setText((String)cursorCompanyDefaults.getString(2));

        //Company Name Listener
        etCompanyName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db_company.updateData("COMPANY","COMPANY_NAME",etCompanyName.getText().toString(),"CompanyID");
            }
        });
        etCompanyName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    db_company.updateData("COMPANY","COMPANY_NAME",etCompanyName.getText().toString(),"CompanyID");
                }
            }
        });

        //Company Details Listener
        etCompanyDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db_company.updateData("COMPANY","COMPANY_DETAILS",etCompanyDetails.getText().toString(),"CompanyID");
            }
        });
        etCompanyDetails.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    db_company.updateData("COMPANY","COMPANY_DETAILS",etCompanyDetails.getText().toString(),"CompanyID");
                }
            }
        });

        return view;
    }
}

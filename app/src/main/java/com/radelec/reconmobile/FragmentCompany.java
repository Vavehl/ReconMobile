package com.radelec.reconmobile;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class FragmentCompany extends Fragment {

    private Cursor cursorCompanyDefaults = null;
    private DatabaseOperations db_company;
    public final int SELECT_COMPANY_LOGO = 0;
    public final int SELECT_ANALYST_SIGNATURE = 1;
    private ImageView imgCompanyLogo;
    private ImageView imgAnalystSignature;

    public FragmentCompany() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_company, container, false);

        db_company = new DatabaseOperations(getContext());

        //Create the buttons
        Button btnClearLogo;
        Button btnClearSignature;

        //Get the widgets referenced in fragment_company.xml -- we'll need these to pull default values.
        final TextInputEditText etCompanyName;
        final TextInputEditText etCompanyDetails;
        final TextInputEditText etCompanyEmail;

        //Find TextInputEditText IDs
        etCompanyName = view.findViewById(R.id.company_name);
        etCompanyDetails = view.findViewById(R.id.company_details);
        etCompanyEmail = view.findViewById(R.id.company_email);

        //Find ImageView IDs
        imgCompanyLogo = view.findViewById(R.id.CompanyLogo);
        imgAnalystSignature = view.findViewById(R.id.analystSignature);

        //Find Button IDs
        btnClearLogo = view.findViewById(R.id.btn_clear_logo);
        btnClearSignature = view.findViewById(R.id.btn_clear_signature);

        //Load images, if they already exist.
        try {

            String strAppPath = getContext().getApplicationInfo().dataDir;
            File pathCompanyLogo = new File(strAppPath + File.separator + "app_images" + File.separator + "company_logo.png");
            File pathAnalystSignature = new File(strAppPath + File.separator + "app_images" + File.separator + "signature.png");

            if(pathCompanyLogo.exists()) {
                Log.i("FragmentCompany", "Found company logo image in app image directory!");
                Bitmap bmpCompanyLogo = BitmapFactory.decodeFile(pathCompanyLogo.toString());
                imgCompanyLogo.setImageBitmap(bmpCompanyLogo);
            } else {
                Log.i("FragmentCompany", "No company logo (company_logo.png) found in app image directory.");
            }

            if(pathAnalystSignature.exists()) {
                Log.i("FragmentCompany", "Found signature image in app image directory!");
                Bitmap bmpAnalystSignature = BitmapFactory.decodeFile(pathAnalystSignature.toString());
                imgAnalystSignature.setImageBitmap(bmpAnalystSignature);
            } else {
                Log.i("FragmentCompany", "No digital signature (signature.png) found in app image directory.");
            }

        } catch (NullPointerException ex) {
            Log.d("FragmentCompany", "Failed to find application directory in order to check if company logo and/or signature exists!");
        }

        //Pull the Company Defaults in the database with a Cursor class...
        try {
            cursorCompanyDefaults = db_company.getCompanyData();
            cursorCompanyDefaults.moveToFirst(); //Critical to moveToFirst() here, or else we're sitting at an invalid index.

            //Set the Company Defaults to the TextInputEditText boxes
            //How should we best handle the company logo and signature?
            if(db_company.ReconTableExists("COMPANY",true)) {
                Log.d("FragmentCompany","Column Names in table COMPANY = " + Arrays.toString(cursorCompanyDefaults.getColumnNames()));
                if(cursorCompanyDefaults.getCount()==0) {
                    Log.d("FragmentCompany","No data found in table COMPANY!");
                    db_company.resetCompanyData();
                }
                if(cursorCompanyDefaults.getColumnIndex("COMPANY_NAME")==1) {
                    if(!cursorCompanyDefaults.isNull(1)) {
                        etCompanyName.setText(cursorCompanyDefaults.getString(1));
                    } else {
                        Log.d("FragmentCompany","WARNING! Column COMPANY_NAME is null!");
                    }
                } else {
                    Log.d("FragmentCompany","WARNING! Column COMPANY_NAME not found!");
                }
                if(cursorCompanyDefaults.getColumnIndex("COMPANY_DETAILS")==2) {
                    if(!cursorCompanyDefaults.isNull(2)) {
                        etCompanyDetails.setText(cursorCompanyDefaults.getString(2));
                    } else {
                        Log.d("FragmentCompany","WARNING! Column COMPANY_DETAILS is null!");
                    }
                } else {
                    Log.d("FragmentCompany","WARNING! Column COMPANY_DETAILS not found!");
                }
                if(cursorCompanyDefaults.getColumnIndex("COMPANY_EMAIL")==3) {
                    if(!cursorCompanyDefaults.isNull(3)) {
                        etCompanyEmail.setText(cursorCompanyDefaults.getString(3));
                    } else {
                        Log.d("FragmentCompany","WARNING! Column COMPANY_EMAIL is null!");
                    }
                } else {
                    Log.d("FragmentCompany","WARNING! Column COMPANY_EMAIL not found!");
                }
            } else {
                Log.d("FragmentCompany","WARNING! Table COMPANY not found!");
            }
        } catch (Exception ex) {

        }

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

        //Company Email Listener
        etCompanyEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db_company.updateData("COMPANY","COMPANY_EMAIL",etCompanyEmail.getText().toString(),"CompanyID");
            }
        });
        etCompanyEmail.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    db_company.updateData("COMPANY","COMPANY_EMAIL",etCompanyEmail.getText().toString(),"CompanyID");
                }
            }
        });

        //Company Logo Listener
        imgCompanyLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), SELECT_COMPANY_LOGO);

            }
        });

        //Analyst Signature Listener
        imgAnalystSignature.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), SELECT_ANALYST_SIGNATURE);

            }
        });

        //Button (Clear Logo) Listener
        btnClearLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String strAppPath = getContext().getApplicationInfo().dataDir;
                File pathCompanyLogo = new File(strAppPath + File.separator + "app_images" + File.separator + "company_logo.png");
                if(pathCompanyLogo.exists()) {
                    Log.i("FragmentCompany", "Clear Logo button pressed ... deleting company logo!");
                    imgCompanyLogo.setImageDrawable(null);
                    pathCompanyLogo.delete();
                    Toast.makeText(getContext(),"Clearing Logo...",Toast.LENGTH_SHORT).show();
                } else {
                    Log.i("FragmentCompany", "Clear Logo button pressed, but no company logo found...");
                }
            }
        });

        //Button (Clear Signature) Listener
        btnClearSignature.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String strAppPath = getContext().getApplicationInfo().dataDir;
                File pathAnalystSignature = new File(strAppPath + File.separator + "app_images" + File.separator + "signature.png");
                if(pathAnalystSignature.exists()) {
                    Log.i("FragmentCompany", "Clear Signature button pressed ... deleting signature!");
                    imgAnalystSignature.setImageDrawable(null);
                    pathAnalystSignature.delete();
                    Toast.makeText(getContext(),"Clearing Signature...",Toast.LENGTH_SHORT).show();
                } else {
                    Log.i("FragmentCompany", "Clear Signature button pressed, but no signature found...");
                }
            }
        });

        return view;
    }

    public void onDestroy() {
        Log.d("FragmentCompany","onDestroy() called!");
        if(cursorCompanyDefaults != null) {
            cursorCompanyDefaults.close();
            db_company.close(); //This is needed to prevent a memory leak, I think?
        }
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==SELECT_COMPANY_LOGO && resultCode == Activity.RESULT_OK) {
            Uri selectedImage = data.getData();
            Bitmap bmpCompanyLogo;
            try {
                bmpCompanyLogo = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), selectedImage);
                saveBitmapToInternalStorage(bmpCompanyLogo,"company_logo.png");
                imgCompanyLogo.setImageBitmap(bmpCompanyLogo);
            } catch (FileNotFoundException ex) {
                Log.d("FragmentCompany","Failure to find/load company logo image!");
                ex.printStackTrace();
            } catch (IOException ex) {
                Log.d("FragmentCompany", "Generalized I/O error when loading company logo image.");
                ex.printStackTrace();
            } catch (NullPointerException ex) {
                Log.d("FragmentCompany", "Null Pointer Exception when loading company logo image.");
                ex.printStackTrace();
            }
        }

        if(requestCode==SELECT_ANALYST_SIGNATURE && resultCode == Activity.RESULT_OK) {
            Uri selectedImage = data.getData();
            Bitmap bmpAnalystSignature;
            try {
                bmpAnalystSignature = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), selectedImage);
                saveBitmapToInternalStorage(bmpAnalystSignature,"signature.png");
                imgAnalystSignature.setImageBitmap(bmpAnalystSignature);
            } catch (FileNotFoundException ex) {
                Log.d("FragmentCompany","Failure to find/load analyst signature!");
                ex.printStackTrace();
            } catch (IOException ex) {
                Log.d("FragmentCompany", "Generalized I/O error when loading analyst signature.");
                ex.printStackTrace();
            } catch (NullPointerException ex) {
                Log.d("FragmentCompany", "Null Pointer Exception when loading analyst signature.");
                ex.printStackTrace();
            }
        }
    }

    private void saveBitmapToInternalStorage(Bitmap bmpImage, String strFileName) {
        ContextWrapper cw = new ContextWrapper(getContext());

        File directory = cw.getDir("images", Context.MODE_PRIVATE);

        File pathToImage = new File(directory, strFileName);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(pathToImage);
            bmpImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception ex) {
            Log.d("FragmentCompany", "General Exception when saving bitmap to local storage.");
            ex.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException ex) {
                Log.d("FragmentCompany", "I/O Exception when saving bitmap to local storage.");
                ex.printStackTrace();
            }
        }
    }

}

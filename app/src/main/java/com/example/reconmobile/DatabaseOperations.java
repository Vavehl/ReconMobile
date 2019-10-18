package com.example.reconmobile;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseOperations extends SQLiteOpenHelper {

    public DatabaseOperations(Context context) {
        super(context, "ReconMobile.db", null, 1);
        Log.d("DatabaseOperations", "Looking for ReconMobile.db...");
        SQLiteDatabase db = this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        Log.d("DatabaseOperations", "No Database Found! Creating ReconMobile.db...");
        String createTable;

        //Create Settings Table
        createTable = "CREATE TABLE SETTINGS (SettingsID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "AUTO_CLEAR_SESSIONS VARCHAR(6), UNIT_SYSTEM VARCHAR(2), SIGNATURE_OPTIONS VARCHAR(20), TILT_SENSITIVITY TINYINT(2))";
        db.execSQL(createTable);

        //Create Company Table
        createTable = "CREATE TABLE COMPANY (CompanyID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "COMPANY_NAME TEXT, COMPANY_DETAILS TEXT, COMPANY_EMAIL TEXT)";
        db.execSQL(createTable);

        //Create Report (default) Table
        createTable = "CREATE TABLE REPORT_DEFAULTS (DefaultID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "INSTRUMENT_LOCATION TEXT, CUSTOMER_INFORMATION TEXT, TEST_SITE_INFORMATION TEXT, DEPLOYED_BY TEXT, RETRIEVED_BY TEXT," +
                "ANALYZED_BY TEXT, PROTOCOL TEXT, TAMPERING TEXT, WEATHER TEXT, MITIGATION TEXT, COMMENT TEXT, REPORT_TEXT TEXT)";
        db.execSQL(createTable);

        //Default Report Text, to be inserted into a new database.
        String txtReportText;
        txtReportText = "The U.S. Environmental Protection Agency (US EPA) and the Surgeon General strongly recommend that further action be " +
                "taken when a home''s radon test results are 4.0 pCi/L or greater. The national average indoor radon level is about 1.3 " +
                "pCi/L. The higher the home''s radon level, the greater the health risk to you and your family. Reducing your radon levels " +
                "can be done easily, effectively and fairly inexpensively. Even homes with very high radon levels can be reduced below " +
                "4.0 pCi/L. Please refer to the EPA website at www.epa.gov/radon for further information to assist you in evaluating your " +
                "test results or deciding if further action is needed.";

        //Assign default values to newly created database
        db.execSQL("INSERT INTO SETTINGS (AUTO_CLEAR_SESSIONS, UNIT_SYSTEM, SIGNATURE_OPTIONS, TILT_SENSITIVITY) VALUES ('Always', 'US', 'Digitally Signed', '5')");
        db.execSQL("INSERT INTO COMPANY (COMPANY_NAME, COMPANY_DETAILS) VALUES ('','')");
        db.execSQL("INSERT INTO REPORT_DEFAULTS (INSTRUMENT_LOCATION, PROTOCOL, TAMPERING, WEATHER, MITIGATION, COMMENT, REPORT_TEXT) VALUES ('Basement', " +
                "'Closed Building Conditions Met', 'No Tampering Detected', 'No Abnormal Weather Conditions', 'No Mitigation System Installed', " +
                "'Thanks for the business!', '" + txtReportText + "')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS SETTINGS");
        db.execSQL("DROP TABLE IF EXISTS COMPANY");
        db.execSQL("DROP TABLE IF EXISTS REPORT_DEFAULTS");
        onCreate(db);
    }

    public void insertData(String table, String column, String value) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(column, value);

        long result = db.insert(table, null, contentValues);
        if(result == -1) {
            Log.d("DatabaseOperations", "DatabaseOperations.addData: FAILED to add " + value + "to " + table + "." + column);
        } else {
            Log.d("DatabaseOperations", "DatabaseOperations.addData: Adding " + value + "to " + table + "." + column);
        }

    }

    public void updateData(String table, String column, String value, String primarycolumn) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(column, value);

        db.update(table, contentValues,primarycolumn+"='1'",null);

        Log.d("DatabaseOperations", "DatabaseOperations.replaceData: Updating " + table + "." + column + " with" + value);
    }

    public Cursor getReportDefaultData() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor result = db.rawQuery("SELECT * FROM REPORT_DEFAULTS",null);
        return result;
    }

    public Cursor getCompanyData() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor result = db.rawQuery("SELECT * FROM COMPANY",null);
        return result;
    }

}

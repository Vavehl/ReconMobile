package com.example.reconmobile;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseOperations extends SQLiteOpenHelper {

    public DatabaseOperations(Context context) {
        super(context, "SETTINGS", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable;

        //Create Settings Table
        createTable = "CREATE TABLE SETTINGS (SettingsID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "AUTO_CLEAR_SESSIONS VARCHAR(6), UNIT_SYSTEM VARCHAR(2), SIGNATURE_OPTIONS VARCHAR(20), TILT_SENSITIVITY TINYINT(2)," +
                "DISPLAY_UNITS_RADON VARCHAR(6), DISPLAY_UNITS_TEMPERATURE VARCHAR(10), DISPLAY_UNITS_PRESSURE VARCHAR(4)," +
                "DISPLAY_DUAL_CHAMBER BOOLEAN, DISPLAY_READING_INTERVAL VARCHAR(15))";
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

        //Assign default values to newly created database

        //Settings
        addData("SETTINGS","AUTO_CLEAR_SESSIONS","Always");
        addData("SETTINGS","UNIT_SYSTEM","US");
        addData("SETTINGS","SIGNATURE_OPTIONS","Digitally Signed");
        addData("SETTINGS","TILT_SENSITIVITY","5");

        //...not adding display settings, as they are localized to the instrument.
        //Should we remove display settings from the database? I don't think they'll ever be used...

        //Report Defaults
        addData("REPORT_DEFAULTS","INSTRUMENT_LOCATION","Basement");
        addData("REPORT_DEFAULTS","PROTOCOL","Closed Building Conditions Met");
        addData("REPORT_DEFAULTS","TAMPERING","No Tampering Detected");
        addData("REPORT_DEFAULTS","WEATHER","No Abnormal Weather Conditions");
        addData("REPORT_DEFAULTS","MITIGATION","No Mitigation System Installed");
        addData("REPORT_DEFAULTS","COMMENT","Thanks for the business!");
        addData("REPORT_DEFAULTS","REPORT_TEXT","Radon is the second leading cause of lung cancer after smoking. " +
                "The U.S. Environmental Protection Agency (US EPA) and the Surgeon General strongly recommend that further action be " +
                "taken when a home’s radon test results are 4.0 pCi/L or greater. The national average indoor radon level is about 1.3 " +
                "pCi/L. The higher the home’s radon level, the greater the health risk to you and your family. Reducing your radon levels " +
                "can be done easily, effectively and fairly inexpensively. Even homes with very high radon levels can be reduced below " +
                "4.0 pCi/L. Please refer to the EPA website at www.epa.gov/radon for further information to assist you in evaluating your " +
                "test results or deciding if further action is needed.");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS SETTINGS");
        db.execSQL("DROP TABLE IF EXISTS COMPANY");
        db.execSQL("DROP TABLE IF EXISTS REPORT_DEFAULTS");
        onCreate(db);
    }

    public void addData(String table, String column, String item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(column, item);

        db.insert(table, null, contentValues);

        Log.d("DatabaseOperations", "DatabaseOperations.addData: Adding " + item + "to " + table + "." + column);
    }

    public void replaceData(String table, String column, String item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(column, item);

        db.replace(table, null, contentValues);

        Log.d("DatabaseOperations", "DatabaseOperations.replaceData: Replacing " + table + "." + column + " with" + item);
    }

    public Cursor getData(String table, String column) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT " + column + " FROM " + table;
        Cursor result = db.rawQuery(query,null);
        return result;
    }

}

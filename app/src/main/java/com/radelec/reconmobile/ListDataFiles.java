package com.radelec.reconmobile;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

public class ListDataFiles {

    private String FileName;
    private String DateModified;
    private String FilePath;

    public ListDataFiles(String FileName, String DateModified, String FilePath) {
    }

    public String getFileName() {
        return FileName;
    }

    public String getDateModified() {
        return DateModified;
    }

    public String getFilePath() {
        return FilePath;
    }

    public static ArrayList<ListDataFiles> CreateDataFileList(Context context) {
        ArrayList<ListDataFiles> alDataFiles = new ArrayList<ListDataFiles>();
        File f = new File(Objects.requireNonNull(context.getFilesDir().getAbsolutePath()));
        Log.d("ListDataFiles","Data directory = " + f);
        File[] files = f.listFiles();
        if(files!=null && files.length>0) {
            Log.d("ListDataFiles","Generating list of internal Recon data files...");
            alDataFiles.clear();
            for (File file : files) {
                ListDataFiles element = new ListDataFiles("","","");
                element.FileName = file.getName();
                element.DateModified = Long.toString(file.lastModified());
                element.FilePath = file.getAbsolutePath();
                alDataFiles.add(element);
                Log.d("ListDataFiles","Found " + file.getName());
            }
        } else {
            Log.d("ListDataFiles","No data files found -- returning empty list.");
        }
        ShowDataFileList(alDataFiles);
        return alDataFiles;
    }

    public static void ShowDataFileList(ArrayList<ListDataFiles> alDataFiles) {
        Log.d("ListDataFiles", "ShowDataFileList called!");
        for(int i=0; i<alDataFiles.size(); i++) {
            ListDataFiles element = alDataFiles.get(i);
            Log.d("ListDataFiles","[" + i + "] FileName=" + element.getFileName() + " / DateModified=" + element.getDateModified() + " / FilePath=" + element.getFilePath());
        }
    }
}

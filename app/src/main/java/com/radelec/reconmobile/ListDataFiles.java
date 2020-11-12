package com.radelec.reconmobile;

import android.content.Context;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

public class ListDataFiles {

    private String FileName;
    private Long DateModified;
    private String FilePath;

    public ListDataFiles(String FileName, Long DateModified, String FilePath) {
    }

    public String getFileName() {
        return FileName;
    }

    public Long getDateModified() {
        return DateModified;
    }

    public String getFilePath() {
        return FilePath;
    }

    public static ArrayList<ListDataFiles> CreateDataFileList(Context context) {
        ArrayList<ListDataFiles> alDataFiles = new ArrayList<ListDataFiles>();
        File f = new File(Objects.requireNonNull(context.getFilesDir().getAbsolutePath()));
        Logging.main("ListDataFiles","Data directory = " + f);
        File[] files = f.listFiles();
        if(files!=null && files.length>0) {
            Logging.main("ListDataFiles","Generating list of internal Recon data files...");
            alDataFiles.clear();
            for (File file : files) {
                ListDataFiles element = new ListDataFiles("", (long) 0,"");
                element.FileName = file.getName();
                element.DateModified = file.lastModified();
                element.FilePath = file.getAbsolutePath();
                alDataFiles.add(element);
                Logging.main("ListDataFiles","Found " + file.getName());
            }
        } else {
            Logging.main("ListDataFiles","No data files found -- returning empty list.");
        }
        ShowDataFileList(alDataFiles);
        return alDataFiles;
    }

    public static void ShowDataFileList(ArrayList<ListDataFiles> alDataFiles) {
        Logging.main("ListDataFiles", "ShowDataFileList called!");
        for(int i=0; i<alDataFiles.size(); i++) {
            ListDataFiles element = alDataFiles.get(i);
            Logging.main("ListDataFiles","[" + i + "] FileName=" + element.getFileName() + " / DateModified=" + element.getDateModified() + " / FilePath=" + element.getFilePath());
        }
    }
}

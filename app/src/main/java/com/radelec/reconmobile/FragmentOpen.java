package com.radelec.reconmobile;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Objects;

public class FragmentOpen extends DialogFragment implements FileSearchListAdapter.OnFileSearchListAdapterListener {

    //RecyclerView stuff
    private ArrayList<ListDataFiles> alDataFiles;
    private RecyclerView mRecyclerView;

    public FragmentOpen() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d("FragmentOpen","onCreate() called!");
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.DialogStyle);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Log.d("FragmentOpen","FragmentOpen.onCreateView() called!");
        View view = inflater.inflate(R.layout.fragment_open, container, false);
        ImageView imgCloseSearch = view.findViewById(R.id.imgClose_openFile);
        alDataFiles = ListDataFiles.CreateDataFileList(Objects.requireNonNull(getContext()));
        mRecyclerView = (RecyclerView) view.findViewById(R.id.rvReconDataFiles);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(new FileSearchListAdapter(ListDataFiles.CreateDataFileList(Objects.requireNonNull(getContext())),this));

        imgCloseSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("FragmentOpen","Close button pressed!");
                dismiss();
            }
        });

        //The dialog box looks too small if we don't set a minimum height (regardless of whether we discover a Recon or not)
        view.setMinimumHeight(1000);

        return view;
    }

    @Override
    public void onFileSearchListAdapterClick(int position) {
        Log.d("FragmentOpen","onFileSearchListAdapterClick called for position[" + position + "]");
        if(Globals.boolClickToLoad) {
            String strFileName = alDataFiles.get(position).getFileName();
            String strFilePath = alDataFiles.get(position).getFilePath();
            Globals.globalLoadedFileName = "";
            LoadSavedFile.main(strFilePath, strFileName);
            Globals.connected = Globals.ReconConnected.Loaded;
            Toast msgSave = Toast.makeText(getContext(),"Loading file...",Toast.LENGTH_SHORT);
            msgSave.show();

            //BEGIN: Refresh FragmentConnect
            Fragment frg = null;
            if (getFragmentManager() != null) {
                frg = getFragmentManager().findFragmentByTag("fragConnect");
            }
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.detach(frg);
            ft.attach(frg);
            ft.commit();
            //END: Refresh FragmentConnect

            dismiss();
        } else {
            Globals.boolClickToLoad = true;
        }
    }
}

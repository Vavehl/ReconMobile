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

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.Objects;

import static com.radelec.reconmobile.Globals.connected;

public class FragmentOpen extends DialogFragment implements FileSearchListAdapter.OnFileSearchListAdapterListener {

    //RecyclerView stuff
    private ArrayList<ListDataFiles> alDataFiles;

    private int intLastPositionHighlighted = -1;

    public FragmentOpen() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Logging.main("FragmentOpen","onCreate() called!");
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.DialogStyle);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        Logging.main("FragmentOpen","FragmentOpen.onCreateView() called!");
        View view = inflater.inflate(R.layout.fragment_open, container, false);
        ImageView imgCloseSearch = view.findViewById(R.id.imgClose_openFile);
        alDataFiles = ListDataFiles.CreateDataFileList(Objects.requireNonNull(getContext()));
        RecyclerView mRecyclerView = view.findViewById(R.id.rvReconDataFiles);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(new FileSearchListAdapter(ListDataFiles.CreateDataFileList(Objects.requireNonNull(getContext())),this));

        imgCloseSearch.setOnClickListener(v -> {
            Logging.main("FragmentOpen","Close button pressed!");
            dismiss();
        });

        //The dialog box looks too small if we don't set a minimum height (regardless of whether we discover a Recon or not)
        view.setMinimumHeight(1000);

        return view;
    }

    @Override
    public void onFileSearchListAdapterClick(int position) {
        Logging.main("FragmentOpen","onFileSearchListAdapterClick called for position[" + position + "]");
        if(Globals.boolClickToLoad && intLastPositionHighlighted == position) {
            String strFileName = alDataFiles.get(position).getFileName();
            String strFilePath = alDataFiles.get(position).getFilePath();
            Globals.globalLoadedFileName = "";
            LoadSavedFile.main(strFilePath, strFileName);
            Globals.connected = Globals.ReconConnected.Loaded;
            Toast msgSave = Toast.makeText(getContext(),"Loading file...",Toast.LENGTH_SHORT);
            msgSave.show();

            //BEGIN: Refresh FragmentConnect
            Fragment frg;
            if (getFragmentManager() != null) {
                frg = getFragmentManager().findFragmentByTag("fragConnect");
                FragmentTransaction ft;
                ft = getFragmentManager().beginTransaction();
                if (frg != null) {
                    ft.detach(frg);
                    ft.attach(frg);
                    ft.commit();
                    TabLayout tabLayout = frg.getView().getRootView().findViewById(R.id.tabLayout_Main);
                    if (connected == Globals.ReconConnected.Loaded) {
                        tabLayout.getTabAt(2).setText("Report");
                        Logging.main("FragmentOpen","Setting tab to Report...");
                    } else {
                        tabLayout.getTabAt(2).setText("Defaults");
                        Logging.main("FragmentOpen","Setting tab to Defaults...");
                    }
                }
            }
            //END: Refresh FragmentConnect

            dismiss();
        } else {
            Globals.boolClickToLoad = true;
            intLastPositionHighlighted = position;
        }
    }
}

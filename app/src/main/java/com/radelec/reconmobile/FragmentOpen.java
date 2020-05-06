package com.radelec.reconmobile;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

public class FragmentOpen extends DialogFragment {

    private FragmentSearch.OnFragmentInteractionListener mListener;

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

        imgCloseSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("FragmentOpen","Close button pressed!");
                dismiss();
            }
        });

        //Begin searching for Recons and populate the Dialog fragment with the discovered devices...
        getChildFragmentManager().beginTransaction().add(R.id.openFile_layout, new FileSearchList()).addToBackStack(null).commit();

        //The dialog box looks too small if we don't set a minimum height (regardless of whether we discover a Recon or not)
        view.setMinimumHeight(1000);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        Log.d("FragmentOpen","onAttach() called!");
        super.onAttach(context);
        if (context instanceof FragmentSearch.OnFragmentInteractionListener) {
            mListener = (FragmentSearch.OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        Log.d("FragmentOpen","onDetach() called!");
        super.onDetach();
        mListener = null;
    }

}

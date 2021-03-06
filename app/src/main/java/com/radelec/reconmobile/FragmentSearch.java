package com.radelec.reconmobile;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import static com.radelec.reconmobile.Globals.*;

public class FragmentSearch extends DialogFragment {

    private OnFragmentInteractionListener mListener;

    public FragmentSearch() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Logging.main("FragmentSearch","onCreate() called!");
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.DialogStyle);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Logging.main("FragmentSearch","FragmentSearch.onCreateView() called!");
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        ImageView imgCloseSearch = view.findViewById(R.id.imgCloseSearch);

        imgCloseSearch.setOnClickListener(v -> {
            Logging.main("FragmentSearch","Close button pressed!");
            Logging.main("FragmentSearch", "Recon Connected? [" + connected + "]");

            //BEGIN: Refresh FragmentConnect
            Fragment frg = null;
            if (getFragmentManager() != null) {
                frg = getFragmentManager().findFragmentByTag("fragConnect");
                FragmentTransaction ft = null;
                ft = getFragmentManager().beginTransaction();
                if (frg != null) {
                    ft.detach(frg);
                    ft.attach(frg);
                    ft.commit();
                }
            }
            //END: Refresh FragmentConnect

            dismiss();
        });

        //Begin searching for Recons and populate the Dialog fragment with the discovered devices...
        getChildFragmentManager().beginTransaction().add(R.id.device_search_layout, new ReconSearchList()).addToBackStack(null).commit();

        //The dialog box looks too small if we don't set a minimum height (regardless of whether we discover a Recon or not)
        view.setMinimumHeight(1000);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        Logging.main("FragmentSearch","onAttach() called!");
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        Logging.main("FragmentSearch","onDetach() called!");
        super.onDetach();
        mListener = null;
        FragmentManager fm = getFragmentManager();
        FragmentConnect fragmentConnect = null;
        if (fm != null) {
            fragmentConnect = (FragmentConnect)fm.findFragmentByTag("fragConnect");
            if (fragmentConnect != null) {
                fragmentConnect.checkConnectionStatus();
            } else {
                Logging.main("FragmentSearch","Fragment Connect is NULL!");
            }
        } else {
            Logging.main("FragmentSearch","Fragment Manager is NULL!");
        }

    }

    interface OnFragmentInteractionListener {
    }
}

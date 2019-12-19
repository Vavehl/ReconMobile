package com.example.reconmobile;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import static com.example.reconmobile.Globals.*;

public class FragmentSearch extends DialogFragment {

    private OnFragmentInteractionListener mListener;

    public FragmentSearch() {
        // Required empty public constructor
    }

    public static FragmentSearch newInstance() {
        FragmentSearch fragment = new FragmentSearch();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d("FragmentSearch","onCreate() called!");
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.DialogStyle);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Log.d("FragmentSearch","FragmentSearch.onCreateView() called!");
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        ImageView imgCloseSearch = view.findViewById(R.id.imgCloseSearch);

        imgCloseSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("FragmentSearch","Close button pressed!");
                Log.d("FragmentSearch", "Recon Connected? [" + connected + "]");
                dismiss();
            }
        });

        //Begin searching for Recons and populate the Dialog fragment with the discovered devices...
        getChildFragmentManager().beginTransaction().add(R.id.device_search_layout, new ReconSearchList()).addToBackStack(null).commit();

        //The dialog box looks too small if we don't set a minimum height (regardless of whether we discover a Recon or not)
        view.setMinimumHeight(1000);

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }

    }

    @Override
    public void onAttach(Context context) {
        Log.d("FragmentSearch","onAttach() called!");
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
        Log.d("FragmentSearch","onDetach() called!");
        super.onDetach();
        mListener = null;
        FragmentManager fm = getFragmentManager();
        FragmentConnect fragmentConnect = null;
        if (fm != null) {
            fragmentConnect = (FragmentConnect)fm.findFragmentByTag("fragConnect");
            if (fragmentConnect != null) {
                fragmentConnect.checkConnectionStatus();
            } else {
                Log.d("FragmentSearch","Fragment Connect is NULL!");
            }
        } else {
            Log.d("FragmentSearch","Fragment Manager is NULL!");
        }

    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}

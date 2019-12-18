package com.example.reconmobile;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import static com.example.reconmobile.Globals.*;

public class FragmentConnect extends Fragment {

    Button btnConnect;
    Button btnDownload;
    Button btnClear;
    TextView txtReconSerial;
    TextView txtSystemConsole;
    Space spaceConnect_1;

    public FragmentConnect() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_connect, container, false);

        btnConnect = view.findViewById(R.id.buttonConnect);
        btnDownload = view.findViewById(R.id.buttonDownload);
        btnClear = view.findViewById(R.id.buttonClear);
        txtReconSerial = view.findViewById(R.id.txtReconSerial);
        txtSystemConsole = view.findViewById(R.id.txtConsole);
        spaceConnect_1 = view.findViewById(R.id.spaceConnect_1);

        switch(connected) {
            case True:
                Log.d("FragmentConnect","Setting button to Disconnect [connected = " + connected + "]");
                btnConnect.setText("Disconnect");
                break;
            case False:
                Log.d("FragmentConnect","Setting button to Connect [connected = " + connected + "]");
                btnConnect.setText("Connect");
                break;
            case Pending:
                Log.d("FragmentConnect","Setting button to Wait [connected = " + connected + "]");
                btnConnect.setText("Wait");
                break;
        }
        checkConnectionStatus();

        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch(connected) {
                    case True:
                        Log.d("FragmentConnect","Disconnect button pressed!");
                        Toast txtOnClick_Disconnect = Toast.makeText(getContext(),"Disconnecting...",Toast.LENGTH_SHORT);
                        txtOnClick_Disconnect.show();
                        disconnect();
                        checkConnectionStatus();
                        break;
                    case False:
                        Log.d("FragmentConnect","Connect button pressed!");
                        Toast txtOnClick_Connect = Toast.makeText(getContext(),"Searching for Recon...",Toast.LENGTH_SHORT);
                        txtOnClick_Connect.show();
                        showSearchList();
                        break;
                    case Pending:
                        Toast txtOnClick_Pending = Toast.makeText(getContext(),"Please wait...",Toast.LENGTH_SHORT);
                        txtOnClick_Pending.show();
                        break;
                }
            }
        });

        return view;
    }

    //Show Device Search popup
    protected void showSearchList() {
        Log.d("FragmentConnect", "showSearchList() called!");
        FragmentSearch dialogSearch = new FragmentSearch();
        dialogSearch.setRetainInstance(true);
        dialogSearch.show(getFragmentManager(),"");

        if(dialogSearch.isDetached()) {
            Log.d("FragmentConnect","SEARCH FRAGMENT DETACHED!");
            checkConnectionStatus();
        }
    }

    public void onStart() {
        Log.d("FragmentConnect", "onStart() called!");
        super.onStart();
    }

    public void onPause() {
        Log.d("FragmentConnect", "onPause() called!");
        super.onPause();
    }

    public void onResume() {
        Log.d("FragmentConnect", "onResume() called!");
        super.onResume();
        checkConnectionStatus(); //This is needed to retain button configuration, system console, etc. when the screen orientation changes.
    }

    public void checkConnectionStatus() {
        Log.d("FragmentConnect", "checkConnectionStatus() called!");
        switch(connected) {
            case True:
                Log.d("FragmentConnect","Setting button to Disconnect [connected = " + connected + "]");
                btnClear.setVisibility(View.VISIBLE);
                btnConnect.setText("Disconnect");
                btnDownload.setVisibility(View.VISIBLE);
                spaceConnect_1.setVisibility(View.GONE);
                txtReconSerial.setText(String.format("Recon #%s", globalReconSerial));
                txtReconSerial.setVisibility(View.VISIBLE);
                txtSystemConsole.setVisibility(View.VISIBLE);
                break;
            case False:
                Log.d("FragmentConnect","Setting button to Connect [connected = " + connected + "]");
                btnClear.setVisibility(View.GONE);
                btnConnect.setText("Connect");
                btnDownload.setVisibility(View.GONE);
                spaceConnect_1.setVisibility(View.VISIBLE);
                txtReconSerial.setText("No Recon Connected");
                txtReconSerial.setVisibility(View.GONE);
                txtSystemConsole.setVisibility(View.GONE);
                break;
            case Pending: //This should never proc...
                Log.d("FragmentConnect","Pending connection... [connected = " + connected + "]");
                btnClear.setVisibility(View.GONE);
                btnConnect.setText("Disconnect");
                btnDownload.setVisibility(View.GONE);
                spaceConnect_1.setVisibility(View.GONE);
                txtReconSerial.setText("Please Wait...");
                txtReconSerial.setVisibility(View.GONE);
                txtSystemConsole.setVisibility(View.GONE);
                break;
        }
    }

    private void disconnect() {
        Log.d("FragmentConnect","disconnect() called!");
        connected = ReconConnected.False;
        service.disconnect();
        socket.disconnect();
        socket = null;
        globalReconSerial = "";
        globalReconFirmwareRevision = 0;
    }

}

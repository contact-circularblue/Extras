package com.example.pooja.style;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    ListView lv;
    ArrayAdapter<String> adapter;
    ArrayList<String> available_conn = new ArrayList<String>();
    ArrayList<String> bssid_list = new ArrayList<String>();
    EditText pass;
    Button button, connect;
    WifiManager wifiManager;
    WifiReceiver wifiReceiver;
    List<ScanResult> wifiList;
    int networkId;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_WIFI = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = (Button) findViewById(R.id.button);
        connect = (Button) findViewById(R.id.connect);
        pass = (EditText) findViewById(R.id.pass);
        pass.setEnabled(false);
        lv = (ListView) findViewById(R.id.lv);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, available_conn);
        lv.setAdapter(adapter);
        available_conn.clear();
        bssid_list.clear();
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if (wifiManager.isWifiEnabled() == false) {
            wifiManager.setWifiEnabled(true);
        }
        wifiReceiver = new WifiReceiver();
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        wifiManager.startScan();
        askPermission();
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wifiManager.startScan();
            }
        });
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String list = available_conn.toString();
                Log.d("list", list);
                final String selected_wifi = (String) parent.getItemAtPosition(position);
                pass.setEnabled(true);
                connect.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        connectWifi(selected_wifi);
                    }
                });
            }
        });
    }

    private void askPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_WIFI);

        } else {
            wifiManager.startScan();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_WIFI: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    wifiManager.startScan();
                } else {
                    Toast.makeText(getApplicationContext(),
                            "failed, please try again.", Toast.LENGTH_LONG).show();
                    return;
                }
            }
        }
    }

    public void connectWifi(String networkSSID) {
        WifiConfiguration conf = new WifiConfiguration();
        String networkPass = pass.getText().toString();
        conf.SSID = "\"" + networkSSID + "\"";
        conf.preSharedKey = "\"" + networkPass + "\"";
        wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        networkId = wifiManager.addNetwork(conf);
        wifiManager.disconnect();
        wifiManager.enableNetwork(networkId, true);
        wifiManager.reconnect();
        Log.d("statewifi",networkId+"");
        Toast.makeText(MainActivity.this, "wifi should be connected", Toast.LENGTH_SHORT).show();
    }

    protected void onPause() {
        unregisterReceiver(wifiReceiver);
        super.onPause();
    }

    protected void onResume() {
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        super.onResume();
    }

    class WifiReceiver extends BroadcastReceiver {
        public void onReceive(Context c, Intent intent) {
            if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                wifiList = wifiManager.getScanResults();
                for (int i = 0; i < wifiList.size(); i++) {
                    String wifi = wifiList.get(i).toString();
                    Log.d("wifi", wifi);
                    String[] s = wifi.split(",");
                    String s1 = s[0];
                    String s2 = s[1];
                    String[] ssid = s1.split(":");
                    String ssid_final = ssid[1].trim();
                    String[] bssid = s2.split("BSSID:");
                    String bssid_final = bssid[1].trim();
                    if (bssid_list.toString().contains(bssid_final)) {
                    } else {
                        available_conn.add(ssid_final);
                        adapter.notifyDataSetChanged();
                        bssid_list.add(bssid_final);
                    }
                }
            }

        }
    }
}
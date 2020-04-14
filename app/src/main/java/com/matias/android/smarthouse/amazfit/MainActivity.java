package com.matias.android.smarthouse.amazfit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    public static final String TokenPreference = "device_token";

    private static final String URL = "https://us-central1-smart-house-dbd56.cloudfunctions.net/api";
    private static final String DEVICES_PATH = URL + "/devices/";
    private static final String DEVICE_PATH = URL + "/device/";
    private String DEVICE_TOKEN = "";
    private RequestQueue queue;

    private Spinner spinner;
    private LinearLayout onOffContainer, openContainer;
    private Button wifion, wifioff, lighton, lightoff, dooropen;
    private String url, type;
    private ProgressBar progress;
    private WifiManager wifi;
    private ArrayList<ArrayList<String>> switches;

    public void wifiGoesOn () {
        if(wifion.isEnabled()) {
            wifion.setEnabled(false);
            wifioff.setEnabled(true);
            lighton.setEnabled(true);
            lightoff.setEnabled(true);
            dooropen.setEnabled(true);
            progress.setVisibility(ProgressBar.INVISIBLE);

            String url = DEVICES_PATH + DEVICE_TOKEN;
            final JsonArrayRequest request = new JsonArrayRequest(
                    url,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            switches = new ArrayList<>();
                            Log.e(MainActivity.class.getSimpleName(), "Response is: "+ response.toString());

                            try {
                                for(int i = 0; i < response.length(); i++) {
                                    final JSONObject deviceJson = response.getJSONObject(i);
                                    switches.add(new ArrayList<String>() {
                                        {
                                            add(getDeviceName(deviceJson));
                                            add(deviceJson.getString("id"));
                                            add(deviceJson.getString("type"));
                                        }
                                    });
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            setUrls(0);
                            setSpinnerAdapter();
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e(MainActivity.class.getSimpleName(), "That didn't work!", error);
                        }
                    }
            );
            queue.add(request);
        }
    }

    public void wifiGoesOff () {
        if(wifioff.isEnabled()) {
            wifion.setEnabled(true);
            wifioff.setEnabled(false);
            lighton.setEnabled(false);
            lightoff.setEnabled(false);
            dooropen.setEnabled(false);
            progress.setVisibility(ProgressBar.INVISIBLE);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //ma = this;
        setContentView(R.layout.activity_main);

        SharedPreferences preferences = getSharedPreferences("smart_home", MODE_PRIVATE);
        if(!preferences.contains(TokenPreference)) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return;
        }
        DEVICE_TOKEN = preferences.getString(TokenPreference, "");

        wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        progress = findViewById(R.id.progressBar);
        onOffContainer = findViewById(R.id.on_off_container);
        openContainer = findViewById(R.id.open_container);

        wifion = findViewById(R.id.wifi_on);
        wifion.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wifi.setWifiEnabled(true);
                progress.setVisibility(ProgressBar.VISIBLE);
            }
        });

        wifioff = findViewById(R.id.wifi_off);
        wifioff.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wifi.setWifiEnabled(false);
                progress.setVisibility(ProgressBar.VISIBLE);
            }
        });

        queue = Volley.newRequestQueue(this);

        lighton = findViewById(R.id.light_on);
        lighton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(MainActivity.class.getSimpleName(), "on: " + url + "?state=true");

                StringRequest request = new StringRequest(
                        url + "?state=true",
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Log.e(MainActivity.class.getSimpleName(), "Response is: "+ response);
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.e(MainActivity.class.getSimpleName(), "That didn't work!", error);
                            }
                        }
                );
                queue.add(request);
            }
        });

        lightoff = findViewById(R.id.light_off);
        lightoff.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(MainActivity.class.getSimpleName(), "off: " + url);

                StringRequest request = new StringRequest(
                        url + "?state=false",
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Log.e(MainActivity.class.getSimpleName(), "Response is: "+ response);
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.e(MainActivity.class.getSimpleName(), "That didn't work!", error);
                            }
                        }
                );
                queue.add(request);
            }
        });

        dooropen = findViewById(R.id.door_open);
        dooropen.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(MainActivity.class.getSimpleName(), "open: " + url);

                StringRequest request = new StringRequest(
                        url + "?state=true",
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Log.e(MainActivity.class.getSimpleName(), "Response is: "+ response);
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.e(MainActivity.class.getSimpleName(), "That didn't work!", error);
                            }
                        }
                );
                queue.add(request);
            }
        });

        spinner = findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setUrls(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { setUrls(0); }
        });

        WifiReceiver wifirec = new WifiReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        registerReceiver(wifirec, filter);
    }

    private void setSpinnerAdapter() {
        ArrayList<String> names = new ArrayList<String>();
        for (int counter = 0; counter < switches.size(); counter++) {
            names.add (switches.get(counter).get(0) );
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, names);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        this.spinner.setAdapter(adapter);
    }

    private void setUrls(int position) {
        String deviceId = MainActivity.this.switches.get(position).get(1);
        String type = MainActivity.this.switches.get(position).get(2);
        if(type.equals("action.devices.types.DOOR")) {
            url = DEVICES_PATH + DEVICE_TOKEN + "/" + deviceId + "/open";
            openContainer.setVisibility(View.VISIBLE);
            onOffContainer.setVisibility(View.GONE);
        }
        else {
            url = DEVICES_PATH + DEVICE_TOKEN + "/" + deviceId + "/on";
            onOffContainer.setVisibility(View.VISIBLE);
            openContainer.setVisibility(View.GONE);
        }
    }

    public class WifiReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            NetworkInfo.State connected = info.getState();
            if (connected == NetworkInfo.State.CONNECTED) {
                MainActivity.this.wifiGoesOn();
            } else if (connected == NetworkInfo.State.DISCONNECTED) {
                MainActivity.this.wifiGoesOff();
            }
        }
    }

    private String getDeviceName(JSONObject object) throws JSONException {
        JSONObject names = object.getJSONObject("name");

        String s = names.getString("name");
        if (names.getJSONArray("defaultNames").length() > 0) {
            s = names.getJSONArray("defaultNames").getString(0);
        }

        if (names.getJSONArray("nicknames").length() > 0) {
            s = names.getJSONArray("nicknames").getString(0);
        }

        return s;
    }
}
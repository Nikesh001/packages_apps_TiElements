/*
 * Copyright (C) 2019 TitaniumOS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.titanium.tielements.fragments;

import android.content.Context;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import android.provider.Settings;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.Utils;

public class Maintainers extends SettingsPreferenceFragment implements Preference.OnPreferenceChangeListener {
    
    private static final String TAG = "Maintainers";
    private static final String REQUEST_TAG = "loadDeviceList";
    private static final String ANDROID_VERSION_TAG = "ten";
    private static final String URL = "https://raw.githubusercontent.com/Titanium-OS-Devices/official_devices/master/devices.json";
    private static final String DOWNLOAD_WEBSITE = "https://downloads.titaniumandroidos.com";
    private static final String SHARED_PREF_NAME = "titanium";
    private static final int MENU_RELOAD  = 0;
    PreferenceScreen prefScreen;
    SharedPreferences sharedPreferences;
    ProgressDialog progressDialog;
    OkHttpClient httpClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.maintainers_devices);
        setRetainInstance(true);

        ContentResolver resolver = getActivity().getContentResolver();
        prefScreen = getPreferenceScreen();
        sharedPreferences = getActivity().getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        loadDeviceList();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(0, MENU_RELOAD, 0, R.string.reload_list)
                .setIcon(R.drawable.ic_menu_refresh)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_RELOAD:
                loadDeviceList();
                return true;
            default:
                return false;
        }
    }

    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage(getString(R.string.loading_devices));
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(true);
            progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    cancelRequests(httpClient,REQUEST_TAG);
                }
            });
        }
        progressDialog.show();
    }

    private void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void loadDeviceList() {
        showProgressDialog();
        prefScreen.removeAll();
        final String jsonLocal = getLocalJSON();
        final Boolean isJsonLocalValid = isJSONValid(jsonLocal);
        Request request = new Request.Builder()
                .tag(REQUEST_TAG)
                .url(URL)
                .build();
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("API Request failed", e.toString());
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dismissProgressDialog();
                        if (isJsonLocalValid) {
                            populate(jsonLocal);
                        } else {
                            showToast(getString(R.string.loading_devices_failed));
                            deleteLocalJSON();
                        }
                    }
                });
            }
            @Override
            public void onResponse(Call call, final Response response) throws IOException {
//                Log.d("API Request Success", response.body().string());
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dismissProgressDialog();
                        try {
                            if (response.isSuccessful()) {
                                String jsonStr = response.body().string();
                                if (!isJSONValid(jsonStr)) {
                                    if (isJsonLocalValid) {
                                        jsonStr = jsonLocal;
                                    }else{
                                        showToast(getString(R.string.loading_devices_failed));
                                        deleteLocalJSON();
                                    }
                                } else {
                                    saveJSON(jsonStr);
                                }
                                populate(jsonStr);
                            }else{
                                if (isJsonLocalValid) {
                                    populate(jsonLocal);
                                } else {
                                    showToast(getString(R.string.loading_devices_failed));
                                    deleteLocalJSON();
                                }
                            }
                        } catch (Exception e) {
                            showToast(getString(R.string.loading_devices_failed));
                            deleteLocalJSON();
                            e.printStackTrace();
                        }
                    }
                });
            }
        });

    }

    public void cancelRequests(OkHttpClient client, Object tag) {
        for (Call call : client.dispatcher().queuedCalls()) {
            if (tag.equals(call.request().tag())) call.cancel();
        }
        for (Call call : client.dispatcher().runningCalls()) {
            if (tag.equals(call.request().tag())) call.cancel();
        }
    }

    private void populate(String jsonStr) {
        ArrayList<String> brands = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(jsonStr);
            if (jsonArray.length() < 1){
                showToast(getString(R.string.devices_list_empty));
                deleteLocalJSON();
            }else {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObj = jsonArray.getJSONObject(i);
                    final String brand = jsonObj.getString("brand");
                    if (!brands.contains(brand)){
                        brands.add(brand);
                    }
                }
                Collections.sort(brands);
                for (final String brand: brands) {
                    PreferenceCategory brandCategory;
                    brandCategory = new PreferenceCategory(prefScreen.getContext());
                    brandCategory.setTitle(brand);
                    prefScreen.addPreference(brandCategory);
                    HashMap<String, HashMap<String, String>> devices = getDevicesByBrand(jsonArray,brand);
                    SortedSet<String> devices_sorted = new TreeSet<>(devices.keySet());
                    for (final String name : devices_sorted) {
                        final String codename = devices.get(name).get("codename");
                        final String maintainer_name = devices.get(name).get("maintainer_name");
                        final String xda_thread = devices.get(name).get("xda_thread");
                        Preference devicePref = new Preference(prefScreen.getContext());
                        devicePref.setIcon(R.drawable.ic_devs_phone);
                        devicePref.setTitle(name);
                        devicePref.setSummary(codename + "\n" + String.format(getString(R.string.maintainer_description), maintainer_name));
                        devicePref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                            @Override
                            public boolean onPreferenceClick(Preference preference) {
                                boolean isEmpty = xda_thread == null || xda_thread.trim().length() == 0;
                                try{
                                    if (isEmpty) {
                                        getActivity().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(DOWNLOAD_WEBSITE + "/" + Uri.encode(codename) + "/")));
                                    }else{
                                        getActivity().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(xda_thread)));
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Log.e(TAG, e.toString());
                                }
                                return true;
                            }
                        });
                        brandCategory.addPreference(devicePref);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());
        }
    }


    private HashMap<String, HashMap<String, String>> getDevicesByBrand(JSONArray jsonArray_, String brand_){
        HashMap<String, HashMap<String, String>> result = new HashMap<>();
        JSONArray jsonArray = jsonArray_;

        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject jsonObj = jsonArray.getJSONObject(i);
                String name = jsonObj.getString("name");
                String brand = jsonObj.getString("brand");
                String codename = jsonObj.getString("codename");
                String maintainer_name = null, xda_thread = null;
                JSONArray android_versions = jsonObj.getJSONArray("android_versions");
                for (int j = 0; j < android_versions.length(); j++) {
                    try {
                        JSONObject versionObj = android_versions.getJSONObject(j);
                        String android_version = versionObj.getString("version_code");
                        if (android_version.equals(ANDROID_VERSION_TAG)) {
                            maintainer_name = versionObj.getString("maintainer_name");
                            xda_thread = versionObj.getString("xda_thread");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        result = new HashMap<>();
                    }
                }
                if (brand.equals(brand_)){
                    HashMap<String, String> vars = new HashMap<>();
                    vars.put("codename",codename);
                    vars.put("maintainer_name",maintainer_name);
                    vars.put("xda_thread",xda_thread);
                    result.put(name,vars);
                }
            } catch (Exception e) {
                e.printStackTrace();
                result = new HashMap<>();
            }
        }
        return result;
    }

    private void showToast(String message, int duration) {
        Toast.makeText(getActivity(), message, duration).show();
    }

    private void showToast(String message) {
        showToast(message, Toast.LENGTH_SHORT);
    }

    private void deleteLocalJSON() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    private String getLocalJSON() {
        return sharedPreferences.getString("devices", null);
    }

    private void saveJSON(String json) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("devices", json);
        editor.apply();
    }

    private boolean isJSONValid(String str) {
        if (str == null || str.isEmpty()){
            return false;
        }
        try {
            new JSONObject(str);
        } catch (JSONException ex) {
            try {
                new JSONArray(str);
            } catch (JSONException ex1) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.TIELEMENTS;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        final String key = preference.getKey();
        return true;
    }

}
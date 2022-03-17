package com.crisnello.smsforwarder;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.crisnello.smsforwarder.service.BackgroundService;
import com.crisnello.smsforwarder.util.Util;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "SettingsActivity";

    private Button btnStart;

    private String signature;
    public String getSignature() {
        return signature;
    }
    public void setSignature(String signature) {
        this.signature = signature;
    }

    private class SetConf implements OnSetConf{
        @Override
        public void onNewSignature(String sig) {
            setSignature(sig);
        }

        @Override
        public void onNewReply(String rep) {

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment(new SetConf()))
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        btnStart = findViewById(R.id.btnSave);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();  return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(getSignature() != null && !TextUtils.isEmpty(getSignature())) {
            (new Util(this)).showToast("Ass: "+getSignature());
        }
        startActivity(new Intent(SettingsActivity.this,MainActivity.class));
    }


    @Override
    protected void onResume() {
        super.onResume();
        stopService(new Intent(getApplicationContext(), BackgroundService.class));
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {


        private EditTextPreference signature, targetNumber;
        private ListPreference reply;
        private SetConf setConf;

        public SettingsFragment(SetConf setConf) {
            this.setConf = setConf;
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            SharedPreferences spStore = getContext().getSharedPreferences(Constants.spStorage, MODE_PRIVATE);

            reply = (ListPreference) findPreference(Constants.replyKey);
            reply.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    String strReply = (String) newValue;
                    spStore.edit().putString(Constants.replyKey,strReply).apply();
                    setConf.onNewReply(strReply);
                    return true;
                }
            });


            signature = (EditTextPreference) findPreference(Constants.signatureKey);
            signature.setText(spStore.getString(Constants.signatureKey, ""));
            signature.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    String strSignature = (String) newValue;
                    spStore.edit().putString(Constants.signatureKey,strSignature).apply();
                    setConf.onNewSignature(strSignature);
                    return true;
                }
            });

            targetNumber = (EditTextPreference) findPreference(Constants.targetNumberKey);
            targetNumber.setText(spStore.getString(Constants.targetNumberKey, ""));
            targetNumber.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    String strTargetNumber = (String) newValue;
                    spStore.edit().putString(Constants.targetNumberKey,strTargetNumber).apply();
                    return true;
                }
            });

        }
    }
}
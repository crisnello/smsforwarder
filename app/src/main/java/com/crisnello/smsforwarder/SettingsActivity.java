package com.crisnello.smsforwarder;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

public class SettingsActivity extends AppCompatActivity {

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
        Toast.makeText(this," ass: "+getSignature(),Toast.LENGTH_SHORT).show();
        finish();
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        private EditTextPreference signature;
        private SetConf setConf;

        public SettingsFragment(SetConf setConf) {
            this.setConf = setConf;
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            signature = (EditTextPreference) findPreference("signature");
            SharedPreferences getSignatgureStore = getContext().getSharedPreferences("signatureStorage", Context.MODE_PRIVATE);
            signature.setText(getSignatgureStore.getString("signature", ""));
            signature.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    String strSignature = (String) newValue;
//                    preference.setSummary(strSignature);
                    setConf.onNewSignature(strSignature);
                    return true;
                }
            });
        }
    }
}
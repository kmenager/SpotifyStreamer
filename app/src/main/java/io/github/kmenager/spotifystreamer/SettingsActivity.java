package io.github.kmenager.spotifystreamer;


import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.MenuItem;

import com.neovisionaries.i18n.CountryCode;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SettingsActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new PrefsFragment())
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            super.onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class PrefsFragment extends PreferenceFragment implements
            Preference.OnPreferenceChangeListener {

        private ListPreference mListPreference;
        private String mDefaultCountryCode = "";

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.preferences);

            mListPreference = (ListPreference) findPreference("country");

            setCountryCodeList();
            mListPreference.setOnPreferenceChangeListener(this);
            onPreferenceChange(mListPreference,
                    PreferenceManager
                            .getDefaultSharedPreferences(mListPreference.getContext())
                            .getString(mListPreference.getKey(), mDefaultCountryCode));

            Preference preference = findPreference("notification_visibility");
            preference.setOnPreferenceChangeListener(this);
            onPreferenceChange(preference,
                    PreferenceManager
                            .getDefaultSharedPreferences(preference.getContext())
                            .getString(preference.getKey(), "PUBLIC"));
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String stringValue = newValue.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list (since they have separate labels/values).
                ListPreference listPreference = (ListPreference) preference;
                int prefIndex = listPreference.findIndexOfValue(stringValue);
                if (prefIndex >= 0) {
                    preference.setSummary(listPreference.getEntries()[prefIndex]);
                    ((ListPreference) preference).setValueIndex(prefIndex);
                }
            }
            return true;
        }

        private void setCountryCodeList() {
            int size = CountryCode.values().length;
            List<String> countries = new ArrayList<>();
            List<String> codes = new ArrayList<>();
            String locale = CountryCode.getByLocale(Locale.getDefault()).getAlpha2();
            int positionInList = 0;
            int foundedPosition = 0;
            for (CountryCode countryCode : CountryCode.values()) {
                if (countryCode.getAlpha2().equals(locale)) {
                    foundedPosition = positionInList;
                }
                codes.add(countryCode.getAlpha2());
                countries.add(countryCode.getName());
                positionInList++;
            }

            CharSequence[] entries = countries.toArray(new CharSequence[size]);
            CharSequence[] entryValues = codes.toArray(new CharSequence[size]);

            mListPreference.setEntries(entries);
            mListPreference.setEntryValues(entryValues);
            mDefaultCountryCode = codes.get(foundedPosition);
        }
    }
}

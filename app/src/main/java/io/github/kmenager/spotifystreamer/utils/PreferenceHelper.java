package io.github.kmenager.spotifystreamer.utils;

import android.app.Notification;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.neovisionaries.i18n.CountryCode;

import java.util.Locale;

public class PreferenceHelper {

    public static String getCountryPreference(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString("country", getDefaultCountryCode());
    }

    /**
     * Get the current default country code
     */
    public static String getDefaultCountryCode() {
        return CountryCode.getByLocale(Locale.getDefault()).getAlpha2();
    }

    public static int getNotificationVisibility(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String value = preferences.getString("notification_visibilty", "0");
        switch (value) {
            case "PUBLIC":
                return Notification.VISIBILITY_PUBLIC;
            case "PRIVATE":
                return Notification.VISIBILITY_PRIVATE;
            case "SECRET":
                return Notification.VISIBILITY_SECRET;
        }
        return Notification.VISIBILITY_PRIVATE;
    }

}
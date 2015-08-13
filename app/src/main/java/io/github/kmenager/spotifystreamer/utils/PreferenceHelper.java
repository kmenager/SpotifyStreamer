package io.github.kmenager.spotifystreamer.utils;

import android.app.Notification;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.neovisionaries.i18n.CountryCode;

import java.util.Locale;

import io.github.kmenager.spotifystreamer.R;

public class PreferenceHelper {

    /**
     * Get the current preference country code
     */
    public static String getCountryPreference(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(context.getString(R.string.pref_country_key),
                getDefaultCountryCode());
    }

    /**
     * Get the current default country code
     */
    public static String getDefaultCountryCode() {
        return CountryCode.getByLocale(Locale.getDefault()).getAlpha2();
    }

    /**
     * Get the preference notification
     */
    public static int getNotificationVisibility(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String value = preferences.getString(context.getString(R.string.pref_notifications_key),
                context.getString(R.string.pref_notifications_public_values));
        if (context.getString(R.string.pref_notifications_public_values).equals(value)) {
            return Notification.VISIBILITY_PUBLIC;
        } else if (context.getString(R.string.pref_notifications_private_values).equals(value)) {
            return Notification.VISIBILITY_PRIVATE;
        } else if (context.getString(R.string.pref_notifications_secret_values).equals(value)) {
            return Notification.VISIBILITY_SECRET;
        } else {
            return Notification.VISIBILITY_PRIVATE;
        }
    }
}
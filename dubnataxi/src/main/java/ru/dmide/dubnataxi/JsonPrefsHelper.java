package ru.dmide.dubnataxi;

import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import ru.dmide.dubnataxi.ModelFragment;

/**
 * Created by dmide on 10/03/14.
 */
public class JsonPrefsHelper {
    private final static String JSON_PREFIX = "json";

    public static void saveJSONArray(SharedPreferences preferences, String key, JSONArray array) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(JSON_PREFIX + key, array.toString());
        editor.apply();
    }

    public static JSONArray loadJSONArray(SharedPreferences preferences, String key) {
        try {
            return new JSONArray(preferences.getString(JSON_PREFIX + key, "[]"));
        } catch (JSONException e) {
            Log.e(ModelFragment.class.getSimpleName(), "error creating JSON", e);
            return new JSONArray();
        }
    }

    public static Set<String> jsonToSet(JSONArray jsonArray) {
        if (jsonArray != null) {
            Set<String> listdata = new HashSet<String>();
            try {
                for (int i = 0; i < jsonArray.length(); i++) {
                    listdata.add((String) jsonArray.get(i));
                }
            } catch (JSONException e) {
                Log.e(ModelFragment.class.getSimpleName(), "error parsing JSON", e);
            }
            return listdata;
        }
        return Collections.emptySet();
    }

    public static void removeJsonFromPrefs(SharedPreferences preferences, String key) {
        if (preferences.contains(JSON_PREFIX + key)) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.remove(JSON_PREFIX + key);
            editor.commit();
        }
    }
}

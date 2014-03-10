package ru.dmide.dubnataxi;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;

/**
 * Created by dmide on 19/01/14.
 */
public class ContentLoadTask extends AsyncTask<Void, Void, Void> {
    private static final String CONTENT_URL = "http://dubnataxi.esy.es/";
    private static final String CONTENT = "content";
    private final boolean useCache;
    private final LinkedHashMap<String, ArrayList<String>> taxiNumbersTree = new LinkedHashMap<String, ArrayList<String>>();
    private final StringBuilder page = new StringBuilder();
    private final SharedPreferences preferences;
    private Exception e;
    private MainActivity activity;
    private PullToRefreshLayout pullToRefreshLayout;

    public ContentLoadTask(MainActivity activity, PullToRefreshLayout pullToRefreshLayout, boolean useCache) {
        this.activity = activity;
        this.pullToRefreshLayout = pullToRefreshLayout;
        this.useCache = useCache;
        preferences = activity.getSharedPreferences();
    }

    @Override
    protected Void doInBackground(Void... params) {
        if (useCache) {
            String pageStr = preferences.getString(CONTENT, "");
            if (pageStr.length() != 0) {
                page.append(pageStr);
                return (null);
            }
        }
        try {
            WebHelper.loadContent(new URL(CONTENT_URL), new NumbersParser(), "");
        } catch (Exception e) {
            this.e = e;
            Log.e(getClass().getSimpleName(),
                    "Exception retrieving page content", e);
        }
        return (null);
    }

    @Override
    public void onPostExecute(Void params) {
        if (e == null) {
            String pageStr = page.toString();
            preferences.edit().putString(CONTENT, pageStr).apply();
            try {
                JSONObject jsonObject = new JSONObject(pageStr);
                String name;
                JSONArray serviceList = (JSONArray) jsonObject.get("objects");
                for (int i = 0; i < serviceList.length(); i++) {
                    JSONObject service = (JSONObject) serviceList.get(i);
                    name = (String) service.get("name");
                    JSONArray numbers = (JSONArray) service.get("numbers");
                    ArrayList<String> numbersList = new ArrayList<String>();
                    for (int j = 0; j < numbers.length(); j++) {
                        numbersList.add((String) numbers.get(j));
                    }
                    taxiNumbersTree.put(name, numbersList);
                }
                activity.processContent(taxiNumbersTree);
            } catch (JSONException e1) {
                Toast.makeText(activity, activity.getString(R.string.problem),
                        Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(activity, activity.getString(R.string.problem),
                    Toast.LENGTH_LONG).show();
        }
        pullToRefreshLayout.setRefreshComplete();
    }

    private class NumbersParser implements WebHelper.Parser {
        @Override
        public void parse(String line) {
            page.append(line);
        }
    }
}
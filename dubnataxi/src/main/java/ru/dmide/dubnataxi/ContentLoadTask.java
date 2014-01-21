package ru.dmide.dubnataxi;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;
import com.json.parsers.JSONParser;

import java.net.URL;
import java.util.*;

/**
 * Created by dmide on 19/01/14.
 */
public class ContentLoadTask extends AsyncTask<Void, Void, Void> {
    private static final String CONTENT_URL = "http://dubnataxi.esy.es/";
    private static final String CONTENT = "content";


    private Exception e;
    private MainActivity activity;
    private final LinkedHashMap<String, ArrayList<String>> taxiNumbersTree = new LinkedHashMap<String, ArrayList<String>>();
    private final StringBuilder page = new StringBuilder();
    private final SharedPreferences preferences;

    public ContentLoadTask(MainActivity activity) {
        this.activity = activity;
        preferences = PreferenceManager.getDefaultSharedPreferences(activity);
    }

    @Override
    protected Void doInBackground(Void... params) {
        String pageStr = preferences.getString(CONTENT,"");
        if (pageStr.length() != 0){
            page.append(pageStr);
            return (null);
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
            JSONParser parser = new JSONParser();
            String content = pageStr;
            Map jsonData = parser.parseJson(content);
            String name;
            ArrayList<String> numbers;
            ArrayList<HashMap> serviceList = (ArrayList<HashMap>) jsonData.get("objects");
            for (HashMap service : serviceList) {
                name = (String) service.get("name");
                numbers = (ArrayList) service.get("numbers");
                taxiNumbersTree.put(name, numbers);
            }
            activity.drawContent(taxiNumbersTree);
        } else {
            Toast.makeText(activity, activity.getString(R.string.problem),
                    Toast.LENGTH_LONG).show();
        }
    }

    private class NumbersParser implements WebHelper.Parser {
        @Override
        public void parse(String line) {
            page.append(line);
        }
    }
}
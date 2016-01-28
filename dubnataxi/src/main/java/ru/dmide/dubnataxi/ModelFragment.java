package ru.dmide.dubnataxi;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by drevis on 19.02.14.
 */
public class ModelFragment extends android.support.v4.app.Fragment {
    private static final String CALLED_NUMS = "CALLED_NUMS";
    private static final String DELETED_NUMS = "DELETED_NUMS";
    private static final String DELETED_SERVICES = "DELETED_SERVICES";

    private final Set<String> calledPhones = new HashSet<>();
    private final Set<String> deletedPhoneNumbers = new HashSet<>();
    private final Set<String> deletedServices = new HashSet<>();
    private final Map<String, Set<String>> phonesMap = new HashMap<>();
    private final Map<String, List<String>> serviceToPhonesMap = new LinkedHashMap<>();
    private final Set<DataListener> dataListeners = new HashSet<>();

    private String lastSelectedService = "";
    private String lastCalledNumber = "";
    private SharedPreferences sharedPreferences;
    private boolean isLoaded;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MainActivity mainActivity = (MainActivity) getActivity();
        sharedPreferences = mainActivity.getSharedPrefs();

        phonesMap.put(CALLED_NUMS, calledPhones);
        phonesMap.put(DELETED_NUMS, deletedPhoneNumbers);
        phonesMap.put(DELETED_SERVICES, deletedServices);

        loadUserActions();
        loadContent(true, false);
    }

    public void loadContent(boolean useCache, boolean clear) {
        isLoaded = false;
        if (clear) {
            restoreDeletedValues();
        }
        new ContentLoadTask(useCache).execute();
    }

    public void subscribe(DataListener dataListener) {
        dataListeners.add(dataListener);
        if (isLoaded) {
            dataListener.onDataSetChanged();
        }
    }

    public void unsubscribe(DataListener dataListener) {
        dataListeners.remove(dataListener);
    }

    public List<String> getPhonesForService(String serviceId) {
        ArrayList<String> phones = new ArrayList<>(serviceToPhonesMap.get(serviceId));
        phones.removeAll(deletedPhoneNumbers);
        return phones;
    }

    public List<String> getServices() {
        List<String> services = new ArrayList<>(serviceToPhonesMap.keySet());
        services.removeAll(deletedServices);
        return services;
    }

    public void onPhoneNumberCalled(String number) {
        lastCalledNumber = number;
        calledPhones.add(number);
    }

    public String getLastCalledNumber(){
        return lastCalledNumber;
    }

    public boolean isPhoneNumberCalled(String number) {
        return calledPhones.contains(number);
    }

    public void deletePhoneNumber(String phone) {
        deletedPhoneNumbers.add(phone);
    }

    public void restorePhoneNumber(String number) {
        deletedPhoneNumbers.remove(number);
    }

    public void deleteService(String service) {
        deletedServices.add(service);
        notifyDataSetChanged();
    }

    public void restoreDeletedValues() {
        deletedPhoneNumbers.clear();
        deletedServices.clear();
    }

    public void saveUserActions() {
        save(DELETED_SERVICES);
        save(DELETED_NUMS);
        save(CALLED_NUMS);
    }

    public void setLastSelectedService(String service){
        lastSelectedService = service;
    }

    public String getLastSelectedService(){
        return lastSelectedService;
    }

    private void loadUserActions() {
        load(DELETED_SERVICES);
        load(DELETED_NUMS);
        load(CALLED_NUMS);
    }

    private void save(String identifier) {
        Set<String> container = phonesMap.get(identifier);
        JsonPrefsHelper.saveJSONArray(sharedPreferences, identifier, new JSONArray(container));
    }

    private void load(String identifier) {
        Set<String> container = phonesMap.get(identifier);
        JSONArray jsonArray = JsonPrefsHelper.loadJSONArray(sharedPreferences, identifier);
        container.addAll(JsonPrefsHelper.jsonToSet(jsonArray));
    }

    private void notifyDataSetChanged() {
        for (DataListener listener : dataListeners) {
            listener.onDataSetChanged();
        }
    }

    private void notifyDataSetLoading() {
        for (DataListener listener : dataListeners) {
            listener.onDataSetLoading();
        }
    }

    public interface DataListener {
        void onDataSetChanged();
        void onDataSetLoading();
    }

    private class ContentLoadTask extends AsyncTask<Void, Void, Void> {
        private static final String CONTENT_URL = "http://dubnataxi.esy.es/";
        private static final String CONTENT = "content";
        private final boolean useCache;
        private final StringBuilder page = new StringBuilder();
        private Exception e;

        public ContentLoadTask(boolean useCache) {
            this.useCache = useCache;
            notifyDataSetLoading();
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (useCache) {
                String pageStr = sharedPreferences.getString(CONTENT, "");
                if (pageStr.length() != 0) {
                    page.append(pageStr);
                    return null;
                }
            }
            try {
                WebHelper.loadContent(new URL(CONTENT_URL), new PhonesParser(), "");
            } catch (Exception e) {
                this.e = e;
                Log.e(getClass().getSimpleName(),
                        "Exception retrieving page content", e);
            }
            return null;
        }

        @Override
        public void onPostExecute(Void params) {
            FragmentActivity activity = getActivity();
            if (e == null) {
                String pageStr = page.toString();
                sharedPreferences.edit().putString(CONTENT, pageStr).apply();
                try {
                    JSONObject jsonObject = new JSONObject(pageStr);
                    String name;
                    JSONArray serviceList = (JSONArray) jsonObject.get("objects");
                    serviceToPhonesMap.clear();
                    for (int i = 0; i < serviceList.length(); i++) {
                        JSONObject service = (JSONObject) serviceList.get(i);
                        name = (String) service.get("name");
                        JSONArray phones = (JSONArray) service.get("numbers");
                        List<String> phonesList = new ArrayList<>();
                        for (int j = 0; j < phones.length(); j++) {
                            phonesList.add((String) phones.get(j));
                        }
                        serviceToPhonesMap.put(name, phonesList);
                    }
                } catch (JSONException e1) {
                    showProblemToast(activity);
                }
            } else {
                showProblemToast(activity);
            }
            isLoaded = true;
            notifyDataSetChanged();
        }

        private void showProblemToast(FragmentActivity activity) {
            if (activity != null) {
                Toast.makeText(activity, activity.getString(R.string.problem),
                        Toast.LENGTH_LONG).show();
            }
        }

        private class PhonesParser implements WebHelper.Parser {
            @Override
            public void parse(String line) {
                page.append(line);
            }
        }
    }
}

package ru.dmide.dubnataxi;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by drevis on 19.02.14.
 */
public class ModelFragment extends android.support.v4.app.Fragment {
    public static final String CALLED_NUMS = "CALLED_NUMS";
    public static final String DELETED_NUMS = "DELETED_NUMS";
    public static final String DELETED_SERVICES = "DELETED_SERVICES";

    Set<String> calledNumbers = new HashSet<String>();
    Set<String> deletedNumbers = new HashSet<String>();
    Set<String> deletedServices = new HashSet<String>();
    String currentService;

    private Map<String, Set<String>> numbersMap = new HashMap<String, Set<String>>();
    private HashMap<String, ArrayList<String>> taxiNumbersTree;
    private ServicesAdapter servicesAdapter;
    private PhonesAdapter phonesAdapter;
    private MainActivity mainActivity;
    private SharedPreferences sharedPreferences;

    public void init(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        sharedPreferences = mainActivity.getSharedPreferences();

        numbersMap.put(CALLED_NUMS, calledNumbers);
        numbersMap.put(DELETED_NUMS, deletedNumbers);
        numbersMap.put(DELETED_SERVICES, deletedServices);

        servicesAdapter = new ServicesAdapter(mainActivity, this);
        phonesAdapter = new PhonesAdapter(mainActivity, this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            loadSavedActions();
        }
    }

    public void initContent(HashMap<String, ArrayList<String>> taxiNumbersTree) {
        this.taxiNumbersTree = taxiNumbersTree;
        initServicesAdapter();
    }

    public boolean initNumbers(int pos) {
        ArrayList<String> servicesList = new ArrayList<String>(taxiNumbersTree.keySet());
        currentService = servicesList.get(pos);
        ArrayList<String> phones = taxiNumbersTree.get(currentService);
        phones.removeAll(deletedNumbers);
        if (phones.isEmpty()) {
            return false;
        }
        phonesAdapter.init(phones);
        return true;
    }

    public void initServicesAdapter() {
        Set<String> services = taxiNumbersTree.keySet();
        services.removeAll(deletedServices);
        servicesAdapter.init(new ArrayList<String>(services));
    }

    public Set<String> getCalledNumbers() {
        return calledNumbers;
    }

    public MainActivity getMainActivity() {
        return mainActivity;
    }

    ServicesAdapter getServicesAdapter() {
        return servicesAdapter;
    }

    PhonesAdapter getPhonesAdapter() {
        return phonesAdapter;
    }

    private void loadSavedActions() {
        new LoadJsonTask(CALLED_NUMS).execute();
        new LoadJsonTask(DELETED_NUMS).execute();
        new LoadJsonTask(DELETED_SERVICES).execute();
    }


    class LoadJsonTask extends AsyncTask<Void, Void, Void> {
        private String identifier;

        LoadJsonTask(String identifier) {
            this.identifier = identifier;
        }

        @Override
        protected Void doInBackground(Void... params) {
            Set<String> container = numbersMap.get(identifier);
            JSONArray jsonArray = JsonPrefsHelper.loadJSONArray(sharedPreferences, identifier);
            container = JsonPrefsHelper.jsonToSet(jsonArray);
            return null;
        }
    }

    class SaveJsonTask extends AsyncTask<Void, Void, Void> {
        private String identifier;

        SaveJsonTask(String identifier) {
            this.identifier = identifier;
        }

        @Override
        protected Void doInBackground(Void... params) {
            Set<String> container = numbersMap.get(identifier);
            JsonPrefsHelper.saveJSONArray(sharedPreferences, identifier, new JSONArray(container));
            return null;
        }
    }
}

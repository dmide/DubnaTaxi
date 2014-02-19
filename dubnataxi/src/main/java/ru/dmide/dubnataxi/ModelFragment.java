package ru.dmide.dubnataxi;

import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by drevis on 19.02.14.
 */
public class ModelFragment extends android.support.v4.app.Fragment {
    private static final String CALLED_NUMS = "CALLED_NUMS";
    private static final String DELETED_NUMS = "DELETED_NUMS";
    private static final String DELETED_SERVICES = "DELETED_SERVICES";

    Set<String> calledNumbers = new HashSet<String>();
    Set<String> deletedNumbers = new HashSet<String>();
    Set<String> deletedServices = new HashSet<String>();
    String currentService;

    private HashMap<String, ArrayList<String>> taxiNumbersTree;
    private ServicesAdapter servicesAdapter;
    private PhonesAdapter phonesAdapter;
    private MainActivity mainActivity;

    public void init(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
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

    public MainActivity getMainActivity(){
        return mainActivity;
    }

    ServicesAdapter getServicesAdapter() {
        return servicesAdapter;
    }

    PhonesAdapter getPhonesAdapter() {
        return phonesAdapter;
    }

    private void loadSavedActions() {
        new GetCalledNumbersTask().execute();
        new GetDeletedNumbersTask().execute();
        new GetDeletedServicesTask().execute();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    class GetCalledNumbersTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mainActivity);
            //creating a copy of Set due to a bug in API: http://stackoverflow.com/a/17470210/2093236
            calledNumbers = new HashSet<String>(preferences.getStringSet(CALLED_NUMS, new HashSet<String>()));
            return null;
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    class SaveCalledNumbersTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mainActivity);
            preferences.edit().putStringSet(CALLED_NUMS, calledNumbers).commit();
            return null;
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    class GetDeletedNumbersTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mainActivity);
            //creating a copy of Set due to a bug in API: http://stackoverflow.com/a/17470210/2093236
            deletedNumbers = new HashSet<String>(preferences.getStringSet(DELETED_NUMS, new HashSet<String>()));
            return null;
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    class SaveDeletedNumbersTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mainActivity);
            preferences.edit().putStringSet(DELETED_NUMS, deletedNumbers).commit();
            return null;
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    class GetDeletedServicesTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mainActivity);
            //creating a copy of Set due to a bug in API: http://stackoverflow.com/a/17470210/2093236
            deletedServices = new HashSet<String>(preferences.getStringSet(DELETED_SERVICES, new HashSet<String>()));
            return null;
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    class SaveDeletedServicesTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mainActivity);
            preferences.edit().putStringSet(DELETED_SERVICES, deletedServices).commit();
            return null;
        }
    }

}

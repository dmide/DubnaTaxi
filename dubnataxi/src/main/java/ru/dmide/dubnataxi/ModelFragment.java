package ru.dmide.dubnataxi;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.dmide.dubnataxi.web.NetworkClient;
import ru.dmide.dubnataxi.web.Service;
import ru.dmide.dubnataxi.web.ServicesListResponse;

/**
 * Created by drevis on 19.02.14.
 */
public class ModelFragment extends android.support.v4.app.Fragment {
    private static final String TO_RATE_OR_NOT_TO_RATE = "TO_RATE_OR_NOT_TO_RATE";
    private static final String CALLED_NUMS = "CALLED_NUMS";
    private static final String DELETED_NUMS = "DELETED_NUMS";
    private static final String DELETED_SERVICES = "DELETED_SERVICES";

    private final Set<String> calledPhones = new HashSet<>();
    private final Set<String> deletedPhoneNumbers = new HashSet<>();
    private final Set<String> deletedServices = new HashSet<>();
    private final Map<String, Set<String>> phonesMap = new HashMap<>();
    private final Map<String, List<String>> serviceToPhonesMap = new LinkedHashMap<>();
    private final Set<DataListener> dataListeners = new HashSet<>();

    private String contentUrl;
    private SharedPreferences preferences;
    private List<String> selectedServices = new ArrayList<>();
    private String lastCalledNumber = "";
    private boolean isLoaded;
    private NetworkClient networkClient;
    private Context applicationContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applicationContext = getContext().getApplicationContext();
        contentUrl = getString(R.string.content_url);
        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        phonesMap.put(CALLED_NUMS, calledPhones);
        phonesMap.put(DELETED_NUMS, deletedPhoneNumbers);
        phonesMap.put(DELETED_SERVICES, deletedServices);

        loadUserActions();
        loadContent(false);
    }

    public SharedPreferences getSharedPrefs() {
        return preferences;
    }

    public void loadContent(boolean clear) {
        isLoaded = false;
        if (clear) {
            restoreDeletedValues();
        }
        loadContent();
    }

    public Context getApplicationContext() {
        return applicationContext;
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
        preferences.edit().putLong(number, System.currentTimeMillis()).apply();
    }

    public String getLastCalledNumber() {
        return lastCalledNumber;
    }

    public boolean isPhoneNumberCalled(String number) {
        return calledPhones.contains(number);
    }

    public long getPhoneNumberCalledTime(String number) {
        return preferences.getLong(number, 0);
    }

    public void deletePhoneNumber(String phone) {
        deletedPhoneNumbers.add(phone);
    }

    public void restorePhoneNumber(String number) {
        deletedPhoneNumbers.remove(number);
    }

    public void deleteService(String service) {
        removeAllSelections();
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

    public void addSelectedService(String service) {
        selectedServices.add(service);
    }

    public void removeSelectedService(String service) {
        selectedServices.remove(service);
    }

    public void removeAllSelections() {
        selectedServices.clear();
    }

    public boolean isServiceSelected(String id) {
        return selectedServices.contains(id);
    }

    public boolean isShouldShowRateDialog() {
        return preferences.getBoolean(ModelFragment.TO_RATE_OR_NOT_TO_RATE, true);
    }

    public void setShouldShowRateDialog(boolean value) {
        preferences.edit().putBoolean(ModelFragment.TO_RATE_OR_NOT_TO_RATE, value).apply();
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    private void loadUserActions() {
        load(DELETED_SERVICES);
        load(DELETED_NUMS);
        load(CALLED_NUMS);
    }

    private void save(String identifier) {
        Set<String> container = phonesMap.get(identifier);
        JsonPrefsHelper.saveJSONArray(preferences, identifier, new JSONArray(container));
    }

    private void load(String identifier) {
        Set<String> container = phonesMap.get(identifier);
        JSONArray jsonArray = JsonPrefsHelper.loadJSONArray(preferences, identifier);
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

    private void loadContent() {
        notifyDataSetLoading();
        if (networkClient == null) {
            networkClient = new NetworkClient(this, contentUrl);
        }
        Call<ServicesListResponse> listCall = networkClient.getApi().getServices();
        listCall.enqueue(new Callback<ServicesListResponse>() {
            @Override
            public void onResponse(Response<ServicesListResponse> response) {
                serviceToPhonesMap.clear();
                for (Service service : response.body().getServiceList()) {
                    serviceToPhonesMap.put(service.getName(), service.getNumbers());
                }
                isLoaded = true;
                notifyDataSetChanged();
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e(ModelFragment.class.getSimpleName(), "Failed to load the content.", t);
                FragmentActivity activity = getActivity();
                if (activity != null) {
                    showProblemSnack(activity.findViewById(android.R.id.content));
                }
            }
        });
    }

    private void showProblemSnack(View rootview) {
        if (rootview != null) {
            ViewHelper.makeStyledSnack(rootview, rootview.getContext().getString(R.string.problem),
                    Snackbar.LENGTH_LONG).show();
        }
    }
}

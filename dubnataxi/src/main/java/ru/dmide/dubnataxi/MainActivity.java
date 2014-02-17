package ru.dmide.dubnataxi;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.haarman.listviewanimations.itemmanipulation.OnDismissCallback;
import com.haarman.listviewanimations.itemmanipulation.SwipeDismissAdapter;
import com.haarman.listviewanimations.swinginadapters.AnimationAdapter;
import com.haarman.listviewanimations.swinginadapters.prepared.ScaleInAnimationAdapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

public class MainActivity extends ActionBarActivity {

    private static final String CALLED_NUMS = "CALLED_NUMS";
    private static final String DELETED_NUMS = "DELETED_NUMS";
    private static final String DELETED_SERVICES = "DELETED_SERVICES";
    private final ArrayList<ArrayList<String>> numberLists = new ArrayList<ArrayList<String>>();
    private ArrayList<String> servicesList = new ArrayList<String>();
    private ListView servicesListView;
    private ServicesAdapter servicesAdapter;
    private PhonesAdapter phonesAdapter;
    private PullToRefreshLayout pullToRefreshLayout;
    private Set<String> calledNumbers = new HashSet<String>();
    private Set<String> deletedNumbers = new HashSet<String>();
    private Set<String> deletedServices = new HashSet<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        servicesListView = (ListView) findViewById(R.id.list);
        servicesListView.setDivider(null);
        servicesAdapter = new ServicesAdapter(this);
        phonesAdapter = new PhonesAdapter(this);

        pullToRefreshLayout = (PullToRefreshLayout) findViewById(R.id.ptr_layout);
        ActionBarPullToRefresh.from(this)
                .allChildrenArePullable()
                .listener(new OnRefreshListener() {
                    @Override
                    public void onRefreshStarted(View view) {
                        updateContent(false);
                    }
                })
                .setup(pullToRefreshLayout);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            loadSavedActions();
        }
        updateContent(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return (super.onCreateOptionsMenu(menu));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected <T extends View> T viewById(int id) {
        return (T) findViewById(id);
    }

    protected <T extends View> T viewById(View parent, int id) {
        return (T) parent.findViewById(id);
    }

    public void processContent(LinkedHashMap<String, ArrayList<String>> taxiNumbersTree) {
        numberLists.clear();
        for (ArrayList<String> numbers : taxiNumbersTree.values()) {
            numberLists.add(numbers);
        }
        servicesList = new ArrayList<String>(taxiNumbersTree.keySet());
        servicesList.removeAll(deletedServices);
        servicesAdapter.init(servicesList);
        AnimationAdapter animAdapter = new ScaleInAnimationAdapter(servicesAdapter);
        animAdapter.setAbsListView(servicesListView);
        servicesListView.setAdapter(animAdapter);
        servicesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int groupPos, long id) {
                ArrayList<String> phones = numberLists.get(groupPos);
                phones.removeAll(deletedNumbers);
                phonesAdapter.init(phones);
                ListView phonesList = new ListView(MainActivity.this);
                final AlertDialog dialog = new AlertDialog.Builder(MainActivity.this).
                        setView(phonesList).
                        create();
                SwipeDismissAdapter dismissAdapter = new SwipeDismissAdapter(phonesAdapter, new OnDismissCallback() {
                    @Override
                    public void onDismiss(AbsListView absListView, int[] ints) {
                        String deletedPhone = (String) absListView.getItemAtPosition(ints[0]);
                        deletedNumbers.add(deletedPhone);
                        new SaveDeletedNumbersTask().execute();
                        ArrayList<String> phones = numberLists.get(groupPos);
                        phones.removeAll(deletedNumbers);
                        if (phones.isEmpty()) {
                            deletedServices.add(servicesList.get(groupPos));
                            new SaveDeletedServicesTask().execute();
                            servicesList.removeAll(deletedServices);
                            servicesAdapter.init(servicesList);
                            dialog.dismiss();
                        } else {
                            phonesAdapter.init(phones);
                        }
                    }
                });
                dismissAdapter.setAbsListView(phonesList);
                phonesList.setAdapter(dismissAdapter);
                setChildsListener(phonesList);
                dialog.show();
            }
        });
    }

    public Set<String> getCalledNumbers() {
        return calledNumbers;
    }

    private void setChildsListener(ListView list) {
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView numberTV = viewById(view, R.id.phone_number_tv);
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                String number = numberTV.getText().toString();
                callIntent.setData(Uri.parse("tel:" + number));
                calledNumbers.add(number);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    new SaveCalledNumbersTask().execute();
                }
                startActivity(callIntent);
                servicesAdapter.notifyDataSetChanged();
            }
        });
    }

    private void updateContent(boolean useCache) {
        new ContentLoadTask(this, pullToRefreshLayout, useCache).execute();
        if (!useCache){
            clearDeletedValues();
        }
    }

    private void loadSavedActions() {
        new GetCalledNumbersTask().execute();
        new GetDeletedNumbersTask().execute();
        new GetDeletedServicesTask().execute();
    }

    private void clearDeletedValues() {
        deletedNumbers.clear();
        deletedServices.clear();
        new SaveDeletedNumbersTask().execute();
        new SaveDeletedServicesTask().execute();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private class GetCalledNumbersTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
            //creating a copy of Set due to a bug in API: http://stackoverflow.com/a/17470210/2093236
            calledNumbers = new HashSet<String>(preferences.getStringSet(CALLED_NUMS, new HashSet<String>()));
            return null;
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private class SaveCalledNumbersTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
            preferences.edit().putStringSet(CALLED_NUMS, calledNumbers).commit();
            return null;
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private class GetDeletedNumbersTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
            //creating a copy of Set due to a bug in API: http://stackoverflow.com/a/17470210/2093236
            deletedNumbers = new HashSet<String>(preferences.getStringSet(DELETED_NUMS, new HashSet<String>()));
            return null;
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private class SaveDeletedNumbersTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
            preferences.edit().putStringSet(DELETED_NUMS, deletedNumbers).commit();
            return null;
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private class GetDeletedServicesTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
            //creating a copy of Set due to a bug in API: http://stackoverflow.com/a/17470210/2093236
            deletedServices = new HashSet<String>(preferences.getStringSet(DELETED_SERVICES, new HashSet<String>()));
            return null;
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private class SaveDeletedServicesTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
            preferences.edit().putStringSet(DELETED_SERVICES, deletedServices).commit();
            return null;
        }
    }
}

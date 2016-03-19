package ru.dmide.dubnataxi.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import com.nhaarman.listviewanimations.appearance.AnimationAdapter;
import com.nhaarman.listviewanimations.appearance.simple.ScaleInAnimationAdapter;

import butterknife.Bind;
import butterknife.ButterKnife;
import ru.dmide.dubnataxi.Controller;
import ru.dmide.dubnataxi.ModelFragment;
import ru.dmide.dubnataxi.R;
import ru.dmide.dubnataxi.adapters.ServicesAdapter;

public class MainActivity extends BaseActivity implements ModelFragment.DataListener, ActivityCompat.OnRequestPermissionsResultCallback {
    public static final String MODEL = "MODEL";
    private static final int PHONES_ADAPTERS_REFRESH_RATE = 5000;

    private ModelFragment model;
    private Controller controller;

    @Bind(R.id.services_list)
    ListView servicesListView;
    @Bind(R.id.swipe_container)
    SwipeRefreshLayout swipeRefreshLayout;

    private ServicesAdapter servicesAdapter;
    private Handler handler;
    private final Runnable phonesUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            controller.updatePhonesAdapters();
            handler.postDelayed(this, PHONES_ADAPTERS_REFRESH_RATE);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        handler = new Handler();

        swipeRefreshLayout.setOnRefreshListener(() -> model.loadContent(false));
        swipeRefreshLayout.setColorSchemeResources(
                R.color.orange,
                R.color.dark_grey);

        model = initModel();
        model.removeAllSelections(); // clear selection after config. change
        controller = new Controller(model);
        initServicesListView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        model.subscribe(this);
        controller.updatePhonesAdapters();
        handler.postDelayed(phonesUpdateRunnable, PHONES_ADAPTERS_REFRESH_RATE);
    }

    @Override
    protected void onPause() {
        model.saveUserActions();
        model.unsubscribe(this);
        handler.removeCallbacks(phonesUpdateRunnable);
        super.onPause();
    }

    @Override
    public void onDataSetChanged() {
        swipeRefreshLayout.setRefreshing(false);
        servicesAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDataSetLoading() {
        swipeRefreshLayout.setRefreshing(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_clear:
                model.loadContent(true);
                return true;
            case R.id.info:
                controller.showInfo();
                return true;
            case R.id.action_where:
                Uri uri = Uri.parse("geo:");
                Intent w = new Intent(android.content.Intent.ACTION_VIEW, uri);
                startActivity(w);
                Toast.makeText(this, getString(R.string.face), Toast.LENGTH_SHORT).show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (model.isShouldShowRateDialog()) {
            controller.showRateDialog();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == Controller.PERMISSION_REQUEST_CODE) {
            //retry to make a call if permission is granted
            //or perform dial otherwise
            controller.onPhoneNumberClick(model.getLastCalledNumber());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == InfoActivity.REQUEST_CODE && resultCode == InfoActivity.RESULT_CODE_RATED) {
            model.setShouldShowRateDialog(false);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private ModelFragment initModel() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        ModelFragment model = (ModelFragment) fragmentManager.findFragmentByTag(MODEL);
        if (model == null) {
            model = new ModelFragment();
            model.setRetainInstance(true);
            fragmentManager.beginTransaction().add(model, MODEL).commit();
            swipeRefreshLayout.setRefreshing(true);
        }
        return model;
    }

    private void initServicesListView() {
        servicesListView.setDivider(null);

        servicesAdapter = new ServicesAdapter(this, model);
        AnimationAdapter animAdapter = new ScaleInAnimationAdapter(servicesAdapter, .8f);
        animAdapter.setAbsListView(servicesListView);
        animAdapter.getViewAnimator().setAnimationDelayMillis(60);
        servicesListView.setAdapter(animAdapter);

        servicesListView.setOnItemClickListener((parent, view, groupPos, id) -> {
            String serviceId = servicesAdapter.getItem(groupPos);
            controller.onServiceClick(view, serviceId);
        });
    }
}

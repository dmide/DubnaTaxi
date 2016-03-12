package ru.dmide.dubnataxi;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ListView;

import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.SwipeDismissAdapter;
import com.nineoldandroids.animation.Animator;

import ru.dmide.dubnataxi.activity.InfoActivity;
import ru.dmide.dubnataxi.adapters.PhonesAdapter;

import static ru.dmide.dubnataxi.activity.BaseActivity.viewById;

/**
 * Created by drevis on 19.02.14.
 */
public class Controller {
    private static final String SHOULD_SHOW_PHONES_DELETION_TIP = "SHOULD_SHOW_PHONES_DELETION_TIP";

    private final ModelFragment model;

    public Controller(ModelFragment model) {
        this.model = model;
    }

    public void onServiceClick(View serviceView, String serviceId) {
        if (model.isServiceSelected(serviceId)) {
            hidePhones(serviceView);
            model.removeSelectedService(serviceId);
        } else {
            showPhones(serviceView, serviceId);
            model.addSelectedService(serviceId);
        }
        Context c = serviceView.getContext();
        if (model.getSharedPrefs().getBoolean(SHOULD_SHOW_PHONES_DELETION_TIP, true)) {
            ViewHelper.makeStyledSnack(serviceView, R.string.phone_deletion_desc, Snackbar.LENGTH_LONG).show();
            PreferenceManager.getDefaultSharedPreferences(c).edit()
                    .putBoolean(SHOULD_SHOW_PHONES_DELETION_TIP, false)
                    .commit();
        }
    }

    public void onPhoneNumberClick(String number) {
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + number));
        model.onPhoneNumberCalled(number);
        model.getActivity().startActivity(callIntent);
    }

    public void showRateDialog() {
        FragmentActivity activity = model.getActivity();
        View rateDialog = View.inflate(activity, R.layout.rate_dialog, null);
        CheckBox checkBox = viewById(rateDialog, R.id.checkbox);
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) ->
                model.setShouldShowRateDialog(!isChecked));
        new AlertDialog.Builder(activity)
                .setView(rateDialog)
                .setPositiveButton(activity.getString(R.string.yes), (dialog, which) -> {
                    showInfo();
                })
                .setNegativeButton(activity.getString(R.string.no), (dialog, which) -> {
                    activity.finish();
                })
                .setInverseBackgroundForced(true)
                .show();
    }

    public void showInfo() {
        FragmentActivity activity = model.getActivity();
        Intent intent = new Intent(activity, InfoActivity.class);
        activity.startActivityForResult(intent, InfoActivity.REQUEST_CODE);
    }

    private void hidePhones(View serviceView) {
        ListView phonesList = viewById(serviceView, R.id.phones_list);
        ViewHelper.collapse(phonesList, 200).addListener(new ViewHelper.AnimationEndListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                viewById(serviceView, R.id.separator).setVisibility(View.GONE);
            }
        });
    }

    private void showPhones(final View serviceView, String serviceId) {
        ListView phonesList = viewById(serviceView, R.id.phones_list);
        viewById(serviceView, R.id.separator).setVisibility(View.VISIBLE);

        PhonesAdapter phonesAdapter = new PhonesAdapter(model, serviceId);

        SwipeDismissAdapter dismissAdapter = new SwipeDismissAdapter(phonesAdapter, (viewGroup, ints) -> {
            String phoneNumber = phonesAdapter.getItem(ints[0]);
            model.deletePhoneNumber(phoneNumber);
            phonesAdapter.notifyDataSetChanged();

            ViewHelper.makeStyledSnack(serviceView, R.string.phone_deleted, Snackbar.LENGTH_LONG)
                    .setAction(R.string.cancel, v -> {
                        model.restorePhoneNumber(phoneNumber);
                        phonesAdapter.notifyDataSetChanged();
                    })
                    .show();
        });
        dismissAdapter.setAbsListView(phonesList);
        phonesList.setAdapter(dismissAdapter);

        phonesList.setOnItemClickListener((parent, view, position, id) -> {
            onPhoneNumberClick(phonesAdapter.getItem(position));
            phonesAdapter.notifyDataSetChanged();
        });

        int phonesCount = phonesAdapter.getCount();
        int height = ViewHelper.calcListViewHeight(serviceView.getContext(), phonesCount);
        ViewHelper.expand(phonesList, 200, height);
    }
}

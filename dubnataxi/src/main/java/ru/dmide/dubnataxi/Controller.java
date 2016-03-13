package ru.dmide.dubnataxi;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ListView;

import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.SwipeDismissAdapter;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ValueAnimator;

import ru.dmide.dubnataxi.activity.InfoActivity;
import ru.dmide.dubnataxi.adapters.PhonesAdapter;
import ru.dmide.dubnataxi.adapters.ServicesAdapter;

import static ru.dmide.dubnataxi.activity.BaseActivity.viewById;

/**
 * Created by drevis on 19.02.14.
 */
public class Controller {
    public static final int PERMISSION_REQUEST_CODE = 1;
    private static final String SHOULD_SHOW_PHONES_DELETION_TIP = "SHOULD_SHOW_PHONES_DELETION_TIP";
    private static final int PHONES_REVEAL_ANIMATION_DURATION = 250;
    private static final int PHONES_REMOVAL_ANIMATION_DURATION = 150;

    private final ModelFragment model;
    private volatile boolean phonesAnimationInProgress;

    public Controller(ModelFragment model) {
        this.model = model;
    }

    public void onServiceClick(View serviceView, String serviceId) {
        if (phonesAnimationInProgress) {
            return;
        }
        if (model.isServiceSelected(serviceId)) {
            hidePhones(serviceView);
            model.removeSelectedService(serviceId);
        } else {
            showPhones(serviceView, serviceId);
            model.addSelectedService(serviceId);
        }
        if (model.getSharedPrefs().getBoolean(SHOULD_SHOW_PHONES_DELETION_TIP, true)) {
            ViewHelper.makeStyledSnack(serviceView, R.string.phone_deletion_desc, Snackbar.LENGTH_LONG).show();
            PreferenceManager.getDefaultSharedPreferences(serviceView.getContext()).edit()
                    .putBoolean(SHOULD_SHOW_PHONES_DELETION_TIP, false)
                    .commit();
        }
    }

    public void onPhoneNumberClick(String number) {
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + number));
        model.onPhoneNumberCalled(number);

        FragmentActivity activity = model.getActivity();
        String callPhonePermission = Manifest.permission.CALL_PHONE;
        int permissionStatus = ActivityCompat.checkSelfPermission(activity, callPhonePermission);
        if (permissionStatus == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            activity.startActivity(callIntent);
        } else {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    Manifest.permission.CALL_PHONE)) {
                ActivityCompat.requestPermissions(activity, new String[]{callPhonePermission}, PERMISSION_REQUEST_CODE);
            } else {
                //if permission have been denied, fallback to dial
                Intent dialIntent = new Intent(Intent.ACTION_DIAL);
                dialIntent.setData(Uri.parse("tel:" + number));
                activity.startActivity(dialIntent);
            }
        }
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
        collapsePhonesList(serviceView);
        ViewCompat.animate(viewById(serviceView, R.id.arrow))
                .rotation(0)
                .setDuration(PHONES_REVEAL_ANIMATION_DURATION)
                .start();
    }

    private void showPhones(final View serviceView, String serviceId) {
        ListView phonesList = viewById(serviceView, R.id.phones_list);
        PhonesAdapter phonesAdapter = new PhonesAdapter(model, serviceId);
        preparePhonesList(serviceView, phonesList, phonesAdapter);

        expandPhonesList(serviceView, phonesAdapter);
        ViewCompat.animate(viewById(serviceView, R.id.arrow))
                .rotation(-270)
                .setDuration(PHONES_REVEAL_ANIMATION_DURATION)
                .start();
    }

    private void preparePhonesList(View serviceView, ListView phonesList, PhonesAdapter phonesAdapter) {
        String serviceId = phonesAdapter.getServiceId();
        SwipeDismissAdapter dismissAdapter = new SwipeDismissAdapter(phonesAdapter, (viewGroup, ints) -> {
            Context c = serviceView.getContext();
            String phoneNumber = phonesAdapter.getItem(ints[0]);
            model.deletePhoneNumber(phoneNumber);
            phonesAdapter.notifyDataSetChanged();

            float dividerHeight = c.getResources().getDimension(R.dimen.phones_list_divider_height);
            int initialHeight = ViewHelper.calcListViewHeight(c, phonesAdapter.getCount() + 1, dividerHeight);
            int newHeight = ViewHelper.calcListViewHeight(c, phonesAdapter.getCount(), dividerHeight);

            ViewHelper.makeStyledSnack(serviceView, R.string.phone_deleted, Snackbar.LENGTH_LONG)
                    .setAction(R.string.cancel, v -> {
                        model.restorePhoneNumber(phoneNumber);
                        phonesAdapter.notifyDataSetChanged();
                        if (phonesAdapter.getCount() == 1) { // need to reopen service
                            onServiceClick(serviceView, serviceId);
                            return;
                        }
                        ViewHelper.getResizeAnimator(phonesList, PHONES_REMOVAL_ANIMATION_DURATION, newHeight, initialHeight).start();
                    })
                    .setCallback(new Snackbar.Callback() {
                        @Override
                        public void onDismissed(Snackbar snackbar, int event) {
                            if (phonesAdapter.getCount() == 0 && event == DISMISS_EVENT_TIMEOUT) {
                                model.deleteService(serviceId);
                            }
                        }
                    })
                    .show();

            if (phonesAdapter.getCount() == 0) { // close service
                onServiceClick(serviceView, serviceId);
                return;
            }
            ViewHelper.getResizeAnimator(phonesList, PHONES_REMOVAL_ANIMATION_DURATION, initialHeight, newHeight).start();
        });
        dismissAdapter.setAbsListView(phonesList);
        phonesList.setAdapter(dismissAdapter);

        phonesList.setOnItemClickListener((parent, view, position, id) -> {
            onPhoneNumberClick(phonesAdapter.getItem(position));
            phonesAdapter.notifyDataSetChanged();
        });
    }

    private void expandPhonesList(View serviceView, PhonesAdapter phonesAdapter) {
        phonesAnimationInProgress = true;
        ServicesAdapter.ViewHolder holder = (ServicesAdapter.ViewHolder) serviceView.getTag();
        float dividerHeight = serviceView.getContext().getResources().getDimension(R.dimen.phones_list_divider_height);
        int targetHeight = ViewHelper.calcListViewHeight(serviceView.getContext(), phonesAdapter.getCount(), dividerHeight);

        ValueAnimator expandAnimator = ViewHelper.getResizeAnimator(holder.phonesList, PHONES_REVEAL_ANIMATION_DURATION, 0, targetHeight);
        expandAnimator.addUpdateListener(animation -> {
            if (holder.phonesList.getVisibility() != View.VISIBLE && targetHeight * animation.getAnimatedFraction() >= holder.shadowsHeight()) {
                ViewHelper.applyVisibility(new View[]{holder.phonesList, holder.shadowDown, holder.shadowUp}, View.VISIBLE);
            }
        });
        startAnimation(expandAnimator);
    }

    private void collapsePhonesList(View serviceView) {
        phonesAnimationInProgress = true;
        ServicesAdapter.ViewHolder holder = (ServicesAdapter.ViewHolder) serviceView.getTag();
        int initialHeight = holder.phonesList.getLayoutParams().height;

        ValueAnimator collapseAnimator = ViewHelper.getResizeAnimator(holder.phonesList, PHONES_REVEAL_ANIMATION_DURATION, initialHeight, 0);
        collapseAnimator.addUpdateListener(animation -> {
            if (initialHeight * (1f - animation.getAnimatedFraction()) <= holder.shadowsHeight() && holder.phonesList.getVisibility() == View.VISIBLE) {
                ViewHelper.applyVisibility(new View[]{holder.phonesList, holder.shadowDown, holder.shadowUp}, View.GONE);
            }
        });
        startAnimation(collapseAnimator);
    }


    private void startAnimation(ValueAnimator animator) {
        animator.addListener(new ViewHelper.AnimationEndListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                phonesAnimationInProgress = false;
            }
        });
        animator.start();
    }
}

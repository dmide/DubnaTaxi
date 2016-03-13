package ru.dmide.dubnataxi;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
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

import static ru.dmide.dubnataxi.activity.BaseActivity.viewById;

/**
 * Created by drevis on 19.02.14.
 */
public class Controller {
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

        expandPhonesList(serviceView, phonesList, phonesAdapter);
        ViewCompat.animate(viewById(serviceView, R.id.arrow))
                .rotation(-270)
                .setDuration(PHONES_REVEAL_ANIMATION_DURATION)
                .start();
    }

    private void preparePhonesList(View serviceView, ListView phonesList, PhonesAdapter phonesAdapter) {
        SwipeDismissAdapter dismissAdapter = new SwipeDismissAdapter(phonesAdapter, (viewGroup, ints) -> {
            Context c = serviceView.getContext();
            String phoneNumber = phonesAdapter.getItem(ints[0]);
            model.deletePhoneNumber(phoneNumber);
            phonesAdapter.notifyDataSetChanged();

            int initialHeight = ViewHelper.calcListViewHeight(c, phonesAdapter.getCount() + 1);
            int newHeight = ViewHelper.calcListViewHeight(c, phonesAdapter.getCount());
            ViewHelper.getResizeAnimator(phonesList, PHONES_REMOVAL_ANIMATION_DURATION, initialHeight, newHeight).start();

            ViewHelper.makeStyledSnack(serviceView, R.string.phone_deleted, Snackbar.LENGTH_LONG)
                    .setAction(R.string.cancel, v -> {
                        model.restorePhoneNumber(phoneNumber);
                        phonesAdapter.notifyDataSetChanged();
                        ViewHelper.getResizeAnimator(phonesList, PHONES_REMOVAL_ANIMATION_DURATION, newHeight, initialHeight).start();
                    })
                    .show();
        });
        dismissAdapter.setAbsListView(phonesList);
        phonesList.setAdapter(dismissAdapter);

        phonesList.setOnItemClickListener((parent, view, position, id) -> {
            onPhoneNumberClick(phonesAdapter.getItem(position));
            phonesAdapter.notifyDataSetChanged();
        });
    }

    private void expandPhonesList(View serviceView, ListView phonesList, PhonesAdapter phonesAdapter) {
        phonesAnimationInProgress = true;
        Context c = serviceView.getContext();
        int listHeight = ViewHelper.calcListViewHeight(c, phonesAdapter.getCount());
        int shadowHeight = (int) c.getResources().getDimension(R.dimen.shadow_height);
        View shadowDown = viewById(serviceView, R.id.shadow_down);
        View shadowUp = viewById(serviceView, R.id.shadow_up);

        ValueAnimator expandAnimator = ViewHelper.getResizeAnimator(phonesList, PHONES_REVEAL_ANIMATION_DURATION, 0, listHeight);
        phonesList.setVisibility(View.VISIBLE);
        expandAnimator.addUpdateListener(animation -> {
            if (shadowDown.getVisibility() != View.VISIBLE && listHeight * animation.getAnimatedFraction() >= shadowHeight) {
                shadowDown.setVisibility(View.VISIBLE);
                shadowUp.setVisibility(View.VISIBLE);
            }
        });
        expandAnimator.addListener(new ViewHelper.AnimationEndListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                phonesAnimationInProgress = false;
            }
        });
        expandAnimator.start();
    }

    private void collapsePhonesList(final View serviceView) {
        phonesAnimationInProgress = true;
        ListView phonesList = viewById(serviceView, R.id.phones_list);
        int initialHeight = phonesList.getLayoutParams().height;
        ValueAnimator collapseAnimator = ViewHelper.getResizeAnimator(phonesList, PHONES_REVEAL_ANIMATION_DURATION, initialHeight, 0);
        collapseAnimator.addListener(new ViewHelper.AnimationEndListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                viewById(serviceView, R.id.shadow_down).setVisibility(View.GONE);
                viewById(serviceView, R.id.shadow_up).setVisibility(View.GONE);
                phonesList.setVisibility(View.GONE);
                phonesAnimationInProgress = false;
            }
        });
        collapseAnimator.start();
    }
}

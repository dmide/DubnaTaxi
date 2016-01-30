package ru.dmide.dubnataxi;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;


import ru.dmide.dubnataxi.activity.InfoActivity;
import ru.dmide.dubnataxi.activity.MainActivity;
import ru.dmide.dubnataxi.dialogs.PhonesListDialog;

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

    public void onServiceClick(String serviceId, final MainActivity activity) {
        PhonesListDialog phonesListDialog = new PhonesListDialog(activity, model, this, serviceId);
        phonesListDialog.setTitle(serviceId);
        phonesListDialog.show();
        model.setLastSelectedService(serviceId);
        if (model.getSharedPrefs().getBoolean(SHOULD_SHOW_PHONES_DELETION_TIP, true)) {
            Toast.makeText(activity, R.string.phone_deletion_desc, Toast.LENGTH_SHORT).show();
            PreferenceManager.getDefaultSharedPreferences(activity).edit()
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

    public void showRateDialog(){
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
}

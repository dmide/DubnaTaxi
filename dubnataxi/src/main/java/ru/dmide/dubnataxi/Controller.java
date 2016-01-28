package ru.dmide.dubnataxi;

import android.content.Intent;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.widget.Toast;


import ru.dmide.dubnataxi.dialogs.PhonesListDialog;

/**
 * Created by drevis on 19.02.14.
 */
public class Controller {
    private static final String SHOULD_SHOW_PHONES_DELETION_TIP = "SHOULD_SHOW_PHONES_DELETION_TIP";

    private final ModelFragment model;

    Controller(ModelFragment model) {
        this.model = model;
    }

    public void onServiceClick(String serviceId, final MainActivity activity) {
        PhonesListDialog phonesListDialog = new PhonesListDialog(activity, model, this, serviceId);
        phonesListDialog.setTitle(serviceId);
        phonesListDialog.show();
        model.setLastSelectedService(serviceId);
        if (activity.getSharedPrefs().getBoolean(SHOULD_SHOW_PHONES_DELETION_TIP, true)) {
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
}

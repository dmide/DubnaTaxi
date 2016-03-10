package ru.dmide.dubnataxi.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.SwipeDismissAdapter;

import java.util.HashSet;
import java.util.Set;

import ru.dmide.dubnataxi.Controller;
import ru.dmide.dubnataxi.ModelFragment;
import ru.dmide.dubnataxi.R;
import ru.dmide.dubnataxi.adapters.PhonesAdapter;

/**
 * Created by dmide on 24/01/2016.
 */
public class PhonesListDialog {

    private final Context context;
    private final ModelFragment model;
    private final Controller controller;
    private final String serviceId;
    private final Set<String> phonesToDelete = new HashSet<>();
    private final AlertDialog.Builder builder;
    private final PhonesAdapter phonesAdapter;

    private AlertDialog dialog;
    private Button okButton, cancelButton;

    public PhonesListDialog(Context context, ModelFragment model, Controller controller, String serviceId) {
        this.context = context;
        this.controller = controller;
        this.model = model;
        this.serviceId = serviceId;
        builder = new AlertDialog.Builder(context, R.style.ServicesDialogStyle)
                .setTitle(serviceId)
                .setPositiveButton(R.string.ok, (dialog1, which) -> {
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.cancel, (dialog2, which1) -> {
                    restoreDeleted();
                    dialog.dismiss();
                })
                .setView(R.layout.phones_dialog_view)
                .setOnDismissListener(dialog3 -> restoreDeleted());
        phonesAdapter = new PhonesAdapter(model, serviceId);
    }

    public void show() {
        dialog = builder.show();

        okButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        okButton.setVisibility(View.GONE);
        cancelButton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        cancelButton.setVisibility(View.GONE);

        ListView phonesList = (ListView) dialog.findViewById(R.id.phones_list);

        SwipeDismissAdapter dismissAdapter = new SwipeDismissAdapter(phonesAdapter, (viewGroup, ints) -> {
            deletePhone(phonesAdapter.getItem(ints[0]));
            okButton.setVisibility(View.VISIBLE);
            cancelButton.setVisibility(View.VISIBLE);
            Toast.makeText(context.getApplicationContext(), R.string.phone_deleted, Toast.LENGTH_SHORT).show();
        });
        dismissAdapter.setAbsListView(phonesList);
        phonesList.setAdapter(dismissAdapter);

        phonesList.setOnItemClickListener((parent, view, position, id) -> {
            controller.onPhoneNumberClick(phonesAdapter.getItem(position));
            phonesAdapter.notifyDataSetChanged();
        });
    }

    private void deletePhone(String number) {
        phonesToDelete.add(number);
        model.deletePhoneNumber(number);
        phonesAdapter.notifyDataSetChanged();

        if (model.getPhonesForService(serviceId).isEmpty()) {
            promptServiceRemoval();
        }
    }

    private void promptServiceRemoval() {
        dialog.findViewById(R.id.prompt).setVisibility(View.VISIBLE);
        okButton.setOnClickListener(v -> {
            model.deleteService(serviceId);
            dialog.dismiss();
        });
    }

    private void restoreDeleted() {
        for (String phone : phonesToDelete) {
            model.restorePhoneNumber(phone);
        }
        phonesAdapter.notifyDataSetChanged();
    }
}

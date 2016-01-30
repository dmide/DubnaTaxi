package ru.dmide.dubnataxi.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialog;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.SwipeDismissAdapter;

import java.util.HashSet;
import java.util.Set;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ru.dmide.dubnataxi.Controller;
import ru.dmide.dubnataxi.ModelFragment;
import ru.dmide.dubnataxi.adapters.PhonesAdapter;
import ru.dmide.dubnataxi.R;

/**
 * Created by dmide on 24/01/2016.
 */
public class PhonesListDialog extends AppCompatDialog {

    @Bind(R.id.buttons)
    LinearLayout buttons;
    @Bind(R.id.phones_list)
    ListView phonesList;

    private final ModelFragment model;
    private final Controller controller;
    private final String serviceId;
    private final Set<String> phonesToDelete = new HashSet<>();
    private PhonesAdapter phonesAdapter;

    public PhonesListDialog(Context context, ModelFragment model, Controller controller, String serviceId) {
        super(context, R.style.ServicesDialogStyle);
        this.model = model;
        this.controller = controller;
        this.serviceId = serviceId;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.phones_dialog_view);
        ButterKnife.bind(this);

        phonesAdapter = new PhonesAdapter(model, serviceId);

        SwipeDismissAdapter dismissAdapter = new SwipeDismissAdapter(phonesAdapter, (viewGroup, ints) -> {
            deletePhone(phonesAdapter.getItem(ints[0]));
            buttons.setVisibility(View.VISIBLE);
            Toast.makeText(getContext().getApplicationContext(), R.string.phone_deleted, Toast.LENGTH_SHORT).show();
        });
        dismissAdapter.setAbsListView(phonesList);
        phonesList.setAdapter(dismissAdapter);

        phonesList.setOnItemClickListener((parent, view, position, id) -> {
            controller.onPhoneNumberClick(phonesAdapter.getItem(position));
            phonesAdapter.notifyDataSetChanged();
        });
    }

    @OnClick(R.id.ok)
    public void ok() {
        dismiss();
    }

    @OnClick(R.id.cancel)
    public void cancel() {
        restoreDeleted();
        dismiss();
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
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.delete_service)
                .setNegativeButton(R.string.no, (dialog, which) -> {
                    restoreDeleted();
                })
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    model.deleteService(serviceId);
                })
                .show();
        dismiss();
    }

    private void restoreDeleted() {
        for (String phone: phonesToDelete){
            model.restorePhoneNumber(phone);
        }
        phonesAdapter.notifyDataSetChanged();
    }
}

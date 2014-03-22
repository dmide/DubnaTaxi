package ru.dmide.dubnataxi;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.haarman.listviewanimations.itemmanipulation.OnDismissCallback;
import com.haarman.listviewanimations.itemmanipulation.SwipeDismissAdapter;

import java.util.HashSet;
import java.util.Set;

import static ru.dmide.dubnataxi.BaseActivity.viewById;

/**
 * Created by drevis on 19.02.14.
 */
public class Controller {

    private ModelFragment model;
    private final Set<String> deleted = new HashSet<String>();

    Controller(ModelFragment model) {
        this.model = model;
    }

    public void callNumber(String number) {
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + number));
        model.calledNumbers.add(number);
        model.new SaveJsonTask(ModelFragment.CALLED_NUMS).execute();
        model.getMainActivity().startActivity(callIntent);
        model.getPhonesAdapter().notifyDataSetChanged();
    }

    public void deleteNumber(String numberToDelete) {
        model.deletedNumbers.add(numberToDelete);
        model.new SaveJsonTask(ModelFragment.DELETED_NUMS).execute();
    }

    public void restoreNumbers() {
        model.deletedNumbers.removeAll(deleted);
        model.new SaveJsonTask(ModelFragment.DELETED_NUMS).execute();
    }

    public void deleteCurrentService() {
        model.deletedServices.add(model.currentService);
        model.new SaveJsonTask(ModelFragment.DELETED_SERVICES).execute();
        model.initServicesAdapter();
    }

    public void clearDeletedValues() {
        model.deletedNumbers.clear();
        model.deletedServices.clear();
        model.new SaveJsonTask(ModelFragment.DELETED_NUMS).execute();
        model.new SaveJsonTask(ModelFragment.DELETED_SERVICES).execute();
    }

    public void onServiceClick(final int groupPos, final Activity activity) {
        deleted.clear();
        model.initPhonesAdapter(groupPos);
        ListView phonesList = new ListView(activity);
        final AlertDialog dialog = new AlertDialog.Builder(activity)
                .setTitle(model.currentService)
                .setView(phonesList)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        restoreNumbers();
                    }
                })
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .create();
        SwipeDismissAdapter dismissAdapter = new SwipeDismissAdapter(model.getPhonesAdapter(), new OnDismissCallback() {
            @Override
            public void onDismiss(AbsListView absListView, int[] ints) {
                deletePhone((String) model.getPhonesAdapter().getItem(ints[0]), groupPos, activity, dialog);
            }
        });
        dismissAdapter.setAbsListView(phonesList);
        phonesList.setAdapter(dismissAdapter);
        setChildsListener(phonesList);
        dialog.show();
    }

    private void deletePhone(String itemAtPosition, final int groupPos, final Activity activity, Dialog dialog) {
        deleted.add(itemAtPosition);
        deleteNumber(itemAtPosition);
        boolean isEmpty = !model.initPhonesAdapter(groupPos);
        if (isEmpty) {
            dialog.dismiss();
            new AlertDialog.Builder(activity)
                    .setTitle(R.string.delete_service)
                    .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            restoreNumbers();
                            onServiceClick(groupPos, activity);
                        }
                    })
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deleteCurrentService();
                        }
                    })
                    .show();
        }
    }

    private void setChildsListener(ListView list) {
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView numberTV = viewById(view, R.id.phone_number_tv);
                String number = numberTV.getText().toString();
                callNumber(number);
            }
        });
    }
}

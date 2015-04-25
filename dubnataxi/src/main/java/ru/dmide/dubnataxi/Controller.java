package ru.dmide.dubnataxi;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.OnDismissCallback;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.SwipeDismissAdapter;

import java.util.HashSet;
import java.util.Set;

import static ru.dmide.dubnataxi.BaseActivity.viewById;

/**
 * Created by drevis on 19.02.14.
 */
public class Controller {
    private static final String PHONE_DELETION_INFO = "PHONE_DELETION_INFO";

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

    public void onServiceClick(final int groupPos, final MainActivity activity) {
        deleted.clear();
        model.initPhonesAdapter(groupPos);
        View view = activity.getLayoutInflater().inflate(R.layout.dialog_view, null);
        final Button okBtn = viewById(view, R.id.ok);
        final Button cancelBtn = viewById(view, R.id.cancel);
        final LinearLayout buttons = viewById(view, R.id.buttons);
        final ListView phonesList = viewById(view, R.id.list);
        final AlertDialog dialog = new AlertDialog.Builder(activity)
                .setTitle(model.currentService)
                .setView(view)
                .create();
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                restoreNumbers();
                dialog.dismiss();
            }
        });
        SwipeDismissAdapter dismissAdapter = new SwipeDismissAdapter(model.getPhonesAdapter(), new OnDismissCallback() {
            @Override
            public void onDismiss(@NonNull ViewGroup viewGroup, @NonNull int[] ints) {
                deletePhone((String) model.getPhonesAdapter().getItem(ints[0]), groupPos, activity, dialog);
                buttons.setVisibility(View.VISIBLE);
                Toast.makeText(activity, R.string.phone_deleted, Toast.LENGTH_SHORT).show();
            }
        });
        dismissAdapter.setAbsListView(phonesList);
        phonesList.setAdapter(dismissAdapter);
        setChildsListener(phonesList);
        dialog.show();
        if (activity.getSharedPrefs().getBoolean(PHONE_DELETION_INFO, true)) {
            Toast.makeText(activity, R.string.phone_deletion_desc, Toast.LENGTH_SHORT).show();
            checkDeletionInfoShowed(activity);
        }
    }

    private void deletePhone(String itemAtPosition, final int groupPos, final MainActivity activity, Dialog dialog) {
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

    private void checkDeletionInfoShowed(Activity activity) {
        PreferenceManager.getDefaultSharedPreferences(activity)
                .edit()
                .putBoolean(PHONE_DELETION_INFO, false)
                .commit();
    }
}

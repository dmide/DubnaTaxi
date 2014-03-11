package ru.dmide.dubnataxi;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.haarman.listviewanimations.itemmanipulation.OnDismissCallback;
import com.haarman.listviewanimations.itemmanipulation.SwipeDismissAdapter;

import static ru.dmide.dubnataxi.BaseActivity.viewById;

/**
 * Created by drevis on 19.02.14.
 */
public class Controller {

    private ModelFragment model;

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

    public void onServiceClick(final int groupPos, Activity activity) {
        model.initPhonesAdapter(groupPos);
        ListView phonesList = new ListView(activity);
        final AlertDialog dialog = new AlertDialog.Builder(activity).
                setView(phonesList).
                create();
        SwipeDismissAdapter dismissAdapter = new SwipeDismissAdapter(model.getPhonesAdapter(), new OnDismissCallback() {
            @Override
            public void onDismiss(AbsListView absListView, int[] ints) {
                String number = (String) absListView.getItemAtPosition(ints[0]);
                deleteNumber(number);
                boolean isEmpty = !model.initPhonesAdapter(groupPos);
                if (isEmpty) {
                    deleteCurrentService();
                    dialog.dismiss();
                }
            }
        });
        dismissAdapter.setAbsListView(phonesList);
        phonesList.setAdapter(dismissAdapter);
        setChildsListener(phonesList);
        dialog.show();
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

package ru.dmide.dubnataxi;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;

/**
 * Created by drevis on 19.02.14.
 */
public class Controller {

    private ModelFragment model;

    Controller(ModelFragment model){
        this.model = model;
    }

    public void callNumber(String number){
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + number));
        model.calledNumbers.add(number);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            model.new SaveCalledNumbersTask().execute();
        }
        model.getMainActivity().startActivity(callIntent);
        model.getServicesAdapter().notifyDataSetChanged();
    }

    public void deleteNumber(String numberToDelete) {
        model.deletedNumbers.add(numberToDelete);
        model.new SaveDeletedNumbersTask().execute();
    }

    public void deleteCurrentService() {
        model.deletedServices.add(model.currentService);
        model.new SaveDeletedServicesTask().execute();
        model.initServicesAdapter();
    }

    public void clearDeletedValues() {
        model.deletedNumbers.clear();
        model.deletedServices.clear();
        model.new SaveDeletedNumbersTask().execute();
        model.new SaveDeletedServicesTask().execute();
    }
}

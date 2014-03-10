package ru.dmide.dubnataxi;

import android.content.Intent;
import android.net.Uri;

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
}

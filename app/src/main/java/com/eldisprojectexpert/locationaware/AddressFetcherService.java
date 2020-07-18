package com.eldisprojectexpert.locationaware;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions and extra parameters.
 */
public class AddressFetcherService extends IntentService {

    private static final String TAG = "AddressFetcherService";
    private ResultReceiver resultReceiver;
    private Location location;

    public AddressFetcherService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        resultReceiver = intent.getParcelableExtra(Constants.RECEIVER);
        if (resultReceiver == null){
            Log.i(TAG, "onHandleIntent: receiver not available");
            return;
        }
        
        location = intent.getParcelableExtra(Constants.LOCATION_DATA_EXTRA);
        if (location == null){
            Log.i(TAG, "onHandleIntent: location data is not available");
            respondWithResult(Constants.FAILURE_RESULT, "location data is not available");
            return;
        }

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addressList = null;

        try {
            addressList = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addressList == null && addressList.size() == 0){
                Log.i(TAG, "onHandleIntent: address unavailable");
                respondWithResult(Constants.FAILURE_RESULT, "address unavailable");
            } else {
                StringBuilder stringBuilder = new StringBuilder();
                Address address = addressList.get(0);
                for (int i = 0; i <= address.getMaxAddressLineIndex(); i++){
                    stringBuilder.append(address.getAddressLine(i));
                }

                respondWithResult(Constants.SUCCESS_RESULT, stringBuilder.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
            respondWithResult(Constants.FAILURE_RESULT, e.getMessage());
        }



    }

    private void respondWithResult(int resultCode, String resultMessage){
        Bundle bundle = new Bundle();
        bundle.putString(Constants.RESULT_DATA_KEY, resultMessage);
        resultReceiver.send(resultCode, bundle);
    }


}

package com.example.locationapp;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

class FetchAddressTask extends AsyncTask<Location, Void, String> {
    private final String TAG = FetchAddressTask.class.getSimpleName();
    private Context mContext;
    private OnTaskCompleted mListener;

    FetchAddressTask(Context appContext, OnTaskCompleted listener) {
        mContext = appContext;
        mListener = listener;
    }

    @Override
    protected String doInBackground(Location... locations) {
        Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
        Location location = locations[0];
        List<Address> addresses = null;
        String resultMessage = "";
        try {
            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses == null || addresses.size() == 0) {
                if (resultMessage.isEmpty()) {
                    resultMessage = mContext.getString(R.string.no_address_found);
                    Log.e(TAG, resultMessage);
                }
            } else {
                Log.d(TAG, "Got address from geocoder");
                Address address = addresses.get(0);
                ArrayList<String> addressParts = new ArrayList<>();
                for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                    addressParts.add(address.getAddressLine(i));
                }
                resultMessage = TextUtils.join("\n", addressParts);
            }
        } catch (IOException e) {
            e.printStackTrace();
            resultMessage = mContext.getString(R.string.no_address_service);
            Log.e(TAG, resultMessage, e);
        } catch (IllegalArgumentException iae) {
            iae.printStackTrace();
            resultMessage = mContext.getString(R.string.invalid_coordinates);
            Log.e(TAG, resultMessage + ". Lat: " + location.getLatitude() + ", Long: " + location.getLongitude(), iae);
        }
        return resultMessage;
    }

    @Override
    protected void onPostExecute(String address) {
        mListener.onTaskCompleted(address);
        super.onPostExecute(address);
    }



    interface OnTaskCompleted {
        void onTaskCompleted(String result);
    }


}
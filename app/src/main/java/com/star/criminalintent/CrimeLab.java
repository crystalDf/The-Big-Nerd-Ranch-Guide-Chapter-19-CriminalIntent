package com.star.criminalintent;


import android.content.Context;
import android.util.Log;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

public class CrimeLab {

    private static final String TAG = "CrimeLab";
    private static final String FILENAME = "crime.json";

    private static CrimeLab sCrimeLab;
    private Context mAppContext;

    private ArrayList<Crime> mCrimes;
    private CriminalIntentJSONSerializer mSerializer;

    private CrimeLab(Context appContext) {
        mAppContext = appContext;
//        mCrimes = new ArrayList<>();

        mSerializer = new CriminalIntentJSONSerializer(appContext, FILENAME);

        try {
            mCrimes = mSerializer.loadCrimes();
        } catch (JSONException e) {
            e.printStackTrace();
            mCrimes = new ArrayList<>();
            Log.e(TAG, "Error loading crimes: ", e);
        } catch (IOException e) {
            e.printStackTrace();
            mCrimes = new ArrayList<>();
            Log.e(TAG, "Error loading crimes: ", e);
        }
    }

    public static CrimeLab getCrimeLab(Context c) {
        if (sCrimeLab == null) {
            sCrimeLab = new CrimeLab(c.getApplicationContext());
        }
        return sCrimeLab;
    }

    public ArrayList<Crime> getCrimes() {
        return mCrimes;
    }

    public Crime getCrime(UUID id) {
        for (Crime c : mCrimes) {
            if (c.getId().equals(id)) {
                return c;
            }
        }
        return null;
    }

    public void addCrime(Crime c) {
        mCrimes.add(c);
    }

    public void deleteCrime(Crime c) {
        mCrimes.remove(c);
    }

    public boolean saveCrimes() {
        try {
            mSerializer.saveCrimes(mCrimes);
            Log.d(TAG, "crimes saved to file");
            return true;
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "Error saving crimes: ", e);
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Error saving crimes: ", e);
            return false;
        }

    }
}

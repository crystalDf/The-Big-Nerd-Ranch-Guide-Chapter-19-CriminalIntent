package com.star.criminalintent;


import android.content.Context;
import android.os.Environment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

public class CriminalIntentJSONSerializer {

    private Context mContext;
    private String mFileName;

    public CriminalIntentJSONSerializer(Context context, String fileName) {
        mContext = context;
        mFileName = fileName;
    }

    public void saveCrimes(ArrayList<Crime> crimes) throws JSONException, IOException {

        File sdCard = Environment.getExternalStorageDirectory();

        File dir = new File(sdCard.getAbsolutePath() + File.separator + mContext.getString(R.string.app_name));

        if (!dir.exists()) {
            dir.mkdirs();
        }

        File file = new File(dir, mFileName);

        JSONArray array = new JSONArray();

        for (Crime c : crimes) {
            array.put(c.toJSON());
        }

        PrintWriter out = null;

        try {
//            out = new PrintWriter(mContext.openFileOutput(mFileName, Context.MODE_PRIVATE));
            out = new PrintWriter(file);
            out.println(array.toString());
        } finally {
            if (out != null) {
                out.close();
            }
        }

    }

    public ArrayList<Crime> loadCrimes() throws JSONException, IOException {

        File sdCard = Environment.getExternalStorageDirectory();

        File dir = new File(sdCard.getAbsolutePath() + File.separator + mContext.getString(R.string.app_name));

        File file = new File(dir, mFileName);

        ArrayList<Crime> crimes = new ArrayList<>();

        Scanner in = null;

        try {
//            in = new Scanner(mContext.openFileInput(mFileName), "UTF-8");
            in = new Scanner(file, "UTF-8");

            StringBuilder jsonString = new StringBuilder();

            String line;
            while (in.hasNextLine()) {
                line = in.nextLine();
                jsonString.append(line);
            }

            JSONArray array = (JSONArray) new JSONTokener(jsonString.toString()).nextValue();

            for (int i = 0; i < array.length(); i++) {
                crimes.add(new Crime(array.getJSONObject(i)));
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                in.close();
            }
        }
        return crimes;
    }
}

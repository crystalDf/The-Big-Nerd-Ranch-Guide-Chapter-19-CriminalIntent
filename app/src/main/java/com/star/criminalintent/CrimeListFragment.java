package com.star.criminalintent;


import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ListFragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.view.ActionMode;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

public class CrimeListFragment extends ListFragment {

    private ArrayList<Crime> mCrimes;

    private boolean mSubtitleVisible;

    private static final String TAG = "CrimeListFragment";

    private static final int REQUEST_CRIME = 1;

    private ActionMode mActionMode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
        mSubtitleVisible = false;

        setHasOptionsMenu(true);

        getActivity().setTitle(R.string.crimes_title);

        mCrimes = CrimeLab.getCrimeLab(getActivity()).getCrimes();

//        ArrayAdapter<Crime> adapter =
//                new ArrayAdapter<Crime>(getActivity(), android.R.layout.simple_list_item_1, mCrimes);

        CrimeAdapter adapter = new CrimeAdapter(mCrimes);

        setListAdapter(adapter);

    }

//    @TargetApi(11)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_crime_list, container, false);

//        ListView listView = (ListView) v.findViewById(android.R.id.list);
//
//        listView.setEmptyView(v.findViewById(android.R.id.empty));

        Button button = (Button) v.findViewById(R.id.emptyButton);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Crime c = new Crime();
                CrimeLab.getCrimeLab(getActivity()).addCrime(c);
                Intent i = new Intent(getActivity(), CrimePagerActivity.class);
                i.putExtra(CrimeFragment.EXTRA_CRIME_ID, c.getId());
                startActivityForResult(i, 0);
            }
        });

        if (mSubtitleVisible) {
            ((ActionBarActivity)getActivity()).getSupportActionBar().setSubtitle(R.string.subtitle);
        }

        ListView listView = (ListView) v.findViewById(android.R.id.list);

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                if (mActionMode != null) {
                    return false;
                }

                getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                getListView().setItemChecked(position, true);

                ((ActionBarActivity)getActivity()).startSupportActionMode(new ActionMode.Callback() {
                    @Override
                    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                        MenuInflater inflater = actionMode.getMenuInflater();
                        inflater.inflate(R.menu.context_menu_fragment_crime_list_item_delete_crime, menu);
                        mActionMode = actionMode;
                        return true;
                    }

                    @Override
                    public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                        return false;
                    }

                    @Override
                    public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.context_menu_item_delete_crime:
                                CrimeAdapter adapter = (CrimeAdapter) getListAdapter();
                                CrimeLab crimeLab = CrimeLab.getCrimeLab(getActivity());
                                for (int i = adapter.getCount() - 1; i >= 0; i--) {
                                    if (getListView().isItemChecked(i)) {
                                        deleteOldPhoto(adapter.getItem(i));
                                        crimeLab.deleteCrime(adapter.getItem(i));
                                    }
                                }
                                actionMode.finish();
                                adapter.notifyDataSetChanged();
                                return true;
                            default:
                                return false;
                        }
                    }

                    @Override
                    public void onDestroyActionMode(ActionMode actionMode) {
                        getListView().clearChoices();

                        for (int i = 0; i < getListView().getChildCount(); i++) {
                            getListView().getChildAt(i).getBackground().setState(new int[] {0});
                        }

                        getListView().setChoiceMode(ListView.CHOICE_MODE_NONE);
                        mActionMode = null;
                    }
                });

                return true;
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mActionMode != null) {
                    if (getListView().isItemChecked(position)) {
                        getListView().setItemChecked(position, false);
                    } else {
                        getListView().setItemChecked(position, true);
                    }
                }
            }
        });

//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
//            registerForContextMenu(listView);
//        } else {
//            // setMultiChoiceModeListener还不在v7 support库里，需要自己实现
//            listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
//            listView.setMultiChoiceModeListener(
//                    new AbsListView.MultiChoiceModeListener() {
//                @Override
//                public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
//
//                }
//
//                @Override
//                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
//                    MenuInflater inflater = mode.getMenuInflater();
//                    inflater.inflate(R.menu.context_menu_fragment_crime_list_item_delete_crime, menu);
//                    return true;
//                }
//
//                @Override
//                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
//                    return false;
//                }
//
//                @Override
//                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
//                    switch (item.getItemId()) {
//                        case R.id.menu_item_delete_crime:
//                            CrimeAdapter adapter = (CrimeAdapter) getListAdapter();
//                            CrimeLab crimeLab = CrimeLab.getCrimeLab(getActivity());
//                            for (int i = adapter.getCount() - 1; i >= 0; i--) {
//                                if (getListView().isItemChecked(i)) {
//                                    crimeLab.deleteCrime(adapter.getItem(i));
//                                }
//                            }
//                            mode.finish();
//                            adapter.notifyDataSetChanged();
//                            return true;
//                        default:
//                            return false;
//                    }
//                }
//
//                @Override
//                public void onDestroyActionMode(ActionMode mode) {
//
//                }
//            });
//        }
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

        //针对2.2后退键，4.1后退键具备刷新功能，可省略，应用图标层级式导航具备刷新功能，可省略
        ((CrimeAdapter)getListAdapter()).notifyDataSetChanged();
    }

    @Override
    public void onPause() {
        super.onPause();
        CrimeLab.getCrimeLab(getActivity()).saveCrimes();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        if (mActionMode == null) {
            Crime c = (Crime) getListAdapter().getItem(position);
//        Log.d(TAG, c.getTitle() + " was clicked");

//        Intent i = new Intent(getActivity(), CrimeActivity.class);
            Intent i = new Intent(getActivity(), CrimePagerActivity.class);
            i.putExtra(CrimeFragment.EXTRA_CRIME_ID, c.getId());
//        startActivity(i);
            startActivityForResult(i, REQUEST_CRIME);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CRIME) {
            // handle result
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.option_menu_fragment_crime_list, menu);

        MenuItem showSubtitle = menu.findItem(R.id.option_menu_item_show_subtitle);
        if (mSubtitleVisible && showSubtitle != null) {
            showSubtitle.setTitle(R.string.hide_subtitle);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.option_menu_item_new_crime:
                Crime c = new Crime();
                CrimeLab.getCrimeLab(getActivity()).addCrime(c);
                Intent i = new Intent(getActivity(), CrimePagerActivity.class);
                i.putExtra(CrimeFragment.EXTRA_CRIME_ID, c.getId());
                startActivityForResult(i, 0);
                return true;
            case R.id.option_menu_item_show_subtitle:
                if (((ActionBarActivity)getActivity()).getSupportActionBar().getSubtitle() == null) {
                    ((ActionBarActivity)getActivity()).getSupportActionBar().
                            setSubtitle(R.string.subtitle);
                    mSubtitleVisible = true;
                    item.setTitle(R.string.hide_subtitle);
                } else {
                    ((ActionBarActivity)getActivity()).getSupportActionBar().setSubtitle(null);
                    mSubtitleVisible = false;
                    item.setTitle(R.string.show_subtitle);
                }

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getActivity().getMenuInflater().inflate(R.menu.context_menu_fragment_crime_list_item_delete_crime, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int position = info.position;
        CrimeAdapter adapter = (CrimeAdapter) getListAdapter();
        Crime crime = adapter.getItem(position);

        switch (item.getItemId()) {
            case R.id.context_menu_item_delete_crime:
                CrimeLab.getCrimeLab(getActivity()).deleteCrime(crime);
                adapter.notifyDataSetChanged();
                return true;
        }

        return super.onContextItemSelected(item);
    }

    private class CrimeAdapter extends ArrayAdapter<Crime> {

        public CrimeAdapter(ArrayList<Crime> crimes) {
            super(getActivity(), 0, crimes);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.list_item_crime, null);
            }

            Crime c = getItem(position);

            TextView titleTextView =
                    (TextView) convertView.findViewById(R.id.crime_list_item_titleTextView);
            titleTextView.setText(c.getTitle());

            TextView dateTextView =
                    (TextView) convertView.findViewById(R.id.crime_list_item_dateTextView);
            dateTextView.setText(c.getFormattedDate());

            CheckBox solvedCheckBox =
                    (CheckBox) convertView.findViewById(R.id.crime_list_item_solvedCheckBox);
            solvedCheckBox.setChecked(c.isSolved());

            return convertView;
        }
    }

    private String getSDPhotoPath(String filename) {

        File sdCard = Environment.getExternalStorageDirectory();

        File dir = new File(sdCard.getAbsolutePath() + File.separator + getActivity().getString(R.string.app_name));

        File file = new File(dir, filename);

        return file.getAbsolutePath();
    }

    private void deleteOldPhoto(Crime crime) {

        Photo photo = crime.getPhoto();

        if (photo != null) {
            String path = getSDPhotoPath(photo.getFilename());

            File file = new File(path);
            file.delete();
        }
    }
}

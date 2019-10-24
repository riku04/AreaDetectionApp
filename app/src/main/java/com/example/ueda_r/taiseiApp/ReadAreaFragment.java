package com.example.ueda_r.taiseiApp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;

import com.example.ueda_r.osmdroidtest1022.R;

import java.io.File;
import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ReadAreaFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ReadAreaFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ReadAreaFragment extends Fragment implements View.OnClickListener, AreaListAdapter.AreaListAdapterCallback{

    AreaListAdapter.AreaListAdapterCallback areaListAdapterCallback;

    private ReadAreaFragmentCallback callback;

    ListView listView;
    AreaListAdapter adapter;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public ReadAreaFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ReadAreaFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ReadAreaFragment newInstance(String param1, String param2) {
        ReadAreaFragment fragment = new ReadAreaFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }



    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);
        container.removeAllViews();

//        Bundle bundle = getArguments();
//        ParcelableParameter parameter = bundle.getParcelable("PARAMETER");
//        parameter.readParameter(MainActivity.paramPath);

        View v = inflater.inflate(R.layout.fragment_read_area, container, false);
        v.setFocusableInTouchMode(true);
        v.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                    // 何らかの処理
                    removeThisFragment();
                    return true;
                }
                return false;
            }
        });


        ImageButton backButton = v.findViewById(R.id.readAreaBackButton);
        backButton.setOnClickListener(this);

        listView = (ListView) v.findViewById(R.id.readAreaListView);

        adapter = areaListAdapterBuilder();

        listView.setAdapter(adapter);

        return v;
    }

    public AreaListAdapter areaListAdapterBuilder() {
        AreaListAdapter areaListAdapter;

        ArrayList<AreaListItem> areaListItems = new ArrayList<>();
        ArrayList<String> filenameList = getAreaDataListFromExternalStorage();

        for(int i=0;i<=filenameList.size()-1;i++){
            AreaListItem item = new AreaListItem(filenameList.get(i));
            areaListItems.add(item);
        }
        areaListAdapter = new AreaListAdapter(this.getContext(),R.layout.csv_list,areaListItems);
        areaListAdapter.setCallback(this);
        return areaListAdapter;
    }


    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onClick(View v) {
        removeThisFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communzicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private void removeThisFragment(){
        getActivity().getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.enter_from_right,R.anim.exit_to_right).remove(this).commit();
        DrawerLayout drawer = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
        drawer.openDrawer(GravityCompat.START);
    }
    private void removeThisFragmentWithoutOpenDrawer() {
        getActivity().getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.enter_from_right,R.anim.exit_to_right).remove(this).commit();
    }

    public ArrayList<String> getAreaDataListFromExternalStorage() {
        ArrayList<String> areaDataList = new ArrayList<>();

        String path = MainActivity.PATH_MAIN_DIRECTORY;
        Log.d("Files", "Path: " + path);
        File directory = new File(path);
        File[] files = directory.listFiles();
        Log.d("Files", "Size: "+ files.length);
        for (int i = 0; i < files.length; i++)
        {
            Log.d("Files", "FileName:" + files[i].getName());
            if(files[i].getName().contains(".area")){
                areaDataList.add(files[i].getName());
            }
        }
        return areaDataList;
    }

    public void titlePressed(final String filename) {
        new AlertDialog.Builder(getActivity())
                .setTitle("領域データ読み込み")
                .setMessage(filename+"\r\n\r\n現在の領域は削除されます")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String string = filename;
                        callback.readAreaDataCsv(filename);
                        removeThisFragmentWithoutOpenDrawer();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    public void deletePressed(final String filename) {
        new AlertDialog.Builder(getActivity())
                .setTitle("領域データ削除")
                .setMessage(filename)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String string = filename;
                        callback.deleteAreaDataCsv(filename);

                        //wait for remove
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                adapter = areaListAdapterBuilder();
                                listView.setAdapter(adapter);
                                adapter.notifyDataSetChanged();
                            }
                        },100);

                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    public void setCallback(ReadAreaFragmentCallback callback) {
        this.callback = callback;
    }
    interface ReadAreaFragmentCallback {
        void readAreaDataCsv(String filename);
        void deleteAreaDataCsv(String filename);
    }
}

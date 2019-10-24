package com.example.ueda_r.taiseiApp;

import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ueda_r.osmdroidtest1022.R;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SettingFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SettingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingFragment extends Fragment implements View.OnClickListener{

    AlertDialog dialog;

    private boolean admin = false;
    private boolean isCloseAlertOn = false;
    private boolean isEnterAlertOn = false;
    private boolean isJukiAlertOn = false;
    private boolean isLoggingOn = false;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public SettingFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SettingFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SettingFragment newInstance(String param1, String param2) {
        SettingFragment fragment = new SettingFragment();
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
    public View onCreateView(LayoutInflater inflater,ViewGroup container,
                             Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.fragment_setting, null);

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

        Bundle bundle = getArguments();     //インテントでカスタムParameterクラスのオブジェクトを渡す
        final MainActivity.Parameter parameter = (MainActivity.Parameter) bundle.getParcelable("PARAMETER");
        Toast.makeText(getActivity().getApplicationContext(), parameter.getUserID(), Toast.LENGTH_SHORT).show();
        admin = parameter.isAdmin();

        ImageButton backButton = v.findViewById(R.id.settingBackButton);
        backButton.setOnClickListener(this);

        TextView userSetting = v.findViewById(R.id.setting_menu_user);
        userSetting.setClickable(true);
        userSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity().getApplicationContext(), "user setting", Toast.LENGTH_SHORT).show();

                LayoutInflater inflater = LayoutInflater.from(getContext());
                final View layout = inflater.inflate(R.layout.dialog_setting_user, null);

                final EditText EditTextUserID = layout.findViewById(R.id.userID);
                EditTextUserID.setText(parameter.getUserID());

                final EditText EditTextGroupID = layout.findViewById(R.id.groupID);
                EditTextGroupID.setText(parameter.getGroupID());

                final EditText password = layout.findViewById(R.id.password);

                final Switch adminSwitch = layout.findViewById(R.id.adminSwitch);
                adminSwitch.setChecked(parameter.isAdmin());
                adminSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                        String pass = password.getText().toString();
                        if (pass.equals("1111")) {
                            if (isChecked) {
                                admin = true;
                            } else {
                                admin = false;
                            }
                        } else {
                            admin = false;
                            adminSwitch.setChecked(false);
                        }

                    }
                });

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("ユーザー設定");
                builder.setView(layout);
                builder.setPositiveButton("変更", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (admin) {
                            //EditText userID = layout.findViewById(R.id.userID);
                            String userid = EditTextUserID.getText().toString();
                            parameter.setUserID(userid);
                            //EditText groupID = layout.findViewById(R.id.groupID);
                            String groupid = EditTextGroupID.getText().toString();
                            parameter.setGroupID(groupid);
                            parameter.setAdmin(admin);

                            parameter.outputParamToCsv();
                        } else {
                            parameter.setAdmin(admin);
                        }
                    }
                });
                builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                     //parameter.setAdmin(admin);
                    }
                });
                // 表示
                dialog = builder.create();
                dialog.setCancelable(false);
                dialog.show();
                //builder.create().show();
            }
        });

        TextView alertSetting = v.findViewById(R.id.setting_menu_alert);
        alertSetting.setClickable(true);
        alertSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(getActivity().getApplicationContext(), "alert setting", Toast.LENGTH_SHORT).show();

                LayoutInflater inflater = LayoutInflater.from(getContext());
                final View layout = inflater.inflate(R.layout.dialog_setting_alert, null);

                final Switch closeAlertSwitch = layout.findViewById(R.id.closeAlertSwitch);
                closeAlertSwitch.setChecked(parameter.isCloseAlertOn());
                isCloseAlertOn = parameter.isCloseAlertOn();
                closeAlertSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if(isChecked) {
                            isCloseAlertOn = true;
                        } else {
                            isCloseAlertOn = false;
                        }
                    }
                });

                final Switch enterAlertSwitch = layout.findViewById(R.id.enterAlertSwitch);
                enterAlertSwitch.setChecked(parameter.isEnterAlertOn());
                isEnterAlertOn = parameter.isEnterAlertOn();
                enterAlertSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if(isChecked) {
                            isEnterAlertOn = true;
                        } else {
                            isEnterAlertOn = false;
                        }
                    }
                });

                final Switch jukiAlertSwitch = layout.findViewById(R.id.jukiAlertSwitch);
                jukiAlertSwitch.setChecked(parameter.isJukiAlertOn());
                isJukiAlertOn = parameter.isJukiAlertOn();
                jukiAlertSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if(isChecked) {
                            isJukiAlertOn = true;
                        } else {
                            isJukiAlertOn = false;
                        }
                    }
                });

                final EditText EditTextCloseDistance = layout.findViewById(R.id.closeDistance);
                EditTextCloseDistance.setText(Integer.toString(parameter.getCloseDistance()));

                final EditText EditTextJukiDistance = layout.findViewById(R.id.jukiDistance);
                EditTextJukiDistance.setText(Integer.toString(parameter.getJukiDistance()));

                final int closeVolume = parameter.getCloseVolume();
                final SeekBar closeVolumeBar = layout.findViewById(R.id.closeVolume);
                closeVolumeBar.setProgress(parameter.getCloseVolume());


                final int enterVolume = parameter.getEnterVolume();
                final SeekBar enterVolumeBar = layout.findViewById(R.id.enterVolume);
                enterVolumeBar.setProgress(parameter.getEnterVolume());

                final int jukiVolume = parameter.getJukiVolume();
                final SeekBar jukiVolumeBar = layout.findViewById(R.id.jukiVolume);
                jukiVolumeBar.setProgress(parameter.getJukiVolume());


                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("警報設定");
                builder.setView(layout);
                builder.setPositiveButton("変更", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        parameter.setCloseAlert(isCloseAlertOn);
                        parameter.setEnterAlert(isEnterAlertOn);
                        parameter.setJukiAlert(isJukiAlertOn);

                        int closeDistance = Integer.parseInt(EditTextCloseDistance.getText().toString());
                        parameter.setCloseDistance(closeDistance);
                        int jukiDistance = Integer.parseInt(EditTextJukiDistance.getText().toString());
                        parameter.setJukiDistance(jukiDistance);

                        parameter.setCloseVolume(closeVolumeBar.getProgress());
                        parameter.setEnterVolume(enterVolumeBar.getProgress());
                        parameter.setJukiVolume(jukiVolumeBar.getProgress());

                        parameter.outputParamToCsv();
                    }
                });
                builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                // 表示
                if (!parameter.isAdmin()) {
                    Toast.makeText(getActivity().getApplicationContext(), "この操作を行う権限がありません\r\n設定→ユーザー設定から権限を設定して下さい", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    builder.create().show();
                }
            }
        });

        final int[] HOUR_LIST = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23};
        final int[] MINUTE_LIST = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59};



        TextView logSetting = v.findViewById(R.id.setting_menu_log);
        logSetting.setClickable(true);
        logSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity().getApplicationContext(), "log setting", Toast.LENGTH_SHORT).show();

                LayoutInflater inflater = LayoutInflater.from(getContext());
                final View layout = inflater.inflate(R.layout.dialog_setting_log, null);

                final Switch logSwitch = layout.findViewById(R.id.logSwitch);
                logSwitch.setChecked(parameter.isLoggingOn());
                isLoggingOn = parameter.isLoggingOn();
                logSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if(isChecked) {
                            isLoggingOn = true;
                        } else {
                            isLoggingOn = false;
                        }
                    }
                });

//                final EditText editTextStartTime = layout.findViewById(R.id.startTime);
//                editTextStartTime.setText(Integer.toString(parameter.getStartHour()));
//
//                final EditText editTextEndTime = layout.findViewById(R.id.endTime);
//                editTextEndTime.setText(Integer.toString(parameter.getEndHour()));

                final NumberPicker startHourPicker = layout.findViewById(R.id.startHourPicker);
                startHourPicker.setMaxValue(23);
                startHourPicker.setMinValue(0);
                startHourPicker.setValue(parameter.getStartHour());
                startHourPicker.setWrapSelectorWheel(false);

                final NumberPicker startMinutePicker = layout.findViewById(R.id.startMinutePicker);
                startMinutePicker.setMaxValue(59);
                startMinutePicker.setMinValue(0);
                startMinutePicker.setValue(parameter.getStartMinute());
                startMinutePicker.setWrapSelectorWheel(false);

                final NumberPicker startLunchHourPicker = layout.findViewById(R.id.startLunchHourPicker);
                startLunchHourPicker.setMaxValue(23);
                startLunchHourPicker.setMinValue(0);
                startLunchHourPicker.setValue(parameter.getStartLunchHour());
                startLunchHourPicker.setWrapSelectorWheel(false);

                final NumberPicker startLunchMinutePicker = layout.findViewById(R.id.startLunchMinutePicker);
                startLunchMinutePicker.setMaxValue(59);
                startLunchMinutePicker.setMinValue(0);
                startLunchMinutePicker.setValue(parameter.getStartLunchMinute());
                startLunchMinutePicker.setWrapSelectorWheel(false);

                final NumberPicker endLunchHourPicker = layout.findViewById(R.id.endLunchHourPicker);
                endLunchHourPicker.setMaxValue(23);
                endLunchHourPicker.setMinValue(0);
                endLunchHourPicker.setValue(parameter.getEndLunchHour());
                endLunchHourPicker.setWrapSelectorWheel(false);

                final NumberPicker endLunchMinutePicker = layout.findViewById(R.id.endLunchMinutePicker);
                endLunchMinutePicker.setMaxValue(59);
                endLunchMinutePicker.setMinValue(0);
                endLunchMinutePicker.setValue(parameter.getEndLunchMinute());
                endLunchMinutePicker.setWrapSelectorWheel(false);

                final NumberPicker endHourPicker = layout.findViewById(R.id.endHourPicker);
                endHourPicker.setMaxValue(23);
                endHourPicker.setMinValue(0);
                endHourPicker.setValue(parameter.getEndHour());
                endHourPicker.setWrapSelectorWheel(false);

                final NumberPicker endMinutePicker = layout.findViewById(R.id.endMinutePicker);
                endMinutePicker.setMaxValue(59);
                endMinutePicker.setMinValue(0);
                endMinutePicker.setValue(parameter.getEndMinute());
                endMinutePicker.setWrapSelectorWheel(false);

                final TextView errorMessage = layout.findViewById(R.id.errorMessage);

                final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("履歴保存設定");
                builder.setView(layout);

                builder.setPositiveButton("変更", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        parameter.setLoggingOn(isLoggingOn);
//                        parameter.setStartHour(Integer.parseInt(editTextStartTime.getText().toString()));
//                        parameter.setEndHour(Integer.parseInt(editTextEndTime.getText().toString()));
                        parameter.setStartHour(startHourPicker.getValue());
                        parameter.setStartMinute(startMinutePicker.getValue());
                        parameter.setStartLunchHour(startLunchHourPicker.getValue());
                        parameter.setStartLunchMinute(startLunchMinutePicker.getValue());
                        parameter.setEndLunchHour(endLunchHourPicker.getValue());
                        parameter.setEndLunchMinute(endLunchMinutePicker.getValue());
                        parameter.setEndHour(endHourPicker.getValue());
                        parameter.setEndMinute(endMinutePicker.getValue());
                        parameter.outputParamToCsv();
                    }
                });
                builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // CANCEL ボタンクリック処理
                    }
                });

               final AlertDialog dialog = builder.create();

                final NumberPicker.OnValueChangeListener pickerListner = new NumberPicker.OnValueChangeListener() {
                    @Override
                    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                        int startHour = startHourPicker.getValue();
                        int startMinute = startMinutePicker.getValue();
                        int startLunchHour = startLunchHourPicker.getValue();
                        int startLunchMinute = startLunchMinutePicker.getValue();
                        int endLunchHour = endLunchHourPicker.getValue();
                        int endLunchMinute = endLunchMinutePicker.getValue();
                        int endHour = endHourPicker.getValue();
                        int endMinute = endMinutePicker.getValue();

                        double startTime = startHour + (double) startMinute / 60;
                        double startLunchTime = startLunchHour + (double) startLunchMinute / 60;
                        double endLunchTime = endLunchHour + (double) endLunchMinute / 60;
                        double endTime = endHour + (double) endMinute / 60;

                        //***********************************************************
                        // 1.startTime must be set faster than every other times.
                        // 2.startLunchTime must be set same or faster than endLunchTime.
                        // 3.endLunchTime must be set faster than endTime.
                        //***********************************************************

                        boolean rule1 = (startTime < startLunchTime) && (startTime < endLunchTime) && (startTime < endTime);
                        boolean rule2 = (startLunchTime <= endLunchTime);
                        boolean rule3 = (endLunchTime < endTime);

                        if (rule1 && rule2 && rule3) {
                            Log.i("SettingFragment", "log setting : input time OK");
                            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                            errorMessage.setText("");
                            // no problem
                        } else {
                            Log.i("SettingFragment", "log setting : input time NG");
                            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                            errorMessage.setText("入力が不正です");
                            // invalid input
                        }
                    }
                };
                startHourPicker.setOnValueChangedListener(pickerListner);
                startMinutePicker.setOnValueChangedListener(pickerListner);
                startLunchHourPicker.setOnValueChangedListener(pickerListner);
                startLunchHourPicker.setOnValueChangedListener(pickerListner);
                endLunchHourPicker.setOnValueChangedListener(pickerListner);
                endLunchMinutePicker.setOnValueChangedListener(pickerListner);
                endHourPicker.setOnValueChangedListener(pickerListner);
                endMinutePicker.setOnValueChangedListener(pickerListner);

                // 表示
                if (!parameter.isAdmin()) {
                    Toast.makeText(getActivity().getApplicationContext(), "この操作を行う権限がありません\r\n設定→ユーザー設定から権限を設定して下さい", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    dialog.show();
                }
            }
        });

//        TextView mapSetting = v.findViewById(R.id.setting_menu_map);
//        mapSetting.setClickable(true);
//        mapSetting.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(getActivity().getApplicationContext(), "map setting", Toast.LENGTH_SHORT).show();
//            }
//        });

        return v;
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
     * fragment to allow an interaction in this fragment to be communicated
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

    public void titlePressed(final String filename) {

    }

    public void deletePressed(final String filename) {

    }

}

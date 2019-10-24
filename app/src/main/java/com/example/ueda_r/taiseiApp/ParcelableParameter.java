package com.example.ueda_r.taiseiApp;

import android.os.Parcel;
import android.os.Parcelable;

import org.osmdroid.util.GeoPoint;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class ParcelableParameter implements Parcelable {
    private String userID = "user";  //ユーザーID
    private String groupID = "group";    //グループID

    private boolean admin = false;

    private int areaQty = 0;    //禁止領域数
    private ArrayList<ArrayList<GeoPoint>> areaData;    //禁止領域データ
    private boolean enterAlert = true;  //進入時アラートオン
    private boolean closeAlert = true;  //接近時アラートオン
    private boolean jukiAlert = true;   //重機接近時アラートオン
    private boolean vibrationOn = true; //アラート時振動オン
    private int closeVolume = 10;    //アラート音量
    private int enterVolume = 10;
    private int jukiVolume = 10;
    private boolean loggingOn = true;   //記録オン

    private int startHour = 9;  //開始時刻
    private int startMinute = 0;

    private int startLunchHour = 12;
    private int startLunchMinute = 0;

    private int endLunchHour = 13;
    private int endLunchMinute = 0;

    private int endHour = 18;   //終了時刻
    private int endMinute = 0;

    private int closeDistance = 10;
    private int jukiDistance = 10;

    private int normalLogIntvl = 10;    //通常時記録間隔
    private int semiCloseLogIntvl = 10; //準接近時記録間隔
    private int closeLogIntvl = 5;  //接近時記録間隔
    private int enterLogIntvl = 3;  //進入時記録間隔
    private int jukiCloseLogIntvl = 3;  //重機接近時記録間隔
    private int jukiQty = 0;    //重機数
    private ArrayList<String> jukiList; //重機データ
    public ParcelableParameter() {
        this.areaData = new ArrayList<>();
        this.jukiList = new ArrayList<>();
    }
    public ParcelableParameter(MainActivity.Parameter parameter) {
        this.userID = parameter.getUserID();
        this.groupID = parameter.getGroupID();
        this.admin = parameter.isAdmin();
        this.areaQty = parameter.getAreaQty();
        this.areaData = parameter.getAreaData();
        this.enterAlert = parameter.isEnterAlertOn();
        this.closeAlert = parameter.isCloseAlertOn();
        this.jukiAlert = parameter.isJukiAlertOn();
        this.vibrationOn = parameter.isVibrationOn();
        this.closeVolume = parameter.getCloseVolume();
        this.enterVolume = parameter.getEnterVolume();
        this.jukiVolume = parameter.getJukiVolume();
        this.loggingOn = parameter.isLoggingOn();
        this.startHour = parameter.getStartHour();
        this.startMinute = parameter.getStartMinute();
        this.startLunchHour = parameter.getStartLunchHour();
        this.endLunchHour = parameter.getEndLunchHour();
        this.endHour = parameter.getEndHour();
        this.endMinute = parameter.getEndMinute();
        this.closeDistance = parameter.getCloseDistance();
        this.jukiDistance = parameter.getJukiDistance();
        this.normalLogIntvl = parameter.getNormalLogIntvl();
        this.semiCloseLogIntvl = parameter.getSemiCloseLogIntvl();
        this.closeLogIntvl = parameter.getCloseLogIntvl();
        this.enterLogIntvl = parameter.getEnterLogIntvl();
        this.jukiCloseLogIntvl = parameter.getJukiCloseLogIntvl();
        this.jukiQty = parameter.getJukiQty();
        this.jukiList = parameter.getJukiList();
    }

    public void readParameter(String path) {
        ArrayList<String> titleList = new ArrayList<>();
        ArrayList<String> dataList = new ArrayList<>();

        int nowListNum = 0;

        try {
            FileInputStream fileInputStream = new FileInputStream(path);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = "";

            while ((line = reader.readLine()) != null) {
                StringTokenizer stringTokenizer =
                        new StringTokenizer(line, ",");

                titleList.add(stringTokenizer.nextToken());
                dataList.add(stringTokenizer.nextToken());
            }
            reader.close();
            inputStreamReader.close();
            fileInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.userID = dataList.get(0);
        nowListNum++;

        this.groupID = dataList.get(1);
        nowListNum++;

        this.admin = Boolean.parseBoolean(dataList.get(2));
        nowListNum++;

        this.areaQty = Integer.parseInt(dataList.get(3));
        nowListNum++;

        ArrayList<Integer> coordQtyList = new ArrayList<>();
        for (int areaNum = 1; areaNum <= areaQty; areaNum++) {
            String qty = dataList.get(nowListNum);
            nowListNum++;
            coordQtyList.add(Integer.parseInt(qty));
        }
        ArrayList<ArrayList<GeoPoint>> areaData = new ArrayList<>();
        ArrayList<GeoPoint> pointlist = new ArrayList<>();
        for (int areaNum = 1; areaNum <= areaQty; areaNum++) {
            int coordQty = coordQtyList.get(areaNum - 1);
            for (int coordNum = 1; coordNum <= coordQty; coordNum++) {
                String coordStr = dataList.get(nowListNum);
                nowListNum++;
                StringTokenizer stringTokenizer = new StringTokenizer(coordStr, "/");
                GeoPoint point = new GeoPoint(
                        Double.parseDouble(stringTokenizer.nextToken()),
                        Double.parseDouble(stringTokenizer.nextToken())
                );
                pointlist.add(new GeoPoint(point));
            }
            areaData.add(new ArrayList<GeoPoint>(pointlist));
            //Log.i("LocationService", "Current area :" + areaData.toString());
            pointlist.clear();
        }
        this.areaData = areaData;

        this.enterAlert = Boolean.parseBoolean(dataList.get(nowListNum));
        nowListNum++;

        this.closeAlert = Boolean.parseBoolean(dataList.get(nowListNum));
        nowListNum++;

        this.jukiAlert = Boolean.parseBoolean(dataList.get(nowListNum));
        nowListNum++;

        this.vibrationOn = Boolean.parseBoolean(dataList.get(nowListNum));
        nowListNum++;

        this.closeVolume = Integer.parseInt(dataList.get(nowListNum));
        nowListNum++;

        this.enterVolume = Integer.parseInt(dataList.get(nowListNum));
        nowListNum++;

        this.jukiVolume = Integer.parseInt(dataList.get(nowListNum));
        nowListNum++;

        this.loggingOn = Boolean.parseBoolean(dataList.get(nowListNum));
        nowListNum++;

        String time = dataList.get(nowListNum);
        StringTokenizer stringTokenizer = new StringTokenizer(time, ":");
        this.startHour = Integer.parseInt(stringTokenizer.nextToken());
        this.startMinute = Integer.parseInt(stringTokenizer.nextToken());
        nowListNum++;

        time = dataList.get(nowListNum);
        stringTokenizer = new StringTokenizer(time, ":");
        startLunchHour = Integer.parseInt(stringTokenizer.nextToken());
        startLunchMinute = Integer.parseInt(stringTokenizer.nextToken());
        nowListNum++;

        time = dataList.get(nowListNum);
        stringTokenizer = new StringTokenizer(time, ":");
        endLunchHour = Integer.parseInt(stringTokenizer.nextToken());
        endLunchMinute = Integer.parseInt(stringTokenizer.nextToken());
        nowListNum++;

        time = dataList.get(nowListNum);
        stringTokenizer = new StringTokenizer(time, ":");
        this.endHour = Integer.parseInt(stringTokenizer.nextToken());
        this.endMinute = Integer.parseInt(stringTokenizer.nextToken());
        nowListNum++;

        this.closeDistance = Integer.parseInt(dataList.get(nowListNum));
        nowListNum++;

        this.jukiDistance = Integer.parseInt(dataList.get(nowListNum));
        nowListNum++;

        this.enterLogIntvl = Integer.parseInt(dataList.get(nowListNum));
        nowListNum++;

        this.closeLogIntvl = Integer.parseInt(dataList.get(nowListNum));
        nowListNum++;

        this.semiCloseLogIntvl = Integer.parseInt(dataList.get(nowListNum));
        nowListNum++;

        this.jukiCloseLogIntvl = Integer.parseInt(dataList.get(nowListNum));
        nowListNum++;

        this.normalLogIntvl = Integer.parseInt(dataList.get(nowListNum));
        nowListNum++;

        ArrayList<String> jukiList = new ArrayList<>();
        this.jukiQty = Integer.parseInt(dataList.get(nowListNum));

        nowListNum++;
        for (int jukiNum = 1; jukiNum <= jukiQty; jukiNum++) {
            jukiList.add(dataList.get(nowListNum));
            nowListNum++;
        }
        this.jukiList = jukiList;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.userID);
        dest.writeString(this.groupID);
        dest.writeByte(this.admin ? (byte) 1 : (byte) 0);
        dest.writeInt(this.areaQty);
        dest.writeList(this.areaData);
        dest.writeByte(this.enterAlert ? (byte) 1 : (byte) 0);
        dest.writeByte(this.closeAlert ? (byte) 1 : (byte) 0);
        dest.writeByte(this.jukiAlert ? (byte) 1 : (byte) 0);
        dest.writeByte(this.vibrationOn ? (byte) 1 : (byte) 0);
        dest.writeInt(this.closeVolume);
        dest.writeInt(this.enterVolume);
        dest.writeInt(this.jukiVolume);
        dest.writeByte(this.loggingOn ? (byte) 1 : (byte) 0);
        dest.writeInt(this.startHour);
        dest.writeInt(this.startMinute);
        dest.writeInt(this.startLunchHour);
        dest.writeInt(this.startLunchMinute);
        dest.writeInt(this.endLunchHour);
        dest.writeInt(this.endLunchMinute);
        dest.writeInt(this.endHour);
        dest.writeInt(this.endMinute);
        dest.writeInt(this.closeDistance);
        dest.writeInt(this.jukiDistance);
        dest.writeInt(this.normalLogIntvl);
        dest.writeInt(this.semiCloseLogIntvl);
        dest.writeInt(this.closeLogIntvl);
        dest.writeInt(this.enterLogIntvl);
        dest.writeInt(this.jukiCloseLogIntvl);
        dest.writeInt(this.jukiQty);
        dest.writeStringList(this.jukiList);
    }

    protected ParcelableParameter(Parcel in) {
        this.userID = in.readString();
        this.groupID = in.readString();
        this.admin = in.readByte() != 0;
        this.areaQty = in.readInt();
        this.areaData = new ArrayList<ArrayList<GeoPoint>>();
        in.readList(this.areaData, ArrayList.class.getClassLoader());
        this.enterAlert = in.readByte() != 0;
        this.closeAlert = in.readByte() != 0;
        this.jukiAlert = in.readByte() != 0;
        this.vibrationOn = in.readByte() != 0;
        this.closeVolume = in.readInt();
        this.enterVolume = in.readInt();
        this.jukiVolume = in.readInt();
        this.loggingOn = in.readByte() != 0;
        this.startHour = in.readInt();
        this.startMinute = in.readInt();
        this.startLunchHour = in.readInt();
        this.startLunchMinute = in.readInt();
        this.endLunchHour = in.readInt();
        this.endLunchMinute = in.readInt();
        this.endHour = in.readInt();
        this.endMinute = in.readInt();
        this.closeDistance = in.readInt();
        this.jukiDistance = in.readInt();
        this.normalLogIntvl = in.readInt();
        this.semiCloseLogIntvl = in.readInt();
        this.closeLogIntvl = in.readInt();
        this.enterLogIntvl = in.readInt();
        this.jukiCloseLogIntvl = in.readInt();
        this.jukiQty = in.readInt();
        this.jukiList = in.createStringArrayList();
    }

    public static final Parcelable.Creator<ParcelableParameter> CREATOR = new Parcelable.Creator<ParcelableParameter>() {
        @Override
        public ParcelableParameter createFromParcel(Parcel source) {
            return new ParcelableParameter(source);
        }

        @Override
        public ParcelableParameter[] newArray(int size) {
            return new ParcelableParameter[size];
        }
    };
}
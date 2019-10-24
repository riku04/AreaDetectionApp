package com.example.ueda_r.taiseiApp;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class DataLogger {
    private ArrayList<String> logList = new ArrayList<>();
    public void addStringLine(String line) {
        logList.add(line);
    }
    public void clearData() {
        logList.clear();
    }
    public void outputCsvLog(String path, String filename) {
        int listSize = logList.size() - 1;
        try {
            FileWriter fw = new FileWriter(path + "/" + filename, false);
            PrintWriter pw = new PrintWriter(new BufferedWriter(fw));
            pw.println();
            for (int i = 0; i <= listSize; i++) {
                pw.print(logList.get(i));
                pw.println();
            }
            pw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void outPutCsvLogWithDate(String path) {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Tokyo"), Locale.JAPAN);
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        String formatDate = format.format(calendar.getTime());
        outputCsvLog(path, "debugLog_" + formatDate + ".txt");
    }
}

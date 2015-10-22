package com.chinaykl.apkinfo;

import android.app.Service;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MyService extends Service {
    private final static String TAG = "APKInfo";
    private final static String RESULT_PATH = Environment.getExternalStorageDirectory().getPath() + "/Download/appinfo.txt";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "Scan apk started");

        PackageManager pm = getPackageManager();
        List<PackageInfo> packageList = pm.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
        Log.i(TAG, packageList.size() + " packages found");

        ArrayList<Info> infoList = new ArrayList<>();
        for (int i = 0; i < packageList.size(); i++) {
            PackageInfo pi = packageList.get(i);

            Info info = new Info();
            String path = pi.applicationInfo.sourceDir;
            info.packagename = path.substring(path.lastIndexOf('/') + 1);
            info.name = pi.applicationInfo.loadLabel(pm).toString();
            info.version = pi.versionName;
            if ((pi.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM) {
                info.uninstallable = "no";
            } else {
                info.uninstallable = "yes";
            }

            infoList.add(info);
        }

        boolean result = true;
        File file = new File(RESULT_PATH);
        if (file.exists()) {
            result = file.delete();
        }
        if(result){
            Log.i(TAG, "Result File Path is " + RESULT_PATH);
        }else{
            Log.i(TAG, "Result File Path Error");
            return;
        }

        try {
            result = file.createNewFile();
        } catch (IOException e) {
            Log.i(TAG, "Result File Creat Fail");
            return;
        }
        if (result){
            Log.i(TAG, "Result File Creat Succeed");
        }else{
            Log.i(TAG, "Result File Creat Error");
            return;
        }

        FileOutputStream fos;
        try {
            fos = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            Log.i(TAG, "Result File Not Found");
            return;
        }

        for (int i = 0; i < infoList.size(); i++) {
            try {
                fos.write((infoList.get(i).packagename + ',').getBytes());
                fos.write((infoList.get(i).name + ',').getBytes());
                fos.write((infoList.get(i).version + ',').getBytes());
                fos.write((infoList.get(i).uninstallable + '\n').getBytes());
            } catch (IOException e) {
                Log.e(TAG, "Result File Write Failed");
                return;
            }

        }

        try {
            fos.flush();
            fos.close();
        } catch (IOException e) {
            Log.e(TAG, "Result File Close Failed");
            return;
        }

        Log.d(TAG, "Scan apk finished");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Scan apk ended");
    }

    private class Info {
        public String packagename;
        public String name;
        public String version;
        public String uninstallable;
    }
}

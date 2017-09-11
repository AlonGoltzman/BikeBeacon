package com.bikebeacon.background.utility;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.NeighboringCellInfo;
import android.telephony.TelephonyManager;

import org.jetbrains.annotations.Contract;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.bikebeacon.background.utility.Constants.PERMISSION_REQUEST_CODE;
import static com.bikebeacon.background.utility.Constants.SHARED_PREFERENCES_FIRST_RUN;

/**
 * Created by Alon on 8/20/2017.
 */

public final class GeneralUtility {

    private static String UUID;
    private static boolean hasPermissions = false;


    public static boolean hasExternalStorage() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) || Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED_READ_ONLY);
    }

    public static boolean canWriteToExternalStorage() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    public static File getExternalStorageDir() {
        return new File(Environment.getExternalStorageDirectory(), "/BikeBeacon");
    }

    public static boolean isFirstRun(Activity caller) {
        return !(Boolean) SharedPreferencesManager.getManager(caller).get(SHARED_PREFERENCES_FIRST_RUN);
    }

    public static boolean requestRuntimePermissions(Activity caller) {
        if (requiresRuntimePermissions()) {
            ArrayList<String> requiredPerms = new ArrayList<>(Arrays.asList(Manifest.permission.INTERNET,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.CALL_PHONE));
            ArrayList<String> perms = new ArrayList<>();
            for (String requiredPerm : requiredPerms)
                if (caller.checkSelfPermission(requiredPerm) != PackageManager.PERMISSION_GRANTED)
                    perms.add(requiredPerm);
            if (perms.size() > 0) {
                String[] values = Arrays.copyOf(perms.toArray(), perms.size(), String[].class);
                caller.requestPermissions(values, PERMISSION_REQUEST_CODE);
                setHasPermissions(false);
                return true;
            }
        }
        setHasPermissions(true);
        return false;
    }

    public static boolean hasPermissions() {
        return hasPermissions;
    }

    public static void setHasPermissions(boolean flag) {
        hasPermissions = flag;
    }


    @Contract("null->fail")
    public static void setUUID(String uuid) {
        if (uuid != null && !uuid.isEmpty())
            if (UUID != null && !UUID.isEmpty()) {
                if (!UUID.equals(uuid))
                    UUID = uuid;
            } else
                UUID = uuid;
        else
            throw new NullPointerException("UUID given is either null or empty.");
    }

    public static String getCellInfo(Context context) {
        TelephonyManager tel = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        ArrayList<String> ids = new ArrayList<>();

        //from Android M up must use getAllCellInfo
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            List<NeighboringCellInfo> neighCells = tel.getNeighboringCellInfo();
            for (int i = 0; i < neighCells.size(); i++) {
                try {
                    NeighboringCellInfo thisCell = neighCells.get(i);
                    ids.add(String.valueOf(thisCell.getCid()));
                } catch (Exception ignored) {
                }
            }

        } else {
            List<CellInfo> infos = tel.getAllCellInfo();
            for (int i = 0; i < infos.size(); ++i) {
                try {
                    CellInfo info = infos.get(i);
                    if (info instanceof CellInfoGsm) {
                        CellIdentityGsm identityGsm = ((CellInfoGsm) info).getCellIdentity();
                        ids.add(String.valueOf(identityGsm.getCid()));
                    } else if (info instanceof CellInfoLte) {
                        CellIdentityLte identityLte = ((CellInfoLte) info).getCellIdentity();
                        ids.add(String.valueOf(identityLte.getCi()));
                    }
                } catch (Exception ignored) {
                }
            }
        }
        return ids.toString().replace("[", "").replace("]", "");
    }

    private static boolean requiresRuntimePermissions() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }
}

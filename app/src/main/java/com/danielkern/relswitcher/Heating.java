package com.danielkern.relswitcher;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import com.google.gson.Gson;

import java.io.IOException;
import java.net.URI;

/**
 * Created by Daniel Kern on 03/01/2018.
 */

public class Heating extends Fragment {
    OkHttpClient client;
    Button BtnHOFF, BtnHON, BtnHST, HtimeB;
    TextView textsView, tempLabel;
    Spinner devicesS;
    Device device1, device2, device3, currentD;
    boolean h, w;
    Intent notificationIntent;
    Context mContext;
    Activity mActivity;
    PendingIntent intent;
    String[] devicesN;
    String number;
    SharedPreferences devicesPref;
    View waterView;
    Gson gson;
    String device1json, device2json, device3json;

    Common common;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.heating, container, false);
        waterView = inflater.inflate(R.layout.water, container, false);

        mContext = requireContext();
        mActivity = requireActivity();

        common = Common.getInstance(mActivity, waterView, view);

        devicesPref = mActivity.getSharedPreferences("devices", Context.MODE_PRIVATE);
        number = "07xxx xxxxxx";

        BtnHOFF = view.findViewById(R.id.Hoff);
        BtnHON = view.findViewById(R.id.Hon);
        BtnHST = view.findViewById(R.id.Hstatus);
        textsView = view.findViewById(R.id.texts1);
        HtimeB = view.findViewById(R.id.HtimeB);
        devicesS = view.findViewById(R.id.devices);
        tempLabel = view.findViewById(R.id.tempLabel);
        BtnHOFF.setOnClickListener(view1 -> common.sendMsg("#REL2=OFF", currentD));
        BtnHON.setOnClickListener(view2 -> common.sendMsg("#REL2=ON", currentD));
        BtnHST.setOnClickListener(view3 -> common.sendMsg("#STATUS", currentD));

        HtimeB.setOnClickListener(view4 -> common.sendMsg("#REL1=ON#" + "60", currentD));
        notificationIntent = new Intent(mContext, Heating.class);

        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        intent = PendingIntent.getActivity(getContext(), 0,
                notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        common.h1_w1 = new NotificationCompat.Builder(mContext, "13")
                .setSmallIcon(R.mipmap.icon)
                .setContentTitle("RelSwitcher")
                .setContentText("Heating ON & Water ON")
                .setContentIntent(intent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        //H-OFF, W-ON
        common.h0_w1 = new NotificationCompat.Builder(mContext, "13")
                .setSmallIcon(R.mipmap.icon)
                .setContentTitle("RelSwitcher")
                .setContentText("Heating OFF & Water ON")
                .setContentIntent(intent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        //H-ON, W-OFF
        common.h1_w0 = new NotificationCompat.Builder(mContext, "13")
                .setSmallIcon(R.mipmap.icon)
                .setContentTitle("RelSwitcher")
                .setContentText("Heating ON & Water OFF")
                .setContentIntent(intent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        //H-OFF, W-OFF
        common.h0_w0 = new NotificationCompat.Builder(mContext, "13")
                .setSmallIcon(R.mipmap.icon)
                .setContentTitle("RelSwitcher")
                .setContentText("Heating OFF & Water OFF")
                .setContentIntent(intent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        devicesS.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        currentD = device1;
                        number = currentD.number;
                        break;
                    case 1:
                        currentD = device2;
                        number = currentD.number;
                        break;
                    case 2:
                        currentD = device3;
                        number = currentD.number;
                        break;
                    default:
                        currentD = device1;
                        number = currentD.number;
                        break;
                }

            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                currentD = device1;
            }
        });
        gson = new Gson();
        device1json = devicesPref.getString("device1", "");
        device2json = devicesPref.getString("device2", "");
        device3json = devicesPref.getString("device3", "");
        try {
            common.getTemp(devicesPref, tempLabel);
        } catch (IOException e) {
            Log.e("RS-Heating", "onCreateView: ", e);
        }
        if (!device1json.isEmpty() && !device2json.isEmpty() && !device3json.isEmpty()) {
            device1 = gson.fromJson(device1json, Device.class);
            device2 = gson.fromJson(device2json, Device.class);
            device3 = gson.fromJson(device3json, Device.class);

            devicesN = new String[3];
            devicesN[0] = device1.name;
            devicesN[1] = device2.name;
            devicesN[2] = device3.name;

            ArrayAdapter<String> adapter = getStringArrayAdapter();
            devicesS.setAdapter(adapter);
        }
        return view;
    }

    private ArrayAdapter<String> getStringArrayAdapter() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(mActivity.getApplicationContext(), R.layout.spinnerlayout, devicesN) {
            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView textview = (TextView) view;
                switch (position) {
                    case 0:
                        textview.setTextColor(device1.enabled ? Color.BLACK : Color.GRAY);
                        break;
                    case 1:
                        textview.setTextColor(device2.enabled ? Color.BLACK : Color.GRAY);
                        break;
                    case 2:
                        textview.setTextColor(device3.enabled ? Color.BLACK : Color.GRAY);
                        break;
                    default:
                        textview.setTextColor(Color.BLACK);
                }
                return view;
            }

            @Override
            public boolean isEnabled(int position) {
                return switch (position) {
                    case 0 -> device1.enabled;
                    case 1 -> device2.enabled;
                    case 2 -> device3.enabled;
                    default -> false;
                };
            }
        };
        adapter.notifyDataSetChanged();
        return adapter;
    }

    public BroadcastReceiver smsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            SmsMessage[] msgs;
            String num = null, msg = null;
            if (bundle != null) {
                Object[] pdus = (Object[]) bundle.get("pdus");
                if (pdus != null) {
                    msgs = new SmsMessage[pdus.length];
                    for (int i = 0; i < msgs.length; i++) {
                        msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i], "3gpp");
                        num = msgs[i].getOriginatingAddress();
                        msg = msgs[i].getMessageBody();
                    }
                    common.setMsg(msg, num, number, textsView);
                }
            }
        }
    };

    @Override
    public void onDestroy() throws IllegalArgumentException {
        mContext.unregisterReceiver(smsReceiver);
        super.onDestroy();
    }

    @Override
    public void onResume() {
        gson = new Gson();
        device1json = devicesPref.getString("device1", "");
        device2json = devicesPref.getString("device2", "");
        device3json = devicesPref.getString("device3", "");
        device1 = gson.fromJson(device1json, Device.class);
        device2 = gson.fromJson(device2json, Device.class);
        device3 = gson.fromJson(device3json, Device.class);

        mContext.registerReceiver(smsReceiver, new IntentFilter("android.provider.Telephony.SMS_RECEIVED"));
        super.onResume();
    }
}




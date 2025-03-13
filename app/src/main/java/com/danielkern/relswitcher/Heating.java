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
    NotificationManagerCompat notificationManager;
    public NotificationCompat.Builder h1_w1, h1_w0, h0_w1, h0_w0;
    View waterView;
    Gson gson;
    String device1json, device2json, device3json;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.heating, container, false);
        waterView = inflater.inflate(R.layout.water, container, false);

        client = new OkHttpClient();

        mContext = requireContext();
        mActivity = requireActivity();

        devicesPref = mActivity.getSharedPreferences("devices", Context.MODE_PRIVATE);
        number = "07xxx xxxxxx";

        notificationManager = NotificationManagerCompat.from(mContext);
        BtnHOFF = view.findViewById(R.id.Hoff);
        BtnHON = view.findViewById(R.id.Hon);
        BtnHST = view.findViewById(R.id.Hstatus);
        textsView = view.findViewById(R.id.texts1);
        HtimeB = view.findViewById(R.id.HtimeB);
        devicesS = view.findViewById(R.id.devices);
        tempLabel = view.findViewById(R.id.tempLabel);
        BtnHOFF.setOnClickListener(view1 -> sendMsg("#REL2=OFF", currentD));
        BtnHON.setOnClickListener(view2 -> sendMsg("#REL2=ON", currentD));
        BtnHST.setOnClickListener(view3 -> sendMsg("#STATUS", currentD));

        HtimeB.setOnClickListener(view4 -> sendMsg("#REL1=ON#" + "60", currentD));
        notificationIntent = new Intent(mContext, Heating.class);

        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        intent = PendingIntent.getActivity(getContext(), 0,
                notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        //H-On, W-ON
        h1_w1 = new NotificationCompat.Builder(mContext, "13")
                .setSmallIcon(R.mipmap.icon)
                .setContentTitle("RelSwitcher")
                .setContentText("Heating ON & Water ON")
                .setContentIntent(intent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        //H-OFF, W-ON
        h0_w1 = new NotificationCompat.Builder(mContext, "13")
                .setSmallIcon(R.mipmap.icon)
                .setContentTitle("RelSwitcher")
                .setContentText("Heating OFF & Water ON")
                .setContentIntent(intent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        //H-ON, W-OFF
        h1_w0 = new NotificationCompat.Builder(mContext, "13")
                .setSmallIcon(R.mipmap.icon)
                .setContentTitle("RelSwitcher")
                .setContentText("Heating ON & Water OFF")
                .setContentIntent(intent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        //H-OFF, W-OFF
        h0_w0 = new NotificationCompat.Builder(mContext, "13")
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
            getTemp();
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

    public void setMsg(String msg, String num, TextView textsView) {
        if (textsView != null) {
            if (num.equals(number) || num.equals(convNum(number))) {
                if (msg.contains("REL 1 OFF")) {
                    w=false;
                } else if (msg.contains("REL 1 ON")) {
                    w=true;
                }
                if (msg.contains("REL 2 OFF")) {
                    h=false;
                } else if (msg.contains("REL 2 ON")) {
                    h=true;
                }
                sendNotification(h, w);
                if (h && w) {
                    textsView.setText(R.string.hON_wON);
                    ((TextView) waterView.findViewById(R.id.texts1)).setText(R.string.hON_wON);
                } else if (h) {
                    textsView.setText(R.string.hON_wOFF);
                    ((TextView) waterView.findViewById(R.id.texts1)).setText(R.string.hON_wOFF);
                } else if (w) {
                    textsView.setText(R.string.hOFF_wON);
                    ((TextView) waterView.findViewById(R.id.texts1)).setText(R.string.hOFF_wON);
                } else {
                    textsView.setText(R.string.hOFF_wOFF);
                    ((TextView) waterView.findViewById(R.id.texts1)).setText(R.string.hOFF_wOFF);
                }
            }
        }
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
                    setMsg(msg, num, textsView);
                }
            }
        }
    };

    void get(String url, Callback callback) {
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        Call call = client.newCall(request);
        call.enqueue(callback);
    }

    public String convNum(String num) {
        char[] numA = num.toCharArray();
        char[] numB = new char[13];
        numB[0] = '+';
        numB[1] = '4';
        numB[2] = '4';
        System.arraycopy(numA, 1, numB, 3, 10);
        return new String(numB);
    }

    public void sendMsg(String msg, Device device) {
        if (device == null || device.number == null || device.number.equals("07xxx xxxxxx")) {
            Toast.makeText(mContext, "No device selected, or number not set!", Toast.LENGTH_LONG).show();
            return;
        }
        try {
            SmsManager smsManager = mContext.getSystemService(SmsManager.class).createForSubscriptionId(SmsManager.getDefaultSmsSubscriptionId());
            smsManager.sendTextMessage(device.number, null, msg, null, null);
            Toast.makeText(mContext, "SMS Sent!",
                    Toast.LENGTH_LONG).show();
            getTemp();
        } catch (Exception e) {
            Toast.makeText(mContext,
                    "SMS failed, grant permission!",
                    Toast.LENGTH_LONG).show();
        }
    }

    public void sendNotification(boolean h, boolean w) {
        if (ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(mActivity, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1);
            return;
        }
        if (h && w)
            notificationManager.notify(13, h1_w1.build());
        else if (!h && w) notificationManager.notify(13, h0_w1.build());
        else if (h) notificationManager.notify(13, h1_w0.build());
        else notificationManager.notify(13, h0_w0.build());
    }

    public void getTemp() throws IOException {
        String ip = devicesPref.getString("netIP", "1.1.1.1");
        if (tempLabel != null && !ip.equals("1.1.1.1")) {
            String url = new HttpUrl.Builder()
                    .scheme("http")
                    .host(ip)
                    .addPathSegment("temp")
                    .build().toString();
            get(url, new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.e("temp", "onFailure: " + e);
                }
                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.isSuccessful()) {
                        assert response.body() != null;
                        String resp = response.body().string();
                        requireActivity().runOnUiThread(() -> {
                            tempLabel.setText("Temperature: " + resp);
                            Log.d("temp", "onResponse: " + resp);
                        });
                    }
                }
            });
        }
    }

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




package com.danielkern.relswitcher;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Common {

    View waterView, heatingView;
    Activity activity;
    static Common instance;

    public NotificationCompat.Builder h1_w1, h1_w0, h0_w1, h0_w0;

    static Common getInstance(Activity activity, View waterView, View heatingView) {
        if (instance == null) {
            instance = new Common();
            instance.activity = activity;
            instance.waterView = waterView;
            instance.heatingView = heatingView;
        }
        return instance;
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
    public void setMsg(String msg, String num, String number, TextView textsView) {
        boolean w,h;
        if (textsView != null) {
            if (num.equals(number) || num.equals(convNum(number))) {
                if (msg.contains("REL 1 OFF")) {
                    w=false;
                } else if (msg.contains("REL 1 ON")) {
                    w=true;
                } else {
                    Log.e("RelSwitcher", "setMsg: bad sms received");
                    return;
                }
                if (msg.contains("REL 2 OFF")) {
                    h=false;
                } else if (msg.contains("REL 2 ON")) {
                    h=true;
                } else {
                    Log.e("RelSwitcher", "setMsg: bad sms received");
                    return;
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

    public void sendMsg(String msg, Device device) {
        if (device == null || device.number == null || device.number.equals("07xxx xxxxxx")) {
            Toast.makeText(activity, "No device selected, or number not set!", Toast.LENGTH_LONG).show();
            return;
        }
        try {
            SmsManager smsManager = activity.getSystemService(SmsManager.class).createForSubscriptionId(SmsManager.getDefaultSmsSubscriptionId());
            smsManager.sendTextMessage(device.number, null, msg, null, null);
            Toast.makeText(activity, "SMS Sent!",
                    Toast.LENGTH_LONG).show();
            getTemp(activity.getSharedPreferences("devices", Context.MODE_PRIVATE), heatingView.findViewById(R.id.tempLabel));
        } catch (Exception e) {
            Toast.makeText(activity,
                    "SMS failed, grant permission!",
                    Toast.LENGTH_LONG).show();
        }
    }

    public void sendNotification(boolean h, boolean w) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(activity);
        if (ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1);
            return;
        }
        if (h && w)
            notificationManager.notify(13, h1_w1.build());
        else if (!h && w) notificationManager.notify(13, h0_w1.build());
        else if (h) notificationManager.notify(13, h1_w0.build());
        else notificationManager.notify(13, h0_w0.build());
    }

    public void getTemp(SharedPreferences devicesPref, TextView tempLabel) throws IOException {
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
                        activity.runOnUiThread(() -> {
                            tempLabel.setText(activity.getString(R.string.temperature) + resp);
                            Log.d("temp", "onResponse: " + resp);
                        });
                    }
                }
            }, new OkHttpClient());
        }
    }
    void get(String url, Callback callback, OkHttpClient client) {
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        Call call = client.newCall(request);
        call.enqueue(callback);
    }
}

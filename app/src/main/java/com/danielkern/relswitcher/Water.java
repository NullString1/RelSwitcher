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
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

import com.google.gson.Gson;

/**
 * Created by Daniel Kern on 03/01/2018.
 */

public class Water extends Fragment {
    Button BtnWOFF, BtnWON, BtnWST;
    TextView textsView;
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
    View heatingView;
    Gson gson;
    String device1json, device2json, device3json;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.water, container, false);
        heatingView = inflater.inflate(R.layout.heating, container, false);

        mContext = requireContext();
        mActivity = requireActivity();

        devicesPref = mActivity.getSharedPreferences("devices", Context.MODE_PRIVATE);
        number = "07xxx xxxxxx";

        BtnWOFF = view.findViewById(R.id.Woff);
        BtnWON = view.findViewById(R.id.Won);
        BtnWST = view.findViewById(R.id.Wstatus);
        textsView = view.findViewById(R.id.texts1);
        devicesS = view.findViewById(R.id.devices);

        notificationManager = NotificationManagerCompat.from(mContext);

        BtnWOFF.setOnClickListener(view2 -> sendMsg("#REL1=OFF", currentD));
        BtnWON.setOnClickListener(view3 -> sendMsg("#REL1=ON", currentD));
        BtnWST.setOnClickListener(view4 -> sendMsg("#STATUS", currentD));

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
        if (ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(mContext, "SMS failed, grant permission!", Toast.LENGTH_LONG).show();
            return;
        }
       if (textsView != null) {
           if (num.equals(number) || num.equals(convNum(number))) {                //works
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
                    ((TextView) heatingView.findViewById(R.id.texts1)).setText(R.string.hON_wON);
                } else if (h) {
                    textsView.setText(R.string.hON_wOFF);
                    ((TextView) heatingView.findViewById(R.id.texts1)).setText(R.string.hON_wOFF);
                } else if (w) {
                    textsView.setText(R.string.hOFF_wON);
                    ((TextView) heatingView.findViewById(R.id.texts1)).setText(R.string.hOFF_wON);
                } else {
                    textsView.setText(R.string.hOFF_wOFF);
                    ((TextView) heatingView.findViewById(R.id.texts1)).setText(R.string.hOFF_wOFF);
                }
            }
        }
    }

    private final BroadcastReceiver smsReceiver = new BroadcastReceiver() {
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

    public void sendNotification(boolean h, boolean w) {
        if (ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(mContext, "Notification failed, grant permission!", Toast.LENGTH_LONG).show();
            ActivityCompat.requestPermissions(mActivity, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1);
            return;
        }

        if (h && w)
            notificationManager.notify(13, h1_w1.build());
        else if (!h && w) notificationManager.notify(13, h0_w1.build());
        else if (h) notificationManager.notify(13, h1_w0.build());
        else notificationManager.notify(13, h0_w0.build());
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
        } catch (Exception e) {
            Toast.makeText(mContext,
                    "SMS failed, grant permission!",
                    Toast.LENGTH_LONG).show();
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
        device2 = gson.fromJson(device2json, Device.class);
        device3 = gson.fromJson(device3json, Device.class);
        device1 = gson.fromJson(device1json, Device.class);

        mContext.registerReceiver(smsReceiver, new IntentFilter("android.provider.Telephony.SMS_RECEIVED"));
        super.onResume();
    }
}

package com.danielkern.relswitcher;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
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

import java.util.Objects;


/**
 * Created by Daniel Kern on 03/01/2018.
 */

public class heating extends Fragment {
    Button BtnHOFF, BtnHON, BtnHST, HtimeB;
    TextView textsView;
    Spinner devicesS;
    settings.device device1, device2, device3, currentD;
    boolean h, w;
    Intent notificationIntent;
    Context mContext;
    Activity activity;
    PendingIntent intent;
    String[] devicesN;
    String number;
    SharedPreferences devicesPref;
    NotificationManagerCompat notificationManager;
    public NotificationCompat.Builder h1_w1, h1_w0, h0_w1, h0_w0;
    View view2;
    Gson gson;
    String device1json, device2json, device3json;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mContext = getContext();
        activity = getActivity();
        View view = inflater.inflate(R.layout.heating, container, false);
        view2 = inflater.inflate(R.layout.water, container, false);
        devicesPref = activity.getSharedPreferences("devices", Context.MODE_PRIVATE);
        number = "07xxx xxxxxx";
        notificationManager = NotificationManagerCompat.from(mContext);
        BtnHOFF = view.findViewById(R.id.Hoff);
        BtnHON = view.findViewById(R.id.Hon);
        BtnHST = view.findViewById(R.id.Hstatus);
        textsView = view.findViewById(R.id.texts1);
        HtimeB = view.findViewById(R.id.HtimeB);
        devicesS = view.findViewById(R.id.devices);
        BtnHOFF.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                sendMsg("#REL2=OFF", currentD.number);
            }
        });
        BtnHON.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                sendMsg("#REL2=ON", currentD.number);
            }
        });
        BtnHST.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                sendMsg("#STATUS", currentD.number);
            }
        });

        HtimeB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMsg("#REL1=ON#"+"60", currentD.number);
            }
        });
        notificationIntent = new Intent(getContext(), heating.class);

        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        intent = PendingIntent.getActivity(getContext(), 0,
                notificationIntent, 0);

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
                Log.d("Spinner: ", "onItemSelected: Parent: " + parent + "; View: " + view + "; Position: " + position + "; Id: " + id);

                switch (position){
                    case 0:
                        currentD = device1;
                        number = currentD.number;
                        Log.d("dev1", "onItemSelected: error in dev 1");
                        break;
                    case 1:
                        currentD = device2;
                        number = currentD.number;
                        Log.d("dev2", "onItemSelected: error in dev 2");
                        break;
                    case 2:
                        currentD = device3;
                        number = currentD.number;
                        Log.d("dev3", "onItemSelected: error in dev 3");
                        break;
                    default:
                        currentD = device1;
                        number = currentD.number;
                        Log.d("dev0", "onItemSelected: error in dev 0");
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

        if(!device1json.equals("") && !device2json.equals("") && !device3json.equals("")){
            device1 = gson.fromJson(device1json, settings.device.class);
            device2 = gson.fromJson(device2json, settings.device.class);
            device3 = gson.fromJson(device3json, settings.device.class);



            devicesN = new String[3];
            devicesN[0] = device1.name;
            devicesN[1] = device2.name;
            devicesN[2] = device3.name;

            Log.d("Spinner", "onCreateView: Set Spinner: devices: " + device1 + device2 + device3);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(activity.getApplicationContext(), R.layout.spinnerlayout, devicesN) {
                @Override
                public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                    View view = super.getDropDownView(position, convertView, parent);
                    TextView textview = (TextView) view;
                    switch (position) {
                        case 0:
                            if(device1.enabled) textview.setTextColor(Color.BLACK); else textview.setTextColor(Color.GRAY);
                            break;
                        case 1:
                            if(device2.enabled) textview.setTextColor(Color.BLACK); else textview.setTextColor(Color.GRAY);
                            break;
                        case 2:
                            if(device3.enabled) textview.setTextColor(Color.BLACK); else textview.setTextColor(Color.GRAY);
                            break;
                        default:
                            textview.setTextColor(Color.BLACK);
                    }
                    return view;
                }

                @Override
                public boolean isEnabled(int position) {
                    boolean enabled;
                    switch (position) {
                        case 0:
                            enabled = device1.enabled;
                            break;
                        case 1:
                            enabled = device2.enabled;
                            break;
                        case 2:
                            enabled = device3.enabled;
                            break;
                        default:
                            enabled = false;
                    }
                    return enabled;
                }
            };
            adapter.notifyDataSetChanged();
            devicesS.setAdapter(adapter);
            Log.d("Spinner: ", "onCreateView: Spinner set");

        }
        return view;
    }

    public void setMsg(String msg, String num, TextView textsView) {
        Log.d("SetMsg: ", "setMsg: msg = " + msg + " num = " + num);
        Log.d("texts view", "setMsg: is textsview existing? : " + textsView);
        if (textsView != null) {

            Log.d("setMSG", "setMsg: Function called" + msg);
            if (num.equals(number) || num.equals(convNum(number))) {
                Log.d("setmsg: ", "setMsg: num checks out");
                switch (msg) {
                    case "REL 1 OFF==REL 2 OFF":
                        Log.d("Parse: ", "setMsg: REL 1 OFF==REL 2 OFF");
                        h = false;
                        w = false;
                        break;
                    case "REL 1 ON==REL 2 OFF":
                        Log.d("Parse: ", "setMsg: REL 1 ON==REL 2 OFF");
                        h = false;
                        w = true;
                        break;
                    case "REL 1 OFF==REL 2 ON":
                        Log.d("Parse: ", "setMsg: REL 1 OFF==REL 2 ON");
                        h = true;
                        w = false;
                        break;
                    case "REL 1 ON==REL 2 ON":
                        Log.d("Parse: ", "setMsg: REL 1 ON==REL 2 ON");
                        h = true;
                        w = true;
                        break;
                    default:
                        Toast.makeText(Objects.requireNonNull(getContext()).getApplicationContext(),
                                "Check Number! Incorrect Message Recieved!",
                                Toast.LENGTH_LONG).show();
                        break;
                }
                sendNotification(h, w);
                if(h && w) {
                    textsView.setText(R.string.hON_wON);
                    ((TextView)view2.findViewById(R.id.texts1)).setText(R.string.hON_wON);
                } else if (h && !w) {
                    textsView.setText(R.string.hON_wOFF);
                    ((TextView)view2.findViewById(R.id.texts1)).setText(R.string.hON_wOFF);
                } else if (!h && w) {
                    textsView.setText(R.string.hOFF_wON);
                    ((TextView)view2.findViewById(R.id.texts1)).setText(R.string.hOFF_wON);
                } else if (!h && !w) {
                    textsView.setText(R.string.hOFF_wOFF);
                    ((TextView)view2.findViewById(R.id.texts1)).setText(R.string.hOFF_wOFF);
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
                //---retrieve the SMS message received---
                Object[] pdus = (Object[]) bundle.get("pdus");
                if(pdus != null) {
                    msgs = new SmsMessage[pdus.length];
                    for (int i = 0; i < msgs.length; i++) {
                        msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                        num = msgs[i].getOriginatingAddress();
                        msg = msgs[i].getMessageBody();
                    }
                    setMsg(msg, num, textsView);
                }
            }
        }
    };

    public String convNum(String num){
        char[] numA = num.toCharArray();
        char[] numB = new char[13];
        numB[0]='+';
        numB[1]='4';
        numB[2]='4';
        System.arraycopy(numA, 1, numB, 3, 10);
        return new String(numB);
    }
    public void sendMsg(String msg, String num) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(num, null, msg, null, null);
            Toast.makeText(Objects.requireNonNull(getActivity()).getApplicationContext(), "SMS Sent!",
                    Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(Objects.requireNonNull(getActivity()).getApplicationContext(),
                    "SMS failed, grant permission!",
                    Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    public void sendNotification(boolean h, boolean w){
        if(h && w) notificationManager.notify(13, h1_w1.build());
        else if(!h && w) notificationManager.notify(13, h0_w1.build());
        else if(h) notificationManager.notify(13, h1_w0.build());
        else notificationManager.notify(13, h0_w0.build());
    }


    @Override
    public void onDestroy() throws IllegalArgumentException {
        Objects.requireNonNull(getContext()).unregisterReceiver(smsReceiver);
        super.onDestroy();
    }

    @Override
    public void onResume() {
        gson = new Gson();
        device1json = devicesPref.getString("device1", "");
        device2json = devicesPref.getString("device2", "");
        device3json = devicesPref.getString("device3", "");
        device1 = gson.fromJson(device1json, settings.device.class);
        device2 = gson.fromJson(device2json, settings.device.class);
        device3 = gson.fromJson(device3json, settings.device.class);

        Objects.requireNonNull(getContext()).registerReceiver(smsReceiver, new IntentFilter("android.provider.Telephony.SMS_RECEIVED"));
        super.onResume();
    }
}




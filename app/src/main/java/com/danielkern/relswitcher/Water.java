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
    Settings.device device1,device2,device3,currentD;
    boolean h,w;
    Intent notificationIntent;
    Context mContext;
    Activity mActivity;
    PendingIntent intent;
    String[] devicesN;
    String number;
    SharedPreferences devicesPref;
    NotificationManagerCompat notificationManager;
    public NotificationCompat.Builder h1_w1, h1_w0, h0_w1, h0_w0;
    View view1;
    Gson gson;
    String device1json, device2json, device3json;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mContext = requireContext();
        mActivity = requireActivity();

        View view = inflater.inflate(R.layout.water, container, false);
        view1 = inflater.inflate(R.layout.heating, container, false);

        devicesPref = mActivity.getSharedPreferences("devices", Context.MODE_PRIVATE);
        number = "07xxx xxxxxx";

        BtnWOFF = view.findViewById(R.id.Woff);
        BtnWON = view.findViewById(R.id.Won);
        BtnWST = view.findViewById(R.id.Wstatus);
        textsView = view.findViewById(R.id.texts1);
        devicesS = view.findViewById(R.id.devices);

        notificationManager = NotificationManagerCompat.from(mContext);

        BtnWOFF.setOnClickListener(view2 -> sendMsg("#REL1=OFF", currentD.number));
        BtnWON.setOnClickListener(view3 -> sendMsg("#REL1=ON", currentD.number));
        BtnWST.setOnClickListener(view4 -> sendMsg("#STATUS", currentD.number));

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

        if(!device1json.isEmpty() && !device2json.isEmpty() && !device3json.isEmpty()){
            device1 = gson.fromJson(device1json, Settings.device.class);
            device2 = gson.fromJson(device2json, Settings.device.class);
            device3 = gson.fromJson(device3json, Settings.device.class);

            devicesN = new String[3];
            devicesN[0] = device1.name;
            devicesN[1] = device2.name;
            devicesN[2] = device3.name;

            Log.d("Spinner", "onCreateView: Set Spinner: devices: " + device1 + device2 + device3);
            ArrayAdapter<String> adapter = getStringArrayAdapter();
            devicesS.setAdapter(adapter);
            Log.d("Spinner: ", "onCreateView: Spinner set");
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
        Log.d("SetMsg: ", "setMsg: msg = " + msg + " num = " + num); // works
        Log.d("texts view", "setMsg: is textsview existing? : " + textsView); //works
        if (textsView != null) {                                                        //works
            //Convert MSG                                                               //works

            Log.d("setMSG", "setMsg: Function called" + msg);
            Log.d("numcheck: ", "setMsg: num: " + num + " convNum: " + convNum(number));//works
            if (num.equals(number) || num.equals(convNum(number))){                //works
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
                        Toast.makeText(requireContext().getApplicationContext(),
                                "Check Number! Incorrect Message Recieved!",
                                Toast.LENGTH_LONG).show();
                        break;
                }
                sendNotification(h, w);
                if(h && w) {
                    textsView.setText(R.string.hON_wON);
                    ((TextView)view1.findViewById(R.id.texts1)).setText(R.string.hON_wON);
                } else if (h) {
                    textsView.setText(R.string.hON_wOFF);
                    ((TextView)view1.findViewById(R.id.texts1)).setText(R.string.hON_wOFF);
                } else if (w) {
                    textsView.setText(R.string.hOFF_wON);
                    ((TextView)view1.findViewById(R.id.texts1)).setText(R.string.hOFF_wON);
                } else {
                    textsView.setText(R.string.hOFF_wOFF);
                    ((TextView)view1.findViewById(R.id.texts1)).setText(R.string.hOFF_wOFF);
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
                //---retrieve the SMS message received---
                Object[] pdus = (Object[]) bundle.get("pdus");
                if(pdus != null) {
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

    public void sendNotification(boolean h, boolean w){
        if (ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        if(h && w)
            notificationManager.notify(13, h1_w1.build());
        else if(!h && w) notificationManager.notify(13, h0_w1.build());
        else if(h) notificationManager.notify(13, h1_w0.build());
        else notificationManager.notify(13, h0_w0.build());
    }
    public String convNum(String num){
        char[] numA = num.toCharArray();
        char[] numB = new char[13];
        numB[0]='+';
        numB[1]='4';
        numB[2]='4';
        System.arraycopy(numA, 1, numB, 3, 10);
        return new String(numB);
    }
    public void sendMsg(String msg, String num){
        try {
            SmsManager smsManager = mContext.getSystemService(SmsManager.class).createForSubscriptionId(SmsManager.getDefaultSmsSubscriptionId());
            smsManager.sendTextMessage(num, null, msg, null, null);
            Toast.makeText(mContext, "SMS Sent!",
                    Toast.LENGTH_LONG).show();
            } catch (Exception e) {
            Toast.makeText(mContext,
                    "SMS failed, grant permission!",
                    Toast.LENGTH_LONG).show();
            Log.e("RS-Water", "sendMsg: ", e);
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
        device1 = gson.fromJson(device1json, Settings.device.class);
        device2 = gson.fromJson(device2json, Settings.device.class);
        device3 = gson.fromJson(device3json, Settings.device.class);

        mContext.registerReceiver(smsReceiver, new IntentFilter("android.provider.Telephony.SMS_RECEIVED"));
        super.onResume();
    }}

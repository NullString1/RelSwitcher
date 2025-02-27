package com.danielkern.relswitcher;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;

public class Settings extends AppCompatActivity {
    CheckBox dev1,dev2,dev3;
    TextView num1,num2,num3;
    TextView name1,name2,name3;
    Button save;
    device device1, device2, device3, currentD;
    SharedPreferences devicesPref;
    Gson gson = new Gson();
    String dev1json, dev2json, dev3json;
    EditText netIP;

    static class device {
        boolean enabled;
        String name;
        String number;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        devicesPref = getSharedPreferences("devices", MODE_PRIVATE);
        save=findViewById(R.id.saveB);

        dev1=findViewById(R.id.dev1);
        name1=findViewById(R.id.dev1Name);
        num1=findViewById(R.id.dev1Num);
        //
        dev2=findViewById(R.id.dev2);
        name2=findViewById(R.id.dev2Name);
        num2=findViewById(R.id.dev2Num);
        //
        dev3=findViewById(R.id.dev3);
        name3=findViewById(R.id.dev3Name);
        num3=findViewById(R.id.dev3Num);

        netIP=findViewById(R.id.netIP);

        device1 = new device();
        device2 = new device();
        device3 = new device();
        currentD = new device();

        Log.d("settings ", "onCreate: error in settings file");

        device1.enabled = dev1.isChecked();
        device1.name = name1.getText().toString();
        device1.number = num1.getText().toString();

        device2.enabled = dev2.isChecked();
        device2.name = name2.getText().toString();
        device2.number = num2.getText().toString();

        device3.enabled = dev3.isChecked();
        device3.name = name3.getText().toString();
        device3.number = num3.getText().toString();
        LoadOptions();


        dev1.setOnCheckedChangeListener((buttonView, isChecked) -> {
            device1.enabled = dev1.isChecked();
            device1.name = name1.getText().toString();
            device1.number = num1.getText().toString();
            dev1json = gson.toJson(device1);
            SetPref("device1", dev1json);
        });

        dev2.setOnCheckedChangeListener((buttonView, isChecked) -> {
            device2.enabled = dev2.isChecked();
            device2.name = name2.getText().toString();
            device2.number = num2.getText().toString();
            dev2json = gson.toJson(device2);
            SetPref("device2", dev2json);
        });

        dev3.setOnCheckedChangeListener((buttonView, isChecked) -> {
            device3.enabled = dev3.isChecked();
            device3.name = name3.getText().toString();
            device3.number = num3.getText().toString();
            dev3json = gson.toJson(device3);
            SetPref("device3", dev3json);
        });

        save.setOnClickListener(v -> {
            device1.enabled = dev1.isChecked();
            device1.name = name1.getText().toString();
            device1.number = num1.getText().toString();

            device2.enabled = dev2.isChecked();
            device2.name = name2.getText().toString();
            device2.number = num2.getText().toString();

            device3.enabled = dev3.isChecked();
            device3.name = name3.getText().toString();
            device3.number = num3.getText().toString();

            dev1json = gson.toJson(device1);
            dev2json = gson.toJson(device2);
            dev3json = gson.toJson(device3);
            SetPref("device1", dev1json);
            SetPref("device2", dev2json);
            SetPref("device3", dev3json);
            SetPref("netIP", netIP.getText().toString());

        });
    }

    void SetPref(String key, String value) {
        devicesPref.edit().putString(key, value).apply();
    }
    String GetName(String key, String def) {
        String temp = devicesPref.getString(key, def);
        if(!temp.equals(def)) {
            return gson.fromJson(temp, device.class).name;
        } else {
            return "Unknown";
        }
    }
    String GetNum(String key) {
        String temp = devicesPref.getString(key, "07xxx xxxxxx");
        if(!temp.equals("07xxx xxxxxx")) {
            return gson.fromJson(temp, device.class).number;
        } else {
            return "07xxx xxxxxx";
        }
    }
    Boolean GetEn(String key, Boolean def) {
        String temp = devicesPref.getString(key, def.toString());
        if(!temp.equals(def.toString())) {
            return gson.fromJson(temp, device.class).enabled;
        } else {
            return def;
        }

    }

    void LoadOptions(){
        dev1.setChecked(GetEn("device1", true));
        Log.d("LoadOptions ", "LoadOptions: dev1: " + devicesPref.getString("device1", ""));
        name1.setText(GetName("device1", "Home"));
        num1.setText(GetNum("device1"));

        dev2.setChecked(GetEn("device2", false));
        name2.setText(GetName("device2", "Work"));
        num2.setText(GetNum("device2"));

        dev3.setChecked(GetEn("device3", false));
        name3.setText(GetName("device3", "Other"));
        num3.setText(GetNum("device3"));

        netIP.setText(devicesPref.getString("netIP", "1.1.1.1"));

    }

}

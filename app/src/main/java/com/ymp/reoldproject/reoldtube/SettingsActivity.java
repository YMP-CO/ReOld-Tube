package com.ymp.reoldproject.reoldtube;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ymp.reoldproject.reoldtube.MainActivity;

import java.util.HashMap;
import java.util.Map;

public class SettingsActivity extends Activity {
    private static final String TAG = "SettingsActivity";

    public static final String PREF_REGION = "region";
    public static final String PREF_VIDEO_QUALITY = "video_quality";

    private static final Map<String, String> REGIONS = new HashMap<String, String>() {{
        put("US", "USA");
        put("GB", "United Kingdom");
        put("RU", "Russia");
        put("DE", "Germany");
        put("FR", "France");
        put("JP", "Japan");
        put("KR", "South Korea");
        put("IN", "India");
        put("BR", "Brazil");
        put("CA", "Canada");
    }};

    private static final String[] VIDEO_QUALITIES = {
            "Automatic",
            "720p",
            "480p",
            "360p",
            "240p",
            "144p"
    };

    private EditText instanceUrlInput;
    private Spinner regionSpinner;
    private Spinner qualitySpinner;
    private Button resetInstanceButton;
    private Button clearCacheButton;
    private Button saveSettingsButton;

    private SharedPreferences preferences;
    private String[] regionCodes;
    private String[] regionNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        preferences = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE);

        initRegionsArrays();
        initViews();
        loadSettings();
        setupListeners();

        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;
            TextView appVersionTextView = (TextView) findViewById(R.id.app_version);
            appVersionTextView.setText("Version: " + version);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void initRegionsArrays() {
        regionCodes = new String[REGIONS.size()];
        regionNames = new String[REGIONS.size()];

        int i = 0;
        for (Map.Entry<String, String> entry : REGIONS.entrySet()) {
            regionCodes[i] = entry.getKey();
            regionNames[i] = entry.getValue();
            i++;
        }
    }

    private void initViews() {
        instanceUrlInput = (EditText) findViewById(R.id.instance_url);
        regionSpinner = (Spinner) findViewById(R.id.region_spinner);
        qualitySpinner = (Spinner) findViewById(R.id.quality_spinner);
        resetInstanceButton = (Button) findViewById(R.id.reset_instance);
        clearCacheButton = (Button) findViewById(R.id.clear_cache);
        saveSettingsButton = (Button) findViewById(R.id.save_settings);

        ArrayAdapter<String> regionAdapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, regionNames);
        regionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        regionSpinner.setAdapter(regionAdapter);

        ArrayAdapter<String> qualityAdapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, VIDEO_QUALITIES);
        qualityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        qualitySpinner.setAdapter(qualityAdapter);

        ImageButton backButton = (ImageButton) findViewById(R.id.settings_back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void loadSettings() {
        String instanceUrl = preferences.getString(MainActivity.PREF_INSTANCE_URL,
                MainActivity.DEFAULT_INSTANCE_URL);
        String region = preferences.getString(PREF_REGION, "US");
        String quality = preferences.getString(PREF_VIDEO_QUALITY, VIDEO_QUALITIES[0]);

        instanceUrlInput.setText(instanceUrl);

        int regionPosition = 0;
        for (int i = 0; i < regionCodes.length; i++) {
            if (regionCodes[i].equals(region)) {
                regionPosition = i;
                break;
            }
        }
        regionSpinner.setSelection(regionPosition);

        int qualityPosition = 0;
        for (int i = 0; i < VIDEO_QUALITIES.length; i++) {
            if (VIDEO_QUALITIES[i].equals(quality)) {
                qualityPosition = i;
                break;
            }
        }
        qualitySpinner.setSelection(qualityPosition);
    }

    private void setupListeners() {
        resetInstanceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                instanceUrlInput.setText(MainActivity.DEFAULT_INSTANCE_URL);
            }
        });

        clearCacheButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageLoader.getInstance().clearCache();
                Toast.makeText(SettingsActivity.this,
                        "Image cache cleared", Toast.LENGTH_SHORT).show();
            }
        });

        saveSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSettings();
            }
        });
    }

    private void saveSettings() {
        String instanceUrl = instanceUrlInput.getText().toString().trim();
        if (instanceUrl.isEmpty()) {
            instanceUrl = MainActivity.DEFAULT_INSTANCE_URL;
        }

        String region = regionCodes[regionSpinner.getSelectedItemPosition()];
        String quality = VIDEO_QUALITIES[qualitySpinner.getSelectedItemPosition()];

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(MainActivity.PREF_INSTANCE_URL, instanceUrl);
        editor.putString(PREF_REGION, region);
        editor.putString(PREF_VIDEO_QUALITY, quality);
        editor.commit();

        Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show();

        finish();
    }
}
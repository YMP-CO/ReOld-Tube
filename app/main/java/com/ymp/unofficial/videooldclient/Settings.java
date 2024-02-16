package com.ymp.unofficial.videooldclient;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Settings extends Activity {
    private static final String PREFS_NAME = "MyPrefs";
    private static final String KEY_INSTANCE_INVIDIOUS_LINK = "https://yt.cdaut.de/";
   private Button buttonSave;
    private EditText editTextLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        editTextLink = findViewById(R.id.editTextUrl);
      buttonSave = findViewById(R.id.buttonSave);
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveInstanceInvidiousLink();
            }
        });

        loadInstanceInvidiousLink();

    }
    private void saveInstanceInvidiousLink() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_INSTANCE_INVIDIOUS_LINK, editTextLink.getText().toString());
        editor.apply();
        Toast.makeText(this, "Ссылка сохранена", Toast.LENGTH_SHORT).show();
    }

    private void loadInstanceInvidiousLink() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String savedLink = sharedPreferences.getString(KEY_INSTANCE_INVIDIOUS_LINK, "");
        editTextLink.setText(savedLink);
    }
}

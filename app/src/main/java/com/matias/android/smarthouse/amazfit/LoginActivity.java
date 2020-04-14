package com.matias.android.smarthouse.amazfit;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.matias.android.musicsocial.utils.QRUtils;

import org.json.JSONObject;

import java.util.Random;

public class LoginActivity extends AppCompatActivity {
    private static final String ALLOWED_CHARACTERS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    @SuppressLint("StaticFieldLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final ImageView qrImg = findViewById(R.id.qr_img);
        Button next = findViewById(R.id.next);

        final SharedPreferences preferences = getSharedPreferences("smart_home", MODE_PRIVATE);
        final String code = random(6);

        new AsyncTask<Void, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Void... voids) {
                try {
                    JSONObject object = new JSONObject();
                    object.put("agent", "code");
                    object.put("code", code);
                    return QRUtils.generateQRCode(LoginActivity.this, object.toString());
                }
                catch(Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                super.onPostExecute(bitmap);
                if(bitmap != null){
                    qrImg.setImageBitmap(bitmap);
                }
            }
        }.execute();

        next.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ApplySharedPref")
            @Override
            public void onClick(View v) {
                preferences
                        .edit()
                        .putString(MainActivity.TokenPreference, code)
                        .commit();
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            }
        });
    }

    private String random(int sizeOfRandomString) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder(sizeOfRandomString);
        for (int i = 0; i < sizeOfRandomString; i++) {
            sb.append(ALLOWED_CHARACTERS.charAt(random.nextInt(ALLOWED_CHARACTERS.length())));
        }
        return sb.toString();
    }
}
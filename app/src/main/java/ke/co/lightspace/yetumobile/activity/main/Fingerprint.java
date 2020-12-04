package ke.co.lightspace.yetumobile.activity.main;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import justtide.FingerprintReader;
import ke.co.lightspace.yetumobile.R;

public class Fingerprint extends AppCompatActivity {
    FingerprintReader fingerprintReader = FingerprintReader.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fingerprint);


    }
}

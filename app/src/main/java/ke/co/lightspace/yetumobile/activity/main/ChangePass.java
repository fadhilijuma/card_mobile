package ke.co.lightspace.yetumobile.activity.main;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import butterknife.BindView;
import butterknife.ButterKnife;
import ke.co.lightspace.yetumobile.R;
import ke.co.lightspace.yetumobile.activity.config.Config;
import ke.co.lightspace.yetumobile.activity.db.MainDB;
import ke.co.lightspace.yetumobile.activity.model.RealmApplication;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

public class ChangePass extends MyBaseActivity {

    @BindView(R.id.oldpass)
    EditText oldpass;

    @BindView(R.id.newpass)
    EditText newpass;

    @BindView(R.id.confirm)
    EditText confirm;

    @BindView(R.id.submit)
    Button submit;
    @BindView(R.id.coordinatorLayout)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.progress_update)
    ProgressBar progressBar;

    @BindView(R.id.oldpassInput)
    TextInputLayout oldpassword;

    @BindView(R.id.newpassInput)
    TextInputLayout newpassword;

    @BindView(R.id.confirmInput)
    TextInputLayout confirmInput;

    private MainDB dbs;
    private SQLiteDatabase db;
    @BindView(R.id.toolbars)
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_pass);

        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        initToolbar();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        final String pass = prefs.getString("Password", null);
        final String username = prefs.getString("createdby", null);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {

                    if (oldpass.getText().toString().equals(pass) && newpass.getText().toString().equals(confirm.getText().toString())) {

                        makeJsonObjectRequest(username, confirm.getText().toString());
                    } else {
                        showErrorSnackbar("Password yako hailingani na ile ya kwanza. Tafadhali jaribu tena..");

                    }

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private void makeJsonObjectRequest(String username, String pass) throws UnsupportedEncodingException {
        progressBar.setVisibility(View.VISIBLE);

        HashMap<String, String> params = new HashMap<>();
        params.put("username", username);
        params.put("password", encrypt("da0k188qL5OiY3eX", "_VSUrIqGV2pHSye1", pass).trim());

        JsonObjectRequest req = new JsonObjectRequest(Config.PassChange, new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getString("password").equals("200")) {
                                dbs = new MainDB(getApplicationContext());
                                db = dbs.getWritableDatabase();
                                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                                String createdby = prefs.getString("createdby", null);

                                ContentValues values = new ContentValues();
                                values.put("Username", createdby);
                                values.put("Password", confirm.getText().toString());

                                db.insert("tb_login", null, values);
                                progressBar.setVisibility(View.INVISIBLE);
                                startActivity(new Intent(ChangePass.this, Login.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                            } else {
                                Snackbar snackbar = Snackbar
                                        .make(coordinatorLayout, "Tafadhali jaribu tena!", Snackbar.LENGTH_LONG)
                                        .setAction("RETRY", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                            }
                                        });

                                snackbar.setActionTextColor(Color.RED);

                                View sbView = snackbar.getView();
                                TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                                textView.setTextColor(Color.YELLOW);
                                snackbar.show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                progressBar.setVisibility(View.INVISIBLE);
            }
        }){

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put(
                        "Authorization",
                        String.format("Basic %s", Base64.encodeToString(
                                String.format("%s:%s", "yetu", "p4ssw0rd").getBytes(), Base64.DEFAULT)));
                return headers;
            }
        };

        // Adding request to request queue
        RealmApplication.getInstance().addToRequestQueue(req);
    }

    public static String encrypt(String key, String initVector, String value) {
        try {
            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

            byte[] encrypted = cipher.doFinal(value.getBytes());

            return Base64.encodeToString(encrypted, Base64.DEFAULT);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    private void showErrorSnackbar(String message) {
        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, message, Snackbar.LENGTH_LONG)
                .setAction("RETRY", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                    }
                });

// Changing message text color
        snackbar.setActionTextColor(Color.RED);

// Changing action button text color
        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.YELLOW);
        snackbar.show();
    }

    private void initToolbar() {
        final ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}

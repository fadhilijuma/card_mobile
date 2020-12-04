package ke.co.lightspace.yetumobile.activity.transactions;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import dmax.dialog.SpotsDialog;
import ke.co.lightspace.yetumobile.R;
import ke.co.lightspace.yetumobile.activity.config.Config;
import ke.co.lightspace.yetumobile.activity.main.BarCode;
import ke.co.lightspace.yetumobile.activity.main.Login;
import ke.co.lightspace.yetumobile.activity.main.MyBaseActivity;
import ke.co.lightspace.yetumobile.activity.model.RealmApplication;

public class ATMCardConnect extends MyBaseActivity {
    private Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_connect);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initToolbar();


    }

    public void CardActivation(View v) {

       startActivity(new Intent(this,CardActivation.class));
    }
    public void PinReset(View v) {

        startActivity(new Intent(this,PinReset.class));
    }

    public void UpdateUsername(View v) {

        dialogUsername();
    }
    public void PinChange(View v) {

        startActivity(new Intent(this,PinChange.class));
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
    private void changeUsername(HashMap<String, String> params) {
        final android.app.AlertDialog dialog = new SpotsDialog(context);

        dialog.show();

        JsonObjectRequest req = new JsonObjectRequest(Config.changeUsername, new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getString("ResponseCode").equals("200")) {

                                dialog.dismiss();

                                Intent next = new Intent(getApplicationContext(), Login.class);
                                next.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(next);


                            } else if (response.getString("ResponseCode").equals("401")) {
                                dialog.dismiss();
                                showError("Nenosiri sio sahihi.");
                            }else{
                                dialog.dismiss();
                                showError("Kuna tatizo la kimitambo. Tafadhali jaribu tena baadaye.");

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                dialog.dismiss();

            }
        }) {

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
    private void showError(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message).setTitle("Error")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        startActivity(new Intent(ATMCardConnect.this, ATMCardConnect.class));
                    }
                });


        AlertDialog alert = builder.create();
        alert.show();
    }
    private boolean dialogUsername() {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                context);

        alertDialogBuilder.setTitle("KUBADILI USERNAME");
        alertDialogBuilder.setIcon(R.mipmap.loan);

        LayoutInflater li = LayoutInflater.from(context);
        final View dialogView = li.inflate(R.layout.username_update, null);

        final EditText username2 = dialogView
                .findViewById(R.id.username2);
        final EditText pass = dialogView
                .findViewById(R.id.password_);

        alertDialogBuilder.setView(dialogView);

        alertDialogBuilder.setPositiveButton(R.string.confirm,
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {

                        if (username2.getText().toString().isEmpty()){
                            showError("Username mpya inahitajika.");
                        }
                        if (pass.getText().toString().isEmpty()){
                            showError("Nenosiri linaitajika.");
                        }

                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        String createdBy = prefs.getString("createdby", null);

                        String phoneNumber = username2.getText().toString().replaceFirst("0", "255");

                        final HashMap<String, String> params = new HashMap<>();
                        params.put("username1", createdBy);
                        params.put("username2", phoneNumber);
                        params.put("Password",encrypt("da0k188qL5OiY3eX", "_VSUrIqGV2pHSye1", pass.getText().toString()).trim());


                        if (isNetworkAvailable(getApplicationContext())) {


                            new MaterialStyledDialog.Builder(ATMCardConnect.this)
                                    .setTitle("THIBITISHA")
                                    .setDescription("USERNAME MPYA: " + username2.getText().toString())
                                    .setIcon(R.mipmap.mucoba)
                                    .setPositiveText("OK")
                                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                            changeUsername(params);

                                        }
                                    }).setNegativeText("Cancel")
                                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                            Intent intent = new Intent(getApplicationContext(), TransConnect.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            startActivity(intent);

                                        }
                                    })
                                    //.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_launcher))
                                    .show();


                        } else {
                            showError("No internet Connectivity....");
                        }


                    }

                });

        alertDialogBuilder.setNegativeButton(R.string.reject,
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {

                        Intent intent = new Intent(getApplicationContext(), TransConnect.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);


                    }

                });

        AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.show();
        return true;

    }
    private boolean isNetworkAvailable(final Context context) {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }
    private static String encrypt(String key, String initVector, String value) {
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

}

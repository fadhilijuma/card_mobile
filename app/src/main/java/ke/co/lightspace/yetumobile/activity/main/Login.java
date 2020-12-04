package ke.co.lightspace.yetumobile.activity.main;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import ke.co.lightspace.yetumobile.R;
import ke.co.lightspace.yetumobile.activity.config.Config;
import ke.co.lightspace.yetumobile.activity.db.MainDB;
import ke.co.lightspace.yetumobile.activity.model.RealmApplication;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

public class Login extends AppCompatActivity {

    @BindView(R.id.input_username)
    EditText username;

    @BindView(R.id.input_password)
    EditText password;
    @BindView(R.id.input_layout_username)
    TextInputLayout input_username;
    @BindView(R.id.input_layout_password)
    TextInputLayout inputPass;
    @BindView(R.id.btnLogin)
    Button login;
    String createdby;
    @BindView(R.id.progress_update)
    ProgressBar progressBar;
    @BindView(R.id.codLayout)
    CoordinatorLayout coordinatorLayout;
    private MainDB dbs;
    private SQLiteDatabase db;
    private Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ButterKnife.bind(this);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {

                    //startActivity(new Intent(Login.this,MainActivity.class));
                    submitForm();
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private void submitForm() throws JSONException, IOException {
        if (validateName() && validatePassword()) {

            createdby = username.getText().toString();

            if (isNetworkAvailable(getApplicationContext())) {
                makeJsonObjectRequest(createdby, password.getText().toString());
            } else {
                Snackbar snackbar = Snackbar
                        .make(coordinatorLayout, "Hauna Mtandao", Snackbar.LENGTH_LONG)
                        .setAction("RETRY", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                            }
                        });

                snackbar.setActionTextColor(Color.RED);

                View sbView = snackbar.getView();
                TextView textView = sbView.findViewById(android.support.design.R.id.snackbar_text);
                textView.setTextColor(Color.YELLOW);
                snackbar.show();
            }


        } else {
            return;
        }
    }

    private boolean validateName() {
        if (username.getText().toString().trim().isEmpty()) {
            input_username.setError(getString(R.string.err_msg_name));
            requestFocus(username);
            return false;
        } else {
            input_username.setErrorEnabled(false);
        }

        return true;
    }


    private boolean validatePassword() {
        if (password.getText().toString().trim().isEmpty()) {
            inputPass.setError(getString(R.string.err_msg_password));
            requestFocus(password);
            return false;
        } else {
            inputPass.setErrorEnabled(false);
        }

        return true;
    }

    public boolean isNetworkAvailable(final Context context) {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    private void makeJsonObjectRequest(final String user, final String pass) throws UnsupportedEncodingException {
        final android.app.AlertDialog dialog = new SpotsDialog(context);

        dialog.show();

        String encryptedPass = encrypt("da0k188qL5OiY3eX", "_VSUrIqGV2pHSye1", pass).trim();
        final String phoneNumber = user.replaceFirst("0", "255");
        HashMap<String, String> params = new HashMap<>();
        params.put("username", phoneNumber);
        params.put("password", encryptedPass);

        JsonObjectRequest req = new JsonObjectRequest(Config.LOGIN, new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String responseCode = response.getString("ResponseCode");

                            if (responseCode.equals("000")) {

                                username.setText("");
                                password.setText("");

                                String params=response.getString("params");

                                String [] paramsArray=params.split("\\|");

                                if (paramsArray[0].equals("200")){

                                    String branch=paramsArray[1];
                                    String products=paramsArray[2];
                                    String GL=paramsArray[3];
                                    String AgentNames=paramsArray[4];
                                    SharedPreferences preferences = getDefaultSharedPreferences(Login.this);
                                    preferences.edit().putString("branch", branch).apply();
                                    preferences.edit().putString("products", products).apply();
                                    preferences.edit().putString("createdby", phoneNumber).apply();
                                    preferences.edit().putString("Password", pass).apply();
                                    preferences.edit().putString("GL", GL).apply();
                                    preferences.edit().putString("AgentNames", AgentNames).apply();

                                    dialog.dismiss();

                                    if (CredentialsCount(getApplicationContext()) > 0) {
                                        startActivity(new Intent(Login.this, MainActivity.class));
                                        username.setText("");
                                        password.setText("");
                                    } else {
                                        startActivity(new Intent(Login.this, ChangePass.class));
                                        username.setText("");
                                        password.setText("");

                                    }

                                }else{

                                    showErrorSnackbar("Password sio sahihi");

                                    dialog.dismiss();

                                    username.setText("");
                                    password.setText("");
                                }




                            } else {

                                showErrorSnackbar("Password sio sahihi");

                                dialog.dismiss();

                                username.setText("");
                                password.setText("");
                            }
                        } catch (JSONException e) {

                            dialog.dismiss();

                            showErrorSnackbar("Tafadhali jaribu tena");
                            username.setText("");
                            password.setText("");
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                dialog.dismiss();

                showErrorSnackbar("Tafadhali jaribu tena");
                username.setText("");
                password.setText("");
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

    public void showErrorSnackbar(String message) {
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

    private int CredentialsCount(Context context) {
        int record = 0;
        try {
            dbs = new MainDB(context);
            db = dbs.getWritableDatabase();
            Cursor cursor = db.rawQuery("SELECT *  FROM tb_login", null);
            record = cursor.getCount();
            cursor.close();
            db.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return record;
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

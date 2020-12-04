package ke.co.lightspace.yetumobile.activity.main;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.View;
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

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import ke.co.lightspace.yetumobile.R;
import ke.co.lightspace.yetumobile.activity.config.Config;
import ke.co.lightspace.yetumobile.activity.model.RealmApplication;

public class MobileRegistration extends MyBaseActivity {

    @BindView(R.id.coordinatorLay)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.progress_update)
    ProgressBar progressBar;

    @BindView(R.id.fname)
    EditText FName;
    @BindView(R.id.inputSName)
    EditText SName;
    @BindView(R.id.inputLastName)
    EditText LName;
    @BindView(R.id.AccountNumber)
    EditText accountNumber;
    @BindView(R.id.phoneNumber)
    EditText PhoneNumber;

    @BindView(R.id.saveData)
    Button submit;

    @BindView(R.id.inputFName)
    TextInputLayout FirstName;

    @BindView(R.id.inputLayoutSName)
    TextInputLayout SecondName;

    @BindView(R.id.inputLName)
    TextInputLayout LastName;

    @BindView(R.id.account)
    TextInputLayout account;

    @BindView(R.id.phone)
    TextInputLayout Phone;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mobile_reg);

        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        initToolbar();

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (accountNumber.getText().toString().isEmpty()) {
                    account.setError("Akaunti inahitajika");

                }else if(FName.getText().toString().isEmpty()){
                    FirstName.setError("Jina La Kwanza Linahitajika");

                } else if(SName.getText().toString().isEmpty()){
                    SecondName.setError("Jina La Kati Linahitajika");

                }else if(LName.getText().toString().isEmpty()){
                    LastName.setError("Jina La Ukoo Linahitajika");

                }else if(PhoneNumber.getText().toString().isEmpty()){
                    Phone.setError("Namba ya Simu Inahitajika");

                }else {
                    if (isNetworkAvailable(getApplicationContext())) {
                        makeJsonObjectRequest();
                    } else {
                        Snackbar snackbar = Snackbar
                                .make(coordinatorLayout, "Hauna Internet!", Snackbar.LENGTH_LONG)
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

                }

            }
        });
    }

    private void makeJsonObjectRequest() {
        progressBar.setVisibility(View.VISIBLE);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String createdby = prefs.getString("createdby", null);


        HashMap<String, String> params = new HashMap<>();
        final String phoneN = PhoneNumber.getText().toString().replaceFirst("0", "255");
        params.put("names", FName.getText().toString().toUpperCase()+" "+SName.getText().toString().toUpperCase()+" "+LName.getText().toString().toUpperCase());
        params.put("msisdn", phoneN);
        params.put("createdby", createdby);
        params.put("account", accountNumber.getText().toString());

        JsonObjectRequest req = new JsonObjectRequest(Config.mobilebankingreg, new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        String mStatusCode;
                        try {
                            mStatusCode = response.getString("response");

                            if (mStatusCode.equals("200")) {

                                FName.setText("");
                                SName.setText("");
                                LName.setText("");
                                PhoneNumber.setText("");
                                accountNumber.setText("");
                                progressBar.setVisibility(View.INVISIBLE);
                                Snackbar snackbar = Snackbar
                                        .make(coordinatorLayout, "Ujumbe umepokelewa. Subiri ujumbe wa SMS.", Snackbar.LENGTH_LONG)
                                        .setAction("OK", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                            }
                                        });

                                snackbar.setActionTextColor(Color.RED);

                                View sbView = snackbar.getView();
                                TextView textView = sbView.findViewById(android.support.design.R.id.snackbar_text);
                                textView.setTextColor(Color.YELLOW);
                                snackbar.show();
                                accountNumber.setText("");
                            } else {
                                progressBar.setVisibility(View.INVISIBLE);
                                Snackbar snackbar = Snackbar
                                        .make(coordinatorLayout, "Tafadhali jaribu tena!", Snackbar.LENGTH_LONG)
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
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                progressBar.setVisibility(View.INVISIBLE);
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

    public boolean isNetworkAvailable(final Context context) {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
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

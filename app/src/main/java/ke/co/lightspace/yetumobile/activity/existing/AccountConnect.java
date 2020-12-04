package ke.co.lightspace.yetumobile.activity.existing;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import ke.co.lightspace.yetumobile.R;
import ke.co.lightspace.yetumobile.activity.config.Config;
import ke.co.lightspace.yetumobile.activity.main.MainActivity;
import ke.co.lightspace.yetumobile.activity.main.MyBaseActivity;
import ke.co.lightspace.yetumobile.activity.model.RealmApplication;
import ke.co.lightspace.yetumobile.activity.transactions.CashWithdrawal;

public class AccountConnect extends MyBaseActivity {

    private Context context = this;
    @BindView(R.id.progress_update)
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_connect);

        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolb);
        setSupportActionBar(toolbar);

        initToolbar();


    }

    public void GroupRegistration(View v) {

        RegisterGroup();
    }

    public void MemberRegistration(View v) {

        RegisterMember();
    }


    public boolean RegisterGroup() {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                context);

        alertDialogBuilder.setTitle("KUSAJILI KIKUNDI");
        alertDialogBuilder.setIcon(R.mipmap.pay);

        LayoutInflater li = LayoutInflater.from(context);
        final View dialogView = li.inflate(R.layout.group_reg, null);

        final EditText group_name = dialogView
                .findViewById(R.id.group_id);

        alertDialogBuilder.setView(dialogView);

        alertDialogBuilder.setPositiveButton(R.string.group_reg,
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {


                        if (isNetworkAvailable(getApplicationContext())) {

                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                            String branch = prefs.getString("branch", null);
                            String createdBy = prefs.getString("createdby", null);

                            HashMap<String, String> params = new HashMap<>();
                            params.put("BranchID", branch);
                            params.put("GroupName", group_name.getText().toString());
                            params.put("CreatedBy", createdBy);

                            PostGroupRegistration(params);

                        } else {
                            showAlert("No internet Connectivity....");
                        }


                    }

                });

        alertDialogBuilder.setNegativeButton(R.string.fetch,
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {

                    }

                });

        AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.show();
        return true;

    }

    public boolean RegisterMember() {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                context);

        alertDialogBuilder.setTitle("KUSAJILI MWANACHAMA");
        alertDialogBuilder.setIcon(R.mipmap.pay);

        LayoutInflater li = LayoutInflater.from(context);
        final View dialogView = li.inflate(R.layout.member_reg, null);

        final EditText group_id =dialogView
                .findViewById(R.id.group_id);

        final EditText client_id =dialogView
                .findViewById(R.id.client_id);


        alertDialogBuilder.setView(dialogView);

        alertDialogBuilder.setPositiveButton(R.string.group_reg,
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {


                        if (isNetworkAvailable(getApplicationContext())) {

                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                            String branch = prefs.getString("branch", null);
                            String createdBy = prefs.getString("createdby", null);

                            final HashMap<String, String> params = new HashMap<>();
                            params.put("BranchID", branch);
                            params.put("GroupID", group_id.getText().toString());
                            params.put("ClientID", client_id.getText().toString());
                            params.put("CreatedBy", createdBy);

                            new MaterialStyledDialog.Builder(AccountConnect.this)
                                    .setTitle("DHIBITISHA")
                                    .setDescription("GROUP ID: " + group_id.getText().toString()+"\n"+
                                    "CLIENT ID: "+client_id.getText().toString())
                                    .setIcon(R.mipmap.mucoba)
                                    .setPositiveText("OK")
                                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                            PostMemberRegistration(params);

                                        }
                                    }).setNegativeText("Cancel")
                                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            startActivity(intent);

                                        }
                                    })
                                    //.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_launcher))
                                    .show();


                        } else {
                            showAlert("No internet Connectivity....");
                        }


                    }

                });

        alertDialogBuilder.setNegativeButton(R.string.fetch,
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {

                    }

                });

        AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.show();
        return true;

    }

    private void showAlert(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message).setTitle("Message")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public boolean isNetworkAvailable(final Context context) {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }

    private void PostGroupRegistration(HashMap<String, String> params) {

        progressBar.setVisibility(View.VISIBLE);

        JsonObjectRequest req = new JsonObjectRequest(Config.Group, new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getString("response").equals("200")) {

                                progressBar.setVisibility(View.INVISIBLE);

                                showAlert("OK");


                            } else {
                                Toast.makeText(getApplicationContext(),"Tafadhali jaribu tena baadaye!",Toast.LENGTH_LONG).show();

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressBar.setVisibility(View.INVISIBLE);

                Toast.makeText(getApplicationContext(),"Tafadhali jaribu tena baadaye!",Toast.LENGTH_LONG).show();

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

    private void PostMemberRegistration(HashMap<String, String> params) {

        progressBar.setVisibility(View.VISIBLE);

        JsonObjectRequest req = new JsonObjectRequest(Config.GroupMember, new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getString("response").equals("200")) {

                                progressBar.setVisibility(View.INVISIBLE);

                                showAlert("OK");


                            } else {
                                Toast.makeText(getApplicationContext(),"Tafadhali jaribu tena baadaye!",Toast.LENGTH_LONG).show();

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressBar.setVisibility(View.INVISIBLE);

                Toast.makeText(getApplicationContext(),"Tafadhali jaribu tena baadaye!",Toast.LENGTH_LONG).show();

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

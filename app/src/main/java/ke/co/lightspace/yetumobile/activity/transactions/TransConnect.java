package ke.co.lightspace.yetumobile.activity.transactions;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;
import justtide.ThermalPrinter;
import ke.co.lightspace.yetumobile.R;
import ke.co.lightspace.yetumobile.activity.config.Config;
import ke.co.lightspace.yetumobile.activity.db.MainDB;
import ke.co.lightspace.yetumobile.activity.main.BarCode;
import ke.co.lightspace.yetumobile.activity.main.MyBaseActivity;
import ke.co.lightspace.yetumobile.activity.model.RealmApplication;

public class TransConnect extends MyBaseActivity {
    private Context context = this;
    ThermalPrinter thermalPrinter = ThermalPrinter.getInstance();
    public MainDB dbs;
    public SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trans_connect);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initToolbar();


    }

    public void Balance(View v) {

        startActivity(new Intent(this, BalanceInquiry.class));
    }

    public void CashWithdraw(View v) {

        startActivity(new Intent(this, CashWithdrawal.class));
    }

    public void BarCode(View v) {

        startActivity(new Intent(this, BarCode.class));
    }

    public void GlPosting(View v) {
        dialogGLPosting();
    }

    public void GenerateReport(View v) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String createdBy = prefs.getString("createdby", null);
        HashMap<String, String> params = new HashMap<>();
        params.put("CreatedBy", createdBy);

        if (isNetworkAvailable(getApplicationContext())) {

            makeJsonObjectRequest(params);

        } else {
            showError("Umeishiwa na bando.");

        }
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

    private void makeJsonObjectRequest(HashMap<String, String> params) {
        final android.app.AlertDialog dialog = new SpotsDialog(context);

        dialog.show();

        JsonObjectRequest req = new JsonObjectRequest(Config.statement, new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {

                            String responseCode = response.getString("Code");

                            switch (responseCode) {
                                case "00": {
                                    dbs = new MainDB(getApplicationContext());
                                    db = dbs.getWritableDatabase();

                                    String respObject = response.toString().replace("\\r\\n", " ").replace("\\", "").replace(" ", "").replace("[\"{", "[{").replace("}\"]", "}]").replace("\"{", "{").replace("}\"", "}");


                                    JSONObject lastResp = new JSONObject(respObject);
                                    JSONArray jsonArray = lastResp.getJSONArray("Data");

                                    if (jsonArray.length()==0){
                                        showError("No transaction records found.");
                                    }
                                    printLogo();

                                    ContentValues values = new ContentValues();

                                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                                    String agentNames = prefs.getString("AgentNames", null);
                                    thermalPrinter = ThermalPrinter.getInstance();
                                    thermalPrinter.initBuffer();
                                    thermalPrinter.setGray(7);
                                    thermalPrinter.setHeightAndLeft(0, 0);
                                    thermalPrinter.setLineSpacing(5);
                                    thermalPrinter.getFontCH();
                                    thermalPrinter.setFont(6, 1);
                                    thermalPrinter.print(" WAKALA: " + agentNames + "\n\n\n");
                                    thermalPrinter.print("TAREHE: " + DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime()) + "\n\n");
                                    thermalPrinter.print("=====RIPOTI YA MIAMALA=====\n\n");

                                    for (int i = 0, size = jsonArray.length(); i < size; i++) {
                                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                                        values.put("TransType", jsonObject.getString("1"));
                                        values.put("Account", jsonObject.getString("2"));
                                        values.put("Amount", jsonObject.getString("3"));
                                        db.insert("TRANSACTIONS", null, values);

                                    }
                                    db.close();

                                    GetDeposit();

                                    GetWithdrawal();
                                    CleanDb();

                                    thermalPrinter.setLineSpacing(5);
                                    thermalPrinter.print("      Asante kwa kubenki nasi.\n\n");
                                    thermalPrinter.print("          Mucoba Bank Plc\n");
                                    thermalPrinter.print("           Benki yako,\n");
                                    thermalPrinter.print("       Kwa maendeleo yako.\n\n\n");
                                    thermalPrinter.shiftRight(60);
                                    thermalPrinter.printStart();
                                    thermalPrinter.waitForPrintFinish();
                                    printSpace();
                                    showAlert("Success");
                                    break;
                                }
                                case "01": {
                                    showError("Haujafanya muamala wowote.");

                                    break;
                                }
                                default: {
                                    showError("Kuna tatizo la kimitambo. Tafadhali kjaribu tena baadaye.");

                                    break;
                                }
                            }

                            dialog.dismiss();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                dialog.dismiss();
                showError("Kuna tatizo la kimitambo. Tafadhali jaribu tena baadaye.");
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

                        startActivity(new Intent(TransConnect.this, TransConnect.class));
                    }
                });


        AlertDialog alert = builder.create();
        alert.show();
    }

    private int printLogo() {
        this.thermalPrinter = ThermalPrinter.getInstance();
        this.thermalPrinter.initBuffer();
        this.thermalPrinter.setGray(7);
        this.thermalPrinter.printLogo(0, 1, BitmapFactory.decodeResource(getResources(), R.mipmap.mucobas));
        this.thermalPrinter.setStep(20);
        this.thermalPrinter.printStart();
        return this.thermalPrinter.waitForPrintFinish();
    }

    private int printSpace() {
        this.thermalPrinter = ThermalPrinter.getInstance();
        this.thermalPrinter.initBuffer();
        this.thermalPrinter.setGray(7);
        this.thermalPrinter.setHeightAndLeft(0, 0);
        this.thermalPrinter.setLineSpacing(5);
        this.thermalPrinter.getFontCH();
        this.thermalPrinter.setFont(6, 1);
        this.thermalPrinter.setStep(4);
        this.thermalPrinter.setFont(6, 1);
        this.thermalPrinter.print(" \n");
        this.thermalPrinter.setStep(4);
        this.thermalPrinter.setFont(6, 1);
        this.thermalPrinter.print(" \n");
        this.thermalPrinter.setStep(4);
        this.thermalPrinter.setFont(6, 1);
        this.thermalPrinter.print(" \n");
        this.thermalPrinter.setStep(4);
        this.thermalPrinter.setFont(6, 1);
        this.thermalPrinter.print(" \n");
        this.thermalPrinter.setStep(4);
        this.thermalPrinter.setFont(6, 1);
        this.thermalPrinter.print(" \n");
        this.thermalPrinter.shiftRight(60);
        this.thermalPrinter.setStep(4);
        this.thermalPrinter.setFont(6, 1);
        this.thermalPrinter.print(" \n");
        this.thermalPrinter.shiftRight(60);
        this.thermalPrinter.shiftRight(60);
        this.thermalPrinter.setStep(4);
        this.thermalPrinter.setFont(6, 1);
        this.thermalPrinter.print(" \n");
        this.thermalPrinter.printStart();
        return this.thermalPrinter.waitForPrintFinish();
    }

    private boolean isNetworkAvailable(final Context context) {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }

    private void showAlert(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message).setTitle("Success")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        dialog.dismiss();
                        Intent next = new Intent(getApplicationContext(), TransConnect.class);
                        next.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(next);
                    }
                });


        AlertDialog alert = builder.create();
        alert.show();
    }

    private String getDTotal() {
        String totalDeposits;
        Cursor cur = db.rawQuery("SELECT SUM(Amount) AS TOTAL FROM TRANSACTIONS Where TransType='CR'", null);
        if (cur.moveToFirst()) {
            totalDeposits = cur.getString(cur.getColumnIndex("TOTAL"));
        } else {
            totalDeposits = "0.00";
        }//TextUtils.isEmpty(totals)
        if (TextUtils.isEmpty(totalDeposits)){
            totalDeposits="000";
        }


        cur.close();

        return totalDeposits;
    }

    private String getWTotal() {
        dbs = new MainDB(getApplicationContext());
        db = dbs.getWritableDatabase();
        String totalDeposits;
        Cursor cursor1 = db.rawQuery("SELECT SUM(Amount) AS TOTAL FROM TRANSACTIONS where TransType='DR'", null);
        if (cursor1.moveToFirst()) {
            totalDeposits = cursor1.getString(cursor1.getColumnIndex("TOTAL"));
        } else {
            totalDeposits = "0.00";
        }
        if (TextUtils.isEmpty(totalDeposits)){
            totalDeposits="000";
        }

        cursor1.close();
        db.close();

        return totalDeposits;
    }

    private void GetDeposit() {
        dbs = new MainDB(getApplicationContext());
        db = dbs.getWritableDatabase();

        String query = "SELECT Account,Amount from  TRANSACTIONS WHERE TransType='CR'";

        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            this.thermalPrinter.print("KUWEKA\n\n");
            this.thermalPrinter.print("----------------------------" + "\n");
            do {
                String Amount = cursor.getString(cursor.getColumnIndex("Amount"));

                String Account = cursor.getString(cursor.getColumnIndex("Account"));
                DecimalFormat dFormat = new DecimalFormat("####,###,###.00");
                double DepA = Double.parseDouble(Amount);

                this.thermalPrinter.print(Account + "=> TSHS." + dFormat.format(DepA) + "\n");


            } while (cursor.moveToNext());
        }
        DecimalFormat dFormats = new DecimalFormat("####,###,###.00");
        String totals=getDTotal();
        if (TextUtils.isEmpty(totals)){
            this.thermalPrinter.print("KUWEKA\n\n");
            this.thermalPrinter.print(" " + "\n");
            this.thermalPrinter.print("----------------------------" + "\n");
            this.thermalPrinter.print("TOTAL.==>> TSHS.0.00" + "\n\n");
            this.thermalPrinter.print("----------------------------" + "\n");
        }else {
            double DepTotal = Double.parseDouble(totals);
            this.thermalPrinter.print(" " + "\n");
            this.thermalPrinter.print("----------------------------" + "\n");
            this.thermalPrinter.print("TOTAL.==>> TSHS." + dFormats.format(DepTotal) + "\n\n");
            this.thermalPrinter.print("----------------------------" + "\n");


        }
        cursor.close();
    }

    private void GetWithdrawal() {
        dbs = new MainDB(getApplicationContext());
        db = dbs.getWritableDatabase();

        String query = "SELECT Account,Amount from  TRANSACTIONS WHERE TransType='DR'";

        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            this.thermalPrinter.print("KUTOA\n\n");
            this.thermalPrinter.print("----------------------------" + "\n");
            do {
                String Amount = cursor.getString(cursor.getColumnIndex("Amount"));

                String Account = cursor.getString(cursor.getColumnIndex("Account"));
                DecimalFormat dFormat = new DecimalFormat("####,###,###.00");
                double DepA = Double.parseDouble(Amount);

                String printResult=dFormat.format(DepA);

                this.thermalPrinter.print(Account + "=> TSHS." + printResult + "\n");


            } while (cursor.moveToNext());
        }
        DecimalFormat dFormats = new DecimalFormat("####,###,###.00");
        String totals=getWTotal();

        if (TextUtils.isEmpty(totals)){
            this.thermalPrinter.print("KUTOA\n\n");
            this.thermalPrinter.print(" " + "\n");
            this.thermalPrinter.print("----------------------------" + "\n");
            this.thermalPrinter.print("TOTAL.==>> TSHS.0.00" + "\n\n");
            this.thermalPrinter.print("----------------------------" + "\n");

        }else {
            double WithTotal = Double.parseDouble(totals);
            this.thermalPrinter.print(" " + "\n");
            this.thermalPrinter.print("----------------------------" + "\n");
            this.thermalPrinter.print("TOTAL.==>> TSHS." + dFormats.format(WithTotal) + "\n\n");
            this.thermalPrinter.print("----------------------------" + "\n");

        }

        cursor.close();
    }

    private void CleanDb() {
        dbs = new MainDB(getApplicationContext());
        db = dbs.getWritableDatabase();

        String delete = "DELETE FROM TRANSACTIONS";
        db.execSQL(delete);
        db.close();
    }

    private void makeJGLPostingRequest(HashMap<String, String> params) {
        final android.app.AlertDialog dialog = new SpotsDialog(context);

        dialog.show();

        JsonObjectRequest req = new JsonObjectRequest(Config.glPosting, new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getString("response").equals("200")) {

                                dialog.dismiss();

                                showAlert("Muamala umefaulu. Tafadhali subiri ujumbe wa SMS.");


                            } else if (response.getString("response").equals("201")) {
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

    private boolean dialogGLPosting() {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                context);

        alertDialogBuilder.setTitle("KUWEKA KWENYE GL");
        alertDialogBuilder.setIcon(R.mipmap.loan);

        LayoutInflater li = LayoutInflater.from(context);
        final View dialogView = li.inflate(R.layout.glposting, null);

        final EditText account = dialogView
                .findViewById(R.id.account);
        final EditText amount = dialogView
                .findViewById(R.id.amount);
        final EditText narration = dialogView
                .findViewById(R.id.narrations);

        alertDialogBuilder.setView(dialogView);

        alertDialogBuilder.setPositiveButton(R.string.confirm,
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {

                        if (account.getText().toString().isEmpty()){
                            showError("Account required.");
                        }
                        if (amount.getText().toString().isEmpty()){
                            showError("Amount required.");
                        }
                        if (narration.getText().toString().isEmpty()){
                            showError("Names required.");
                        }
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        String branch = prefs.getString("branch", null);
                        String createdBy = prefs.getString("createdby", null);

                        final HashMap<String, String> params = new HashMap<>();
                        params.put("AccountNumber", account.getText().toString());
                        params.put("CustomerNames", narration.getText().toString());
                        params.put("CreatedBy", createdBy);
                        params.put("BranchID", branch);
                        params.put("Amount", amount.getText().toString());


                        if (isNetworkAvailable(getApplicationContext())) {

                            double value = Double.parseDouble(amount.getText().toString());
                            DecimalFormat myFormatter = new DecimalFormat("###,###,###,###.##");
                            String output = myFormatter.format(value);

                            new MaterialStyledDialog.Builder(TransConnect.this)
                                    .setTitle("THIBITISHA KUWEKA PESA")
                                    .setDescription("GL NUMBER: " + account.getText().toString() +
                                            "\nKIASI CHA PESA: TSHS. " + output + "\n" +
                                            "JINA LA MTEJA: " + narration.getText().toString())
                                    .setIcon(R.mipmap.mucoba)
                                    .setPositiveText("OK")
                                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                            makeJGLPostingRequest(params);

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
}

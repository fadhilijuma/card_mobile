package ke.co.lightspace.yetumobile.activity.transactions;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import justtide.ThermalPrinter;
import ke.co.lightspace.yetumobile.R;
import ke.co.lightspace.yetumobile.activity.config.Config;
import ke.co.lightspace.yetumobile.activity.db.MainDB;
import ke.co.lightspace.yetumobile.activity.main.MainActivity;
import ke.co.lightspace.yetumobile.activity.main.MyBaseActivity;
import ke.co.lightspace.yetumobile.activity.model.RealmApplication;


public class CashDeposit extends MyBaseActivity {

    @BindView(R.id.codlayout)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.account)
    EditText accountNo;
    @BindView(R.id.amount)
    EditText DepositAmount;
    @BindView(R.id.pnumber)
    EditText PhoneNumber;
    @BindView(R.id.narration)
    EditText narration;
    @BindView(R.id.submit)
    Button submit;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.accountNumberInput)
    TextInputLayout accountNumberInput;

    @BindView(R.id.cashInput)
    TextInputLayout cashInput;

    @BindView(R.id.phoneInput)
    TextInputLayout phoneInput;

    @BindView(R.id.narrateInput)
    TextInputLayout narrateInput;
    @BindView(R.id.progress_update)
    ProgressBar progressBar;
    private Context context = this;
    private MainDB dbs;
    private SQLiteDatabase db;

    ThermalPrinter thermalPrinter = ThermalPrinter.getInstance();

    @SuppressLint("HandlerLeak")

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.cash_deposit);

        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        initToolbar();


        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (TextUtils.isEmpty(accountNo.getText().toString())) {

                    accountNumberInput.setError("Account required");

                } else if (TextUtils.isEmpty(DepositAmount.getText().toString())) {
                    cashInput.setError("Amount required");

                } else if (TextUtils.isEmpty(narration.getText().toString())) {
                    narrateInput.setError("Narration required");

                } else {

                    if (isNetworkAvailable(getApplicationContext())) {

                        double value = Double.parseDouble(DepositAmount.getText().toString());
                        DecimalFormat myFormatter = new DecimalFormat("###,###,###,###.##");
                        String output = myFormatter.format(value);

                        String transaction = "KUWEKA PESA \n\nAKAUNTI: " + accountNo.getText().toString() + "\n" + "KIASI CHA PESA: Tsh." + output;

                        confirmTransaction(transaction);


                    } else {
                        Error("No internet Connectivity....");
                    }

                }
            }
        });

    }

    private void confirmTransaction(String message) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setMessage(message).setTitle(R.string.warn_trans)
                .setCancelable(false)
                .setIcon(R.drawable.delete)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        String branch = prefs.getString("branch", null);
                        String createdBy = prefs.getString("createdby", null);
                        String gl = prefs.getString("GL", null);

                        String phone_number = PhoneNumber.getText().toString().replaceFirst("0", "255");

                        if (PhoneNumber.getText().toString().isEmpty()){
                            phone_number=createdBy;
                        }

                        HashMap<String, String> params = new HashMap<>();
                        params.put("accountnumber", accountNo.getText().toString());
                        params.put("amount", DepositAmount.getText().toString());
                        params.put("phoneNumber", phone_number);
                        params.put("Narration", createdBy);
                        params.put("BranchId", branch);
                        params.put("glcode", gl);
                        params.put("depositor", narration.getText().toString());

                        if (isNetworkAvailable(getApplicationContext())) {

                            makeJsonObjectRequest(params);

                        } else {
                            Snackbar snackbar = Snackbar
                                    .make(coordinatorLayout, "Hauna internet!", Snackbar.LENGTH_LONG)
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
                }).setNegativeButton(R.string.reject, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {


            }
        });


        android.app.AlertDialog alert = builder.create();
        alert.show();
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

    private void Error(String message) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setMessage(message).setTitle("Oops!")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        startActivity(new Intent(CashDeposit.this, MainActivity.class));

                    }
                });


        android.app.AlertDialog alert = builder.create();
        alert.show();
    }


    public boolean isNetworkAvailable(final Context context) {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }


    private int print(String AccountNames) {

        DecimalFormat dFormat = new DecimalFormat("####,###,###.00");
        double deposit = Double.parseDouble(DepositAmount.getText().toString());
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String agentNames = prefs.getString("AgentNames", null);
        this.thermalPrinter = ThermalPrinter.getInstance();
        this.thermalPrinter.initBuffer();
        this.thermalPrinter.setGray(7);
        this.thermalPrinter.setHeightAndLeft(0, 0);
        this.thermalPrinter.setLineSpacing(5);
        this.thermalPrinter.getFontCH();
        this.thermalPrinter.setFont(6, 1);
        this.thermalPrinter.print(" WAKALA: " + agentNames + "\n\n\n");
        this.thermalPrinter.print("===========KUWEKA PESA==========\n\n");
        this.thermalPrinter.print("TAREHE: " + DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime()) + "\n\n");
        this.thermalPrinter.print("AKAUNTI: " + accountNo.getText().toString() + "\n\n");
        this.thermalPrinter.print("JINA: " + AccountNames + "\n\n");
        this.thermalPrinter.print("ALIYEWEKA: " + narration.getText().toString().toUpperCase() + "\n\n");
        this.thermalPrinter.print("MAELEZO: " + "KUWEKA PESA" + "\n\n\n");
        this.thermalPrinter.print("KIASI: TSHS." + dFormat.format(deposit) + "\n\n\n");
        this.thermalPrinter.print("      Asante kwa kubenki nasi.\n\n");
        this.thermalPrinter.print("          Mucoba Bank Plc\n");
        this.thermalPrinter.print("           Benki yako,\n");
        this.thermalPrinter.print("       Kwa maendeleo yako.\n\n\n");
        this.thermalPrinter.shiftRight(60);
        this.thermalPrinter.printStart();
        return this.thermalPrinter.waitForPrintFinish();
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

    private int printBarCode() {
        double value = Double.parseDouble(DepositAmount.getText().toString());
        DecimalFormat myFormatter = new DecimalFormat("###,###,###,###.00");
        String output = myFormatter.format(value);
        this.thermalPrinter = ThermalPrinter.getInstance();
        this.thermalPrinter.initBuffer();
        this.thermalPrinter.setGray(7);
        this.thermalPrinter.printLogo(0, 1, barcodeGenerator("D" + output));
        this.thermalPrinter.setStep(20);
        this.thermalPrinter.printStart();
        this.thermalPrinter.print("================================\n");
        return this.thermalPrinter.waitForPrintFinish();
    }

    private Bitmap barcodeGenerator(String data) {
        Bitmap ImageBitmap = null;
        try {
            MultiFormatWriter writer = new MultiFormatWriter();

            BitMatrix bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, 300, 200);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            ImageBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

            for (int i = 0; i < width; i++) {//width
                for (int j = 0; j < height; j++) {//height
                    ImageBitmap.setPixel(i, j, bitMatrix.get(i, j) ? Color.BLACK : Color.WHITE);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return ImageBitmap;
    }

    private void makeJsonObjectRequest(HashMap<String, String> params) {
        final android.app.AlertDialog dialog = new SpotsDialog(context);

        dialog.show();

        JsonObjectRequest req = new JsonObjectRequest(Config.cash_deposit, new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getString("response").equals("200")) {

                                dialog.dismiss();

                                printLogo();
                                print(response.getString("Names"));
                                printBarCode();
                                printSpace();

                                printLogo();
                                print(response.getString("Names"));
                                printBarCode();
                                printSpace();

                                showAlert("OK");
                                String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

                                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                                Date dates = dateFormat.parse(date);
                                long unixTime = dates.getTime() / 1000;
                                dbs = new MainDB(getApplicationContext());
                                db = dbs.getWritableDatabase();

                                ContentValues values = new ContentValues();
                                values.put("Account", accountNo.getText().toString());
                                values.put("TransType", "CR");
                                values.put("Deposit", DepositAmount.getText().toString());
                                values.put("Date", unixTime);
                                values.put("Withdrawal", "");

                                db.insert("TRANSACTIONS", null, values);

                                db.close();


                            } else if (response.getString("response").equals("201")) {
                                showError("Akaunti uliyoweka sio sahihi.");
                            } else if (response.getString("response").equals("202")) {
                                showError("Kuna tatizo la kimitambo. Tafadhali jaribu tena baadaye.");
                            }
                        } catch (JSONException | ParseException e) {
                            e.printStackTrace();
                        }


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                dialog.dismiss();
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

        req.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
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

    private void showAlert(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message).setTitle("Success")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        accountNo.setText("");
                        DepositAmount.setText("");
                        PhoneNumber.setText("");

                        startActivity(new Intent(CashDeposit.this, MainActivity.class));
                    }
                });


        AlertDialog alert = builder.create();
        alert.show();
    }

    private void showError(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message).setTitle("Error")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        accountNo.setText("");
                        DepositAmount.setText("");
                        PhoneNumber.setText("");

                        startActivity(new Intent(CashDeposit.this, MainActivity.class));
                    }
                });


        AlertDialog alert = builder.create();
        alert.show();
    }
}

package ke.co.lightspace.yetumobile.activity.transactions;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import butterknife.BindView;
import dmax.dialog.SpotsDialog;
import justtide.CommandApdu;
import justtide.ContactCard;
import justtide.Emvl2;
import justtide.IccException;
import justtide.IccReader;
import justtide.ResponseApdu;
import ke.co.lightspace.yetumobile.R;
import ke.co.lightspace.yetumobile.activity.config.Config;
import ke.co.lightspace.yetumobile.activity.main.MainActivity;
import ke.co.lightspace.yetumobile.activity.main.MyBaseActivity;
import ke.co.lightspace.yetumobile.activity.model.RealmApplication;

import static java.lang.Boolean.TRUE;

public class CardActivation extends MyBaseActivity {

    private Thread mThread = null;
    private Thread EmvThread = null;
    private Handler mHandler = null;
    private TextView show;
    private String strShow = "";
    protected static final String TAG = CardActivation.class.getName();
    protected static final int CHECKCARD = 0;
    protected static final int REFRESH = 1;
    private TextView showDataText;
    private Context context = this;
    boolean blnCheck = true;
    Emvl2 emvl2 = Emvl2.getInstance();
    IccReader iccReader = IccReader.getInstance();
    ContactCard contactCard = null;
    static String sixteenDigit = "";
    static String AID = "";
    static String ICCData1 = "";
    @BindView(R.id.progress_update)
    ProgressBar progressBar;

    @BindView(R.id.cashwithlayout)
    CoordinatorLayout coordinatorLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cash_withdrawal);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        context = this;
        show = (TextView) findViewById(R.id.mcr_show);
        showDataText = (TextView) findViewById(R.id.mcrcarddata);

        mHandler = new Handler() {
            public void handleMessage(Message msg) {
                if (msg.what == CHECKCARD) {
                    if (EmvThread == null) {
                        show.setText("ANZA MUAMALA!");
                        EmvThread = new ProcessICThread();
                        EmvThread.start();
                    }
                } else if (msg.what == REFRESH) {
                    show.setText(strShow);
                }
                super.handleMessage(msg);
            }
        };

        if (mThread == null) {
            show.setText(strShow + "INGIZA KADI....");
            mThread = new CheckIcCardThread();
            mThread.start();
        }


    }

    private class ProcessICThread extends Thread {
        public void run() {

            strShow = "";
            try {
                contactCard = iccReader.enableCard(IccReader.ICCARD_SLOT, IccReader.CARD_VCC_5V, TRUE);
            } catch (IOException | IccException e) {
                e.printStackTrace();
            }

            ResponseApdu responseApdu = null;


            //1PAY.SYS.DDF01-Chip
            byte[] PAY_SYS_DDF01 = {(byte) 0x00, (byte) 0xa4, (byte) 0x04, (byte) 0x00
                    , (byte) 0x0e, (byte) 0x31, (byte) 0x50, (byte) 0x41, (byte) 0x59,
                    (byte) 0x2e, (byte) 0x53, (byte) 0x59, (byte) 0x53, (byte) 0x2e, (byte) 0x44, (byte) 0x44,
                    (byte) 0x46, (byte) 0x30, (byte) 0x31, (byte) 0x00};

            //2PAY.SYS.DDF01-NFC
            byte[] select_Dir = new byte[]{
                    (byte) 0x00, (byte) 0xa4, (byte) 0x04, (byte) 0x00, (byte) 0x0e,
                    (byte) 0x32, (byte) 0x50, (byte) 0x41, (byte) 0x59, (byte) 0x2e,
                    (byte) 0x53, (byte) 0x59, (byte) 0x53, (byte) 0x2e, (byte) 0x44,
                    (byte) 0x44, (byte) 0x46, (byte) 0x30, (byte) 0x31
            };

            //Select CC Applet
            byte[] SELECT_UMOJA = new byte[]{
                    (byte) 0x00, (byte) 0xa4, (byte) 0x04, (byte) 0x00, (byte) 7,
                    (byte) 0xa0, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x65,
                    (byte) 0x00, (byte) 0x00
            };


            //a0 00 00 00 03 10 10
            byte[] SELECT_VISA = new byte[]{
                    (byte) 0x00, (byte) 0xa4, (byte) 0x04, (byte) 0x00, (byte) 7,
                    (byte) 0xa0, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x03,
                    (byte) 0x10, (byte) 0x10
            };

            //Send GET PROCESSING OPTIONS command/80A80000028300
            byte[] GET_PROCESSING_OPTIONS = new byte[]{
                    (byte) 0x80, (byte) 0xA8, (byte) 0x00, (byte) 0x00, (byte) 0x02,
                    (byte) 0x83, (byte) 0x00
            };

            byte[] READ_UMOJA_1 = {(byte) 0x00, (byte) 0xB2, (byte) 0x02, (byte) 0x14, (byte) 0x00};


            byte SW1;
            byte SW2;


            CommandApdu commandApdu = new CommandApdu(PAY_SYS_DDF01);


            try {
                responseApdu = iccReader.transmit(contactCard, commandApdu);
                SW1 = (byte) responseApdu.getSW1();
                SW2 = (byte) responseApdu.getSW2();

                if (SW1 == (byte) 0x90 && SW2 == (byte) 0x00) {
                    Log.e(TAG, "1PAY.SYS.DDF01" + responseApdu);
                    Log.e(TAG, "PAY.SYS.DDF01:" + byteToString(responseApdu.getData(), responseApdu.getData().length));
                    commandApdu = new CommandApdu(SELECT_UMOJA);

                    responseApdu = iccReader.transmit(contactCard, commandApdu);

                    SW1 = (byte) responseApdu.getSW1();
                    SW2 = (byte) responseApdu.getSW2();

                    if (SW1 == (byte) 0x90 && SW2 == (byte) 0x00) {
                        Log.e(TAG, "SELECT_APPLET:" + responseApdu.getSW());
                        Log.e(TAG, "SELECT_APPLET:" + byteToString(responseApdu.getData(), responseApdu.getData().length));

                        AID = getAID(byteToString(responseApdu.getData(), responseApdu.getData().length));

                        commandApdu = new CommandApdu(GET_PROCESSING_OPTIONS);

                        responseApdu = iccReader.transmit(contactCard, commandApdu);

                        SW1 = (byte) responseApdu.getSW1();
                        SW2 = (byte) responseApdu.getSW2();

                        if (SW1 == (byte) 0x90 && SW2 == (byte) 0x00) {
                            Log.e(TAG, "GET_PROCESSING_OPTIONS" + responseApdu);
                            Log.e(TAG, "GET_PROCESSING_OPTIONS:" + byteToString(responseApdu.getData(), responseApdu.getData().length));

                            commandApdu = new CommandApdu(READ_UMOJA_1);
                            responseApdu = iccReader.transmit(contactCard, commandApdu);

                            SW1 = (byte) responseApdu.getSW1();
                            SW2 = (byte) responseApdu.getSW2();

                            if (SW1 == (byte) 0x90 && SW2 == (byte) 0x00) {
                                Log.e(TAG, "GET_DATA_02" + responseApdu);
                                Log.e(TAG, "GET_DATA_02:" + byteToString(responseApdu.getData(), responseApdu.getData().length));

                                ICCData1 = byteToString(responseApdu.getData(), responseApdu.getData().length);

                                getPAN(byteToString(responseApdu.getData(), responseApdu.getData().length));

                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {

                                        InstantCardActivation();
                                    }
                                });


                            } else {
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {

                                        WrongAID("Kadi ina tatizo. Tafadhali jaribu tena.");

                                    }
                                });

                            }


                        } else {
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {

                                    WrongAID("Kadi ina tatizo. Tafadhali jaribu tena.");

                                }
                            });

                        }


                    } else {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {

                                WrongAID("Kadi ina tatizo. Tafadhali jaribu tena.");

                            }
                        });

                    }


                } else {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {

                            WrongAID("Kadi ambazo sio za UMOJA haziruhusiwi.");

                        }
                    });
                }


            } catch (IOException | IccException e) {
                e.printStackTrace();
            }

            if (contactCard != null) {
                try {
                    iccReader.disableCard(contactCard);
                } catch (IOException | IccException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            Message msg = new Message();
            msg.what = REFRESH;
            mHandler.sendMessage(msg);
            EmvThread = null;
        }
    }

    private String byteToString(byte[] arg, int length) {
        String str = "", strTemp = "";
        int temp;
        for (int i = 0; i < length; i++) {
            temp = (int) arg[i] & 0xff;
            if (temp <= 0xf) {
                strTemp = "0";
                strTemp += Integer.toHexString(arg[i] & 0xff);
            } else {
                strTemp = Integer.toHexString(arg[i] & 0xff);
            }

            str = str + strTemp;
        }
        return str;
    }

    private class CheckIcCardThread extends Thread {
        public void run() {
            System.out.println("iccCheck....");


            while (blnCheck) {

                boolean bret = false;

                try {
                    bret = iccReader.check(IccReader.ICCARD_SLOT);
                } catch (IOException | IccException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                if (bret) {
                    blnCheck = false;
                    try {
                        emvl2.emvSetReadCardType((byte) 0x02);
                        contactCard = iccReader.enableCard(
                                IccReader.ICCARD_SLOT, IccReader.CARD_VCC_5V,
                                true);
                    } catch (IOException | IccException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    Message msg = new Message();
                    msg.what = CHECKCARD;
                    mHandler.sendMessage(msg);
                }
            }
            mThread = null;
        }
    }

    private boolean InstantCardActivation() {
        String masked = maskNumber(sixteenDigit, "####xxxxxxxx####");

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                context);

        alertDialogBuilder.setTitle("ATM CHAPCHAP");
        alertDialogBuilder.setIcon(R.mipmap.cardz);

        LayoutInflater li = LayoutInflater.from(context);
        final View dialogView = li.inflate(R.layout.card_activation, null);

        final TextView account = dialogView
                .findViewById(R.id.accountDialog);
        account.setText(masked);

        final EditText AccountNumber = dialogView
                .findViewById(R.id._AccountNumber);
        final EditText PhoneNumber = dialogView
                .findViewById(R.id._phoneNumber);

        alertDialogBuilder.setView(dialogView);

        alertDialogBuilder.setPositiveButton(R.string.confirm,
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {

                        if (AccountNumber.getText().toString().trim().isEmpty()) {

                            WrongAID("AccountNumber Inaitajika");
                            return;
                        }
                        if (PhoneNumber.getText().toString().trim().isEmpty()) {

                            WrongAID("PhoneNumber Inaitajika");
                            return;
                        }

                        if (PhoneNumber.getText().toString().length() == 10) {

                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                            String createdBy = prefs.getString("createdby", null);
                            String branch = prefs.getString("branch", null);

                            final HashMap<String, String> params = new HashMap<>();
                            params.put("pan", sixteenDigit);
                            params.put("AccountNumber", AccountNumber.getText().toString());
                            params.put("phonenumber", PhoneNumber.getText().toString());
                            params.put("CreatedBy", createdBy);
                            params.put("BranchID", branch);

                            if (isNetworkAvailable(getApplicationContext())) {

                                new MaterialStyledDialog.Builder(CardActivation.this)
                                        .setTitle("THIBITISHA ATM CHAPCHAP")
                                        .setDescription("AKAUNTI: " + AccountNumber.getText().toString() + "\n" +
                                                "NAMBA YA SIMU: " + PhoneNumber.getText().toString())
                                        .setIcon(R.mipmap.mucoba)
                                        .setPositiveText("OK")
                                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                                            @Override
                                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                                makeJsonObjectRequest(params);

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
                                Error("No internet Connectivity....");
                            }
                        } else {
                            Error("Namba sio sahihi.");
                        }

                    }

                });

        alertDialogBuilder.setNegativeButton(R.string.reject,
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {

                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);


                    }

                });

        AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.show();
        return true;

    }

    private String maskNumber(String number, String mask) {

        int index = 0;
        StringBuilder masked = new StringBuilder();
        for (int i = 0; i < mask.length(); i++) {
            char c = mask.charAt(i);
            if (c == '#') {
                masked.append(number.charAt(index));
                index++;
            } else if (c == 'x') {
                masked.append(c);
                index++;
            } else {
                masked.append(c);
            }
        }
        return masked.toString();
    }

    private void Error(String message) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setMessage(message).setTitle("Oops!")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent next = new Intent(getApplicationContext(), MainActivity.class);
                        next.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(next);

                    }
                });


        android.app.AlertDialog alert = builder.create();
        alert.show();
    }

    private boolean isNetworkAvailable(final Context context) {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }


    private void getPAN(String st) {
        int index;
        index = st.indexOf("5a");
        if (index != -1) {
            sixteenDigit = st.substring(index + 4, index + 20);

            Log.e(TAG, "PAN" + sixteenDigit);

        }


    }

    private String getAID(String st) {
        int index;
        String aid = null;

        index = st.indexOf("84");
        if (index != -1) {
            aid = st.substring(index + 4, index + 18).toUpperCase();

            Log.e(TAG, "AID:" + aid);
        }

        return aid;

    }

    private void makeJsonObjectRequest(HashMap<String, String> params) {

        final android.app.AlertDialog dialog = new SpotsDialog(context);

        dialog.show();

        JsonObjectRequest req = new JsonObjectRequest(Config.CardActivation, new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getString("response").equals("200")) {

                                dialog.dismiss();


                                Success("OK");


                            } else {
                                dialog.dismiss();

                                Success("Tafadhali jaribu tena...");

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                dialog.dismiss();

                Success("Tafadhali jaribu tena...");
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

    private void Success(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message).setTitle("ATM CHAPCHAP")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        Intent next = new Intent(getApplicationContext(), MainActivity.class);
                        next.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(next);


                    }
                });


        AlertDialog alert = builder.create();
        alert.show();
    }

    private void WrongAID(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message).setTitle("ILANI")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        Intent next = new Intent(getApplicationContext(), MainActivity.class);
                        next.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(next);


                    }
                });


        AlertDialog alert = builder.create();
        alert.show();
    }

}

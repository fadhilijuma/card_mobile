package ke.co.lightspace.yetumobile.activity.transactions;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
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

public class BalanceInquiry extends MyBaseActivity {
    private Thread mThread = null;
    private Thread EmvThread = null;
    private Handler mHandler = null;
    private TextView show;
    private String strShow = "";
    protected static final String TAG = CashWithdrawal.class.getName();
    protected static final int CHECKCARD = 0;
    protected static final int REFRESH = 1;
    private TextView showDataText;
    private Context context = this;
    boolean blnCheck = true;
    Emvl2 emvl2 = Emvl2.getInstance();
    IccReader iccReader = IccReader.getInstance();
    ContactCard contactCard = null;
    static String sixteenDigit = "";
    static String expireYear = "";
    static String expireMonth = "";
    static String track2 = "";
    static String AID = "";
    static String expirydate = "";
    static String ICCData1 = "";
    static String ICCData2 = "";
    @BindView(R.id.progress_update)
    ProgressBar progressBar;
    static String ApplicationCryptogram = "";
    static String ApplicationTransactionCounter = "";
    static String IssuerApplicationData = "";
    static String TransactionCurrencyCode = "0834";
    static String TransactionType = "21";
    static String TransactionDate = "";
    static String UnPredictableNumber = "";
    static String ApplicationInterchangeProfile = "1800";
    static String Terminal_Type = "22";
    static String TerminalVerificationResults = "0000000000";
    static String TerminalCountryCode = "0834";
    static String AmountOther = "000000000000";
    static String AmountAuthorized = "000000000000";
    static String CardData = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.balance_main);
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

            byte[] READ_UMOJA_2 = {(byte) 0x00, (byte) 0xB2, (byte) 0x03, (byte) 0x14, (byte) 0x00};

            byte[] READ_VISA = {(byte) 0x00, (byte) 0xB2, (byte) 0x01, (byte) 0x0c, (byte) 0x4f};

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
                                getEXPIRYDATE(byteToString(responseApdu.getData(), responseApdu.getData().length));

                                commandApdu = new CommandApdu(READ_UMOJA_2);

                                responseApdu = iccReader.transmit(contactCard, commandApdu);

                                if (SW1 == (byte) 0x90 && SW2 == (byte) 0x00) {

                                    Log.e(TAG, "GET_DATA_03" + responseApdu);
                                    Log.e(TAG, "GET_DATA_03:" + byteToString(responseApdu.getData(), responseApdu.getData().length));

                                    ICCData2 = byteToString(responseApdu.getData(), responseApdu.getData().length);

                                    getTRACK2(byteToString(responseApdu.getData(), responseApdu.getData().length));

                                    generateAC();

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

    public boolean BalanceInq() {
        String masked = maskNumber(sixteenDigit, "####xxxxxxxx####");

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                context);

        alertDialogBuilder.setTitle("SALIO");
        alertDialogBuilder.setIcon(R.mipmap.loan);

        LayoutInflater li = LayoutInflater.from(context);
        final View dialogView = li.inflate(R.layout.balance_dialog, null);

        final EditText pin = dialogView
                .findViewById(R.id.pinNumber);
        final TextView account = dialogView
                .findViewById(R.id.accountDialog);
        account.setText(masked);


        alertDialogBuilder.setView(dialogView);

        alertDialogBuilder.setPositiveButton(R.string.confirm,
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {

                        if (pin.getText().toString().length()==4) {

                            CardData = "9F2608" + ApplicationCryptogram + "5F2A02" + TransactionCurrencyCode + "9C01" + TransactionType +
                                    "9A03" + TransactionDate + "9F3704" + UnPredictableNumber + "8202" + ApplicationInterchangeProfile +
                                    "9F3602" + ApplicationTransactionCounter + "9F3501" + Terminal_Type + "9505" + TerminalVerificationResults +
                                    "9F1A02" + TerminalCountryCode + "9F0306" + AmountOther + "9F0206" + AmountAuthorized + "9F1012" + IssuerApplicationData;

                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                            String branch = prefs.getString("branch", null);
                            String createdBy = prefs.getString("createdby", null);
                            String gl = prefs.getString("GL", null);

                            final HashMap<String, String> params = new HashMap<>();
                            params.put("pan", sixteenDigit);
                            params.put("pin", encrypt("da0k188qL5OiY3eX", "_VSUrIqGV2pHSye1", pin.getText().toString()));
                            params.put("expiry", expirydate);
                            params.put("track2", track2);
                            params.put("phonenumber", createdBy);
                            params.put("BranchId", branch);
                            params.put("glcode", gl);
                            params.put("ICC", CardData);
                            params.put("tranType", "BI");

                            if (isNetworkAvailable(getApplicationContext())) {

                                makeJsonObjectRequest(params);


                            } else {
                                Error("No internet Connectivity....");
                            }
                        }else{
                            Error("PIN Sio sahihi.");
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

    public String maskNumber(String number, String mask) {

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

    public boolean isNetworkAvailable(final Context context) {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }


    public void getPAN(String st) {
        int index;
        index = st.indexOf("5a");
        if (index != -1) {
            sixteenDigit = st.substring(index + 4, index + 20);

            Log.e(TAG, "PAN" + sixteenDigit);

        }


    }

    public String getTRACK2(String st) {
        int index;
        index = st.indexOf("57");
        if (index != -1) {
            track2 = st.substring(index + 4, index + 32);

            Log.e(TAG, "TRACK_2" + track2);
        }
        String trk = track2.replace("d", "=").replace("f", "");

        Log.e(TAG, "TRKD" + trk);
        track2 = trk;

        return trk;

    }

    public void getEXPIRYDATE(String st) {
        int index;
        index = st.indexOf("5f24");
        if (index != -1) {
            expireYear = st.substring(index + 6, index + 8);
            expireMonth = st.substring(index + 8, index + 10);

            expirydate = expireYear + expireMonth;

            Log.e(TAG, "EXPIRE_DATE:" + expirydate);
        }

    }

    public String getAID(String st) {
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

        JsonObjectRequest req = new JsonObjectRequest(Config.bal, new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getString("response").equals("200")) {

                                dialog.dismiss();


                                Success("OK");


                            } else {
                                dialog.dismiss();
                                WrongAID("Tafadhali jaribu tena baadaye");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                dialog.dismiss();

                WrongAID("Tafadhali jaribu tena baadaye");
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


    private void generateAC() {

        SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
        String currentDate = sdf.format(new Date());
        TransactionDate = currentDate;

        byte[] command;
        ResponseApdu responseApdu;
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        byte[] authorizedAmount = fromHexString(AmountAuthorized);//9F0206
        byte[] secondaryAmount = fromHexString(AmountOther);//9F0306
        byte[] CountryCode = fromHexString(TerminalCountryCode);//9F1A02
        byte[] tvr = fromHexString(TerminalVerificationResults);//9505
        byte[] transactionCurrencyCode = fromHexString(TransactionCurrencyCode);//5F2A02
        byte[] transactionDate = fromHexString(currentDate);//9A03
        byte[] transactionType = fromHexString(TransactionType);//9C01
        byte[] terminalUnpredictableNumber = fromHexString(generateRandom(8));//9F3704
        byte[] TerminalType = fromHexString(Terminal_Type);//9F3501
        byte[] CVM = fromHexString("00 00 00");//9F3403
        byte[] AIP = fromHexString("180000");//8202
        byte[] padding = fromHexString("000000000000000000000000000000000000000000008080808080808080808080808000000000000000000000000000000000");//9F3602
        //byte[] IssueApplicationData = Util.fromHexString("80");//9F1032

        buf.write(authorizedAmount, 0, authorizedAmount.length);
        buf.write(secondaryAmount, 0, secondaryAmount.length);
        buf.write(CountryCode, 0, CountryCode.length);
        buf.write(tvr, 0, tvr.length);
        buf.write(transactionCurrencyCode, 0, transactionCurrencyCode.length);
        buf.write(transactionDate, 0, transactionDate.length);
        buf.write(transactionType, 0, transactionType.length);
        buf.write(terminalUnpredictableNumber, 0, terminalUnpredictableNumber.length);
        buf.write(TerminalType, 0, TerminalType.length);
        buf.write(CVM, 0, CVM.length);
        buf.write(AIP, 0, AIP.length);
        buf.write(padding, 0, padding.length);
        // buf.write(IssueApplicationData, 0, IssueApplicationData.length);
        //0x40 = TC
        //0x80 = ARQC
        command = generateAC_APDU((byte) 0x40, buf.toByteArray());

        CommandApdu generateACResponse = new CommandApdu(command);

        //'9000' indicates a successful execution of the command.

        try {
            responseApdu = iccReader.transmit(contactCard, generateACResponse);
            byte SW1 = (byte) responseApdu.getSW1();
            byte SW2 = (byte) responseApdu.getSW2();

            String Response = byteToString(responseApdu.getData(), responseApdu.getData().length);
            Log.e(TAG, "GENERATE_AC_COMMAND:" + Response);

            getApplicationTransactionCounter(Response);

            if (SW1 == (byte) 0x90 && SW2 == (byte) 0x00) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {

                        BalanceInq();
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
        } catch (IOException | IccException e) {
            e.printStackTrace();
        }


    }

    private void getApplicationTransactionCounter(String st) {
        int index;
        index = st.indexOf("9f26");
        if (index != -1) {
            ApplicationCryptogram = st.substring(index + 6, index + 22).toUpperCase();

            Log.e(TAG, "ApplicationCryptogram:" + ApplicationCryptogram);
        }
        index = st.indexOf("9f36");
        if (index != -1) {
            ApplicationTransactionCounter = st.substring(index + 6, index + 10).toUpperCase();

            Log.e(TAG, "ApplicationTransactionCounter:" + ApplicationTransactionCounter);
        }
        index = st.indexOf("9f10");
        if (index != -1) {
            IssuerApplicationData = st.substring(index + 6, index + 42).toUpperCase();

            Log.e(TAG, "IssuerApplicationData:" + IssuerApplicationData);
        }

    }

    private static String generateRandom(int length) {
        String characters = "0123456789";
        String randomNumber;
        if (length <= 0) {
            throw new IllegalArgumentException("String length must be a positive integer");
        }

        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(characters.charAt(random.nextInt(characters.length())));
        }

        randomNumber = sb.toString();
        UnPredictableNumber = randomNumber;

        return randomNumber;
    }

    private static byte[] generateAC_APDU(byte referenceControlParameterP1, byte[] transactionRelatedData) {
        if (transactionRelatedData == null) {
            throw new IllegalArgumentException("Param 'transactionRelatedData' cannot be null");
        }
        byte[] cmd = new byte[5 + transactionRelatedData.length + 1];
        cmd[0] = (byte) 0x80;
        cmd[1] = (byte) 0xAE;
        cmd[2] = referenceControlParameterP1;
        cmd[3] = 0x00;
        cmd[4] = (byte) transactionRelatedData.length;
        System.arraycopy(transactionRelatedData, 0, cmd, 5, transactionRelatedData.length);
        cmd[cmd.length - 1] = 0x00; //Le
        return cmd;
    }

    public static byte[] fromHexString(String encoded) {
        encoded = removeSpaces(encoded);
        if (encoded.length() == 0) {
            return new byte[0];
        }
        if ((encoded.length() % 2) != 0) {
            throw new IllegalArgumentException("Input string must contain an even number of characters: " + encoded);
        }
        final byte result[] = new byte[encoded.length() / 2];
        final char enc[] = encoded.toCharArray();
        for (int i = 0; i < enc.length; i += 2) {
            StringBuilder curr = new StringBuilder(2);
            curr.append(enc[i]).append(enc[i + 1]);
            result[i / 2] = (byte) Integer.parseInt(curr.toString(), 16);
        }
        return result;
    }

    public static String removeSpaces(String s) {
        return s.replaceAll(" ", "");
    }

    private void Success(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message).setTitle("SALIO")
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
        builder.setMessage(message).setTitle("UJUMBE")
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

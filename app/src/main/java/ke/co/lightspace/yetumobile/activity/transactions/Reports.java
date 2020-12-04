package ke.co.lightspace.yetumobile.activity.transactions;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.borax12.materialdaterangepicker.date.DatePickerDialog;
import com.borax12.materialdaterangepicker.time.RadialPickerLayout;
import com.borax12.materialdaterangepicker.time.TimePickerDialog;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import justtide.ThermalPrinter;
import ke.co.lightspace.yetumobile.R;
import ke.co.lightspace.yetumobile.activity.db.MainDB;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;


public class Reports extends AppCompatActivity implements
        DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    private TextView dateTextView;
    private static String agentName = null, Branch = null;
    public MainDB dbs;
    public SQLiteDatabase db;

    ThermalPrinter thermalPrinter = ThermalPrinter.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reports);
        dateTextView = (TextView) findViewById(R.id.date_textview);
        Button dateButton = (Button) findViewById(R.id.date_button);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        agentName = prefs.getString("AgentNames", null);
        Branch = prefs.getString("branch", null);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(toolbar);

        Calendar now = Calendar.getInstance();
        DatePickerDialog dpd = com.borax12.materialdaterangepicker.date.DatePickerDialog.newInstance(
                Reports.this,
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
        );
        dpd.show(getFragmentManager(), "Datepickerdialog");

        dateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar now = Calendar.getInstance();
                DatePickerDialog dpd = com.borax12.materialdaterangepicker.date.DatePickerDialog.newInstance(
                        Reports.this,
                        now.get(Calendar.YEAR),
                        now.get(Calendar.MONTH),
                        now.get(Calendar.DAY_OF_MONTH)
                );
                dpd.show(getFragmentManager(), "Datepickerdialog");
            }
        });
    }


    @Override
    public void onResume() {
        super.onResume();
        DatePickerDialog dpd = (DatePickerDialog) getFragmentManager().findFragmentByTag("Datepickerdialog");
        if (dpd != null) dpd.setOnDateSetListener(this);
    }


    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth, int yearEnd, int monthOfYearEnd, int dayOfMonthEnd) {

        SharedPreferences prefs = getDefaultSharedPreferences(getApplicationContext());
        prefs.edit().putString("From", year + "-" + (++monthOfYear) + "-" + dayOfMonth).apply();
        prefs.edit().putString("To", yearEnd + "-" + (++monthOfYearEnd) + "-" + dayOfMonthEnd).apply();
        String date = "Date Range: From- " + year + "-" + (++monthOfYear) + "-" + dayOfMonth + " To " + yearEnd + "-" + (++monthOfYearEnd) + "-" + dayOfMonthEnd;
        prefs.edit().putString("Date", date).apply();
        dateTextView.setText(date);
    }

    @Override
    public void onTimeSet(RadialPickerLayout radialPickerLayout, int i, int i1, int i2, int i3) {

    }

    public void printPDF(View v) {

        try {

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

            String from = prefs.getString("From", null);
            String to = prefs.getString("To", null);

            createRangeTable(from, to);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createRangeTable(String startDate, String endDate) throws ParseException {

        dbs = new MainDB(getApplicationContext());
        db = dbs.getWritableDatabase();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = dateFormat.parse(startDate);
        Date date2 = dateFormat.parse(endDate);
        long unixTime = date.getTime() / 1000;
        long unixTime2 = date2.getTime() / 1000;

        printLogo();

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
        this.thermalPrinter.print("TAREHE: " + DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime()) + "\n\n");

        this.thermalPrinter.print("=====RIPOTI YA MIAMALA=====\n\n");

        String query = "SELECT * from  TRANSACTIONS WHERE  Date BETWEEN '" + unixTime + "'  AND '" + unixTime2 + "' ";


        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            this.thermalPrinter.print("KUWEKA\n\n");
            do {
                String Amount=cursor.getString(cursor.getColumnIndex("Deposit"));

                if (Amount.isEmpty()){
                }else{

                    String Account=cursor.getString(cursor.getColumnIndex("Account"));
                    DecimalFormat dFormat = new DecimalFormat("####,###,###.00");
                    double DepA = Double.parseDouble(Amount);

                    this.thermalPrinter.print(Account + "=> TSHS." + dFormat.format(DepA) + "\n");
                }




            } while (cursor.moveToNext());
        }
        cursor.close();
        DecimalFormat dFormats= new DecimalFormat("####,###,###.00");
        double DepTotal = Double.parseDouble(getDTotal());
        this.thermalPrinter.print(" " + "\n");
        this.thermalPrinter.print("TOTAL.==>> TSHS." + dFormats.format(DepTotal) + "\n\n");
        this.thermalPrinter.print("----------------------------" + "\n");

        String query2 = "SELECT * from  TRANSACTIONS WHERE Date BETWEEN '" + unixTime + "'  AND '" + unixTime2 + "' ";

        Cursor cursor2 = db.rawQuery(query2, null);

        if (cursor2.moveToFirst()) {
            this.thermalPrinter.print("KUTOA\n\n");

            do {
                String Amount=cursor2.getString(cursor2.getColumnIndex("Withdrawal"));
                String Account=cursor2.getString(cursor2.getColumnIndex("Account"));
                if (Amount.isEmpty()){
                }else{

                    DecimalFormat dFormat = new DecimalFormat("####,###,###.00");
                    double withD = Double.parseDouble(Amount);
                    this.thermalPrinter.print(Account + "=> TSHS." + dFormat.format(withD) + "\n");

                }


            } while (cursor2.moveToNext());
        }
        cursor2.close();

        double WithTotal = Double.parseDouble(getWTotal());
        this.thermalPrinter.print(" " + "\n");
        this.thermalPrinter.print("TOTAL.==>> TSHS." + dFormats.format(WithTotal) + "\n\n");
        this.thermalPrinter.print("----------------------------" + "\n");




        this.thermalPrinter.print("      Asante kwa kubenki nasi.\n\n");
        this.thermalPrinter.print("          Mucoba Bank Plc\n");
        this.thermalPrinter.print("           Benki yako,\n");
        this.thermalPrinter.print("       Kwa maendeleo yako.\n\n\n");
        this.thermalPrinter.shiftRight(60);
        this.thermalPrinter.printStart();
        this.thermalPrinter.waitForPrintFinish();
        printSpace();

        db.close();

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

    private String getDTotal() {
        String totalDeposits;
        Cursor cur = db.rawQuery("SELECT SUM(Amount) AS TOTAL FROM TRANSACTIONS Where TransType='CR'", null);
        if (cur.moveToFirst()) {
            totalDeposits = cur.getString(cur.getColumnIndex("TOTAL"));
        } else {
            totalDeposits = "0.00";
        }


        cur.close();

        return totalDeposits;
    }

    private String getWTotal() {
        String totalDeposits;
        Cursor cursor1 = db.rawQuery("SELECT SUM(Amount) AS TOTAL FROM TRANSACTIONS where TransType='DR'", null);
        if (cursor1.moveToFirst()) {
            totalDeposits = cursor1.getString(cursor1.getColumnIndex("TOTAL"));
        } else {
            totalDeposits = "0.00";
        }
        cursor1.close();
        db.close();

        return totalDeposits;
    }


}

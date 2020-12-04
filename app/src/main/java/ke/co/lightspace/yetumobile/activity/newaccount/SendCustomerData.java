package ke.co.lightspace.yetumobile.activity.newaccount;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import ke.co.lightspace.yetumobile.R;
import ke.co.lightspace.yetumobile.activity.adapter.AndroidMultiPartEntity;
import ke.co.lightspace.yetumobile.activity.config.Config;
import ke.co.lightspace.yetumobile.activity.main.MainActivity;
import ke.co.lightspace.yetumobile.activity.db.MainDB;
import ke.co.lightspace.yetumobile.activity.main.MyBaseActivity;

public class SendCustomerData extends MyBaseActivity {
    ListView lstView;
    public MainDB dbs;
    public SQLiteDatabase db;
    private ArrayAdapter<String> listAdapter;
    public static String fname;
    @BindView(R.id.txtPercentage)
    TextView txtPercentage;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    private String filePath = null;
    long totalSize = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_customer_data);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(toolbar);

        initToolbar();

        lstView = (ListView) findViewById(R.id.lstSample);

        initialize();

        lstView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // TODO Auto-generated method stub

                fname = (((TextView) view).getText()).toString();
                try {

                    if (isNetworkAvailable(getApplicationContext())) {

                        ExecuteFileUpload();

                    } else {
                        showError("No internet Connectivity....");
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        lstView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

                fname = (((TextView) view).getText()).toString();

                delete(fname);

                return false;
            }
        });
    }


    private void initialize() {
        ArrayList<String> group_name = person(this);
        listAdapter = new ArrayAdapter<String>(SendCustomerData.this, R.layout.simplerow, group_name);
        lstView.setAdapter(listAdapter);
    }

    private ArrayList<String> person(Context context) {
        ArrayList<String> first_name = new ArrayList<String>();
        try {
            dbs = new MainDB(context);
            db = dbs.getWritableDatabase();
            Cursor cursor = db.rawQuery("SELECT LastName FROM tb_customer", null);
            if (cursor.moveToFirst()) {
                do {
                    String fname = cursor.getString(0);
                    first_name.add(fname);
                } while (cursor.moveToNext());
            }
            cursor.close();
            db.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return first_name;
    }


    public boolean isNetworkAvailable(final Context context) {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }

    private void showError(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message).setTitle("Oops!")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // do nothing

                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);

                    }
                });


        AlertDialog alert = builder.create();
        alert.show();
    }

    private void success(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message).setTitle("Success")
                .setCancelable(false)
                .setIcon(R.mipmap.tick)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // do nothing

                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);

                    }
                });


        AlertDialog alert = builder.create();
        alert.show();
    }

    private void delete(final String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message).setTitle("Delete Item")
                .setCancelable(false)
                .setIcon(R.mipmap.tick)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dbs = new MainDB(SendCustomerData.this);
                        db = dbs.getWritableDatabase();
//                    String updateQuery = "Update TB_CUSTOMERS set sync_status = '" + "True" + "' where LastName='" + fname + "'";
//                    db.execSQL(updateQuery);

                        String delete = "DELETE FROM tb_customer  where LastName='" + message + "'";//where LastName='" + fname + "'", null
                        db.execSQL(delete);
                        db.close();

                        initialize();

                    }
                });


        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * Method to show alert dialog
     */
    private void showAlert(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.mipmap.tick);
        builder.setMessage(message).setTitle("Response from Servers")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // do nothing
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void ExecuteFileUpload() {

        dbs = new MainDB(SendCustomerData.this);
        db = dbs.getWritableDatabase();


        try {

            Cursor cursor = db.rawQuery("SELECT FaceImagePath,IDImagePath FROM tb_customer where LastName='" + fname + "'", null);

            while (cursor.moveToNext()) {

                String faceImage=cursor.getString(0);
                String csign=cursor.getString(1);

                startActivity(new Intent(SendCustomerData.this, UploadActivity.class)
                        .putExtra("face", faceImage)
                        .putExtra("ID", csign)
                        .putExtra("LastName", fname)
                        .putExtra("isImage", true));

            }

            cursor.close();

        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        db.close();

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

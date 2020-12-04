package ke.co.lightspace.yetumobile.activity.existing;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.blackbox_vision.datetimepickeredittext.view.DatePickerInputEditText;
import ke.co.lightspace.yetumobile.R;
import ke.co.lightspace.yetumobile.activity.main.MyBaseActivity;

public class ExistingCustomerBio extends MyBaseActivity {


    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.phoneNumber)
    EditText phone;
    @BindView(R.id.clientID)
    EditText ID;
    @BindView(R.id.account_type)
    MaterialBetterSpinner accountType;
    @BindView(R.id.saveData)
    Button Save;
    @BindView(R.id.coordinatorLayout)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.datePickerInputEditText)
    DatePickerInputEditText dateOfBirth;
    @BindView(R.id.radioSex)
    RadioGroup radioGroup;
    String gender;
    String title;
    String branchcode;
    String createdby;
    @BindView(R.id.inputDistrict)
    EditText DistrictOfBirth;
    @BindView(R.id.inputIDNumber)
    EditText IDNumber;
    @BindView(R.id.id_type)
    MaterialBetterSpinner TypeOfID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.existing_customer_bio);

        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        initToolbar();

        dateOfBirth.setManager(getSupportFragmentManager());

        DistrictOfBirth.setFilters(new InputFilter[]{new InputFilter.AllCaps()});

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String productsString = prefs.getString("products", null);
        branchcode = prefs.getString("branch", null);
        createdby = prefs.getString("createdby", null);



        String[] products = new String[0];
        if (productsString != null) {
            products = productsString.split("#");
        }

        ArrayAdapter<String> type_id = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, IDTYPE);

        TypeOfID.setAdapter(type_id);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, products);

        accountType.setAdapter(adapter);

        Save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (accountType.getText().toString().trim().isEmpty()) {

                    ShowSnack("Account Type required");
                    return;
                }
                if (phone.getText().toString().trim().isEmpty()) {

                    ShowSnack("PhoneNumber required");
                    return;
                }
                if (ID.getText().toString().trim().isEmpty()) {

                    ShowSnack("Client ID required");
                    return;
                }
                int selectedId = radioGroup.getCheckedRadioButtonId();
                if (selectedId == R.id.radioMale) {
                    gender = "M";
                    title = "Mr";

                } else if (selectedId == R.id.radioFemale) {
                    gender = "F";
                    title = "Mrs";

                } else {
                    ShowSnack("Gender required");
                    return;

                }
                if (DistrictOfBirth.getText().toString().trim().isEmpty()) {

                    ShowSnack("District of Birth required");
                    return;
                }
                if (IDNumber.getText().toString().trim().isEmpty()) {

                    ShowSnack("IDNumber required");
                    return;
                }

                new MaterialStyledDialog.Builder(ExistingCustomerBio.this)
                        .setTitle("Confirm Customer Details!")
                        .setDescription("AccountType  : " + accountType.getText().toString() + "\n" +
                                "MobileNumber: " + phone.getText().toString() + "\n" +
                                "District    : " + DistrictOfBirth.getText().toString() + "\n" +
                                "Gender      : " + gender + "\n" +
                                "IDNumber    : " + IDNumber.getText().toString() + "\n" +
                                "DateOfBirth : " + dateOfBirth.getText().toString() + "\n" +
                                "IDType : " + TypeOfID.getText().toString() + "\n" +
                                "ClientID    : " + ID.getText().toString())
                        .setIcon(R.mipmap.mucoba)
                        .setPositiveText("OK")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                final String phoneN = phone.getText().toString().replaceFirst("0", "255");
//                                String[] accounts = accountType.getText().toString().split("-");
//                                String accountCode = accounts[1];

                                String customer = accountType.getText().toString() + "=" + phoneN + "=" + ID.getText().toString()+
                                        "="+branchcode+"="+createdby+"="+dateOfBirth.getText().toString()+"="+DistrictOfBirth.getText().toString()+"="
                                        +IDNumber.getText().toString()+"="+gender+"="+title+"="+TypeOfID.getText().toString();

                                startActivity(new Intent(getApplicationContext(), ExistingFaceImage.class)
                                        .putExtra("data", customer)
                                        .putExtra("phone", phoneN));
                                phone.setText("");
                                ID.setText("");
                                accountType.setText("");

                            }
                        }).setNegativeText("Cancel")
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                            }
                        })
                        //.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_launcher))
                        .show();

            }
        });

    }

    private static final String[] IDTYPE = new String[]{
            "NATIONAL ID", "DRIVING LICENSE", "VOTERS CARD", "PASSPORT"
    };

    public void ShowSnack(String message) {
        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, message, Snackbar.LENGTH_LONG);
        snackbar.show();
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

package ke.co.lightspace.yetumobile.activity.newaccount;

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
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
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

public class OpenAccountIndv extends MyBaseActivity {


    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.inputFirstName)
    EditText firstName;
    @BindView(R.id.phoneNumber)
    EditText phone;
    @BindView(R.id.inputMiddleName)
    EditText midName;
    @BindView(R.id.inputLastName)
    EditText LastName;
    @BindView(R.id.inputIDNumber)
    EditText ID;
    @BindView(R.id.id_type)
    MaterialBetterSpinner TypeOfID;
    @BindView(R.id.inputDistrict)
    EditText DistrictOfBirth;
    @BindView(R.id.radioMale)
    RadioButton male;
    @BindView(R.id.radioFemale)
    RadioButton female;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_account_indv);

        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        initToolbar();

        dateOfBirth.setManager(getSupportFragmentManager());

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String productsString = prefs.getString("products", null);
        branchcode = prefs.getString("branch", null);
        createdby = prefs.getString("createdby", null);

        String[] products = new String[0];
        if (productsString != null) {
            products = productsString.split("#");
        }

        List<String> list = new ArrayList<>(Arrays.asList(products));
        list.remove(2);

        products = list.toArray(new String[0]);

        firstName.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
        midName.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
        LastName.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
        DistrictOfBirth.setFilters(new InputFilter[]{new InputFilter.AllCaps()});

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, products);

        accountType.setAdapter(adapter);

        ArrayAdapter<String> type_id = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, IDTYPE);

        TypeOfID.setAdapter(type_id);


        Save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (firstName.getText().toString().trim().isEmpty()) {

                    ShowSnack("FirstName required");
                    return;
                }
                if (midName.getText().toString().trim().isEmpty()) {

                    ShowSnack("Second Name required");
                    return;
                }
                if (LastName.getText().toString().trim().isEmpty()) {

                    ShowSnack("LastName required");
                    return;
                }
                if (ID.getText().toString().trim().isEmpty()) {

                    ShowSnack("ID Number required");
                    return;
                }
                if (TypeOfID.getText().toString().trim().isEmpty()) {

                    ShowSnack("ID Type required");
                    return;
                }
                if (dateOfBirth.getText().toString().trim().isEmpty()) {

                    ShowSnack("Date of Birth required");
                    return;
                }


                if (DistrictOfBirth.getText().toString().trim().isEmpty()) {

                    ShowSnack("District of Birth required");
                    return;
                }
                if (accountType.getText().toString().trim().isEmpty()) {

                    ShowSnack("Account Type required");
                    return;
                }
                if (phone.getText().toString().trim().isEmpty()) {

                    ShowSnack("PhoneNumber required");
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
                new MaterialStyledDialog.Builder(OpenAccountIndv.this)
                        .setTitle("Confirm Customer Details!")
                        .setDescription("First Name  : " + firstName.getText().toString() + "\n" +
                                "MiddleName  : " + midName.getText().toString() + "\n" +
                                "LastName    : " + LastName.getText().toString() + "\n" +
                                "MobileNumber: " + phone.getText().toString() + "\n" +
                                "District    : " + DistrictOfBirth.getText().toString() + "\n" +
                                "DateOfBirth : " + dateOfBirth.getText().toString() + "\n" +
                                "Gender      : " + gender + "\n" +
                                "IDType      : " + TypeOfID.getText().toString() + "\n" +
                                "IDNumber    : " + ID.getText().toString() + "\n" +
                                "Account     : " + accountType.getText().toString())
                        .setIcon(R.mipmap.mucoba)
                        .setPositiveText("OK")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                final String phoneN = phone.getText().toString().replaceFirst("0", "255");
//                                String[] accounts = accountType.getText().toString().split("-");
//                                String accountCode = accounts[1];

                                String names=firstName.getText().toString()+" "+midName.getText().toString()+" "+LastName.getText().toString();


                                String customer =names+"=" + dateOfBirth.getText().toString() +
                                        "=" + TypeOfID.getText().toString() + "=" + DistrictOfBirth.getText().toString() + "=" + ID.getText().toString() + "=" + accountType.getText().toString()
                                        + "=" + phoneN + "=" + title + "=" + branchcode + "=" + gender + "=" + createdby;

                                startActivity(new Intent(getApplicationContext(), FaceIMage.class)
                                        .putExtra("data", customer)
                                        .putExtra("FirstName", firstName.getText().toString())
                                        .putExtra("LastName", LastName.getText().toString())
                                        .putExtra("phone", phoneN));
                                firstName.setText("");
                                midName.setText("");
                                LastName.setText("");
                                phone.setText("");
                                dateOfBirth.setText("");
                                DistrictOfBirth.setText("");
                                radioGroup.clearCheck();
                                TypeOfID.setText("");
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

package ke.co.lightspace.yetumobile.activity.children;

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

import butterknife.BindView;
import butterknife.ButterKnife;
import io.blackbox_vision.datetimepickeredittext.view.DatePickerInputEditText;
import ke.co.lightspace.yetumobile.R;
import ke.co.lightspace.yetumobile.activity.main.MyBaseActivity;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

public class ChildData extends MyBaseActivity {


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
    @BindView(R.id.saveData)
    Button Save;
    @BindView(R.id.coordinatorLayout)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.datePickerInputEditText)
    DatePickerInputEditText dateOfBirth;

    //Child Names
    @BindView(R.id.inputChildFName)
    EditText ChildFirstName;
    @BindView(R.id.inputChildMiddleName)
    EditText ChildMiddleName;
    @BindView(R.id.inputChildLastName)
    EditText ChildLastName;
    //END

    @BindView(R.id.radioSex)
    RadioGroup radioGroup;

    @BindView(R.id.clientid)
    EditText clientid;

    @BindView(R.id.district)
    EditText district;
    String gender;
    String title;
    String branchcode;
    String createdby;
    @BindView(R.id.id_type_)
    MaterialBetterSpinner TypeOfID;
    @BindView(R.id.identification)
    EditText IDNumber;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.child_data);

        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        initToolbar();

        dateOfBirth.setManager(getSupportFragmentManager());
        ChildFirstName.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
        ChildMiddleName.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
        ChildLastName.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
        firstName.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
        midName.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
        LastName.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
        district.setFilters(new InputFilter[]{new InputFilter.AllCaps()});

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        branchcode = prefs.getString("branch", null);
        createdby = prefs.getString("createdby", null);

        ArrayAdapter<String> type_id = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, IDTYPE);

        TypeOfID.setAdapter(type_id);


        Save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ChildFirstName.getText().toString().trim().isEmpty()) {

                    ShowSnack("FirstName required");
                    return;
                }
                if (ChildMiddleName.getText().toString().trim().isEmpty()) {

                    ShowSnack("SecondName required");
                    return;
                }
                if (ChildLastName.getText().toString().trim().isEmpty()) {

                    ShowSnack("LastName required");
                    return;
                }
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
                if (dateOfBirth.getText().toString().trim().isEmpty()) {

                    ShowSnack("Date of Birth required");
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
                new MaterialStyledDialog.Builder(ChildData.this)
                        .setTitle(R.string.confirmation)
                        .setDescription(getString(R.string.child_f) + ChildFirstName.getText().toString() + "\n" +
                                getString(R.string.child_s) + ChildMiddleName.getText().toString() + "\n" +
                                getString(R.string.child_l) + ChildLastName.getText().toString() + "\n" +
                                getString(R.string.fnameconf) + firstName.getText().toString() + "\n" +
                                getString(R.string.mnameconf) + midName.getText().toString() + "\n" +
                                getString(R.string.lnameconf) + LastName.getText().toString() + "\n" +
                                getString(R.string.mobconf) + phone.getText().toString() + "\n" +
                                getString(R.string.dobconf) + dateOfBirth.getText().toString() + "\n" +
                                getString(R.string.genderconf) + gender + "\n" +
                                getString(R.string.dist) + district.getText().toString() + "\n" +
                                "ID Number: " + IDNumber.getText().toString() + "\n" +
                                "ID Type: " + TypeOfID.getText().toString())
                        .setIcon(R.mipmap.mucoba)
                        .setPositiveText(R.string.okconf)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                final String phoneN = phone.getText().toString().replaceFirst("0", "255");

                                String name_combined=firstName.getText().toString()+" "+midName.getText().toString()+" "+LastName.getText().toString();

                                String child_names=ChildFirstName.getText().toString()+" "+ChildMiddleName.getText().toString()+" "+ChildLastName.getText().toString();

                                String names=name_combined+"-IFO-"+child_names;

                                String customer = names + "=" + dateOfBirth.getText().toString() +
                                        "=" + TypeOfID.getText().toString()+"="+district.getText().toString()+"="+IDNumber.getText().toString()+ "=" + "WATOTO ACCOUNT"+
                                        "="+phoneN+"="+title+"="+branchcode+"="+gender+"="+createdby;

                                SharedPreferences prefs = getDefaultSharedPreferences(getApplicationContext());
                                prefs.edit().putString("custPhone", phoneN).apply();
                                prefs.edit().putString("customer_details", customer).apply();

                                startActivity(new Intent(getApplicationContext(), ChildImage.class));

                                ChildFirstName.setText("");
                                ChildMiddleName.setText("");
                                ChildLastName.setText("");
                                firstName.setText("");
                                midName.setText("");
                                LastName.setText("");
                                phone.setText("");
                                dateOfBirth.setText("");
                                clientid.setText("");
                                radioGroup.clearCheck();


                            }
                        }).setNegativeText(R.string.reject)
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

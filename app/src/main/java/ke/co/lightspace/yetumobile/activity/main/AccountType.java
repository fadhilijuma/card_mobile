package ke.co.lightspace.yetumobile.activity.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import ke.co.lightspace.yetumobile.R;
import ke.co.lightspace.yetumobile.activity.existing.ExistingCustomerBio;
import ke.co.lightspace.yetumobile.activity.newaccount.OpenAccountIndv;
import ke.co.lightspace.yetumobile.activity.transactions.BalanceInquiry;
import ke.co.lightspace.yetumobile.activity.transactions.CashWithdrawal;

public class AccountType extends MyBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_types);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initToolbar();


    }

    public void NewCustomer(View v) {

       startActivity(new Intent(this,OpenAccountIndv.class));
    }

    public void ExistingCustomerAccount(View v) {

        startActivity(new Intent(this,ExistingCustomerBio.class));
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

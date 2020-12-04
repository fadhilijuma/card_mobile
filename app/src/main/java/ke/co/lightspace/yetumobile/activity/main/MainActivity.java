package ke.co.lightspace.yetumobile.activity.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.mikhaellopez.circularimageview.CircularImageView;

import ke.co.lightspace.yetumobile.R;
import ke.co.lightspace.yetumobile.activity.children.ChildData;
import ke.co.lightspace.yetumobile.activity.existing.AccountConnect;
import ke.co.lightspace.yetumobile.activity.newaccount.SendCustomerData;
import ke.co.lightspace.yetumobile.activity.transactions.ATMCardConnect;
import ke.co.lightspace.yetumobile.activity.transactions.CardActivation;
import ke.co.lightspace.yetumobile.activity.transactions.CashDeposit;
import ke.co.lightspace.yetumobile.activity.transactions.TransConnect;


public class MainActivity extends MyBaseActivity {

    private View content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        content = findViewById(R.id.content);


        CircularImageView circularImageView = (CircularImageView)findViewById(R.id.imagv);

        YoYo.with(Techniques.RollIn)
                .playOn(circularImageView);
        CircularImageView send = (CircularImageView)findViewById(R.id.imag2);

        YoYo.with(Techniques.RollIn)
                .playOn(send);
        CircularImageView existing = (CircularImageView)findViewById(R.id.imag);

        YoYo.with(Techniques.RollIn)
                .playOn(existing);
        CircularImageView deposit = (CircularImageView)findViewById(R.id.imagvs);

        YoYo.with(Techniques.RollIn)
                .playOn(deposit);
        CircularImageView children = (CircularImageView)findViewById(R.id.imags);

        YoYo.with(Techniques.RollIn)
                .playOn(children);
        CircularImageView bal = (CircularImageView)findViewById(R.id.imag2s);

        YoYo.with(Techniques.RollIn)
                .playOn(bal);

        circularImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),AccountType.class));
            }
        });
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),ChildData.class));
            }
        });
        existing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),AccountConnect.class));
            }
        });
        children.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),ATMCardConnect.class));
            }
        });
        deposit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),CashDeposit.class));
            }
        });
        bal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),TransConnect.class));
            }
        });

    }


}

package com.example.brandon.SubscriptionsManager;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.EditText;

import java.util.Calendar;
import java.util.Locale;

public class TemplateSubscriptionActivity extends ActionBarActivity {

    private Subscriptions newSubscription;

    private View     subscription;
    private Typeface fontAwesome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.subscription_form_template);

        final Toolbar toolbar = (Toolbar)findViewById(R.id.edit_subscription_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);

        Button deleteSubscription = (Button)findViewById(R.id.deleteSubscription);
        deleteSubscription.setVisibility(View.GONE);

        Bundle extras = getIntent().getExtras();
        newSubscription = (Subscriptions)extras.getSerializable("subscription");
        newSubscription.setFirstBillingDate(today());

        final EditText description = (EditText)findViewById(R.id.description);
        description.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                newSubscription.setDescription(charSequence.toString());
                newSubscription.fillOutView(subscription, fontAwesome);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        final EditText amount = (EditText)findViewById(R.id.amount);
        amount.setText(newSubscription.getAmountString());
        amount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.length() != 0) {
                    try {
                        if (charSequence.charAt(0) != '$') {
                            float value = Float.parseFloat(charSequence.toString());
                            newSubscription.setAmount(value);
                            newSubscription.fillOutView(subscription, fontAwesome);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }else{
                    newSubscription.setAmount(0);
                    newSubscription.fillOutView(subscription, fontAwesome);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        amount.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(!b){
                    amount.setText(newSubscription.getAmountString());
                }else{
                    amount.setText(String.format(Locale.US, "%.2f", newSubscription.getAmount()));
                }
            }
        });

        final EditText billingCycle = (EditText)findViewById(R.id.billingCycle);
        billingCycle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BillingCycleDialogFragment frag = new BillingCycleDialogFragment();
                frag.show(getSupportFragmentManager(), "billing_cycle");
                frag.setOnFinishedListener(new BillingCycleDialogFragment.OnFinishedListener() {
                    @Override
                    public void onFinishedWithResult(int index, String name) {
                        newSubscription.setBillingCycleID(index);
                        newSubscription.fillOutView(subscription, fontAwesome);
                        billingCycle.setText(name);
                    }
                });
            }
        });

        final EditText firstBillingDate = (EditText)findViewById(R.id.firstBillingDate);
        firstBillingDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirstBillingDateDialogFragment frag = new FirstBillingDateDialogFragment();
                frag.setOnFinishedListener(new FirstBillingDateDialogFragment.OnFinishedListener() {
                    @Override
                    public void onFinishedWithResult(String monthName, int day, int year, long time) {
                        String date = String.format(Locale.getDefault(),
                                "%s %d, %d", monthName, day, year);
                        firstBillingDate.setText(date);
                        newSubscription.setFirstBillingDate(time);
                        newSubscription.fillOutView(subscription, fontAwesome);
                    }
                });

                frag.show(getSupportFragmentManager(), "date_selector");
            }
        });

        final EditText reminders = (EditText)findViewById(R.id.reminders);
        reminders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RemindersDialogFragment frag = new RemindersDialogFragment();
                frag.show(getSupportFragmentManager(), "reminders");
                frag.setOnFinishedListener(new RemindersDialogFragment.OnFinishedListener() {
                    @Override
                    public void onFinishedWithResult(int index, String name) {
                        newSubscription.setReminderID(index);
                        newSubscription.fillOutView(subscription, fontAwesome);
                        reminders.setText(name);
                    }
                });
            }
        });

        ViewStub subscriptionStubView = (ViewStub)findViewById(R.id.viewStub);
        subscription = subscriptionStubView.inflate();

        fontAwesome = Typeface.createFromAsset(getAssets(), "fontawesome-webfont.ttf");
        newSubscription.fillOutView(subscription, fontAwesome);
    }

    private long today(){
        return Calendar.getInstance().getTimeInMillis();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit_subscription, menu);
        return true;
    }

    public void createSubscription(MenuItem item) {
        Intent resultIntent = new Intent();

        resultIntent.putExtra("newSubscription", newSubscription);

        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }
}

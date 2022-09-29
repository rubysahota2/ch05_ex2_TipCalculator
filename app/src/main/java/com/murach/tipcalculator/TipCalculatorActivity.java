package com.murach.tipcalculator;

import java.text.NumberFormat;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import android.widget.TextView.OnEditorActionListener;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

public class TipCalculatorActivity extends AppCompatActivity
implements OnEditorActionListener, OnClickListener, AdapterView.OnItemSelectedListener {

    private final int ROUND_NONE = 0;
    private final int ROUND_TIP = 1;
    private final int ROUND_TOTAL = 2;
    // define variables for the widgets
    private EditText billAmountEditText;
    private TextView percentTextView;   
    private Button   percentUpButton;
    private Button   percentDownButton;
    private TextView tipTextView;
    private TextView totalTextView;
    private RadioButton roundNoneRadioButton;
    private RadioButton roundTipRadioButton;
    private RadioButton roundTotalRadioButton;
    private Spinner splitSpinner;
    private TextView perPersonLabel;
    private TextView perPersonTextView;    
    private Button   applyButton;
    
    // define the SharedPreferences object
    private SharedPreferences pref;
    
    // define instance variables that should be saved
    private String billAmountString = "";
    private float tipPercent = .15f;

    private boolean rememberTipPercent = true;
    private int rounding = ROUND_NONE;
        
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tip_calculator);
        
        // get references to the widgets
        billAmountEditText = (EditText) findViewById(R.id.billAmountEditText);
        percentTextView = (TextView) findViewById(R.id.percentTextView);
        percentUpButton = (Button) findViewById(R.id.percentUpButton);
        percentDownButton = (Button) findViewById(R.id.percentDownButton);
        tipTextView = (TextView) findViewById(R.id.tipTextView);
        totalTextView = (TextView) findViewById(R.id.totalTextView);
        roundNoneRadioButton = (RadioButton) findViewById(R.id.roundNoneRadioButton);
        roundTipRadioButton = (RadioButton) findViewById(R.id.roundTipRadioButton);
        roundTotalRadioButton = (RadioButton) findViewById(R.id.roundTotalRadioButton);
        splitSpinner = (Spinner) findViewById(R.id.splitSpinner);
        perPersonLabel = (TextView) findViewById(R.id.perPersonLabel);
        perPersonTextView = (TextView) findViewById(R.id.perPersonTextView);
        applyButton = (Button) findViewById(R.id.applyButton);

        // set array adapter for spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
            this, R.array.split_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item);
        splitSpinner.setAdapter(adapter);
        
        // set the listeners
        billAmountEditText.setOnEditorActionListener(this);
        percentUpButton.setOnClickListener(this);
        percentDownButton.setOnClickListener(this);
        applyButton.setOnClickListener(this);
        //get default values for preferences
        PreferenceManager.setDefaultValues(this, R.xml.root_preferences, false);

        // get SharedPreferences object
        pref = PreferenceManager.getDefaultSharedPreferences(this);
    }
    
    @Override
    public void onPause() {
        // save the instance variables       
        Editor editor = pref.edit();
        editor.putString("billAmountString", billAmountString);
        editor.putFloat("tipPercent", tipPercent);
        editor.commit();        

        super.onPause();      
    }
    
    @Override
    public void onResume() {
        super.onResume();
        
        // get the instance variables
        billAmountString = pref.getString("billAmountString", "");
//        tipPercent = pref.getFloat("tipPercent", 0.15f);
        rememberTipPercent = pref.getBoolean("pref_remember_percent", true);
        rounding = Integer.parseInt(pref.getString("pref_rounding", "0"));
        if(rememberTipPercent){
            tipPercent = pref.getFloat("tipPercent", 0.15f);
        }
        else {
            tipPercent = 0.15f;
        }


        // set the bill amount on its widget
        billAmountEditText.setText(billAmountString);
        
        // calculate and display
        calculateAndDisplay();
    }    
    
    public void calculateAndDisplay() {        

        // get the bill amount
        String billAmountString = billAmountEditText.getText().toString();
        float billAmount; 
        if (billAmountString.equals("")) {
            billAmount = 0;
        }
        else {
            billAmount = Float.parseFloat(billAmountString);
        }
        
        // calculate tip and total 
        float tipAmount = 0;
        float totalAmount = 0;
        //if (rounding == ROUND_NONE)
        if (roundNoneRadioButton.isChecked()) {
            tipAmount = billAmount * tipPercent;
            totalAmount = billAmount + tipAmount;
        }
        //(rounding == ROUND_TIP)
        else if (roundTipRadioButton.isChecked()) {
            tipAmount = StrictMath.round(billAmount * tipPercent);
            totalAmount = billAmount + tipAmount;
        }
        ////(rounding == ROUND_TOTAL)
        else if (roundTotalRadioButton.isChecked()) {
            float tipNotRounded = billAmount * tipPercent;
            totalAmount = StrictMath.round(billAmount + tipNotRounded);
            tipAmount = totalAmount - billAmount;
        }


        // split amount and show/hide split amount widgets
        int splitPosition = splitSpinner.getSelectedItemPosition();
        int split = splitPosition + 1;
        float perPersonAmount = 0;
        if (split == 1) {  // no split - hide widgets
            perPersonLabel.setVisibility(View.GONE);
            perPersonTextView.setVisibility(View.GONE);
        }
        else { // split - show widgets
            perPersonAmount = totalAmount / split;
            perPersonLabel.setVisibility(View.VISIBLE);
            perPersonTextView.setVisibility(View.VISIBLE);
        }        
        
        // display the other results with formatting
        NumberFormat currency = NumberFormat.getCurrencyInstance();
        tipTextView.setText(currency.format(tipAmount));
        totalTextView.setText(currency.format(totalAmount));
        perPersonTextView.setText(currency.format(perPersonAmount));
        
        NumberFormat percent = NumberFormat.getPercentInstance();
        percentTextView.setText(percent.format(tipPercent));
    }
    
    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE ||
    		actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
            calculateAndDisplay();
        }        
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.percentDownButton:
            tipPercent = tipPercent - .01f;
            roundNoneRadioButton.setChecked(true);
            calculateAndDisplay();
            break;
        case R.id.percentUpButton:
            tipPercent = tipPercent + .01f;
            roundNoneRadioButton.setChecked(true);
            calculateAndDisplay();
            break;
        case R.id.applyButton:
            calculateAndDisplay();
            break;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_tip_calculator, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.menu_settings:
                startActivity(new Intent(getApplicationContext(), Settings.class));
//                Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show();
               return true;
            case R.id.menu_about:
                Toast.makeText(this, "About", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
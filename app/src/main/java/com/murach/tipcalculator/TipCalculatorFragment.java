package com.murach.tipcalculator;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.preference.PreferenceManager;

import java.text.NumberFormat;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TipCalculatorFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TipCalculatorFragment extends Fragment implements TextView.OnEditorActionListener , View.OnClickListener, AdapterView.OnItemSelectedListener {
    private final int ROUND_NONE = 0;
    private final int ROUND_TIP = 1;
    private final int ROUND_TOTAL = 2;
    // define variables for the widgets
    private EditText billAmountEditText;
    private TextView percentTextView;
    private Button percentUpButton;
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


    // define instance variables that should be saved
    private String billAmountString = "";
    private float tipPercent = .15f;

    private boolean rememberTipPercent = true;
    private int rounding = ROUND_NONE;

    private SharedPreferences pref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager.setDefaultValues(getActivity(), R.xml.root_preferences, false);
        pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        setHasOptionsMenu(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tip_calculator, container, false);
        // get references to the widgets
        billAmountEditText = (EditText) view.findViewById(R.id.billAmountEditText);
        percentTextView = (TextView) view.findViewById(R.id.percentTextView);
        percentUpButton = (Button) view.findViewById(R.id.percentUpButton);
        percentDownButton = (Button) view.findViewById(R.id.percentDownButton);
        tipTextView = (TextView) view.findViewById(R.id.tipTextView);
        totalTextView = (TextView) view.findViewById(R.id.totalTextView);
        roundNoneRadioButton = (RadioButton) view.findViewById(R.id.roundNoneRadioButton);
        roundTipRadioButton = (RadioButton) view.findViewById(R.id.roundTipRadioButton);
        roundTotalRadioButton = (RadioButton) view.findViewById(R.id.roundTotalRadioButton);
        splitSpinner = (Spinner) view.findViewById(R.id.splitSpinner);
        perPersonLabel = (TextView) view.findViewById(R.id.perPersonLabel);
        perPersonTextView = (TextView) view.findViewById(R.id.perPersonTextView);
        applyButton = (Button) view.findViewById(R.id.applyButton);

        // set array adapter for spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                getActivity(), R.array.split_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        splitSpinner.setAdapter(adapter);

        // set the listeners
        billAmountEditText.setOnEditorActionListener(this);
        percentUpButton.setOnClickListener(this);
        percentDownButton.setOnClickListener(this);
        applyButton.setOnClickListener(this);
        return view;
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
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE ||
                actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
            calculateAndDisplay();
        }
        return false;
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
    public void onPause() {
        // save the instance variables
        SharedPreferences.Editor editor = pref.edit();
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


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

}
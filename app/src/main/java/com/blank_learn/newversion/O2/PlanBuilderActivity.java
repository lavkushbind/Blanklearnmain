package com.blank_learn.newversion.O2;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.blank_learn.dark.R;
// Analytics Imports
import com.google.firebase.analytics.FirebaseAnalytics;
import com.facebook.appevents.AppEventsLogger;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;

import org.json.JSONObject;
import java.util.Currency;

public class PlanBuilderActivity extends AppCompatActivity implements PaymentResultListener {

    // UI Components
    private ProgressBar progressBar;
    private TextView stepTitle, text_skip;
    private Button nextButton;
    private ImageView backButton;
    private Button proceedToPayButton;

    // ViewModel & State
    private PlanBuilderViewModel viewModel;
    private int currentStep = 0;

    // Fragments Array
    private final Fragment[] steps = new Fragment[]{
            new ClassSelectionFragment(),
            new BoardSelectionFragment(),
            new SubjectSelectionFragment(),
            new ScheduleSelectionFragment(),
            new PlanSelectionFragment()
    };
    private final int totalSteps = steps.length;

    // Firebase & Analytics
    private FirebaseAuth mAuth;
    private DatabaseReference dbSelectionsRef;
    private DatabaseReference dbSubscriptionsRef;
    private FirebaseUser currentUser;
    private UserPlanData currentPlanData;

    private FirebaseAnalytics mFirebaseAnalytics;
    private AppEventsLogger fbLogger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_builder);

        // 1. Init Analytics
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        fbLogger = AppEventsLogger.newLogger(this);
        logEvent("plan_builder_started", null);

        viewModel = new ViewModelProvider(this).get(PlanBuilderViewModel.class);

        // UI Init
        progressBar = findViewById(R.id.progress_indicator);
        stepTitle = findViewById(R.id.text_step_title);
        text_skip = findViewById(R.id.text_skip);
        nextButton = findViewById(R.id.button_next);
        backButton = findViewById(R.id.button_previous);
        proceedToPayButton = findViewById(R.id.button_proceed_to_pay);

        // Firebase Init
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please log in first.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        dbSelectionsRef = FirebaseDatabase.getInstance().getReference("user_selections");
        dbSubscriptionsRef = FirebaseDatabase.getInstance().getReference("user_subscriptions");

        // Setup
        progressBar.setMax(totalSteps);
        showStep(currentStep);
        observeViewModel();

        // Listeners
        nextButton.setOnClickListener(v -> {
            if (currentStep < totalSteps - 1) {
                logStepCompletion(currentStep); // Log step analytics
                currentStep++;
                showStep(currentStep);
            }
        });

        backButton.setOnClickListener(v -> goBack());

        proceedToPayButton.setOnClickListener(v -> {
            logEvent("click_proceed_pay", null);
            saveSelectionAndStartPayment();
        });

        text_skip.setOnClickListener(v -> {
            logEvent("click_skip_plan", null);
            navigateToHome();
        });
    }

    private void navigateToHome() {
        Intent intent = new Intent(PlanBuilderActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void showStep(int step) {
        progressBar.setProgress(step + 1, true);
        stepTitle.setText(getStepTitle(step));

        if (step > 0) backButton.setVisibility(View.VISIBLE);
        else backButton.setVisibility(View.INVISIBLE);

        if (step == totalSteps - 1) {
            nextButton.setVisibility(View.GONE);
            proceedToPayButton.setVisibility(View.VISIBLE);
        } else {
            nextButton.setVisibility(View.VISIBLE);
            proceedToPayButton.setVisibility(View.GONE);
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, steps[step])
                .commit();

        validateCurrentStep();
    }

    private void validateCurrentStep() {
        boolean isEnabled = false;
        switch (currentStep) {
            case 0: isEnabled = (viewModel.selectedClass.getValue() != null && !viewModel.selectedClass.getValue().isEmpty()); break;
            case 1: isEnabled = (viewModel.selectedBoard.getValue() != null && !viewModel.selectedBoard.getValue().isEmpty()); break;
            case 2: isEnabled = (viewModel.selectedSubjects.getValue() != null && !viewModel.selectedSubjects.getValue().isEmpty()); break;
            case 3: isEnabled = (viewModel.selectedSlot.getValue() != null && !viewModel.selectedSlot.getValue().isEmpty()); break;
            case 4:
                Integer price = viewModel.selectedPlanPrice.getValue();
                isEnabled = (price != null && price > 0);
                if (isEnabled) proceedToPayButton.setText("Proceed to Pay ₹" + price);
                else proceedToPayButton.setText("Select a Plan");
                proceedToPayButton.setEnabled(isEnabled);
                proceedToPayButton.setAlpha(isEnabled ? 1.0f : 0.5f);
                break;
        }

        if (currentStep < 4) {
            nextButton.setEnabled(isEnabled);
            nextButton.setAlpha(isEnabled ? 1.0f : 0.5f);
        }
    }

    private void observeViewModel() {
        viewModel.selectedClass.observe(this, val -> { if(currentStep == 0) validateCurrentStep(); });
        viewModel.selectedBoard.observe(this, val -> { if(currentStep == 1) validateCurrentStep(); });
        viewModel.selectedSubjects.observe(this, val -> { if(currentStep == 2) validateCurrentStep(); });
        viewModel.selectedSlot.observe(this, val -> { if(currentStep == 3) validateCurrentStep(); });
        viewModel.selectedPlanPrice.observe(this, val -> { if(currentStep == 4) validateCurrentStep(); });
    }

    private void goBack() {
        if (currentStep > 0) {
            currentStep--;
            showStep(currentStep);
        }
    }

    @Override
    public void onBackPressed() {
        if (currentStep > 0) goBack();
        else super.onBackPressed();
    }

    private String getStepTitle(int step) {
        String[] titles = {"Select Class", "Select Board", "Select Subjects", "Select Schedule", "Choose Plan"};
        return titles[step];
    }

    // --- ANALYTICS HELPER ---
    private void logEvent(String eventName, Bundle params) {
        if (params == null) params = new Bundle();
        mFirebaseAnalytics.logEvent(eventName, params);
        fbLogger.logEvent(eventName, params);
    }

    private void logStepCompletion(int step) {
        Bundle params = new Bundle();
        params.putInt("step_number", step + 1);

        if (step == 0 && viewModel.selectedClass.getValue() != null)
            params.putString("selected_class", viewModel.selectedClass.getValue());

        if (step == 1 && viewModel.selectedBoard.getValue() != null)
            params.putString("selected_board", viewModel.selectedBoard.getValue());

        logEvent("plan_step_completed", params);
    }

    // --- PAYMENT LOGIC ---

    private void saveSelectionAndStartPayment() {
        if (currentUser == null || viewModel.selectedClass.getValue() == null) return;

        currentPlanData = new UserPlanData(
                currentUser.getUid(),
                viewModel.selectedClass.getValue(),
                viewModel.selectedBoard.getValue(),
                viewModel.selectedSubjects.getValue(),
                viewModel.selectedSlot.getValue(),
                viewModel.selectedPlanPrice.getValue()
        );

        startRazorpayPayment(currentPlanData.selectedPrice);
    }

    private void startRazorpayPayment(int amount) {
        final Activity activity = this;
        final Checkout co = new Checkout();
        co.setKeyID("rzp_live_6vd9RApruseTAi");

        try {
            JSONObject options = new JSONObject();
            options.put("name", "Blanklearn");
            options.put("description", "Premium Subscription");
            options.put("theme.color", "#FFD700");
            options.put("currency", "INR");
            options.put("amount", amount * 100);
            options.put("prefill.email", currentUser.getEmail());
            co.open(activity, options);

            // Log Initiate Checkout
            Bundle params = new Bundle();
            params.putDouble(FirebaseAnalytics.Param.VALUE, amount);
            params.putString(FirebaseAnalytics.Param.CURRENCY, "INR");
            logEvent(FirebaseAnalytics.Event.BEGIN_CHECKOUT, params);

        } catch (Exception e) {
            Toast.makeText(activity, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPaymentSuccess(String razorpayPaymentID) {
        if (currentPlanData != null) {
            currentPlanData.paymentId = razorpayPaymentID;
            currentPlanData.orderStatus = "paid";

            dbSubscriptionsRef.child(currentUser.getUid()).setValue(currentPlanData)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Subscription Active!", Toast.LENGTH_LONG).show();

                        // Log Purchase Event (Revenue)
                        Bundle params = new Bundle();
                        params.putString(FirebaseAnalytics.Param.TRANSACTION_ID, razorpayPaymentID);
                        params.putDouble(FirebaseAnalytics.Param.VALUE, currentPlanData.selectedPrice);
                        params.putString(FirebaseAnalytics.Param.CURRENCY, "INR");

                        // Facebook Purchase
                        fbLogger.logPurchase(
                                java.math.BigDecimal.valueOf(currentPlanData.selectedPrice),
                                Currency.getInstance("INR"),
                                params
                        );
                        // Google Purchase
                        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.PURCHASE, params);

                        navigateToHome();
                    });
        }
    }

    @Override
    public void onPaymentError(int code, String response) {
        logEvent("payment_failed", null);
        Toast.makeText(this, "Payment Failed", Toast.LENGTH_SHORT).show();
    }
}
package com.blank_learn.Onboarding;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;
import android.widget.TextView;

import com.blank_learn.dark.R;
import com.google.android.material.progressindicator.LinearProgressIndicator;

public class ProfileSetupActivity extends AppCompatActivity {

    private ProfileSetupViewModel viewModel;
    private LinearProgressIndicator progressBar;
    private TextView progressText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_setup);

        // UI एलिमेंट्स को एक्सेस करें
        progressBar = findViewById(R.id.progress_bar);
        progressText = findViewById(R.id.tv_progress_indicator);

        // ViewModel को इस Activity के स्कोप में इनिशियलाइज़ करें
        // यह सुनिश्चित करता है कि सभी फ्रैगमेंट्स एक ही ViewModel इंस्टेंस का उपयोग करें
        viewModel = new ViewModelProvider(this).get(ProfileSetupViewModel.class);

        // यह सुनिश्चित करें कि पहला फ्रैगमेंट सिर्फ एक बार लोड हो
        if (savedInstanceState == null) {
            loadFragment(new Step1NameFragment(), false); // पहले फ्रैगमेंट को बैकस्टैक में न डालें
        }

        // Back Stack में बदलाव सुनने के लिए Listener, ताकि Back बटन दबाने पर प्रोग्रेस बार अपडेट हो
        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            int currentStep = getSupportFragmentManager().getBackStackEntryCount() + 1;
            updateProgress(currentStep);
        });
    }

    /**
     * एक फ्रैगमेंट को दूसरे से बदलता है, साथ में एनिमेशन भी दिखाता है।
     * @param fragment      दिखाने वाला नया फ्रैगमेंट।

     */
    public void navigateToFragment(Fragment fragment) {
        loadFragment(fragment, true);
    }

    private void loadFragment(Fragment fragment, boolean addToBackStack) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        // वर्ल्ड-क्लास UI के लिए स्मूथ स्लाइड एनिमेशन सेट करें
        transaction.setCustomAnimations(
                R.anim.shimmer_animation,  // नया फ्रैगमेंट आने का एनिमेशन
                R.anim.shimmer_animation,  // पुराना फ्रैगमेंट जाने का एनिमेशन
                R.anim.shimmer_animation,   // Back दबाने पर पुराना फ्रैगमेंट वापस आने का एनिमेशन
                R.anim.shimmer_animation  // Back दबाने पर मौजूदा फ्रैगमेंट जाने का एनिमेशन
        );

        transaction.replace(R.id.fragment_container, fragment);

        if (addToBackStack) {
            transaction.addToBackStack(null); // Back बटन दबाने पर वापस आने के लिए
        }

        transaction.commit();
    }

    /**
     * UI पर प्रोग्रेस बार और टेक्स्ट को अपडेट करता है।
     * @param step मौजूदा स्टेप नंबर (1 से शुरू)।
     */
    public void updateProgress(int step) {
        if (step > 5) { // कन्फर्मेशन स्क्रीन के लिए
            progressBar.setProgress(5);
            progressText.setText("Setup Complete!");
        } else {
            progressBar.setProgress(step, true); // true से एनिमेटेड प्रोग्रेस होती है
            progressText.setText("Step " + step + " of 5");
        }
    }
}
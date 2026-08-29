package com.blank_learn.newversion.O2;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.blank_learn.dark.databinding.FragmentPlanSelectionBinding;

import java.util.ArrayList;
import java.util.Arrays;

public class PlanSelectionFragment extends Fragment implements PlanAdapter.OnPlanSelectedListener {

    private FragmentPlanSelectionBinding binding;
    private PlanAdapter planAdapter;
    private ArrayList<Plan> planList;
    private PlanBuilderViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPlanSelectionBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ViewModel initialization
        viewModel = new ViewModelProvider(requireActivity()).get(PlanBuilderViewModel.class);

        planList = new ArrayList<>();
        // Adapter initialization with listener
        planAdapter = new PlanAdapter(requireContext(), planList, this);

        setupRecyclerView();
        // Load only the two requested demo plans
        loadDemoPlans();
    }

    private void setupRecyclerView() {
        binding.plansRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.plansRecyclerView.setAdapter(planAdapter);
    }

    /**
     * यह मेथड सिर्फ दो Paid Trial Session (Demo Class) के ऑप्शन्स लोड करता है।
     */
    private void loadDemoPlans() {
        planList.clear();

        // --- 1. TRIAL: GROUP SESSION (Max 3 Students) ---
        planList.add(new Plan(
                "\uD83D\uDCAC Trial: Group Session (Max 5 Students)",
                29, // Price: ₹49 only
                "1-Hour Demo Class", // Duration
                new ArrayList<>(Arrays.asList(
                        "Single 1-hour interactive trial class.",
                        "Cost-effective demo designed for teamwork.",
                        "Payment required to secure slot.",
                        "Monthly fees: ₹1499"

                ))
        ));

        // --- 2. TRIAL: 1-on-1 SESSION (Single Student) ---
        planList.add(new Plan(
                "\uD83C\udf93 Trial: 1-on-1 Session (Individual)",
                49, // Price: ₹69 only
                "1-Hour Demo Class", // Duration
                new ArrayList<>(Arrays.asList(
                        "Single 1-hour private live trial class.",
                        "Complete personalized attention and mentorship.",
                        "Payment required to secure slot.",
                        "Monthly fees: ₹3999"

                ))
        ));

        planAdapter.notifyDataSetChanged();
    }

    // जब यूजर कोई प्लान सेलेक्ट करता है
    @Override
    public void onPlanSelected(Plan plan) {
        // ViewModel को अपडेट करें
        viewModel.selectedPlanPrice.setValue(plan.getPrice());
//        viewModel.selectedPlanName.setValue(plan.getPlanName());

        Toast.makeText(getContext(), plan.getPlanName() + " selected! Price: ₹" + plan.getPrice(), Toast.LENGTH_LONG).show();

        // Note: यहाँ से आपको Time Slot Selection Fragment पर नेविगेट करना होगा।
        // आप PlanBuilderActivity में navigation logic define कर सकते हैं।
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
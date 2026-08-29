package com.blank_learn.newversion.O2;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

public class PlanBuilderViewModel extends ViewModel {
    public final MutableLiveData<String> selectedClass = new MutableLiveData<>();
    public final MutableLiveData<String> selectedBoard = new MutableLiveData<>();
    // अब यह एक लिस्ट स्टोर करेगा
    public MutableLiveData<List<String>> selectedSubjects = new MutableLiveData<>();
    public final MutableLiveData<String> selectedSlot = new MutableLiveData<>();
    public final MutableLiveData<Integer> selectedPlanPrice = new MutableLiveData<>();

 }
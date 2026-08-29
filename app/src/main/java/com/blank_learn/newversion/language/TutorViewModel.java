package com.blank_learn.newversion.language;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class TutorViewModel extends ViewModel {
    // Note: Hum Native Lang ko hardcode nahi kar rahe, selection se lenge
    public final MutableLiveData<String> nativeLanguage = new MutableLiveData<>("Hindi");
    public final MutableLiveData<String> targetLanguage = new MutableLiveData<>();
    public final MutableLiveData<String> selectedLevel = new MutableLiveData<>();
}
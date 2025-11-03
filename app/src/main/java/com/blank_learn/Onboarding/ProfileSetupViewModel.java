package com.blank_learn.Onboarding;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

// यह क्लास सभी फ्रैगमेंट्स में डेटा शेयर करने के लिए है।
// यह स्क्रीन रोटेट होने पर भी डेटा को सुरक्षित रखती है।
public class ProfileSetupViewModel extends ViewModel {

    // ChildProfile ऑब्जेक्ट को रखने के लिए एक प्राइवेट, बदलने योग्य LiveData।
    private final MutableLiveData<ChildProfile> profileData = new MutableLiveData<>();

    // ChildProfile ऑब्जेक्ट को सिर्फ पढ़ने के लिए एक पब्लिक LiveData।
    // फ्रैगमेंट्स इसे "observe" (देख) सकते हैं।
    public LiveData<ChildProfile> getProfileData() {
        return profileData;
    }

    // जब ViewModel पहली बार बनता है, तो एक खाली ChildProfile ऑब्जेक्ट बना दो।
    public ProfileSetupViewModel() {
        profileData.setValue(new ChildProfile());
    }

    // --- डेटा को अपडेट करने के लिए मेथड्स ---

    public void setName(String name) {
        ChildProfile currentProfile = profileData.getValue();
        if (currentProfile != null) {
            currentProfile.setName(name);
            profileData.setValue(currentProfile); // LiveData को अपडेट करो ताकि observers को पता चले।
        }
    }

    public void setGrade(String grade) {
        ChildProfile currentProfile = profileData.getValue();
        if (currentProfile != null) {
            currentProfile.setGrade(grade);
            profileData.setValue(currentProfile);
        }
    }

    public void setBoard(String board) {
        ChildProfile currentProfile = profileData.getValue();
        if (currentProfile != null) {
            currentProfile.setBoard(board);
            profileData.setValue(currentProfile);
        }
    }

    public void setSubjects(List<String> subjects) {
        ChildProfile currentProfile = profileData.getValue();
        if (currentProfile != null) {
            currentProfile.setSubjects(subjects);
            profileData.setValue(currentProfile);
        }
    }

    public void setPreferredTiming(String timing) {
        ChildProfile currentProfile = profileData.getValue();
        if (currentProfile != null) {
            currentProfile.setPreferredTiming(timing);
            profileData.setValue(currentProfile);
        }
    }
}
// SetupViewModel.java
package com.example.aap.ui.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ProfileViewModel extends ViewModel {

    private final MutableLiveData<String> mText;
    private final MutableLiveData<Boolean> _hasNavigated;
    public final LiveData<Boolean> hasNavigated;

    public ProfileViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Choose your goal");

        _hasNavigated = new MutableLiveData<>();
        _hasNavigated.setValue(false);
        hasNavigated = _hasNavigated;
    }

    // Text LiveData methods
    public void setText(String text) {
        mText.setValue(text);
    }

    public LiveData<String> getText() {
        return mText;
    }

    // hasNavigated LiveData methods
    public void setHasNavigated(boolean value) {
        _hasNavigated.setValue(value);
    }

    public LiveData<Boolean> getNavigated(boolean value) {
        return hasNavigated;
    }
}

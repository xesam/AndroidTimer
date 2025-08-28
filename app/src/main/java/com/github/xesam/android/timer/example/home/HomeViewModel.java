package com.github.xesam.android.timer.example.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HomeViewModel extends ViewModel {

    private final MutableLiveData<String> mText;
    private final MutableLiveData<String> mCountDownText;

    public HomeViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("?:?");
        mCountDownText = new MutableLiveData<>();
        mCountDownText.setValue("?:?");
    }

    public LiveData<String> getText() {
        return mText;
    }

    public void setText(String text) {
        mText.setValue(text);
    }

    public LiveData<String> getCountDownText() {
        return mCountDownText;
    }

    public void setCountDownText(String text) {
        mCountDownText.setValue(text);
    }
}
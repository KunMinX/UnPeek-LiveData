package com.kunminx.event;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Create by KunMinX at 2020/7/9
 */
public class EventLiveData<T> extends MutableLiveData<T> {

    private boolean hasHandled;
    private boolean isDelaying;

    @Override
    public void observe(@NonNull LifecycleOwner owner, @NonNull Observer<? super T> observer) {

        if (!hasHandled) {
            hasHandled = true;
            isDelaying = true;
            Timer timer = new Timer();
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    isDelaying = false;
                    EventLiveData.super.setValue(null);
                }
            };
            timer.schedule(task, 1000);
            super.observe(owner, observer);
        } else if (isDelaying) {
            super.observe(owner, observer);
        }
    }

    @Override
    public void setValue(T value) {
        hasHandled = false;
        isDelaying = false;
        super.setValue(value);
    }

    @Override
    public void postValue(T value) {
        hasHandled = false;
        isDelaying = false;
        super.postValue(value);
    }
}

package com.kunminx.event;

import androidx.lifecycle.LiveData;

/**
 * Create by KunMinX at 2020/7/3
 */
public class EventLiveData<T> extends LiveData<Event<T>> {

    public EventLiveData() {
        super();
    }

    public void postEvent(T value) {
        super.postValue(new Event<>(value));
    }

    public void setEvent(T value) {
        super.setValue(new Event<>(value));
    }
}

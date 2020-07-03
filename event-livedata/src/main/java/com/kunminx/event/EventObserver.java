package com.kunminx.event;

import androidx.lifecycle.Observer;

/**
 * Create by KunMinX at 2020/7/3
 */
public class EventObserver<T> implements Observer<Event<T>> {

    private Observer<T> mTObserver;

    public EventObserver(Observer<T> TObserver) {
        mTObserver = TObserver;
    }

    @Override
    public void onChanged(Event<T> event) {
        mTObserver.onChanged(event.getContent());
    }
}

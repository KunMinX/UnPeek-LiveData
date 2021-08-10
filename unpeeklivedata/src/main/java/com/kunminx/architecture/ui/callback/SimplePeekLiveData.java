package com.example.roomdemo.livedatabus;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicInteger;

public class SimplePeekLiveData<T> extends LiveData<T> {

    private final AtomicInteger currentVersion = new AtomicInteger(-1);

    @Override
    public void observe(@NonNull @NotNull LifecycleOwner owner, @NonNull @NotNull Observer<? super T> observer) {
        super.observe(owner, new ObserverWrapper(observer, currentVersion.get()));
    }

    @Override
    public void observeForever(@NonNull @NotNull Observer<? super T> observer) {
        super.observeForever(new ObserverWrapper(observer, currentVersion.get()));
    }

    @Override
    public void setValue(T value) {
        currentVersion.getAndIncrement();
        super.setValue(value);
    }

    @Override
    public void postValue(T value) {
        currentVersion.getAndIncrement();
        super.postValue(value);
    }

    class ObserverWrapper implements Observer<T> {
        private final Observer<? super T> observer;
        private int mVersion = -1;

        public ObserverWrapper(@NotNull Observer<? super T> observer, int mVersion) {
            this.observer = observer;
            this.mVersion = mVersion;
        }

        @Override
        public void onChanged(T t) {
            if (currentVersion.get() > mVersion) {
                observer.onChanged(t);
            }
        }
    }

}

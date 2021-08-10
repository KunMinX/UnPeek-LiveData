package com.kunminx.architecture.ui.callback;

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Create by RebornWolfman at 2021/8/10
 */
public class SimpleUnPeekLiveData<T> extends LiveData<T> {

  private final AtomicInteger currentVersion = new AtomicInteger(-1);

  @Override
  public void observe(@NonNull LifecycleOwner owner, @NonNull Observer<? super T> observer) {
    super.observe(owner, createObserverWrapper(observer, currentVersion.get()));
  }

  @Override
  public void observeForever(@NonNull Observer<? super T> observer) {
    super.observeForever(createObserverWrapper(observer, currentVersion.get()));
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

  /**
   * 添加一个包装类，添加一个版本号判断
   * 重写equals方法和hashCode 忽略版本号的判断，
   * 好处1：那么在removeObserver的时候只需要传入你实际创建的Observer那么就不需要添加集合来自己缓存
   * 好处2：就是你在添加监听的时候也是不需要自己去重复，源码livedata里面已经有了判断
   */
  class ObserverWrapper implements Observer<T> {
    private final Observer<? super T> observer;
    private int mVersion = -1;

    public ObserverWrapper(@NonNull Observer<? super T> observer, int mVersion) {
      this.observer = observer;
      this.mVersion = mVersion;
    }

    @Override
    public void onChanged(T t) {
      if (currentVersion.get() > mVersion) {
        observer.onChanged(t);
      }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      ObserverWrapper that = (ObserverWrapper) o;
      return Objects.equals(observer, that.observer);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public int hashCode() {
      return Objects.hash(observer);
    }
  }

  private ObserverWrapper createObserverWrapper(@NonNull Observer<? super T> observer, int version) {
    return new ObserverWrapper(observer, version);
  }

  @Override
  public void removeObserver(@NonNull Observer<? super T> observer) {
    super.removeObserver(createObserverWrapper(observer, -1));
  }
}

package com.kunminx.architecture.ui.callback;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * TODO V7 版源码完善自小伙伴 RebornWolfman 在 issue 中分享，
 * https://github.com/KunMinX/UnPeek-LiveData/issues/17
 * <p>
 * 相比 V6 版改进在于：
 * 通过在 "代理类/包装类" 中自行维护一个版本号，在 UnPeekLiveData 中维护一个当前版本号，
 * 分别来在 setValue 和 Observe 的时机来改变和对齐版本号，
 * 如此使得无需另外管理一个 Observer map，从而进一步规避内存管理问题，
 * 这是继 V6 版源码以来，最简源码设计。
 *
 * <p>
 * <p>
 * TODO 唯一可信源设计
 * 我们在 V6 中继续沿用从 V3 版 "唯一可信源" 理念设计，
 * 确保 "事件" 发送权牢牢握在可信逻辑中枢手里，从而确保所有订阅者收到消息皆可靠且致，
 * <p>
 * 如这么说无体会，详见《吃透 LiveData 本质，享用可靠消息鉴权机制》解析：
 * https://xiaozhuanlan.com/topic/6017825943
 * <p>
 * TODO 以及支持消息从内存清空
 * 我们在 V6 中继续沿用从 V3 版 "消息清空" 设计，
 * 支持通过 clear 方法手动将消息从内存中清空，
 * 以免无用消息随着 SharedViewModel 长时间驻留而导致内存溢出发生。
 * <p>
 * <p>
 * Create by RebornWolfman, KunMinX at 2021/8/10
 */
@Deprecated
public class ProtectedUnPeekLiveDataV7_1<T> extends LiveData<T> {

  private final static int START_VERSION = -1;

  private final AtomicInteger mCurrentVersion = new AtomicInteger(START_VERSION);

  protected boolean isAllowNullValue;

  /**
   * TODO 当 liveData 用作 event 时，可使用该方法观察 "生命周期敏感" 非粘性消息
   * <p>
   * state 可变且私有，event 只读且公有，
   * state 倒灌应景，event 倒灌不符预期，
   * <p>
   * 如这么说无体会，详见《吃透 LiveData 本质，享用可靠消息鉴权机制》解析：
   * https://xiaozhuanlan.com/topic/6017825943
   *
   * @param owner    activity 传入 this，fragment 建议传入 getViewLifecycleOwner
   * @param observer observer
   */
  @Override
  public void observe(@NonNull LifecycleOwner owner, @NonNull Observer<? super T> observer) {
    super.observe(owner, createObserverWrapper(observer, mCurrentVersion.get()));
  }

  /**
   * TODO 当 liveData 用作 event 时，可使用该方法观察 "生命周期不敏感" 非粘性消息
   *
   * @param observer observer
   */
  @Override
  public void observeForever(@NonNull Observer<? super T> observer) {
    super.observeForever(createObserverForeverWrapper(observer, mCurrentVersion.get()));
  }

  /**
   * TODO 当 liveData 用作 state 时，可使用该方法来观察 "生命周期敏感" 粘性消息
   *
   * @param owner    activity 传入 this，fragment 建议传入 getViewLifecycleOwner
   * @param observer observer
   */
  public void observeSticky(@NonNull LifecycleOwner owner, @NonNull Observer<T> observer) {
    super.observe(owner, createObserverWrapper(observer, START_VERSION));
  }

  /**
   * TODO 当 liveData 用作 state 时，可使用该方法来观察 "生命周期不敏感" 粘性消息
   *
   * @param observer observer
   */
  public void observeStickyForever(@NonNull Observer<? super T> observer) {
    super.observeForever(createObserverForeverWrapper(observer, START_VERSION));
  }

  /**
   * TODO tip：只需重写 setValue
   * postValue 最终还是会经过这里
   *
   * @param value value
   */
  @Override
  protected void setValue(T value) {
    mCurrentVersion.getAndIncrement();
    super.setValue(value);
  }

  /**
   * TODO tip：
   * 1.添加一个包装类，自己维护一个版本号判断，用于无需 map 帮助也能逐一判断消费情况
   * 2.重写 equals 方法和 hashCode，在用于手动 removeObserver 时，忽略版本号的变化引起的变化
   */
  class ObserverWrapper implements Observer<T> {
    private final Observer<? super T> mObserver;
    private int mVersion = START_VERSION;
    private boolean mIsForever;

    public ObserverWrapper(@NonNull Observer<? super T> observer, int version, boolean isForever) {
      this(observer, version);
      this.mIsForever = isForever;
    }

    public ObserverWrapper(@NonNull Observer<? super T> observer, int version) {
      this.mObserver = observer;
      this.mVersion = version;
    }

    @Override
    public void onChanged(T t) {
      if (mCurrentVersion.get() > mVersion && (t != null || isAllowNullValue)) {
        mObserver.onChanged(t);
      }
    }

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
      return Objects.equals(mObserver, that.mObserver);
    }

    @Override
    public int hashCode() {
      return Objects.hash(mObserver);
    }

    @NonNull
    @Override
    public String toString() {
      return mIsForever ? "IS_FOREVER" : "";
    }
  }

  @Override
  public void removeObserver(@NonNull Observer<? super T> observer) {
    if (TextUtils.isEmpty(observer.toString())) {
      super.removeObserver(observer);
    } else {
      super.removeObserver(createObserverWrapper(observer, START_VERSION));
    }
  }

  private ObserverWrapper createObserverForeverWrapper(@NonNull Observer<? super T> observer, int version) {
    return new ObserverWrapper(observer, version, true);
  }

  private ObserverWrapper createObserverWrapper(@NonNull Observer<? super T> observer, int version) {
    return new ObserverWrapper(observer, version);
  }

  /**
   * TODO tip：
   * 手动将消息从内存中清空，
   * 以免无用消息随着 SharedViewModel 长时间驻留而导致内存溢出发生。
   */
  public void clear() {
    super.setValue(null);
  }

}

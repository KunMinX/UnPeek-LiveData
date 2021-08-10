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
 * TODO 感谢小伙伴 RebornWolfman 对 UnPeekLiveData 源码的演化做出的贡献，
 * <p>
 * V7 版源码完善自小伙伴 RebornWolfman 在 issue 中的分享，
 * https://github.com/KunMinX/UnPeek-LiveData/issues/17
 * <p>
 * V7 版源码相比于 V6 版的改进之处在于：
 * 通过在 "代理类/包装类" 中自行维护一个版本号，在 UnPeekLiveData 中维护一个当前版本号，
 * 分别来在 setValue 和 Observe 的时机来改变和对齐版本号，
 * 如此使得无需另外管理一个 Observer map，从而进一步规避了内存管理的问题，
 * 同时也是继 V6 版源码以来，最简的源码设计，方便阅读理解和后续修改。
 *
 * <p>
 * <p>
 * TODO 唯一可信源设计
 * 我们在 V7 中继续沿用从 V3 版延续下来的基于 "唯一可信源" 理念的设计，
 * 来确保 "事件" 的发送权牢牢握在可信的逻辑中枢单元手里，从而确保所有订阅者收到的信息都是可靠且一致的，
 * <p>
 * 如果这样说还不理解，可自行查阅《LiveData 唯一可信源 读写分离设计》的解析：
 * https://xiaozhuanlan.com/topic/2049857631
 * <p>
 * TODO 以及支持消息从内存清空
 * 我们在 V7 中继续沿用从 V3 版延续下来的 "消息清空" 设计，
 * 我们支持通过 clear 方法手动将消息从内存中清空，
 * 以免无用消息随着 SharedViewModel 的长时间驻留而导致内存溢出的发生。
 * <p>
 * <p>
 * Create by RebornWolfman, KunMinX at 2021/8/10
 */
public class ProtectedUnPeekLiveData<T> extends LiveData<T> {

  private final AtomicInteger currentVersion = new AtomicInteger(-1);

  protected boolean isAllowNullValue;

  /**
   * TODO 当 liveData 用作 event 用途时，可使用该方法来观察 "生命周期敏感" 的非粘性消息
   * <p>
   * state 是可变且私用的，event 是只读且公用的，
   * state 的倒灌是应景的，event 倒灌是不符预期的，
   * <p>
   * 如果这样说还不理解，详见《LiveData 唯一可信源 读写分离设计》的解析：
   * https://xiaozhuanlan.com/topic/2049857631
   *
   * @param owner
   * @param observer
   */
  @Override
  public void observe(@NonNull LifecycleOwner owner, @NonNull Observer<? super T> observer) {
    super.observe(owner, createObserverWrapper(observer, currentVersion.get()));
  }

  /**
   * TODO 当 liveData 用作 event 用途时，可使用该方法来观察 "生命周期不敏感" 的非粘性消息
   * <p>
   * state 是可变且私用的，event 是只读且公用的，
   * state 的倒灌是应景的，event 倒灌是不符预期的，
   * <p>
   * 如果这样说还不理解，详见《LiveData 唯一可信源 读写分离设计》的解析：
   * https://xiaozhuanlan.com/topic/2049857631
   *
   * @param observer
   */
  @Override
  public void observeForever(@NonNull Observer<? super T> observer) {
    super.observeForever(createObserverWrapper(observer, currentVersion.get()));
  }

  /**
   * TODO 当 liveData 用作 state 用途时，可使用该方法来观察 "生命周期敏感" 的粘性消息
   * <p>
   * state 是可变且私用的，event 是只读且公用的，
   * state 的倒灌是应景的，event 倒灌是不符预期的，
   * <p>
   * 如果这样说还不理解，详见《LiveData 唯一可信源 读写分离设计》的解析：
   * https://xiaozhuanlan.com/topic/2049857631
   *
   * @param owner
   * @param observer
   */
  public void observeSticky(@NonNull LifecycleOwner owner, @NonNull Observer<T> observer) {
    super.observe(owner, observer);
  }

  /**
   * TODO 当 liveData 用作 state 用途时，可使用该方法来观察 "生命周期不敏感" 的粘性消息
   * <p>
   * state 是可变且私用的，event 是只读且公用的
   * state 的倒灌是应景的，event 倒灌是不符预期的，
   * <p>
   * 如果这样说还不理解，详见《LiveData 唯一可信源 读写分离设计》的解析：
   * https://xiaozhuanlan.com/topic/2049857631
   *
   * @param observer
   */
  public void observeStickyForever(@NonNull Observer<? super T> observer) {
    super.observeForever(observer);
  }


  @Override
  protected void setValue(T value) {
    currentVersion.getAndIncrement();
    super.setValue(value);
  }

  @Override
  protected void postValue(T value) {
    currentVersion.getAndIncrement();
    super.postValue(value);
  }

  /**
   * 1.添加一个包装类，自己维护一个版本号判断，用于无需 map 的帮助也能逐一判断消费情况
   * 2.重写 equals 方法和 hashCode，在用于手动 removeObserver 时，忽略版本号的变化引起的变化
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
      if (currentVersion.get() > mVersion && (t != null || isAllowNullValue)) {
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

  @Override
  public void removeObserver(@NonNull Observer<? super T> observer) {
    super.removeObserver(createObserverWrapper(observer, -1));
  }

  private ObserverWrapper createObserverWrapper(@NonNull Observer<? super T> observer, int version) {
    return new ObserverWrapper(observer, version);
  }

  /**
   * 手动将消息从内存中清空，
   * 以免无用消息随着 SharedViewModel 的长时间驻留而导致内存溢出的发生。
   */
  public void clear() {
    super.setValue(null);
  }
}

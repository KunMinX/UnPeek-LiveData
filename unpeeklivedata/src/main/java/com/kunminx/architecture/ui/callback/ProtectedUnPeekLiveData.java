package com.kunminx.architecture.ui.callback;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TODO 感谢小伙伴 wl0073921、LiWeiGe 对 UnPeekLiveData 源码的演化做出的贡献，
 * <p>
 * V6 版源码翻译和完善自小伙伴 wl0073921 在 issue 中的分享，
 * https://github.com/KunMinX/UnPeek-LiveData/issues/11
 * <p>
 * V6.1 版源码源于小伙伴 LiWeiGe 在 issue 中的启发，
 * https://github.com/KunMinX/UnPeek-LiveData/issues/16
 * <p>
 * V6 版源码相比于 V5 版的改进之处在于，引入 Observer 代理类的设计，
 * 这使得在旋屏重建时，无需通过反射方式跟踪和复用基类 Map 中的 Observer，
 * 转而通过 removeObserver 的方式来自动移除和在页面重建后重建新的 Observer，
 * <p>
 * 因而复杂度由原先的分散于基类数据结构，到集中在 proxy 对象这一处，
 * 进一步方便了源码逻辑的阅读和后续的修改。
 * <p>
 * <p>
 * TODO 唯一可信源设计
 * 我们在 V6 中继续沿用从 V3 版延续下来的基于 "唯一可信源" 理念的设计，
 * 来确保 "事件" 的发送权牢牢握在可信的逻辑中枢单元手里，从而确保所有订阅者收到的信息都是可靠且一致的，
 * <p>
 * 如果这样说还不理解，可自行查阅《LiveData 唯一可信源 读写分离设计》的解析：
 * https://xiaozhuanlan.com/topic/2049857631
 * <p>
 * TODO 以及支持消息从内存清空
 * 我们在 V6 中继续沿用从 V3 版延续下来的 "消息清空" 设计，
 * 我们支持通过 clear 方法手动将消息从内存中清空，
 * 以免无用消息随着 SharedViewModel 的长时间驻留而导致内存溢出的发生。
 * <p>
 * <p>
 * Create by KunMinX at 2021/6/17
 */
public class ProtectedUnPeekLiveData<T> extends LiveData<T> {

  private final static String TAG = "V6Test";

  protected boolean isAllowNullValue;

  private final ConcurrentHashMap<Observer<? super T>, ObserverProxy> observerMap = new ConcurrentHashMap();

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
    Observer<? super T> observer1 = getObserverProxy(observer);
    if (observer1 != null) {
      super.observe(owner, observer1);
    }
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
    Observer<? super T> observer1 = getObserverProxy(observer);
    if (observer1 != null) {
      super.observeForever(observer1);
    }
  }

  private Observer<? super T> getObserverProxy(Observer<? super T> observer) {
    if (observerMap.containsKey(observer)) {
      Log.d(TAG, "observe repeatedly, observer has been attached to owner");
      return null;
    } else {
      ObserverProxy proxy = new ObserverProxy(observer);
      observerMap.put(observer, proxy);
      return proxy;
    }
  }

  private class ObserverProxy implements Observer<T> {

    public final Observer<? super T> target;

    public boolean state;

    public ObserverProxy(Observer<? super T> target) {
      this.target = target;
    }

    @Override
    public void onChanged(T t) {
      if (state) {
        state = false;
        if (t != null || isAllowNullValue) {
          target.onChanged(t);
        }
      }
    }
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
  public void observeSticky(LifecycleOwner owner, Observer<T> observer) {
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
  public void observeStickyForever(Observer<T> observer) {
    super.observeForever(observer);
  }

  @Override
  protected void setValue(T value) {
    if (value != null || isAllowNullValue) {
      for (Map.Entry<Observer<? super T>, ObserverProxy> entry : observerMap.entrySet()) {
        entry.getValue().state = true;
      }
      super.setValue(value);
    }
  }

  @Override
  public void removeObserver(@NonNull Observer<? super T> observer) {
    Observer<? super T> proxy;
    Observer<? super T> target;

    //移除防倒灌的 ObserverProxy，否则就是移除粘性的 Observer
    if (observer instanceof ProtectedUnPeekLiveData.ObserverProxy) {
      proxy = observer;
      target = ((ObserverProxy) observer).target;
    } else {
      proxy = observerMap.get(observer);
      target = (proxy != null) ? observer : null;
    }
    if (proxy != null && target != null) {
      observerMap.remove(target);
      super.removeObserver(proxy);
    }
  }

  /**
   * 手动将消息从内存中清空，
   * 以免无用消息随着 SharedViewModel 的长时间驻留而导致内存溢出的发生。
   */
  public void clear() {
    super.setValue(null);
  }

}

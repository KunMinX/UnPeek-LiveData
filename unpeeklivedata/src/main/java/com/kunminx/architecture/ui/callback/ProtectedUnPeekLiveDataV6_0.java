package com.kunminx.architecture.ui.callback;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TODO V6 版源码翻译和完善自小伙伴 wl0073921 在 issue 中分享，
 * https://github.com/KunMinX/UnPeek-LiveData/issues/11
 * <p>
 * 相比 V5 版改进在于，引入 Observer 代理类设计，
 * 这使旋屏重建时，无需通过反射方式跟踪和复用基类 Map 中 Observer，
 * 转而通过 removeObserver 方式来自动移除和在页面重建后重建新 Observer，
 * <p>
 * 因而复杂度由原先分散于基类数据结构，到集中在 proxy 对象这一处，
 * 进一步方便源码逻辑阅读和后续修改。
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
 * Create by KunMinX at 2021/6/17
 */
@Deprecated
public class ProtectedUnPeekLiveDataV6_0<T> extends LiveData<T> {

  private final static String TAG = "V6Test";

  protected boolean isAllowNullValue;

  private final ConcurrentHashMap<Observer<? super T>, Boolean> observerStateMap = new ConcurrentHashMap();

  private final ConcurrentHashMap<Observer<? super T>, Observer<? super T>> observerProxyMap = new ConcurrentHashMap();

  /**
   * TODO 当 liveData 用作 event 时，可使用该方法观察 "生命周期敏感" 非粘性消息
   * <p>
   * state 可变且私有，event 只读且公有，
   * state 倒灌应景，event 倒灌不符预期，
   * <p>
   * 如这么说无体会，详见《吃透 LiveData 本质，享用可靠消息鉴权机制》解析：
   * https://xiaozhuanlan.com/topic/6017825943
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
   * TODO 当 liveData 用作 event 时，可使用该方法观察 "生命周期不敏感" 非粘性消息
   * <p>
   * state 可变且私有，event 只读且公有，
   * state 倒灌应景，event 倒灌不符预期，
   * <p>
   * 如这么说无体会，详见《吃透 LiveData 本质，享用可靠消息鉴权机制》解析：
   * https://xiaozhuanlan.com/topic/6017825943
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
    if (observerStateMap.containsKey(observer)) {
      Log.d(TAG, "observe repeatedly, observer has been attached to owner");
      return null;
    } else {
      observerStateMap.put(observer, false);
      ObserverProxy proxy = new ObserverProxy(observer);
      observerProxyMap.put(observer, proxy);
      return proxy;
    }
  }

  private class ObserverProxy implements Observer<T> {

    private final Observer<? super T> target;

    public ObserverProxy(Observer<? super T> target) {
      this.target = target;
    }

    public Observer<? super T> getTarget() {
      return target;
    }

    @Override
    public void onChanged(T t) {
      if (observerStateMap.get(target) != null && observerStateMap.get(target)) {
        observerStateMap.put(target, false);
        if (t != null || isAllowNullValue) {
          target.onChanged(t);
        }
      }
    }
  }

  /**
   * TODO 当 liveData 用作 state 时，可使用该方法来观察 "生命周期敏感" 粘性消息
   * <p>
   * state 可变且私有，event 只读且公有，
   * state 倒灌应景，event 倒灌不符预期，
   * <p>
   * 如这么说无体会，详见《吃透 LiveData 本质，享用可靠消息鉴权机制》解析：
   * https://xiaozhuanlan.com/topic/6017825943
   *
   * @param owner
   * @param observer
   */
  public void observeSticky(LifecycleOwner owner, Observer<T> observer) {
    super.observe(owner, observer);
  }

  /**
   * TODO 当 liveData 用作 state 时，可使用该方法来观察 "生命周期不敏感" 粘性消息
   * <p>
   * state 可变且私有，event 只读且公有，
   * state 倒灌应景，event 倒灌不符预期，
   * <p>
   * 如这么说无体会，详见《吃透 LiveData 本质，享用可靠消息鉴权机制》解析：
   * https://xiaozhuanlan.com/topic/6017825943
   *
   * @param observer
   */
  public void observeStickyForever(Observer<T> observer) {
    super.observeForever(observer);
  }

  @Override
  protected void setValue(T value) {
    if (value != null || isAllowNullValue) {
      for (Map.Entry<Observer<? super T>, Boolean> entry : observerStateMap.entrySet()) {
        entry.setValue(true);
      }
      super.setValue(value);
    }
  }

  @Override
  public void removeObserver(@NonNull Observer<? super T> observer) {
    Observer<? super T> proxy;
    Observer<? super T> target;
    if (observer instanceof ProtectedUnPeekLiveDataV6_0.ObserverProxy) {
      proxy = observer;
      target = ((ObserverProxy) observer).getTarget();
    } else {
      proxy = observerProxyMap.get(observer);
      target = (proxy != null) ? observer : null;
    }
    if (proxy != null && target != null) {
      observerProxyMap.remove(target);
      observerStateMap.remove(target);
      super.removeObserver(proxy);
    }
  }

  /**
   * 手动将消息从内存中清空，
   * 以免无用消息随着 SharedViewModel 长时间驻留而导致内存溢出发生。
   */
  public void clear() {
    super.setValue(null);
  }

}

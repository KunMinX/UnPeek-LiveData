
/*
 * Copyright 2018-present KunMinX
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kunminx.architecture.ui.callback;

import androidx.annotation.NonNull;
import androidx.arch.core.internal.SafeIterableMap;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TODO：UnPeekLiveData 的存在是为在 "重回二级页面" 场景下，解决 "数据倒灌" 问题。
 * 对 "数据倒灌" 状况不理解小伙伴，可参考《LiveData 数据倒灌 背景缘由全貌 独家解析》解析
 * <p>
 * https://xiaozhuanlan.com/topic/6719328450
 * <p>
 * <p>
 * 对{@link ProtectedUnPeekLiveDataV4}进行重构，修复了V4版本已知问题：
 * TODO: 1、UnPeekLiveDataV4 中 observers HashMap 恒久存在，注册 Observer 越多，占用内存越大，且除非 UnPeekLiveDataV4 被回收，否则恒久存在内存当中
 * 在 removeObserver 方法中移除 map 中对应存储 storeId
 * <p>
 * TODO: 2、无法通过 removeObserver 方法移除指定 Observer（某些场景需要提前 removeObserver）
 * 通过维护外部传入 Observer 与内部代理 Observer 映射关系，在 removeObserver 调用时，通过反射找到真正注册到 LiveData 中 Observer，实现移除
 * <p>
 * TODO: 3、同一个 Observer 对象，注册多次，UnPeekLiveDataV4 内部实际上会注册多个不同 Observer，从而导致重复回调，产生不可预期问题
 * 内部不会每次调用 observe 方法时都新创建一个代理 Observer，而是复用已经存在代理 Observer
 * 注意！！！Kotlin + LiveData + Lambda 由于编译器优化，可能会抛 Cannot add the same observer with different lifecycles 异常
 * <p>
 * TODO: 4、无法使用 observerForever 方法
 * UnPeekLiveData 内部直接持有 forever 类型 Observer
 * <p>
 * TODO: 最终实现对谷歌原生 LiveData 完全无侵入性目的。在多人协作的场景下，其他同学就只需要理解 UnPeekLiveData 能解决粘性事件、数据倒灌问题，其余无需了解，用法上完全跟原生 LiveData 保持一致
 *
 * <p>
 * TODO：增加一层 ProtectedUnPeekLiveData，
 * 用于限制从 Activity/Fragment 推送数据，推送数据务必通过唯一可信源来分发，
 * 如这么说无体会，详见：
 * https://xiaozhuanlan.com/topic/6719328450 和 https://xiaozhuanlan.com/topic/0168753249
 * <p>
 * <p>
 * Create by Jim at 2021/4/21
 */
@Deprecated
public class ProtectedUnPeekLiveDataV5<T> extends LiveData<T> {

  protected boolean isAllowNullValue;

  private final ConcurrentHashMap<Integer, Boolean> observers = new ConcurrentHashMap<>();

  /**
   * 保存外部传入的 Observer 与代理 Observer 之间映射关系
   */
  private final ConcurrentHashMap<Integer, Integer> observerMap = new ConcurrentHashMap<>();

  /**
   * 这里会持有永久性注册 Observer 对象，因为是永久性注册，必须调用 remove 才会注销，所有这里持有 Observer 对象不存在内存泄漏问题，
   * 因为一旦泄漏，只能说明业务使用方没有 remove
   */
  private final ConcurrentHashMap<Integer, Observer<? super T>> foreverObservers = new ConcurrentHashMap<>();

  private Observer<? super T> createProxyObserver(@NonNull Observer<? super T> originalObserver, @NonNull Integer observeKey) {
    return t -> {
      if (!observers.get(observeKey)) {
        observers.put(observeKey, true);
        if (t != null || isAllowNullValue) {
          originalObserver.onChanged(t);
        }
      }
    };
  }

  @Override
  public void observe(@NonNull LifecycleOwner owner, @NonNull Observer<? super T> observer) {
//    if (owner instanceof Fragment && ((Fragment) owner).getViewLifecycleOwner() != null) {
//      /**
//       * Fragment 场景下使用 getViewLifeCycleOwner 作为 liveData 订阅者，
//       * 如此可确保 "视图实例" 生命周期安全（getView 不为 null），
//       * 因而需要注意的是，getViewLifeCycleOwner 使用应在 onCreateView 之后和 onDestroyView 之前。
//       *
//       * 如这么说无体会，详见《LiveData 鲜为人知 身世背景 和 独特使命》解析
//       * https://xiaozhuanlan.com/topic/0168753249
//       */
//      owner = ((Fragment) owner).getViewLifecycleOwner();
//    }

    Integer observeKey = System.identityHashCode(observer);
    observe(observeKey, owner, observer);
  }

  @Override
  public void observeForever(@NonNull Observer<? super T> observer) {
    Integer observeKey = System.identityHashCode(observer);
    observeForever(observeKey, observer);
  }

  private void observe(@NonNull Integer observeKey,
                       @NonNull LifecycleOwner owner,
                       @NonNull Observer<? super T> observer) {

    if (observers.get(observeKey) == null) {
      observers.put(observeKey, true);
    }

    Observer<? super T> registerObserver;
    if (observerMap.get(observeKey) == null) {
      registerObserver = createProxyObserver(observer, observeKey);
      // 保存外部 Observer 及内部代理 Observer 映射关系
      observerMap.put(observeKey, System.identityHashCode(registerObserver));
    } else {
      // 通过反射拿到真正注册到 LiveData 中 Observer
      Integer registerObserverStoreId = observerMap.get(observeKey);
      assert registerObserverStoreId != null;
      registerObserver = getObserver(this, registerObserverStoreId);
      if (registerObserver == null) {
        registerObserver = createProxyObserver(observer, observeKey);
        // 保存外部 Observer 及内部代理 Observer 映射关系
        observerMap.put(observeKey, System.identityHashCode(registerObserver));
      }
    }

    super.observe(owner, registerObserver);
  }

  private void observeForever(@NonNull Integer observeKey, @NonNull Observer<? super T> observer) {

    if (observers.get(observeKey) == null) {
      observers.put(observeKey, true);
    }

    Observer<? super T> registerObserver = foreverObservers.get(observeKey);
    if (registerObserver == null) {
      registerObserver = createProxyObserver(observer, observeKey);
      foreverObservers.put(observeKey, registerObserver);
    }

    super.observeForever(registerObserver);
  }

  @Override
  public void removeObserver(@NonNull Observer<? super T> observer) {
    Integer observeKey = System.identityHashCode(observer);
    Observer<? super T> registerObserver = foreverObservers.remove(observeKey);
    if (registerObserver == null && observerMap.containsKey(observeKey)) {
      // 反射拿到真正注册到 LiveData 中 observer
      Integer registerObserverStoreId = observerMap.remove(observeKey);
      assert registerObserverStoreId != null;
      registerObserver = getObserver(this, registerObserverStoreId);
    }

    if (registerObserver != null) {
      observers.remove(observeKey);
    }

    super.removeObserver(registerObserver != null ? registerObserver : observer);
  }

  /**
   * 重写的 setValue 方法，默认不接收 null
   * 可通过 Builder 配置允许接收
   * 可通过 Builder 配置消息延时清理时间
   * <p>
   * override setValue, do not receive null by default
   * You can configure to allow receiving through Builder
   * And also, You can configure the delay time of message clearing through Builder
   *
   * @param value
   */
  @Override
  protected void setValue(T value) {
    if (value != null || isAllowNullValue) {
      for (Map.Entry<Integer, Boolean> entry : observers.entrySet()) {
        entry.setValue(false);
      }
      super.setValue(value);
    }
  }

  public void clear() {
    super.setValue(null);
  }

  /**
   * 通过反射，获取指定 LiveData 中 Observer 对象
   *
   * @param liveData         指定的LiveData
   * @param identityHashCode 想要获取的Observer对象的identityHashCode {@code System.identityHashCode}
   * @return
   */
  private Observer<? super T> getObserver(@NonNull LiveData<T> liveData, @NonNull Integer identityHashCode) {

    try {
      Field field = LiveData.class.getDeclaredField("mObservers");
      field.setAccessible(true);
      SafeIterableMap<Observer<? super T>, Object> observers
              = (SafeIterableMap<Observer<? super T>, Object>) field.get(liveData);
      if (observers != null) {
        for (Map.Entry<Observer<? super T>, Object> entry : observers) {
          Observer<? super T> observer = entry.getKey();
          if (System.identityHashCode(observer) == identityHashCode) {
            return observer;
          }
        }
      }
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } catch (NoSuchFieldException e) {
      e.printStackTrace();
    }
    return null;
  }
}


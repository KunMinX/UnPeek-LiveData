
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
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import java.util.HashMap;
import java.util.Map;

/**
 * TODO：UnPeekLiveData 的存在是为在 "重回二级页面" 场景下，解决 "数据倒灌" 问题。
 * 对 "数据倒灌" 状况不理解小伙伴，可参考《LiveData 数据倒灌 背景缘由全貌 独家解析》解析
 * <p>
 * https://xiaozhuanlan.com/topic/6719328450
 * <p>
 * 本类参考官方 SingleEventLive 非入侵设计，
 * 及小伙伴 Flywith24 在 wrapperLiveData 中通过 ViewModelStore 来唯一确定订阅者思路，
 * <p>
 * TODO：当前最新版，我们透过对 ViewModelStore 内存地址遍历，
 * 来确保：
 * 1.一条消息能被多个观察者消费
 * 2.消息被所有观察者消费完毕后才开始阻止倒灌
 * 3.可以通过 clear 方法手动将消息从内存中移除
 * 4.让非入侵设计成为可能，遵循开闭原则
 * <p>
 * TODO：增加一层 ProtectedUnPeekLiveData，
 * 用于限制从 Activity/Fragment 推送数据，推送数据务必通过唯一可信源来分发，
 * 如这么说无体会，详见：
 * https://xiaozhuanlan.com/topic/6719328450 和 https://xiaozhuanlan.com/topic/0168753249
 * <p>
 * Create by KunMinX at 19/9/23
 */
@Deprecated
public class ProtectedUnPeekLiveDataV4<T> extends LiveData<T> {

  protected boolean isAllowNullValue;

  private final HashMap<Integer, Boolean> observers = new HashMap<>();

  /**
   * 适用于 activity 的 observe UnPeek 方法
   * A Observe UnPeek method which suitable for use in an activity
   *
   * @param activity
   * @param observer
   */
//    public void observeInActivity(@NonNull AppCompatActivity activity, @NonNull Observer<? super T> observer) {
//        LifecycleOwner owner = activity;
//        Integer storeId = System.identityHashCode(activity.getViewModelStore());
//        observe(storeId, owner, observer);
//    }

  /**
   * 适用于 fragment 的 observe UnPeek 方法
   * A Observe UnPeek method which suitable for use in an fragment
   *
   * @param fragment
   * @param observer
   */
//    public void observeInFragment(@NonNull Fragment fragment, @NonNull Observer<? super T> observer) {
//        LifecycleOwner owner = fragment.getViewLifecycleOwner();
//        Integer storeId = System.identityHashCode(fragment.getViewModelStore());
//        observe(storeId, owner, observer);
//    }

  /**
   * 通用的 observe UnPeek 方法
   * A universal Observe UnPeek method
   *
   * @param owner
   * @param store
   * @param observer
   */
//    public void observeUnPeek(@NonNull LifecycleOwner owner, @NonNull ViewModelStore store, @NonNull Observer<? super T> observer) {
//        Integer storeId = System.identityHashCode(store);
//        observe(storeId, owner, observer);
//    }
  private void observe(@NonNull Integer storeId,
                       @NonNull LifecycleOwner owner,
                       @NonNull Observer<? super T> observer) {

    if (observers.get(storeId) == null) {
      observers.put(storeId, true);
    }

    super.observe(owner, t -> {
      if (!observers.get(storeId)) {
        observers.put(storeId, true);
        if (t != null || isAllowNullValue) {
          observer.onChanged(t);
        }
      }
    });
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
}


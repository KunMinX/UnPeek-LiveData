/*
 * Copyright 2018-2019 KunMinX
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelStore;

import java.util.HashMap;
import java.util.Map;

/**
 * TODO：UnPeekLiveData 的存在是为了在 "重回二级页面" 的场景下，解决 "数据倒灌" 的问题。
 * 对 "数据倒灌" 的状况不理解的小伙伴，可参考《LiveData 数据倒灌 背景缘由全貌 独家解析》文章开头的解析
 * <p>
 * https://xiaozhuanlan.com/topic/6719328450
 * <p>
 * 本类参考了官方 SingleEventLive 的非入侵设计，
 * 以及小伙伴 Flywith24 在 wrapperLiveData 中通过 ViewModelStore 来唯一确定订阅者的思路，
 * <p>
 * TODO：在当前最新版中，我们透过对 ViewModelStore 和 Observer 的遍历，
 * 来确保：
 * 1.一条消息能被多个观察者消费
 * 2.消息被所有观察者消费完毕后才开始阻止倒灌
 * 3.可以通过 clear 方法手动将消息从内存中移除
 * 4.让非入侵设计成为可能，遵循开闭原则
 * <p>
 * TODO：增加一层 ProtectedUnPeekLiveData，
 * 用于限制从 Activity/Fragment 篡改来自 "数据层" 的数据，数据层的数据务必通过 "唯一可信源" 来分发，
 * 如果这样说还不理解，详见：
 * https://xiaozhuanlan.com/topic/0168753249 和 https://xiaozhuanlan.com/topic/6719328450
 * <p>
 * Create by KunMinX at 19/9/23
 */
public class ProtectedUnPeekLiveData<T> extends LiveData<T> {

    protected boolean isAllowNullValue;

    private final HashMap<ViewModelStore, Boolean> observers = new HashMap<>();
    private final HashMap<Observer<? super T>, ViewModelStore> stores = new HashMap<>();

    public void observeInActivity(@NonNull AppCompatActivity activity, @NonNull Observer<? super T> observer) {

        LifecycleOwner owner = null;
        ViewModelStore store = null;

        if (activity != null) {
            owner = activity;
            store = activity.getViewModelStore();
        }

        observe(store, owner, observer);
    }

    public void observeInFragment(@NonNull Fragment fragment, @NonNull Observer<? super T> observer) {

        LifecycleOwner owner = null;
        ViewModelStore store = null;

        if (fragment != null) {
            owner = fragment.getViewLifecycleOwner();
            store = fragment.getViewModelStore();
        }

        observe(store, owner, observer);
    }

    private void observe(@NonNull ViewModelStore store,
                         @NonNull LifecycleOwner owner,
                         @NonNull Observer<? super T> observer) {

        if (store != null && observers.get(store) == null) {
            observers.put(store, true);
            stores.put(observer, store);
        }

        super.observe(owner, t -> {
            if (store != null) {
                if (!observers.get(store)) {
                    observers.put(store, true);
                    if (t != null || isAllowNullValue) {
                        observer.onChanged(t);
                    }
                }
            }
        });
    }

    @Override
    public void removeObserver(@NonNull Observer<? super T> observer) {
        if (observer == null) {
            return;
        }

        ViewModelStore store = stores.get(observer);
        if (store != null) {
            for (Map.Entry<ViewModelStore, Boolean> entry : observers.entrySet()) {
                if (store.equals(entry.getKey())) {
                    observers.remove(entry.getKey());
                    stores.remove(observer);
                    break;
                }
            }
        }

        super.removeObserver(observer);
    }

    /**
     * 重写的 setValue 方法，默认不接收 null
     * 可通过 Builder 配置允许接收
     * 可通过 Builder 配置消息延时清理的时间
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
            for (Map.Entry<ViewModelStore, Boolean> entry : observers.entrySet()) {
                entry.setValue(false);
            }
            super.setValue(value);
        }
    }

    protected void clear() {
        super.setValue(null);
    }
}

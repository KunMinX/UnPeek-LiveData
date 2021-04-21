
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
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import com.kunminx.architecture.ui.callback.util.LiveDataUtil;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TODO：UnPeekLiveData 的存在是为了在 "重回二级页面" 的场景下，解决 "数据倒灌" 的问题。
 * 对 "数据倒灌" 的状况不理解的小伙伴，可参考《LiveData 数据倒灌 背景缘由全貌 独家解析》文章开头的解析
 * <p>
 * https://xiaozhuanlan.com/topic/6719328450
 * <p>
 *
 * 对{@link ProtectedUnPeekLiveDataV4}进行了重构，修复了V4版本的已知问题：
 * TODO: 1、UnPeekLiveData 中的 observers 这个 HashMap 恒久存在，注册的 Observer 越多，占用的内存越大，并且除非 UnPeekLiveData 被回收，否则恒久存在内存当中
 * 在 removeObserver 方法中移除 map 中对应存储的 storeId
 *
 * TODO: 2、无法通过 removeObserver 方法移除指定的 Observer（某些场景需要提前 removeObserver）
 * 通过维护外部传入 Observer 与内部代理 Observer 的映射关系，在 removeObserver 调用时，通过反射找到真正注册到 LiveData 中的 Observer，实现移除
 *
 * TODO: 3、同一个 Observer 对象，注册多次，UnPeekLiveData 内部实际上会注册了多个不同的 Observer，从而导致重复回调，产生一些不可预期的问题
 * 内部不会每次调用 observe 方法时都新创建一个代理 Observer，而是复用已经存在的代理 Observer
 * 注意！！！Kotlin + LiveData + Lamda 由于编译器优化，可能会抛 Cannot add the same observer with different lifecycles 异常
 *
 * TODO: 4、无法使用 observerForever 方法
 * UnPeekLiveData 内部直接持有 forever 类型的 Observer
 *
 * TODO: 最终实现对谷歌原生 LiveData 完全无侵入性的目的。在多人协作的场景下，其他同学就只需要理解 UnPeekLiveData 能够解决粘性事件、数据倒灌问题，其他的完全不需要理解，用法上完全跟原生 LiveData 保持一致
 *
 * <p>
 * TODO：增加一层 ProtectedUnPeekLiveData，
 * 用于限制从 Activity/Fragment 篡改来自 "数据层" 的数据，数据层的数据务必通过 "唯一可信源" 来分发，
 * 如果这样说还不理解，详见：
 * https://xiaozhuanlan.com/topic/0168753249 和 https://xiaozhuanlan.com/topic/6719328450
 * <p>
 *
 * Create by Jim at 2021/4/21
 */
public class ProtectedUnPeekLiveData<T> extends LiveData<T> {

    protected boolean isAllowNullValue;

    private final ConcurrentHashMap<Integer, Boolean> observers = new ConcurrentHashMap<>();

    /**
     * 保存外部传入的Observer与代理Observer之间的映射关系
     */
    private final ConcurrentHashMap<Integer, Integer> observerMap = new ConcurrentHashMap<>();

    /**
     * 这里会持有永久性注册的Observer对象，因为是永久性注册的，必须调用remove才会注销，所有这里持有Observer对象不存在内存泄漏问题，
     * 因为一旦泄漏了，只能说明是业务使用方没有remove
     */
    private final ConcurrentHashMap<Integer, Observer> foreverObservers = new ConcurrentHashMap<>();

    private Observer<T> createProxyObserver(@NonNull Observer originalObserver, @NonNull Integer storeId) {
        return t -> {
            if (!observers.get(storeId)) {
                observers.put(storeId, true);
                if (t != null || isAllowNullValue) {
                    originalObserver.onChanged(t);
                }
            }
        };
    }

    @Override
    public void observe(@NonNull LifecycleOwner owner, @NonNull Observer<? super T> observer) {
        if (owner instanceof Fragment && ((Fragment) owner).getViewLifecycleOwner() != null) {
            /**
             * 2020年起，Fragment中LiveData传入的LifeCycleOwner从fragment.this改进为getViewLifeCycleOwner。这样设计，主要是为了解决getView()的生命长度比fragment短（仅存活于onCreateView之后和onDestroyView之前），
             * 导致某些时候fragment其他成员还活着，但getView()为null的生命周期安全问题，
             * 也即，在Fragment的场景下，请使用getViewLifeCycleOwner来作为liveData的订阅者。（并且注意，对getViewLifeCycleOwner的使用应在onCreateView之后和onDestroyView之前）
             */
            owner = ((Fragment) owner).getViewLifecycleOwner();
        }

        Integer storeId = System.identityHashCode(observer);
        observe(storeId, owner, observer);
    }

    @Override
    public void observeForever(@NonNull Observer<? super T> observer) {
        Integer storeId = System.identityHashCode(observer);
        observeForever(storeId, observer);
    }

    private void observe(@NonNull Integer storeId,
                         @NonNull LifecycleOwner owner,
                         @NonNull Observer<? super T> observer) {

        if (observers.get(storeId) == null) {
            observers.put(storeId, true);
        }

        Observer registerObserver;
        if (observerMap.get(storeId) == null) {
            registerObserver = createProxyObserver(observer, storeId);
            // 保存外部Observer以及内部代理Observer的映射关系
            observerMap.put(storeId, System.identityHashCode(registerObserver));
        } else {
            // 通过反射拿到真正注册到LiveData中的Observer
            Integer registerObserverStoreId = observerMap.get(storeId);
            registerObserver = LiveDataUtil.getObserver(this, registerObserverStoreId);
            if (registerObserver == null) {
                registerObserver = createProxyObserver(observer, storeId);
                // 保存外部Observer以及内部代理Observer的映射关系
                observerMap.put(storeId, System.identityHashCode(registerObserver));
            }
        }

        super.observe(owner, registerObserver);
    }

    private void observeForever(@NonNull Integer storeId, @NonNull Observer<? super T> observer) {

        if (observers.get(storeId) == null) {
            observers.put(storeId, true);
        }

        Observer registerObserver = foreverObservers.get(storeId);
        if (registerObserver == null) {
            registerObserver = createProxyObserver(observer, storeId);
            foreverObservers.put(storeId, registerObserver);
        }

        super.observeForever(registerObserver);
    }

    @Override
    public void removeObserver(@NonNull Observer<? super T> observer) {
        Integer storeId = System.identityHashCode(observer);
        Observer registerObserver = foreverObservers.remove(storeId);
        if (registerObserver == null && observerMap.containsKey(storeId)) {
            // 反射拿到真正注册到LiveData中的observer
            Integer registerObserverStoreId = observerMap.remove(storeId);
            registerObserver = LiveDataUtil.getObserver(this, registerObserverStoreId);
        }

        if (registerObserver != null) {
            observers.remove(storeId);
        }

        super.removeObserver(registerObserver != null ? registerObserver : observer);
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


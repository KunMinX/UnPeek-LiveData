package com.kunminx.architecture.ui.callback;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;

/**
 * TODO：UnPeekLiveData 的存在是为了在 "重回二级页面" 的场景下，解决 "数据倒灌" 的问题。
 * 对 "数据倒灌" 的状况不理解的小伙伴，可参考《LiveData 数据倒灌 背景缘由全貌 独家解析》文章开头的解析
 * <p>
 * https://xiaozhuanlan.com/topic/6719328450
 * <p>
 * 本类参考了官方 SingleEventLive 的非入侵设计，
 * 以及小伙伴 Flywith24 在 wrapperLiveData 中通过 ViewModelStore 来唯一确定订阅者的思路，
 * <p>
 * TODO：在当前最新版中，我们透过对 ViewModelStore 的内存地址的遍历，
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
 * Create by KunMinX at 2020/7/21
 */
@Deprecated
public class UnPeekLiveDataV4<T> extends ProtectedUnPeekLiveData<T> {

    @Override
    public void setValue(T value) {
        super.setValue(value);
    }

    @Override
    public void postValue(T value) {
        super.postValue(value);
    }

    /**
     * TODO：Tip：请不要在 UnPeekLiveData 中使用 observe 方法。
     * 取而代之的是在 Activity 和 fragment 中分别使用 observeInActivity 和 observeInFragment 来观察。
     * <p>
     * 2020.10.15 背景缘由：
     * UnPeekLiveData 通过 ViewModelStore 来在各种场景下（如旋屏后）确定订阅者的唯一性和消息的消费状况，
     * 因而在 Activity 和 fragment 对 LifecycleOwner 的使用存在差异的现状下，
     * 我们采取注入局部变量的方式，来获取 store 和 owner。
     *
     * @param owner
     * @param observer
     */
    @Override
    @Deprecated
    public void observe(@NonNull LifecycleOwner owner, @NonNull Observer<? super T> observer) {
        throw new IllegalArgumentException("请不要在 UnPeekLiveData 中使用 observe 方法。" +
                "取而代之的是在 Activity 和 Fragment 中分别使用 observeInActivity 和 observeInFragment 来观察。\n\n" +
                "Taking into account the normal permission of preventing backflow logic, " +
                " do not use observeForever to communicate between pages." +
                "Instead, you can use ObserveInActivity and ObserveInFragment methods " +
                "to observe in Activity and Fragment respectively.");
    }

    /**
     * TODO：Tip：请不要在 UnPeekLiveData 中使用 observeForever 方法。
     * <p>
     * 2020.8.1 背景缘由：
     * UnPeekLiveData 主要用于表现层的 页面转场 和 页面间通信 场景下的非粘性消息分发，
     * 出于生命周期安全等因素的考虑，不建议使用 observeForever 方法，
     * <p>
     * 对于数据层的工作，如有需要，可结合实际场景使用 RxJava 或 kotlin flow。
     *
     * @param observer
     */
    @Override
    @Deprecated
    public void observeForever(@NonNull Observer<? super T> observer) {
        throw new IllegalArgumentException("出于生命周期安全的考虑，请不要在 UnPeekLiveData 中使用 observeForever 方法。\n\n" +
                "Considering avoid lifecycle security issues," +
                " do not use observeForever for communication between pages.");
    }

    public static class Builder<T> {

        /**
         * 是否允许传入 null value
         */
        private boolean isAllowNullValue;

        public Builder<T> setAllowNullValue(boolean allowNullValue) {
            this.isAllowNullValue = allowNullValue;
            return this;
        }

        public UnPeekLiveData<T> create() {
            UnPeekLiveData<T> liveData = new UnPeekLiveData<>();
            liveData.isAllowNullValue = this.isAllowNullValue;
            return liveData;
        }
    }
}

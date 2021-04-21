package com.kunminx.architecture.ui.callback;

/**
 * TODO：UnPeekLiveData 的存在是为了在 "重回二级页面" 的场景下，解决 "数据倒灌" 的问题。
 * 对 "数据倒灌" 的状况不理解的小伙伴，可参考《LiveData 数据倒灌 背景缘由全貌 独家解析》文章开头的解析
 * <p>
 * https://xiaozhuanlan.com/topic/6719328450
 * <p>
 *
 * 对{@link ProtectedUnPeekLiveDataV4}进行了重构，修复了V4版本的已知问题：
 * TODO: 1、UnPeekLiveDataV4 中的 observers 这个 HashMap 恒久存在，注册的 Observer 越多，占用的内存越大，并且除非 UnPeekLiveDataV4 被回收，否则恒久存在内存当中
 * 在 removeObserver 方法中移除 map 中对应存储的 storeId
 *
 * TODO: 2、无法通过 removeObserver 方法移除指定的 Observer（某些场景需要提前 removeObserver）
 * 通过维护外部传入 Observer 与内部代理 Observer 的映射关系，在 removeObserver 调用时，通过反射找到真正注册到 LiveData 中的 Observer，实现移除
 *
 * TODO: 3、同一个 Observer 对象，注册多次，UnPeekLiveDataV4 内部实际上会注册了多个不同的 Observer，从而导致重复回调，产生一些不可预期的问题
 * 内部不会每次调用 observe 方法时都新创建一个代理 Observer，而是复用已经存在的代理 Observer
 * 注意！！！Kotlin + LiveData + Lambda 由于编译器优化，可能会抛 Cannot add the same observer with different lifecycles 异常
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
public class UnPeekLiveData<T> extends ProtectedUnPeekLiveData<T> {

    @Override
    public void setValue(T value) {
        super.setValue(value);
    }

    @Override
    public void postValue(T value) {
        super.postValue(value);
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

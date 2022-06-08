package com.kunminx.architecture.ui.callback;

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
 *
 * Create by Jim at 2021/4/21
 */
@Deprecated
public class UnPeekLiveDataV5<T> extends ProtectedUnPeekLiveDataV5<T> {

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

        public UnPeekLiveDataV5<T> create() {
            UnPeekLiveDataV5<T> liveData = new UnPeekLiveDataV5<>();
            liveData.isAllowNullValue = this.isAllowNullValue;
            return liveData;
        }
    }
}

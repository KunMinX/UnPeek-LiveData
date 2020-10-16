package com.kunminx.architecture.ui.callback;

/**
 * TODO：Note 2020.10.15:
 * 这是第 3 代 UnPeekLiveData 设计，目前最新版本为更少隐患的第 4 代。
 *
 * TODO：UnPeekLiveData 的存在是为了在 "重回二级页面" 的场景下，解决 "数据倒灌" 的问题。
 * 对 "数据倒灌" 的状况不理解的小伙伴，可参考《jetpack MVVM 精讲》的解析
 * <p>
 * https://juejin.im/post/5dafc49b6fb9a04e17209922
 * <p>
 * 本类参考了官方 SingleEventLive 的非入侵设计，
 * <p>
 * TODO：并创新性地引入了 "延迟清空消息" 的设计，
 * 如此可确保：
 * 1.一条消息能被多个观察者消费
 * 2.延迟期结束，不再能够收到旧消息的推送
 * 3.并且旧消息在延迟期结束时能从内存中释放，避免内存溢出等问题
 * 4.让非入侵设计成为可能，遵循开闭原则
 * <p>
 * TODO：增加一层 ProtectedUnPeekLiveData，
 * 用于限制从 Activity/Fragment 篡改来自 "数据层" 的数据，数据层的数据务必通过唯一可信源来分发，
 * 如果这样说还不理解，详见：
 * https://xiaozhuanlan.com/topic/6719328450 和 https://xiaozhuanlan.com/topic/0168753249
 * <p>
 * Create by KunMinX at 2020/7/21
 */
@Deprecated
public class UnPeekLiveDataV3<T> extends ProtectedUnPeekLiveDataV3<T> {

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
         * 消息的生存时长
         */
        private int eventSurvivalTime = 1000;

        /**
         * 是否允许传入 null value
         */
        private boolean isAllowNullValue;

        /**
         * 是否允许自动清理，默认 true
         */
        private boolean isAllowToClear = true;

        public Builder<T> setEventSurvivalTime(int eventSurvivalTime) {
            this.eventSurvivalTime = eventSurvivalTime;
            return this;
        }

        public Builder<T> setAllowNullValue(boolean allowNullValue) {
            this.isAllowNullValue = allowNullValue;
            return this;
        }

        public Builder<T> setAllowToClear(boolean allowToClear) {
            this.isAllowToClear = allowToClear;
            return this;
        }

        public UnPeekLiveDataV3<T> create() {
            UnPeekLiveDataV3<T> liveData = new UnPeekLiveDataV3<>();
            liveData.DELAY_TO_CLEAR_EVENT = this.eventSurvivalTime;
            liveData.isAllowNullValue = this.isAllowNullValue;
            liveData.isAllowToClear = this.isAllowToClear;
            return liveData;
        }
    }
}

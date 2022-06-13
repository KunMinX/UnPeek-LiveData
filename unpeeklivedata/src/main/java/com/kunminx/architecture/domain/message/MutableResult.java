package com.kunminx.architecture.domain.message;

/**
 * Create by KunMinX at 2022/5/31
 */
public class MutableResult<T> extends Result<T> {

  public MutableResult(T value) {
    super(value);
  }

  public MutableResult() {
    super();
  }

  /**
   * 勿在 Activity/Fragment 等页面处使用该 API，
   * 请在 SharedViewModel 等唯一可信源处使用该 API，
   * do not use this method in View Controller like Activity,Fragment etc,
   * only use this method in the Only Source of Truth like SharedViewModel.
   *
   * @param value
   */
  public void setValue(T value) {
    super.setValue(value);
  }

  /**
   * 勿在 Activity/Fragment 等页面处使用该 API，
   * 请在 SharedViewModel 等唯一可信源处使用该 API，
   * do not use this method in View Controller like Activity,Fragment etc,
   * only use this method in the Only Source of Truth like SharedViewModel.
   *
   * @param value
   */
  public void postValue(T value) {
    super.postValue(value);
  }

  public static class Builder<T> {
    private boolean isAllowNullValue;

    public Builder() {
    }

    public Builder<T> setAllowNullValue(boolean allowNullValue) {
      this.isAllowNullValue = allowNullValue;
      return this;
    }

    public MutableResult<T> create() {
      MutableResult<T> liveData = new MutableResult<>();
      liveData.isAllowNullValue = this.isAllowNullValue;
      return liveData;
    }
  }
}

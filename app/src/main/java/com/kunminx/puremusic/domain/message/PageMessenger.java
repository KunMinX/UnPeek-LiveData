/*
 * Copyright 2018-2020 KunMinX
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

package com.kunminx.puremusic.domain.message;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModel;

import com.kunminx.architecture.domain.message.MutableResult;
import com.kunminx.architecture.domain.message.Result;
import com.kunminx.puremusic.data.bean.Moment;

/**
 * Create by KunMinX at 2020/5/30
 */
public class PageMessenger extends ViewModel {

  private final MutableResult<Moment> momentResult = new MutableResult<>();

  private final MutableResult<String> testDelayMsgResult = new MutableResult<>();

  private final MutableResult<String> dispatchStringResult = new MutableResult<>();

  public Result<Moment> getMomentResult() {
    return momentResult;
  }

  public Result<String> getTestDelayMsgResult() {
    return testDelayMsgResult;
  }

  public Result<String> getDispatchStringResult() {
    return dispatchStringResult;
  }

  public void requestMoment(Moment moment) {
    momentResult.setValue(moment);
  }

  public void requestTestDelayMsg(String s) {
    testDelayMsgResult.setValue(s);
  }

  public void requestDispatchString(String s) {
    dispatchStringResult.setValue(s);
  }

  public void requestRemoveObservers(LifecycleOwner owner) {
    dispatchStringResult.removeObservers(owner);
  }
}

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

package com.kunminx.puremusic.ui.callback;

import androidx.lifecycle.ViewModel;

import com.kunminx.architecture.ui.callback.ProtectedUnPeekLiveData;
import com.kunminx.architecture.ui.callback.UnPeekLiveData;
import com.kunminx.puremusic.data.bean.Moment;

/**
 * Create by KunMinX at 2020/5/30
 */
public class SharedViewModel extends ViewModel {

    private final UnPeekLiveData<Moment> mMoment = new UnPeekLiveData<>();

    private final UnPeekLiveData<String> mTestDelayMsg = new UnPeekLiveData<>();

    public ProtectedUnPeekLiveData<Moment> getMoment() {
        return mMoment;
    }

    public ProtectedUnPeekLiveData<String> getTestDelayMsg() {
        return mTestDelayMsg;
    }

    public void requestMoment(Moment moment) {
        mMoment.setValue(moment);
    }

    public void requestTestDelayMsg(String s) {
        mTestDelayMsg.setValue(s);
    }

}

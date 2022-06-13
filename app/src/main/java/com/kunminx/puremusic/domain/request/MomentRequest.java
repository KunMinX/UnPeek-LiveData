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

package com.kunminx.puremusic.domain.request;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.kunminx.architecture.domain.message.MutableResult;
import com.kunminx.puremusic.data.DataRepository;
import com.kunminx.puremusic.data.bean.Moment;

import java.util.List;

/**
 * Create by KunMinX at 2020/5/30
 */
public class MomentRequest extends ViewModel {

  private final MutableResult<List<Moment>> listResult = new MutableResult<>();

  public LiveData<List<Moment>> getListResult() {
    return listResult;
  }

  public void requestList() {
    DataRepository.getInstance().requestList(listResult);
  }
}

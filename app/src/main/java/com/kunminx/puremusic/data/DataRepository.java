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

package com.kunminx.puremusic.data;

import com.kunminx.architecture.domain.message.MutableResult;
import com.kunminx.puremusic.R;
import com.kunminx.puremusic.data.bean.Moment;
import com.kunminx.puremusic.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Create by KunMinX at 2020/5/30
 */
public class DataRepository {

  private static final DataRepository sRepository = new DataRepository();

  public static DataRepository getInstance() {
    return sRepository;
  }

  private DataRepository() {
  }

  public void requestList(MutableResult<List<Moment>> listMutableResult) {
    List<Moment> list = new ArrayList<>();

    list.add(new Moment(getUUID(), Utils.getApp().getString(R.string.moment_content),
            "台北夜市一条街", null, "KunMinX", null));

    list.add(new Moment(getUUID(), Utils.getApp().getString(R.string.moment_content),
            "台北夜市一条街", null, "KunMinX", null));

    list.add(new Moment(getUUID(), Utils.getApp().getString(R.string.moment_content),
            "台北夜市一条街", null, "KunMinX", null));

    list.add(new Moment(getUUID(), Utils.getApp().getString(R.string.moment_content),
            "台北夜市一条街", null, "KunMinX", null));

    list.add(new Moment(getUUID(), Utils.getApp().getString(R.string.moment_content),
            "台北夜市一条街", null, "KunMinX", null));

    list.add(new Moment(getUUID(), Utils.getApp().getString(R.string.moment_content),
            "台北夜市一条街", null, "KunMinX", null));

    list.add(new Moment(getUUID(), Utils.getApp().getString(R.string.moment_content),
            "台北夜市一条街", null, "KunMinX", null));

    list.add(new Moment(getUUID(), Utils.getApp().getString(R.string.moment_content),
            "台北夜市一条街", null, "KunMinX", null));

    listMutableResult.setValue(list);
  }

  private String getUUID() {
    return UUID.randomUUID().toString().replaceAll("-", "");
  }
}

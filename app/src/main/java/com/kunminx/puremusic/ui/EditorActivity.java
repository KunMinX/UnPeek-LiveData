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

package com.kunminx.puremusic.ui;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModel;

import com.kunminx.architecture.ui.state.State;
import com.kunminx.puremusic.R;
import com.kunminx.puremusic.data.bean.Moment;
import com.kunminx.puremusic.databinding.ActivityEditorBinding;
import com.kunminx.puremusic.domain.message.PageMessenger;
import com.kunminx.puremusic.ui.base.BaseActivity;

import java.util.UUID;

/**
 * Create by KunMinX at 2020/5/30
 */
public class EditorActivity extends BaseActivity {

  private EditorViewModel mState;
  private PageMessenger mMessenger;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mState = getActivityViewModel(EditorViewModel.class);
    mMessenger = getApplicationScopeViewModel(PageMessenger.class);

    ActivityEditorBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_editor);
    binding.setLifecycleOwner(this);
    binding.setVm(mState);
    binding.setClick(new ClickProxy());
  }

  public class ClickProxy implements Toolbar.OnMenuItemClickListener {

    public void locate() {
      mMessenger.requestTestDelayMsg("延迟显示了");
    }

    public void back() {
      finish();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
      if (item.getItemId() == R.id.menu_save) {
        toggleSoftInput();
        Moment moment = new Moment(
                UUID.randomUUID().toString(),
                mState.content.get(),
                mState.location.get(),
                null,
                "KunMinX",
                null
        );
        mMessenger.requestMoment(moment);
        finish();
      }
      return true;
    }
  }

  public static class EditorViewModel extends ViewModel {
    public final State<String> content = new State<>("");
    public final State<String> location = new State<>("发送 Toast");
    public Moment moment = new Moment(null, null, null, null, null, null);
  }
}

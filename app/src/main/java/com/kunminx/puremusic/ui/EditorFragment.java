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
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModel;

import com.kunminx.architecture.ui.page.State;
import com.kunminx.puremusic.R;
import com.kunminx.puremusic.data.bean.Moment;
import com.kunminx.puremusic.databinding.FragmentEditorBinding;
import com.kunminx.puremusic.domain.message.PageMessenger;
import com.kunminx.puremusic.ui.base.BaseFragment;

import java.util.UUID;

/**
 * Create by KunMinX at 2020/5/30
 */
public class EditorFragment extends BaseFragment {

  private EditorViewModel mState;
  private PageMessenger mMessenger;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mState = getFragmentViewModel(EditorViewModel.class);
    mMessenger = getActivityViewModel(PageMessenger.class);
    if (getArguments() != null) {
      mState.moment = getArguments().getParcelable(Moment.MOMENT);
    }
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_editor, container, false);
    FragmentEditorBinding binding = FragmentEditorBinding.bind(view);
    binding.setLifecycleOwner(this);
    binding.setVm(mState);
    binding.setClick(new ClickProxy());
    return view;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    mState.content.set(mState.moment.getContent());
  }

  public class ClickProxy implements Toolbar.OnMenuItemClickListener {

    public void locate() {
      mMessenger.requestTestDelayMsg("延迟显示了");
    }

    public void back() {
      nav().navigateUp();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
      if (item.getItemId() == R.id.menu_save) {
        toggleSoftInput();
        mState.moment = new Moment(
                UUID.randomUUID().toString(),
                mState.content.get(),
                mState.location.get(),
                null,
                "KunMinX",
                null
        );
        mMessenger.requestMoment(mState.moment);
        nav().navigateUp();
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

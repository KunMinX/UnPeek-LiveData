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
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModel;

import com.kunminx.architecture.ui.state.State;
import com.kunminx.puremusic.R;
import com.kunminx.puremusic.data.bean.Moment;
import com.kunminx.puremusic.databinding.FragmentDetailBinding;
import com.kunminx.puremusic.domain.message.PageMessenger;
import com.kunminx.puremusic.ui.base.BaseFragment;

/**
 * Create by KunMinX at 2020/5/30
 */
public class DetailFragment extends BaseFragment {

  private DetailViewModel mState;
  private PageMessenger mMessenger;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mState = getFragmentViewModel(DetailViewModel.class);
    mMessenger = getActivityViewModel(PageMessenger.class);
    if (mState.moment == null && getArguments() != null) {
      mState.moment = getArguments().getParcelable(Moment.MOMENT);
    }
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_detail, container, false);
    FragmentDetailBinding binding = FragmentDetailBinding.bind(view);
    binding.setLifecycleOwner(this);
    binding.setVm(mState);
    binding.setClick(new ClickProxy());
    return view;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    mState.content.set(mState.moment.getContent());

    mMessenger.getMomentResult().observe(getViewLifecycleOwner(), moment -> {
      mState.moment = moment;
      mState.content.set(moment.getContent());
    });
  }

  public class ClickProxy {

    public void edit() {
      Bundle bundle = new Bundle();
      bundle.putParcelable(Moment.MOMENT, mState.moment);
      nav().navigate(R.id.action_detailFragment_to_editorFragment, bundle);
    }

    public void back() {
      nav().navigateUp();
    }
  }

  public static class DetailViewModel extends ViewModel {
    public final State<String> content = new State<>("");
    public final State<String> edit = new State<>("编辑");
    public Moment moment;
  }
}

package com.kunminx.puremusic.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kunminx.puremusic.R;
import com.kunminx.puremusic.databinding.FragmentEventSenderBinding;
import com.kunminx.puremusic.ui.base.BaseFragment;
import com.kunminx.puremusic.ui.event.SharedViewModel;
import com.kunminx.puremusic.ui.state.SenderViewModel;

/**
 * Create by KunMinX at 2021/8/10
 */
public class SenderFragment extends BaseFragment {

  private SharedViewModel mEvent;
  private SenderViewModel mState;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mEvent = getApplicationScopeViewModel(SharedViewModel.class);
    mState = getFragmentViewModel(SenderViewModel.class);
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_event_sender, container, false);
    FragmentEventSenderBinding binding = FragmentEventSenderBinding.bind(view);
    binding.setLifecycleOwner(this);
    binding.setClick(new ClickProxy());
    binding.setVm(mState);
    return view;
  }

  public class ClickProxy {
    public void addNumber() {
      mState.number.setValue(mState.number.getValue() + 1);
      mEvent.requestDispatchString(String.valueOf(mState.number.getValue()));
    }
  }
}

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
import com.kunminx.puremusic.databinding.FragmentEventSenderBinding;
import com.kunminx.puremusic.domain.message.PageMessenger;
import com.kunminx.puremusic.ui.base.BaseFragment;

/**
 * Create by KunMinX at 2021/8/10
 */
public class SenderFragment extends BaseFragment {

  private SenderViewModel mState;
  private PageMessenger mMessenger;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mState = getFragmentViewModel(SenderViewModel.class);
    mMessenger = getApplicationScopeViewModel(PageMessenger.class);
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
      mState.number.set(mState.number.get() + 1);
      mMessenger.requestDispatchString(String.valueOf(mState.number.get()));
    }
  }

  public static class SenderViewModel extends ViewModel {
    public final State<Integer> number = new State<>(0);
  }
}

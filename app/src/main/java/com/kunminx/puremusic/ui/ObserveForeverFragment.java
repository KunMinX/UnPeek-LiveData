package com.kunminx.puremusic.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import com.kunminx.architecture.ui.page.State;
import com.kunminx.puremusic.R;
import com.kunminx.puremusic.databinding.FragmentObserveForeverBinding;
import com.kunminx.puremusic.domain.message.PageMessenger;
import com.kunminx.puremusic.ui.base.BaseFragment;

/**
 * Create by KunMinX at 2021/8/10
 */
public class ObserveForeverFragment extends BaseFragment {

  private ObserverViewModel mState;
  private PageMessenger mMessenger;
  private Observer<String> mObserver;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mState = getFragmentViewModel(ObserverViewModel.class);
    mMessenger = getApplicationScopeViewModel(PageMessenger.class);
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_observe_forever, container, false);
    FragmentObserveForeverBinding binding = FragmentObserveForeverBinding.bind(view);
    binding.setLifecycleOwner(this);
    binding.setClick(new ClickProxy());
    binding.setVm(mState);
    return view;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    mMessenger.getDispatchStringResult().observeForever(mObserver = s -> {
      mState.number.set(s);
    });
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    if (mObserver != null) {
      mMessenger.getDispatchStringResult().removeObserver(mObserver);
    }
  }

  public class ClickProxy {
    public void removeObserver() {
      if (mObserver != null) {
        mMessenger.getDispatchStringResult().removeObserver(mObserver);
      }
    }
  }

  public static class ObserverViewModel extends ViewModel {
    public final State<String> number = new State<>("0");
  }
}

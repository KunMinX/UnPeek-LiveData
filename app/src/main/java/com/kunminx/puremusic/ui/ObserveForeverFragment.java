package com.kunminx.puremusic.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;

import com.kunminx.puremusic.R;
import com.kunminx.puremusic.databinding.FragmentObserveForeverBinding;
import com.kunminx.puremusic.ui.base.BaseFragment;
import com.kunminx.puremusic.ui.event.SharedViewModel;
import com.kunminx.puremusic.ui.state.ObserverViewModel;

/**
 * Create by KunMinX at 2021/8/10
 */
public class ObserveForeverFragment extends BaseFragment {

  private SharedViewModel mEvent;
  private ObserverViewModel mState;
  private Observer<String> mObserver;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mEvent = getApplicationScopeViewModel(SharedViewModel.class);
    mState = getFragmentViewModel(ObserverViewModel.class);
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

    mEvent.getDispatchString().observeForever(mObserver = s -> {
      mState.number.setValue(s);
    });
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    if (mObserver != null) {
      mEvent.getDispatchString().removeObserver(mObserver);
    }
  }

  public class ClickProxy {
    public void removeObserver() {
      if (mObserver != null) {
        mEvent.getDispatchString().removeObserver(mObserver);
      }
    }
  }
}

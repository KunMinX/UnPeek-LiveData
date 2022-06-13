package com.kunminx.puremusic.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModel;

import com.kunminx.architecture.ui.page.State;
import com.kunminx.puremusic.R;
import com.kunminx.puremusic.databinding.FragmentObserverBinding;
import com.kunminx.puremusic.domain.message.PageMessenger;
import com.kunminx.puremusic.ui.base.BaseFragment;

/**
 * Create by KunMinX at 2021/8/10
 */
public class ObserverFragment extends BaseFragment {

  private ObserverViewModel mState;
  private PageMessenger mMessenger;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mState = getFragmentViewModel(ObserverViewModel.class);
    mMessenger = getApplicationScopeViewModel(PageMessenger.class);
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_observer, container, false);
    FragmentObserverBinding binding = FragmentObserverBinding.bind(view);
    binding.setLifecycleOwner(this);
    binding.setVm(mState);

    binding.btn.setOnClickListener(v -> {
      mMessenger.requestRemoveObservers(getViewLifecycleOwner());
    });

    return view;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    mMessenger.getDispatchStringResult().observe(getViewLifecycleOwner(), s -> {
      mState.number.set(s);
    });
  }

  public static class ObserverViewModel extends ViewModel {
    public final State<String> number = new State<>("0");
  }
}

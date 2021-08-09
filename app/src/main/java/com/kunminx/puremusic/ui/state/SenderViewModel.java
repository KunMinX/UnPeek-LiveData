package com.kunminx.puremusic.ui.state;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * Create by KunMinX at 2021/8/10
 */
public class SenderViewModel extends ViewModel {

  public final MutableLiveData<Integer> number = new MutableLiveData<>(0);
}

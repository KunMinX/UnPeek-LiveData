package com.kunminx.puremusic.ui.state;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * Create by KunMinX at 2021/8/10
 */
public class ObserverViewModel extends ViewModel {

  public final MutableLiveData<String> number = new MutableLiveData<>("0");
}

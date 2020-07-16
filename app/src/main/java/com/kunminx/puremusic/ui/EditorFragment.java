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

import com.kunminx.puremusic.ui.base.BaseFragment;
import com.kunminx.puremusic.R;
import com.kunminx.puremusic.data.bean.Moment;
import com.kunminx.puremusic.databinding.FragmentEditorBinding;
import com.kunminx.puremusic.ui.callback.SharedViewModel;
import com.kunminx.puremusic.ui.state.EditorViewModel;

import java.util.UUID;

/**
 * Create by KunMinX at 2020/5/30
 */
public class EditorFragment extends BaseFragment {

    private EditorViewModel mEditorViewModel;
    private SharedViewModel mSharedViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mEditorViewModel = getFragmentViewModel(EditorViewModel.class);
        mSharedViewModel = getActivityViewModel(SharedViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_editor, container, false);
        FragmentEditorBinding binding = FragmentEditorBinding.bind(view);
        binding.setLifecycleOwner(this);
        binding.setVm(mEditorViewModel);
        binding.setClick(new ClickProxy());
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


    }

    public class ClickProxy implements Toolbar.OnMenuItemClickListener {

        public void locate() {

        }

        public void back() {
            nav().navigateUp();
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            if (item.getItemId() == R.id.menu_save) {
                toggleSoftInput();
                Moment moment = new Moment();
                moment.setUuid(UUID.randomUUID().toString());
                moment.setUserName("KunMinX");
                moment.setLocation(mEditorViewModel.location.get());
                moment.setContent(mEditorViewModel.content.get());
                mSharedViewModel.moment.postValue(moment);
                nav().navigateUp();
            }
            return true;
        }
    }
}

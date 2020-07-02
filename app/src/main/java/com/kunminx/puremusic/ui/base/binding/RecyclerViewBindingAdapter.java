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

package com.kunminx.puremusic.ui.base.binding;

import androidx.databinding.BindingAdapter;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * Create by KunMinX at 20/4/18
 */
public class RecyclerViewBindingAdapter {

    @BindingAdapter(value = {"adapter", "refreshList", "autoScrollToTopWhenInsert", "autoScrollToBottomWhenInsert"}, requireAll = false)
    public static void bindList(RecyclerView recyclerView, ListAdapter adapter, List list,
                                boolean autoScrollToTopWhenInsert, boolean autoScrollToBottomWhenInsert) {

        if (recyclerView != null && list != null) {
            if (recyclerView.getAdapter() == null) {
                recyclerView.setAdapter(adapter);

                adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                    @Override
                    public void onItemRangeInserted(int positionStart, int itemCount) {
                        if (autoScrollToTopWhenInsert) {
                            recyclerView.scrollToPosition(0);
                        } else if (autoScrollToBottomWhenInsert) {
                            recyclerView.scrollToPosition(recyclerView.getAdapter().getItemCount());
                        }
                    }
                });
            }

            adapter.submitList(list);
        }
    }
}

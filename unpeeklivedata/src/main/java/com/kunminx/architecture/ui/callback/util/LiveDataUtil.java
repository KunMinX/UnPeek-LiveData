package com.kunminx.architecture.ui.callback.util;

/*
 * Copyright 2021-present Jim
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

import androidx.annotation.NonNull;
import androidx.arch.core.internal.SafeIterableMap;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import java.lang.reflect.Field;
import java.util.Map;

/**
 * Create by Jim at 2021/4/21
 */
public class LiveDataUtil {

    /**
     * 通过反射，获取指定LiveData中的Observer对象
     *
     * @param liveData 指定的LiveData
     * @param identityHashCode 想要获取的Observer对象的identityHashCode {@code System.identityHashCode}
     * @return
     */
    public static Observer getObserver(@NonNull LiveData liveData, @NonNull Integer identityHashCode) {
        if (liveData == null || identityHashCode == null) {
            return null;
        }

        try {
            Field field = LiveData.class.getDeclaredField("mObservers");
            field.setAccessible(true);
            SafeIterableMap<Observer, Object> observers = (SafeIterableMap<Observer, Object>) field.get(liveData);
            if (observers != null) {
                for (Map.Entry<Observer, Object> entry : observers) {
                    Observer observer = entry.getKey();
                    if (System.identityHashCode(observer) == identityHashCode) {
                        return observer;
                    }
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return null;
    }
}

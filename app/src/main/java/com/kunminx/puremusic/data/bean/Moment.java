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

package com.kunminx.puremusic.data.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Create by KunMinX at 2020/5/30
 */
public class Moment implements Parcelable {

  public final static String MOMENT = "MOMENT";

  private final String uuid;
  private final String content;
  private final String location;
  private final String imgUrl;
  private final String userName;
  private final String userAvatar;

  public Moment(String uuid, String content, String location, String imgUrl, String userName, String userAvatar) {
    this.uuid = uuid;
    this.content = content;
    this.location = location;
    this.imgUrl = imgUrl;
    this.userName = userName;
    this.userAvatar = userAvatar;
  }

  protected Moment(Parcel in) {
    uuid = in.readString();
    content = in.readString();
    location = in.readString();
    imgUrl = in.readString();
    userName = in.readString();
    userAvatar = in.readString();
  }

  public static final Creator<Moment> CREATOR = new Creator<Moment>() {
    @Override
    public Moment createFromParcel(Parcel in) {
      return new Moment(in);
    }

    @Override
    public Moment[] newArray(int size) {
      return new Moment[size];
    }
  };

  public String getUuid() {
    return uuid;
  }

  public String getContent() {
    return content;
  }

  public String getLocation() {
    return location;
  }

  public String getImgUrl() {
    return imgUrl;
  }

  public String getUserName() {
    return userName;
  }

  public String getUserAvatar() {
    return userAvatar;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(uuid);
    dest.writeString(content);
    dest.writeString(location);
    dest.writeString(imgUrl);
    dest.writeString(userName);
    dest.writeString(userAvatar);
  }
}

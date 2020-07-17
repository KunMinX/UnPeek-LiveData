![](https://i.loli.net/2020/07/15/MFnDRgWBz27IJxs.png)

## 前言

很高兴见到你！我是[《Jetpack MVVM Best Practice》](https://github.com/KunMinX/Jetpack-MVVM-Best-Practice)作者 KunMinX。

今天提到的 “数据倒灌” 一词，是我为了方便理解和记忆 **“页面在 ‘二进宫’ 时收到旧数据推送” 的情况**，而在 2019 年 **自创并在网上传播的 对此类现象的概括**。

它主要发生在：通过 SharedViewModel + LiveData 的组合 来解决页面通信的场景。

&nbsp;

## 本文的目标

由于本文的目标主要是来介绍 官方 Demo 现有解决方案的缺陷，以及经过 1 年迭代的完美解决方案，

所以我假设在座的诸位 对最基本的背景缘由有一定的了解，知道：

> 为什么 LiveData 默认被设计为粘性事件

> 为什么 [官方文档 ](https://developer.android.google.cn/topic/libraries/architecture/viewmodel#sharing) 推荐使用 SharedViewModel + LiveData（文档没明说，但事实上包含三个关键的背景缘由）

> 乃至为什么存在 “数据倒灌” 的现象

> 以及为什么在 “页面通信” 的场景下，不用静态单例、不用 LiveDataBus

如果对于这些前置知识也尚不了解，可结合个人的兴趣前往[《LiveData 数据倒灌 背景缘由全貌 独家解析》](https://xiaozhuanlan.com/topic/6719328450)查阅，此处不再累述。

&nbsp;

## 现有解决方案及各自缺陷

在[《Jetpack MVVM 精讲》](https://juejin.im/post/5dafc49b6fb9a04e17209922)中我分别提到了 **Event 事件包装器、反射方式、SingleLiveEvent** 这三种方式来解决 “数据倒灌” 的问题。它们分别来自上文我们提到的[外网](https://medium.com/androiddevelopers/livedata-with-snackbar-navigation-and-other-events-the-singleliveevent-case-ac2622673150)、[美团](https://tech.meituan.com/2018/07/26/android-livedatabus.html)的文章，和[官方最新 demo](https://github.com/android/architecture-samples/blob/dev-todo-mvvm-live/todoapp/app/src/main/java/com/example/android/architecture/blueprints/todoapp/SingleLiveEvent.java)。

但正如我在[《Jetpack MVVM 精讲》](https://juejin.im/post/5dafc49b6fb9a04e17209922)介绍的，它们分别存在如下问题：

> **Event 事件包装器：**

 对于多观察者的情况，只允许第一个观察者消费，这不符合现实需求；

 而且手写 Event 事件包装器，在 Java 中存在 null 安全的一致性问题。


> **反射干预 Version 的方式：**

 存在延迟，无法用于对实时性有要求的场景；

 并且数据会随着 SharedViewModel 长久滞留在内存中得不到释放。


> **官方最新 demo 中的 SingleLiveEvent：**

 是对 Event 事件包装器 一致性问题的改进，但未解决多观察者消费的问题；

 而且额外引入了消息未能从内存中释放的问题。

&nbsp;

## UnPeekLiveData 特点

UnPeekLiveData 通过 **独创的 “延时自动清理消息” 的设计**，来满足：

> 1.消息被分发给多个观察者时，**不会因第一个观察者消费了而直接被置空**

> 2.时限到了，**消息便不再会被倒灌**

> 3.时限到了，**消息自动从内存中清理释放**

> 4.使非入侵的设计成为可能，并最终结合官方 SingleLiveEvent 的设计实现了 **遵循开闭原则的非入侵重写**。

并且 UnPeekLiveData 提供了构造器模式，可通过构造器组装适合自己业务场景的 UnPeekLiveData。

```java
UnPeekLiveData<Moment> test =
  new UnPeekLiveData.Builder<Moment>()
    .setAllowNullValue(false)
    .setAllowToClear(true)
    .setEventSurvivalTime(500)
    .create();
```


|                          零入侵设计                          |                       延时自动清理消息                       |                        Builder 构造器                        |
| :----------------------------------------------------------: | :----------------------------------------------------------: | :----------------------------------------------------------: |
| ![4021594785998_.pic_hd.jpg](https://i.loli.net/2020/07/15/5mXOKHp9DRjGzV7.jpg) | ![4031594785998_.pic_hd.jpg](https://i.loli.net/2020/07/15/up3D1gcItrywCK5.jpg) | ![4041594785999_.pic_hd.jpg](https://i.loli.net/2020/07/15/X4JCght5PRIHGFL.jpg) |

&nbsp;

PS：非常感谢近期 [hegaojian](https://github.com/hegaojian)、Angki、Flynn、[Joker_Wan](https://juejin.im/user/5829b958d20309005403f4d6) 等小伙伴积极的试用和反馈，使得未被觉察的问题 被及时发现和纳入考虑。

&nbsp;

## JCenter 依赖

```groovy
implementation 'com.kunminx.archi:unpeek-livedata:2.9.7-beta4'
```

&nbsp;

## License

本文以 [CC 署名-非商业性使用-禁止演绎 4.0 国际协议](https://creativecommons.org/licenses/by-nc-nd/4.0/deed.zh) 发行。

Copyright © 2019-present KunMinX

![](https://images.xiaozhuanlan.com/photo/2020/8fc6f51263babeb544bb4a7dae6cde59.jpg)

文中提到的 对 “**数据倒灌**” 一词及其现象的概括、对 Event 事件包装器、反射方式、SingleLiveEvent **各自存在的缺陷**的理解，以及对 UnPeekLiveData 的 “**延迟自动清理消息**” 的设计，**均属于本人独立原创的成果**，本人对此享有最终解释权。

任何个人或组织在引用上述内容时，**须注明原作者和出处**。未经授权不得用于洗稿、广告包装等商业用途。

```
Copyright 2019-present KunMinX

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
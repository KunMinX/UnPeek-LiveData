![](https://tva1.sinaimg.cn/large/008i3skNly1gre5lf81e3j312u0asdgu.jpg)

## 前言

很高兴见到你！我是[《Jetpack MVVM Best Practice》](https://github.com/KunMinX/Jetpack-MVVM-Best-Practice)作者 KunMinX。

今天提到的 “数据倒灌” 一词，是我为了方便理解和记忆 **“页面在 ‘二进宫’ 时收到旧数据推送” 的情况**，而在 2019 年 **自创并在网上传播的 对此类现象的概括**。

它主要发生在：通过 SharedViewModel + LiveData 的组合 来解决页面通信的场景。

&nbsp;

## 本文的目标

由于本文的目标主要是来介绍 官方 Demo 现有解决方案的缺陷，以及经过 2 年迭代的趋于完美的解决方案，

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



## 最新更新动态

### UnPeekLiveData v6.0 特点

感谢小伙伴 @[wl0073921](https://github.com/wl0073921) 对 UnPeekLiveData 源码的演化做出的贡献，

V6 版源码翻译和完善自小伙伴 wl0073921 在 [issue](https://github.com/KunMinX/UnPeek-LiveData/issues/11) 中的分享，

V6 版源码相比于 V5 版的改进之处在于，引入 Observer 代理类的设计，这使得在旋屏重建时，无需通过反射方式跟踪和复用基类 Map 中的 Observer，转而通过 removeObserver 的方式来自动移除和在页面重建后重建新的 Observer，

因而复杂度由原先的分散于基类数据结构，到集中在 proxy 对象这一处，进一步方便了源码逻辑的阅读和后续的修改。

> 具体可参见 UnPeekLiveData 最新源码及注释的说明。

&nbsp;

### UnPeekLiveData v5.0 特点

感谢就职于 “腾讯音乐部门” 的小伙伴 @[zhangjianlaoda](https://github.com/zhangjianlaoda) 应邀对 UnPeekLiveData 做的优化和升级。

**该版本保留了 UnPeekLiveData v4 的下述几大特点**，并在适当时机基于反射等机制，来彻底解决 UnPeekLiveData v4 下 Observers 无法释放、重复创建，以及 foreverObserver、removeObserver 被禁用等问题，将 UnPeekLiveData 的内存性能再往上提升了一个阶梯。

同时，该版本使 Observe 等方法的方法名和形参列表与官方 API 保持一致，尽可能减少新上手小伙伴的学习成本。

> 具体可参见 UnPeekLiveData 最新源码及注释的说明。

&nbsp;

### UnPeekLiveData v4.0 特点

我们在 UnPeekLiveData v3.0 的基础上，参考了小伙伴 [Flywith24](https://github.com/Flywith24) - [WrapperLiveData](https://github.com/Flywith24/WrapperLiveData) 遍历 ViewModelStore 的思路，以此提升 “防止倒灌时机” 的精准度。

> 注：出于在现有 AndroidX 源码的背景下实现 "防倒灌机制" 的需要，**v4.0 对 Observe 方法的使用做了微调**，改为分别针对 Activity/Fragment 提供 ObserveInActivity 和 ObserveInFragment 方法，具体缘由详见源码注释的说明。

目前为止，UnPeekLiveData 实现和保留的特点如下：

> 1.一条消息能被多个观察者消费（since v1.0）

> 2.消息被所有观察者消费完毕后才开始阻止倒灌（since v4.0）

> 3.可以通过 clear 方法手动将消息从内存中移除（since v4.0）

> 4.让非入侵设计成为可能，遵循开闭原则（since v3.0）

> 5.基于 "访问权限控制" 支持 "读写分离"，遵循唯一可信源的消息分发理念（since v2.0，详见 ProtectedUnPeekLiveData）

并且 UnPeekLiveData 提供了构造器模式，可通过构造器组装适合自己业务场景的 UnPeekLiveData。

```java
UnPeekLiveData<Moment> test =
  new UnPeekLiveData.Builder<Moment>()
    .setAllowNullValue(false)
    .create();
```


|                       零入侵设计                       |                       防倒灌机制                       |                     Builder 构造器                     |
| :----------------------------------------------------: | :----------------------------------------------------: | :----------------------------------------------------: |
| ![](https://i.loli.net/2020/10/17/WTXzc48qkajwvd1.jpg) | ![](https://i.loli.net/2020/10/17/PbAkvTwVCflXY7G.jpg) | ![](https://i.loli.net/2020/10/17/RBfncrZkCWwb9eV.jpg) |

&nbsp;

## Thanks

PS：非常感谢近期 [hegaojian](https://github.com/hegaojian)、Angki、Flynn、[Joker_Wan](https://juejin.im/user/5829b958d20309005403f4d6)、小暑知秋、[大棋](https://juejin.im/user/1785262613208376/posts)、空白、qh、lvrenzhao 等小伙伴积极的试用和反馈，使得未被觉察的问题 被及时发现和纳入考虑。

&nbsp;

## Maven 依赖

```groovy
implementation 'com.kunminx.arch:unpeek-livedata:6.0.0-beta1'
```

> 温馨提示：
>
> 1.上述 implementation 的命名，我们已从 `archi` 改为 `arch`，请注意修改，
>
> 2.鉴于 Jcenter 的关闭，我们已将仓库迁移至 Maven Central，请自行在根目录 build.gradle 添加 `mavenCentral()`。


&nbsp;

## 谁在使用

感谢小伙伴们对 “开源库使用情况” 匿名调查问卷的参与，截至 2021年4月25日，我们了解到

包括 “腾讯音乐、BMW、TCL” 在内的诸多知名厂商的软件，都参考过我们开源的 [Jetpack MVVM Scaffold](https://github.com/KunMinX/Jetpack-MVVM-Scaffold) 架构模式，以及正在使用我们维护的 UnPeek-LiveData 等框架。

目前我们已将具体的统计数据更新到 相关的开源库 ReadMe 中，问卷调查我们也继续保持开放，不定期将小伙伴们登记的公司和产品更新到表格，以便吸引到更多的小伙伴 参与到对这些架构组件的 使用、反馈，集众人之所长，让架构组件得以不断演化和升级。

https://wj.qq.com/s2/8362688/124a/

| 集团 / 公司          | 产品               |
| -------------------- | ------------------ |
| 腾讯音乐             | 即将上线，暂时保密 |
| TCL                  | 内置应用，暂时保密 |
| 左医科技             | 诊室听译机器人     |
| BMW                  | Speech             |
| 上海互教信息有限公司 | 知心慧学教师       |
| 美术宝               | 弹唱宝             |
|                      | 网安               |

&nbsp;

## History

### UnPeekLiveData v5.0

Update since 2021.4.21

&nbsp;

### UnPeekLiveData v4.0

Update since 2020.10.16

&nbsp;

### UnPeekLiveData v3.0

Update since 2020.7.10

通过 **独创的 “延时自动清理消息” 的设计**，来满足：

> 1.消息被分发给多个观察者时，**不会因第一个观察者消费了而直接被置空**

> 2.时限到了，**消息便不再会被倒灌**

> 3.时限到了，**消息自动从内存中清理释放**

> 4.使非入侵的设计成为可能，并最终结合官方 SingleLiveEvent 的设计实现了 **遵循开闭原则的非入侵重写**。

&nbsp;

### UnPeekLiveData v2.0

Update since 2020.5

> 1.结合 Event 包装类的使用，对 LiveData 类进行入侵性修改。

> 2.提供 ProtectedUnPeekLiveData，基于访问权限控制实现 "读写分离"：支持只从 "唯一可信源"（例如 ViewModel）内部发送、而 Activity/Fragment 只允许 Observe。

&nbsp;

### UnPeekLiveData v1.0

Update since 2019

> 1.针对 **“页面在 ‘二进宫’ 时收到旧数据推送” 的情况** 创建 “数据倒灌” 的定义，并在网上交流和传播。

> 2.参考美团 LiveDataBus 的设计，透过反射的方式拦截并修改 Last Version 来防止倒灌。

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

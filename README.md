![](https://i.loli.net/2020/07/15/MFnDRgWBz27IJxs.png)

很高兴见到你！

UnPeekLiveData 主要用于 页面通信（如事件回调）等场景，避免 "数据倒灌" 现象的发生，

（背景缘由详见 [《独家记忆 | Jetpack MVVM 高频提问和解答》](https://juejin.im/post/5ef061d0e51d4573e71f3243)）

UnPeekLiveData 通过 **独创的 “延时自动清理消息” 的设计**，来满足：

1.消息被分发给多个观察者时，不会因第一个观察者消费了而直接被置空

2.时限到了，消息便不再会被倒灌

3.时限到了，消息自动从内存中清理释放

4.使非入侵的设计成为可能，并最终结合官方 SingleLiveEvent 的设计实现了非入侵重写。

并且 UnPeekLiveData 提供了构造器模式，可通过构造器组装适合自己业务场景的 UnPeekLiveData。

`implementation 'com.kunminx.archi:unpeeklivedata:2.9.6-beta2'`

Copyright © 2020 KunMinX

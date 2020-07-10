# UnPeekLiveData

很高兴见到你！

UnPeekLiveData 主要用于 页面通信（如事件回调）等场景，避免 "数据倒灌" 现象的发生，

（为什么在页面通信的场景使用 SharedViewModel + Event-LiveData 而不直接使用单例或 Bus？背景缘由详见 [《独家记忆 | Jetpack MVVM 高频提问和解答》](https://juejin.im/post/5ef061d0e51d4573e71f3243)）

![WechatIMG378.jpeg](https://i.loli.net/2020/07/10/ZPA7gHczYlxstyB.jpg)

UnPeekLiveData 通过独创的 “延时自动清理消息” 的设计，来满足：

1.消息被分发给多个观察者时，不会因第一个观察者消费了而直接被置空

2.时限到了，消息便不再会被倒灌

3.时限到了，消息自动从内存中清理释放

4.使非入侵的设计成为可能，并最终结合官方 SingleLiveEvent 的设计实现了非入侵重写。

并且 UnPeekLiveData 提供了构造器模式，可通过构造器组装适合自己业务场景的 UnPeekLiveData。

`implementation 'com.kunminx.archi:unpeeklivedata:2.9.5-beta1'`

Copyright © 2020 KunMinX

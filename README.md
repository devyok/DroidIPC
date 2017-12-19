# 废弃 #
请参考：[ServiceManager](https://github.com/devyok/ServiceManager)

# DroidIPC
Android进程间通信框架


背景：
为了应对移动应用内存限制的问题，移动应用通常进行多进程化(根据职责驱动原则和模式分解，分割业务到不同的进程中提高应用稳定性)，而多进程间通信的实现方式有多种方法，比如：aidl；
那么DroidIPC主要抛开aidl基于Binder定义的一套进程间通信接口，进程间的数据承载在android.os.Bundle对象中。通信双方只需定义通信接口(标准Java接口)与Bundle的通信协议，Bundle的收发由框架完成。


## 优势 ##


- 由于不依赖aidl,进程间通信的数据部分的打包(parcel.write)与解包(parcel.read)需由服务提供者与使用者手动完成，对于这部分的实现扩大了灵活性。例如：统一在打包或解包时进行拦截，修改打包方式。 同时这也是最大的劣势，手动的打包与解包大大降低了开发效率；
- 统一的服务管理进程(svcmgr)，提供了灵活性，扩展性与可读性，维护成本降低；


## 不足 ##
- 手动的打包与解包大大降低了开发效率；
- 繁琐的配置（服务提供者与使用者都需要在AndroidMainfest.xml中进行配置）；
  在我们旧的产品上通过gradel插件解决了，但是这个整体方案已被废弃，仅供参考学习。 所以不在更新更多内容，我们新的产品已替换到[ServiceManager](https://github.com/devyok/ServiceManager)；


# 废弃 #
请参考：[ServiceManager](https://github.com/devyok/ServiceManager)

# DroidIPC
Android进程间通信框架


背景：
为了应对移动应用内存限制的问题，移动应用通常进行多进程化(根据职责驱动原则和模式分解，分割业务
到不同的进程中提高应用稳定性)，而多进程间通信的实现方式有多种方法，比如：aidl；那么DroidIPC
主要抛开aidl基于Binder定义的一套进程间通信接口，进程间数据传输的对象为：android.os.Bundle。
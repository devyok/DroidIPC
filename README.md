# 废弃 #
请参考：[ServiceManager](https://github.com/devyok/ServiceManager)

# DroidIPC
Android进程间通信框架


背景：
为了应对移动应用内存限制的问题，移动应用通常进行多进程化(根据职责驱动原则和模式分解，分割业务到不同的进程中提高应用稳定性)，而多进程间通信的实现方式有多种方法，比如：aidl；
那么DroidIPC主要抛开aidl基于Binder定义的一套进程间通信接口，进程间的数据承载在android.os.Bundle对象中。通信双方只需定义通信接口(标准Java接口)与Bundle的通信协议，Bundle的收发由框架完成。


## 优势 ##


- 由于不依赖aidl,进程间通信的数据部分的打包(parcel.write)与解包(parcel.read)需由服务提供者与使用者手动完成，对于这部分的实现扩大了灵活性。例如：统一在打包或解包时进行拦截，修改打包方式。 同时这也是最大的劣势，手动的打包与解包大大降低了开发效率；
- 统一的服务管理进程(svcmgr)，维护成本降低，提高了灵活性，扩展性与可读性；


## 不足 ##
- 手动的打包与解包大大降低了开发效率；
- 繁琐的配置（服务提供者与使用者都需要在AndroidMainfest.xml中进行配置）；
  虽然在我们旧的产品上通过gradel插件解决了，但是这个整体方案已被废弃，仅供参考学习。 所以不在更新更多内容，我们新的产品已替换到[ServiceManager](https://github.com/devyok/ServiceManager)；


## 如何使用 ##

### 第一步 ###
定义服务接口(标准的java接口)

	public interface IActivityServcie extends IInterface{

		public static final String ACTION_START_ACTIVITY = "com.devyok.action.START_ACTIVITY"; 
		public int startActivity(String className);
	
	}

### 第二步 ###
实现接口，发送请求到对端进程

	class IActivityServiceImpl implements IActivityServcie{

	private static IActivityServiceImpl sInstance = new IActivityServiceImpl();
	
		private IBundleSender bundleSender ;
		
		public static IActivityServcie asService(IInterface iInterface) {
			sInstance.bundleSender = (IBundleSender) iInterface;
			return sInstance;
		}
	
		@Override
		public int startActivity(String className) {
			Bundle bundle = new Bundle();
			bundle.putString(BundleColumns.ACTION, ACTION_START_ACTIVITY);
			Bundle result = bundleSender.send(bundle);
			return result.getInt(BundleColumns.RESULT);
		}

	}

### 第三步 ###
接收对端的Bundle请求，并实现这个请求并返回
	public class ActivityServiceProvider extends BundleReceiver {

		public Bundle onReceiver(Bundle bundle) {
			
			Bundle result = new Bundle();
			
			String action = bundle.getString(BundleColumns.ACTION);
			
			if(action.equals("com.devyok.action.START_ACTIVITY")){
				result.putInt(BundleColumns.RESULT, 10010);
			}
			
			return result;
		}

	}
实现以上服务之后，需要在Mainfest.xml中配置
	
	<service android:name="com.devy.droidipc.RemoteCommandListener">
    	<meta-data android:name="remove.service.names" android:value="activity_service"/>
		<intent-filter>
            <action android:name="com.devy.service_provider._SERVICE_" />
        </intent-filter>
	</service>

intent-filter中的action是（包名+.+_SERVICE_），同时使用meta-data声明服务的名称“activity_service”

### 第四步 ###
使用服务

	try {
		IActivityServcie activityServcie = (IActivityServcie)IPC.proxyService(IPC.Context.ACTIVITY_SERVICE);
		int result = activityServcie.startActivity(MainActivity.class.getName());
	} catch (IPCServiceNotFoundException e) {
		e.printStackTrace();
	} catch (IPCTimeoutException e) {
		e.printStackTrace();
	}

如果使用服务的进程为独立APK，那么就需要在Manifest.xml中配置以下service
	
	<service android:name="com.devy.droidipc.RemoteCommandListener">
		<intent-filter>
            <action android:name="com.devy.service_client._SERVICE_" />
        </intent-filter>
	</service>

### 设计 ###
由于目前这个框架我们主要是定制的android系统的产品中使用，所以下图看到的会有UID是System的应用，请参考：

![](https://raw.githubusercontent.com/devyok/DroidIPC/master/DroidIPC_Design.png)

## License ##
ServiceManager is released under the [Apache 2.0 license](https://github.com/devyok/DroidIPC/blob/master/LICENSE).

Copyright (C) 2017 DengWei.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
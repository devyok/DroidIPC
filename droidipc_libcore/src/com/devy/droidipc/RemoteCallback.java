package com.devy.droidipc;

import android.os.Bundle;
/**
 * RemoteCallback 远程回调
 * @author wei.deng
 */
@Deprecated
public interface RemoteCallback{
	public Bundle handle(Bundle bundle);
}

package com.sanyinchen.nativebridge;

import com.facebook.soloader.SoLoader;

public class BridgeLoad {
    static {
        SoLoader.loadLibrary("corebridge");
    }

    public BridgeLoad() {
        //  initHybrid()
    }


    public native String getHelloDynamicStr();


    // private static native HybridData initHybrid(ReadableNativeMap jscConfig);
}

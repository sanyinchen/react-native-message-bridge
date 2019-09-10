package com.sanyinchen.nativebridge;

import com.facebook.soloader.SoLoader;

public class BridgeLoad {
    static {
        SoLoader.loadLibrary("jscexecutor");
    }

    public BridgeLoad() {
      //  initHybrid()
    }


   // private static native HybridData initHybrid(ReadableNativeMap jscConfig);
}

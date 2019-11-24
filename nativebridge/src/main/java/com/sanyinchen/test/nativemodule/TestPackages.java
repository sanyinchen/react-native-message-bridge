package com.sanyinchen.test.nativemodule;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

public class TestPackages implements ReactPackage {

    private LogModule.LogUpdate runnable;

    public TestPackages(LogModule.LogUpdate runnable) {
        this.runnable = runnable;
    }

    @Nonnull
    @Override
    public List<NativeModule> createNativeModules(@Nonnull ReactApplicationContext reactContext) {
        List<NativeModule> modules = new ArrayList<>();
        modules.add(new LogModule(reactContext, runnable));
        return modules;
    }


}

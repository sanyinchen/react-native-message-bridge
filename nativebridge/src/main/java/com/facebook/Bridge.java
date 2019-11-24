package com.facebook;

import android.app.Application;

import com.facebook.react.ReactInstanceManager;
import com.facebook.react.bridge.NativeModuleCallExceptionHandler;

public enum Bridge {
    INS;

    public void test(Application application) {

        ReactInstanceManager reactInstanceManager = ReactInstanceManager.builder()
                .setApplication(application)
                .setJSBundleFile("js-bridge-bundle.js")
                .setNativeModuleCallExceptionHandler(new NativeModuleCallExceptionHandler() {
                    @Override
                    public void handleException(Exception e) {
                        e.printStackTrace();
                    }
                })
                .build();
        reactInstanceManager.createReactContextInBackground();
    }
}

package com.sanyinchen.test.nativemodule;

import android.util.Log;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import javax.annotation.Nonnull;

public class LogModule extends ReactContextBaseJavaModule {
    private LogUpdate logUpdate;

    public LogModule(@Nonnull ReactApplicationContext reactContext, LogUpdate logUpdate) {
        super(reactContext);
        this.logUpdate = logUpdate;
    }

    @Nonnull
    @Override
    public String getName() {
        return "NativeLog";
    }

    @ReactMethod
    public void log(String message) {
        Log.d("src_test", "message:" + message);
        if (logUpdate != null) {
            logUpdate.log(message);
        }
    }

    public interface LogUpdate {
        void log(String message);
    }
}

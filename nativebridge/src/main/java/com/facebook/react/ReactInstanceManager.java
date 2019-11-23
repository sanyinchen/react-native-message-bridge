/**
 * Copyright (c) Facebook, Inc. and its affiliates.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.react;


import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.facebook.infer.annotation.Assertions;
import com.facebook.infer.annotation.ThreadConfined;
import com.facebook.react.bridge.JSBundleLoader;
import com.facebook.react.bridge.JSIModulePackage;
import com.facebook.react.bridge.JavaScriptExecutorFactory;
import com.facebook.react.bridge.NativeModuleCallExceptionHandler;
import com.facebook.react.bridge.NotThreadSafeBridgeIdleDebugListener;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.common.ReactConstants;
import com.facebook.soloader.SoLoader;
import com.facebook.systrace.Systrace;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import static com.facebook.infer.annotation.ThreadConfined.UI;
import static com.facebook.systrace.Systrace.TRACE_TAG_REACT_JAVA_BRIDGE;

class ReactInstanceManager {

    private @Nullable
    @ThreadConfined(UI)
    ReactContextInitParams mPendingReactContextInitParams;
    private volatile @Nullable
    Thread mCreateReactContextThread;
    /* accessed from any thread */
    private final JavaScriptExecutorFactory mJavaScriptExecutorFactory;

    private final @Nullable
    JSBundleLoader mBundleLoader;
    private final @Nullable
    String mJSMainModulePath; /* path to JS bundle root on packager server */
    private final List<ReactPackage> mPackages;
    private final Object mReactContextLock = new Object();
    private @Nullable
    volatile ReactContext mCurrentReactContext;
    private final Context mApplicationContext;
    private @Nullable
    Activity mCurrentActivity;
    private final Collection<ReactInstanceEventListener> mReactInstanceEventListeners =
            Collections.synchronizedSet(new HashSet<ReactInstanceEventListener>());
    // Identifies whether the instance manager is or soon will be initialized (on background thread)
    private volatile boolean mHasStartedCreatingInitialContext = false;
    // Identifies whether the instance manager destroy function is in process,
    // while true any spawned create thread should wait for proper clean up before initializing
    private volatile Boolean mHasStartedDestroying = false;
    private final @Nullable
    NativeModuleCallExceptionHandler mNativeModuleCallExceptionHandler;
    private final @Nullable
    JSIModulePackage mJSIModulePackage;

    /**
     * Listener interface for react instance events.
     */
    public interface ReactInstanceEventListener {

        /**
         * Called when the react context is initialized (all modules registered). Always called on the
         * UI thread.
         */
        void onReactContextInitialized(ReactContext context);
    }

    private class ReactContextInitParams {
        private final JavaScriptExecutorFactory mJsExecutorFactory;
        private final JSBundleLoader mJsBundleLoader;

        public ReactContextInitParams(
                JavaScriptExecutorFactory jsExecutorFactory,
                JSBundleLoader jsBundleLoader) {
            mJsExecutorFactory = Assertions.assertNotNull(jsExecutorFactory);
            mJsBundleLoader = Assertions.assertNotNull(jsBundleLoader);
        }

        public JavaScriptExecutorFactory getJsExecutorFactory() {
            return mJsExecutorFactory;
        }

        public JSBundleLoader getJsBundleLoader() {
            return mJsBundleLoader;
        }
    }

    public static ReactInstanceManagerBuilder builder() {
        return new ReactInstanceManagerBuilder();
    }

    /* package */ ReactInstanceManager(
            Context applicationContext,
            @Nullable Activity currentActivity,
            JavaScriptExecutorFactory javaScriptExecutorFactory,
            @Nullable JSBundleLoader bundleLoader,
            @Nullable String jsMainModulePath,
            List<ReactPackage> packages,
            boolean useDeveloperSupport,
            @Nullable NotThreadSafeBridgeIdleDebugListener bridgeIdleDebugListener,
            NativeModuleCallExceptionHandler nativeModuleCallExceptionHandler,
            boolean lazyViewManagersEnabled,
            int minNumShakes,
            int minTimeLeftInFrameForNonBatchedOperationMs,
            @Nullable JSIModulePackage jsiModulePackage) {
        Log.d(ReactConstants.TAG, "ReactInstanceManager.ctor()");
        initializeSoLoaderIfNecessary(applicationContext);

        mApplicationContext = applicationContext;
        mCurrentActivity = currentActivity;
        mJavaScriptExecutorFactory = javaScriptExecutorFactory;
        mBundleLoader = bundleLoader;
        mJSMainModulePath = jsMainModulePath;
        mPackages = new ArrayList<>();
//        Systrace.beginSection(
//                TRACE_TAG_REACT_JAVA_BRIDGE, "ReactInstanceManager.initDevSupportManager");
//
//        Systrace.endSection(TRACE_TAG_REACT_JAVA_BRIDGE);
        mNativeModuleCallExceptionHandler = nativeModuleCallExceptionHandler;
        synchronized (mPackages) {
//            PrinterHolder.getPrinter()
//                    .logMessage(ReactDebugOverlayTags.RN_CORE, "RNCore: Use Split Packages");
//
//            mPackages.add(
//                    new CoreModulesPackage(
//                            this,
//                            new DefaultHardwareBackBtnHandler() {
//                                @Override
//                                public void invokeDefaultOnBackPressed() {
//                                    ReactInstanceManager.this.invokeDefaultOnBackPressed();
//                                }
//                            },
//                            mUIImplementationProvider,
//                            lazyViewManagersEnabled,
//                            minTimeLeftInFrameForNonBatchedOperationMs));

            mPackages.addAll(packages);
        }
        mJSIModulePackage = jsiModulePackage;

        // Instantiate ReactChoreographer in UI thread.
        //ReactChoreographer.initialize();

    }

    private static void initializeSoLoaderIfNecessary(Context applicationContext) {
        // Call SoLoader.initialize here, this is required for apps that does not use exopackage and
        // does not use SoLoader for loading other native code except from the one used by React Native
        // This way we don't need to require others to have additional initialization code and to
        // subclass android.app.Application.

        // Method SoLoader.init is idempotent, so if you wish to use native exopackage, just call
        // SoLoader.init with appropriate args before initializing ReactInstanceManager
        SoLoader.init(applicationContext, /* native exopackage */ false);
    }
}

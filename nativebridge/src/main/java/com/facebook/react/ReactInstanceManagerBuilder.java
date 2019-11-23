// Copyright (c) Facebook, Inc. and its affiliates.

// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

package com.facebook.react;

import android.app.Activity;
import android.app.Application;

import com.facebook.infer.annotation.Assertions;
import com.facebook.react.bridge.JSBundleLoader;
import com.facebook.react.bridge.JSIModulePackage;
import com.facebook.react.bridge.JavaScriptExecutorFactory;
import com.facebook.react.bridge.NativeModuleCallExceptionHandler;
import com.facebook.react.bridge.NotThreadSafeBridgeIdleDebugListener;
import com.facebook.react.jscexecutor.JSCExecutorFactory;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

/**
 * Builder class for {@link ReactInstanceManager}
 */
public class ReactInstanceManagerBuilder {

    private final List<ReactPackage> mPackages = new ArrayList<>();

    private @Nullable
    String mJSBundleAssetUrl;
    private @Nullable
    JSBundleLoader mJSBundleLoader;
    private @Nullable
    String mJSMainModulePath;
    private @Nullable
    NotThreadSafeBridgeIdleDebugListener mBridgeIdleDebugListener;
    private @Nullable
    Application mApplication;
    private boolean mUseDeveloperSupport;
    private @Nullable
    NativeModuleCallExceptionHandler mNativeModuleCallExceptionHandler;
    private @Nullable
    Activity mCurrentActivity;
    private boolean mLazyViewManagersEnabled;
    private @Nullable
    JavaScriptExecutorFactory mJavaScriptExecutorFactory;
    private int mMinNumShakes = 1;
    private int mMinTimeLeftInFrameForNonBatchedOperationMs = -1;
    private @Nullable
    JSIModulePackage mJSIModulesPackage;

    /* package protected */ ReactInstanceManagerBuilder() {
    }


    public ReactInstanceManagerBuilder setJSIModulesPackage(
            @Nullable JSIModulePackage jsiModulePackage) {
        mJSIModulesPackage = jsiModulePackage;
        return this;
    }

    /**
     * Factory for desired implementation of JavaScriptExecutor.
     */
    public ReactInstanceManagerBuilder setJavaScriptExecutorFactory(
            @Nullable JavaScriptExecutorFactory javaScriptExecutorFactory) {
        mJavaScriptExecutorFactory = javaScriptExecutorFactory;
        return this;
    }

    /**
     * Name of the JS bundle file to be loaded from application's raw assets.
     * Example: {@code "index.android.js"}
     */
    public ReactInstanceManagerBuilder setBundleAssetName(String bundleAssetName) {
        mJSBundleAssetUrl = (bundleAssetName == null ? null : "assets://" + bundleAssetName);
        mJSBundleLoader = null;
        return this;
    }

    /**
     * Path to the JS bundle file to be loaded from the file system.
     * <p>
     * Example: {@code "assets://index.android.js" or "/sdcard/main.jsbundle"}
     */
    public ReactInstanceManagerBuilder setJSBundleFile(String jsBundleFile) {
        if (jsBundleFile.startsWith("assets://")) {
            mJSBundleAssetUrl = jsBundleFile;
            mJSBundleLoader = null;
            return this;
        }
        return setJSBundleLoader(JSBundleLoader.createFileLoader(jsBundleFile));
    }

    /**
     * Bundle loader to use when setting up JS environment. This supersedes
     * prior invocations of {@link setJSBundleFile} and {@link setBundleAssetName}.
     * <p>
     * Example: {@code JSBundleLoader.createFileLoader(application, bundleFile)}
     */
    public ReactInstanceManagerBuilder setJSBundleLoader(JSBundleLoader jsBundleLoader) {
        mJSBundleLoader = jsBundleLoader;
        mJSBundleAssetUrl = null;
        return this;
    }

    /**
     * Path to your app's main module on the packager server. This is used when
     * reloading JS during development. All paths are relative to the root folder
     * the packager is serving files from.
     * Examples:
     * {@code "index.android"} or
     * {@code "subdirectory/index.android"}
     */
    public ReactInstanceManagerBuilder setJSMainModulePath(String jsMainModulePath) {
        mJSMainModulePath = jsMainModulePath;
        return this;
    }

    public ReactInstanceManagerBuilder addPackage(ReactPackage reactPackage) {
        mPackages.add(reactPackage);
        return this;
    }

    public ReactInstanceManagerBuilder addPackages(List<ReactPackage> reactPackages) {
        mPackages.addAll(reactPackages);
        return this;
    }

    public ReactInstanceManagerBuilder setBridgeIdleDebugListener(
            NotThreadSafeBridgeIdleDebugListener bridgeIdleDebugListener) {
        mBridgeIdleDebugListener = bridgeIdleDebugListener;
        return this;
    }

    /**
     * Required. This must be your {@code Application} instance.
     */
    public ReactInstanceManagerBuilder setApplication(Application application) {
        mApplication = application;
        return this;
    }

    public ReactInstanceManagerBuilder setCurrentActivity(Activity activity) {
        mCurrentActivity = activity;
        return this;
    }


    /**
     * Set the exception handler for all native module calls. If not set, the default
     * {@link DevSupportManager} will be used, which shows a redbox in dev mode and rethrows
     * (crashes the app) in prod mode.
     */
    public ReactInstanceManagerBuilder setNativeModuleCallExceptionHandler(
            NativeModuleCallExceptionHandler handler) {
        mNativeModuleCallExceptionHandler = handler;
        return this;
    }


    public ReactInstanceManagerBuilder setLazyViewManagersEnabled(boolean lazyViewManagersEnabled) {
        mLazyViewManagersEnabled = lazyViewManagersEnabled;
        return this;
    }


    /**
     * Instantiates a new {@link ReactInstanceManager}.
     * Before calling {@code build}, the following must be called:
     * <ul>
     * <li> {@link #setApplication}
     * <li> {@link #setCurrentActivity} if the activity has already resumed
     * <li> {@link #setDefaultHardwareBackBtnHandler} if the activity has already resumed
     * <li> {@link #setJSBundleFile} or {@link #setJSMainModulePath}
     * </ul>
     */
    public ReactInstanceManager build() {
        Assertions.assertNotNull(
                mApplication,
                "Application property has not been set with this builder");

        Assertions.assertCondition(
                mUseDeveloperSupport || mJSBundleAssetUrl != null || mJSBundleLoader != null,
                "JS Bundle File or Asset URL has to be provided when dev support is disabled");

        Assertions.assertCondition(
                mJSMainModulePath != null || mJSBundleAssetUrl != null || mJSBundleLoader != null,
                "Either MainModulePath or JS Bundle File needs to be provided");

        // We use the name of the device and the app for debugging & metrics
        String appName = mApplication.getPackageName();

        return new ReactInstanceManager(
                mApplication,
                mCurrentActivity,
                mJavaScriptExecutorFactory == null
                        ? new JSCExecutorFactory(appName, "android")
                        : mJavaScriptExecutorFactory,
                (mJSBundleLoader == null && mJSBundleAssetUrl != null)
                        ? JSBundleLoader.createAssetLoader(
                        mApplication, mJSBundleAssetUrl, false /*Asynchronous*/)
                        : mJSBundleLoader,
                mJSMainModulePath,
                mPackages,
                mUseDeveloperSupport,
                mBridgeIdleDebugListener,
                mNativeModuleCallExceptionHandler,
                mLazyViewManagersEnabled,
                mMinNumShakes,
                mMinTimeLeftInFrameForNonBatchedOperationMs,
                mJSIModulesPackage);
    }
}

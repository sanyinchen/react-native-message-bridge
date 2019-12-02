/**
 * Copyright (c) Facebook, Inc. and its affiliates.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.react;


import android.app.Activity;
import android.content.Context;
import android.os.Process;
import android.util.Log;

import com.facebook.debug.holder.PrinterHolder;
import com.facebook.debug.tags.ReactDebugOverlayTags;
import com.facebook.infer.annotation.Assertions;
import com.facebook.infer.annotation.ThreadConfined;
import com.facebook.infer.annotation.ThreadSafe;
import com.facebook.react.bridge.CatalystInstance;
import com.facebook.react.bridge.CatalystInstanceImpl;
import com.facebook.react.bridge.JSBundleLoader;
import com.facebook.react.bridge.JSIModulePackage;
import com.facebook.react.bridge.JavaScriptExecutor;
import com.facebook.react.bridge.JavaScriptExecutorFactory;
import com.facebook.react.bridge.NativeModuleCallExceptionHandler;
import com.facebook.react.bridge.NativeModuleRegistry;
import com.facebook.react.bridge.NotThreadSafeBridgeIdleDebugListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactMarker;
import com.facebook.react.bridge.ReactMarkerConstants;
import com.facebook.react.bridge.UiThreadUtil;
import com.facebook.react.bridge.queue.ReactQueueConfigurationSpec;
import com.facebook.react.common.ReactConstants;
import com.facebook.react.module.core.ReactChoreographer;
import com.facebook.soloader.SoLoader;
import com.facebook.systrace.Systrace;
import com.facebook.systrace.SystraceMessage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import static com.facebook.infer.annotation.ThreadConfined.UI;
import static com.facebook.react.bridge.ReactMarkerConstants.ATTACH_MEASURED_ROOT_VIEWS_END;
import static com.facebook.react.bridge.ReactMarkerConstants.ATTACH_MEASURED_ROOT_VIEWS_START;
import static com.facebook.react.bridge.ReactMarkerConstants.BUILD_NATIVE_MODULE_REGISTRY_END;
import static com.facebook.react.bridge.ReactMarkerConstants.BUILD_NATIVE_MODULE_REGISTRY_START;
import static com.facebook.react.bridge.ReactMarkerConstants.CHANGE_THREAD_PRIORITY;
import static com.facebook.react.bridge.ReactMarkerConstants.CREATE_CATALYST_INSTANCE_END;
import static com.facebook.react.bridge.ReactMarkerConstants.CREATE_CATALYST_INSTANCE_START;
import static com.facebook.react.bridge.ReactMarkerConstants.CREATE_REACT_CONTEXT_START;
import static com.facebook.react.bridge.ReactMarkerConstants.PRE_SETUP_REACT_CONTEXT_END;
import static com.facebook.react.bridge.ReactMarkerConstants.PRE_SETUP_REACT_CONTEXT_START;
import static com.facebook.react.bridge.ReactMarkerConstants.PROCESS_PACKAGES_END;
import static com.facebook.react.bridge.ReactMarkerConstants.PROCESS_PACKAGES_START;
import static com.facebook.react.bridge.ReactMarkerConstants.REACT_CONTEXT_THREAD_END;
import static com.facebook.react.bridge.ReactMarkerConstants.REACT_CONTEXT_THREAD_START;
import static com.facebook.react.bridge.ReactMarkerConstants.SETUP_REACT_CONTEXT_END;
import static com.facebook.react.bridge.ReactMarkerConstants.SETUP_REACT_CONTEXT_START;
import static com.facebook.react.bridge.ReactMarkerConstants.VM_INIT;
import static com.facebook.systrace.Systrace.TRACE_TAG_REACT_APPS;
import static com.facebook.systrace.Systrace.TRACE_TAG_REACT_JAVA_BRIDGE;
import static com.facebook.systrace.Systrace.TRACE_TAG_REACT_JS_VM_CALLS;

@ThreadSafe
public class ReactInstanceManager {

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
            mPackages.add(
                    new CoreModulesPackage(
                            this,
                            lazyViewManagersEnabled,
                            minTimeLeftInFrameForNonBatchedOperationMs));

            mPackages.addAll(packages);
        }
        mJSIModulePackage = jsiModulePackage;

        // Instantiate ReactChoreographer in UI thread.
        ReactChoreographer.initialize();

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

    @ThreadConfined(UI)
    public void createReactContextInBackground() {
        Log.d(ReactConstants.TAG, "ReactInstanceManager.createReactContextInBackground()");
        Assertions.assertCondition(
                !mHasStartedCreatingInitialContext,
                "createReactContextInBackground should only be called when creating the react " +
                        "application for the first time. When reloading JS, e.g. from a new file, explicitly" +
                        "use recreateReactContextInBackground");

        mHasStartedCreatingInitialContext = true;
        recreateReactContextInBackgroundInner();
    }

    @ThreadConfined(UI)
    private void recreateReactContextInBackgroundInner() {
        Log.d(ReactConstants.TAG, "ReactInstanceManager.recreateReactContextInBackgroundInner()");
        PrinterHolder.getPrinter()
                .logMessage(ReactDebugOverlayTags.RN_CORE, "RNCore: recreateReactContextInBackground");
        UiThreadUtil.assertOnUiThread();


        recreateReactContextInBackgroundFromBundleLoader();
    }

    @ThreadConfined(UI)
    private void recreateReactContextInBackgroundFromBundleLoader() {
        Log.d(
                ReactConstants.TAG,
                "ReactInstanceManager.recreateReactContextInBackgroundFromBundleLoader()");
        PrinterHolder.getPrinter()
                .logMessage(ReactDebugOverlayTags.RN_CORE, "RNCore: load from BundleLoader");
        recreateReactContextInBackground(mJavaScriptExecutorFactory, mBundleLoader);
    }

    @ThreadConfined(UI)
    private void recreateReactContextInBackground(
            JavaScriptExecutorFactory jsExecutorFactory,
            JSBundleLoader jsBundleLoader) {
        Log.d(ReactConstants.TAG, "ReactInstanceManager.recreateReactContextInBackground()");
        UiThreadUtil.assertOnUiThread();

        final ReactContextInitParams initParams = new ReactContextInitParams(
                jsExecutorFactory,
                jsBundleLoader);
        if (mCreateReactContextThread == null) {
            runCreateReactContextOnNewThread(initParams);
        } else {
            mPendingReactContextInitParams = initParams;
        }
    }

    @ThreadConfined(UI)
    private void runCreateReactContextOnNewThread(final ReactContextInitParams initParams) {
        Log.d(ReactConstants.TAG, "ReactInstanceManager.runCreateReactContextOnNewThread()");
        UiThreadUtil.assertOnUiThread();


        mCreateReactContextThread =
                new Thread(
                        null,
                        new Runnable() {
                            @Override
                            public void run() {
                                ReactMarker.logMarker(REACT_CONTEXT_THREAD_END);
                                synchronized (ReactInstanceManager.this.mHasStartedDestroying) {
                                    while (ReactInstanceManager.this.mHasStartedDestroying) {
                                        try {
                                            ReactInstanceManager.this.mHasStartedDestroying.wait();
                                        } catch (InterruptedException e) {
                                            continue;
                                        }
                                    }
                                }
                                // As destroy() may have run and set this to false, ensure that it is true before we create
                                mHasStartedCreatingInitialContext = true;

                                try {
                                    Process.setThreadPriority(Process.THREAD_PRIORITY_DISPLAY);
                                    ReactMarker.logMarker(VM_INIT);
                                    final ReactApplicationContext reactApplicationContext =
                                            createReactContext(
                                                    initParams.getJsExecutorFactory().create(),
                                                    initParams.getJsBundleLoader());

                                    mCreateReactContextThread = null;
                                    ReactMarker.logMarker(PRE_SETUP_REACT_CONTEXT_START);
                                    final Runnable maybeRecreateReactContextRunnable =
                                            new Runnable() {
                                                @Override
                                                public void run() {
                                                    if (mPendingReactContextInitParams != null) {
                                                        runCreateReactContextOnNewThread(mPendingReactContextInitParams);
                                                        mPendingReactContextInitParams = null;
                                                    }
                                                }
                                            };
                                    Runnable setupReactContextRunnable =
                                            new Runnable() {
                                                @Override
                                                public void run() {
                                                    try {
                                                        setupReactContext(reactApplicationContext);
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                        // mDevSupportManager.handleException(e);
                                                    }
                                                }
                                            };

                                    reactApplicationContext.runOnNativeModulesQueueThread(setupReactContextRunnable);
                                    UiThreadUtil.runOnUiThread(maybeRecreateReactContextRunnable);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        "create_react_context");
        ReactMarker.logMarker(REACT_CONTEXT_THREAD_START);
        mCreateReactContextThread.start();
    }

    private void setupReactContext(final ReactApplicationContext reactContext) {
        Log.d(ReactConstants.TAG, "ReactInstanceManager.setupReactContext()");
        ReactMarker.logMarker(PRE_SETUP_REACT_CONTEXT_END);
        ReactMarker.logMarker(SETUP_REACT_CONTEXT_START);
        Systrace.beginSection(TRACE_TAG_REACT_JAVA_BRIDGE, "setupReactContext");
        synchronized (mReactContextLock) {
            mCurrentReactContext = Assertions.assertNotNull(reactContext);
        }

        CatalystInstance catalystInstance =
                Assertions.assertNotNull(reactContext.getCatalystInstance());

        catalystInstance.initialize();

        ReactMarker.logMarker(ATTACH_MEASURED_ROOT_VIEWS_START);
        ReactMarker.logMarker(ATTACH_MEASURED_ROOT_VIEWS_END);


        ReactInstanceEventListener[] listeners =
                new ReactInstanceEventListener[mReactInstanceEventListeners.size()];
        final ReactInstanceEventListener[] finalListeners =
                mReactInstanceEventListeners.toArray(listeners);

        UiThreadUtil.runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        for (ReactInstanceEventListener listener : finalListeners) {
                            listener.onReactContextInitialized(reactContext);
                        }
                    }
                });
        Systrace.endSection(TRACE_TAG_REACT_JAVA_BRIDGE);
        ReactMarker.logMarker(SETUP_REACT_CONTEXT_END);
        reactContext.runOnJSQueueThread(
                new Runnable() {
                    @Override
                    public void run() {
                        Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT);
                        ReactMarker.logMarker(CHANGE_THREAD_PRIORITY, "js_default");
                    }
                });
        reactContext.runOnNativeModulesQueueThread(
                new Runnable() {
                    @Override
                    public void run() {
                        Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT);
                    }
                });
    }

    /**
     * @return instance of {@link ReactContext} configured a {@link CatalystInstance} set
     */
    private ReactApplicationContext createReactContext(
            JavaScriptExecutor jsExecutor,
            JSBundleLoader jsBundleLoader) {
        Log.d(ReactConstants.TAG, "ReactInstanceManager.createReactContext()");
        ReactMarker.logMarker(CREATE_REACT_CONTEXT_START, jsExecutor.getName());
        final ReactApplicationContext reactContext = new ReactApplicationContext(mApplicationContext);

        NativeModuleCallExceptionHandler exceptionHandler = mNativeModuleCallExceptionHandler;

        reactContext.setNativeModuleCallExceptionHandler(exceptionHandler);

        NativeModuleRegistry nativeModuleRegistry = processPackages(reactContext, mPackages, false);

        CatalystInstanceImpl.Builder catalystInstanceBuilder = new CatalystInstanceImpl.Builder()
                .setReactQueueConfigurationSpec(ReactQueueConfigurationSpec.createDefault())
                .setJSExecutor(jsExecutor)
                .setRegistry(nativeModuleRegistry)
                .setJSBundleLoader(jsBundleLoader)
                .setNativeModuleCallExceptionHandler(exceptionHandler);

        ReactMarker.logMarker(CREATE_CATALYST_INSTANCE_START);
        // CREATE_CATALYST_INSTANCE_END is in JSCExecutor.cpp
        Systrace.beginSection(TRACE_TAG_REACT_JAVA_BRIDGE, "createCatalystInstance");
        final CatalystInstance catalystInstance;
        try {
            catalystInstance = catalystInstanceBuilder.build();
        } finally {
            Systrace.endSection(TRACE_TAG_REACT_JAVA_BRIDGE);
            ReactMarker.logMarker(CREATE_CATALYST_INSTANCE_END);
        }
        if (mJSIModulePackage != null) {
            catalystInstance.addJSIModules(mJSIModulePackage
                    .getJSIModules(reactContext, catalystInstance.getJavaScriptContextHolder()));
        }

        if (Systrace.isTracing(TRACE_TAG_REACT_APPS | TRACE_TAG_REACT_JS_VM_CALLS)) {
            catalystInstance.setGlobalVariable("__RCTProfileIsProfiling", "true");
        }
        ReactMarker.logMarker(ReactMarkerConstants.PRE_RUN_JS_BUNDLE_START);
        Systrace.beginSection(TRACE_TAG_REACT_JAVA_BRIDGE, "runJSBundle");
        catalystInstance.runJSBundle();
        Systrace.endSection(TRACE_TAG_REACT_JAVA_BRIDGE);

        reactContext.initializeWithInstance(catalystInstance);


        return reactContext;
    }

    private NativeModuleRegistry processPackages(
            ReactApplicationContext reactContext,
            List<ReactPackage> packages,
            boolean checkAndUpdatePackageMembership) {
        NativeModuleRegistryBuilder nativeModuleRegistryBuilder = new NativeModuleRegistryBuilder(
                reactContext,
                this);

        ReactMarker.logMarker(PROCESS_PACKAGES_START);

        // TODO(6818138): Solve use-case of native modules overriding
        synchronized (mPackages) {
            for (ReactPackage reactPackage : packages) {
                if (checkAndUpdatePackageMembership && mPackages.contains(reactPackage)) {
                    continue;
                }
                Systrace.beginSection(TRACE_TAG_REACT_JAVA_BRIDGE, "createAndProcessCustomReactPackage");
                try {
                    if (checkAndUpdatePackageMembership) {
                        mPackages.add(reactPackage);
                    }
                    processPackage(reactPackage, nativeModuleRegistryBuilder);
                } finally {
                    Systrace.endSection(TRACE_TAG_REACT_JAVA_BRIDGE);
                }
            }
        }
        ReactMarker.logMarker(PROCESS_PACKAGES_END);

        ReactMarker.logMarker(BUILD_NATIVE_MODULE_REGISTRY_START);
        Systrace.beginSection(TRACE_TAG_REACT_JAVA_BRIDGE, "buildNativeModuleRegistry");
        NativeModuleRegistry nativeModuleRegistry;
        try {
            nativeModuleRegistry = nativeModuleRegistryBuilder.build();
        } finally {
            Systrace.endSection(TRACE_TAG_REACT_JAVA_BRIDGE);
            ReactMarker.logMarker(BUILD_NATIVE_MODULE_REGISTRY_END);
        }

        return nativeModuleRegistry;
    }

    private void processPackage(
            ReactPackage reactPackage,
            NativeModuleRegistryBuilder nativeModuleRegistryBuilder) {
        SystraceMessage.beginSection(TRACE_TAG_REACT_JAVA_BRIDGE, "processPackage")
                .arg("className", reactPackage.getClass().getSimpleName())
                .flush();
        if (reactPackage instanceof ReactPackageLogger) {
            ((ReactPackageLogger) reactPackage).startProcessPackage();
        }
        nativeModuleRegistryBuilder.processPackage(reactPackage);

        if (reactPackage instanceof ReactPackageLogger) {
            ((ReactPackageLogger) reactPackage).endProcessPackage();
        }
        SystraceMessage.endSection(TRACE_TAG_REACT_JAVA_BRIDGE).flush();
    }
}

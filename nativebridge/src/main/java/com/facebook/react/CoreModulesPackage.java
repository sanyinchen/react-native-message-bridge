/**
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * <p>This source code is licensed under the MIT license found in the LICENSE file in the root
 * directory of this source tree.
 */
package com.facebook.react;

import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactMarker;
import com.facebook.react.module.annotations.ReactModule;
import com.facebook.react.module.annotations.ReactModuleList;
import com.facebook.react.module.core.Timing;
import com.facebook.react.module.model.ReactModuleInfo;
import com.facebook.react.module.model.ReactModuleInfoProvider;

import com.facebook.systrace.Systrace;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import static com.facebook.react.bridge.ReactMarkerConstants.CREATE_UI_MANAGER_MODULE_END;
import static com.facebook.react.bridge.ReactMarkerConstants.CREATE_UI_MANAGER_MODULE_START;
import static com.facebook.react.bridge.ReactMarkerConstants.PROCESS_CORE_REACT_PACKAGE_END;
import static com.facebook.react.bridge.ReactMarkerConstants.PROCESS_CORE_REACT_PACKAGE_START;

/**
 * This is the basic module to support React Native. The debug modules are now in DebugCorePackage.
 */
@ReactModuleList(
        // WARNING: If you modify this list, ensure that the list below in method
        // getReactModuleInfoByInitialization is also updated
        nativeModules = {
                Timing.class,
        })
        /* package */ class CoreModulesPackage extends TurboReactPackage implements ReactPackageLogger {

    private final ReactInstanceManager mReactInstanceManager;
    private final boolean mLazyViewManagersEnabled;
    private final int mMinTimeLeftInFrameForNonBatchedOperationMs;

    CoreModulesPackage(
            ReactInstanceManager reactInstanceManager,
            boolean lazyViewManagersEnabled,
            int minTimeLeftInFrameForNonBatchedOperationMs) {
        mReactInstanceManager = reactInstanceManager;
        mLazyViewManagersEnabled = lazyViewManagersEnabled;
        mMinTimeLeftInFrameForNonBatchedOperationMs = minTimeLeftInFrameForNonBatchedOperationMs;
    }

    /**
     * This method is overridden, since OSS does not run the annotation processor to generate {@link
     * CoreModulesPackage$$ReactModuleInfoProvider} class. Here we check if it exists. If it does not
     * exist, we generate one manually in {@link
     * CoreModulesPackage#getReactModuleInfoByInitialization()} and return that instead.
     */
    @Override
    public ReactModuleInfoProvider getReactModuleInfoProvider() {
        try {
            Class<?> reactModuleInfoProviderClass =
                    Class.forName("com.facebook.react.CoreModulesPackage$$ReactModuleInfoProvider");
            return (ReactModuleInfoProvider) reactModuleInfoProviderClass.newInstance();
        } catch (ClassNotFoundException e) {
            // In OSS case, the annotation processor does not run. We fall back on creating this byhand
            Class<? extends NativeModule>[] moduleList =
                    new Class[]{
                            Timing.class,
                    };

            final Map<String, ReactModuleInfo> reactModuleInfoMap = new HashMap<>();
            for (Class<? extends NativeModule> moduleClass : moduleList) {
                ReactModule reactModule = moduleClass.getAnnotation(ReactModule.class);

                reactModuleInfoMap.put(
                        reactModule.name(),
                        new ReactModuleInfo(
                                reactModule.name(),
                                moduleClass.getName(),
                                reactModule.canOverrideExistingModule(),
                                reactModule.needsEagerInit(),
                                reactModule.hasConstants(),
                                reactModule.isCxxModule(),
                                false));
            }

            return new ReactModuleInfoProvider() {
                @Override
                public Map<String, ReactModuleInfo> getReactModuleInfos() {
                    return reactModuleInfoMap;
                }
            };
        } catch (InstantiationException e) {
            throw new RuntimeException(
                    "No ReactModuleInfoProvider for CoreModulesPackage$$ReactModuleInfoProvider", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(
                    "No ReactModuleInfoProvider for CoreModulesPackage$$ReactModuleInfoProvider", e);
        }
    }

    @Override
    public NativeModule getModule(String name, ReactApplicationContext reactContext) {
        switch (name) {
            case Timing.NAME:
                return new Timing(reactContext);
            default:
                throw new IllegalArgumentException(
                        "In CoreModulesPackage, could not find Native module for " + name);
        }
    }

    @Override
    public void startProcessPackage() {
        ReactMarker.logMarker(PROCESS_CORE_REACT_PACKAGE_START);
    }

    @Override
    public void endProcessPackage() {
        ReactMarker.logMarker(PROCESS_CORE_REACT_PACKAGE_END);
    }
}

package com.nickhagenov.RCTCamera;

import java.util.Collections;
import java.util.List;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;
import com.facebook.react.bridge.JavaScriptModule;

public class RCTCameraPackage implements ReactPackage {
    private static RCTCameraModule instance = null;

    public static RCTCameraModule getModuleInstance() {
        return instance;
    }
    @Override
    public List<NativeModule> createNativeModules(ReactApplicationContext reactApplicationContext) {
        instance = new RCTCameraModule(reactApplicationContext);
        return Collections.<NativeModule>singletonList(instance);
    }

    @Override
    public List<Class<? extends JavaScriptModule>> createJSModules() {
        return Collections.emptyList();
    }

    @Override
    public List<ViewManager> createViewManagers(ReactApplicationContext reactApplicationContext) {
        //noinspection ArraysAsListWithZeroOrOneArgument
        return Collections.<ViewManager>singletonList(new RCTCameraViewManager());
    }

}

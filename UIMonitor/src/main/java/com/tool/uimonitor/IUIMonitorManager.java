package com.tool.uimonitor;

import android.support.annotation.NonNull;

public interface IUIMonitorManager {
    String version();
    void start();
    void stop();
    void dump();
    void dump(@NonNull String serviceName);
}

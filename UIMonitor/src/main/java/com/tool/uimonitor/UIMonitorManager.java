package com.tool.uimonitor;

import android.support.annotation.NonNull;

import com.tool.log.LOG;
import com.tool.log.LogTag;

public class UIMonitorManager implements IUIMonitorManager {
    private static UIMonitorManager manager = new UIMonitorManager();
    private static String VERSION = "1.0.0";

    private UIMonitorManager(){
        LOG.Logger logcatLogger = new LOG.LogcatLogger()
                .setLogLevel(LOG.LEVEL_VERBOSE)
                .enableFullClassName(false);
        LOG.setConsoleLogger(logcatLogger);
    }
    public static UIMonitorManager getInstance() {
        return manager;
    }

    @Override
    public String version() {
        return VERSION;
    }

    @Override
    public void start() {
        UIMonitor.getInstance().start();
    }

    @Override
    public void stop() {
        UIMonitor.getInstance().stop();
    }

    @Override
    public void dump() {
        StringBuilder builder = new StringBuilder();
        StateServiceHub.dumpAllState(builder);
        LOG.d("%s dump all UIMonitor : %s", LogTag.TAG_DUMP,builder.toString());
    }

    @Override
    public void dump(@NonNull String serviceName) {
        StringBuilder builder = new StringBuilder();
        StateServiceHub.dumpServiceState(serviceName, builder);
        LOG.d("%s dump serviceName UIMonitor : %s", LogTag.TAG_DUMP,builder.toString());

    }
}

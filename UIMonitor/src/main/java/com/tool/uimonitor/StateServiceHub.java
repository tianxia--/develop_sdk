package com.tool.uimonitor;

import android.support.annotation.NonNull;

import com.tool.log.LOG;
import com.tool.log.LogTag;

public class StateServiceHub {
    public interface StateService {
        String serviceName();
        void dumpState(StringBuilder sb);
    }

    private static WeakListenerManager<StateService> allServices = new WeakListenerManager<>();

    public static void register(@NonNull StateService service) {
        allServices.addListener(service);
    }

    public static void dumpAllState(@NonNull StringBuilder sb) {
        allServices.notifyListeners(l -> {
            l.dumpState(sb);
        });
    }

    public static void dumpServiceState(@NonNull String serviceName, @NonNull StringBuilder sb) {
        allServices.notifyListeners(l -> {
            if (serviceName.equals(l.serviceName())) {
                l.dumpState(sb);
            }
        });
    }

    public static void logAllState() {
        StringBuilder sb = new StringBuilder(4096);
        dumpAllState(sb);
        LOG.i("%s Dump of all state\n%s", LogTag.TAG_DUMP, sb.toString());
    }
}

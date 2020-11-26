package com.tool.uimonitor;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.SystemClock;
import android.support.annotation.NonNull;

import com.tool.log.LOG;
import com.tool.log.LogTag;

import java.util.Locale;

class UIMonitor implements StateServiceHub.StateService {
    private static final int TIME_BLOCK = 800;

    private static UIMonitor instance = new UIMonitor();

    private final long monitorStartTime;
    private int dispatchCount;
    private int blockCount;

    private long timeStart;

    private StackTraceElement[] lastBlockStackTrace;

    private Handler handler;
    private Runnable monitorTask = new Runnable() {
        @Override
        public void run() {
            lastBlockStackTrace = Looper.getMainLooper().getThread().getStackTrace();
        }
    };

    private UIMonitor() {
        HandlerThread monitorThread = new HandlerThread("ui-monitor");
        monitorThread.start();
        handler = new Handler(monitorThread.getLooper());
        monitorStartTime = SystemClock.elapsedRealtime();
        StateServiceHub.register(this);
    }

    static UIMonitor getInstance() {
        return instance;
    }

    void start() {
        LOG.i("%s UIMonitor started", LogTag.TAG_THREAD);
        Looper.getMainLooper().setMessageLogging(x -> {
            if (x.startsWith(">>>>> Dispatching")) {
                onMessageStart();
            } else if (x.startsWith("<<<<< Finished")) {
                onMessageFinish();
            }
        });
    }

    void stop(){
        LOG.i("%s UIMonitor stop", LogTag.TAG_THREAD);
        Looper.getMainLooper().setMessageLogging(null);
    }

    private void onMessageStart() {
        if (dispatchCount < Integer.MAX_VALUE) {
            dispatchCount++;
            timeStart = SystemClock.elapsedRealtime();
            lastBlockStackTrace = null;
            handler.postDelayed(monitorTask,TIME_BLOCK);
        } else {
            timeStart = 0;
        }
    }

    private void onMessageFinish() {
        if (timeStart > 0) {
            handler.removeCallbacks(monitorTask);
            long timeUsed = SystemClock.elapsedRealtime() - timeStart;
            StackTraceElement[] blockStackTrace = lastBlockStackTrace;
            if (timeUsed > TIME_BLOCK && blockStackTrace != null) {
                logBlockEvent(timeUsed, blockStackTrace);
            }
        }
    }

    private void logBlockEvent(long timeUsed, @NonNull StackTraceElement[] blockStackTrace) {
        blockCount++;

        final int maxStacksPerLog = 30;
        final boolean multiPart = blockStackTrace.length > maxStacksPerLog;
        StringBuilder sb = new StringBuilder();
        int count = 0;
        int partNo = 0;
        for (StackTraceElement s : blockStackTrace) {
            sb.append("\tat ").append(s.toString()).append("\n");
            if (multiPart && ++count == maxStacksPerLog) {
                LOG.e("%s UIMonitor found block, timeUsed: %dms (Part %d)\n%s",
                        LogTag.TAG_THREAD, timeUsed, ++partNo, sb.toString());
                sb = new StringBuilder();
                count = 0;
            }
        }

        if (sb.length() > 0) {
            if (multiPart) {
                LOG.e("%s UIMonitor found block, timeUsed: %dms (Part %d)\n%s",
                        LogTag.TAG_THREAD, timeUsed, ++partNo, sb.toString());
            } else {
                LOG.e("%s UIMonitor found block, timeUsed: %dms\n%s",
                        LogTag.TAG_THREAD, timeUsed, sb.toString());
            }
        }
    }

    @Override
    public String serviceName() {
        return "UIMonitor";
    }

    @Override
    public void dumpState(StringBuilder sb) {
        long timeUsed = SystemClock.elapsedRealtime() - monitorStartTime;
        String timeUsedStr = timeUsed + "ms";
        int totalCount = dispatchCount;
        int blockCount = this.blockCount;

        String blocksRate = String.format(Locale.US, "%d/%d = %.2f%%",
                blockCount, totalCount, blockCount * 100f / totalCount);
        sb.append("Dump of UIMonitor:\n");
        sb.append("  time: ").append(timeUsedStr).append('\n');
        sb.append("  dispatches: ").append(totalCount).append('\n');
        sb.append("  blocks: ").append(blockCount).append('\n');
        sb.append("  blocksRate: ").append(blocksRate).append('\n');
    }
}

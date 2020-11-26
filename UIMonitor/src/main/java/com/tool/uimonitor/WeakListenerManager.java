package com.tool.uimonitor;

import android.support.annotation.NonNull;

import com.tool.log.LOG;
import com.tool.log.LogTag;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class WeakListenerManager<IListener> {
    public interface NotifyAction<IListener> {
        void notify(IListener listener);
    }

    private class ListenerInfo {
        String className;
        WeakReference<IListener> holder;

        ListenerInfo(IListener listener) {
            className = listener.getClass().getName();
            holder = new WeakReference<>(listener);
        }
    }

    private final List<ListenerInfo> mListeners = new ArrayList<>();

    /**
     * Only invoked when invoke {@link #addListener(Object)}
     */
    protected void onFirstListenerAdd() {
        // nothing to do
    }

    /**
     * Only invoked when invoke {@link #removeListener(Object)}
     */
    protected void onLastListenerRemoved() {
        // nothing to do
    }

    /**
     * Override this method to notify the listener when registered.
     */
    protected void onListenerAdded(@NonNull IListener listener) {
        // nothing to do
    }

    public void addListener(@NonNull IListener listener) {
        synchronized (mListeners) {
            if (mListeners.size() == 0) {
                onFirstListenerAdd();
            }

            for (ListenerInfo l : mListeners) {
                if (l.holder.get() == listener) return; // skip duplicate listeners
            }
            mListeners.add(new ListenerInfo(listener));
        }

        // Notify the listener to get initialized
        onListenerAdded(listener);
    }

    public void removeListener(@NonNull IListener listener) {
        synchronized (mListeners) {
            final int N = mListeners.size();
            boolean removed = false;
            for (int i = 0; i < N; i++) {
                ListenerInfo listenerInfo = mListeners.get(i);
                if (listenerInfo.holder.get() == listener) {
                    mListeners.remove(i);
                    removed = true;
                    break;
                }
            }
            if (mListeners.size() == 0 && removed) {
                onLastListenerRemoved();
            }
        }
    }

    public void notifyListeners(@NonNull NotifyAction<IListener> action) {
        synchronized (mListeners) {
            // The listener may unregister itself!
            List<ListenerInfo> listenersCopied = new ArrayList<>(mListeners);
            for (int i = 0; i < listenersCopied.size(); i++) {
                ListenerInfo listenerInfo = listenersCopied.get(i);
                IListener l = listenerInfo.holder.get();
                if (l == null) {
                    LOG.e("%s listener leak found: %s", LogTag.TAG_BASE_MISC, listenerInfo.className);
                    mListeners.remove(listenerInfo);
                } else {
                    LOG.d("%s notify #%d: %s", LogTag.TAG_BASE_MISC, i, listenerInfo.className);
                    action.notify(l);
                }
            }
            LOG.d("%s notify done, cur size: %d", LogTag.TAG_BASE_MISC, mListeners.size());
        }
    }
}

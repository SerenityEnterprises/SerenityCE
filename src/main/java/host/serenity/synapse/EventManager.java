package host.serenity.synapse;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class EventManager {
    private static boolean DEBUG = true;
    private static Set<Listener> listeners = new CopyOnWriteArraySet<>();

    @SuppressWarnings("unchecked")
    public static <E> E post(E event) {
        listeners.forEach(listener -> {
            if (listener.getTargetClass() == event.getClass()) {
                if (listener.shouldListen()) {
                    try {
                        listener.call(event);
                    } catch (Exception e) {
                        if (DEBUG) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });

        return event;
    }

    public static void register(Listener<?> listener) {
        listeners.add(listener);
    }

    public static void unregister(Listener<?> listener) {
        listeners.remove(listener);
    }

    public static Set<Listener> getListeners() {
        return listeners;
    }
}

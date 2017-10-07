package host.serenity.synapse;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Stream;

public class EventManager {
    private static boolean DEBUG = true;
    private static final ConcurrentMap<Class<?>, Set<Listener>> LISTENERS = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public static <E> E post(E event) {
        Class<?> key = event.getClass();

        Set<Listener> listeners = LISTENERS.get(key);

        if (listeners == null) {
            return event;
        }

        listeners.forEach(listener -> {
            try {
                listener.call(event);
            } catch (Exception e) {
                if (DEBUG) {
                    e.printStackTrace();
                }
            }
        });

        return event;
    }

    public static void register(Listener<?> listener) {
        LISTENERS.computeIfAbsent(listener.getTargetClass(), ignored -> new CopyOnWriteArraySet<>()).add(listener);
    }

    public static void unregister(Listener<?> listener) {
        LISTENERS.computeIfPresent(listener.getTargetClass(), (ignored, listeners) -> {
            listeners.remove(listener);

            return listeners.isEmpty()
                    ? null
                    : listeners;
        });
    }

    public static Stream<Listener> getAllListeners() {
        return LISTENERS.values().stream().flatMap(Set::stream);
    }

    public static void clear() {
        LISTENERS.values().forEach(Set::clear);
        LISTENERS.clear();
    }
}

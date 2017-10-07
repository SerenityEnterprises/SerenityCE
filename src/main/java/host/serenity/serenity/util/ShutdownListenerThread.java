package host.serenity.serenity.util;

import host.serenity.serenity.event.internal.GameShutdown;
import host.serenity.synapse.EventManager;

public class ShutdownListenerThread extends Thread {
    public ShutdownListenerThread() {
        super(() -> {
            EventManager.post(new GameShutdown());
        });
    }
}

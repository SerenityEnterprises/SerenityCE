package host.serenity.serenity.modules.player;

import host.serenity.serenity.api.module.Module;
import host.serenity.serenity.api.module.ModuleCategory;
import host.serenity.serenity.api.module.mode.ModuleMode;
import host.serenity.serenity.api.value.Value;
import host.serenity.serenity.event.player.PlayerWalkingUpdate;
import host.serenity.synapse.Listener;

import java.util.List;
import java.util.Random;

public class Derp extends Module {
    public Derp() {
        super("Derp", 0xFF5297, ModuleCategory.PLAYER);

        registerMode(new ModuleMode("Random") {
            private Random random = new Random();

            @Override
            public void addListeners(List<Listener<?>> listeners) {
                listeners.add(new Listener<PlayerWalkingUpdate>() {
                    @Override
                    public void call(PlayerWalkingUpdate event) {
                        event.setYaw(random.nextFloat() * 360 - 180);
                        event.setPitch(random.nextFloat() * 180 - 90);
                    }
                });
            }

            @Override
            public Value<?>[] getValues() {
                return new Value<?>[0];
            }
        });

        registerMode(new ModuleMode("Fake Reverse") {
            @Override
            public void addListeners(List<Listener<?>> listeners) {
                listeners.add(new Listener<PlayerWalkingUpdate>() {
                    @Override
                    public void call(PlayerWalkingUpdate event) {
                        event.setYaw(event.getYaw() + 180);
                    }
                });
            }

            @Override
            public Value<?>[] getValues() {
                return new Value<?>[0];
            }
        });

        setActiveMode("Random");
    }
}

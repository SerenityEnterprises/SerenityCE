package host.serenity.serenity.util.prediction;

import host.serenity.serenity.event.player.PlayerUpdate;
import host.serenity.synapse.EventManager;
import host.serenity.synapse.Listener;
import me.jordin.deltoid.vector.Vec3;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.player.EntityPlayer;

import java.util.*;

public class PredictionEngine {
    private Map<EntityPlayer, List<Vec3>> playerPositions = new WeakHashMap<>();
    private int bufferSize = 10;

    private final Minecraft mc = Minecraft.getMinecraft();

    public PredictionEngine() {
        EventManager.register(new Listener<PlayerUpdate>() {
            @Override
            public void call(PlayerUpdate event) {
                playerPositions.keySet().removeIf(p -> !mc.theWorld.playerEntities.contains(p));

                //noinspection unchecked
                for (EntityPlayer player : (List<EntityPlayer>) mc.theWorld.playerEntities) {
                    playerPositions.putIfAbsent(player, new LinkedList<>());

                    List<Vec3> pastPositions = playerPositions.get(player);
                    Vec3 currentPosition = new Vec3(player.posX, player.posY, player.posZ);
                    if (!pastPositions.isEmpty()) {
                        Vec3 pastPosition = pastPositions.get(pastPositions.size() - 1);

                        if (currentPosition.subtract(pastPosition).length() > 8) {
                            pastPositions.clear();
                        }
                    }

                    pastPositions.add(currentPosition);
                    if (pastPositions.size() > bufferSize) {
                        int i = 0;
                        for (Vec3 position : new LinkedList<>(pastPositions)) {
                            if (i < pastPositions.size() - bufferSize) {
                                pastPositions.remove(position);
                            } else {
                                break;
                            }
                            i++;
                        }
                    }
                }
            }
        });
    }

    public Vec3 predictPlayerLocation(EntityPlayer player, int ticksIntoFuture) {
        if (playerPositions.containsKey(player)) {
            List<Vec3> pastPositions = playerPositions.get(player);
            if (pastPositions.size() > 1) {
                Vec3 first = pastPositions.get(0);

                List<Vec3> deltas = new LinkedList<>();
                Vec3 previous = first;
                for (Vec3 position : pastPositions) {
                    deltas.add(position.subtract(previous));
                    previous = position;
                }

                Vec3 mean = Vec3.ORIGIN;
                for (Vec3 delta : deltas)
                    mean = mean.add(delta);

                mean = mean.divideElements(deltas.size());

                EntityPlayer simulated = new EntityOtherPlayerMP(mc.theWorld, player.getGameProfile());
                simulated.noClip = false;
                simulated.setPosition(player.posX, player.posY, player.posZ);
                for (int i = 0; i < ticksIntoFuture; i++) {
                    double x = mean.x, y = mean.y, z = mean.z;
                    simulated.moveEntity(x, y, z);

                    if (!player.capabilities.isFlying) {
                        y = y * 0.8 - 0.02;
                    }
                    if (simulated.onGround && y < 0) {
                        y = 0;
                    }

                    x *= 0.99;
                    z *= 0.99;

                    mean = new Vec3(x, y, z);
                }

                return new Vec3(simulated.posX, simulated.posY, simulated.posZ);
            }
        }
        return null;
    }
}

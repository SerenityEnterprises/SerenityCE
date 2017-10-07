package host.serenity.serenity.modules.movement;

import host.serenity.serenity.api.module.Module;
import host.serenity.serenity.api.module.ModuleCategory;
import host.serenity.serenity.event.network.SendPacket;
import host.serenity.serenity.event.player._FinalPlayerWalkingUpdate;
import host.serenity.serenity.event.player.MoveInput;
import host.serenity.serenity.event.player.PlayerWalkingUpdate;
import host.serenity.synapse.Listener;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

public class NoSlowdown extends Module {
    private boolean hasUnblocked, shouldNotReblock;

    public NoSlowdown() {
        super("No Slowdown", 0x89FFA5, ModuleCategory.MOVEMENT);
        setHidden(true);

        listeners.add(new Listener<PlayerWalkingUpdate>() {
            @Override
            public void call(PlayerWalkingUpdate event) {
                if (mc.thePlayer.isBlocking()) {
                    if (!shouldNotReblock)
                        mc.getNetHandler().addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                }

                shouldNotReblock = false;
                hasUnblocked = true;
            }
        });

        listeners.add(new Listener<_FinalPlayerWalkingUpdate>() {
            @Override
            public void call(_FinalPlayerWalkingUpdate event) {
                if (mc.thePlayer.isBlocking()) {
                    if (!shouldNotReblock)
                        mc.getNetHandler().addToSendQueue(new C08PacketPlayerBlockPlacement(mc.thePlayer.getHeldItem()));
                }

                hasUnblocked = false;
            }
        });

        listeners.add(new Listener<SendPacket>() {
            @Override
            public void call(SendPacket event) {
                if (event.getPacket() instanceof C02PacketUseEntity) {
                    C02PacketUseEntity packet = (C02PacketUseEntity) event.getPacket();
                    if (packet.getAction() == C02PacketUseEntity.Action.ATTACK) {
                        if (hasUnblocked)
                            shouldNotReblock = true;
                    }
                }
            }
        });

        listeners.add(new Listener<MoveInput>() {
            @Override
            public void call(MoveInput event) {
                if (mc.thePlayer.isUsingItem() && !mc.thePlayer.isRiding()) {
                    event.getMovementInput().moveForward /= 0.2F;
                    event.getMovementInput().moveStrafe /= 0.2F;
                }
            }
        });
    }
}

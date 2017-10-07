package host.serenity.serenity.modules.minigames;

import host.serenity.serenity.api.module.Module;
import host.serenity.serenity.api.module.ModuleCategory;
import host.serenity.serenity.event.network.NetMovingUpdate;
import host.serenity.synapse.Listener;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;

public class AntiVoid extends Module {
    public AntiVoid() {
        super("Anti Void", 0xDEFDFF, ModuleCategory.MINIGAMES);

        listeners.add(new Listener<NetMovingUpdate>() {
            @Override
            public void call(NetMovingUpdate event) {
                if (mc.thePlayer.fallDistance > 5) {
                    if (mc.thePlayer.posY < 0) {
                        event.setY(event.getY() + 8);
                    } else {
                        for (int i = (int) Math.ceil(mc.thePlayer.posY); i >= 0; i--) {
                            if (mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX, i, mc.thePlayer.posZ)).getBlock() != Blocks.air) {
                                return;
                            }
                        }

                        event.setY(event.getY() + 8);
                    }
                }
            }
        });
    }
}

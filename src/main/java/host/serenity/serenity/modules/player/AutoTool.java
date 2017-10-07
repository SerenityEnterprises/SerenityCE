package host.serenity.serenity.modules.player;

import host.serenity.serenity.api.module.Module;
import host.serenity.serenity.api.module.ModuleCategory;
import host.serenity.serenity.api.module.mode.ModuleMode;
import host.serenity.serenity.api.value.Value;
import host.serenity.serenity.event.player.BlockDigging;
import host.serenity.synapse.Listener;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C09PacketHeldItemChange;

import java.util.List;

public class AutoTool extends Module {
    public AutoTool() {
        super("Auto Tool", 0x71FF88, ModuleCategory.PLAYER);

        getModuleModes().add(new ModuleMode("Simple") {
            @Override
            public void addListeners(List<Listener<?>> listeners) {
                listeners.add(new Listener<BlockDigging>() {
                    @Override
                    public void call(BlockDigging event) {
                        int bestSlot = getBestSlotMining(mc.theWorld.getBlockState(event.getPos()).getBlock());
                        if (bestSlot != -1) {
                            mc.thePlayer.inventory.currentItem = bestSlot - 36;
                            mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(bestSlot - 36));
                        }
                    }
                });
            }

            @Override
            public Value<?>[] getValues() {
                return new Value[0];
            }
        });

        setActiveMode("Simple");
    }

    private int getBestSlotMining(Block block) {
        int bestSlot = -1;
        float bestHardness = 1F;

        for (int index = 36; index < 45; index++) {
            ItemStack stack = mc.thePlayer.inventoryContainer.getSlot(index).getStack();
            if (stack != null) {
                float str = stack.getStrVsBlock(block);
                if (str > 1F) {
                    int efficiencyLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.efficiency.effectId, stack);
                    str += (efficiencyLevel * efficiencyLevel + 1);
                }

                if (str > bestHardness) {
                    bestHardness = str;
                    bestSlot = index;
                }
            }
        }

        return bestSlot;
    }
}

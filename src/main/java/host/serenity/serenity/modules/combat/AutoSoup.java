package host.serenity.serenity.modules.combat;

import host.serenity.serenity.api.help.ValueDescription;
import host.serenity.serenity.api.module.Module;
import host.serenity.serenity.api.module.ModuleCategory;
import host.serenity.serenity.api.value.DoubleValue;
import host.serenity.serenity.api.value.ModuleValue;
import host.serenity.serenity.event.player.PlayerWalkingUpdate;
import host.serenity.serenity.util.TimeHelper;
import host.serenity.synapse.Listener;
import net.minecraft.init.Items;
import net.minecraft.item.ItemSoup;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.EnumFacing;

public class AutoSoup extends Module {
    @ModuleValue
    @ValueDescription("Health at which to use a soup.")
    private DoubleValue health = new DoubleValue("Health", 10, 1, 20);

    private TimeHelper time = new TimeHelper();

    public AutoSoup() {
        super("Auto Soup", 0xFFB675, ModuleCategory.COMBAT);

        listeners.add(new Listener<PlayerWalkingUpdate>() {
            @Override
            public void call(PlayerWalkingUpdate event) {
                setDisplay("Auto Soup" + EnumChatFormatting.GRAY + String.format(" [%s]", countSoups()));

                if (mc.thePlayer.getHealth() / mc.thePlayer.getMaxHealth() * 20 < health.getValue()) {
                    if (time.hasReached(50)) {
                        if (hotbarHasSoups()) {
                            useSoup();
                        } else {
                            getSoupFromInventory();
                            time.reset();
                        }
                    }
                }
            }
        });
    }

    private boolean hotbarHasSoups() {
        for (int index = 36; index < 45; index++) {
            ItemStack itemStack = mc.thePlayer.inventoryContainer.getSlot(index).getStack();
            if (itemStack != null) {
                if (itemStack.getItem() instanceof ItemSoup) {
                    return true;
                }
            }
        }

        return false;
    }

    private int countSoups() {
        int items = 0;
        for (int index = 9; index < 45; index++) {
            ItemStack itemStack = mc.thePlayer.inventoryContainer.getSlot(index).getStack();
            if (itemStack != null) {
                if (itemStack.getItem() instanceof ItemSoup) {
                    items += itemStack.stackSize;
                }
            }
        }

        return items;
    }

    private void getSoupFromInventory() {
        int item = -1;
        boolean found = false;
        for (int index = 36; index >= 9; index--) {
            ItemStack itemStack = mc.thePlayer.inventoryContainer.getSlot(index).getStack();
            if (itemStack != null) {
                if (itemStack.getItem() instanceof ItemSoup) {
                    item = index;
                    found = true;
                    break;
                }
            }
        }
        if (found) {
            for (int index = 0; index < 45; index++) {
                ItemStack itemStack = mc.thePlayer.inventoryContainer.getSlot(index).getStack();
                if (itemStack != null) {
                    if ((itemStack.getItem() == Items.bowl) && (index >= 36) && (index <= 44)) {
                        mc.playerController.windowClick(0, index, 0, 0, mc.thePlayer);
                        mc.playerController.windowClick(0, -999, 0, 0, mc.thePlayer);
                    }
                    break;
                }
            }
            mc.playerController.windowClick(0, item, 0, 1, mc.thePlayer);
        }
    }

    private void useSoup() {
        int item = -1;
        boolean found = false;
        for (int index = 36; index < 45; index++) {
            ItemStack itemStack = mc.thePlayer.inventoryContainer.getSlot(index).getStack();
            if (itemStack != null) {
                if (itemStack.getItem() instanceof ItemSoup) {
                    item = index;
                    found = true;
                    break;
                }
            }
        }
        if (found) {
            for (int index = 0; index < 45; index++) {
                ItemStack itemStack = mc.thePlayer.inventoryContainer.getSlot(index).getStack();
                if (itemStack != null) {
                    if ((itemStack.getItem() == Items.bowl) && (index >= 36) && (index <= 44)) {
                        mc.playerController.windowClick(0, index, 0, 0, mc.thePlayer);
                        mc.playerController.windowClick(0, -999, 0, 0, mc.thePlayer);
                    }
                }
            }
            int slot = mc.thePlayer.inventory.currentItem;
            mc.thePlayer.inventory.currentItem = (item - 36);
            mc.playerController.updateController();
            mc.thePlayer.sendQueue.addToSendQueue(new C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getCurrentItem()));
            mc.thePlayer.sendQueue.addToSendQueue(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
            mc.thePlayer.sendQueue.addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
            mc.thePlayer.stopUsingItem();

            mc.thePlayer.inventory.currentItem = slot;
        }
    }
}

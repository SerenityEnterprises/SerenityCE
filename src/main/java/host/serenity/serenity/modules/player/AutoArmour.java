package host.serenity.serenity.modules.player;

import host.serenity.serenity.Serenity;
import host.serenity.serenity.api.help.ValueDescription;
import host.serenity.serenity.api.module.Module;
import host.serenity.serenity.api.module.ModuleCategory;
import host.serenity.serenity.api.value.IntValue;
import host.serenity.serenity.api.value.ModuleValue;
import host.serenity.serenity.event.player.PlayerWalkingUpdate;
import host.serenity.serenity.modules.world.ChestStealer;
import host.serenity.serenity.util.TimeHelper;
import host.serenity.synapse.Listener;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class AutoArmour extends Module {
    private TimeHelper time = new TimeHelper();

    private Item[] helmets = { Items.diamond_helmet, Items.iron_helmet, Items.chainmail_helmet, Items.golden_helmet, Items.leather_helmet };
    private Item[] chestplates = { Items.diamond_chestplate, Items.iron_chestplate, Items.chainmail_chestplate, Items.golden_chestplate, Items.leather_chestplate };
    private Item[] leggings = { Items.diamond_leggings, Items.iron_leggings, Items.chainmail_leggings, Items.golden_leggings, Items.leather_leggings };
    private Item[] boots = { Items.diamond_boots, Items.iron_boots, Items.chainmail_boots, Items.golden_boots, Items.leather_boots };

    @ModuleValue
    @ValueDescription("The delay between inventory clicks.")
    private IntValue delay = new IntValue("Delay", 100);

    public AutoArmour() {
        super("Auto Armour", 0x7FD6FF, ModuleCategory.PLAYER);

        listeners.add(new Listener<PlayerWalkingUpdate>() {
            @Override
            public void call(PlayerWalkingUpdate event) {
                if (!time.hasReached(delay.getValue()) || mc.thePlayer.openContainer != mc.thePlayer.inventoryContainer
                        || Serenity.getInstance().getModuleManager().getModule(ChestStealer.class).isStealing())
                    return;

                boolean hasInvSpace = false;
                for (ItemStack stack : mc.thePlayer.inventory.mainInventory) {
                    if (stack == null) {
                        hasInvSpace = true;
                        break;
                    }
                }

                Item[][] armour = new Item[][] { chestplates, leggings, helmets, boots };
                int[] slots = new int[] { 2, 1, 3, 0 };

                assert slots.length == armour.length;

                for (int i = 0; i < armour.length; i++) {
                    boolean better = armourIsBetter(slots[i], armour[i]);
                    if (mc.thePlayer.inventory.armorInventory[slots[i]] == null || better) {
                        for (Item item : armour[i]) {
                            int slot = findItem(Item.getIdFromItem(item));
                            if (slot != -1) {
                                time.reset();

                                if (better) {
                                    mc.playerController.windowClick(0, armour.length - slots[i] + 4, 0, hasInvSpace ? 1 : 4, mc.thePlayer);
                                    return;
                                }

                                mc.playerController.windowClick(0, slot, 0, 1, mc.thePlayer);
                                return;
                            }
                        }
                    }
                }

            }
        });
    }

    private int findItem(int id) {
        for (int index = 9; index < 45; index++) {
            final ItemStack item = mc.thePlayer.inventoryContainer.getSlot(index).getStack();

            if (item != null && Item.getIdFromItem(item.getItem()) == id)
                return index;
        }

        return -1;
    }

    public boolean armourIsBetter(int slot, Item[] armourtype) {
        if (mc.thePlayer.inventory.armorInventory[slot] != null) {
            int currentIndex = 0;
            int finalCurrentIndex = -1;
            int invIndex = 0;
            int finalInvIndex = -1;
            for (Item armour : armourtype) {
                if (mc.thePlayer.inventory.armorInventory[slot].getItem() == armour) {
                    finalCurrentIndex = currentIndex;
                    break;
                }
                currentIndex++;
            }

            for (Item armour : armourtype) {
                if (findItem(Item.getIdFromItem(armour)) != -1) {
                    finalInvIndex = invIndex;
                    break;
                }
                invIndex++;
            }

            if (finalInvIndex > -1) {
                return finalInvIndex < finalCurrentIndex;
            }
        }
        return false;
    }
}

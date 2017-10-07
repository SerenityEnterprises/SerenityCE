package host.serenity.serenity.modules.combat;

import host.serenity.serenity.Serenity;
import host.serenity.serenity.api.help.ValueDescription;
import host.serenity.serenity.api.module.Module;
import host.serenity.serenity.api.module.ModuleCategory;
import host.serenity.serenity.api.value.BooleanValue;
import host.serenity.serenity.api.value.ModuleValue;
import host.serenity.serenity.event.network.SendPacket;
import host.serenity.synapse.Listener;
import net.minecraft.entity.Entity;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.C02PacketUseEntity;

/**
 * Created by Jordin on 2/22/2017.
 * Jordin is still best hacker.
 */
public class FriendProtect extends Module {
    @ModuleValue
    @ValueDescription("Notifies you when you attempt to attack a protected entity.")
    public BooleanValue notify = new BooleanValue("Notify", true);

    @ModuleValue
    @ValueDescription("Only prevents you from attacking with swords.")
    public BooleanValue swordsOnly = new BooleanValue("Swords Only", false);

    @ModuleValue
    @ValueDescription("Allows you to heal on MineZ with friend protect on.")
    public BooleanValue minezHealing = new BooleanValue("MineZ Healing", true);

    public FriendProtect() {
        super("Friend Protect", 0xFFF6194A, ModuleCategory.COMBAT);
        setHidden(true);

        listeners.add(new Listener<SendPacket>() {
            @Override
            public void call(SendPacket event) {
                if (event.getPacket() instanceof C02PacketUseEntity) {
                    C02PacketUseEntity useEntity = (C02PacketUseEntity) event.getPacket();
                    Entity entity = useEntity.getEntityFromWorld(mc.theWorld);

                    if (useEntity.getAction() == C02PacketUseEntity.Action.ATTACK && Serenity.getInstance().getFriendManager().isFriend(entity.getCommandSenderName())) {
                        ItemStack currentItem = mc.thePlayer.getCurrentEquippedItem();
                        Item item = currentItem == null ? null : currentItem.getItem();

                        if (minezHealing.getValue()) {
                            if (item != null && (item == Items.paper || (item == Items.dye) || item == Items.shears)) {
                                return;
                            }
                        }

                        if (swordsOnly.getValue() && (item instanceof ItemSword)) {
                            return;
                        }

                        if (notify.getValue()) {
                            Serenity.getInstance().addChatMessage(String.format("%s is your friend.", entity.getCommandSenderName()));
                        }
                        event.setCancelled(true);
                    }
                }
            }
        });
        setState(true);
    }
}

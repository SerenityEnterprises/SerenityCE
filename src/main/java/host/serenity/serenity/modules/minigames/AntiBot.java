package host.serenity.serenity.modules.minigames;

import host.serenity.serenity.api.module.Module;
import host.serenity.serenity.api.module.ModuleCategory;
import host.serenity.serenity.api.value.BooleanValue;
import host.serenity.serenity.api.value.ModuleValue;
import host.serenity.serenity.event.player.PostPlayerWalkingUpdate;
import host.serenity.synapse.Listener;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.StringUtils;

import java.util.ArrayList;

public class AntiBot extends Module {
    @ModuleValue
    private BooleanValue remover = new BooleanValue("Remover", false);

    public AntiBot() {
        super("Anti Bot", 0x89ACFF, ModuleCategory.MINIGAMES);

        listeners.add(new Listener<PostPlayerWalkingUpdate>(remover::getValue) {
            @Override
            public void call(PostPlayerWalkingUpdate event) {
                for (EntityPlayer player : new ArrayList<EntityPlayer>(mc.theWorld.playerEntities)) {
                    if (isBot(player))
                        mc.theWorld.removeEntity(player);
                }
            }
        });

        setHidden(true);
    }

    public static boolean isBot(EntityPlayer entity) {
        if (entity.getUniqueID().toString().startsWith(entity.getCommandSenderName()))
            return true;
        if (!StringUtils.stripControlCodes(entity.getGameProfile().getName()).equals(entity.getCommandSenderName()))
            return true;
        if (entity.getGameProfile().getId() != entity.getUniqueID())
            return true;

        return false;
    }
}

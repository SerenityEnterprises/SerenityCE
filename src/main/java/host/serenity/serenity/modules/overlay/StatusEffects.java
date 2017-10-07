package host.serenity.serenity.modules.overlay;

import host.serenity.serenity.api.module.Module;
import host.serenity.serenity.api.module.ModuleCategory;
import host.serenity.serenity.util.overlay.OverlayArea;
import host.serenity.serenity.util.overlay.OverlayContextManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

public class StatusEffects extends Module {

    public StatusEffects() {
        super("Status Effects", 0xAEDEFF, ModuleCategory.OVERLAY);
        setHidden(true);

        OverlayContextManager.INSTANCE.register(OverlayArea.BOTTOM_RIGHT, 1000, ctx -> {
            for (final Potion potion : Potion.potionTypes) {
                if (potion == null)
                    continue;

                final PotionEffect effect = mc.thePlayer.getActivePotionEffect(potion);
                if (effect == null)
                    continue;

                StringBuilder builder = new StringBuilder(I18n.format(potion.getName(), new Object[0]));
                builder.append(" (");
                if (effect.getAmplifier() > 0) {
                    builder.append(effect.getAmplifier() + 1);
                    builder.append(" : ");
                }
                builder.append(Potion.getDurationString(effect));
                builder.append(")");
                ctx.drawString(builder.toString(), potion.getLiquidColor(), true);
            }
        }, this::isEnabled, () -> !mc.gameSettings.showDebugInfo);
    }
}

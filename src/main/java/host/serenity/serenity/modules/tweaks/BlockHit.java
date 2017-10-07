package host.serenity.serenity.modules.tweaks;

import host.serenity.serenity.api.module.Module;
import host.serenity.serenity.api.module.ModuleCategory;

public class BlockHit extends Module {
    public BlockHit() {
        super("Block Hit", 0xFF6E8B, ModuleCategory.TWEAKS);
        setHidden(true);

        // See: MixinItemRenderer
    }
}

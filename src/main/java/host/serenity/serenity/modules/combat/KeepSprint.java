package host.serenity.serenity.modules.combat;

import host.serenity.serenity.api.module.Module;
import host.serenity.serenity.api.module.ModuleCategory;

public class KeepSprint extends Module {
    public KeepSprint() {
        super("Keep Sprint", 0xFCFF74, ModuleCategory.COMBAT);
        setHidden(true);

        // See: MixinEntityPlayer, MixinEntityPlayerSP
    }
}

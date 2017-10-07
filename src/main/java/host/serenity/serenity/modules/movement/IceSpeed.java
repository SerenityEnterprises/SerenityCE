package host.serenity.serenity.modules.movement;

import host.serenity.serenity.api.module.Module;
import host.serenity.serenity.api.module.ModuleCategory;
import net.minecraft.init.Blocks;

public class IceSpeed extends Module {
    public IceSpeed() {
        super("Ice Speed", 0xB1E7FF, ModuleCategory.MOVEMENT);
        setHidden(true);

    }

    @Override
    protected void onEnable() {
        Blocks.ice.slipperiness = 0.4F;
        Blocks.packed_ice.slipperiness = 0.4F;
    }

    @Override
    protected void onDisable() {
        Blocks.ice.slipperiness = 0.98F;
        Blocks.packed_ice.slipperiness = 0.98F;
    }
}

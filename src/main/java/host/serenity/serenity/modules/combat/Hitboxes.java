package host.serenity.serenity.modules.combat;

import host.serenity.serenity.api.help.ValueDescription;
import host.serenity.serenity.api.module.Module;
import host.serenity.serenity.api.module.ModuleCategory;
import host.serenity.serenity.api.value.DoubleValue;
import host.serenity.serenity.api.value.ModuleValue;
import host.serenity.serenity.event.internal.HitboxSize;
import host.serenity.synapse.Listener;

public class Hitboxes extends Module {
    @ModuleValue
    @ValueDescription("The size to expand hitboxes by.")
    private DoubleValue size = new DoubleValue("Size", 0.3D);

    public Hitboxes() {
        super("Hitboxes", 0xFF90CA, ModuleCategory.COMBAT);

        listeners.add(new Listener<HitboxSize>() {
            @Override
            public void call(HitboxSize event) {
                event.setSize(size.getValue().floatValue());
            }
        });
    }
}

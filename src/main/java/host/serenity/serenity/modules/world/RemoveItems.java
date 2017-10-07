package host.serenity.serenity.modules.world;

import host.serenity.serenity.api.module.Module;
import host.serenity.serenity.api.module.ModuleCategory;
import host.serenity.serenity.event.core.RunTick;
import host.serenity.synapse.Listener;
import net.minecraft.entity.item.EntityItem;

import java.util.LinkedList;
import java.util.List;

public class RemoveItems extends Module {
    public RemoveItems() {
        super("Remove Items", 0xB8F8FF, ModuleCategory.WORLD);

        listeners.add(new Listener<RunTick>() {
            @Override
            public void call(RunTick event) {
                if (mc.theWorld != null) {

                    List<EntityItem> items = new LinkedList<>();

                    for (Object o : mc.theWorld.loadedEntityList) {
                        if (o instanceof EntityItem) {
                            items.add((EntityItem) o);
                        }
                    }

                    items.forEach(mc.theWorld::removeEntity);
                }
            }
        });
    }
}

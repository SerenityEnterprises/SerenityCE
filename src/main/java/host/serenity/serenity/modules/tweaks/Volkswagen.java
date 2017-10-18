package host.serenity.serenity.modules.tweaks;

import host.serenity.serenity.api.module.Module;
import host.serenity.serenity.api.module.ModuleCategory;
import host.serenity.serenity.event.core.BlockBB;
import host.serenity.synapse.Listener;
import me.jordin.deltoid.region.CuboidRegion;
import me.jordin.deltoid.vector.Vec3;
import net.minecraft.block.BlockCarpet;
import net.minecraft.client.multiplayer.ServerData;

public class Volkswagen extends Module {
    public Volkswagen() {
        super("Volkswagen", 0xFFFFFF, ModuleCategory.TWEAKS);
        setHidden(true);

        listeners.add(new Listener<BlockBB>() {
            @Override
            public void call(BlockBB event) {
                String serverIP = getServerIP();

                if (serverIP == null) {
                    return;
                }

                if (serverIP.toLowerCase().contains("ihaq.ca")) {
                    if (new CuboidRegion(new Vec3(835, 5, 154), new Vec3(825, 5, 128)).contains(new Vec3(event.getX(), event.getY(), event.getZ()))) {
                        if (event.getBlock() instanceof BlockCarpet) {
                            event.setBoundingBox(null);
                        }
                    }
                }
            }
        });


        setState(true);
    }

    private String getServerIP() {
        ServerData serverData = mc.getCurrentServerData();

        if (serverData == null) {
            return null;
        }

        return serverData.serverIP;
    }

    @Override
    public void setState(boolean state) {
        if (state)
            super.setState(true);
    }
}

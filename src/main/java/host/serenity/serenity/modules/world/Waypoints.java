package host.serenity.serenity.modules.world;

import com.google.common.collect.ImmutableList;
import host.serenity.serenity.Serenity;
import host.serenity.serenity.api.command.Command;
import host.serenity.serenity.api.command.parser.CommandBranch;
import host.serenity.serenity.api.command.parser.argument.impl.DoubleArgument;
import host.serenity.serenity.api.command.parser.argument.impl.StringArgument;
import host.serenity.serenity.api.module.Module;
import host.serenity.serenity.api.module.ModuleCategory;
import host.serenity.serenity.event.network.ReceivePacket;
import host.serenity.serenity.event.render.RenderWorld;
import host.serenity.serenity.modules.world.util.waypoints.Waypoint;
import host.serenity.serenity.modules.world.util.waypoints.WaypointList;
import host.serenity.serenity.util.RenderUtilities;
import host.serenity.serenity.util.file.JSONConfiguration;
import host.serenity.synapse.EventManager;
import host.serenity.synapse.Listener;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.network.play.server.S01PacketJoinGame;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class Waypoints extends Module {
    private static final String ADD_WAYPOINT = "Added \"%s\" waypoint at: %.1f %.1f %.1f";
    private static final String REMOVE_WAYPOINT = "Removed \"%s\" waypoint at: %.1f %.1f %.1f";
    private static final String LIST_WAYPOINT = "\"%s\" is located at: %.1f %.1f %.1f";
    private static final String CLEAR_WAYPOINTS = "Removed all %d waypoint(s).";

    private List<Waypoint> waypoints;
    private JSONConfiguration config;

    public Waypoints() {
        super("Waypoints", 0x66F3FF, ModuleCategory.WORLD);
        setHidden(true);

        loadCommand();

        EventManager.register(new Listener<ReceivePacket>() {
            @Override
            public void call(ReceivePacket event) {
                if (event.getPacket() instanceof S01PacketJoinGame) {
                    setupNew();
                }
            }
        });

        listeners.add(new Listener<RenderWorld>() {
            @Override
            public void call(RenderWorld event) {
                for (Waypoint waypoint : waypoints) {
                    Vec3 pos = new Vec3(waypoint.getX(), waypoint.getY(), waypoint.getZ());
                    if (mc.thePlayer.getDistanceSq(pos.xCoord, pos.yCoord, pos.zCoord) > 50 * 50) {
                        Vec3 delta = pos.subtract(new Vec3(mc.getRenderManager().viewerPosX, mc.getRenderManager().viewerPosY, mc.getRenderManager().viewerPosZ));
                        delta = delta.normalize();

                        pos = new Vec3(mc.getRenderManager().viewerPosX + delta.xCoord * 50, mc.getRenderManager().viewerPosY + delta.yCoord * 50, mc.getRenderManager().viewerPosZ + delta.zCoord * 50);
                    }
                    double scale = mc.thePlayer.getDistance(pos.xCoord, pos.yCoord, pos.zCoord) / 4;
                    if (scale < 1.6F) {
                        scale = 1.6F;
                    }

                    scale /= 100;

                    RenderManager renderManager = mc.getRenderManager();
                    GL11.glPushMatrix();
                    GL11.glTranslated(-mc.getRenderManager().viewerPosX, -mc.getRenderManager().viewerPosY, -mc.getRenderManager().viewerPosZ);

                    GL11.glTranslatef((float) pos.xCoord + .5F, (float) pos.yCoord + .5F, (float) pos.zCoord + .5F);
                    GL11.glNormal3f(0.0F, 1.0F, 0.0F);
                    GL11.glRotatef(-renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
                    GL11.glRotatef(renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
                    GL11.glScaled(-scale, -scale, scale);
                    GL11.glDisable(GL11.GL_LIGHTING);
                    GL11.glDisable(GL11.GL_DEPTH_TEST);
                    GL11.glEnable(GL11.GL_BLEND);
                    GL11.glBlendFunc(770, 771);

                    String text = String.format("%s (%.1fm)", waypoint.getName(), mc.thePlayer.getDistance(waypoint.getX(), waypoint.getY(), waypoint.getZ()));
                    int width = mc.fontRendererObj.getStringWidth(text) / 2;
                    RenderUtilities.drawBorderedRect(-width - 2, -(mc.fontRendererObj.FONT_HEIGHT + 1), width + 2, 2, 1F, 0x00000000, 0xAF060606);
                    mc.fontRendererObj.drawStringWithShadow(text, -width, -(mc.fontRendererObj.FONT_HEIGHT - 1), 0xFFFFFF);

                    GL11.glDisable(GL11.GL_BLEND);
                    GL11.glEnable(GL11.GL_LIGHTING);
                    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                    GL11.glPopMatrix();
                }
            }
        });
    }

    public void loadCommand() {
        Serenity.getInstance().getCommandManager().getCommands().add(new Command("wp") {
            {

                branches.add(new CommandBranch("add", data -> {
                    setState(true);
                    String name = data.getArgumentValue("name", String.class);
                    Waypoint waypoint = new Waypoint(name, mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
                    addWaypoint(waypoint);
                }, new StringArgument("name")));

                branches.add(new CommandBranch("add", data -> {
                    setState(true);
                    String name = data.getArgumentValue("name", String.class);
                    Waypoint waypoint = new Waypoint(name, data.getArgumentValue("x", Double.class), data.getArgumentValue("y", Double.class), data.getArgumentValue("z", Double.class));
                    addWaypoint(waypoint);
                }, new StringArgument("name"), new DoubleArgument("x"), new DoubleArgument("y"), new DoubleArgument("z")));

                branches.add(new CommandBranch("chunk", data -> {
                    setState(true);
                    String name = data.getArgumentValue("name", String.class);
                    Waypoint waypoint = new Waypoint(name, data.getArgumentValue("chunkX", Double.class) * 16, 70, data.getArgumentValue("chunkZ", Double.class) * 16);
                    addWaypoint(waypoint);
                }, new StringArgument("name"), new DoubleArgument("chunkX"), new DoubleArgument("chunkZ")));

                branches.add(new CommandBranch("del", data -> {
                    setState(true);
                    String name = data.getArgumentValue("name", String.class);
                    ImmutableList.copyOf(waypoints).stream().filter(wp -> wp.getName().equalsIgnoreCase(name)).forEach(wp -> {
                        waypoints.remove(wp);
                        out(REMOVE_WAYPOINT, wp.getName(), wp.getX(), wp.getY(), wp.getZ());
                    });
                    save();
                }, new StringArgument("name")));

                branches.add(new CommandBranch("list", data -> {
                    setState(true);
                    if (waypoints.isEmpty()) {
                        out("You do not have any waypoints.");
                    } else {
                        waypoints.forEach(waypoint -> out(LIST_WAYPOINT, waypoint.getName(), waypoint.getX(), waypoint.getY(), waypoint.getZ()));
                    }
                }));

                branches.add(new CommandBranch("clear", data -> {
                    setState(true);
                    if (waypoints.isEmpty()) {
                        out("You do not have any waypoints.");
                    } else {
                        out(CLEAR_WAYPOINTS, waypoints.size());
                        waypoints.clear();
                        save();
                    }
                }));
            }
        });

        setupNew();
    }

    private void setupNew() {
        ServerData sd = mc.getCurrentServerData();
        if (sd != null) {
            this.config = new JSONConfiguration(String.format("/waypoints/%s.json", sd.serverIP.replace(":", "_")));
            this.load();
        } else {
            waypoints = new ArrayList<>();
        }
    }

    private void addWaypoint(Waypoint waypoint) {
        this.waypoints.add(waypoint);
        save();
        Serenity.getInstance().addChatMessage(String.format(ADD_WAYPOINT, waypoint.getName(), waypoint.getX(), waypoint.getY(), waypoint.getZ()));
    }

    public void load() {
        WaypointList waypointList = this.config.load(WaypointList.class);
        try {
            this.waypoints = waypointList.getWaypoints();
        } catch (NullPointerException ignored) {
            this.waypoints = new ArrayList<>();
        }
    }

    public void save() {
        if (this.waypoints.isEmpty()) {
            this.config.delete();
        } else {
            this.config.save(new WaypointList(this.waypoints));
        }
    }
}

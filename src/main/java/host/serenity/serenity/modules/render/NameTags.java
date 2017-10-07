package host.serenity.serenity.modules.render;

import host.serenity.serenity.Serenity;
import host.serenity.serenity.api.module.Module;
import host.serenity.serenity.api.module.ModuleCategory;
import host.serenity.serenity.api.value.BooleanValue;
import host.serenity.serenity.api.value.EnumValue;
import host.serenity.serenity.api.value.ModuleValue;
import host.serenity.serenity.event.render.RenderWorld;
import host.serenity.serenity.util.BotDetector;
import host.serenity.serenity.util.ColourUtilities;
import host.serenity.serenity.util.RenderUtilities;
import host.serenity.synapse.Listener;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class NameTags extends Module {
    private enum HealthMode {
        HEARTS("Hearts"), HP("HP"), NONE("None");

        private String display;

        HealthMode(String display) {
            this.display = display;
        }

        @Override
        public String toString() {
            return display;
        }
    }

    @ModuleValue
    private EnumValue<HealthMode> healthMode = new EnumValue<>("Health Mode", HealthMode.HP);

    @ModuleValue
    private BooleanValue formatting = new BooleanValue("Formatting", false);

    private BotDetector botDetector = new BotDetector();

    public NameTags() {
        super("Name Tags", 0x66F5FF, ModuleCategory.RENDER);
        setHidden(true);

        listeners.add(new Listener<RenderWorld>() {
            @Override
            public void call(RenderWorld event) {
                List<EntityPlayer> players = new ArrayList<>();

                for (EntityPlayer player : (List<EntityPlayer>) mc.theWorld.playerEntities) {
                    if (player != mc.getRenderViewEntity() && player.isEntityAlive() && !players.contains(player)) {
                        players.add(player);
                    }
                }

                // Sort by reverse distance (render close after far.)
                players.sort((p1, p2) -> Double.compare(p2.getDistanceSqToEntity(mc.getRenderViewEntity()), p1.getDistanceSqToEntity(mc.getRenderViewEntity())));

                GL11.glPushMatrix();
                GL11.glTranslated(-mc.getRenderManager().viewerPosX, -mc.getRenderManager().viewerPosY, -mc.getRenderManager().viewerPosZ);

                for (EntityPlayer player : players) {
                    String tag = formatting.getValue() ? player.getDisplayName().getFormattedText() : player.getCommandSenderName();
                    try {
                        tag = Serenity.getInstance().getFriendManager().applyProtection(tag);
                    } catch (Exception ignored) {}

                    double posX = player.lastTickPosX + (player.posX - player.lastTickPosX) * event.getRenderPartialTicks();
                    double posY = player.lastTickPosY + (player.posY - player.lastTickPosY) * event.getRenderPartialTicks();
                    double posZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * event.getRenderPartialTicks();

                    posY += player.isSneaking() ? 0.5 : 0.7;

                    double scale = Math.max(1.6, mc.getRenderViewEntity().getDistanceToEntity(player) / 4);

                    int colour = 0xFFFFFF;
                    if (Serenity.getInstance().getFriendManager().isFriend(player.getCommandSenderName())) {
                        colour = 0x53BEF6;
                    } else if (player.isInvisible()) {
                        colour = 0xFFF26A;
                    } else if (player.isSneaking()) {
                        colour = 0x9E1535;
                    }

                    double hp = player.getHealth() + player.getAbsorptionAmount();
                    double health = Math.ceil(hp) / 2;
                    hp = Math.ceil(hp * 10) / 10; // Round up to nearest .1

                    String healthStr = null;

                    switch (healthMode.getValue()) {
                        case HEARTS:
                            if (Math.floor(health) == health) {
                                healthStr = String.valueOf((int) Math.floor(health));
                            } else {
                                healthStr = String.valueOf(health);
                            }
                            break;
                        case HP:
                            if (Math.floor(hp) == hp) {
                                healthStr = String.valueOf((int) Math.floor(hp));
                            } else {
                                healthStr = String.valueOf(hp);
                            }
                            break;
                    }

                    int healthCol = getColorByHealth(player.getMaxHealth(), player.getHealth());

                    scale /= 100;
                    GL11.glPushMatrix();
                    GL11.glTranslated(posX, posY + 1.4, posZ);
                    GL11.glNormal3i(0, 1, 0);
                    GL11.glRotatef(-mc.getRenderManager().playerViewY, 0, 1, 0);
                    GL11.glRotatef(mc.getRenderManager().playerViewX, 1, 0, 0);
                    GL11.glScaled(-scale, -scale, scale);
                    GL11.glDisable(GL11.GL_LIGHTING);
                    GL11.glDisable(GL11.GL_DEPTH_TEST);

                    int width;
                    if (healthStr != null) {
                        width = mc.fontRendererObj.getStringWidth(tag + " " + healthStr) / 2;
                    } else {
                        width = mc.fontRendererObj.getStringWidth(tag) / 2;
                    }

                    GL11.glEnable(GL11.GL_BLEND);
                    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                    RenderUtilities.drawRect(-width - 2, -(mc.fontRendererObj.FONT_HEIGHT + 1), width + 2, 2, 0x9F0A0A0A);
                    mc.fontRendererObj.drawStringWithShadow(tag, -width, -(mc.fontRendererObj.FONT_HEIGHT - 1), colour);
                    if (healthStr != null)
                        mc.fontRendererObj.drawStringWithShadow(healthStr, -width + mc.fontRendererObj.getStringWidth(tag + " "), -(mc.fontRendererObj.FONT_HEIGHT - 1), healthCol);

                    GL11.glEnable(GL11.GL_DEPTH_TEST);
                    GL11.glDisable(GL11.GL_BLEND);
                    GL11.glEnable(GL11.GL_LIGHTING);
                    GL11.glColor4f(1, 1, 1, 1);
                    GL11.glPopMatrix();
                }

                GL11.glPopMatrix();
            }
        });
    }

    private int getColorByHealth(float maxHealth, float health) {
        Color green = new Color(72, 255, 94);
        Color yellow = new Color(255, 250, 57);
        Color red = new Color(255, 35, 40);

        float middleHealth = maxHealth / 2;

        if (health <= middleHealth) {
            return ColourUtilities.blend(yellow, red, (health / middleHealth)).getRGB();
        } else if (health <= (middleHealth * 2)) {
            return ColourUtilities.blend(green, yellow, ((health - middleHealth) / middleHealth)).getRGB();
        }
        return green.getRGB();
    }
}

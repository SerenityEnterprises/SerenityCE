package host.serenity.serenity.modules.render;

import host.serenity.serenity.api.help.ValueDescription;
import host.serenity.serenity.api.module.Module;
import host.serenity.serenity.api.module.ModuleCategory;
import host.serenity.serenity.api.value.BooleanValue;
import host.serenity.serenity.api.value.ModuleValue;
import host.serenity.serenity.event.render.RenderWorld;
import host.serenity.serenity.util.RenderUtilities;
import host.serenity.serenity.util.math.Vector3;
import host.serenity.serenity.util.projectile.ProjectileTrajectoryData;
import host.serenity.serenity.util.projectile.ProjectileType;
import host.serenity.synapse.Listener;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;

// TODO: Code deduplication
public class Trajectories extends Module {
    @ModuleValue
    @ValueDescription("Show the predicted trajectory from throwable / shootable items in the hand.")
    public BooleanValue fromHand = new BooleanValue("From Hand", true);

    @ModuleValue
    @ValueDescription("Show the predicted trajectory of projectiles already in flight.")
    public BooleanValue inFlight = new BooleanValue("In Flight", true);

    public Trajectories() {
        super("Trajectories", 0x58FF78, ModuleCategory.RENDER);
        setHidden(true);

        listeners.add(new Listener<RenderWorld>(fromHand::getValue) {
            @Override
            public void call(RenderWorld event) {
                ItemStack stack = mc.thePlayer.getHeldItem();
                if (stack == null || !isItemShootable(stack))
                    return;

                ProjectileType projectileType = ProjectileType.getProjectileTypeFromShootableItem(stack);

                assert projectileType != null;

                double posX = mc.getRenderManager().viewerPosX - Math.cos(Math.toRadians(mc.thePlayer.rotationYaw)) * 0.16;
                double posY = mc.getRenderManager().viewerPosY + mc.thePlayer.getEyeHeight() - 0.1;
                double posZ = mc.getRenderManager().viewerPosZ - Math.sin(Math.toRadians(mc.thePlayer.rotationYaw)) * 0.16;

                double motionX = Math.sin(Math.toRadians(mc.thePlayer.rotationYaw)) * Math.cos(Math.toRadians(mc.thePlayer.rotationPitch))
                        * (projectileType == ProjectileType.ARROW ? -1 : -0.4);
                double motionY = Math.sin(Math.toRadians(mc.thePlayer.rotationPitch - (projectileType == ProjectileType.POTION ? 20 : 0)))
                        * (projectileType == ProjectileType.ARROW ? -1 : -0.4);
                double motionZ = Math.cos(Math.toRadians(mc.thePlayer.rotationYaw)) * Math.cos(Math.toRadians(mc.thePlayer.rotationPitch))
                        * (projectileType == ProjectileType.ARROW ? 1 : 0.4);

                double power = (72000 - mc.thePlayer.getItemInUseCount()) / 20D;
                power = (power * power + power * 2) / 3D;

                if (power < 0.1)
                    return;

                power = Math.min(power, 1);

                double speed = Math.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);
                motionX /= speed;
                motionY /= speed;
                motionZ /= speed;

                motionX *= (projectileType == ProjectileType.ARROW ? power * 2 : 1) * (projectileType == ProjectileType.POTION ? 0.5 : 1.5);
                motionY *= (projectileType == ProjectileType.ARROW ? power * 2 : 1) * (projectileType == ProjectileType.POTION ? 0.5 : 1.5);
                motionZ *= (projectileType == ProjectileType.ARROW ? power * 2 : 1) * (projectileType == ProjectileType.POTION ? 0.5 : 1.5);

                ProjectileTrajectoryData trajectoryData = getProjectileTrajectory(projectileType,
                        new Vector3(posX, posY, posZ),
                        new Vector3(motionX, motionY, motionZ));

                Color renderColour = new Color(255, 255, 255);
                switch (projectileType) {
                    case ARROW:
                        renderColour = new Color(216, 32, 32);
                        break;
                    case ENDER_PEARL:
                        renderColour = new Color(21, 120, 10);
                        break;
                    case POTION:
                        renderColour = new Color(Items.potionitem.getColorFromDamage(stack.getItemDamage()));
                }

                GL11.glPushMatrix();

                GL11.glDisable(GL11.GL_TEXTURE_2D);
                GL11.glDisable(GL11.GL_LIGHTING);
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                GL11.glDisable(GL11.GL_DEPTH_TEST);
                GL11.glEnable(GL11.GL_LINE_SMOOTH);
                GL11.glDepthMask(false);
                GL11.glLineWidth(1.0F);

                GL11.glColor3f(renderColour.getRed() / 255F, renderColour.getGreen() / 255F, renderColour.getBlue() / 255F);

                GL11.glTranslated(-mc.getRenderManager().viewerPosX, -mc.getRenderManager().viewerPosY, -mc.getRenderManager().viewerPosZ);

                GL11.glBegin(GL11.GL_LINE_STRIP);
                for (Vector3 position : trajectoryData.getTrajectory()) {
                    GL11.glVertex3d(position.x, position.y, position.z);
                }

                GL11.glEnd();

                if (trajectoryData.getLandingData() != null) {
                    MovingObjectPosition landingPosition = trajectoryData.getLandingData();

                    if (landingPosition.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                        GL11.glTranslated(landingPosition.hitVec.xCoord, landingPosition.hitVec.yCoord, landingPosition.hitVec.zCoord);
                        int sideIndex = landingPosition.sideHit.getIndex();
                        if (sideIndex == 1) {
                            GL11.glRotatef(180.0F, 1.0F, 0.0F, 0.0F);
                        } else if (sideIndex == 2) {
                            GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
                        } else if (sideIndex == 3) {
                            GL11.glRotatef(-90.0F, 1.0F, 0.0F, 0.0F);
                        } else if (sideIndex == 4) {
                            GL11.glRotatef(-90.0F, 0.0F, 0.0F, 1.0F);
                        } else if (sideIndex == 5) {
                            GL11.glRotatef(90.0F, 0.0F, 0.0F, 1.0F);
                        }
                        GL11.glRotatef(-90.0F, 1.0F, 0.0F, 0.0F);

                        Color colourWithAlpha = new Color(renderColour.getRed(), renderColour.getGreen(), renderColour.getBlue(), 175);
                        RenderUtilities.drawBorderedRect(-0.4F, -0.4F, 0.4F, 0.4F, 1.0F, renderColour.getRGB(), colourWithAlpha.getRGB());
                        GL11.glTranslated(-landingPosition.hitVec.xCoord, -landingPosition.hitVec.yCoord, -landingPosition.hitVec.zCoord);
                    }

                    if (landingPosition.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
                        GL11.glColor4f(renderColour.getRed() / 255F, renderColour.getGreen() / 255F, renderColour.getBlue() / 255F, 0.17F);
                        RenderUtilities.drawBoundingBox(landingPosition.entityHit.getEntityBoundingBox().expand(0.125, 0, 0.125).offset(0, 0.1, 0));

                        GL11.glColor3f(renderColour.getRed() / 255F, renderColour.getGreen() / 255F, renderColour.getBlue() / 255F);
                        GL11.glLineWidth(0.5F);
                        RenderUtilities.drawOutlinedBoundingBox(landingPosition.entityHit.getEntityBoundingBox().expand(0.125, 0, 0.125).offset(0, 0.1, 0));
                    }
                }

                GL11.glDepthMask(true);
                GL11.glDisable(GL11.GL_LINE_SMOOTH);
                GL11.glEnable(GL11.GL_DEPTH_TEST);
                GL11.glEnable(GL11.GL_LIGHTING);
                GL11.glDisable(GL11.GL_BLEND);
                GL11.glEnable(GL11.GL_TEXTURE_2D);

                GL11.glPopMatrix();
            }
        });

        listeners.add(new Listener<RenderWorld>(inFlight::getValue) {
            @Override
            public void call(RenderWorld event) {
                for (Object o : mc.theWorld.loadedEntityList) {
                    if (o instanceof IProjectile) {
                        Entity projectile = (Entity) o;
                        if (projectile instanceof EntityArrow) {
                            // jesus this is hackery
                            NBTTagCompound tempCompound = new NBTTagCompound();
                            projectile.writeToNBT(tempCompound);
                            if (tempCompound.getByte("inGround") == 1) {
                                continue;
                            }
                        }

                        ProjectileType projectileType = ProjectileType.getProjectTypeFromProjectileEntity(projectile);

                        if (projectileType == null)
                            return;

                        ProjectileTrajectoryData trajectoryData = getProjectileTrajectory(projectileType,
                                new Vector3(projectile.posX, projectile.posY, projectile.posZ),
                                new Vector3(projectile.motionX, projectile.motionY, projectile.motionZ));

                        Color renderColour = new Color(255, 255, 255);
                        switch (projectileType) {
                            case ARROW:
                                renderColour = new Color(216, 32, 32);
                                break;
                            case ENDER_PEARL:
                                renderColour = new Color(21, 120, 10);
                                break;
                            case POTION:
                                assert projectile instanceof EntityPotion;

                                EntityPotion entityPotion = (EntityPotion) projectile;
                                renderColour = new Color(Items.potionitem.getColorFromDamage(entityPotion.getPotionDamage()));
                        }

                        GL11.glPushMatrix();

                        GL11.glDisable(GL11.GL_TEXTURE_2D);
                        GL11.glDisable(GL11.GL_LIGHTING);
                        GL11.glEnable(GL11.GL_BLEND);
                        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                        GL11.glDisable(GL11.GL_DEPTH_TEST);
                        GL11.glEnable(GL11.GL_LINE_SMOOTH);
                        GL11.glDepthMask(false);
                        GL11.glLineWidth(1.0F);

                        GL11.glColor3f(renderColour.getRed() / 255F, renderColour.getGreen() / 255F, renderColour.getBlue() / 255F);

                        GL11.glTranslated(-mc.getRenderManager().viewerPosX, -mc.getRenderManager().viewerPosY, -mc.getRenderManager().viewerPosZ);

                        GL11.glBegin(GL11.GL_LINE_STRIP);
                        for (Vector3 position : trajectoryData.getTrajectory()) {
                            GL11.glVertex3d(position.x, position.y, position.z);
                        }

                        GL11.glEnd();

                        if (trajectoryData.getLandingData() != null) {
                            MovingObjectPosition landingPosition = trajectoryData.getLandingData();

                            if (landingPosition.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                                GL11.glTranslated(landingPosition.hitVec.xCoord, landingPosition.hitVec.yCoord, landingPosition.hitVec.zCoord);
                                int sideIndex = landingPosition.sideHit.getIndex();
                                if (sideIndex == 1) {
                                    GL11.glRotatef(180.0F, 1.0F, 0.0F, 0.0F);
                                } else if (sideIndex == 2) {
                                    GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
                                } else if (sideIndex == 3) {
                                    GL11.glRotatef(-90.0F, 1.0F, 0.0F, 0.0F);
                                } else if (sideIndex == 4) {
                                    GL11.glRotatef(-90.0F, 0.0F, 0.0F, 1.0F);
                                } else if (sideIndex == 5) {
                                    GL11.glRotatef(90.0F, 0.0F, 0.0F, 1.0F);
                                }
                                GL11.glRotatef(-90.0F, 1.0F, 0.0F, 0.0F);

                                Color colourWithAlpha = new Color(renderColour.getRed(), renderColour.getGreen(), renderColour.getBlue(), 175);
                                RenderUtilities.drawBorderedRect(-0.4F, -0.4F, 0.4F, 0.4F, 1.0F, renderColour.getRGB(), colourWithAlpha.getRGB());
                                GL11.glTranslated(-landingPosition.hitVec.xCoord, -landingPosition.hitVec.yCoord, -landingPosition.hitVec.zCoord);
                            }

                            if (landingPosition.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
                                GL11.glColor4f(renderColour.getRed() / 255F, renderColour.getGreen() / 255F, renderColour.getBlue() / 255F, 0.17F);
                                RenderUtilities.drawBoundingBox(landingPosition.entityHit.getEntityBoundingBox().expand(0.125, 0, 0.125).offset(0, 0.1, 0));

                                GL11.glColor3f(renderColour.getRed() / 255F, renderColour.getGreen() / 255F, renderColour.getBlue() / 255F);
                                GL11.glLineWidth(0.5F);
                                RenderUtilities.drawOutlinedBoundingBox(landingPosition.entityHit.getEntityBoundingBox().expand(0.125, 0, 0.125).offset(0, 0.1, 0));
                            }
                        }

                        GL11.glDepthMask(true);
                        GL11.glDisable(GL11.GL_LINE_SMOOTH);
                        GL11.glEnable(GL11.GL_DEPTH_TEST);
                        GL11.glEnable(GL11.GL_LIGHTING);
                        GL11.glDisable(GL11.GL_BLEND);
                        GL11.glEnable(GL11.GL_TEXTURE_2D);

                        GL11.glPopMatrix();
                    }
                }
            }
        });
    }

    public ProjectileTrajectoryData getProjectileTrajectory(ProjectileType projectileType, Vector3 sourcePosition, Vector3 sourceVelocity) {
        // Let's not mutate the passed-in Vector3 objects.
        List<Vector3> trajectory = new LinkedList<>();

        double posX = sourcePosition.x;
        double posY = sourcePosition.y;
        double posZ = sourcePosition.z;

        double motionX = sourceVelocity.x;
        double motionY = sourceVelocity.y;
        double motionZ = sourceVelocity.z;

        boolean hasLanded = false;
        MovingObjectPosition landingPosition = null;
        while (!hasLanded && posY > 0.0D) {
            Vec3 present = new Vec3(posX, posY, posZ);
            Vec3 future = new Vec3(posX + motionX, posY + motionY, posZ + motionZ);

            MovingObjectPosition possibleLandingPosition = mc.theWorld.rayTraceBlocks(present, future, false, true, false);

            if (possibleLandingPosition != null) {
                if (possibleLandingPosition.typeOfHit != MovingObjectPosition.MovingObjectType.MISS) {
                    landingPosition = possibleLandingPosition;
                    hasLanded = true;
                }
            } else {
                Entity entityHit = getEntityHit(projectileType, present, future);
                if (entityHit != null) {
                    landingPosition = new MovingObjectPosition(entityHit);
                    hasLanded = true;
                }
            }

            posX += motionX;
            posY += motionY;
            posZ += motionZ;

            float motionAdjustment = 0.99F; // TODO: Check if the projectile is inside a water block.
            motionX *= motionAdjustment;
            motionY *= motionAdjustment;
            motionZ *= motionAdjustment;

            motionY -= projectileType.getGravity();

            if (hasLanded) {
                if (landingPosition.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK)
                    trajectory.add(new Vector3(landingPosition.hitVec.xCoord, landingPosition.hitVec.yCoord, landingPosition.hitVec.zCoord));
            } else {
                trajectory.add(new Vector3(posX, posY, posZ));
            }
        }

        return new ProjectileTrajectoryData(trajectory.toArray(new Vector3[trajectory.size()]), landingPosition);
    }

    private static Entity getEntityHit(ProjectileType type, Vec3 source, Vec3 destination) {
        for (Object o : Minecraft.getMinecraft().theWorld.loadedEntityList) {
            if (o == Minecraft.getMinecraft().getRenderViewEntity())
                continue;
            if (o instanceof EntityLivingBase) {
                EntityLivingBase entity = (EntityLivingBase) o;

                double expander = (type == ProjectileType.ARROW) ? 0.2 : 0.125;
                AxisAlignedBB boundingBox = entity.getEntityBoundingBox().expand(expander, expander, expander);

                MovingObjectPosition raytraceResult = boundingBox.calculateIntercept(source, destination);
                if (raytraceResult != null) {
                    return entity;
                }
            }
        }

        return null;
    }

    private static boolean isItemShootable(ItemStack stack) {
        return ProjectileType.getProjectileTypeFromShootableItem(stack) != null;
    }
}

package host.serenity.serenity.modules.world;

import host.serenity.serenity.api.help.ValueDescription;
import host.serenity.serenity.api.module.Module;
import host.serenity.serenity.api.module.ModuleCategory;
import host.serenity.serenity.api.value.BooleanValue;
import host.serenity.serenity.api.value.IntValue;
import host.serenity.serenity.api.value.ModuleValue;
import host.serenity.serenity.event.player.PlayerWalkingUpdate;
import host.serenity.serenity.event.player.PostPlayerWalkingUpdate;
import host.serenity.serenity.event.player.ShouldSafeWalk;
import host.serenity.serenity.util.BlockHelper;
import host.serenity.serenity.util.iface.EntityLivingBaseExtension;
import host.serenity.synapse.Listener;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.BlockLiquid;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import org.lwjgl.input.Keyboard;

public class Scaffold extends Module {
    private class BlockData {
        public BlockPos position;
        public EnumFacing face;

        public BlockData(BlockPos position, EnumFacing face) {
            this.position = position;
            this.face = face;
        }
    }

    @ModuleValue
    @ValueDescription("The delay between places. (ticks)")
    private IntValue delay = new IntValue("Delay", 2);

    @ModuleValue
    @ValueDescription("Prevents you from falling.")
    private BooleanValue safeWalk = new BooleanValue("Safe Walk", true);

    @ModuleValue
    @ValueDescription("Only go horizontally. (Jumping does nothing to the block place height while moving)")
    private BooleanValue horizontal = new BooleanValue("Horizontal Only", true);

    @ModuleValue
    @ValueDescription("Automatically select bglocks from your hotbar")
    private BooleanValue autoSelect = new BooleanValue("Auto Select", true);

    private BlockData target;

    private int ticker;
    private int lastPlacedY;

    private int slot;

    public Scaffold() {
        super("Scaffold", 0x5DFFA0, ModuleCategory.WORLD);
        registerToggleKeybinding(Keyboard.KEY_Y);

        listeners.add(new Listener<PlayerWalkingUpdate>() {
            @Override
            public void call(PlayerWalkingUpdate event) {
                slot = -1;
                if (autoSelect.getValue()) {
                    int total = 0;

                    for (int index = 36; index < 45; index++) {
                        ItemStack itemStack = mc.thePlayer.inventoryContainer.getSlot(index).getStack();
                        if (itemStack != null) {
                            if (itemStack.getItem() instanceof ItemBlock) {
                                if (((ItemBlock) itemStack.getItem()).getBlock() instanceof BlockFalling)
                                    continue;

                                total += itemStack.stackSize;
                            }
                        }
                    }

                    setDisplay(getName() + EnumChatFormatting.GRAY + String.format(" [%s]", total));
                } else {
                    setDisplay(getName());
                }

                target = null;
                boolean blockInHand = false;
                slot = mc.thePlayer.inventory.currentItem;

                if (mc.thePlayer.getHeldItem() != null)
                    blockInHand = mc.thePlayer.getHeldItem().getItem() instanceof ItemBlock && mc.thePlayer.getHeldItem().stackSize > 0;

                boolean canAutoSelect = autoSelect.getValue() && getHotbarBlock() != -1;
                if (blockInHand || canAutoSelect) {

                    BlockPos blockBelowPlayer = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1, mc.thePlayer.posZ);
                    if (horizontal.getValue()) {
                        if (!mc.thePlayer.onGround && (Math.abs(mc.thePlayer.motionX) > 0.05 || Math.abs(mc.thePlayer.motionZ) > 0.05)) {
                            if (lastPlacedY < mc.thePlayer.posY && lastPlacedY > mc.thePlayer.posY - 3)
                                blockBelowPlayer = new BlockPos(mc.thePlayer.posX, lastPlacedY, mc.thePlayer.posZ);
                        } else if (mc.thePlayer.onGround) {
                            lastPlacedY = (int) Math.floor(mc.thePlayer.posY - 1);
                        }
                    }

                    Block block = mc.theWorld.getBlockState(blockBelowPlayer).getBlock();
                    if (/* mc.theWorld.getBlockState(blockBelowPlayer).getBlock().isReplaceable(mc.theWorld, blockBelowPlayer) || */ block instanceof BlockLiquid || block instanceof BlockAir) {
                        target = getTarget(blockBelowPlayer);
                        if (target != null) {
                            float[] values = BlockHelper.getFacingRotations(target.position.getX(), target.position.getY(), target.position.getZ(), target.face);
                            event.setYaw((float) (values[0] + Math.random() * 2 - 1));
                            event.setPitch((float) (values[1] + Math.random() * 2 - 1));

                            if (autoSelect.getValue() && !blockInHand) {
                                slot = mc.thePlayer.inventory.currentItem;
                                mc.thePlayer.inventory.currentItem = getHotbarBlock();

                                mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
                            }
                        }
                    }
                }
            }
        });

        listeners.add(new Listener<PostPlayerWalkingUpdate>() {
            @Override
            public void call(PostPlayerWalkingUpdate event) {
                if (ticker++ >= delay.getValue()) {
                    if (target != null) {
                        ticker = 0;


                        mc.getNetHandler().addToSendQueue(new C0APacketAnimation());
                        if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld,
                                mc.thePlayer.getHeldItem(), target.position, target.face,
                                new Vec3(target.position.getX(), target.position.getY(), target.position.getZ()))) {
                            lastPlacedY = target.position.getY();
                            if (!mc.thePlayer.onGround && mc.thePlayer.motionY > 0 && !(Math.abs(mc.thePlayer.motionX) > 0.05 || Math.abs(mc.thePlayer.motionZ) > 0.05)) {
                                mc.thePlayer.setEntityBoundingBox(mc.thePlayer.getEntityBoundingBox().offset(0, mc.thePlayer.posY - Math.floor(mc.thePlayer.posY), 0));
                                mc.thePlayer.motionY = 0;
                                ((EntityLivingBaseExtension) mc.thePlayer).setJumpTicks(0);

                                if (mc.thePlayer.movementInput.jump) {
                                    mc.thePlayer.onGround = false;
                                }

                            }
                        }
                    }
                }

                if (autoSelect.getValue() && slot != -1) {
                    mc.thePlayer.inventory.currentItem = slot;
                    slot = -1;
                }
            }
        });

        listeners.add(new Listener<ShouldSafeWalk>(() -> safeWalk.getValue()) {
            @Override
            public void call(ShouldSafeWalk event) {
                if (mc.thePlayer.onGround)
                    event.setShouldSafeWalk(true);
            }
        });
    }

    public BlockData getTarget(BlockPos pos) {
        EnumFacing[] orderedFacingValues = new EnumFacing[] {
                EnumFacing.UP,
                EnumFacing.EAST,
                EnumFacing.NORTH,
                EnumFacing.WEST,
                EnumFacing.SOUTH,
                EnumFacing.DOWN
        };

        for (EnumFacing facing : orderedFacingValues) {
            BlockPos alteredPos = pos.add(facing.getOpposite().getDirectionVec());

            if (!mc.theWorld.getBlockState(alteredPos).getBlock().isReplaceable(mc.theWorld, alteredPos) && !(mc.theWorld.getBlockState(alteredPos).getBlock() instanceof BlockLiquid) && !(mc.theWorld.getBlockState(alteredPos).getBlock() instanceof BlockAir)) {
                return new BlockData(alteredPos, facing);
            }
        }

        return null;
    }

    private int getHotbarBlock() {
        for (int index = 36; index < 45; index++) {
            ItemStack itemStack = mc.thePlayer.inventoryContainer.getSlot(index).getStack();
            if (itemStack != null) {
                if (itemStack.getItem() instanceof ItemBlock) {
                    if (((ItemBlock) itemStack.getItem()).getBlock() instanceof BlockFalling)
                        continue;

                    if (itemStack.stackSize >= 1) {
                        return index - 36;
                    }
                }
            }
        }

        return -1;
    }

    @Override
    protected void onEnable() {
        setDisplay(getName());

        if (mc.thePlayer != null)
            lastPlacedY = (int) Math.floor(mc.thePlayer.posY - 1);
    }

    @Override
    protected void onDisable() {
        mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
    }
}

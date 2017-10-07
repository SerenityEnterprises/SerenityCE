package host.serenity.serenity.modules.world;

import host.serenity.serenity.api.module.Module;
import host.serenity.serenity.api.module.ModuleCategory;
import host.serenity.serenity.api.module.mode.ModuleMode;
import host.serenity.serenity.api.value.BooleanValue;
import host.serenity.serenity.api.value.Value;
import host.serenity.serenity.event.network.ReceivePacket;
import host.serenity.serenity.event.player.PlayerWalkingUpdate;
import host.serenity.serenity.event.player.PostPlayerWalkingUpdate;
import host.serenity.serenity.util.EntityHelper;
import host.serenity.serenity.util.TimeHelper;
import host.serenity.synapse.Listener;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.player.inventory.ContainerLocalMenu;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C0DPacketCloseWindow;
import net.minecraft.network.play.client.C0EPacketClickWindow;
import net.minecraft.network.play.server.S2DPacketOpenWindow;
import net.minecraft.network.play.server.S2EPacketCloseWindow;
import net.minecraft.network.play.server.S30PacketWindowItems;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class ChestStealer extends Module {
    private boolean stealing;

    public boolean isStealing() {
        return isEnabled() && stealing;
    }

    public ChestStealer() {
        super("Chest Stealer", 0x4CFFAB, ModuleCategory.WORLD);

        registerMode(new ModuleMode("Single") {
            private TimeHelper time = new TimeHelper();

            private BooleanValue autoClose = new BooleanValue("Auto Close", true);

            @Override
            public void addListeners(List<Listener<?>> listeners) {
                listeners.add(new Listener<PlayerWalkingUpdate>() {
                    @Override
                    public void call(PlayerWalkingUpdate event) {
                        if (mc.currentScreen instanceof GuiChest) {
                            if (!isContainerEmpty(mc.thePlayer.openContainer)) {
                                for (Slot slot : (List<Slot>) mc.thePlayer.openContainer.inventorySlots) {
                                    if (slot != null) {
                                        if (slot.getStack() != null) {
                                            if (time.hasReached(65L)) {
                                                mc.playerController.windowClick(mc.thePlayer.openContainer.windowId, slot.slotNumber, 0, 1, mc.thePlayer);
                                                time.reset();
                                            }
                                        }
                                    }
                                }
                            } else {
                                if (autoClose.getValue()) {
                                    mc.displayGuiScreen(null);
                                }
                            }
                        }
                    }
                });
            }

            @Override
            public Value<?>[] getValues() {
                return new Value<?>[] { autoClose };
            }
        });

        registerMode(new ModuleMode("Aura") {
            private List<BlockPos> raidedChests = new ArrayList<>();
            private List<C0EPacketClickWindow> queuedClicks = new ArrayList<>();

            private int waiting;
            private List<Integer> windowIds = new ArrayList<>();
            private boolean openingChest;
            private Container container;
            private int clicks;

            private C08PacketPlayerBlockPlacement placePacket = null;

            private int state;

            @Override
            public void addListeners(List<Listener<?>> listeners) {
                listeners.add(new Listener<PlayerWalkingUpdate>() {
                    @Override
                    public void call(PlayerWalkingUpdate event) {
                        if (openingChest && queuedClicks.isEmpty()) {
                            waiting++;
                            if (waiting > 20) {
                                openingChest = false;
                            }
                        } else {
                            waiting = 0;
                        }
                        /* new ArrayList<>(raidedChests).stream().filter(position -> mc.thePlayer.getDistanceSq(position.getX() + 0.5, position.getY() + 0.5 - mc.thePlayer.getEyeHeight(), position.getZ() + 0.5) > 4.5F * 4.5F).forEach(position -> {
                            raidedChests.remove(position);
                        }); */
                        if (clicks < 3 && !queuedClicks.isEmpty()) {
                            C0EPacketClickWindow windowClick = queuedClicks.remove(0);
                            mc.getNetHandler().getNetworkManager().sendPacket(windowClick);
                            if (container != null) {
                                container.slotClick(windowClick.getSlotId(), windowClick.getUsedButton(), windowClick.getMode(), mc.thePlayer);
                            }
                            clicks++;
                            if (queuedClicks.isEmpty()) {
                                mc.getNetHandler().getNetworkManager().sendPacket(new C0DPacketCloseWindow(0));
                            }
                        } else {
                            clicks = 0;
                        }
                        if (!queuedClicks.isEmpty() || openingChest || (mc.thePlayer.getCurrentEquippedItem() != null && mc.thePlayer.isSneaking())) {
                            return;
                        }
                        List<BlockPos> positions = new ArrayList<>();
                        for (TileEntity tileEntity : (List<TileEntity>) mc.theWorld.loadedTileEntityList) {
                            BlockPos position = tileEntity.getPos();
                            if (!raidedChests.contains(position) && mc.thePlayer.getDistanceSq(position.getX() + 0.5, position.getY() + 0.5 - mc.thePlayer.getEyeHeight(), position.getZ() + 0.5) < 4.5F*4.5F) {
                                if (tileEntity instanceof TileEntityChest) {
                                    TileEntityChest chest = (TileEntityChest) tileEntity;
                                    if (chest.adjacentChestZNeg == null && chest.adjacentChestXNeg == null) {
                                        positions.add(position);
                                    }
                                }
                            }
                        }
                        if (positions.isEmpty()) {
                            raidedChests.clear();
                        } else {
                            BlockPos position = positions.get(0);
                            raidedChests.add(position);
                            float[] rotation = EntityHelper.getAnglesToPosition(mc.thePlayer, position.getX() + 0.5, position.getY() + 1, position.getZ() + 0.5);

                            event.setYaw((float) (rotation[0] + Math.random() - 0.5));
                            event.setPitch((float) (rotation[1] + Math.random() - 0.5));

                            openingChest = true;
                            mc.getNetHandler().getNetworkManager().sendPacket(new C0DPacketCloseWindow(0));
                            placePacket = new C08PacketPlayerBlockPlacement(position, 1, null, 0, 0, 0);
                        }
                    }
                });

                listeners.add(new Listener<PostPlayerWalkingUpdate>() {
                    @Override
                    public void call(PostPlayerWalkingUpdate event) {
                        if (placePacket != null) {
                            mc.getNetHandler().getNetworkManager().sendPacket(placePacket);
                            stealing = true;
                            placePacket = null;
                        }
                    }
                });

                listeners.add(new Listener<ReceivePacket>() {
                    @Override
                    public void call(ReceivePacket event) {
                        if (event.getPacket() instanceof S2DPacketOpenWindow && ((S2DPacketOpenWindow) event.getPacket()).getGuiId().equals("minecraft:chest")) {
                            if (openingChest) {
                                event.setCancelled(true);
                                S2DPacketOpenWindow openWindow = (S2DPacketOpenWindow) event.getPacket();
                                container = new GuiChest(mc.thePlayer.inventory, new ContainerLocalMenu(openWindow.getGuiId(), openWindow.getWindowTitle(), openWindow.getSlotCount())).inventorySlots;
                                windowIds.add(((S2DPacketOpenWindow) event.getPacket()).getWindowId());
                                stealing = true;
                            }
                        }

                        if (event.getPacket() instanceof S30PacketWindowItems) {
                            S30PacketWindowItems windowItems = (S30PacketWindowItems) event.getPacket();
                            if (openingChest && windowIds.contains(windowItems.func_148911_c())) {
                                stealing = true;
                                short clickNumber = 0;
                                for (int index = 0; index < windowItems.getItemStacks().length - 36; index++) {
                                    ItemStack itemStack = windowItems.getItemStacks()[index];
                                    if (itemStack != null) {
                                        queuedClicks.add(new C0EPacketClickWindow(windowItems.func_148911_c(), index, 0, 1, itemStack, clickNumber++));
                                    }
                                }
                                container.putStacksInSlots(windowItems.getItemStacks());
                                windowIds.remove(Integer.valueOf(windowItems.func_148911_c()));
                                openingChest = false;

                                if (queuedClicks.isEmpty()) {
                                    mc.getNetHandler().getNetworkManager().sendPacket(new C0DPacketCloseWindow(0));
                                    stealing = false;
                                }
                            }
                        }

                        if (event.getPacket() instanceof S2EPacketCloseWindow && mc.currentScreen instanceof GuiChat) {
                            event.setCancelled(true);
                        }
                    }
                });
            }

            @Override
            public Value<?>[] getValues() {
                return new Value<?>[0];
            }

            @Override
            public void onEnable() {
                this.openingChest = false;
                this.windowIds.clear();
                this.raidedChests.clear();
                this.queuedClicks.clear();
                this.state = 0;
            }

            @Override
            public void onDisable() {
                if (stealing) {
                    stealing = false;
                    mc.getNetHandler().getNetworkManager().sendPacket(new C0DPacketCloseWindow(0));
                }
            }
        });

        setActiveMode("Aura");
    }

    public boolean isContainerEmpty(final Container container) {
        boolean temp = true;
        for (int i = 0, slotAmount = (container.inventorySlots.size() == 90) ? 54 : 27; i < slotAmount; ++i) {
            if (container.getSlot(i).getHasStack()) {
                temp = false;
            }
        }
        return temp;
    }
}

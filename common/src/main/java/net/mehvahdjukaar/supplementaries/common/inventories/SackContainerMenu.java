package net.mehvahdjukaar.supplementaries.common.inventories;

import net.mehvahdjukaar.moonlight.api.misc.IContainerProvider;
import net.mehvahdjukaar.supplementaries.common.block.tiles.SackBlockTile;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModMenuTypes;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;


public class SackContainerMenu extends AbstractContainerMenu implements IContainerProvider {
    public final Container inventory;

    @Override
    public Container getContainer() {
        return inventory;
    }

    public SackContainerMenu(int id, Inventory playerInventory, FriendlyByteBuf packetBuffer) {
        this(id, playerInventory, getContainerFromPacket(playerInventory, packetBuffer));
    }

    //hack for snowy spirit sleds entities
    @Nullable
    private static Container getContainerFromPacket(Inventory playerInventory, FriendlyByteBuf packetBuffer) {
        boolean isBlockPos = packetBuffer.readBoolean();
        Level level = playerInventory.player.level();
        if (isBlockPos) {
            return ModRegistry.SACK_TILE.get().getBlockEntity(
                    level, packetBuffer.readBlockPos()
            );
        } else {
            var e = level.getEntity(packetBuffer.readVarInt());
            if (e instanceof Container c) return c;
            else if (e instanceof IContainerProvider c) return c.getContainer();
            throw new UnsupportedOperationException("Cannot find container associated with entity "+ e);
        }
    }

    public SackContainerMenu(int id, Inventory playerInventory, Container container) {
        super(ModMenuTypes.SACK.get(), id);
        //tile container
        this.inventory = container;
        checkContainerSize(container, SackBlockTile.getUnlockedSlots());
        container.startOpen(playerInventory.player);

        int size = CommonConfigs.Functional.SACK_SLOTS.get();

        int[] dims = SackContainerMenu.getRatio(size);
        if (dims[0] > 9) {
            dims[0] = 9;
            dims[1] = (int) Math.ceil(size / 9f);
        }

        int yp = 17 + (18 * 3) / 2 - (9) * dims[1];

        int dimx = 0;
        int dimXPrev;
        int xp;
        for (int h = 0; h < dims[1]; ++h) {
            dimXPrev = dimx;
            dimx = Math.min(dims[0], size);
            xp = 8 + (18 * 9) / 2 - (dimx * 18) / 2;
            for (int j = 0; j < dimx; ++j) {
                this.addSlot(new DelegatingSlot(container, j + (h * dimXPrev), xp + j * 18, yp + 18 * h));
            }
            size -= dims[0];
        }

        for (int si = 0; si < 3; ++si)
            for (int sj = 0; sj < 9; ++sj)
                this.addSlot(new Slot(playerInventory, sj + (si + 1) * 9, 8 + sj * 18, 84 + si * 18));
        for (int si = 0; si < 9; ++si)
            this.addSlot(new Slot(playerInventory, si, 8 + si * 18, 142));
    }


    @Override
    public boolean stillValid(Player playerIn) {
        return this.inventory.stillValid(playerIn);
    }

    /**
     * Handle when the stack in slot {@code index} is shift-clicked. Normally this moves the stack between the player
     * inventory and the other inventory(s).
     */
    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack item = slot.getItem();
            itemstack = item.copy();
            int activeSlots = CommonConfigs.Functional.SACK_SLOTS.get();
            if (index < activeSlots) {
                if (!this.moveItemStackTo(item, activeSlots, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(item, 0, activeSlots, false)) {
                return ItemStack.EMPTY;
            }

            if (item.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;
    }


    /**
     * Called when the container is closed.
     */
    @Override
    public void removed(Player playerIn) {
        super.removed(playerIn);
        this.inventory.stopOpen(playerIn);
    }


    public static int[] getRatio(int maxSize) {
        int[] dims = {Math.min(maxSize, 23), Math.max(maxSize / 23, 1)};
        for (int[] testAgainst : TARGET_RATIOS) {
            if (testAgainst[0] * testAgainst[1] == maxSize) {
                dims = testAgainst;
                break;
            }
        }
        return dims;
    }


    private static final int[][] TARGET_RATIOS = new int[][]{
            {1, 1},
            {2, 2},
            {3, 2},
            {3, 3},
            {4, 2},
            {5, 2},
            {6, 2},
            {7, 2},
            {8, 2},
            {8, 2},
            {9, 2},
            {10, 2},
            {7, 3},
            {8, 3},
            {9, 3}
    };


}



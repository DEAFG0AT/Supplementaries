package net.mehvahdjukaar.supplementaries.blocks;

import net.mehvahdjukaar.supplementaries.common.CommonUtil;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.List;


public class JarBlock extends Block {
    protected static final VoxelShape SHAPE = VoxelShapes.or(VoxelShapes.create(0.1875D, 0D, 0.1875D, 0.8125D, 0.875D, 0.8125D),
            VoxelShapes.create(0.3125, 0.875, 0.3125, 0.6875, 1, 0.6875));
    public static final DirectionProperty FACING = HorizontalBlock.HORIZONTAL_FACING;
    public static final IntegerProperty LIGHT_LEVEL = CommonUtil.LIGHT_LEVEL_0_15;
    public JarBlock(Properties properties) {
        super(properties);
        this.setDefaultState(this.stateContainer.getBaseState().with(LIGHT_LEVEL, 0).with(FACING, Direction.NORTH));
    }

    @Override
    public float[] getBeaconColorMultiplier(BlockState state, IWorldReader world, BlockPos pos, BlockPos beaconPos) {
        TileEntity tileentity = world.getTileEntity(pos);
        if (tileentity instanceof JarBlockTile) {
            JarBlockTile te = (JarBlockTile) tileentity;
            int color = te.color;
            if (te.isEmpty() || color == 0x000000)
                return null;
            float r = (float) ((color >> 16 & 255)) / 255.0F;
            float g = (float) ((color >> 8 & 255)) / 255.0F;
            float b = (float) ((color & 255)) / 255.0F;
            return new float[]{r, g, b};
        }
        return null;
    }

    //TODO: add ai path nodes to all blocks

    @Override
    public PathNodeType getAiPathNodeType(BlockState state, IBlockReader world, BlockPos pos,  MobEntity entity) {
        return PathNodeType.BLOCKED;
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
                                             BlockRayTraceResult hit) {
        TileEntity tileentity = worldIn.getTileEntity(pos);
        if (tileentity instanceof JarBlockTile) {
            // make te do the work
            JarBlockTile te = (JarBlockTile) tileentity;
            if (te.handleInteraction(player, handIn)) {
                if (!worldIn.isRemote())
                    te.markDirty();
                return ActionResultType.SUCCESS;
            }
        }
        return ActionResultType.PASS;
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if (stack.hasDisplayName()) {
            TileEntity tileentity = worldIn.getTileEntity(pos);
            if (tileentity instanceof JarBlockTile) {
                ((JarBlockTile) tileentity).setCustomName(stack.getDisplayName());
            }
        }
    }

    public ItemStack getJarItem(JarBlockTile te){
        ItemStack returnStack;
        if(te.isEmpty()&&te.hasNoMob()){
            returnStack = new ItemStack(Registry.EMPTY_JAR_ITEM);
        }
        else{
            returnStack = new ItemStack(Registry.JAR_ITEM);
            CompoundNBT compoundnbt = te.saveToNbt(new CompoundNBT());
            if (!compoundnbt.isEmpty())
                returnStack.setTagInfo("BlockEntityTag", compoundnbt);
            //TODO: learn how to use BlockEntityTag
            CommonUtil.saveJarMobItemNBT(returnStack, te.mob);
        }
        if(te.hasCustomName()){
            returnStack.setDisplayName(te.getCustomName());
        }
        return returnStack;
    }

    // shulker box code
    @Override
    public void onBlockHarvested(World worldIn, BlockPos pos, BlockState state, PlayerEntity player) {
        TileEntity tileentity = worldIn.getTileEntity(pos);
        if (tileentity instanceof JarBlockTile) {
            JarBlockTile tile = (JarBlockTile) tileentity;
            if (!worldIn.isRemote && player.isCreative()) {

                ItemStack itemstack = this.getJarItem(tile);

                ItemEntity itementity = new ItemEntity(worldIn, pos.getX()+0.5, pos.getY()+0.5, pos.getZ()+0.5, itemstack);
                itementity.setDefaultPickupDelay();
                worldIn.addEntity(itementity);
            } else {
                tile.fillWithLoot(player);
            }
        }
        super.onBlockHarvested(worldIn, pos, state, player);
    }


    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        TileEntity tileentity = builder.get(LootParameters.BLOCK_ENTITY);
        if (tileentity instanceof JarBlockTile) {
            JarBlockTile tile = (JarBlockTile) tileentity;

            ItemStack itemstack = this.getJarItem(tile);

            return Collections.singletonList(itemstack);
        }
        return super.getDrops(state, builder);
    }

    //for pick block
    @Override
    public ItemStack getItem(IBlockReader worldIn, BlockPos pos, BlockState state) {

        TileEntity tileentity = worldIn.getTileEntity(pos);
        if (tileentity instanceof JarBlockTile) {
            JarBlockTile tile = (JarBlockTile) tileentity;
            return this.getJarItem(tile);
        }
        return super.getItem(worldIn, pos, state);
    }

    // end shoulker box code
    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(LIGHT_LEVEL,FACING);
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        return SHAPE;
    }

    @Override
    public PushReaction getPushReaction(BlockState state) {
        return PushReaction.DESTROY;
    }

    @Override
    public INamedContainerProvider getContainer(BlockState state, World worldIn, BlockPos pos) {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        return tileEntity instanceof INamedContainerProvider ? (INamedContainerProvider) tileEntity : null;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new JarBlockTile();
    }

    @Override
    public boolean eventReceived(BlockState state, World world, BlockPos pos, int eventID, int eventParam) {
        super.eventReceived(state, world, pos, eventID, eventParam);
        TileEntity tileentity = world.getTileEntity(pos);
        return tileentity != null && tileentity.receiveClientEvent(eventID, eventParam);
    }

    @Override
    public int getLightValue(BlockState state, IBlockReader world, BlockPos pos) {
        return state.get(LIGHT_LEVEL);
    }

    @Override
    public boolean hasComparatorInputOverride(BlockState state) {
        return true;
    }

    @Override
    public int getComparatorInputOverride(BlockState blockState, World world, BlockPos pos) {
        TileEntity tileentity = world.getTileEntity(pos);
        if (tileentity instanceof JarBlockTile)
            return Container.calcRedstoneFromInventory((JarBlockTile) tileentity);
        else
            return 0;
    }

    public BlockState rotate(BlockState state, Rotation rot) {
        return state.with(FACING, rot.rotate(state.get(FACING)));
    }

    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.toRotation(state.get(FACING)));
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return this.getDefaultState().with(FACING, context.getPlacementHorizontalFacing().getOpposite());
    }


}
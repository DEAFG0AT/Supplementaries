package net.mehvahdjukaar.supplementaries.fabric;

import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.mehvahdjukaar.supplementaries.mixins.fabric.MobBucketItemAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.storage.loot.LootPool;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class ForgeHelperImpl {

    public static boolean canEntityDestroy(Level level, BlockPos pos, Animal animal) {
        if (!level.isLoaded(pos)) {
            return false;
        } else {
            return PlatformHelper.isMobGriefingOn(level, animal);
        }
    }

    public static void openContainerScreen(ServerPlayer player, MenuProvider menuProvider, BlockPos pos) {
        //TODO: check this
        player.openMenu(menuProvider);
    }

    public static boolean onExplosionStart(Level level, Explosion explosion) {
        return true;
    }

    // TODO: fabric
    public static void onExplosionDetonate(Level level, Explosion explosion, List<Entity> entities, double diameter) {
    }

    public static void onLivingConvert(LivingEntity skellyHorseMixin, LivingEntity newHorse) {
    }

    public static boolean canLivingConvert(LivingEntity entity, EntityType<? extends LivingEntity> outcome, Consumer<Integer> timer) {
        return true;
    }

    public static double getReachDistance(LivingEntity entity) {
        return 4.5;
    }

    public static float getExplosionResistance(BlockState state, Level level, BlockPos pos, Explosion explosion) {
        return state.getBlock().getExplosionResistance();
    }

    public static void onBlockExploded(BlockState blockstate, Level level, BlockPos blockpos, Explosion explosion) {
    }

    public static boolean areStacksEqual(ItemStack stack, ItemStack other, boolean sameNbt) {
        return stack.equals(other);
    }

    public static boolean isFireSource(BlockState blockState, Level level, BlockPos pos, Direction up) {
        return blockState.is(level.dimensionType().infiniburn());
    }

    public static boolean canDropFromExplosion(BlockState blockstate, Level level, BlockPos blockpos, Explosion explosion) {
        return blockstate.getBlock().dropFromExplosion(explosion);
    }

    public static boolean isDye(ItemStack itemstack) {
        return itemstack.getItem() instanceof DyeItem;
    }

    public static DyeColor getColor(ItemStack stack) {
        if (stack.getItem() instanceof DyeItem dyeItem) {
            return dyeItem.getDyeColor();
        }
        return null;
    }

    public static BlockState rotateBlock(BlockState state, Level world, BlockPos targetPos, Rotation rot) {
        return state.rotate(rot);
    }

    public static boolean canHarvestBlock(BlockState state, ServerLevel level, BlockPos pos, ServerPlayer player) {
        return !state.requiresCorrectToolForDrops() || player.hasCorrectToolForDrops(state);
    }

    public static boolean isMultipartEntity(Entity e) {
        return e instanceof EnderDragon;
    }

    public static void setPoolName(LootPool.Builder pool, String name) {
    }

    public static RailShape getRailDirection(BaseRailBlock railBlock, BlockState blockstate, Level level, BlockPos blockpos, AbstractMinecart o) {
        return blockstate.getValue(railBlock.getShapeProperty());
    }

    public static Optional<ItemStack> getCraftingRemainingItem(ItemStack itemstack) {
        return Optional.ofNullable(itemstack.getItem().getCraftingRemainingItem()).map(Item::getDefaultInstance);
    }

    public static void reviveEntity(Entity entity) {
    }

    public static EntityType<?> getFishType(MobBucketItem bucketItem) {
        return ((MobBucketItemAccessor) bucketItem).getType();
    }


}

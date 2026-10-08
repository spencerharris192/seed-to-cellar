package io.github.spencerharris192.seedtocellar.decor;

import io.github.spencerharris192.seedtocellar.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

/**
 * The invisible seat a player sits on when they use a Bar Stool. It sits at the stool, carries its rider at the cushion's
 * height, and goes away when the rider stands up or the stool is gone. It's never saved with the world.
 */
public class SeatEntity extends Entity {
    /** A sitting player's hips are this far above their feet (where riders are placed from). */
    private static final double HIPS = 0.35;

    public SeatEntity(EntityType<? extends SeatEntity> type, Level level) {
        super(type, level);
        noPhysics = true;
    }

    public SeatEntity(Level level, BlockPos stool, double seat) {
        this(ModEntities.SEAT.get(), level);
        setPos(stool.getX() + 0.5, stool.getY() + seat - HIPS, stool.getZ() + 0.5);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide() && (getPassengers().isEmpty()
                || !(level().getBlockState(blockPosition()).getBlock() instanceof BarStoolBlock))) {
            ejectPassengers();
            discard();
        }
    }

    /** The rider's feet go where the seat is. */
    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity passenger, EntityDimensions dimensions, float scale) {
        return Vec3.ZERO;
    }

    /** Stands the rider up beside the stool, wherever they fit. */
    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
        BlockPos stool = blockPosition();
        for (Direction side : Direction.Plane.HORIZONTAL) {
            Vec3 spot = DismountHelper.findSafeDismountLocation(passenger.getType(), level(), stool.relative(side), true);
            if (spot != null) return spot;
        }
        return super.getDismountLocationForPassenger(passenger);
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        return false;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
    }
}

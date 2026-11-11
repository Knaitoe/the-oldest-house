package io.github.knaitoe.theoldesthouse.labyrinth;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * 0.4.435, the owner: "Let players ride bikes? Maintaining balance and pedaling left/right should be a function." Patrick's bike or
 * Kendra's, being ridden. The bike block (BorrascaShopBlock BIKE) becomes this while someone is on it. When they get off or come off, it
 * goes back to being a block: on its kickstand, or lying on its side.
 * <p>
 * The ride itself is {@link RiddenBike}'s, which Bike night's bikes share (0.4.463): the pedals that are also the balance, and since
 * then the hop. This class is what makes it one of Drisking's two: whose it is, the block it stands as, and what the town's children
 * say about it.
 */
public final class BorrascaBike extends RiddenBike {
    private static final EntityDataAccessor<Integer> WHOSE=SynchedEntityData.defineId(BorrascaBike.class,EntityDataSerializers.INT);
    public static final int PATRICK=0,KENDRA=1;

    public BorrascaBike(EntityType<? extends BorrascaBike> type,Level level){super(type,level);}

    @Override protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(WHOSE,PATRICK);}
    public int whose(){return entityData.get(WHOSE);}
    public void setUp(ServerPlayer p,int whose,float yaw){setUp(p,yaw);entityData.set(WHOSE,whose);}

    @Override protected Stats stats(){return Stats.PLAIN;}
    @Override protected void ridden(ServerPlayer p){BorrascaStory.bikeRidden(p,this);}
    @Override protected void came(ServerPlayer p,int side,Off why){BorrascaStory.bikeFell(p,whose(),why);}
    @Override protected void parkAs(ServerLevel l,@Nullable BlockPos at,boolean lying){
        if(at!=null)l.setBlock(at,BorrascaShopBlock.of(BorrascaShopBlock.Kind.BIKE,Direction.fromYRot(getYRot()).getCounterClockWise(),lying?1:0),3);
        BorrascaStory.bikeParked(l,reader,whose(),at);
    }
}

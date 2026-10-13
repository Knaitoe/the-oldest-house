package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The lake house's green canoe with brown trim. Underneath it is a vanilla boat, so it floats, paddles and seats two the
 * way readers already know; the client draws it as a canoe ({@code LakeHouseCanoeRenderer}).
 * <p>It is lent: each reader has one waiting by the dock, it cannot be broken or picked up and drops nothing, and it is
 * put away once its reader has left the scene and nobody is sitting in it. Anyone in the scene may ride in it; Tess takes
 * the second seat only behind a reader.
 * <p>Its paint wears in the culvert ({@link #chip()}), drawn as three textures.
 */
public final class LakeHouseCanoe extends Boat {
    public static final String TAG="LakeHouseCanoe";
    /** How worn the paint is: 0 fresh, 1 chipped, 2 scraped. */
    private static final EntityDataAccessor<Integer> CHIPS=SynchedEntityData.defineId(LakeHouseCanoe.class,EntityDataSerializers.INT);
    public static final int MAX_CHIPS=2;
    /** Where the canoes wait, one per reader: either side of the dock's far end. */
    static final BlockPos[] SPOTS={new BlockPos(4,-1,-19),new BlockPos(-4,-1,-19),new BlockPos(4,-1,-16),new BlockPos(-4,-1,-16)};
    private @Nullable UUID owner;
    private boolean inCulvert;
    private int scrapeTicks;

    public LakeHouseCanoe(EntityType<? extends Boat> type,Level level){super(type,level);setInvulnerable(true);}
    @Override protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(CHIPS,0);}

    public int chips(){return entityData.get(CHIPS);}
    /** One more knock against the culvert's walls. */
    public void chip(){entityData.set(CHIPS,Math.min(MAX_CHIPS,chips()+1));}
    void chips(int chips){entityData.set(CHIPS,Math.max(0,Math.min(MAX_CHIPS,chips)));}
    public @Nullable UUID owner(){return owner;}

    @Override protected boolean canAddPassenger(Entity e){
        if(!super.canAddPassenger(e))return false;
        if(e instanceof Player)return true;
        // Tess sits behind her reader, never at the paddle on her own.
        return e instanceof LiteraryActor&&getFirstPassenger() instanceof Player;
    }
    @Override public boolean hurt(DamageSource source,float amount){return false;}
    @Override public Item getDropItem(){return Items.AIR;}
    /** A canoe dropped onto land is only set down: no splintering into planks. */
    @Override protected void checkFallDamage(double y,boolean onGround,BlockState state,BlockPos pos){resetFallDistance();}

    /**
     * Under the service drive the culvert's ceiling is a block over the gunwales: too low and narrow for the paddles, so the
     * canoe crawls along as its readers push off the walls, scraping, and comes out with its paint chipped.
     */
    @Override public void tick(){
        super.tick();
        var above=blockPosition().above(2);
        boolean low=isVehicle()&&!level().getBlockState(above).getCollisionShape(level(),above).isEmpty();
        if(low&&isControlledByLocalInstance())setDeltaMovement(getDeltaMovement().multiply(.82,1,.82));
        if(low&&!level().isClientSide){
            if(!inCulvert){chip();for(var e:getPassengers())if(e instanceof ServerPlayer p)p.displayClientMessage(Component.literal("Too low for the paddles. You push along the walls with your hands."),true);}
            double dx=getX()-xo,dz=getZ()-zo;
            if(--scrapeTicks<=0&&dx*dx+dz*dz>1e-4){level().playSound(null,getX(),getY(),getZ(),LakeHouseRegistry.SCRAPE.get(),SoundSource.NEUTRAL,.7F,.9F+random.nextFloat()*.2F);scrapeTicks=30;}
        }
        inCulvert=low;
    }

    @Override public void addAdditionalSaveData(CompoundTag tag){super.addAdditionalSaveData(tag);if(owner!=null)tag.putUUID("Owner",owner);tag.putInt("Chips",chips());}
    @Override public void readAdditionalSaveData(CompoundTag tag){super.readAdditionalSaveData(tag);owner=tag.hasUUID("Owner")?tag.getUUID("Owner"):null;chips(tag.getInt("Chips"));}

    // ------------------------------------------------------------------------------------------------ lending
    /** Puts a canoe by the dock for this reader unless one of theirs is already in the scene. */
    public static void ensure(ServerPlayer p){
        var b=LakeHouse.readerBase(p);if(b==null)return;var l=p.serverLevel();
        if(l.dimension()!=NovelRooms.dimension(LakeHouse.PLACE))return;
        var box=LakeHouse.box(b);
        if(!l.getEntitiesOfClass(LakeHouseCanoe.class,box,c->p.getUUID().equals(c.owner)).isEmpty())return;
        var c=LakeHouseRegistry.CANOE.get().create(l);if(c==null)return;
        c.owner=p.getUUID();c.addTag(TAG);
        var at=b.offset(SPOTS[0]);
        for(var spot:SPOTS){var cell=b.offset(spot);
            if(l.getEntitiesOfClass(LakeHouseCanoe.class,new net.minecraft.world.phys.AABB(cell).inflate(1.5)).isEmpty()){at=cell;break;}}
        // Bow to the north, toward the culvert and the third lake.
        c.moveTo(at.getX()+.5,at.getY()+.5,at.getZ()+.5,180,0);
        l.addFreshEntity(c);
    }
    /** Puts away every empty canoe whose reader is no longer in the scene. */
    public static void tidy(ServerLevel l,BlockPos b){
        var box=LakeHouse.box(b);
        for(var c:l.getEntitiesOfClass(LakeHouseCanoe.class,box)){
            if(c.isVehicle())continue;
            var reader=c.owner==null?null:l.getServer().getPlayerList().getPlayer(c.owner);
            if(reader==null||reader.level()!=l||!box.contains(reader.position()))c.discard();
        }
    }
}

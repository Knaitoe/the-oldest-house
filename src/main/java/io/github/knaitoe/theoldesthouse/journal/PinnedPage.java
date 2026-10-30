package io.github.knaitoe.theoldesthouse.journal;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * A torn page pinned to a wall, or lying flat on a table (TornPages). It shows its first few lines at a glance and opens in full to
 * a right-click. Only its writer can take it down, and it goes back into their journal's stub; if what holds it up goes, it falls.
 */
public final class PinnedPage extends Entity {
    public static final float WIDTH=.6F, HEIGHT=.75F, THICK=1/16F;
    private static final EntityDataAccessor<Direction> FACE=SynchedEntityData.defineId(PinnedPage.class,EntityDataSerializers.DIRECTION);
    private static final EntityDataAccessor<Float> TURN=SynchedEntityData.defineId(PinnedPage.class,EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<String> NAME=SynchedEntityData.defineId(PinnedPage.class,EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> GLANCE=SynchedEntityData.defineId(PinnedPage.class,EntityDataSerializers.STRING);
    private UUID owner=new UUID(0,0);private int page,serial;private BlockPos support=BlockPos.ZERO;

    public PinnedPage(EntityType<? extends PinnedPage> type,Level level){super(type,level);noPhysics=true;}
    static PinnedPage create(ServerLevel level,BlockPos support,Direction face,float turn,TornPages.Ref ref,String name,String glance){
        var e=new PinnedPage(JournalRegistry.PINNED_PAGE.get(),level);e.owner=ref.owner();e.page=ref.page();e.serial=ref.serial();e.support=support.immutable();
        e.entityData.set(FACE,face);e.entityData.set(TURN,turn);e.entityData.set(NAME,name);e.entityData.set(GLANCE,glance);e.place();return e;
    }
    public TornPages.Ref ref(){return new TornPages.Ref(owner,page,serial);}
    public Direction face(){return entityData.get(FACE);}
    public float turn(){return entityData.get(TURN);}
    public String writer(){return entityData.get(NAME);}
    public String glance(){return entityData.get(GLANCE);}
    public BlockPos support(){return support;}

    @Override protected void defineSynchedData(SynchedEntityData.Builder b){b.define(FACE,Direction.NORTH);b.define(TURN,0F);b.define(NAME,"");b.define(GLANCE,"");}
    @Override public void onSyncedDataUpdated(EntityDataAccessor<?> key){super.onSyncedDataUpdated(key);if(FACE.equals(key)||TURN.equals(key))setBoundingBox(makeBoundingBox());}
    /** On the face of its support, centred: up against a wall, or flat on a table top. */
    private void place(){
        var face=face();var c=Vec3.atCenterOf(support);
        if(face==Direction.UP)setPos(c.x,support.getY()+1,c.z);
        else setPos(c.x+face.getStepX()*(.5+THICK/2),c.y-HEIGHT/2,c.z+face.getStepZ()*(.5+THICK/2));
    }
    @Override protected AABB makeBoundingBox(){
        var at=position();Direction face=entityData==null?Direction.NORTH:face();
        if(face==Direction.UP){boolean across=entityData!=null&&Math.abs(Math.round(turn()/90F))%2==1;double hx=(across?HEIGHT:WIDTH)/2,hz=(across?WIDTH:HEIGHT)/2;return new AABB(at.x-hx,at.y,at.z-hz,at.x+hx,at.y+.02,at.z+hz);}
        double hx=face.getAxis()==Direction.Axis.X?THICK/2:WIDTH/2,hz=face.getAxis()==Direction.Axis.Z?THICK/2:WIDTH/2;
        return new AABB(at.x-hx,at.y,at.z-hz,at.x+hx,at.y+HEIGHT,at.z+hz);
    }
    @Override public void tick(){
        checkBelowWorld();
        if(!(level() instanceof ServerLevel l)||tickCount%20!=0)return;
        var t=TornPages.torn(Journals.get(l.getServer()),ref());
        if(t==null||t.where!=TornPages.Where.PINNED||!getUUID().equals(t.entity)){discard();return;}
        // What held it up is gone: it falls where it was.
        if(!l.getBlockState(support).isFaceSturdy(l,support,face())){
            TornPages.carried(l.getServer(),ref());
            var item=new ItemEntity(l,getX(),getY()+.1,getZ(),TornPages.stack(owner,writer(),page,serial));item.setDefaultPickUpDelay();
            discard();l.addFreshEntity(item);
        }
    }
    /** Read by anyone; its writer, crouching, takes it back into their journal. */
    @Override public InteractionResult interact(Player player,InteractionHand hand){
        if(!(player instanceof ServerPlayer p))return InteractionResult.SUCCESS;
        if(p.getUUID().equals(owner)&&p.isShiftKeyDown()){
            if(!TornPages.reclaim(p,page))p.displayClientMessage(Component.literal("It has to go back into your journal, and you aren't carrying it."),true);
            return InteractionResult.CONSUME;
        }
        TornPages.read(p,ref(),writer());return InteractionResult.CONSUME;
    }
    /** A blow takes it down only for its writer, and then it goes back into their journal. Nobody else can tear it down. */
    @Override public boolean skipAttackInteraction(Entity attacker){
        if(attacker instanceof ServerPlayer p&&p.getUUID().equals(owner)&&!TornPages.reclaim(p,page))p.displayClientMessage(Component.literal("It has to go back into your journal, and you aren't carrying it."),true);
        return true;
    }
    @Override public boolean hurt(DamageSource source,float amount){return false;}
    @Override public boolean isPickable(){return true;}
    @Override public boolean isPushable(){return false;}
    @Override public boolean canBeHitByProjectile(){return false;}
    @Override public boolean shouldRenderAtSqrDistance(double d){return d<48*48;}
    @Override protected void readAdditionalSaveData(CompoundTag t){
        if(t.hasUUID("Owner"))owner=t.getUUID("Owner");page=t.getInt("Page");serial=t.getInt("Serial");support=BlockPos.of(t.getLong("Support"));
        var face=Direction.byName(t.getString("Face"));entityData.set(FACE,face==null||face==Direction.DOWN?Direction.NORTH:face);
        entityData.set(TURN,t.getFloat("Turn"));entityData.set(NAME,t.getString("Name"));entityData.set(GLANCE,t.getString("Glance"));place();
    }
    @Override protected void addAdditionalSaveData(CompoundTag t){
        t.putUUID("Owner",owner);t.putInt("Page",page);t.putInt("Serial",serial);t.putLong("Support",support.asLong());t.putString("Face",face().getName());
        t.putFloat("Turn",turn());t.putString("Name",writer());t.putString("Glance",glance());
    }
}

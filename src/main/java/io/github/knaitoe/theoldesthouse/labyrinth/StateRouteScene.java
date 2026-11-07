package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 0.4.333: the highway's one shared state, kept on the anchor under the median and sent to every client on the road: the traffic in
 * its four lanes, the semis already hissing on the tar, whether the road is at speed, crawling or dead, and who is on it. Vehicles run
 * at a constant speed between updates, so clients carry them on from the last update themselves. The story is StateRoute's.
 */
public final class StateRouteScene extends BlockEntity {
    /** The road at seventy miles an hour; bumper to bumper at ten once the key is out; dead black once the gate is open. */
    public static final int FAST=0,CRAWL=1,DARK=2;
    /** What is on the road. The wagon only ever crawls. */
    public static final int PICKUP=0,CAR=1,SEMI=2,WAGON=3;
    public static final class Vehicle {
        public int id,kind,lane;public double x,speed;public int paint;
        public Vehicle(int id,int kind,int lane,double x,double speed,int paint){this.id=id;this.kind=kind;this.lane=lane;this.x=x;this.speed=speed;this.paint=paint;}
        public int dir(){return StateRouteSite.dir(lane);}
        public double length(){return StateRouteSite.length(kind);}
        public double front(){return x+dir()*length()/2;}
        public double rear(){return x-dir()*length()/2;}
    }
    /** A semi already announced: its lane and the tick its nose reaches the middle of the road. */
    public record Coming(int lane,long arrives){}
    /**
     * 0.4.336: what a car or a semi left of a reader on the tar, for everyone on the road to see a while: whose, where, which way it lies,
     * when, what hit it, and the House's body they were wearing (null when the House's bodies are off and their own skin is drawn).
     */
    public record Wreck(UUID reader,double x,double z,float yaw,long at,int kind,@Nullable HouseBodies.Body body){}
    public final List<Wreck> wrecks=new ArrayList<>();
    public int mode=FAST;public long modeAt;
    public final List<Vehicle> traffic=new ArrayList<>();
    public final List<Coming> coming=new ArrayList<>();
    public long[] nextSpawn=new long[4];
    public int nextId=1;
    /** The key: in the mailbox, or out (and who has it). */
    public boolean keyOut;public @Nullable UUID keyHolder;
    /** The convoy that closes in round whoever first drops into the median: when it was called, and that it has been. */
    public long convoyAt=-1;public boolean convoyDone;
    public long releasedAt=-1;public @Nullable UUID releasedBy;
    /** Readers the House read as ones who learned to keep out of sight, crouched in the median weeds: the wagon's heads do not find them. */
    public final Set<UUID> unseen=new HashSet<>();
    public final Set<UUID> party=new HashSet<>();
    public final Map<UUID,Long> partySeen=new HashMap<>();
    public int emptyTicks;
    /** Client only: when the last update came, for carrying the traffic on between updates. */
    public long syncedAt;
    public StateRouteScene(BlockPos at,BlockState state){super(StateRouteRegistry.SCENE.get(),at,state);}
    public BlockPos base(){return worldPosition.subtract(StateRouteSite.ANCHOR);}
    public void sync(){setChanged();if(level!=null&&!level.isClientSide){level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),Block.UPDATE_CLIENTS);}}
    public void tick(){StateRoute.tick(this);}
    private static void uuid(CompoundTag t,String k,@Nullable UUID id){if(id!=null)t.putUUID(k,id);}
    private static @Nullable UUID uuid(CompoundTag t,String k){return t.hasUUID(k)?t.getUUID(k):null;}
    @Override protected void saveAdditional(CompoundTag t,HolderLookup.Provider r){
        super.saveAdditional(t,r);
        t.putInt("Mode",mode);t.putLong("ModeAt",modeAt);t.putInt("NextId",nextId);t.putLongArray("NextSpawn",nextSpawn);
        var list=new ListTag();for(var v:traffic){var e=new CompoundTag();e.putInt("Id",v.id);e.putInt("Kind",v.kind);e.putInt("Lane",v.lane);e.putDouble("X",v.x);e.putDouble("Speed",v.speed);e.putInt("Paint",v.paint);list.add(e);}t.put("Traffic",list);
        var coming=new ListTag();for(var c:this.coming){var e=new CompoundTag();e.putInt("Lane",c.lane());e.putLong("Arrives",c.arrives());coming.add(e);}t.put("Coming",coming);
        t.putBoolean("KeyOut",keyOut);uuid(t,"KeyHolder",keyHolder);t.putLong("ConvoyAt",convoyAt);t.putBoolean("ConvoyDone",convoyDone);t.putLong("Released",releasedAt);uuid(t,"ReleasedBy",releasedBy);
        var p=new ListTag();for(var id:party){var e=new CompoundTag();e.putUUID("Id",id);e.putLong("Seen",partySeen.getOrDefault(id,0L));p.add(e);}t.put("Party",p);
        var u=new ListTag();for(var id:unseen){var e=new CompoundTag();e.putUUID("Id",id);u.add(e);}t.put("Unseen",u);
        t.putInt("Empty",emptyTicks);
        var w=new ListTag();for(var x:wrecks){var e=new CompoundTag();e.putUUID("Id",x.reader());e.putDouble("X",x.x());e.putDouble("Z",x.z());e.putFloat("Yaw",x.yaw());e.putLong("At",x.at());e.putInt("Kind",x.kind());
            if(x.body()!=null){var bd=x.body();e.putIntArray("Body",new int[]{bd.age(),bd.presentation(),bd.cut(),bd.tone(),bd.hair(),bd.face()});}w.add(e);}t.put("Wrecks",w);
    }
    @Override protected void loadAdditional(CompoundTag t,HolderLookup.Provider r){
        super.loadAdditional(t,r);
        mode=t.getInt("Mode");modeAt=t.getLong("ModeAt");nextId=Math.max(1,t.getInt("NextId"));var ns=t.getLongArray("NextSpawn");nextSpawn=ns.length==4?ns:new long[4];
        traffic.clear();for(var e:t.getList("Traffic",Tag.TAG_COMPOUND)){var c=(CompoundTag)e;traffic.add(new Vehicle(c.getInt("Id"),c.getInt("Kind"),c.getInt("Lane"),c.getDouble("X"),c.getDouble("Speed"),c.getInt("Paint")));}
        coming.clear();for(var e:t.getList("Coming",Tag.TAG_COMPOUND)){var c=(CompoundTag)e;coming.add(new Coming(c.getInt("Lane"),c.getLong("Arrives")));}
        keyOut=t.getBoolean("KeyOut");keyHolder=uuid(t,"KeyHolder");convoyAt=t.contains("ConvoyAt")?t.getLong("ConvoyAt"):-1;convoyDone=t.getBoolean("ConvoyDone");releasedAt=t.contains("Released")?t.getLong("Released"):-1;releasedBy=uuid(t,"ReleasedBy");
        party.clear();partySeen.clear();for(var e:t.getList("Party",Tag.TAG_COMPOUND)){var c=(CompoundTag)e;if(c.hasUUID("Id")){party.add(c.getUUID("Id"));partySeen.put(c.getUUID("Id"),c.getLong("Seen"));}}
        unseen.clear();for(var e:t.getList("Unseen",Tag.TAG_COMPOUND)){var c=(CompoundTag)e;if(c.hasUUID("Id"))unseen.add(c.getUUID("Id"));}
        emptyTicks=t.getInt("Empty");
        wrecks.clear();for(var e:t.getList("Wrecks",Tag.TAG_COMPOUND)){var c=(CompoundTag)e;if(!c.hasUUID("Id"))continue;var bd=c.getIntArray("Body");
            wrecks.add(new Wreck(c.getUUID("Id"),c.getDouble("X"),c.getDouble("Z"),c.getFloat("Yaw"),c.getLong("At"),c.getInt("Kind"),bd.length==6?new HouseBodies.Body(bd[0],bd[1],bd[2],bd[3],bd[4],bd[5]):null));}
        if(level!=null&&level.isClientSide)syncedAt=level.getGameTime();
    }
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider r){var t=new CompoundTag();saveAdditional(t,r);t.remove("Party");return t;}
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
}

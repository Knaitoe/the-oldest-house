package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import io.github.knaitoe.theoldesthouse.house.TractHouseBlock;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.ChunkWatchEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 0.4.232, the familiar room (docs/FAMILIAR_ROOM_0_4_232.md). The owner, 1 November: "A modest kitchen that comes back through
 * different approaches. Its contents persist: supplies left, chairs arranged. Its ordinary sounds (the fridge, the clock, the tap) are
 * its own, and can be heard through other doors. Over several visits, its window shows passages the reader has already walked, from
 * above. Shared: groups who find it meet there."
 *
 * <p>One kitchen, the same for everyone, dealt like a hall from the third crossing (and far more readily to a reader while someone else
 * is already in it), from whatever hall a reader happens to be in: the same room through different doors. It is built once and kept:
 * what is left in its pantry stays there, and its chairs (the loose chairs of {@link TraceRooms}) stay where they are pulled. Its
 * clock ticks, its fridge hums, its tap drips, and its clock is heard through any door that leads to it ({@link DoorSounds}).
 *
 * <p>The window over the sink looks down into a yard below. The first time, there is only the yard. After that, each time, the yard
 * is laid out (for that reader alone) like the last halls they walked, seen from above at a third of their size, one more each
 * visit up to three.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class FamiliarRoom {
    private FamiliarRoom(){}
    public static final LabyrinthPlace KITCHEN=LabyrinthPlace.FAMILIAR_KITCHEN;
    public static final String STATE="familiar_0232";
    private static final int FLAGS=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    /** The table and its four chairs, the pantry, the sink under the window, the fridge, the clock on the north wall. */
    public static final BlockPos TABLE=new BlockPos(1,0,-8),PANTRY=new BlockPos(-5,0,-3),SINK=new BlockPos(-5,0,-7),FRIDGE=new BlockPos(-5,0,-12),CLOCK=new BlockPos(0,3,-14);
    /** The yard under the window: its ground, and how far it reaches (west of the kitchen's wall, north of its door). */
    public static final int GROUND_Y=-5,YARD_EAST=-8,YARD_WEST=-30,LANES=3,LANE=4,WALKED=12;
    /** A reader's weight for the kitchen, and while somebody else is in it. */
    public static final int WEIGHT=4,MEET=40;

    static BlockState ground(){return Blocks.DEEPSLATE_TILES.defaultBlockState();}
    static BlockState passage(){return Blocks.SMOOTH_STONE.defaultBlockState();}
    static void set(ServerLevel l,BlockPos b,int x,int y,int z,BlockState s){l.setBlock(b.offset(x,y,z),s,FLAGS);}
    static void set(ServerLevel l,BlockPos b,BlockPos at,BlockState s){l.setBlock(b.offset(at),s,FLAGS);}
    static void fill(ServerLevel l,BlockPos b,int x0,int y0,int z0,int x1,int y1,int z1,BlockState s){PuzzleRooms.fill(l,b,x0,y0,z0,x1,y1,z1,s);}
    static BlockState tract(TractHouseBlock.Kind kind,Direction facing){return TractHouseBlock.of(kind,facing);}

    public static void build(ServerLevel l,BlockPos b){
        // Built once and kept: a kitchen that stands already is left as its readers left it.
        if(l.getBlockState(b.offset(PANTRY)).is(Blocks.BARREL))return;
        var box=KITCHEN.room();var paper=TraceRegistry.material("paper").defaultBlockState();var panel=TraceRegistry.material("panel").defaultBlockState();
        var plaster=TraceRegistry.material("plaster").defaultBlockState();var lino=VacantRegistry.material("lino").defaultBlockState();var brick=VacantRegistry.material("brick").defaultBlockState();
        // The whole box: brick round the yard, solid under the kitchen, air above both, a plaster ceiling over all.
        fill(l,b,box.minX(),box.minY(),box.minZ(),box.maxX(),box.maxY(),box.maxZ(),brick);
        fill(l,b,box.minX()+1,GROUND_Y+1,box.minZ()+1,-7,box.maxY()-1,-1,Blocks.AIR.defaultBlockState());
        fill(l,b,box.minX()+1,GROUND_Y,box.minZ()+1,-7,GROUND_Y,-1,ground());
        fill(l,b,box.minX(),box.maxY(),box.minZ(),box.maxX(),box.maxY(),box.maxZ(),plaster);
        // The kitchen: lino floor, panelled to the dado, papered above.
        fill(l,b,-6,-1,box.minZ(),box.maxX(),-1,0,lino);
        for(int x=-6;x<=box.maxX();x++)for(int z=box.minZ();z<=0;z++)for(int y=0;y<=5;y++){
            boolean wall=x==-6||x==box.maxX()||z==box.minZ()||z==0;
            set(l,b,x,y,z,wall?(y<=1?panel:paper):Blocks.AIR.defaultBlockState());
        }
        // The window over the sink, looking down into the yard; light in the yard so the ground can be seen.
        for(int z=-6;z>=-9;z--)for(int y=1;y<=2;y++)set(l,b,-6,y,z,Blocks.GLASS_PANE.defaultBlockState().setValue(IronBarsBlock.NORTH,true).setValue(IronBarsBlock.SOUTH,true));
        for(int x=-28;x<=-10;x+=6)for(int z=-3;z>=-13;z-=5)set(l,b,x,-1,z,Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL,12));
        // The run along the west wall: pantry, counters, the sink under the window, the stove and the fridge; cupboards above.
        set(l,b,PANTRY,Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING,Direction.EAST));
        for(int z:new int[]{-2,-4,-5,-6,-8,-9,-10})set(l,b,-5,0,z,tract(TractHouseBlock.Kind.COUNTER,Direction.EAST));
        set(l,b,SINK,tract(TractHouseBlock.Kind.COUNTER_SINK,Direction.EAST));
        set(l,b,-5,0,-11,tract(TractHouseBlock.Kind.STOVE,Direction.EAST));
        set(l,b,FRIDGE,tract(TractHouseBlock.Kind.FRIDGE_LOWER,Direction.EAST));set(l,b,FRIDGE.above(),tract(TractHouseBlock.Kind.FRIDGE_UPPER,Direction.EAST));
        for(int z:new int[]{-3,-4,-10,-11})set(l,b,-5,3,z,tract(TractHouseBlock.Kind.WALL_CABINET,Direction.EAST));
        // The table, its four chairs (loose: pulled where anyone likes, and left there), and the light over it.
        set(l,b,TABLE,TraceBlock.of(TraceBlock.Kind.TABLE,Direction.NORTH));
        set(l,b,TABLE.south(),TraceBlock.of(TraceBlock.Kind.CHAIR,Direction.NORTH));set(l,b,TABLE.north(),TraceBlock.of(TraceBlock.Kind.CHAIR,Direction.SOUTH));
        set(l,b,TABLE.west(),TraceBlock.of(TraceBlock.Kind.CHAIR,Direction.EAST));set(l,b,TABLE.east(),TraceBlock.of(TraceBlock.Kind.CHAIR,Direction.WEST));
        set(l,b,TABLE.above(5),tract(TractHouseBlock.Kind.PENDANT_LIGHT,Direction.NORTH));
        LabyrinthBuilder.entrance(l,b,paper,lino,plaster);
        LabyrinthBuilder.doors(l,b,KITCHEN);
        stock(l,b);clock(l,b);
    }
    /** What the pantry holds when the kitchen is first built: a little, once. What is taken or left after that is the readers'. */
    static void stock(ServerLevel l,BlockPos b){
        if(l.getBlockEntity(b.offset(PANTRY)) instanceof net.minecraft.world.level.block.entity.BarrelBlockEntity barrel&&barrel.isEmpty()){
            barrel.setItem(0,new ItemStack(Items.BREAD,3));barrel.setItem(4,new ItemStack(Items.APPLE,2));barrel.setItem(13,new ItemStack(Items.CANDLE,1));barrel.setChanged();
        }
    }
    /** The clock on the north wall: an ordinary clock in a frame. Inside the House it does not keep any time. */
    static void clock(ServerLevel l,BlockPos b){
        var at=b.offset(CLOCK);
        if(!l.getEntitiesOfClass(ItemFrame.class,new AABB(at)).isEmpty())return;
        var frame=new ItemFrame(l,at,Direction.SOUTH);frame.setItem(new ItemStack(Items.CLOCK),false);var tag=new net.minecraft.nbt.CompoundTag();frame.saveWithoutId(tag);tag.putBoolean("Fixed",true);tag.putBoolean("Invulnerable",true);frame.load(tag);l.addFreshEntity(frame);
    }

    // ------------------------------------------------------------------ pacing
    private static final Set<UUID> IN=new HashSet<>();
    /** Readers who are hungry or hurt, refreshed with the room's own clock. */
    private static final Set<UUID> NEED=new HashSet<>();
    /** Crossings after a visit before the kitchen can come again, before it is back at its ordinary weight, and before it is twice as likely. */
    public static final int SOON=4,BACK=10,LONG=24;
    /** A reader's weight for the kitchen: from the third crossing, and far more while somebody else is already there. */
    public static int weight(UUID reader,int depth){return weight(null,reader,depth);}
    /**
     * 0.4.332 (owner: "the kitchen should have a weighted frequency for appearance too"): the kitchen comes when it is wanted, not
     * on a flat chance. Not again straight after a visit; back by degrees over the next few crossings; twice as likely once a reader
     * has been a long time away from it; three times as likely while they are hungry or hurt; half as likely in the deep halls,
     * where the House's rooms are further from home. Someone already in it still draws everyone else there.
     */
    public static int weight(@Nullable LabyrinthData data,UUID reader,int depth){
        if(depth<3)return 0;
        for(var other:IN)if(!other.equals(reader))return MEET;
        int w=WEIGHT;
        if(data!=null){var own=own(data,reader);
            if(own.getInt("Visits")>0){int since=own.getInt("Since");
                if(since<SOON)return 0;
                if(since<BACK)w=Math.max(1,WEIGHT*(since-SOON+1)/(BACK-SOON+1));
                else if(since>=LONG)w=WEIGHT*2;}}
        if(NEED.contains(reader))w*=3;
        if(depth>=LabyrinthPacing.DEEP_DEPTH)w=Math.max(1,(w+1)/2);
        return w;
    }
    /** For tests: mark a reader hungry or hurt, or not. */
    public static void need(UUID reader,boolean needs){if(needs)NEED.add(reader);else NEED.remove(reader);}
    public static boolean occupied(){return !IN.isEmpty();}
    /** 0.4.239: the kitchen through its door: its own rhythm (0.4.246: the fridge, the clock, the drip), or, while somebody else is in it, someone moving about. */
    public static DoorSounds.Through heard(UUID listener){
        for(var other:IN)if(!other.equals(listener))return DoorSounds.Through.of(DoorSounds.Carry.SOMEONE);
        return DoorSounds.Through.of(DoorSounds.Carry.KITCHEN);
    }

    // ------------------------------------------------------------------ the walk the window shows
    static net.minecraft.nbt.CompoundTag own(LabyrinthData data,UUID reader){return data.stateEntry(STATE,reader.toString());}
    static void save(LabyrinthData data,UUID reader,net.minecraft.nbt.CompoundTag own){data.setStateEntry(STATE,reader.toString(),own);}
    /** An ordinary hall walked: remembered, latest last, for the window. */
    public static void walked(ServerPlayer p,LabyrinthPlace place){
        if(p.isSpectator())return;
        // Every crossing away from the kitchen counts toward its coming again.
        if(place!=KITCHEN){var data=LabyrinthData.get(p.server);var own=own(data,p.getUUID());own.putInt("Since",Math.min(1000,own.getInt("Since")+1));save(data,p.getUUID(),own);}
        if(!LabyrinthPacing.ordinary(place))return;
        var data=LabyrinthData.get(p.server);var own=own(data,p.getUUID());var list=own.getList("Walked",8);
        if(!list.isEmpty()&&list.getString(list.size()-1).equals(place.id()))return;
        for(int i=list.size()-1;i>=0;i--)if(list.getString(i).equals(place.id()))list.remove(i);
        list.add(StringTag.valueOf(place.id()));while(list.size()>WALKED)list.remove(0);
        own.put("Walked",list);save(data,p.getUUID(),own);
    }
    public static int visits(LabyrinthData data,UUID reader){return own(data,reader).getInt("Visits");}
    /** The halls the window shows a reader now: the last ones they walked, one more each visit after the first, up to three. */
    public static List<LabyrinthPlace> shown(LabyrinthData data,UUID reader){
        var own=own(data,reader);int n=Math.min(LANES,Math.max(0,own.getInt("Visits")-1));var list=own.getList("Walked",8);var out=new ArrayList<LabyrinthPlace>();
        for(int i=list.size()-1;i>=0&&out.size()<n;i--){var p=LabyrinthPlace.byId(list.getString(i));if(p!=null&&p.room()!=null)out.add(p);}
        return out;
    }
    /** Arriving in the kitchen: a visit counted, and the window's yard laid out for this reader. */
    public static void arrived(ServerPlayer p){
        if(p.isSpectator())return;var data=LabyrinthData.get(p.server);var own=own(data,p.getUUID());own.putInt("Visits",own.getInt("Visits")+1);own.putInt("Since",0);save(data,p.getUUID(),own);
        PENDING.put(p.getUUID(),p.server.getTickCount()+10);
    }

    // ------------------------------------------------------------------ the yard, from above
    private static final Map<LabyrinthPlace,boolean[][]> PLANS=new EnumMap<>(LabyrinthPlace.class);
    private static final Map<UUID,Integer> PENDING=new HashMap<>();
    private static final Map<UUID,Integer> SHOWN=new HashMap<>();
    /** A hall's floor seen from above at a third of its size: [along it, from its door][across it, west to east]; true where it can be walked. */
    public static @Nullable boolean[][] plan(ServerLevel l,BlockPos origin,LabyrinthPlace hall){
        var cached=PLANS.get(hall);if(cached!=null)return cached;
        var base=LabyrinthPlaces.base(origin,hall);var box=hall.room();if(base==null||box==null)return null;
        int x0=box.minX()+1,x1=box.maxX()-1,z0=box.maxZ()-1,z1=box.minZ()+1;
        int along=(z0-z1+3)/3,across=(x1-x0+3)/3;var plan=new boolean[along][across];boolean any=false;
        for(int u=0;u<along;u++)for(int v=0;v<across;v++){
            boolean open=false;
            for(int dz=0;dz<3&&!open;dz++)for(int dx=0;dx<3&&!open;dx++){
                int x=x0+3*v+dx,z=z0-3*u-dz;if(x>x1||z<z1)continue;var at=base.offset(x,0,z);
                open=l.getBlockState(at).getCollisionShape(l,at).isEmpty()&&l.getBlockState(at.above()).getCollisionShape(l,at.above()).isEmpty()&&!l.getBlockState(at.below()).getCollisionShape(l,at.below()).isEmpty();
            }
            plan[u][v]=open;any|=open;
        }
        if(!any)return null;
        PLANS.put(hall,plan);return plan;
    }
    /** The yard cells (kitchen-relative, on the ground) laid out like these halls: lane by lane, nearest the window first. */
    public static Set<BlockPos> yard(ServerLevel l,BlockPos origin,List<LabyrinthPlace> halls){
        var out=new HashSet<BlockPos>();
        for(int i=0;i<halls.size()&&i<LANES;i++){
            var plan=plan(l,origin,halls.get(i));if(plan==null)continue;int top=-1-5*i;
            for(int u=0;u<plan.length;u++){
                int x=YARD_EAST-u;if(x<YARD_WEST)break;int across=plan[u].length,skip=Math.max(0,(across-LANE)/2);
                for(int v=skip;v<across&&v-skip<LANE;v++)if(plan[u][v])out.add(new BlockPos(x,GROUND_Y,top-(v-skip)));
            }
        }
        return out;
    }
    /** The yard as this reader sees it from the window: their halls in pale stone on the dark ground; nobody else sees it. */
    static void show(ServerPlayer p,ServerLevel l,BlockPos origin,BlockPos b){
        var cells=yard(l,origin,shown(LabyrinthData.get(p.server),p.getUUID()));
        for(int x=YARD_WEST;x<=YARD_EAST;x++)for(int z=-1;z>=-5*LANES+1;z--){var at=new BlockPos(x,GROUND_Y,z);
            p.connection.send(new ClientboundBlockUpdatePacket(b.offset(at),cells.contains(at)?passage():l.getBlockState(b.offset(at))));}
        SHOWN.put(p.getUUID(),cells.hashCode());
    }

    // ------------------------------------------------------------------ the clock
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        var server=e.getServer();int now=server.getTickCount();var origin=HouseSavedData.get(server).houseOrigin();var l=server.getLevel(HouseDimensions.INTERIOR);
        if(origin==null||l==null)return;
        if(now%10==0){
            IN.clear();for(var p:l.players())if(!p.isSpectator()&&LabyrinthPlaces.placeAt(origin,p.blockPosition())==KITCHEN)IN.add(p.getUUID());
            for(var p:server.getPlayerList().getPlayers()){if(p.getFoodData().getFoodLevel()<=12||p.getHealth()<=p.getMaxHealth()/2)NEED.add(p.getUUID());else NEED.remove(p.getUUID());}
        }
        var b=LabyrinthPlaces.base(origin,KITCHEN);if(b==null||LabyrinthData.get(server).door(KITCHEN.entryDoorId())==null)return;
        if(!PENDING.isEmpty()){var it=PENDING.entrySet().iterator();while(it.hasNext()){var due=it.next();if(due.getValue()>now)continue;it.remove();var p=server.getPlayerList().getPlayer(due.getKey());if(p!=null&&p.serverLevel()==l&&l.isLoaded(b))show(p,l,origin,b);}}
        if(IN.isEmpty()||!l.isLoaded(b))return;
        // Its own sounds: the clock every second, the tap now and then, the fridge's hum.
        if(now%20==0)l.playSound(null,b.offset(CLOCK),StaircaseLeakRegistry.CLOCK.get(),SoundSource.BLOCKS,.35F,1F);
        if(now%70==13)l.playSound(null,b.offset(SINK),StaircaseLeakRegistry.DRIP.get(),SoundSource.BLOCKS,.4F,1F);
        if(now%120==40)l.playSound(null,b.offset(FRIDGE),NovelRegistry.WARD_HUM.get(),SoundSource.BLOCKS,.18F,.7F);
    }
    public static void forget(UUID reader){PENDING.remove(reader);SHOWN.remove(reader);IN.remove(reader);}

    // ------------------------------------------------------------------ keeping the view on the reader's client
    /** Whether a reader stands in the kitchen now. */
    static boolean inKitchen(ServerPlayer p,BlockPos origin){return !p.isSpectator()&&p.level().dimension().equals(HouseDimensions.INTERIOR)&&LabyrinthPlaces.placeAt(origin,p.blockPosition())==KITCHEN;}
    /**
     * The window's yard lives only on the reader's client, so it is lost whenever the client is sent the yard's chunks afresh: on
     * reconnecting, or when the chunk comes back into view. Lay it out again then. This is not a visit: arriving is counted only by
     * walking in (TraceRooms keeps where a reader was across a reconnect for that reason).
     */
    public static void refresh(ServerPlayer p){var origin=HouseSavedData.get(p.server).houseOrigin();if(origin==null||!inKitchen(p,origin))return;PENDING.put(p.getUUID(),p.server.getTickCount()+2);}
    /** Whether a chunk holds any of the yard below the window. */
    static boolean underWindow(BlockPos b,net.minecraft.world.level.ChunkPos chunk){
        int x0=b.getX()+YARD_WEST,x1=b.getX()+YARD_EAST,z0=b.getZ()-5*LANES+1,z1=b.getZ()-1;
        return chunk.getMaxBlockX()>=x0&&chunk.getMinBlockX()<=x1&&chunk.getMaxBlockZ()>=z0&&chunk.getMinBlockZ()<=z1;
    }
    @SubscribeEvent public static void loggedIn(PlayerEvent.PlayerLoggedInEvent e){if(e.getEntity() instanceof ServerPlayer p)refresh(p);}
    @SubscribeEvent public static void loggedOut(PlayerEvent.PlayerLoggedOutEvent e){forget(e.getEntity().getUUID());}
    @SubscribeEvent public static void chunkSent(ChunkWatchEvent.Sent e){
        var p=e.getPlayer();if(!e.getLevel().dimension().equals(HouseDimensions.INTERIOR))return;var origin=HouseSavedData.get(p.server).houseOrigin();if(origin==null)return;
        var b=LabyrinthPlaces.base(origin,KITCHEN);if(b!=null&&underWindow(b,e.getPos()))refresh(p);
    }
    /** For tests: whether a reader's view is waiting to be laid out, and whether it has been. */
    public static boolean pending(UUID reader){return PENDING.containsKey(reader);}
    public static boolean viewed(UUID reader){return SHOWN.containsKey(reader);}
}

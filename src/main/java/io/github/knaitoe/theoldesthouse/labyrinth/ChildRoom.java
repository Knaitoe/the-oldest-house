package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import io.github.knaitoe.theoldesthouse.house.HouseWatchers;
import io.github.knaitoe.theoldesthouse.labyrinth.NurseryBlock.Kind;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * The child's room, rebuilt (0.4.74): a real child's bedroom with its own furniture and toys, a crawlspace under the floor
 * and a basement beneath it.
 *
 * Once a reader has read the bedtime card the room's own exits go, one at a time and only while nobody is looking at them:
 * the two windows, then the two doors. Each takes one of the floor toys up to the ceiling above the bed, and every doll
 * turns to face it. A reader who has personally looked at all four sealed exits, then gone below the bed, through the
 * crawlspace and down into the basement, finds the last account there. The basement ladder comes up through a hatch beside
 * the door the reader came in by, which never goes. Everything shared (the exits, the toys) is shared; everything earned is
 * personal.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class ChildRoom {
    public static final LabyrinthPlace PLACE=LabyrinthPlace.CHILD_ROOM;
    /** Saved-world rebuild checkpoint, keyed by the room's base. */
    public static final String STATE="child_room_0474";
    public static final BlockPos CARD=new BlockPos(8,1,-18),ACCOUNT=new BlockPos(8,-3,-10),HATCH=new BlockPos(4,-1,-4),
            BED_HEAD=new BlockPos(0,1,-23),BED_FOOT=new BlockPos(0,1,-22),GAP=new BlockPos(0,-1,-23),BASEMENT_DROP=new BlockPos(6,-2,-17);
    /** The four exits, in the order they go: two windows, then two doors. */
    public static final List<List<BlockPos>> EXITS=List.of(
            List.of(new BlockPos(-7,2,-24),new BlockPos(-6,2,-24),new BlockPos(-7,3,-24),new BlockPos(-6,3,-24)),
            List.of(new BlockPos(6,2,-24),new BlockPos(7,2,-24),new BlockPos(6,3,-24),new BlockPos(7,3,-24)),
            List.of(new BlockPos(-11,0,-12),new BlockPos(-11,1,-12)),
            List.of(new BlockPos(11,0,-8),new BlockPos(11,1,-8)));
    public static final int ALL=(1<<EXITS.size())-1;
    /** The floor toy each exit takes to the ceiling, where it starts, and where it ends up. */
    public static final List<Kind> PAIRED=List.of(Kind.TEDDY,Kind.DOLL,Kind.BLOCKS,Kind.BALL);
    public static final List<BlockPos> TOY_HOMES=List.of(new BlockPos(-2,0,-22),new BlockPos(-9,1,-20),new BlockPos(3,0,-12),new BlockPos(2,0,-15)),
            CEILING_SLOTS=List.of(new BlockPos(-2,5,-21),new BlockPos(2,5,-21),new BlockPos(-1,5,-19),new BlockPos(1,5,-19));
    /** The crawlspace, from the gap under the bed to the opening into the basement, and its one dead end. */
    public static final List<BlockPos> CRAWLSPACE=List.of(new BlockPos(0,-2,-23),new BlockPos(1,-2,-23),new BlockPos(2,-2,-23),new BlockPos(3,-2,-23),
            new BlockPos(4,-2,-23),new BlockPos(5,-2,-23),new BlockPos(6,-2,-23),new BlockPos(6,-2,-22),new BlockPos(6,-2,-21),new BlockPos(6,-2,-20),
            new BlockPos(6,-2,-19),new BlockPos(6,-2,-18),BASEMENT_DROP,new BlockPos(3,-2,-22),new BlockPos(3,-2,-21),new BlockPos(3,-2,-20));
    /** The toy train's loop of track on the floor, in running order. */
    public static final List<BlockPos> TRACK=List.of(new BlockPos(-7,0,-11),new BlockPos(-6,0,-11),new BlockPos(-5,0,-11),new BlockPos(-4,0,-11),
            new BlockPos(-4,0,-10),new BlockPos(-4,0,-9),new BlockPos(-4,0,-8),new BlockPos(-5,0,-8),new BlockPos(-6,0,-8),new BlockPos(-7,0,-8),
            new BlockPos(-7,0,-9),new BlockPos(-7,0,-10));
    public static final int SEAL_EVERY=120,LOOK=20,LOOK_REACH=14;
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE|Block.UPDATE_SUPPRESS_DROPS;
    private static final String[] SEEN={
            "Where the window was there is only wallpaper. It is cold to touch.",
            "The other window has gone the same way. The paper matches perfectly.",
            "There was a door here. The panelling runs straight across.",
            "The bathroom door is wall now. Not even a seam."};
    private ChildRoom(){}

    public static @Nullable BlockPos base(net.minecraft.server.MinecraftServer s){var origin=HouseSavedData.get(s).houseOrigin();return origin==null?null:LabyrinthPlaces.base(origin,PLACE);}
    /** The wall each sealed cell becomes: panelling to the rail, the rail, then the paper. */
    static BlockState wallAt(int y){return (y<=1?Blocks.BIRCH_PLANKS:y==2?Blocks.STRIPPED_BIRCH_WOOD:LiteraryRegistry.CHILD_WALLPAPER.get()).defaultBlockState();}

    // ------------------------------------------------------------------------------------------------ building
    private static void set(ServerLevel l,BlockPos b,int x,int y,int z,BlockState s){BuildBlocks.set(l,b.offset(x,y,z),s,F);}
    private static void set(ServerLevel l,BlockPos b,BlockPos r,BlockState s){BuildBlocks.set(l,b.offset(r),s,F);}
    private static void fill(ServerLevel l,BlockPos b,int x0,int y0,int z0,int x1,int y1,int z1,BlockState s){for(int x=x0;x<=x1;x++)for(int y=y0;y<=y1;y++)for(int z=z0;z<=z1;z++)set(l,b,x,y,z,s);}
    private static void toy(ServerLevel l,BlockPos b,BlockPos r,Kind k,Direction f){set(l,b,r,NurseryBlock.of(k,f));}
    private static void toy(ServerLevel l,BlockPos b,int x,int y,int z,Kind k,Direction f){set(l,b,x,y,z,NurseryBlock.of(k,f));}
    public static void build(ServerLevel l,BlockPos b){
        var air=Blocks.AIR.defaultBlockState();
        // Earth under the house first, so nothing beneath the floor is open but what is meant to be.
        fill(l,b,-12,-5,-25,12,-1,0,Blocks.DIRT.defaultBlockState());
        // The bedroom: panelled to the rail, papered above it, a pale ceiling.
        fill(l,b,-11,-1,-24,11,-1,0,Blocks.BIRCH_PLANKS.defaultBlockState());fill(l,b,-11,6,-24,11,6,0,Blocks.SMOOTH_QUARTZ.defaultBlockState());
        for(int y=0;y<=5;y++){var wall=wallAt(y);for(int x=-11;x<=11;x++){set(l,b,x,y,-24,wall);set(l,b,x,y,0,wall);}for(int z=-24;z<=0;z++){set(l,b,-11,y,z,wall);set(l,b,11,y,z,wall);}}
        for(int x:new int[]{-11,11})for(int z:new int[]{-24,0})fill(l,b,x,0,z,x,5,z,Blocks.STRIPPED_BIRCH_LOG.defaultBlockState());
        fill(l,b,-10,0,-23,10,5,-1,air);
        // Two windows onto a night that is only dark, and two doors of the room's own.
        for(var w:EXITS.subList(0,2))for(var c:w){set(l,b,c,Blocks.GLASS.defaultBlockState());set(l,b,c.getX(),c.getY(),-25,Blocks.BLACK_CONCRETE.defaultBlockState());}
        for(int x:new int[]{-7,6})set(l,b,x,1,-23,Blocks.BIRCH_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,net.minecraft.world.level.block.state.properties.SlabType.TOP));
        for(int x:new int[]{-6,7})set(l,b,x,1,-23,Blocks.BIRCH_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,net.minecraft.world.level.block.state.properties.SlabType.TOP));
        NovelRooms.door(l,b.offset(-11,0,-12),Direction.EAST,Blocks.BIRCH_DOOR,false);NovelRooms.door(l,b.offset(11,0,-8),Direction.WEST,Blocks.BIRCH_DOOR,false);
        // The bed stands on legs against the north wall. Under it there is room for a child, and a gap in the boards.
        toy(l,b,BED_HEAD,Kind.BED_HEAD,Direction.NORTH);toy(l,b,BED_FOOT,Kind.BED_FOOT,Direction.NORTH);
        set(l,b,GAP,Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.SOUTH));
        toy(l,b,1,0,-23,Kind.NIGHT_LIGHT,Direction.SOUTH);toy(l,b,0,5,-22,Kind.MOBILE,Direction.NORTH);toy(l,b,0,5,-12,Kind.CEILING_LAMP,Direction.NORTH);toy(l,b,-6,5,-19,Kind.CEILING_LAMP,Direction.NORTH);toy(l,b,6,5,-19,Kind.CEILING_LAMP,Direction.NORTH);
        // Desk and chair, the bedtime card left on the desk.
        toy(l,b,8,0,-18,Kind.DESK,Direction.WEST);toy(l,b,7,0,-18,Kind.CHAIR,Direction.EAST);toy(l,b,CARD,Kind.CARD,Direction.WEST);
        toy(l,b,-9,0,-20,Kind.TOY_CHEST,Direction.EAST);toy(l,b,-10,0,-16,Kind.SHELF,Direction.EAST);toy(l,b,-10,0,-15,Kind.SHELF,Direction.EAST);
        toy(l,b,-10,1,-15,Kind.MUSIC_BOX,Direction.EAST);toy(l,b,-10,1,-16,Kind.DOLL,Direction.EAST);toy(l,b,10,0,-13,Kind.WARDROBE_LOW,Direction.WEST);toy(l,b,10,1,-13,Kind.WARDROBE_HIGH,Direction.WEST);
        toy(l,b,-9,0,-6,Kind.DOLLHOUSE,Direction.EAST);toy(l,b,6,0,-7,Kind.ROCKING_HORSE,Direction.WEST);toy(l,b,10,2,-20,Kind.DRAWINGS,Direction.WEST);
        for(int x=-2;x<=2;x++)for(int z=-12;z<=-9;z++)toy(l,b,x,0,z,Kind.RUG,Direction.NORTH);
        // The toys, where a child left them.
        toy(l,b,TOY_HOMES.get(0),Kind.TEDDY,Direction.EAST);toy(l,b,TOY_HOMES.get(1),Kind.DOLL,Direction.EAST);toy(l,b,TOY_HOMES.get(2),Kind.BLOCKS,Direction.NORTH);
        toy(l,b,TOY_HOMES.get(3),Kind.BALL,Direction.NORTH);toy(l,b,-6,0,-14,Kind.JACK_BOX,Direction.SOUTH);toy(l,b,3,0,-8,Kind.TOP,Direction.NORTH);
        for(int i=0;i<TRACK.size();i++){var at=TRACK.get(i);var next=TRACK.get((i+1)%TRACK.size());var f=Direction.getNearest(next.getX()-at.getX(),0,next.getZ()-at.getZ());toy(l,b,at,i==0?Kind.TRAIN:Kind.TRACK,f);}
        // The crawlspace: packed earth underfoot, the underside of the floor overhead, one candle somebody left.
        for(var c:CRAWLSPACE){set(l,b,c,air);set(l,b,c.below(),Blocks.COARSE_DIRT.defaultBlockState());}
        set(l,b,GAP.below(),Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.SOUTH));
        set(l,b,new BlockPos(3,-2,-20),Blocks.CANDLE.defaultBlockState());
        // The basement: brick walls, a stone floor, the boiler, the things that were put away. A child can stand up here.
        fill(l,b,2,-5,-17,11,-2,-3,Blocks.BRICKS.defaultBlockState());fill(l,b,3,-5,-16,10,-5,-4,Blocks.POLISHED_ANDESITE.defaultBlockState());fill(l,b,3,-4,-16,10,-2,-4,air);
        set(l,b,BASEMENT_DROP,air);
        set(l,b,10,-4,-15,Blocks.FURNACE.defaultBlockState().setValue(AbstractFurnaceBlock.FACING,Direction.WEST).setValue(AbstractFurnaceBlock.LIT,true));
        set(l,b,6,-2,-10,Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true));
        toy(l,b,4,-4,-15,Kind.BOXES,Direction.SOUTH);toy(l,b,5,-4,-15,Kind.BOXES,Direction.SOUTH);toy(l,b,4,-3,-15,Kind.BOXES,Direction.EAST);
        toy(l,b,9,-4,-6,Kind.CRIB,Direction.WEST);set(l,b,8,-4,-10,Blocks.CRAFTING_TABLE.defaultBlockState());toy(l,b,ACCOUNT,Kind.ACCOUNT,Direction.WEST);
        set(l,b,3,-3,-10,NurseryBlock.of(Kind.DRAWINGS,Direction.EAST,1));
        // The ladder up, and the hatch that comes out beside the door the reader came in by.
        for(int y=-4;y<=-2;y++)set(l,b,4,y,-4,Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.NORTH));
        set(l,b,HATCH,Blocks.IRON_TRAPDOOR.defaultBlockState().setValue(TrapDoorBlock.HALF,Half.TOP).setValue(TrapDoorBlock.FACING,Direction.NORTH).setValue(TrapDoorBlock.OPEN,false));
        markBuilt(LabyrinthData.get(l.getServer()),b);
    }
    static void markBuilt(LabyrinthData d,BlockPos b){var s=d.state(STATE);s.putBoolean(Long.toString(b.asLong()),true);d.setState(STATE,s);}
    public static boolean built(LabyrinthData d,BlockPos b){return d.state(STATE).getBoolean(Long.toString(b.asLong()));}

    // ------------------------------------------------------------------------------------------------ where a reader is
    private static BlockPos rel(ServerPlayer p,BlockPos b){return BlockPos.containing(p.getX(),p.getY()+.05,p.getZ()).subtract(b);}
    /** Below the bed, or in the crawlspace: the body crawls there. */
    public static boolean crawling(ServerPlayer p,BlockPos b){var r=rel(p,b);return r.getY()==-2&&CRAWLSPACE.contains(r)||r.getY()==-1&&r.equals(GAP)
            ||r.getY()==0&&r.getX()==0&&(r.getZ()==-23||r.getZ()==-22);}
    public static boolean inCrawlspace(ServerPlayer p,BlockPos b){var r=rel(p,b);return (r.getY()==-2||r.getY()==-1)&&(CRAWLSPACE.contains(r.atY(-2)));}
    public static boolean inBasement(ServerPlayer p,BlockPos b){var r=rel(p,b);return r.getY()>=-4&&r.getY()<=-2&&r.getX()>=3&&r.getX()<=10&&r.getZ()>=-16&&r.getZ()<=-4;}

    // ------------------------------------------------------------------------------------------------ the sequence
    /** Called on every arrival: what a reader found when they first came in, which names their outcome. */
    public static void arrive(ServerPlayer p,CompoundTag own){
        if(!own.contains("ExitsAtArrival0474"))own.putInt("ExitsAtArrival0474",LiteraryVignettes.shared(LabyrinthData.get(p.server),PLACE).getInt("Exits0474"));
    }
    /** A reader's scene tick, every five ticks while they are present. */
    public static void tick(ServerPlayer p,BlockPos b,CompoundTag own){
        var l=p.serverLevel();var d=LabyrinthData.get(p.server);var world=LiteraryVignettes.shared(d,PLACE);int mask=world.getInt("Exits0474");
        // The room goes on its own clock once anyone present has read the card; it never goes while it is watched.
        if(own.getBoolean("Read_Source")&&mask!=ALL&&world.getLong("SealTick")!=p.server.getTickCount()){
            world.putLong("SealTick",p.server.getTickCount());int clock=world.getInt("SealClock")+5;
            if(clock>=SEAL_EVERY){int i=Integer.numberOfTrailingZeros(~mask);if(i<EXITS.size()&&seal(l,b,i)){mask|=1<<i;world.putInt("Exits0474",mask);clock=0;}}
            world.putInt("SealClock",clock);LiteraryVignettes.shared(d,PLACE,world);
        }
        // Each reader must see for themselves what has become of every exit.
        int seen=own.getInt("ExitSeen0474");
        if(own.getBoolean("Read_Source"))for(int i=0;i<EXITS.size();i++){
            if((mask&(1<<i))==0||(seen&(1<<i))!=0)continue;var at=centre(b,i);
            if(p.position().distanceTo(at)>LOOK_REACH||!HouseWatchers.sees(p,at))continue;
            int look=own.getInt("ExitLook0474_"+i)+5;own.putInt("ExitLook0474_"+i,look);
            if(look>=LOOK){seen|=1<<i;own.putInt("ExitSeen0474",seen);p.displayClientMessage(Component.literal(SEEN[i]),true);}
        }
        boolean allSeen=mask==ALL&&seen==ALL;
        if(allSeen&&!own.getBoolean("AllSeen0474")){own.putBoolean("AllSeen0474",true);p.displayClientMessage(Component.literal("Every way out of the room is wall now."),false);}
        if(inCrawlspace(p,b)){
            if(!own.getBoolean("UnderFloor0474")){own.putBoolean("UnderFloor0474",true);p.displayClientMessage(Component.literal("Dust, and the underside of the floor."),true);}
            if(allSeen)own.putBoolean("Crawled0474",true);
        }
        if(inBasement(p,b)){
            if(own.getBoolean("Crawled0474")&&!own.getBoolean("StoodUp0474")){own.putBoolean("StoodUp0474",true);p.displayClientMessage(Component.literal("You can stand up here."),true);}
            if(own.getBoolean("Crawled0474"))LiteraryVignettes.ready(p,PLACE,own,own.getInt("ExitsAtArrival0474")==0
                    ?"examined_the_lost_exits_and_crawled_below_the_bed":"examined_the_sealed_thresholds_and_found_the_remaining_crawl");
        }
        // Once the room is shut, something above the bed speaks now and then. Only the reader hears it.
        if(mask==ALL&&own.getInt("Present")%600==300&&!inBasement(p,b)){var at=Vec3.atCenterOf(b.offset(0,5,-21));
            p.connection.send(new ClientboundSoundPacket(LiteraryRegistry.NURSERY_WHISPER,SoundSource.AMBIENT,at.x,at.y,at.z,.55F,.9F+p.getRandom().nextFloat()*.15F,p.getRandom().nextLong()));}
    }
    public static Vec3 centre(BlockPos b,int i){var cells=EXITS.get(i);double x=0,y=0,z=0;for(var c:cells){x+=c.getX()+.5;y+=c.getY()+.5;z+=c.getZ()+.5;}
        return new Vec3(b.getX()+x/cells.size(),b.getY()+y/cells.size(),b.getZ()+z/cells.size());}
    /** Seals one exit if nobody can see it and no body is in it; moves its toy to the ceiling and turns the dolls toward it. */
    public static boolean seal(ServerLevel l,BlockPos b,int i){
        var cells=EXITS.get(i);
        for(var c:cells){var at=b.offset(c);
            if(!l.getEntitiesOfClass(LivingEntity.class,new AABB(at).inflate(.05),e->e.isAlive()&&!e.isSpectator()).isEmpty()||seen(l,Vec3.atCenterOf(at)))return false;}
        for(var c:cells)set(l,b,c,wallAt(c.getY()));
        var at=centre(b,i);l.playSound(null,at.x,at.y,at.z,LiteraryRegistry.NURSERY_EXIT.value(),SoundSource.BLOCKS,.8F,.9F+l.getRandom().nextFloat()*.2F);
        // The paired toy leaves the floor; it is found stuck to the ceiling above the bed.
        var kind=PAIRED.get(i);var found=nearest(l,b,kind,TOY_HOMES.get(i));if(found!=null)l.setBlock(found,Blocks.AIR.defaultBlockState(),F);
        set(l,b,CEILING_SLOTS.get(i),NurseryBlock.of(kind,Direction.SOUTH,NurseryBlock.CEILING));
        for(var doll:all(l,b,Kind.DOLL))if(l.getBlockState(doll).getValue(NurseryBlock.STAGE)!=NurseryBlock.CEILING)
            l.setBlock(doll,l.getBlockState(doll).setValue(NurseryBlock.FACING,Direction.getNearest(at.x-doll.getX()-.5,0,at.z-doll.getZ()-.5)),F);
        return true;
    }
    /** Whether any camera, spectators' included, has the point in view: within a moderate cone and with a clear line. */
    static boolean seen(ServerLevel l,Vec3 point){
        for(var p:l.players()){if(!p.isAlive()||p.isSleeping())continue;var delta=point.subtract(p.getEyePosition());double dist=delta.length();
            if(dist>48||dist>1.5&&p.getLookAngle().dot(delta.normalize())<.5)continue;
            var hit=l.clip(new ClipContext(p.getEyePosition(),point,ClipContext.Block.VISUAL,ClipContext.Fluid.NONE,p));
            if(hit.getType()==HitResult.Type.MISS||hit.getBlockPos().distManhattan(BlockPos.containing(point))<=1)return true;}
        return false;
    }
    /** Every nursery block of a kind still on the floor or furniture of the bedroom. */
    static List<BlockPos> all(ServerLevel l,BlockPos b,Kind kind){var out=new java.util.ArrayList<BlockPos>();
        for(int x=-10;x<=10;x++)for(int y=0;y<=4;y++)for(int z=-23;z<=-1;z++){var at=b.offset(x,y,z);if(NurseryBlock.is(l.getBlockState(at),kind))out.add(at);}return out;}
    static @Nullable BlockPos nearest(ServerLevel l,BlockPos b,Kind kind,BlockPos home){BlockPos best=null;double d=Double.MAX_VALUE;var h=b.offset(home);
        for(var at:all(l,b,kind)){if(l.getBlockState(at).getValue(NurseryBlock.STAGE)==NurseryBlock.CEILING)continue;double e=at.distSqr(h);if(e<d){d=e;best=at;}}return best;}

    // ------------------------------------------------------------------------------------------------ saved worlds
    /**
     * Rebuilds an older child's room once ({@value #STATE}): only when its chunks and entity sections are loaded, no camera
     * is near and nothing living is inside. Readers who finished keep everything; for the others the room's sequence starts
     * over (the card they read stays read). The old ceiling figure is removed.
     */
    private static AABB area(BlockPos b){var r=PLACE.room();return new AABB(Vec3.atLowerCornerOf(b.offset(r.minX()-1,r.minY()-1,r.minZ()-1)),Vec3.atLowerCornerOf(b.offset(r.maxX()+2,r.maxY()+2,r.maxZ()+2)));}
    /** The room's chunks and entity sections are loaded. */
    public static boolean loaded(ServerLevel l,BlockPos b){var area=area(b);
        for(int x=(int)Math.floor(area.minX)>>4;x<=(int)Math.floor(area.maxX)>>4;x++)for(int z=(int)Math.floor(area.minZ)>>4;z<=(int)Math.floor(area.maxZ)>>4;z++)
            if(!l.hasChunk(x,z)||!l.areEntitiesLoaded(ChunkPos.asLong(x,z)))return false;
        return true;
    }
    public static boolean rebuild(ServerLevel l,BlockPos b){
        var d=LabyrinthData.get(l.getServer());if(built(d,b))return true;var area=area(b);if(!loaded(l,b))return false;
        if(l.players().stream().anyMatch(p->area.inflate(16).intersects(p.getCamera().getBoundingBox())))return false;
        if(!l.getEntitiesOfClass(LivingEntity.class,area,e->e.isAlive()&&!e.isSpectator()&&!(e instanceof LiteraryActor)).isEmpty())return false;
        var world=LiteraryVignettes.shared(d,PLACE);
        if(world.hasUUID("CeilingToy")&&l.getEntity(world.getUUID("CeilingToy")) instanceof LiteraryActor old)old.discard();
        for(var actor:l.getEntitiesOfClass(LiteraryActor.class,area))actor.discard();
        LiteraryRooms.build(l,b,PLACE);
        world=LiteraryVignettes.shared(d,PLACE);world.remove("CeilingToy");world.remove("LostExits");world.putInt("Exits0474",0);world.putInt("SealClock",0);LiteraryVignettes.shared(d,PLACE,world);
        for(var key:d.stateKeys(LiteraryVignettes.stateId(PLACE))){java.util.UUID id;try{id=java.util.UUID.fromString(key);}catch(IllegalArgumentException notReader){continue;}var own=LiteraryVignettes.personal(d,id,PLACE);
            if(own.getBoolean("Completed"))continue;
            for(var k:List.of("Ready","Outcome","ExaminedLostExits","CrawledUnderBed","ExitSeen0474","Crawled0474","AllSeen0474","StoodUp0474","UnderFloor0474","ExitsAtArrival0474"))own.remove(k);
            for(int i=0;i<5;i++){own.remove("ExitExamineTicks"+i);own.remove("ExitLook0474_"+i);}
            LiteraryVignettes.save(d,id,PLACE,own);}
        markBuilt(d,b);
        return true;
    }
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        var s=e.getServer();if(s instanceof net.minecraft.gametest.framework.GameTestServer||s.getTickCount()%40!=19||LabyrinthBuilder.isCarving())return;
        var data=LabyrinthData.get(s);var b=base(s);if(b==null||data.door(PLACE.entryDoorId())==null||!LabyrinthBuilder.isPlaceReady(data,PLACE)||built(data,b))return;
        var l=s.getLevel(NovelRooms.dimension(PLACE));if(l!=null)rebuild(l,b);
    }
}

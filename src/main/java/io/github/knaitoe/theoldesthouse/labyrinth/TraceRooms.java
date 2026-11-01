package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import io.github.knaitoe.theoldesthouse.house.HouseWatchers;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 0.4.231, traces and the door wedge (docs/TRACES_0_4_231.md). The owner, 1 November: "Dust keeps footprints; a chair stays where it
 * was dragged; a wedge holds a door where it was left. Readers can know a room by what they did in it." And, for the lighter
 * puzzles: "a door wedge becomes a way to hold an unstable route open."
 *
 * <p>The shut-up rooms (DUST_HALL) are an ordinary hall under dust: walking through it leaves prints pointing the way the reader went,
 * and its one loose chair, pulled a block at a time, scores the dust where it is dragged. Nothing tidies either away. The cloakroom
 * (PROPPED_DOOR, early, family HOLD) has an inner door on a closer that shuts itself, a long passage, and a far door that gives only
 * while the way behind stands open; the wedge on the hook holds it. The sprung rooms (SPRUNG_ROOMS, deep) have two such doors, held at
 * once by the wedge and by the chair dragged into a doorway. Wedges themselves (in these doors and in the House's own) are
 * {@link Wedges}.
 *
 * <p>Rarely, and only after many dependable returns (arriving to find one's own prints, chair or wedge as they were left), the House
 * adds one thing while nobody is there to see: a second set of prints beside the reader's own, or, if they drew the chair up to a
 * table, a second chair drawn up across it. Once to a place, for each reader. Most traces stay exactly as left.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class TraceRooms {
    private TraceRooms(){}
    public static final LabyrinthPlace DUST=LabyrinthPlace.DUST_HALL,CLOAK=LabyrinthPlace.PROPPED_DOOR,SPRUNG=LabyrinthPlace.SPRUNG_ROOMS;
    public static final String STATE="traces_0231";
    private static final int FLAGS=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    /** A door on a closer swings shut this long after it was last held open. */
    public static final int SPRING_TICKS=30;
    /** Dependable returns before the House adds anything, the chance on each later departure, and how many cells of a reader's path are kept. */
    public static final int MANY=6,CHANCE=4,PATH=48;
    /** Tests only: the next eligible departure always adds its trace. */
    public static boolean always;

    // ------------------------------------------------------------------ the shut-up rooms
    /** The table in the shut-up rooms, the loose chair's first place, the dust sheets. */
    public static final BlockPos DUST_TABLE=new BlockPos(-1,0,-24),DUST_CHAIR=new BlockPos(1,0,-12);
    static final BlockPos[] DUST_SHEETS={new BlockPos(-3,0,-5),new BlockPos(-3,0,-6),new BlockPos(3,0,-9),new BlockPos(-3,0,-13),new BlockPos(3,0,-14),new BlockPos(3,1,-14),new BlockPos(3,0,-27),new BlockPos(3,0,-28),new BlockPos(-3,0,-31)};

    // ------------------------------------------------------------------ the cloakroom
    /** The wedge's hook, the inner door on its closer, the passage's turn, and the cell in front of the far door. */
    public static final BlockPos CLOAK_HOOK=new BlockPos(-5,1,-3),CLOAK_DOOR=new BlockPos(-3,0,-9),CLOAK_TURN=new BlockPos(-3,0,-21),CLOAK_FRONT=new BlockPos(4,0,-21);

    // ------------------------------------------------------------------ the sprung rooms
    /** The two doors on closers, the loose chair's first place, the hook, the table in the last room, and the cell in front of the far door. */
    public static final BlockPos SPRUNG_A=new BlockPos(0,0,-9),SPRUNG_B=new BlockPos(-4,0,-19),SPRUNG_CHAIR=new BlockPos(5,0,-4),SPRUNG_HOOK=new BlockPos(-7,1,-3),SPRUNG_TABLE=new BlockPos(3,0,-24),SPRUNG_FRONT=new BlockPos(0,0,-29);

    static BlockState mat(String name){return TraceRegistry.material(name).defaultBlockState();}
    static void set(ServerLevel l,BlockPos b,BlockPos at,BlockState s){l.setBlock(b.offset(at),s,FLAGS);}
    static void set(ServerLevel l,BlockPos b,int x,int y,int z,BlockState s){l.setBlock(b.offset(x,y,z),s,FLAGS);}
    static void hang(ServerLevel l,BlockPos b,int x,int y,int z){set(l,b,x,y,z,Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true));}
    static void springDoor(ServerLevel l,BlockPos b,BlockPos at,Direction facing){
        var s=Blocks.SPRUCE_DOOR.defaultBlockState().setValue(DoorBlock.FACING,facing).setValue(DoorBlock.HINGE,DoorHingeSide.LEFT).setValue(DoorBlock.OPEN,false);
        set(l,b,at,s.setValue(DoorBlock.HALF,DoubleBlockHalf.LOWER));set(l,b,at.above(),s.setValue(DoorBlock.HALF,DoubleBlockHalf.UPPER));
        set(l,b,at.above(2).relative(facing),TraceBlock.of(TraceBlock.Kind.CLOSER,facing));
    }
    /** The room's shell: panelled to the dado, papered above, boards underfoot, plaster over. */
    static void shell(ServerLevel l,BlockPos b,LabyrinthPlace place){
        var box=place.room();var paper=mat("paper");var panel=mat("panel");var boards=mat("boards");var plaster=mat("plaster");
        for(int x=box.minX();x<=box.maxX();x++)for(int z=box.minZ();z<=box.maxZ();z++)for(int y=box.minY();y<=box.maxY();y++){
            boolean inside=x>box.minX()&&x<box.maxX()&&z>box.minZ()&&z<box.maxZ();
            l.setBlock(b.offset(x,y,z),!inside&&y>box.minY()&&y<box.maxY()?(y<=1?panel:paper):y==box.minY()?boards:y==box.maxY()?plaster:Blocks.AIR.defaultBlockState(),FLAGS);
        }
    }
    static void wall(ServerLevel l,BlockPos b,int x0,int x1,int z){for(int x=x0;x<=x1;x++)for(int y=0;y<=5;y++)set(l,b,x,y,z,y<=1?mat("panel"):mat("paper"));}
    /** Dust over every bare floor cell in a box. */
    static void dust(ServerLevel l,BlockPos b,int x0,int z0,int x1,int z1){
        for(int x=Math.min(x0,x1);x<=Math.max(x0,x1);x++)for(int z=Math.min(z0,z1);z<=Math.max(z0,z1);z++)
            if(l.getBlockState(b.offset(x,0,z)).isAir()&&l.getBlockState(b.offset(x,-1,z)).isSolid())set(l,b,x,0,z,TraceBlock.dust());
    }

    public static void buildDust(ServerLevel l,BlockPos b){
        shell(l,b,DUST);
        for(var at:DUST_SHEETS)set(l,b,at,TraceBlock.of(TraceBlock.Kind.SHEET,Direction.NORTH));
        set(l,b,DUST_TABLE,TraceBlock.of(TraceBlock.Kind.TABLE,Direction.NORTH));
        set(l,b,DUST_CHAIR,TraceBlock.of(TraceBlock.Kind.CHAIR,Direction.EAST,1));
        for(var at:List.of(new BlockPos(-3,5,-1),new BlockPos(3,5,-18),new BlockPos(-3,5,-35)))set(l,b,at,Blocks.COBWEB.defaultBlockState());
        hang(l,b,0,5,-9);hang(l,b,0,5,-28);
        LabyrinthBuilder.entrance(l,b,mat("paper"),mat("boards"),mat("plaster"));
        LabyrinthBuilder.doors(l,b,DUST);
        dust(l,b,-3,-1,3,-35);
    }
    public static void buildCloak(ServerLevel l,BlockPos b){
        shell(l,b,CLOAK);boolean solved=PuzzleRooms.solved(l.getServer(),CLOAK);
        // The cloakroom: the hook with the wedge, a hat shelf, a bench along the east wall, a pot for umbrellas.
        set(l,b,CLOAK_HOOK,TraceBlock.of(TraceBlock.Kind.HOOK,Direction.EAST));
        for(int z=-4;z>=-7;z--)set(l,b,-5,2,z,Blocks.SPRUCE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,SlabType.TOP));
        for(int z=-3;z>=-6;z--)set(l,b,5,0,z,Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.EAST));
        set(l,b,5,0,-8,Blocks.DECORATED_POT.defaultBlockState());
        hang(l,b,0,5,-5);
        // The partition and its door on a closer; behind it, solid but for the passage, which turns once and runs to the far door.
        wall(l,b,-5,5,-9);
        PuzzleRooms.fill(l,b,-5,0,-21,5,5,-10,mat("plaster"));
        PuzzleRooms.fill(l,b,-3,0,-21,-3,2,-10,Blocks.AIR.defaultBlockState());
        PuzzleRooms.fill(l,b,-3,0,-21,4,2,-21,Blocks.AIR.defaultBlockState());
        hang(l,b,-3,2,-15);hang(l,b,1,2,-21);
        springDoor(l,b,CLOAK_DOOR,Direction.SOUTH);
        LabyrinthBuilder.entrance(l,b,mat("paper"),mat("boards"),mat("plaster"));
        LabyrinthBuilder.doors(l,b,CLOAK);
        PuzzleRooms.ironDoor(l,b,CLOAK,solved);
    }
    public static void buildSprung(ServerLevel l,BlockPos b){
        shell(l,b,SPRUNG);boolean solved=PuzzleRooms.solved(l.getServer(),SPRUNG);
        wall(l,b,-7,7,-9);wall(l,b,-7,7,-19);
        set(l,b,SPRUNG_HOOK,TraceBlock.of(TraceBlock.Kind.HOOK,Direction.EAST));
        for(var at:List.of(new BlockPos(-7,0,-7),new BlockPos(-6,0,-7),new BlockPos(6,0,-12),new BlockPos(6,0,-13),new BlockPos(-7,0,-16),new BlockPos(5,0,-26)))set(l,b,at,TraceBlock.of(TraceBlock.Kind.SHEET,Direction.NORTH));
        set(l,b,SPRUNG_TABLE,TraceBlock.of(TraceBlock.Kind.TABLE,Direction.NORTH));
        set(l,b,SPRUNG_CHAIR,TraceBlock.of(TraceBlock.Kind.CHAIR,Direction.WEST,1));
        hang(l,b,0,5,-4);hang(l,b,0,5,-14);hang(l,b,0,5,-25);
        springDoor(l,b,SPRUNG_A,Direction.SOUTH);springDoor(l,b,SPRUNG_B,Direction.SOUTH);
        LabyrinthBuilder.entrance(l,b,mat("paper"),mat("boards"),mat("plaster"));
        LabyrinthBuilder.doors(l,b,SPRUNG);
        PuzzleRooms.ironDoor(l,b,SPRUNG,solved);
        dust(l,b,-7,-1,7,-29);
    }

    // ------------------------------------------------------------------ dust
    /** A reader's foot in the dust: prints pointing the way they went, kept on their path. */
    public static void step(ServerPlayer p,BlockPos pos,BlockState s){
        if(p.isSpectator()||!p.isAlive())return;
        var v=p.getDeltaMovement();double dx=p.getX()-p.xo,dz=p.getZ()-p.zo;
        if(dx*dx+dz*dz<1.0E-4&&v.x*v.x+v.z*v.z<1.0E-4)return;
        var facing=Direction.fromYRot(Math.toDegrees(Math.atan2(-(dx==0&&dz==0?v.x:dx),dx==0&&dz==0?v.z:dz)));
        int stage=s.getValue(TraceBlock.STAGE);int next=stage==TraceBlock.OTHER||stage==TraceBlock.BOTH?TraceBlock.BOTH:TraceBlock.PRINTS;
        if(stage==next&&s.getValue(TraceBlock.FACING)==facing)return;
        var l=p.serverLevel();l.setBlock(pos,s.setValue(TraceBlock.STAGE,next).setValue(TraceBlock.FACING,facing),FLAGS);
        var place=placeOf(p.server,l,pos);if(place!=null)remember(p,place,pos);
    }
    static void remember(ServerPlayer p,LabyrinthPlace place,BlockPos pos){
        var data=LabyrinthData.get(p.server);var own=own(data,p.getUUID());var prints=own.getCompound("Prints");
        var path=new ArrayList<Long>();for(long at:prints.getLongArray(place.id()))if(at!=pos.asLong())path.add(at);
        path.add(pos.asLong());while(path.size()>PATH)path.remove(0);
        prints.put(place.id(),new LongArrayTag(path));own.put("Prints",prints);save(data,p.getUUID(),own);
    }

    // ------------------------------------------------------------------ the loose chair
    /** A hand on the loose chair: pulled a block toward the reader, scoring the dust it leaves; turned, crouching. */
    public static boolean chair(ServerPlayer p,BlockPos pos,BlockState s){
        if(p.isSpectator())return false;var l=p.serverLevel();
        if(p.isShiftKeyDown()){
            l.setBlock(pos,s.setValue(TraceBlock.FACING,s.getValue(TraceBlock.FACING).getClockWise()),FLAGS);
            l.playSound(null,pos,SoundEvents.WOOD_HIT,SoundSource.BLOCKS,.7F,.8F);return true;
        }
        double dx=p.getX()-(pos.getX()+.5),dz=p.getZ()-(pos.getZ()+.5);
        var toward=Math.abs(dx)>=Math.abs(dz)?(dx>0?Direction.EAST:Direction.WEST):(dz>0?Direction.SOUTH:Direction.NORTH);
        var to=pos.relative(toward);var there=l.getBlockState(to);
        boolean floor=(there.isAir()||TraceBlock.is(there,TraceBlock.Kind.DUST))&&l.getBlockState(to.below()).isFaceSturdy(l,to.below(),Direction.UP)&&l.getBlockState(to.above()).isAir();
        if(!floor){PuzzleRooms.say(p,"It won't go that way.");return true;}
        if(!l.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class,new AABB(to).deflate(.05)).isEmpty()){PuzzleRooms.say(p,"There is no room to pull it to.");return true;}
        boolean dusty=s.getValue(TraceBlock.STAGE)==1;
        l.setBlock(pos,dusty?TraceBlock.of(TraceBlock.Kind.DUST,toward,TraceBlock.DRAGGED):Blocks.AIR.defaultBlockState(),FLAGS);
        l.setBlock(to,s.setValue(TraceBlock.STAGE,TraceBlock.is(there,TraceBlock.Kind.DUST)?1:0),FLAGS);
        l.playSound(null,to,SoundEvents.WOOD_STEP,SoundSource.BLOCKS,1F,.55F);l.playSound(null,to,SoundEvents.WOOD_PLACE,SoundSource.BLOCKS,.5F,.6F);
        var place=placeOf(p.server,l,to);
        if(place!=null){
            var data=LabyrinthData.get(p.server);var own=own(data,p.getUUID());var chairs=own.getCompound("Chairs");
            var at=new CompoundTag();at.putLong("At",to.asLong());var table=tableFacing(l,to,s.getValue(TraceBlock.FACING));if(table!=null)at.putLong("Table",table.asLong());
            chairs.put(place.id(),at);own.put("Chairs",chairs);save(data,p.getUUID(),own);
        }
        return true;
    }
    /** The table a chair at {@code at}, facing {@code facing}, is drawn up to, if any. */
    static @Nullable BlockPos tableFacing(ServerLevel l,BlockPos at,Direction facing){var t=at.relative(facing);return TraceBlock.is(l.getBlockState(t),TraceBlock.Kind.TABLE)?t:null;}
    /** Whether a loose chair stands in a doorway's way: in the cell on either side of it. */
    static boolean chairIn(ServerLevel l,BlockPos door,Direction facing){return TraceBlock.is(l.getBlockState(door.relative(facing)),TraceBlock.Kind.CHAIR)||TraceBlock.is(l.getBlockState(door.relative(facing.getOpposite())),TraceBlock.Kind.CHAIR);}

    // ------------------------------------------------------------------ the hook
    /** The wedge on the hook: one to a reader, ever. */
    public static boolean hook(ServerPlayer p,BlockPos pos){
        if(p.isSpectator())return false;var data=LabyrinthData.get(p.server);var own=own(data,p.getUUID());
        if(own.getBoolean("Wedge")){PuzzleRooms.say(p,"There is only the one wedge on the hook, and it is the one you took.");return true;}
        own.putBoolean("Wedge",true);save(data,p.getUUID(),own);
        give(p,new ItemStack(TraceRegistry.WEDGE.get()));
        p.serverLevel().playSound(null,pos,SoundEvents.WOOD_HIT,SoundSource.PLAYERS,.6F,1.2F);
        PuzzleRooms.say(p,"A wooden wedge, worn smooth along one side where a door has stood on it.");
        return true;
    }
    static void give(ServerPlayer p,ItemStack stack){if(!p.getInventory().add(stack))p.drop(stack,false);}
    public static boolean tookWedge(LabyrinthData data,UUID reader){return own(data,reader).getBoolean("Wedge");}

    // ------------------------------------------------------------------ doors on closers
    private static final Map<Long,Long> OPENED=new HashMap<>();
    /** The doors on closers, in a room's own coordinates. */
    static List<BlockPos> springs(LabyrinthPlace place){return place==CLOAK?List.of(CLOAK_DOOR):place==SPRUNG?List.of(SPRUNG_A,SPRUNG_B):List.of();}
    public static boolean spring(ServerLevel l,BlockPos lower){
        var origin=HouseSavedData.get(l.getServer()).houseOrigin();if(origin==null||!l.dimension().equals(HouseDimensions.INTERIOR))return false;
        var place=LabyrinthPlaces.placeAt(origin,lower);if(place!=CLOAK&&place!=SPRUNG)return false;var b=LabyrinthPlaces.base(origin,place);
        for(var at:springs(place))if(b.offset(at).equals(lower))return true;return false;
    }
    /** Whether a door on a closer is held open: wedged, a chair in its way, or someone standing in it. */
    public static boolean held(ServerLevel l,BlockPos lower){
        var s=l.getBlockState(lower);if(!(s.getBlock() instanceof DoorBlock)||!s.getValue(DoorBlock.OPEN))return false;
        if(Wedges.at(l,lower)||chairIn(l,lower,s.getValue(DoorBlock.FACING)))return true;
        return !l.getEntitiesOfClass(Player.class,new AABB(lower).expandTowards(0,1,0),e->!e.isSpectator()).isEmpty();
    }
    static boolean open(ServerLevel l,BlockPos lower){var s=l.getBlockState(lower);return s.getBlock() instanceof DoorBlock&&s.getValue(DoorBlock.OPEN);}
    static void shut(ServerLevel l,BlockPos lower){
        for(var half:new BlockPos[]{lower,lower.above()}){var s=l.getBlockState(half);if(s.getBlock() instanceof DoorBlock)l.setBlock(half,s.setValue(DoorBlock.OPEN,false),Block.UPDATE_ALL);}
        l.playSound(null,lower,SoundEvents.WOODEN_DOOR_CLOSE,SoundSource.BLOCKS,1F,.8F);l.playSound(null,lower,SoundEvents.WOODEN_TRAPDOOR_CLOSE,SoundSource.BLOCKS,.5F,.55F);
    }
    /** One room's closers and far door, at {@code now}: a door left open and unheld swings shut; the far door gives while every one stands open. */
    public static void springs(ServerLevel l,BlockPos b,LabyrinthPlace place,long now){
        boolean allOpen=true;
        for(var at:springs(place)){
            var lower=b.offset(at);
            if(!open(l,lower)){OPENED.remove(lower.asLong());allOpen=false;continue;}
            if(held(l,lower)){OPENED.put(lower.asLong(),now);continue;}
            long since=OPENED.computeIfAbsent(lower.asLong(),k->now);
            if(now-since>=SPRING_TICKS){shut(l,lower);OPENED.remove(lower.asLong());allOpen=false;}
        }
        if(!allOpen||PuzzleRooms.solved(l.getServer(),place))return;
        var front=b.offset(place==CLOAK?CLOAK_FRONT:SPRUNG_FRONT);
        for(var p:l.players())if(!p.isSpectator()&&p.isAlive()&&p.position().distanceToSqr(front.getBottomCenter())<=1.6*1.6){
            PuzzleRooms.ironDoor(l,b,place,true);l.playSound(null,front,SoundEvents.IRON_DOOR_OPEN,SoundSource.BLOCKS,1F,.9F);
            PuzzleRooms.solve(l,place,place==CLOAK?"The far door gives. Behind you, the way you came is still standing open."
                    :"The far door gives. Both doors behind you are standing open, all the way back.");
            return;
        }
    }

    // ------------------------------------------------------------------ the clock
    private static final Map<UUID,LabyrinthPlace> HERE=new HashMap<>();
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        var server=e.getServer();long now=server.getTickCount();var origin=HouseSavedData.get(server).houseOrigin();var l=server.getLevel(HouseDimensions.INTERIOR);
        if(origin==null||l==null)return;
        if(now%5==0)for(var place:new LabyrinthPlace[]{CLOAK,SPRUNG}){
            if(LabyrinthData.get(server).door(place.entryDoorId())==null)continue;
            var b=LabyrinthPlaces.base(origin,place);if(b==null||!l.isLoaded(b))continue;
            boolean someone=false;for(var p:l.players())if(PuzzleRooms.at(p)==place){someone=true;break;}
            if(someone||!OPENED.isEmpty())springs(l,b,place,now);
        }
        if(now%20==3)for(var p:server.getPlayerList().getPlayers())moved(l,p,origin);
    }
    static @Nullable LabyrinthPlace placeOf(net.minecraft.server.MinecraftServer server,ServerLevel l,BlockPos pos){
        var origin=HouseSavedData.get(server).houseOrigin();if(origin==null||!l.dimension().equals(HouseDimensions.INTERIOR))return null;return LabyrinthPlaces.placeAt(origin,pos);
    }
    /** Where each reader is, so arriving at their traces and leaving them are both noticed. */
    static void moved(ServerLevel l,ServerPlayer p,BlockPos origin){
        if(p.isSpectator())return;
        var now=p.level().dimension().equals(HouseDimensions.INTERIOR)?LabyrinthPlaces.placeAt(origin,p.blockPosition()):null;
        var before=HERE.get(p.getUUID());if(before==now)return;
        if(now==null)HERE.remove(p.getUUID());else HERE.put(p.getUUID(),now);
        if(before!=null)departed(l,p,before);
        if(now!=null)arrived(p,now);
    }
    public static void forget(UUID reader){HERE.remove(reader);}

    // ------------------------------------------------------------------ dependable returns, and the rare exception
    static CompoundTag own(LabyrinthData data,UUID reader){return data.stateEntry(STATE,reader.toString());}
    static void save(LabyrinthData data,UUID reader,CompoundTag own){data.setStateEntry(STATE,reader.toString(),own);}
    public static int returns(LabyrinthData data,UUID reader){return own(data,reader).getInt("Returns");}
    /** How many cells of a reader's path through a place's dust are kept. */
    public static int pathLength(LabyrinthData data,UUID reader,LabyrinthPlace place){return own(data,reader).getCompound("Prints").getLongArray(place.id()).length;}
    /** Whether the reader last left a chair drawn up to a table in this place. */
    public static boolean drawnUp(LabyrinthData data,UUID reader,LabyrinthPlace place){return own(data,reader).getCompound("Chairs").getCompound(place.id()).contains("Table");}
    /** Whether the House has added its one thing for this reader in this place. */
    public static boolean added(LabyrinthData data,UUID reader,LabyrinthPlace place){return own(data,reader).getCompound("Second").getBoolean(place.id());}
    /** Arriving somewhere the reader left a trace: if it is as they left it, that is a dependable return. */
    public static void arrived(ServerPlayer p,LabyrinthPlace place){
        var l=p.serverLevel();var data=LabyrinthData.get(p.server);var own=own(data,p.getUUID());
        if(dependable(l,data,p.getUUID(),place,own)){own.putInt("Returns",own.getInt("Returns")+1);save(data,p.getUUID(),own);}
    }
    static boolean dependable(ServerLevel l,LabyrinthData data,UUID reader,LabyrinthPlace place,CompoundTag own){
        var path=own.getCompound("Prints").getLongArray(place.id());
        if(path.length>=4){int kept=0;for(long at:path){var s=l.getBlockState(BlockPos.of(at));if(TraceBlock.is(s,TraceBlock.Kind.DUST)&&(s.getValue(TraceBlock.STAGE)==TraceBlock.PRINTS||s.getValue(TraceBlock.STAGE)==TraceBlock.BOTH))kept++;}if(kept*2>=path.length)return true;}
        var chair=own.getCompound("Chairs").getCompound(place.id());
        if(chair.contains("At")&&TraceBlock.is(l.getBlockState(BlockPos.of(chair.getLong("At"))),TraceBlock.Kind.CHAIR))return true;
        return Wedges.ownedIn(data,reader,place);
    }
    /** Leaving a place: after many dependable returns, now and then, and only with nobody there to see, one thing more. */
    public static void departed(ServerLevel l,ServerPlayer p,LabyrinthPlace place){
        var data=LabyrinthData.get(p.server);var own=own(data,p.getUUID());
        if(own.getInt("Returns")<MANY||own.getCompound("Second").getBoolean(place.id()))return;
        if(!always&&l.random.nextInt(CHANCE)!=0)return;
        var origin=HouseSavedData.get(p.server).houseOrigin();if(origin==null)return;
        for(var other:l.players())if(other!=p&&LabyrinthPlaces.placeAt(origin,other.blockPosition())==place)return;
        if(second(l,own,place)){var second=own.getCompound("Second");second.putBoolean(place.id(),true);own.put("Second",second);save(data,p.getUUID(),own);}
    }
    /** The one thing more: a second chair drawn up across the reader's table, or a second set of prints beside theirs. */
    static boolean second(ServerLevel l,CompoundTag own,LabyrinthPlace place){
        var chair=own.getCompound("Chairs").getCompound(place.id());
        if(chair.contains("Table")){
            var at=BlockPos.of(chair.getLong("At"));var table=BlockPos.of(chair.getLong("Table"));var s=l.getBlockState(at);
            if(TraceBlock.is(s,TraceBlock.Kind.CHAIR)&&TraceBlock.is(l.getBlockState(table),TraceBlock.Kind.TABLE)){
                var facing=s.getValue(TraceBlock.FACING);var across=table.relative(facing);var there=l.getBlockState(across);
                if((there.isAir()||TraceBlock.is(there,TraceBlock.Kind.DUST))&&!HouseWatchers.isWatched(l,across)){
                    l.setBlock(across,TraceBlock.of(TraceBlock.Kind.CHAIR,facing.getOpposite(),TraceBlock.is(there,TraceBlock.Kind.DUST)?1:0),FLAGS);return true;
                }
            }
        }
        var path=own.getCompound("Prints").getLongArray(place.id());if(path.length<4)return false;
        int laid=0;
        for(long at:path){
            var pos=BlockPos.of(at);var s=l.getBlockState(pos);if(!TraceBlock.is(s,TraceBlock.Kind.DUST))continue;int stage=s.getValue(TraceBlock.STAGE);
            if(stage!=TraceBlock.PRINTS&&stage!=TraceBlock.BOTH)continue;var facing=s.getValue(TraceBlock.FACING);var beside=pos.relative(facing.getClockWise());var b=l.getBlockState(beside);
            if(!TraceBlock.is(b,TraceBlock.Kind.DUST)||HouseWatchers.isWatched(l,beside))continue;int was=b.getValue(TraceBlock.STAGE);
            int next=was==TraceBlock.PRINTS||was==TraceBlock.BOTH?TraceBlock.BOTH:TraceBlock.OTHER;
            l.setBlock(beside,b.setValue(TraceBlock.STAGE,next).setValue(TraceBlock.FACING,facing),FLAGS);laid++;
        }
        return laid>0;
    }
}

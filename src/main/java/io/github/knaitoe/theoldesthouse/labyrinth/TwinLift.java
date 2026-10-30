package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseInternalTeleport;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 0.4.199, the Service Lift's deep room: the twin lifts. The owner: "Two lifts share a counterweight. Raising one lowers the
 * other, changing which floors connect. The player uses both to reach a door they could see from the beginning."
 *
 * A tall brick lift hall on three floors. From the door you see, high up across the hall, the far door at the end of a
 * gallery. Two goods lifts stand in open brick shafts, one each side, hung from a single counterweight: whatever one rises,
 * the other sinks, and the two always stand two floors apart in all. The west lift opens onto the ground floor and the
 * middle walkway; the east lift onto the middle walkway and the top gallery. Each lift has a pale cord to go up and a dark
 * cord to go down; each landing has a brass-handled cord that calls its lift a floor toward it. Ride the west lift to the
 * walkway, and the east lift has come down to meet you there; ride it up, and the west lift goes back to the ground.
 *
 * Nothing can be stranded: a lift never comes down onto someone below it; the bottom of each shaft holds water; the west
 * shaft's bottom opens onto the floor, and the east shaft has a ladder in a slot in its back wall up to the walkway's
 * height, with its own call cord beside it. Once anyone has stood on the top gallery, ladders are let down from the walkway
 * and the gallery, so the room is quick to cross again.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class TwinLift {
    public static final LabyrinthPlace PLACE=LabyrinthPlace.TWIN_LIFT;
    public static final String STATE="twin_lift_0199";
    /** The west lift (A) and the east lift (B): the near corner of each shaft's three-by-three well. */
    public static final int AX=-6,BX=4,SZ=-11,FLOOR=5,TICKS=3;
    /** The call cords: the ground floor's for the west lift, the walkway's for each lift, the gallery's, and the east shaft's own. */
    public static final BlockPos[] CALLS={new BlockPos(AX+3,1,SZ+4),new BlockPos(-2,6,SZ-1),new BlockPos(2,6,SZ-1),new BlockPos(3,11,-13),new BlockPos(BX,0,SZ-1)};
    static final Direction[] CALL_WALLS={Direction.NORTH,Direction.WEST,Direction.EAST,Direction.SOUTH,Direction.NORTH};
    /** The east shaft's ladder slot, the height its ladder climbs to, and the niche for its call cord. */
    public static final BlockPos SLOT=new BlockPos(BX+1,0,SZ-1),NICHE=new BlockPos(BX,0,SZ-1);
    public static final int LADDER_TOP=5;
    private static final int FLAGS=PuzzleRooms.FLAGS;
    private TwinLift(){}

    /** Where a lift's boards stand at a floor: ground (0), the walkway (1), the gallery (2). */
    public static int height(int level){return FLOOR*level-1;}
    public static int start(long seed){return Math.floorMod(seed^0x3c6ef372L,2)==0?2:1;}

    // ------------------------------------------------------------------ state
    static CompoundTag state(ServerLevel l){var s=LabyrinthData.get(l.getServer()).state(STATE);if(!s.contains("A")){int a=start(l.getSeed());s.putInt("A",a);s.putInt("HA",height(a));s.putInt("HB",height(2-a));}return s;}
    static void save(ServerLevel l,CompoundTag s){LabyrinthData.get(l.getServer()).setState(STATE,s);}
    /** The west lift's floor; the east lift's is always two less. */
    public static int level(ServerLevel l){return state(l).getInt("A");}
    public static int heightA(ServerLevel l){return state(l).getInt("HA");}
    public static int heightB(ServerLevel l){return state(l).getInt("HB");}
    public static boolean moving(ServerLevel l){var s=state(l);return s.getInt("HA")!=height(s.getInt("A"))||s.getInt("HB")!=height(2-s.getInt("A"));}

    // ------------------------------------------------------------------ building
    public static void build(ServerLevel l,BlockPos b){
        var box=PLACE.room();
        var brick=Blocks.BRICKS.defaultBlockState();var floor=Blocks.SPRUCE_PLANKS.defaultBlockState();var ceiling=Blocks.DARK_OAK_PLANKS.defaultBlockState();var sub=Blocks.STONE_BRICKS.defaultBlockState();
        PuzzleRooms.shell(l,b,box,brick,sub,ceiling);
        PuzzleRooms.fill(l,b,-9,-2,-14,9,-2,0,sub);PuzzleRooms.fill(l,b,-9,-1,-14,9,-1,0,floor);
        // The two brick shafts, open where their lifts meet a floor.
        for(int x0:new int[]{AX,BX}){
            PuzzleRooms.fill(l,b,x0-1,-1,SZ-1,x0+3,13,SZ+3,brick);PuzzleRooms.fill(l,b,x0,-1,SZ,x0+2,13,SZ+2,Blocks.AIR.defaultBlockState());
            PuzzleRooms.fill(l,b,x0,-2,SZ,x0+2,-2,SZ+2,sub);
        }
        PuzzleRooms.fill(l,b,AX,0,SZ+3,AX+2,2,SZ+3,Blocks.AIR.defaultBlockState());   // west lift: the ground floor
        PuzzleRooms.fill(l,b,AX+3,5,SZ,AX+3,7,SZ+2,Blocks.AIR.defaultBlockState());   // west lift: the walkway
        PuzzleRooms.fill(l,b,BX-1,5,SZ,BX-1,7,SZ+2,Blocks.AIR.defaultBlockState());   // east lift: the walkway
        PuzzleRooms.fill(l,b,BX,10,SZ-1,BX+2,12,SZ-1,Blocks.AIR.defaultBlockState()); // east lift: the gallery
        // The east shaft's way out from the bottom: a ladder in a slot in its back wall, and a call cord beside it.
        PuzzleRooms.fill(l,b,BX,-1,SZ-2,BX+2,8,SZ-2,brick);
        PuzzleRooms.fill(l,b,SLOT.getX(),0,SLOT.getZ(),SLOT.getX(),LADDER_TOP+1,SLOT.getZ(),Blocks.AIR.defaultBlockState());
        for(int y=0;y<=LADDER_TOP;y++)l.setBlock(b.offset(SLOT.getX(),y,SLOT.getZ()),Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.SOUTH),FLAGS);
        l.setBlock(b.offset(NICHE),Blocks.AIR.defaultBlockState(),FLAGS);
        // The walkway between the shafts on its pier, and the gallery to the far door on its column, each with a lip for its rails.
        PuzzleRooms.fill(l,b,-2,4,SZ-1,2,4,SZ+3,floor);PuzzleRooms.fill(l,b,0,0,SZ,0,3,SZ+2,sub);
        PuzzleRooms.fill(l,b,-2,9,-14,6,9,-13,floor);PuzzleRooms.fill(l,b,-3,9,-12,2,9,-12,floor);PuzzleRooms.fill(l,b,-3,9,-14,-3,9,-13,floor);PuzzleRooms.fill(l,b,0,0,-13,0,8,-13,sub);
        var rails=rails();
        if(PuzzleRooms.solved(l.getServer(),PLACE))ladders(l,b,rails);
        TurningLandings.fences(l,b,rails);
        // Light: a lantern down the middle of the hall, lanterns on the walkway's rail posts and at the gallery's end.
        PuzzleRooms.fill(l,b,0,12,-4,0,13,-4,Blocks.CHAIN.defaultBlockState());l.setBlock(b.offset(0,11,-4),Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true),FLAGS);
        l.setBlock(b.offset(-2,6,SZ+3),Blocks.LANTERN.defaultBlockState(),FLAGS);l.setBlock(b.offset(2,6,SZ+3),Blocks.LANTERN.defaultBlockState(),FLAGS);
        l.setBlock(b.offset(6,10,-14),Blocks.LANTERN.defaultBlockState(),FLAGS);
        for(int x:new int[]{-7,7}){l.setBlock(b.offset(x,13,-4),Blocks.CHAIN.defaultBlockState(),FLAGS);l.setBlock(b.offset(x,12,-4),Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true),FLAGS);}
        LabyrinthBuilder.entrance(l,b,brick,floor,ceiling);
        LabyrinthBuilder.doors(l,b,PLACE);
        // The call cords at each landing, each hung off the wall beside it, and the lifts where they were left.
        for(int i=0;i<CALLS.length;i++)call(l,b,CALLS[i],CALL_WALLS[i]);
        var s=state(l);s.putInt("HA",height(s.getInt("A")));s.putInt("HB",height(2-s.getInt("A")));save(l,s);
        for(int y=-1;y<=12;y++){layer(l,b,AX,y,false,false);layer(l,b,BX,y,false,false);}
        layer(l,b,AX,heightA(l),true,false);layer(l,b,BX,heightB(l),true,true);
    }
    static void call(ServerLevel l,BlockPos b,BlockPos at,Direction wall){l.setBlock(b.offset(at),PuzzleFittingBlock.of(PuzzleFittingBlock.Kind.CALL,wall),FLAGS);}
    /** The rails round the walkway and the gallery, each on its lip. */
    static List<BlockPos> rails(){
        var rails=new ArrayList<BlockPos>();
        for(int x=-2;x<=2;x++){rails.add(new BlockPos(x,5,SZ+3));rails.add(new BlockPos(x,5,SZ-1));}
        for(int x=-3;x<=2;x++)rails.add(new BlockPos(x,10,-12));
        rails.add(new BlockPos(-3,10,-13));rails.add(new BlockPos(-3,10,-14));
        return rails;
    }
    /** The walkway's and the gallery's ladders, let down once the room is solved. */
    static void ladders(ServerLevel l,BlockPos b,List<BlockPos> rails){
        rails.remove(new BlockPos(0,5,SZ+3));rails.remove(new BlockPos(0,5,SZ-1));rails.remove(new BlockPos(0,10,-12));
        for(int y=0;y<=4;y++)l.setBlock(b.offset(0,y,SZ+3),Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.SOUTH),FLAGS);
        for(int y=5;y<=9;y++)l.setBlock(b.offset(0,y,-12),Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.SOUTH),FLAGS);
        l.setBlock(b.offset(0,5,SZ+3),Blocks.AIR.defaultBlockState(),FLAGS);l.setBlock(b.offset(0,10,-12),Blocks.AIR.defaultBlockState(),FLAGS);
    }
    /** One layer of a shaft: the lift's boards (with its cords on top) or nothing, or water at the bottom where no lift stands. */
    static void layer(ServerLevel l,BlockPos b,int x0,int y,boolean boards,boolean east){
        for(int x=x0;x<=x0+2;x++)for(int z=SZ;z<=SZ+2;z++){
            BlockState s=boards?PuzzleFittingBlock.of(PuzzleFittingBlock.Kind.PLATFORM,Direction.NORTH):y==-1?Blocks.WATER.defaultBlockState():Blocks.AIR.defaultBlockState();
            l.setBlock(b.offset(x,y,z),s,FLAGS);
        }
        if(!boards)return;
        // The cords stand at the back of the lift, out of the way of its openings.
        var up=east?new BlockPos(x0+2,y+1,SZ+2):new BlockPos(x0,y+1,SZ);var down=east?new BlockPos(x0+2,y+1,SZ+1):new BlockPos(x0,y+1,SZ+1);var wall=east?Direction.EAST:Direction.WEST;
        l.setBlock(b.offset(up),PuzzleFittingBlock.of(PuzzleFittingBlock.Kind.CORD_UP,wall),FLAGS);
        l.setBlock(b.offset(down),PuzzleFittingBlock.of(PuzzleFittingBlock.Kind.CORD_DOWN,wall),FLAGS);
    }
    static void clearCords(ServerLevel l,BlockPos b,int x0,int y){for(int x=x0;x<=x0+2;x++)for(int z=SZ;z<=SZ+2;z++){var at=b.offset(x,y+1,z);var s=l.getBlockState(at);if(PuzzleFittingBlock.is(s,PuzzleFittingBlock.Kind.CORD_UP)||PuzzleFittingBlock.is(s,PuzzleFittingBlock.Kind.CORD_DOWN))l.setBlock(at,Blocks.AIR.defaultBlockState(),FLAGS);}}

    // ------------------------------------------------------------------ moving
    /** Sends the west lift a floor up (+1) or down (-1); the east lift goes the other way. Returns why not, or null. */
    static String send(ServerLevel l,BlockPos b,int by){
        var s=state(l);if(moving(l))return "The lifts are still moving.";
        int a=s.getInt("A")+by;if(a<0||a>2)return by>0?"It is as high as it goes.":"It is as low as it goes.";
        // Neither lift comes down onto someone below it in its shaft; one at the bottom is safe under a lift stopping above them.
        int descending=by>0?BX:AX,from=by>0?heightB(l):heightA(l),to=height(by>0?2-a:a);
        for(var p:l.players())if(!p.isSpectator()&&inShaft(b,descending,p)&&p.getY()<b.getY()+from-.2&&p.getY()+p.getBbHeight()>b.getY()+to+.05)return "The lift will not come down onto someone below it.";
        s.putInt("A",a);save(l,s);
        l.playSound(null,b.offset(0,8,SZ+1),SoundEvents.CHAIN_PLACE,SoundSource.BLOCKS,1F,.5F);l.playSound(null,b.offset(0,8,SZ+1),SoundEvents.WOODEN_TRAPDOOR_OPEN,SoundSource.BLOCKS,.8F,.5F);
        return null;
    }
    static boolean inShaft(BlockPos b,int x0,ServerPlayer p){double x=p.getX()-b.getX(),z=p.getZ()-b.getZ();return x>=x0&&x<x0+3&&z>=SZ&&z<SZ+3;}
    /** One step of each moving lift, a block at a time, carrying whoever stands on it. */
    public static void step(ServerLevel l,BlockPos b){
        var s=state(l);int a=s.getInt("A");
        int ha=s.getInt("HA"),hb=s.getInt("HB"),ta=height(a),tb=height(2-a);
        if(ha!=ta){s.putInt("HA",move(l,b,AX,ha,Integer.signum(ta-ha),false));}
        if(hb!=tb){s.putInt("HB",move(l,b,BX,hb,Integer.signum(tb-hb),true));}
        save(l,s);
        if(!moving(l))l.playSound(null,b.offset(0,8,SZ+1),SoundEvents.CHAIN_STEP,SoundSource.BLOCKS,1F,.7F);
    }
    static int move(ServerLevel l,BlockPos b,int x0,int h,int dir,boolean east){
        var riders=new ArrayList<ServerPlayer>();double top=b.getY()+h+1;
        for(var p:l.players())if(!p.isSpectator()&&inShaft(b,x0,p)&&p.getY()>=top-.3&&p.getY()<=top+1.5)riders.add(p);
        if(dir>0)for(var p:riders)HouseInternalTeleport.translate(p,0,1,0);
        clearCords(l,b,x0,h);layer(l,b,x0,h,false,east);layer(l,b,x0,h+dir,true,east);
        if(dir<0)for(var p:riders)HouseInternalTeleport.translate(p,0,-1,0);
        return h+dir;
    }

    // ------------------------------------------------------------------ hands on
    static boolean use(ServerPlayer p,ServerLevel l,BlockPos b,BlockPos pos,BlockState s){
        var rel=pos.subtract(b);boolean east=rel.getX()>0;var kind=s.getValue(PuzzleFittingBlock.KIND);
        String why;
        switch(kind){
            case CORD_UP,CORD_DOWN->{
                int by=(kind==PuzzleFittingBlock.Kind.CORD_UP?1:-1)*(east?-1:1);
                why=send(l,b,by);
                PuzzleRooms.say(p,why!=null?why:kind==PuzzleFittingBlock.Kind.CORD_UP?"You pull the pale cord. The lift starts up, and across the hall the other lift starts down."
                        :"You pull the dark cord. The lift starts down, and across the hall the other lift starts up.");
                return true;
            }
            case CALL->{
                // A landing's cord calls its lift a floor toward it.
                boolean forEast=rel.getX()>=2;int floor=Math.max(0,(rel.getY()+1)/FLOOR);
                int now=forEast?2-level(l):level(l);
                if(now==floor){PuzzleRooms.say(p,"You pull the cord. The lift is already here.");return true;}
                int by=Integer.signum(floor-now)*(forEast?-1:1);why=send(l,b,by);
                PuzzleRooms.say(p,why!=null?why:"You pull the cord. Somewhere a bell rings once, and the lifts begin to move.");
                if(why==null)l.playSound(null,pos,SoundEvents.NOTE_BLOCK_BELL.value(),SoundSource.BLOCKS,.6F,1.2F);
                return true;
            }
            case PLATFORM->{PuzzleRooms.say(p,"A goods lift, its boards rimmed with iron. It hangs from the same counterweight as the lift across the hall.");return true;}
            default->{return false;}
        }
    }

    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        var server=e.getServer();if(server.getTickCount()%TICKS!=0)return;var origin=HouseSavedData.get(server).houseOrigin();var l=server.getLevel(HouseDimensions.INTERIOR);
        if(origin==null||l==null||LabyrinthData.get(server).door(PLACE.entryDoorId())==null)return;
        var b=LabyrinthPlaces.base(origin,PLACE);if(b==null||!l.isLoaded(b))return;
        if(moving(l))step(l,b);
        if(server.getTickCount()%(TICKS*4)==0)gallery(l,b);
    }
    /** Whether anyone has reached the top gallery: the room is solved, and the ladders let down. */
    public static void gallery(ServerLevel l,BlockPos b){
        if(PuzzleRooms.solved(l.getServer(),PLACE))return;
        for(var p:l.players())if(!p.isSpectator()&&PuzzleRooms.at(p)==PLACE&&p.getY()>=b.getY()+9.5&&p.getZ()<=b.getZ()-12.5){
            if(PuzzleRooms.solve(l,PLACE,"From the gallery, ladders unroll to the walkway and the floor below. The way is quick now.")){
                var rails=rails();ladders(l,b,rails);TurningLandings.fences(l,b,rails);
                l.playSound(null,b.offset(0,6,SZ+3),SoundEvents.LADDER_PLACE,SoundSource.BLOCKS,1F,.8F);
            }
            break;
        }
    }
}

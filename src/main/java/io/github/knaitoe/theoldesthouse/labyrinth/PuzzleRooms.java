package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/**
 * 0.4.199: what the puzzle rooms share. The owner: "a story, some wandering, then a place where the player can stop and work
 * something out. Solving one should make the House feel briefly understandable." Three families, each an early room that
 * teaches one household mechanism and a deep room that combines it across spaces: Borrowed Light and the lamp rooms, the
 * Service Lift and the twin lifts, the Turning Landing and the turning stair.
 *
 * One rhythm for all of them, so they stay rare: a reader is offered a puzzle only after resolving a story since their last
 * puzzle and walking a few halls past it; the offer is shared among the puzzles they could have, never more than one room's
 * worth. Early rooms come from the third crossing and never in the stone; a deep room only once its family's early room is
 * solved, and only from the strange depths. Solved, any of them is now and then just a pleasant room on the way.
 *
 * Nothing in them counts down, hurts, or is used up; a solved room stays solved, for everyone. None is a story: nothing here
 * counts toward the Witness.
 */
public final class PuzzleRooms {
    public enum Family {LIGHT,LIFT,LANDING}
    public enum Puzzle {
        BORROWED_LIGHT(LabyrinthPlace.BORROWED_LIGHT,Family.LIGHT,false,"The door has no handle on this side. The sun beside it is cold."),
        SERVICE_LIFT(LabyrinthPlace.SERVICE_LIFT,Family.LIFT,false,null),
        TURNING_LANDING(LabyrinthPlace.TURNING_LANDING,Family.LANDING,false,null),
        LAMP_ROOMS(LabyrinthPlace.LAMP_ROOMS,Family.LIGHT,true,"The door has no handle on this side. The sun beside it is cold."),
        TWIN_LIFT(LabyrinthPlace.TWIN_LIFT,Family.LIFT,true,null),
        TURNING_STAIR(LabyrinthPlace.TURNING_STAIR,Family.LANDING,true,null);
        public final LabyrinthPlace place;public final Family family;public final boolean deep;
        /** What the far door says while the room is unsolved, if the far door itself is what waits. */
        @Nullable final String held;
        Puzzle(LabyrinthPlace place,Family family,boolean deep,@Nullable String held){this.place=place;this.family=family;this.deep=deep;this.held=held;}
        public static @Nullable Puzzle of(LabyrinthPlace place){for(var q:values())if(q.place==place)return q;return null;}
        public Puzzle early(){for(var q:values())if(q.family==family&&!q.deep)return q;return this;}
    }
    public static final String STATE="puzzle_rooms_0199";
    /** The shared offer, the occasional return to a solved room, and the halls between. */
    public static final int OFFER=40,REVISIT=1,AFTER=3,AGAIN=6;
    private PuzzleRooms(){}

    public static boolean is(@Nullable LabyrinthPlace place){return place!=null&&Puzzle.of(place)!=null;}
    public static boolean solved(LabyrinthData data,LabyrinthPlace place){return data.state(STATE).getCompound("Solved").getBoolean(place.id());}
    public static boolean solved(MinecraftServer server,LabyrinthPlace place){return solved(LabyrinthData.get(server),place);}
    public static void markSolved(LabyrinthData data,LabyrinthPlace place){var all=data.state(STATE);var solved=all.getCompound("Solved");solved.putBoolean(place.id(),true);all.put("Solved",solved);data.setState(STATE,all);}
    /** Records a room solved for good and tells whoever is in it. Returns whether it was solved just now. */
    public static boolean solve(ServerLevel l,LabyrinthPlace place,String line){
        var data=LabyrinthData.get(l.getServer());if(solved(data,place))return false;markSolved(data,place);
        for(var p:l.players())if(at(p)==place)say(p,line);
        return true;
    }

    // ------------------------------------------------------------------ pacing
    private static CompoundTag own(LabyrinthData data,UUID reader){return data.stateEntry(STATE,reader.toString());}
    /** Whether this puzzle can be had at this depth at all: early rooms before the stone, deep ones from the strange depths once their family's early room is solved. */
    static boolean open(LabyrinthData data,Puzzle q,int depth){
        if(depth<AFTER)return false;
        if(q.deep?depth<LabyrinthPacing.STRANGE_DEPTH:depth>=LabyrinthPacing.STONE_DEPTH)return false;
        return !q.deep||solved(data,q.early().place);
    }
    /** A reader's weight for a puzzle room among the gray places. */
    public static int weight(LabyrinthData data,UUID reader,LabyrinthPlace place,int depth){
        var q=Puzzle.of(place);if(q==null||!open(data,q,depth))return 0;
        var own=own(data,reader);
        if(solved(data,place))return depth-own.getInt("Depth")>=AGAIN?REVISIT:0;
        if(WitnessAccount.count(data,reader)<=own.getInt("Stories")||depth-own.getInt("Depth")<AFTER)return 0;
        int eligible=0;for(var other:Puzzle.values())if(!solved(data,other.place)&&open(data,other,depth))eligible++;
        return Math.max(1,OFFER/Math.max(1,eligible));
    }
    /** A reader arriving in any puzzle room: the rhythm starts again from here. */
    public static void arrived(ServerPlayer p,LabyrinthPlace place){
        if(!is(place)||p.isSpectator())return;var data=LabyrinthData.get(p.server);var own=own(data,p.getUUID());
        own.putInt("Visits",own.getInt("Visits")+1);own.putInt("Stories",WitnessAccount.count(data,p.getUUID()));own.putInt("Depth",data.returnDepth(p.getUUID()));
        var seen=own.getCompound("Seen");seen.putBoolean(place.id(),true);own.put("Seen",seen);
        var all=data.state(STATE);all.put(p.getUUID().toString(),own);data.setState(STATE,all);
    }

    // ------------------------------------------------------------------ the rooms
    /** Which puzzle room a player stands in, if any. */
    public static @Nullable LabyrinthPlace at(ServerPlayer p){
        var origin=HouseSavedData.get(p.server).houseOrigin();
        if(origin==null||!p.level().dimension().equals(HouseDimensions.INTERIOR))return null;
        var place=LabyrinthPlaces.placeAt(origin,p.blockPosition());return is(place)?place:null;
    }
    public static @Nullable BlockPos base(MinecraftServer server,LabyrinthPlace place){var origin=HouseSavedData.get(server).houseOrigin();return origin==null?null:LabyrinthPlaces.base(origin,place);}
    /** The far door, before its room is solved, when the far door itself is what waits. */
    public static boolean holds(ServerPlayer p,LabyrinthData.Door door){
        for(var q:Puzzle.values())if(q.held!=null&&door.id.equals(q.place.id()+"/far")&&!solved(p.server,q.place)){say(p,q.held);return true;}
        return false;
    }
    public static BlockPos farDoor(LabyrinthPlace place){return place.doors().stream().filter(d->d.name().equals("far")).findFirst().orElseThrow().rel();}
    /** A far door with no handle on this side: iron, shut, then opened for good. */
    static void ironDoor(ServerLevel l,BlockPos b,LabyrinthPlace place,boolean open){
        var lower=b.offset(farDoor(place));
        var door=Blocks.IRON_DOOR.defaultBlockState().setValue(DoorBlock.FACING,Direction.SOUTH).setValue(DoorBlock.HINGE,DoorHingeSide.LEFT).setValue(DoorBlock.OPEN,open);
        l.setBlock(lower,door.setValue(DoorBlock.HALF,DoubleBlockHalf.LOWER),FLAGS);
        l.setBlock(lower.above(),door.setValue(DoorBlock.HALF,DoubleBlockHalf.UPPER),FLAGS);
    }
    static final int FLAGS=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    /** A room's shell: its walls, floor and ceiling, everything inside it air (and water below {@code waterTop}, if any). */
    static void shell(ServerLevel l,BlockPos b,BoundingBox box,BlockState wall,BlockState floor,BlockState ceiling){
        for(int x=box.minX();x<=box.maxX();x++)for(int z=box.minZ();z<=box.maxZ();z++)for(int y=box.minY();y<=box.maxY();y++){
            boolean inside=x>box.minX()&&x<box.maxX()&&z>box.minZ();
            l.setBlock(b.offset(x,y,z),!inside?wall:y==box.minY()?floor:y==box.maxY()?ceiling:Blocks.AIR.defaultBlockState(),FLAGS);
        }
    }
    static void fill(ServerLevel l,BlockPos b,int x0,int y0,int z0,int x1,int y1,int z1,BlockState s){
        for(int x=Math.min(x0,x1);x<=Math.max(x0,x1);x++)for(int y=Math.min(y0,y1);y<=Math.max(y0,y1);y++)for(int z=Math.min(z0,z1);z<=Math.max(z0,z1);z++)l.setBlock(b.offset(x,y,z),s,FLAGS);
    }
    public static void say(ServerPlayer p,String line){p.displayClientMessage(Component.literal(line).withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),true);}

    /** A reader's hand on one of the puzzle fittings: handed to the room it is in. */
    public static boolean use(ServerPlayer p,BlockPos pos,BlockState s,double height){
        if(p.isSpectator())return false;
        var origin=HouseSavedData.get(p.server).houseOrigin();if(origin==null)return false;
        var place=LabyrinthPlaces.placeAt(origin,pos);if(place==null)return false;
        var b=LabyrinthPlaces.base(origin,place);var l=p.serverLevel();
        return switch(place){
            case SERVICE_LIFT->ServiceLift.use(p,l,b,pos,s);
            case TURNING_LANDING,TURNING_STAIR->TurningLandings.use(p,l,b,place,pos,s);
            case TWIN_LIFT->TwinLift.use(p,l,b,pos,s);
            default->false;
        };
    }
}

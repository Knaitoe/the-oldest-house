package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.AABB;

/**
 * 0.4.199, the second puzzle room: the Service Lift. The owner: "A pantry dumbwaiter sits below its opening. Moving a few
 * kitchen weights onto its exposed counterweight raises it, revealing a passage behind the cage. The mechanism visibly
 * responds to each weight."
 *
 * A whitewashed pantry. In the far wall a dumbwaiter cage rests in a low opening, cold air coming round it; beside it, in
 * an open recess, the counterweight pan hangs on a chain that runs up over a pulley wheel and down to the top of the cage.
 * Three cast-iron weights stand on the counter, stamped with bars. A hand on a weight sets it in the pan: the pan sinks a
 * step for every bar, the chain runs over the wheel, and the cage climbs as far. Two steps clear the opening; a catch
 * clicks and holds the cage there for good. A hand on the pan lifts the last weight back out to its place on the counter.
 * Nothing leaves the room or is used up; the weights are never carried.
 */
public final class ServiceLift {
    public static final LabyrinthPlace PLACE=LabyrinthPlace.SERVICE_LIFT;
    public static final String STATE="service_lift_0199";
    /** The cage's column in the far wall, the counterweight's recess beside it, the pulley above between them. */
    public static final int SHAFT_X=0,RECESS_X=2,WALL_Z=-16,PULLEY_Y=7,PAN_TOP=5,CLEAR=2,TOP=3;
    /** The counter's three places for the weights. */
    public static final BlockPos[] SPOTS={new BlockPos(5,1,-7),new BlockPos(5,1,-8),new BlockPos(5,1,-9)};
    private static final int FLAGS=PuzzleRooms.FLAGS;
    private ServiceLift(){}

    /** The weights' bars, by place on the counter: always two ones and a two, in an order the world chooses. */
    public static int[] bars(long seed){int k=Math.floorMod(seed^0x2f6b1d3aL,3);return k==0?new int[]{1,1,2}:k==1?new int[]{2,1,1}:new int[]{1,2,1};}

    // ------------------------------------------------------------------ state
    static CompoundTag state(ServerLevel l){return LabyrinthData.get(l.getServer()).state(STATE);}
    /** The weights in the pan, by their place on the counter, in the order they were put in. */
    public static List<Integer> pan(ServerLevel l){var out=new ArrayList<Integer>();for(int i:state(l).getIntArray("Pan"))out.add(i);return out;}
    static void setPan(ServerLevel l,List<Integer> pan){var data=LabyrinthData.get(l.getServer());var s=data.state(STATE);s.put("Pan",new IntArrayTag(pan.stream().mapToInt(Integer::intValue).toArray()));data.setState(STATE,s);}
    /** How far the counterweight lifts the cage: a step for every bar in the pan, up to the top of the shaft. */
    public static int lift(ServerLevel l){int bars=0;var each=bars(l.getSeed());for(int i:pan(l))bars+=each[i];return Math.min(TOP,bars);}
    /** Where the cage stands: as far as the pan lifts it, and once the catch has held it, never below the opening. */
    public static int cage(ServerLevel l){int h=lift(l);return PuzzleRooms.solved(l.getServer(),PLACE)?Math.max(CLEAR,h):h;}

    // ------------------------------------------------------------------ building
    public static void build(ServerLevel l,BlockPos b){
        var box=PLACE.room();
        var wall=Blocks.CALCITE.defaultBlockState();var floor=Blocks.SPRUCE_PLANKS.defaultBlockState();var ceiling=Blocks.STRIPPED_SPRUCE_WOOD.defaultBlockState();
        PuzzleRooms.shell(l,b,box,wall,floor,ceiling);
        // The far wall of the pantry, with the shaft and the recess in it, and the cold passage behind.
        PuzzleRooms.fill(l,b,-5,0,WALL_Z,5,7,WALL_Z,wall);
        PuzzleRooms.fill(l,b,-5,0,WALL_Z-1,5,7,box.minZ()+1,Blocks.STONE_BRICKS.defaultBlockState());
        PuzzleRooms.fill(l,b,-1,0,WALL_Z-1,1,1,box.minZ()+1,Blocks.AIR.defaultBlockState());
        PuzzleRooms.fill(l,b,-1,-1,WALL_Z-1,1,-1,box.minZ()+1,Blocks.STONE_BRICKS.defaultBlockState());
        PuzzleRooms.fill(l,b,SHAFT_X,0,WALL_Z,SHAFT_X,PULLEY_Y,WALL_Z,Blocks.AIR.defaultBlockState());
        PuzzleRooms.fill(l,b,RECESS_X,1,WALL_Z,RECESS_X,PULLEY_Y,WALL_Z,Blocks.AIR.defaultBlockState());
        l.setBlock(b.offset(1,PULLEY_Y,WALL_Z),Blocks.AIR.defaultBlockState(),FLAGS);
        // A spruce beam across the far wall over the shaft and the recess.
        PuzzleRooms.fill(l,b,-5,6,WALL_Z,-1,6,WALL_Z,Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS,Direction.Axis.X));
        PuzzleRooms.fill(l,b,3,6,WALL_Z,5,6,WALL_Z,Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS,Direction.Axis.X));
        // The counter the weights stand on, with a scale on it for show, and shelves of jars along the west wall.
        for(int z=-6;z>=-10;z--)l.setBlock(b.offset(5,0,z),Blocks.STRIPPED_SPRUCE_WOOD.defaultBlockState(),FLAGS);
        for(int z=-4;z>=-12;z--){l.setBlock(b.offset(-5,2,z),Blocks.SPRUCE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,SlabType.TOP),FLAGS);
            if(z%2==0)l.setBlock(b.offset(-5,3,z),Blocks.DECORATED_POT.defaultBlockState(),FLAGS);}
        for(int x:new int[]{-3,3})l.setBlock(b.offset(x,7,-8),Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true),FLAGS);
        l.setBlock(b.offset(0,1,box.minZ()+2),Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true),FLAGS);
        LabyrinthBuilder.entrance(l,b,wall,floor,ceiling);
        LabyrinthBuilder.doors(l,b,PLACE);
        // The weights: on the counter, or in the pan, as they were left.
        var pan=pan(l);var each=bars(l.getSeed());
        for(int i=0;i<SPOTS.length;i++)l.setBlock(b.offset(SPOTS[i]),pan.contains(i)?Blocks.AIR.defaultBlockState():PuzzleFittingBlock.of(PuzzleFittingBlock.Kind.WEIGHT,Direction.WEST,each[i]),FLAGS);
        l.setBlock(b.offset(1,PULLEY_Y,WALL_Z),PuzzleFittingBlock.of(PuzzleFittingBlock.Kind.PULLEY,Direction.NORTH),FLAGS);
        mechanism(l,b,-1);
    }
    /** Lays the cage, the pan and both runs of chain where the pan's weights put them. */
    static void mechanism(ServerLevel l,BlockPos b,int was){
        int h=cage(l),n=pan(l).size(),pan=PAN_TOP-lift(l);
        for(int y=0;y<PULLEY_Y;y++){
            BlockState at=y==h?PuzzleFittingBlock.of(PuzzleFittingBlock.Kind.CAGE,Direction.NORTH,0):y==h+1?PuzzleFittingBlock.of(PuzzleFittingBlock.Kind.CAGE,Direction.NORTH,1)
                    :y>h+1?Blocks.CHAIN.defaultBlockState():Blocks.AIR.defaultBlockState();
            l.setBlock(b.offset(SHAFT_X,y,WALL_Z),at,FLAGS);
        }
        l.setBlock(b.offset(SHAFT_X,PULLEY_Y,WALL_Z),Blocks.CHAIN.defaultBlockState(),FLAGS);
        for(int y=1;y<=PULLEY_Y;y++){
            BlockState at=y==pan?PuzzleFittingBlock.of(PuzzleFittingBlock.Kind.PAN,Direction.NORTH,n):y>pan?Blocks.CHAIN.defaultBlockState():Blocks.AIR.defaultBlockState();
            l.setBlock(b.offset(RECESS_X,y,WALL_Z),at,FLAGS);
        }
        if(was>=0&&was!=h)l.playSound(null,b.offset(SHAFT_X,h,WALL_Z),SoundEvents.CHAIN_STEP,SoundSource.BLOCKS,1F,h>was?1.1F:.8F);
    }

    // ------------------------------------------------------------------ hands on
    static boolean use(ServerPlayer p,ServerLevel l,BlockPos b,BlockPos pos,BlockState s){
        var rel=pos.subtract(b);var each=bars(l.getSeed());
        switch(s.getValue(PuzzleFittingBlock.KIND)){
            case WEIGHT->{
                int i=-1;for(int k=0;k<SPOTS.length;k++)if(SPOTS[k].equals(rel))i=k;if(i<0)return false;
                var pan=pan(l);if(pan.contains(i))return true;
                int was=cage(l);pan.add(i);setPan(l,pan);l.setBlock(pos,Blocks.AIR.defaultBlockState(),FLAGS);
                l.playSound(null,b.offset(RECESS_X,PAN_TOP,WALL_Z),SoundEvents.CHAIN_PLACE,SoundSource.BLOCKS,1F,.7F);
                mechanism(l,b,was);
                PuzzleRooms.say(p,each[i]==1?"You set the weight in the pan. It sinks a step, the chain runs over the wheel, and the cage lifts as far."
                        :"You set the heavy weight in the pan. It sinks two steps, and the cage climbs two.");
                if(cage(l)>=CLEAR&&PuzzleRooms.solve(l,PLACE,"Something clicks under the cage and holds it. The way behind it is open."))
                    l.playSound(null,b.offset(SHAFT_X,CLEAR,WALL_Z),SoundEvents.BELL_BLOCK,SoundSource.BLOCKS,.5F,1.6F);
                return true;
            }
            case PAN->{
                var pan=pan(l);if(pan.isEmpty()){PuzzleRooms.say(p,"The counterweight pan. It is empty, and it hangs as high as it can.");return true;}
                int i=pan.get(pan.size()-1),was=cage(l);
                // The cage never comes down onto anyone standing in its opening.
                int to=Math.min(TOP,Math.max(0,barsWithout(each,pan)));if(PuzzleRooms.solved(l.getServer(),PLACE))to=Math.max(CLEAR,to);
                if(to<was&&!l.getEntitiesOfClass(LivingEntity.class,new AABB(b.offset(SHAFT_X,to,WALL_Z)).expandTowards(0,was-to,0).deflate(.05)).isEmpty()){PuzzleRooms.say(p,"Someone is under the cage.");return true;}
                pan.remove(pan.size()-1);setPan(l,pan);l.setBlock(b.offset(SPOTS[i]),PuzzleFittingBlock.of(PuzzleFittingBlock.Kind.WEIGHT,Direction.WEST,each[i]),FLAGS);
                l.playSound(null,pos,SoundEvents.CHAIN_PLACE,SoundSource.BLOCKS,1F,1F);mechanism(l,b,was);
                PuzzleRooms.say(p,PuzzleRooms.solved(l.getServer(),PLACE)?"You lift a weight back out to the counter. The catch holds the cage where it is.":"You lift a weight back out to the counter. The pan rises, and the cage settles.");
                return true;
            }
            case CAGE->{PuzzleRooms.say(p,cage(l)>=CLEAR?"The cage hangs in its shaft. The way behind it is open.":"A dumbwaiter cage at the bottom of its shaft. There is a dark space behind it, and cold air comes round it.");return true;}
            case PULLEY->{PuzzleRooms.say(p,"A pulley wheel. One chain runs down to the cage, the other to the counterweight pan.");return true;}
            default->{return false;}
        }
    }
    private static int barsWithout(int[] each,List<Integer> pan){int n=0;for(int k=0;k+1<pan.size();k++)n+=each[pan.get(k)];return n;}
}

package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.labyrinth.TheListHunt.Step;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.725, The list part 6c (docs/THE_LIST_0_4_725.md): night three's field house. By the third night the ones left on the list do not
 * go out. They are together in the field house's locker room, both of its doors bolted behind them, and the school has the field's
 * lights on. Each of them has two hiding places inside it: from where they stand to the first, and from there to the second.
 * <p>
 * The slasher gets in as at any bolted steel door, at the fifth blow. Once one of them screams, the rest run.
 */
public final class TheListHuddle{
    private TheListHuddle(){}

    /** Where each of them stands in the locker room, south of the bench, by who they are (TheListPeople); null for the rest. */
    public static final BlockPos[] AT=new BlockPos[TheListPeople.CAST];
    /** The field house's street door, onto Main Street's sidewalk. */
    public static final BlockPos STREET_DOOR=new BlockPos(TheListSite.FIELD_X-6,0,TheListSite.MAIN_FRONT_S);
    /** Each one's two ways inside the field house, and back from the second to the first. */
    static final Step[][][] WAYS=new Step[TheListPeople.CAST][][];
    static final Step[][] BACK=new Step[TheListPeople.CAST][];
    static {
        AT[TheListPeople.DEEK]=new BlockPos(-134,0,-103);
        AT[TheListPeople.WES]=new BlockPos(-132,0,-103);
        AT[TheListPeople.MEL]=new BlockPos(-128,0,-103);
        AT[TheListPeople.LESLEY]=new BlockPos(-126,0,-103);
        AT[TheListPeople.SHANNON]=new BlockPos(-123,0,-103);
        // Deek to the showers, then back to the corner by the coach's wall.
        WAYS[TheListPeople.DEEK]=new Step[][]{
            {TheListHunt.w(-133.5,0,-101.5),TheListHunt.w(-122.5,0,-101.5),TheListHunt.w(-120.5,0,-101.5),TheListHunt.hide(-113.5,0,-100.5,Direction.WEST)},
            {TheListHunt.w(-120.5,0,-101.5),TheListHunt.w(-122.5,0,-101.5),TheListHunt.hide(-122.5,0,-103.5,Direction.WEST)}};
        // Wes through the gap in the bench to the coach's office, then back along the lockers.
        WAYS[TheListPeople.WES]=new Step[][]{
            {TheListHunt.w(-130.5,0,-103.5),TheListHunt.w(-130.5,0,-105.5),TheListHunt.w(-121.5,0,-105.5),TheListHunt.w(-120.5,0,-105.5),TheListHunt.w(-118.5,0,-104.5),
                TheListHunt.w(-114.5,0,-104.5),TheListHunt.hide(-114.5,0,-106.5,Direction.EAST)},
            {TheListHunt.w(-118.5,0,-104.5),TheListHunt.w(-120.5,0,-105.5),TheListHunt.w(-121.5,0,-105.5),TheListHunt.hide(-127.5,0,-106.5,Direction.SOUTH)}};
        // Mel to the south-west corner, then along the back wall.
        WAYS[TheListPeople.MEL]=new Step[][]{
            {TheListHunt.w(-130.5,0,-101.5),TheListHunt.hide(-133.5,0,-100.5,Direction.EAST)},
            {TheListHunt.w(-128.5,0,-100.5),TheListHunt.hide(-125.5,0,-100.5,Direction.NORTH)}};
        // Lesley through the gap in the bench to the north-west corner, then back through it.
        WAYS[TheListPeople.LESLEY]=new Step[][]{
            {TheListHunt.w(-130.5,0,-103.5),TheListHunt.w(-130.5,0,-106.5),TheListHunt.hide(-133.5,0,-106.5,Direction.EAST)},
            {TheListHunt.w(-130.5,0,-106.5),TheListHunt.w(-130.5,0,-103.5),TheListHunt.hide(-128.5,0,-101.5,Direction.NORTH)}};
        // Shannon to the far end of the showers, then out to the lockers on the west wall.
        WAYS[TheListPeople.SHANNON]=new Step[][]{
            {TheListHunt.w(-122.5,0,-101.5),TheListHunt.w(-120.5,0,-101.5),TheListHunt.hide(-118.5,0,-100.5,Direction.WEST)},
            {TheListHunt.w(-120.5,0,-101.5),TheListHunt.w(-122.5,0,-101.5),TheListHunt.w(-130.5,0,-102.5),TheListHunt.hide(-133.5,0,-103.5,Direction.EAST)}};
        for(int who:TheListBody.LISTED){
            var second=WAYS[who][1];var back=new ArrayList<Step>();
            for(int i=second.length-2;i>=0;i--){var st=second[i];back.add(TheListHunt.w(st.at().x,st.at().y,st.at().z));}
            var first=WAYS[who][0];back.add(first[first.length-1]);
            BACK[who]=back.toArray(Step[]::new);
        }
    }
    /** The field house's three doors, by where their lower halves stand: the back door's two leaves and the street door. */
    static final BlockPos[] DOORS={TheListHunt.FIELD_DOOR,TheListHunt.FIELD_DOOR.east(),STREET_DOOR};

    /** Whether the night given is the one they spend in the field house. */
    public static boolean huddled(int night){return night==3;}
    /** Where one of them hides at the end of their first or second way inside the field house. */
    public static Vec3 hidingPlace(int who,int way){var steps=WAYS[who][way];return steps[steps.length-1].at();}

    /** Night three's hunt begun: the field house's back doors bolted; the street door is bolted once they are all in (0.4.739). */
    static void bolt(TheListHunt.Hunt h){h.bolted.add(DOORS[0]);h.bolted.add(DOORS[1]);}

    // ------------------------------------------------------------------------------------------------ 0.4.739: walking in
    /**
     * 0.4.739 (docs/THE_LIST_0_4_739.md): they are not set in the locker room; they come to it. When night three's hunt begins the five
     * are on Main Street's sidewalk beside the field house, and one after another, a second apart, they go in at the street door, through
     * the gap in the bench to where each stands. When the last is in, the street door is shut and bolted behind them. One the slasher
     * startles on the way is the hunt's from there; one held up for {@link #ARRIVE_LIMIT} ticks is let in where they stand.
     */
    static final Map<Long,Map<Integer,Arrival>> ARRIVING=new HashMap<>();
    static final class Arrival{int step;int wait;int still;double best=Double.MAX_VALUE;}
    public static final int ARRIVE_LIMIT=900,SPACING=20,STUCK=80;
    static final double WALK=.9;
    /** Where each starts on the sidewalk, east of the street door, in the order they go in. */
    static Vec3 start(int i){return new Vec3(-127.5+i*2,0,TheListSite.WALK_S_S-.5);}
    /** The way in: to the street door from outside, through it, round the corner to the gap in the bench, through it, to their place. */
    static Vec3[] route(int who){
        var at=Vec3.atBottomCenterOf(AT[who]);
        return new Vec3[]{new Vec3(-129.5,0,-109.5),new Vec3(-129.5,0,-107.5),new Vec3(-130.5,0,-106.5),new Vec3(-130.5,0,-103.5),new Vec3(-130.5,0,-102.5),at};
    }
    /** The hunt on night three begun: the five put out on the sidewalk, the street door opened for them. */
    static void arrive(net.minecraft.server.level.ServerLevel l,BlockPos b){
        var going=new HashMap<Integer,Arrival>();int i=0;
        for(int who:TheListBody.LISTED){
            var a=TheListPeople.find(l,b,who);if(a==null||AT[who]==null)continue;
            var s=Vec3.atLowerCornerOf(b).add(start(i));
            BorrascaCast.stop(a);a.moveTo(s.x,s.y,s.z,90,0);a.setYBodyRot(90);a.setYHeadRot(90);
            var r=new Arrival();r.wait=i*SPACING;going.put(who,r);i++;
        }
        if(going.isEmpty())return;
        ARRIVING.put(b.asLong(),going);
        open(l,b,true);
    }
    /** Whether one of them is still on their way in (the town's people leave them be). */
    public static boolean arriving(BlockPos b,int who){var m=ARRIVING.get(b.asLong());return m!=null&&m.containsKey(who);}
    /** Whether any of them is still on their way in (for a check). */
    public static boolean arriving(BlockPos b){var m=ARRIVING.get(b.asLong());return m!=null&&!m.isEmpty();}
    /** One tick of their walk in. */
    static void walkIn(net.minecraft.server.level.ServerLevel l,BlockPos b,TheListHunt.Hunt h){
        var m=ARRIVING.get(b.asLong());if(m==null)return;
        boolean late=h.tick>=ARRIVE_LIMIT;
        for(var it=m.entrySet().iterator();it.hasNext();){
            var e=it.next();int who=e.getKey();var r=e.getValue();
            var a=TheListPeople.find(l,b,who);
            if(a==null||TheListHunt.state(b,who)!=TheListHunt.State.CALM){it.remove();continue;}
            var route=route(who);
            if(late){settle(a,b,who);it.remove();continue;}
            if(r.wait>0){r.wait--;continue;}
            var goal=Vec3.atLowerCornerOf(b).add(route[r.step]);
            double d=horizontal(a.position(),goal);
            if(d<.6){
                if(++r.step>=route.length){settle(a,b,who);it.remove();continue;}
                goal=Vec3.atLowerCornerOf(b).add(route[r.step]);r.still=0;r.best=Double.MAX_VALUE;d=horizontal(a.position(),goal);
            }
            // Held up (by a wall, a bench end, or one of the others in the way): no nearer for a few seconds, they are let past.
            if(d<r.best-.05){r.best=d;r.still=0;}else if(++r.still>=STUCK){a.moveTo(goal.x,goal.y,goal.z,a.getYRot(),0);r.still=0;r.best=Double.MAX_VALUE;}
            BorrascaCast.walk(a,goal,WALK);
        }
        if(m.isEmpty()){ARRIVING.remove(b.asLong());open(l,b,false);h.bolted.add(STREET_DOOR);}
    }
    static double horizontal(Vec3 a,Vec3 g){return Math.hypot(a.x-g.x,a.z-g.z);}
    /** At their place in the locker room, facing the street door's wall as they all stand. */
    static void settle(LiteraryActor a,BlockPos b,int who){
        BorrascaCast.stop(a);var at=Vec3.atBottomCenterOf(b.offset(AT[who]));
        a.moveTo(at.x,at.y,at.z,180,0);a.setYBodyRot(180);a.setYHeadRot(180);a.setNoGravity(true);
    }
    static void open(net.minecraft.server.level.ServerLevel l,BlockPos b,boolean open){
        for(var at:new BlockPos[]{b.offset(STREET_DOOR),b.offset(STREET_DOOR).above()}){
            var st=l.getBlockState(at);if(st.getBlock() instanceof net.minecraft.world.level.block.DoorBlock&&st.getValue(net.minecraft.world.level.block.DoorBlock.OPEN)!=open)
                l.setBlock(at,st.setValue(net.minecraft.world.level.block.DoorBlock.OPEN,open),Block.UPDATE_CLIENTS);
        }
    }
    /** The night put away: nobody is on their way in. */
    static void reset(BlockPos b){ARRIVING.remove(b.asLong());}
    public static void clearAll(){ARRIVING.clear();}

    // ------------------------------------------------------------------------------------------------ the field's lights
    /** Where the field's lights fall, as light blocks a few blocks over the field and the ground between it and the field house. */
    static final List<BlockPos> LIGHTS=new ArrayList<>();
    static final int LIGHT_Y=6,LEVEL=15;
    static {
        for(int x=TheListSite.FIELD_W+2;x<=TheListSite.FIELD_E;x+=7){
            for(int z=TheListSite.FIELD_N+2;z<=TheListSite.FIELD_S;z+=8)LIGHTS.add(new BlockPos(x,LIGHT_Y,z));
            LIGHTS.add(new BlockPos(x,LIGHT_Y,(TheListSite.FIELD_N+TheListSite.FIELD_HOUSE_S)/2));
        }
    }
    /** The field's lights on or off: each light set in air where it falls, or taken out. */
    static void lights(ServerLevel l,BlockPos b,boolean on){
        // 0.4.738: the banks on the four towers lit or dark with them.
        for(var t:TheListSite.LIGHT_TOWERS){
            var at=b.offset(t[0],TheListSite.BANK_Y,t[1]);if(!l.isLoaded(at))continue;var st=l.getBlockState(at);
            var want=on?TheListBlock.of(TheListBlock.Kind.TOWER,TheListSite.bankFacing(t),7):TheListBlock.of(TheListBlock.Kind.FIELD,TheListSite.bankFacing(t),7);
            if(TheListBlock.is(st)&&(TheListBlock.is(st,TheListBlock.Kind.FIELD)||TheListBlock.is(st,TheListBlock.Kind.TOWER))&&st.getValue(TheListBlock.STAGE)==7&&st!=want)l.setBlock(at,want,Block.UPDATE_CLIENTS);
        }
        for(var rel:LIGHTS){
            var at=b.offset(rel);if(!l.isLoaded(at))continue;var st=l.getBlockState(at);
            if(on&&st.isAir())l.setBlock(at,Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL,LEVEL),Block.UPDATE_CLIENTS);
            else if(!on&&st.is(Blocks.LIGHT))l.setBlock(at,Blocks.AIR.defaultBlockState(),Block.UPDATE_CLIENTS);
        }
    }
    /** Whether the field's lights are on (for a check): every one of them that is loaded is lit. */
    /** Whether a tower's bank shows lit (for a check). */
    public static boolean bankLit(ServerLevel l,BlockPos b,int i){var t=TheListSite.LIGHT_TOWERS[i];var st=l.getBlockState(b.offset(t[0],TheListSite.BANK_Y,t[1]));return TheListBlock.is(st,TheListBlock.Kind.TOWER)&&st.getValue(TheListBlock.STAGE)==7;}
    public static boolean lit(ServerLevel l,BlockPos b){
        boolean any=false;
        for(var rel:LIGHTS){var at=b.offset(rel);if(!l.isLoaded(at))continue;if(!l.getBlockState(at).is(Blocks.LIGHT))return false;any=true;}
        return any;
    }
}

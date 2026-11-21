package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.labyrinth.TheListHunt.Step;
import java.util.ArrayList;
import java.util.List;
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

    /** Night three's hunt begun: the field house's doors bolted behind them. */
    static void bolt(TheListHunt.Hunt h){for(var d:DOORS)h.bolted.add(d);}

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
        for(var rel:LIGHTS){
            var at=b.offset(rel);if(!l.isLoaded(at))continue;var st=l.getBlockState(at);
            if(on&&st.isAir())l.setBlock(at,Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL,LEVEL),Block.UPDATE_CLIENTS);
            else if(!on&&st.is(Blocks.LIGHT))l.setBlock(at,Blocks.AIR.defaultBlockState(),Block.UPDATE_CLIENTS);
        }
    }
    /** Whether the field's lights are on (for a check): every one of them that is loaded is lit. */
    public static boolean lit(ServerLevel l,BlockPos b){
        boolean any=false;
        for(var rel:LIGHTS){var at=b.offset(rel);if(!l.isLoaded(at))continue;if(!l.getBlockState(at).is(Blocks.LIGHT))return false;any=true;}
        return any;
    }
}

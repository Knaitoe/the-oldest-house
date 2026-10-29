package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;

import static io.github.knaitoe.theoldesthouse.labyrinth.HouseReadPropBlock.Part.*;

/**
 * 0.4.161, the owner's playtest notes on the quiet rooms ("Calendar placement is fucked up. It's instances like that I need
 * you to check for." / "No custom textures. Blocks not connecting."): every built room is walked block by block.
 * A wall fitting must sit flush on a solid face, a hanging one under one, anything else on a surface; every block must
 * already have its joined shape; and nothing but the listed natural ground is a plain vanilla block.
 */
final class HouseReadAudit {
    private HouseReadAudit(){}
    private static final Set<Block> NATURAL=Set.of(Blocks.AIR,Blocks.LIGHT,Blocks.WATER,Blocks.GRASS_BLOCK,Blocks.DIRT,Blocks.COARSE_DIRT,Blocks.GRAVEL,Blocks.OAK_LOG,Blocks.OAK_LEAVES,Blocks.SPRUCE_LEAVES,Blocks.DEEPSLATE,Blocks.WHITE_BED);
    private static final Set<HouseReadPropBlock.Part> WALL=EnumSet.of(WOOL,TRENCH,BARN,RINGMASTER,PEA_COAT,CHORE_COAT,SWING_COAT,COLLARED_COAT,RAIN_MAC,PATCHED_JACKET,DUFFEL,LONG_CARDIGAN,TEE_JEANS,CROP_SHORTS,TANK_SHORTS,MIRROR,MIRROR_TOP,COVERED_MIRROR,RADIATOR,CALENDAR,LIGHT_SWITCH,POSTCARD,WALL_CLOCK,OUTLET,TOWEL_BAR,VANITY_LIGHT,EMPTY_HANGER,SCARF_HOOK,PORCH_LIGHT,TOILET);
    private static final Set<HouseReadPropBlock.Part> HANGING=EnumSet.of(PENDANT,DECK_LANTERN);
    /** Set into a ceiling, strung between posts, hung in a doorway, floating or carried by its neighbours. */
    private static final Set<HouseReadPropBlock.Part> FREE=EnumSet.of(CEILING_PANEL,CURTAIN,RECEPTION,STRING_LIGHT,SWING,ROWBOAT,TABLE_MID);
    private static final double FLUSH=.02;

    static void check(GameTestHelper h,ServerLevel l,BlockPos b,HouseReadSites.Room room){
        var area=HouseReadRooms.bounds(b,room);var problems=new ArrayList<String>();
        for(var at:BlockPos.betweenClosed((int)Math.floor(area.minX),(int)Math.floor(area.minY),(int)Math.floor(area.minZ),(int)Math.ceil(area.maxX)-1,(int)Math.ceil(area.maxY)-1,(int)Math.ceil(area.maxZ)-1)){
            var state=l.getBlockState(at);if(state.isAir())continue;var rel=at.subtract(b);
            var key=BuiltInRegistries.BLOCK.getKey(state.getBlock());
            if(key.getNamespace().equals("minecraft")&&!NATURAL.contains(state.getBlock()))problems.add("plain vanilla "+key.getPath()+" at "+rel.toShortString());
            if(state.getFluidState().isEmpty()){var joined=Block.updateFromNeighbourShapes(state,l,at.immutable());if(!joined.equals(state)&&!joined.isAir())problems.add("unjoined "+key.getPath()+" at "+rel.toShortString());}
            if(state.getBlock() instanceof HouseReadPropBlock)fitting(l,at.immutable(),rel,state,problems);
        }
        h.assertTrue(problems.isEmpty(),room+" audit: "+problems.size()+" problem(s): "+String.join("; ",problems.subList(0,Math.min(12,problems.size()))));
    }

    private static void fitting(ServerLevel l,BlockPos at,BlockPos rel,BlockState state,List<String> problems){
        var part=state.getValue(HouseReadPropBlock.PART);var shape=HouseReadGeometry.shape(state);if(shape.isEmpty()||FREE.contains(part))return;
        var name=part.getSerializedName()+" at "+rel.toShortString();
        if(WALL.contains(part)){
            var facing=state.getValue(HorizontalDirectionalBlock.FACING);var back=facing.getOpposite();
            boolean flush=switch(back){case NORTH->shape.min(Direction.Axis.Z)<=FLUSH;case SOUTH->shape.max(Direction.Axis.Z)>=1-FLUSH;case WEST->shape.min(Direction.Axis.X)<=FLUSH;default->shape.max(Direction.Axis.X)>=1-FLUSH;};
            if(!flush)problems.add(name+" floats off its wall");
            var wall=at.relative(back);if(!l.getBlockState(wall).isFaceSturdy(l,wall,facing))problems.add(name+" has no solid wall behind it");
        } else if(HANGING.contains(part)){
            var above=at.above();if(shape.max(Direction.Axis.Y)<1-FLUSH||!l.getBlockState(above).isFaceSturdy(l,above,Direction.DOWN))problems.add(name+" hangs from nothing");
        } else {
            var below=at.below();var under=l.getBlockState(below).getShape(l,below);
            if(shape.min(Direction.Axis.Y)>FLUSH)problems.add(name+" floats above its cell's floor");
            else if(under.isEmpty()||under.max(Direction.Axis.Y)<1-FLUSH)problems.add(name+" stands on nothing");
        }
    }
}

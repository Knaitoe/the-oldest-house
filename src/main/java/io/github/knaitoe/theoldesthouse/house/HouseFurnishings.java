package io.github.knaitoe.theoldesthouse.house;

import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;

/** Decoration upgrades touch exact furniture states and clear paper surfaces, never containers or actors. */
public final class HouseFurnishings {
    public static final String ID="furnishings_0421";
    private HouseFurnishings() {}
    private static void replace(ServerLevel level,BlockPos pos,Block expected,HouseholdFurnitureBlock.Kind kind,Direction facing) {
        BlockState old=level.getBlockState(pos);
        if(!old.is(expected) || level.getBlockEntity(pos)!=null) return;
        if(old.getBlock() instanceof StairBlock && (old.getValue(StairBlock.HALF)!=Half.BOTTOM
                || old.getValue(StairBlock.FACING).getOpposite()!=facing)) return;
        level.setBlock(pos,HouseholdFurnitureBlock.state(kind,facing),HouseCanvas.BUILD_FLAGS);
    }
    public static void paper(ServerLevel level,BlockPos pos,HouseMarginalia.Thread thread,Direction facing) {
        if(level.getBlockState(pos).isAir() && !level.getBlockState(pos.below()).isAir())
            level.setBlock(pos,NoteSurfaceBlock.state(thread,facing),HouseCanvas.BUILD_FLAGS);
    }
    private static void table(ServerLevel level,BlockPos pos,Block expected,HouseholdFurnitureBlock.Kind kind,Direction facing) {
        if(!level.getBlockState(pos).is(expected) || !level.getBlockState(pos.above()).is(Blocks.OAK_PRESSURE_PLATE)) return;
        replace(level,pos,expected,kind,facing);
        if(level.getBlockState(pos).getBlock() instanceof HouseholdFurnitureBlock) level.setBlock(pos.above(),Blocks.AIR.defaultBlockState(),HouseCanvas.BUILD_FLAGS);
    }
    private static void add(ServerLevel level,BlockPos pos,HouseholdFurnitureBlock.Kind kind,Direction facing) {
        if(level.getBlockState(pos).isAir() && level.getBlockState(pos.above()).isAir() && !level.getBlockState(pos.below()).isAir())
            level.setBlock(pos,HouseholdFurnitureBlock.state(kind,facing),HouseCanvas.BUILD_FLAGS);
    }
    public static void decorate(ServerLevel level,BlockPos base,LabyrinthPlace place) {
        if(place==LabyrinthPlace.JUNCTION) {
            for(int z:new int[]{-9,-10}) replace(level,base.offset(-4,0,z),Blocks.SPRUCE_STAIRS,HouseholdFurnitureBlock.Kind.BLUE_SOFA,Direction.EAST);
            replace(level,base.offset(4,0,-3),Blocks.OAK_STAIRS,HouseholdFurnitureBlock.Kind.WALNUT_DESK,Direction.WEST);
            paper(level,base.offset(4,1,-3),HouseMarginalia.Thread.HOUSEKEEPING,Direction.WEST);
            // The old cupboard fronts are embedded in the wall. A shallow sideboard provides bare tops.
            add(level,base.offset(-3,0,-12),HouseholdFurnitureBlock.Kind.BEDSIDE_TABLE,Direction.SOUTH);
            add(level,base.offset(-2,0,-12),HouseholdFurnitureBlock.Kind.CHEST_OF_DRAWERS,Direction.SOUTH);
            add(level,base.offset(-1,0,-12),HouseholdFurnitureBlock.Kind.WALNUT_DESK,Direction.SOUTH);
            paper(level,base.offset(-3,1,-12),HouseMarginalia.Thread.CALLS,Direction.SOUTH);
            paper(level,base.offset(-2,1,-12),HouseMarginalia.Thread.ROOM,Direction.SOUTH);
            paper(level,base.offset(-1,1,-12),HouseMarginalia.Thread.POEMS,Direction.SOUTH);
        } else if(LabyrinthHalls.isHall(place)) {
            int i=place.slot();
            for(var f:LabyrinthDomestic.fragments(place)) {
                int cx=(f.x0()+f.x1())/2,cz=(f.z0()+f.z1())/2;
                BlockPos back=base.offset(switch(f.entrance()) {
                    case EAST->new BlockPos(f.x0(),0,cz); case WEST->new BlockPos(f.x1(),0,cz);
                    case NORTH->new BlockPos(cx,0,f.z1()); default->new BlockPos(cx,0,f.z0());
                });
                dressFragment(level,back,f.entrance().getOpposite(),f.room(),i++);
                // A second piece in a room's unused rear corner gives the fragment a different silhouette.
                BlockPos corner=base.offset(f.x0(),0,f.z0());
                if(corner.equals(back)) corner=base.offset(f.x1(),0,f.z1());
                add(level,corner,f.room()==LabyrinthDomestic.Room.BEDROOM ? HouseholdFurnitureBlock.Kind.CHEST_OF_DRAWERS
                        : f.room()==LabyrinthDomestic.Room.LAUNDRY ? HouseholdFurnitureBlock.Kind.RADIATOR
                        : HouseholdFurnitureBlock.Kind.BEDSIDE_TABLE,f.entrance());
                if(level.getBlockState(corner).getBlock() instanceof HouseholdFurnitureBlock
                        && level.getBlockState(corner).getValue(HouseholdFurnitureBlock.KIND)!=HouseholdFurnitureBlock.Kind.RADIATOR)
                    paper(level,corner.above(),HouseMarginalia.Thread.values()[Math.floorMod(i,4)],f.entrance());
            }
            if(place==LabyrinthPlace.QUIET_ROOM) {
                var kinds=List.of(HouseholdFurnitureBlock.Kind.GREEN_ARMCHAIR,HouseholdFurnitureBlock.Kind.FLORAL_ARMCHAIR,
                        HouseholdFurnitureBlock.Kind.BLUE_SOFA,HouseholdFurnitureBlock.Kind.FOOTSTOOL);
                for(int z=-8;z>=-11;z--) replace(level,base.offset(-4,0,z),Blocks.SPRUCE_STAIRS,kinds.get(-z-8),Direction.EAST);
                table(level,base.offset(-3,0,-12),Blocks.DARK_OAK_FENCE,HouseholdFurnitureBlock.Kind.WALNUT_DESK,Direction.SOUTH);
                paper(level,base.offset(-3,1,-12),HouseMarginalia.Thread.POEMS,Direction.SOUTH);
                add(level,base.offset(2,0,-13),HouseholdFurnitureBlock.Kind.CHEST_OF_DRAWERS,Direction.NORTH);
                paper(level,base.offset(2,1,-13),HouseMarginalia.Thread.HOUSEKEEPING,Direction.NORTH);
            }
        } else if(LabyrinthMaze.isMaze(place)) {
            int i=place.slot();
            for(var f:LabyrinthDomestic.mazeFragments(place,LabyrinthMaze.layout(level.getServer(),place)))
                dressFragment(level,base.offset(f.back()),f.into(),f.room(),i++);
        }
    }
    private static void dressFragment(ServerLevel level,BlockPos back,Direction into,LabyrinthDomestic.Room room,int variation) {
        Direction along=into.getClockWise(),front=into.getOpposite();
        BlockPos surface;
        switch(room) {
            case DINING -> {
                table(level,back,Blocks.OAK_FENCE,HouseholdFurnitureBlock.Kind.FORMICA_TABLE,front);
                replace(level,back.relative(along),Blocks.OAK_STAIRS,HouseholdFurnitureBlock.Kind.CANE_CHAIR,along.getOpposite());
                replace(level,back.relative(along.getOpposite()),Blocks.OAK_STAIRS,HouseholdFurnitureBlock.Kind.KITCHEN_STOOL,along);
                surface=back.above();
            }
            case LAUNDRY -> {
                replace(level,back.relative(along.getOpposite()),Blocks.OAK_SLAB,HouseholdFurnitureBlock.Kind.WASHING_MACHINE,front);
                surface=back.relative(along.getOpposite()).above();
            }
            case KITCHEN -> surface=back.relative(along.getOpposite()).above();
            default -> {
                // The native bed, drawer inventory and flower pot all remain in place.
                surface=back.relative(along.getOpposite()).above();
                if(level.getBlockState(surface.below()).is(Blocks.WHITE_BED)) surface=back.above();
            }
        }
        if(!level.getBlockState(surface.below()).isAir() && !level.getBlockState(surface.below()).is(Blocks.WHITE_BED))
            paper(level,surface,HouseMarginalia.Thread.values()[Math.floorMod(variation,4)],front);
    }
    public static void upgrade(ServerLevel level,BlockPos origin,LabyrinthPlace place) {
        LabyrinthData data=LabyrinthData.get(level.getServer()); CompoundTag state=data.state(ID);
        if(!state.contains("Origin") || state.getLong("Origin")!=origin.asLong()) state=new CompoundTag();
        if(state.getBoolean(place.id())) return;
        BlockPos base=LabyrinthPlaces.base(origin,place); if(base!=null) decorate(level,base,place);
        state.putLong("Origin",origin.asLong()); state.putBoolean(place.id(),true); data.setState(ID,state);
    }
    public static void upgradeManor(ServerLevel level,BlockPos origin) {
        LabyrinthData data=LabyrinthData.get(level.getServer()); CompoundTag state=data.state(ID);
        if(!state.contains("Origin") || state.getLong("Origin")!=origin.asLong()) state=new CompoundTag();
        if(state.getBoolean("Manor")) return;
        decorateManor(level,origin); state.putLong("Origin",origin.asLong()); state.putBoolean("Manor",true); data.setState(ID,state);
    }
    static void decorateManor(ServerLevel level,BlockPos origin) {
        replace(level,origin.offset(4,1,5),Blocks.DARK_OAK_STAIRS,HouseholdFurnitureBlock.Kind.FLORAL_ARMCHAIR,Direction.SOUTH);
        replace(level,origin.offset(4,1,10),Blocks.DARK_OAK_STAIRS,HouseholdFurnitureBlock.Kind.GREEN_ARMCHAIR,Direction.NORTH);
        for(int z=6;z<=9;z++) replace(level,origin.offset(7,1,z),Blocks.DARK_OAK_STAIRS,HouseholdFurnitureBlock.Kind.BLUE_SOFA,Direction.WEST);
        replace(level,origin.offset(11,1,12),Blocks.SPRUCE_STAIRS,HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.WEST);
        replace(level,origin.offset(10,1,13),Blocks.SPRUCE_STAIRS,HouseholdFurnitureBlock.Kind.KITCHEN_STOOL,Direction.NORTH);
        replace(level,origin.offset(6,1,22),Blocks.DARK_OAK_STAIRS,HouseholdFurnitureBlock.Kind.GREEN_ARMCHAIR,Direction.SOUTH);
        replace(level,origin.offset(3,1,22),Blocks.DARK_OAK_STAIRS,HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.WEST);
        replace(level,origin.offset(18,1,24),Blocks.SPRUCE_STAIRS,HouseholdFurnitureBlock.Kind.KITCHEN_STOOL,Direction.EAST);
        // No containers or their established contents are replaced. Paper is placed only on a bare top.
        paper(level,origin.offset(7,2,14),HouseMarginalia.Thread.CALLS,Direction.NORTH);
        paper(level,origin.offset(7,2,19),HouseMarginalia.Thread.POEMS,Direction.NORTH);
    }
}

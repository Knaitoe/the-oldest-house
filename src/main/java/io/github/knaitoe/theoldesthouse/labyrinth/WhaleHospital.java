package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Recognized ward fixtures only: an in-place material pass, with exact container/source custody. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class WhaleHospital {
    public static final String STATE="whale_hospital_0470";
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE|Block.UPDATE_SUPPRESS_DROPS;
    private WhaleHospital(){}
    private static String key(BlockPos b){return Long.toString(b.asLong());}
    public static void fresh(ServerLevel l,BlockPos origin){var b=LabyrinthPlaces.base(origin,LabyrinthPlace.WHALE);BuildBlocks.after(l,()->refit(l,b,false));}
    static void freshBase(ServerLevel l,BlockPos b){BuildBlocks.after(l,()->refit(l,b,false));}
    public static void forget(net.minecraft.server.MinecraftServer s,BlockPos origin){var d=LabyrinthData.get(s);var state=d.state(STATE);state.remove(key(LabyrinthPlaces.base(origin,LabyrinthPlace.WHALE)));d.setState(STATE,state);}
    public static boolean apply(ServerLevel l,BlockPos b){return refit(l,b,true);}
    private static boolean refit(ServerLevel l,BlockPos b,boolean guarded){
        var d=LabyrinthData.get(l.getServer());var all=d.state(STATE);String key=key(b);var saved=all.getCompound(key);if(guarded&&saved.getBoolean("Done"))return true;
        var area=new AABB(Vec3.atLowerCornerOf(b.offset(-15,-1,-34)),Vec3.atLowerCornerOf(b.offset(16,12,2)));
        if(guarded){
            for(int x=(int)Math.floor(area.minX)>>4;x<=(int)Math.floor(area.maxX)>>4;x++)for(int z=(int)Math.floor(area.minZ)>>4;z<=(int)Math.floor(area.maxZ)>>4;z++)if(!l.hasChunk(x,z)||!l.areEntitiesLoaded(ChunkPos.asLong(x,z)))return false;
            if(l.players().stream().anyMatch(p->area.inflate(48).intersects(p.getCamera().getBoundingBox()))||!l.getEntitiesOfClass(LivingEntity.class,area).isEmpty())return false;
        }
        var edits=new LinkedHashMap<BlockPos,BlockState>();
        for(int x=-14;x<=14;x++)for(int z=-33;z<=0;z++)for(int y=-1;y<=10;y++){
            var at=b.offset(x,y,z);var old=l.getBlockState(at);var next=replacement(old,x,y,z);if(next!=null&&!next.equals(old))edits.put(at,next);
        }
        // Only the unchanged authored calendar is retired. A player's edited sign keeps its words and place.
        var calendar=b.offset(WhaleInstitute.CALENDAR);boolean replaceCalendar=l.getBlockEntity(calendar) instanceof SignBlockEntity sign&&calendarOriginal(sign);
        var table=b.offset(WhaleInstitute.CALENDAR_BOOK.below());var notebook=b.offset(WhaleInstitute.CALENDAR_BOOK);
        if(replaceCalendar&&l.getBlockState(table).isAir()&&l.getBlockState(notebook).isAir()){
            edits.put(calendar,Blocks.AIR.defaultBlockState());edits.put(table,InstituteFixtureBlock.of(InstituteFixtureBlock.Kind.BEDSIDE,Direction.SOUTH));edits.put(notebook,InstituteNotebookBlock.of(false,Direction.SOUTH));
        }
        var containers=saved.getCompound("Containers");var sources=saved.getCompound("Sources");
        for(var e:edits.entrySet()){
            var at=e.getKey();var old=l.getBlockState(at);var be=l.getBlockEntity(at);
            if(guarded&&!l.getEntitiesOfClass(Entity.class,new AABB(at).inflate(.01),entity->!(entity instanceof net.minecraft.world.entity.decoration.HangingEntity)).isEmpty())return false;
            if(be instanceof BarrelBlockEntity&&e.getValue().is(NovelRegistry.WARD_CABINET.get())){String pos=Long.toString(at.asLong());if(!containers.contains(pos))containers.put(pos,be.saveWithFullMetadata(l.registryAccess()));}
            else if(be instanceof LecternBlockEntity lectern&&e.getValue().is(NovelRegistry.WARD_NOTEBOOK.get())){String pos=Long.toString(at.asLong());if(!sources.contains(pos))sources.put(pos,lectern.getBook().saveOptional(l.registryAccess()));}
            else if(be!=null&&!(be instanceof BedBlockEntity)&&!(replaceCalendar&&at.equals(calendar)))return false;
            // A fixture resting on a changed full block remains supported; no story object is removed.
        }
        saved.put("Containers",containers);saved.put("Sources",sources);all.put(key,saved);d.setState(STATE,all);
        for(String pos:containers.getAllKeys()){var at=BlockPos.of(Long.parseLong(pos));if(l.getBlockEntity(at) instanceof BarrelBlockEntity barrel){var empty=barrel.saveWithFullMetadata(l.registryAccess());empty.remove("Items");empty.remove("LootTable");empty.remove("LootTableSeed");barrel.loadWithComponents(empty,l.registryAccess());}}
        for(String pos:sources.getAllKeys()){var at=BlockPos.of(Long.parseLong(pos));if(l.getBlockEntity(at) instanceof LecternBlockEntity lectern)lectern.setBook(net.minecraft.world.item.ItemStack.EMPTY);}
        for(var e:edits.entrySet())l.setBlock(e.getKey(),e.getValue(),F);
        // Pending custody also finishes after an interruption which already installed the new block.
        for(String pos:containers.getAllKeys()){var at=BlockPos.of(Long.parseLong(pos));if(!(l.getBlockEntity(at) instanceof InstituteCabinetBlockEntity cabinet))return false;cabinet.loadWithComponents(containers.getCompound(pos),l.registryAccess());cabinet.setChanged();}
        for(String pos:sources.getAllKeys()){var at=BlockPos.of(Long.parseLong(pos));if(!(l.getBlockEntity(at) instanceof InstituteNotebookBlockEntity book))return false;book.book(net.minecraft.world.item.ItemStack.parseOptional(l.registryAccess(),sources.getCompound(pos)));}
        // Re-evaluate native fence connections after both ends of every rail have their final material.
        for(var at:edits.keySet()){var state=l.getBlockState(at);if(state.is(NovelRegistry.WARD_RAIL.get()))l.setBlock(at,Block.updateFromNeighbourShapes(state,l,at),F);}
        saved.remove("Containers");saved.remove("Sources");saved.putBoolean("Done",true);all.put(key,saved);d.setState(STATE,all);return true;
    }
    private static boolean calendarOriginal(SignBlockEntity s){String[] text={"","Thursday","the 14th",""};for(int i=0;i<4;i++)if(!s.getFrontText().getMessage(i,false).getString().equals(text[i])||!s.getBackText().getMessage(i,false).getString().isEmpty())return false;return true;}
    private static BlockState replacement(BlockState s,int x,int y,int z){
        if(s.is(NovelRegistry.INSTITUTE.get()))return paint(x,y,z);
        if(s.is(Blocks.WHITE_CONCRETE)&&y==-1)return InstituteFixtureBlock.of(InstituteFixtureBlock.Kind.FLOOR_IVORY);
        if(s.is(Blocks.LIGHT_GRAY_CONCRETE)&&y==-1)return InstituteFixtureBlock.of(InstituteFixtureBlock.Kind.FLOOR_GRAY);
        if(s.is(Blocks.POLISHED_ANDESITE))return InstituteFixtureBlock.of(InstituteFixtureBlock.Kind.DADO);
        if(s.is(Blocks.SMOOTH_STONE)&&y==5)return InstituteFixtureBlock.of(InstituteFixtureBlock.Kind.CEILING);
        if(s.is(Blocks.BOOKSHELF))return InstituteFixtureBlock.of(InstituteFixtureBlock.Kind.SHELF);
        if(s.is(Blocks.SPRUCE_PLANKS)||s.is(Blocks.DARK_OAK_PLANKS)||s.is(Blocks.BIRCH_PLANKS)){
            if(y==10)return InstituteFixtureBlock.of(InstituteFixtureBlock.Kind.CEILING);
            if(y==0&&z==-27&&x>=8&&x<=11)return InstituteFixtureBlock.of(InstituteFixtureBlock.Kind.COUNTER,Direction.SOUTH);
            return wall(x,y,z)?paint(x,y,z):InstituteFixtureBlock.of(InstituteFixtureBlock.Kind.IVORY);
        }
        if(s.is(Blocks.SPRUCE_STAIRS)){
            if(y==0&&(z==-3||z==-6)&&x>=-11&&x<=-7)return InstituteFixtureBlock.of(InstituteFixtureBlock.Kind.BENCH,Direction.NORTH);
            return stairs(s);
        }
        if(s.getBlock() instanceof SlabBlock&&(s.is(Blocks.SPRUCE_SLAB)||s.is(Blocks.DARK_OAK_SLAB)))return NovelRegistry.WARD_SLAB.get().defaultBlockState().setValue(SlabBlock.TYPE,s.getValue(SlabBlock.TYPE)).setValue(SlabBlock.WATERLOGGED,s.getValue(SlabBlock.WATERLOGGED));
        if(s.is(Blocks.SPRUCE_FENCE)||s.is(Blocks.OAK_FENCE)||s.is(Blocks.DARK_OAK_FENCE))return rail(s);
        if(s.is(Blocks.BARREL))return NovelRegistry.WARD_CABINET.get().defaultBlockState().setValue(InstituteCabinetBlock.FACING,Direction.SOUTH);
        if(s.is(HouseBlocks.HOUSEHOLD_FURNITURE.get())){
            var facing=s.getValue(HouseholdFurnitureBlock.FACING);return switch(s.getValue(HouseholdFurnitureBlock.KIND)){
                case CANE_CHAIR,GREEN_ARMCHAIR,FLORAL_ARMCHAIR->InstituteFixtureBlock.of(InstituteFixtureBlock.Kind.CHAIR,facing);
                case KITCHEN_STOOL,FOOTSTOOL->InstituteFixtureBlock.of(InstituteFixtureBlock.Kind.STOOL,facing);
                case WALNUT_DESK,READING_DESK->InstituteFixtureBlock.of(InstituteFixtureBlock.Kind.DESK,facing);
                case FORMICA_TABLE,CHESS_TABLE->InstituteFixtureBlock.of(InstituteFixtureBlock.Kind.TABLE,facing);
                case BEDSIDE_TABLE->InstituteFixtureBlock.of(InstituteFixtureBlock.Kind.BEDSIDE,facing);
                case CHEST_OF_DRAWERS->NovelRegistry.WARD_CABINET.get().defaultBlockState().setValue(InstituteCabinetBlock.FACING,facing);
                default->null;
            };
        }
        if(s.is(Blocks.WHITE_BED))return InstituteFixtureBlock.of(s.getValue(BedBlock.PART)==BedPart.HEAD?InstituteFixtureBlock.Kind.BED_HEAD:InstituteFixtureBlock.Kind.BED_FOOT,s.getValue(BedBlock.FACING));
        if(s.is(Blocks.WATER_CAULDRON))return InstituteFixtureBlock.of(InstituteFixtureBlock.Kind.SINK,Direction.WEST);
        if(s.is(Blocks.LANTERN))return InstituteFixtureBlock.of(InstituteFixtureBlock.Kind.LIGHT);
        if(s.is(Blocks.WHITE_STAINED_GLASS_PANE)){var next=NovelRegistry.WARD_GLASS.get().defaultBlockState();for(var p:new BooleanProperty[]{IronBarsBlock.NORTH,IronBarsBlock.SOUTH,IronBarsBlock.EAST,IronBarsBlock.WEST,IronBarsBlock.WATERLOGGED})next=next.setValue(p,s.getValue(p));return next;}
        if(s.is(Blocks.BIRCH_DOOR)||s.is(Blocks.IRON_DOOR)){var next=(s.is(Blocks.IRON_DOOR)?NovelRegistry.WARD_LOCKED_DOOR:NovelRegistry.WARD_DOOR).get().defaultBlockState();return next.setValue(DoorBlock.FACING,s.getValue(DoorBlock.FACING)).setValue(DoorBlock.HALF,s.getValue(DoorBlock.HALF)).setValue(DoorBlock.HINGE,s.getValue(DoorBlock.HINGE)).setValue(DoorBlock.OPEN,s.getValue(DoorBlock.OPEN)).setValue(DoorBlock.POWERED,s.getValue(DoorBlock.POWERED));}
        if(s.is(Blocks.LECTERN)&&((x==WhaleInstitute.LECTERN.getX()&&y==WhaleInstitute.LECTERN.getY()&&z==WhaleInstitute.LECTERN.getZ())||(x==WhaleInstitute.ATTIC_DESK.getX()&&y==WhaleInstitute.ATTIC_DESK.getY()&&z==WhaleInstitute.ATTIC_DESK.getZ())))return InstituteNotebookBlock.of(true,Direction.SOUTH);
        return null;
    }
    private static boolean wall(int x,int y,int z){return x==-13||x==13||z==-32||z==0||z==-9||((x==-2||x==2)&&z>=-25&&z<=-10)||((z==-13||z==-17||z==-21||z==-25)&&Math.abs(x)>=3)||(y>=6&&(x==-4||x==4));}
    private static BlockState paint(int x,int y,int z){var kind=y<3&&(Math.abs(x)>=12||z<=-31)&&Math.floorMod(x*7+z,9)<3?InstituteFixtureBlock.Kind.DAMP:Math.floorMod(x*11+z*7+y/2,7)<2?InstituteFixtureBlock.Kind.PEEL:InstituteFixtureBlock.Kind.PAINT;return InstituteFixtureBlock.of(kind);}
    private static BlockState stairs(BlockState s){return NovelRegistry.WARD_STAIRS.get().defaultBlockState().setValue(StairBlock.FACING,s.getValue(StairBlock.FACING)).setValue(StairBlock.HALF,s.getValue(StairBlock.HALF)).setValue(StairBlock.SHAPE,s.getValue(StairBlock.SHAPE)).setValue(StairBlock.WATERLOGGED,s.getValue(StairBlock.WATERLOGGED));}
    private static BlockState rail(BlockState s){var next=NovelRegistry.WARD_RAIL.get().defaultBlockState();for(var p:new BooleanProperty[]{FenceBlock.NORTH,FenceBlock.SOUTH,FenceBlock.EAST,FenceBlock.WEST,FenceBlock.WATERLOGGED})next=next.setValue(p,s.getValue(p));return next;}
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){var s=e.getServer();if(s instanceof net.minecraft.gametest.framework.GameTestServer||s.getTickCount()%40!=27||LabyrinthBuilder.isCarving())return;var origin=HouseSavedData.get(s).houseOrigin();if(origin==null)return;var d=LabyrinthData.get(s);var l=s.getLevel(HouseDimensions.INTERIOR);if(l!=null&&d.builtVersion()>=39&&LabyrinthBuilder.isPlaceReady(d,LabyrinthPlace.WHALE))apply(l,LabyrinthPlaces.base(origin,LabyrinthPlace.WHALE));}
}

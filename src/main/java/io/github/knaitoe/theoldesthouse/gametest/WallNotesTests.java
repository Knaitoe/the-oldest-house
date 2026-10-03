package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(TheOldestHouse.MOD_ID) @PrefixGameTestTemplate(false)
public final class WallNotesTests {
    @GameTest(template="empty") public static void nativeGoatmanDoorOpensOntoTheActualTrailWithoutResettingTheTrailer(GameTestHelper h){
        var l=h.getLevel();var b=h.absolutePos(BlockPos.ZERO).offset(80,12,80);GoatmanWoods.build(l,b);
        var p=NativeTestPlayers.survival(h,"goat_throat");
        try{
            var cache=(BarrelBlockEntity)l.getBlockEntity(b.offset(7,0,-50));cache.setItem(3,new ItemStack(Items.EMERALD,5));
            NovelRooms.door(l,b.offset(0,0,1),Direction.SOUTH,Blocks.DARK_OAK_DOOR,true);
            // Reproduce the painted old wall, then migrate only that wall.
            l.setBlock(b,Blocks.ORANGE_TERRACOTTA.defaultBlockState(),2);l.setBlock(b.above(),Blocks.ORANGE_TERRACOTTA.defaultBlockState(),2);GoatmanWoods.repairEntrance(l,b);
            for(int z=2;z>=-2;z--){p.moveTo(Vec3.atBottomCenterOf(b.offset(0,0,z)));h.assertTrue(l.noCollision(p,p.getBoundingBox())&&!l.getBlockState(b.offset(0,-1,z)).isAir(),"a real survival body walks from the copied approach through z=0 onto the Goatman trail: "+z);}
            h.assertTrue(l.getBlockEntity(b.offset(7,0,-50))==cache&&cache.getItem(3).getCount()==5,"repairing the native arrival preserves the actual trailer cache and its contents");h.succeed();
        }finally{NativeTestPlayers.remove(p);l.getEntitiesOfClass(Display.ItemDisplay.class,IndianLakeRooms.bounds(b,LabyrinthPlace.GOATMAN),e->e.getTags().contains(GoatmanWoods.PLATE)).forEach(Display.ItemDisplay::discard);}
    }
    private static ServerLevel sandLevel;private static BlockPos sandBase;private static final List<ChunkPos> sandTickets=new ArrayList<>();
    @AfterBatch(batch="wall_sand") public static void cleanupSand(ServerLevel ignored){
        if(sandLevel!=null){for(var chunk:sandTickets)sandLevel.getChunkSource().removeRegionTicket(TicketType.PORTAL,chunk,3,sandBase);
            sandLevel.getEntitiesOfClass(FallingBlockEntity.class,new AABB(sandBase).inflate(90),e->e.getBlockState().is(Blocks.SAND)).forEach(FallingBlockEntity::discard);}
        sandTickets.clear();sandLevel=null;sandBase=null;
    }
    @GameTest(template="empty",batch="wall_sand",timeoutTicks=100) public static void nativeSandUpdatesCannotCollapseTheRepairedPlain(GameTestHelper h){
        sandLevel=h.getLevel();sandBase=h.absolutePos(BlockPos.ZERO).offset(60,30,40);var l=sandLevel;var b=sandBase;
        for(int x=(b.getX()-29)>>4;x<=(b.getX()+34)>>4;x++)for(int z=(b.getZ()-65)>>4;z<=b.getZ()>>4;z++){
            var chunk=new ChunkPos(x,z);sandTickets.add(chunk);l.getChunkSource().addRegionTicket(TicketType.PORTAL,chunk,3,b);l.getChunk(x,z);
        }
        // Reproduce the outside island's empty foundation independently of this test world's terrain.
        for(int x=-28;x<=28;x++)for(int z=-64;z<=0;z++)for(int y=-3;y<=-1;y++)l.setBlock(b.offset(x,y,z),Blocks.AIR.defaultBlockState(),2);
        var edited=b.offset(-12,-1,-12);l.setBlock(edited,Blocks.PRISMARINE.defaultBlockState(),2);
        var cachePos=b.offset(3,0,-7);l.setBlock(cachePos,Blocks.BARREL.defaultBlockState(),2);var cache=(BarrelBlockEntity)l.getBlockEntity(cachePos);cache.setItem(4,new ItemStack(Items.EMERALD,7));
        NovelRooms.repairPlainGround(l,b);
        for(int x=-28;x<=28;x++)for(int z=-64;z<=0;z++){
            var at=b.offset(x,-1,z);h.assertTrue(!l.getBlockState(at).isAir()&&l.getBlockState(at.below()).isCollisionShapeFullBlock(l,at.below()),"every repaired authored sand tile has native solid support");
            if(l.getBlockState(at).is(Blocks.SAND))l.getBlockState(at).tick(l,at,l.getRandom());
        }
        // Invoke the same native block tick for an explicitly unsupported control; no chunk-timing guess.
        var control=b.offset(32,4,-4);l.setBlock(control.below(),Blocks.AIR.defaultBlockState(),2);l.setBlock(control,Blocks.SAND.defaultBlockState(),2);l.getBlockState(control).tick(l,control,l.getRandom());
        h.assertTrue(l.getBlockState(control).isAir(),"the native unsupported control leaves its cell to become a falling block");
        for(int x=-28;x<=28;x++)for(int z=-64;z<=0;z++)h.assertTrue(!l.getBlockState(b.offset(x,-1,z)).isAir(),"actual native sand ticks leave every repaired tile whole, without a falling-block cascade");
        h.assertTrue(l.getBlockState(edited).is(Blocks.PRISMARINE)&&l.getBlockEntity(cachePos)==cache&&cache.getItem(4).getCount()==7,"the player edit and original finite cache survive actual physics ticks");h.succeed();
    }
    @GameTest(template="empty") public static void nativeHallPatchAndBedroomFinishPreserveTheOriginalFurnishings(GameTestHelper h){
        var l=h.getLevel();var o=h.absolutePos(BlockPos.ZERO).offset(10,20,100);HouseImpossibleHallway.build(l,o);
        var patch=o.offset(HouseLayout.AXIS_X+2,2,HouseImpossibleHallway.START_Z_OFFSET+26);l.setBlock(patch,Blocks.YELLOW_TERRACOTTA.defaultBlockState(),2);
        var custom=patch.above();l.setBlock(custom,Blocks.BRICKS.defaultBlockState(),2);
        var cupboard=o.offset(HouseLayout.AXIS_X-2,1,HouseImpossibleHallway.START_Z_OFFSET+12);var cache=(BarrelBlockEntity)l.getBlockEntity(cupboard);cache.setItem(2,new ItemStack(Items.PAPER,3));
        HouseImpossibleHallway.dressDomesticApproach(l,o);
        var b=o.offset(80,0,0);LabyrinthHalls.build(l,b,LabyrinthPlace.BENT_HALL);
        var bedPos=b.offset(-9,0,-28);var bed=l.getBlockState(bedPos);var wall=b.offset(-13,1,-27);l.setBlock(wall,Blocks.PINK_TERRACOTTA.defaultBlockState(),2);WallNotesRepairs.neutralBedroom(l,b);
        h.assertTrue(l.getBlockState(patch).is(Blocks.WHITE_TERRACOTTA)&&l.getBlockState(custom).is(Blocks.BRICKS)&&l.getBlockEntity(cupboard)==cache&&cache.getItem(2).getCount()==3,"the old yellow patch becomes matching plaster while an edit and actual cupboard remain");
        h.assertTrue(bed.getBlock() instanceof BedBlock&&l.getBlockState(wall).is(Blocks.LIGHT_GRAY_TERRACOTTA)&&l.getBlockState(bedPos).equals(bed),"the red-looking bedroom gets an ordinary neutral finish without rebuilding its actual bed");h.succeed();
    }
    @GameTest(template="empty") public static void recurringHomeCopiesHavePersonalBreathingRoomAfterReload(GameTestHelper h){
        var d=new LabyrinthData();var reader=UUID.randomUUID();var peer=UUID.randomUUID();d.setReady(RedRoom.ID,true);d.visit(reader,LabyrinthPlace.RED_ROOM);
        var saved=LabyrinthData.FACTORY.deserializer().apply(d.save(new CompoundTag(),h.getLevel().registryAccess()),h.getLevel().registryAccess());
        h.assertTrue(!LabyrinthDealer.vignettesAvailable(saved,reader).contains(LabyrinthPlace.RED_ROOM)&&LabyrinthDealer.vignettesAvailable(saved,peer).contains(LabyrinthPlace.RED_ROOM),"an actual saved home-copy visit pauses repetition only for that reader after reload");
        saved.setHillaryScent(reader,true);h.assertTrue(LabyrinthDealer.vignettesAvailable(saved,reader).contains(LabyrinthPlace.RED_ROOM),"a deliberate companion scent still reaches the recurring source");saved.setHillaryScent(reader,false);
        saved.visit(reader,LabyrinthPlace.BENT_HALL);h.assertTrue(LabyrinthDealer.rememberedWeight(saved,reader,LabyrinthPlace.BENT_HALL,26)<LabyrinthDealer.rememberedWeight(saved,peer,LabyrinthPlace.BENT_HALL,26),"a recent native corridor has lower weight for its actual explorer");
        for(int i=0;i<5;i++)saved.visit(reader,LabyrinthPlace.STRAIGHT_HALL);
        h.assertTrue(LabyrinthDealer.vignettesAvailable(saved,reader).contains(LabyrinthPlace.RED_ROOM),"six intervening native visits restore the recurring source without resetting its story");h.succeed();
    }
    @GameTest(template="empty") public static void nativeWestMazeEntryKeepsItsPassageAndClosesTheSkyHole(GameTestHelper h){
        var l=h.getLevel();var origin=h.absolutePos(BlockPos.ZERO).offset(160,0,160);var b=FinaleArchitecture.base(origin);var plan=new LinkedHashMap<BlockPos,BlockState>();StaircaseMazes.plan(plan,b);
        plan.forEach((at,s)->{int x=at.getX()-b.getX(),z=at.getZ()-b.getZ();if(x>=-24&&x<=-18&&z>=32&&z<=38)l.setBlock(at,s,2);});
        var opening=b.offset(-20,FinaleArchitecture.ARENA,38);for(int y=0;y<3;y++)l.setBlock(opening.above(y),Blocks.AIR.defaultBlockState(),2);
        var changed=b.offset(-24,FinaleArchitecture.ARENA+3,38);l.setBlock(changed,Blocks.EMERALD_BLOCK.defaultBlockState(),2);
        var p=NativeTestPlayers.survival(h,"maze_cap");
        try{
            p.moveTo(Vec3.atBottomCenterOf(opening));h.assertTrue(l.noCollision(p,p.getBoundingBox()),"the reproduced old connector cap exposes a native player-sized opening");WallNotesRepairs.repairWestConnector(l,origin);
            h.assertTrue(!l.noCollision(p,p.getBoundingBox())&&l.getBlockState(changed).is(Blocks.EMERALD_BLOCK),"the new backing closes the actual outside hole while preserving an edit");
            for(int x=18;x<=23;x++){p.moveTo(Vec3.atBottomCenterOf(b.offset(-x,FinaleArchitecture.ARENA,36)));h.assertTrue(l.noCollision(p,p.getBoundingBox()),"the real west connector remains physically walkable");}h.succeed();
        }finally{NativeTestPlayers.remove(p);}
    }
}

package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.*;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.gametest.*;

/** Actual cuts, native walking and physical furniture clearances from the owner's second playtest. */
@GameTestHolder(TheOldestHouse.MOD_ID+"_uncanny") @PrefixGameTestTemplate(false)
public final class Playtest0131Tests {
    private static LiteraryTests.Fixture fixture;
    private static ClassicsTests.Fixture sitting;
    private static void close(){if(fixture!=null){fixture.close();fixture=null;}if(sitting!=null){sitting.close();sitting=null;}}
    private static CompoundTag world(LiteraryTests.Fixture f,LabyrinthPlace place){return f.data().stateEntry(UncannyStories.WORLD,place.id()+":"+f.base(place).asLong());}
    private static Set<BlockPos> flesh(LiteraryTests.Fixture f){
        var found=new HashSet<BlockPos>();var b=f.base(LabyrinthPlace.LAYERED_MAN);var l=f.level(LabyrinthPlace.LAYERED_MAN);
        for(var r:BlockPos.betweenClosed(new BlockPos(-6,-5,-28),new BlockPos(6,-2,-10)))if(UncannyFleshBlock.is(l.getBlockState(b.offset(r))))found.add(r.immutable());return found;
    }
    private static void removeFlesh(LiteraryTests.Fixture f){for(var r:flesh(f))f.level(LabyrinthPlace.LAYERED_MAN).destroyBlock(f.base(LabyrinthPlace.LAYERED_MAN).offset(r),false);}

    @GameTest(template="empty",batch="review131_worker",timeoutTicks=900)
    public static void theOriginalWorkerActuallyCutsAndTheManReactsToTheRealBladeFaceAndMouth(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);fixture=f;var place=LabyrinthPlace.LAYERED_MAN;var p=f.player("helping_cut_reader",place);var l=f.level(place);var b=f.base(place);f.chunks.hold(l,IndianLakeRooms.bounds(b,place));UUID[] worker={null};
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the real worker and flesh are resident"))
            .thenExecute(()->worker[0]=world(f,place).getUUID("Peeler"))
            .thenWaitUntil(()->h.assertTrue(world(f,place).getInt("WorkerCuts131")>0,"the original worker finishes mining an exposed native flesh cell"))
            .thenExecute(()->{
                var shared=world(f,place);boolean open=false;for(var row:shared.getList("Regrow",Tag.TAG_COMPOUND))open|=l.isEmptyBlock(b.offset(BlockPos.of(((CompoundTag)row).getLong("At"))));
                h.assertTrue(open&&f.own(p,place).getInt("Peeled")==0&&l.getEntity(worker[0]) instanceof LiteraryActor,"helper mining removes real tissue without fabricating the reader's cuts or replacing the worker");
                f.click(p,place,UncannyRooms.TOOL);h.assertTrue(f.own(p,place).getBoolean("CleaverPrompt131")&&p.getInventory().countItem(UncannyRegistry.CLEAVER.get())==1,"taking the original blade immediately triggers the man's request");
                for(int i=0;i<p.getInventory().getContainerSize();i++)if(p.getInventory().getItem(i).is(UncannyRegistry.CLEAVER.get())){p.getInventory().selected=i;break;}
                f.at(p,place,.5,-5,-17.5);
            }).thenIdle(10).thenExecute(()->{
                f.click(p,place,new BlockPos(-1,-2,-21));h.assertTrue(world(f,place).getBoolean("FaceCut131")&&f.own(p,place).getBoolean("FaceCutReply131")&&l.isEmptyBlock(b.offset(-1,-2,-21)),"cutting the actual eye removes it and produces the blindness response");
            }).thenIdle(10).thenExecute(()->{
                f.click(p,place,new BlockPos(0,-3,-21));h.assertTrue(world(f,place).getBoolean("MouthCut131")&&l.isEmptyBlock(b.offset(0,-3,-21))&&f.own(p,place).getLong("CellarRumbleUntil130")>l.getGameTime(),"the actual mouth cut immediately replaces words with a spatial gurgle");f.at(p,place,.5,-2,-9.5);
            }).thenIdle(10).thenExecute(()->h.assertTrue(f.own(p,place).getLong("NextLeavePlea131")>l.getGameTime()&&UncannyCellar.remains(l,b),"starting up the stairs while tissue remains produces the worker's plea")).thenSucceed();
    }
    @AfterBatch(batch="review131_worker") public static void workerDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="review131_eradication",timeoutTicks=1200)
    public static void everyNewCellHasSurvivingFleshBesideItAndEradicationSurvivesActualDepartureAndReload(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);fixture=f;var place=LabyrinthPlace.LAYERED_MAN;var p=f.player("eradication_reader",place);var l=f.level(place);var b=f.base(place);f.chunks.hold(l,IndianLakeRooms.bounds(b,place));
        var previous=new HashSet<BlockPos>();boolean[] tracking={false};int[] grew={0};var isolated=new BlockPos(5,-2,-11);UUID[] worker={null};
        h.onEachTick(()->{if(!tracking[0])return;var current=flesh(f);for(var r:current)if(!previous.contains(r)){boolean adjacent=false;for(var d:Direction.values())adjacent|=previous.contains(r.relative(d));h.assertTrue(adjacent,"a new cell must come from surviving neighboring flesh: "+r);grew[0]++;}previous.clear();previous.addAll(current);});
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"native cellar sections loaded"))
            .thenExecute(()->{
                worker[0]=world(f,place).getUUID("Peeler");removeFlesh(f);l.setBlock(b.offset(0,-4,-23),UncannyFleshBlock.at(new BlockPos(0,-4,-23)),3);
                var shared=world(f,place);UncannyCellar.cut(l,b,isolated,shared);for(var row:shared.getList("Regrow",Tag.TAG_COMPOUND))((CompoundTag)row).putLong("Due",0);shared.putBoolean("RegrowthBudget130",true);f.data().setStateEntry(UncannyStories.WORLD,place.id()+":"+b.asLong(),shared);
                previous.addAll(flesh(f));tracking[0]=true;
            }).thenIdle(120).thenExecute(()->{
                tracking[0]=false;h.assertTrue(grew[0]>0&&l.isEmptyBlock(b.offset(isolated)),"actual growth occurs but an isolated overdue cut cannot seed new flesh");
                removeFlesh(f);
            }).thenIdle(6).thenExecute(()->{
                h.assertTrue(world(f,place).getBoolean("Eradicated131")&&!UncannyCellar.remains(l,b),"removing the final real tissue cell permanently ends growth");
                f.at(p,place,80,0,40);f.reload();
            }).thenIdle(240).thenExecute(()->f.arrive(p,place)).thenIdle(440).thenExecute(()->{
                h.assertTrue(world(f,place).getBoolean("Eradicated131")&&!UncannyCellar.remains(l,b)&&world(f,place).getList("Regrow",Tag.TAG_COMPOUND).isEmpty(),"vacancy, saved-data reload, actual re-entry and overdue growth never rebuild eradicated flesh");
                h.assertTrue(worker[0].equals(world(f,place).getUUID("Peeler"))&&l.getEntity(worker[0]) instanceof LiteraryActor,"the original worker survives the permanent change");
            }).thenSucceed();
    }
    @AfterBatch(batch="review131_eradication") public static void eradicationDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="review131_clearance",timeoutTicks=1100)
    public static void theSameSlimTallWomanWalksUnderAllFourRealTunnelLightsWithNativeCollision(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);fixture=f;var place=LabyrinthPlace.TUNNEL_PERIPHERY;var p=f.player("lamp_clearance_reader",place);var l=f.level(place);var b=f.base(place);f.chunks.hold(l,IndianLakeRooms.bounds(b,place));LiteraryActor[] woman={null};UUID[] id={null};var passed=new HashSet<Integer>();
        h.onEachTick(()->{var a=woman[0];if(a==null)return;TunnelStride.seek(a,new Vec3(b.getX()+.5,b.getY(),b.getZ()-60.5));h.assertTrue(a.getUUID().equals(id[0])&&!a.noPhysics&&l.noCollision(a,a.getBoundingBox().deflate(1e-7)),"the same woman keeps native floor, wall and ceiling collision");for(int z:new int[]{-6,-20,-36,-52})if(a.getZ()<b.getZ()+z-1)passed.add(z);});
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"all four real light bays are resident"))
            .thenExecute(()->{
                var a=UncannyStories.actor(p,f.own(p,place),"CastGiant");h.assertTrue(a!=null&&a.getBbWidth()<=.81&&a.getBbHeight()>=2.7&&a.getBbHeight()<3,"the physical body is slim, tall and below the three-block ceiling");id[0]=a.getUUID();
                for(int z:new int[]{-6,-20,-36,-52})h.assertTrue(l.getBlockState(b.offset(0,3,z)).is(UncannyRegistry.FURNITURE.get())&&l.getBlockState(b.offset(0,3,z)).getValue(UncannyFurnitureBlock.KIND)==UncannyFurnitureBlock.Kind.FLUSH_LIGHT&&l.isEmptyBlock(b.offset(0,2,z)),"the actual ceiling lamp is recessed above the full passage at "+z);
                f.at(p,place,40,0,40);a.moveTo(b.getX()+.5,b.getY(),b.getZ()-3.5,180,0);a.appearance(LiteraryActor.UNCANNY_GIANT,2);a.setNoGravity(false);woman[0]=a;
            }).thenWaitUntil(()->h.assertTrue(woman[0].getZ()<b.getZ()-59,"native every-tick walking passes the complete lamp route: "+woman[0].position().subtract(Vec3.atLowerCornerOf(b))))
            .thenExecute(()->{h.assertTrue(passed.size()==4&&Math.abs(woman[0].getY()-b.getY())<.02,"ordinary walking clears all four lamps on the real floor without a collision bypass or intervening teleport");woman[0]=null;}).thenSucceed();
    }
    @AfterBatch(batch="review131_clearance") public static void clearanceDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="review131_rooms",timeoutTicks=600)
    public static void packedEarthAndRemovableSunBoardsEncloseTheFamilyAndTheBedroomHasClearUsableFurniture(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);fixture=f;var place=LabyrinthPlace.PAINTED_INTERIOR;var p=f.player("sun_board_reader",place);var l=f.level(place);var b=f.base(place);f.chunks.hold(l,IndianLakeRooms.bounds(b,place));f.build(LabyrinthPlace.MISSING_SLEEP);var bed=f.base(LabyrinthPlace.MISSING_SLEEP);f.chunks.hold(l,IndianLakeRooms.bounds(bed,LabyrinthPlace.MISSING_SLEEP));
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"both actual furnished rooms are resident"))
            .thenExecute(()->{
                for(var r:List.of(new BlockPos(-9,2,-22),new BlockPos(9,2,-27),new BlockPos(0,6,-22),new BlockPos(0,2,-29)))h.assertTrue(l.getBlockState(b.offset(r)).is(Blocks.DIRT),"packed earth touches the house's walls and roof: "+r);
                var body=new AABB(b.getX()+.2,b.getY()+.01,b.getZ()-15.8,b.getX()+.8,b.getY()+1.8,b.getZ()-14.8);h.assertTrue(!l.noCollision(null,body),"painted sun boards physically block the real family-room entrance");var original=l.getBlockEntity(b.offset(UncannyRooms.source(place)));
                f.click(p,place,UncannyRooms.PARLOR_BOARD);h.assertTrue(l.isEmptyBlock(b.offset(0,0,-15))&&l.isEmptyBlock(b.offset(0,1,-15))&&l.noCollision(null,body)&&l.getBlockEntity(b.offset(UncannyRooms.source(place)))==original,"actual board removal clears both halves and preserves the original source");
                for(int z=-23;z>=-26;z--)h.assertTrue(l.noCollision(null,new AABB(bed.getX()-4.8,bed.getY()+5.01,bed.getZ()+z+.2,bed.getX()-4.2,bed.getY()+6.8,bed.getZ()+z+.8)),"a full native body has an open aisle between the monitoring desk and bed at "+z);
                h.assertTrue(l.isEmptyBlock(bed.offset(-6,5,-23))&&l.getBlockState(bed.offset(-5,5,-21)).getBlock() instanceof HouseholdFurnitureBlock&&l.getBlockState(bed.offset(-5,5,-21)).getValue(HouseholdFurnitureBlock.FACING)==Direction.NORTH,"one chair faces the straight CCTV desk; the overlapping third desk is absent");
                h.assertTrue(l.getBlockEntity(bed.offset(UncannyRooms.source(LabyrinthPlace.MISSING_SLEEP)))!=null&&l.getBlockState(bed.offset(UncannyRooms.BED_AT)).getBlock() instanceof net.minecraft.world.level.block.BedBlock,"the actual monitor/source and native bed remain usable originals");
            }).thenSucceed();
    }
    @AfterBatch(batch="review131_rooms") public static void roomsDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="review131_seating",timeoutTicks=360)
    public static void aBlockedFloorKeepsTheOriginalFamilySeatedThenTheyRiseInFrontPauseAndFlee(GameTestHelper h){
        sitting=new ClassicsTests.Fixture(h,58900,LabyrinthPlace.SEANCE);var f=sitting;var p=f.player("standing_floor_reader");List<UUID> ids=new ArrayList<>();var stand=f.b.offset(BlockPos.containing(SeanceSeating.STANDS.getFirst()));
        h.startSequence().thenIdle(20).thenExecute(()->{
            var cast=ClassicsVignettes.cast(f.l);h.assertTrue(cast.size()==4,"the four original people are present");for(var a:cast){ids.add(a.getUUID());h.assertTrue(a.seated()&&a.getVehicle() instanceof SeatEntity&&a.getVehicle().blockPosition().equals(f.b.offset(SeanceSeating.CHAIRS.get(a.role()))),"each original begins on its own native chair");}
            f.l.setBlock(stand,Blocks.GOLD_BLOCK.defaultBlockState(),3);f.click(p,ClassicsRooms.CUPBOARD);f.click(p,ClassicsRooms.CANDLES.getFirst());f.click(p,ClassicsRooms.CANDLES.get(1));
        }).thenIdle(12).thenExecute(()->{
            var a=ClassicsVignettes.cast(f.l).stream().filter(e->e.role()==0).findFirst().orElseThrow();h.assertTrue(a.seated()&&a.getVehicle() instanceof SeatEntity,"a blocked standing cell keeps the frightened person in the chair");f.l.setBlock(stand,Blocks.AIR.defaultBlockState(),3);
        }).thenWaitUntil(()->h.assertTrue(!ClassicsVignettes.cast(f.l).stream().filter(e->e.role()==0).findFirst().orElseThrow().seated(),"clearing the actual floor permits standing"))
            .thenExecute(()->{var a=ClassicsVignettes.cast(f.l).stream().filter(e->e.role()==0).findFirst().orElseThrow();h.assertTrue(a.position().distanceToSqr(SeanceSeating.STANDS.getFirst().add(f.b.getX(),f.b.getY(),f.b.getZ()))<.001&&Math.abs(a.getY()-f.b.getY())<.01&&a.getVehicle()==null&&f.l.noCollision(a,a.getBoundingBox()),"the original stands on supported floor in front of its chair and pauses before fleeing");})
            .thenWaitUntil(()->h.assertTrue(f.data().state(ClassicsVignettes.STATE).getBoolean("Fled0477"),"the same frightened family completes its original physical exit route"))
            .thenExecute(()->h.assertTrue(ClassicsVignettes.cast(f.l).stream().map(Entity::getUUID).toList().containsAll(ids),"flight preserves all four original identities")).thenSucceed();
    }
    @AfterBatch(batch="review131_seating") public static void seatingDone(ServerLevel l){close();}
}

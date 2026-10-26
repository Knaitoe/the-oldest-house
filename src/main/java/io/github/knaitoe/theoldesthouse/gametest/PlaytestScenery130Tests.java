package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.gametest.*;

/** Finite originals and physical access survive the three photographed furnishing repairs. */
@GameTestHolder(TheOldestHouse.MOD_ID+"_interior_review") @PrefixGameTestTemplate(false)
public final class PlaytestScenery130Tests {
    private static LiteraryTests.Fixture fixture;
    private static final Map<BlockPos,BlockEntity> originals=new HashMap<>();
    private static void close(){if(fixture!=null){fixture.close();fixture=null;}originals.clear();}
    @GameTest(template="empty",batch="scenery130_cabin",timeoutTicks=600)
    public static void cabinFurnitureHasRealSeatsAHollowSinkAndJarWithoutReplacingEitherOriginalBook(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);fixture=f;HouseSavedData.get(h.getLevel().getServer()).markInteriorInitialized();var scene=LabyrinthPlace.END_WORLD_CABIN;var l=f.level(scene);var b=f.base(scene);f.chunks.hold(l,SceneReview.area(b,scene));
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"cabin and entity sections resident")).thenExecute(()->{
            f.build(scene);ScenePolish.polishOnce(l,f.origin,scene);var source=b.offset(LiteraryRooms.source(scene));var account=b.offset(LiteraryRooms.ending(scene));originals.put(source,l.getBlockEntity(source));originals.put(account,l.getBlockEntity(account));
            h.assertTrue(originals.values().stream().allMatch(Objects::nonNull),"both finite reading surfaces really exist");
            var p=NativeTestPlayers.survival(h,"cabin130");f.players.add(p);p.teleportTo(l,b.getX()-8.5,b.getY(),b.getZ()-16.5,0,0);p.hasChangedDimension();p.setNoGravity(true);
            var seat=b.offset(-8,0,-17);h.assertTrue(VignetteInteriorBlock.is(l.getBlockState(seat),VignetteInteriorBlock.Kind.CABIN_STOOL)&&HouseSitting.sit(p,seat),"the replacement log stool uses the actual native sitting interaction");p.stopRiding();p.moveTo(b.getX()-7,b.getY(),b.getZ()-16);
            var sink=b.offset(-9,0,-21);h.assertTrue(VignetteInteriorBlock.is(l.getBlockState(sink),VignetteInteriorBlock.Kind.HOLIDAY_SINK),"the pictured cabin sink uses the corrected production model");
            h.assertTrue(l.noCollision(null,new AABB(sink.getX()+.3,sink.getY()+.83,sink.getZ()+.3,sink.getX()+.6,sink.getY()+.98,sink.getZ()+.6)),"the sink has an actual recessed open bowl, with no overlapping full worktop");
            h.assertTrue(VignetteInteriorBlock.interact(l,sink,p)==InteractionResult.CONSUME&&l.getBlockState(sink).getValue(VignetteInteriorBlock.ACTIVE),"the repaired tap still has native saved use");
            var jar=b.offset(-7,0,-9);h.assertTrue(l.getBlockState(jar).is(LiteraryRegistry.PROP.get())&&l.getBlockEntity(jar)!=null,"the original interactive jar and its block entity are retained");
            for(int z=-16;z>=-28;z--)h.assertTrue(l.noCollision(null,new AABB(b.getX()+.2,b.getY()+.01,b.getZ()+z+.2,b.getX()+.8,b.getY()+1.8,b.getZ()+z+.8)),"the cabin's real doorway-to-table route is unobstructed");
            f.reload();PlaytestScenery130.fresh(l,f.origin,scene);for(var e:originals.entrySet())h.assertTrue(l.getBlockEntity(e.getKey())==e.getValue(),"the original source/account block entity survives repeated repair and native reload");h.assertTrue(l.getBlockState(sink).getValue(VignetteInteriorBlock.ACTIVE),"the repaired scenery never switches off a used tap");
            try{ArchitectureTests.export(l,b,scene);java.nio.file.Files.copy(java.nio.file.Path.of("../build/architecture-proof",scene.id()+".json"),java.nio.file.Path.of("../build/interior-environment-proof",scene.id()+".json"),java.nio.file.StandardCopyOption.REPLACE_EXISTING);}catch(Exception e){throw new RuntimeException(e);}
        }).thenSucceed();
    }
    @AfterBatch(batch="scenery130_cabin") public static void cabinDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="scenery130_legacy",timeoutTicks=600)
    public static void quietRoomAndDumpsterRefitsPreserveOriginalSuppliesEditsWaterAndUsedLids(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);fixture=f;HouseSavedData.get(h.getLevel().getServer()).markInteriorInitialized();var quiet=LabyrinthPlace.QUIET_ROOM;var elk=LabyrinthPlace.ELK_LOT;var l=f.level(quiet);var b=f.base(quiet);var out=f.level(elk);var eb=f.base(elk);
        f.chunks.hold(l,SceneReview.area(b,quiet));f.chunks.hold(out,SceneReview.area(eb,elk));
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"both old rooms are resident")).thenExecute(()->{
            f.built.add(quiet);LabyrinthHalls.build(l,b,quiet);LabyrinthBuilder.registerDoors(f.data(),quiet,b);f.build(elk);
            var cache=b.offset(LabyrinthHalls.QUIET_CACHE);var note=b.offset(4,0,-13);originals.put(cache,l.getBlockEntity(cache));originals.put(note,l.getBlockEntity(note));((Container)originals.get(cache)).removeItem(0,1);
            l.setBlock(b.offset(-4,0,-11),Blocks.GOLD_BLOCK.defaultBlockState(),3);
        }).thenWaitUntil(()->h.assertTrue(PlaytestScenery130.step(l,f.origin,quiet)&&PlaytestScenery130.step(out,f.origin,elk),"the bounded recognized-state migrations finish"))
        .thenExecute(()->{
            for(var e:originals.entrySet())h.assertTrue(e.getValue()!=null&&l.getBlockEntity(e.getKey())==e.getValue(),"the exact finite cache and original note remain");
            h.assertTrue(((Container)originals.get(b.offset(LabyrinthHalls.QUIET_CACHE))).getItem(0).getCount()==1,"the repair never refills removed supplies");h.assertTrue(l.getBlockState(b.offset(4,0,-7)).is(Blocks.WATER_CAULDRON),"the original functional water basin remains");h.assertTrue(l.getBlockState(b.offset(-4,0,-11)).is(Blocks.GOLD_BLOCK),"a player's edited chair cell is never replaced");
            for(int z=-5;z>=-13;z--)h.assertTrue(l.noCollision(null,new AABB(b.getX()+.2,b.getY()+.01,b.getZ()+z+.2,b.getX()+.8,b.getY()+1.8,b.getZ()+z+.8)),"the reading and utility areas leave the actual central route clear");
            var p=NativeTestPlayers.survival(h,"bin130");f.players.add(p);p.teleportTo(out,eb.getX()-12,eb.getY(),eb.getZ()-29,0,0);p.hasChangedDimension();var at=eb.offset(-12,0,-31);
            h.assertTrue(VignetteInteriorBlock.is(out.getBlockState(at),VignetteInteriorBlock.Kind.DUMPSTER_LEFT)&&out.isEmptyBlock(at.above()),"steel bin replaces the exact green cube and wooden lid");h.assertTrue(VignetteInteriorBlock.interact(out,at,p)==InteractionResult.CONSUME&&out.getBlockState(at).getValue(VignetteInteriorBlock.ACTIVE),"the actual bin lid opens through native use");
            f.reload();PlaytestScenery130.step(out,f.origin,elk);h.assertTrue(out.getBlockState(at).getValue(VignetteInteriorBlock.ACTIVE),"the saved once-only repair retains an opened lid");
            try{ArchitectureTests.export(l,b,quiet);ArchitectureTests.export(out,eb,elk);}catch(Exception e){throw new RuntimeException(e);}
        }).thenSucceed();
    }
    @AfterBatch(batch="scenery130_legacy") public static void legacyDone(ServerLevel l){close();}
}

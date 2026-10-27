package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.*;
import java.nio.file.*;
import com.google.gson.*;
import com.mojang.serialization.JsonOps;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.gametest.*;

/** Real wind/voice packets, native door movement and confirmed personal House outcomes. */
@GameTestHolder(TheOldestHouse.MOD_ID+"_foyer") @PrefixGameTestTemplate(false)
public final class FoyerOutcomeTests {
    private static final LabyrinthPlace P=LabyrinthPlace.MOTION_LIGHT;
    private static LiteraryTests.Fixture active;
    @AfterBatch(batch="foyer136_admission") public static void admissionDone(ServerLevel l){close();}
    @AfterBatch(batch="foyer136_return") public static void returnDone(ServerLevel l){close();}
    @AfterBatch(batch="foyer136_routes") public static void routesDone(ServerLevel l){close();}
    private static void close(){if(active!=null){active.close();active=null;}}
    private static LiteraryTests.Fixture fixture(GameTestHelper h){var f=new LiteraryTests.Fixture(h);active=f;f.build(P);f.chunks.hold(f.in,IndianLakeRooms.bounds(f.base(P),P).inflate(2));f.chunks.hold(f.in,new AABB(f.origin).inflate(12));h.onEachTick(()->{for(var p:List.copyOf(f.players))if(LabyrinthDoors.isBusy(p))HouseTransitionEvents.onPlayerTick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(p));});return f;}
    private static ServerPlayer player(GameTestHelper h,LiteraryTests.Fixture f,String name){var p=f.player(name,P);p.connection.resetPosition();var from=f.origin.offset(0,0,6);for(var at:BlockPos.betweenClosed(from.offset(-5,-1,-5),from.offset(5,-1,5)))f.in.setBlock(at,Blocks.STONE.defaultBlockState(),18);NovelRooms.door(f.in,from,Direction.SOUTH,Blocks.DARK_OAK_DOOR,false);f.data().putDoor(new LabyrinthData.Door("foyer136/native/source",f.in.dimension(),from,Direction.SOUTH,"place:motion_light",false));f.data().pushReturn(p.getUUID(),new LabyrinthData.Waypoint(f.in.dimension(),Vec3.atBottomCenterOf(from),180,true));return p;}
    private static void tick(LiteraryTests.Fixture f,ServerPlayer p){var t=f.own(p,P);UncannyStories.reader(p,f.base(P),P,t);LiteraryVignettes.save(f.data(),p.getUUID(),P,t);}
    private static void use(ServerPlayer p,BlockPos at){p.gameMode.useItemOn(p,p.serverLevel(),p.getMainHandItem(),InteractionHand.MAIN_HAND,new BlockHitResult(at.getCenter(),Direction.WEST,at,false));}
    private static void ordinaryExit(LiteraryTests.Fixture f,ServerPlayer p){var door=f.data().door(P.entryDoorId());p.moveTo(door.lower.getX()+.5,door.lower.getY(),door.lower.getZ()-1.5);LabyrinthDoors.tickPlayer(p,f.origin);use(p,door.lower);p.move(net.minecraft.world.entity.MoverType.SELF,new Vec3(0,0,3));LabyrinthDoors.tickPlayer(p,f.origin);}

    @GameTest(template="empty",batch="foyer136_admission",timeoutTicks=1200)
    public static void actualWindAndBabblePrecedeTheOriginalAndOpeningTheGlassHasAPersonalConsequence(GameTestHelper h){
        var f=fixture(h);ServerPlayer[] p={null},peer={null};UUID[] original={null};Vec3[] peerAt={null};boolean[] babbleBefore={false},stepsBefore={false};
        h.onEachTick(()->{if(p[0]==null)return;var visitor=UncannyStories.actor(p[0],f.own(p[0],P),"CastVisitor");for(var packet:NativeTestPlayers.packets(p[0]))if(packet instanceof ClientboundSoundPacket sound&&visitor!=null&&visitor.phase()==0){if(sound.getSound().value()==UncannyRegistry.FOYER_BABBLE.get())babbleBefore[0]=true;if(sound.getSound().value()==UncannyRegistry.FOYER_STEPS.get())stepsBefore[0]=true;}});
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the actual native foyer and House return are resident")).thenExecute(()->{p[0]=player(h,f,"foyer_glass_reader");peer[0]=player(h,f,"foyer_uninvited_peer");f.at(p[0],P,.5,0,-11.5);f.at(peer[0],P,-1.5,0,-7.5);peerAt[0]=peer[0].position();p[0].setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.TORCH,37));})
        .thenWaitUntil(()->h.assertTrue(f.own(p[0],P).getBoolean("FoyerThreat136"),"a genuine wind lull, sensor timeout and unseen sounds reveal the original visitor"))
        .thenExecute(()->{h.assertTrue(babbleBefore[0]&&stepsBefore[0]&&!f.own(p[0],P).getBoolean("Read_Source")&&f.own(p[0],P).getBoolean("FoyerWindSeen136"),"actual private sound packets precede the visible body without archive or chain gates");original[0]=f.own(p[0],P).getUUID("CastVisitor");export(f,"foyer_standoff",-9,10,-25,-1,-1,6,-45);use(p[0],f.base(P).offset(FoyerEncounter.GLASS_DOOR));h.assertTrue(f.own(p[0],P).getBoolean("FoyerAdmitted136")&&f.in.getBlockState(f.base(P).offset(FoyerEncounter.GLASS_DOOR)).getValue(DoorBlock.OPEN)&&!f.own(peer[0],P).getBoolean("FoyerAdmitted136"),"opening the real glass door while holding belongings admits only that reader");export(f,"foyer_admitted",-4,4,-22,-9,-1,6,-45);})
        .thenWaitUntil(()->h.assertTrue(f.own(p[0],P).getBoolean("FoyerFailureReturned136")&&!LabyrinthDoors.isBusy(p[0]),"the actual intrusion ends with a confirmed native House return"))
        .thenExecute(()->{h.assertTrue(f.own(p[0],P).getUUID("CastVisitor").equals(original[0])&&f.data().returnDepth(p[0].getUUID())==0&&p[0].getHealth()==20&&p[0].getMainHandItem().getCount()==37,"the original visitor and carried stack survive the failure, which consumes only the completed native return");h.assertTrue(!WitnessAccount.has(f.data(),p[0].getUUID(),WitnessAccount.Story.MOTION_LIGHT)&&!WitnessAccount.has(f.data(),peer[0].getUUID(),WitnessAccount.Story.MOTION_LIGHT)&&peer[0].position().distanceToSqr(peerAt[0])<.01,"failure earns no survival and moves no peer");}).thenSucceed();
    }

    @GameTest(template="empty",batch="foyer136_return",timeoutTicks=1200)
    public static void actualServiceDoorAndConfirmedHouseLandingResolveWhileOrdinaryRetreatDoesNot(GameTestHelper h){
        var f=fixture(h);ServerPlayer[] p={null},peer={null};
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"native foyer routes are resident")).thenExecute(()->{p[0]=player(h,f,"foyer_service_reader");peer[0]=player(h,f,"foyer_early_retreat");ordinaryExit(f,peer[0]);})
        .thenWaitUntil(()->h.assertTrue(f.data().returnDepth(peer[0].getUUID())==0&&!LabyrinthDoors.isBusy(peer[0]),"ordinary unfinished retreat lands at its actual House source"))
        .thenExecute(()->h.assertTrue(!WitnessAccount.has(f.data(),peer[0].getUUID(),WitnessAccount.Story.MOTION_LIGHT),"an early ordinary retreat is not the successful standoff"))
        .thenWaitUntil(()->h.assertTrue(f.own(p[0],P).getBoolean("FoyerThreat136"),"the reader actually experiences the first windless standoff"))
        .thenExecute(()->{var b=f.base(P);f.at(p[0],P,3.2,0,-4.5);tick(f,p[0]);use(p[0],b.offset(UncannyRooms.HINGED));p[0].move(net.minecraft.world.entity.MoverType.SELF,new Vec3(3.7,0,0));LabyrinthDoors.tickPlayer(p[0],f.origin);use(p[0],b.offset(FoyerEncounter.EXIT));p[0].move(net.minecraft.world.entity.MoverType.SELF,new Vec3(2.6,0,0));h.assertTrue(!WitnessAccount.has(f.data(),p[0].getUUID(),WitnessAccount.Story.MOTION_LIGHT),"touching and crossing the exit confer nothing before the actual House landing");export(f,"foyer_service_exit",3,10,-9,-4,-1,4,-45);LabyrinthDoors.tickPlayer(p[0],f.origin);})
        .thenWaitUntil(()->h.assertTrue(f.own(p[0],P).getBoolean("FoyerServiceReturned136")&&!LabyrinthDoors.isBusy(p[0]),"the real service exit confirms its native House landing"))
        .thenExecute(()->{h.assertTrue(WitnessAccount.has(f.data(),p[0].getUUID(),WitnessAccount.Story.MOTION_LIGHT)&&f.own(p[0],P).getBoolean("Completed")&&!f.own(p[0],P).getBoolean("Read_Source")&&f.data().returnDepth(p[0].getUUID())==0,"the actual escape alone grants the personal account without a paper checklist");h.assertTrue(!WitnessAccount.has(f.data(),peer[0].getUUID(),WitnessAccount.Story.MOTION_LIGHT)&&p[0].getHealth()==20,"a peer and a health payment cannot substitute for this reader's successful return");}).thenSucceed();
    }

    @GameTest(template="empty",batch="foyer136_routes",timeoutTicks=1500)
    public static void savedRouteAdditionsPreserveOriginalsAndActuallyEnteringTheUnhingedHallLoses(GameTestHelper h){
        var f=fixture(h);ServerPlayer[] p={null};net.minecraft.world.level.block.entity.BlockEntity[] paper={null};UUID[] original={null};
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"saved foyer footprint is resident")).thenExecute(()->{p[0]=player(h,f,"foyer_false_hall");paper[0]=f.in.getBlockEntity(f.base(P).offset(UncannyRooms.source(P)));original[0]=f.own(p[0],P).getUUID("CastVisitor");f.in.setBlock(f.base(P).offset(FoyerEncounter.EXIT),UncannyRegistry.material("lobby_wall").defaultBlockState(),18);f.in.setBlock(f.base(P).offset(FoyerEncounter.EXIT.above()),UncannyRegistry.material("lobby_wall").defaultBlockState(),18);f.in.setBlock(f.base(P).offset(-6,0,-8),Blocks.GOLD_BLOCK.defaultBlockState(),18);for(int y=0;y<2;y++)f.in.setBlock(f.base(P).offset(4,y,-11),UncannyPropBlock.of(UncannyPropBlock.Kind.FALSE_DOOR,Direction.SOUTH).setValue(UncannyPropBlock.STAGE,2+y),18);h.assertTrue(!FoyerRefit.step(f.in,f.origin),"a present native camera pauses the actual saved route additions");f.at(p[0],P,80,0,-5);})
        .thenWaitUntil(()->h.assertTrue(FoyerRefit.step(f.in,f.origin),"the bounded, unwatched recognized-cell additions complete"))
        .thenExecute(()->{h.assertTrue(f.in.getBlockEntity(f.base(P).offset(UncannyRooms.source(P)))==paper[0]&&f.in.getBlockState(f.base(P).offset(-6,0,-8)).is(Blocks.GOLD_BLOCK)&&f.in.getBlockState(f.base(P).offset(FoyerEncounter.EXIT)).getBlock() instanceof DoorBlock,"the new return route preserves the exact original surface and a player's edit");for(int y=0;y<2;y++){var frame=f.in.getBlockState(f.base(P).offset(4,y,-11));h.assertTrue(frame.getValue(UncannyPropBlock.FACING)==Direction.EAST&&frame.getValue(UncannyPropBlock.STAGE)==2+y,"the recognized legacy east frame retains both open stages while facing its supported passage");}f.at(p[0],P,.5,0,-10.5);})
        .thenWaitUntil(()->h.assertTrue(f.own(p[0],P).getBoolean("FoyerThreat136"),"the reader experiences a real dark standoff"))
        .thenExecute(()->{f.at(p[0],P,-3.0,0,-3.5);p[0].setYRot(180);p[0].setYHeadRot(180);tick(f,p[0]);var door=f.base(P).offset(UncannyRooms.FALSE_DOORS.getFirst());h.assertTrue(f.in.getBlockState(door).getValue(UncannyPropBlock.STAGE)>=2,"the actual unhinged leaf opens onto its bounded hallway");export(f,"foyer_unhinged_hall",-9,-2,-5,-3,-1,3,-45);p[0].move(net.minecraft.world.entity.MoverType.SELF,new Vec3(-2.2,0,0));tick(f,p[0]);h.assertTrue(f.own(p[0],P).getString("FoyerLastFailure136").equals("entered_the_unhinged_hall"),"actual native crossing chooses the dangerous hallway");})
        .thenWaitUntil(()->h.assertTrue(f.own(p[0],P).getBoolean("FoyerFailureReturned136")&&!LabyrinthDoors.isBusy(p[0]),"the wrong hallway also concludes with a confirmed native House return"))
        .thenExecute(()->{f.reload();h.assertTrue(f.own(p[0],P).getUUID("CastVisitor").equals(original[0])&&!WitnessAccount.has(f.data(),p[0].getUUID(),WitnessAccount.Story.MOTION_LIGHT)&&p[0].getHealth()==20,"reload retains the original visitor, real failure and unspent personal resolution");}).thenSucceed();
    }
    private static void export(LiteraryTests.Fixture f,String name,int x0,int x1,int z0,int z1,int y0,int y1,float yaw){
        var l=f.level(P);var b=f.base(P);var file=new JsonObject();file.addProperty("name",name);file.addProperty("viewYaw",yaw);
        var palette=new JsonArray();var blocks=new JsonArray();Map<BlockState,Integer> states=new LinkedHashMap<>();
        for(var rel:BlockPos.betweenClosed(x0,y0,z0,x1,y1,z1)){
            var at=b.offset(rel);var s=l.getBlockState(at);if(s.isAir()||s.is(Blocks.BARRIER)||s.is(Blocks.LIGHT))continue;
            int x=rel.getX(),y=rel.getY(),z=rel.getZ();if(y>y0&&(x==x1||z==z1||y==y1))continue;
            if(y==5&&z>=-14&&s.is(Blocks.BIRCH_PLANKS))continue;
            int index=states.computeIfAbsent(s,k->{palette.add(BlockState.CODEC.encodeStart(JsonOps.INSTANCE,k).getOrThrow());return states.size();});
            var row=new JsonArray();row.add(x);row.add(y);row.add(z);row.add(index);
            if(l.getBlockEntity(at) instanceof UncannyPropBlock.Fitting fit)row.add(fit.swing.sample(l.getGameTime()).angle());
            blocks.add(row);
        }
        file.add("palette",palette);file.add("blocks",blocks);
        try{var folder=Path.of("../build/foyer136-proof");Files.createDirectories(folder);Files.writeString(folder.resolve(name+".json"),new Gson().toJson(file));}catch(java.io.IOException e){throw new IllegalStateException(e);}
    }

}

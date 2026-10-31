package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import io.github.knaitoe.theoldesthouse.network.BurnEmbersPayload;
import io.netty.buffer.Unpooled;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(TheOldestHouse.MOD_ID+"_exploration")
@PrefixGameTestTemplate(false)
public final class ExplorationPassTests {
    @GameTest(template="empty")
    public static void outdoorArrivalHasGroundAndKeepsItsNativeReturnCollision(GameTestHelper h){
        var l=h.getLevel();var b=h.absolutePos(BlockPos.ZERO).offset(3700,8,500);
        for(var rel:List.of(new BlockPos(-7,0,10),new BlockPos(7,7,16))){
            var old=Blocks.WHITE_TERRACOTTA.defaultBlockState();var next=ScenePlaytestRepairs.arrivalSkin(rel,old);
            h.assertTrue(next.is(LiteraryRegistry.SIDING.get())&&old.getCollisionShape(l,b.offset(rel)).equals(next.getCollisionShape(l,b.offset(rel))),"the exposed white shell becomes siding with the same native full-block collision");
        }
        var protectedWall=new BlockPos(2,2,10);h.assertTrue(ScenePlaytestRepairs.arrivalSkin(protectedWall,Blocks.WHITE_TERRACOTTA.defaultBlockState()).is(Blocks.WHITE_TERRACOTTA),"the central copied return passage remains exact");
        var edit=b.offset(9,-1,9);l.setBlock(edit,Blocks.DIAMOND_BLOCK.defaultBlockState(),2);ScenePlaytestRepairs.arrivalGround(l,b);
        h.assertTrue(l.getBlockState(b.offset(8,-1,9)).isCollisionShapeFullBlock(l,b.offset(8,-1,9))&&l.getBlockState(b.offset(8,-2,9)).is(Blocks.DIRT)&&l.getBlockState(edit).is(Blocks.DIAMOND_BLOCK),"the gap beside the arrival has rooted ground and retains player edits");h.succeed();
    }
    @GameTest(template="empty")
    public static void sevenForwardPassesOfferOnlyStoneOrdinaryHallsAndRetainDrawnMaps(GameTestHelper h){
        var d=new LabyrinthData();var room=LabyrinthPlace.CROSS_HALL;LabyrinthBuilder.registerDoors(d,room,new BlockPos(0,80,0));
        for(int at:new int[]{7,12,20}){
            var p=UUID.randomUUID();depth(d,p,at);
            for(int seed=0;seed<300;seed++){
                d.setDryDeals(p,0);LabyrinthDealer.dealPlace(d,p,room,RandomSource.create(seed));
                for(var id:map(d,p,room).values()){var destination=LabyrinthPlace.byId(id);
                    if(LabyrinthPacing.ordinary(destination))h.assertTrue(StoneHalls.isStone(destination),"new ordinary halls are stone at depth "+at+": "+id);
                }
            }
        }
        var early=UUID.randomUUID();depth(d,early,6);int wooden=0;
        for(int seed=0;seed<100;seed++){LabyrinthDealer.dealPlace(d,early,room,RandomSource.create(seed));for(var id:map(d,early,room).values())if(LabyrinthPacing.ordinary(LabyrinthPlace.byId(id)))wooden++;}
        h.assertTrue(wooden>0&&!LabyrinthDealer.grayAvailable(d,early).stream().anyMatch(StoneHalls::isStone),"the approach still has domestic halls before seven forward passes");
        for(var p:LabyrinthPlace.values())if(StoneHalls.isStone(p))h.assertTrue(LabyrinthBuilder.requiredDepth(p)<=7,"stone construction happens ahead of its availability");
        h.succeed();
    }
    @GameTest(template="empty")
    public static void legacyCabinRepairOpensTheLadderAndPreservesItsNativeContents(GameTestHelper h){
        var l=h.getLevel();var b=h.absolutePos(BlockPos.ZERO).offset(3400,8,500);
        for(int y=-5;y<=-1;y++){l.setBlock(b.offset(0,y,-18),Blocks.DIRT.defaultBlockState(),2);l.setBlock(b.offset(0,y,-17),Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.SOUTH),2);}
        l.setBlock(b.offset(0,-2,-17),Blocks.STONE_BRICKS.defaultBlockState(),2);l.setBlock(b.offset(0,-1,-17),Blocks.STONE_BRICKS.defaultBlockState(),2);
        var chest=b.offset(11,0,-10);l.setBlock(chest,Blocks.BARREL.defaultBlockState(),2);
        var inventory=(net.minecraft.world.level.block.entity.BarrelBlockEntity)l.getBlockEntity(chest);inventory.setItem(0,new ItemStack(Items.PAPER,3));
        var edit=b.offset(-6,-1,-18);l.setBlock(edit,Blocks.DIAMOND_BLOCK.defaultBlockState(),2);
        ScenePlaytestRepairs.cabin(l,b);ScenePlaytestRepairs.cabin(l,b);
        for(int y=-5;y<=-1;y++)h.assertTrue(l.getBlockState(b.offset(0,y,-17)).is(Blocks.LADDER)&&l.getBlockState(b.offset(0,y,-17)).canSurvive(l,b.offset(0,y,-17)),"the full cellar ladder has native support at "+y);
        h.assertTrue(l.getBlockState(b.offset(0,4,-24)).is(Blocks.CALCITE)&&!l.getBlockState(b.offset(0,2,-24)).isAir(),"the cabin partition meets its ceiling and each door has a header");
        h.assertTrue(l.getBlockEntity(chest)==inventory&&inventory.getItem(0).getCount()==3&&l.getBlockState(edit).is(Blocks.DIAMOND_BLOCK),"targeted retries keep native containers, finite contents and player edits");h.succeed();
    }
    @GameTest(template="empty")
    public static void nativePanesJoinTheirFrameWithoutRebuildingIt(GameTestHelper h){
        var l=h.getLevel();var b=h.absolutePos(BlockPos.ZERO).offset(3450,8,500);
        l.setBlock(b.west(),Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState(),2);l.setBlock(b.east(2),Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState(),2);
        BuildBlocks.box(l,b,b.east(),Blocks.GLASS_PANE.defaultBlockState(),18);
        for(var at:List.of(b,b.east())){var joined=l.getBlockState(at);h.assertTrue(joined.getValue(IronBarsBlock.WEST)&&joined.getValue(IronBarsBlock.EAST)&&joined.equals(ScenePlaytestRepairs.connected(l,at)),"each placed native pane already joins both its neighbour and frame");}
        h.assertTrue(l.getBlockState(b.west()).is(Blocks.STRIPPED_SPRUCE_LOG),"joining panes preserves the existing window frame");h.succeed();
    }
    @GameTest(template="empty")
    public static void playtestLogIsOptInLocalAndHidesPlayerIds(GameTestHelper h){
        var p=NativeTestPlayers.survival(h,"playtest_reader");java.nio.file.Path file=null;
        try{
            h.assertTrue(!io.github.knaitoe.theoldesthouse.house.PlaytestLog.enabled(),"the playtest log is off unless the server asks for it");
            file=java.nio.file.Files.createTempFile("playtest-log",".jsonl");io.github.knaitoe.theoldesthouse.house.PlaytestLog.overrideForTesting(file);
            io.github.knaitoe.theoldesthouse.house.PlaytestLog.refused(p,"door_sticks");
            io.github.knaitoe.theoldesthouse.house.PlaytestLog.event(p,"arrive","place","playtest_marker","depth",3,"story",false);
            io.github.knaitoe.theoldesthouse.house.PlaytestLog.flush();
            var lines=java.nio.file.Files.readAllLines(file);String raw=p.getUUID().toString();
            h.assertTrue(lines.stream().noneMatch(line->line.contains(raw)),"player UUIDs are replaced by one-way hashes");
            var arrive=lines.stream().map(line->com.google.gson.JsonParser.parseString(line).getAsJsonObject())
                    .filter(o->"playtest_marker".equals(o.has("place")?o.get("place").getAsString():"")).findFirst().orElse(null);
            h.assertTrue(arrive!=null&&"arrive".equals(arrive.get("event").getAsString())&&arrive.get("depth").getAsInt()==3&&!arrive.get("story").getAsBoolean()
                    &&arrive.has("time")&&arrive.has("gameTime")&&arrive.get("player").getAsString().length()==12,"each event is one JSON line with times, a hashed player and typed fields: "+lines);
            h.assertTrue(lines.stream().anyMatch(line->line.contains("\"refused\"")&&line.contains("\"door_sticks\"")),"refusals record their gate");
            h.assertTrue(arrive.get("dimension").getAsString().equals(p.serverLevel().dimension().location().toString())
                    &&arrive.has("inLabyrinth")&&arrive.has("activity")&&arrive.has("session"),"dimension, occupancy and connection identity accompany every event");
            String connection=arrive.get("session").getAsString();
            io.github.knaitoe.theoldesthouse.house.PlaytestLog.logout(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent(p));
            io.github.knaitoe.theoldesthouse.house.PlaytestLog.login(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent(p));
            io.github.knaitoe.theoldesthouse.house.PlaytestLog.flush();
            var starts=java.nio.file.Files.readAllLines(file).stream().map(line->com.google.gson.JsonParser.parseString(line).getAsJsonObject())
                    .filter(o->"session_start".equals(o.get("event").getAsString())).toList();
            h.assertTrue(starts.size()==2&&!starts.get(1).get("session").getAsString().equals(connection),"the same player reconnects with a distinct connection boundary");
            h.assertTrue("overworld".equals(io.github.knaitoe.theoldesthouse.house.PlaytestLog.place(p))
                    &&!io.github.knaitoe.theoldesthouse.house.PlaytestLog.inLabyrinth(p),"Overworld coordinates cannot masquerade as an interior story room");

            h.succeed();
        }catch(java.io.IOException e){throw new RuntimeException(e);}
        finally{io.github.knaitoe.theoldesthouse.house.PlaytestLog.overrideForTesting(null);NativeTestPlayers.remove(p);if(file!=null)try{java.nio.file.Files.deleteIfExists(file);}catch(java.io.IOException ignored){}}
    }
    private static String accountText(LabyrinthData data,UUID player){
        return WitnessAccount.book(data,player,"reader",false).get(net.minecraft.core.component.DataComponents.WRITTEN_BOOK_CONTENT)
                .pages().stream().map(page->page.raw().getString()).collect(java.util.stream.Collectors.joining("\n"));
    }
    @GameTest(template="empty")
    public static void doorHelpUsesARollingRealMinuteAndResetsAcrossGatesAndLogout(GameTestHelper h){
        var p=NativeTestPlayers.survival(h,"door_hint_reader");
        try{
            var id=p.getUUID();
            h.assertTrue(LabyrinthDoors.constructionAttempts(id,"door_a",0)==1
                    &&LabyrinthDoors.constructionAttempts(id,"door_a",59_000_000_000L)==2
                    &&LabyrinthDoors.constructionAttempts(id,"door_a",60_100_000_000L)==2
                    &&LabyrinthDoors.constructionAttempts(id,"door_a",61_000_000_000L)==3,
                    "three attempts at 59, 60.1 and 61 seconds still earn help across the first-minute boundary");
            h.assertTrue(LabyrinthDoors.constructionAttempts(id,"door_b",62_000_000_000L)==1,"another physical door starts its own run");
            LabyrinthDoors.onPlayerLoggedOut(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent(p));
            h.assertTrue(LabyrinthDoors.constructionAttempts(id,"door_b",63_000_000_000L)==1,"logout clears this connection's attempts");h.succeed();
        }finally{LabyrinthDoors.onPlayerLoggedOut(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent(p));NativeTestPlayers.remove(p);}
    }
    @GameTest(template="empty")
    public static void witnessAccountKeepsPersonalBeginningsAndMutableProgressWithoutCredit(GameTestHelper h){
        var p=NativeTestPlayers.survival(h,"account_reader");var peer=NativeTestPlayers.survival(h,"account_peer");
        var spectator=NativeTestPlayers.survival(h,"account_camera");spectator.setGameMode(GameType.SPECTATOR);
        try{
            var data=LabyrinthData.get(p.server);
            VignetteGate.begin(p,LabyrinthPlace.HIDE_AND_CLAP);VignetteGate.begin(peer,LabyrinthPlace.HARRIGAN);
            VignetteGate.begin(spectator,LabyrinthPlace.ELK_FAN);VignetteGate.begin(p,LabyrinthPlace.KAREN_ROOM);
            h.assertTrue(WitnessAccount.count(data,p.getUUID())==0&&!WitnessAccount.ready(data,p.getUUID()),"a beginning never grants a resolution or unlock");
            h.assertTrue(WitnessAccount.begun(data,p.getUUID(),WitnessAccount.Story.CLAP)
                    &&!WitnessAccount.begun(data,peer.getUUID(),WitnessAccount.Story.CLAP)
                    &&!WitnessAccount.begun(data,spectator.getUUID(),WitnessAccount.Story.ELK_FAN),"beginnings are personal and observers earn none");
            data.visit(p.getUUID(),LabyrinthPlace.MINIATURES);for(int i=0;i<9;i++)data.visit(p.getUUID(),LabyrinthPlace.CROSS_HALL);
            h.assertTrue(WitnessAccount.begun(data,p.getUUID(),WitnessAccount.Story.MINIATURES),"actual older discoveries survive the last-eight-crossings cache");
            String unfinished=accountText(data,p.getUUID());
            h.assertTrue(unfinished.contains("The wardrobe")&&unfinished.contains("The little rooms")&&unfinished.contains("unfinished")
                    &&!unfinished.contains("The study")&&!unfinished.contains("karen_room"),"unfinished prose names only this reader's actual Witness beginnings");
            WitnessAccount.resolve(p,WitnessAccount.Story.FLOORBOARDS,"read");
            String first=accountText(data,p.getUUID());
            h.assertTrue(first.contains("one room to the end")&&first.contains("OTHER ENDINGS"),"a prose count and another-kind hint come from real resolutions");
            WitnessAccount.resolve(p,WitnessAccount.Story.HARRIGAN,"listened");
            String second=accountText(data,p.getUUID());
            h.assertTrue(second.contains("two rooms to the end")&&!second.contains("OTHER ENDINGS")
                    &&second.contains("The wardrobe")&&WitnessAccount.count(data,peer.getUUID())==0,"the mutable account advances without crediting a peer or dropping unfinished entries");
            var traded=WitnessAccount.book(data,p.getUUID(),"reader",false);peer.getInventory().setItem(0,traded.copy());WitnessAccount.updateBook(peer,false);
            h.assertTrue(ItemStack.isSameItemSameComponents(traded,peer.getInventory().getItem(0)),"a borrowed account keeps its owner's components");
            var loaded=LabyrinthData.FACTORY.deserializer().apply(data.save(new CompoundTag(),h.getLevel().registryAccess()),h.getLevel().registryAccess());
            h.assertTrue(second.equals(accountText(loaded,p.getUUID())),"native save/reload preserves evidence and beginnings");
            h.succeed();
        }finally{NativeTestPlayers.remove(p);NativeTestPlayers.remove(peer);NativeTestPlayers.remove(spectator);}
    }
    @GameTest(template="empty")
    public static void witnessProgressMilestonesFollowTheDerivedQuotaAndKeepTheFinalGate(GameTestHelper h){
        var data=new LabyrinthData();var id=UUID.randomUUID();int needed=WitnessAccount.REQUIRED;
        for(int i=0;i<needed-1;i++)WitnessAccount.resolve(data,id,WitnessAccount.Story.values()[i],"heard");
        String near=accountText(data,id);
        h.assertTrue(near.contains("Only a few gaps remain")&&!near.contains("Crouch and open the bars")
                &&!WitnessAccount.ready(data,id),"near-completion prose never unlocks the cell early");
        WitnessAccount.resolve(data,id,WitnessAccount.Story.values()[needed-1],"heard");
        String ready=accountText(data,id);
        h.assertTrue(WitnessAccount.ready(data,id)&&ready.contains("The account holds together")
                &&ready.contains("Crouch and open the bars"),"the original derived quota and real kinds still govern the final passage");
        h.assertTrue(WitnessAccount.Story.values().length==56&&needed==42,"six personally resolvable uncanny sources retain the derived seventy-five-percent quota");h.succeed();
    }
    @GameTest(template="empty")
    public static void tomKeepsOneFiniteOriginalWhenItsReaderStoresOrTradesTheLighter(GameTestHelper h){
        var p=NativeTestPlayers.survival(h,"lighter_reader");var peer=NativeTestPlayers.survival(h,"lighter_peer");
        var actor=NovelRegistry.ACTOR.get().create(p.serverLevel());
        try{
            h.assertTrue(actor!=null,"Tom is a native actor");actor.appearance(p.getUUID(),0);actor.moveTo(p.position());p.serverLevel().addFreshEntity(actor);
            NovelVignettes.meetTom(p,actor);
            ItemStack original=ItemStack.EMPTY;
            for(int i=0;i<p.getInventory().getContainerSize();i++)if(p.getInventory().getItem(i).is(NovelRegistry.LIGHTER.get())){
                original=p.getInventory().getItem(i).copy();p.getInventory().setItem(i,ItemStack.EMPTY);break;
            }
            h.assertTrue(!original.isEmpty(),"the actual handoff gives the one original lighter");
            peer.getInventory().setItem(0,original.copy());for(int i=0;i<4;i++)NovelVignettes.meetTom(p,actor);
            h.assertTrue(!p.getInventory().hasAnyMatching(stack->stack.is(NovelRegistry.LIGHTER.get()))
                    &&ItemStack.isSameItemSameComponents(original,peer.getInventory().getItem(0)),"an empty carried inventory does not mint replacements while a peer holds the original");
            var dropped=peer.drop(peer.getInventory().getItem(0).copy(),false);peer.getInventory().setItem(0,ItemStack.EMPTY);NovelVignettes.meetTom(p,actor);
            h.assertTrue(dropped!=null&&!p.getInventory().hasAnyMatching(stack->stack.is(NovelRegistry.LIGHTER.get()))
                    &&ItemStack.isSameItemSameComponents(original,dropped.getItem()),"the dropped original remains real and no duplicate appears");dropped.discard();h.succeed();
        }finally{if(actor!=null)actor.discard();NativeTestPlayers.remove(p);NativeTestPlayers.remove(peer);}
    }
    @GameTest(template="empty")
    public static void swimmingChannelHasTwoBlockHeadroomAndDryArrival(GameTestHelper h){
        var l=h.getLevel();var b=h.absolutePos(BlockPos.ZERO).offset(3550,8,500);LabyrinthHazards.buildFloodedPassage(l,b);
        for(var feet:LabyrinthHazards.floodRoute(b)){
            var body=new AABB(feet.getX()+.2,feet.getY()+.01,feet.getZ()+.2,feet.getX()+.8,feet.getY()+1.81,feet.getZ()+.8);
            h.assertTrue(l.noCollision(null,body),"the submerged route has native body clearance at "+feet);
        }
        h.runAfterDelay(40,()->{h.assertTrue(l.getBlockState(b.offset(0,0,0)).getFluidState().isEmpty(),"the source-water channel cannot flood the arrival doorway");
            for(int x=-7;x<=7;x++){
                for(int z=-2;z<=-1;z++)h.assertTrue(l.getBlockState(b.offset(x,0,z)).getFluidState().isEmpty(),"the dry arrival stays dry at "+x+","+z);
                if(x<0||x>1)h.assertTrue(l.getBlockState(b.offset(x,0,-27)).getFluidState().isEmpty(),"the far landing stays dry at "+x);
            }h.succeed();});
    }
    @GameTest(template="empty")
    public static void childDistanceMakesTheSavedOccupiedClockGrowlAndShakeMoreOften(GameTestHelper h){
        h.assertTrue(StaircaseAtmosphere.growlPeriod(0)==30&&StaircaseAtmosphere.growlPeriod(1280)==60&&StaircaseAtmosphere.shakePeriod(0)==30&&StaircaseAtmosphere.shakePeriod(1280)==60,"growls are slower and the shake uses the same cue");
        var clock=new CompoundTag();int pulses=0;for(int i=0;i<90;i++)if(StaircaseAtmosphere.occupiedSecond(clock,30))pulses++;
        h.assertTrue(pulses==3&&StaircaseAtmosphere.growlVolume(0)>StaircaseAtmosphere.growlVolume(1280),"occupied seconds, rather than the number of peers, govern a louder nearby growl");h.succeed();
    }
    @GameTest(template="empty",batch="staircase_debris",timeoutTicks=400)
    public static void neighboringReadersShareOneNativeFallingBlockWithoutOpeningTheShaft(GameTestHelper h){
        var l=HouseTestLevel.get(h.getLevel().getServer());var origin=new BlockPos(610000,0,610000);var base=FinaleArchitecture.base(origin);var at=base.offset(24,FinaleArchitecture.TOP-128,0);
        var lease=new NativeTestChunks();lease.hold(l,new AABB(at).inflate(15,12,6));
        var a=NativeTestPlayers.survival(h,"debris_a");var b=NativeTestPlayers.survival(h,"debris_b");var observer=NativeTestPlayers.survival(h,"debris_observer");observer.setGameMode(GameType.SPECTATOR);
        var walls=new ArrayList<BlockPos>();var touched=new HashSet<BlockPos>();
        Runnable cleanup=()->{l.getEntitiesOfClass(net.minecraft.world.entity.item.FallingBlockEntity.class,new AABB(at).inflate(20),e->e.getTags().contains(StaircaseDebris.TAG)).forEach(net.minecraft.world.entity.item.FallingBlockEntity::discard);for(var cell:touched)l.setBlock(cell,Blocks.AIR.defaultBlockState(),2);NativeTestPlayers.remove(a);NativeTestPlayers.remove(b);NativeTestPlayers.remove(observer);lease.close();};
        net.minecraft.world.entity.item.FallingBlockEntity[] piece={null};long[] started={-1};boolean[] motion={false},done={false};double[] initialY={0};
        h.runAfterDelay(399,()->{if(!done[0]){done[0]=true;cleanup.run();}});
        h.onEachTick(()->{if(done[0]||!lease.ready())return;
            try{
                if(started[0]<0){
                    for(int y=3;y<=8;y++)for(int z=-2;z<=2;z++){var wall=base.offset(34,at.getY()+y,z);walls.add(wall);touched.add(wall);touched.add(wall.east());l.setBlock(wall,Blocks.DEEPSLATE_TILES.defaultBlockState(),2);}
                    for(int x=22;x<=35;x++)for(int z=-3;z<=3;z++){var floor=base.offset(x,at.getY()-2,z);touched.add(floor);l.setBlock(floor,Blocks.STONE.defaultBlockState(),2);}
                    for(var p:List.of(a,b,observer)){p.teleportTo(l,at.getX()+.5,at.getY()+(p==b?1:0),at.getZ()+.5,0,0);p.connection.resetPosition();}
                    h.assertTrue(StaircaseDebris.fallForPlayers(l,origin,List.of(a,b,observer))==1,"two living neighbors across the clock boundary share one block; observers add none");
                    var falling=l.getEntitiesOfClass(net.minecraft.world.entity.item.FallingBlockEntity.class,new AABB(at).inflate(20),e->e.getTags().contains(StaircaseDebris.TAG));h.assertTrue(falling.size()==1,"one authoritative native falling entity is visible to both readers");piece[0]=falling.getFirst();initialY[0]=piece[0].getY();
                    var broken=walls.stream().filter(cell->l.getBlockState(cell).isAir()).toList();h.assertTrue(broken.size()==1&&l.getBlockState(broken.getFirst().east()).isCollisionShapeFullBlock(l,broken.getFirst().east()),"one real wall cell breaks and a solid backing retains the enclosure");started[0]=l.getGameTime();return;
                }
                if(l.getGameTime()-started[0]>=6&&!motion[0]){h.assertTrue(!piece[0].isRemoved()&&piece[0].getY()<initialY[0]&&piece[0].getDeltaMovement().y<0,"the native block actually falls clear of the wall face");motion[0]=true;}
                if(l.getGameTime()-started[0]>=40){
                    h.assertTrue(piece[0].isRemoved()&&l.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new AABB(at).inflate(20)).isEmpty(),"landing does not place an obstruction or create free drops");h.assertTrue(a.getHealth()==20&&b.getHealth()==20,"debris does not hurt either reader");
                    for(var wall:walls)l.setBlock(wall,Blocks.DIAMOND_BLOCK.defaultBlockState(),2);h.assertTrue(StaircaseDebris.fallForPlayers(l,origin,List.of(a,b,observer))==0&&walls.stream().allMatch(cell->l.getBlockState(cell).is(Blocks.DIAMOND_BLOCK)),"player-edited walls cannot become debris");done[0]=true;cleanup.run();h.succeed();
                }
            }catch(RuntimeException|Error failure){done[0]=true;cleanup.run();throw failure;}
        });
    }
    private static final List<LabyrinthPlace> HALLS=List.of(LabyrinthPlace.ALCOVE_HALL,LabyrinthPlace.OFFSET_HALL,LabyrinthPlace.SERVICE_LANDING,
            LabyrinthPlace.STONE_ARCADE,LabyrinthPlace.STONE_BEND,LabyrinthPlace.STONE_LANDING);
    private static Set<BlockPos> walk(ServerLevel l,BlockPos start) {
        var out=new HashSet<BlockPos>();var queue=new ArrayDeque<BlockPos>();out.add(start);queue.add(start);
        while(!queue.isEmpty()) {
          BlockPos from=queue.removeFirst();
          for(var side:Direction.Plane.HORIZONTAL) {
            BlockPos at=from.relative(side);
            var body=new AABB(at.getX()+.2,at.getY()+.01,at.getZ()+.2,at.getX()+.8,at.getY()+1.81,at.getZ()+.8);
            if(!out.contains(at)&&l.getBlockState(at.below()).isCollisionShapeFullBlock(l,at.below())&&l.noCollision(null,body)) {out.add(at);queue.add(at);}
          }
        }
        return out;
    }
    @GameTest(template="empty")
    public static void sixHallwaysHaveRealRoutesSupportedLampsAndRoomForTwoBodies(GameTestHelper h)throws Exception {
        halls(h,HALLS,2100);
        h.succeed();
    }
    /**
     * 0.4.197 (owner: "Design more hallways that are just hallways."): the four plain halls have the same real routes, supported
     * lamps and room for two as the others, nothing bled into them, and come only before the stone depth.
     */
    @GameTest(template="empty")
    public static void fourPlainHallsAreJustHallwaysWithRealRoutes(GameTestHelper h)throws Exception {
        var plain=List.of(LabyrinthPlace.JOGGED_HALL,LabyrinthPlace.FORKED_HALL,LabyrinthPlace.PILLAR_HALL,LabyrinthPlace.ZIGZAG_HALL);
        for(var hall:plain){
            h.assertTrue(HallVariations.plain(hall)&&LabyrinthDomestic.fragments(hall).isEmpty()&&LabyrinthBuilder.requiredDepth(hall)==0,hall.id()+" is a plain hall from the start, with no room bled into it");
            h.assertTrue(LabyrinthDealer.grayWeight(hall,0)>0&&LabyrinthDealer.grayWeight(hall,LabyrinthPacing.STONE_DEPTH)==0,hall.id()+" is dealt on the approach and gives way to stone");
            h.assertTrue(!StaircaseProse.place(hall.id()).equals(StaircaseProse.place("unnamed_room_for_test")),hall.id()+" has a name in prose");
        }
        halls(h,plain,3500);
        var l=h.getLevel();var b=h.absolutePos(BlockPos.ZERO).offset(3500+2*60,8,280);
        for(int z:new int[]{-9,-15,-21,-27,-33,-39})h.assertTrue(l.getBlockState(b.offset(0,1,z)).is(Blocks.STRIPPED_SPRUCE_LOG),"the pillar hall's columns stand down its middle");
        h.succeed();
    }
    /**
     * 0.4.199 (owner: "one with a massive ceiling and one with a crumbled floor players have to 'parkour' across"). The vaulted
     * hall passes every ordinary hall check under a fifteen-block ceiling it shows only once through a low neck. The crumbled
     * hall's crossing is a chain of jumps none longer than two blocks or higher than one; its pit is three deep (a fall never
     * hurts), climbs back only to the near door, and offers no step out toward the far one.
     */
    private static LiteraryTests.Fixture glimpses;
    @AfterBatch(batch="domestic_glimpses") public static void glimpsesDone(ServerLevel l){if(glimpses!=null){glimpses.close();glimpses=null;}}
    /** 0.4.199, the owner: "a small chance of players seeing occupants living inside" the household slices that lead nowhere yet. */
    @GameTest(template="empty",batch="domestic_glimpses",timeoutTicks=1200)
    public static void someoneIsSometimesSeenInAHouseholdSliceThatLeadsNowhereYet(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);glimpses=f;var place=LabyrinthPlace.STRAIGHT_HALL;var b=f.base(place);
        var slot=LabyrinthPlaces.slotBounds(f.origin,place);f.chunks.hold(f.in,new AABB(slot.minX(),slot.minY(),slot.minZ(),slot.maxX()+1,slot.maxY()+1,slot.maxZ()+1));
        var p=NativeTestPlayers.survival(h,"glimpse_reader");var peer=NativeTestPlayers.survival(h,"glimpse_peer");f.players.add(p);f.players.add(peer);
        // The glimpse looks every fifth tick of the reader's own; the case's player is not ticked, so each look is lined up with one.
        Runnable tick=()->{p.tickCount=p.tickCount/5*5+5;DomesticGlimpses.tick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(p));};
        java.util.function.BiConsumer<BlockPos,Float> stand=(at,yaw)->{p.teleportTo(f.in,at.getX()+.5,at.getY(),at.getZ()+.5,yaw,0);};
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the hall is resident"))
            .thenExecute(()->{
                LabyrinthHalls.build(f.in,b,place);LabyrinthDomestic.decorateHall(f.in,b,place);
                h.assertTrue(DomesticGlimpses.free(f.in,f.origin,place).equals(List.of(0,1)),"both slices lead nowhere while no home has bled in");
                // A chosen visit: someone is put into one of them where the reader is not looking.
                stand.accept(b.offset(0,0,-2),0F);DomesticGlimpses.force=true;tick.run();
                var someone=DomesticGlimpses.occupant(p);
                h.assertTrue(someone!=null,"someone is put in: at "+LabyrinthPlaces.placeAt(f.origin,p.blockPosition())+" "+p.level().dimension().location()+" state "+p.getPersistentData().getCompound("DomesticGlimpse199"));
                h.assertTrue(someone.owner().filter(p.getUUID()::equals).isPresent()&&someone.role()==LiteraryActor.STRANGER&&!someone.broadcastToPlayer(peer),"someone is in a slice, for this reader's eyes alone");
                int index=-1;for(int i=0;i<2;i++)if(someone.blockPosition().equals(DomesticGlimpses.stand(b,LabyrinthDomestic.fragments(place).get(i))))index=i;
                h.assertTrue(index>=0,"they stand at the back of the slice, at its counter or tub: "+someone.blockPosition().subtract(b));
                var frag=LabyrinthDomestic.fragments(place).get(index);var at=DomesticGlimpses.stand(b,frag);var head=Vec3.atBottomCenterOf(at).add(0,1.5,0);
                float toward=frag.entrance().getOpposite().toYRot(),away=frag.entrance().toYRot();
                // Looked at from across the hall, they stay; looked away from, they are gone.
                stand.accept(at.relative(frag.entrance(),6),toward);p.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES,head);
                for(int i=0;i<5;i++)tick.run();
                h.assertTrue(someone.isAlive(),"watched, they stay at what they are doing");
                p.setYRot(away);p.setXRot(0);p.setYHeadRot(away);tick.run();
                h.assertTrue(!someone.isAlive()&&DomesticGlimpses.occupant(p)==null,"looked away from, they are gone");
                p.setYRot(toward);tick.run();p.setYRot(away);tick.run();
                h.assertTrue(DomesticGlimpses.occupant(p)==null,"and nobody comes back on the same visit");
                // Another visit: walked right up to, the slice is empty.
                p.teleportTo(f.in,b.getX()+.5,b.getY()+200,b.getZ()+.5,0,0);tick.run();
                stand.accept(b.offset(0,0,-2),0F);DomesticGlimpses.force=true;tick.run();
                var again=DomesticGlimpses.occupant(p);h.assertTrue(again!=null,"a later chosen visit can see someone again");
                var there=again.blockPosition();Direction way=null;for(var fr:LabyrinthDomestic.fragments(place))if(DomesticGlimpses.stand(b,fr).equals(there))way=fr.entrance();
                stand.accept(there.relative(way,3),way.getOpposite().toYRot());tick.run();
                h.assertTrue(!again.isAlive()&&DomesticGlimpses.occupant(p)==null,"walked up to, the slice is empty");
                // A slice whose home has bled in is a doorway, and nobody is glimpsed there.
                var family=DomesticLives.HOMES.get(0);var state=f.data().state(DomesticLives.STATE);state.putBoolean(DomesticLives.key(b,family),true);f.data().setState(DomesticLives.STATE,state);
                h.assertTrue(DomesticGlimpses.free(f.in,f.origin,place).equals(List.of(1)),"the family's kitchen leads home now, so only the laundry is left");
                h.assertTrue(WitnessAccount.count(f.data(),p.getUUID())==0,"nothing is counted");
            }).thenSucceed();
    }
    private static LiteraryTests.Fixture borrowedLight;
    @AfterBatch(batch="borrowed_light") public static void borrowedLightDone(ServerLevel l){if(borrowedLight!=null){borrowedLight.close();borrowedLight=null;}}
    /** 0.4.199, the first puzzle room: everything in view, every action answered, nothing used up, and once solved, solved. */
    @GameTest(template="empty",batch="borrowed_light",timeoutTicks=1200)
    public static void borrowedLightIsSolvedByLayingTheLampsBeamOnTheSun(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);borrowedLight=f;var place=LabyrinthPlace.BORROWED_LIGHT;var b=f.base(place);var l=f.in;
        var slot=LabyrinthPlaces.slotBounds(f.origin,place);f.chunks.hold(l,new AABB(slot.minX(),slot.minY(),slot.minZ(),slot.maxX()+1,slot.maxY()+1,slot.maxZ()+1));
        var p=NativeTestPlayers.survival(h,"borrowed_light_reader");f.players.add(p);
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the room is resident"))
            .thenExecute(()->{
                h.assertTrue(!place.isVignette()&&!ScenePolish.applies(place)&&WitnessAccount.Story.of(place.id())==null,"a puzzle room, built whole, and no story");
                BorrowedLight.build(l,b);LabyrinthBuilder.registerDoors(f.data(),place,b);
                p.teleportTo(l,b.getX()+.5,b.getY(),b.getZ()-4.5,180,0);
                var sun=BorrowedLight.sun(l.getSeed());var mirrorAt=b.offset(0,BorrowedLight.MIRROR_Y,BorrowedLight.TRACK_Z);
                java.util.function.Function<BlockPos,net.minecraft.world.level.block.state.BlockState> at=rel->l.getBlockState(b.offset(rel));
                // Before anything is touched, everything is in view: the lamp behind its shutter, the mirror on its track, the cold sun.
                h.assertTrue(BorrowedLightBlock.is(at.apply(BorrowedLight.LAMP),BorrowedLightBlock.Kind.LAMP)&&!at.apply(BorrowedLight.SHUTTER).getValue(BorrowedLightBlock.OPEN),"the lamp is lit behind a shut shutter");
                h.assertTrue(BorrowedLightBlock.is(l.getBlockState(mirrorAt),BorrowedLightBlock.Kind.MIRROR)&&!l.getBlockState(mirrorAt).getValue(BorrowedLightBlock.TURNED),"the mirror stands on the track between its stops");
                int stops=0;for(int x=BorrowedLight.TRACK_WEST;x<=BorrowedLight.TRACK_EAST;x++){var t=at.apply(new BlockPos(x,-1,BorrowedLight.TRACK_Z));h.assertTrue(BorrowedLightBlock.is(t,BorrowedLightBlock.Kind.TRACK),"the track runs the width of the room: "+x);if(t.getValue(BorrowedLightBlock.OPEN))stops++;}
                h.assertTrue(stops==BorrowedLight.STOPS.length,"three deeper scrapes mark the stops");
                h.assertTrue(Math.abs(sun.getX())==3&&BorrowedLightBlock.is(at.apply(sun),BorrowedLightBlock.Kind.PLATE)&&!at.apply(sun).getValue(BorrowedLightBlock.OPEN)&&BorrowedLightBlock.light(at.apply(sun))==0,"a cold brass sun beside the far door");
                var lower=b.offset(BorrowedLight.farDoor());var far=f.data().door(place.id()+"/far");
                h.assertTrue(l.getBlockState(lower).is(Blocks.IRON_DOOR)&&!l.getBlockState(lower).getValue(DoorBlock.OPEN)&&far!=null,"the far door is shut and has no handle");
                h.assertTrue(BorrowedLight.holds(p,far)&&!BorrowedLight.holds(p,f.data().door(place.entryDoorId())),"the far door refuses, and the way back never does");
                h.assertTrue(BorrowedLight.beam(l,b).path().isEmpty(),"no beam behind the shutter");
                // Lifting the catch, by the reader's own hand: the beam comes out and stops at the mirror's edge.
                var shutter=b.offset(BorrowedLight.SHUTTER);var native_=l.getBlockState(shutter).useWithoutItem(l,p,new BlockHitResult(shutter.getCenter(),Direction.EAST,shutter,false));
                h.assertTrue(native_.consumesAction()&&l.getBlockState(shutter).getValue(BorrowedLightBlock.OPEN),"the catch lifts and the shutter latches open: "+native_);
                var t=BorrowedLight.beam(l,b);h.assertTrue(t.path().size()==2&&t.path().get(1).equals(mirrorAt.above())&&!t.lit(),"the beam crosses to the mirror and stops at its edge: "+t.path());
                for(int x=1;x<=4;x++)h.assertTrue(l.getBlockState(b.offset(x,BorrowedLight.BEAM_Y,BorrowedLight.TRACK_Z)).is(Blocks.LIGHT),"the beam lights the air it crosses: "+x);
                h.assertTrue(l.getBlockEntity(b.offset(BorrowedLight.LAMP)) instanceof BorrowedLightBlock.Beam beam&&beam.points().equals(t.path()),"the lamp carries the beam for the renderer");
                // A hand on the glass turns it an eighth: the beam goes to the far wall, to the door, not the sun.
                h.assertTrue(BorrowedLight.use(p,mirrorAt,l.getBlockState(mirrorAt),.9),"the glass answers a hand");
                t=BorrowedLight.beam(l,b);h.assertTrue(l.getBlockState(mirrorAt).getValue(BorrowedLightBlock.TURNED)&&t.path().size()==3&&t.path().get(2).getZ()==b.getZ()-22&&t.path().get(2).getX()==b.getX()&&!t.lit(),"turned, the glass sends the beam to the far door, beside the sun: "+t.path());
                // A kick at its base slides it along the track, away from the reader, to the next stop: under the sun.
                int toward=Integer.signum(sun.getX());p.teleportTo(l,mirrorAt.getX()+.5-toward*1.5,b.getY(),mirrorAt.getZ()+.5,toward>0?-90:90,20);
                h.assertTrue(BorrowedLight.use(p,mirrorAt,l.getBlockState(mirrorAt),.2),"the base answers a kick");
                var moved=b.offset(sun.getX(),BorrowedLight.MIRROR_Y,BorrowedLight.TRACK_Z);
                h.assertTrue(l.getBlockState(mirrorAt).isAir()&&BorrowedLightBlock.is(l.getBlockState(moved),BorrowedLightBlock.Kind.MIRROR)&&l.getBlockState(moved).getValue(BorrowedLightBlock.TURNED),"it slides, still turned, to the stop under the sun");
                t=BorrowedLight.beam(l,b);h.assertTrue(t.lit()&&t.path().get(t.path().size()-1).equals(b.offset(sun)),"the beam lies on the sun: "+t.path());
                h.assertTrue(at.apply(sun).getValue(BorrowedLightBlock.OPEN)&&BorrowedLightBlock.light(at.apply(sun))>0,"the sun is lit and holds the light");
                h.assertTrue(BorrowedLight.solved(l)&&l.getBlockState(lower).getValue(DoorBlock.OPEN)&&!BorrowedLight.holds(p,far),"the door gives, and the far door lets the reader on");
                h.assertTrue(BorrowedLight.use(p,b.offset(sun),at.apply(sun),.5),"the sun answers a hand");
                // Kicked again, at the end of the track, it says so and stays.
                p.teleportTo(l,moved.getX()+.5-toward*1.5,b.getY(),moved.getZ()+.5,0,20);BorrowedLight.use(p,moved,l.getBlockState(moved),.2);
                h.assertTrue(BorrowedLightBlock.is(l.getBlockState(moved),BorrowedLightBlock.Kind.MIRROR),"at the end of its track it stays");
                // Solved stays solved: through a reload and a rebuild, the beam is laid on the sun as it was left.
                f.reload();h.assertTrue(BorrowedLight.solved(l),"the room keeps that it was solved");
                BorrowedLight.build(l,b);t=BorrowedLight.trace(l,b);
                h.assertTrue(t.lit()&&at.apply(BorrowedLight.SHUTTER).getValue(BorrowedLightBlock.OPEN)&&l.getBlockState(lower).getValue(DoorBlock.OPEN)&&at.apply(sun).getValue(BorrowedLightBlock.OPEN),"rebuilt, it stands as it was left: shutter open, beam on the sun, door open");
                h.assertTrue(p.isAlive()&&p.getHealth()==p.getMaxHealth()&&p.getInventory().isEmpty(),"nothing hurt the reader and nothing was used up");
                h.assertTrue(WitnessAccount.count(f.data(),p.getUUID())==0,"nothing is counted toward the Witness");
                exportRoom(l,b,place);
            }).thenSucceed();
    }
    /** 0.4.199: the puzzle rooms' one rhythm: never early, early rooms never in the stone, deep rooms after their early room, after a story and some wandering. */
    @GameTest(template="empty")
    public static void puzzleRoomsComeAfterAStoryAndSomeWandering(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);
        try{
            var p=NativeTestPlayers.survival(h,"puzzle_pacing");f.players.add(p);var id=p.getUUID();var data=f.data();
            java.util.function.BiFunction<LabyrinthPlace,Integer,Integer> weight=(place,depth)->LabyrinthDealer.grayWeight(data,id,place,depth);
            Runnable story=()->{var all=data.state(WitnessAccount.STATE);var own=all.getCompound(id.toString());var stories=own.getCompound("Stories");
                for(var s:WitnessAccount.Story.values())if(!stories.contains(s.id)){stories.putString(s.id,"test");break;}
                own.put("Stories",stories);all.put(id.toString(),own);data.setState(WitnessAccount.STATE,all);};
            var early=List.of(LabyrinthPlace.BORROWED_LIGHT,LabyrinthPlace.SERVICE_LIFT,LabyrinthPlace.TURNING_LANDING);
            var deep=List.of(LabyrinthPlace.LAMP_ROOMS,LabyrinthPlace.TWIN_LIFT,LabyrinthPlace.TURNING_STAIR);
            for(var q:PuzzleRooms.Puzzle.values())h.assertTrue(LabyrinthDealer.grayWeight(q.place,5)==0&&q.place.kind()==LabyrinthPlace.Kind.GRAY&&WitnessAccount.Story.of(q.place.id())==null,"a puzzle room is never dealt by depth alone, and is no story: "+q);
            h.assertTrue(early.stream().allMatch(q->weight.apply(q,5)==0),"not before the reader has resolved a story");
            story.run();
            h.assertTrue(early.stream().allMatch(q->weight.apply(q,2)==0&&weight.apply(q,LabyrinthPacing.STONE_DEPTH)==0),"never in the first crossings, early rooms never in the stone");
            int offered=early.stream().mapToInt(q->weight.apply(q,3)).sum();
            h.assertTrue(early.stream().allMatch(q->weight.apply(q,3)>0)&&offered<=PuzzleRooms.OFFER,"after a story, a few halls in, an early room is likely, the offer shared among them, never more than one room's worth: "+offered);
            h.assertTrue(deep.stream().allMatch(q->weight.apply(q,LabyrinthPacing.STRANGE_DEPTH)==0),"no deep room before its family's early room is solved");
            BorrowedLight.arrived(p);
            h.assertTrue(early.stream().allMatch(q->weight.apply(q,6)==0),"once a puzzle is seen, none again until another story");
            story.run();
            h.assertTrue(weight.apply(LabyrinthPlace.SERVICE_LIFT,2)==0&&weight.apply(LabyrinthPlace.SERVICE_LIFT,3)>0,"after another story and three more halls past the last puzzle, they come back");
            PuzzleRooms.markSolved(data,LabyrinthPlace.BORROWED_LIGHT);
            h.assertTrue(weight.apply(LabyrinthPlace.LAMP_ROOMS,LabyrinthPacing.STRANGE_DEPTH)>0&&weight.apply(LabyrinthPlace.LAMP_ROOMS,LabyrinthPacing.STRANGE_DEPTH-1)==0,"Borrowed Light solved, its deep room comes from the strange depths");
            h.assertTrue(weight.apply(LabyrinthPlace.TWIN_LIFT,LabyrinthPacing.STRANGE_DEPTH)==0,"each deep room waits on its own family");
            BorrowedLight.arrived(p);
            h.assertTrue(weight.apply(LabyrinthPlace.BORROWED_LIGHT,5)==0&&weight.apply(LabyrinthPlace.BORROWED_LIGHT,6)==PuzzleRooms.REVISIT,"solved, it is only an occasional pleasant room on the way, six halls apart");
            h.succeed();
        }finally{f.close();}
    }
    // ------------------------------------------------------------------ 0.4.199: the other five puzzle rooms
    private static void holdSlot(LiteraryTests.Fixture f,LabyrinthPlace place){var slot=LabyrinthPlaces.slotBounds(f.origin,place);f.chunks.hold(f.in,new AABB(slot.minX(),slot.minY(),slot.minZ(),slot.maxX()+1,slot.maxY()+1,slot.maxZ()+1));}
    /** A reader's own hand on a block, through the block's native use. */
    private static boolean press(ServerLevel l,net.minecraft.server.level.ServerPlayer p,BlockPos at){return l.getBlockState(at).useWithoutItem(l,p,new BlockHitResult(at.getCenter(),Direction.UP,at,false)).consumesAction();}
    /** The solved room, for the native architecture views. */
    private static void exportRoom(ServerLevel l,BlockPos b,LabyrinthPlace place){try{ArchitectureTests.export(l,b,place);}catch(Exception e){throw new RuntimeException(e);}}
    private static boolean clear(ServerLevel l,BlockPos at){return l.getBlockState(at).getCollisionShape(l,at).isEmpty();}
    /** Whether a body can stand here: a floor under it (not a rail's top), and room for it. */
    private static boolean stands(ServerLevel l,BlockPos at){
        var below=l.getBlockState(at.below()).getCollisionShape(l,at.below());
        return !below.isEmpty()&&below.max(Direction.Axis.Y)>=.5&&below.max(Direction.Axis.Y)<=1&&clear(l,at)&&clear(l,at.above());
    }
    /** Whether a body can walk from one spot to another inside a room, stepping or jumping one block, dropping no more than one, never climbing. */
    private static boolean walks(ServerLevel l,BlockPos b,LabyrinthPlace place,BlockPos from,BlockPos to){
        var box=place.room();var seen=new HashSet<BlockPos>(List.of(from));var queue=new ArrayDeque<BlockPos>(List.of(from));
        while(!queue.isEmpty()){
            var c=queue.poll();if(c.equals(to))return true;
            for(var d:Direction.Plane.HORIZONTAL)for(int dy=-1;dy<=1;dy++){
                var n=c.relative(d).above(dy);
                if(!box.isInside(n)||seen.contains(n)||!stands(l,b.offset(n)))continue;
                if(dy>0&&!clear(l,b.offset(c).above(2))||dy<0&&!clear(l,b.offset(n).above(2)))continue;
                seen.add(n);queue.add(n);
            }
        }
        return false;
    }
    private static LiteraryTests.Fixture serviceLift;
    @AfterBatch(batch="service_lift") public static void serviceLiftDone(ServerLevel l){if(serviceLift!=null){serviceLift.close();serviceLift=null;}}
    /** 0.4.199, the Service Lift: weights in the pan lift the cage by their bars, two steps clear the passage, a catch holds it. */
    @GameTest(template="empty",batch="service_lift",timeoutTicks=1200)
    public static void theServiceLiftsWeightsRaiseTheCageOffThePassage(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);serviceLift=f;var place=LabyrinthPlace.SERVICE_LIFT;var b=f.base(place);var l=f.in;holdSlot(f,place);
        var p=NativeTestPlayers.survival(h,"service_lift_reader");f.players.add(p);
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the room is resident"))
            .thenExecute(()->{
                h.assertTrue(PuzzleRooms.is(place)&&!place.isVignette()&&!ScenePolish.applies(place)&&WitnessAccount.Story.of(place.id())==null,"a puzzle room, built whole, and no story");
                ServiceLift.build(l,b);LabyrinthBuilder.registerDoors(f.data(),place,b);
                p.teleportTo(l,b.getX()+3.5,b.getY(),b.getZ()-8.5,90,0);
                java.util.function.Function<BlockPos,net.minecraft.world.level.block.state.BlockState> at=rel->l.getBlockState(b.offset(rel));
                java.util.function.Supplier<BlockPos> panAt=()->new BlockPos(ServiceLift.RECESS_X,ServiceLift.PAN_TOP-ServiceLift.lift(l),ServiceLift.WALL_Z);
                var shaft=new BlockPos(ServiceLift.SHAFT_X,0,ServiceLift.WALL_Z);var door=PuzzleRooms.farDoor(place);
                h.assertTrue(PuzzleFittingBlock.is(at.apply(shaft),PuzzleFittingBlock.Kind.CAGE)&&PuzzleFittingBlock.is(at.apply(shaft.above()),PuzzleFittingBlock.Kind.CAGE),"the cage rests in the only opening");
                h.assertTrue(!walks(l,b,place,new BlockPos(0,0,-1),door.south()),"nothing but the cage stands between the reader and the far door");
                var each=ServiceLift.bars(l.getSeed());int one=-1,two=-1;for(int i=0;i<3;i++){if(each[i]==1&&one<0)one=i;if(each[i]==2)two=i;}
                h.assertTrue(one>=0&&two>=0&&Arrays.stream(each).sum()==4,"two light weights and a heavy one: "+Arrays.toString(each));
                for(int i=0;i<3;i++)h.assertTrue(PuzzleFittingBlock.is(at.apply(ServiceLift.SPOTS[i]),PuzzleFittingBlock.Kind.WEIGHT)&&at.apply(ServiceLift.SPOTS[i]).getValue(PuzzleFittingBlock.STAGE)==each[i],"each weight stands on the counter, stamped with its bars: "+i);
                h.assertTrue(PuzzleFittingBlock.is(at.apply(panAt.get()),PuzzleFittingBlock.Kind.PAN)&&at.apply(panAt.get()).getValue(PuzzleFittingBlock.STAGE)==0&&PuzzleFittingBlock.is(at.apply(new BlockPos(1,ServiceLift.PULLEY_Y,ServiceLift.WALL_Z)),PuzzleFittingBlock.Kind.PULLEY),"the pan hangs empty under the pulley");
                var far=f.data().door(place.id()+"/far");h.assertTrue(far!=null&&!PuzzleRooms.holds(p,far)&&at.apply(door).getBlock() instanceof DoorBlock&&!at.apply(door).is(Blocks.IRON_DOOR),"the far door is an ordinary door: only the cage is in the way");
                // A light weight: the pan sinks one, the cage lifts one, and that is not enough.
                h.assertTrue(press(l,p,b.offset(ServiceLift.SPOTS[one])),"a hand on a weight");
                h.assertTrue(at.apply(ServiceLift.SPOTS[one]).isAir()&&ServiceLift.pan(l).equals(List.of(one))&&ServiceLift.cage(l)==1,"set in the pan, it lifts the cage a step");
                h.assertTrue(at.apply(shaft).isAir()&&PuzzleFittingBlock.is(at.apply(shaft.above()),PuzzleFittingBlock.Kind.CAGE)&&at.apply(panAt.get()).getValue(PuzzleFittingBlock.STAGE)==1&&panAt.get().getY()==ServiceLift.PAN_TOP-1,"the cage climbs a block and the pan, one weight in it, sinks a block");
                h.assertTrue(!PuzzleRooms.solved(f.data(),place)&&!walks(l,b,place,new BlockPos(0,0,-1),door.south()),"one step does not clear the opening");
                // Someone under the cage: it will not settle on them.
                var under=NativeTestPlayers.survival(h,"service_lift_under");f.players.add(under);under.teleportTo(l,b.getX()+.5,b.getY(),b.getZ()+ServiceLift.WALL_Z+.5,0,0);
                h.assertTrue(press(l,p,b.offset(panAt.get()))&&ServiceLift.cage(l)==1&&ServiceLift.pan(l).size()==1,"the cage never comes down onto someone under it");
                under.teleportTo(l,b.getX()-2.5,b.getY(),b.getZ()-6.5,0,0);
                h.assertTrue(press(l,p,b.offset(panAt.get()))&&ServiceLift.cage(l)==0&&ServiceLift.pan(l).isEmpty()&&PuzzleFittingBlock.is(at.apply(ServiceLift.SPOTS[one]),PuzzleFittingBlock.Kind.WEIGHT),"a hand on the pan lifts the weight back out to its own place, and the cage settles");
                // The heavy one: two steps, the way clear, and a catch holds the cage.
                h.assertTrue(press(l,p,b.offset(ServiceLift.SPOTS[two]))&&ServiceLift.cage(l)==2&&PuzzleRooms.solved(f.data(),place),"the heavy weight lifts it two, and the catch holds it");
                h.assertTrue(clear(l,b.offset(shaft))&&clear(l,b.offset(shaft.above()))&&walks(l,b,place,new BlockPos(0,0,-1),door.south()),"a body walks under the cage, down the cold passage, to the far door");
                h.assertTrue(press(l,p,b.offset(panAt.get()))&&ServiceLift.pan(l).isEmpty()&&ServiceLift.cage(l)==2&&clear(l,b.offset(shaft)),"emptied again, the pan rises but the catch keeps the cage up");
                for(int i=0;i<3;i++)press(l,p,b.offset(ServiceLift.SPOTS[i]));
                h.assertTrue(ServiceLift.pan(l).size()==3&&ServiceLift.cage(l)==ServiceLift.TOP&&at.apply(panAt.get()).getValue(PuzzleFittingBlock.STAGE)==3,"all three in, the pan shows them and the cage is at the top of its shaft");
                for(int i=0;i<3;i++)h.assertTrue(at.apply(ServiceLift.SPOTS[i]).isAir(),"the counter is bare: "+i);
                // Solved stays solved: through a reload and a rebuild, everything as it was left.
                f.reload();ServiceLift.build(l,b);
                h.assertTrue(PuzzleRooms.solved(f.data(),place)&&ServiceLift.pan(l).size()==3&&ServiceLift.cage(l)==ServiceLift.TOP&&at.apply(ServiceLift.SPOTS[0]).isAir()&&clear(l,b.offset(shaft)),"rebuilt, the weights are still in the pan and the cage still up");
                h.assertTrue(p.isAlive()&&p.getHealth()==p.getMaxHealth()&&p.getInventory().isEmpty()&&WitnessAccount.count(f.data(),p.getUUID())==0,"nothing hurt, nothing carried, nothing counted");
                exportRoom(l,b,place);
            }).thenSucceed();
    }
    private static LiteraryTests.Fixture turningLanding;
    @AfterBatch(batch="turning_landing") public static void turningLandingDone(ServerLevel l){if(turningLanding!=null){turningLanding.close();turningLanding=null;}}
    /** 0.4.199, the Turning Landing: a quarter turn of the handle brings the landing into line, carrying whoever is on it. */
    @GameTest(template="empty",batch="turning_landing",timeoutTicks=1200)
    public static void theTurningLandingComesRoundToTheFarDoor(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);turningLanding=f;var place=LabyrinthPlace.TURNING_LANDING;var b=f.base(place);var l=f.in;holdSlot(f,place);
        var p=NativeTestPlayers.survival(h,"turning_landing_reader");f.players.add(p);
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the room is resident"))
            .thenExecute(()->{
                h.assertTrue(PuzzleRooms.is(place)&&!place.isVignette()&&!ScenePolish.applies(place)&&WitnessAccount.Story.of(place.id())==null,"a puzzle room, built whole, and no story");
                TurningLandings.buildEarly(l,b);LabyrinthBuilder.registerDoors(f.data(),place,b);
                var land=TurningLandings.EARLY;int start=TurningLandings.rot(l,place,land);var handle=b.offset(TurningLandings.EARLY_HANDLE);var front=PuzzleRooms.farDoor(place).south();var entry=new BlockPos(0,0,-1);
                h.assertTrue((start==0||start==2)&&!TurningLandings.through(l,place)&&!walks(l,b,place,entry,front),"it starts open to the stair or to the bridge, never both, and the far door cannot be walked to");
                h.assertTrue(PuzzleFittingBlock.is(l.getBlockState(handle),PuzzleFittingBlock.Kind.HANDLE)&&l.getBlockState(b.offset(0,3,-7)).getValue(PuzzleFittingBlock.KIND)==PuzzleFittingBlock.Kind.SCRAPED,"the handle stands by the stair top, and the boards there are scraped where the landing swings past");
                h.assertTrue(walks(l,b,place,entry,new BlockPos(1,4,-7)),"the stair top and the handle can be walked to");
                // Every side but its gaps is railed; its gaps' boards are worn.
                java.util.function.IntConsumer rails=gaps->{for(int i=0;i<4;i++){var side=Direction.from2DDataValue((i+2)%4);int bit=switch(side){case NORTH->TurningLandings.N;case EAST->TurningLandings.E;case SOUTH->TurningLandings.S;default->TurningLandings.W;};
                    for(int k=-1;k<=1;k++){var edge=new BlockPos(land.cx()+2*side.getStepX()+(side.getStepX()==0?k:0),land.y(),land.cz()+2*side.getStepZ()+(side.getStepZ()==0?k:0));boolean gap=(gaps&bit)!=0;
                        h.assertTrue(l.getBlockState(b.offset(edge.above())).is(Blocks.DARK_OAK_FENCE)!=gap,"railed only where there is no gap: "+side+" "+k);
                        if(k==0)h.assertTrue(PuzzleFittingBlock.is(l.getBlockState(b.offset(edge)),PuzzleFittingBlock.Kind.WORN)==gap,"worn boards where feet cross: "+side);}}
                    for(int dx:new int[]{-2,2})for(int dz:new int[]{-2,2})h.assertTrue(l.getBlockState(b.offset(land.cx()+dx,land.y()+1,land.cz()+dz)).is(Blocks.DARK_OAK_FENCE),"the corners are always railed");};
                rails.accept(TurningLandings.gaps(l,place,land));
                // A reader on its open east edge, a second by the post: the handle turns the landing and both come round with it.
                var rider=NativeTestPlayers.survival(h,"turning_landing_rider");f.players.add(rider);
                double cx=b.getX()+land.cx()+.5,cz=b.getZ()+land.cz()+.5,top=b.getY()+land.y()+1;
                rider.teleportTo(l,cx+1.9,top,cz,0,0);p.teleportTo(l,cx+1,top,cz,0,0);
                h.assertTrue(press(l,p,handle),"a hand on the handle");
                h.assertTrue(TurningLandings.rot(l,place,land)==start+1&&TurningLandings.through(l,place)&&PuzzleRooms.solved(f.data(),place),"a quarter turn brings it into line with both the stair and the bridge");
                h.assertTrue(Math.abs(p.getX()-cx)<.01&&Math.abs(p.getZ()-(cz+1))<.01&&Math.abs(p.getYRot()-90)<.01,"the reader by the post is carried round a quarter, facing round with it: "+p.position());
                h.assertTrue(Math.abs(rider.getX()-cx)<.01&&Math.abs(rider.getZ()-(cz+1.4))<.01,"the one on the edge is set a step in from the rail it was carried into: "+rider.position());
                rails.accept(TurningLandings.gaps(l,place,land));
                h.assertTrue(l.getBlockState(handle).getValue(PuzzleFittingBlock.STAGE)==start+1,"the handle's grip points round with it");
                h.assertTrue(walks(l,b,place,entry,front),"up the stair, across the landing and the bridge, to the far door");
                // Every arrangement can be turned back: two of the four line up.
                p.teleportTo(l,b.getX()+1.5,b.getY()+4,b.getZ()-6.5,0,0);rider.teleportTo(l,b.getX()-.5,b.getY()+4,b.getZ()-6.5,0,0);
                for(int turn=2;turn<=5;turn++){press(l,p,handle);int r=Math.floorMod(start+turn,4);
                    h.assertTrue(TurningLandings.rot(l,place,land)==r&&TurningLandings.through(l,place)==(r%2==1)&&walks(l,b,place,entry,front)==(r%2==1),"turned again, it lines up every other quarter: "+r);
                    rails.accept(TurningLandings.gaps(l,place,land));}
                // Solved stays solved: through a reload and a rebuild, as it was left.
                f.reload();TurningLandings.buildEarly(l,b);
                h.assertTrue(PuzzleRooms.solved(f.data(),place)&&TurningLandings.rot(l,place,land)==Math.floorMod(start+1,4)&&walks(l,b,place,entry,front),"rebuilt, the landing stands as it was turned");
                h.assertTrue(p.isAlive()&&p.getHealth()==p.getMaxHealth()&&p.getInventory().isEmpty()&&WitnessAccount.count(f.data(),p.getUUID())==0,"nothing hurt, nothing carried, nothing counted");
                exportRoom(l,b,place);
            }).thenSucceed();
    }
    private static LiteraryTests.Fixture lampRooms;
    @AfterBatch(batch="lamp_rooms") public static void lampRoomsDone(ServerLevel l){if(lampRooms!=null){lampRooms.close();lampRooms=null;}}
    /** 0.4.199, Borrowed Light's deep room: the beam goes from room to room through windows, and each mirror is reached the long way round. */
    @GameTest(template="empty",batch="lamp_rooms",timeoutTicks=1200)
    public static void theLampRoomsCarryTheBeamThroughWindowsToARoomReachedTheLongWay(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);lampRooms=f;var place=LabyrinthPlace.LAMP_ROOMS;var b=f.base(place);var l=f.in;holdSlot(f,place);
        var p=NativeTestPlayers.survival(h,"lamp_rooms_reader");f.players.add(p);
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the rooms are resident"))
            .thenExecute(()->{
                h.assertTrue(PuzzleRooms.is(place)&&!place.isVignette()&&!ScenePolish.applies(place)&&WitnessAccount.Story.of(place.id())==null,"a puzzle room, built whole, and no story");
                LampRooms.build(l,b);LabyrinthBuilder.registerDoors(f.data(),place,b);p.teleportTo(l,b.getX()-6.5,b.getY(),b.getZ()-4.5,0,0);
                java.util.function.Function<BlockPos,net.minecraft.world.level.block.state.BlockState> at=rel->l.getBlockState(b.offset(rel));
                var lower=b.offset(PuzzleRooms.farDoor(place));var far=f.data().door(place.id()+"/far");var hall=new BlockPos(0,0,-1);
                h.assertTrue(!at.apply(LampRooms.SHUTTER).getValue(BorrowedLightBlock.OPEN)&&LampRooms.beam(l,b).path().isEmpty(),"the lamp is lit behind its shutter");
                for(int i=0;i<3;i++)h.assertTrue(BorrowedLightBlock.is(at.apply(LampRooms.MIRRORS[i]),BorrowedLightBlock.Kind.MIRROR)&&LampRooms.eighth(at.apply(LampRooms.MIRRORS[i]))!=LampRooms.AIM[i],"each mirror stands turned away from where it is needed: "+i);
                h.assertTrue(l.getBlockState(lower).is(Blocks.IRON_DOOR)&&!l.getBlockState(lower).getValue(DoorBlock.OPEN)&&far!=null&&PuzzleRooms.holds(p,far)&&!PuzzleRooms.holds(p,f.data().door(place.entryDoorId())),"the far door has no handle, and the way back never holds");
                // The windows show the way the light must go; they are glass to a body.
                for(var w:new BlockPos[]{LampRooms.NORTH_WINDOW,LampRooms.EAST_WINDOW})for(int y=1;y<=2;y++){var c=w.atY(y);h.assertTrue(BorrowedLightBlock.is(at.apply(c),BorrowedLightBlock.Kind.WINDOW)&&!clear(l,b.offset(c)),"a leaded window, solid to a body: "+c);}
                // The rooms: every mirror can be walked to, the second only the long way, through the east room and the far room.
                h.assertTrue(walks(l,b,place,hall,LampRooms.MIRRORS[0].west())&&walks(l,b,place,hall,LampRooms.MIRRORS[2].south())&&walks(l,b,place,hall,LampRooms.MIRRORS[1].south()),"each mirror can be walked to");
                l.setBlock(b.offset(0,0,-26),Blocks.STONE.defaultBlockState(),2);h.assertTrue(!walks(l,b,place,hall,LampRooms.MIRRORS[1].south()),"the second mirror's room has no way in but the far room's door");l.setBlock(b.offset(0,0,-26),Blocks.AIR.defaultBlockState(),2);
                l.setBlock(b.offset(6,0,-13),Blocks.STONE.defaultBlockState(),2);h.assertTrue(!walks(l,b,place,hall,LampRooms.MIRRORS[2].south()),"and the far room none but the east room's");l.setBlock(b.offset(6,0,-13),Blocks.AIR.defaultBlockState(),2);
                // Bolted down: the base answers, and does not move.
                var first=b.offset(LampRooms.MIRRORS[0]);var was=l.getBlockState(first);
                h.assertTrue(BorrowedLight.use(p,first,was,.2)&&l.getBlockState(first)==was,"these mirrors are bolted to the floor");
                h.assertTrue(press(l,p,b.offset(LampRooms.SHUTTER))&&at.apply(LampRooms.SHUTTER).getValue(BorrowedLightBlock.OPEN),"the catch lifts and the shutter latches open");
                h.assertTrue(LampRooms.beam(l,b).path().contains(b.offset(LampRooms.MIRRORS[0].above())),"the beam crosses to the first mirror");
                java.util.function.IntConsumer aim=i->{var m=b.offset(LampRooms.MIRRORS[i]);for(int k=0;k<8&&LampRooms.eighth(l.getBlockState(m))!=LampRooms.AIM[i];k++)h.assertTrue(BorrowedLight.use(p,m,l.getBlockState(m),.9),"the glass turns an eighth: "+i);
                    h.assertTrue(LampRooms.eighth(l.getBlockState(m))==LampRooms.AIM[i],"turned to its aim: "+i);};
                aim.accept(0);var t=LampRooms.beam(l,b);
                h.assertTrue(t.path().contains(b.offset(LampRooms.MIRRORS[1].above()))&&l.getBlockState(b.offset(-4,1,-16)).is(Blocks.LIGHT)&&!t.lit(),"turned, the first mirror sends the beam through the north window, across the next room, to the second mirror: "+t.path());
                aim.accept(1);t=LampRooms.beam(l,b);
                h.assertTrue(t.path().contains(b.offset(LampRooms.MIRRORS[2].above()))&&l.getBlockState(b.offset(3,1,-20)).is(Blocks.LIGHT)&&!t.lit()&&!PuzzleRooms.solved(f.data(),place),"the second sends it through the east window to the third, and the sun is still cold: "+t.path());
                aim.accept(2);t=LampRooms.beam(l,b);
                h.assertTrue(t.lit()&&t.path().get(t.path().size()-1).equals(b.offset(LampRooms.SUN))&&at.apply(LampRooms.SUN).getValue(BorrowedLightBlock.OPEN),"the third lays it on the sun: "+t.path());
                h.assertTrue(PuzzleRooms.solved(f.data(),place)&&l.getBlockState(lower).getValue(DoorBlock.OPEN)&&!PuzzleRooms.holds(p,far),"the door gives");
                f.reload();LampRooms.build(l,b);t=LampRooms.trace(l,b);
                h.assertTrue(t.lit()&&at.apply(LampRooms.SHUTTER).getValue(BorrowedLightBlock.OPEN)&&l.getBlockState(lower).getValue(DoorBlock.OPEN),"rebuilt, the beam lies on the sun as it was left");
                h.assertTrue(p.isAlive()&&p.getHealth()==p.getMaxHealth()&&p.getInventory().isEmpty()&&WitnessAccount.count(f.data(),p.getUUID())==0,"nothing hurt, nothing carried, nothing counted");
                exportRoom(l,b,place);
            }).thenSucceed();
    }
    private static LiteraryTests.Fixture twinLift;
    @AfterBatch(batch="twin_lift") public static void twinLiftDone(ServerLevel l){if(twinLift!=null){twinLift.close();twinLift=null;}}
    /** 0.4.199, the Service Lift's deep room: two lifts on one counterweight, ridden in turn to the gallery seen from the door. */
    @GameTest(template="empty",batch="twin_lift",timeoutTicks=1200)
    public static void theTwinLiftsAreRiddenInTurnToTheGallery(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);twinLift=f;var place=LabyrinthPlace.TWIN_LIFT;var b=f.base(place);var l=f.in;holdSlot(f,place);
        var p=NativeTestPlayers.survival(h,"twin_lift_reader");f.players.add(p);
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the hall is resident"))
            .thenExecute(()->{
                h.assertTrue(PuzzleRooms.is(place)&&!place.isVignette()&&!ScenePolish.applies(place)&&WitnessAccount.Story.of(place.id())==null,"a puzzle room, built whole, and no story");
                TwinLift.build(l,b);LabyrinthBuilder.registerDoors(f.data(),place,b);p.teleportTo(l,b.getX()+.5,b.getY(),b.getZ()-1.5,180,0);
                int ax=TwinLift.AX,bx=TwinLift.BX,sz=TwinLift.SZ;var hall=new BlockPos(0,0,-1);var front=PuzzleRooms.farDoor(place).south();
                Runnable settle=()->{for(int i=0;i<40&&TwinLift.moving(l);i++)TwinLift.step(l,b);};
                int a=TwinLift.level(l);
                h.assertTrue((a==1||a==2)&&TwinLift.heightA(l)==TwinLift.height(a)&&TwinLift.heightB(l)==TwinLift.height(2-a)&&!TwinLift.moving(l),"the lifts hang two floors apart in all, the west one off the ground");
                h.assertTrue(PuzzleFittingBlock.is(l.getBlockState(b.offset(ax+1,TwinLift.heightA(l),sz+1)),PuzzleFittingBlock.Kind.PLATFORM)&&l.getBlockState(b.offset(ax+1,-1,sz+1)).is(Blocks.WATER),"the west lift's boards stand at its floor, water at the bottom of its shaft");
                h.assertTrue(!walks(l,b,place,hall,front)&&l.getBlockState(b.offset(PuzzleRooms.farDoor(place))).getBlock() instanceof DoorBlock,"the far door is in sight from the start, high on the gallery, and out of reach");
                // Call the west lift down to the ground floor, a floor at a time.
                var callDown=b.offset(TwinLift.CALLS[0]);
                while(TwinLift.level(l)>0){h.assertTrue(press(l,p,callDown),"the ground floor's cord");settle.run();}
                h.assertTrue(TwinLift.level(l)==0&&TwinLift.heightA(l)==-1&&TwinLift.heightB(l)==TwinLift.height(2),"called down, the west lift is at the ground and the east lift at the top");
                var onA=new BlockPos(ax+1,0,sz+1);h.assertTrue(walks(l,b,place,hall,onA),"the reader walks onto the west lift");
                p.teleportTo(l,b.getX()+onA.getX()+.5,b.getY(),b.getZ()+onA.getZ()+.5,0,0);
                // Ride it up: the east lift comes down to meet the walkway.
                h.assertTrue(press(l,p,b.offset(ax,0,sz)),"the pale cord");h.assertTrue(TwinLift.moving(l)&&press(l,p,b.offset(ax,0,sz+1))&&TwinLift.level(l)==1,"while they move, the lifts will not be sent again");settle.run();
                h.assertTrue(Math.abs(p.getY()-(b.getY()+5))<.01&&TwinLift.level(l)==1&&TwinLift.heightB(l)==TwinLift.height(1),"the west lift carries the reader to the walkway, and the east lift has come down to it: "+p.position());
                var onB=new BlockPos(bx+1,5,sz+1);h.assertTrue(walks(l,b,place,new BlockPos(ax+1,5,sz+1),onB),"across the walkway onto the east lift");
                h.assertTrue(!walks(l,b,place,onB,front),"but not yet up to the gallery");
                p.teleportTo(l,b.getX()+onB.getX()+.5,b.getY()+5,b.getZ()+onB.getZ()+.5,0,0);
                h.assertTrue(press(l,p,b.offset(bx+2,5,sz+2)),"the east lift's pale cord");settle.run();
                h.assertTrue(Math.abs(p.getY()-(b.getY()+10))<.01&&TwinLift.level(l)==0,"the east lift carries the reader up, and the west lift goes back to the ground: "+p.position());
                h.assertTrue(walks(l,b,place,new BlockPos(bx+1,10,sz+1),front),"off the east lift onto the gallery, to the far door");
                h.assertTrue(!PuzzleRooms.solved(f.data(),place),"not solved until someone stands on the gallery");
                p.teleportTo(l,b.getX()+.5,b.getY()+10,b.getZ()-13.5,0,0);TwinLift.gallery(l,b);
                h.assertTrue(PuzzleRooms.solved(f.data(),place),"standing on the gallery solves it");
                for(int y=0;y<=4;y++)h.assertTrue(l.getBlockState(b.offset(0,y,sz+3)).is(Blocks.LADDER),"a ladder down from the walkway: "+y);
                for(int y=5;y<=9;y++)h.assertTrue(l.getBlockState(b.offset(0,y,-12)).is(Blocks.LADDER),"a ladder down from the gallery: "+y);
                h.assertTrue(clear(l,b.offset(0,5,sz+3))&&clear(l,b.offset(0,10,-12))&&!l.getBlockState(b.offset(1,5,sz+3)).getValue(FenceBlock.WEST),"the rails open where the ladders come up, and the rails beside them are rejoined");
                // Nobody stranded: someone at the bottom of the east shaft is safe under a lift stopping above, and climbs out.
                var down=NativeTestPlayers.survival(h,"twin_lift_bottom");f.players.add(down);down.teleportTo(l,b.getX()+bx+1.5,b.getY()-1,b.getZ()+sz+1.5,0,0);
                var niche=b.offset(TwinLift.NICHE);
                h.assertTrue(PuzzleFittingBlock.is(l.getBlockState(niche),PuzzleFittingBlock.Kind.CALL)&&press(l,down,niche),"the east shaft's own cord, in its niche at the bottom");settle.run();
                h.assertTrue(TwinLift.level(l)==1&&TwinLift.heightB(l)==TwinLift.height(1),"the east lift comes down as far as the walkway, above the one below it");
                h.assertTrue(press(l,down,niche)&&TwinLift.level(l)==1&&!TwinLift.moving(l),"and will not come further down onto them");
                for(int y=0;y<=TwinLift.LADDER_TOP;y++)h.assertTrue(l.getBlockState(b.offset(TwinLift.SLOT.atY(y))).is(Blocks.LADDER),"a ladder in the slot in the shaft's back wall: "+y);
                h.assertTrue(clear(l,b.offset(TwinLift.SLOT.atY(TwinLift.LADDER_TOP+1)))&&stands(l,b.offset(TwinLift.SLOT.getX(),TwinLift.LADDER_TOP,sz)),"from its top they step onto the lift at the walkway");
                // 0.4.199: checked at every step. Sent down while its shaft is clear, the east lift stops short of someone who comes in
                // under it after it was sent, and both lifts wait; once they are clear, the two go on.
                down.teleportTo(l,b.getX()+.5,b.getY()+10,b.getZ()-13.5,0,0);
                h.assertTrue(press(l,p,b.offset(bx+2,TwinLift.heightB(l)+1,sz+1))&&TwinLift.moving(l)&&TwinLift.level(l)==2,"the east lift's dark cord sends it down, the shaft clear");
                TwinLift.step(l,b);int heldA=TwinLift.heightA(l),heldB=TwinLift.heightB(l);
                down.teleportTo(l,b.getX()+bx+1.5,b.getY()+heldB-2,b.getZ()+sz+1.5,0,0);
                for(int i=0;i<4;i++)TwinLift.step(l,b);
                h.assertTrue(TwinLift.moving(l)&&TwinLift.heightA(l)==heldA&&TwinLift.heightB(l)==heldB&&!PuzzleFittingBlock.is(l.getBlockState(b.offset(bx+1,heldB-1,sz+1)),PuzzleFittingBlock.Kind.PLATFORM),"it stops short of the one who came in under it, and the other lift waits with it: "+heldA+"/"+heldB);
                h.assertTrue(down.isAlive()&&down.getHealth()==down.getMaxHealth(),"nothing laid through them");
                down.teleportTo(l,b.getX()+.5,b.getY()+10,b.getZ()-13.5,0,0);settle.run();
                h.assertTrue(!TwinLift.moving(l)&&TwinLift.heightB(l)==TwinLift.height(0)&&TwinLift.heightA(l)==TwinLift.height(2),"clear of the shaft, the two go on to where they were sent");
                // Solved stays solved: through a reload and a rebuild.
                f.reload();TwinLift.build(l,b);
                h.assertTrue(PuzzleRooms.solved(f.data(),place)&&TwinLift.level(l)==2&&l.getBlockState(b.offset(0,2,sz+3)).is(Blocks.LADDER)&&l.getBlockState(b.offset(0,7,-12)).is(Blocks.LADDER),"rebuilt, the lifts stand as they were left and the ladders stay down");
                h.assertTrue(p.isAlive()&&p.getHealth()==p.getMaxHealth()&&p.getInventory().isEmpty()&&WitnessAccount.count(f.data(),p.getUUID())==0,"nothing hurt, nothing carried, nothing counted");
                exportRoom(l,b,place);
            }).thenSucceed();
    }
    private static LiteraryTests.Fixture turningStair;
    @AfterBatch(batch="turning_stair") public static void turningStairDone(ServerLevel l){if(turningStair!=null){turningStair.close();turningStair=null;}}
    /** 0.4.199, the Turning Landing's deep room: two geared landings; the way through needs the second handle, in a nook only one arrangement opens. */
    @GameTest(template="empty",batch="turning_stair",timeoutTicks=1200)
    public static void theTurningStairNeedsBothHandles(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);turningStair=f;var place=LabyrinthPlace.TURNING_STAIR;var b=f.base(place);var l=f.in;holdSlot(f,place);
        var p=NativeTestPlayers.survival(h,"turning_stair_reader");f.players.add(p);
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the stairwell is resident"))
            .thenExecute(()->{
                h.assertTrue(PuzzleRooms.is(place)&&!place.isVignette()&&!ScenePolish.applies(place)&&WitnessAccount.Story.of(place.id())==null,"a puzzle room, built whole, and no story");
                TurningLandings.buildDeep(l,b);LabyrinthBuilder.registerDoors(f.data(),place,b);p.teleportTo(l,b.getX()-3.5,b.getY()+4,b.getZ()-5.5,0,0);
                var lowerL=TurningLandings.LOWER;var upperL=TurningLandings.UPPER;var entry=new BlockPos(-4,0,-1);var front=PuzzleRooms.farDoor(place).south();
                var geared=b.offset(TurningLandings.GEARED_HANDLE);var upper=b.offset(TurningLandings.UPPER_HANDLE);var nook=new BlockPos(-8,4,-9);
                java.util.function.IntSupplier lo=()->TurningLandings.rot(l,place,lowerL),up=()->TurningLandings.rot(l,place,upperL);
                int lo0=lo.getAsInt(),up0=up.getAsInt();
                h.assertTrue(Math.floorMod(lo0+up0,4)!=1&&!TurningLandings.through(l,place)&&!walks(l,b,place,entry,front),"it starts out of line, and so that the handle by the stair will never line it up alone");
                h.assertTrue(walks(l,b,place,entry,TurningLandings.GEARED_HANDLE.west()),"the stair top and its handle can be walked to");
                // The geared handle: the lower landing one way, the upper the other. Round and round, never both in line.
                for(int i=1;i<=4;i++){h.assertTrue(press(l,p,geared),"the handle by the stair");
                    h.assertTrue(lo.getAsInt()==Math.floorMod(lo0+i,4)&&up.getAsInt()==Math.floorMod(up0-i,4)&&!TurningLandings.through(l,place)&&!walks(l,b,place,entry,front),"the lower landing turns and the upper turns the other way, and they never line up: "+i);}
                // The way: turn the lower landing open to the west, set the upper one from the nook there, then turn both into line.
                while(lo.getAsInt()!=2)press(l,p,geared);
                h.assertTrue(TurningLandings.gaps(l,place,lowerL)==(TurningLandings.S|TurningLandings.W)&&walks(l,b,place,entry,nook),"turned open south and west, the lower landing leads to the nook with the second handle");
                while(up.getAsInt()!=3)press(l,p,upper);
                h.assertTrue(lo.getAsInt()==2&&!PuzzleRooms.solved(f.data(),place),"the nook's handle turns the upper landing alone");
                for(int i=0;i<3;i++)press(l,p,geared);
                h.assertTrue(TurningLandings.through(l,place)&&TurningLandings.gaps(l,place,lowerL)==(TurningLandings.E|TurningLandings.S)&&TurningLandings.gaps(l,place,upperL)==(TurningLandings.N|TurningLandings.E),"three turns of the stair's handle bring both into line");
                h.assertTrue(PuzzleRooms.solved(f.data(),place)&&walks(l,b,place,entry,front),"up through both landings to the far door");
                // Every arrangement, set with both handles: the far door can be walked to in exactly one.
                int lines=0;
                for(int i=0;i<4;i++){for(int j=0;j<4;j++){boolean through=TurningLandings.through(l,place);
                        h.assertTrue(walks(l,b,place,entry,front)==through,"the far door is reached only when both landings are in line: "+lo.getAsInt()+","+up.getAsInt());if(through)lines++;press(l,p,upper);}
                    press(l,p,geared);}
                h.assertTrue(lines==1,"one arrangement of the sixteen goes through");
                f.reload();TurningLandings.buildDeep(l,b);
                h.assertTrue(PuzzleRooms.solved(f.data(),place)&&TurningLandings.through(l,place)&&walks(l,b,place,entry,front),"rebuilt, both landings stand as they were turned");
                h.assertTrue(p.isAlive()&&p.getHealth()==p.getMaxHealth()&&p.getInventory().isEmpty()&&WitnessAccount.count(f.data(),p.getUUID())==0,"nothing hurt, nothing carried, nothing counted");
                exportRoom(l,b,place);
            }).thenSucceed();
    }
    @GameTest(template="empty")
    public static void theVaultedHallAndTheCrumbledHallAreHallwaysToCrossNotRooms(GameTestHelper h)throws Exception {
        for(var hall:List.of(LabyrinthPlace.VAULTED_HALL,LabyrinthPlace.CRUMBLED_HALL)){
            h.assertTrue(HallVariations.plain(hall)&&LabyrinthDomestic.fragments(hall).isEmpty()&&LabyrinthBuilder.requiredDepth(hall)==0,hall.id()+" is a plain hall, with no room bled into it");
            h.assertTrue(LabyrinthDealer.grayWeight(hall,LabyrinthPacing.STONE_DEPTH)==0&&LabyrinthDealer.onward(hall)&&LabyrinthPacing.ordinary(hall),hall.id()+" is an ordinary way on that gives way to stone");
            h.assertTrue(!StaircaseProse.place(hall.id()).equals(StaircaseProse.place("unnamed_room_for_test")),hall.id()+" has a name in prose");
        }
        h.assertTrue(LabyrinthDealer.grayWeight(LabyrinthPlace.VAULTED_HALL,0)>0,"the vaulted hall comes from the first crossing");
        h.assertTrue(LabyrinthDealer.grayWeight(LabyrinthPlace.CRUMBLED_HALL,2)==0&&LabyrinthDealer.grayWeight(LabyrinthPlace.CRUMBLED_HALL,3)>0,"nobody's first steps in the House are a jump");
        halls(h,List.of(LabyrinthPlace.VAULTED_HALL),3900);
        var l=h.getLevel();var v=h.absolutePos(BlockPos.ZERO).offset(3900,8,280);
        for(int y=1;y<=HallVariations.VAULT;y++)h.assertTrue(l.getBlockState(v.offset(0,y,-20)).isAir(),"the nave is open to fifteen blocks: "+y);
        h.assertTrue(!l.getBlockState(v.offset(0,HallVariations.VAULT+1,-20)).isAir()&&!l.getBlockState(v.offset(0,5,-1)).isAir()&&l.getBlockState(v.offset(0,4,-1)).isAir(),"its ceiling is up in the dark, and the neck it is seen through keeps an ordinary one");
        for(int x=-1;x<=1;x++)for(int z:new int[]{0,-1,-2,-3,-41})for(int y=5;y<=HallVariations.VAULT;y++)h.assertTrue(!l.getBlockState(v.offset(x,y,z)).isAir(),"each neck is a low passage, roofed solid above it: "+x+","+y+","+z);
        for(int z:new int[]{-9,-17,-25,-33})h.assertTrue(l.getBlockState(v.offset(0,5,z)).is(Blocks.LANTERN)&&l.getBlockState(v.offset(0,6,z)).is(Blocks.CHAIN)&&l.getBlockState(v.offset(0,HallVariations.VAULT,z)).is(Blocks.CHAIN),"each lamp hangs on ten blocks of chain, low, under the vault");
        // The crumbled hall.
        var c=h.absolutePos(BlockPos.ZERO).offset(3960,8,280);var hall=LabyrinthPlace.CRUMBLED_HALL;
        LabyrinthHalls.build(l,c,hall);HouseFurnishings.decorate(l,c,hall);ScenePolish.apply(l,c,hall);
        for(var door:hall.doors())h.assertTrue(l.getBlockState(c.offset(door.rel())).getBlock() instanceof DoorBlock,"the crumbled hall keeps its native "+door.name()+" door");
        var pieces=HallVariations.CROSSING;
        for(var piece:pieces)for(int x=piece.x0();x<=piece.x1();x++)for(int z=piece.z0();z<=piece.z1();z++){
            var at=c.offset(x,piece.y(),z);var body=new AABB(at.getX()+.2,at.getY()+.01,at.getZ()+.2,at.getX()+.8,at.getY()+1.81,at.getZ()+.8);
            h.assertTrue(l.getBlockState(at.below()).isCollisionShapeFullBlock(l,at.below())&&l.noCollision(null,body),"each piece left standing bears a body: "+x+","+piece.y()+","+z);
        }
        for(int i=0;i+1<pieces.size();i++){
            var a=pieces.get(i);var b=pieces.get(i+1);
            int gx=Math.max(0,Math.max(b.x0()-a.x1()-1,a.x0()-b.x1()-1)),gz=Math.max(0,Math.max(b.z0()-a.z1()-1,a.z0()-b.z1()-1));
            h.assertTrue(Math.sqrt(gx*gx+gz*gz)<=2&&b.y()-a.y()<=1,"each jump is at most two blocks across and one up: "+i+" gap "+gx+","+gz);
        }
        int ladders=0;
        for(int x=-2;x<=2;x++)for(int z=HallVariations.PIT_NEAR;z>=HallVariations.PIT_FAR;z--){
            if(!HallVariations.pit(x,z))continue;
            h.assertTrue(l.getBlockState(c.offset(x,HallVariations.PIT_FLOOR,z)).isCollisionShapeFullBlock(l,c.offset(x,HallVariations.PIT_FLOOR,z)),"the pit has a floor three below the boards, so a fall never hurts: "+x+","+z);
            for(int y=HallVariations.PIT_FLOOR+1;y<=-1;y++){var s=l.getBlockState(c.offset(x,y,z));
                if(s.is(Blocks.LADDER)){ladders++;h.assertTrue(z==HallVariations.PIT_NEAR&&s.canSurvive(l,c.offset(x,y,z)),"the only ladder climbs the near wall, back to the door they came in by");}
                else h.assertTrue(s.isAir()||s.is(Blocks.WALL_TORCH)||(y==HallVariations.PIT_FLOOR+1&&s.is(Blocks.SPRUCE_SLAB)),"nothing in the pit stands higher than a fallen board: "+x+","+y+","+z);}
        }
        h.assertTrue(ladders==3,"one ladder, three rungs high: "+ladders);
        for(int x=-2;x<=2;x++)for(int y=HallVariations.PIT_FLOOR+1;y<=-2;y++)h.assertTrue(l.getBlockState(c.offset(x,y,HallVariations.PIT_FAR-1)).isCollisionShapeFullBlock(l,c.offset(x,y,HallVariations.PIT_FAR-1)),"the far side of the pit is a sheer wall");
        var near=walk(l,c.offset(0,0,-1));var far=walk(l,c.offset(0,0,-31));
        h.assertTrue(!near.contains(c.offset(0,0,-38))&&far.contains(c.offset(0,0,-38)),"the far door is reached across the boards, not by walking");
        for(var at:BlockPos.betweenClosed(c.offset(hall.room().minX(),hall.room().minY(),hall.room().minZ()),c.offset(hall.room().maxX(),hall.room().maxY(),0)))
            if(l.getBlockState(at).is(Blocks.LANTERN)||l.getBlockState(at).is(Blocks.CHAIN))h.assertTrue(l.getBlockState(at).canSurvive(l,at)&&at.getY()-c.getY()>=3,"every lamp has real support and hangs clear of the jumps: "+at.subtract(c));
        ArchitectureTests.export(l,c,hall);
        h.succeed();
    }
    private static void halls(GameTestHelper h,List<LabyrinthPlace> list,int x0)throws Exception {
        var l=h.getLevel();int i=0;
        for(var hall:list) {
            BlockPos b=h.absolutePos(BlockPos.ZERO).offset(x0+i++*60,8,280);
            if(HallVariations.domestic(hall))LabyrinthHalls.build(l,b,hall);else StoneHalls.build(l,b,hall);
            HouseFurnishings.decorate(l,b,hall);
            ScenePolish.apply(l,b,hall);
            var reached=walk(l,b.offset(0,0,-1));
            for(var door:hall.doors()) {
                var at=b.offset(door.rel());h.assertTrue(l.getBlockState(at).getBlock() instanceof DoorBlock,hall.id()+" retains its native "+door.name()+" door");
                if(!door.name().equals("entry"))h.assertTrue(reached.contains(at.relative(door.facing())),"a native body reaches "+hall.id()+"/"+door.name());
            }
            for(var relative:HallVariations.floor(hall)) {
                var at=b.offset(relative);h.assertTrue(l.getBlockState(at.below()).isCollisionShapeFullBlock(l,at.below()),"every bay rests on a full supported floor");
                if(l.getBlockState(at).isAir())h.assertTrue(reached.contains(at),"every empty alcove belongs to the reachable floor: "+hall.id()+"/"+relative);
            }
            for(var at:BlockPos.betweenClosed(b.offset(hall.room().minX(),0,hall.room().minZ()),b.offset(hall.room().maxX(),hall.room().maxY(),0)))
                if(l.getBlockState(at).is(Blocks.LANTERN))h.assertTrue(l.getBlockState(at).canSurvive(l,at),"every new lamp has real support");
            for(double side:new double[]{-.65,.65}) {
                var at=b.offset(0,0,-4);var body=new AABB(at.getX()+.5+side-.3,at.getY()+.01,at.getZ()+.2,at.getX()+.5+side+.3,at.getY()+1.81,at.getZ()+.8);
                h.assertTrue(l.noCollision(null,body),hall.id()+" leaves two actual bodies clear beside one another");
            }
            h.assertTrue(LabyrinthDealer.onward(hall)&&LabyrinthPacing.ordinary(hall),"variation is an ordinary route, not another hazard or Witness source");
            ArchitectureTests.export(l,b,hall);
        }
    }
    @GameTest(template="empty")
    public static void hallwayConstructionIsPacedAndMatchesItsDirectNativeFixture(GameTestHelper h) {
        var l=h.getLevel();int i=0;
        for(var hall:List.of(LabyrinthPlace.OFFSET_HALL,LabyrinthPlace.STONE_ARCADE)) {
            var direct=h.absolutePos(BlockPos.ZERO).offset(2900+i++*100,8,280);var paced=direct.east(55);
            Runnable first=()->{if(HallVariations.domestic(hall))LabyrinthHalls.build(l,direct,hall);else StoneHalls.build(l,direct,hall);};first.run();
            var plan=BuildBlocks.record(l,()->{if(HallVariations.domestic(hall))LabyrinthHalls.build(l,paced,hall);else StoneHalls.build(l,paced,hall);});
            h.assertTrue(l.getBlockState(paced.offset(0,-1,-5)).isAir(),"recording the fresh hall does not synchronously carve its native blocks");
            boolean done=false;int slices=0;while(!done&&slices++<5000){done=plan.tick();h.assertTrue(plan.lastVisits()<=BuildBlocks.MAX_VISITS,"every hall construction slice has a hard native block-visit cap");}
            h.assertTrue(done&&slices>1,"the complete hall requires multiple bounded slices");
            var box=hall.room();for(var relative:BlockPos.betweenClosed(new BlockPos(box.minX(),-1,box.minZ()),new BlockPos(box.maxX(),box.maxY(),17)))
                h.assertTrue(l.getBlockState(direct.offset(relative)).equals(l.getBlockState(paced.offset(relative))),"paced geometry preserves exact authored native blocks: "+hall.id()+"/"+relative);
        }
        h.succeed();
    }
    private static void depth(LabyrinthData d,UUID p,int count) {
        for(int i=0;i<count;i++)d.pushReturn(p,new LabyrinthData.Waypoint(HouseDimensions.INTERIOR,new Vec3(i,80,3),0,true));
    }
    private static Map<String,String> map(LabyrinthData d,UUID p,LabyrinthPlace room) {
        Map<String,String> out=new HashMap<>();for(var spec:room.doors())if(LabyrinthData.DEALT.equals(spec.destination())) {
            var door=d.door(room.doorId(spec));out.put(door.id,d.deal(p,door).place());
        }return out;
    }
    @GameTest(template="empty")
    public static void threeDifferentHistoriesShareFreshOrdinaryRoutesAndKeepThemAfterReload(GameTestHelper h) {
        var d=new LabyrinthData();var room=LabyrinthPlace.CROSS_HALL;LabyrinthBuilder.registerDoors(d,room,new BlockPos(400,80,0));
        var a=UUID.randomUUID();var b=UUID.randomUUID();var c=UUID.randomUUID();
        for(var hall:HALLS)d.visit(a,hall);d.visit(b,LabyrinthPlace.BENT_HALL);d.visit(b,LabyrinthPlace.STRAIGHT_HALL);
        // A spectator taking the route first keeps a personal map but writes nothing the explorers will share.
        var watcher=UUID.randomUUID();depth(d,watcher,2);d.visit(watcher,room);LabyrinthDealer.arriveAt(d,watcher,room,17,false);
        h.assertTrue(d.stateEntry("shared_halls_0448",Long.toUnsignedString(d.nodeKey(watcher,room)^17)).isEmpty(),"a spectator's arrival does not decide the shared halls");
        for(var p:List.of(a,b,c)){depth(d,p,2);d.visit(p,room);LabyrinthDealer.arriveAt(d,p,room,17);}
        h.assertTrue(map(d,a,room).equals(map(d,b,room))&&map(d,a,room).equals(map(d,c,room)),"three explorers taking the same new route share its halls despite different recent visits");
        int dry=d.dryDeals(b);var before=map(d,b,room);var key=d.nodeKey(b,room);
        var loaded=LabyrinthData.load(d.save(new CompoundTag(),h.getLevel().registryAccess()),h.getLevel().registryAccess());
        loaded.visit(b,LabyrinthPlace.QUIET_ROOM);LabyrinthDealer.arriveAt(loaded,b,room,17);
        h.assertTrue(before.equals(map(loaded,b,room))&&loaded.dryDeals(b)==dry&&loaded.node(b,key)!=null,"reconnect and backtracking restore the saved route without banking stories");
        h.succeed();
    }
    @GameTest(template="empty")
    public static void legacyPersonalMapsWinOverNewSharedDiscoveries(GameTestHelper h) {
        var d=new LabyrinthData();var p=UUID.randomUUID();var room=LabyrinthPlace.STRAIGHT_HALL;
        LabyrinthBuilder.registerDoors(d,room,new BlockPos(0,80,0));depth(d,p,2);
        var door=d.door(room.doorId(room.doors().getLast()));d.deal(p,door,LabyrinthPlace.BENT_HALL.id(),false);
        long key=d.nodeKey(p,room);d.rememberNode(p,key,Map.of(door.id,d.deal(p,door)));
        var shared=new CompoundTag();shared.putString(door.id,LabyrinthPlace.OFFSET_HALL.id());d.setBoundedStateEntry("shared_halls_0448",Long.toUnsignedString(key^21),shared,2048);
        LabyrinthDealer.arriveAt(d,p,room,21);
        h.assertTrue(d.deal(p,door).place().equals(LabyrinthPlace.BENT_HALL.id()),"an explorer's existing drawn map is never replaced by another reader's new discovery");h.succeed();
    }
    @GameTest(template="empty")
    public static void sharedDiscoveryStorageIsBoundedWithoutErasingPersonalMaps(GameTestHelper h) {
        var d=new LabyrinthData();var p=UUID.randomUUID();d.rememberNode(p,9,Map.of("kept",new LabyrinthData.Deal("bent_hall",false,false)));
        for(int i=0;i<2050;i++){var tag=new CompoundTag();tag.putString("door","straight_hall");d.setBoundedStateEntry("shared_halls_0448",Integer.toString(i),tag,2048);}
        var loaded=LabyrinthData.load(d.save(new CompoundTag(),h.getLevel().registryAccess()),h.getLevel().registryAccess());
        h.assertTrue(loaded.state("shared_halls_0448").getAllKeys().size()==2049&&loaded.stateEntry("shared_halls_0448","0").isEmpty()&&!loaded.stateEntry("shared_halls_0448","2049").isEmpty(),"the 2048 shared discoveries evict the oldest and survive reload");
        h.assertTrue(loaded.node(p,9).get("kept").place().equals("bent_hall"),"shared cache eviction cannot erase personal routes");h.succeed();
    }
    @GameTest(template="empty")
    public static void spacingPausesDryDealsAndScentStillBypassesIt(GameTestHelper h) {
        var d=new LabyrinthData();var p=UUID.randomUUID();depth(d,p,12);LabyrinthBuilder.registerDoors(d,LabyrinthPlace.CROSS_HALL,new BlockPos(0,80,0));
        d.visit(p,LabyrinthPlace.HARRIGAN);d.setDryDeals(p,4);
        h.assertTrue(LabyrinthDealer.vignetteChance(d,p)==0,"a just-visited story leaves ordinary exploration ahead");
        LabyrinthDealer.dealPlace(d,p,LabyrinthPlace.CROSS_HALL,RandomSource.create(5));h.assertTrue(d.dryDeals(p)==4,"ineligible spacing cannot bank dry deals");
        d.setHillaryScent(p,true);LabyrinthDealer.dealPlace(d,p,LabyrinthPlace.CROSS_HALL,RandomSource.create(6));
        h.assertTrue(map(d,p,LabyrinthPlace.CROSS_HALL).values().stream().anyMatch(id->LabyrinthPlace.byId(id).isVignette())&&!d.hillaryScent(p),"a deliberate scent consumes one real personal story offer during spacing");
        d.visit(p,LabyrinthPlace.ALCOVE_HALL);d.visit(p,LabyrinthPlace.OFFSET_HALL);d.visit(p,LabyrinthPlace.STONE_ARCADE);
        d.setDryDeals(p,12);h.assertTrue(LabyrinthDealer.vignetteChance(d,p)==100,"a long eligible dry spell still guarantees a story");h.succeed();
    }
    @GameTest(template="empty")
    public static void aNewArrivalNeverStacksStoryAnomalyAndPhysicalHazard(GameTestHelper h) {
        var d=new LabyrinthData();var p=UUID.randomUUID();depth(d,p,16);var room=LabyrinthPlace.CROSS_HALL;
        LabyrinthBuilder.registerDoors(d,room,new BlockPos(0,80,0));int stories=0,hazards=0,quiet=0;
        for(int i=0;i<1000;i++) {
            d.setDryDeals(p,0);LabyrinthDealer.dealPlace(d,p,room,RandomSource.create(448L+i));int events=0;
            for(var id:map(d,p,room).values()) {
                var place=LabyrinthPlace.byId(id);if(place.isVignette()){events++;stories++;}
                else if(LabyrinthPacing.anomaly(place)||LabyrinthPacing.physicalTrial(place)){events++;hazards++;}else quiet++;
            }
            h.assertTrue(events<=1,"one new route cannot stack a story and a hazard across its different doors");
        }
        h.assertTrue(stories>300&&stories<480&&hazards>200&&hazards<440&&quiet>1800,"configured deep offers leave most doors ordinary: stories="+stories+", hazards="+hazards+", ordinary="+quiet);
        h.succeed();
    }
    @GameTest(template="empty")
    public static void modestEncounterPresetShortensDryRoutesWithoutRerollingMaps(GameTestHelper h){
        var d=new LabyrinthData();UUID p=UUID.randomUUID(),peer=UUID.randomUUID();var room=LabyrinthPlace.CROSS_HALL;
        LabyrinthBuilder.registerDoors(d,room,new BlockPos(0,80,0));depth(d,p,12);depth(d,peer,12);
        h.assertTrue(HouseConfig.STORY_EARLY_CHANCE.getDefault()==28&&HouseConfig.STORY_BASE_CHANCE.getDefault()==44
                &&HouseConfig.HAZARD_EARLY_CHANCE.getDefault()==28&&HouseConfig.HAZARD_MIDDLE_CHANCE.getDefault()==42&&HouseConfig.HAZARD_DEEP_CHANCE.getDefault()==54,"the modest preset increases eligible offers while keeping most individual doors ordinary");
        d.setDryDeals(p,5);h.assertTrue(LabyrinthDealer.vignetteChance(d,p)==100&&LabyrinthDealer.vignetteChance(d,peer)==44,"five eligible empty routes guarantee an offer only for that reader");
        LabyrinthDealer.arriveAt(d,p,room,62);var before=map(d,p,room);int dry=d.dryDeals(p);
        var loaded=LabyrinthData.load(d.save(new CompoundTag(),h.getLevel().registryAccess()),h.getLevel().registryAccess());
        LabyrinthDealer.arriveAt(loaded,p,room,62);h.assertTrue(before.equals(map(loaded,p,room))&&loaded.dryDeals(p)==dry,"returning after reload keeps the map and cannot manufacture extra offers");
        loaded.visit(p,LabyrinthPlace.HARRIGAN);h.assertTrue(LabyrinthDealer.vignetteChance(loaded,p)==0,"ordinary spacing after a story remains authoritative");h.succeed();
    }
    @GameTest(template="empty")
    public static void emberExcerptUsesTheSavedHandAndNeverChangesTheOriginal(GameTestHelper h) {
        var own=new CompoundTag();own.putUUID("Original",UUID.randomUUID());own.putString("Hand","KAREN");
        var pages=new net.minecraft.nbt.ListTag();pages.add(net.minecraft.nbt.StringTag.valueOf("I walked 1,234 metres before the House. The second sentence is deliberately longer than the smoke should ever carry."));own.put("Pages",pages);
        var before=own.copy();var payload=BurnEmbers.excerpt(own,0,new BlockPos(3,80,9));var buf=Unpooled.buffer();
        try {BurnEmbersPayload.STREAM_CODEC.encode(buf,payload);var read=BurnEmbersPayload.STREAM_CODEC.decode(buf);
            h.assertTrue(read.equals(payload)&&read.text().equals("I walked 1,234 metres before the House.")&&read.hand().equals(HouseWriting.WritingStyle.KAREN.font()),"native payload preserves the original page excerpt and narrator's font");
            h.assertTrue(own.equals(before)&&payload.memory()==1,"presentation cannot rewrite saved words, found leaves or burn cursor");
        }finally{buf.release();}h.succeed();
    }
    @GameTest(template="empty")
    public static void roomSoundClockPausesEmptyAndDoesNotMultiplyForPeers(GameTestHelper h) {
        var own=new CompoundTag();for(int i=0;i<59;i++)h.assertTrue(!HallAtmosphere.occupiedSecond(own,60),"the shared room has no early cue");
        h.assertTrue(HallAtmosphere.occupiedSecond(own,60)&&own.getInt("Returns")==1&&own.getInt("Seconds")==0,"one native physical-room interval produces one cue for every listener");
        own.putBoolean("Occupied",false);var d=new LabyrinthData();d.setStateEntry(HallAtmosphere.STATE,"room",own);
        var loaded=LabyrinthData.load(d.save(new CompoundTag(),h.getLevel().registryAccess()),h.getLevel().registryAccess());own=loaded.stateEntry(HallAtmosphere.STATE,"room");
        h.assertTrue(!HallAtmosphere.occupiedSecond(own,60)&&own.getInt("Returns")==2&&own.getInt("Seconds")==1,"offline time and empty rooms produce no catch-up sound burst");h.succeed();
    }
    @GameTest(template="empty",batch="exploration_quiet",timeoutTicks=1200)
    public static void unseenChairWaitsForSpectatorsAndLivingStayPetsWithoutTouchingTheCache(GameTestHelper h) {
        var l=h.getLevel();var b=h.absolutePos(BlockPos.ZERO).offset(2700,6,350);var lease=new NativeTestChunks();
        lease.hold(l,new AABB(Vec3.atLowerCornerOf(b.offset(-8,-1,-17)),Vec3.atLowerCornerOf(b.offset(8,8,18))));LabyrinthHalls.build(l,b,LabyrinthPlace.QUIET_ROOM);HouseFurnishings.decorate(l,b,LabyrinthPlace.QUIET_ROOM);
        var chair=b.offset(-4,0,-8);var original=l.getBlockState(chair);var cache=(net.minecraft.world.Container)l.getBlockEntity(b.offset(LabyrinthHalls.QUIET_CACHE));cache.setItem(0,ItemStack.EMPTY);cache.setItem(4,new ItemStack(Items.DIAMOND,3));
        var p=NativeTestPlayers.survival(h,"quiet_camera");p.gameMode.changeGameModeForPlayer(GameType.SPECTATOR);p.teleportTo(l,b.getX()+30,b.getY()+2,b.getZ()-8,0,0);
        var cat=EntityType.CAT.create(l);cat.moveTo(Vec3.atBottomCenterOf(b.offset(-3,0,-8)));cat.setTame(true,false);cat.setOwnerUUID(p.getUUID());cat.setOrderedToSit(true);cat.setHealth(5);l.addFreshEntity(cat);var uuid=cat.getUUID();final boolean[] done={false};
        h.onEachTick(()->{if(done[0]||!lease.ready())return;done[0]=true;
            try {
                h.assertTrue(!HallAtmosphere.turnQuietChair(l,b)&&l.getBlockState(chair).equals(original),"a real spectator camera blocks the unseen change");
                p.teleportTo(l,b.getX()+100,b.getY()+2,b.getZ(),0,0);
                h.assertTrue(!HallAtmosphere.turnQuietChair(l,b)&&cat.getUUID().equals(uuid)&&cat.getHealth()==5&&cat.isOrderedToSit(),"a living Stay pet prevents changes without moving or damaging it");
                cat.moveTo(b.getX()+100,b.getY(),b.getZ());h.assertTrue(HallAtmosphere.turnQuietChair(l,b),"the vacant, native-loaded room permits one small change");
                h.assertTrue(l.getBlockState(chair).getValue(HouseholdFurnitureBlock.FACING)==Direction.NORTH&&cache==l.getBlockEntity(b.offset(LabyrinthHalls.QUIET_CACHE))&&cache.getItem(0).isEmpty()&&cache.getItem(4).getCount()==3,"only the supported chair's angle changes; original property is retained without refill");
                h.succeed();
            }finally{cat.discard();NativeTestPlayers.remove(p);lease.close();}
        });
    }
    @GameTest(template="empty",batch="exploration_notes")
    public static void actualLooseNotesAreUniquePrivateFiniteAndKeepLegacyOriginalsAfterReload(GameTestHelper h){
        var server=h.getLevel().getServer();var l=HouseTestLevel.get(server);var store=server.overworld().getDataStorage();
        var oldHouse=HouseSavedData.get(server);var oldData=LabyrinthData.get(server);var origin=new BlockPos(640000,80,640000);
        var a=NativeTestPlayers.survival(h,"stair_notes_a");var b=NativeTestPlayers.survival(h,"stair_notes_b");
        try{
            var house=new HouseSavedData();house.markSpawned(origin);store.set("the_oldest_house",house);var d=new LabyrinthData();store.set("the_oldest_house_labyrinth",d);
            var positions=StaircaseWriting.positions(origin);h.assertTrue(!positions.isEmpty()&&positions.size()<=StaircaseNotes.TEXTS.size(),"the complete two-part physical descent has a distinct authored sheet for every landing");
            h.assertTrue(StaircaseWriting.pantherLanding(origin).getY()>FinaleArchitecture.ARENA && positions.getLast().getY()<FinaleArchitecture.ARENA,"the poem anticipates the cell rather than lying beyond it in the lower shaft");
            Set<String> texts=new HashSet<>();ItemStack first=null;
            for(var at:positions){
                l.setBlock(at.below(),Blocks.STONE.defaultBlockState(),2);l.setBlock(at,NoteSurfaceBlock.state(HouseMarginalia.Thread.POEMS,Direction.WEST),2);
                a.teleportTo(l,at.getX()+1.5,at.getY(),at.getZ()+.5,0,0);a.hasChangedDimension();
                h.assertTrue(StaircaseWriting.open(a,at),"a real stair sheet opens the native reader");var menu=(net.minecraft.world.inventory.LecternMenu)a.containerMenu;
                var book=menu.getSlot(0).getItem().copy();String text=book.get(net.minecraft.core.component.DataComponents.WRITTEN_BOOK_CONTENT).pages().getFirst().raw().getString();
                h.assertTrue(texts.add(text),"no two authored landings repeat their sheet");
                if(at.equals(StaircaseWriting.pantherLanding(origin))){
                    var content=book.get(net.minecraft.core.component.DataComponents.WRITTEN_BOOK_CONTENT);
                    h.assertTrue(content.title().raw().equals("The Panther")&&content.author().equals("Zampanò")&&content.pages().size()>1,"the last landing above the prisoner carries the complete source poem in its own hand");
                    h.assertTrue(menu.clickMenuButton(a,3)&&!menu.clickMenuButton(a,3),"the final-approach source is one finite personal original");
                }
                if(first==null){first=book;h.assertTrue(menu.clickMenuButton(a,3)&&!menu.clickMenuButton(a,3),"the shared surface yields one finite original to its own reader");}
                a.closeContainer();
            }
            var at=positions.getFirst();b.teleportTo(l,at.getX()+1.5,at.getY(),at.getZ()+.5,0,0);b.hasChangedDimension();StaircaseWriting.open(b,at);
            h.assertTrue(((net.minecraft.world.inventory.LecternMenu)b.containerMenu).clickMenuButton(b,3),"a peer has an independent finite collection, not the first reader's taken bit");b.closeContainer();
            var own=d.stateEntry(StaircaseWriting.ID,a.getUUID().toString());var books=own.getCompound("Books");var legacy=HouseWriting.book("A saved sheet","Ruth",HouseWriting.WritingStyle.KAREN,List.of("These exact words were already here."));
            books.put(Long.toString(at.asLong()),legacy.save(a.registryAccess()));own.put("Books",books);var editions=own.getCompound("Editions");editions.putInt(Long.toString(at.asLong()),427);own.put("Editions",editions);d.setStateEntry(StaircaseWriting.ID,a.getUUID().toString(),own);
            var peer=d.stateEntry(StaircaseWriting.ID,b.getUUID().toString());var peerBooks=peer.getCompound("Books");String finalKey=Long.toString(StaircaseWriting.pantherLanding(origin).asLong());peerBooks.put(finalKey,legacy.save(b.registryAccess()));peer.put("Books",peerBooks);var peerTaken=peer.getCompound("Taken");peerTaken.putBoolean(finalKey,true);peer.put("Taken",peerTaken);d.setStateEntry(StaircaseWriting.ID,b.getUUID().toString(),peer);
            var loaded=LabyrinthData.load(d.save(new CompoundTag(),a.registryAccess()),a.registryAccess());store.set("the_oldest_house_labyrinth",loaded);
            a.teleportTo(l,at.getX()+1.5,at.getY(),at.getZ()+.5,0,0);StaircaseWriting.open(a,at);var menu=(net.minecraft.world.inventory.LecternMenu)a.containerMenu;
            h.assertTrue(ItemStack.isSameItemSameComponents(legacy,menu.getSlot(0).getItem())&&!menu.clickMenuButton(a,3),"an older read/taken original keeps its exact components and cannot refill after reload");a.closeContainer();
            var last=StaircaseWriting.pantherLanding(origin);b.teleportTo(l,last.getX()+1.5,last.getY(),last.getZ()+.5,0,0);StaircaseWriting.open(b,last);var kept=(net.minecraft.world.inventory.LecternMenu)b.containerMenu;
            h.assertTrue(ItemStack.isSameItemSameComponents(legacy,kept.getSlot(0).getItem())&&!kept.clickMenuButton(b,3),"an already saved final-approach sheet wins over the Panther and retains finite custody");b.closeContainer();
            h.succeed();
        }finally{a.closeContainer();b.closeContainer();NativeTestPlayers.remove(a);NativeTestPlayers.remove(b);store.set("the_oldest_house",oldHouse);store.set("the_oldest_house_labyrinth",oldData);}
    }
    @GameTest(template="empty")
    public static void staircasePulsesPauseOfflineAndRemainOneOccupiedSectionClock(GameTestHelper h){
        var clock=new CompoundTag();for(int i=0;i<54;i++)h.assertTrue(!StaircaseAtmosphere.occupiedSecond(clock,55),"a section's cue cannot arrive early");
        var d=new LabyrinthData();d.setStateEntry(StaircaseAtmosphere.STATE,"section",clock);var loaded=LabyrinthData.load(d.save(new CompoundTag(),h.getLevel().registryAccess()),h.getLevel().registryAccess());
        clock=loaded.stateEntry(StaircaseAtmosphere.STATE,"section");h.assertTrue(StaircaseAtmosphere.occupiedSecond(clock,55)&&clock.getInt("Seconds")==0,"one occupied second after reload yields one cue, without offline catch-up");h.succeed();
    }
    @GameTest(template="empty")
    public static void stairDamageStaysAwayFromLandingsSourcesAndTheCentralRoute(GameTestHelper h){
        var origin=new BlockPos(0,80,0);var marks=StaircaseWear.marks(origin);var base=FinaleArchitecture.base(origin);
        h.assertTrue(!marks.isEmpty()&&marks.size()<20,"the long two-part descent has sparse, recognizable edge damage");
        for(var mark:marks){
            h.assertTrue(Math.abs(mark.hole().getX()-mark.route().getX())+Math.abs(mark.hole().getZ()-mark.route().getZ())==4,"a hole is on the edge, outside the seven central columns");
            h.assertTrue(Math.min(Math.abs(mark.route().getX()-base.getX()),Math.abs(mark.route().getZ()-base.getZ()))<=15,"turn platforms remain whole");
            for(var p:StaircaseFire.braziers(origin))h.assertTrue(p.distSqr(mark.hole())>=100,"canonical hearth approaches remain clear");
            for(var p:StaircaseLeaves.positions(origin))h.assertTrue(p.distSqr(mark.hole())>=100,"canonical original leaves stay on supported accessible treads");
        }h.succeed();
    }
    @GameTest(template="empty",batch="exploration_wear",timeoutTicks=1200)
    public static void oldStairWearWaitsForRealCamerasAndStayPetsAndHonorsPlayerEdits(GameTestHelper h){
        var l=HouseTestLevel.get(h.getLevel().getServer());var at=h.absolutePos(BlockPos.ZERO).offset(3400,10,3400);
        var mark=new StaircaseWear.Mark(at,at.east(4).below(),at.east(5));var lease=new NativeTestChunks();lease.hold(l,new AABB(mark.hole()).inflate(7,9,7));
        for(var cell:mark.cells())l.setBlock(cell,Blocks.DEEPSLATE_BRICKS.defaultBlockState(),2);l.setBlock(mark.rail(),Blocks.IRON_BARS.defaultBlockState(),2);
        var p=NativeTestPlayers.survival(h,"stair_wear_camera");p.setGameMode(GameType.SPECTATOR);p.teleportTo(l,at.getX()+10,at.getY(),at.getZ(),0,0);
        var cat=EntityType.CAT.create(l);cat.moveTo(Vec3.atBottomCenterOf(mark.hole().above()));cat.setTame(true,true);cat.setOwnerUUID(p.getUUID());cat.setOrderedToSit(true);cat.setHealth(5);l.addFreshEntity(cat);var id=cat.getUUID();boolean[] done={false};
        h.onEachTick(()->{if(done[0]||!lease.ready())return;done[0]=true;
            try{
                h.assertTrue(!StaircaseWear.apply(l,mark),"an actual spectator camera prevents visible structural change");p.teleportTo(l,at.getX()+100,at.getY(),at.getZ(),0,0);
                h.assertTrue(!StaircaseWear.apply(l,mark)&&cat.getUUID().equals(id)&&cat.getHealth()==5&&cat.isOrderedToSit(),"a living Stay pet blocks the planned edge change without losing identity or orders");cat.moveTo(at.getX()+100,at.getY(),at.getZ());
                l.setBlock(mark.hole(),Blocks.DIAMOND_BLOCK.defaultBlockState(),2);h.assertTrue(StaircaseWear.apply(l,mark)&&l.getBlockState(mark.hole()).is(Blocks.DIAMOND_BLOCK)&&l.getBlockState(mark.rail()).is(Blocks.IRON_BARS),"player-edited flooring is retained together with its rail");
                l.setBlock(mark.hole(),Blocks.DEEPSLATE_BRICKS.defaultBlockState(),2);
                var beside=mark.rail().north();l.setBlock(beside,Blocks.IRON_BARS.defaultBlockState().setValue(net.minecraft.world.level.block.IronBarsBlock.SOUTH,true).setValue(net.minecraft.world.level.block.IronBarsBlock.NORTH,true),2);
                h.assertTrue(StaircaseWear.apply(l,mark)&&mark.cells().stream().allMatch(cell->l.getBlockState(cell).isAir()),"a vacant, native-loaded authored edge receives only the three planned changes");
                h.assertTrue(!l.getBlockState(beside).getValue(net.minecraft.world.level.block.IronBarsBlock.SOUTH)&&l.getBlockState(beside).getValue(net.minecraft.world.level.block.IronBarsBlock.NORTH),"the rail beside the break stops at it instead of reaching into the gap");h.succeed();
            }finally{cat.discard();NativeTestPlayers.remove(p);lease.close();}
        });
    }
}

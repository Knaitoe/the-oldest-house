package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.gametest.*;

/** Exercise the complete House boundary tick, including idle ticks after the door fade ends. */
@GameTestHolder(TheOldestHouse.MOD_ID+"_house_read") @PrefixGameTestTemplate(false)
public final class EntrancePlaytestTests {
    private static LiteraryTests.Fixture active;
    private static void close(){HouseReadSites.clear();HouseTransitionEvents.clearAll();if(active!=null){active.close();active=null;}}
    @AfterBatch(batch="read_entrance142") public static void entranceDone(ServerLevel l){close();}
    @AfterBatch(batch="cross_hall142") public static void crossDone(ServerLevel l){close();}
    @AfterBatch(batch="retire_entrance142") public static void retireDone(ServerLevel l){close();}
    @AfterBatch(batch="finished_passage143") public static void passageDone(ServerLevel l){close();}
    @AfterBatch(batch="onward_doors144") public static void onwardDone(ServerLevel l){close();}
    @AfterBatch(batch="onward_saved144") public static void onwardSavedDone(ServerLevel l){close();}
    private static ServerPlayer player(GameTestHelper h,LiteraryTests.Fixture f,String name){var p=NativeTestPlayers.survival(h,name);f.players.add(p);return p;}
    private static void at(ServerPlayer p,ServerLevel l,BlockPos at){p.stopRiding();p.teleportTo(l,at.getX()+.5,at.getY(),at.getZ()+.5,180,0);p.setDeltaMovement(Vec3.ZERO);p.connection.resetPosition();p.hasChangedDimension();p.resetFallDistance();}
    private static void click(ServerPlayer p,BlockPos at){p.gameMode.useItemOn(p,p.serverLevel(),p.getMainHandItem(),InteractionHand.MAIN_HAND,new BlockHitResult(at.getCenter(),Direction.SOUTH,at,false));}
    private static void tick(ServerPlayer p){HouseTransitionEvents.onPlayerTick(new PlayerTickEvent.Post(p));}

    private static void proceed(GameTestHelper h,LiteraryTests.Fixture f,ServerPlayer p,HouseReadSites.Room room){
        var own=HouseReadSites.state(p.server,p.getUUID());var visit=own.getCompound(room.key());
        if(room==HouseReadSites.Room.WAITING)visit.putInt("Age",HouseReadMoments.WAIT);if(room==HouseReadSites.Room.BATHROOM)visit.putBoolean("BedroomOpen",true);
        own.put(room.key(),visit);f.data().setStateEntry(HouseReadSites.STATE,p.getUUID().toString(),own);
        h.assertTrue(HouseReadMoments.canProceed(p,room),"the room's own way on is open: "+room);
        var b=HouseReadSites.base(p.server,p.getUUID(),room);
        if(room==HouseReadSites.Room.COATROOM){var curtain=b.offset(HouseReadRooms.CURTAIN_AT);at(p,p.serverLevel(),curtain.south(2));HouseReadSites.use(p,curtain,HouseReadPropBlock.Part.CURTAIN);return;}
        var on=f.data().door("house_read/"+p.getUUID()+"/"+room.key()+"/"+(room==HouseReadSites.Room.PORCH?"road":"on"));
        h.assertTrue(on!=null,"each quiet room registers its own way on: "+room);at(p,p.serverLevel(),on.lower.relative(on.facing));click(p,on.lower);
    }

    @GameTest(template="empty",batch="read_entrance142",timeoutTicks=16000)
    public static void quietRoomsComeOneAtATimeFirstFromTheJunctionThenAsTheNextStoryDealt(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);active=f;
        var p=player(h,f,"entrance_reader");var peer=player(h,f,"entrance_observer");
        var hall=f.origin.offset(HouseLayout.AXIS_X,1,HouseImpossibleHallway.END_Z_OFFSET-20);var junction=f.base(LabyrinthPlace.JUNCTION);var corridor=f.base(LabyrinthPlace.GRAY_CORRIDOR);
        f.chunks.hold(f.in,new AABB(hall).inflate(42,5,42));f.chunks.hold(f.in,new AABB(junction).inflate(8,4,16));f.chunks.hold(f.in,new AABB(corridor).inflate(16,4,32));
        boolean[] ticking={false};LabyrinthData.Door[] west={null},owed={null};
        h.onEachTick(()->{if(ticking[0]){tick(p);tick(peer);}});
        var rooms=HouseReadSites.Room.values();
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the actual approach, junction and first hall are resident"))
            .thenExecute(()->{
                HouseSavedData.get(p.server).markImpossibleDoorRevealed();HouseImpossibleHallway.build(f.in,f.origin);
                for(var room:rooms)h.assertTrue(f.data().door(HouseReadEntrance.id(room))==null,"a new hallway is a plain approach without quiet-room doors: "+room);
                LabyrinthBuilder.buildJunction(f.in,junction);LabyrinthBuilder.registerDoors(f.data(),LabyrinthPlace.JUNCTION,junction);f.built.add(LabyrinthPlace.JUNCTION);
                LabyrinthBuilder.buildCorridor(f.in,corridor);LabyrinthBuilder.registerDoors(f.data(),LabyrinthPlace.GRAY_CORRIDOR,corridor);f.built.add(LabyrinthPlace.GRAY_CORRIDOR);
                west[0]=f.data().door(LabyrinthPlace.JUNCTION.id()+"/west");h.assertTrue(west[0]!=null,"the junction's west door is registered");
                f.data().deal(p.getUUID(),west[0],LabyrinthPlace.GRAY_CORRIDOR.id(),false);
                at(p,f.in,west[0].lower.relative(west[0].facing));at(peer,f.in,junction.offset(0,0,-3));
                h.assertTrue(!HouseReadSites.offer(p,west[0],LabyrinthPlace.GRAY_CORRIDOR)&&!HouseReadSites.state(p.server,p.getUUID()).contains("Index"),"the junction does not bypass the opening");
                io.github.knaitoe.theoldesthouse.opening.OpeningSequence.state(p).markEntered();
                h.assertTrue(!HouseReadSites.owing(f.data(),p.getUUID()),"a reader who has not begun the quiet rooms is dealt stories as before");
                p.setGameMode(net.minecraft.world.level.GameType.SPECTATOR);
                h.assertTrue(!HouseReadSites.offer(p,west[0],LabyrinthPlace.GRAY_CORRIDOR),"an observer cannot start a personal visit");
                p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
                var ordinary=new LabyrinthData.Door("native/plain142",f.in.dimension(),hall,Direction.SOUTH,LabyrinthData.DEALT,false);
                h.assertTrue(!HouseReadSites.offer(p,ordinary,LabyrinthPlace.CROSS_HALL),"the first quiet room waits behind a junction's onward door");
                ticking[0]=true;click(p,west[0].lower);
            })
            .thenWaitUntil(()->{if(HouseReadSites.inside(p)!=HouseReadSites.Room.WAITING)java.util.concurrent.locks.LockSupport.parkNanos(2_000_000L);h.assertTrue(HouseReadSites.inside(p)==HouseReadSites.Room.WAITING&&!LabyrinthDoors.isBusy(p),"the junction's real onward door reaches the first quiet room");})
            .thenExecute(()->{var b=HouseReadSites.base(p.server,p.getUUID(),HouseReadSites.Room.WAITING);at(peer,p.serverLevel(),b.offset(2,0,-2));})
            .thenIdle(24).thenExecute(()->{
                h.assertTrue(HouseReadSites.inside(p)==HouseReadSites.Room.WAITING&&HouseReadSites.contains(peer)&&!HouseTransitionEvents.isPending(p)&&!HouseTransitionEvents.isPending(peer),"idle owner and observer remain in recognized House space after the fade");
                h.assertTrue(p.isAlive()&&p.getHealth()==20&&peer.isAlive()&&peer.getHealth()==20,"the full boundary tick neither ejects nor kills either native player");
                f.reload();at(peer,f.in,junction.offset(0,0,-3));proceed(h,f,p,HouseReadSites.Room.WAITING);
            })
            .thenWaitUntil(()->h.assertTrue(HouseReadSites.inside(p)==null&&!LabyrinthDoors.isBusy(p)&&p.serverLevel()==f.in&&LabyrinthPlaces.placeAt(f.origin,p.blockPosition())==LabyrinthPlace.GRAY_CORRIDOR,"the first quiet room's way on is the hall its junction door was dealt, not another quiet room"))
            .thenExecute(()->{
                h.assertTrue(HouseReadSites.next(p.server,p.getUUID())==HouseReadSites.Room.BATHROOM&&HouseReadSites.owing(f.data(),p.getUUID()),"the next quiet room is owed once the first has been entered");
                h.assertTrue(f.data().peekReturn(p.getUUID())!=null&&f.data().peekReturn(p.getUUID()).held()&&f.data().returnDepth(p.getUUID())==0&&WitnessAccount.count(f.data(),p.getUUID())==0,"one genuine forward crossing, held while quiet rooms are to come, and no story credit");
                // 0.4.163: every door of the hall after a quiet room is the way into the next, so none is missed.
                var own=HouseReadSites.state(p.server,p.getUUID());own.remove("Owed");f.data().setStateEntry(HouseReadSites.STATE,p.getUUID().toString(),own);
                var doors=f.data().doors().stream().filter(d->d.id.startsWith(LabyrinthPlace.GRAY_CORRIDOR.id()+"/")&&LabyrinthData.DEALT.equals(d.destination)).toList();h.assertTrue(!doors.isEmpty(),"the hall has onward doors");
                LabyrinthDealer.deal(f.data(),p.getUUID(),doors,LabyrinthPlace.GRAY_CORRIDOR,net.minecraft.util.RandomSource.create(160));
                var list=HouseReadSites.state(p.server,p.getUUID()).getList("Owed",net.minecraft.nbt.Tag.TAG_STRING);
                for(var door:doors){h.assertTrue(list.stream().anyMatch(tag->tag.getAsString().equals(door.id)),"every one of the hall's doors is the way into the next quiet room: "+door.id+" in "+list);
                    var dealt=f.data().deal(p.getUUID(),door);h.assertTrue(dealt!=null&&LabyrinthPacing.ordinary(LabyrinthPlace.byId(dealt.place())),"each is dealt an ordinary hall, for afterwards: "+door.id);}
                owed[0]=f.data().door(list.getString(0));
                for(var door:doors){var deal=f.data().deal(p.getUUID(),door);h.assertTrue(deal==null||!LabyrinthPlace.byId(deal.place()).isVignette(),"no other story is dealt while a quiet room is owed: "+door.id);}
                // The fixture has only these halls built: the quiet room will lead on into the junction.
                f.data().deal(p.getUUID(),owed[0],LabyrinthPlace.JUNCTION.id(),false);
                at(p,f.in,owed[0].lower.relative(owed[0].facing));click(p,owed[0].lower);
            })
            .thenWaitUntil(()->{if(HouseReadSites.inside(p)!=HouseReadSites.Room.BATHROOM)java.util.concurrent.locks.LockSupport.parkNanos(2_000_000L);h.assertTrue(HouseReadSites.inside(p)==HouseReadSites.Room.BATHROOM&&!LabyrinthDoors.isBusy(p),"the owed door in an ordinary hall reaches the next quiet room");})
            .thenExecute(()->{
                var visit=HouseReadSites.state(p.server,p.getUUID()).getCompound(HouseReadSites.Room.BATHROOM.key());
                h.assertTrue(owed[0].id.equals(visit.getString("SourceDoor"))&&LabyrinthPlace.JUNCTION.id().equals(visit.getString("NextPlace"))&&!visit.contains("Chain"),"the room keeps the door it came from and the hall that door was dealt");
                h.assertTrue(HouseReadSites.state(p.server,p.getUUID()).getList("Owed",net.minecraft.nbt.Tag.TAG_STRING).isEmpty(),"entering it settles every owed door");
                proceed(h,f,p,HouseReadSites.Room.BATHROOM);
            })
            .thenWaitUntil(()->h.assertTrue(HouseReadSites.inside(p)==null&&!LabyrinthDoors.isBusy(p)&&p.serverLevel()==f.in&&LabyrinthPlaces.placeAt(f.origin,p.blockPosition())==LabyrinthPlace.JUNCTION,"the second quiet room also ends in its own dealt hall"))
            .thenExecute(()->{
                h.assertTrue(HouseReadSites.next(p.server,p.getUUID())==HouseReadSites.Room.COATROOM,"the rooms still come in their order");
                // Entering the last room settles every owed door, as entering each one does.
                var own=HouseReadSites.state(p.server,p.getUUID());for(var room:rooms){var visit=own.getCompound(room.key());visit.putBoolean("Entered",true);own.put(room.key(),visit);}own.remove("Owed");f.data().setStateEntry(HouseReadSites.STATE,p.getUUID().toString(),own);
                h.assertTrue(!HouseReadSites.owing(f.data(),p.getUUID())&&HouseReadSites.next(p.server,p.getUUID())==null,"after every quiet room nothing more is owed");
                var doors=f.data().doors().stream().filter(d->d.id.startsWith(LabyrinthPlace.JUNCTION.id()+"/")&&LabyrinthData.DEALT.equals(d.destination)).toList();
                LabyrinthDealer.deal(f.data(),p.getUUID(),doors,LabyrinthPlace.JUNCTION,net.minecraft.util.RandomSource.create(161));
                h.assertTrue(HouseReadSites.state(p.server,p.getUUID()).getList("Owed",net.minecraft.nbt.Tag.TAG_STRING).isEmpty()&&!HouseReadSites.offer(p,west[0],LabyrinthPlace.GRAY_CORRIDOR),"and the junction's doors are ordinary again");
                h.assertTrue(p.isAlive()&&p.getHealth()==20,"the reader arrives alive and unhurt");
            }).thenSucceed();
    }

    /** 0.4.163 (owner: "house navigation needs to be desperately balanced"): the quiet rooms come before the deep interior. */
    @GameTest(template="empty",batch="read_depth163",timeoutTicks=200)
    public static void untilTheQuietRoomsAreDoneTheHouseStaysDomesticThenDeepensACrossingAtATime(GameTestHelper h){
        var server=h.getLevel().getServer();var d=LabyrinthData.get(server);var id=java.util.UUID.randomUUID();
        var spot=new LabyrinthData.Waypoint(h.getLevel().dimension(),Vec3.ZERO,0,true);
        try{
            for(int i=0;i<9;i++)d.pushReturn(id,spot);
            h.assertTrue(d.returnDepth(id)==9,"a reader who never began the quiet rooms deepens as before: "+d.returnDepth(id));
            var own=new net.minecraft.nbt.CompoundTag();var waiting=new net.minecraft.nbt.CompoundTag();waiting.putBoolean("Entered",true);own.put(HouseReadSites.Room.WAITING.key(),waiting);d.setStateEntry(HouseReadSites.STATE,id.toString(),own);
            h.assertTrue(d.returnDepth(id)==LabyrinthPacing.STORY_DEPTH-1,"a reader already deep when the quiet rooms began is held in the domestic halls until they are done: "+d.returnDepth(id));
            d.clearReturns(id);
            for(int i=0;i<12;i++)d.pushReturn(id,spot);
            h.assertTrue(d.returnDepth(id)==0&&d.peekReturn(id).held(),"crossings among the quiet rooms do not deepen the House: "+d.returnDepth(id));
            h.assertTrue(d.returnDepth(id)<LabyrinthPacing.STONE_DEPTH&&LabyrinthDealer.mazeTier(d,id)==0&&LabyrinthPacing.trialChance(d,id)==0&&LabyrinthPacing.anomalyChance(d,id)==0&&!LabyrinthDealer.finaleOffered(d,id),"so no stone halls, trials, impossible rooms or staircase yet");
            var doors=new java.util.ArrayList<LabyrinthData.Door>();for(int i=0;i<3;i++)doors.add(new LabyrinthData.Door("native/depth163/"+id+"/"+i,h.getLevel().dimension(),new BlockPos(0,-60,i*4),Direction.NORTH,LabyrinthData.DEALT,false));
            LabyrinthDealer.deal(d,id,doors,LabyrinthPlace.GRAY_CORRIDOR,net.minecraft.util.RandomSource.create(163));
            var owed=d.stateEntry(HouseReadSites.STATE,id.toString()).getList("Owed",net.minecraft.nbt.Tag.TAG_STRING);
            for(var door:doors){h.assertTrue(owed.stream().anyMatch(tag->tag.getAsString().equals(door.id)),"every door leads on to the next quiet room: "+door.id);var deal=d.deal(id,door);h.assertTrue(deal!=null&&LabyrinthPacing.ordinary(LabyrinthPlace.byId(deal.place()))&&!StoneHalls.isStone(LabyrinthPlace.byId(deal.place())),"and afterwards to a domestic hall: "+door.id);}
            own=d.stateEntry(HouseReadSites.STATE,id.toString());for(var room:HouseReadSites.Room.values()){var visit=own.getCompound(room.key());visit.putBoolean("Entered",true);own.put(room.key(),visit);}d.setStateEntry(HouseReadSites.STATE,id.toString(),own);
            d.holdReturns(id);
            h.assertTrue(d.returnDepth(id)==0&&d.peekReturn(id)!=null,"with all five done the count starts again, and the way back is kept: "+d.returnDepth(id));
            for(int i=1;i<LabyrinthPacing.STORY_DEPTH;i++){d.pushReturn(id,spot);h.assertTrue(LabyrinthPacing.domestic(d.returnDepth(id)),"the halls after the quiet rooms are domestic again for a while: "+d.returnDepth(id));}
            d.pushReturn(id,spot);h.assertTrue(d.returnDepth(id)==LabyrinthPacing.STORY_DEPTH,"stories can begin six crossings after: "+d.returnDepth(id));
            d.pushReturn(id,spot);h.assertTrue(d.returnDepth(id)==LabyrinthPacing.STONE_DEPTH,"and the stone a crossing later: "+d.returnDepth(id));
            for(int i=0;i<LabyrinthPacing.STONE_DEPTH;i++)d.popReturn(id);h.assertTrue(d.returnDepth(id)==0&&d.peekReturn(id)!=null,"going back undoes those crossings and keeps the older way back");
            var copy=LabyrinthData.load(d.save(new net.minecraft.nbt.CompoundTag(),server.registryAccess()),server.registryAccess());
            h.assertTrue(copy.returnDepth(id)==0&&copy.peekReturn(id)!=null&&copy.peekReturn(id).held(),"and the held way back is saved");
        }finally{d.clearReturns(id);d.setStateEntry(HouseReadSites.STATE,id.toString(),new net.minecraft.nbt.CompoundTag());}
        h.succeed();
    }

    @GameTest(template="empty",batch="retire_entrance142",timeoutTicks=2400)
    public static void savedHallwayQuietDoorsAreWalledUpUnseenOnlyOnceNoVisitReturnsThroughThem(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);active=f;var p=player(h,f,"retire_reader");
        var hall=f.origin.offset(HouseLayout.AXIS_X,1,HouseImpossibleHallway.END_Z_OFFSET-20);f.chunks.hold(f.in,new AABB(hall).inflate(42,5,42));
        var rooms=HouseReadSites.Room.values();BlockPos[] lower=new BlockPos[rooms.length];
        // The wait lets the House's five-second door pass run over the fixture first, as it can on a slow host.
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the actual approach is resident")).thenIdle(110)
            .thenExecute(()->{
                HouseSavedData.get(p.server).markImpossibleDoorRevealed();HouseImpossibleHallway.build(f.in,f.origin);
                // The 0.4.142 installer's own positions and facings, as saved worlds hold them.
                for(var room:rooms){int i=room.ordinal();var facing=i%2==0?Direction.EAST:Direction.WEST;
                    lower[i]=f.origin.offset(HouseLayout.AXIS_X+(i%2==0?-2:2),1,HouseImpossibleHallway.END_Z_OFFSET-36+i*7);
                    LabyrinthBuilder.placeDoor(f.in,lower[i],facing);f.data().putDoor(new LabyrinthData.Door(HouseReadEntrance.id(room),HouseDimensions.INTERIOR,lower[i],facing,"house_read:entrance",false));}
                // The House's own pass runs every five seconds; while this fixture waited for its chunks it found no doors and
                // recorded every one as retired. A saved world that holds these doors has no such record.
                HouseReadEntrance.forgetRetired(f.data());
                var own=HouseReadSites.state(p.server,p.getUUID());var visit=own.getCompound(HouseReadSites.Room.COATROOM.key());visit.putBoolean("Entered",true);visit.putString("SourceDoor",HouseReadEntrance.id(HouseReadSites.Room.COATROOM));own.put(HouseReadSites.Room.COATROOM.key(),visit);own.putString("Active",HouseReadSites.Room.COATROOM.key());f.data().setStateEntry(HouseReadSites.STATE,p.getUUID().toString(),own);
                at(p,f.in,hall);HouseReadEntrance.retire(p.server);
                for(var room:rooms)h.assertTrue(f.data().door(HouseReadEntrance.id(room))!=null&&f.in.getBlockState(lower[room.ordinal()]).getBlock() instanceof DoorBlock,"a watched hallway keeps its doors until nobody sees them go: "+room);
                at(p,f.out,f.origin.offset(0,80,0));HouseReadEntrance.retire(p.server);
                for(var room:rooms){var i=room.ordinal();boolean kept=room==HouseReadSites.Room.COATROOM;
                    // What keeps a door: a camera within 24 blocks, its sections not loaded, or a visit returning through it.
                    var near=f.in.players().stream().filter(q->new AABB(lower[i]).inflate(24).intersects(q.getCamera().getBoundingBox())).map(q->q.getGameProfile().getName()+"@"+q.blockPosition().toShortString()+(q.isRemoved()?" removed":"")).toList();
                    String why=" (cameras near: "+near+", loaded "+f.in.isLoaded(lower[i])+"/"+f.in.areEntitiesLoaded(net.minecraft.world.level.ChunkPos.asLong(lower[i]))+", readers "+f.data().state(HouseReadSites.STATE).getAllKeys().size()+")";
                    h.assertTrue((f.data().door(HouseReadEntrance.id(room))!=null)==kept,"only the door an active visit returns through stays registered: "+room+why);
                    if(!kept){var along=(i%2==0?Direction.EAST:Direction.WEST).getClockWise();
                        h.assertTrue(f.in.getBlockState(lower[i]).equals(f.in.getBlockState(lower[i].relative(along)))||f.in.getBlockState(lower[i]).equals(f.in.getBlockState(lower[i].relative(along.getOpposite()))),"the lower cell matches its hallway course: "+room);
                        h.assertTrue(!(f.in.getBlockState(lower[i].above()).getBlock() instanceof DoorBlock)&&!f.in.getBlockState(lower[i].above()).isAir(),"the upper half is wall again: "+room);}}
                h.assertTrue(f.in.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new AABB(hall).inflate(42,5,42)).isEmpty(),"retiring a door drops nothing");
                own=HouseReadSites.state(p.server,p.getUUID());own.remove("Active");f.data().setStateEntry(HouseReadSites.STATE,p.getUUID().toString(),own);HouseReadEntrance.retire(p.server);
                h.assertTrue(f.data().door(HouseReadEntrance.id(HouseReadSites.Room.COATROOM))==null&&!(f.in.getBlockState(lower[HouseReadSites.Room.COATROOM.ordinal()]).getBlock() instanceof DoorBlock),"the last door goes once its visit is over");
                f.in.setBlock(lower[0],Blocks.AIR.defaultBlockState(),2);HouseReadEntrance.retire(p.server);
                h.assertTrue(f.in.getBlockState(lower[0]).isAir(),"a retired door is never revisited, so later edits stay");
            }).thenSucceed();
    }

    @GameTest(template="empty",batch="finished_passage143",timeoutTicks=4000)
    public static void aFinishedStorysDealtDoorIsOnlyAWayBetweenHalls(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);active=f;var p=player(h,f,"passage_reader");
        var halls=java.util.List.of(LabyrinthPlace.GRAY_CORRIDOR,LabyrinthPlace.STRAIGHT_HALL,LabyrinthPlace.BENT_HALL,LabyrinthPlace.CROSS_HALL,LabyrinthPlace.ALCOVE_HALL,LabyrinthPlace.OFFSET_HALL,LabyrinthPlace.SERVICE_LANDING,LabyrinthPlace.QUIET_ROOM);
        var junction=f.base(LabyrinthPlace.JUNCTION);f.chunks.hold(f.in,new AABB(junction).inflate(8,4,16));
        for(var hall:halls){var slot=LabyrinthPlaces.slotBounds(f.origin,hall);f.chunks.hold(f.in,new AABB(slot.minX(),slot.minY(),slot.minZ(),slot.maxX()+1,slot.maxY()+1,slot.maxZ()+1));}
        boolean[] ticking={false};LabyrinthData.Door[] west={null};var story=WitnessAccount.Story.of(LabyrinthPlace.HILL_NURSERY.id());
        h.onEachTick(()->{if(ticking[0])tick(p);});
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the junction and every ordinary hall it can open onto are resident"))
            .thenExecute(()->{
                LabyrinthBuilder.buildJunction(f.in,junction);LabyrinthBuilder.registerDoors(f.data(),LabyrinthPlace.JUNCTION,junction);f.built.add(LabyrinthPlace.JUNCTION);
                for(var hall:halls){var b=f.base(hall);if(hall==LabyrinthPlace.GRAY_CORRIDOR)LabyrinthBuilder.buildCorridor(f.in,b);else LabyrinthHalls.build(f.in,b,hall);LabyrinthBuilder.registerDoors(f.data(),hall,b);f.built.add(hall);}
                west[0]=f.data().door(LabyrinthPlace.JUNCTION.id()+"/west");
                h.assertTrue(!LabyrinthDealer.finishedFor(f.data(),p.getUUID(),LabyrinthPlace.HILL_NURSERY),"an unread story is not a passage");
                WitnessAccount.resolve(f.data(),p.getUUID(),story,"native passage fixture");
                h.assertTrue(LabyrinthDealer.finishedFor(f.data(),p.getUUID(),LabyrinthPlace.HILL_NURSERY),"a personally resolved story that can no longer be dealt is finished for its reader");
                h.assertTrue(!LabyrinthDealer.finishedFor(f.data(),p.getUUID(),LabyrinthPlace.HOTEL)&&!LabyrinthDealer.finishedFor(f.data(),p.getUUID(),LabyrinthPlace.GRAY_CORRIDOR),"recurring places and halls never finish");
                f.data().deal(p.getUUID(),west[0],LabyrinthPlace.HILL_NURSERY.id(),false);
                at(p,f.in,west[0].lower.relative(west[0].facing));ticking[0]=true;click(p,west[0].lower);
            })
            .thenWaitUntil(()->h.assertTrue(!LabyrinthDoors.isBusy(p)&&halls.contains(LabyrinthPlaces.placeAt(f.origin,p.blockPosition())),"the finished story's door leads straight into an ordinary hall"))
            .thenExecute(()->{
                var deal=f.data().deal(p.getUUID(),west[0]);
                h.assertTrue(deal!=null&&halls.contains(LabyrinthPlace.byId(deal.place())),"the door is re-dealt for this reader to the hall they reached");
                h.assertTrue(f.data().returnDepth(p.getUUID())==1&&WitnessAccount.has(f.data(),p.getUUID(),story)&&p.isAlive()&&p.getHealth()==20,"one ordinary forward crossing; the story's resolution and the reader are untouched");
            }).thenSucceed();
    }

    @GameTest(template="empty",batch="cross_hall142",timeoutTicks=1400)
    public static void actualCrossHallCommandArrivalAndAllThreeBranchesKeepTheirLivingSurvivalReader(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);active=f;var p=player(h,f,"cross_survival");
        var b=f.base(LabyrinthPlace.CROSS_HALL);var source=f.origin.offset(-30,1,-30);
        f.chunks.hold(f.in,IndianLakeRooms.bounds(b,LabyrinthPlace.CROSS_HALL));f.chunks.hold(f.out,new AABB(source).inflate(5));
        boolean[] ticking={false};h.onEachTick(()->{if(ticking[0])tick(p);});
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"native cross_hall and source are resident"))
          .thenExecute(()->{
              LabyrinthHalls.build(f.in,b,LabyrinthPlace.CROSS_HALL);LabyrinthBuilder.registerDoors(f.data(),LabyrinthPlace.CROSS_HALL,b);f.built.add(LabyrinthPlace.CROSS_HALL);
              for(var pos:BlockPos.betweenClosed(source.offset(-4,-1,-4),source.offset(4,-1,4)))f.out.setBlock(pos,Blocks.OAK_PLANKS.defaultBlockState(),2);
              LabyrinthBuilder.placeDoor(f.out,source,Direction.SOUTH);var door=new LabyrinthData.Door("native/cross_command142",f.out.dimension(),source,Direction.SOUTH,LabyrinthData.toPlace(LabyrinthPlace.CROSS_HALL),true);f.data().putDoor(door);
              io.github.knaitoe.theoldesthouse.opening.OpeningSequence.state(p).markEntered();at(p,f.out,source.south(2));p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.DIAMOND));click(p,source);ticking[0]=true;
          }).thenWaitUntil(()->h.assertTrue(!LabyrinthDoors.isBusy(p)&&p.serverLevel()==f.in&&LabyrinthPlaces.placeAt(f.origin,p.blockPosition())==LabyrinthPlace.CROSS_HALL,"the real command door reaches cross_hall itself"))
          .thenExecute(()->at(p,f.in,b.offset(0,0,-3)))
          .thenIdle(30).thenExecute(()->{
              for(int z=-4;z>=-29;z--)step(h,p,b.offset(0,0,z));
              for(int z=-28;z<=-15;z++)step(h,p,b.offset(0,0,z));
              for(int x=-1;x>=-13;x--)step(h,p,b.offset(x,0,-15));
              for(int x=-12;x<=13;x++)step(h,p,b.offset(x,0,-15));
          }).thenIdle(80).thenExecute(()->{
              h.assertTrue(p.isAlive()&&p.getHealth()==20&&p.getMainHandItem().is(Items.DIAMOND)&&p.serverLevel()==f.in&&!HouseTransitionEvents.isPending(p),"ordinary cross_hall keeps health, original possessions and native dimension through arrival, branches and idle ticks");
              h.assertTrue(HouseReadSites.inside(p)==null&&f.data().returnDepth(p.getUUID())==1,"the actual command crossing retains exactly its own ordinary return");
          }).thenSucceed();
    }
    private static final java.util.List<LabyrinthPlace> HALLS=java.util.List.of(LabyrinthPlace.GRAY_CORRIDOR,LabyrinthPlace.STRAIGHT_HALL,LabyrinthPlace.BENT_HALL,LabyrinthPlace.CROSS_HALL,LabyrinthPlace.ALCOVE_HALL,LabyrinthPlace.OFFSET_HALL,LabyrinthPlace.SERVICE_LANDING,LabyrinthPlace.QUIET_ROOM);
    private static boolean hall(LabyrinthPlace place){return place!=null&&(LabyrinthPacing.ordinary(place)||LabyrinthPacing.quiet(place));}
    private static void hold(LiteraryTests.Fixture f,LabyrinthPlace place){var slot=LabyrinthPlaces.slotBounds(f.origin,place);f.chunks.hold(f.level(place),new AABB(slot.minX(),slot.minY(),slot.minZ(),slot.maxX()+1,slot.maxY()+1,slot.maxZ()+1));}

    @GameTest(template="empty",batch="onward_doors144",timeoutTicks=4000)
    public static void aScenesFittedWayOnLeadsIntoAnOrdinaryHallAsOneCrossingAndLeavesTheSceneBehind(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);active=f;var p=player(h,f,"onward_reader");var scene=LabyrinthPlace.HILL_NURSERY;
        var junction=f.base(LabyrinthPlace.JUNCTION);f.chunks.hold(f.in,new AABB(junction).inflate(8,4,16));for(var hall:HALLS)hold(f,hall);hold(f,scene);
        boolean[] ticking={false};LabyrinthData.Door[] west={null},onward={null};
        h.onEachTick(()->{if(ticking[0])tick(p);});
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the junction, the ordinary halls and the nursery are resident"))
            .thenExecute(()->{
                LabyrinthBuilder.buildJunction(f.in,junction);LabyrinthBuilder.registerDoors(f.data(),LabyrinthPlace.JUNCTION,junction);f.built.add(LabyrinthPlace.JUNCTION);
                for(var hall:HALLS){var b=f.base(hall);if(hall==LabyrinthPlace.GRAY_CORRIDOR)LabyrinthBuilder.buildCorridor(f.in,b);else LabyrinthHalls.build(f.in,b,hall);LabyrinthBuilder.registerDoors(f.data(),hall,b);f.built.add(hall);}
                f.build(scene);
                h.assertTrue(f.data().door(OnwardDoors.id(scene))==null,"the scene's authored doors are registered without its way on, which waits for its door to stand");
                h.assertTrue(OnwardDoors.install(f.level(scene),f.base(scene),scene),"the fitted door goes in where it was placed");
                onward[0]=f.data().door(OnwardDoors.id(scene));var l=f.level(scene);
                h.assertTrue(onward[0]!=null&&LabyrinthData.DEALT.equals(onward[0].destination)&&l.getBlockState(onward[0].lower).is(Blocks.DARK_OAK_DOOR)&&l.getBlockState(onward[0].lower.above()).is(Blocks.DARK_OAK_DOOR),"a dark oak door stands in the nursery's far wall as a dealt way on");
                west[0]=f.data().door(LabyrinthPlace.JUNCTION.id()+"/west");f.data().deal(p.getUUID(),west[0],scene.id(),false);
                at(p,f.in,west[0].lower.relative(west[0].facing));ticking[0]=true;click(p,west[0].lower);
            })
            .thenWaitUntil(()->h.assertTrue(!LabyrinthDoors.isBusy(p)&&LabyrinthPlaces.placeAt(f.origin,p.blockPosition())==scene,"the reader comes into the scene by its entry"))
            .thenExecute(()->{
                var deal=f.data().deal(p.getUUID(),onward[0]);
                h.assertTrue(deal!=null&&hall(LabyrinthPlace.byId(deal.place())),"arriving deals the way on an ordinary hall: "+deal);
                h.assertTrue(f.data().returnDepth(p.getUUID())==1,"coming into the scene is one crossing");
                // Whatever else dealt it a story, a way on leads into a hall when it is used.
                f.data().deal(p.getUUID(),onward[0],LabyrinthPlace.SEANCE.id(),false);
                at(p,f.in,onward[0].lower.relative(onward[0].facing));click(p,onward[0].lower);
            })
            .thenWaitUntil(()->h.assertTrue(!LabyrinthDoors.isBusy(p)&&hall(LabyrinthPlaces.placeAt(f.origin,p.blockPosition())),"the way on leads into an ordinary hall"))
            .thenExecute(()->{
                var deal=f.data().deal(p.getUUID(),onward[0]);
                h.assertTrue(deal!=null&&hall(LabyrinthPlace.byId(deal.place()))&&LabyrinthPlace.byId(deal.place())==LabyrinthPlaces.placeAt(f.origin,p.blockPosition()),"the story it had been dealt gave way to the hall the reader reached");
                h.assertTrue(f.data().returnDepth(p.getUUID())==1,"the scene and its way on are one forward crossing");
                var back=f.data().peekReturn(p.getUUID());
                h.assertTrue(back!=null&&back.pos().distanceTo(Vec3.atBottomCenterOf(west[0].lower))<.01,"going back from that hall returns to the door that led into the scene");
                h.assertTrue(VignetteGate.dormant(f.data(),p.getUUID(),west[0]),"going on leaves the scene's door quiet behind the reader, as going back out of it does");
                h.assertTrue(p.isAlive()&&p.getHealth()==20,"the reader is untouched");
                f.reload();var saved=f.data().door(OnwardDoors.id(scene));
                h.assertTrue(saved!=null&&LabyrinthData.DEALT.equals(saved.destination)&&saved.lower.equals(onward[0].lower),"the way on survives a reload");
            }).thenSucceed();
    }

    @GameTest(template="empty",batch="onward_saved144",timeoutTicks=2400)
    public static void everySharedSceneHasAWayOnSavedScenesGetItUnseenAndTheElkGateLeadsOn(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);active=f;var p=player(h,f,"onward_watcher");var scene=LabyrinthPlace.HILL_NURSERY;hold(f,scene);
        var elk=LabyrinthPlace.ELK_CARCASSES;var gate=new LabyrinthData.Door[1];
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the nursery is resident"))
            .thenExecute(()->{
                // Only the private rooms, the read rooms and scenes with ways on of their own go without.
                var privateRooms=java.util.EnumSet.of(LabyrinthPlace.BACKSEAT,LabyrinthPlace.SILO,LabyrinthPlace.BORRASCA,LabyrinthPlace.FAMILY_COPY,LabyrinthPlace.OLD_CABIN,LabyrinthPlace.RED_ROOM,LabyrinthPlace.LAKE_HOUSE_EMPTY);
                for(var place:LabyrinthPlace.values()){
                    if(!place.isVignette()||place.slot()<0||privateRooms.contains(place)||HouseReadSites.kind(place)!=null)continue;
                    long ways=place.doors().stream().filter(spec->LabyrinthData.DEALT.equals(spec.destination())).count();
                    h.assertTrue(ways>=1,place.id()+" has a way on into the halls");
                    if(OnwardDoors.places().contains(place))h.assertTrue(ways==1,place.id()+" has exactly one way on");
                    var spec=OnwardDoors.doorSpec(place);if(spec!=null)h.assertTrue(place.room().isInside(spec.rel())&&place.room().isInside(spec.rel().relative(spec.facing())),place.id()+" stands its way on inside its own room");
                }
                for(var place:privateRooms)h.assertTrue(!OnwardDoors.places().contains(place),"a private room keeps to itself: "+place.id());
                // A scene built before 0.4.144: the reader stands in it looking straight at where its way on goes.
                f.build(scene);var at=OnwardDoors.lower(f.base(scene),scene);
                h.assertTrue(f.data().door(OnwardDoors.id(scene))==null&&!(f.in.getBlockState(at).getBlock() instanceof DoorBlock),"the saved scene has no way on yet");
                p.teleportTo(f.in,at.getX()+.5,at.getY(),at.getZ()+4.5,180,0);p.setYHeadRot(180);p.setDeltaMovement(Vec3.ZERO);p.connection.resetPosition();
                // The crew's gate was saved as a second way back.
                LabyrinthBuilder.registerDoors(f.data(),elk,f.base(elk));var service=elk.doors().stream().filter(spec->spec.name().equals("service")).findFirst().orElseThrow();
                gate[0]=f.data().door(elk.doorId(service));
                h.assertTrue(gate[0]!=null&&LabyrinthData.DEALT.equals(gate[0].destination)&&gate[0].facing==Direction.SOUTH&&OnwardDoors.placeOf(gate[0])==elk,"a new elk scene's gate at the valley head is its way on");
                f.data().putDoor(new LabyrinthData.Door(gate[0].id,gate[0].dimension,gate[0].lower,Direction.NORTH,LabyrinthData.RETURN,false));
            })
            .thenIdle(80)
            .thenExecute(()->{
                h.assertTrue(f.data().door(OnwardDoors.id(scene))==null&&!(f.in.getBlockState(OnwardDoors.lower(f.base(scene),scene)).getBlock() instanceof DoorBlock),"no door appears in a wall someone is looking at");
                var g=f.data().door(gate[0].id);h.assertTrue(g!=null&&LabyrinthData.DEALT.equals(g.destination)&&g.facing==Direction.SOUTH,"the saved gate leads on now, facing the valley it is used from");
                var at=OnwardDoors.lower(f.base(scene),scene);p.teleportTo(f.in,at.getX()+.5,at.getY(),at.getZ()+4.5,0,0);p.setYHeadRot(0);p.connection.resetPosition();
            })
            .thenWaitUntil(()->{var door=f.data().door(OnwardDoors.id(scene));h.assertTrue(door!=null&&f.in.getBlockState(door.lower).getBlock() instanceof DoorBlock,"once nobody can see the wall, the saved scene gets its way on");})
            .thenExecute(()->{
                // The way on is dealt only halls, and holds shut while its scene holds the reader.
                f.data().deal(p.getUUID(),gate[0],LabyrinthPlace.SEANCE.id(),false);LabyrinthDealer.dealOnward(f.data(),p.getUUID(),f.data().door(gate[0].id));
                var deal=f.data().deal(p.getUUID(),f.data().door(gate[0].id));var to=deal==null?null:LabyrinthPlace.byId(deal.place());
                h.assertTrue(hall(to),"a story dealt to a way on is replaced by a hall: "+to);
                var well=LabyrinthPlace.BARN_WELL;LabyrinthBuilder.registerDoors(f.data(),well,f.base(well));
                var wellOn=new LabyrinthData.Door(OnwardDoors.id(well),NovelRooms.dimension(well),f.base(well).offset(3,0,-38),Direction.SOUTH,LabyrinthData.DEALT,false);
                h.assertTrue(OnwardDoors.isOnward(wellOn)&&!OnwardDoors.locked(p,wellOn),"an ordinary visit may always go on");
                var own=NovelVignettes.personal(f.data(),p.getUUID());own.putBoolean("WellEntered",true);NovelVignettes.save(f.data(),p.getUUID(),own);
                h.assertTrue(OnwardDoors.locked(p,wellOn),"while the well holds its reader, its way on is held shut as its entry is");
                f.data().removeDoor(well.entryDoorId());
            }).thenSucceed();
    }

    private static void step(GameTestHelper h,ServerPlayer p,BlockPos at){
        var to=Vec3.atBottomCenterOf(at);p.move(net.minecraft.world.entity.MoverType.SELF,to.subtract(p.position()));
        h.assertTrue(p.position().distanceToSqr(to)<.001,"real native collision permits the cross_hall tread "+at);
        tick(p);h.assertTrue(p.getHealth()==20&&!HouseTransitionEvents.isPending(p),"a cross_hall tread is valid living House space");
    }
}

package io.github.knaitoe.theoldesthouse.gametest;

import com.mojang.authlib.GameProfile;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.io.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(TheOldestHouse.MOD_ID)
@PrefixGameTestTemplate(false)
public final class GrowlTests {
    @GameTest(template="empty") public static void growlOriginsAndDeliveryVaryWhilePacketsStayPositional(GameTestHelper h){
        var random=RandomSource.create(4014);Vec3 listener=new Vec3(130,80,-70);
        for(var kind:Growl.Kind.values()){
            double min=Double.MAX_VALUE,max=0;Set<Float> pitches=new HashSet<>(),volumes=new HashSet<>();
            for(int i=0;i<512;i++){
                var voice=Growl.voice(listener,0,kind,random);Vec3 delta=voice.position().subtract(listener);
                double horizontal=delta.horizontalDistance();
                if(kind==Growl.Kind.FAR)h.assertTrue(horizontal>=24&&horizontal<72,"far origins span 24 to 72 blocks");
                if(kind==Growl.Kind.NEAR)h.assertTrue(horizontal>=4&&horizontal<12&&delta.z<0,"near origins remain behind the listener, four to twelve blocks away");
                if(kind==Growl.Kind.BELOW)h.assertTrue(delta.y<=-8&&delta.y>-32&&Math.abs(delta.x)<=7&&Math.abs(delta.z)<=7,"below origins vary in depth and lateral position");
                double distance=kind==Growl.Kind.BELOW?-delta.y:horizontal;min=Math.min(min,distance);max=Math.max(max,distance);
                pitches.add(voice.pitch());volumes.add(voice.volume());
                var packet=Growl.packet(voice);
                h.assertTrue(packet.getSound().value().getLocation().equals(kind.sound())&&packet.getSeed()==voice.seed()
                        &&packet.getPitch()==voice.pitch()&&packet.getVolume()==voice.volume(),"the native packet retains the event, delivery and seed");
                h.assertTrue(Math.abs(packet.getX()-voice.position().x)<.126&&Math.abs(packet.getY()-voice.position().y)<.126
                        &&Math.abs(packet.getZ()-voice.position().z)<.126,"native sound coordinates retain their world position");
            }
            h.assertTrue(max-min>(kind==Growl.Kind.NEAR?7:20)&&pitches.size()>100&&volumes.size()>100,"each band varies substantially across utterances");
        }h.succeed();
    }
    @GameTest(template="empty") public static void growlAssetsAreMonoAndTheirAttenuationCoversTheNewOrigins(GameTestHelper h) throws IOException {
        try(var input=TheOldestHouse.class.getResourceAsStream("/assets/the_oldest_house/sounds.json")){
            var definitions=com.google.gson.JsonParser.parseReader(new InputStreamReader(Objects.requireNonNull(input))).getAsJsonObject();
            for(var kind:Growl.Kind.values()){
                var sounds=definitions.getAsJsonObject(kind.sound().getPath()).getAsJsonArray("sounds");
                for(var entry:sounds){
                    var sound=entry.getAsJsonObject();int range=sound.get("attenuation_distance").getAsInt();
                    h.assertTrue(range==(kind==Growl.Kind.FAR?96:kind==Growl.Kind.BELOW?48:32),"native attenuation is sized for this band");
                    String path="/assets/the_oldest_house/sounds/"+sound.get("name").getAsString().split(":")[1]+".ogg";
                    try(var clip=TheOldestHouse.class.getResourceAsStream(path)){
                        byte[] bytes=Objects.requireNonNull(clip).readAllBytes();int header=-1;
                        for(int i=0;i+16<Math.min(256,bytes.length);i++)if(bytes[i]==1
                                &&new String(bytes,i+1,6,java.nio.charset.StandardCharsets.US_ASCII).equals("vorbis")){header=i;break;}
                        h.assertTrue(header>=0&&bytes[header+11]==1,"the actual packaged Vorbis clip is mono: "+path);
                    }
                }
            }
        }h.succeed();
    }

    private static final int FLAGS=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    private static final class Fixture implements AutoCloseable {
        final MinecraftServer server;final ServerLevel level;final BlockPos origin,base;final LabyrinthPlace place;
        final HouseSavedData oldHouse;final LabyrinthData oldLabyrinth;final Growl.GrowlData oldGrowl;
        final List<ServerPlayer> players=new ArrayList<>();final LabyrinthData.Door door;
        Fixture(GameTestHelper h,int x,LabyrinthPlace place){
            server=h.getLevel().getServer();level=HouseTestLevel.get(server);origin=new BlockPos(x,80,x);this.place=place;
            oldHouse=HouseSavedData.get(server);oldLabyrinth=LabyrinthData.get(server);oldGrowl=Growl.GrowlData.get(server);
            var house=new HouseSavedData();house.markSpawned(origin);house.markInteriorInitialized();house.markImpossibleDoorRevealed();
            server.overworld().getDataStorage().set("the_oldest_house",house);
            server.overworld().getDataStorage().set("the_oldest_house_labyrinth",new LabyrinthData());
            server.overworld().getDataStorage().set("the_oldest_house_growl",new Growl.GrowlData());
            base=LabyrinthPlaces.base(origin,place);var room=place.room();
            for(BlockPos pos:BlockPos.betweenClosed(base.offset(room.minX(),-1,room.minZ()),base.offset(room.maxX(),3,room.maxZ()))){
                BlockPos rel=pos.subtract(base);boolean shell=rel.getY()==-1||rel.getY()==3
                        ||rel.getX()==room.minX()||rel.getX()==room.maxX()||rel.getZ()==room.minZ()||rel.getZ()==room.maxZ();
                level.setBlock(pos,shell?Blocks.STONE.defaultBlockState():Blocks.AIR.defaultBlockState(),FLAGS);
            }
            var spec=place.doors().stream().filter(s->s.name().equals(place==LabyrinthPlace.JUNCTION?"north":"far")).findFirst().orElseThrow();
            door=new LabyrinthData.Door(place.doorId(spec),level.dimension(),base.offset(0,0,room.minZ()),Direction.SOUTH,LabyrinthData.DEALT,false);
            LabyrinthData.get(server).putDoor(door);setDoor(level,door.lower,false);
            LabyrinthData.get(server).putDoor(new LabyrinthData.Door(place.entryDoorId(),level.dimension(),base.south(),Direction.SOUTH,LabyrinthData.RETURN,false));
        }
        ServerPlayer player(String name,int z,float yaw){
            var p=FakePlayerFactory.get(level,new GameProfile(UUID.randomUUID(),name));p.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
            move(p,Vec3.atBottomCenterOf(base.offset(0,0,z)),yaw);level.addNewPlayer(p);players.add(p);return p;
        }
        public void close(){
            for(var player:players)player.discard();
            server.overworld().getDataStorage().set("the_oldest_house",oldHouse);
            server.overworld().getDataStorage().set("the_oldest_house_labyrinth",oldLabyrinth);
            server.overworld().getDataStorage().set("the_oldest_house_growl",oldGrowl);
            Growl.clearAll();LabyrinthLighting.clearAll();HouseShifts.refreshCache(oldHouse);
        }
    }
    private static void move(ServerPlayer player,Vec3 pos,float yaw){player.moveTo(pos.x,pos.y,pos.z,yaw,0);}
    private static void setDoor(ServerLevel level,BlockPos pos,boolean open){
        var state=Blocks.IRON_DOOR.defaultBlockState().setValue(DoorBlock.FACING,Direction.SOUTH).setValue(DoorBlock.OPEN,open);
        level.setBlock(pos,state.setValue(DoorBlock.HALF,DoubleBlockHalf.LOWER),FLAGS);
        level.setBlock(pos.above(),state.setValue(DoorBlock.HALF,DoubleBlockHalf.UPPER),FLAGS);
    }
    @GameTest(template="empty",batch="growl_rolls") public static void nativePlaybackRollsOncePerEligibleUtteranceAndExcludesQuietAndAuthoredScenes(GameTestHelper h){
        try(var f=new Fixture(h,8100,LabyrinthPlace.JUNCTION)){
            var player=f.player("growl_rolls",-2,0);player.getRandom().setSeed(140014);
            for(int i=0;i<1000;i++)Growl.growl(player,Growl.Kind.FAR);
            var data=LabyrinthData.get(f.server);var record=GrowlChanges.record(data,player.getUUID());
            h.assertTrue(GrowlChanges.CHANCE==30&&record.getInt("Heard")==1000&&Growl.GrowlData.get(f.server).heard(player.getUUID())==1000,"each actual native playback creates one eligible roll and one hearing");
            int selected=record.getInt("Selected");h.assertTrue(selected>240&&selected<360,"the fixed-seed sample follows a thirty-percent chance");
            h.assertTrue(GrowlChanges.pending(data,player.getUUID())==selected&&record.getInt("Applied")==0,"every selected utterance queues one change; losing rolls add none");
            for(var place:List.of(LabyrinthPlace.QUIET_ROOM,LabyrinthPlace.PRESERVED_CAVE)){
                player.moveTo(Vec3.atBottomCenterOf(LabyrinthPlaces.base(f.origin,place).north(2)));
                Growl.growl(player,Growl.Kind.FAR);
                h.assertTrue(!GrowlChanges.request(player)&&GrowlChanges.record(data,player.getUUID()).getInt("Heard")==1000,"forced audio cannot mutate a rest stop or authored scene");
            }
        }h.succeed();
    }
    @GameTest(template="empty",batch="growl_routes") public static void closedDoorChangesRespectEveryObserverPersonalDealsAndTheOriginalReturn(GameTestHelper h){
        try(var f=new Fixture(h,8400,LabyrinthPlace.JUNCTION)){
            var player=f.player("growl_owner",-2,0);var peer=f.player("growl_watcher",-4,180);var data=LabyrinthData.get(f.server);
            data.deal(player.getUUID(),f.door,LabyrinthPlace.GRAY_CORRIDOR.id(),false);data.deal(peer.getUUID(),f.door,LabyrinthPlace.BENT_HALL.id(),false);
            var back=new LabyrinthData.Waypoint(f.level.dimension(),f.origin.getCenter(),90,true);data.pushReturn(player.getUUID(),back);
            h.assertTrue(!GrowlChanges.mayChangeDoor(f.level,f.door)&&GrowlChanges.changeRoute(player,f.origin,f.place)==null,"another observer protects the closed door");
            peer.setYRot(0);setDoor(f.level,f.door.lower,true);
            h.assertTrue(GrowlChanges.changeRoute(player,f.origin,f.place)==null,"an open threshold never changes");setDoor(f.level,f.door.lower,false);
            move(peer,Vec3.atBottomCenterOf(f.door.lower.south(2)),0);
            h.assertTrue(GrowlChanges.changeRoute(player,f.origin,f.place)==null,"standing near a threshold protects it even while looking away");move(peer,Vec3.atBottomCenterOf(f.base.north(4)),0);
            for(String reserved:List.of(LabyrinthPlace.FLOORBOARDS.id(),LabyrinthPlace.PRESERVED_CAVE.id(),LabyrinthPlace.QUIET_ROOM.id(),LabyrinthPlace.FOLDED_MAZE.id(),FinaleArchitecture.ID)){
                data.deal(player.getUUID(),f.door,reserved,true);h.assertTrue(GrowlChanges.changeRoute(player,f.origin,f.place)==null,"the Growl preserves the reserved destination "+reserved);
            }
            data.deal(player.getUUID(),f.door,LabyrinthPlace.GRAY_CORRIDOR.id(),false);
            h.assertTrue(GrowlChanges.changeRoute(player,f.origin,f.place)!=null&&!data.deal(player.getUUID(),f.door).place().equals(LabyrinthPlace.GRAY_CORRIDOR.id()),"an unseen closed ordinary door genuinely changes its destination");
            h.assertTrue(data.deal(peer.getUUID(),f.door).place().equals(LabyrinthPlace.BENT_HALL.id())&&data.returnDepth(player.getUUID())==1
                    &&data.popReturn(player.getUUID()).equals(back)&&data.door(f.place.entryDoorId()).destination.equals(LabyrinthData.RETURN),"peer deals and the exact saved way back remain intact");
        }h.succeed();
    }
    private static Fixture waiting,lighting,manor;
    @AfterBatch(batch="growl_waiting") public static void cleanWaiting(ServerLevel ignored){if(waiting!=null){waiting.close();waiting=null;}}
    @AfterBatch(batch="growl_lighting") public static void cleanLighting(ServerLevel ignored){if(lighting!=null){lighting.close();lighting=null;}}
    @AfterBatch(batch="growl_manor") public static void cleanManor(ServerLevel ignored){if(manor!=null){manor.close();manor=null;}}
    @GameTest(template="empty",batch="growl_waiting",timeoutTicks=120) public static void pendingChangeSurvivesReloadAndWaitsForTheLastObserver(GameTestHelper h){
        var f=waiting=new Fixture(h,8700,LabyrinthPlace.JUNCTION);var player=f.player("growl_wait",-2,0);var peer=f.player("growl_guard",-4,180);
        var data=LabyrinthData.get(f.server);data.deal(player.getUUID(),f.door,LabyrinthPlace.GRAY_CORRIDOR.id(),false);
        h.assertTrue(GrowlChanges.request(player),"a manual fixture can request the same safe deferred change");
        var registries=f.level.registryAccess();var loaded=LabyrinthData.FACTORY.deserializer().apply(data.save(new CompoundTag(),registries),registries);
        f.server.overworld().getDataStorage().set("the_oldest_house_labyrinth",loaded);
        h.assertTrue(GrowlChanges.pending(loaded,player.getUUID())==1,"the selected opportunity survives world-data reload");
        h.runAfterDelay(60,()->{
            h.assertTrue(GrowlChanges.pending(loaded,player.getUUID())==1&&loaded.deal(player.getUUID(),f.door).place().equals(LabyrinthPlace.GRAY_CORRIDOR.id()),"waiting retries do not reroll or change a witnessed door");
            peer.setYRot(0);h.assertTrue(GrowlChanges.tickPlayer(player),"the saved opportunity applies once the last observer turns away");
            h.assertTrue(GrowlChanges.pending(loaded,player.getUUID())==0&&GrowlChanges.record(loaded,player.getUUID()).getInt("Applied")==1
                    &&!GrowlChanges.tickPlayer(player),"one queued opportunity produces exactly one actual change");h.succeed();
        });
    }
    @GameTest(template="empty",batch="growl_marks") public static void markerChangesStayDeepOwnedUnwatchedAndSpaced(GameTestHelper h){
        try(var f=new Fixture(h,9000,LabyrinthPlace.GRAY_CORRIDOR)){
            var player=f.player("growl_chalk",-2,0);var data=LabyrinthData.get(f.server);
            BlockPos own=f.base.north(22),other=f.base.offset(2,0,-24);
            h.assertTrue(NavigationAids.placeChalk(f.level,own,Direction.UP,Direction.NORTH)&&NavigationAids.placeChalk(f.level,other,Direction.UP,Direction.NORTH),"real supported floor marks can be placed");
            NavigationAids.remember(f.level,own,player.getUUID(),true);NavigationAids.remember(f.level,other,UUID.randomUUID(),true);
            h.assertTrue(!NavigationAids.alterOne(player,f.place),"shallow exploration keeps marks dependable");
            for(int i=0;i<6;i++)data.pushReturn(player.getUUID(),new LabyrinthData.Waypoint(f.level.dimension(),f.origin.getCenter(),0));
            player.setYRot(180);h.assertTrue(!NavigationAids.alterOne(player,f.place),"looking at the mark protects it");player.setYRot(0);
            h.assertTrue(NavigationAids.alterOne(player,f.place)&&f.level.getBlockState(own).getValue(ChalkMarkBlock.ARROW)==Direction.EAST
                    &&f.level.getBlockState(other).getValue(ChalkMarkBlock.ARROW)==Direction.NORTH,"only the owner's unseen arrow turns");
            h.assertTrue(!NavigationAids.alterOne(player,f.place),"the shared two-minute cooldown prevents repeated tampering");
        }h.succeed();
    }
    @GameTest(template="empty",batch="growl_lighting",timeoutTicks=120) public static void selectedGrowlMovesOneRealLightWithoutDeletingOrDuplicatingIt(GameTestHelper h){
        var f=lighting=new Fixture(h,9300,LabyrinthPlace.JUNCTION);var player=f.player("growl_lamp",-2,0);var peer=f.player("lamp_watcher",-4,180);
        var data=LabyrinthData.get(f.server);data.removeDoor(f.door.id);BlockPos source=f.base.north(11);
        f.level.setBlock(source,Blocks.TORCH.defaultBlockState(),FLAGS);h.assertTrue(GrowlChanges.request(player),"an actual light provides another change opportunity");
        h.runAfterDelay(60,()->{
            h.assertTrue(f.level.getBlockState(source).is(Blocks.TORCH)&&GrowlChanges.pending(data,player.getUUID())==1,"a witnessed light stays in place");
            peer.setYRot(0);h.assertTrue(GrowlChanges.tickPlayer(player)&&f.level.getBlockState(source).isAir(),"the Growl moves the actual unseen torch");
            int count=0;var room=f.place.room();
            for(BlockPos pos:BlockPos.betweenClosed(f.base.offset(room.minX(),0,room.minZ()),f.base.offset(room.maxX(),2,room.maxZ())))if(f.level.getBlockState(pos).is(Blocks.TORCH))count++;
            h.assertTrue(count==1&&GrowlChanges.record(data,player.getUUID()).getInt("Applied")==1,"the original light is conserved and one change is counted");h.succeed();
        });
    }
    @GameTest(template="empty",batch="growl_manor",timeoutTicks=120) public static void cellarGrowlOpportunityChangesThePhysicalManorAndItsProxy(GameTestHelper h){
        var f=manor=new Fixture(h,9600,LabyrinthPlace.JUNCTION);var player=f.player("growl_cellar",-2,180);
        move(player,Vec3.atBottomCenterOf(f.origin.offset(8,-3,10)),180);
        BlockPos target=f.origin.offset(13,1,20);setDoor(f.level,target,false);
        h.assertTrue(GrowlChanges.request(player),"the ordinary cellar can queue a manor change");
        h.runAfterDelay(60,()->{
            GrowlChanges.tickPlayer(player);var data=LabyrinthData.get(f.server);
            h.assertTrue(f.level.getBlockState(target).getValue(DoorBlock.HINGE)==DoorHingeSide.RIGHT
                    &&f.server.overworld().getBlockState(target).equals(f.level.getBlockState(target)),"the real manor door and exterior proxy agree after the shift");
            h.assertTrue(GrowlChanges.pending(data,player.getUUID())==0&&GrowlChanges.record(data,player.getUUID()).getInt("Applied")==1,"the cellar opportunity applies once");h.succeed();
        });
    }
}

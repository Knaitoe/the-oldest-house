package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseTransitionEvents;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import io.github.knaitoe.theoldesthouse.opening.OpeningSequence;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.gametest.*;

/** Two real private rooms, staggered fades, a saved common apron and distinct original continuations. */
@GameTestHolder(TheOldestHouse.MOD_ID+"_exploration") @PrefixGameTestTemplate(false)
public final class YardReturnTests {
    private static LiteraryTests.Fixture active;
    @AfterBatch(batch="yard_return") public static void done(ServerLevel l){HouseReadSites.clear();if(active!=null){active.close();active=null;}}
    private static void stand(ServerPlayer p,ServerLevel l,BlockPos b){p.teleportTo(l,b.getX()+.5,b.getY(),b.getZ()+.5,180,0);p.setNoGravity(true);p.hasChangedDimension();}
    private static void ready(ServerPlayer p,LabyrinthData.Door from,LabyrinthPlace next){
        var d=LabyrinthData.get(p.server);var own=HouseReadSites.state(p.server,p.getUUID());var v=own.getCompound("kitchen");
        v.putBoolean("Entered",true);v.putBoolean("Here",true);v.putBoolean("Hung",true);v.putString("SourceDoor",from.id);v.putString("NextPlace",next.id());own.put("kitchen",v);own.putString("Active","kitchen");d.setStateEntry(HouseReadSites.STATE,p.getUUID().toString(),own);
    }
    private static boolean kitchensReady(ServerPlayer p,ServerPlayer q){boolean first=HouseReadSites.prepare(p,HouseReadSites.Room.KITCHEN)!=null,second=HouseReadSites.prepare(q,HouseReadSites.Room.KITCHEN)!=null;if(!first||!second)java.util.concurrent.locks.LockSupport.parkNanos(2_000_000L);return first&&second;}
    private static boolean arrived(ServerPlayer p,BlockPos origin){return HouseReadSites.inside(p)==null&&!LabyrinthDoors.isBusy(p)&&LabyrinthPlaces.placeAt(origin,p.blockPosition())==YardReturns.PLACE&&p.level().dimension().equals(NovelRooms.dimension(YardReturns.PLACE));}

    @GameTest(template="empty",batch="yard_return",timeoutTicks=3600)
    public static void separatePrivateReturnsMeetOnOneSavedApronWithoutSharingProgressOrChangingTheirWaysOn(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);active=f;var b=f.base(YardReturns.PLACE);var a=LabyrinthPlaces.slotBounds(f.origin,YardReturns.PLACE);
        f.chunks.hold(f.out,new AABB(a.minX(),a.minY(),a.minZ(),a.maxX()+1,a.maxY()+1,a.maxZ()+1));
        var p=NativeTestPlayers.survival(h,"first_return");var q=NativeTestPlayers.survival(h,"late_return");f.players.add(p);f.players.add(q);OpeningSequence.state(p).markEntered();OpeningSequence.state(q).markEntered();
        var source=f.origin.offset(0,0,-5);f.chunks.hold(f.in,new AABB(source).inflate(8));
        var from=new LabyrinthData.Door("native/yard/source",f.in.dimension(),source,Direction.SOUTH,LabyrinthData.DEALT,false);
        h.onEachTick(()->{for(var reader:java.util.List.of(p,q))if(LabyrinthDoors.isBusy(reader))HouseTransitionEvents.onPlayerTick(new PlayerTickEvent.Post(reader));});
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the shared apron and source are resident"))
            .thenExecute(()->{
                YardRooms.build(f.out,b,YardReturns.PLACE);LabyrinthBuilder.registerDoors(f.data(),YardReturns.PLACE,b);f.data().putDoor(from);
                NovelRooms.door(f.in,source,Direction.SOUTH,Blocks.OAK_DOOR,false);stand(p,f.in,source.relative(Direction.SOUTH,2));stand(q,f.in,source.relative(Direction.SOUTH,3));
                f.data().deal(p.getUUID(),from,LabyrinthPlace.GRAY_CORRIDOR.id(),false);f.data().deal(q.getUUID(),from,LabyrinthPlace.STRAIGHT_HALL.id(),false);
                YardReturns.reserve(p,from);HouseReadSites.prepare(p,HouseReadSites.Room.KITCHEN);HouseReadSites.prepare(q,HouseReadSites.Room.KITCHEN);
                for(var reader:java.util.List.of(p,q))f.chunks.hold(f.in,HouseReadRooms.bounds(HouseReadSites.base(reader.server,reader.getUUID(),HouseReadSites.Room.KITCHEN),HouseReadSites.Room.KITCHEN).inflate(2));
            })
            .thenWaitUntil(()->h.assertTrue(kitchensReady(p,q),"both original private kitchens finish"))
            .thenExecute(()->{
                ready(p,from,LabyrinthPlace.GRAY_CORRIDOR);stand(p,f.in,HouseReadSites.base(p.server,p.getUUID(),HouseReadSites.Room.KITCHEN));
                // The late reader joins the saved reservation after the first has already gone behind a private door.
                YardReturns.reserve(q,from);ready(q,from,LabyrinthPlace.STRAIGHT_HALL);stand(q,f.in,HouseReadSites.base(q.server,q.getUUID(),HouseReadSites.Room.KITCHEN));
                h.assertTrue(YardReturns.departQuiet(p,from,LabyrinthPlace.GRAY_CORRIDOR),"the finished private room uses the common return");
            })
            .thenWaitUntil(()->h.assertTrue(arrived(p,f.origin),"the first reader reaches the actual apron while the friend is still private"))
            .thenExecute(()->{
                h.assertTrue(HouseReadSites.inside(q)==HouseReadSites.Room.KITCHEN,"waiting outside does not complete or move the other private room");
                var loaded=LabyrinthData.FACTORY.deserializer().apply(f.data().save(new CompoundTag(),f.in.registryAccess()),f.in.registryAccess());
                f.in.getServer().overworld().getDataStorage().set("the_oldest_house_labyrinth",loaded);
                h.assertTrue(YardReturns.departQuiet(q,from,LabyrinthPlace.STRAIGHT_HALL),"a delayed arrival retains its reservation after saved-data reload");
            })
            .thenWaitUntil(()->h.assertTrue(arrived(q,f.origin),"the late reader emerges onto the same shared apron"))
            .thenExecute(()->{
                var d=f.data();var left=YardReturns.entry(p,YardReturns.PLACE);var right=YardReturns.entry(q,YardReturns.PLACE);var exit=d.door(YardReturns.PLACE.id()+"/exit");
                h.assertTrue(left!=null&&right!=null&&!left.lower.equals(right.lower)&&p.distanceToSqr(left.lower.getCenter())<25&&q.distanceToSqr(right.lower.getCenter())<25,"separate visible doors show where the two readers came from");
                h.assertTrue(d.deal(p.getUUID(),exit).place().equals(LabyrinthPlace.GRAY_CORRIDOR.id())&&d.deal(q.getUUID(),exit).place().equals(LabyrinthPlace.STRAIGHT_HALL.id()),"the far gate retains each reader's original dealt continuation");
                h.assertTrue(d.deal(p.getUUID(),from).place().equals(LabyrinthPlace.GRAY_CORRIDOR.id())&&d.deal(q.getUUID(),from).place().equals(LabyrinthPlace.STRAIGHT_HALL.id()),"neither original source discovery was overwritten");
                h.assertTrue(d.peekReturn(p.getUUID()).pos().equals(Vec3.atBottomCenterOf(from.lower))&&d.peekReturn(q.getUUID()).pos().equals(Vec3.atBottomCenterOf(from.lower)),"both personal ways back retain their original source");
                h.assertTrue(WitnessAccount.count(d,p.getUUID())==0&&WitnessAccount.count(d,q.getUUID())==0,"meeting on the gravel grants neither reader another story's credit");
                h.assertTrue(f.out.getEntity(p.getUUID())==p&&f.out.getEntity(q.getUUID())==q,"the original two readers meet, without substitutes");
            }).thenSucceed();
    }
}

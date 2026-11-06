package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.gametest.*;

/** Operate the real cable fittings, push-bar and moving platform with shared occupants. */
@GameTestHolder(TheOldestHouse.MOD_ID+"_exploration") @PrefixGameTestTemplate(false)
public final class DeepTensionTests {
    private static LiteraryTests.Fixture active;
    @AfterBatch(batch="deep_tension") public static void done(ServerLevel l){close();}
    @AfterBatch(batch="deep_tension_safety") public static void safetyDone(ServerLevel l){close();}
    private static void close(){DeepTensionRooms.clearAll();if(active!=null){active.close();active=null;}}
    private static LiteraryTests.Fixture fixture(GameTestHelper h){
        active=new LiteraryTests.Fixture(h);var bounds=LabyrinthPlaces.slotBounds(active.origin,DeepTensionRooms.PLACE);
        active.chunks.hold(active.in,new AABB(bounds.minX(),bounds.minY(),bounds.minZ(),bounds.maxX()+1,bounds.maxY()+1,bounds.maxZ()+1));return active;
    }
    private static ServerPlayer reader(GameTestHelper h,LiteraryTests.Fixture f,String name){var p=NativeTestPlayers.survival(h,name);f.players.add(p);p.setNoGravity(true);return p;}
    private static void stand(ServerPlayer p,ServerLevel l,BlockPos b,double x,double y,double z){p.teleportTo(l,b.getX()+x,b.getY()+y,b.getZ()+z,0,0);p.hasChangedDimension();}
    private static void press(ServerPlayer p,ServerLevel l,BlockPos b,BlockPos rel,boolean back){
        DeepTensionRooms.clearAll();p.setShiftKeyDown(back);var at=b.offset(rel);
        l.getBlockState(at).useWithoutItem(l,p,new BlockHitResult(at.getCenter(),Direction.EAST,at,false));p.setShiftKeyDown(false);
    }
    private static void wheel(ServerPlayer p,ServerLevel l,BlockPos b,BlockPos rel,int turns){
        stand(p,l,b,rel.getX()+1.5,0,rel.getZ()+.5);for(int i=0;i<Math.abs(turns);i++)press(p,l,b,rel,turns<0);
    }
    private static void build(LiteraryTests.Fixture f,BlockPos b){DeepTensionRooms.build(f.in,b);LabyrinthBuilder.registerDoors(f.data(),DeepTensionRooms.PLACE,b);}

    @GameTest(template="empty",batch="deep_tension",timeoutTicks=1400)
    public static void theThreeRoomsHoldTheirCableAndTheFarSidePushBarPreservesTheWayBack(GameTestHelper h){
        var f=fixture(h);var l=f.in;var b=f.base(DeepTensionRooms.PLACE);var p=reader(h,f,"cable_keeper");var q=reader(h,f,"cable_walker");int witnesses=WitnessAccount.count(f.data(),p.getUUID());
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the complete cable rooms are resident"))
            .thenExecute(()->{
                build(f,b);
                h.assertTrue(PuzzleRooms.Puzzle.of(DeepTensionRooms.PLACE).builtOn().equals(java.util.List.of(LabyrinthPlace.TENSION_ROOM)),"the deep room follows the solved early room");
                h.assertTrue(l.getBlockState(b.offset(-2,1,-12)).is(Blocks.GLASS)&&l.getBlockState(b.offset(-2,1,-25)).is(Blocks.GLASS),"real windows join the three mechanisms");
                for(int x=-4;x<=4;x++)for(int z=DeepTensionRooms.PIT_FAR;z<=DeepTensionRooms.PIT_NEAR;z++){var cell=l.getBlockState(b.offset(x,-1,z));h.assertTrue(cell.isAir()||cell.is(Blocks.LADDER),"the final gap spans the full floor at "+x+","+z+"; only its recovery ladder remains");}
                wheel(p,l,b,DeepTensionRooms.ENTRY,4);wheel(p,l,b,DeepTensionRooms.MIDDLE,2);
                h.assertTrue(DeepTensionRooms.doorOpen(l,b)&&!DeepTensionRooms.raised(l),"the first two stations hold the door without solving the final pin");
                stand(q,l,b,.5,0,-24.5);
            })
            .thenWaitUntil(()->h.assertTrue(l.getEntitiesOfClass(LivingEntity.class,new AABB(b.offset(DeepTensionRooms.GATE)).inflate(1)).contains(q),"the walker occupies the actual door section"))
            .thenExecute(()->{
                wheel(p,l,b,DeepTensionRooms.ENTRY,-1);
                h.assertTrue(DeepTensionRooms.weight(l)==4&&DeepTensionRooms.doorOpen(l,b),"a body in the door prevents closing and leaves the cable unchanged");
                stand(q,l,b,1.5,0,-28);
            })
            .thenWaitUntil(()->h.assertTrue(!l.getEntitiesOfClass(LivingEntity.class,new AABB(b.offset(DeepTensionRooms.GATE)).expandTowards(0,1,0).inflate(.1)).contains(q),"the walker has cleared the closing door"))
            .thenExecute(()->{
                wheel(p,l,b,DeepTensionRooms.ENTRY,-1);
                h.assertTrue(DeepTensionRooms.weight(l)==3&&!DeepTensionRooms.doorOpen(l,b),"paying out releases the door to its closer");
                stand(q,l,b,1.5,0,-27.5);press(q,l,b,DeepTensionRooms.BAR,false);
                h.assertTrue(DeepTensionRooms.doorOpen(l,b)&&DeepTensionRooms.weight(l)==3,"the far push-bar opens a way back without winding the cable");
                stand(q,l,b,.5,0,-19.5);
            })
            .thenWaitUntil(()->h.assertTrue(!DeepTensionRooms.doorOpen(l,b),"the closer takes the door after the walker is clear"))
            .thenExecute(()->{
                wheel(p,l,b,DeepTensionRooms.ENTRY,1);wheel(p,l,b,DeepTensionRooms.FAR,1);
                h.assertTrue(!DeepTensionRooms.raised(l),"the last pin needs its full travel");stand(q,l,b,.5,-2,-37.5);
            })
            .thenWaitUntil(()->h.assertTrue(l.getEntitiesOfClass(LivingEntity.class,new AABB(b.offset(0,-2,-38)).inflate(1)).contains(q),"the platform rider is indexed"))
            .thenExecute(()->{
                wheel(p,l,b,DeepTensionRooms.FAR,1);
                h.assertTrue(DeepTensionRooms.raised(l)&&Math.abs(q.getY()-b.getY())<.001&&l.getEntity(q.getUUID())==q,"the shared platform carries the original rider to the landing");
                var saved=LabyrinthData.FACTORY.deserializer().apply(f.data().save(new CompoundTag(),l.registryAccess()),l.registryAccess());
                h.assertTrue(PuzzleRooms.solved(saved,DeepTensionRooms.PLACE)&&saved.state(DeepTensionRooms.STATE).getInt("Far")==2,"the settled catch and cable persist");
                wheel(p,l,b,DeepTensionRooms.ENTRY,-4);
                h.assertTrue(TensionBlock.is(l.getBlockState(b.offset(0,-1,-38)),TensionBlock.Kind.PLATFORM)&&DeepTensionRooms.raised(l),"paying out afterwards leaves the caught platform up");
                h.assertTrue(WitnessAccount.count(f.data(),p.getUUID())==witnesses,"the mechanical passage grants no story credit");
            }).thenSucceed();
    }

    @GameTest(template="empty",batch="deep_tension_safety",timeoutTicks=1600)
    public static void blockedLoadsDoNotConsumeTurnsAndLivingRidersKeepTheirBodies(GameTestHelper h){
        var f=fixture(h);var l=f.in;var b=f.base(DeepTensionRooms.PLACE);var p=reader(h,f,"deep_wheel");var q=reader(h,f,"deep_rider");Pig[] pig={null};
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the safety fixture's native sections are ready"))
            .thenExecute(()->{
                build(f,b);pig[0]=EntityType.PIG.create(l);h.assertTrue(pig[0]!=null,"the persistent animal exists");pig[0].setPersistenceRequired();pig[0].setNoGravity(true);f.extras.add(pig[0]);
                pig[0].moveTo(b.getX()-4.5,b.getY(),b.getZ()-7.5);h.assertTrue(l.addFreshEntity(pig[0]),"the persistent pig is admitted");
            })
            .thenWaitUntil(()->h.assertTrue(l.getEntitiesOfClass(LivingEntity.class,new AABB(b.offset(DeepTensionRooms.SHAFT)).inflate(.1)).contains(pig[0]),"the animal occupies the shaft"))
            .thenExecute(()->{
                wheel(p,l,b,DeepTensionRooms.ENTRY,1);h.assertTrue(DeepTensionRooms.weight(l)==0,"an occupied shaft keeps the complete mechanism unchanged");
                pig[0].teleportTo(b.getX()+2.5,b.getY(),b.getZ()-5.5);
            })
            .thenWaitUntil(()->h.assertTrue(l.getEntitiesOfClass(LivingEntity.class,new AABB(b.offset(DeepTensionRooms.SHAFT)).inflate(.1)).isEmpty(),"the shaft is clear"))
            .thenExecute(()->{
                wheel(p,l,b,DeepTensionRooms.ENTRY,4);wheel(p,l,b,DeepTensionRooms.MIDDLE,2);wheel(p,l,b,DeepTensionRooms.FAR,1);
                l.setBlock(b.offset(0,-1,-38),Blocks.COBBLESTONE.defaultBlockState(),18);wheel(p,l,b,DeepTensionRooms.FAR,1);
                h.assertTrue(DeepTensionRooms.far(l)==1&&!DeepTensionRooms.raised(l)&&l.getBlockState(b.offset(0,-1,-38)).is(Blocks.COBBLESTONE),"a solid obstruction is retained and the turn is not taken");
                l.setBlock(b.offset(0,-1,-38),Blocks.AIR.defaultBlockState(),18);stand(q,l,b,.5,-2,-37.5);
                pig[0].teleportTo(b.getX()+1.5,b.getY()-2,b.getZ()-37.5);
            })
            .thenWaitUntil(()->h.assertTrue(l.getEntitiesOfClass(LivingEntity.class,new AABB(b.offset(0,-2,-38)).inflate(2)).containsAll(java.util.List.of(q,pig[0])),"both actual riders are indexed"))
            .thenExecute(()->{
                l.setBlock(b.offset(0,1,-38),Blocks.STONE.defaultBlockState(),18);wheel(p,l,b,DeepTensionRooms.FAR,1);
                h.assertTrue(!DeepTensionRooms.raised(l)&&DeepTensionRooms.far(l)==1&&q.getY()==b.getY()-2,"headroom blocks the rise before either rider moves");
                l.setBlock(b.offset(0,1,-38),Blocks.AIR.defaultBlockState(),18);wheel(p,l,b,DeepTensionRooms.FAR,1);
                h.assertTrue(DeepTensionRooms.raised(l)&&Math.abs(q.getY()-b.getY())<.001&&Math.abs(pig[0].getY()-b.getY())<.001&&l.getEntity(pig[0].getUUID())==pig[0],"the reader and animal ride intact, without replacement");
            }).thenSucceed();
    }
}

package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.gametest.*;

/**
 * 0.4.346, the ditch in flood (YardFlash), with readers in it: the cut up the east bank walked from the water to its ledge; rain, then the
 * water up to the knees without a push; the surge carrying the reader in the trench down it, into the culvert's crawl and out onto the
 * gravel beyond, breath kept and unhurt, while the reader on the ledge stays dry and still; the water down again.
 */
@GameTestHolder(TheOldestHouse.MOD_ID+"_exploration") @PrefixGameTestTemplate(false)
public final class YardFlashTests {
    private static LiteraryTests.Fixture active;
    @AfterBatch(batch="yard_flash") public static void flashDone(ServerLevel l){close();}
    private static void close(){if(active!=null){active.close();active=null;}}
    private static LiteraryTests.Fixture fixture(GameTestHelper h){active=new LiteraryTests.Fixture(h);var a=LabyrinthPlaces.slotBounds(active.origin,YardFlash.PLACE);active.chunks.hold(active.out,new AABB(a.minX(),a.minY(),a.minZ(),a.maxX()+1,a.maxY()+1,a.maxZ()+1));return active;}
    private static ServerPlayer player(GameTestHelper h,LiteraryTests.Fixture f,String name){var p=NativeTestPlayers.survival(h,name);f.players.add(p);p.setNoGravity(true);return p;}
    private static void at(ServerPlayer p,ServerLevel l,BlockPos b,double x,double y,double z){p.teleportTo(l,b.getX()+x,b.getY()+y,b.getZ()+z,180,0);p.setDeltaMovement(Vec3.ZERO);p.hasChangedDimension();}
    private static void clock(LiteraryTests.Fixture f,ServerLevel l,long c){var s=YardFlash.state(l);s.putLong("Clock",c);f.data().setStateEntry(YardFlash.STATE,YardFlash.PLACE.id(),s);}
    private static Vec3 rel(ServerPlayer p,BlockPos b){return p.position().subtract(Vec3.atLowerCornerOf(b));}
    /** Any water above the ditch's floor (its waterlogged slabs aside), or null. */
    private static BlockPos standing(ServerLevel l,BlockPos b){for(int z=0;z>=-35;z--)for(int x=-6;x<=6;x++)for(int y=0;y<=3;y++)if(l.getBlockState(b.offset(x,y,z)).is(Blocks.WATER))return new BlockPos(x,y,z);return null;}

    @GameTest(template="empty",batch="yard_flash",timeoutTicks=2400)
    public static void theSurgeCarriesTheTrenchThroughThePipeAndTheLedgeStaysDry(GameTestHelper h){
        var f=fixture(h);var l=f.out;var b=f.base(YardFlash.PLACE);var climber=player(h,f,"flash_climber");var wader=player(h,f,"flash_wader");var ledge=player(h,f,"flash_ledge");
        var route=List.of(new Vec3(1.5,0,-5.5),new Vec3(2.5,0,-5.5),new Vec3(3.5,.5,-5.5),new Vec3(4.5,1,-5.5));int[] step={-1};int[] stuck={0};double[] best={Double.MAX_VALUE};boolean[] carry={false};Vec3[] dry={null};
        h.onEachTick(()->{
            if(step[0]>=0&&step[0]<route.size()){var goal=Vec3.atLowerCornerOf(b).add(route.get(step[0]));var d=goal.subtract(climber.position()).multiply(1,0,1);
                if(d.length()<.18){step[0]++;best[0]=Double.MAX_VALUE;stuck[0]=0;}
                else{if(d.length()<best[0]-.015){best[0]=d.length();stuck[0]=0;}else stuck[0]++;h.assertTrue(stuck[0]<90,"the cut up the bank can be walked at "+rel(climber,b));
                    var move=d.normalize().scale(.11);double vy=climber.onGround()?0:(climber.getDeltaMovement().y-.08)*.98;climber.setDeltaMovement(move.x,vy,move.z);climber.move(MoverType.SELF,climber.getDeltaMovement());if(climber.verticalCollision)climber.setDeltaMovement(move.x,0,move.z);}}
            // Carried as a body is: the current's push, and its weight on the floor, so it steps up over the culvert's lip.
            if(carry[0]){var v=wader.getDeltaMovement();wader.move(MoverType.SELF,new Vec3(v.x,wader.onGround()?0:-.08,v.z));}
        });
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the ditch is resident"))
            .thenExecute(()->{
                YardConnectors.build(l,b,YardFlash.PLACE);LabyrinthBuilder.registerDoors(f.data(),YardFlash.PLACE,b);
                h.assertTrue(l.getBlockState(b.offset(0,0,-14)).isAir()&&!l.getBlockState(b.offset(0,1,-14)).isAir(),"the same ditch: the culvert's crawl one block high");
                at(climber,l,b,.5,-.5,-5.5);at(wader,l,b,.5,-.5,-7.5);at(ledge,l,b,4.5,1,-8.5);dry[0]=ledge.position();step[0]=0;
            })
            .thenWaitUntil(()->h.assertTrue(step[0]==route.size()&&climber.getY()-b.getY()>.9,"out of the water, up the half-steps onto the ledge: "+rel(climber,b)))
            .thenExecute(()->clock(f,l,YardFlash.WARNING-1))
            .thenWaitUntil(()->h.assertTrue(YardFlash.phase(YardFlash.state(l).getLong("Clock"))==1,"rain coming on"))
            .thenExecute(()->{h.assertTrue(l.getBlockState(b.offset(0,0,-7)).isAir(),"still only boot-deep");clock(f,l,YardFlash.RISING-1);})
            .thenWaitUntil(()->h.assertTrue(l.getBlockState(b.offset(0,0,-7)).is(Blocks.WATER),"the water rises to the knees"))
            .thenExecute(()->{h.assertTrue(wader.getDeltaMovement().z==0,"rising, it doesn't carry anyone yet");clock(f,l,YardFlash.SURGE-1);carry[0]=true;})
            .thenWaitUntil(()->h.assertTrue(YardCrawl.crawling(wader),"carried to the culvert, the reader is taken into the crawl: "+rel(wader,b)))
            .thenWaitUntil(()->h.assertTrue(rel(wader,b).z<-24.5,"and through the pipe onto the gravel beyond: "+rel(wader,b)))
            .thenExecute(()->{
                carry[0]=false;
                h.assertTrue(wader.getHealth()==wader.getMaxHealth()&&wader.getAirSupply()>200,"unhurt, breath kept");
                h.assertTrue(ledge.position().distanceTo(dry[0])<.01&&!l.getBlockState(b.offset(4,1,-8)).is(Blocks.WATER),"the reader on the ledge is dry and where they stood");
                clock(f,l,YardFlash.DRAIN-1);
            })
            .thenWaitUntil(()->h.assertTrue(l.getBlockState(b.offset(0,0,-7)).isAir()&&l.getBlockState(b.offset(0,0,-16)).isAir(),"the water goes back down"))
            // Nothing left standing where it ran: the doorway, the culvert's sides, the gravel beyond, the cuts' first steps.
            .thenWaitUntil(()->h.assertTrue(standing(l,b)==null,"every drop goes with it: water left at "+standing(l,b)))
            .thenExecute(()->{
                h.assertTrue(WitnessAccount.count(f.data(),wader.getUUID())==0,"no story credit");
                // Left mid-surge, it comes back as the quiet ditch, with the whole warning ahead of the next reader.
                clock(f,l,YardFlash.SURGE+5);
            })
            .thenWaitUntil(()->h.assertTrue(l.getBlockState(b.offset(0,0,-16)).is(Blocks.WATER),"the surge again"))
            .thenExecute(()->{YardFlash.reset(l,b,YardFlash.state(l));h.assertTrue(YardFlash.phase(YardFlash.state(l).getLong("Clock"))==0,"the clock back to its start");})
            .thenWaitUntil(()->h.assertTrue(standing(l,b)==null,"and dry: water left at "+standing(l,b)))
            .thenSucceed();
    }
}

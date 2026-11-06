package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(TheOldestHouse.MOD_ID+"_exploration") @PrefixGameTestTemplate(false)
public final class YardFieldsTests {
    private static LiteraryTests.Fixture active;
    @AfterBatch(batch="yard_survey") public static void surveyDone(ServerLevel l){close();}
    @AfterBatch(batch="yard_flood") public static void floodDone(ServerLevel l){close();}
    @AfterBatch(batch="yard_flood_paths") public static void floodPathsDone(ServerLevel l){close();}
    private static void close(){if(active!=null){active.close();active=null;}}
    private static LiteraryTests.Fixture fixture(GameTestHelper h,LabyrinthPlace place){active=new LiteraryTests.Fixture(h);var a=LabyrinthPlaces.slotBounds(active.origin,place);active.chunks.hold(active.out,new AABB(a.minX(),a.minY(),a.minZ(),a.maxX()+1,a.maxY()+1,a.maxZ()+1));return active;}
    private static ServerPlayer player(GameTestHelper h,LiteraryTests.Fixture f,String name){var p=NativeTestPlayers.survival(h,name);f.players.add(p);p.setNoGravity(true);return p;}
    private static void at(ServerPlayer p,ServerLevel l,BlockPos b,double x,double y,double z){p.teleportTo(l,b.getX()+x,b.getY()+y,b.getZ()+z,180,0);p.setDeltaMovement(Vec3.ZERO);p.hasChangedDimension();}
    private static void use(ServerPlayer p,BlockPos pos){p.serverLevel().getBlockState(pos).useWithoutItem(p.serverLevel(),p,new BlockHitResult(pos.getCenter(),Direction.SOUTH,pos,false));}

    @GameTest(template="empty",batch="yard_survey",timeoutTicks=2600)
    public static void twoRealSlotsNameOneGateAndItsDipCanBeWalkedOutAndBack(GameTestHelper h){
        var place=LabyrinthPlace.YARD_SURVEY;var f=fixture(h,place);var l=f.out;var b=f.base(place);var p=player(h,f,"stake_reader");int[] path={-1};List<Vec3> route=new ArrayList<>();int[] stuck={0};double[] best={Double.MAX_VALUE};
        h.onEachTick(()->{
            if(path[0]<0||path[0]>=route.size())return;var d=route.get(path[0]).subtract(p.position()).multiply(1,0,1);
            if(d.length()<.18){path[0]++;best[0]=Double.MAX_VALUE;stuck[0]=0;return;}
            if(d.length()<best[0]-.015){best[0]=d.length();stuck[0]=0;}else stuck[0]++;
            h.assertTrue(stuck[0]<90,"the field path supports a walking body at "+p.position().subtract(Vec3.atLowerCornerOf(b)));
            var move=d.normalize().scale(.11);double vy=p.onGround()?0:(p.getDeltaMovement().y-.08)*.98;
            p.setDeltaMovement(move.x,vy,move.z);p.move(MoverType.SELF,p.getDeltaMovement());if(p.verticalCollision)p.setDeltaMovement(move.x,0,move.z);
        });
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the pasture is resident"))
            .thenExecute(()->{
                YardFields.build(l,b,place);LabyrinthBuilder.registerDoors(f.data(),place,b);at(p,l,b,.5,0,-8.5);
                int gate=YardFields.GATES[YardFields.live(l)],aligned=0;var eye=p.getEyePosition();
                for(int x:YardFields.GATES){boolean through=true;var target=new Vec3(b.getX()+x+.5,eye.y,b.getZ()-24.5);
                    for(var rel:List.of(new BlockPos(gate/4,1,-13),new BlockPos(gate/2,1,-17))){var pos=b.offset(rel);var block=l.getBlockState(pos);
                        through&=new AABB(pos).clip(eye,target).isPresent()&&block.getCollisionShape(l,pos).clip(eye,target,pos)==null;
                    }
                    if(through){aligned++;h.assertTrue(x==gate,"the actual slots sight the saved gate");}
                }
                h.assertTrue(aligned==1,"exactly one gate lines up through both native apertures");
                int wrong=YardFields.GATES[(YardFields.live(l)+1)%3];at(p,l,b,wrong+.5,0,-22.5);use(p,b.offset(wrong+1,1,-24));at(p,l,b,wrong+.5,0,-26);
            }).thenIdle(3).thenExecute(()->{
                h.assertTrue(!PuzzleRooms.solved(f.data(),place),"a different gate into the pasture does not resolve the alignment");
                int gate=YardFields.GATES[YardFields.live(l)];at(p,l,b,gate+.5,0,-22.5);use(p,b.offset(gate+1,1,-24));at(p,l,b,gate+.5,0,-26);
                route.add(Vec3.atLowerCornerOf(b).add(gate+.5,0,-36.5));route.add(Vec3.atLowerCornerOf(b).add(8.5,0,-36.5));route.add(Vec3.atLowerCornerOf(b).add(8.5,0,-41.3));path[0]=0;
            })
            .thenWaitUntil(()->h.assertTrue(path[0]==route.size(),"the reader walks the dip, the bend and every half-step to the barn's back gate"))
            .thenExecute(()->{
                h.assertTrue(PuzzleRooms.solved(f.data(),place)&&Math.abs(p.getY()-b.getY())<.1,"the alignment opens a physically reachable settled route");
                route.add(Vec3.atLowerCornerOf(b).add(8.5,0,-36.5));route.add(Vec3.atLowerCornerOf(b).add(YardFields.GATES[YardFields.live(l)]+.5,0,-36.5));route.add(Vec3.atLowerCornerOf(b).add(YardFields.GATES[YardFields.live(l)]+.5,0,-24));
            })
            .thenWaitUntil(()->h.assertTrue(path[0]==route.size(),"the same native route also climbs back out of the dip"))
            .thenExecute(()->{
                var loaded=LabyrinthData.FACTORY.deserializer().apply(f.data().save(new CompoundTag(),l.registryAccess()),l.registryAccess());
                h.assertTrue(PuzzleRooms.solved(loaded,place)&&loaded.stateEntry(YardFields.STATE,place.id()).getInt("Live")==YardFields.live(l)&&WitnessAccount.count(loaded,p.getUUID())==0,"the stable proof and solved passage save without story credit");
            }).thenSucceed();
    }

    @GameTest(template="empty",batch="yard_flood_paths",timeoutTicks=2600)
    public static void aWalkingBodyCanReachTheRecessAndUseTheHighBankInBothDirections(GameTestHelper h){
        var f=fixture(h,LabyrinthPlace.YARD_FLOOD);var l=f.out;var b=f.base(LabyrinthPlace.YARD_FLOOD);var p=player(h,f,"bank_walker");var q=player(h,f,"recess_walker");
        var paths=List.of(List.of(new Vec3(-4.5,0,-3.5),new Vec3(-4.5,2,-12.5),new Vec3(-4.5,0,-35.5),new Vec3(-4.5,2,-12.5),new Vec3(-4.5,0,-3.5),new Vec3(-.5,0,-3.5)),List.of(new Vec3(3.5,1,-17.5),new Vec3(.5,-.5,-17.5)));
        int[] step={-1,-1},stuck={0,0};double[] best={Double.MAX_VALUE,Double.MAX_VALUE};var walkers=List.of(p,q);
        h.onEachTick(()->{for(int i=0;i<2;i++){if(step[i]<0||step[i]>=paths.get(i).size())continue;var who=walkers.get(i);var goal=Vec3.atLowerCornerOf(b).add(paths.get(i).get(step[i]));var delta=goal.subtract(who.position()).multiply(1,0,1);
            if(delta.length()<.18){h.assertTrue(Math.abs(who.getY()-goal.y)<.55,"the walking body reaches the route's actual height");step[i]++;best[i]=Double.MAX_VALUE;stuck[i]=0;continue;}
            if(delta.length()<best[i]-.015){best[i]=delta.length();stuck[i]=0;}else stuck[i]++;h.assertTrue(stuck[i]<90,"the shelter route can be walked at "+who.position().subtract(Vec3.atLowerCornerOf(b)));
            var move=delta.normalize().scale(.11);double vy=who.onGround()?0:(who.getDeltaMovement().y-.08)*.98;who.setDeltaMovement(move.x,vy,move.z);who.move(MoverType.SELF,who.getDeltaMovement());if(who.verticalCollision)who.setDeltaMovement(move.x,0,move.z);
        }});
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the bank and recess sections are resident"))
            .thenExecute(()->{YardFields.build(l,b,LabyrinthPlace.YARD_FLOOD);at(p,l,b,-.5,0,-3.5);at(q,l,b,.5,-.5,-17.5);step[0]=0;step[1]=0;})
            .thenWaitUntil(()->h.assertTrue(step[0]==paths.get(0).size()&&step[1]==paths.get(1).size(),"both native shelter routes can be walked out and back"))
            .thenSucceed();
    }

    @GameTest(template="empty",batch="yard_flood",timeoutTicks=1600)
    public static void theWarnedSurgeCarriesBodiesToTheTurnoutWhileBothSheltersStaySafe(GameTestHelper h){
        var place=LabyrinthPlace.YARD_FLOOD;var f=fixture(h,place);var l=f.out;var b=f.base(place);var p=player(h,f,"flood_walker");var q=player(h,f,"recess_friend");var high=player(h,f,"bank_friend");Pig[] pig={null};boolean[] carry={false};Vec3[] safe={null,null};
        h.onEachTick(()->{if(carry[0])for(var e:List.of(p,pig[0])){var v=e.getDeltaMovement();e.move(MoverType.SELF,v);}});
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the cut, recess and embankment are resident"))
            .thenExecute(()->{
                YardFields.build(l,b,place);LabyrinthBuilder.registerDoors(f.data(),place,b);at(p,l,b,.5,-.5,-15.5);at(q,l,b,3.5,1,-18.5);at(high,l,b,-4.5,2,-15.5);safe[0]=q.position();safe[1]=high.position();
                pig[0]=EntityType.PIG.create(l);h.assertTrue(pig[0]!=null,"the animal exists");pig[0].setPersistenceRequired();pig[0].setNoGravity(true);pig[0].setNoAi(true);pig[0].moveTo(b.getX()+.5,b.getY()-.5,b.getZ()-12.5);f.extras.add(pig[0]);h.assertTrue(l.addFreshEntity(pig[0]),"the original persistent animal is admitted");
                var state=YardFields.state(l,place);state.putLong("Clock",YardFields.WARNING-1);f.data().setStateEntry(YardFields.STATE,place.id(),state);
            })
            .thenWaitUntil(()->h.assertTrue(l.getBlockState(b.offset(YardFields.GRATE)).getValue(YardBlock.STAGE)==1,"twigs visibly collect before the water rises"))
            .thenExecute(()->{h.assertTrue(p.getDeltaMovement().z==0,"the debris warning does not silently shove the reader");var s=YardFields.state(l,place);s.putLong("Clock",YardFields.RISING-1);f.data().setStateEntry(YardFields.STATE,place.id(),s);})
            .thenWaitUntil(()->h.assertTrue(l.getBlockState(b.offset(0,0,-15)).is(Blocks.WATER),"water backs up before the surge"))
            .thenExecute(()->{var s=YardFields.state(l,place);s.putLong("Clock",YardFields.SURGE-1);f.data().setStateEntry(YardFields.STATE,place.id(),s);carry[0]=true;})
            .thenWaitUntil(()->h.assertTrue(p.getZ()<b.getZ()-30&&pig[0].getZ()<b.getZ()-30,"actual motion carries the reader and the animal into the downstream turnout: "+p.position().subtract(Vec3.atLowerCornerOf(b))+" / "+pig[0].position().subtract(Vec3.atLowerCornerOf(b))))
            .thenExecute(()->{
                carry[0]=false;h.assertTrue(p.getHealth()==p.getMaxHealth()&&pig[0].getHealth()==pig[0].getMaxHealth()&&p.getAirSupply()>200&&l.getEntity(pig[0].getUUID())==pig[0],"the wash preserves living bodies and their breath");
                h.assertTrue(q.position().distanceTo(safe[0])<.01&&high.position().distanceTo(safe[1])<.01,"the maintenance recess and the longer high bank shelter their readers");
                h.assertTrue(l.noCollision(p,new AABB(b.getX()-4.8,b.getY()+2.01,b.getZ()-19.8,b.getX()-4.2,b.getY()+3.8,b.getZ()-19.2)),"the high return route has real standing headroom");
                var s=YardFields.state(l,place);s.putLong("Clock",YardFields.DRAIN-1);f.data().setStateEntry(YardFields.STATE,place.id(),s);
            })
            .thenWaitUntil(()->h.assertTrue(l.getBlockState(b.offset(0,0,-15)).isAir(),"the authored surge drains again"))
            .thenSucceed();
    }
}

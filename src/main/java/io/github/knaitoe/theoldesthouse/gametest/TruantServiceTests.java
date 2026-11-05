package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseExperience;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;

/**
 * 0.4.307, Johnny Truant's service halls (docs/TRUANT_SERVICE_0_4_307.md), played with readers' hands. The chalk arrow drawn toward the
 * drained branch for help and turned toward the drop for the one he wants, the fall into the sump kept against him, and a reader under
 * twenty beside them keeping it a claim. The steam main shivering, venting and scalding whoever is round the corner in it, his knock at
 * the quiet for help and at the shiver when turned. The lift held until everyone is aboard and let down, brought back up for the
 * landing, and let go as the one he wants crosses the sill: the car lurches and drops with whoever is aboard, the one at the sill
 * falls after it, and the car's own brake stops it.
 */
@GameTestHolder(TheOldestHouse.MOD_ID+"_exploration") @PrefixGameTestTemplate(false)
public final class TruantServiceTests {
    private TruantServiceTests(){}
    private static LiteraryTests.Fixture active;
    static void done(){
        Talk.clearAll();
        if(active!=null){for(var place:TruantService.PLACES){var b=active.base(place);var r=place.room();
            for(var a:active.in.getEntitiesOfClass(LiteraryActor.class,new AABB(b.getX()+r.minX(),b.getY()+r.minY(),b.getZ()+r.minZ(),b.getX()+r.maxX()+1,b.getY()+r.maxY()+1,b.getZ()+r.maxZ()+1)))a.discard();}
            active.close();active=null;}
        Truant.fresh();Truant.apart();Truant.force(null);TruantService.clearAll();
    }
    @AfterBatch(batch="truant_chalk") public static void doneChalk(ServerLevel l){done();}
    @AfterBatch(batch="truant_knock") public static void doneKnock(ServerLevel l){done();}
    @AfterBatch(batch="truant_lift") public static void doneLift(ServerLevel l){done();}
    private static void hold(LiteraryTests.Fixture f,LabyrinthPlace place){var slot=LabyrinthPlaces.slotBounds(f.origin,place);f.chunks.hold(f.in,new AABB(slot.minX(),slot.minY(),slot.minZ(),slot.maxX()+1,slot.maxY()+1,slot.maxZ()+1));}
    private static void at(ServerPlayer p,ServerLevel l,BlockPos b,double x,double y,double z,boolean floating){p.teleportTo(l,b.getX()+x,b.getY()+y,b.getZ()+z,180,0);p.setNoGravity(floating);p.setDeltaMovement(Vec3.ZERO);}
    private static void deepest(LiteraryTests.Fixture f,ServerPlayer p,int depth){var d=f.data();var exp=HouseExperience.record(d,p.getUUID());exp.putInt("Deepest",depth);HouseExperience.save(d,p.getUUID(),exp);}
    /** A veteran's history with him: met three times, twenty passes noted at the second meeting, his name known. */
    private static CompoundTag history(){var own=new CompoundTag();own.putInt("Meetings",3);own.putInt("Talked",3);own.putInt("Help",Truant.HELP_DOOR|Truant.HELP_STEADY|Truant.HELP_LAMP);own.putInt("Revealed",2);own.putBoolean("Twenty",true);own.putInt("TwentyAt",2);own.putBoolean("Named",true);return own;}
    private static void vulnerable(ServerPlayer p){p.setHealth(p.getMaxHealth());p.hasChangedDimension();p.invulnerableTime=0;}
    private static void away(ServerPlayer p,LiteraryTests.Fixture f){var b=f.base(LabyrinthPlace.JUNCTION);p.teleportTo(f.in,b.getX()+.5,b.getY(),b.getZ()-3.5,180,0);p.setNoGravity(true);}
    private static void setUp(LiteraryTests.Fixture f,LabyrinthPlace place){Truant.fresh();Truant.apart();TruantService.clearAll();TruantPlaces.build(f.in,f.base(place),place);LabyrinthBuilder.registerDoors(f.data(),place,f.base(place));}

    @GameTest(template="empty",batch="truant_chalk",timeoutTicks=1600)
    public static void johnnysChalkArrowHelpsThenTurnsTowardTheDrop(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);active=f;var place=TruantService.CHALK;var b=f.base(place);var l=f.in;var server=l.getServer();hold(f,place);hold(f,LabyrinthPlace.JUNCTION);
        var a=NativeTestPlayers.survival(h,"chalk_veteran");var e=NativeTestPlayers.survival(h,"chalk_early");f.players.add(a);f.players.add(e);
        var arrow=b.offset(TruantService.ARROW_AT);
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the junction is resident"))
            .thenExecute(()->{
                setUp(f,place);deepest(f,a,25);deepest(f,e,5);Truant.remember(server,a.getUUID(),history());away(e,f);
                h.assertTrue(place.isGray()&&TruantPlaces.is(place)&&TruantPlaces.dealt(LabyrinthPacing.STONE_DEPTH-1).indexOf(place)<0&&TruantPlaces.dealt(LabyrinthPacing.STONE_DEPTH).contains(place),"one of his places, dealt only from the stone halls");
                h.assertTrue(TruantFittingBlock.stage(l.getBlockState(arrow))==0,"the chalk wall bare");
                Truant.force(Truant.Turn.HELP);at(a,l,b,.5,0,-6.5,true);})
            .thenWaitUntil(()->h.assertTrue(TruantFittingBlock.stage(l.getBlockState(arrow))==1,"for help he chalks the arrow toward the branch the pipes run down"))
            .thenExecute(()->h.assertTrue((Truant.record(server,a.getUUID()).getInt("Help")&TruantService.HELP_CHALK)!=0,"and that help is kept"))
            .thenExecute(()->{away(a,f);})
            .thenIdle(5)
            .thenExecute(()->{Truant.fresh();Truant.apart();TruantService.clearAll();Truant.force(Truant.Turn.ARROW);at(a,l,b,.5,0,-6.5,true);at(e,l,b,-.5,0,-5.5,true);})
            .thenWaitUntil(()->h.assertTrue(Truant.turn()==Truant.Turn.CLAIM,"with a reader under twenty beside them, the turned arrow becomes a claim"))
            .thenIdle(20)
            .thenExecute(()->{
                h.assertTrue(TruantFittingBlock.stage(l.getBlockState(arrow))==1,"and the arrow still points the safe way");
                away(a,f);away(e,f);})
            .thenIdle(5)
            .thenExecute(()->{Truant.fresh();Truant.apart();TruantService.clearAll();Truant.force(Truant.Turn.ARROW);at(a,l,b,.5,0,-6.5,true);})
            .thenWaitUntil(()->h.assertTrue(Truant.turn()==Truant.Turn.ARROW&&TruantFittingBlock.stage(l.getBlockState(arrow))==2,"alone, on a later pass, he turns the arrow toward the drop"))
            .thenExecute(()->{
                h.assertTrue(l.getBlockState(b.offset(TruantService.SUMP_X,-2,-10)).isAir()&&l.getBlockState(b.offset(TruantService.SUMP_X,-3,-12)).is(net.minecraft.world.level.block.Blocks.LADDER),"the drop is real, and there is a ladder out of it");
                at(a,l,b,TruantService.SUMP_X+.5,TruantService.SUMP_FLOOR+1,-10.5,false);})
            .thenWaitUntil(()->h.assertTrue(Truant.record(server,a.getUUID()).getString("Harm").equals("arrow")&&Truant.record(server,a.getUUID()).getInt("Arrowed")==1,"the fall is kept against him"))
            .thenSucceed();
    }

    @GameTest(template="empty",batch="truant_knock",timeoutTicks=2400)
    public static void johnnysKnockMeansClearUntilItDoesnt(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);active=f;var place=TruantService.KNOCK;var b=f.base(place);var l=f.in;var server=l.getServer();hold(f,place);hold(f,LabyrinthPlace.JUNCTION);
        var a=NativeTestPlayers.survival(h,"knock_veteran");f.players.add(a);
        float[] hp={0};
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the elbow is resident"))
            .thenExecute(()->{setUp(f,place);deepest(f,a,25);Truant.remember(server,a.getUUID(),history());Truant.force(Truant.Turn.HELP);at(a,l,b,.5,0,-5.5,true);})
            .thenWaitUntil(()->h.assertTrue((Truant.record(server,a.getUUID()).getInt("Help")&TruantService.HELP_KNOCK)!=0,"from round the corner he knocks once the main is quiet"))
            .thenExecute(()->h.assertTrue(TruantService.phase(l)>=TruantService.VENT,"his knock came in the quiet, after the vent: "+TruantService.phase(l)))
            .thenWaitUntil(()->h.assertTrue(TruantService.phase(l)>=1&&TruantService.phase(l)<TruantService.SHIVER&&TruantFittingBlock.stage(l.getBlockState(b.offset(-1,2,-3)))==1,"the shiver shows on the pipe all the way back up the corridor"))
            .thenExecute(()->{vulnerable(a);hp[0]=a.getHealth();at(a,l,b,-.5,0,-10.5,true);})
            .thenWaitUntil(()->h.assertTrue(a.getHealth()<hp[0],"whoever is round the corner when it vents is scalded"))
            .thenExecute(()->{
                h.assertTrue(!Truant.record(server,a.getUUID()).getString("Harm").equals("knock"),"in a helpful meeting the steam is the hall's, not his");
                away(a,f);})
            .thenIdle(5)
            .thenExecute(()->{Truant.fresh();Truant.apart();TruantService.clearAll();Truant.force(Truant.Turn.KNOCK);vulnerable(a);at(a,l,b,-.5,0,-10.5,true);})
            .thenWaitUntil(()->h.assertTrue(Truant.record(server,a.getUUID()).getString("Harm").equals("knock")&&Truant.record(server,a.getUUID()).getInt("Scalded")==1,"turned, the same knock comes at the shiver, and the scald is kept against him"))
            .thenSucceed();
    }

    @GameTest(template="empty",batch="truant_lift",timeoutTicks=2400)
    public static void johnnyHoldsTheLiftThenLetsItGo(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);active=f;var place=TruantService.LIFT;var b=f.base(place);var l=f.in;var server=l.getServer();hold(f,place);hold(f,LabyrinthPlace.JUNCTION);
        var a=NativeTestPlayers.survival(h,"lift_veteran");var c=NativeTestPlayers.survival(h,"lift_friend");f.players.add(a);f.players.add(c);
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the lift is resident"))
            .thenExecute(()->{
                setUp(f,place);deepest(f,a,25);deepest(f,c,25);Truant.remember(server,a.getUUID(),history());
                h.assertTrue(TruantService.car(l,b)==TruantService.TOP&&l.getBlockState(b.offset(TruantService.LEDGE)).isAir()&&!l.getBlockState(b.offset(TruantService.LEDGE.below())).isAir(),"the car at the landing, a ledge in the shaft wall");
                Truant.force(Truant.Turn.HELP);at(a,l,b,-.5,TruantService.LANDING,-12.5,false);at(c,l,b,.5,TruantService.LANDING,-11.5,false);})
            .thenWaitUntil(()->h.assertTrue(TruantService.car(l,b)==TruantService.BOTTOM,"once everyone is aboard he lets it down"))
            .thenExecute(()->{
                h.assertTrue(a.getY()-b.getY()<TruantService.BOTTOM+1.6&&c.getY()-b.getY()<TruantService.BOTTOM+1.6,"and both ride it down");
                h.assertTrue((Truant.record(server,a.getUUID()).getInt("Help")&TruantService.HELP_BRAKE)!=0,"held for them");
                away(c,f);at(a,l,b,.5,TruantService.LANDING,-9.5,true);})
            .thenWaitUntil(()->h.assertTrue(TruantService.car(l,b)==TruantService.TOP,"empty, it comes back up for whoever is on the landing"))
            .thenExecute(()->{away(a,f);})
            .thenIdle(5)
            .thenExecute(()->{Truant.fresh();Truant.apart();TruantService.clearAll();Truant.force(Truant.Turn.BRAKE);Truant.remember(server,c.getUUID(),history());
                vulnerable(a);at(c,l,b,-.5,TruantService.LANDING,-12.5,false);at(a,l,b,.5,TruantService.LANDING,-11.2,false);})
            .thenWaitUntil(()->h.assertTrue(TruantService.car(l,b)<TruantService.TOP,"as the one he wants crosses the sill, he lets go: the car lurches"))
            .thenWaitUntil(()->h.assertTrue(TruantService.car(l,b)==TruantService.BOTTOM&&c.getY()-b.getY()<TruantService.BOTTOM+1.6,"it drops, and whoever was aboard rides it down"))
            .thenExecute(()->{
                // Test players do not simulate their own gravity (a client does): the fall down the empty shaft, as vanilla would land it.
                h.assertTrue(a.getY()-b.getY()>TruantService.LANDING-.1,"the one at the sill was not carried");
                at(a,l,b,.5,TruantService.BOTTOM+1,-11.2,false);a.causeFallDamage(TruantService.LANDING,1F,l.damageSources().fall());})
            .thenWaitUntil(()->h.assertTrue(Truant.record(server,a.getUUID()).getString("Harm").equals("brake")&&a.getHealth()<a.getMaxHealth(),"they fall after it, are hurt, and it is kept against him"))
            .thenExecute(()->{
                // Once more, and this time the friend aboard throws the car's own brake.
                away(a,f);away(c,f);})
            .thenIdle(5)
            .thenExecute(()->{TruantService.buildLift(l,b);Truant.fresh();Truant.apart();TruantService.clearAll();Truant.force(Truant.Turn.BRAKE);
                at(c,l,b,-.5,TruantService.LANDING,-12.5,false);at(a,l,b,.5,TruantService.LANDING,-11.2,false);})
            .thenWaitUntil(()->h.assertTrue(TruantService.car(l,b)<=TruantService.TOP-2,"let go again"))
            .thenExecute(()->{
                int y=TruantService.car(l,b);var brake=b.offset(1,y+1,TruantService.SHAFT_Z1);
                h.assertTrue(l.getBlockState(brake).useWithoutItem(l,c,new BlockHitResult(brake.getCenter(),Direction.EAST,brake,false)).consumesAction(),"the friend throws the car's brake");})
            .thenIdle(10)
            .thenExecute(()->h.assertTrue(TruantService.car(l,b)>TruantService.BOTTOM,"and it holds: "+TruantService.car(l,b)))
            .thenSucceed();
    }
}

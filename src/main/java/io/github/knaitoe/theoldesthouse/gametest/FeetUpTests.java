package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.gametest.*;

/** 0.4.200, Feet Up: the children's game in native time, over the built furniture, with real gravity. */
@GameTestHolder(TheOldestHouse.MOD_ID+"_literary") @PrefixGameTestTemplate(false)
public final class FeetUpTests {
    private static final LabyrinthPlace P=LabyrinthPlace.FEET_UP;
    private static LiteraryTests.Fixture active;
    @AfterBatch(batch="feet_up_house") public static void houseDone(ServerLevel l){close();}
    @AfterBatch(batch="feet_up_count") public static void countDone(ServerLevel l){close();}
    @AfterBatch(batch="feet_up_chairs") public static void chairsDone(ServerLevel l){close();}
    @AfterBatch(batch="feet_up_boards") public static void boardsDone(ServerLevel l){close();}
    @AfterBatch(batch="feet_up_fan") public static void fanDone(ServerLevel l){close();}
    @AfterBatch(batch="feet_up_lift") public static void liftDone(ServerLevel l){close();}
    @AfterBatch(batch="feet_up_attic") public static void atticDone(ServerLevel l){close();}
    @AfterBatch(batch="feet_up_coop") public static void coopDone(ServerLevel l){close();}
    @AfterBatch(batch="feet_up_dark") public static void darkDone(ServerLevel l){close();}
    private static void close(){if(active!=null){active.close();active=null;}FeetUp.clearAll();}
    private static LiteraryTests.Fixture fixture(GameTestHelper h){var f=new LiteraryTests.Fixture(h);active=f;f.build(P);f.chunks.hold(f.in,FeetUpRooms.bounds(f.base(P)).inflate(4));return f;}
    /** A reader set down standing on a cell's top, under gravity, facing north. */
    private static ServerPlayer stand(LiteraryTests.Fixture f,String name,BlockPos rel){
        var p=f.player(name,P);var at=FeetUpRooms.standing(f.in,f.base(P),rel);
        p.setNoGravity(false);p.teleportTo(f.in,at.x,at.y,at.z,0,0);p.connection.resetPosition();p.setDeltaMovement(Vec3.ZERO);p.setOnGround(true);p.fallDistance=0;body(f.h,p);return p;
    }
    /**
     * A server player's movement comes from its client, and these have none. Each tick runs the real collision mover with
     * gravity and friction, as a client would, so a reader falls, lands, is knocked aside and stands on what is under them.
     */
    static void body(GameTestHelper h,ServerPlayer p){
        h.onEachTick(()->{if(p.isRemoved()||p.isSpectator())return;var v=p.getDeltaMovement();double y=p.onGround()&&v.y<=0?-.08:(v.y-.08)*.98;
            p.setDeltaMovement(v.x,y,v.z);p.move(net.minecraft.world.entity.MoverType.SELF,p.getDeltaMovement());
            var after=p.getDeltaMovement();double friction=p.onGround()?.546:.91;p.setDeltaMovement(after.x*friction,after.y,after.z*friction);});
    }
    private static void moveTo(LiteraryTests.Fixture f,ServerPlayer p,BlockPos rel){var at=FeetUpRooms.standing(f.in,f.base(P),rel);p.teleportTo(f.in,at.x,at.y,at.z,p.getYRot(),p.getXRot());p.connection.resetPosition();p.setDeltaMovement(Vec3.ZERO);p.setOnGround(true);p.fallDistance=0;}
    private static double top(ServerLevel l,BlockPos at){var s=l.getBlockState(at).getCollisionShape(l,at);return s.isEmpty()?at.getY():at.getY()+s.max(Direction.Axis.Y);}
    /** Every footing is something to stand on with room to stand, and every step is within a walking jump, measured from the built collision. */
    static void footings(GameTestHelper h,ServerLevel l,BlockPos b,List<List<BlockPos>> routes){
        for(var route:routes)for(int i=0;i<route.size();i++){
            var cell=b.offset(route.get(i));var shape=l.getBlockState(cell).getCollisionShape(l,cell);
            // The top of the shaft is a footing once the cage has climbed to it: a full block, like the cage at rest.
            boolean cageTop=route.get(i).equals(new BlockPos(FeetUpRooms.SHAFT_X,FeetUpRooms.CAGE_HIGH,FeetUpRooms.SHAFT_Z));
            h.assertTrue(cageTop?l.getBlockState(cell).isAir():!shape.isEmpty(),"a footing to stand on at "+route.get(i)+" ("+l.getBlockState(cell)+")");
            // Room to stand, or, right under the fan's housing, room to crouch: the sweep is passed low.
            double t=cageTop?cell.getY()+1:top(l,cell);boolean underFan=route.get(i).getX()==FeetUpRooms.FAN.getX()&&route.get(i).getZ()==FeetUpRooms.FAN.getZ();
            var room=new AABB(cell.getX()+.2,t+.05,cell.getZ()+.2,cell.getX()+.8,t+(underFan?1.5:1.85),cell.getZ()+.8);
            h.assertTrue(l.noCollision(null,room),"room to "+(underFan?"crouch":"stand")+" at "+route.get(i));
            if(i==0)continue;var prev=b.offset(route.get(i-1));double up=t-(route.get(i-1).equals(new BlockPos(FeetUpRooms.SHAFT_X,FeetUpRooms.CAGE_HIGH,FeetUpRooms.SHAFT_Z))?prev.getY()+1:top(l,prev));double across=Math.hypot(cell.getX()-prev.getX(),cell.getZ()-prev.getZ());
            boolean fromMattress=FeetUpBlock.is(l.getBlockState(prev),FeetUpBlock.Kind.MATTRESS),lift=route.get(i-1).getX()==FeetUpRooms.SHAFT_X&&route.get(i).getX()==FeetUpRooms.SHAFT_X;
            if(lift)continue;
            h.assertTrue(up<=(fromMattress?2.5:1.25)+1e-6,"no step up the reader cannot make: "+route.get(i-1)+" to "+route.get(i)+" rises "+up);
            h.assertTrue(across<=(up>.6?2.3:3.05)+1e-6,"no gap that needs a sprint: "+route.get(i-1)+" to "+route.get(i)+" is "+across+" across, "+up+" up");
        }
    }
    private static void chairsNear(ServerLevel l,BlockPos b){
        l.setBlock(b.offset(FeetUpRooms.HELP_A_FAR),Blocks.AIR.defaultBlockState(),3);l.setBlock(b.offset(FeetUpRooms.HELP_A_NEAR),FeetUpRooms.helperState(),3);
        l.setBlock(b.offset(FeetUpRooms.HELP_B_FAR),Blocks.AIR.defaultBlockState(),3);l.setBlock(b.offset(FeetUpRooms.HELP_B_NEAR),FeetUpRooms.chairHelperState(),3);
    }

    @GameTest(template="empty",batch="feet_up_house",timeoutTicks=400)
    public static void theHouseStandsAndEveryStepOfTheRouteIsAJumpWithoutASprint(GameTestHelper h){
        var f=fixture(h);var b=f.base(P);var l=f.in;
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the house is resident")).thenExecute(()->{
            h.assertTrue(LiteraryRooms.isLiterary(P)&&P.kind()==LabyrinthPlace.Kind.ONE_SHOT&&WitnessAccount.Story.of(P.id())==WitnessAccount.Story.FEET_UP&&WitnessAccount.Story.FEET_UP.kind.equals("survival"),"a literary survival source");
            h.assertTrue(LiteraryRooms.source(P).equals(FeetUpRooms.RULES)&&LiteraryRooms.ending(P).equals(FeetUpRooms.NOTEBOOK_AT),"the rules on the hall table are the source, the notebook under the lantern the account");
            h.assertTrue(FeetUpBlock.is(l.getBlockState(b.offset(FeetUpRooms.RULES)),FeetUpBlock.Kind.RULES_PAGE)&&FeetUpBlock.is(l.getBlockState(b.offset(FeetUpRooms.NOTEBOOK_AT)),FeetUpBlock.Kind.NOTEBOOK),"both lie where the rules say");
            var hatch=l.getBlockState(b.offset(FeetUpRooms.HATCH));h.assertTrue(hatch.is(Blocks.OAK_TRAPDOOR)&&!hatch.getValue(TrapDoorBlock.OPEN)&&l.getBlockState(b.offset(FeetUpRooms.HATCH.getX(),FeetUpRooms.LADDER_Y0,FeetUpRooms.HATCH.getZ())).isAir(),"the hatch is shut and its ladder is up");
            h.assertTrue(l.getBlockState(b.offset(FeetUpRooms.FIRE)).is(Blocks.CAMPFIRE)&&FeetUpBlock.is(l.getBlockState(b.offset(FeetUpRooms.STOVE_AT)),FeetUpBlock.Kind.STOVE),"the fire and the stove are the hot places");
            for(var c:FeetUpRooms.CHALK_MARKS)h.assertTrue(FeetUpBlock.is(l.getBlockState(b.offset(c.at())),FeetUpBlock.Kind.CHALK)&&l.getBlockState(b.offset(c.at())).getValue(FeetUpBlock.STAGE)==c.stage(),"chalk at "+c.at());
            var door=l.getBlockState(b.offset(FeetUpRooms.PARLOUR_DOOR));h.assertTrue(door.getBlock() instanceof DoorBlock&&!door.getValue(DoorBlock.OPEN),"the parlour door is shut on the voices");
            // Every footing is something to stand on with room to stand, and every step is within a walking jump.
            chairsNear(l,b);footings(h,l,b,List.of(FeetUpRooms.ROUTE,FeetUpRooms.LONG_WAY));
            // The helping chairs, where they start, leave a gap no walking jump crosses.
            h.assertTrue(Math.hypot(FeetUpRooms.HELP_A_FAR.getX()-(-9),FeetUpRooms.HELP_A_FAR.getZ()-(-1))>3.05,"the armchair starts too far from the mantel");
            // The account never reads the reader.
            var own=new net.minecraft.nbt.CompoundTag();own.putBoolean("Read_Source",true);own.putInt(FeetUp.RUN_TOUCHES,2);own.putInt(FeetUp.ATTEMPTS,1);own.putBoolean(FeetUp.TOOK_RAIL,true);own.putBoolean(FeetUp.OPENED_HATCH,true);own.putBoolean(FeetUp.PULLED_UP,true);var account=FeetUpTexts.account(own);
            h.assertTrue(account.startsWith("The rules were on the hall table")&&account.contains("The floor had me twice")&&account.contains("along the rail")&&account.contains("whispered where I had gone wrong")&&account.contains("The ladder was up.")&&account.endsWith("the way the rules said."),"the account follows the run: "+account);
            for(String area:List.of("front","hall","dining","kitchen","landing","rail","long_way","boards",""))h.assertTrue(FeetUpTexts.hint(area).endsWith("."),"a whisper for every part of the house: "+area);
            var words=new HashSet<>(Arrays.asList(account.toLowerCase(Locale.ROOT).split("[^a-z]+")));
            for(String word:List.of("boy","girl","young","old","white","black","brown","red","blue","green"))h.assertTrue(!words.contains(word),"the account never reads the reader or names a colour: "+word);
            try{ArchitectureTests.export(l,b,P);}catch(Exception e){throw new IllegalStateException(e);}
            h.succeed();
        });
    }

    @GameTest(template="empty",batch="feet_up_count",timeoutTicks=600)
    public static void theGameIsOnFromTheBigChairAFootOnTheFloorCountsAndThreeAreOut(GameTestHelper h){
        var f=fixture(h);var b=f.base(P);var l=f.in;ServerPlayer[] p={null};
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the house is resident"))
        .thenExecute(()->{p[0]=f.player("feet_up_reader",P);p[0].setNoGravity(false);f.at(p[0],P,.5,0,-1.5);p[0].setOnGround(true);body(h,p[0]);})
        .thenIdle(10).thenExecute(()->{var own=f.own(p[0],P);h.assertTrue(!own.getBoolean(FeetUp.STARTED)&&own.getInt(FeetUp.RUN_TOUCHES)==0,"before the big chair the hall floor is only a floor");moveTo(f,p[0],FeetUpRooms.BIG_CHAIR);})
        .thenWaitUntil(()->h.assertTrue(f.own(p[0],P).getBoolean(FeetUp.STARTED),"on the big chair the game is on"))
        .thenExecute(()->{moveTo(f,p[0],new BlockPos(-5,-1,-5));p[0].setOnGround(true);})
        .thenWaitUntil(()->h.assertTrue(f.own(p[0],P).getInt(FeetUp.RUN_TOUCHES)==1,"a foot on the floor counts once"))
        .thenWaitUntil(()->h.assertTrue(f.own(p[0],P).getBoolean(FeetUp.MOVED)&&p[0].position().distanceTo(FeetUpRooms.standing(l,b,FeetUpRooms.BIG_CHAIR))<.5,"a beat later the reader is back on their base"))
        .thenWaitUntil(()->h.assertTrue(f.own(p[0],P).getInt(FeetUp.BEAT)==0,"and the beat is over"))
        .thenExecute(()->{moveTo(f,p[0],new BlockPos(-5,-1,-5));})
        .thenWaitUntil(()->h.assertTrue(f.own(p[0],P).getInt(FeetUp.RUN_TOUCHES)==2&&f.own(p[0],P).getInt(FeetUp.BEAT)==0,"two"))
        .thenExecute(()->{moveTo(f,p[0],new BlockPos(-5,-1,-5));})
        .thenWaitUntil(()->h.assertTrue(f.own(p[0],P).getInt(FeetUp.OUT_AT)>0,"three, and the reader is out"))
        .thenExecute(()->{h.assertTrue(l.getBlockState(b.offset(FeetUpRooms.PARLOUR_DOOR)).getValue(DoorBlock.OPEN),"the parlour door is thrown open");
            h.assertTrue(f.own(p[0],P).getString(FeetUp.HINT).equals("front"),"the whisper will be about where the reader fell from: "+f.own(p[0],P).getString(FeetUp.HINT));
            var lamp=l.getBlockState(b.offset(-2,1,-1));h.assertTrue(FeetUpBlock.is(lamp,FeetUpBlock.Kind.LAMP)&&lamp.getValue(FeetUpBlock.STAGE)==1,"the lamps go down");})
        .thenWaitUntil(()->h.assertTrue(f.own(p[0],P).getInt(FeetUp.OUT_AT)==0&&!f.own(p[0],P).getBoolean(FeetUp.STARTED),"then the attempt is over"))
        .thenExecute(()->{var own=f.own(p[0],P);h.assertTrue(own.getInt(FeetUp.ATTEMPTS)==1&&own.getInt(FeetUp.RUN_TOUCHES)==0&&p[0].position().distanceTo(new Vec3(b.getX()+.5,b.getY(),b.getZ()-.5))<.6,"at the front door with the count at nothing");
            h.assertTrue(!l.getBlockState(b.offset(FeetUpRooms.PARLOUR_DOOR)).getValue(DoorBlock.OPEN)&&l.getBlockState(b.offset(-2,1,-1)).getValue(FeetUpBlock.STAGE)==0,"the door shuts and the lamps come back");h.succeed();});
    }

    @GameTest(template="empty",batch="feet_up_chairs",timeoutTicks=400)
    public static void theChairsComeOnlyWhileNoCameraSeesThem(GameTestHelper h){
        var f=fixture(h);var b=f.base(P);var l=f.in;ServerPlayer[] p={null};
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the house is resident"))
        .thenExecute(()->{p[0]=stand(f,"feet_up_looker",new BlockPos(-9,2,-1));f.look(p[0],b.offset(FeetUpRooms.HELP_A_FAR));})
        .thenIdle(FeetUp.HELP_UNSEEN*2).thenExecute(()->{h.assertTrue(l.getBlockState(b.offset(FeetUpRooms.HELP_A_FAR)).equals(FeetUpRooms.helperState())&&l.getBlockState(b.offset(FeetUpRooms.HELP_A_NEAR)).isAir(),"watched, the armchair stays where it is");
            // Then the reader turns to the chimney breast and looks up at the ceiling over the mantel: the chair is behind them.
            p[0].setYRot(90);p[0].setYHeadRot(90);p[0].setXRot(-60);})
        .thenWaitUntil(()->h.assertTrue(l.getBlockState(b.offset(FeetUpRooms.HELP_A_NEAR)).equals(FeetUpRooms.helperState())&&l.getBlockState(b.offset(FeetUpRooms.HELP_A_FAR)).isAir(),"unseen, it comes a cell closer and stops"))
        .thenExecute(()->{h.assertTrue(FeetUp.seen(l,Vec3.atCenterOf(b.offset(-9,5,-1))),"the ceiling the reader looks up at is seen");
            // Gone, the house is set for the next game: the chair back where it started.
            p[0].teleportTo(f.in,f.origin.getX()+.5,f.origin.getY()+1,f.origin.getZ()+.5,0,0);})
        .thenWaitUntil(()->h.assertTrue(l.getBlockState(b.offset(FeetUpRooms.HELP_A_FAR)).equals(FeetUpRooms.helperState())&&l.getBlockState(b.offset(FeetUpRooms.HELP_A_NEAR)).isAir(),"an empty house puts the chair back"))
        .thenExecute(h::succeed);
    }

    @GameTest(template="empty",batch="feet_up_boards",timeoutTicks=600)
    public static void aLooseBoardGoesUnderAReaderLiesBelowAndComesBackOnlyUnseen(GameTestHelper h){
        var f=fixture(h);var b=f.base(P);var l=f.in;ServerPlayer[] p={null,null};var board=b.offset(0,5,-6);var debris=b.offset(0,0,-6);
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the house is resident"))
        .thenExecute(()->{h.assertTrue(FeetUpBlock.is(l.getBlockState(board),FeetUpBlock.Kind.LOOSE_BOARD),"the upstairs hall is loose boards");
            // A friend on the press watches the board the whole time.
            p[1]=stand(f,"feet_up_watcher",FeetUpRooms.PRESS_TOP);f.look(p[1],board);
            p[0]=stand(f,"feet_up_boarder",new BlockPos(0,5,-6));})
        .thenWaitUntil(()->h.assertTrue(l.getBlockState(board).isAir(),"half a second under a reader and the board goes"))
        .thenWaitUntil(()->h.assertTrue(l.getEntitiesOfClass(FallingBlockEntity.class,FeetUpRooms.bounds(b)).isEmpty()&&FeetUpBlock.is(l.getBlockState(debris),FeetUpBlock.Kind.LOOSE_BOARD),"it lies on the hall floor below"))
        .thenExecute(()->{h.assertTrue(FeetUpRooms.floor(l,b,debris)&&!FeetUpRooms.floor(l,b,b.offset(1,5,-6))&&l.getBlockState(debris).getValue(FeetUpBlock.STAGE)==1,"debris downstairs lies flat and is floor; a board upstairs is not");
            h.assertTrue(p[0].getY()<b.getY()+5,"the reader came down with it");})
        // The fall counted, and the reader is back on their base.
        .thenWaitUntil(()->h.assertTrue(f.own(p[0],P).getInt(FeetUp.RUN_TOUCHES)==1&&f.own(p[0],P).getBoolean(FeetUp.FELL_BOARDS)&&f.own(p[0],P).getInt(FeetUp.BEAT)==0,"coming down counts, and the beat is over"))
        .thenIdle(20).thenExecute(()->{h.assertTrue(l.getBlockState(board).isAir(),"while the friend on the press looks at the gap it stays");
            // The friend goes; nobody sees the gap or the board below.
            p[1].teleportTo(f.in,f.origin.getX()+.5,f.origin.getY()+1,f.origin.getZ()+.5,0,0);})
        .thenWaitUntil(()->h.assertTrue(FeetUpBlock.is(l.getBlockState(board),FeetUpBlock.Kind.LOOSE_BOARD)&&l.getBlockState(board).getValue(FeetUpBlock.STAGE)==0&&!FeetUpBlock.is(l.getBlockState(debris),FeetUpBlock.Kind.LOOSE_BOARD),"unseen, the board comes back and the debris goes"))
        .thenExecute(h::succeed);
    }

    @GameTest(template="empty",batch="feet_up_fan",timeoutTicks=300)
    public static void theFanKnocksAStandingReaderOffTheTableAndSparesOneWhoKeepsLow(GameTestHelper h){
        var f=fixture(h);var b=f.base(P);var l=f.in;ServerPlayer[] p={null,null};
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the house is resident"))
        .thenExecute(()->{p[0]=stand(f,"feet_up_tall",new BlockPos(8,0,-4));p[1]=stand(f,"feet_up_low",new BlockPos(6,0,-4));p[1].setShiftKeyDown(true);p[1].setPose(net.minecraft.world.entity.Pose.CROUCHING);})
        .thenWaitUntil(()->h.assertTrue(Math.hypot(p[0].getX()-(b.getX()+8.5),p[0].getZ()-(b.getZ()-3.5))>.3||p[0].getY()<b.getY()+.8,"within a turn of the blades the standing reader is knocked off"))
        .thenExecute(()->{h.assertTrue(Math.hypot(p[1].getX()-(b.getX()+6.5),p[1].getZ()-(b.getZ()-3.5))<.2,"the one who keeps low is under them");h.succeed();});
    }

    @GameTest(template="empty",batch="feet_up_lift",timeoutTicks=400)
    public static void theDumbwaiterClimbsWhenItsRopeIsPulledAndCarriesWhoeverStandsInIt(GameTestHelper h){
        var f=fixture(h);var b=f.base(P);var l=f.in;ServerPlayer[] p={null};var low=new BlockPos(FeetUpRooms.SHAFT_X,FeetUpRooms.CAGE_LOW,FeetUpRooms.SHAFT_Z);var high=new BlockPos(FeetUpRooms.SHAFT_X,FeetUpRooms.CAGE_HIGH,FeetUpRooms.SHAFT_Z);
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the house is resident"))
        .thenExecute(()->{h.assertTrue(FeetUpBlock.is(l.getBlockState(b.offset(low)),FeetUpBlock.Kind.CAGE),"the cage rests at the bottom of its travel");p[0]=stand(f,"feet_up_rider",low);
            h.assertTrue(FeetUp.click(p[0],b,low,f.own(p[0],P)),"a hand on the cage pulls the rope");})
        .thenWaitUntil(()->h.assertTrue(FeetUpBlock.is(l.getBlockState(b.offset(high)),FeetUpBlock.Kind.CAGE)&&l.getBlockState(b.offset(low)).isAir(),"it climbs to the landing"))
        .thenExecute(()->{h.assertTrue(Math.abs(p[0].getY()-(b.getY()+FeetUpRooms.CAGE_HIGH+1))<.2,"with its rider: "+(p[0].getY()-b.getY()));moveTo(f,p[0],new BlockPos(2,6,-18));})
        .thenWaitUntil(()->h.assertTrue(FeetUpBlock.is(l.getBlockState(b.offset(low)),FeetUpBlock.Kind.CAGE),"left empty, it comes back down"))
        .thenExecute(h::succeed);
    }

    @GameTest(template="empty",batch="feet_up_attic",timeoutTicks=600)
    public static void theHatchOpensOnlyFromThePressAndTheAtticEndsTheGameWithTheAccount(GameTestHelper h){
        var f=fixture(h);var b=f.base(P);var l=f.in;ServerPlayer[] p={null,null};
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the house is resident"))
        .thenExecute(()->{p[0]=stand(f,"feet_up_climber",FeetUpRooms.PRESS_TOP);f.source(p[0],P);p[0].closeContainer();moveTo(f,p[0],FeetUpRooms.PRESS_TOP);p[1]=stand(f,"feet_up_short",new BlockPos(2,5,-2));
            h.assertTrue(FeetUp.click(p[1],b,FeetUpRooms.HATCH,f.own(p[1],P))&&!l.getBlockState(b.offset(FeetUpRooms.HATCH)).getValue(TrapDoorBlock.OPEN),"from the boards the catch is out of reach");
            boolean gave=FeetUp.click(p[0],b,FeetUpRooms.HATCH,f.own(p[0],P));
            h.assertTrue(gave&&l.getBlockState(b.offset(FeetUpRooms.HATCH)).getValue(TrapDoorBlock.OPEN),"from the press it gives: handled "+gave+", eye "+(p[0].getEyeY()-b.getY())+", feet "+(p[0].getY()-b.getY())+", "+l.getBlockState(b.offset(FeetUpRooms.HATCH)));
            for(int y=FeetUpRooms.LADDER_Y0;y<=FeetUpRooms.LADDER_Y1;y++)h.assertTrue(l.getBlockState(b.offset(FeetUpRooms.HATCH.getX(),y,FeetUpRooms.HATCH.getZ())).is(Blocks.LADDER),"and the ladder comes down the wall");
            // The one who climbs: the game is on for them, a touch counted, then the attic.
            var own=f.own(p[0],P);own.putBoolean(FeetUp.STARTED,true);own.putInt(FeetUp.BASE,4);own.putInt(FeetUp.RUN_TOUCHES,1);LiteraryVignettes.save(f.data(),p[0].getUUID(),P,own);
            moveTo(f,p[0],new BlockPos(1,10,-3));})
        .thenWaitUntil(()->h.assertTrue(f.own(p[0],P).getBoolean(FeetUp.FINISHED)&&f.own(p[0],P).getBoolean("Ready"),"the attic ends the game and the account is ready"))
        .thenExecute(()->{var own=f.own(p[0],P);h.assertTrue(own.getInt(FeetUp.RUNS)==1&&own.getInt(FeetUp.BEST)==1&&!own.getBoolean(FeetUp.CLEAN),"the tally keeps the run: one touch, not clean");
            h.assertTrue(!WitnessAccount.has(f.data(),p[0].getUUID(),WitnessAccount.Story.FEET_UP),"nothing is resolved until the account is read");
            f.read(p[0],P,LiteraryRooms.ending(P));})
        .thenWaitUntil(()->h.assertTrue(WitnessAccount.has(f.data(),p[0].getUUID(),WitnessAccount.Story.FEET_UP)&&f.own(p[0],P).getBoolean("Completed"),"reading the account resolves it for this reader"))
        .thenExecute(()->{p[0].closeContainer();h.assertTrue(!WitnessAccount.has(f.data(),p[1].getUUID(),WitnessAccount.Story.FEET_UP)&&!f.own(p[1],P).getBoolean(FeetUp.FINISHED),"the other reader's game is their own");
            // Both gone: the hatch is shut and the ladder up for the next game.
            for(var q:p)q.teleportTo(f.in,f.origin.getX()+.5,f.origin.getY()+1,f.origin.getZ()+.5,0,0);})
        .thenWaitUntil(()->h.assertTrue(!l.getBlockState(b.offset(FeetUpRooms.HATCH)).getValue(TrapDoorBlock.OPEN)&&l.getBlockState(b.offset(FeetUpRooms.HATCH.getX(),FeetUpRooms.LADDER_Y0,FeetUpRooms.HATCH.getZ())).isAir(),"an empty house pulls the ladder up"))
        .thenExecute(h::succeed);
    }

    /** 0.4.200: up first with a friend still below, the ladder is left down; the last one up pulls it up. Each account says which. */
    @GameTest(template="empty",batch="feet_up_coop",timeoutTicks=600)
    public static void theFirstUpLeavesTheLadderDownForWhoeverIsStillBelowAndTheLastPullsItUp(GameTestHelper h){
        var f=fixture(h);var b=f.base(P);var l=f.in;ServerPlayer[] p={null,null};
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the house is resident"))
        .thenExecute(()->{p[0]=stand(f,"feet_up_first",FeetUpRooms.PRESS_TOP);p[1]=stand(f,"feet_up_second",FeetUpRooms.SIDEBOARD);
            for(var q:p){var own=f.own(q,P);own.putBoolean(FeetUp.STARTED,true);own.putInt(FeetUp.BASE,q==p[0]?4:1);LiteraryVignettes.save(f.data(),q.getUUID(),P,own);}
            var own=f.own(p[0],P);h.assertTrue(FeetUp.click(p[0],b,FeetUpRooms.HATCH,own)&&l.getBlockState(b.offset(FeetUpRooms.HATCH)).getValue(TrapDoorBlock.OPEN),"the first reaches the catch from the press");
            LiteraryVignettes.save(f.data(),p[0].getUUID(),P,own);moveTo(f,p[0],new BlockPos(1,10,-20));})
        .thenWaitUntil(()->h.assertTrue(f.own(p[0],P).getBoolean(FeetUp.FINISHED),"the first is up"))
        .thenExecute(()->{var own=f.own(p[0],P);var text=FeetUpTexts.account(own);
            h.assertTrue(own.getBoolean(FeetUp.LEFT_DOWN)&&!own.getBoolean(FeetUp.PULLED_UP)&&l.getBlockState(b.offset(FeetUpRooms.HATCH)).getValue(TrapDoorBlock.OPEN),"with a friend still below, the ladder is left down");
            h.assertTrue(text.contains("The ladder was up.")&&text.contains("so I left the ladder down"),"and the first account says so: "+text);
            h.assertTrue(!f.own(p[1],P).getBoolean(FeetUp.FINISHED)&&f.own(p[1],P).getBoolean(FeetUp.STARTED),"the friend's game goes on");
            moveTo(f,p[1],new BlockPos(5,10,-20));})
        .thenWaitUntil(()->h.assertTrue(f.own(p[1],P).getBoolean(FeetUp.FINISHED),"the second is up"))
        .thenExecute(()->{var own=f.own(p[1],P);var text=FeetUpTexts.account(own);
            h.assertTrue(own.getBoolean(FeetUp.PULLED_UP)&&!own.getBoolean(FeetUp.LEFT_DOWN)&&!l.getBlockState(b.offset(FeetUpRooms.HATCH)).getValue(TrapDoorBlock.OPEN)
                &&l.getBlockState(b.offset(FeetUpRooms.HATCH.getX(),FeetUpRooms.LADDER_Y0,FeetUpRooms.HATCH.getZ())).isAir(),"the last one up pulls the ladder up");
            h.assertTrue(text.contains("already down when I got there")&&text.endsWith("the way the rules said."),"and the second account follows: "+text);
            // Each comes through for themselves, and their accounts close on who was ahead of them and who was still below.
            var together=SharedVignettes.together(P);h.assertTrue(together!=null&&SharedVignettes.drafted(P)==null&&SharedVignettes.followable(P),"friends go in together, and each keeps off the floor for themselves");
            h.assertTrue(together.coda("reached_the_attic",List.of("feet_up_first"),List.of()).startsWith("feet_up_first had reached the attic before me")
                &&together.coda("reached_the_attic",List.of(),List.of("feet_up_second")).equals("feet_up_second was still on the furniture below when I reached the attic, so I left the ladder down."),"the closing lines name the party");
            // Finished, the house is a passage: the floor counts nothing for them.
            moveTo(f,p[1],new BlockPos(-5,-1,-5));})
        .thenIdle(10).thenExecute(()->{h.assertTrue(f.own(p[1],P).getInt(FeetUp.RUN_TOUCHES)==0&&f.own(p[1],P).getInt(FeetUp.OUT_AT)==0,"a finished reader walks the floor uncounted");h.succeed();});
    }

    /** 0.4.200: a friend put out darkens the house for everyone, but only while they are out, and their count is their own. */
    @GameTest(template="empty",batch="feet_up_dark",timeoutTicks=600)
    public static void aFriendPutOutDarkensTheHouseForEveryoneOnlyWhileTheyAreOut(GameTestHelper h){
        var f=fixture(h);var b=f.base(P);var l=f.in;ServerPlayer[] p={null,null};var lamp=b.offset(-2,1,-1);
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the house is resident"))
        .thenExecute(()->{p[0]=stand(f,"feet_up_falling",FeetUpRooms.BIG_CHAIR);p[1]=stand(f,"feet_up_watching",FeetUpRooms.SIDEBOARD);
            var own=f.own(p[0],P);own.putBoolean(FeetUp.STARTED,true);own.putInt(FeetUp.RUN_TOUCHES,2);LiteraryVignettes.save(f.data(),p[0].getUUID(),P,own);
            var other=f.own(p[1],P);other.putBoolean(FeetUp.STARTED,true);other.putInt(FeetUp.BASE,1);LiteraryVignettes.save(f.data(),p[1].getUUID(),P,other);})
        .thenIdle(5).thenExecute(()->moveTo(f,p[0],new BlockPos(-5,-1,-5)))
        .thenWaitUntil(()->h.assertTrue(f.own(p[0],P).getInt(FeetUp.OUT_AT)>0,"the third touch puts the one reader out"))
        .thenExecute(()->{h.assertTrue(l.getBlockState(lamp).getValue(FeetUpBlock.STAGE)==1&&l.getBlockState(b.offset(FeetUpRooms.PARLOUR_DOOR)).getValue(DoorBlock.OPEN),"the lamps go down and the door opens for everyone in the house");
            h.assertTrue(f.own(p[1],P).getInt(FeetUp.RUN_TOUCHES)==0&&f.own(p[1],P).getBoolean(FeetUp.STARTED),"the friend's count is untouched");
            // The one who was put out walks away before the footsteps arrive.
            p[0].teleportTo(f.in,f.origin.getX()+.5,f.origin.getY()+1,f.origin.getZ()+.5,0,0);})
        .thenWaitUntil(()->h.assertTrue(l.getBlockState(lamp).getValue(FeetUpBlock.STAGE)==0&&!l.getBlockState(b.offset(FeetUpRooms.PARLOUR_DOOR)).getValue(DoorBlock.OPEN),"with nobody out in it, the house is lit again for the one still playing"))
        .thenExecute(()->{h.assertTrue(f.own(p[1],P).getBoolean(FeetUp.STARTED)&&f.own(p[1],P).getInt(FeetUp.BASE)==1,"and that game goes on where it was");h.succeed();});
    }

    // ------------------------------------------------------------------------------------------------ 0.4.383
    private static final LabyrinthPlace L=LabyrinthPlace.FEET_UP_LATER;
    @AfterBatch(batch="feet_up_later_house") public static void laterHouseDone(ServerLevel l){close();}
    @AfterBatch(batch="feet_up_later_dark") public static void laterDarkDone(ServerLevel l){close();}
    @AfterBatch(batch="feet_up_later_attic") public static void laterAtticDone(ServerLevel l){close();}
    @AfterBatch(batch="feet_up_chalk") public static void chalkDone(ServerLevel l){close();}
    /** The house later, built as the labyrinth builds it (a gray room, not through the literary rooms), its doors registered. */
    private static LiteraryTests.Fixture later(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);active=f;var b=f.base(L);FeetUpRooms.buildLaterPlace(f.in,b);LabyrinthBuilder.registerDoors(f.data(),L,b);f.built.add(L);
        f.chunks.hold(f.in,FeetUpRooms.bounds(b,L).inflate(4));return f;
    }
    private static ServerPlayer standLater(LiteraryTests.Fixture f,String name,BlockPos rel){
        var p=f.player(name,L);FeetUp.onArrive(p,L);var at=FeetUpRooms.standing(f.in,f.base(L),rel);
        p.setNoGravity(false);p.teleportTo(f.in,at.x,at.y,at.z,0,0);p.connection.resetPosition();p.setDeltaMovement(Vec3.ZERO);p.setOnGround(true);p.fallDistance=0;body(f.h,p);return p;
    }
    private static void moveLater(LiteraryTests.Fixture f,ServerPlayer p,BlockPos rel){var at=FeetUpRooms.standing(f.in,f.base(L),rel);p.teleportTo(f.in,at.x,at.y,at.z,p.getYRot(),p.getXRot());p.connection.resetPosition();p.setDeltaMovement(Vec3.ZERO);p.fallDistance=0;}

    /** 0.4.383: the same house later, every floor gone over the dark, every footing where it was, and nobody's but its own. */
    @GameTest(template="empty",batch="feet_up_later_house",timeoutTicks=400)
    public static void theHouseLaterHasNoFloorsAndTheSameWayUp(GameTestHelper h){
        var f=later(h);var b=f.base(L);var l=f.in;
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the house is resident")).thenExecute(()->{
            var q=PuzzleRooms.Puzzle.of(L);
            h.assertTrue(q!=null&&q.deep&&q.family==PuzzleRooms.Family.FEET&&q.builtOn().equals(List.of(P))&&L.kind()==LabyrinthPlace.Kind.GRAY&&L.authored()&&WitnessAccount.Story.of(L.id())==null,"a deep puzzle room built on the first house, and no story");
            var far=L.doors().stream().filter(d->d.name().equals("far")).findFirst().orElseThrow();
            h.assertTrue(f.data().door(L.doorId(far))!=null&&far.rel().equals(new BlockPos(0,FeetUpRooms.ATTIC,FeetUpRooms.Z0-1)),"the way on is at the attic's gable");
            // No floor anywhere but the doormat inside the front door: the ground floor, the stair and the first floor's boards are gone.
            int dark=0;
            for(int x=FeetUpRooms.X0;x<=FeetUpRooms.X1;x++)for(int z=FeetUpRooms.Z0;z<=0;z++)for(int y=FeetUpRooms.PIT;y<=9;y++){
                var at=new BlockPos(x,y,z);var s=l.getBlockState(b.offset(at));
                if(s.is(FeetUpRegistry.material("under"))){dark++;continue;}
                if(s.is(Blocks.DARK_OAK_STAIRS))h.fail("a stair tread is left at "+at);
                if(s.is(FeetUpRegistry.material("hall_tile"))&&z<-1)h.fail("a hall tile is left past the doormat at "+at);
                if(s.is(FeetUpRegistry.material("floor"))&&l.getBlockState(b.offset(at.above())).isAir())h.fail("a floorboard is left under a room at "+at);
                if(y==5&&FeetUpBlock.is(s,FeetUpBlock.Kind.LOOSE_BOARD)&&!FeetUpRooms.ROUTE.contains(at)&&!FeetUpRooms.LONG_WAY.contains(at))h.fail("a loose board off the way up at "+at);
                if(y==5&&s.is(FeetUpRegistry.material("board"))&&l.getBlockState(b.offset(at.above())).isAir())h.fail("a board that holds is left under a room at "+at);
            }
            h.assertTrue(dark>300,"the dark lies under every room: "+dark);
            h.assertTrue(l.getBlockState(b.offset(0,-1,-1)).is(FeetUpRegistry.material("hall_tile"))&&l.getBlockState(b.offset(0,-1,0)).is(FeetUpRegistry.material("hall_tile")),"the doormat is still inside the door");
            // Nothing is lit; the fire is cold; the parlour stands open; the jar is empty.
            for(var at:BlockPos.betweenClosed(b.offset(FeetUpRooms.X0,0,FeetUpRooms.Z0),b.offset(FeetUpRooms.X1,FeetUpRooms.ROOF-1,0))){var s=l.getBlockState(at);
                if(s.is(FeetUpRegistry.FITTING.get())){var k=s.getValue(FeetUpBlock.KIND);int st=s.getValue(FeetUpBlock.STAGE);
                    h.assertTrue(!((k==FeetUpBlock.Kind.LAMP||k==FeetUpBlock.Kind.CEILING_LIGHT)&&st==0)&&!(k==FeetUpBlock.Kind.FLOOR_LAMP&&st==1),"a lamp is lit in the house later at "+at.subtract(b));}}
            var fire=l.getBlockState(b.offset(FeetUpRooms.FIRE));h.assertTrue(fire.is(Blocks.CAMPFIRE)&&!fire.getValue(CampfireBlock.LIT),"the fire is cold");
            h.assertTrue(l.getBlockState(b.offset(FeetUpRooms.PARLOUR_DOOR)).getValue(DoorBlock.OPEN),"the parlour stands open");
            var jar=l.getBlockState(b.offset(FeetUpRooms.CHALK_JAR_AT));h.assertTrue(FeetUpBlock.is(jar,FeetUpBlock.Kind.CHALK_JAR)&&jar.getValue(FeetUpBlock.STAGE)==1,"the jar in the attic is empty");
            // Every footing hangs where it stood, and the way up is the same: from the doormat, the stool and the table to the big chair, then on.
            chairsNear(l,b);footings(h,l,b,List.of(FeetUpRooms.LATER_START,FeetUpRooms.ROUTE,FeetUpRooms.LONG_WAY));
            // Offered only to a reader who reached the first house's attic, once someone has, deep in the House.
            var reader=UUID.randomUUID();var d=f.data();int deep=LabyrinthPacing.STRANGE_DEPTH+4;
            WitnessAccount.resolve(d,reader,WitnessAccount.Story.CHILD_ROOM,"test");PuzzleRooms.markSolved(d,P);
            h.assertTrue(PuzzleRooms.weight(d,reader,L,deep)==0,"never to a reader who has not been up the first house");
            var own=LiteraryVignettes.personal(d,reader,P);own.putBoolean(FeetUp.FINISHED,true);LiteraryVignettes.save(d,reader,P,own);
            h.assertTrue(PuzzleRooms.weight(d,reader,L,deep)>0&&PuzzleRooms.weight(d,reader,L,LabyrinthPacing.STRANGE_DEPTH-1)==0,"to one who has, and only deep");
            try{ArchitectureTests.export(l,b,L);}catch(Exception e){throw new IllegalStateException(e);}
            h.succeed();
        });
    }

    /** 0.4.383: in the house later a fall into the dark counts, nobody whispers it, nothing hurts, and three are out at the doormat. */
    @GameTest(template="empty",batch="feet_up_later_dark",timeoutTicks=900)
    public static void aFallIntoTheDarkCountsAndThreeAreOutAtTheDoormat(GameTestHelper h){
        var f=later(h);var b=f.base(L);var l=f.in;ServerPlayer[] p={null,null};var over=new BlockPos(-6,-1,-6);
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the house is resident"))
        .thenExecute(()->{p[0]=standLater(f,"later_reader",FeetUpRooms.BIG_CHAIR);
            // A second reader who never starts: off the doormat into the dark, and let go at the front door without a count.
            p[1]=standLater(f,"later_wanderer",new BlockPos(0,-1,0));})
        .thenWaitUntil(()->h.assertTrue(f.own(p[0],L).getBoolean(FeetUp.STARTED),"on the big chair the game is on"))
        .thenExecute(()->{h.assertTrue(!f.own(p[1],L).getBoolean(FeetUp.STARTED),"the doormat is not a start: at "+p[1].position().subtract(Vec3.atLowerCornerOf(b))+" on "+p[1].getOnPos().subtract(b)+" "+l.getBlockState(p[1].getOnPos()));moveLater(f,p[1],new BlockPos(0,-1,-3));moveLater(f,p[0],over);})
        .thenWaitUntil(()->h.assertTrue(f.own(p[0],L).getInt(FeetUp.RUN_TOUCHES)==1,"a fall into the dark counts once"))
        .thenWaitUntil(()->h.assertTrue(f.own(p[0],L).getBoolean(FeetUp.MOVED)&&p[0].position().distanceTo(FeetUpRooms.standing(l,b,FeetUpRooms.BIG_CHAIR))<.5,"and a beat later the reader is back on the big chair"))
        .thenExecute(()->{h.assertTrue(p[0].getHealth()>=p[0].getMaxHealth(),"the dark hurts nobody: "+p[0].getHealth());
            h.assertTrue(f.own(p[1],L).getInt(FeetUp.RUN_TOUCHES)==0&&!f.own(p[1],L).getBoolean(FeetUp.STARTED)&&p[1].position().distanceTo(new Vec3(b.getX()+.5,b.getY(),b.getZ()-.5))<.6&&p[1].getHealth()>=p[1].getMaxHealth(),"the one who never started is let go at the doormat, uncounted");})
        .thenWaitUntil(()->h.assertTrue(f.own(p[0],L).getInt(FeetUp.BEAT)==0,"the beat is over"))
        .thenExecute(()->moveLater(f,p[0],over))
        .thenWaitUntil(()->h.assertTrue(f.own(p[0],L).getInt(FeetUp.RUN_TOUCHES)==2&&f.own(p[0],L).getInt(FeetUp.BEAT)==0,"two"))
        .thenExecute(()->moveLater(f,p[0],over))
        .thenWaitUntil(()->h.assertTrue(f.own(p[0],L).getInt(FeetUp.OUT_AT)>0,"three, and out"))
        .thenExecute(()->h.assertTrue(f.own(p[0],L).getString(FeetUp.HINT).equals("front")&&FeetUpTexts.hintLater("hall").contains("stool"),"what the reader remembers is about where they fell from"))
        .thenWaitUntil(()->h.assertTrue(f.own(p[0],L).getInt(FeetUp.OUT_AT)==0&&!f.own(p[0],L).getBoolean(FeetUp.STARTED),"then the attempt is over"))
        .thenExecute(()->{var own=f.own(p[0],L);h.assertTrue(own.getInt(FeetUp.ATTEMPTS)==1&&own.getInt(FeetUp.RUN_TOUCHES)==0&&p[0].position().distanceTo(new Vec3(b.getX()+.5,b.getY(),b.getZ()-.5))<.6,"at the doormat with the count at nothing");
            h.assertTrue(l.getBlockState(b.offset(FeetUpRooms.PARLOUR_DOOR)).getValue(DoorBlock.OPEN)&&l.getBlockState(b.offset(-2,1,-1)).getValue(FeetUpBlock.STAGE)==1,"the house later keeps its door open and its lamps out");
            h.assertTrue(f.own(p[0],P).getInt(FeetUp.RUN_TOUCHES)==0&&f.own(p[0],P).getInt(FeetUp.ATTEMPTS)==0,"the first house's count is its own");h.succeed();});
    }

    /** 0.4.383: the house later's attic solves it for good, says what the tally says, and touches nothing of the first house's story. */
    @GameTest(template="empty",batch="feet_up_later_attic",timeoutTicks=400)
    public static void theHouseLatersAtticSolvesItAndResolvesNoStory(GameTestHelper h){
        var f=later(h);var b=f.base(L);var l=f.in;ServerPlayer[] p={null};
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the house is resident"))
        .thenExecute(()->{p[0]=standLater(f,"later_climber",FeetUpRooms.PRESS_TOP);
            h.assertTrue(FeetUp.click(p[0],b,FeetUpRooms.HATCH,f.own(p[0],L))&&l.getBlockState(b.offset(FeetUpRooms.HATCH)).getValue(TrapDoorBlock.OPEN),"the hatch opens from the press, as before");
            var own=f.own(p[0],L);own.putBoolean(FeetUp.STARTED,true);own.putInt(FeetUp.BASE,4);LiteraryVignettes.save(f.data(),p[0].getUUID(),L,own);
            h.assertTrue(!PuzzleRooms.solved(f.data(),L),"unsolved before anyone is up");moveLater(f,p[0],new BlockPos(1,10,-3));})
        .thenWaitUntil(()->h.assertTrue(f.own(p[0],L).getBoolean(FeetUp.FINISHED),"the attic ends the game in the house later too"))
        .thenExecute(()->{h.assertTrue(PuzzleRooms.solved(f.data(),L)&&f.own(p[0],L).getBoolean(FeetUp.CLEAN),"solved for good, and a clean run is kept");
            h.assertTrue(!f.own(p[0],L).getBoolean("Ready")&&!f.own(p[0],P).getBoolean(FeetUp.FINISHED)&&!WitnessAccount.has(f.data(),p[0].getUUID(),WitnessAccount.Story.FEET_UP),"no account, and nothing of the first house is resolved");
            h.assertTrue(FeetUpTexts.LATER_TALLY.contains("one mark now")&&FeetUpTexts.LATER_RULES.contains("count for yourself")&&FeetUpTexts.chalkLater(5).endsWith("WE DID"),"the tally, the sixth rule and the answered chalk");
            var words=new HashSet<String>();for(String t:List.of(FeetUpTexts.LATER_TALLY,FeetUpTexts.LATER_RULES,FeetUpTexts.chalkLater(4),FeetUpTexts.chalkLater(5),FeetUpTexts.COUNT_LATER[0],FeetUpTexts.hintLater("hall"),FeetUpTexts.hintLater("boards")))words.addAll(Arrays.asList(t.toLowerCase(Locale.ROOT).split("[^a-z]+")));
            for(String word:List.of("boy","girl","young","old","white","black","brown","red","blue","green"))h.assertTrue(!words.contains(word),"the house later never reads the reader or names a colour: "+word);
            h.succeed();});
    }

    /** 0.4.383: one stub of the children's chalk from the attic jar for a reader who reached it; carried in the House, a short fall costs nothing. */
    @GameTest(template="empty",batch="feet_up_chalk",timeoutTicks=300)
    public static void theAtticJarGivesOneStubAndItKeepsAShortFallFree(GameTestHelper h){
        var f=fixture(h);var b=f.base(P);var l=f.in;ServerPlayer[] p={null,null};
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the house is resident"))
        .thenExecute(()->{var jar=l.getBlockState(b.offset(FeetUpRooms.CHALK_JAR_AT));h.assertTrue(FeetUpBlock.is(jar,FeetUpBlock.Kind.CHALK_JAR)&&jar.getValue(FeetUpBlock.STAGE)==0,"the jar stands full beside the chest");
            p[0]=stand(f,"chalk_taker",new BlockPos(1,10,-21));p[1]=stand(f,"chalk_other",new BlockPos(-1,10,-21));
            var own=f.own(p[0],P);h.assertTrue(FeetUp.click(p[0],b,FeetUpRooms.CHALK_JAR_AT,own)&&!own.getBoolean(FeetUp.CHALK_TAKEN)&&count(p[0])==0,"nothing for a reader who has not played up to here");
            own.putBoolean(FeetUp.FINISHED,true);FeetUp.click(p[0],b,FeetUpRooms.CHALK_JAR_AT,own);LiteraryVignettes.save(f.data(),p[0].getUUID(),P,own);
            h.assertTrue(own.getBoolean(FeetUp.CHALK_TAKEN)&&count(p[0])==1,"one stub for the reader who reached it");
            FeetUp.click(p[0],b,FeetUpRooms.CHALK_JAR_AT,own);h.assertTrue(count(p[0])==1,"only one, ever");
            ItemStack stub=ItemStack.EMPTY;for(var s:p[0].getInventory().items)if(Keepsakes.mine(s,p[0],P,FeetUp.CHALK_KEY))stub=s;
            h.assertTrue(!stub.isEmpty()&&stub.is(LabyrinthRegistry.CHALK.get())&&stub.getDamageValue()==FeetUp.STUB_WORN&&!Keepsakes.mine(stub,p[1],P,FeetUp.CHALK_KEY),"a worn stick of the House's chalk, theirs alone");
            var lore=stub.get(net.minecraft.core.component.DataComponents.LORE);h.assertTrue(lore!=null&&lore.lines().stream().anyMatch(c->c.getString().equals(Keepsakes.line(P,FeetUp.CHALK_KEY))),"its line says what it does");
            // Carried, a short fall in the House costs nothing; without it, or from higher, the fall is the fall.
            var kept=new net.neoforged.neoforge.event.entity.living.LivingFallEvent(p[0],5F,1F);Keepsakes.chalkFall(kept);
            var other=new net.neoforged.neoforge.event.entity.living.LivingFallEvent(p[1],5F,1F);Keepsakes.chalkFall(other);
            var high=new net.neoforged.neoforge.event.entity.living.LivingFallEvent(p[0],9F,1F);Keepsakes.chalkFall(high);
            h.assertTrue(kept.getDamageMultiplier()==0&&other.getDamageMultiplier()==1&&high.getDamageMultiplier()==1,"five blocks forgiven with the chalk; not without it; not from nine");
            h.assertTrue(Keepsakes.record(p[0]).getInt("ChalkKept")==1&&AccountAfterwards.chalk(Keepsakes.record(p[0])).startsWith("Afterwards, once,"),"the account's afterwards remembers it");
            h.succeed();});
    }
    private static int count(ServerPlayer p){int n=0;for(var s:p.getInventory().items)if(s.is(LabyrinthRegistry.CHALK.get()))n+=s.getCount();return n;}
}

package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;

/**
 * 0.4.306, the Sphinx's first crossing (docs/SPHINX_0_4_306.md), played with two readers' hands: the question when the hall is entered;
 * the stand moved and only the live chamber's view following it; a wrong latch catching at its stop with a warning and an exposed pin;
 * the right latch opening on the vestibule and the step through coming out of the vestibule's own south wall; each reader's first answer
 * kept apart; the pin pulled, her paw over the frame, whoever stays in her reach swept back past the sill and nobody hurt; the hall left
 * empty and the room settling back into its question; and the open way kept when the room is built again. 0.4.334: the co-op survey
 * pins (docs/SPHINX_0_4_334.md). 0.4.337: her whole face over the wall, in sight from the hall, her eyes following (docs/SPHINX_FACE_0_4_337.md).
 */
@GameTestHolder(TheOldestHouse.MOD_ID+"_exploration") @PrefixGameTestTemplate(false)
public final class SphinxTests {
    private SphinxTests(){}
    private static LiteraryTests.Fixture active;
    @AfterBatch(batch="sphinx_vestibule") public static void done(ServerLevel l){if(active!=null){active.close();active=null;}Sphinx.clearAll();}
    @AfterBatch(batch="sphinx_pins") public static void donePins(ServerLevel l){done(l);}
    private static void hold(LiteraryTests.Fixture f,LabyrinthPlace place){var slot=LabyrinthPlaces.slotBounds(f.origin,place);f.chunks.hold(f.in,new AABB(slot.minX(),slot.minY(),slot.minZ(),slot.maxX()+1,slot.maxY()+1,slot.maxZ()+1));}
    private static boolean press(ServerLevel l,ServerPlayer p,BlockPos at,boolean crouch){
        p.setShiftKeyDown(crouch);
        boolean used=l.getBlockState(at).useWithoutItem(l,p,new BlockHitResult(at.getCenter(),Direction.SOUTH,at,false)).consumesAction();
        p.setShiftKeyDown(false);return used;
    }
    private static void stand(ServerPlayer p,ServerLevel l,BlockPos b,double x,double z,float yaw){p.teleportTo(l,b.getX()+x,b.getY(),b.getZ()+z,yaw,0);}
    /** Where the server has her eyes turned (SphinxFaceBlock.LOOK). */
    private static int look(ServerLevel l,BlockPos b){return l.getBlockState(b.offset(Sphinx.FACE)).getValue(SphinxFaceBlock.LOOK);}

    @GameTest(template="empty",batch="sphinx_vestibule",timeoutTicks=1400)
    public static void theSphinxsFirstCrossingIsTestedAnsweredAndForced(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);active=f;var place=Sphinx.PLACE;var b=f.base(place);var l=f.in;hold(f,place);
        var p=NativeTestPlayers.survival(h,"sphinx_reader");f.players.add(p);
        var q=NativeTestPlayers.survival(h,"sphinx_friend");f.players.add(q);
        int[] live={-1},wrong={-1};
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the room is resident"))
            .thenExecute(()->{
                h.assertTrue(!place.isVignette()&&WitnessAccount.Story.of(place.id())==null,"a detour in the halls, not a story");
                Sphinx.clearAll();Sphinx.build(l,b);LabyrinthBuilder.registerDoors(f.data(),place,b);
                live[0]=Sphinx.live(l);wrong[0]=(live[0]+1)%3;
                h.assertTrue(!Sphinx.jibOpen(l,b)&&!Sphinx.solved(l),"the vestibule's south wall is plain wall beside its door");
                // The vestibule is an ordinary room; the hall is not: its north wall stops, and over it and back from it is her dark.
                h.assertTrue(!l.getBlockState(b.offset(0,4,-3)).isAir(),"the vestibule has a ceiling");
                h.assertTrue(l.getBlockState(b.offset(0,Sphinx.TOP-1,-8)).isAir()&&!l.getBlockState(b.offset(0,Sphinx.WALL_TOP,Sphinx.DOOR_Z)).isAir()&&l.getBlockState(b.offset(0,Sphinx.WALL_TOP+1,Sphinx.DOOR_Z)).isAir()&&l.getBlockState(b.offset(0,Sphinx.WALL_TOP+5,Sphinx.CAVITY_Z1)).isAir(),"the hall rises into the dark, and over its north wall the dark goes back");
                h.assertTrue(SphinxFaceBlock.is(l.getBlockState(b.offset(Sphinx.FACE)))&&!Sphinx.faceShown(l,b),"her face waits in the dark over the wall, not yet looking");
                h.assertTrue(l.getBlockState(b.offset(-9,Sphinx.TOP-2,-8)).is(PuzzleRegistry.sphinxMaterial("dark"))||l.getBlockState(b.offset(-10,Sphinx.TOP-2,-8)).is(PuzzleRegistry.sphinxMaterial("dark")),"the walls go dark above head height");
                for(int i=0;i<3;i++){
                    h.assertTrue(l.getBlockState(b.offset(Sphinx.door(i))).getBlock() instanceof DoorBlock&&!Sphinx.doorOpen(l,b,i),"three shut doors");
                    h.assertTrue(SphinxBlock.stage(l.getBlockState(b.offset(Sphinx.pin(i))))==0,"each pin hidden in its stop");
                }
                int agree=0;for(int i=0;i<3;i++)if(Sphinx.standOnA(l,b,i)==Sphinx.standOnA(l,b,-1))agree++;
                h.assertTrue(agree==2&&Sphinx.standOnA(l,b,live[0])==Sphinx.standOnA(l,b,-1),"the live view and one staged view agree with the vestibule; testing must tell them apart");
                stand(p,l,b,.5,-8.5,180);stand(q,l,b,.5,-3.5,180);})
            .thenWaitUntil(()->h.assertTrue(Sphinx.record(l,p.getUUID()).getBoolean("Heard"),"a voice asks its question as the hall is entered"))
            .thenExecute(()->{
                h.assertTrue(!Sphinx.record(l,q.getUUID()).getBoolean("Heard"),"the friend, still in the vestibule, has not heard it");
                // A door by hand does nothing.
                var door=b.offset(Sphinx.door(live[0]));
                var ev=new net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock(p,net.minecraft.world.InteractionHand.MAIN_HAND,door,new BlockHitResult(door.getCenter(),Direction.SOUTH,door,false));
                net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(ev);
                h.assertTrue(ev.isCanceled()&&!Sphinx.doorOpen(l,b,live[0]),"the doors open only by their latches");
                // The friend moves the stand; only the live chamber's stand follows it.
                boolean[] before=new boolean[3];for(int i=0;i<3;i++)before[i]=Sphinx.standOnA(l,b,i);boolean was=Sphinx.standOnA(l,b,-1);
                press(l,q,b.offset(Sphinx.TILE_A.getX(),0,Sphinx.TILE_A.getZ()).offset(was?0:2,0,0),false);
                h.assertTrue(Sphinx.standOnA(l,b,-1)!=was,"the stand is on the other marked tile");
                for(int i=0;i<3;i++)h.assertTrue((Sphinx.standOnA(l,b,i)!=before[i])==(i==live[0]),"only the live view changes with it: chamber "+i);
                // The flap opens on a real hole.
                h.assertTrue(press(l,p,b.offset(Sphinx.flap(live[0])),false)&&SphinxBlock.stage(l.getBlockState(b.offset(Sphinx.flap(live[0]))))==1,"the letter flap lifts");
                h.assertTrue(l.getBlockState(b.offset(Sphinx.flap(live[0]))).getCollisionShape(l,b.offset(Sphinx.flap(live[0]))).bounds().getYsize()>0
                    &&l.getBlockState(b.offset(Sphinx.flap(live[0]))).getShape(l,b.offset(Sphinx.flap(live[0]))).toAabbs().size()>1,"a frame round a hole, not a solid block");})
            .thenIdle(5)
            .thenExecute(()->{
                // A wrong answer: caught, warned, the pin shows.
                press(l,p,b.offset(Sphinx.latch(wrong[0])),false);
                h.assertTrue(!Sphinx.doorOpen(l,b,wrong[0])&&Sphinx.warned(l,wrong[0]),"the wrong door catches at its stop and she warns it off");
                h.assertTrue(SphinxBlock.stage(l.getBlockState(b.offset(Sphinx.pin(wrong[0]))))==1,"its stop pin is exposed");
                h.assertTrue(Sphinx.record(l,p.getUUID()).getInt("First")==2,"the reader's first answer is kept: wrong");
                h.assertTrue(Sphinx.faceShown(l,b)&&Sphinx.watching(l,b,wrong[0]),"with the warning, her face rises over the wall and looks down");
                // 0.4.337 (owner: "a fully visible sphinx face"): her whole face is in sight, crown to chin and side to side, from the back of
                // the hall and from its middle, and her eyes from right under her at the doors.
                var o=Vec3.atLowerCornerOf(b);var n=Sphinx.faceNormal();
                Vec3[] points={Sphinx.faceAt(144,24),Sphinx.faceAt(144,230),Sphinx.faceAt(24,120),Sphinx.faceAt(264,120),Sphinx.faceAt(Sphinx.FACE_EYES[0][0],Sphinx.FACE_EYES[0][1]),Sphinx.faceAt(Sphinx.FACE_EYES[1][0],Sphinx.FACE_EYES[1][1])};
                for(double z:new double[]{-7.7,-10.5})for(double x:new double[]{-6.5,.5,7.5})for(var pt:points){
                    var from=o.add(x,1.62,z);var at=o.add(pt).add(n.scale(.05));
                    var hit=l.clip(new net.minecraft.world.level.ClipContext(from,at,net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,p));
                    h.assertTrue(hit.getLocation().distanceTo(at)<.3,"her face is in sight from ("+x+", "+z+"): "+pt+" stops at "+hit.getLocation().subtract(o));
                }
                for(int k=0;k<2;k++){
                    var from=o.add(Sphinx.DOORS[wrong[0]]+.5,1.62,Sphinx.DOOR_Z+1.6);var at=o.add(points[4+k]).add(n.scale(.05));
                    h.assertTrue(l.clip(new net.minecraft.world.level.ClipContext(from,at,net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,p)).getLocation().distanceTo(at)<.3,"and her eyes from right at the door, looking up: "+k);
                }})
            .thenIdle(5)
            .thenExecute(()->{
                // 0.4.330 (owner: a second eye left the first open for good): the third door warned too opens a second eye, and both look.
                press(l,p,b.offset(Sphinx.latch((live[0]+2)%3)),false);
                h.assertTrue(Sphinx.watching(l,b,wrong[0])&&Sphinx.watching(l,b,(live[0]+2)%3),"a second warning keeps her looking: both doors' looks last together");})
            .thenExecute(()->{stand(q,l,b,.5,-3.5,0);stand(p,l,b,-6.5,-8.5,180);})
            .thenWaitUntil(()->h.assertTrue(look(l,b)%3==0,"her eyes follow the reader along the hall: to the west, though the friend in the vestibule is nearer"))
            .thenExecute(()->stand(p,l,b,7.5,-8.5,180))
            .thenWaitUntil(()->h.assertTrue(look(l,b)%3==2,"and to the east"))
            .thenExecute(()->stand(p,l,b,7.5,Sphinx.DOOR_Z+1.5,180))
            .thenWaitUntil(()->h.assertTrue(look(l,b)/3==0,"and down, right under her at the doors"))
            .thenWaitUntil(()->h.assertTrue(!Sphinx.faceShown(l,b),"and when both looks are over her face sinks back into the dark"))
            .thenExecute(()->{})
            .thenIdle(5)
            .thenExecute(()->{
                press(l,p,b.offset(Sphinx.latch(live[0])),false);
                h.assertTrue(Sphinx.doorOpen(l,b,live[0])&&Sphinx.record(l,p.getUUID()).getBoolean("Solved")&&Sphinx.record(l,p.getUUID()).getInt("First")==2,"a clean retry opens the live door; the first answer still counts as it was");
                stand(p,l,b,Sphinx.DOORS[live[0]]+.5,Sphinx.DOOR_Z-.5,180);})
            .thenWaitUntil(()->h.assertTrue(Sphinx.solved(l)&&p.getZ()-b.getZ()>-1.5&&p.getZ()-b.getZ()<1&&Math.abs(p.getX()-b.getX()-(Sphinx.JIB_IN.getX()+.5))<.6,
                "through the live door and out of the vestibule's south wall: "+(p.getX()-b.getX())+","+(p.getZ()-b.getZ())))
            .thenExecute(()->{
                h.assertTrue(Sphinx.jibOpen(l,b)&&l.getBlockState(b.offset(Sphinx.JIB_ON)).isAir(),"the wall beside the entry and the vestibule's way onward stand open");
                // The friend answers for themselves: right first time.
                stand(q,l,b,.5,-8.5,180);})
            .thenWaitUntil(()->h.assertTrue(Sphinx.record(l,q.getUUID()).getBoolean("Heard"),"the friend hears the question"))
            .thenIdle(5)
            .thenExecute(()->{
                press(l,q,b.offset(Sphinx.latch(live[0])),false);
                h.assertTrue(Sphinx.record(l,q.getUUID()).getInt("First")==1&&Sphinx.record(l,p.getUUID()).getInt("First")==2,"each reader's first answer is their own");})
            .thenIdle(5)
            .thenExecute(()->{
                // The provocation: the friend pulls the warned door's pin, standing in her reach.
                q.setHealth(q.getMaxHealth());q.hasChangedDimension();q.invulnerableTime=0;
                press(l,q,b.offset(Sphinx.pin(wrong[0])),false);
                h.assertTrue(Sphinx.provoked(l)==wrong[0]&&Sphinx.record(l,q.getUUID()).getBoolean("Forced")&&!Sphinx.record(l,p.getUUID()).getBoolean("Forced"),"the one who pulled the pin owns it");
                stand(q,l,b,Sphinx.DOORS[wrong[0]]+.5,Sphinx.DOOR_Z+.6,180);stand(p,l,b,.5,-3.5,0);})
            .thenWaitUntil(()->h.assertTrue(Sphinx.watching(l,b,wrong[0])&&SphinxBlock.is(l.getBlockState(b.offset(Sphinx.door(wrong[0])).above(Sphinx.WALL_TOP+1)),SphinxBlock.Kind.FINGERS),"her face rises and stays; her paw comes over the top of the wall"))
            .thenWaitUntil(()->h.assertTrue(SphinxBlock.is(l.getBlockState(b.offset(Sphinx.door(wrong[0])).offset(0,2,1)),SphinxBlock.Kind.FINGERS)&&Sphinx.doorOpen(l,b,wrong[0]),"down the wall to fold over the frame, and the door bows open"))
            .thenWaitUntil(()->h.assertTrue(q.getZ()-b.getZ()>Sphinx.HALL_Z0+.5&&Sphinx.record(l,q.getUUID()).getInt("Swept")==1,"whoever stays in her reach is swept back by her paw, out past the sill"))
            .thenExecute(()->{
                h.assertTrue(q.getHealth()==q.getMaxHealth()&&p.getHealth()==p.getMaxHealth(),"she hurts nobody");
                h.assertTrue(!Sphinx.sealed(l,wrong[0])&&q.getZ()-b.getZ()>Sphinx.DOOR_Z+.5,"standing right at her door is not going through it: only walking in is");
                stand(q,l,b,.5,-3.5,0);})
            .thenWaitUntil(()->h.assertTrue(Sphinx.provoked(l)<0&&!SphinxBlock.is(l.getBlockState(b.offset(Sphinx.door(wrong[0])).offset(0,2,1)),SphinxBlock.Kind.FINGERS)&&!Sphinx.faceShown(l,b)&&!SphinxBlock.is(l.getBlockState(b.offset(Sphinx.door(wrong[0])).above(Sphinx.WALL_TOP+1)),SphinxBlock.Kind.FINGERS),"the hall empty, she withdraws into the dark"))
            .thenExecute(()->{
                h.assertTrue(!Sphinx.doorOpen(l,b,wrong[0])&&SphinxBlock.stage(l.getBlockState(b.offset(Sphinx.pin(wrong[0]))))==0&&!Sphinx.sealed(l,wrong[0]),"the door back on its stop, the pin back in: the room is its question again");
                Sphinx.build(l,b);
                h.assertTrue(Sphinx.doorOpen(l,b,live[0])&&Sphinx.jibOpen(l,b),"built again: the live door and the walls she opened stay open, for everyone");
                h.assertTrue(WitnessAccount.count(f.data(),p.getUUID())==0,"and nothing counts toward the Witness");})
            .thenSucceed();
    }

    /**
     * 0.4.334, the co-op survey pins: a reader pins one door and a friend another while the latches are shut; the first latch lifted locks both
     * pins as their answers (the friend at the right door is credited though a wrong latch was pulled), every pin comes out, a second
     * latch on the same tick is refused, and a reader who walks out with a pin set takes it with them unanswered.
     */
    @GameTest(template="empty",batch="sphinx_pins",timeoutTicks=600)
    public static void surveyPinsLockEachReadersAnswerAtTheFirstLatch(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);active=f;var place=Sphinx.PLACE;var b=f.base(place);var l=f.in;hold(f,place);
        var p=NativeTestPlayers.survival(h,"sphinx_pinner");f.players.add(p);
        var q=NativeTestPlayers.survival(h,"sphinx_pinfriend");f.players.add(q);
        var w=NativeTestPlayers.survival(h,"sphinx_wanderer");f.players.add(w);
        int[] live={-1},wrong={-1},other={-1};
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the room is resident"))
            .thenExecute(()->{
                Sphinx.clearAll();Sphinx.build(l,b);LabyrinthBuilder.registerDoors(f.data(),place,b);
                live[0]=Sphinx.live(l);wrong[0]=(live[0]+1)%3;other[0]=(live[0]+2)%3;
                for(int i=0;i<3;i++)h.assertTrue(SphinxBlock.is(l.getBlockState(b.offset(Sphinx.survey(i))),SphinxBlock.Kind.SURVEY)&&SphinxBlock.stage(l.getBlockState(b.offset(Sphinx.survey(i))))==0,"an empty survey socket beside every latch");
                stand(p,l,b,.5,-8.5,180);stand(q,l,b,1.5,-8.5,180);stand(w,l,b,-1.5,-8.5,180);})
            .thenWaitUntil(()->h.assertTrue(Sphinx.record(l,p.getUUID()).getBoolean("Heard")&&Sphinx.record(l,q.getUUID()).getBoolean("Heard")&&Sphinx.record(l,w.getUUID()).getBoolean("Heard"),"all three hear the question"))
            .thenExecute(()->{
                h.assertTrue(press(l,p,b.offset(Sphinx.survey(wrong[0])),false)&&press(l,q,b.offset(Sphinx.survey(live[0])),false),"each reader sets a pin");
                h.assertTrue(SphinxBlock.stage(l.getBlockState(b.offset(Sphinx.survey(wrong[0]))))==1&&SphinxBlock.stage(l.getBlockState(b.offset(Sphinx.survey(live[0]))))==1,"each socket shows its pin to everyone");
                h.assertTrue(!Sphinx.record(l,p.getUUID()).contains("First")&&!Sphinx.record(l,q.getUUID()).contains("First"),"a pin is not yet an answer");
                // The wanderer pins too, moves it, and then walks out with it.
                press(l,w,b.offset(Sphinx.survey(other[0])),false);})
            .thenIdle(5)
            .thenExecute(()->press(l,w,b.offset(Sphinx.survey(wrong[0])),false))
            .thenIdle(2)
            .thenExecute(()->{
                h.assertTrue(SphinxBlock.stage(l.getBlockState(b.offset(Sphinx.survey(wrong[0]))))==2&&SphinxBlock.stage(l.getBlockState(b.offset(Sphinx.survey(other[0]))))==0,"a pin moved: two in one socket, the other empty");
                w.teleportTo(l.getServer().overworld(),.5,120,.5,0,0);})
            .thenWaitUntil(()->h.assertTrue(SphinxCore.pinOf(l,Sphinx.ROOM,w.getUUID())<0&&SphinxBlock.stage(l.getBlockState(b.offset(Sphinx.survey(wrong[0]))))==1,"a reader who leaves takes their pin with them"))
            .thenWaitUntil(()->h.assertTrue(!Sphinx.record(l,w.getUUID()).contains("First")&&Sphinx.record(l,w.getUUID()).getInt("Walked")>=0,"and has not answered"))
            .thenExecute(()->{
                // Two latches on the same tick: one attempt.
                press(l,p,b.offset(Sphinx.latch(wrong[0])),false);press(l,q,b.offset(Sphinx.latch(other[0])),false);
                h.assertTrue(Sphinx.warned(l,wrong[0])&&!Sphinx.warned(l,other[0]),"the second latch, still moving behind the first, does nothing");
                var rp=Sphinx.record(l,p.getUUID());var rq=Sphinx.record(l,q.getUUID());
                h.assertTrue(rp.getInt("First")==2&&!rp.getBoolean("Solved"),"the puller's pin was wrong: their answer is wrong");
                h.assertTrue(rq.getInt("First")==1&&rq.getBoolean("Solved")&&rq.getInt("Pinned")==live[0]+1,"the friend pinned the right door: credited, though a wrong latch was pulled");
                h.assertTrue(SphinxCore.pins(l,Sphinx.ROOM).isEmpty(),"the attempt over, every pin comes out");
                for(int i=0;i<3;i++)h.assertTrue(SphinxBlock.stage(l.getBlockState(b.offset(Sphinx.survey(i))))==0,"and every socket is empty again");
                h.assertTrue(!Sphinx.doorOpen(l,b,live[0]),"a wrong latch does not open the right door");})
            .thenIdle(6)
            .thenExecute(()->{
                // Solo again: with no pin, the hands on the latch answer.
                press(l,q,b.offset(Sphinx.latch(live[0])),false);
                h.assertTrue(Sphinx.doorOpen(l,b,live[0])&&Sphinx.record(l,q.getUUID()).getInt("First")==1,"the right latch opens; the friend's first answer stays as it was");
                h.assertTrue(WitnessAccount.count(f.data(),q.getUUID())==0,"and nothing counts toward the Witness");})
            .thenSucceed();
    }
}

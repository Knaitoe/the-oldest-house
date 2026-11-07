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
 * empty and the room settling back into its question; and the open way kept when the room is built again.
 */
@GameTestHolder(TheOldestHouse.MOD_ID+"_exploration") @PrefixGameTestTemplate(false)
public final class SphinxTests {
    private SphinxTests(){}
    private static LiteraryTests.Fixture active;
    @AfterBatch(batch="sphinx_vestibule") public static void done(ServerLevel l){if(active!=null){active.close();active=null;}Sphinx.clearAll();}
    private static void hold(LiteraryTests.Fixture f,LabyrinthPlace place){var slot=LabyrinthPlaces.slotBounds(f.origin,place);f.chunks.hold(f.in,new AABB(slot.minX(),slot.minY(),slot.minZ(),slot.maxX()+1,slot.maxY()+1,slot.maxZ()+1));}
    private static boolean press(ServerLevel l,ServerPlayer p,BlockPos at,boolean crouch){
        p.setShiftKeyDown(crouch);
        boolean used=l.getBlockState(at).useWithoutItem(l,p,new BlockHitResult(at.getCenter(),Direction.SOUTH,at,false)).consumesAction();
        p.setShiftKeyDown(false);return used;
    }
    private static void stand(ServerPlayer p,ServerLevel l,BlockPos b,double x,double z,float yaw){p.teleportTo(l,b.getX()+x,b.getY(),b.getZ()+z,yaw,0);}

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
                h.assertTrue(l.getBlockState(b.offset(0,Sphinx.TOP-1,-8)).isAir()&&!l.getBlockState(b.offset(0,Sphinx.WALL_TOP,Sphinx.DOOR_Z)).isAir()&&l.getBlockState(b.offset(0,Sphinx.WALL_TOP+1,Sphinx.DOOR_Z)).isAir()&&l.getBlockState(b.offset(0,Sphinx.EYE_Y,Sphinx.EYE_Z-3)).isAir(),"the hall rises into the dark, and over its north wall the dark goes back");
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
                h.assertTrue(Sphinx.eyeOpen(l,b,wrong[0]),"with the warning, an eye opens in the dark over that door");
                // Looking up from anywhere in the hall finds it over the wall.
                var eyeAt=new Vec3(b.getX()+Sphinx.DOORS[wrong[0]]+1,b.getY()+Sphinx.EYE_Y-.05,b.getZ()+Sphinx.EYE_Z+1);
                var back=new Vec3(b.getX()+Sphinx.DOORS[wrong[0]]+1,b.getY()+1.62,b.getZ()-7.6);var close=new Vec3(b.getX()+Sphinx.DOORS[wrong[0]]+1,b.getY()+1.62,b.getZ()-8.7);
                var fromBack=l.clip(new net.minecraft.world.level.ClipContext(back,eyeAt,net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,p));
                var fromDoor=l.clip(new net.minecraft.world.level.ClipContext(close,eyeAt,net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,p));
                h.assertTrue(fromBack.getLocation().distanceTo(eyeAt)<.7,"from the back of the hall the eye is in sight over the wall: "+fromBack.getLocation().subtract(Vec3.atLowerCornerOf(b)));
                // Not a knife-edge: from the middle of the hall, and with the line raised or lowered a little, it is still in sight.
                var mid=new Vec3(b.getX()+Sphinx.DOORS[wrong[0]]+.6,b.getY()+1.62,b.getZ()-8.2);
                for(double dy:new double[]{-.6,0,.6})h.assertTrue(l.clip(new net.minecraft.world.level.ClipContext(mid,eyeAt.add(0,dy,0),net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,p)).getLocation().distanceTo(eyeAt.add(0,dy,0))<.7,"from the middle of the hall too, offset "+dy);})
            .thenIdle(5)
            .thenExecute(()->{
                // 0.4.330 (owner: a second eye left the first open for good): the third door warned too opens a second eye, and both look.
                press(l,p,b.offset(Sphinx.latch((live[0]+2)%3)),false);
                h.assertTrue(Sphinx.eyeOpen(l,b,wrong[0])&&Sphinx.eyeOpen(l,b,(live[0]+2)%3),"a second warning opens a second eye, and the first stays open with it");})
            .thenExecute(()->{stand(q,l,b,.5,-3.5,0);stand(p,l,b,Sphinx.DOORS[wrong[0]]-2.5,-8.5,180);})
            .thenWaitUntil(()->h.assertTrue(l.getBlockState(b.offset(Sphinx.eye(wrong[0]))).getValue(SphinxEyeBlock.LOOK)%3==0,"her pupil follows the reader along the hall: to the west"))
            .thenWaitUntil(()->{int o=(live[0]+2)%3;h.assertTrue(l.getBlockState(b.offset(Sphinx.eye(o))).getValue(SphinxEyeBlock.LOOK)==Sphinx.lookAt(l,b,o),"the second eye follows too");})
            .thenExecute(()->stand(p,l,b,Sphinx.DOORS[wrong[0]]+4.5,-8.5,180))
            .thenWaitUntil(()->h.assertTrue(l.getBlockState(b.offset(Sphinx.eye(wrong[0]))).getValue(SphinxEyeBlock.LOOK)%3==2,"and to the east"))
            .thenWaitUntil(()->{int o=(live[0]+2)%3;h.assertTrue(l.getBlockState(b.offset(Sphinx.eye(o))).getValue(SphinxEyeBlock.LOOK)==Sphinx.lookAt(l,b,o),"and so does the second");})
            .thenWaitUntil(()->h.assertTrue(!Sphinx.eyeOpen(l,b,wrong[0])&&!Sphinx.eyeOpen(l,b,(live[0]+2)%3),"and both close again"))
            .thenExecute(()->{})
            .thenIdle(5)
            .thenExecute(()->{
                press(l,p,b.offset(Sphinx.latch(live[0])),false);
                h.assertTrue(Sphinx.doorOpen(l,b,live[0])&&Sphinx.record(l,p.getUUID()).getBoolean("Solved")&&Sphinx.record(l,p.getUUID()).getInt("First")==2,"a clean retry opens the live door; the first answer still counts as it was");
                stand(p,l,b,Sphinx.DOORS[live[0]]+.5,-10.5,180);})
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
                stand(q,l,b,Sphinx.DOORS[wrong[0]]+.5,-9.4,180);stand(p,l,b,.5,-3.5,0);})
            .thenWaitUntil(()->h.assertTrue(Sphinx.eyeOpen(l,b,wrong[0])&&SphinxBlock.is(l.getBlockState(b.offset(Sphinx.door(wrong[0])).above(Sphinx.WALL_TOP+1)),SphinxBlock.Kind.FINGERS),"the eye opens and stays; her fingers come over the top of the wall"))
            .thenWaitUntil(()->h.assertTrue(SphinxBlock.is(l.getBlockState(b.offset(Sphinx.door(wrong[0])).offset(0,2,1)),SphinxBlock.Kind.FINGERS)&&Sphinx.doorOpen(l,b,wrong[0]),"down the wall to fold over the frame, and the door bows open"))
            .thenWaitUntil(()->h.assertTrue(q.getZ()-b.getZ()>Sphinx.HALL_Z0+.5&&Sphinx.record(l,q.getUUID()).getInt("Swept")==1,"whoever stays in her reach is swept back by her paw, out past the sill"))
            .thenExecute(()->{
                h.assertTrue(q.getHealth()==q.getMaxHealth()&&p.getHealth()==p.getMaxHealth(),"she hurts nobody");
                h.assertTrue(!Sphinx.sealed(l,wrong[0])&&q.getZ()-b.getZ()>Sphinx.DOOR_Z+.5,"standing right at her door is not going through it: only walking in is");
                stand(q,l,b,.5,-3.5,0);})
            .thenWaitUntil(()->h.assertTrue(Sphinx.provoked(l)<0&&!SphinxBlock.is(l.getBlockState(b.offset(Sphinx.door(wrong[0])).offset(0,2,1)),SphinxBlock.Kind.FINGERS)&&!Sphinx.eyeOpen(l,b,wrong[0])&&!SphinxBlock.is(l.getBlockState(b.offset(Sphinx.door(wrong[0])).above(Sphinx.WALL_TOP+1)),SphinxBlock.Kind.FINGERS),"the hall empty, she withdraws into the dark"))
            .thenExecute(()->{
                h.assertTrue(!Sphinx.doorOpen(l,b,wrong[0])&&SphinxBlock.stage(l.getBlockState(b.offset(Sphinx.pin(wrong[0]))))==0&&!Sphinx.sealed(l,wrong[0]),"the door back on its stop, the pin back in: the room is its question again");
                Sphinx.build(l,b);
                h.assertTrue(Sphinx.doorOpen(l,b,live[0])&&Sphinx.jibOpen(l,b),"built again: the live door and the walls she opened stay open, for everyone");
                h.assertTrue(WitnessAccount.count(f.data(),p.getUUID())==0,"and nothing counts toward the Witness");})
            .thenSucceed();
    }
}

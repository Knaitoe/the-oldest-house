package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.gametest.*;

/**
 * 0.4.335, the Sphinx's stair (docs/SPHINX_0_4_335.md), played by two readers: the question from under the step as the gallery is entered;
 * a chip from the tray settling at the foot of a false flight and coming out onto the landing above the live one; survey pins locked by a
 * refused gate (each reader's own answer), her warning, the exposed pin and her eye in the gap; the live gate released and the flight
 * down coming up onto the landing, the way onward opening for everyone; the pin pulled, her eye and foreleg coming up, the paw over the
 * gate, a reader in reach swept back over the approach rail unhurt; the gallery empty and her withdrawing; going down under her sealing
 * that mouth; a fall into the shaft given back at the rail; chips left behind on leaving; and the open way kept when the room is built again.
 */
@GameTestHolder(TheOldestHouse.MOD_ID+"_exploration") @PrefixGameTestTemplate(false)
public final class SphinxStairTests {
    private SphinxStairTests(){}
    private static LiteraryTests.Fixture active;
    @AfterBatch(batch="sphinx_stair") public static void done(ServerLevel l){if(active!=null){active.close();active=null;}SphinxStair.clearAll();}
    private static void hold(LiteraryTests.Fixture f,LabyrinthPlace place){var slot=LabyrinthPlaces.slotBounds(f.origin,place);f.chunks.hold(f.in,new AABB(slot.minX(),slot.minY(),slot.minZ(),slot.maxX()+1,slot.maxY()+1,slot.maxZ()+1));}
    private static boolean press(ServerLevel l,ServerPlayer p,BlockPos at){
        return l.getBlockState(at).useWithoutItem(l,p,new BlockHitResult(at.getCenter(),Direction.SOUTH,at,false)).consumesAction();
    }
    private static void stand(ServerPlayer p,ServerLevel l,BlockPos b,double x,double y,double z,float yaw){p.teleportTo(l,b.getX()+x,b.getY()+y,b.getZ()+z,yaw,0);}
    private static boolean chipAt(ServerLevel l,BlockPos at){return SphinxBlock.is(l.getBlockState(at),SphinxBlock.Kind.CHIP);}

    @GameTest(template="empty",batch="sphinx_stair",timeoutTicks=2000)
    public static void theStairIsTestedWithChipsAnsweredClimbedAndForced(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);active=f;var place=SphinxStair.PLACE;var b=f.base(place);var l=f.in;hold(f,place);
        var p=NativeTestPlayers.survival(h,"stair_reader");f.players.add(p);
        var q=NativeTestPlayers.survival(h,"stair_friend");f.players.add(q);
        int[] live={-1},wrong={-1},other={-1};
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the room is resident"))
            .thenExecute(()->{
                h.assertTrue(!place.isVignette()&&WitnessAccount.Story.of(place.id())==null,"a detour in the halls, not a story");
                SphinxStair.clearAll();SphinxStair.build(l,b);LabyrinthBuilder.registerDoors(f.data(),place,b);
                live[0]=SphinxStair.live(l);wrong[0]=(live[0]+1)%3;other[0]=(live[0]+2)%3;
                for(int i=0;i<3;i++){
                    h.assertTrue(SphinxStair.gateStage(l,b,i)==0,"three shut gates");
                    h.assertTrue(!l.getBlockState(b.offset(SphinxStair.gate(i,0))).getCollisionShape(l,b.offset(SphinxStair.gate(i,0))).isEmpty(),"a shut gate stops a body");
                    h.assertTrue(l.getBlockState(b.offset(SphinxStair.tread(i,0)).below()).getBlock() instanceof net.minecraft.world.level.block.StairBlock,"a flight goes down from each mouth");
                }
                h.assertTrue(SphinxBlock.is(l.getBlockState(b.offset(SphinxStair.onLanding(live[0])).below()),SphinxBlock.Kind.GRATE),"a grated landing hangs over the flights");
                h.assertTrue(!SphinxStair.jibOpen(l,b),"the landing's stair down is plain stone");
                // From the gallery the landing's floor is in sight through its grating.
                var eyePos=net.minecraft.world.phys.Vec3.atLowerCornerOf(b).add(SphinxStair.MOUTHS[live[0]]+.5,1.62,-5.2);
                var lip=net.minecraft.world.phys.Vec3.atLowerCornerOf(b.offset(SphinxStair.onLanding(live[0]))).add(.5,.1,.5);
                var hit=l.clip(new net.minecraft.world.level.ClipContext(eyePos,lip,net.minecraft.world.level.ClipContext.Block.VISUAL,net.minecraft.world.level.ClipContext.Fluid.NONE,p));
                h.assertTrue(hit.getType()==net.minecraft.world.phys.HitResult.Type.MISS||hit.getLocation().distanceTo(lip)<1.0||SphinxBlock.is(l.getBlockState(hit.getBlockPos()),SphinxBlock.Kind.GRATE),
                    "the landing's lip can be seen from the gallery: "+hit.getLocation().subtract(net.minecraft.world.phys.Vec3.atLowerCornerOf(b)));
                stand(p,l,b,SphinxStair.MOUTHS[wrong[0]]+.5,0,-5.5,180);stand(q,l,b,.5,0,-1.5,180);})
            .thenWaitUntil(()->h.assertTrue(SphinxStair.record(l,p.getUUID()).getBoolean("Heard"),"her question comes from under the step as the gallery is entered"))
            .thenExecute(()->{
                h.assertTrue(!SphinxStair.record(l,q.getUUID()).getBoolean("Heard"),"the friend behind the approach rail has not heard it");
                h.assertTrue(press(l,p,b.offset(SphinxStair.TRAY)),"the tray gives a chip");
                h.assertTrue(SphinxStair.isChip(p.getMainHandItem())||p.getInventory().items.stream().anyMatch(SphinxStair::isChip),"a stone chip in hand");
                for(int k=0;k<p.getInventory().items.size();k++)if(SphinxStair.isChip(p.getInventory().items.get(k))){var c=p.getInventory().items.get(k);p.getInventory().items.set(k,ItemStack.EMPTY);p.setItemInHand(InteractionHand.MAIN_HAND,c);break;}})
            .thenIdle(5)
            .thenExecute(()->{
                h.assertTrue(press(l,p,b.offset(SphinxStair.gate(wrong[0],0))),"a chip let go at a mouth");
                h.assertTrue(SphinxStair.rolling(wrong[0]),"and it goes down");})
            .thenWaitUntil(()->h.assertTrue(!SphinxStair.rolling(wrong[0])&&chipAt(l,b.offset(SphinxStair.foot(wrong[0]))),"down a false flight the chip settles far below"))
            .thenIdle(5)
            .thenExecute(()->{
                p.setItemInHand(InteractionHand.MAIN_HAND,SphinxStair.chip());
                stand(p,l,b,SphinxStair.MOUTHS[live[0]]+.5,0,-5.5,180);
                h.assertTrue(press(l,p,b.offset(SphinxStair.gate(live[0],0))),"a chip at the live mouth");})
            .thenWaitUntil(()->h.assertTrue(!SphinxStair.rolling(live[0])&&chipAt(l,b.offset(SphinxStair.onLanding(live[0]))),"down the live flight it comes back out on the landing overhead, against the lip"))
            .thenExecute(()->{
                h.assertTrue(!chipAt(l,b.offset(SphinxStair.foot(live[0]))),"and not at the foot of its stair");
                // Survey pins, then a refused gate locks them.
                stand(q,l,b,SphinxStair.MOUTHS[live[0]]+.5,0,-4.5,180);})
            .thenWaitUntil(()->h.assertTrue(SphinxStair.record(l,q.getUUID()).getBoolean("Heard"),"the friend hears it too"))
            .thenIdle(5)
            .thenExecute(()->{
                h.assertTrue(press(l,p,b.offset(SphinxStair.survey(wrong[0])))&&press(l,q,b.offset(SphinxStair.survey(live[0]))),"each sets a pin");
                h.assertTrue(SphinxBlock.stage(l.getBlockState(b.offset(SphinxStair.survey(live[0]))))==1,"the socket shows it");})
            .thenIdle(5)
            .thenExecute(()->{
                press(l,p,b.offset(SphinxStair.release(wrong[0])));
                h.assertTrue(SphinxStair.warned(l,wrong[0])&&SphinxStair.gateStage(l,b,wrong[0])==0,"a refused gate catches and she warns it off");
                h.assertTrue(SphinxBlock.stage(l.getBlockState(b.offset(SphinxStair.pin(wrong[0]))))==1,"its stop pin shows");
                h.assertTrue(SphinxStair.record(l,p.getUUID()).getInt("First")==2&&SphinxStair.record(l,q.getUUID()).getInt("First")==1,"each pin is its reader's own answer");
                h.assertTrue(SphinxStair.eyeY(l,b,wrong[0])==-4,"an eye opens in the gap below the flights, looking up");})
            .thenWaitUntil(()->h.assertTrue(SphinxStair.eyeY(l,b,wrong[0])==Integer.MIN_VALUE,"and closes again"))
            .thenExecute(()->{
                press(l,q,b.offset(SphinxStair.release(live[0])));
                h.assertTrue(SphinxStair.gateStage(l,b,live[0])==1&&l.getBlockState(b.offset(SphinxStair.gate(live[0],0))).getCollisionShape(l,b.offset(SphinxStair.gate(live[0],0))).isEmpty(),"the live gate swings back");
                // Down the flight: up onto the landing.
                stand(q,l,b,SphinxStair.MOUTHS[live[0]]+.5,-4,SphinxStair.GATE_Z-1-4+.5,180);})
            .thenWaitUntil(()->h.assertTrue(q.getY()-b.getY()>SphinxStair.LANDING_Y+.5&&q.getZ()-b.getZ()<SphinxStair.LANDING_Z0&&q.getZ()-b.getZ()>SphinxStair.LANDING_Z1-1,
                "down the stair and round the turn, up onto the landing: "+(q.getX()-b.getX())+","+(q.getY()-b.getY())+","+(q.getZ()-b.getZ())))
            .thenExecute(()->{
                h.assertTrue(SphinxStair.solved(l)&&SphinxStair.jibOpen(l,b),"the landing's stair down to the way onward stands open, for everyone");
                // The provocation, by the reader still in the gallery: warned again, then the pin.
                stand(p,l,b,SphinxStair.MOUTHS[wrong[0]]+.5,0,-5.5,180);})
            .thenIdle(5)
            .thenExecute(()->press(l,p,b.offset(SphinxStair.release(wrong[0]))))
            .thenIdle(5)
            .thenExecute(()->{
                p.setHealth(p.getMaxHealth());
                press(l,p,b.offset(SphinxStair.pin(wrong[0])));
                h.assertTrue(SphinxStair.provoked(l)==wrong[0]&&SphinxStair.record(l,p.getUUID()).getBoolean("Forced")&&!SphinxStair.record(l,q.getUUID()).getBoolean("Forced"),"the one who pulled the pin owns it");})
            .thenWaitUntil(()->h.assertTrue(SphinxStair.eyeY(l,b,wrong[0])>=-3,"an eye comes up through the gap between the flights"))
            .thenWaitUntil(()->h.assertTrue(SphinxBlock.is(l.getBlockState(b.offset(SphinxStair.tread(wrong[0],3))),SphinxBlock.Kind.LIMB),"her foreleg comes up the stair"))
            .thenWaitUntil(()->h.assertTrue(SphinxBlock.is(l.getBlockState(b.offset(SphinxStair.tread(wrong[0],0))),SphinxBlock.Kind.FINGERS)&&SphinxStair.gateStage(l,b,wrong[0])==2,"her paw over the top tread, the gate bowed out"))
            .thenWaitUntil(()->h.assertTrue(p.getZ()-b.getZ()>SphinxStair.RAIL_Z&&SphinxStair.record(l,p.getUUID()).getInt("Swept")==1,"whoever stays in her reach is swept back over the approach rail"))
            .thenExecute(()->h.assertTrue(p.getHealth()==p.getMaxHealth(),"she hurts nobody"))
            .thenWaitUntil(()->h.assertTrue(SphinxStair.provoked(l)<0&&SphinxStair.eyeY(l,b,wrong[0])==Integer.MIN_VALUE&&!SphinxBlock.is(l.getBlockState(b.offset(SphinxStair.tread(wrong[0],3))),SphinxBlock.Kind.LIMB),"the gallery empty, she goes back down"))
            .thenExecute(()->{
                h.assertTrue(SphinxStair.gateStage(l,b,wrong[0])==0&&SphinxBlock.stage(l.getBlockState(b.offset(SphinxStair.pin(wrong[0]))))==0,"the gate back on its stop, the pin back in");
                stand(p,l,b,SphinxStair.MOUTHS[other[0]]+.5,0,-5.5,180);})
            .thenIdle(5)
            .thenExecute(()->press(l,p,b.offset(SphinxStair.release(other[0]))))
            .thenIdle(5)
            .thenExecute(()->press(l,p,b.offset(SphinxStair.pin(other[0]))))
            .thenWaitUntil(()->h.assertTrue(SphinxStair.gateStage(l,b,other[0])==2,"provoked again, at the third stair"))
            .thenExecute(()->stand(p,l,b,SphinxStair.MOUTHS[other[0]]+.5,-1,SphinxStair.GATE_Z-1.5,180))
            .thenWaitUntil(()->h.assertTrue(SphinxStair.sealed(l,other[0])&&p.getX()-b.getX()>SphinxStair.ONWARD_X-.6,"down under her, out by the way onward, and that mouth closes behind"))
            .thenExecute(()->{
                h.assertTrue(l.getBlockState(b.offset(SphinxStair.gate(other[0],0))).is(PuzzleRegistry.sphinxMaterial("ashlar_seam")),"cracked masonry where the mouth was");
                // A fall into the shaft is given back at the rail.
                stand(p,l,b,-3.5,-8,-10.5,180);})
            .thenWaitUntil(()->h.assertTrue(p.getZ()-b.getZ()>SphinxStair.RAIL_Z&&p.getY()-b.getY()>-.5,"a fall into the dark is given back at the rail"))
            .thenExecute(()->{
                p.setItemInHand(InteractionHand.MAIN_HAND,SphinxStair.chip());
                p.teleportTo(l.getServer().overworld(),.5,120,.5,0,0);})
            .thenWaitUntil(()->h.assertTrue(p.getInventory().items.stream().noneMatch(SphinxStair::isChip),"a chip goes no further than the room"))
            .thenExecute(()->{
                SphinxStair.build(l,b);
                h.assertTrue(SphinxStair.gateStage(l,b,live[0])==1&&SphinxStair.jibOpen(l,b)&&SphinxStair.sealed(l,other[0]),"built again: the released gate, the open way and the sealed mouth all stay as they were");
                h.assertTrue(WitnessAccount.count(f.data(),p.getUUID())==0,"and nothing counts toward the Witness");})
            .thenSucceed();
    }
}

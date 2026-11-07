package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;

/**
 * 0.4.339, the Sphinx's field (docs/SPHINX_0_4_339.md), played by two readers outside at dusk: her question from the ditch, and again at
 * the culvert's grate for a friend on the lane; the loose soil brushed at two gaps, footprints at one and the old road's asphalt at the
 * other; the surveyor's tin giving each reader who heard her one folded survey and no second while they carry it; survey pins; a gate on a
 * newer path caught on its stop, her warning, the stop pin showing and her eye coming up over the corn; the gate on the old road opening,
 * and walking the road to its strip of asphalt opening the way to the lane for everyone; the pin pulled, her shoulder rising, the corn
 * flattening, her hand across the track and the gate bowed open; a reader in reach swept back into the ditch unhurt; her withdrawing once
 * the clearing is empty; walking on past her hand setting the reader down on the lane and closing the gap in corn; and the open gate, the
 * open way, the brushed soil and the closed gap kept when the field is built again.
 */
@GameTestHolder(TheOldestHouse.MOD_ID+"_exploration") @PrefixGameTestTemplate(false)
public final class SphinxFieldTests {
    private SphinxFieldTests(){}
    private static LiteraryTests.Fixture active;
    @AfterBatch(batch="sphinx_field") public static void done(ServerLevel l){if(active!=null){active.close();active=null;}SphinxField.clearAll();}
    private static void hold(LiteraryTests.Fixture f,LabyrinthPlace place){var slot=LabyrinthPlaces.slotBounds(f.origin,place);f.chunks.hold(f.out,new AABB(slot.minX(),slot.minY(),slot.minZ(),slot.maxX()+1,slot.maxY()+1,slot.maxZ()+1));}
    private static boolean press(ServerLevel l,ServerPlayer p,BlockPos at){
        return l.getBlockState(at).useWithoutItem(l,p,new BlockHitResult(at.getCenter(),Direction.SOUTH,at,false)).consumesAction();
    }
    /** A gate opened the way a hand opens one: through the native right-click, so the House's own handler answers it. */
    private static void lift(ServerLevel l,ServerPlayer p,BlockPos at){
        p.gameMode.useItemOn(p,l,p.getMainHandItem(),InteractionHand.MAIN_HAND,new BlockHitResult(at.getCenter(),Direction.SOUTH,at,false));
    }
    private static void stand(ServerPlayer p,ServerLevel l,BlockPos b,double x,double y,double z){p.teleportTo(l,b.getX()+x,b.getY()+y,b.getZ()+z,180,0);p.setDeltaMovement(Vec3.ZERO);p.fallDistance=0;}
    private static boolean fits(ServerLevel l,BlockPos b,double x,double y,double z){var at=Vec3.atLowerCornerOf(b).add(x,y,z);return l.noCollision(null,new AABB(at.x-.3,at.y+.01,at.z-.3,at.x+.3,at.y+1.8,at.z+.3));}
    private static double x(int i){return SphinxField.GAPS[i]+.5;}
    private static int surveys(ServerPlayer p){int n=0;for(var s:p.getInventory().items)if(SphinxField.isSurvey(s))n+=s.getCount();return n;}
    private static boolean gateOpen(ServerLevel l,BlockPos b,int i){var s=l.getBlockState(b.offset(SphinxField.gate(i)));return s.getBlock() instanceof FenceGateBlock&&s.getValue(FenceGateBlock.OPEN);}

    @GameTest(template="empty",batch="sphinx_field",timeoutTicks=3000)
    public static void theFieldIsBrushedAnsweredWalkedAndForced(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);active=f;var place=SphinxField.PLACE;var b=f.base(place);var l=f.out;hold(f,place);
        var p=NativeTestPlayers.survival(h,"field_reader");f.players.add(p);
        var q=NativeTestPlayers.survival(h,"field_friend");f.players.add(q);
        int[] live={-1},wrong={-1},other={-1};
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the field is resident"))
            .thenExecute(()->{
                h.assertTrue(!place.isVignette()&&WitnessAccount.Story.of(place.id())==null,"a detour, not a story");
                h.assertTrue(NovelRooms.outside(place)&&NovelRooms.dimension(place).equals(HouseDimensions.OUTSIDE),"outside");
                SphinxField.clearAll();SphinxField.build(l,b);LabyrinthBuilder.registerDoors(f.data(),place,b);
                h.assertTrue(f.data().door(place.id()+"/far").dimension.equals(HouseDimensions.OUTSIDE),"its way onward opens outside");
                live[0]=SphinxField.live(l);wrong[0]=(live[0]+1)%3;other[0]=(live[0]+2)%3;
                for(int i=0;i<3;i++){
                    h.assertTrue(!gateOpen(l,b,i)&&SphinxField.soilStage(l,b,i)==0,"a shut field gate and loose soil at each gap");
                    h.assertTrue(SphinxBlock.stage(l.getBlockState(b.offset(SphinxField.pin(i))))==0&&SphinxBlock.is(l.getBlockState(b.offset(SphinxField.survey(i))),SphinxBlock.Kind.SURVEY),"a flush stop pin and a survey socket on each gate's posts");
                    h.assertTrue(fits(l,b,x(i),0,SphinxField.GATE_Z-3.5)&&!fits(l,b,x(i)+1,0,SphinxField.GATE_Z-3.5),"a worn path in, between standing corn a body cannot push through");
                }
                h.assertTrue(fits(l,b,x(live[0]),0,SphinxField.ROAD_Z0-.5)&&!fits(l,b,x(wrong[0]),0,SphinxField.PATH_END-4.5),"the old road runs through to the strip; the newer paths end in the corn");
                h.assertTrue(!SphinxField.jibOpen(l,b),"corn between the road's strip and the lane");
                h.assertTrue(fits(l,b,14.5,0,-10.5)&&fits(l,b,15.5,0,-30.5)&&fits(l,b,15.5,0,SphinxField.DOOR_Z+1.5),"the lane up the east edge is open the whole way");
                h.assertTrue(SceneClock.at(f.origin,b.offset(0,1,-10),HouseDimensions.OUTSIDE)==3,"dusk");
                stand(p,l,b,x(wrong[0]),-1,-4.5);stand(q,l,b,15.5,0,-20.5);})
            .thenWaitUntil(()->h.assertTrue(SphinxField.record(l,p.getUUID()).getBoolean("Heard"),"her question from the ditch"))
            .thenExecute(()->{
                h.assertTrue(!SphinxField.record(l,q.getUUID()).getBoolean("Heard"),"the friend on the lane has not heard it");
                h.assertTrue(press(l,q,b.offset(SphinxField.CULVERT))&&SphinxField.record(l,q.getUUID()).getBoolean("Heard"),"the culvert's grate asks it again");
                stand(p,l,b,x(wrong[0]),0,SphinxField.GATE_Z+1.5);})
            .thenIdle(5)
            .thenExecute(()->{
                h.assertTrue(press(l,p,b.offset(SphinxField.soil(wrong[0])))&&SphinxField.soilStage(l,b,wrong[0])==1,"footprints going in under one gap's soil");
                h.assertTrue(press(l,q,b.offset(SphinxField.soil(live[0])))&&SphinxField.soilStage(l,b,live[0])==2,"cracked asphalt and paint under another's");
                h.assertTrue((SphinxField.record(l,p.getUUID()).getInt("Brushed")&(1<<wrong[0]))!=0&&(SphinxField.record(l,q.getUUID()).getInt("Brushed")&(1<<live[0]))!=0,"each remembers what they found");})
            .thenIdle(5)
            .thenExecute(()->{
                h.assertTrue(press(l,p,b.offset(SphinxField.BOX))&&surveys(p)==1,"the surveyor's tin gives a reader who heard her a folded survey");
                h.assertTrue(press(l,q,b.offset(SphinxField.BOX))&&surveys(q)==1,"and the friend one of their own");
                var lore=q.getInventory().items.stream().filter(SphinxField::isSurvey).findFirst().orElseThrow().get(DataComponents.LORE);
                h.assertTrue(lore!=null&&lore.lines().size()==4,"the friend who brushed the old road bare has a line to add");})
            .thenIdle(5)
            .thenExecute(()->{
                h.assertTrue(press(l,p,b.offset(SphinxField.BOX))&&surveys(p)==1,"no second survey while the first is carried");})
            .thenIdle(5)
            .thenExecute(()->{
                h.assertTrue(press(l,p,b.offset(SphinxField.survey(wrong[0])))&&press(l,q,b.offset(SphinxField.survey(live[0]))),"each sets a pin");
                h.assertTrue(SphinxBlock.stage(l.getBlockState(b.offset(SphinxField.survey(live[0]))))==1,"the socket shows it");})
            .thenIdle(5)
            .thenExecute(()->{
                lift(l,p,b.offset(SphinxField.gate(wrong[0])));
                h.assertTrue(SphinxField.warned(l,wrong[0])&&!gateOpen(l,b,wrong[0]),"a gate on a newer path catches on its stop");
                h.assertTrue(SphinxBlock.stage(l.getBlockState(b.offset(SphinxField.pin(wrong[0]))))==1,"its stop pin shows");
                h.assertTrue(SphinxField.record(l,p.getUUID()).getInt("First")==2&&SphinxField.record(l,q.getUUID()).getInt("First")==1,"each pin is its reader's own answer");
                h.assertTrue(SphinxField.eyeUp(l,b,wrong[0]),"her eye comes up over the corn behind it");})
            .thenWaitUntil(()->h.assertTrue(!SphinxField.eyeUp(l,b,wrong[0]),"and sinks again"))
            .thenIdle(5)
            .thenExecute(()->{
                stand(q,l,b,x(live[0]),0,SphinxField.GATE_Z+1.5);lift(l,q,b.offset(SphinxField.gate(live[0])));
                h.assertTrue(SphinxField.opened(l)&&gateOpen(l,b,live[0]),"the gate on the old road swings open");
                stand(q,l,b,x(live[0]),0,SphinxField.ROAD_Z0-1.5);})
            .thenWaitUntil(()->h.assertTrue(SphinxField.solved(l)&&SphinxField.jibOpen(l,b),"up the old road onto its strip: the way round to the lane, open for everyone"))
            .thenExecute(()->h.assertTrue(fits(l,b,SphinxField.LANE_X-.5,0,SphinxField.ROAD_Z0-1.5),"the strip meets the lane"))
            .thenIdle(5)
            .thenExecute(()->{
                // The provocation: the reader at the warned gate pulls its pin.
                p.setHealth(p.getMaxHealth());stand(p,l,b,x(wrong[0]),0,SphinxField.GATE_Z+1.5);
                press(l,p,b.offset(SphinxField.pin(wrong[0])));
                h.assertTrue(SphinxField.provoked(l)==wrong[0]&&SphinxField.record(l,p.getUUID()).getBoolean("Forced")&&!SphinxField.record(l,q.getUUID()).getBoolean("Forced"),"the one who pulled the pin owns it");})
            .thenWaitUntil(()->h.assertTrue(SphinxField.shoulderUp(l,b,wrong[0]),"the sweep of a shoulder rises over the corn"))
            .thenWaitUntil(()->h.assertTrue(SphinxField.flattened(l,b,wrong[0]),"the rows flatten toward the gate"))
            .thenWaitUntil(()->h.assertTrue(SphinxField.handDown(l,b,wrong[0])&&gateOpen(l,b,wrong[0])&&SphinxField.eyeUp(l,b,wrong[0]),"her hand across the track, the gate bowed open, her eye over the corn"))
            .thenWaitUntil(()->h.assertTrue(p.getZ()-b.getZ()>SphinxField.DITCH_Z1-.5&&SphinxField.record(l,p.getUUID()).getInt("Swept")==1,"whoever stays in her reach is swept back into the ditch"))
            .thenExecute(()->h.assertTrue(p.getHealth()==p.getMaxHealth(),"she hurts nobody"))
            .thenWaitUntil(()->h.assertTrue(SphinxField.provoked(l)<0&&!SphinxField.handDown(l,b,wrong[0])&&!SphinxField.flattened(l,b,wrong[0])&&!SphinxField.shoulderUp(l,b,wrong[0])&&!gateOpen(l,b,wrong[0]),"with the clearing empty she sinks back into the corn and the gate drops shut"))
            .thenExecute(()->{
                h.assertTrue(SphinxBlock.stage(l.getBlockState(b.offset(SphinxField.pin(wrong[0]))))==0,"the pin back in");
                stand(p,l,b,x(other[0]),0,SphinxField.GATE_Z+1.5);})
            .thenIdle(5)
            .thenExecute(()->lift(l,p,b.offset(SphinxField.gate(other[0]))))
            .thenIdle(5)
            .thenExecute(()->{press(l,p,b.offset(SphinxField.pin(other[0])));stand(p,l,b,x(other[0]),0,SphinxField.GATE_Z-2.5);})
            .thenWaitUntil(()->h.assertTrue(SphinxField.sealed(l,other[0])&&p.getX()-b.getX()>SphinxField.LANE_X-.6,"on past her hand: set down past her on the lane"))
            .thenExecute(()->{
                h.assertTrue(StateRouteBlock.is(l.getBlockState(b.offset(SphinxField.gate(other[0]))),StateRouteBlock.Kind.CORN)&&!SphinxField.handDown(l,b,other[0])&&SphinxField.provoked(l)<0,"the gap closed behind them in standing corn");
                SphinxField.build(l,b);
                h.assertTrue(gateOpen(l,b,live[0])&&SphinxField.jibOpen(l,b)&&SphinxField.sealed(l,other[0])&&StateRouteBlock.is(l.getBlockState(b.offset(SphinxField.gate(other[0]))),StateRouteBlock.Kind.CORN),"built again: the open gate, the open way and the closed gap stay as they were");
                h.assertTrue(SphinxField.soilStage(l,b,wrong[0])==1&&SphinxField.soilStage(l,b,live[0])==2,"and the brushed soil stays bare");
                p.getInventory().clearContent();
                h.assertTrue(press(l,p,b.offset(SphinxField.BOX))&&surveys(p)==1,"a reader who lost theirs finds another in the tin");
                h.assertTrue(WitnessAccount.count(f.data(),p.getUUID())==0,"and nothing counts toward the Witness");})
            .thenSucceed();
    }
}

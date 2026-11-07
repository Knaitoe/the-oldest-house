package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.gametest.*;

/**
 * 0.4.338, the Sphinx's shelf (docs/SPHINX_0_4_338.md), played by two readers: her question as the ledge is reached, from below and then
 * from above; her breath lifting two shelves past their pins while the third holds still, and the question asked again at the grille
 * bringing the next breath sooner; survey pins locked by a refused ladder, her warning, the exposed pin and her eye upright in the gap; the
 * live ladder lowered and the passage behind the fixed shelf opening the way onward for everyone; the pin pulled, the ladder dropping onto
 * a moving shelf, the shelf sliding off her back, her paw on the far wall and her eye lowering to the ledge; a reader in reach swept back
 * over the sill unhurt; the ladder put back and her withdrawing; going out along a dropped ladder sealing it; a fall into the cavern given
 * back in the passage; and the lowered ladder, the open way and the seal kept when the room is built again.
 */
@GameTestHolder(TheOldestHouse.MOD_ID+"_exploration") @PrefixGameTestTemplate(false)
public final class SphinxShelfTests {
    private SphinxShelfTests(){}
    private static LiteraryTests.Fixture active;
    @AfterBatch(batch="sphinx_shelf") public static void done(ServerLevel l){if(active!=null){active.close();active=null;}SphinxShelf.clearAll();}
    private static void hold(LiteraryTests.Fixture f,LabyrinthPlace place){var slot=LabyrinthPlaces.slotBounds(f.origin,place);f.chunks.hold(f.in,new AABB(slot.minX(),slot.minY(),slot.minZ(),slot.maxX()+1,slot.maxY()+1,slot.maxZ()+1));}
    private static boolean press(ServerLevel l,ServerPlayer p,BlockPos at){
        return l.getBlockState(at).useWithoutItem(l,p,new BlockHitResult(at.getCenter(),Direction.SOUTH,at,false)).consumesAction();
    }
    private static void stand(ServerPlayer p,ServerLevel l,BlockPos b,double x,double y,double z,float yaw){p.teleportTo(l,b.getX()+x,b.getY()+y,b.getZ()+z,yaw,0);p.fallDistance=0;}
    private static double x(int i){return SphinxShelf.SHELVES[i]+.5;}

    @GameTest(template="empty",batch="sphinx_shelf",timeoutTicks=2400)
    public static void theShelvesAreWatchedAnsweredCrossedAndForced(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);active=f;var place=SphinxShelf.PLACE;var b=f.base(place);var l=f.in;hold(f,place);
        var p=NativeTestPlayers.survival(h,"shelf_reader");f.players.add(p);
        var q=NativeTestPlayers.survival(h,"shelf_friend");f.players.add(q);
        int[] live={-1},wrong={-1},other={-1};
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the room is resident"))
            .thenExecute(()->{
                h.assertTrue(!place.isVignette()&&WitnessAccount.Story.of(place.id())==null,"a detour in the halls, not a story");
                SphinxShelf.clearAll();SphinxShelf.build(l,b);LabyrinthBuilder.registerDoors(f.data(),place,b);
                live[0]=SphinxShelf.live(l);wrong[0]=(live[0]+1)%3;other[0]=(live[0]+2)%3;
                for(int i=0;i<3;i++){
                    h.assertTrue(SphinxShelf.shelfStage(l,b,i)==0&&SphinxShelf.ladderStage(l,b,i)==0&&!SphinxShelf.spanned(l,b,i),"three settled shelves across the gap, a ladder standing at the edge for each");
                    h.assertTrue(SphinxBlock.is(l.getBlockState(b.offset(SphinxShelf.peg(i))),SphinxBlock.Kind.PEG),"an iron survey pin in the far wall beside each shelf");
                }
                h.assertTrue(!l.getBlockState(b.offset(0,SphinxShelf.OVERHANG,-7)).isAir()&&l.getBlockState(b.offset(0,-8,-11)).isAir(),"a low overhang over the ledge, and the cavern falling away below the gap");
                h.assertTrue(!SphinxShelf.jibOpen(l,b),"the passage's far end is rock");
                stand(p,l,b,x(wrong[0]),0,-6.5,180);stand(q,l,b,.5,0,-2.5,180);})
            .thenWaitUntil(()->h.assertTrue(SphinxShelf.record(l,p.getUUID()).getBoolean("Heard"),"her question as the ledge is reached"))
            .thenExecute(()->h.assertTrue(!SphinxShelf.record(l,q.getUUID()).getBoolean("Heard"),"the friend in the passage has not heard it"))
            .thenWaitUntil(()->h.assertTrue(SphinxShelf.shelfStage(l,b,wrong[0])==1&&SphinxShelf.shelfStage(l,b,other[0])==1&&SphinxShelf.shelfStage(l,b,live[0])==0,"she breathes: two shelves lift past their pins, and the third holds still"))
            .thenWaitUntil(()->h.assertTrue(SphinxShelf.shelfStage(l,b,wrong[0])==0&&SphinxShelf.shelfStage(l,b,other[0])==0,"and they settle again"))
            .thenExecute(()->{
                // The question asked again at the grille brings her next breath sooner.
                h.assertTrue(press(l,q,b.offset(SphinxShelf.GRILLE_AT))&&SphinxShelf.record(l,q.getUUID()).getBoolean("Heard"),"the grille asks it again");})
            .thenWaitUntil(()->h.assertTrue(SphinxShelf.shelfStage(l,b,wrong[0])==1,"and she breathes again, sooner"))
            .thenExecute(()->stand(q,l,b,x(live[0]),0,-6.5,180))
            .thenIdle(5)
            .thenExecute(()->{
                h.assertTrue(press(l,p,b.offset(SphinxShelf.survey(wrong[0])))&&press(l,q,b.offset(SphinxShelf.survey(live[0]))),"each sets a pin");
                h.assertTrue(SphinxBlock.stage(l.getBlockState(b.offset(SphinxShelf.survey(live[0]))))==1,"the socket shows it");})
            .thenIdle(5)
            .thenExecute(()->{
                press(l,p,b.offset(SphinxShelf.ladder(wrong[0])));
                h.assertTrue(SphinxShelf.warned(l,wrong[0])&&SphinxShelf.ladderStage(l,b,wrong[0])==0&&!SphinxShelf.spanned(l,b,wrong[0]),"a ladder toward a moving shelf tips and catches, and she warns it off");
                h.assertTrue(SphinxBlock.stage(l.getBlockState(b.offset(SphinxShelf.pin(wrong[0]))))==1,"its stop pin shows");
                h.assertTrue(SphinxShelf.record(l,p.getUUID()).getInt("First")==2&&SphinxShelf.record(l,q.getUUID()).getInt("First")==1,"each pin is its reader's own answer");
                h.assertTrue(SphinxShelf.eyeY(l,b,wrong[0])==3,"her eye opens upright in the gap before that shelf, her face on its side");})
            .thenWaitUntil(()->h.assertTrue(SphinxShelf.eyeY(l,b,wrong[0])==Integer.MIN_VALUE,"and goes again"))
            .thenIdle(5)
            .thenExecute(()->{
                press(l,q,b.offset(SphinxShelf.ladder(live[0])));
                h.assertTrue(SphinxShelf.ladderStage(l,b,live[0])==1&&SphinxShelf.spanned(l,b,live[0]),"the ladder lowered to the fixed shelf lies across the gap");
                stand(q,l,b,x(live[0]),0,-11.5,180);})
            .thenExecute(()->{var under=b.offset(SphinxShelf.SHELVES[live[0]],-1,-12);h.assertTrue(l.getBlockState(under).getCollisionShape(l,under).max(net.minecraft.core.Direction.Axis.Y)>=.99,"the ladder is something to walk on, out over the gap");})
            .thenExecute(()->stand(q,l,b,x(live[0]),0,SphinxShelf.WALL_Z-.5,180))
            .thenWaitUntil(()->h.assertTrue(SphinxShelf.solved(l)&&SphinxShelf.jibOpen(l,b),"behind the fixed shelf a passage round to the way onward, open for everyone"))
            .thenIdle(5)
            .thenExecute(()->{
                // The provocation: the reader still on the ledge pulls the warned ladder's pin.
                p.setHealth(p.getMaxHealth());stand(p,l,b,x(wrong[0]),0,-6.5,180);
                press(l,p,b.offset(SphinxShelf.pin(wrong[0])));
                h.assertTrue(SphinxShelf.provoked(l)==wrong[0]&&SphinxShelf.record(l,p.getUUID()).getBoolean("Forced")&&!SphinxShelf.record(l,q.getUUID()).getBoolean("Forced"),"the one who pulled the pin owns it");
                h.assertTrue(SphinxShelf.ladderStage(l,b,wrong[0])==2&&SphinxShelf.spanned(l,b,wrong[0]),"the ladder drops across onto the moving shelf");})
            .thenWaitUntil(()->h.assertTrue(SphinxShelf.shelfAt(l,b,wrong[0],(wrong[0]==2?-1:1)),"the shelf slides sideways off what it was resting on"))
            .thenWaitUntil(()->h.assertTrue(SphinxBlock.is(l.getBlockState(b.offset(wrong[0]==0?-4:3,1,SphinxShelf.SHELF_Z1)),SphinxBlock.Kind.FINGERS),"her paw settles against the far wall"))
            .thenWaitUntil(()->h.assertTrue(SphinxShelf.eyeY(l,b,wrong[0])==1,"her eye lowers until it is level with the ledge"))
            .thenWaitUntil(()->h.assertTrue(p.getZ()-b.getZ()>SphinxShelf.SILL_Z&&SphinxShelf.record(l,p.getUUID()).getInt("Swept")==1,"whoever stays in her reach is swept back over the sill"))
            .thenExecute(()->{
                h.assertTrue(p.getHealth()==p.getMaxHealth(),"she hurts nobody");
                // Back onto the ledge, quickly, to give her the ladder back.
                stand(p,l,b,x(wrong[0]),0,-7.5,180);})
            .thenIdle(5)
            .thenExecute(()->press(l,p,b.offset(SphinxShelf.ladder(wrong[0]))))
            .thenWaitUntil(()->h.assertTrue(SphinxShelf.provoked(l)<0&&SphinxShelf.ladderStage(l,b,wrong[0])==0&&!SphinxShelf.spanned(l,b,wrong[0])&&SphinxShelf.shelfAt(l,b,wrong[0],0)&&SphinxShelf.eyeY(l,b,wrong[0])==Integer.MIN_VALUE,"the ladder put back, she goes back under"))
            .thenExecute(()->{
                h.assertTrue(SphinxShelf.record(l,p.getUUID()).getBoolean("PutBack")&&SphinxBlock.stage(l.getBlockState(b.offset(SphinxShelf.pin(wrong[0]))))==0,"recorded, and the pin back in");
                stand(p,l,b,x(other[0]),0,-6.5,180);})
            .thenIdle(5)
            .thenExecute(()->press(l,p,b.offset(SphinxShelf.ladder(other[0]))))
            .thenIdle(5)
            .thenExecute(()->{press(l,p,b.offset(SphinxShelf.pin(other[0])));stand(p,l,b,x(other[0]),0,-10.5,180);})
            .thenWaitUntil(()->h.assertTrue(SphinxShelf.sealed(l,other[0])&&p.getX()-b.getX()>SphinxShelf.ONWARD_X-.6,"out along the dropped ladder, set down past her by her hand"))
            .thenExecute(()->{
                h.assertTrue(l.getBlockState(b.offset(SphinxShelf.ladder(other[0]))).is(PuzzleRegistry.sphinxMaterial("rock_seam"))&&!SphinxShelf.spanned(l,b,other[0]),"the ladder gone under it, closed rock where it stood");
                // A fall into the cavern is given back in the passage.
                stand(p,l,b,-3.5,-8,-11.5,180);})
            .thenWaitUntil(()->h.assertTrue(p.getZ()-b.getZ()>SphinxShelf.SILL_Z&&p.getY()-b.getY()>-.5,"a fall into the dark is given back in the passage"))
            .thenExecute(()->{
                SphinxShelf.build(l,b);
                h.assertTrue(SphinxShelf.ladderStage(l,b,live[0])==1&&SphinxShelf.spanned(l,b,live[0])&&SphinxShelf.jibOpen(l,b)&&SphinxShelf.sealed(l,other[0]),"built again: the lowered ladder, the open way and the closed rock all stay as they were");
                h.assertTrue(WitnessAccount.count(f.data(),p.getUUID())==0,"and nothing counts toward the Witness");})
            .thenSucceed();
    }
}

package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * 0.4.207, the God of the Pitt, played in the deeper passages with real readers: dealt only after the squeeze is retraced;
 * the seams cut in the draft and the fist-sized hole; the still air bringing the slab down on a moving reader and the draft
 * letting it up; the line to the round room and the collapse that follows the reader back along it; Brad gone and the chimney
 * changed; the footprints over the bridges, the hatch, the moor and the field journal; and a second reader on the belay hauling a
 * pinned partner out.
 */
@GameTestHolder(TheOldestHouse.MOD_ID+"_literary") @PrefixGameTestTemplate(false)
public final class PittTests {
    private PittTests(){}
    private static final LabyrinthPlace P=LabyrinthPlace.PITT;
    private static LiteraryTests.Fixture active;
    private static LiteraryTests.Fixture fixture(GameTestHelper h){if(active!=null)active.close();active=new LiteraryTests.Fixture(h);PittGame.clearAll();return active;}
    private static void close(){if(active!=null){active.close();active=null;}PittGame.clearAll();}
    private static net.minecraft.nbt.CompoundTag set(LiteraryTests.Fixture f){return PittGame.set(f.in.getServer());}
    private static int count(ServerPlayer p,Item item){int n=0;var inv=p.getInventory();for(int i=0;i<inv.getContainerSize();i++)if(inv.getItem(i).is(item))n+=inv.getItem(i).getCount();return n;}
    private static void hold(ServerPlayer p,Item item){var inv=p.getInventory();for(int i=0;i<inv.getContainerSize();i++)if(inv.getItem(i).is(item)){var s=inv.getItem(i).copy();inv.setItem(i,ItemStack.EMPTY);p.setItemInHand(InteractionHand.MAIN_HAND,s);return;}}
    private static void hold(LiteraryTests.Fixture f){var b=f.base(P);f.chunks.hold(f.out,new AABB(b.getX()-25,b.getY()-40,b.getZ()-65,b.getX()+26,b.getY()+17,b.getZ()+3));}
    /** A right-click from where the reader stands, without the fixture's helper moving them to the block first. */
    private static void clickFrom(LiteraryTests.Fixture f,ServerPlayer p,BlockPos rel){var at=f.base(P).offset(rel);NeoForge.EVENT_BUS.post(new PlayerInteractEvent.RightClickBlock(p,InteractionHand.MAIN_HAND,at,new BlockHitResult(at.getCenter(),Direction.DOWN,at,false)));}
    /** The reader has already retraced Ted the Caver's squeeze, and has done this many visits of the deeper passages. */
    private static void caver(LiteraryTests.Fixture f,ServerPlayer p){WitnessAccount.resolve(p,WitnessAccount.Story.TED_CAVER,"retraced_the_squeeze");}
    private static void visit(LiteraryTests.Fixture f,ServerPlayer p,int done){var own=f.own(p,P);own.putInt("Visit",done);own.putInt("Beat",done);LiteraryVignettes.save(f.data(),p.getUUID(),P,own);f.arrive(p,P);}
    private static int slab(LiteraryTests.Fixture f,int z){var s=f.out.getBlockState(f.base(P).offset(0,1,z));return PittBlock.is(s,PittBlock.Kind.SLAB)?s.getValue(PittBlock.STAGE):-1;}

    @GameTest(template="empty",batch="pitt_deal",timeoutTicks=400)
    public static void onlyAReaderWhoRetracedTheSqueezeIsDealtTheDeeperPassages(GameTestHelper h){
        var f=fixture(h);var p=f.player("pitt_deal",P);var l=f.out;var b=f.base(P);
        h.assertTrue(LiteraryRooms.isLiterary(P)&&LiteraryRooms.outside(P)&&P.kind()==LabyrinthPlace.Kind.MULTI_VISIT&&WitnessAccount.Story.of(P.id())==null,"an outdoor literary place over four visits, with no Witness story of its own");
        h.assertTrue(WitnessAccount.Story.values().length==61&&WitnessAccount.REQUIRED==46,"the Witness pool is unchanged");
        h.assertTrue(b.getY()==PittCave.BASE_Y&&LiteraryVignettes.inside(p,P),"the passages stand at their own height and a reader in them is found there");
        var fresh=java.util.UUID.randomUUID();h.assertTrue(!LiteraryVignettes.canDeal(f.data(),fresh,P),"a reader who has not retraced the squeeze is never dealt them");
        caver(f,p);h.assertTrue(LiteraryVignettes.canDeal(f.data(),p.getUUID(),P),"one who has is");
        h.assertTrue(LiteraryRooms.source(P).equals(PittCave.NOTE)&&LiteraryRooms.ending(P).equals(PittCave.JOURNAL),"Brad's note is the source and the field journal the account");
        h.assertTrue(PittBlock.is(l.getBlockState(b.offset(PittCave.NOTE)),PittBlock.Kind.NOTE)&&PittBlock.is(l.getBlockState(b.offset(PittCave.BRAD)),PittBlock.Kind.BRAD)&&PittBlock.is(l.getBlockState(b.offset(PittCave.HOLE)),PittBlock.Kind.HOLE),"the note, Brad at the belay and the fist-sized hole are there");
        for(int z:PittCave.SEAMS)h.assertTrue(l.getBlockState(b.offset(0,0,z)).getValue(PittBlock.STAGE)==0,"the four seams are shut on the first visit");
        h.assertTrue(PittClimbBlock.is(l.getBlockState(b.offset(0,3,-8)),PittClimbBlock.Kind.ROPE)&&l.getBlockState(b.offset(PittCave.HATCH)).is(Blocks.IRON_TRAPDOOR),"Brad's rope hangs down the sinkhole's throat, and the hatch is in the moor");
        f.source(p,P);h.assertTrue(f.own(p,P).getBoolean("Read_Source"),"the note is read");
        h.succeed();
    }
    @AfterBatch(batch="pitt_deal") public static void dealDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="pitt_seams",timeoutTicks=600)
    public static void theSeamsAreCutOnlyInTheDraftAndTheFistHoleEndsTheFirstVisit(GameTestHelper h){
        var f=fixture(h);var p=f.player("pitt_seams",P);var b=f.base(P);
        h.startSequence().thenExecute(()->{hold(f);caver(f,p);}).thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the passages are resident"))
            .thenExecute(()->{PittGame.TEST_FAST=true;
                f.click(p,P,PittCave.CRATE);h.assertTrue(count(p,PittRegistry.HAMMER.get())==1&&count(p,PittRegistry.LAMP.get())==1&&count(p,PittRegistry.BAROMETER.get())==1,"Brad's crate gives the hammer, the lamp and the barometer");
                hold(p,PittRegistry.HAMMER.get());var seam=new BlockPos(0,0,PittCave.SEAMS[0]);
                PittGame.force(p.server,PittGame.P_WARN);f.click(p,P,seam);h.assertTrue(f.out.getBlockState(b.offset(seam)).getValue(PittBlock.STAGE)==0&&f.own(p,P).getInt("Strokes207")==0,"while the draft is dying the hammer does nothing");
                PittGame.force(p.server,PittGame.P_CRUSH);int before=slab(f,PittCave.SEAMS[0]+2);f.click(p,P,seam);
                h.assertTrue(slab(f,PittCave.SEAMS[0]+2)>=before+3&&set(f).getBoolean("Awake"),"in the still air the hammer brings the ceiling down three stages and wakes what is in the fissure");
                PittGame.force(p.server,PittGame.P_SAFE);
                for(int z:PittCave.SEAMS)for(int i=0;i<PittGame.STROKES*4;i++)f.click(p,P,new BlockPos(0,0,z));
                for(int z:PittCave.SEAMS)h.assertTrue(f.out.getBlockState(b.offset(0,0,z)).getValue(PittBlock.STAGE)==4&&f.out.getBlockState(b.offset(0,0,z)).getCollisionShape(f.out,b.offset(0,0,z)).isEmpty(),"in the draft every seam is cut through, and is an opening");
                h.assertTrue(f.own(p,P).getInt("Seams207")==4,"the four seams are the reader's own work");
                f.click(p,P,PittCave.HOLE);h.assertTrue(f.own(p,P).getInt("Beat")==1&&f.own(p,P).getBoolean("Hole207"),"the fist-sized hole finishes the first visit");})
            .thenSucceed();
    }
    @AfterBatch(batch="pitt_seams") public static void seamsDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="pitt_crush",timeoutTicks=900)
    public static void inTheStillAirMovingBringsTheSlabDownAndTheDraftLetsItUp(GameTestHelper h){
        var f=fixture(h);var p=f.player("pitt_crush",P);var b=f.base(P);int z=-22;int[] t={0};
        h.startSequence().thenExecute(()->{hold(f);caver(f,p);visit(f,p,1);}).thenWaitUntil(()->h.assertTrue(f.chunks.ready()&&set(f).getInt("RunVisit")==2,"the second visit, the seams cut"))
            .thenExecute(()->{f.at(p,P,.5,0,z+.5);PittGame.force(p.server,PittGame.P_CRUSH);})
            .thenWaitUntil(()->{t[0]++;f.at(p,P,.5+(t[0]%2==0?.3:0),0,z+.5);h.assertTrue(PittGame.pinned(p),"moving in the still air steps the slab down a stage a tick until it pins the reader");})
            .thenExecute(()->{h.assertTrue(slab(f,z)==PittGame.PINNED&&f.own(p,P).getInt("Pinned207")==1,"the slab over them is at its last stage");
                var at=p.position();p.teleportTo(f.out,at.x,at.y,at.z+2,0,0);})
            .thenIdle(2)
            .thenExecute(()->{h.assertTrue(Math.abs(p.getZ()-(b.getZ()+z+.5))<.2,"pinned, the reader cannot move from under it");
                PittGame.force(p.server,PittGame.P_SAFE);})
            .thenWaitUntil(()->h.assertTrue(!PittGame.pinned(p)&&slab(f,z)<PittGame.PINNED,"when the draft comes back the slab lifts and lets them go"))
            .thenExecute(()->{p.setHealth(2.5F);f.at(p,P,.5,0,z-1+.5);PittGame.force(p.server,PittGame.P_CRUSH);t[0]=0;})
            .thenWaitUntil(()->{t[0]++;var r=p.position().subtract(b.getX(),b.getY(),b.getZ());if(r.z<-15)f.at(p,P,.5+(t[0]%2==0?.3:0),0,z-1+.5);
                h.assertTrue(f.own(p,P).getInt("Blackouts207")==1,"pinned to their last heart, the reader blacks out");})
            .thenExecute(()->{var r=p.position().subtract(b.getX(),b.getY(),b.getZ());h.assertTrue(r.z>-15&&r.z<0&&p.getHealth()>=2F,"and wakes on the staging ledge, never killed");})
            .thenSucceed();
    }
    @AfterBatch(batch="pitt_crush") public static void crushDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="pitt_line",timeoutTicks=1200)
    public static void theLineToTheRoundRoomAndTheCollapseBehindItEndTheSecondVisit(GameTestHelper h){
        var f=fixture(h);var p=f.player("pitt_line",P);var b=f.base(P);
        h.startSequence().thenExecute(()->{hold(f);caver(f,p);visit(f,p,1);}).thenWaitUntil(()->h.assertTrue(f.chunks.ready()&&set(f).getInt("RunVisit")==2,"the second visit"))
            .thenExecute(()->{
                h.assertTrue(f.out.getBlockState(b.offset(PittCave.HOLE)).isAir(),"the fist-sized hole is broken through");
                f.click(p,P,PittCave.ANCHORS[2]);h.assertTrue(f.own(p,P).getInt("Anchors207")==0,"the line will not reach a bolt before the one before it is clipped");
                f.click(p,P,new BlockPos(-3,-3,-45));
                for(var a:PittCave.ANCHORS)f.click(p,P,a);h.assertTrue(f.own(p,P).getInt("Anchors207")==(1<<PittCave.ANCHORS.length)-1,"every bolt clipped in order, from the belay to the round room");
                f.click(p,P,new BlockPos(-3,-3,-45));var t=set(f);
                h.assertTrue(t.getInt("Collapse")>0&&f.own(p,P).getBoolean("Grooves207"),"looking at the stone's track brings the room down");
                f.at(p,P,.5,-2,-44.5);})
            .thenWaitUntil(()->h.assertTrue(set(f).getInt("Front")>-41,"after a breath the collapse starts back along the passage"))
            .thenExecute(()->h.assertTrue(f.own(p,P).getInt("Blackouts207")==1&&p.getZ()>b.getZ()-15,"a reader still in the round room is cut off, and wakes on the ledge"))
            .thenWaitUntil(()->h.assertTrue(set(f).getInt("Collapse")==0&&f.own(p,P).getInt("Beat")==1,"with nobody left in it the passage settles, and the visit is still to do"))
            .thenExecute(()->{f.click(p,P,new BlockPos(-3,-3,-45));f.at(p,P,.5,0,-36.5);})
            .thenWaitUntil(()->h.assertTrue(set(f).getInt("Collapse")>PittGame.COLLAPSE_DELAY,"the collapse comes"))
            .thenExecute(()->f.at(p,P,.5,0,-10.5))
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getInt("Beat")==2&&f.own(p,P).getBoolean("Escaped207"),"back on the ledge ahead of it, along the line, the second visit is done"))
            .thenSucceed();
    }
    @AfterBatch(batch="pitt_line") public static void lineDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="pitt_shift",timeoutTicks=600)
    public static void onTheThirdVisitBradIsGoneTheStoneIsOutAndTheChimneyHasChanged(GameTestHelper h){
        var f=fixture(h);var p=f.player("pitt_shift",P);var b=f.base(P);var l=f.out;
        h.startSequence().thenExecute(()->{hold(f);caver(f,p);visit(f,p,2);}).thenWaitUntil(()->h.assertTrue(f.chunks.ready()&&set(f).getInt("RunVisit")==3,"the third visit"))
            .thenExecute(()->{
                h.assertTrue(l.getBlockState(b.offset(PittCave.BRAD)).isAir()&&l.getBlockState(b.offset(PittCave.BELAY)).getValue(PittBlock.STAGE)==1,"Brad is gone and his belay hangs slack");
                h.assertTrue(PittBlock.is(l.getBlockState(b.offset(PittCave.WHEEL_OUT_X,PittCave.ROOM_Y,PittCave.WHEEL_Z)),PittBlock.Kind.WHEEL_LOW)&&l.getBlockState(b.offset(PittCave.WHEEL_X,PittCave.ROOM_Y,PittCave.WHEEL_Z)).isAir(),"the round stone has been pushed out along its grooves, and there is a way behind it");
                h.assertTrue(PittBlock.is(l.getBlockState(b.offset(PittCave.MOUTH_A)),PittBlock.Kind.RUBBLE)&&l.getBlockState(b.offset(PittCave.MOUTH_B)).isAir(),"the chimney the reader was shown is choked, and the other is open");
                h.assertTrue(PittBlock.is(l.getBlockState(b.offset(PittCave.HELMET)),PittBlock.Kind.HELMET)&&PittBlock.is(l.getBlockState(b.offset(PittCave.CUT)),PittBlock.Kind.CUT),"Brad's helmet is on the floor and his line goes into a crack");
                f.click(p,P,PittCave.HELMET);f.click(p,P,PittCave.CUT);
                f.at(p,P,PittCave.LANDING.getX()+.5,PittCave.LANDING.getY(),PittCave.LANDING.getZ()+.5);})
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getInt("Beat")==3&&f.own(p,P).getBoolean("Landing207"),"reaching the ledge at the chimney's foot finishes the third visit"))
            .thenExecute(()->{var o=f.own(p,P);h.assertTrue(o.getBoolean("Helmet207")&&o.getBoolean("Cut207"),"what the reader looked at is kept for the journal");
                f.at(p,P,PittCave.LADDER.getX()+.5,PittCave.MOOR_Y-1,PittCave.LADDER.getZ()+.5);clickFrom(f,p,PittCave.HATCH);
                h.assertTrue(!l.getBlockState(b.offset(PittCave.HATCH)).getValue(TrapDoorBlock.OPEN),"the hatch is rusted shut before the last visit");})
            .thenSucceed();
    }
    @AfterBatch(batch="pitt_shift") public static void shiftDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="pitt_ledge",timeoutTicks=800)
    public static void theFootprintsCrossTheBridgesAndTheLadderClimbsOutToTheJournal(GameTestHelper h){
        var f=fixture(h);var p=f.player("pitt_ledge",P);var b=f.base(P);var l=f.out;
        h.startSequence().thenExecute(()->{hold(f);caver(f,p);f.source(p,P);visit(f,p,3);}).thenWaitUntil(()->h.assertTrue(f.chunks.ready()&&set(f).getInt("RunVisit")==4,"the fourth visit"))
            .thenExecute(()->{
                h.assertTrue(PittGame.phase(set(f))==PittGame.P_STILL,"the draft is steady");
                var bridge=b.offset(PittCave.PATH.get(3));h.assertTrue(PittBlock.is(l.getBlockState(bridge),PittBlock.Kind.BRIDGE)&&!l.getBlockState(bridge).getCollisionShape(l,bridge).isEmpty()&&l.getBlockState(bridge).getRenderShape()==net.minecraft.world.level.block.RenderShape.INVISIBLE,"the bridges are solid and never drawn");
                f.at(p,P,4.5,PittCave.LEDGE_Y-10,-58.5);})
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getInt("Falls207")==1,"falling off the ledge"))
            .thenExecute(()->{var r=p.position().subtract(b.getX(),b.getY(),b.getZ());h.assertTrue(PittCave.onLanding(r),"puts the reader back on its landing, unhurt");
                var c=PittCave.PATH.get(4);f.at(p,P,c.getX()+.5,c.getY()+1,c.getZ()+.5);})
            .thenIdle(6)
            .thenExecute(()->{h.assertTrue(f.own(p,P).getInt("Prints207")>=5,"the reader walks in their own footprints over the bridges");
                f.at(p,P,PittCave.LADDER.getX()+.5,PittCave.MOOR_Y-1,PittCave.LADDER.getZ()+.5);clickFrom(f,p,PittCave.HATCH);
                h.assertTrue(l.getBlockState(b.offset(PittCave.HATCH)).getValue(TrapDoorBlock.OPEN)&&f.own(p,P).getBoolean("Hatch207"),"from below, on the last visit, the hatch gives");
                f.at(p,P,PittCave.JOURNAL.getX()+.5,PittCave.MOOR_Y+1,PittCave.JOURNAL.getZ()+2.5);})
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getBoolean("Ready")&&"climbed_out".equals(f.own(p,P).getString("Outcome")),"out on the moor the account is ready"))
            .thenExecute(()->{f.read(p,P,PittCave.JOURNAL);var o=f.own(p,P);
                h.assertTrue(o.getBoolean("Completed")&&!LiteraryVignettes.canDeal(f.data(),p.getUUID(),P),"reading the field journal ends the deeper passages for good");
                var text=PittTexts.accountText(p.getGameProfile().getName(),o);h.assertTrue(text.contains("LOG #4")&&text.contains("fell once")&&text.contains("B. isn't coming up."),"the journal's last log is the ledge and what the reader did on it");})
            .thenSucceed();
    }
    @AfterBatch(batch="pitt_ledge") public static void ledgeDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="pitt_belay",timeoutTicks=900)
    public static void aSecondReaderTakesTheBelayAndHaulsAPinnedPartnerOut(GameTestHelper h){
        var f=fixture(h);var p=f.player("pitt_belay_in",P);var b=f.base(P);int z=-26;int[] t={0};ServerPlayer[] second=new ServerPlayer[1];
        h.startSequence().thenExecute(()->{hold(f);caver(f,p);visit(f,p,1);second[0]=f.player("pitt_belay_on",P);caver(f,second[0]);visit(f,second[0],1);})
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready()&&set(f).getInt("RunVisit")==2&&f.own(second[0],P).getInt("Visit")==2,"two readers on the second visit"))
            .thenExecute(()->{var q=second[0];f.click(q,P,PittCave.BELAY);h.assertTrue(set(f).hasUUID("Belayer")&&f.out.getBlockState(b.offset(PittCave.BRAD)).isAir()&&f.own(q,P).getBoolean("HeldBelay207"),"the second reader takes the belay from Brad, who goes up for carbide");
                f.at(q,P,PittCave.BELAY.getX()+.5,0,PittCave.BELAY.getZ()+2.5);f.at(p,P,.5,0,z+.5);PittGame.force(p.server,PittGame.P_CRUSH);})
            .thenWaitUntil(()->{t[0]++;f.at(p,P,.5+(t[0]%2==0?.3:0),0,z+.5);h.assertTrue(PittGame.pinned(p),"the reader in the Tomb moves in the still air and is pinned");})
            .thenExecute(()->{var q=second[0];f.click(q,P,PittCave.BELAY);
                h.assertTrue(!PittGame.pinned(p)&&p.getZ()>b.getZ()+z+1&&slab(f,z)==0,"the one on the belay hauls them out from under it");
                h.assertTrue(f.own(p,P).getString("HauledBy207").equals(q.getGameProfile().getName())&&f.own(q,P).getString("Hauled207").contains(p.getGameProfile().getName()),"both journals will say so");})
            .thenSucceed();
    }
    @AfterBatch(batch="pitt_belay") public static void belayDone(ServerLevel l){close();}
}

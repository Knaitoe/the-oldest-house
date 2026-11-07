package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;

/**
 * 0.4.343, the piece of the Dream Stela against loose fill (docs/STELA_PASSAGE_0_4_343.md). Four readers, each at a buried passage running
 * east: a standing place, a run of sand and gravel with more sand over it, and a one-high pocket behind, roofed in stone.
 * <ul>
 * <li>A patient stone parts the fill quietly; the sand over it holds; the reader crawls in; a passage due to close waits while they are
 * inside and settles once they are out, every block back as it was. Pressed again where the pocket is farther in than it reaches, or
 * where stone lies behind, nothing moves.</li>
 * <li>A hostile stone opens at once, holds less time than a patient one, and brings a creature nearby toward it.</li>
 * <li>A dismissive stone must be held: a crack, solid to a body, comes first; let go, it closes; held on, it opens.</li>
 * <li>One passage to a stone at a time; nobody presses another's stone; and an open passage is saved and read back.</li>
 * </ul>
 */
@GameTestHolder(TheOldestHouse.MOD_ID+"_exploration") @PrefixGameTestTemplate(false)
public final class StelaPassageTests {
    private StelaPassageTests(){}
    private static final List<ServerPlayer> PLAYERS=new ArrayList<>();
    @AfterBatch(batch="stela_passage") public static void done(ServerLevel l){for(var p:PLAYERS)NativeTestPlayers.remove(p);PLAYERS.clear();}

    /** A buried passage running east from {@code b}: room to stand at b, the fill at x 1..n with sand over it, then a pocket (or stone) behind. */
    private static void buried(ServerLevel l,BlockPos b,BlockState[] fill,boolean pocket){
        for(int x=-3;x<=fill.length+3;x++)for(int z=-2;z<=2;z++)for(int y=-1;y<=3;y++)l.setBlock(b.offset(x,y,z),Blocks.STONE.defaultBlockState(),Block.UPDATE_CLIENTS);
        for(int x=-3;x<=0;x++)for(int z=-1;z<=1;z++)for(int y=0;y<=2;y++)l.setBlock(b.offset(x,y,z),Blocks.AIR.defaultBlockState(),Block.UPDATE_CLIENTS);
        for(int i=0;i<fill.length;i++){l.setBlock(b.offset(1+i,0,0),fill[i],Block.UPDATE_CLIENTS);l.setBlock(b.offset(1+i,1,0),Blocks.SAND.defaultBlockState(),Block.UPDATE_CLIENTS);}
        if(pocket)for(int x=fill.length+1;x<=fill.length+2;x++)l.setBlock(b.offset(x,0,0),Blocks.AIR.defaultBlockState(),Block.UPDATE_CLIENTS);
    }
    private static BlockState[] run(int n){var out=new BlockState[n];for(int i=0;i<n;i++)out[i]=(i==2?Blocks.GRAVEL:Blocks.SAND).defaultBlockState();return out;}
    private static void face(ServerPlayer p,ServerLevel l,BlockPos b){p.teleportTo(l,b.getX()+.5,b.getY(),b.getZ()+.5,-90,0);p.setDeltaMovement(Vec3.ZERO);}
    private static void hold(ServerPlayer p,String opening,int reach){p.setItemInHand(InteractionHand.MAIN_HAND,DreamStela.make(p.getUUID(),reach,opening,"early",false));}
    private static boolean parted(ServerLevel l,BlockPos b,int n,int phase){for(int i=1;i<=n;i++){var s=l.getBlockState(b.offset(i,0,0));if(!StelaFillBlock.is(s)||s.getValue(StelaFillBlock.PHASE)!=phase)return false;}return true;}
    private static boolean asBuilt(ServerLevel l,BlockPos b,BlockState[] fill){for(int i=0;i<fill.length;i++)if(l.getBlockState(b.offset(1+i,0,0))!=fill[i]||!l.getBlockState(b.offset(1+i,1,0)).is(Blocks.SAND))return false;return true;}

    @GameTest(template="empty",batch="stela_passage",timeoutTicks=2400)
    public static void theStoneHoldsLooseFillAsideLongEnoughToCrawlThrough(GameTestHelper h){
        var l=h.getLevel();var s=l.getServer();
        var a=NativeTestPlayers.survival(h,"stela_patient");var b=NativeTestPlayers.survival(h,"stela_hostile");var e=NativeTestPlayers.survival(h,"stela_dismissive");var c=NativeTestPlayers.survival(h,"stela_stranger");
        PLAYERS.addAll(List.of(a,b,e,c));
        var o=h.absolutePos(new BlockPos(4,2,4));
        BlockPos pa=o,pb=o.offset(0,0,8),pe=o.offset(0,0,16),far=o.offset(0,0,24),hard=o.offset(0,0,32);
        var fill=run(4);var husk=new Husk[1];long[] opened=new long[1];
        h.startSequence()
            .thenExecute(()->{
                buried(l,pa,fill,true);buried(l,pb,fill,true);buried(l,pe,fill,true);buried(l,far,run(6),true);buried(l,hard,run(2),false);
                // Patient: the fill parts quietly, all four blocks of it; the sand above holds.
                face(a,l,pa);hold(a,"patient",DreamStela.MEDIUM);
                h.assertTrue(StelaPassage.press(a,a.getMainHandItem(),pa.offset(1,0,0),InteractionHand.MAIN_HAND).consumesAction(),"the stone presses against the sand");
                h.assertTrue(parted(l,pa,4,StelaFillBlock.OPEN),"four blocks of fill held aside as a passage");
                opened[0]=l.getGameTime();
                long patient=StelaPassage.closeAt(s,a.getUUID())-l.getGameTime();
                h.assertTrue(patient==StelaPassage.hold("patient",4),"held open the patient length: "+patient);
                // One passage to a stone at a time.
                face(a,l,far);StelaPassage.press(a,a.getMainHandItem(),far.offset(1,0,0),InteractionHand.MAIN_HAND);
                h.assertTrue(asBuilt(l,far,run(6))&&StelaPassage.openFor(s,a.getUUID()).size()==4,"while it holds one way open the stone parts nothing else");
                // Nobody presses another's stone.
                face(c,l,pb);c.setItemInHand(InteractionHand.MAIN_HAND,a.getMainHandItem().copy());
                StelaPassage.press(c,c.getMainHandItem(),pb.offset(1,0,0),InteractionHand.MAIN_HAND);
                h.assertTrue(asBuilt(l,pb,fill)&&StelaPassage.openFor(s,c.getUUID())==null,"a stone pressed by someone it is not theirs to press moves nothing");
                // Hostile: at once, holding less time, and a creature nearby hears it.
                var hk=EntityType.HUSK.create(l);hk.moveTo(pb.getX()-6.5,pb.getY(),pb.getZ()+.5,90,0);hk.setPersistenceRequired();l.addFreshEntity(hk);husk[0]=hk;
                face(b,l,pb);hold(b,"hostile",DreamStela.DEEP);StelaPassage.press(b,b.getMainHandItem(),pb.offset(1,0,0),InteractionHand.MAIN_HAND);
                h.assertTrue(parted(l,pb,4,StelaFillBlock.OPEN),"the hostile stone opens at once");
                h.assertTrue(StelaPassage.closeAt(s,b.getUUID())-l.getGameTime()<patient,"and holds less time than the patient one");
                h.assertTrue(hk.getTarget()==b||hk.getNavigation().isInProgress(),"the crack carries to the creature nearby");
                // Dismissive: pressed, nothing yet; held, a crack, solid to a body; let go, it closes.
                face(e,l,pe);hold(e,"dismissive",DreamStela.SHORT+2);StelaPassage.press(e,e.getMainHandItem(),pe.offset(1,0,0),InteractionHand.MAIN_HAND);
                h.assertTrue(asBuilt(l,pe,fill),"a dismissive stone opens nothing until it is held there");
                StelaPassage.holding(e,25);
                h.assertTrue(parted(l,pe,4,StelaFillBlock.CRACK)&&!l.getBlockState(pe.offset(1,0,0)).getCollisionShape(l,pe.offset(1,0,0)).isEmpty(),"a narrow crack first, solid to a body");
                StelaPassage.released(e);
                h.assertTrue(asBuilt(l,pe,fill)&&StelaPassage.openFor(s,e.getUUID())==null,"let go before it opens, and the crack closes");
                // Held on, it opens.
                StelaPassage.press(e,e.getMainHandItem(),pe.offset(1,0,0),InteractionHand.MAIN_HAND);StelaPassage.holding(e,20);StelaPassage.holding(e,70);
                h.assertTrue(parted(l,pe,4,StelaFillBlock.OPEN),"held on, it opens");
                // An open passage is saved and read back.
                var saved=StelaPassage.Store.get(s).save(new CompoundTag(),s.registryAccess());var back=StelaPassage.Store.load(saved,s.registryAccess());
                h.assertTrue(back.save(new CompoundTag(),s.registryAccess()).equals(saved),"the open passages are saved and read back the same");
            })
            // The sand over the passage stays up.
            .thenIdle(30)
            .thenExecute(()->{
                for(int i=1;i<=4;i++)h.assertTrue(l.getBlockState(pa.offset(i,1,0)).is(Blocks.SAND),"the sand over the passage holds");
                // The reader crawls in; the passage is brought due while they are inside.
                a.teleportTo(l,pa.getX()+3.5,pa.getY(),pa.getZ()+.5,-90,0);a.setDeltaMovement(Vec3.ZERO);
                StelaPassage.closeIn(s,a.getUUID(),5);
            })
            .thenWaitUntil(()->h.assertTrue(StelaPassage.crawling(a),"inside the passage the reader crawls"))
            .thenIdle(40)
            .thenExecute(()->{
                h.assertTrue(parted(l,pa,4,StelaFillBlock.SHEDDING),"due while someone is inside, it waits, shedding grit");
                // Out again: it settles, every block back as it was.
                a.teleportTo(l,pa.getX()-1.5,pa.getY(),pa.getZ()+.5,-90,0);a.setDeltaMovement(Vec3.ZERO);
            })
            .thenWaitUntil(()->h.assertTrue(asBuilt(l,pa,fill)&&StelaPassage.openFor(s,a.getUUID())==null,"once they are out it settles, the exact fill back"))
            .thenWaitUntil(()->h.assertTrue(!StelaPassage.crawling(a),"out in the open the reader stands"))
            .thenExecute(()->{
                // Farther in than it reaches, the near edge only shifts; against stone, it gives and stops.
                face(a,l,far);StelaPassage.press(a,a.getMainHandItem(),far.offset(1,0,0),InteractionHand.MAIN_HAND);
                h.assertTrue(asBuilt(l,far,run(6))&&StelaPassage.openFor(s,a.getUUID())==null,"a pocket farther in than the stone reaches: nothing parts");
                face(a,l,hard);StelaPassage.press(a,a.getMainHandItem(),hard.offset(1,0,0),InteractionHand.MAIN_HAND);
                h.assertTrue(asBuilt(l,hard,run(2))&&StelaPassage.openFor(s,a.getUUID())==null,"stone behind the fill: nothing parts");
                // The others settle in their time.
                StelaPassage.closeIn(s,b.getUUID(),2);StelaPassage.closeIn(s,e.getUUID(),2);
            })
            .thenWaitUntil(()->h.assertTrue(asBuilt(l,pb,fill)&&asBuilt(l,pe,fill),"the hostile and dismissive passages settle back too"))
            .thenExecute(()->{if(husk[0]!=null)husk[0].discard();})
            .thenSucceed();
    }
}

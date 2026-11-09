package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.gametest.*;

/**
 * 0.4.410, the Kept, with real readers. The machine built whole: the corridor of cabinets and screens, the cave of ice, the wire with its one
 * crawl, the hall of a hundred chairs, the crate and the four cells. The four sat on the floor get up when a reader comes near and walk
 * behind whoever has gone furthest; the screens read the reader their own record, truthfully; the cave is cold short of freezing; the chairs
 * turn to whoever is nearest. At the cells, a door opened with eyes open does nothing; with eyes shut the one asking walks into the glare,
 * and only the readers whose eyes were shut by it are credited. A reader who has let all four go has their mouth taken, permanently, and
 * their account; the other reader has neither. The machine puts the four back when it is empty, and remembers who let them go.
 */
@GameTestHolder(TheOldestHouse.MOD_ID+"_literary") @PrefixGameTestTemplate(false)
public final class KeptTests {
    private KeptTests(){}
    private static final LabyrinthPlace P=LabyrinthPlace.KEPT;
    private static LiteraryTests.Fixture active;
    private static LiteraryTests.Fixture fixture(GameTestHelper h){if(active!=null)active.close();active=new LiteraryTests.Fixture(h);Kept.clearAll();return active;}
    private static AABB box(LiteraryTests.Fixture f){var b=f.base(P);return new AABB(b.getX()-23,b.getY()-3,b.getZ()-99,b.getX()+23,b.getY()+11,b.getZ()+12);}
    private static String text(net.minecraft.world.item.ItemStack book){var c=book.get(DataComponents.WRITTEN_BOOK_CONTENT);return String.join(" ",c.pages().stream().map(pg->pg.raw().getString()).toList());}
    /** Stands in front of a cell's door, on the hall side, and right-clicks it. */
    private static void door(LiteraryTests.Fixture f,ServerPlayer p,int cell){
        var b=f.base(P);var d=b.offset(KeptSite.CELL_DOORS[cell]);
        NeoForge.EVENT_BUS.post(new PlayerInteractEvent.RightClickBlock(p,InteractionHand.MAIN_HAND,d,new BlockHitResult(d.getCenter(),KeptSite.CELL_DOORS[cell].getX()<0?Direction.EAST:Direction.WEST,d,false)));
    }
    private static void front(LiteraryTests.Fixture f,ServerPlayer p,int cell,double along){var at=KeptSite.cellFront(cell);f.at(p,P,at.x+(at.x<0?1.1:-1.1),0,at.z+along);}

    @GameTest(template="empty",batch="kept_walk",timeoutTicks=3000)
    public static void theMachineTheWalkAndWhatTheScreensReadOut(GameTestHelper h){
        var f=fixture(h);var b=f.base(P);var l=f.level(P);f.build(P);
        ServerPlayer[] me=new ServerPlayer[1];
        h.startSequence().thenExecute(()->f.chunks.hold(l,box(f)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the machine is resident"))
            .thenExecute(()->{
                h.assertTrue(LiteraryRooms.isLiterary(P)&&!LiteraryRooms.outside(P)&&P.kind()==LabyrinthPlace.Kind.RECURRING&&P.authored(),"a literary place inside the House, built whole");
                var story=WitnessAccount.Story.of(P.id());h.assertTrue(story!=null&&story.kind.equals("release")&&WitnessAccount.Story.values().length==WitnessPool.SOURCES&&WitnessAccount.REQUIRED==WitnessPool.REQUIRED,"a release story, the quota derived");
                h.assertTrue(KeptBlock.is(l.getBlockState(b.offset(KeptSite.PRINTER)),KeptBlock.Kind.PRINTER)&&KeptBlock.is(l.getBlockState(b.offset(KeptSite.TRAY)),KeptBlock.Kind.TRAY)&&KeptBlock.is(l.getBlockState(b.offset(KeptSite.CRATE)),KeptBlock.Kind.CRATE),"the printer, the tray and the crate");
                h.assertTrue(KeptSite.CHAIRS.size()==100,"a hundred chairs");
                for(var c:KeptSite.CHAIRS)h.assertTrue(KeptBlock.is(l.getBlockState(b.offset(c)),KeptBlock.Kind.CHAIR),"a chair at "+c);
                for(int i=0;i<4;i++){var s=l.getBlockState(b.offset(KeptSite.CELL_DOORS[i]));h.assertTrue(s.getBlock() instanceof DoorBlock&&!s.getValue(DoorBlock.OPEN),"cell "+i+" shut behind an iron door");h.assertTrue(l.getBlockState(b.offset(KeptSite.cellEnd(i).x<0?-22:22,1,KeptSite.CELL_DOORS[i].getZ())).is(KeptRegistry.material("glare")),"glare behind cell "+i);}
                for(var c:KeptSite.CRAWL){h.assertTrue(l.getBlockState(b.offset(c)).isAir()&&KeptBlock.is(l.getBlockState(b.offset(c).above()),KeptBlock.Kind.WIRE)&&KeptSite.crawling(c),"the crawl under the wire at "+c);}
                h.assertTrue(KeptSite.crawling(KeptSite.CRAWL_IN)&&!KeptSite.crawling(KeptSite.CRAWL_IN.south())&&l.getBlockState(b.offset(KeptSite.CRAWL_IN).above()).isAir(),"the crawl begins at its mouth, and the clearing stands full height");
                h.assertTrue(KeptBlock.is(l.getBlockState(b.offset(3,0,-60)),KeptBlock.Kind.WIRE)&&KeptBlock.is(l.getBlockState(b.offset(-5,2,-58)),KeptBlock.Kind.WIRE),"the thicket is too thick to walk through");
                for(int z:new int[]{-27,-50,-70})h.assertTrue(l.getBlockState(b.offset(0,0,z)).isAir()&&l.getBlockState(b.offset(0,1,z)).isAir(),"a doorway along the walk at "+z);
                h.assertTrue(KeptBlock.is(l.getBlockState(b.offset(5,1,-5)),KeptBlock.Kind.SCREEN),"a screen set into the cabinets");
                var all=text(LiteraryTexts.source(P));h.assertTrue(all.contains("PROMISED THEM FOOD")&&all.contains("THERE IS A FIFTH TODAY"),"the printout: "+all);
                me[0]=f.player("kept_reader",P);me[0].awardStat(Stats.DEATHS,3);
                f.at(me[0],P,.5,0,-10.5);
            })
            .thenWaitUntil(()->h.assertTrue(Kept.stage(b)==Kept.WALKING,"the four get up when a reader comes near: "+Kept.stage(b)))
            .thenWaitUntil(()->h.assertTrue(f.own(me[0],P).getInt("ToldDeaths")==3&&(f.own(me[0],P).getInt("Said")&1)!=0,"the screens read out how many times the reader has died, as the world counted it: "+f.own(me[0],P)))
            .thenExecute(()->{
                var a=Kept.actor(l,b,0);h.assertTrue(a!=null&&a.role()==LiteraryActor.STRANGER&&a.phase()==Kept.SILAS,"Silas, on his own phase");
                h.assertTrue(KeptBlock.is(l.getBlockState(b.offset(5,1,-9)),KeptBlock.Kind.SCREEN)&&l.getBlockState(b.offset(5,1,-9)).getValue(KeptBlock.STAGE)==1,"the screen beside the reader is awake");
                me[0].setTicksFrozen(0);f.at(me[0],P,.5,0,-36.5);
            })
            .thenWaitUntil(()->h.assertTrue(me[0].getTicksFrozen()>=40&&(f.own(me[0],P).getInt("Said")&2)!=0,"in the cave the cold builds, and the screens read out what the reader lost: "+me[0].getTicksFrozen()))
            .thenIdle(80)
            .thenExecute(()->{
                h.assertTrue(me[0].getTicksFrozen()<me[0].getTicksRequiredToFreeze(),"the cold stops short of freezing: "+me[0].getTicksFrozen());
                f.at(me[0],P,.5,0,-52.5);
            })
            .thenWaitUntil(()->h.assertTrue((f.own(me[0],P).getInt("Said")&4)!=0,"at the wire the screens read out what the reader walked out of"))
            .thenExecute(()->f.at(me[0],P,-5.5,0,-83.5))
            .thenWaitUntil(()->h.assertTrue(Kept.stage(b)==Kept.AT_TABLE&&(f.own(me[0],P).getInt("Said")&8)!=0,"the four are there at the mouth of the hall before the reader, and the machine counts the chairs: "+Kept.stage(b)))
            .thenWaitUntil(()->h.assertTrue(l.getBlockState(b.offset(-9,0,-83)).getValue(KeptBlock.FACING)==Direction.EAST,"the chairs turn to face the reader"))
            .thenExecute(()->{
                h.assertTrue(!f.own(me[0],P).getBoolean("Ready")&&LiteraryVignettes.canDeal(f.data(),me[0].getUUID(),P),"nothing is ready, and the machine is there again on the next visit");
                f.click(me[0],P,KeptSite.CRATE);
                h.assertTrue(f.own(me[0],P).getBoolean("Crate"),"the crate tried, and nothing to open the tins with");
                Kept.resetNow(l,b);
            })
            .thenSucceed();
    }

    @GameTest(template="empty",batch="kept_doors",timeoutTicks=6000)
    public static void theDoorsOpenedWithEyesShutAndTheMachineTakesAMouth(GameTestHelper h){
        var f=fixture(h);var b=f.base(P);var l=f.level(P);f.build(P);
        ServerPlayer[] r=new ServerPlayer[2];
        h.startSequence().thenExecute(()->f.chunks.hold(l,box(f)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the machine is resident"))
            .thenExecute(()->{
                r[0]=f.player("kept_shut",P);r[1]=f.player("kept_open",P);r[0].awardStat(Stats.DEATHS,1);
                f.source(r[0],P);f.source(r[1],P);
                f.at(r[0],P,-5.5,0,-80.5);f.at(r[1],P,-6.5,0,-81.5);
            })
            .thenWaitUntil(()->{var a=Kept.actor(l,b,0);h.assertTrue(Kept.stage(b)==Kept.SITTING&&a!=null&&a.gesture()==Kept.SIT,"the four are sat in the corridor");})
            .thenExecute(()->Kept.skip(b,600))
            .thenWaitUntil(()->h.assertTrue(Kept.stage(b)==Kept.AT_TABLE,"readers already in the hall: the four are at its mouth"))
            .thenIdle(40)
            .thenExecute(()->{Kept.tryCrate(l,b);Kept.skip(b,340);})
            .thenWaitUntil(()->h.assertTrue(Kept.stage(b)==Kept.ASKING&&Kept.asking(b)==0,"after the crate, the first goes to their door to ask"))
            .thenWaitUntil(()->{var a=Kept.actor(l,b,0);h.assertTrue(a!=null&&a.gesture()==Kept.HAND,"Silas at his door, a hand flat on it");})
            .thenExecute(()->{
                front(f,r[0],0,0);front(f,r[1],0,1.2);
                door(f,r[0],0);
                h.assertTrue(!KeptSite.doorOpen(l,b,0)&&f.own(r[0],P).getInt("Released")==0&&f.own(r[0],P).getBoolean("Hinted"),"with eyes open the door does nothing, and the reader is told how to shut them");
                r[0].setShiftKeyDown(true);
            })
            .thenWaitUntil(()->h.assertTrue(Kept.closed(r[0])>=Kept.CLOSE_EYES,"eyes shut by the door: "+Kept.closed(r[0])))
            .thenExecute(()->{
                h.assertTrue(Kept.closed(r[1])==0,"the other reader's eyes are open");
                door(f,r[0],0);
                h.assertTrue(KeptSite.doorOpen(l,b,0)&&f.own(r[0],P).getInt("Released")==1&&f.own(r[1],P).getInt("Released")==0,"the door opens onto the glare, and only the reader whose eyes were shut is credited");
            })
            .thenWaitUntil(()->h.assertTrue(Kept.gone(b,0)&&Kept.asking(b)==1&&!KeptSite.doorOpen(l,b,0),"Silas is gone into the glare, the door shut behind him, and the next one asks"))
            .thenExecute(()->{
                h.assertTrue(Kept.actor(l,b,0)==null,"he is not anywhere in the machine");
                // Bring the shut-eyed reader to their last: the other two counted as already let go on earlier visits.
                var own=f.own(r[0],P);own.putInt("Released",13);LiteraryVignettes.save(f.data(),r[0].getUUID(),P,own);
                front(f,r[0],1,0);front(f,r[1],1,1.2);
            })
            .thenWaitUntil(()->{var a=Kept.actor(l,b,1);h.assertTrue(a!=null&&a.gesture()==Kept.HAND&&Kept.closed(r[0])>=Kept.CLOSE_EYES,"Petra at her door, and the reader's eyes shut by it");})
            .thenExecute(()->door(f,r[0],1))
            .thenWaitUntil(()->h.assertTrue(FaceLoss.taken(l.getServer(),r[0].getUUID())&&f.own(r[0],P).getBoolean("Ready")&&f.own(r[0],P).getBoolean("Taken"),"all four let go: the machine notices, takes the reader's mouth, and the account is ready"))
            .thenExecute(()->{
                h.assertTrue(FaceLoss.faceless(r[0])&&!FaceLoss.faceless(r[1])&&!FaceLoss.taken(l.getServer(),r[1].getUUID())&&!f.own(r[1],P).getBoolean("Ready"),"only the reader who let them all go is changed");
                h.assertTrue(!FaceLoss.take(r[0])&&FaceLoss.record(l.getServer(),r[0].getUUID()).getBoolean("Mouth"),"once taken, it stays taken");
                r[0].setShiftKeyDown(false);
                f.read(r[0],P,KeptSite.TRAY);
                h.assertTrue(f.own(r[0],P).getBoolean("Completed")&&WitnessAccount.has(f.data(),r[0].getUUID(),WitnessAccount.Story.KEPT),"the account read from the tray: the story resolved");
                var account=text(KeptTexts.account(r[0].getGameProfile().getName(),f.own(r[0],P)));
                h.assertTrue(account.contains("nothing there to open")&&account.contains("how many times I had died")&&account.contains("because I still can"),"the account: "+account);
                f.click(r[1],P,KeptSite.TRAY);h.assertTrue(!f.own(r[1],P).getBoolean("Completed"),"the tray is empty for the other reader");
                Kept.resetNow(l,b);
                f.arrive(r[0],P);
                h.assertTrue(f.own(r[0],P).getInt("ReleasedBefore")==15&&f.own(r[0],P).getInt("Visits")==2,"on the next visit the machine has them back, and they remember the reader who let them go");
            })
            .thenWaitUntil(()->{for(int i=0;i<4;i++){var a=Kept.actor(l,b,i);h.assertTrue(a!=null&&a.gesture()==Kept.SIT,"all four sat in the corridor again: "+i);}})
            .thenExecute(()->Kept.resetNow(l,b))
            .thenSucceed();
    }
}

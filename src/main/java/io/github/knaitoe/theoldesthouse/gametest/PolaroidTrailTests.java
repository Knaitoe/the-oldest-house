package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;

/**
 * 0.4.204, the Polaroid Trail, played through its own controller with real readers in it: the balloon tag with the reader's
 * name and every way on shut; the creek photographed from the culvert, the photograph taken from under its balloon and the
 * look back down the sightline that opens the grate; looking at the flash, which puts the reader back on the porch; the
 * crawlspace's bowl and bed of clothes and the hatch; the cinema's four flashes, the one behind coming nearer, and turning round;
 * and the trench in the pines, looking straight up, and the album.
 */
@GameTestHolder(TheOldestHouse.MOD_ID+"_literary") @PrefixGameTestTemplate(false)
public final class PolaroidTrailTests {
    private PolaroidTrailTests(){}
    private static final LabyrinthPlace P=LabyrinthPlace.POLAROID_TRAIL;
    private static LiteraryTests.Fixture active;
    private static LiteraryTests.Fixture fixture(GameTestHelper h){if(active!=null)active.close();active=new LiteraryTests.Fixture(h);return active;}
    private static void close(){if(active!=null){active.close();active=null;}}

    private static int count(ServerPlayer p,Item item){int n=0;var inv=p.getInventory();for(int i=0;i<inv.getContainerSize();i++)if(inv.getItem(i).is(item))n+=inv.getItem(i).getCount();return n;}
    private static int state(LiteraryTests.Fixture f,ServerPlayer p,String id){return PolaroidTrail.state(f.own(p,P),id);}
    /** Brings a photograph's next moment to now: the flash, if it is charging, or the photograph left, if it is developing. */
    private static void due(LiteraryTests.Fixture f,ServerPlayer p,String id){
        var own=f.own(p,P);var t=PolaroidTrail.find(own,id);if(t==null)throw new GameTestAssertException("no photograph "+id);
        t.putLong("At",f.out.getGameTime());t.putBoolean("Charged",true);own.putLong("LastFlash204",0);LiteraryVignettes.save(f.data(),p.getUUID(),P,own);
    }
    /** Stands the reader at a place in the trail facing a way (yaw), level. */
    private static void stand(LiteraryTests.Fixture f,ServerPlayer p,double x,double y,double z,float yaw,float pitch){f.at(p,P,x,y,z);p.setYRot(yaw);p.setYHeadRot(yaw);p.setXRot(pitch);}
    /** Stands the reader back where a photograph was taken of them and turns them to the camera. */
    private static void lookBack(LiteraryTests.Fixture f,ServerPlayer p,String id){
        var t=PolaroidTrail.find(f.own(p,P),id);f.at(p,P,t.getDouble("SX"),t.getDouble("SY"),t.getDouble("SZ"));
        p.lookAt(EntityAnchorArgument.Anchor.EYES,PolaroidTrail.at(f.base(P),PolaroidTrail.shot(id).node()));
    }
    /** One whole photograph: the flash with the reader turned away, the photograph left, and picked up. A camera already further along (a busy server) is taken from where it is. */
    private static GameTestSequence photograph(GameTestHelper h,LiteraryTests.Fixture f,ServerPlayer p,GameTestSequence seq,String id){
        return seq.thenExecute(()->{if(state(f,p,id)==0)due(f,p,id);})
            .thenWaitUntil(()->h.assertTrue(state(f,p,id)>=1,"the flash goes off for "+id+" and the reader is not caught"))
            .thenExecute(()->{if(state(f,p,id)==1)due(f,p,id);})
            .thenWaitUntil(()->h.assertTrue(state(f,p,id)==2&&PolaroidBlock.is(f.out.getBlockState(f.base(P).offset(PolaroidTrail.shot(id).drop())),PolaroidBlock.Kind.PHOTO),"the photograph "+id+" is left under its balloon"))
            .thenExecute(()->{f.click(p,P,PolaroidTrail.shot(id).drop());h.assertTrue(state(f,p,id)==3,"picked up, "+id+" is the reader's");});
    }
    private static String text(net.minecraft.world.item.ItemStack book){var c=book.get(DataComponents.WRITTEN_BOOK_CONTENT);var b=new StringBuilder();if(c!=null)for(var page:c.pages())b.append(page.raw().getString()).append('|');return b.toString();}
    private static void visit(LiteraryTests.Fixture f,ServerPlayer p,int done,int gates){
        var own=f.own(p,P);own.putInt("Visit",done);own.putInt("Beat",done);own.putInt("Gates204",gates);LiteraryVignettes.save(f.data(),p.getUUID(),P,own);f.arrive(p,P);
    }

    @GameTest(template="empty",batch="polaroid_tag",timeoutTicks=400)
    public static void theBalloonTagHasTheReadersNameAndEveryWayOnStartsShut(GameTestHelper h){
        var f=fixture(h);var p=f.player("polaroid_tag",P);var l=f.out;var b=f.base(P);
        h.assertTrue(LiteraryRooms.isLiterary(P)&&LiteraryRooms.outside(P)&&P.kind()==LabyrinthPlace.Kind.MULTI_VISIT&&WitnessAccount.Story.of(P.id())==WitnessAccount.Story.POLAROID_TRAIL&&WitnessAccount.Story.POLAROID_TRAIL.kind.equals("understanding"),"an outdoor literary understanding source over four visits");
        h.assertTrue(LiteraryRooms.source(P).equals(PolaroidSite.TAG1)&&LiteraryRooms.ending(P).equals(PolaroidSite.ALBUM),"the tag on the porch rail is the source and the album in the deer blind the account");
        h.assertTrue(PolaroidBlock.is(l.getBlockState(b.offset(PolaroidSite.TAG1)),PolaroidBlock.Kind.TAG)&&PolaroidBlock.is(l.getBlockState(b.offset(PolaroidSite.ALBUM)),PolaroidBlock.Kind.ALBUM),"the tag and the album are in place");
        h.assertTrue(l.getBlockState(b.offset(PolaroidSite.CULVERT_X0,0,PolaroidSite.GRATE_Z)).getValue(PolaroidBlock.STAGE)==0&&l.getBlockState(b.offset(PolaroidSite.HATCH)).getValue(PolaroidBlock.STAGE)==0
            &&!l.getBlockState(b.offset(PolaroidSite.FIRE_EXIT)).getValue(DoorBlock.OPEN)&&l.getBlockState(b.offset(PolaroidSite.PROJECTOR)).getValue(PolaroidBlock.STAGE)==0,"the grate, the hatch and the fire exit are shut and the projector is off");
        h.assertTrue(f.own(p,P).getList("Josh204",Tag.TAG_STRING).getString(0).equals("start"),"Josh is on the walkie when the reader comes out onto the porch");
        f.source(p,P);
        var original=net.minecraft.world.item.ItemStack.parseOptional(l.registryAccess(),f.own(p,P).getCompound("Original_Source"));var book=original.get(DataComponents.WRITTEN_BOOK_CONTENT);
        // The name is in the child's hand (alternate glyphs), so the tag read is compared with the tag written for this reader and for another.
        var mine=text(PolaroidTexts.tag(1,"polaroid_tag"));var theirs=text(PolaroidTexts.tag(1,"someone_else"));
        h.assertTrue(book!=null&&text(original).equals(mine)&&!mine.equals(theirs),"the tag has the reader's own name on it");
        h.succeed();
    }
    @AfterBatch(batch="polaroid_tag") public static void tagDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="polaroid_creek",timeoutTicks=800)
    public static void theCreekPhotographedFromTheCulvertAndTheLookBackFromTheSameSpotOpensTheGrate(GameTestHelper h){
        var f=fixture(h);var p=f.player("polaroid_creek",P);var b=f.base(P);
        var seq=h.startSequence()
            .thenExecute(()->{f.chunks.hold(f.out,new AABB(b).inflate(30,8,44).move(0,0,-22));stand(f,p,-6.5,-1,-15.5,0,0);})
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready()&&state(f,p,"creek")>=0,"standing in the creek, the reader is photographed from the culvert"));
        photograph(h,f,p,seq,"creek")
            .thenExecute(()->{h.assertTrue(count(p,PolaroidRegistry.PHOTO.get())==1,"the reader carries the photograph");
                var photo=p.getInventory().items.stream().filter(s->s.is(PolaroidRegistry.PHOTO.get())).findFirst().orElseThrow();var tag=photo.get(DataComponents.CUSTOM_DATA).copyTag();
                h.assertTrue(tag.getByteArray("Frame").length==PolaroidTrail.FW*PolaroidTrail.FH&&tag.getString("Shot").equals("creek"),"the photograph is a real picture of the reader from the culvert");
                lookBack(f,p,"creek");})
            .thenWaitUntil(()->{var own=f.own(p,P);h.assertTrue(state(f,p,"creek")==4&&(own.getInt("Gates204")&PolaroidTrail.GRATE)!=0&&own.getInt("Beat")==1,"standing where it was taken and looking back at the camera finishes the first visit");
                h.assertTrue(f.out.getBlockState(b.offset(PolaroidSite.CULVERT_X0,0,PolaroidSite.GRATE_Z)).getValue(PolaroidBlock.STAGE)==1,"and swings the culvert's grate open");})
            .thenSucceed();
    }
    @AfterBatch(batch="polaroid_creek") public static void creekDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="polaroid_caught",timeoutTicks=600)
    public static void lookingAtTheFlashPutsTheReaderBackOnThePorchWithTheVisitToDoAgain(GameTestHelper h){
        var f=fixture(h);var p=f.player("polaroid_caught",P);var b=f.base(P);
        h.startSequence()
            .thenExecute(()->f.chunks.hold(f.out,new AABB(b).inflate(30,8,44).move(0,0,-22)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the creek's native sections are resident before entering the flash trigger"))
            .thenExecute(()->stand(f,p,-13.5,-1,-15.5,0,0))
            .thenWaitUntil(()->h.assertTrue(state(f,p,"creek")>=0,"the camera is charging while the reader faces away"))
            .thenExecute(()->{p.lookAt(EntityAnchorArgument.Anchor.EYES,PolaroidTrail.at(b,PolaroidTrail.shot("creek").node()));due(f,p,"creek");})
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getInt("Caught204")==1,"looking at the flash as it goes off is caught"))
            .thenExecute(()->{var own=f.own(p,P);
                h.assertTrue(own.getBoolean("Haunted204")&&PolaroidTrail.find(own,"creek")==null&&own.getInt("Beat")==0,"the visit is taken back and the halls will photograph the reader from behind");
                h.assertTrue(Math.abs(p.getZ()-b.getZ()+2.5)<1&&Math.abs(p.getY()-b.getY())<1,"the reader is back on the porch");
                h.assertTrue(HouseLens.fromBehind(f.out,p,PolaroidTrail.FW,PolaroidTrail.FH).length==PolaroidTrail.FW*PolaroidTrail.FH,"the hall photograph from behind is a real picture");
                h.assertTrue(PolaroidTexts.accountText("x",own).contains("once I looked at him"),"the album keeps the white pages");})
            .thenSucceed();
    }
    @AfterBatch(batch="polaroid_caught") public static void caughtDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="polaroid_crawl",timeoutTicks=1200)
    public static void underTheHouseTheBowlThenTheBedOfClothesOpenTheHatch(GameTestHelper h){
        var f=fixture(h);var p=f.player("polaroid_crawl",P);var b=f.base(P);
        var seq=h.startSequence()
            .thenExecute(()->{f.chunks.hold(f.out,new AABB(b.offset(-13,3,-50)).inflate(14,6,12));visit(f,p,1,PolaroidTrail.GRATE);stand(f,p,-12.5,3,-44.5,-90,0);})
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready()&&f.own(p,P).getInt("Visit")==2&&PolaroidTrail.crawling(p,b),"on the second visit the reader is down on hands and knees under the house next door"))
            .thenWaitUntil(()->h.assertTrue(state(f,p,"bowl")>=0,"a while crawling, and the camera behind the bricks is charging"));
        seq=photograph(h,f,p,seq,"bowl").thenExecute(()->lookBack(f,p,"bowl"))
            .thenWaitUntil(()->h.assertTrue(state(f,p,"bowl")==4&&state(f,p,"clothes")>=0,"looking back from the bowl, the camera moves on to the bed of clothes"))
            .thenExecute(()->stand(f,p,-12.5,3,-52.5,90,0));
        photograph(h,f,p,seq,"clothes").thenExecute(()->lookBack(f,p,"clothes"))
            .thenWaitUntil(()->{var own=f.own(p,P);h.assertTrue(state(f,p,"clothes")==4&&(own.getInt("Gates204")&PolaroidTrail.HATCH)!=0&&own.getInt("Beat")==2,"looking back from the clothes finishes the second visit");
                h.assertTrue(f.out.getBlockState(b.offset(PolaroidSite.HATCH)).getValue(PolaroidBlock.STAGE)==1&&f.out.getBlockState(b.offset(PolaroidSite.CULVERT_X0,0,PolaroidSite.GRATE_Z)).getValue(PolaroidBlock.STAGE)==1,"the hatch in the clay opens, and the grate the first visit opened stays open");})
            .thenSucceed();
    }
    @AfterBatch(batch="polaroid_crawl") public static void crawlDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="polaroid_cinema",timeoutTicks=1600)
    public static void inTheCinemaTheOneBehindComesNearerAfterEachFlashAndTurningRoundOpensTheFireExit(GameTestHelper h){
        var f=fixture(h);var p=f.player("polaroid_cinema",P);var b=f.base(P);
        var seq=h.startSequence()
            .thenExecute(()->{f.chunks.hold(f.out,new AABB(b.offset(0,6,-84)).inflate(18,8,22));visit(f,p,2,PolaroidTrail.GRATE|PolaroidTrail.HATCH);stand(f,p,.5,3,-96.5,180,0);})
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready()&&f.own(p,P).getBoolean("Projector204")&&f.out.getBlockState(b.offset(PolaroidSite.PROJECTOR)).getValue(PolaroidBlock.STAGE)==1,"in the auditorium the projector comes on behind the reader"))
            .thenWaitUntil(()->h.assertTrue(state(f,p,"c1")>=0,"at the front, the camera in the booth charges"));
        for(int i=1;i<=4;i++){String id="c"+i;int n=i;
            seq=photograph(h,f,p,seq,id).thenExecute(()->stand(f,p,.5,3,-96.5,180,0));
            if(i<4)seq=seq.thenWaitUntil(()->h.assertTrue(state(f,p,"c"+(n+1))>=0,"another flash is coming from the booth after "+id));
        }
        seq.thenWaitUntil(()->h.assertTrue(f.own(p,P).getInt("Sil204")==PolaroidTrail.SITTER_ROWS[4]&&f.own(p,P).getInt("Collected204")==4,"with all four photographs taken the one behind is right behind the reader"))
            .thenExecute(()->p.lookAt(EntityAnchorArgument.Anchor.EYES,PolaroidTrail.sitter(b,PolaroidTrail.SITTER_ROWS[4]).add(0,1.1,0)))
            .thenWaitUntil(()->{var own=f.own(p,P);h.assertTrue((own.getInt("Gates204")&PolaroidTrail.FIRE_EXIT)!=0&&own.getInt("Beat")==3&&!own.getBoolean("Projector204"),"turning round to face them finishes the third visit");
                h.assertTrue(f.out.getBlockState(b.offset(PolaroidSite.FIRE_EXIT)).getValue(DoorBlock.OPEN)&&f.out.getBlockState(b.offset(PolaroidSite.PROJECTOR)).getValue(PolaroidBlock.STAGE)==0,"the power goes and the fire exit stands open");})
            .thenSucceed();
    }
    @AfterBatch(batch="polaroid_cinema") public static void cinemaDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="polaroid_pines",timeoutTicks=1600)
    public static void inThePinesTheReaderLooksStraightUpFromTheTrenchAndTheAlbumResolvesTheTrail(GameTestHelper h){
        var f=fixture(h);var p=f.player("polaroid_pines",P);var b=f.base(P);
        var seq=h.startSequence()
            .thenExecute(()->{f.chunks.hold(f.out,new AABB(b.offset(0,8,-122)).inflate(24,10,20));f.source(p,P);visit(f,p,3,7);stand(f,p,.5,3,-123.5,0,0);})
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready()&&f.own(p,P).getInt("Visit")==4&&state(f,p,"p1")>=0,"in the pines the camera is somewhere up in the trees"));
        seq=photograph(h,f,p,seq,"p1").thenExecute(()->stand(f,p,.5,3,-123.5,0,0)).thenWaitUntil(()->h.assertTrue(state(f,p,"p2")>=0,"and again, higher"));
        seq=photograph(h,f,p,seq,"p2").thenExecute(()->stand(f,p,.5,3,-123.5,0,0)).thenWaitUntil(()->h.assertTrue(state(f,p,"p3")>=0,"and a third time"));
        seq=photograph(h,f,p,seq,"p3").thenExecute(()->stand(f,p,-5.5,1,-123.5,0,60)).thenWaitUntil(()->h.assertTrue(state(f,p,"p4")>=0,"down in the trench on the left, the camera is straight above"));
        photograph(h,f,p,seq,"p4")
            .thenExecute(()->{h.assertTrue(!f.own(p,P).getBoolean("Ready"),"the last photograph alone does not finish it");stand(f,p,-5.5,1,-123.5,0,-90);})
            .thenWaitUntil(()->{var own=f.own(p,P);h.assertTrue(own.getBoolean("Lit204")&&own.getBoolean("Ready")&&own.getString("Outcome").equals("looked_up_from_the_trench"),"looking straight up from the trench finishes the trail");
                var balloon=PolaroidSite.canopyBalloons().stream().filter(at->PolaroidBlock.is(f.out.getBlockState(b.offset(at)),PolaroidBlock.Kind.BALLOON)).findFirst();
                h.assertTrue(balloon.isPresent()&&f.out.getBlockState(b.offset(balloon.get())).getValue(PolaroidBlock.STAGE)==1,"the balloons in the branches are lit");
                h.assertTrue(count(p,PolaroidRegistry.WALKIE.get())==1,"Josh's walkie-talkie is the reader's to keep");})
            .thenExecute(()->{var text=PolaroidTexts.accountText("x",f.own(p,P));h.assertTrue(text.contains("The top of my head")&&text.contains("The first: me in the creek")==false,"the album holds only the photographs this reader picked up");
                h.assertTrue(!WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.POLAROID_TRAIL),"nothing resolves until the album is read");
                f.read(p,P,PolaroidSite.ALBUM);
                h.assertTrue(WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.POLAROID_TRAIL)&&f.own(p,P).getBoolean("Completed"),"reading the album resolves the trail for this reader");})
            .thenSucceed();
    }
    @AfterBatch(batch="polaroid_pines") public static void pinesDone(ServerLevel l){close();}
}

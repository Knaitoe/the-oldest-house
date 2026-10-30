package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.journal.CoAuthor;
import io.github.knaitoe.theoldesthouse.journal.JournalPage;
import io.github.knaitoe.theoldesthouse.journal.Journals;
import io.github.knaitoe.theoldesthouse.journal.Rubbings;
import io.github.knaitoe.theoldesthouse.journal.TornPages;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthData;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthPlace;
import io.github.knaitoe.theoldesthouse.labyrinth.WitnessAccount;
import java.util.List;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.item.ItemExpireEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** The reader's journal (JOURNAL_DESIGN.md): one owner's book, bounded edits kept in saved data, a lost copy that comes back worn, pencil rubbings and the other hand. */
@GameTestHolder(TheOldestHouse.MOD_ID) @PrefixGameTestTemplate(false)
public final class JournalTests {
    private JournalTests(){}
    static CompoundTag run(String text,String shape,float x,float y){var t=new CompoundTag();t.putString("Text",text);t.putString("Shape",shape);t.putFloat("X",x);t.putFloat("Y",y);t.putFloat("Angle",-7);t.putFloat("Size",1);return t;}
    static CompoundTag page(String text,CompoundTag... runs){var t=new CompoundTag();t.putString("Text",text);var list=new ListTag();for(var r:runs)list.add(r);t.put("Placed",list);return t;}
    static ItemStack held(ServerPlayer p){for(var s:p.getInventory().items)if(Journals.owner(s)!=null)return s;return ItemStack.EMPTY;}

    @GameTest(template="empty",batch="journal_owner",timeoutTicks=100)
    public static void theJournalIsItsOwnersAndAnOldCopyGoesBlank(GameTestHelper h){
        var p=NativeTestPlayers.survival(h,"journal_owner");var peer=NativeTestPlayers.survival(h,"journal_peer");
        try{
            var data=Journals.get(h.getLevel().getServer());
            var first=data.parcel(p);
            h.assertTrue(!first.isEmpty()&&data.parcel(p).isEmpty()&&!data.grantIfNone(p),"one journal, once, whether it comes on the doorstep or on the first arrival");
            h.assertTrue(data.check(p,first)==Journals.Opening.OPEN&&data.check(peer,first)==Journals.Opening.NOT_YOURS,"only its owner opens it");
            h.assertTrue(Journals.nameOf(first).equals("journal_owner")&&Journals.wearOf(first)==0,"the owner's name is inside the cover, and it is new");
            h.assertTrue(data.grantIfNone(peer)&&!held(peer).isEmpty()&&data.check(peer,held(peer))==Journals.Opening.OPEN,"a reader who missed the letter is given theirs");
            h.assertTrue(!data.returnLost(p),"a journal that is not lost does not come back");
            data.markLost(first);
            h.assertTrue(data.returnLost(p),"a lost journal comes back to its owner");
            var second=held(p);
            h.assertTrue(data.check(p,first)==Journals.Opening.OLD_ISSUE&&data.check(p,second)==Journals.Opening.OPEN&&Journals.wearOf(second)==1,"the old copy goes blank and the new one is a stage worse");
            h.assertTrue(!data.returnLost(p),"it comes back once");
            data.markLost(first);h.assertTrue(!data.lost(p.getUUID()),"an old copy lost again changes nothing");
        }finally{NativeTestPlayers.remove(p);NativeTestPlayers.remove(peer);}
        h.succeed();
    }

    @GameTest(template="empty",batch="journal_edit",timeoutTicks=200)
    public static void editsAreKeptOnlyFromTheOwnerAndWithinWhatThePageHolds(GameTestHelper h){
        var p=NativeTestPlayers.survival(h,"journal_writer");var peer=NativeTestPlayers.survival(h,"journal_borrower");
        var data=Journals.get(h.getLevel().getServer());var mine=data.issue(p);
        h.assertTrue(Journals.edit(p,1,page("I measured the hall twice."))==Journals.Edit.NOT_CARRIED,"nothing is kept from a reader not carrying their journal");
        p.getInventory().add(mine);peer.getInventory().add(mine.copy());
        h.assertTrue(Journals.edit(peer,1,page("not mine"))==Journals.Edit.NOT_CARRIED,"another reader's journal keeps nothing from its borrower");
        h.assertTrue(Journals.edit(p,0,page("x"))==Journals.Edit.NO_SUCH_PAGE&&Journals.edit(p,Journals.PAGES+1,page("x"))==Journals.Edit.NO_SUCH_PAGE,"only the journal's own pages");
        h.assertTrue(Journals.edit(p,2,page("a".repeat(JournalPage.MAX_TEXT+1)))==Journals.Edit.TOO_MUCH,"no more writing than a page holds");
        h.assertTrue(Journals.edit(p,2,page("\n".repeat(JournalPage.MAX_NEWLINES+1)))==Journals.Edit.TOO_MUCH,"no more lines than a page holds");
        h.assertTrue(Journals.edit(p,2,page("§cred"))==Journals.Edit.TOO_MUCH,"no formatting codes");
        h.assertTrue(Journals.edit(p,1,page("I measured the hall twice.\nLeft at the lamp.",run("it is longer than it was","line",5,.8F),run("the house keeps the measure","spiral",.5F,.3F)))==Journals.Edit.ACCEPTED,"the owner's page is taken");
        int accepted=1;while(Journals.edit(p,3+accepted,page("more"))==Journals.Edit.ACCEPTED)accepted++;
        h.assertTrue(accepted==8,"a reader's screen has an allowance of eight pages at once: "+accepted);
        h.startSequence().thenWaitUntil(()->h.assertTrue(data.page(p.getUUID(),1).text().startsWith("I measured"),"the page is kept once it has passed the text filter"))
        .thenExecute(()->{
            var kept=data.page(p.getUUID(),1);
            // 0.4.185: the reader only writes; shaped lines are the House's, so lines sent in shapes are never kept.
            h.assertTrue(kept.text().equals("I measured the hall twice.\nLeft at the lamp.")&&kept.placed().isEmpty(),"only the writing is kept, never the reader's shaped lines: "+kept);
            h.assertTrue(kept.day()>0&&!kept.house()&&kept.begun().isEmpty(),"the page remembers when it was begun, and that it was not in the House");
            var registries=h.getLevel().registryAccess();var back=Journals.load(data.save(new CompoundTag(),registries),registries);
            h.assertTrue(back.issue(p.getUUID())==data.issue(p.getUUID())&&back.page(p.getUUID(),1).equals(kept),"the journal survives a save and reload");
            h.assertTrue(data.contents(p.getUUID()).getCompound("Pages").contains("1")&&!data.contents(p.getUUID()).getCompound("Pages").contains("2"),"the screen is sent every written page and nothing refused");
            NativeTestPlayers.remove(p);NativeTestPlayers.remove(peer);
        }).thenSucceed();
    }

    @GameTest(template="empty",batch="journal_lost",timeoutTicks=100)
    public static void aJournalLostInTheWorldComesBackWorn(GameTestHelper h){
        var p=NativeTestPlayers.survival(h,"journal_loser");var level=h.getLevel();var data=Journals.get(level.getServer());
        try{
            var at=h.absoluteVec(new net.minecraft.world.phys.Vec3(1,2,1));
            var picked=new ItemEntity(level,at.x,at.y,at.z,data.issue(p));level.addFreshEntity(picked);picked.discard();
            h.assertTrue(!data.lost(p.getUUID()),"picked up or put away, a journal is not lost");
            var expiring=new ItemEntity(level,at.x,at.y,at.z,data.issue(p));level.addFreshEntity(expiring);
            NeoForge.EVENT_BUS.post(new ItemExpireEvent(expiring));
            h.assertTrue(data.lost(p.getUUID())&&expiring.getItem().isEmpty(),"left to despawn, it is lost, and never goes onto the Mother's shelf");
            h.assertTrue(data.returnLost(p)&&Journals.wearOf(held(p))==1,"it comes back water-stained");
            p.getInventory().clearContent();
            var burning=new ItemEntity(level,at.x,at.y,at.z,data.issue(p));level.addFreshEntity(burning);burning.hurt(level.damageSources().lava(),20);
            h.assertTrue(data.lost(p.getUUID()),"burnt, it is lost");
            h.assertTrue(data.returnLost(p)&&Journals.wearOf(held(p))==2,"and comes back dog-eared");
            p.getInventory().clearContent();
            var falling=new ItemEntity(level,at.x,level.getMinBuildHeight()-60,at.z,data.issue(p));level.addFreshEntity(falling);falling.discard();
            h.assertTrue(data.lost(p.getUUID()),"fallen out of the world, it is lost");
            h.assertTrue(data.returnLost(p)&&Journals.wearOf(held(p))==3,"and comes back swollen");
        }finally{NativeTestPlayers.remove(p);}
        h.succeed();
    }

    /** A state of a block in the rooms that gives this rubbing. */
    static net.minecraft.world.level.block.state.BlockState sample(Rubbings.Kind kind){
        for(var block:net.minecraft.core.registries.BuiltInRegistries.BLOCK)for(var state:block.getStateDefinition().getPossibleStates())if(kind.matches().test(state))return state;
        return null;
    }
    @GameTest(template="empty",batch="journal_rubbings",timeoutTicks=100)
    public static void eachAuthoredObjectGivesOneRubbingAndASecondTryTurnsToIt(GameTestHelper h){
        var p=NativeTestPlayers.survival(h,"journal_rubber");var peer=NativeTestPlayers.survival(h,"journal_onlooker");
        try{
            var data=Journals.get(h.getLevel().getServer());var mine=data.issue(p);p.getInventory().add(mine.copy());var theirs=data.issue(peer);
            h.assertTrue(Rubbings.ALL.size()>=12,"about twelve objects to begin with");
            var pages=new java.util.HashSet<Integer>();
            for(var kind:Rubbings.ALL){
                var state=sample(kind);h.assertTrue(state!=null&&Rubbings.of(state)==kind,"an authored "+kind.id()+" gives its own rubbing");
                h.assertTrue(Journals.rub(p,mine,state)==Journals.Rub.RUBBED,"the "+kind.id()+" rubbing is taken");
                int at=data.rubbedPage(p.getUUID(),kind.id());var page=data.page(p.getUUID(),at);
                h.assertTrue(at>=1&&pages.add(at)&&page.rubbings().size()==1&&page.rubbings().get(0).id().equals(kind.id()),"on a page of its own with room to write: "+kind.id()+" on "+at);
                h.assertTrue(Journals.rub(p,mine,state)==Journals.Rub.TURNED&&data.page(p.getUUID(),at).rubbings().size()==1,"a second try only turns to that page");
                String line=" "+kind.observation().toLowerCase().replaceAll("[^a-z' ]"," ")+" ";
                for(var word:java.util.List.of("he","she","him","her","his","hers","man","woman","boy","girl","old","young","years","red","blue","green","yellow","white","black","grey","gray","brown","orange","purple","pink","golden","silver"))
                    h.assertTrue(!line.contains(" "+word+" "),"the line about the "+kind.id()+" keeps the House's writing rules: "+word);
            }
            h.assertTrue(Journals.rub(p,mine,net.minecraft.world.level.block.Blocks.STONE.defaultBlockState())==Journals.Rub.NOT_RUBBABLE,"anything else only opens the journal");
            var gouges=sample(Rubbings.byId("gouges"));
            h.assertTrue(Journals.rub(peer,mine,gouges)==Journals.Rub.NOT_YOURS&&data.rubbedPage(peer.getUUID(),"gouges")<0,"another reader's journal takes nothing");
            // The room's own handlers answer first; the rubbing is taken after, through the real click.
            var at=h.absolutePos(new net.minecraft.core.BlockPos(1,1,1));h.getLevel().setBlock(at,gouges,3);peer.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,theirs);
            var click=new net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock(peer,net.minecraft.world.InteractionHand.MAIN_HAND,at,new net.minecraft.world.phys.BlockHitResult(at.getCenter(),net.minecraft.core.Direction.UP,at,false));
            NeoForge.EVENT_BUS.post(click);
            h.assertTrue(click.isCanceled()&&click.getCancellationResult()==net.minecraft.world.InteractionResult.SUCCESS&&data.rubbedPage(peer.getUUID(),"gouges")==1,"a click with the journal on the gouges takes the rubbing");
            h.getLevel().setBlock(at,net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(),3);
            var plain=new net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock(peer,net.minecraft.world.InteractionHand.MAIN_HAND,at,new net.minecraft.world.phys.BlockHitResult(at.getCenter(),net.minecraft.core.Direction.UP,at,false));
            NeoForge.EVENT_BUS.post(plain);h.assertTrue(!plain.isCanceled(),"a click on anything else is left to open the journal");
            // 0.4.199: charcoal in hand sketches into the reader's own journal, wherever it is on them.
            var initials=sample(Rubbings.byId("well_initials"));h.getLevel().setBlock(at,initials,3);
            peer.getInventory().setItem(20,peer.getMainHandItem().copy());peer.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.CHARCOAL));
            var sketch=new net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock(peer,net.minecraft.world.InteractionHand.MAIN_HAND,at,new net.minecraft.world.phys.BlockHitResult(at.getCenter(),net.minecraft.core.Direction.UP,at,false));
            NeoForge.EVENT_BUS.post(sketch);
            h.assertTrue(sketch.isCanceled()&&data.rubbedPage(peer.getUUID(),"well_initials")>=1&&peer.getMainHandItem().is(net.minecraft.world.item.Items.CHARCOAL)&&peer.getMainHandItem().getCount()==1,"charcoal in hand sketches the initials into the reader's own journal, and is not used up");
            var empty=NativeTestPlayers.survival(h,"journal_charcoal_only");
            try{empty.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.CHARCOAL));
                var none=new net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock(empty,net.minecraft.world.InteractionHand.MAIN_HAND,at,new net.minecraft.world.phys.BlockHitResult(at.getCenter(),net.minecraft.core.Direction.UP,at,false));
                NeoForge.EVENT_BUS.post(none);h.assertTrue(!none.isCanceled(),"charcoal with no journal of one's own sketches nothing");
            }finally{NativeTestPlayers.remove(empty);}
            h.getLevel().setBlock(at,net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(),3);
            var fresh=data.issue(p);h.assertTrue(Journals.rub(p,mine,gouges)==Journals.Rub.NOT_YOURS&&Journals.rub(p,fresh,gouges)==Journals.Rub.TURNED,"an old copy takes nothing; the new one has every rubbing");
        }finally{NativeTestPlayers.remove(p);NativeTestPlayers.remove(peer);}
        h.succeed();
    }
    @GameTest(template="empty",batch="journal_rubbings_kept",timeoutTicks=200)
    public static void rubbingsOutlastTheReadersOwnEditsAndAReload(GameTestHelper h){
        var p=NativeTestPlayers.survival(h,"journal_keeper");var data=Journals.get(h.getLevel().getServer());var mine=data.issue(p);p.getInventory().add(mine.copy());
        h.assertTrue(Journals.rub(p,mine,sample(Rubbings.byId("room_number")))==Journals.Rub.RUBBED,"a rubbing of the room number");
        int at=data.rubbedPage(p.getUUID(),"room_number");
        var forged=page("A rubbing that was never taken.");var list=new ListTag();var fake=new CompoundTag();fake.putString("Id","gouges");list.add(fake);forged.put("Rubbings",list);
        h.assertTrue(Journals.edit(p,at,page("Two one seven. The bell rang twice."))==Journals.Edit.ACCEPTED&&Journals.edit(p,at+1,forged)==Journals.Edit.ACCEPTED,"the reader writes beside it, and sends a page claiming a rubbing");
        h.startSequence().thenWaitUntil(()->h.assertTrue(data.page(p.getUUID(),at).text().startsWith("Two one seven")&&data.page(p.getUUID(),at+1).text().startsWith("A rubbing"),"both pages are kept once filtered"))
        .thenExecute(()->{
            h.assertTrue(data.page(p.getUUID(),at).rubbings().size()==1,"writing beside a rubbing never removes it");
            h.assertTrue(data.page(p.getUUID(),at+1).rubbings().isEmpty()&&data.rubbedPage(p.getUUID(),"gouges")<0,"a reader's screen cannot lay a rubbing");
            var registries=h.getLevel().registryAccess();var back=Journals.load(data.save(new CompoundTag(),registries),registries);
            h.assertTrue(back.page(p.getUUID(),at).rubbings().equals(data.page(p.getUUID(),at).rubbings())&&back.rubbedPage(p.getUUID(),"room_number")==at,"rubbings and where they went survive a save and reload");
            NativeTestPlayers.remove(p);
        }).thenSucceed();
    }

    /** A page as the House keeps it: begun in the House (or not), on day one. */
    static JournalPage kept(String text,boolean house){return new JournalPage(text,List.of(),house?"the lake house":"",1,house,List.of(),List.of());}
    static String notes(JournalPage page){return page.notes().toString();}

    @GameTest(template="empty",batch="journal_other_hand",timeoutTicks=100)
    public static void theOtherHandWritesAfterAMorningAndAWorstRoomsEndOnlyOnHousePagesAndOnceADay(GameTestHelper h){
        var p=NativeTestPlayers.survival(h,"journal_other_hand");var s=h.getLevel().getServer();var id=p.getUUID();var data=Journals.get(s);
        try{
            data.issue(p);CoAuthor.certain(id,true);LabyrinthData.get(s).visit(id,LabyrinthPlace.LAKE_HOUSE);
            data.put(id,1,kept("I followed Tess out of the house and down to the water.",false));
            data.put(id,2,kept("The hall was longer than it was.",true));
            net.neoforged.neoforge.event.EventHooks.onPlayerWakeup(p,true,true);
            h.assertTrue(data.inked(id)==0&&data.page(id,3).notes().isEmpty(),"leaving the bed early is not a morning, and nothing is written");
            net.neoforged.neoforge.event.EventHooks.onPlayerWakeup(p,false,false);
            var blank=data.page(id,3);
            h.assertTrue(blank.notes().size()==1&&blank.notes().get(0).mark()==JournalPage.Mark.WRITTEN&&blank.notes().get(0).text().equals(CoAuthor.BLANK)&&blank.text().isEmpty(),"after a night's sleep the other hand writes on the blank page after the last one begun in the House: "+notes(blank));
            h.assertTrue(data.page(id,1).notes().isEmpty()&&data.page(id,2).notes().isEmpty(),"a page begun outside the House is never touched, even with a name on it");
            h.assertTrue(CoAuthor.consider(p,CoAuthor.Trigger.SLEEP)==CoAuthor.Wrote.ALREADY_TODAY&&CoAuthor.write(p,CoAuthor.Trigger.BLACKOUT)==CoAuthor.Wrote.ALREADY_TODAY,"never twice in one House day, whatever the moment");
            data.inked(id,0);
            data.put(id,2,kept("The hall was longer than it was. I followed Tess down to the water.",true));
            WitnessAccount.resolve(p,WitnessAccount.Story.FLOORBOARDS,"test");
            h.assertTrue(data.page(id,2).notes().isEmpty()&&data.inked(id)==0,"a room that is not among the worst ends without it");
            WitnessAccount.resolve(p,WitnessAccount.Story.GOATMAN,"test");
            var struck=data.page(id,2);
            h.assertTrue(struck.notes().size()==1&&struck.notes().get(0).mark()==JournalPage.Mark.STRUCK&&struck.notes().get(0).word().equals("Tess")&&struck.notes().get(0).use()==0,"after the trailer's night it strikes through a name the reader wrote, one of the people they met: "+notes(struck));
            h.assertTrue(struck.text().equals("The hall was longer than it was. I followed Tess down to the water.")&&struck.notes().get(0).anchor(struck.text())>=0,"the word stays under the stroke");
            h.assertTrue(data.inked(id)==(int)(s.overworld().getDayTime()/24000L)+1,"and the day is spent");
            var registries=h.getLevel().registryAccess();var back=Journals.load(data.save(new CompoundTag(),registries),registries);
            h.assertTrue(back.page(id,2).equals(struck)&&back.page(id,3).equals(blank)&&back.inked(id)==data.inked(id),"the other hand's ink and its day survive a save and reload");
            CoAuthor.certain(id,false);data.inked(id,0);int wrote=0;
            for(int i=0;i<600;i++){data.inked(id,0);if(CoAuthor.consider(p,CoAuthor.Trigger.SLEEP)==CoAuthor.Wrote.WROTE)wrote++;data.put(id,2,kept("The hall was longer than it was. I followed Tess down to the water.",true));data.put(id,3,JournalPage.BLANK);}
            h.assertTrue(wrote>60&&wrote<150,"left to chance it writes about one time in six: "+wrote+" of 600");
        }finally{CoAuthor.certain(id,false);NativeTestPlayers.remove(p);}
        h.succeed();
    }

    @GameTest(template="empty",batch="journal_other_hand_open",timeoutTicks=200)
    public static void theOtherHandNeverWritesOnAnOpenPageOnlyAddsAndItsStrokeGoesWithTheWord(GameTestHelper h){
        var p=NativeTestPlayers.survival(h,"journal_other_open");var s=h.getLevel().getServer();var id=p.getUUID();var data=Journals.get(s);
        var mine=data.issue(p);p.getInventory().add(mine.copy());CoAuthor.certain(id,true);LabyrinthData.get(s).visit(id,LabyrinthPlace.LAKE_HOUSE);
        String left="We went left at the stair.",tess="Tess went first. I followed Tess to the lake.";
        data.put(id,4,kept(left,true));data.put(id,6,kept(tess,true));data.put(id,7,kept("Home again.",false));
        CoAuthor.viewing(p,2);
        h.assertTrue(CoAuthor.write(p,CoAuthor.Trigger.BLACKOUT)==CoAuthor.Wrote.WROTE,"it finds somewhere to write");
        var six=data.page(id,6);
        h.assertTrue(data.page(id,4).notes().isEmpty(),"never on a page the reader has open");
        h.assertTrue(six.notes().size()==1&&six.notes().get(0).mark()==JournalPage.Mark.STRUCK&&six.notes().get(0).use()==1,"a name that opens a sentence may be any word, so it strikes the one inside a sentence: "+notes(six));
        data.inked(id,0);CoAuthor.viewing(p,-1);
        h.assertTrue(CoAuthor.write(p,CoAuthor.Trigger.BLACKOUT)==CoAuthor.Wrote.WROTE,"the journal shut, the page is free");
        var four=data.page(id,4);
        h.assertTrue(four.notes().size()==1&&four.notes().get(0).mark()==JournalPage.Mark.DIRECTION&&four.notes().get(0).text().equals("No. Right.")&&four.notes().get(0).word().equals("left"),"under a line that went left it answers right: "+notes(four));
        data.inked(id,0);
        h.assertTrue(CoAuthor.write(p,CoAuthor.Trigger.BLACKOUT)==CoAuthor.Wrote.NOWHERE&&data.inked(id)==0,"with nothing left to answer it writes nothing, and the day is not spent");
        h.assertTrue(data.page(id,4).text().equals(left)&&data.page(id,6).text().equals(tess)&&data.page(id,7).notes().isEmpty(),"none of the reader's words were taken");
        var sent=page("A reader's own page.");var ink=new ListTag();var forged=new CompoundTag();forged.putString("Mark","written");forged.putString("Text","forged");forged.putFloat("X",.2F);forged.putFloat("Y",.2F);ink.add(forged);sent.put("Notes",ink);
        h.assertTrue(Journals.edit(p,6,page("Tess went first. I followed her to the lake."))==Journals.Edit.ACCEPTED&&Journals.edit(p,4,page("We went on at the stair."))==Journals.Edit.ACCEPTED&&Journals.edit(p,8,sent)==Journals.Edit.ACCEPTED,"the reader rewrites both lines");
        h.startSequence().thenWaitUntil(()->h.assertTrue(data.page(id,6).text().endsWith("her to the lake.")&&data.page(id,4).text().contains("on at")&&!data.page(id,8).empty(),"the edits pass the text filter"))
        .thenExecute(()->{
            h.assertTrue(data.page(id,6).notes().isEmpty(),"the stroke goes with the word it was through");
            h.assertTrue(data.page(id,4).notes().size()==1&&data.page(id,4).notes().get(0).anchor(data.page(id,4).text())<0,"the answer is ink and stays when its line is gone");
            h.assertTrue(data.page(id,8).notes().isEmpty(),"a reader's screen cannot write in the other hand");
            CoAuthor.certain(id,false);CoAuthor.viewing(p,-1);NativeTestPlayers.remove(p);
        }).thenSucceed();
    }

    // ------------------------------------------------------------------ 0.4.199: torn pages (TornPages)
    static int tornCount(ServerPlayer p){int n=0;for(var s:p.getInventory().items)if(TornPages.ref(s)!=null)n++;return n;}
    /** Takes a reader's torn copy of a page out of their pack, to put in their hand. */
    static ItemStack tornIn(ServerPlayer p,int page){var inv=p.getInventory();for(int i=0;i<inv.getContainerSize();i++){var ref=TornPages.ref(inv.getItem(i));if(ref!=null&&ref.page()==page)return inv.removeItemNoUpdate(i);}return ItemStack.EMPTY;}
    /** The reader's own hand puts the page against a block face, as a right-click would. */
    static net.minecraft.world.InteractionResult pin(ServerPlayer p,ItemStack page,net.minecraft.core.BlockPos support,net.minecraft.core.Direction face){
        p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,page);
        var hit=new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(support).add(face.getStepX()*.5,face.getStepY()*.5,face.getStepZ()*.5),face,support,false);
        return page.useOn(new net.minecraft.world.item.context.UseOnContext(p,net.minecraft.world.InteractionHand.MAIN_HAND,hit));
    }
    static java.util.List<io.github.knaitoe.theoldesthouse.journal.PinnedPage> pinnedBy(net.minecraft.server.level.ServerLevel l,ServerPlayer p,net.minecraft.core.BlockPos near){
        return l.getEntitiesOfClass(io.github.knaitoe.theoldesthouse.journal.PinnedPage.class,new net.minecraft.world.phys.AABB(near).inflate(24),e->e.ref().owner().equals(p.getUUID()));
    }

    /** 0.4.199: a written page tears out, the stub stays, anyone holding the page can read it, and its writer can always take it back. */
    @GameTest(template="empty",batch="journal_tear",timeoutTicks=300)
    public static void aWrittenPageTearsOutLeavingItsStubAndItsWriterCanTakeItBack(GameTestHelper h){
        var p=NativeTestPlayers.survival(h,"journal_tearer");var peer=NativeTestPlayers.survival(h,"journal_page_reader");
        var data=Journals.get(h.getLevel().getServer());var id=p.getUUID();
        h.startSequence().thenExecute(()->{
            p.getInventory().add(data.issue(p));
            data.put(id,3,JournalPage.load(page("The lamp hall again.\nLonger than it was.")));
            h.assertTrue(TornPages.tear(peer,3)==TornPages.Tear.NOT_CARRIED,"only a reader carrying their own journal tears from it");
            h.assertTrue(TornPages.tear(p,4)==TornPages.Tear.BLANK,"a blank page does not tear out");
            h.assertTrue(TornPages.tear(p,3)==TornPages.Tear.TEARING,"a written page tears out");
        }).thenWaitUntil(()->h.assertTrue(tornCount(p)==1,"the torn page is in the reader's hands once its words have passed the filter")).thenExecute(()->{
            var stack=tornIn(p,3);var ref=TornPages.ref(stack);
            h.assertTrue(ref!=null&&ref.owner().equals(id)&&TornPages.nameOf(stack).equals("journal_tearer")&&stack.getHoverName().getString().equals("Torn from journal_tearer's journal"),"it is torn from the reader's journal, and says so: "+stack.getHoverName().getString());
            h.assertTrue(data.torn(id,3)&&data.page(id,3).text().isEmpty()&&data.contents(id).getIntArray("Torn").length==1,"the journal keeps the stub, and the words have gone with the page");
            h.assertTrue(TornPages.page(h.getLevel().getServer(),ref).text().equals("The lamp hall again.\nLonger than it was."),"the page carries its words, unaltered");
            h.assertTrue(Journals.edit(p,3,page("over the stub"))==Journals.Edit.TORN&&TornPages.tear(p,3)==TornPages.Tear.ALREADY,"nothing is written on a stub, and it does not tear twice");
            // Handed to someone else, anyone can read it.
            peer.getInventory().add(stack);
            h.assertTrue(TornPages.read(peer,ref,TornPages.nameOf(stack)),"whoever holds it can read it");
            // Its writer takes it back from the stub: the page goes home, and the copy in someone else's pocket goes blank.
            h.assertTrue(TornPages.reclaim(p,3)&&!data.torn(id,3)&&data.page(id,3).text().equals("The lamp hall again.\nLonger than it was."),"its writer takes it back into its stub, every word");
            h.assertTrue(!TornPages.valid(h.getLevel().getServer(),ref)&&!TornPages.read(peer,ref,"journal_tearer"),"the page someone else held is blank now");
            var held=peer.getInventory().items.stream().filter(s->TornPages.ref(s)!=null).findFirst().orElseThrow();
            held.getItem().inventoryTick(held,h.getLevel(),peer,(int)(h.getLevel().getGameTime()%20),false);
            h.assertTrue(held.isEmpty(),"a blank torn page does not stay in a pocket");
            // A copy is blank: only the copy the page was torn as is the page.
            h.assertTrue(TornPages.tear(p,3)==TornPages.Tear.TEARING,"torn out again");
        }).thenWaitUntil(()->h.assertTrue(tornCount(p)==1,"the page is torn out a second time")).thenExecute(()->{
            var again=p.getInventory().items.stream().filter(s->TornPages.ref(s)!=null).findFirst().orElseThrow();
            h.assertTrue(TornPages.ref(again).serial()==2&&TornPages.valid(h.getLevel().getServer(),TornPages.ref(again)),"a page torn again is a new copy");
            h.assertTrue(WitnessAccount.count(LabyrinthData.get(h.getLevel().getServer()),id)==0,"nothing about a torn page counts toward anything");
            TornPages.clear(h.getLevel().getServer(),id);NativeTestPlayers.remove(p);NativeTestPlayers.remove(peer);
        }).thenSucceed();
    }

    private static LiteraryTests.Fixture pins;
    @net.minecraft.gametest.framework.AfterBatch(batch="journal_pins") public static void pinsDone(net.minecraft.server.level.ServerLevel l){if(pins!=null){pins.close();pins=null;}}
    /**
     * 0.4.199: torn pages pin to walls and lie on tables; out in the House a reader may leave six, and a seventh sends the oldest
     * home; in the manor there is no limit. Only the writer takes one down. A page whose wall goes falls and stays where it fell,
     * and one command sends every page of a reader home.
     */
    @GameTest(template="empty",batch="journal_pins",timeoutTicks=900)
    public static void tornPagesPinAndOnlySixAreLeftOutInTheHouseBeyondTheManor(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);pins=f;var l=f.in;var server=l.getServer();
        var hall=f.origin.offset(640,0,640);var manor=f.origin.offset(io.github.knaitoe.theoldesthouse.house.HouseLayout.AXIS_X,1,io.github.knaitoe.theoldesthouse.house.HouseLayout.FRONT_DOOR_Z+3);
        f.chunks.hold(l,new net.minecraft.world.phys.AABB(hall).inflate(20));f.chunks.hold(l,new net.minecraft.world.phys.AABB(manor).inflate(12));
        var p=NativeTestPlayers.survival(h,"journal_pinner");var peer=NativeTestPlayers.survival(h,"journal_pin_peer");f.players.add(p);f.players.add(peer);
        var data=Journals.get(server);var id=p.getUUID();var first=new java.util.UUID[1];
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the hall and the manor are resident")).thenExecute(()->{
            p.teleportTo(l,hall.getX()+.5,hall.getY()+1,hall.getZ()+3.5,180,0);peer.teleportTo(l,hall.getX()+1.5,hall.getY()+1,hall.getZ()+3.5,180,0);
            for(int x=-1;x<=9;x++)for(int z=-1;z<=4;z++)l.setBlockAndUpdate(hall.offset(x,0,z),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
            for(int x=0;x<=8;x++)l.setBlockAndUpdate(hall.offset(x,1,0),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
            l.setBlockAndUpdate(manor.offset(0,0,-2),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());l.setBlockAndUpdate(manor.offset(0,-1,-2),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
            h.assertTrue(TornPages.counts(l,hall.offset(0,1,1))&&!TornPages.counts(l,manor),"pages count out in the House, and not in the manor");
            // The journal goes in the pack, so the hand the pages are pinned from never holds it.
            p.getInventory().setItem(30,data.issue(p));
            for(int i=1;i<=9;i++){data.put(id,i,JournalPage.load(page("note "+i+" by the lamp")));h.assertTrue(TornPages.tear(p,i)==TornPages.Tear.TEARING,"page "+i+" tears");}
        }).thenWaitUntil(()->h.assertTrue(tornCount(p)==9,"nine pages torn out: "+tornCount(p))).thenExecute(()->{
            for(int i=1;i<=6;i++){
                var support=hall.offset(i-1,1,0);
                h.assertTrue(pin(p,tornIn(p,i),support,net.minecraft.core.Direction.SOUTH).consumesAction()&&p.getMainHandItem().isEmpty(),"page "+i+" pins to the wall");
            }
            var pinned=pinnedBy(l,p,hall);
            h.assertTrue(pinned.size()==6&&TornPages.out(server,id)==6,"six pages pinned out in the House: "+pinned.size());
            var one=pinned.stream().filter(e->e.ref().page()==1).findFirst().orElseThrow();first[0]=one.getUUID();
            h.assertTrue(one.glance().equals("note 1 by the lamp")&&one.face()==net.minecraft.core.Direction.SOUTH&&one.getBoundingBox().minZ>=hall.getZ()+1-1e-6&&one.getBoundingBox().maxZ<=hall.getZ()+1.07,"it lies against the wall's face, its first words showing: "+one.getBoundingBox());
            pin(p,tornIn(p,7),hall.offset(0,1,0),net.minecraft.core.Direction.SOUTH);
            h.assertTrue(TornPages.ref(p.getMainHandItem())!=null&&pinnedBy(l,p,hall).size()==6,"never two pages pinned over each other");
            // The seventh, beside them: the oldest goes home.
            h.assertTrue(pin(p,p.getMainHandItem(),hall.offset(6,1,0),net.minecraft.core.Direction.SOUTH).consumesAction()&&p.getMainHandItem().isEmpty(),"a seventh page pins");
            h.assertTrue(TornPages.out(server,id)==6&&!data.torn(id,1)&&data.page(id,1).text().equals("note 1 by the lamp")&&l.getEntity(first[0])==null,"past six, the oldest page goes back into its stub, and off the wall");
            // Flat on a table top.
            var table=hall.offset(8,0,3);l.setBlockAndUpdate(table.above(),net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
            h.assertTrue(pin(p,tornIn(p,8),table,net.minecraft.core.Direction.UP).consumesAction(),"a page lies flat on a table top");
            var flat=pinnedBy(l,p,hall).stream().filter(e->e.ref().page()==8).findFirst().orElseThrow();
            h.assertTrue(flat.face()==net.minecraft.core.Direction.UP&&Math.abs(flat.getY()-(table.getY()+1))<1e-6&&!data.torn(id,2),"it lies on the table, and the next oldest has gone home");
            // In the manor there is no limit.
            p.teleportTo(l,manor.getX()+.5,manor.getY(),manor.getZ()+.5,180,0);
            h.assertTrue(pin(p,tornIn(p,9),manor.offset(0,0,-2),net.minecraft.core.Direction.SOUTH).consumesAction()&&TornPages.out(server,id)==6&&data.torn(id,3),"a page pinned in the manor counts toward nothing and sends nothing home");
            // Nobody else can take a page down; its writer, crouching, takes it back.
            var four=pinnedBy(l,p,hall).stream().filter(e->e.ref().page()==4).findFirst().orElseThrow();
            four.skipAttackInteraction(peer);four.interact(peer,net.minecraft.world.InteractionHand.MAIN_HAND);
            h.assertTrue(four.isAlive()&&data.torn(id,4),"another reader reads it, and cannot tear it down");
            p.teleportTo(l,hall.getX()+3.5,hall.getY()+1,hall.getZ()+2.5,180,0);p.setShiftKeyDown(true);four.interact(p,net.minecraft.world.InteractionHand.MAIN_HAND);p.setShiftKeyDown(false);
            h.assertTrue(!four.isAlive()&&!data.torn(id,4)&&data.page(id,4).text().equals("note 4 by the lamp"),"its writer takes it back into the journal");
            // The wall behind page five goes.
            l.setBlockAndUpdate(hall.offset(4,1,0),net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
        }).thenWaitUntil(()->h.assertTrue(pinnedBy(l,p,hall).stream().noneMatch(e->e.ref().page()==5),"with its wall gone, the page falls")).thenExecute(()->{
            var fallen=l.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new net.minecraft.world.phys.AABB(hall).inflate(12),e->{var r=TornPages.ref(e.getItem());return r!=null&&r.page()==5;});
            h.assertTrue(fallen.size()==1&&fallen.get(0).getAge()==-32768&&data.torn(id,5),"it lies where it fell, and stays there");
            h.assertTrue(TornPages.out(server,id)<=TornPages.LIMIT,"a fallen page still counts among the six");
            int sent=TornPages.clear(server,id);
            h.assertTrue(sent==6&&data.contents(id).getIntArray("Torn").length==0&&pinnedBy(l,p,hall).isEmpty()&&!fallen.get(0).isAlive()&&pinnedBy(l,p,manor).isEmpty(),"one command sends every page of a reader home: "+sent);
            for(int i=1;i<=9;i++)h.assertTrue(data.page(id,i).text().equals("note "+i+" by the lamp"),"nothing is lost: page "+i);
        }).thenSucceed();
    }
}

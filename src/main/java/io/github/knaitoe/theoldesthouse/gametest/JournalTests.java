package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.journal.JournalPage;
import io.github.knaitoe.theoldesthouse.journal.Journals;
import io.github.knaitoe.theoldesthouse.journal.Rubbings;
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

/** The reader's journal (JOURNAL_DESIGN.md): one owner's book, bounded edits kept in saved data, a lost copy that comes back worn, and pencil rubbings. */
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
        var nine=new CompoundTag[JournalPage.MAX_PLACED+1];for(int i=0;i<nine.length;i++)nine[i]=run("line "+i,"line",.5F,.5F);
        h.assertTrue(Journals.edit(p,2,page("",nine))==Journals.Edit.TOO_MUCH,"no more placed lines than a page holds");
        h.assertTrue(Journals.edit(p,2,page("",run("x","wheel",.5F,.5F)))==Journals.Edit.TOO_MUCH,"only shapes the screen makes");
        h.assertTrue(Journals.edit(p,1,page("I measured the hall twice.\nLeft at the lamp.",run("it is longer than it was","line",5,.8F),run("the house keeps the measure","spiral",.5F,.3F)))==Journals.Edit.ACCEPTED,"the owner's page is taken");
        int accepted=1;while(Journals.edit(p,3+accepted,page("more"))==Journals.Edit.ACCEPTED)accepted++;
        h.assertTrue(accepted==8,"a reader's screen has an allowance of eight pages at once: "+accepted);
        h.startSequence().thenWaitUntil(()->h.assertTrue(data.page(p.getUUID(),1).text().startsWith("I measured"),"the page is kept once it has passed the text filter"))
        .thenExecute(()->{
            var kept=data.page(p.getUUID(),1);
            h.assertTrue(kept.placed().size()==2&&kept.placed().get(0).x()==1&&kept.placed().get(1).shape()==JournalPage.Shape.SPIRAL,"placed lines keep their shapes and stay on the page: "+kept);
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
}

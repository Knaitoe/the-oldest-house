package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(TheOldestHouse.MOD_ID)
@PrefixGameTestTemplate(false)
public final class CorrespondenceTests {
    private static final class Fixture implements AutoCloseable {
        final MarginaliaTests.Fixture f;final MotherCollection prior;
        Fixture(GameTestHelper h,int coordinate){f=new MarginaliaTests.Fixture(h,coordinate,false);prior=MotherCollection.get(f.level.getServer());
            f.level.getServer().overworld().getDataStorage().set("the_oldest_house_mother",new MotherCollection());}
        @Override public void close(){f.close();f.level.getServer().overworld().getDataStorage().set("the_oldest_house_mother",prior);}
    }
    private static Fixture ordered,facts,complete,upgrade;
    @AfterBatch(batch="correspondence_order") public static void c1(ServerLevel l){if(ordered!=null){ordered.close();ordered=null;}}
    @AfterBatch(batch="correspondence_facts") public static void c2(ServerLevel l){if(facts!=null){facts.close();facts=null;}}
    @AfterBatch(batch="correspondence_complete") public static void c3(ServerLevel l){if(complete!=null){complete.close();complete=null;}}
    @AfterBatch(batch="correspondence_upgrade") public static void c4(ServerLevel l){if(upgrade!=null){upgrade.close();upgrade=null;}}
    private static String id(ItemStack book){return book.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getString("CorrespondenceId");}
    private static String text(ItemStack book){return book.get(DataComponents.WRITTEN_BOOK_CONTENT).pages().stream().map(p->p.raw().getString()).reduce("",(a,b)->a+"\n"+b);}
    private static void travel(ServerPlayer p){HouseExperience.arrived(p,LabyrinthPlace.JUNCTION);HouseExperience.returned(p);}
    private static CompoundTag own(MarginaliaTests.Fixture f,ServerPlayer p){return HouseCorrespondence.record(f.data(),p.getUUID());}

    @GameTest(template="empty",batch="correspondence_order",timeoutTicks=130)
    public static void actualMenusRequireLastPageAndTravelAndKeepFinitePersonalOriginals(GameTestHelper h){
        ordered=new Fixture(h,97100);var f=ordered.f;var p=f.player();var peer=f.player();var t=HouseMarginalia.Thread.ROOM;
        h.runAfterDelay(8,()->{
            var first=f.open(p,t);var other=f.open(peer,t);
            h.assertTrue(id(first.book()).equals("C01")&&first.book().get(DataComponents.WRITTEN_BOOK_CONTENT).pages().size()>1,"the early letter really requires a native page turn");
            h.assertTrue(!first.clickMenuButton(peer,101)&&!first.clickMenuButton(p,999),"a peer or forged page cannot finish this reader's letter");
            h.assertTrue(first.clickMenuButton(p,3)&&own(f,p).getCompound("Next").getInt("C")==0,"collecting before reading keeps the next installment locked");
            ItemStack kept=p.getInventory().items.stream().filter(s->id(s).equals("C01")).findFirst().orElseThrow().copy();
            f.depth(p,24);var alias=f.open(p,t);
            h.assertTrue(id(alias.book()).equals("C01")&&!alias.clickMenuButton(p,3),"another depth binding cannot duplicate the same original");
            f.end(p,alias);h.assertTrue(own(f,p).getCompound("Next").getInt("C")==1&&own(f,peer).getCompound("Next").getInt("C")==0&&other.getPage()==0,"native page completion is personal");
            h.assertTrue(!HouseCorrespondence.available(p,"C02"),"depth alone cannot rush an episode");
            HouseExperience.arrived(p,LabyrinthPlace.JUNCTION);h.assertTrue(!HouseCorrespondence.available(p,"C02"),"one crossing is insufficient");
            HouseExperience.returned(p);h.assertTrue(HouseCorrespondence.available(p,"C02"),"two actual arrival/return callbacks unlock the due letter");
            var before=own(f,p);HouseCorrespondence.samples(p);h.assertTrue(before.equals(own(f,p)),"all 108 specimens are read-only previews");
            f.reload();h.assertTrue(own(f,p).getLong("Step")==2&&own(f,p).getCompound("Next").getInt("C")==1,"native reload preserves journey and sequence");
            h.assertTrue(p.getInventory().items.stream().anyMatch(s->ItemStack.isSameItemSameComponents(s,kept)),"an early collected original is never rewritten");
            p.gameMode.changeGameModeForPlayer(GameType.SPECTATOR);
            h.assertTrue(p.gameMode.getGameModeForPlayer()==GameType.SPECTATOR,"the native game mode changed before the observer check");
            HouseCorrespondence.crossed(p);h.assertTrue(own(f,p).getLong("Step")==2,"a spectator crossing cannot advance the saved journey");
            h.assertTrue(!alias.clickMenuButton(p,101),"an observer cannot finish the old native reader menu");
            h.assertTrue(WitnessAccount.count(f.data(),p.getUUID())==0,"hostile or tender paper confers no story resolution");h.succeed();
        });
    }

    private static void sourceFacts(MarginaliaTests.Fixture f,ServerPlayer p){
        f.depth(p,24);HouseExperience.arrived(p,LabyrinthPlace.DROWNED_TOWN);HouseExperience.returned(p);HouseCorrespondence.returnedSafely(p,LabyrinthPlace.DROWNED_TOWN.id());travel(p);
        var dog=EntityType.WOLF.create(f.level);dog.setTame(true,true);dog.setOwnerUUID(p.getUUID());dog.setCustomName(Component.literal("Moss"));HouseExperience.cared(p,dog);
        // Persisted source contracts are populated by their own native gameplay suites; this suite tests attribution.
        var camp=f.data().state(HollowayVignette.ID);var players=camp.getCompound("Players");var one=new CompoundTag();one.putBoolean("Looted",true);players.put(p.getUUID().toString(),one);camp.put("Players",players);f.data().setState(HollowayVignette.ID,camp);
        for(int x=0;x<2;x++){var at=f.base.offset(x,0,-8);f.level.setBlock(at.below(),Blocks.STONE.defaultBlockState(),2);
            NavigationAids.placeLine(f.level,at);NavigationAids.remember(f.level,at,p.getUUID(),false);}
        var photo=new ItemStack(Items.FILLED_MAP);var custom=new CompoundTag();custom.putUUID(NovelVignettes.PHOTO_OWNER,p.getUUID());custom.putUUID(NovelVignettes.PHOTO_ID,UUID.randomUUID());photo.set(DataComponents.CUSTOM_DATA,CustomData.of(custom));
        var novel=NovelVignettes.personal(f.data(),p.getUUID());novel.put("Photo",photo.save(p.registryAccess()));NovelVignettes.save(f.data(),p.getUUID(),novel);p.getInventory().setItem(6,photo);
        var drawer=f.origin.offset(8,1,22);f.level.setBlock(drawer,Blocks.BARREL.defaultBlockState(),2);
        var reply=new ItemStack(Items.WRITABLE_BOOK);reply.set(DataComponents.WRITABLE_BOOK_CONTENT,new WritableBookContent(List.of(Filterable.passThrough("I have come back. Let the kettle cool."))));
        // Native manor occupancy confirms two returns, independently of nested hallway backtracking.
        for(int visit=0;visit<3;visit++){
            p.moveTo(f.base.getX()+.5,f.base.getY(),f.base.getZ()-8.5);HouseExperience.tickPlayer(p);
            p.moveTo(HomeLetters.desk(f.origin).getX()+.5,HomeLetters.desk(f.origin).getY(),HomeLetters.desk(f.origin).getZ()+1.5);HouseExperience.tickPlayer(p);
        }
        p.setItemInHand(InteractionHand.MAIN_HAND,reply);
        f.h.assertTrue(HomeLetters.reply(p,f.origin),"an actual nonblank original reply is submitted to the native drawer");
        var object=new ItemStack(Items.IRON_AXE);object.set(DataComponents.CUSTOM_NAME,Component.literal("Winter coat"));MotherCollection.get(p.server).keepVignetteItem(object,p.registryAccess(),p.getUUID(),f.level.getGameTime());
    }

    @GameTest(template="empty",batch="correspondence_facts",timeoutTicks=130)
    public static void conditionalLettersRejectPeersStaleMarksBorrowedPhotosAndRecoveredCustody(GameTestHelper h){
        facts=new Fixture(h,97400);var f=facts.f;var p=f.player();var peer=f.player();f.depth(p,24);f.depth(peer,24);
        h.runAfterDelay(8,()->{
            for(int i=1;i<=8;i++)h.assertTrue(!HouseCorrespondence.available(p,"P0"+i),"no invented personal fact "+i);
            h.assertTrue(!HouseCorrespondence.available(p,"L01"),"the missing-possession chain waits for an actual loss");
            sourceFacts(f,p);
            for(int i=1;i<=8;i++){h.assertTrue(HouseCorrespondence.available(p,"P0"+i),"actual native fact enables its letter "+i);
                h.assertTrue(!HouseCorrespondence.available(peer,"P0"+i),"the same fact cannot be attributed to a peer "+i);}
            f.level.setBlock(f.base.offset(1,0,-8),Blocks.AIR.defaultBlockState(),2);
            h.assertTrue(!HouseCorrespondence.available(p,"P04"),"a saved marker whose real block is gone does not count");
            var photo=p.getInventory().getItem(6);p.getInventory().setItem(6,ItemStack.EMPTY);peer.getInventory().setItem(6,photo);
            h.assertTrue(!HouseCorrespondence.available(p,"P06")&&!HouseCorrespondence.available(peer,"P06"),"historical collection and borrowed custody do not claim retention");p.getInventory().setItem(6,photo);peer.getInventory().setItem(6,ItemStack.EMPTY);
            var forged=photo.copy();CustomData.update(DataComponents.CUSTOM_DATA,forged,t->t.putUUID(NovelVignettes.PHOTO_ID,UUID.randomUUID()));p.getInventory().setItem(6,forged);
            h.assertTrue(!HouseCorrespondence.available(p,"P06"),"another photo ID with the right owner cannot replace the original");
            var held=MotherCollection.get(p.server).all().stream().filter(e->p.getUUID().equals(e.owner)&&!e.pet).findFirst().orElseThrow();
            h.assertTrue(!MotherCollection.get(p.server).claimItem(p.getUUID(),held.id,p.registryAccess()).isEmpty()&&!HouseCorrespondence.available(p,"P08"),"successful native recovery ends a new current-custody claim");
            h.assertTrue(WitnessAccount.count(f.data(),p.getUUID())==0&&WitnessAccount.count(f.data(),peer.getUUID())==0,"paper eligibility and ordinary recovery add no Witness");h.succeed();
        });
    }

    @GameTest(template="empty",batch="correspondence_complete",timeoutTicks=160)
    public static void wholePoolIsReachableInOrderWithoutChangingSignedRepliesOrStoryAuthority(GameTestHelper h){
        complete=new Fixture(h,97700);var f=complete.f;var p=f.player();
        h.runAfterDelay(8,()->{
            sourceFacts(f,p);var drawer=(BarrelBlockEntity)f.level.getBlockEntity(f.origin.offset(8,1,22));ItemStack reply=drawer.getItem(0).copy();
            var seen=new HashSet<String>();var next=new HashMap<String,Integer>();
            for(int iteration=0;iteration<125;iteration++){
                var menu=f.open(p,HouseMarginalia.Thread.values()[iteration%4]);String id=id(menu.book());
                h.assertTrue(seen.add(id),"the expanded pool is reachable without repeated filler: "+id);
                if(id.matches("[A-N]\\d{2}")){
                    String chain=id.substring(0,1);int n=Integer.parseInt(id.substring(1));
                    h.assertTrue(n==next.getOrDefault(chain,1),"each chain reaches its actual next installment: "+id);next.put(chain,n+1);
                }
                f.end(p,menu);travel(p);travel(p);
            }
            h.assertTrue(seen.size()==125&&seen.stream().filter(id->!id.startsWith("R_")).count()==108,"all 108 new pieces and all 17 original installments can be discovered");
            StringBuilder acrostic=new StringBuilder();for(String sentence:text(HouseCorrespondence.preview(p,"B04")).split("\\."))if(!sentence.isBlank())acrostic.append(sentence.strip().charAt(0));
            h.assertTrue(acrostic.toString().equals("LETMEOUT"),"native pagination preserves the maternal letter's optional acrostic");
            boolean[] erased={false};for(var page:HouseCorrespondence.preview(p,"L06").get(DataComponents.WRITTEN_BOOK_CONTENT).pages())page.raw().visit((style,words)->{if(style.isStrikethrough())erased[0]=true;return Optional.empty();},net.minecraft.network.chat.Style.EMPTY);
            h.assertTrue(erased[0]&&HouseCorrespondence.preview(p,"N06").get(DataComponents.WRITTEN_BOOK_CONTENT).author().equals("A correspondent"),"the correction is visibly crossed out and identity claims retain their actual fictional author");
            for(char chain='A';chain<='N';chain++)h.assertTrue(own(f,p).getCompound("Next").getInt(String.valueOf(chain))==6,"the complete six-letter sequence was read: "+chain);
            h.assertTrue(ItemStack.isSameItemSameComponents(reply,drawer.getItem(0)),"appropriating prose never changes the submitted player's original reply");
            var ending=f.open(p,HouseMarginalia.Thread.CALLS);int count=own(f,p).getInt("ReadCount");f.end(p,ending);
            h.assertTrue(own(f,p).getInt("ReadCount")==count&&own(f,p).getCompound("Books").size()==125,"exhaustion revisits a finite saved original");
            f.reload();h.assertTrue(own(f,p).getCompound("Books").size()==125&&own(f,p).getInt("ReadCount")==125,"the whole finite catalogue survives native serialization");
            h.assertTrue(WitnessAccount.count(f.data(),p.getUUID())==0&&WitnessAccount.Story.values().length==43&&WitnessAccount.REQUIRED==33,"the new human voices do not change sources, gates or endings");h.succeed();
        });
    }

    @GameTest(template="empty",batch="correspondence_upgrade",timeoutTicks=130)
    public static void savedLegacyPageRemainsExactBeforeNewCorrespondenceAppears(GameTestHelper h){
        upgrade=new Fixture(h,98000);var f=upgrade.f;var p=f.player();var t=HouseMarginalia.Thread.CALLS;f.seedLegacy(p,t,f.surface(t));
        h.runAfterDelay(8,()->{
            var old=f.open(p,t);ItemStack book=old.book();f.end(p,old);h.assertTrue(old.clickMenuButton(p,3),"the native reader collects the old saved original");
            var oldRecord=HouseMarginalia.record(f.data(),p.getUUID());var block=f.level.getBlockState(f.surface(t));travel(p);
            var fresh=f.open(p,t);h.assertTrue(id(fresh.book()).equals("A01")&&!ItemStack.isSameItemSameComponents(book,fresh.book()),"a later real visit can meet the expanded correspondence");
            f.reload();var restored=f.open(p,t);
            h.assertTrue(ItemStack.isSameItemSameComponents(fresh.book(),restored.book())&&oldRecord.equals(HouseMarginalia.record(f.data(),p.getUUID())),"reload keeps the new snapshot without rewriting any old saved bindings");
            h.assertTrue(p.getInventory().items.stream().anyMatch(s->ItemStack.isSameItemSameComponents(book,s))&&block==f.level.getBlockState(f.surface(t)),"collected original components and the physical paper stay intact");h.succeed();
        });
    }
}

package io.github.knaitoe.theoldesthouse.house;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.*;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/** Six original domestic letters, with per-reader progress and replies in the original study barrel. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class HomeLetters {
    public static final int COUNT=6;
    private HomeLetters(){}
    public static BlockPos desk(BlockPos origin){return origin.offset(10,1,22);}
    public static int available(CompoundTag own){
        int n=0;if(own.contains("Seat")||own.getInt("Sleeps")>0||own.contains("Preparation"))n=1;
        if(own.getInt("Returns")>0)n=2;if(own.getInt("Returns")>=2&&own.getInt("Care")>0)n=3;
        if(own.getInt("Returns")>=3)n=4;if(own.getInt("Deepest")>=12&&own.getCompound("ReplySent").size()>0)n=5;return n;
    }
    @SubscribeEvent(priority=EventPriority.HIGH) public static void interact(PlayerInteractEvent.RightClickBlock e){
        if(e.isCanceled()||e.getHand()!=InteractionHand.MAIN_HAND||!(e.getEntity() instanceof ServerPlayer p)||p.isSpectator()||p.distanceToSqr(e.getPos().getCenter())>25||!HouseExperience.inManor(p,e.getPos()))return;
        var origin=HouseSavedData.get(p.server).houseOrigin();if(origin==null||!e.getPos().equals(desk(origin))||!(p.level().getBlockEntity(e.getPos()) instanceof LecternBlockEntity lectern)||!lectern.getBook().isEmpty())return;
        var hand=p.getMainHandItem();
        if(hand.has(DataComponents.WRITABLE_BOOK_CONTENT)||hand.has(DataComponents.WRITTEN_BOOK_CONTENT)){
            e.setCanceled(true);e.setCancellationResult(InteractionResult.SUCCESS);reply(p,origin);return;
        }
        if(!hand.isEmpty())return;e.setCanceled(true);e.setCancellationResult(InteractionResult.SUCCESS);
        p.openMenu(new SimpleMenuProvider((id,inv,player)->new LetterMenu(id,p,desk(origin)),Component.literal("Letters on the study desk")));
    }
    public static boolean reply(ServerPlayer p,BlockPos origin){
        if(!HouseExperience.inManor(p,desk(origin))||p.distanceToSqr(desk(origin).getCenter())>25)return false;
        ItemStack hand=p.getMainHandItem();var writable=hand.get(DataComponents.WRITABLE_BOOK_CONTENT);var written=hand.get(DataComponents.WRITTEN_BOOK_CONTENT);
        if(writable==null&&written==null||writable!=null&&writable.pages().stream().allMatch(page->page.raw().isBlank())||written!=null&&written.pages().stream().allMatch(page->page.raw().getString().isBlank()))return false;
        var custom=hand.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();if(custom.contains("HomeLetter"))return false;
        var d=LabyrinthData.get(p.server);var own=HouseExperience.record(d,p.getUUID());int stage=Math.min(COUNT-1,Math.max(0,own.getInt("LetterNext")-1));var replies=own.getCompound("ReplySent");if(replies.getBoolean(Integer.toString(stage)))return false;
        if(!(p.level().getBlockEntity(origin.offset(8,1,22)) instanceof BarrelBlockEntity drawer))return false;
        for(int slot=0;slot<drawer.getContainerSize();slot++)if(drawer.getItem(slot).isEmpty()){
            ItemStack original=hand.split(1);drawer.setItem(slot,original);drawer.setChanged();
            replies.putBoolean(Integer.toString(stage),true);own.put("ReplySent",replies);own.putString("ReplyTitle",written==null?"your unsigned pages":written.title().raw());HouseExperience.save(d,p.getUUID(),own);p.inventoryMenu.broadcastChanges();
            p.serverLevel().playSound(null,desk(origin),SoundEvents.BOOK_PUT,SoundSource.BLOCKS,.5F,1);return true;
        }return false;
    }
    private static ItemStack letter(ServerPlayer p,int stage){
        var d=LabyrinthData.get(p.server);var own=HouseExperience.record(d,p.getUUID());var books=own.getCompound("HomeLetters");String key=Integer.toString(stage);if(books.contains(key))return ItemStack.parseOptional(p.registryAccess(),books.getCompound(key));
        var pages=new ArrayList<String>(switch(stage){
            case 0->List.of("Will,\n\nI moved the kettle. It was dripping on the letters. There is bread in the kitchen and a clean blanket upstairs.\n\nCome back while it is still ordinary.","I have left the study desk clear.\n\nYou can leave an answer in the drawer beside it. I would like one that is about something other than measurements.");
            case 1->List.of("The chair has a loose thread. I keep finding it caught on my sleeve.\n\nI thought I would cut it. Then I thought you might notice it was gone.","I washed two cups this morning.\n\nYou were downstairs, or you had already gone.\n\nThose are different mornings. I cannot get them into the right order.");
            case 2->List.of("I feel petulant. Are these even reaching you?\n\nI am not asking how far you went. I am asking whether you read what I left on the desk.","You can turn back with something unfinished.\n\nLeave your muddy shoes at the door. We will still have to clean them in the morning.");
            case 3->List.of("There was a warm place on the rug after the animal got up.\n\nI stepped around it as if it were still occupied.\n\nIt is strange what a room learns from a little weight.","You have touched something here gently.\n\nPlease remember that when a sound in the dark asks you to hurry.\n\nI am trying to remember it too.");
            case 4->List.of("I wrote COME HOME, then crossed out COME.\n\nIt sounded like a command. I meant that there would be somewhere to put your coat.","The first letter says the kettle was dripping.\n\nI checked the desk. The mark is old.\n\nThe kettle is still warm.\n\nPlease read the first letter again.");
            default->List.of("I found your pages.\n\nI read them at the kitchen table with the door open.\n\nFor once I let the hallway remain unmeasured.","If you can hear me, follow the voice only as far as you know the person who made it.\n\nAfter that, turn on a light.\n\nI will leave mine on.");
        });
        var traces=HouseExperience.traces(p);if(stage>=2&&!traces.isEmpty())pages.add("In the margin, a different hand:\n\n"+traces.get(Math.floorMod(stage,traces.size())));
        if(stage==5&&!own.getString("ReplyTitle").isEmpty())pages.add("Your answer was kept as "+own.getString("ReplyTitle")+".\n\nThe original is still in the drawer unless you took it back. I did not make another.");
        ItemStack book=HouseWriting.book("At home, "+(stage+1),"Karen Green",HouseWriting.WritingStyle.KAREN,pages);CustomData.update(DataComponents.CUSTOM_DATA,book,t->{t.putUUID("LetterTo",p.getUUID());t.putInt("HomeLetter",stage);});
        books.put(key,book.save(p.registryAccess()));own.put("HomeLetters",books);HouseExperience.save(d,p.getUUID(),own);return book;
    }
    public static final class LetterMenu extends LecternMenu {
        private final ServerPlayer reader;private final BlockPos desk;private final ItemStack book;private final int stage,pages;
        public LetterMenu(int id,ServerPlayer p,BlockPos pos){this(id,p,pos,new SimpleContainer(1));}
        private LetterMenu(int id,ServerPlayer p,BlockPos pos,SimpleContainer display){
            super(id,display,new SimpleContainerData(1));reader=p;desk=pos.immutable();var own=HouseExperience.record(LabyrinthData.get(p.server),p.getUUID());stage=Math.min(available(own),Math.min(COUNT-1,own.getInt("LetterNext")));book=letter(p,stage);display.setItem(0,book.copy());pages=book.get(DataComponents.WRITTEN_BOOK_CONTENT).pages().size();
        }
        @Override public boolean stillValid(Player p){return p==reader&&reader.isAlive()&&reader.distanceToSqr(desk.getCenter())<25&&HouseExperience.inManor(reader,desk)&&reader.level().getBlockEntity(desk) instanceof LecternBlockEntity lectern&&lectern.getBook().isEmpty();}
        @Override public boolean clickMenuButton(Player p,int button){
            if(!stillValid(p))return false;var d=LabyrinthData.get(reader.server);var own=HouseExperience.record(d,reader.getUUID());
            if(button==3){var taken=own.getCompound("LetterTaken");String key=Integer.toString(stage);if(taken.getBoolean(key))return false;
                taken.putBoolean(key,true);own.put("LetterTaken",taken);HouseExperience.save(d,reader.getUUID(),own);var original=book.copy();if(!reader.getInventory().add(original))reader.drop(original,false);reader.inventoryMenu.broadcastChanges();return true;}
            if(button>=100){if(button-100>=pages)return false;}else if(button==1){if(getPage()<=0)return false;}else if(button==2){if(getPage()>=pages-1)return false;}else return false;
            boolean changed=super.clickMenuButton(p,button);if(changed&&getPage()==pages-1){own.putInt("LetterNext",Math.max(own.getInt("LetterNext"),stage+1));HouseExperience.save(d,reader.getUUID(),own);}return changed;
        }
    }
}

package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/** Knowledge belongs to the explorer, never to a traded book or a world's room flags. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class WitnessAccount {
    public static final String STATE="witness_0410", BOOK_OWNER="HouseWitnessAccount";
    public static final int REQUIRED=3;
    public enum Story {
        FLOORBOARDS("floorboards", "The floor", "understanding",
                "I took up the board. The sound ended. The words underneath belonged to someone who had stayed to care for him."),
        CLAP("hide_and_clap", "The wardrobe", "survival",
                "The game ended at the wardrobe. There was a drawing inside. A child had left a way to remember the room."),
        HARRIGAN("harrigan", "The study", "connection",
                "Reading had given us somewhere to sit together. The telephone was still there when his voice was gone."),
        MODEL_HOME("model_home", "The window", "survival",
                "The tree had crossed the glass. A room sold as a home could no longer keep the outside out."),
        MOTHER("mother_of_strays", "The keeper", "release",
                "She let something go. For a moment, keeping it safe and keeping it forever were different things.");
        public final String id, title, kind, text;
        Story(String id,String title,String kind,String text){this.id=id;this.title=title;this.kind=kind;this.text=text;}
        public static @Nullable Story of(String id){for(Story story:values())if(story.id.equals(id))return story;return null;}
    }
    private WitnessAccount(){}
    public static CompoundTag record(LabyrinthData data,UUID player){return data.state(STATE).getCompound(player.toString()).copy();}
    private static void save(LabyrinthData data,UUID player,CompoundTag record){
        CompoundTag world=data.state(STATE);world.put(player.toString(),record.copy());data.setState(STATE,world);
    }
    public static boolean has(LabyrinthData data,UUID player,Story story){return record(data,player).getCompound("Stories").contains(story.id);}
    public static int count(LabyrinthData data,UUID player){
        CompoundTag stories=record(data,player).getCompound("Stories");int count=0;
        for(Story story:Story.values())if(stories.contains(story.id))count++;return count;
    }
    public static boolean ready(LabyrinthData data,UUID player){
        CompoundTag stories=record(data,player).getCompound("Stories");Set<String> kinds=new HashSet<>();int count=0;
        for(Story story:Story.values())if(stories.contains(story.id)){count++;kinds.add(story.kind);}
        return count>=REQUIRED&&kinds.size()>=2;
    }
    public static boolean resolve(LabyrinthData data,UUID player,Story story,String outcome){
        if(has(data,player,story))return false;
        CompoundTag record=record(data,player),stories=record.getCompound("Stories");
        stories.putString(story.id,outcome);record.put("Stories",stories);save(data,player,record);return true;
    }
    public static void resolve(ServerPlayer player,Story story,String outcome){
        if(player.isSpectator()||FinaleProgress.terminal(FinaleProgress.phase(player.server,player.getUUID())))return;
        LabyrinthData data=LabyrinthData.get(player.server);
        if(resolve(data,player.getUUID(),story,outcome)){
            updateBook(player,true);
            player.displayClientMessage(Component.literal(ready(data,player.getUUID())
                    ?"A passage in your account is readable now. It describes a cell."
                    :"You can set this part of the account down now."),false);
        }
    }
    public static boolean readPlay(LabyrinthData data,UUID player){return record(data,player).getBoolean("ReadPlay");}
    public static void markRead(LabyrinthData data,UUID player){
        if(!ready(data,player))return;CompoundTag record=record(data,player);record.putBoolean("ReadPlay",true);save(data,player,record);
    }
    public static boolean ownedBook(ItemStack stack,UUID player){
        CustomData custom=stack.get(DataComponents.CUSTOM_DATA);return custom!=null&&custom.copyTag().hasUUID(BOOK_OWNER)
                &&player.equals(custom.copyTag().getUUID(BOOK_OWNER));
    }
    public static ItemStack book(LabyrinthData data,UUID player,String name,boolean epilogue){
        List<Component> pages=new ArrayList<>();
        pages.add(HouseWriting.page(HouseWriting.WritingStyle.WILL,
                "AN ACCOUNT\n\nI am writing down what I saw, before I begin remembering something else.\n\nThere are still gaps."));
        CompoundTag stories=record(data,player).getCompound("Stories");
        for(Story story:Story.values())if(stories.contains(story.id)){
            String text=stories.getString(story.id).equals("aftermath")
                    ?"Someone reached this room before me. I examined what remained.\n\n"+switch(story){
                        case FLOORBOARDS->"The board was gone. The floor was quiet. There had been somebody waiting beside the bed.";
                        case CLAP->"The wardrobe stood open. The game had ended here. Something had been left inside it.";
                        case HARRIGAN->"The casket was closed. I remembered the study, and the chair where someone had listened.";
                        case MODEL_HOME->"The tree was inside the window. The glass had failed to keep the world outside.";
                        case MOTHER->"The shelves remained, but she had stopped keeping the things upon them.";
                    }:story.text;
            pages.add(HouseWriting.page(HouseWriting.WritingStyle.WILL,story.title+"\n\n"+text));
        }
        int n=count(data,player);
        if(n>0)pages.add(HouseWriting.page(HouseWriting.WritingStyle.ZAMPANO,
                "A PLAY WITHOUT AN AUDIENCE\n\nTHE PRISONER: Who remembers the room when its door is closed?\n\n[The next line has been crossed out.]"));
        if(n>=2)pages.add(HouseWriting.page(HouseWriting.WritingStyle.ZAMPANO,
                "THE WITNESS: I stayed long enough to hear the end.\n\nTHE KEEPER: ")
                .copy().append(Component.literal("There is no one inside.").withStyle(s->s.withColor(0xA52A2A).withStrikethrough(true))));
        if(ready(data,player)){
            pages.add(HouseWriting.page(HouseWriting.WritingStyle.ZAMPANO,
                    "THE WITNESS: There is someone inside.\n\n[The weapon is laid beside the cell. The witness bows, with both hands empty, and opens the door.]"));
            pages.add(HouseWriting.page(HouseWriting.WritingStyle.WILL,
                    "The weapon is the original. The old man can tell you which.\n\nRead the play at the cell's lectern. Lay the weapon nearby. Crouch and open the bars with empty hands.\n\nLeave the door open."));
        }
        if(epilogue){
            pages.add(HouseWriting.page(HouseWriting.WritingStyle.WILL,
                    "I returned with the things I carried. The day was still the same day.\n\nI have left a space here for what passed me on the stairs."));
            pages.add(HouseWriting.page(HouseWriting.WritingStyle.ZAMPANO,
                    "From inside the cell\n\nI heard "+name+" put the weapon down.\n\nThe door opened. For once, there was someone on the other side who had stayed to listen."));
        }
        ItemStack book=HouseWriting.book(epilogue?"The completed account":"An account",name,pages);
        CustomData.update(DataComponents.CUSTOM_DATA,book,tag->tag.putUUID(BOOK_OWNER,player));return book;
    }
    public static void updateBook(ServerPlayer player,boolean give){
        ItemStack book=book(LabyrinthData.get(player.server),player.getUUID(),player.getGameProfile().getName(),false);boolean found=false;
        for(int i=0;i<player.getInventory().getContainerSize();i++)if(ownedBook(player.getInventory().getItem(i),player.getUUID())){
            player.getInventory().setItem(i,book.copy());found=true;
        }
        if(ownedBook(player.containerMenu.getCarried(),player.getUUID())){player.containerMenu.setCarried(book.copy());found=true;}
        if(give&&!found&&!player.getInventory().add(book))player.drop(book,false);
        player.inventoryMenu.broadcastChanges();
    }
    /** Later explorers must inspect a resolved room's ending prop themselves. */
    public static boolean aftermathTarget(LabyrinthData data,LabyrinthPlace place,BlockPos base,BlockPos at){
        if(!data.isCompleted(place.id()))return false;BlockPos rel=at.subtract(base);
        return switch(place){
            case FLOORBOARDS->rel.equals(TellTaleFloorboards.LOOSE_BOARD.below());
            case HIDE_AND_CLAP->{CompoundTag state=data.state(HideAndClap.ID);
                if(!state.getBoolean("Opened")||!state.contains("Wardrobe"))yield false;
                BlockPos wardrobe=BlockPos.of(state.getLong("Wardrobe"));yield rel.equals(wardrobe)||rel.equals(wardrobe.above());}
            case HARRIGAN->rel.distManhattan(HarriganVignette.CASKET)<=1;
            case MODEL_HOME->rel.equals(ModelHome.WINDOW)||rel.equals(ModelHome.WINDOW.above());
            default->false;
        };
    }
    @SubscribeEvent(priority=EventPriority.HIGH) public static void inspect(PlayerInteractEvent.RightClickBlock event){
        if(!(event.getEntity() instanceof ServerPlayer player)||player.isSpectator()||event.getHand()!=InteractionHand.MAIN_HAND
                ||!player.serverLevel().dimension().equals(HouseDimensions.INTERIOR))return;
        BlockPos origin=HouseSavedData.get(player.server).houseOrigin();if(origin==null)return;
        LabyrinthData data=LabyrinthData.get(player.server);
        if(event.getPos().equals(FinaleArchitecture.lectern(origin))
                &&!FinaleProgress.committed(FinaleProgress.phase(player.server,player.getUUID()))
                &&player.serverLevel().getBlockEntity(event.getPos()) instanceof LecternBlockEntity desk){
            // Each reading is generated from this explorer's evidence. Sharing the page never shares its unlock.
            desk.setBook(book(data,player.getUUID(),player.getGameProfile().getName(),false));desk.setChanged();
            player.serverLevel().setBlock(event.getPos(),Blocks.LECTERN.defaultBlockState().setValue(LecternBlock.HAS_BOOK,true),3);
            markRead(data,player.getUUID());updateBook(player,true);return;
        }
        LabyrinthPlace place=LabyrinthPlaces.placeAt(origin,player.blockPosition());
        if(place==null||Story.of(place.id())==null||!player.isShiftKeyDown())return;
        if(aftermathTarget(data,place,LabyrinthPlaces.base(origin,place),event.getPos())){
            resolve(player,Story.of(place.id()),"aftermath");event.setCanceled(true);event.setCancellationResult(InteractionResult.SUCCESS);
        }
    }
}

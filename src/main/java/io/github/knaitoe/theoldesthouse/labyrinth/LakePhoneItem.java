package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import java.util.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

/** The original recording, with frame times and camera bearings stored on the item itself. */
public final class LakePhoneItem extends WrittenBookItem {
    public LakePhoneItem(Properties properties){super(properties.component(DataComponents.WRITTEN_BOOK_CONTENT,
            HouseWriting.book("The phone's recording","",List.of(HouseWriting.page(HouseWriting.WritingStyle.PLAIN,"No recording yet."))).get(DataComponents.WRITTEN_BOOK_CONTENT)));}
    @Override public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand){
        ItemStack stack=player.getItemInHand(hand);
        if(player instanceof ServerPlayer owner){
            if(!PhoneCanoe.beginFilm(owner,stack)){
                CustomData custom=stack.get(DataComponents.CUSTOM_DATA);
                if(custom!=null&&custom.copyTag().getBoolean("Dropped")&&!owner.isShiftKeyDown()){
                    // 0.4.121: the recovered phone plays its clip back, frame by frame, on its cracked screen; crouch to read its record.
                    var clip=new CompoundTag();var recording=custom.copyTag();clip.putString("Name",recording.getString("Name"));clip.put("Frames",recording.getList("Footage",Tag.TAG_COMPOUND).copy());
                    var show=new CompoundTag();show.put("PhonePlayback",clip);show.putUUID("Reader",owner.getUUID());
                    io.github.knaitoe.theoldesthouse.network.HousePackets.send(owner,new io.github.knaitoe.theoldesthouse.network.LiteraryViewPayload(-1,0,0,show));
                    return InteractionResultHolder.sidedSuccess(stack,level.isClientSide);
                }
                if(custom!=null&&custom.copyTag().getBoolean("Dropped")){
                    CompoundTag recording=custom.copyTag();List<String> pages=new ArrayList<>();
                    pages.add("RECOVERED RECORDING\n\n"+recording.getString("Name")+"\nIndian Lake, at night.\n\nThe screen is cracked. The clip's frame record is still readable.");
                    ListTag frames=recording.getList("Footage",Tag.TAG_COMPOUND);
                    for(int i=0;i<frames.size();i++){
                        CompoundTag frame=frames.getCompound(i);
                        pages.add(String.format(Locale.ROOT,"00:%02d\n\n%s\n\nCamera: %.0f / %.0f\nLake: %.1f, %.1f, %.1f",frame.getInt("Tick")/20,frame.getString("Image"),frame.getFloat("Yaw"),frame.getFloat("Pitch"),frame.getDouble("X"),frame.getDouble("Y"),frame.getDouble("Z")));
                    }
                    ItemStack book=HouseWriting.book("The phone's recording",recording.getString("Name"),pages.stream().map(p->HouseWriting.page(HouseWriting.WritingStyle.PLAIN,p)).toList());
                    stack.set(DataComponents.WRITTEN_BOOK_CONTENT,book.get(DataComponents.WRITTEN_BOOK_CONTENT));owner.inventoryMenu.broadcastChanges();
                    NativeItemReader.open(owner,stack,hand);return InteractionResultHolder.sidedSuccess(stack,level.isClientSide);
                }
            }
        }
        return InteractionResultHolder.sidedSuccess(stack,level.isClientSide);
    }
}

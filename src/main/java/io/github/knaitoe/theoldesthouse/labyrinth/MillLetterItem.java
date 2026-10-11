package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.*;
import net.minecraft.world.level.Level;

/** The compensation envelope opens a real private letter; its money is paid separately, once. */
public final class MillLetterItem extends WrittenBookItem {
    public MillLetterItem(Properties p){super(p.component(DataComponents.WRITTEN_BOOK_CONTENT,content()));}
    public static WrittenBookContent content(){return new WrittenBookContent(Filterable.passThrough("Fenwick's Mill"),"The company secretary",0,
        List.of(Filterable.passThrough(Component.literal("FENWICK'S MILL\n\nMr and Mrs Dunning,\n\nWe regret to confirm that your son William died following an accident in the machinery. His effects will be returned when they have been collected.")),
                Filterable.passThrough(Component.literal("The firm admits no liability for the accident. In consideration of William's services, the directors have authorised the enclosed payment.\n\nSeven emeralds. Three ingots of gold.\n\nPlease acknowledge receipt.\n\nThe company secretary"))),true);}
    public static ItemStack issue(ServerPlayer p){var s=new ItemStack(LiteraryRegistry.MILL_LETTER.get());s.set(DataComponents.ITEM_NAME,Component.literal("Envelope from the mill"));
        var tag=new CompoundTag();tag.putUUID(ThreeFingers.PAW_OWNER,p.getUUID());s.set(DataComponents.CUSTOM_DATA,CustomData.of(tag));return s;}
    @Override public InteractionResultHolder<ItemStack> use(Level l,Player p,InteractionHand hand){var s=p.getItemInHand(hand);if(p instanceof ServerPlayer reader)NativeItemReader.open(reader,s,hand);return InteractionResultHolder.sidedSuccess(s,l.isClientSide());}
    /** Upgrade the old paid envelope in place, without issuing another payment. */
    public static void upgrade(ServerPlayer p){var inv=p.getInventory();for(int i=0;i<inv.getContainerSize();i++){var old=inv.getItem(i);
        if(old.is(Items.PAPER)&&old.has(DataComponents.ITEM_NAME)&&old.get(DataComponents.ITEM_NAME).getString().equals("Envelope from the mill")
                &&old.has(DataComponents.LORE)&&old.get(DataComponents.LORE).lines().stream().anyMatch(c->c.getString().equals("In consideration of services rendered."))){
            var letter=new ItemStack(LiteraryRegistry.MILL_LETTER.get(),old.getCount());letter.applyComponents(old.getComponentsPatch());
            letter.set(DataComponents.WRITTEN_BOOK_CONTENT,content());inv.setItem(i,letter);}
    }}
}

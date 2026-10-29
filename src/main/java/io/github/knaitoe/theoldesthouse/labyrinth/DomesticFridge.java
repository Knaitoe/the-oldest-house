package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemLore;

/**
 * 0.4.170, the owner: "Maybe add food to the fridges." What each household kept cold, shelf by shelf from the top, written
 * in its own hand on cling film, tape and stickers. The food is real and there is only what is there: each shelf's is taken
 * once, by whoever opens the door first, and nothing is put back. The drinker's middle shelf is his bottles, left where they are.
 */
public final class DomesticFridge {
    private DomesticFridge(){}
    public record Food(Item item,int count,String name,String label){
        public ItemStack stack(){
            var out=new ItemStack(item,count);out.set(DataComponents.CUSTOM_NAME,Component.literal(name).withStyle(style->style.withItalic(false)));
            out.set(DataComponents.LORE,new ItemLore(List.of(Component.literal(label).withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC))));return out;
        }
    }
    /** The shelf a fridge's stage bit stands for: 1 top, 2 middle, 3 bottom. */
    public static final int TOP=1,MIDDLE=2,BOTTOM=3;
    public static boolean fridge(DomesticFittingBlock.Kind k){return k==DomesticFittingBlock.Kind.FRIDGE_FAMILY||k==DomesticFittingBlock.Kind.FRIDGE_STUDENTS||k==DomesticFittingBlock.Kind.FRIDGE_DRINKER||k==DomesticFittingBlock.Kind.FRIDGE_GRANDMOTHER;}
    public static @Nullable Food food(DomesticFittingBlock.Kind k,int shelf){
        return switch(k){
            case FRIDGE_FAMILY->switch(shelf){
                case TOP->new Food(Items.BREAD,2,"Sandwiches","In cling film. A strip of tape: LEAH - NO EGG.");
                case MIDDLE->new Food(Items.HONEY_BOTTLE,2,"Apple juice","Two little cartons. BEN on one, in marker.");
                default->new Food(Items.COOKED_CHICKEN,1,"Sunday's chicken","Under foil. For whoever's home first.");};
            case FRIDGE_STUDENTS->switch(shelf){
                case TOP->new Food(Items.MELON_SLICE,3,"Melon","ELI on the cling film. Then MINE, in someone else's hand.");
                case MIDDLE->new Food(Items.BAKED_POTATO,2,"Chips from Friday","Still in the paper. Nobody has claimed them.");
                default->new Food(Items.COOKIE,4,"Biscuits","Half a packet, folded over and clipped.");};
            case FRIDGE_DRINKER->switch(shelf){
                case TOP->new Food(Items.PUMPKIN_PIE,1,"Pudding pot","A sticker on the lid: KIT. SATURDAY.");
                case MIDDLE->null;
                default->new Food(Items.BREAD,1,"The end of a loaf","Gone hard at the corners.");};
            case FRIDGE_GRANDMOTHER->switch(shelf){
                case TOP->new Food(Items.PUMPKIN_PIE,1,"Apple cake","Half of it, under an upturned plate. Wrapped for a train.");
                case MIDDLE->new Food(Items.MUSHROOM_STEW,1,"Soup","A saucer on top. Enough for two days, or two people.");
                default->new Food(Items.APPLE,2,"Apples","Small ones, for the next cake.");};
            default->null;
        };
    }
    /** The shelf under a point on the open fridge, in its own model's height. */
    public static int shelf(double y){return y<5.5/16?BOTTOM:y<11/16.?MIDDLE:TOP;}
}

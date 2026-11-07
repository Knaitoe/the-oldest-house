package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.ItemLore;

/**
 * 0.4.342, the piece of the Dream Stela the Sphinx leaves on the sill (docs/SPHINX_DREAM_0_4_342.md; the owner's draft, "A Stone From
 * Between the Paws"). The stone carries what the dream fixed when it began: how far it reaches through loose fill (three, five or eight
 * blocks, from the reader's accuracy), how it opens (patient, dismissive or hostile, from their conduct), and one small carving from the
 * riddle they most recently solved themselves, or a face worn smooth where a carving would be. A hostile dream leaves a corner freshly
 * chipped. Its use against buried passages comes with 0.4.343.
 */
public final class DreamStela {
    private DreamStela(){}
    public static final String TAG="DreamStela";
    /** The carvings, in the order of the item's model variants. */
    public static final List<String> CARVINGS=List.of("early","stair","shelf","field","smooth");
    public static final int SHORT=3,MEDIUM=5,DEEP=8;

    /** "No scored answers, or under 50% correct: short. At least 50% with fewer than three attempts, or 50% to under 75% across three or more: medium. At least 75% across three or more: deep." */
    public static int reach(double accuracy,int answered){
        if(accuracy<.5)return SHORT;
        return accuracy>=.75&&answered>=3?DEEP:MEDIUM;
    }
    public static String carving(@Nullable String latestSolve){return latestSolve!=null&&CARVINGS.contains(latestSolve)?latestSolve:"smooth";}
    public static String opening(SphinxCore.Conduct c){return c.name().toLowerCase(java.util.Locale.ROOT);}

    public static ItemStack make(UUID owner,int reach,String opening,String carving,boolean chipped){
        var s=new ItemStack(PuzzleRegistry.DREAM_STELA.get());
        var tag=new CompoundTag();tag.putBoolean(TAG,true);tag.putUUID("Owner",owner);tag.putInt("Reach",reach);tag.putString("Opening",opening);tag.putString("Carving",carving);tag.putBoolean("Chipped",chipped);
        s.set(DataComponents.CUSTOM_DATA,CustomData.of(tag));
        s.set(DataComponents.CUSTOM_MODEL_DATA,new CustomModelData(CARVINGS.indexOf(carving)*2+(chipped?1:0)+1));
        var lines=new ArrayList<Component>();
        lines.add(Component.literal("A palm-sized piece of granite. Sand is still in its cuts.").withStyle(ChatFormatting.GRAY));
        lines.add(Component.literal(switch(carving){
            case "early"->"Cut into it: a stand resting on its square of tile.";
            case "stair"->"Cut into it: a chip of stone above its stair.";
            case "shelf"->"Cut into it: an iron pin meeting a level line.";
            case "field"->"Cut into it: a road going under the stalks.";
            default->"It is worn smooth where something should be cut.";
        }).withStyle(ChatFormatting.GRAY));
        if(chipped)lines.add(Component.literal("One corner is freshly broken.").withStyle(ChatFormatting.DARK_GRAY,ChatFormatting.ITALIC));
        s.set(DataComponents.LORE,new ItemLore(lines));
        return s;
    }
    public static @Nullable CompoundTag data(ItemStack s){var d=s.get(DataComponents.CUSTOM_DATA);if(!s.is(PuzzleRegistry.DREAM_STELA.get())||d==null)return null;var t=d.copyTag();return t.getBoolean(TAG)?t:null;}
    public static boolean is(ItemStack s){return data(s)!=null;}
    public static int reachOf(ItemStack s){var t=data(s);return t==null?0:t.getInt("Reach");}
    public static String openingOf(ItemStack s){var t=data(s);return t==null?"":t.getString("Opening");}
    public static String carvingOf(ItemStack s){var t=data(s);return t==null?"":t.getString("Carving");}
    public static boolean chipped(ItemStack s){var t=data(s);return t!=null&&t.getBoolean("Chipped");}
    public static @Nullable UUID ownerOf(ItemStack s){var t=data(s);return t==null||!t.hasUUID("Owner")?null:t.getUUID("Owner");}
}

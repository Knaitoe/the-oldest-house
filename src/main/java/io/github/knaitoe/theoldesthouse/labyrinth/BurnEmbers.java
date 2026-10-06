package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.network.BurnEmbersPayload;
import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

/** Presentation reads the saved leaf before it burns, never recomposes a player's history. */
public final class BurnEmbers {
    private BurnEmbers() {}
    public static BurnEmbersPayload excerpt(CompoundTag own,int leaf,BlockPos fire) {
        String text=StaircaseStory.leaf(own,leaf).replaceAll("\\s+"," ").strip();
        int end=text.indexOf(". ");
        if(end>=24&&end<96)text=text.substring(0,end+1);
        if(text.codePointCount(0,text.length())>96) {
            int cut=text.offsetByCodePoints(0,93),space=text.lastIndexOf(' ',cut);
            text=text.substring(0,space>48?space:cut)+"…";
        }
        var hand=StaircaseStory.leafPage(own,leaf).getStyle().getFont();
        String lower=text.toLowerCase(Locale.ROOT);
        int memory=lower.contains("metres")?1:!own.getString("Built").isEmpty()&&lower.contains(own.getString("Built").toLowerCase(Locale.ROOT))?2:0;
        return new BurnEmbersPayload(fire,text,hand,memory);
    }
}

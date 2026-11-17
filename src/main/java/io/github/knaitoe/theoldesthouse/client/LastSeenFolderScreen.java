package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import io.github.knaitoe.theoldesthouse.labyrinth.LastSeen;
import io.github.knaitoe.theoldesthouse.labyrinth.LastSeenTexts;
import io.github.knaitoe.theoldesthouse.labyrinth.LiteraryActor;
import io.github.knaitoe.theoldesthouse.network.LastSeenFolderPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * 0.4.668, Last Seen (docs/LAST_SEEN_0_4_668.md): a child's folder held open. The manila card (drawn by tools/draw_last_seen_art.py),
 * the child's photograph clipped to it, which is their own face taken from their skin (the 32-pixel square of the head's front on the
 * cast's 512 by 256 grid), the typed form, and Nora's note in pencil at the foot.
 */
public final class LastSeenFolderScreen extends Screen {
    private static final ResourceLocation CARD=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/gui/last_seen_folder.png");
    /** The card is 256 by 176 at the top of its 256-square picture; the paper clip is drawn below it, 14 by 24. */
    static final int W=256,H=176,CLIP_U=0,CLIP_V=184,CLIP_W=14,CLIP_H=24,PHOTO=64;
    private static final int TYPE=0xFF2C2A27,FIELD=0xFF7A6E58,PENCIL=0xFF5F5C57;
    private final LastSeenFolderPayload folder;
    LastSeenFolderScreen(LastSeenFolderPayload folder){super(Component.literal(folder.name()));this.folder=folder;}
    public static void accept(LastSeenFolderPayload p){Minecraft.getInstance().setScreen(new LastSeenFolderScreen(p));}
    @Override public void render(GuiGraphics g,int mx,int my,float partial){
        super.render(g,mx,my,partial);
        int x=(width-W)/2,y=(height-H)/2;
        g.blit(CARD,x,y,0,0,W,H,256,256);
        // The photograph: a print with a white border and a deeper foot, the face filling it, and the clip over its top edge.
        int px=x+22,py=y+40;
        g.fill(px-4,py-4,px+PHOTO+4,py+PHOTO+12,0xFFEDE8DC);g.fill(px-4,py+PHOTO+12,px+PHOTO+4,py+PHOTO+13,0x40000000);
        if(folder.child()>=0&&folder.child()<LastSeen.CHILDREN){
            var skin=LiteraryRenderers.skin(LiteraryActor.STRANGER,LastSeen.CHILDREN_FIRST+folder.child());
            g.blit(skin,px,py,PHOTO,PHOTO,32F,32F,32,32,512,256);
            // and the hat layer over it, where a cut of hair is painted on the skin's second layer
            com.mojang.blaze3d.systems.RenderSystem.enableBlend();
            g.blit(skin,px,py,PHOTO,PHOTO,160F,32F,32,32,512,256);
            com.mojang.blaze3d.systems.RenderSystem.disableBlend();
        }else g.fill(px,py,px+PHOTO,py+PHOTO,0xFF8C8478);
        g.blit(CARD,px+PHOTO-20,py-14,CLIP_U,CLIP_V,CLIP_W,CLIP_H,256,256);
        // The typed form.
        g.drawString(font,LastSeenTexts.FORM,x+22,y+16,TYPE,false);
        g.drawString(font,folder.file(),x+W-22-font.width(folder.file()),y+16,TYPE,false);
        int fx=x+104,fw=W-104-18;
        g.drawString(font,LastSeenTexts.NAME_FIELD,fx,y+42,FIELD,false);
        g.drawString(font,folder.name(),fx,y+53,TYPE,false);
        g.drawString(font,LastSeenTexts.WHERE_FIELD,fx,y+72,FIELD,false);
        int line=y+83;
        for(var part:font.split(Component.literal(folder.where()),fw)){g.drawString(font,part,fx,line,TYPE,false);line+=10;}
        g.drawString(font,LastSeenTexts.DAY_FIELD,fx,Math.max(line+5,y+108),FIELD,false);
        g.drawString(font,folder.day(),fx,Math.max(line+16,y+119),TYPE,false);
        // Nora's note, in pencil, across the foot of the card.
        var note=Component.literal(LastSeenTexts.NOTE).withStyle(s->s.withFont(HouseWriting.MARGIN_FONT));
        g.drawString(font,note,x+W-28-font.width(note),y+H-26,PENCIL,false);
    }
    @Override public boolean isPauseScreen(){return false;}
}

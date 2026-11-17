package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import io.github.knaitoe.theoldesthouse.labyrinth.LastSeen;
import io.github.knaitoe.theoldesthouse.labyrinth.LastSeenTexts;
import io.github.knaitoe.theoldesthouse.labyrinth.LiteraryActor;
import io.github.knaitoe.theoldesthouse.network.LastSeenDrawerPayload;
import io.github.knaitoe.theoldesthouse.network.LastSeenFolderPayload;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.PageButton;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * 0.4.668, Last Seen (docs/LAST_SEEN_0_4_668.md): a child's folder held open. The manila card (drawn by tools/draw_last_seen_art.py),
 * the child's photograph clipped to it, which is their own face taken from their skin (the 32-pixel square of the head's front on the
 * cast's 512 by 256 grid), the typed form, and Nora's note in pencil at the foot.
 * <p>0.4.671 (docs/LAST_SEEN_0_4_671.md): Nora's drawer on the day after, a folder for each reader who has had the night, paged with the
 * book's own page buttons, the opening reader's own first. A reader's photograph is their own face from their own skin, the House body
 * where they wear one, face and hat layer, as the game draws a face in the player list.
 */
public final class LastSeenFolderScreen extends Screen {
    private static final ResourceLocation CARD=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/gui/last_seen_folder.png");
    /** The card is 256 by 176 at the top of its 256-square picture; the paper clip is drawn below it, 14 by 24. */
    static final int W=256,H=176,CLIP_U=0,CLIP_V=184,CLIP_W=14,CLIP_H=24,PHOTO=64;
    private static final int TYPE=0xFF2C2A27,FIELD=0xFF7A6E58,PENCIL=0xFF5F5C57;
    /** One folder: a child's (by their index, reader null) or a reader's (by who they are, child -1). */
    record Page(int child,@Nullable UUID reader,String heading,String file,String name,String where,String day,String note){}
    private final List<Page> pages;
    private int page;
    LastSeenFolderScreen(List<Page> pages){super(Component.literal(pages.get(0).name()));this.pages=pages;}
    public static void accept(LastSeenFolderPayload p){
        Minecraft.getInstance().setScreen(new LastSeenFolderScreen(List.of(new Page(p.child(),null,LastSeenTexts.FORM,p.file(),p.name(),p.where(),p.day(),LastSeenTexts.NOTE))));
    }
    /** 0.4.671: the drawer's folders. */
    public static void drawer(LastSeenDrawerPayload p){
        if(p.folders().isEmpty())return;
        Minecraft.getInstance().setScreen(new LastSeenFolderScreen(p.folders().stream()
            .map(f->new Page(-1,f.reader(),f.heading(),f.file(),f.name(),LastSeenTexts.AT_DOOR,f.on(),f.note())).toList()));
    }
    int page(){return page;}
    int pages(){return pages.size();}
    void turn(int to){page=Math.max(0,Math.min(pages.size()-1,to));rebuildWidgets();}
    @Override protected void init(){
        if(pages.size()<2)return;
        int x=(width-W)/2,y=(height-H)/2;
        if(page>0)addRenderableWidget(new PageButton(x+12,y+H+4,false,b->turn(page-1),true));
        if(page<pages.size()-1)addRenderableWidget(new PageButton(x+W-35,y+H+4,true,b->turn(page+1),true));
    }
    /** A reader's skin: the House body they wear, else their own as the player list knows it, else the account's default. */
    static PlayerSkin skin(UUID reader){
        var body=HouseBodyClient.skin(reader);if(body!=null)return body;
        var mc=Minecraft.getInstance();var info=mc.getConnection()==null?null:mc.getConnection().getPlayerInfo(reader);
        return info!=null?info.getSkin():DefaultPlayerSkin.get(reader);
    }
    @Override public void render(GuiGraphics g,int mx,int my,float partial){
        super.render(g,mx,my,partial);
        var folder=pages.get(page);
        int x=(width-W)/2,y=(height-H)/2;
        g.blit(CARD,x,y,0,0,W,H,256,256);
        // The photograph: a print with a white border and a deeper foot, the face filling it, and the clip over its top edge.
        int px=x+22,py=y+40;
        g.fill(px-4,py-4,px+PHOTO+4,py+PHOTO+12,0xFFEDE8DC);g.fill(px-4,py+PHOTO+12,px+PHOTO+4,py+PHOTO+13,0x40000000);
        if(folder.reader()!=null){
            com.mojang.blaze3d.systems.RenderSystem.enableBlend();
            PlayerFaceRenderer.draw(g,skin(folder.reader()),px,py,PHOTO);
            com.mojang.blaze3d.systems.RenderSystem.disableBlend();
        }else if(folder.child()>=0&&folder.child()<LastSeen.CHILDREN){
            var skin=LiteraryRenderers.skin(LiteraryActor.STRANGER,LastSeen.CHILDREN_FIRST+folder.child());
            g.blit(skin,px,py,PHOTO,PHOTO,32F,32F,32,32,512,256);
            // and the hat layer over it, where a cut of hair is painted on the skin's second layer
            com.mojang.blaze3d.systems.RenderSystem.enableBlend();
            g.blit(skin,px,py,PHOTO,PHOTO,160F,32F,32,32,512,256);
            com.mojang.blaze3d.systems.RenderSystem.disableBlend();
        }else g.fill(px,py,px+PHOTO,py+PHOTO,0xFF8C8478);
        g.blit(CARD,px+PHOTO-20,py-14,CLIP_U,CLIP_V,CLIP_W,CLIP_H,256,256);
        // The typed form.
        g.drawString(font,folder.heading(),x+22,y+16,TYPE,false);
        g.drawString(font,folder.file(),x+W-22-font.width(folder.file()),y+16,TYPE,false);
        int fx=x+104,fw=W-104-18;
        g.drawString(font,LastSeenTexts.NAME_FIELD,fx,y+42,FIELD,false);
        g.drawString(font,folder.name(),fx,y+53,TYPE,false);
        g.drawString(font,LastSeenTexts.WHERE_FIELD,fx,y+72,FIELD,false);
        int line=y+83;
        for(var part:font.split(Component.literal(folder.where()),fw)){g.drawString(font,part,fx,line,TYPE,false);line+=10;}
        g.drawString(font,LastSeenTexts.DAY_FIELD,fx,Math.max(line+5,y+108),FIELD,false);
        int day=Math.max(line+16,y+119);
        for(var part:font.split(Component.literal(folder.day()),fw)){g.drawString(font,part,fx,day,TYPE,false);day+=10;}
        // Nora's note, in pencil, across the foot of the card.
        var note=Component.literal(folder.note()).withStyle(s->s.withFont(HouseWriting.MARGIN_FONT));
        g.drawString(font,note,x+W-28-font.width(note),y+H-26,PENCIL,false);
    }
    @Override public boolean isPauseScreen(){return false;}
}

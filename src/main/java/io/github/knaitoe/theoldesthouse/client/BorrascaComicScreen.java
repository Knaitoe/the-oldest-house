package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.BorrascaComic;
import io.github.knaitoe.theoldesthouse.network.BorrascaComicPagePayload;
import io.github.knaitoe.theoldesthouse.network.BorrascaComicPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * 0.4.445 (docs/BORRASCA_COMIC_0_4_445.md): Kit &amp; Caboose No. 31 open in the reader's hands, a page at a time, drawn from the
 * atlas tools/draw_borrasca_comic.py makes (four pages by two, each 128 by 192), at a whole number of screen pixels to the pixel
 * wherever the window allows. A click on the right of the page, the wheel, the arrow keys or the space bar turn it on; the left of
 * the page, or the other button, turns it back. A page that has been open for a moment is told to the server, which is how the
 * friend can be answered truthfully years later (labyrinth.BorrascaComic).
 */
public final class BorrascaComicScreen extends Screen {
    public static final ResourceLocation FRESH=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/gui/borrasca_comic.png"),
        AGED=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/gui/borrasca_comic_aged.png");
    public static final int W=128,H=192;
    /** How long a page is open before it counts as one the reader has had open. */
    static final int COUNTS=15;
    final boolean aged;int page,open,told;

    BorrascaComicScreen(boolean aged,int page){super(Component.translatable("item.the_oldest_house.borrasca_comic"));this.aged=aged;this.page=Mth.clamp(page,0,BorrascaComic.PAGES-1);}
    public static void accept(BorrascaComicPayload p){Minecraft.getInstance().setScreen(new BorrascaComicScreen(p.aged(),p.page()));}
    @Override public boolean isPauseScreen(){return false;}
    @Override public void tick(){
        if(++open==COUNTS&&(told&1<<page)==0){told|=1<<page;PacketDistributor.sendToServer(new BorrascaComicPagePayload(page));}
    }
    void turn(int by){
        int to=Mth.clamp(page+by,0,BorrascaComic.PAGES-1);if(to==page)return;
        page=to;open=0;Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN,1F));
    }
    /** Screen pixels to the page's pixel: the largest whole number that fits, or a fraction in a window too small for one. */
    static float scale(int width,int height){int k=Math.min((height-40)/H,(width-40)/W);return k>=1?k:Math.max(.25F,Math.min((height-24)/(float)H,(width-24)/(float)W));}
    /** One page of the comic, its top left corner at a place on the screen. */
    public static void draw(GuiGraphics g,boolean aged,int page,int left,int top,float scale){
        var pose=g.pose();pose.pushPose();pose.translate(left,top,0);pose.scale(scale,scale,1);
        g.blit(aged?AGED:FRESH,0,0,(page%4)*W,(page/4)*H,W,H,W*4,H*2);pose.popPose();
    }
    @Override public void renderBackground(GuiGraphics g,int mx,int my,float partial){}
    @Override public void render(GuiGraphics g,int mx,int my,float partial){
        float s=scale(width,height);int w=Math.round(W*s),h=Math.round(H*s),left=(width-w)/2,top=Math.max(4,(height-h)/2-5);
        g.fill(0,0,width,height,0xB4000000);
        // The pages under this one, a pixel of each, and the shadow the whole of it throws.
        g.fill(left+4,top+4,left+w+4,top+h+4,0x80000000);
        if(page<BorrascaComic.PAGES-1)g.fill(left+w,top+2,left+w+2,top+h,aged?0xFFB9BDC4:0xFFD8CEB4);
        draw(g,aged,page,left,top,s);
        boolean back=page>0,on=page<BorrascaComic.PAGES-1,right=mx>=width/2;
        if(back)g.drawString(font,"<",left-14,top+h/2-4,!right?0xFFE8DDC9:0xFF6E675C,false);
        if(on)g.drawString(font,">",left+w+9,top+h/2-4,right?0xFFE8DDC9:0xFF6E675C,false);
        g.drawCenteredString(font,(page+1)+" / "+BorrascaComic.PAGES,width/2,top+h+7,0xFF9A9284);
        super.render(g,mx,my,partial);
    }
    @Override public boolean keyPressed(int key,int scan,int mods){
        if(key==InputConstants.KEY_RIGHT||key==InputConstants.KEY_D||key==InputConstants.KEY_SPACE||key==InputConstants.KEY_PAGEDOWN){turn(1);return true;}
        if(key==InputConstants.KEY_LEFT||key==InputConstants.KEY_A||key==InputConstants.KEY_PAGEUP){turn(-1);return true;}
        if(minecraft!=null&&minecraft.options.keyInventory.matches(key,scan)){onClose();return true;}
        return super.keyPressed(key,scan,mods);
    }
    @Override public boolean mouseClicked(double mx,double my,int button){turn(button==1||mx<width/2.0?-1:1);return true;}
    @Override public boolean mouseScrolled(double mx,double my,double dx,double dy){if(dy!=0)turn(dy<0?1:-1);return true;}
}

package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.journal.JournalPage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

/**
 * A page torn from someone's journal, held up to read (journal/TornPages): one page on the same renderer as the journal, its torn
 * edge ragged, and above it whose journal it came from. It is only read; nothing on it can be changed.
 */
public final class TornPageScreen extends Screen {
    private final String writer;
    private final JournalPage page;
    TornPageScreen(String writer,JournalPage page){super(Component.translatable("item.the_oldest_house.torn_page.from",writer));this.writer=writer;this.page=page;}
    public static void open(String writer,CompoundTag page){Minecraft.getInstance().setScreen(new TornPageScreen(writer,JournalPage.load(page)));}
    static TornPageScreen proof(String writer,JournalPage page){return new TornPageScreen(writer,page);}
    int x(){return (width-JournalPageRenderer.W)/2;}
    int y(){return Math.max(18,(height-JournalPageRenderer.H)/2);}
    @Override public boolean isPauseScreen(){return false;}
    @Override public void renderBackground(GuiGraphics g,int mx,int my,float partial){g.fill(0,0,width,height,0xE01E1A16);}
    @Override public void render(GuiGraphics g,int mx,int my,float partial){
        super.render(g,mx,my,partial);
        int x=x(),y=y();
        g.drawCenteredString(font,getTitle(),width/2,y-12,JournalPageRenderer.FAINT);
        JournalPageRenderer.torn(g,font,x,y,page);
    }
}

package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.labyrinth.HouseReadStory;
import io.github.knaitoe.theoldesthouse.network.HouseReadCardActionPayload;
import io.github.knaitoe.theoldesthouse.network.HouseReadLinePayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * 0.4.166: the House's line between a quiet room and the rest of the House. The screen goes black at once (no fade: the room
 * is cut off), the room's own sound is heard in the dark, and each page of the line comes up and goes, in its own time.
 * <p>
 * 0.4.395 (owner: "remove the read from my pace from the personhood vignette intro/outro text please"): the card has no buttons.
 * The "Read at my pace" choice of 0.4.288, and the page turning it opened, are gone; every card plays through and closes itself.
 */
public final class HouseReadLineScreen extends Screen {
    private final String[] pages;
    private final int[] ends;
    private final boolean title;
    /**
     * 0.4.399, the owner: "can we make the 'This House Has Ghosts in It' text at the end of the personhood vignettes fade out,
     * rather than cut out?" After the title's own time the card does not close at once: the title fades over TITLE_FADE ticks while
     * the black lifts over CLEAR ticks, onto the House the reader has already been carried into.
     */
    static final int TITLE_FADE=30,CLEAR=50;
    private final int token;
    private int age;
    private boolean still;
    private HouseReadLineScreen(HouseReadLinePayload line){
        super(Component.empty());pages=line.text().split(HouseReadStory.PAGE,-1);ends=new int[pages.length];title=line.title();token=line.token();
        int total=0;for(int i=0;i<pages.length;i++){total+=HouseReadStory.page(pages[i]);ends[i]=total;}
    }
    public static void show(HouseReadLinePayload line){
        var mc=Minecraft.getInstance();
        if(line.manual()){
            // No card is ever held now; a held card from the server is let go at once.
            if(mc.getConnection()!=null)PacketDistributor.sendToServer(new HouseReadCardActionPayload(line.token(),true));
            return;
        }
        mc.setScreen(new HouseReadLineScreen(line));
        if(!line.sound().isEmpty())mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvent.createVariableRangeEvent(ResourceLocation.parse(line.sound())),.7F,.9F));
    }
    /** For still images: the card held in the middle of one page, at full strength. */
    static HouseReadLineScreen still(HouseReadLinePayload line,int page){var screen=new HouseReadLineScreen(line);int start=page==0?0:screen.ends[page-1];screen.age=(start+screen.ends[page])/2;screen.still=true;return screen;}

    int page(){
        int index=0;
        while(index<ends.length-1&&age>=ends[index])index++;
        return index;
    }
    /** How long the card shows, in ticks (the proof). */
    int length(){return ends[ends.length-1];}

    @Override public void tick(){if(!still&&++age>=ends[ends.length-1]+(title?CLEAR:0))onClose();}
    @Override public boolean shouldCloseOnEsc(){return false;}
    @Override public boolean isPauseScreen(){return false;}
    @Override public void renderBackground(GuiGraphics g,int mouseX,int mouseY,float partial){}
    @Override public void render(GuiGraphics g,int mouseX,int mouseY,float partial){
        float now=age+partial;int index=page();boolean last=index==pages.length-1;
        float start=index==0?0:ends[index-1],end=ends[index],local=now-start;
        // The black: whole, except at the end of a title card, where it lifts slowly after the title has had its time.
        float black=title&&last?Mth.clamp(1-(now-end)/CLEAR,0F,1F):1F;
        g.fill(0,0,width,height,((int)(black*255)<<24));
        // Each page rises out of the black and sinks back into it; the first beat of the card is black alone. The title sinks slowly.
        float out=title&&last?(end+TITLE_FADE-now)/TITLE_FADE:(end-now)/10F;
        float alpha=Mth.clamp(Math.min((local-8)/14F,out),0F,1F);if(alpha<=.02F){super.render(g,mouseX,mouseY,partial);return;}
        int color=((int)(alpha*255)<<24)|0xD9D4CC;boolean big=title&&index==pages.length-1;float scale=big?2F:1F;
        var lines=font.split(Component.literal(pages[index]),(int)(Math.min(300,width-40)/scale));int lead=font.lineHeight+3;
        g.pose().pushPose();g.pose().translate(width/2F,height/2F-lines.size()*lead*scale/2F,0);g.pose().scale(scale,scale,1);
        for(int i=0;i<lines.size();i++)g.drawString(font,lines.get(i),-font.width(lines.get(i))/2,i*lead,color,false);
        g.pose().popPose();
        super.render(g,mouseX,mouseY,partial);
    }
}

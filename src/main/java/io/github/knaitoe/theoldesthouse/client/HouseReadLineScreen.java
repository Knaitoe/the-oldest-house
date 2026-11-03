package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.labyrinth.HouseReadStory;
import io.github.knaitoe.theoldesthouse.network.HouseReadCardActionPayload;
import io.github.knaitoe.theoldesthouse.network.HouseReadLinePayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

/**
 * 0.4.166: the House's line between a quiet room and the rest of the House. The screen goes black at once (no fade: the room
 * is cut off), the room's own sound is heard in the dark, and each page of the line comes up and goes, in the reader's own
 * time. A reader may hold the card and turn its pages at their own pace.
 */
public final class HouseReadLineScreen extends Screen {
    private final String[] pages;
    private final int[] ends;
    private final boolean title;
    private final int token;
    private int age,manualPage;
    private boolean still,requested,manual;
    private Button back,advance;
    private HouseReadLineScreen(HouseReadLinePayload line){
        super(Component.empty());pages=line.text().split(HouseReadStory.PAGE,-1);ends=new int[pages.length];title=line.title();token=line.token();
        int total=0;for(int i=0;i<pages.length;i++){total+=HouseReadStory.page(pages[i]);ends[i]=total;}
    }
    public static void show(HouseReadLinePayload line){
        var mc=Minecraft.getInstance();
        if(line.manual()){
            if(mc.screen instanceof HouseReadLineScreen card&&card.token==line.token()){
                card.startManual();
            }else if(mc.getConnection()!=null){
                // The timed screen may have closed while its request was in flight.
                PacketDistributor.sendToServer(new HouseReadCardActionPayload(line.token(),true));
            }
            return;
        }
        mc.setScreen(new HouseReadLineScreen(line));
        if(!line.sound().isEmpty())mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvent.createVariableRangeEvent(ResourceLocation.parse(line.sound())),.7F,.9F));
    }
    /** For still images: the card held in the middle of one page, at full strength. */
    static HouseReadLineScreen still(HouseReadLinePayload line,int page){var screen=new HouseReadLineScreen(line);int start=page==0?0:screen.ends[page-1];screen.age=(start+screen.ends[page])/2;screen.still=true;return screen;}
    @Override protected void init(){
        if(still||token==0)return;
        int y=height-27;
        if(!manual){
            addRenderableWidget(Button.builder(Component.literal("Read at my pace"),button->{
                requested=true;
                button.active=false;
                PacketDistributor.sendToServer(new HouseReadCardActionPayload(token,false));
            }).bounds(width/2-59,y,118,20).build()).active=!requested;
            return;
        }
        back=addRenderableWidget(Button.builder(Component.literal("Back"),button->back()).bounds(width/2-83,y,76,20).build());
        advance=addRenderableWidget(Button.builder(Component.literal("Continue"),button->advance()).bounds(width/2+7,y,76,20).build());
        updateButtons();
    }

    private void startManual(){
        if(manual)return;
        manualPage=page();
        manual=true;
        rebuildWidgets();
    }

    private void updateButtons(){
        back.active=manualPage>0;
        advance.setMessage(Component.literal(manualPage==pages.length-1?"Done":"Continue"));
    }

    private void back(){
        if(manualPage>0)manualPage--;
        updateButtons();
    }

    private void advance(){
        if(manualPage==pages.length-1){onClose();return;}
        manualPage++;
        updateButtons();
    }

    int page(){
        if(manual)return manualPage;
        int index=0;
        while(index<ends.length-1&&age>=ends[index])index++;
        return index;
    }

    @Override public boolean keyPressed(int key,int scan,int modifiers){
        if(manual&&key==GLFW.GLFW_KEY_LEFT){back();return true;}
        if(manual&&key==GLFW.GLFW_KEY_RIGHT&&manualPage<pages.length-1){advance();return true;}
        return super.keyPressed(key,scan,modifiers);
    }

    @Override public void removed(){
        if((manual||requested)&&Minecraft.getInstance().getConnection()!=null)
            PacketDistributor.sendToServer(new HouseReadCardActionPayload(token,true));
        super.removed();
    }

    @Override public void tick(){if(!still&&!manual&&++age>=ends[ends.length-1])onClose();}
    @Override public boolean shouldCloseOnEsc(){return false;}
    @Override public boolean isPauseScreen(){return false;}
    @Override public void renderBackground(GuiGraphics g,int mouseX,int mouseY,float partial){}
    @Override public void render(GuiGraphics g,int mouseX,int mouseY,float partial){
        g.fill(0,0,width,height,0xFF000000);
        float now=age+partial;int index=page();
        float start=index==0?0:ends[index-1],end=ends[index],local=now-start;
        // Each page rises out of the black and sinks back into it; the first beat of the card is black alone.
        float alpha=manual?1F:Mth.clamp(Math.min((local-8)/14F,(end-now)/10F),0F,1F);if(alpha<=.02F){super.render(g,mouseX,mouseY,partial);return;}
        int color=((int)(alpha*255)<<24)|0xD9D4CC;boolean big=title&&index==pages.length-1;float scale=big?2F:1F;
        var lines=font.split(Component.literal(pages[index]),(int)(Math.min(300,width-40)/scale));int lead=font.lineHeight+3;
        g.pose().pushPose();g.pose().translate(width/2F,height/2F-lines.size()*lead*scale/2F,0);g.pose().scale(scale,scale,1);
        for(int i=0;i<lines.size();i++)g.drawString(font,lines.get(i),-font.width(lines.get(i))/2,i*lead,color,false);
        g.pose().popPose();
        super.render(g,mouseX,mouseY,partial);
    }
}

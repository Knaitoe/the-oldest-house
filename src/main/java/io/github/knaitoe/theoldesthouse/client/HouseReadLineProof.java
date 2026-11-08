package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.HouseReadSites;
import io.github.knaitoe.theoldesthouse.labyrinth.HouseReadStory;
import io.github.knaitoe.theoldesthouse.network.HouseReadLinePayload;
import java.nio.file.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

/**
 * 0.4.166: the House's lines as a reader sees them between the quiet rooms: the welcome before the first room, a room's
 * closing line, and the warning after the last, each held at full strength. Runs after the reading stands in the same client.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class HouseReadLineProof extends Screen {
    private static final String[] NAMES={"welcome","closing","warning"};
    private static final HouseReadLinePayload[] CARDS={
        new HouseReadLinePayload(HouseReadStory.WELCOME+HouseReadStory.PAGE+HouseReadStory.opening(HouseReadSites.Room.WAITING),"",false),
        new HouseReadLinePayload(HouseReadStory.closing(HouseReadSites.Room.BATHROOM),"",false),
        new HouseReadLinePayload(HouseReadStory.closing(HouseReadSites.Room.PORCH)+HouseReadStory.PAGE+HouseReadStory.HAUNTED+HouseReadStory.PAGE+HouseReadStory.WARNING_LEAD+HouseReadStory.PAGE+HouseReadStory.WARNING,"",true)};
    /** Which page of each card is shown: the welcome, the closing line, the warning itself. */
    private static final int[] PAGES={0,0,2};
    private int page,frames;private HouseReadLineScreen card;
    public HouseReadLineProof(){super(Component.literal("The House's lines"));}
    @Override public void renderBackground(GuiGraphics g,int x,int y,float partial){}
    @Override public void render(GuiGraphics g,int x,int y,float partial){
        if(card==null){card=HouseReadLineScreen.still(CARDS[page],PAGES[page]);card.init(Minecraft.getInstance(),width,height);}
        card.render(g,x,y,0);
    }
    /** 0.4.395: the card has no buttons, holds no reader, and closes itself once its pages have played. */
    private static void checkPlaysThrough(){
        var mc=Minecraft.getInstance();
        String text=CARDS[0].text();
        HouseReadLineScreen.show(new HouseReadLinePayload(text,"",false,1,false));
        if(!(mc.screen instanceof HouseReadLineScreen card))throw new IllegalStateException("No story card opened");
        for(var child:card.children())if(child instanceof Button)throw new IllegalStateException("The story card must have no buttons: "+((Button)child).getMessage().getString());
        int pages=text.split(HouseReadStory.PAGE,-1).length;
        for(int i=0;i<card.length()-1;i++){card.tick();if(mc.screen!=card)throw new IllegalStateException("The story card closed early, at tick "+i);}
        if(card.page()!=pages-1)throw new IllegalStateException("The story card must reach its last page on its own");
        card.tick();
        if(mc.screen==card)throw new IllegalStateException("The story card must close itself once its pages have played");
    }

    /** 0.4.399: the last card, with the title, does not cut: it stays while the title fades and the black lifts, then closes. */
    private static void checkTitleFades(){
        var mc=Minecraft.getInstance();
        HouseReadLineScreen.show(new HouseReadLinePayload(CARDS[CARDS.length-1].text(),"",true,2,false));
        if(!(mc.screen instanceof HouseReadLineScreen card))throw new IllegalStateException("No title card opened");
        int whole=card.length()+HouseReadLineScreen.CLEAR;
        for(int i=0;i<whole-1;i++){card.tick();if(mc.screen!=card)throw new IllegalStateException("The title card must fade out, not cut: it closed at tick "+i+" of "+whole);}
        card.tick();
        if(mc.screen==card)throw new IllegalStateException("The title card must close once the black has lifted");
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post e)throws Exception{
        var mc=Minecraft.getInstance();if(!(mc.screen instanceof HouseReadLineProof s)||++s.frames<12)return;
        var folder=Path.of("../build/house-read-lines");Files.createDirectories(folder);
        try(var image=net.minecraft.client.Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(folder.resolve(NAMES[s.page]+".png"));}
        if(++s.page<NAMES.length){s.frames=0;s.card=null;return;}
        checkPlaysThrough();
        checkTitleFades();
        Files.writeString(folder.resolve("passed.txt"),"The House's lines between the quiet rooms on black: the welcome before the first room, the bathroom's closing line, and the warning after the last room. The card has no buttons, plays through and closes itself.\n");
        mc.setScreen(new HouseReadHairProof());
    }
}

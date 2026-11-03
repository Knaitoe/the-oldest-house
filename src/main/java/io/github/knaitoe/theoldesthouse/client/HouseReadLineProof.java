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
    private static Button button(HouseReadLineScreen card,String label){
        for(var child:card.children())
            if(child instanceof Button button&&button.getMessage().getString().equals(label))return button;
        throw new IllegalStateException("Missing story card button: "+label);
    }

    private static void checkManualControls(){
        var mc=Minecraft.getInstance();
        String text=CARDS[0].text();
        HouseReadLineScreen.show(new HouseReadLinePayload(text,"",false,1,false));
        if(!(mc.screen instanceof HouseReadLineScreen card))throw new IllegalStateException("No story card opened");
        button(card,"Read at my pace");
        HouseReadLineScreen.show(new HouseReadLinePayload(text,"",false,1,true));
        for(int i=0;i<HouseReadStory.ticks(text)+20;i++)card.tick();
        if(mc.screen!=card||card.page()!=0||button(card,"Back").active)
            throw new IllegalStateException("The manual card must stay on its first page");
        button(card,"Continue").onPress();
        if(card.page()!=1)throw new IllegalStateException("Continue must turn one page");
        button(card,"Back").onPress();
        if(card.page()!=0)throw new IllegalStateException("Back must restore the previous page");
        button(card,"Continue").onPress();
        button(card,"Done").onPress();
        if(mc.screen==card)throw new IllegalStateException("Done must close the manual card");
    }

    @SubscribeEvent public static void frame(RenderFrameEvent.Post e)throws Exception{
        var mc=Minecraft.getInstance();if(!(mc.screen instanceof HouseReadLineProof s)||++s.frames<12)return;
        var folder=Path.of("../build/house-read-lines");Files.createDirectories(folder);
        try(var image=net.minecraft.client.Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(folder.resolve(NAMES[s.page]+".png"));}
        if(++s.page<NAMES.length){s.frames=0;s.card=null;return;}
        checkManualControls();
        Files.writeString(folder.resolve("passed.txt"),"The House's lines between the quiet rooms on black: the welcome before the first room, the bathroom's closing line, and the warning after the last room. Manual controls hold, turn both ways and close.\n");
        mc.setScreen(new HouseReadHairProof());
    }
}

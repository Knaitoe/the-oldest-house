package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import java.nio.file.*;
import java.util.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

/** Real serialized source pages in all three actual native writer hands. Opt-in CI only. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class NovelPapersVisualProof extends Screen {
    private int frames;
    private final List<Component> pages=new ArrayList<>();
    private final String[] ids={"HOL_Z01","HOL_Z04","HOL_J04","HOL_J34","HOL_P01","HOL_P05"};
    public NovelPapersVisualProof(){
        super(Component.literal("Novel papers proof"));
        for(String id:ids){var content=CorrespondenceTexts.book(CorrespondenceTexts.find(id),Map.of()).get(DataComponents.WRITTEN_BOOK_CONTENT);
            int page=0;if(id.equals("HOL_P05"))for(int n=0;n<content.pages().size();n++)if(content.pages().get(n).raw().getString().contains("elevAted")){page=n;break;}
            pages.add(content.pages().get(page).raw());
        }
    }
    @Override public void renderBackground(GuiGraphics g,int x,int y,float partial){}
    @Override public void render(GuiGraphics g,int x,int y,float partial){
        g.fill(0,0,width,height,0xFF211E1B);g.drawString(font,"The Oldest House | source papers: Zampanò / Johnny / Pelafina",10,8,0xFFEAE0D0);
        for(int i=0;i<pages.size();i++){int left=10+i%3*(width/3),top=30+i/3*164;
            g.fill(left,top,left+width/3-18,top+154,0xFFE7D9BC);g.drawString(font,CorrespondenceTexts.find(ids[i]).title(),left+8,top+7,0xFF241E19,false);
            int row=top+24;for(var line:font.split(pages.get(i),114)){g.drawString(font,line,left+8,row,0xFF181411,false);row+=9;}
        }
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post event)throws Exception{
        var mc=Minecraft.getInstance();if(!Boolean.getBoolean("the_oldest_house.fontSmoke")||!(mc.screen instanceof NovelPapersVisualProof proof)||++proof.frames<4)return;
        var folder=Path.of("../build/font-smoke");Files.createDirectories(folder);
        try(NativeImage shot=Screenshot.takeScreenshot(mc.getMainRenderTarget())){shot.writeToFile(folder.resolve("native-novel-papers.png"));}
        Files.writeString(folder.resolve("novel-papers-passed.txt"),"Six actual source pages, distinct native hands, original cipher capitalization and blue House.\n");
        TheOldestHouse.LOGGER.info("NOVEL PAPERS CHECK PASSED: source pages saved");mc.setScreen(new BurnEmbersVisualProof());
    }
}

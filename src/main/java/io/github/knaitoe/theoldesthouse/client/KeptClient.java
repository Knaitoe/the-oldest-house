package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseKeeping;
import io.github.knaitoe.theoldesthouse.network.KeptStatePayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

/**
 * 0.4.267, the hall drawer's buttons (house/HouseKeeping). Beside the drawer's six rows: "Retrieve belongings", the page arrows when
 * the drawer runs to more than one page, and how much is kept. The buttons are vanilla menu-button clicks; the server decides.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class KeptClient {
    private KeptClient(){}
    private static int container=-1,page,pages=1,stacks;
    static final int IMAGE_WIDTH=176;

    public static void accept(KeptStatePayload p){
        container=p.container();page=p.page();pages=Math.max(1,p.pages());stacks=p.stacks();
        var mc=Minecraft.getInstance();if(mc.screen instanceof AbstractContainerScreen<?> s&&ours(s))s.init(mc,s.width,s.height);
    }
    static boolean ours(AbstractContainerScreen<?> s){return container>=0&&s.getMenu().containerId==container;}
    /** The column beside the drawer: its left edge and width, wherever there is room for it. */
    static int columnX(AbstractContainerScreen<?> s){return s.getGuiLeft()+IMAGE_WIDTH+4;}
    static int columnW(AbstractContainerScreen<?> s){return Math.max(40,Math.min(110,s.width-columnX(s)-4));}
    static void press(int button){var mc=Minecraft.getInstance();if(mc.gameMode!=null&&mc.player!=null)mc.gameMode.handleInventoryButtonClick(container,button);}

    @SubscribeEvent public static void init(ScreenEvent.Init.Post e){
        if(!(e.getScreen() instanceof AbstractContainerScreen<?> s)||!ours(s))return;
        int x=columnX(s),w=columnW(s),y=s.getGuiTop()+4;
        var all=Button.builder(Component.literal("Retrieve belongings"),b->press(HouseKeeping.RETRIEVE)).bounds(x,y,w,20).build();all.active=stacks>0;e.addListener(all);
        if(pages>1){
            var back=Button.builder(Component.literal("<"),b->press(HouseKeeping.PREVIOUS)).bounds(x,y+24,20,20).build();back.active=page>0;e.addListener(back);
            var on=Button.builder(Component.literal(">"),b->press(HouseKeeping.NEXT)).bounds(x+w-20,y+24,20,20).build();on.active=page<pages-1;e.addListener(on);
        }
    }
    @SubscribeEvent public static void render(ScreenEvent.Render.Post e){
        if(!(e.getScreen() instanceof AbstractContainerScreen<?> s)||!ours(s))return;
        var font=Minecraft.getInstance().font;int x=columnX(s),w=columnW(s),y=s.getGuiTop()+4;var g=e.getGuiGraphics();
        if(pages>1){var label=(page+1)+" of "+pages;g.drawString(font,label,x+w/2-font.width(label)/2,y+30,0xFFE0E0E0,true);}
        g.drawString(font,label(),x,y+(pages>1?50:26),0xFFC8C8C8,true);
    }
    /** How much the drawer holds, in a few words. */
    static String label(){return stacks==0?"Nothing kept":stacks==1?"1 stack kept":stacks+" stacks kept";}
    @SubscribeEvent public static void closing(ScreenEvent.Closing e){if(e.getScreen() instanceof AbstractContainerScreen<?> s&&ours(s))container=-1;}
}

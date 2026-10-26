package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.UncannyReading;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.LecternScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;

/** Uses the existing lectern and inventory-button packet; the server owns passage availability. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class UncannyReadingClient {
    private static LecternScreen initialized;
    private UncannyReadingClient(){}
    private static CompoundTag view(){
        var mc=Minecraft.getInstance();if(mc.player==null)return new CompoundTag();var menu=mc.player.containerMenu;
        if(menu.slots.isEmpty())return new CompoundTag();var c=menu.getSlot(0).getItem().get(DataComponents.CUSTOM_DATA);return c==null?new CompoundTag():c.copyTag();
    }
    @SubscribeEvent public static void tick(ClientTickEvent.Post e){
        var mc=Minecraft.getInstance();if(!(mc.screen instanceof LecternScreen screen)){initialized=null;return;}
        // The opening packet arrives before the book's native slot synchronization.
        if(initialized!=screen&&view().getBoolean(UncannyReading.VIEW)){initialized=screen;screen.init(mc,screen.width,screen.height);}
    }
    @SubscribeEvent public static void init(ScreenEvent.Init.Post e){
        if(!(e.getScreen() instanceof LecternScreen screen))return;var data=view();if(!data.getBoolean(UncannyReading.VIEW))return;
        int mid=screen.width/2,index=data.getInt("PassageIndex"),mask=data.getInt("PassageMask");
        for(var child:screen.children())if(child instanceof AbstractWidget widget){
            if(widget.getMessage().getString().equals(Component.translatable("gui.done").getString())){widget.setX(mid+2);widget.setY(screen.height-22);widget.setWidth(100);}
            if(widget.getMessage().getString().equals(Component.translatable("lectern.take_book").getString())){widget.setMessage(Component.translatable("gui.the_oldest_house.keep_account"));widget.setX(mid-102);widget.setY(screen.height-22);widget.setWidth(100);widget.active=data.getBoolean("CanKeepSource");}
        }
        var previous=button("previous_passage",mid-139,screen.height-44,UncannyReading.PREVIOUS);previous.active=UncannyReading.adjacent(mask,index,false)!=index;e.addListener(previous);
        e.addListener(button("full_account",mid-45,screen.height-44,UncannyReading.FULL));
        var next=button("next_passage",mid+49,screen.height-44,UncannyReading.NEXT);next.active=UncannyReading.adjacent(mask,index,true)!=index;e.addListener(next);
    }
    private static Button button(String key,int x,int y,int id){return Button.builder(Component.translatable("gui.the_oldest_house."+key),b->{var mc=Minecraft.getInstance();if(mc.gameMode!=null&&mc.player!=null)mc.gameMode.handleInventoryButtonClick(mc.player.containerMenu.containerId,id);}).bounds(x,y,90,20).build();}
}

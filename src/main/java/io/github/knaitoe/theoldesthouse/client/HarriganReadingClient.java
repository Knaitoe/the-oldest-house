package io.github.knaitoe.theoldesthouse.client;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.inventory.LecternScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
/** The current interjection stays visible beside the actual owner-checked lectern page. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class HarriganReadingClient {
    private static ClientLevel world;private static int lease;private static String line="";
    private HarriganReadingClient(){}
    public static void accept(CompoundTag tag){var mc=Minecraft.getInstance();if(mc.player==null||!tag.getBoolean("HarriganReading")||!tag.hasUUID("Reader")||!tag.getUUID("Reader").equals(mc.player.getUUID()))return;world=mc.level;lease=25;line=tag.getString("HarriganLine");if(line.length()>300)line=line.substring(0,300);}
    @SubscribeEvent public static void tick(ClientTickEvent.Post e){var mc=Minecraft.getInstance();if(lease>0)lease--;if(lease==0||mc.level!=world||mc.player==null||!mc.player.isAlive()){world=null;lease=0;line="";}}
    @SubscribeEvent public static void init(ScreenEvent.Init.Post e){if(!(e.getScreen() instanceof LecternScreen screen)||screen.width>=500)return;for(var listener:screen.children())if(listener instanceof AbstractWidget widget&&widget.getMessage().getString().equals(Component.translatable("gui.done").getString()))widget.setY(screen.height-22);}
    @SubscribeEvent public static void render(ScreenEvent.Render.Post e){var mc=Minecraft.getInstance();var screen=e.getScreen();if(!(screen instanceof LecternScreen)||lease<=0||world!=mc.level||line.isBlank())return;boolean side=screen.width>=500;int x=side?screen.width/2+112:16,w=side?Math.min(190,screen.width-x-16):screen.width-32;var lines=mc.font.split(Component.literal(line),w-20);int height=lines.size()*10+20,y=side?55:Math.max(8,screen.height-height-26);var g=e.getGuiGraphics();g.fill(x,y,x+w,y+height,0xef17151a);g.fill(x,y,x+2,y+height,0xffa69779);int row=y+10;for(var text:lines){g.drawString(mc.font,text,x+10,row,0xffe5dfd2,false);row+=10;}}
}

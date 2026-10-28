package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/** A spatial presentation clock needs no trait or save-data packet. */
@net.neoforged.fml.common.EventBusSubscriber(modid=io.github.knaitoe.theoldesthouse.TheOldestHouse.MOD_ID,value=net.neoforged.api.distmarker.Dist.CLIENT)
public final class HouseReadClient {
    private HouseReadClient(){}
    @net.neoforged.bus.api.SubscribeEvent public static void noteButtons(net.neoforged.neoforge.client.event.ScreenEvent.Render.Pre e){
        var mc=Minecraft.getInstance();if(!(e.getScreen() instanceof net.minecraft.client.gui.screens.inventory.LecternScreen)||mc.player==null||!(mc.player.containerMenu instanceof net.minecraft.world.inventory.LecternMenu menu))return;
        var data=menu.getSlot(0).getItem().get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);if(data==null||!data.copyTag().getBoolean("HouseReadNote"))return;
        for(var child:e.getScreen().children())if(child instanceof net.minecraft.client.gui.components.Button b&&b.getMessage().getContents() instanceof net.minecraft.network.chat.contents.TranslatableContents message&&message.getKey().equals("lectern.take_book")){b.visible=false;b.active=false;}
    }
    public static HouseReadSites.Room room(){
        var mc=Minecraft.getInstance();var origin=HouseSightlineState.origin();if(mc.player==null||mc.level==null||origin==null)return null;
        int index=(int)Math.round((mc.player.getX()-origin.getX()-8192-.5)/4096);if(index<0||index>=4096)return null;
        for(var room:HouseReadSites.Room.values()){if(!mc.level.dimension().equals(room.dimension()))continue;var b=new BlockPos(origin.getX()+8192+index*4096,80,origin.getZ()-room.offsetZ);if(HouseReadRooms.bounds(b,room).contains(mc.player.position()))return room;}return null;
    }
    public static long time(Level level,long ordinary){var mc=Minecraft.getInstance();if(level!=mc.level)return ordinary;var room=room();return room==HouseReadSites.Room.PORCH?12500:room==HouseReadSites.Room.WAITING?9700:room==HouseReadSites.Room.BATHROOM?17000:room==HouseReadSites.Room.SHELTER?16000:ordinary;}
    @net.neoforged.bus.api.SubscribeEvent public static void music(net.neoforged.neoforge.client.event.sound.PlaySoundEvent e){if(e.getSound()!=null&&e.getSound().getSource()==net.minecraft.sounds.SoundSource.MUSIC&&room()!=null)e.setSound(null);}
    @net.neoforged.bus.api.SubscribeEvent public static void quiet(net.neoforged.neoforge.client.event.ClientTickEvent.Post e){var mc=Minecraft.getInstance();if(mc.player!=null&&mc.player.tickCount%20==0&&room()!=null){mc.getMusicManager().stopPlaying();mc.getSoundManager().stop(null,net.minecraft.sounds.SoundSource.MUSIC);}}
}

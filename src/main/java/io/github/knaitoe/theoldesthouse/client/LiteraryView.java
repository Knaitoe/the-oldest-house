package io.github.knaitoe.theoldesthouse.client;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.LiteraryActor;
import io.github.knaitoe.theoldesthouse.network.LiteraryViewPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.nbt.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
/** Presentation never moves the native player or changes game mode; walking off a mark stays possible. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class LiteraryView {
    private static ClientLevel cameraWorld,modelWorld;private static int lease,drowse,drowseLease,screenLease;private static CompoundTag models=new CompoundTag();
    private LiteraryView(){}
    public static void accept(LiteraryViewPayload payload){var mc=Minecraft.getInstance();if(mc.level==null||mc.player==null||mc.player.isSpectator())return;
        if(payload.models().contains("Models")){var list=payload.models().getList("Models",Tag.TAG_COMPOUND);if(list.size()<=3&&list.stream().allMatch(t->((CompoundTag)t).getIntArray("Blocks").length<=256)&&payload.models().hasUUID("Reader")&&payload.models().getUUID("Reader").equals(mc.player.getUUID())){models=payload.models().copy();modelWorld=mc.level;}}
        if(payload.models().hasUUID("Reader")&&payload.models().getUUID("Reader").equals(mc.player.getUUID())&&payload.models().contains("Scar")){models=payload.models().copy();modelWorld=mc.level;}
        if(payload.models().hasUUID("Reader")&&payload.models().getUUID("Reader").equals(mc.player.getUUID())&&payload.models().getIntArray("Screen").length==384){models=payload.models().copy();modelWorld=mc.level;screenLease=Math.max(0,Math.min(30,payload.ticks()));}
        drowse=Math.max(0,Math.min(90,payload.drowse()));drowseLease=Math.max(0,Math.min(30,payload.ticks()));
        if(payload.actor()<0){if(payload.ticks()==0)restore();return;}var entity=mc.level.getEntity(payload.actor());if(!(entity instanceof LiteraryActor actor)||actor.role()!=LiteraryActor.CAMERA||actor.owner().isEmpty()||!actor.owner().get().equals(mc.player.getUUID()))return;restore();lease=Math.max(0,Math.min(60,payload.ticks()));cameraWorld=mc.level;mc.setCameraEntity(actor);
    }
    public static CompoundTag models(){return Minecraft.getInstance().level==modelWorld?models.copy():new CompoundTag();}
    public static void restore(){var mc=Minecraft.getInstance();if(cameraWorld!=null&&mc.level==cameraWorld&&mc.player!=null)mc.setCameraEntity(mc.player);lease=0;cameraWorld=null;}
    @SubscribeEvent public static void tick(ClientTickEvent.Post e){var mc=Minecraft.getInstance();if(lease>0&&(--lease<=0||mc.level!=cameraWorld||mc.player==null||!mc.player.isAlive()||mc.player.isSpectator()))restore();if(screenLease>0){if(--screenLease==0)models.remove("Screen");}if(drowseLease>0)drowseLease--;else drowse=0;if(mc.level!=modelWorld){models=new CompoundTag();modelWorld=null;}}
    @SubscribeEvent public static void overlay(RenderGuiEvent.Post e){if(drowse<=0||drowseLease<=0)return;var mc=Minecraft.getInstance();if(mc.player==null||!mc.player.isAlive())return;int color=((int)(drowse/100F*255)<<24);e.getGuiGraphics().fill(0,0,mc.getWindow().getGuiScaledWidth(),mc.getWindow().getGuiScaledHeight(),color);}
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut e){restore();models=new CompoundTag();modelWorld=null;drowse=0;drowseLease=0;screenLease=0;}
}

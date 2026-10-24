package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.gametest.LiveExpeditionProof;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.nio.file.*;
import net.minecraft.client.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Display;
import net.minecraft.world.phys.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

/** Actual native displays in the world framebuffer, not separately posed item models. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class SupperSocketClient {
    private static int step,age,verified,clicked;private static String role;private static Vec3 plate;
    private static int[] empty;private static boolean pending;
    private SupperSocketClient(){}
    public static boolean ready(Minecraft mc,int next,int ticks,String viewer,BlockPos base){
        if(step!=next){step=next;verified=0;clicked=0;pending=false;}age=ticks;role=viewer;plate=GoatmanWoods.plate(viewer.equals("A")?0:1).add(base.getX(),base.getY(),base.getZ());
        mc.options.keyUp.setDown(false);mc.options.keyShift.setDown(false);mc.player.getInventory().selected=next==45?7:0;
        var to=plate.subtract(mc.player.getEyePosition());mc.player.setYRot((float)Math.toDegrees(Math.atan2(-to.x,to.z)));mc.player.setXRot((float)-Math.toDegrees(Math.atan2(to.y,Math.hypot(to.x,to.z))));
        if((next==44||next==45)&&ticks>40&&clicked==0){
            var hit=mc.level.clip(new net.minecraft.world.level.ClipContext(mc.player.getEyePosition(),mc.player.getEyePosition().add(mc.player.getLookAngle().scale(mc.player.blockInteractionRange())),net.minecraft.world.level.ClipContext.Block.OUTLINE,net.minecraft.world.level.ClipContext.Fluid.NONE,mc.player));
            if(hit.getType()!=HitResult.Type.BLOCK)throw new IllegalStateException("LIVE SUPPER: the actual child's ray missed the native table");
            mc.gameMode.useItemOn(mc.player,InteractionHand.MAIN_HAND,hit);mc.gameMode.useItem(mc.player,InteractionHand.MAIN_HAND);clicked=1;
        }
        if(ticks>70){
            int expected=viewer.equals("A")?2:1,held=mc.player.getInventory().countItem(GoatmanRegistry.BRAT.get());
            var food=mc.level.getEntitiesOfClass(Display.ItemDisplay.class,new AABB(plate,plate).inflate(.3),d->d.itemRenderState()!=null&&d.itemRenderState().itemStack().is(GoatmanRegistry.BRAT.get()));
            boolean clear=(next==43||next==45)?food.isEmpty():food.size()==1;
            boolean inventory=next==43?held==0:next==45?held==expected:held==expected-1;
            pending=clear&&inventory;
        }
        return verified==next;
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post event)throws Exception{
        if(!LiveExpeditionProof.enabled()||!pending||verified==step||step<43||step>46)return;var mc=Minecraft.getInstance();
        try(NativeImage image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){
            int w=96,h=96,x=image.getWidth()/2-w/2,y=image.getHeight()/2-h/2;int[] pixels=new int[w*h];for(int i=0;i<pixels.length;i++)pixels[i]=image.getPixelRGBA(x+i%w,y+i/w);
            if(step==43)empty=pixels;
            if(step==44||step==46){
                if(empty==null)throw new IllegalStateException("LIVE SUPPER: missing real empty-plate reference");int changed=0;
                for(int i=0;i<pixels.length;i++){int before=empty[i],after=pixels[i],r=after&255,g=after>>8&255,b=after>>16&255,br=before&255,bg=before>>8&255,bb=before>>16&255;
                    if(br>65&&bg>60&&bb>40&&Math.abs(br-bg)<35&&r>g+8&&g>b+8&&Math.abs(r-br)+Math.abs(g-bg)+Math.abs(b-bb)>45)changed++;
                }
                if(changed<6){if(age<180)return;int white=0,brown=0;for(int i=0;i<pixels.length;i++){int br=empty[i]&255,bg=empty[i]>>8&255,bb=empty[i]>>16&255,r=pixels[i]&255,g=pixels[i]>>8&255,b=pixels[i]>>16&255;if(br>65&&bg>60&&bb>40&&Math.abs(br-bg)<35)white++;if(r>g+8&&g>b+8)brown++;}
                    throw new IllegalStateException("LIVE SUPPER: tracked food has no visible brown pixels over the actual white plate; changed="+changed+" white-before="+white+" brown-after="+brown+" eye="+mc.player.getEyePosition()+" plate="+plate);}
                Files.createDirectories(LiveExpeditionProof.folder());Files.writeString(LiveExpeditionProof.folder().resolve(role+"-supper-pixels-"+step+".txt"),"Actual child-eye native world render: "+changed+" white-plate pixels changed to visible food.\n");
            }
            var label=step==43?"empty":step==44?"served":step==45?"picked-up":"old-original-repaired";Files.createDirectories(LiveExpeditionProof.folder());image.writeToFile(LiveExpeditionProof.folder().resolve(role+"-supper-"+label+".png"));verified=step;pending=false;
        }
    }
}

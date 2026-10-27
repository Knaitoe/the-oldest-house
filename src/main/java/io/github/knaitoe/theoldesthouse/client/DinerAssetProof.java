package io.github.knaitoe.theoldesthouse.client;
import com.mojang.blaze3d.platform.NativeImage;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.nio.file.*;
import java.util.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
/** Production item predicates and real stitched meal textures, including the legacy stack. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class DinerAssetProof extends Screen {
    private int frames;
    private record View(String label,ItemStack stack){}
    public DinerAssetProof(){super(Component.literal("Breakfast / native item review"));}
    private List<View> views(){var rows=new ArrayList<View>();for(boolean served:new boolean[]{false,true})for(var order:UncannyBreakfast.Order.values()){
        var s=new ItemStack(served?UncannyRegistry.MEAL.get():UncannyRegistry.BREAKFAST_ICON.get());s.set(DataComponents.CUSTOM_MODEL_DATA,new CustomModelData(order.ordinal()+1));rows.add(new View((served?"served ":"menu ")+order.label,s));
    }rows.add(new View("handed menu",new ItemStack(UncannyRegistry.MENU.get())));rows.add(new View("legacy plate",new ItemStack(UncannyRegistry.MEAL.get())));return rows;}
    @Override public void renderBackground(GuiGraphics g,int x,int y,float partial){}
    @Override public void render(GuiGraphics g,int x,int y,float partial){
        var mc=Minecraft.getInstance();g.fill(0,0,width,height,0xffb1b3a2);g.drawString(font,"Native breakfast choices / individually spoiled plates / finite handed menu",10,10,0xff26322b,false);
        var views=views();float w=width/4F,h=(height-35)/3F,scale=Math.min(w*.65F,h*.65F)/16;
        for(int i=0;i<views.size();i++){var v=views.get(i);var model=mc.getItemRenderer().getModel(v.stack,mc.level,null,0);if(model==mc.getModelManager().getMissingModel()||model.getParticleIcon().contents().name().getPath().equals("missingno"))throw new IllegalStateException("Missing actual breakfast model/texture: "+v.label);
            g.pose().pushPose();g.pose().translate((i%4+.5)*w-8*scale,35+(i/4+.2)*h,150);g.pose().scale(scale,scale,1);g.renderItem(v.stack,0,0);g.pose().popPose();g.drawCenteredString(font,v.label,(int)((i%4+.5)*w),(int)(35+(i/4+.85)*h),0xff26322b);
        }
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post e)throws Exception{var mc=Minecraft.getInstance();if(!(mc.screen instanceof DinerAssetProof s)||++s.frames<14)return;var dir=Path.of("../build/font-smoke");Files.createDirectories(dir);try(NativeImage im=Screenshot.takeScreenshot(mc.getMainRenderTarget())){im.writeToFile(dir.resolve("native-diner-breakfasts.png"));}Files.writeString(dir.resolve("diner-assets-passed.txt"),"All four ordinary menu icons, four spoiled native plate variants, the handed menu and legacy meal fallback resolve real production models/textures.\n");mc.stop();}
}

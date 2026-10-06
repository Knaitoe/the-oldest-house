package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import java.nio.file.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

/** The actual registered items and baked GUI models, rather than source-image previews. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class InventoryVisualProof extends Screen {
    private static final String[] ITEMS={"holloway_shield","lighter","chalk","trail_spool","archive_key","cat_collar","well_ribbon",
            "blindfold","church_key","walkie_talkie","phone","harrigans_phone","lake_phone","scratch_ticket",
            "trailer_plate","shallows_burden","dried_essay_one","dried_essay_two","dried_essay_three",
            "waterlogged_essay_one","waterlogged_essay_two","waterlogged_essay_three"};
    private int frames;
    public InventoryVisualProof(){super(Component.literal("Inventory artwork"));}
    @Override public void renderBackground(GuiGraphics g,int x,int y,float delta){}
    @Override public void render(GuiGraphics g,int x,int y,float delta){
        var mc=Minecraft.getInstance();g.fill(0,0,width,height,0xFF27241F);g.drawString(font,"Native custom inventory / 22 items",12,10,0xFFE4DACA,false);
        int cellW=width/5,cellH=(height-30)/5;
        for(int i=0;i<ITEMS.length;i++){
            var stack=new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,ITEMS[i])));
            if(stack.isEmpty()||mc.getItemRenderer().getModel(stack,null,null,0)==mc.getModelManager().getMissingModel())throw new IllegalStateException("Missing native item model: "+ITEMS[i]);
            int left=(i%5)*cellW+5,top=28+(i/5)*cellH;
            g.fill(left,top,left+cellW-10,top+cellH-5,0xFF3B352D);var pose=g.pose();pose.pushPose();pose.translate(left+cellW/2F-21,top+4,0);pose.scale(2,2,2);g.renderItem(stack,0,0);pose.popPose();
            g.drawString(font,font.plainSubstrByWidth(stack.getHoverName().getString(),cellW-18),left+4,top+38,0xFFE4DACA,false);
        }
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post e)throws Exception{
        var mc=Minecraft.getInstance();if(!Boolean.getBoolean("the_oldest_house.fontSmoke")||!(mc.screen instanceof InventoryVisualProof p)||++p.frames<4)return;
        var dir=Path.of("../build/font-smoke");Files.createDirectories(dir);try(NativeImage im=Screenshot.takeScreenshot(mc.getMainRenderTarget())){im.writeToFile(dir.resolve("native-inventory.png"));}
        Files.writeString(dir.resolve("inventory-passed.txt"),"All twenty-two registered custom inventory items rendered with native baked models.\n");mc.setScreen(new CompanionVisualProof());
    }
}

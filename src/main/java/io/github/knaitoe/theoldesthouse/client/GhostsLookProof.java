package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseBlocks;
import io.github.knaitoe.theoldesthouse.house.HouseholdFurnitureBlock;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

/**
 * A developer's look at corners of the Ghosts set built from the builders' own coordinates, drawn with the production block
 * models (0.4.199, for the owner's notes on the living room, the den's television, the stair's banister and the basement drop).
 * Run with -PghostsLook; it saves one image per corner to build/ghosts-look and closes.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class GhostsLookProof extends Screen {
    record Scene(String name,float yaw,float pitch,List<BlockPos> at,List<BlockState> states){}
    private int page,frames;private static boolean launched;private final List<Scene> scenes=scenes();
    public GhostsLookProof(){super(Component.literal("Ghosts set / a look"));}
    @SubscribeEvent public static void launch(ClientTickEvent.Post ignored){var mc=Minecraft.getInstance();if(Boolean.getBoolean("the_oldest_house.ghostsLook")&&!launched&&mc.screen!=null&&mc.getOverlay()==null){launched=true;mc.options.guiScale().set(2);mc.resizeDisplay();mc.setScreen(new GhostsLookProof());}}
    private static BlockState barrett(BarrettHomeBlock.Kind k,Direction f){return BarrettHomeBlock.of(k,f);}
    private static BlockState ghost(GhostsSetBlock.Kind k,Direction f){return GhostsSetBlock.of(k,f);}
    private static BlockState furniture(HouseholdFurnitureBlock.Kind k,Direction f){return HouseBlocks.HOUSEHOLD_FURNITURE.get().defaultBlockState().setValue(HouseholdFurnitureBlock.KIND,k).setValue(HouseholdFurnitureBlock.FACING,f);}
    private static List<Scene> scenes(){
        var out=new ArrayList<Scene>();var paint=BarrettHomeBlock.of(BarrettHomeBlock.Kind.PAINT);var floor=ghost(GhostsSetBlock.Kind.FLOOR,Direction.NORTH);
        {var at=new ArrayList<BlockPos>();var st=new ArrayList<BlockState>();
            for(int x=-11;x<=-7;x++){at.add(new BlockPos(x,-1,-11));st.add(floor);at.add(new BlockPos(x,-1,-10));st.add(floor);for(int y=0;y<=3;y++){at.add(new BlockPos(x,y,-12));st.add(paint);}}
            at.add(new BlockPos(-10,0,-11));st.add(barrett(BarrettHomeBlock.Kind.BOOKSHELF,Direction.SOUTH));at.add(new BlockPos(-9,0,-11));st.add(barrett(BarrettHomeBlock.Kind.DRESSER,Direction.SOUTH));at.add(new BlockPos(-9,2,-11));st.add(barrett(BarrettHomeBlock.Kind.PHOTOS,Direction.SOUTH));
            out.add(new Scene("front_room_wall",20,15,at,st));}
        {var at=new ArrayList<BlockPos>();var st=new ArrayList<BlockState>();
            for(int z=-9;z<=-4;z++){at.add(new BlockPos(5,-1,z));st.add(floor);for(int y=0;y<=3;y++){at.add(new BlockPos(4,y,z));st.add(paint);}}
            at.add(new BlockPos(5,0,-6));st.add(barrett(BarrettHomeBlock.Kind.TV_STAND,Direction.EAST));at.add(new BlockPos(5,0,-8));st.add(barrett(BarrettHomeBlock.Kind.BOOKSHELF,Direction.EAST));at.add(new BlockPos(5,2,-6));st.add(barrett(BarrettHomeBlock.Kind.PHOTOS,Direction.EAST));
            out.add(new Scene("den_tv",-70,15,at,st));}
        {var at=new ArrayList<BlockPos>();var st=new ArrayList<BlockState>();
            for(int i=0;i<5;i++)for(int x=-3;x<=-1;x++){for(int y=-1;y<i;y++){at.add(new BlockPos(x,y,-4-i));st.add(floor);}at.add(new BlockPos(x,i,-4-i));st.add(ghost(GhostsSetBlock.Kind.STAIR,Direction.NORTH));}
            for(int i=0;i<5;i++){at.add(new BlockPos(0,i+1,-4-i));st.add(ghost(i==4?GhostsSetBlock.Kind.STAIR_RAIL_TOP:GhostsSetBlock.Kind.STAIR_RAIL,Direction.NORTH));}
            for(int x=-3;x<=3;x++)for(int z=-12;z<=-9;z++){at.add(new BlockPos(x,4,z));st.add(BarrettHomeBlock.of(BarrettHomeBlock.Kind.STOREY));}
            for(int x=1;x<=3;x++){at.add(new BlockPos(x,5,-9));st.add(ghost(GhostsSetBlock.Kind.RAIL,Direction.EAST));}
            out.add(new Scene("stair_banister",-60,12,at,st));}
        {var at=new ArrayList<BlockPos>();var st=new ArrayList<BlockState>();
            for(int x=6;x<=10;x++)for(int z=-20;z<=-13;z++){boolean well=x>=7&&x<=9&&z>=-17&&z<=-14;if(!well){at.add(new BlockPos(x,-1,z));st.add(floor);}}
            for(int i=0;i<4;i++)for(int x=7;x<=9;x++){at.add(new BlockPos(x,-1-i,-14-i));st.add(Blocks.OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.SOUTH));}
            at.add(new BlockPos(10,0,-20));st.add(barrett(BarrettHomeBlock.Kind.WASHER,Direction.WEST));at.add(new BlockPos(10,0,-21));st.add(barrett(BarrettHomeBlock.Kind.DRYER,Direction.WEST));
            out.add(new Scene("basement_drop",160,35,at,st));}
        {var at=new ArrayList<BlockPos>();var st=new ArrayList<BlockState>();
            for(int x=-10;x<=-5;x++)for(int z=-21;z<=-13;z++){at.add(new BlockPos(x,-1,z));st.add(floor);}
            for(int z=-21;z<=-13;z++)for(int y=0;y<=2;y++){at.add(new BlockPos(-11,y,z));st.add(paint);}
            at.add(new BlockPos(-9,0,-17));st.add(furniture(HouseholdFurnitureBlock.Kind.GREEN_ARMCHAIR,Direction.EAST));at.add(new BlockPos(-6,0,-17));st.add(furniture(HouseholdFurnitureBlock.Kind.FLORAL_ARMCHAIR,Direction.WEST));
            for(int x=-8;x<=-7;x++)for(int z=-18;z<=-14;z++){at.add(new BlockPos(x,0,z));st.add(ghost(GhostsSetBlock.Kind.RUG,Direction.NORTH));}
            at.add(new BlockPos(-9,0,-18));st.add(furniture(HouseholdFurnitureBlock.Kind.BEDSIDE_TABLE,Direction.EAST));at.add(new BlockPos(-9,1,-18));st.add(ghost(GhostsSetBlock.Kind.BOOKS,Direction.EAST));at.add(new BlockPos(-10,0,-20));st.add(barrett(BarrettHomeBlock.Kind.BOOKSHELF,Direction.EAST));
            at.add(new BlockPos(-8,0,-13));st.add(barrett(BarrettHomeBlock.Kind.TV_STAND,Direction.NORTH));
            out.add(new Scene("family_corner",-120,30,at,st));}
        return out;
    }
    @Override public void renderBackground(GuiGraphics g,int x,int y,float partial){}
    @Override public void render(GuiGraphics g,int mx,int my,float partial){
        var mc=Minecraft.getInstance();g.fill(0,0,width,height,0xff2a2622);var scene=scenes.get(page);g.drawString(font,"Ghosts set / "+scene.name(),10,10,0xffe8ddc9,false);g.flush();
        var buffers=mc.renderBuffers().bufferSource();
        int minX=Integer.MAX_VALUE,maxX=Integer.MIN_VALUE,minY=Integer.MAX_VALUE,maxY=Integer.MIN_VALUE,minZ=Integer.MAX_VALUE,maxZ=Integer.MIN_VALUE;
        for(var p:scene.at()){minX=Math.min(minX,p.getX());maxX=Math.max(maxX,p.getX());minY=Math.min(minY,p.getY());maxY=Math.max(maxY,p.getY());minZ=Math.min(minZ,p.getZ());maxZ=Math.max(maxZ,p.getZ());}
        float cx=(minX+maxX+1)/2F,cy=(minY+maxY+1)/2F,cz=(minZ+maxZ+1)/2F,size=Math.max(maxX-minX,Math.max(maxY-minY,maxZ-minZ))+1;float scale=Math.min(width,height)*.62F/size;
        var pose=g.pose();pose.pushPose();pose.translate(width/2F,height/2F+10,400);pose.scale(scale,-scale,scale);pose.mulPose(Axis.XP.rotationDegrees(scene.pitch()));pose.mulPose(Axis.YP.rotationDegrees(scene.yaw()));pose.translate(-cx,-cy,-cz);
        for(int i=0;i<scene.at().size();i++){var p=scene.at().get(i);pose.pushPose();pose.translate(p.getX(),p.getY(),p.getZ());mc.getBlockRenderer().renderSingleBlock(scene.states().get(i),pose,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);pose.popPose();}
        buffers.endBatch();pose.popPose();
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post e)throws Exception{
        var mc=Minecraft.getInstance();if(!(mc.screen instanceof GhostsLookProof s)||++s.frames<16)return;var dir=Path.of("../build/ghosts-look");Files.createDirectories(dir);
        try(NativeImage image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(dir.resolve(s.scenes.get(s.page).name()+".png"));}
        if(++s.page<s.scenes.size()){s.frames=0;return;}mc.stop();
    }
}

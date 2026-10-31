package io.github.knaitoe.theoldesthouse.client;

import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.nio.file.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import static io.github.knaitoe.theoldesthouse.labyrinth.NightWardBlock.Kind.*;

/** Production hospital meshes and atlas quads; a staged cutaway and two fitted-material sheets. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class NightWardVisualProof extends Screen {
    private int page,frames;private boolean checked;
    public NightWardVisualProof(){super(Component.literal("Night-duty hospital"));}
    @Override public void renderBackground(GuiGraphics g,int x,int y,float partial){}
    private static List<BlockState> states(){
        var out=new ArrayList<BlockState>();for(var k:NightWardBlock.Kind.values())out.add(NightWardBlock.of(k));
        out.add(NovelRegistry.NIGHT_WARD_DOOR.get().defaultBlockState());out.add(NovelRegistry.NIGHT_WARD_DOOR.get().defaultBlockState().setValue(DoorBlock.HALF,DoubleBlockHalf.UPPER));
        out.add(NovelRegistry.NIGHT_WARD_GLASS.get().defaultBlockState());out.add(NovelRooms.prop(NovelPropBlock.Kind.INCUBATOR,Direction.NORTH));
        for(var part:NightWardCurtainBlock.Part.values())out.add(curtain(part,true,true,false,false));
        return out;
    }
    /** 0.4.199: a bed curtain joined the ways given. */
    private static BlockState curtain(NightWardCurtainBlock.Part part,boolean north,boolean south,boolean east,boolean west){
        return NightWardCurtainBlock.of(part).setValue(net.minecraft.world.level.block.IronBarsBlock.NORTH,north).setValue(net.minecraft.world.level.block.IronBarsBlock.SOUTH,south)
            .setValue(net.minecraft.world.level.block.IronBarsBlock.EAST,east).setValue(net.minecraft.world.level.block.IronBarsBlock.WEST,west);
    }
    private static int pages(){return 1+(states().size()+8)/9;}
    private static String name(BlockState s){
        if(s.hasProperty(NightWardBlock.KIND))return s.getValue(NightWardBlock.KIND).getSerializedName().replace('_',' ');
        if(s.hasProperty(NightWardCurtainBlock.PART))return "curtain "+s.getValue(NightWardCurtainBlock.PART).getSerializedName();
        if(s.is(NovelRegistry.NIGHT_WARD_DOOR.get()))return "enamel door";if(s.is(NovelRegistry.NIGHT_WARD_GLASS.get()))return "wired glass";return "empty incubator";
    }
    private static void check(BlockState s){
        var mc=Minecraft.getInstance();var model=mc.getBlockRenderer().getBlockModel(s);int count=0;
        if(model==mc.getModelManager().getMissingModel())throw new IllegalStateException("Missing night-duty hospital model: "+s);
        for(int f=-1;f<6;f++)for(var q:model.getQuads(s,f<0?null:Direction.values()[f],RandomSource.create(1))){
            count++;var id=q.getSprite().contents().name();if(!id.getNamespace().equals(TheOldestHouse.MOD_ID)||!id.getPath().startsWith("block/night_ward/"))throw new IllegalStateException("Hospital placeholder or missing material: "+s+" / "+id);
        }
        if(count==0)throw new IllegalStateException("Empty hospital model: "+s);
    }
    private static void block(GuiGraphics g,BlockState s,int x,int y,int z){
        var mc=Minecraft.getInstance();var p=g.pose();p.pushPose();p.translate(x,y,z);mc.getBlockRenderer().renderSingleBlock(s,p,mc.renderBuffers().bufferSource(),LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);p.popPose();
    }
    /** A bay of the clinical ward (0.4.199): bed, service panel, locker, curtains half drawn, the monitor on its stand, the call column. */
    private void cutaway(GuiGraphics g){
        var p=g.pose();p.pushPose();p.translate(width*.5,height*.70,180);float scale=Math.min(width/14F,height/10F);p.scale(scale,-scale,scale);p.mulPose(Axis.XP.rotationDegrees(29));p.mulPose(Axis.YP.rotationDegrees(-27));p.translate(0,0,3);
        for(int x=-4;x<=4;x++)for(int z=-7;z<=0;z++)block(g,NightWardBlock.of(FLOOR),x,-1,z);
        for(int x=-4;x<=4;x++)for(int y=0;y<=2;y++)block(g,NightWardBlock.of(y==0?DADO:WALL),x,y,-7);
        for(int z=-6;z<=0;z++)for(int y=0;y<=2;y++)block(g,NightWardBlock.of(y==0?DADO:WALL),-4,y,z);
        block(g,NightWardBlock.of(BED_HEAD,Direction.WEST),-3,0,-4);block(g,NightWardBlock.of(BED_FOOT,Direction.WEST),-2,0,-4);block(g,NightWardBlock.of(BED_UNIT,Direction.EAST),-3,1,-4);
        block(g,NightWardBlock.of(BEDSIDE,Direction.EAST),-3,0,-5);block(g,NightWardBlock.of(NIGHT_LIGHT,Direction.EAST),-3,0,-3);block(g,NightWardBlock.of(CHAIR,Direction.NORTH),-2,0,-3);
        var lower=NightWardCurtainBlock.Part.LOWER;var upper=NightWardCurtainBlock.Part.UPPER;var rail=NightWardCurtainBlock.Part.RAIL;
        for(int x=-3;x<=-1;x++){block(g,curtain(lower,false,false,true,true),x,0,-6);block(g,curtain(upper,false,false,true,true),x,1,-6);}
        block(g,curtain(lower,false,true,false,true),0,0,-6);block(g,curtain(upper,false,true,false,true),0,1,-6);
        block(g,curtain(lower,true,false,false,false),0,0,-5);block(g,curtain(upper,true,true,false,false),0,1,-5);
        block(g,curtain(rail,true,true,false,false),0,1,-4);block(g,curtain(rail,true,false,false,false),0,1,-3);
        block(g,NovelRooms.prop(NovelPropBlock.Kind.INCUBATOR,Direction.SOUTH),2,0,-3);block(g,NightWardBlock.of(CHAIR,Direction.WEST),3,0,-1);
        block(g,NightWardBlock.of(MONITOR_STAND,Direction.WEST),3,0,-4);block(g,NightWardBlock.of(MONITOR,Direction.WEST),3,1,-4);
        for(int y=0;y<=2;y++)block(g,NightWardBlock.of(y==1?CALL_PANEL:COLUMN,Direction.WEST),4,y,-6);
        Minecraft.getInstance().renderBuffers().bufferSource().endBatch();p.popPose();
    }
    @Override public void render(GuiGraphics g,int x,int y,float partial){
        var mc=Minecraft.getInstance();if(!checked){
            for(var b:List.of(NovelRegistry.NIGHT_WARD.get(),NovelRegistry.NIGHT_WARD_GLASS.get(),NovelRegistry.NIGHT_WARD_DOOR.get(),NovelRegistry.NIGHT_WARD_CURTAIN.get()))for(var s:b.getStateDefinition().getPossibleStates())check(s);
            for(var d:Direction.Plane.HORIZONTAL)check(NovelRooms.prop(NovelPropBlock.Kind.INCUBATOR,d));checked=true;
        }
        g.fill(0,0,width,height,0xff293638);g.drawString(font,page==0?"Night-duty hospital / native material cutaway":"Night-duty hospital / fitted materials and props "+page,10,9,0xffe4e3d0,false);g.flush();if(page==0){cutaway(g);return;}
        var states=states();float cw=(width-20)/3F,ch=(height-35)/3F;int start=(page-1)*9;
        for(int i=start;i<Math.min(start+9,states.size());i++){
            int n=i-start;var s=states.get(i);var p=g.pose();p.pushPose();p.translate(10+(n%3+.5F)*cw,34+(n/3+.72F)*ch,150);float scale=Math.min(60,ch*.59F);p.scale(scale,-scale,scale);p.mulPose(Axis.XP.rotationDegrees(24));p.mulPose(Axis.YP.rotationDegrees(32));p.translate(-.5,0,-.5);mc.getBlockRenderer().renderSingleBlock(s,p,mc.renderBuffers().bufferSource(),LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);mc.renderBuffers().bufferSource().endBatch();p.popPose();
            g.drawCenteredString(font,name(s),(int)(10+(n%3+.5F)*cw),(int)(33+(n/3+.91F)*ch),0xffe4e3d0);
        }
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post ignored)throws Exception{
        var mc=Minecraft.getInstance();if(!(mc.screen instanceof NightWardVisualProof proof)||++proof.frames<16)return;var dir=Path.of("../build/font-smoke");Files.createDirectories(dir);
        try(var im=Screenshot.takeScreenshot(mc.getMainRenderTarget())){im.writeToFile(dir.resolve("native-night-ward-"+proof.page+".png"));}proof.frames=0;if(++proof.page<pages())return;
        Files.writeString(dir.resolve("night-ward-passed.txt"),"All production night-duty hospital fixture, curtain, connected-glass and door states plus the empty incubator resolve only custom night_ward atlas sprites. "+pages()+" native GPU material views: a staged ward bay and "+(pages()-1)+" fixture sheets. Not an uninterrupted manual ward night.\n");
        if(Boolean.getBoolean("the_oldest_house.trailerSmoke"))mc.stop();else mc.setScreen(new CoffinVisualProof());
    }
}

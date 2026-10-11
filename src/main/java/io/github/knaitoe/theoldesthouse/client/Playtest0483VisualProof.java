package io.github.knaitoe.theoldesthouse.client;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseholdFurnitureBlock;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.nio.file.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
/** Shipped native meshes and actual pose functions, including the previously untested fan-floor image. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class Playtest0483VisualProof extends Screen {
    private static final String[] VIEWS={"fan-floor","camp-bunk","camp-decor","cottage-chess","paw-open","paw-curl","paw-closed"};
    private int page,frames;private final PawClutchClient.Model paw=new PawClutchClient.Model();
    public Playtest0483VisualProof(){super(Component.literal("Playtest repairs"));}
    @Override public void renderBackground(GuiGraphics g,int x,int y,float delta){}
    @Override protected void init(){var pose=new PoseStack();pose.translate(0,LiteraryRooms.FAN.getY(),0);LiteraryRenderers.ModelRenderer.fanElkPose(pose);
        for(float x:new float[]{-5/16F,5/16F})for(float y:new float[]{5/16F,17/16F}){
            var point=new org.joml.Vector3f(x,y,0).mulPosition(pose.last().pose());if(point.y<-.001||point.y>.626)throw new IllegalStateException("Fan elk's native body intersects the floor or floats above it: "+point);}
        paw.curl(2,0);if(paw.root.getChild("finger2").xRot!=0)throw new IllegalStateException("Paw begins with three open fingers");
        paw.curl(2,1);if(paw.root.getChild("finger2").xRot> -1.6F||paw.root.getChild("finger2").getChild("tip").xRot> -1.8F||paw.root.getChild("finger1").xRot!=0)throw new IllegalStateException("One wish must curl one finger through two native joints");
        paw.curl(0,1);for(int i=0;i<3;i++)if(paw.root.getChild("finger"+i).xRot> -1.6F)throw new IllegalStateException("The last wish leaves no raised fingers");
    }
    private static void block(Minecraft mc,net.minecraft.world.level.block.state.BlockState state,PoseStack p,MultiBufferSource out,int light,double x,double y,double z){p.pushPose();p.translate(x,y,z);mc.getBlockRenderer().renderSingleBlock(state,p,out,light,OverlayTexture.NO_OVERLAY);p.popPose();}
    @Override public void render(GuiGraphics g,int x,int y,float delta){var mc=Minecraft.getInstance();g.fill(0,0,width,height,0xff302d28);g.drawString(font,"Native playtest repair / "+VIEWS[page],12,10,0xffe0d5b7,false);g.flush();var p=g.pose();var out=mc.renderBuffers().bufferSource();int light=LightTexture.pack(12,0);
        p.pushPose();p.translate(width*.5,height*.70,160);float scale=page<4?Math.min(width*.19F,height*.30F):Math.min(width,height)*.6F;p.scale(scale,page<4?-scale:scale,scale);
        p.mulPose(Axis.XP.rotationDegrees(page<4?28:-8));p.mulPose(Axis.YP.rotationDegrees(page==0?25:page<4?35:18));
        if(page==0){for(int xx=-1;xx<=1;xx++)for(int zz=-1;zz<=1;zz++)block(mc,Blocks.BIRCH_PLANKS.defaultBlockState(),p,out,light,xx,-1,zz);
            p.pushPose();p.translate(0,LiteraryRooms.FAN.getY(),0);LiteraryRenderers.ModelRenderer.fanElk(new LiteraryRenderers.ElkModel(mc.getEntityModels().bakeLayer(LiteraryRenderers.ELK)),p,out,light,OverlayTexture.NO_OVERLAY,true);p.popPose();
        }else if(page==1){block(mc,CampDecorBlock.of(CampDecorBlock.Kind.BED_HEAD,Direction.NORTH,1),p,out,light,0,0,-1);block(mc,CampDecorBlock.of(CampDecorBlock.Kind.BED_FOOT,Direction.NORTH,1),p,out,light,0,0,0);
            for(int xx=-1;xx<=1;xx++)for(int zz=-1;zz<=1;zz++)block(mc,CampDecorBlock.of(CampDecorBlock.Kind.FLOOR,Direction.NORTH,1),p,out,light,xx,-1,zz);
            block(mc,CampDecorBlock.of(CampDecorBlock.Kind.BOOTS,Direction.NORTH,1),p,out,light,-1,0,0);block(mc,CampDecorBlock.of(CampDecorBlock.Kind.TRUNK,Direction.NORTH,1),p,out,light,1,0,-1);
        }else if(page==2){int index=0;for(var kind:new CampDecorBlock.Kind[]{CampDecorBlock.Kind.SIGN,CampDecorBlock.Kind.NOTICE,CampDecorBlock.Kind.FIRST_AID,CampDecorBlock.Kind.RING,CampDecorBlock.Kind.OAR})block(mc,CampDecorBlock.of(kind,Direction.NORTH,index%6),p,out,light,index++-2,0,0);
        }else if(page==3){block(mc,HouseholdFurnitureBlock.state(HouseholdFurnitureBlock.Kind.CHESS_TABLE,Direction.NORTH),p,out,light,0,0,0);block(mc,CottageBlock.of(CottageBlock.Kind.CHESSBOARD,Direction.NORTH),p,out,light,0,1,0);
            block(mc,HouseholdFurnitureBlock.state(HouseholdFurnitureBlock.Kind.GREEN_ARMCHAIR,Direction.NORTH),p,out,light,0,0,1);block(mc,HouseholdFurnitureBlock.state(HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.SOUTH),p,out,light,0,0,-1);
        }else{p.translate(0,-.4,0);paw.curl(page==6?0:2,page==4?0:page==5?.55F:1);paw.root.render(p,out.getBuffer(RenderType.entityCutoutNoCull(PawClutchClient.TEXTURE)),light,OverlayTexture.NO_OVERLAY,0xffffffff);}
        p.popPose();out.endBatch();
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post e)throws Exception{var mc=Minecraft.getInstance();if(!Boolean.getBoolean("the_oldest_house.fontSmoke")||!(mc.screen instanceof Playtest0483VisualProof proof)||++proof.frames<24)return;
        var dir=Path.of("../build/font-smoke");Files.createDirectories(dir);try(NativeImage im=Screenshot.takeScreenshot(mc.getMainRenderTarget())){im.writeToFile(dir.resolve("native-playtest0483-"+VIEWS[proof.page]+".png"));}
        if(++proof.page<VIEWS.length){proof.frames=0;return;}Files.writeString(dir.resolve("playtest0483-passed.txt"),"Seven native GPU views: corrected fan-floor elk axes and light; raised camp bunk, decor, chessboard and three stages of the paw's articulated curl. Native floor and two-joint finger invariants passed.\n");mc.setScreen(new SceneReviewVisualProof());
    }
}

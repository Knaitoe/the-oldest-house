package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.nio.file.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.model.*;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

/**
 * The 0.4.101 Shallows on its production meshes and transforms: the carry at rest and at both ends of a swing, the bank,
 * the throw, the break on the water, the getting up and the crossing. Bounds checks confirm she lies along the carry with
 * her head to the reader, swings toward the water, lies flat when broken and crawls low; they report after every view is saved.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class ShallowsVisualProof extends Screen {
    private static final String[] VIEWS={"carried, at rest","swung out over the water","swung back","held on the bank","thrown","broken on the water","getting up","crossing the water"};
    private static final ResourceLocation STEVE=ResourceLocation.withDefaultNamespace("textures/entity/player/wide/steve.png");
    private final List<String> problems=new ArrayList<>();
    private int page,frames;
    public ShallowsVisualProof(){super(Component.literal("Shallows"));}
    private static ResourceLocation texture(String name){return ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/"+name+".png");}
    private static ResourceLocation boy(int look){return texture("lake_boy"+(look>0?"_"+look:""));}

    private record Figure(EntityModel<?> model,ResourceLocation skin,double x,double y,double z,float yaw,int phase,float swing,float t){}
    private static LakeWitchModel girl(){var m=new LakeWitchModel(Minecraft.getInstance().getEntityModels().bakeLayer(LakeWitchModel.LAYER));m.young=false;return m;}
    private static LakeCongregantModel boyModel(int carry,float swing,int mood,float t){
        var m=new LakeCongregantModel(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.PLAYER));m.young=false;HumanoidMotion.reset(m);
        m.carrierPose(carry,swing);m.reaction(mood,t);HumanoidMotion.connect(m);return m;
    }
    private static PlayerModel<?> reader(float swing,float reach,boolean carrying){
        var m=new PlayerModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.PLAYER),false);m.young=false;
        if(carrying||reach>0)ShallowsClient.carryArms(m,swing,-1,reach,false);m.leftSleeve.copyFrom(m.leftArm);m.rightSleeve.copyFrom(m.rightArm);return m;
    }
    /** The girl's figure with her pose applied; positions are relative to her place on the bank, the lake toward -z. */
    private static Figure her(int phase,float swing,float t,double x,double y,double z,float yaw){
        var m=girl();
        switch(phase){
            case Shallows.LYING->m.heldPose(false,0,0,1);case Shallows.CARRIED->m.heldPose(true,swing,0,1);case Shallows.FLYING->m.tumblePose(t);
            case Shallows.BROKEN->m.brokenPose(0,1);case Shallows.RISING->m.risingPose(t,0,1);default->m.scurryPose(t,.8F,t,1);
        }
        return new Figure(m,LakeWitchRenderer.MEMORY,x,y,z,yaw,phase,swing,t);
    }
    private static List<Figure> scene(int page){
        List<Figure> f=new ArrayList<>();double hang=Shallows.HANG,r=Shallows.SWING_RADIUS;
        float swing=page==1?.9F:page==2?-.8F:0;
        if(page<=2){
            f.add(her(Shallows.CARRIED,swing,0,0,hang+r*(1-Math.cos(swing)),-r*Math.sin(swing),Shallows.READER_YAW));
            f.add(new Figure(reader(swing,0,true),STEVE,-1.5,0,0,Shallows.READER_YAW,-1,0,0));
            f.add(new Figure(boyModel(Shallows.SWINGING,swing,0,0),boy(1),1.5,0,0,Shallows.CARRIER_YAW,-1,0,0));
        }else if(page==3){
            f.add(her(Shallows.LYING,0,0,0,0,0,Shallows.READER_YAW));
            f.add(new Figure(reader(0,0,false),STEVE,-2.6,0,1.2,-120,-1,0,0));
            f.add(new Figure(boyModel(Shallows.HOLD,0,0,0),boy(1),1.5,0,0,Shallows.CARRIER_YAW,-1,0,0));
            f.add(new Figure(boyModel(0,0,Shallows.JEER,4),boy(0),-3.5,0,1.5,200,-1,0,0));
        }else if(page==4){
            f.add(her(Shallows.FLYING,0,5,0,1.9,-3.2,Shallows.READER_YAW));
            f.add(new Figure(reader(0,1,false),STEVE,-1.5,0,0,Shallows.READER_YAW,-1,0,0));
            f.add(new Figure(boyModel(Shallows.RELEASED,1,0,0),boy(1),1.5,0,0,Shallows.CARRIER_YAW,-1,0,0));
            f.add(new Figure(boyModel(0,0,Shallows.CHEER,6),boy(2),4,0,1,170,-1,0,0));
        }else{
            if(page==5)f.add(her(Shallows.BROKEN,0,0,.3,-.111,-5.5,205));
            if(page==6){f.add(her(Shallows.RISING,0,10,-1.4,-.111,-5.5,205));f.add(her(Shallows.RISING,0,34,1.6,-.111,-5.5,205));}
            if(page==7){f.add(her(Shallows.SCURRY,0,3,-1.2,-.111,-6.5,180));f.add(her(Shallows.SCURRY,0,9,1.5,-.111,-8,175));}
            int mood=page==5?Shallows.FROZEN:Shallows.RECOIL;
            f.add(new Figure(boyModel(0,0,mood,10),boy(0),-3.5,0,1.5,160,-1,0,0));
            f.add(new Figure(boyModel(0,0,mood,10),boy(2),4,0,.5,190,-1,0,0));
            f.add(new Figure(boyModel(0,0,mood,10),boy(3),-2,0,3,175,-1,0,0));
            f.add(new Figure(boyModel(0,0,Shallows.FROZEN,10),boy(1),1.5,0,0,170,-1,0,0));
            f.add(new Figure(reader(0,0,false),STEVE,-1.5,0,0,185,-1,0,0));
        }
        return f;
    }
    /** The same transform the entity renderers use, after a world placement. */
    private static void place(PoseStack p,Figure f){
        p.translate(f.x(),f.y(),f.z());p.mulPose(Axis.YP.rotationDegrees(180-f.yaw()));
        if(f.phase()>=0)LakeWitchRenderer.memory(p,f.phase(),f.swing(),f.t(),1);
        p.scale(-1,-1,1);p.translate(0,-1.501,0);
    }

    @Override protected void init(){
        problems.clear();
        // She lies along the carry, head toward the reader at -x, off the ground and level.
        var rest=bounds(scene(0).getFirst(),false);var head=bounds(scene(0).getFirst(),true);
        if(rest.x1-rest.x0<1.4F||rest.y1-rest.y0>1.05F||rest.y0<.25F)problems.add("carried body does not hang level between them: "+rest);
        if((head.x0+head.x1)/2>-.25F)problems.add("her head is not toward the reader's hands: head "+head);
        var out=bounds(scene(1).getFirst(),false);if((out.z0+out.z1)/2>-.3F)problems.add("a stroke out does not carry her toward the water: "+out);
        var back=bounds(scene(2).getFirst(),false);if((back.z0+back.z1)/2<.25F)problems.add("a stroke back does not carry her away from the water: "+back);
        for(int i=1;i<=2;i++){
            var arms=arms(scene(1).get(i));if((arms.z0+arms.z1)/2>-.05F)problems.add((i==1?"reader's":"carrier's")+" arms do not follow her toward the water: "+arms);
        }
        var lying=bounds(scene(3).getFirst(),false);var lyingHead=bounds(scene(3).getFirst(),true);
        if(lying.y1>1.1F||lying.y0<-.15F||lying.x1-lying.x0<1.3F)problems.add("on the bank she is not lying down with her feet up: "+lying);
        if((lyingHead.x0+lyingHead.x1)/2>-.25F)problems.add("on the bank her head is not toward the reader: "+lyingHead);
        // Face down, only the face may meet the surface from below; nothing else of her goes under.
        var lies=scene(5).getFirst();var broken=bounds(lies,false);
        if(broken.y1-broken.y0>.8F||broken.y0<lies.y()-.2F)problems.add("broken, she is not flat on the water: "+broken);
        var crawl=bounds(scene(7).getFirst(),false);if(crawl.y1-crawl.y0>1.25F)problems.add("crossing, she is not low on all fours: "+crawl);
    }
    private static Bounds bounds(Figure f,boolean headOnly){
        var p=new PoseStack();place(p,f);var b=new Bounds();
        // Her head's cubes are drawn by the jaw helper, not by the head part itself.
        if(headOnly&&f.model() instanceof LakeWitchModel m)LakeWitchJaw.upper(m.head,0,0,p,b,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,-1);
        else f.model().renderToBuffer(p,b,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,-1);
        return b;
    }
    private static Bounds arms(Figure f){
        var p=new PoseStack();place(p,f);var b=new Bounds();var m=(HumanoidModel<?>)f.model();
        m.leftArm.render(p,b,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,-1);m.rightArm.render(p,b,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,-1);
        // Arms are measured from the carrier's own place, so only their reach toward the water counts.
        b.z0-=(float)f.z();b.z1-=(float)f.z();return b;
    }
    private static final class Bounds implements VertexConsumer {
        float x0=Float.POSITIVE_INFINITY,y0=x0,z0=x0,x1=Float.NEGATIVE_INFINITY,y1=x1,z1=x1;
        public VertexConsumer addVertex(float x,float y,float z){x0=Math.min(x0,x);y0=Math.min(y0,y);z0=Math.min(z0,z);x1=Math.max(x1,x);y1=Math.max(y1,y);z1=Math.max(z1,z);return this;}
        public VertexConsumer setColor(int r,int g,int b,int a){return this;}public VertexConsumer setUv(float u,float v){return this;}public VertexConsumer setUv1(int u,int v){return this;}public VertexConsumer setUv2(int u,int v){return this;}public VertexConsumer setNormal(float x,float y,float z){return this;}
        @Override public String toString(){return String.format(Locale.ROOT,"x %.2f..%.2f y %.2f..%.2f z %.2f..%.2f",x0,x1,y0,y1,z0,z1);}
    }

    @Override public void renderBackground(GuiGraphics g,int x,int y,float partial){}
    @Override public void render(GuiGraphics g,int mouseX,int mouseY,float partial){
        g.fill(0,0,width,height,0xff6f8796);g.drawString(font,"The Shallows / "+VIEWS[page],10,9,0xfff2ece0,false);g.flush();
        var mc=Minecraft.getInstance();var out=mc.renderBuffers().bufferSource();var p=g.pose();
        boolean far=page>=5;float scale=Math.min(width,height)/(far?13F:7.5F);
        p.pushPose();p.translate(width*.5F,height*(far?.42F:.6F),400);p.scale(scale,-scale,scale);
        // From the lake side, a little east and above, looking back at the bank.
        p.mulPose(Axis.XP.rotationDegrees(far?32:20));p.mulPose(Axis.YP.rotationDegrees(180-32));
        if(far)p.translate(0,0,3);
        ground(p,out,far);
        for(var f:scene(page)){
            p.pushPose();place(p,f);
            f.model().renderToBuffer(p,out.getBuffer(RenderType.entityCutoutNoCull(f.skin())),LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,-1);
            p.popPose();
        }
        out.endBatch();p.popPose();
    }
    /** Sand on the bank and the lake's surface beyond it, at their real heights. */
    private static void ground(PoseStack p,MultiBufferSource out,boolean far){
        var blocks=Minecraft.getInstance().getBlockRenderer();BlockState sand=Blocks.SAND.defaultBlockState(),water=Blocks.LIGHT_BLUE_CONCRETE.defaultBlockState();
        for(int x=-4;x<=4;x++)for(int z=far?-10:-5;z<=2;z++){
            p.pushPose();p.translate(x-.5,-1,z-.5);boolean lake=z<=-2;if(lake)p.scale(1,.889F,1);
            blocks.renderSingleBlock(lake?water:sand,p,out,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);p.popPose();
        }
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post e)throws Exception{
        var mc=Minecraft.getInstance();if(!(mc.screen instanceof ShallowsVisualProof proof)||++proof.frames<8)return;
        var dir=Path.of("../build/font-smoke");Files.createDirectories(dir);
        try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(dir.resolve("native-shallows-"+proof.page+".png"));}
        if(++proof.page<VIEWS.length){proof.frames=0;return;}
        if(!proof.problems.isEmpty())throw new IllegalStateException("SHALLOWS VIEW CHECK FAILED: "+String.join("; ",proof.problems));
        Files.writeString(dir.resolve("shallows-passed.txt"),"Eight native Shallows views: the level carry with her head to the reader, strokes out over the water and back with both carriers' arms following, the bank, the throw, the flat break on the water, the rise and the low crossing.\n");
        TheOldestHouse.LOGGER.info("SHALLOWS VIEW CHECK PASSED: eight native views");
        mc.setScreen(new CastVisualProof());
    }
}

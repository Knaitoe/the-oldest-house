package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.nio.file.*;
import java.util.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;

/**
 * 0.4.173, "Third Floor Girls'": the dare's art in native renders. The kids in their sheets at each phase, the fittings and
 * the glass whole and shattered, the reader's own face at each count as the glass changes it, and the third count as the
 * glass shows it: the reflection's hands empty, the second flame and the woman behind it.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class DareArtProof extends Screen {
    private static final String[] NAMES={"kids","fittings","faces","third-count","borrowed-light","sphinx-face","sphinx-dream","sphinx-stela"};
    private static final Path FOLDER=Path.of("../build/dare-art");
    private static boolean launched;
    private int page,frames;
    private DareArtProof(){super(Component.literal("Third Floor Girls'"));}
    @SubscribeEvent public static void start(ClientTickEvent.Post ignored){
        var mc=Minecraft.getInstance();if(Boolean.getBoolean("the_oldest_house.dareSmoke")&&!launched&&mc.screen!=null&&mc.getOverlay()==null){launched=true;mc.options.guiScale().set(2);mc.resizeDisplay();mc.setScreen(new DareArtProof());}
    }
    @Override public void renderBackground(GuiGraphics g,int x,int y,float partial){}
    private static int[][] steve()throws Exception{
        try(var in=Minecraft.getInstance().getResourceManager().open(DefaultPlayerSkin.getDefaultTexture());var image=NativeImage.read(in)){
            int[] base=new int[64],hat=new int[64];for(int y=0;y<8;y++)for(int x=0;x<8;x++){base[y*8+x]=image.getPixelRGBA(8+x,8+y)|0xff000000;hat[y*8+x]=image.getPixelRGBA(40+x,8+y);}return new int[][]{base,hat};
        }
    }
    @Override public void render(GuiGraphics g,int mx,int my,float partial){
        var mc=Minecraft.getInstance();g.fill(0,0,width,height,0xff1e2124);var out=mc.renderBuffers().bufferSource();var p=g.pose();
        g.drawString(font,"Third Floor Girls' / "+NAMES[page],10,10,0xffe9e2cf,false);
        try{switch(page){
            case 0->{
                com.mojang.blaze3d.platform.Lighting.setupForEntityInInventory();
                var model=new DareRenderers.SheetModel(mc.getEntityModels().bakeLayer(DareRenderers.SHEET));
                for(int i=0;i<6;i++){int role=i<3?LiteraryActor.DARE_KID_A:LiteraryActor.DARE_KID_B,phase=i%3;
                    p.pushPose();p.translate((i%3+.5F)*width/3F,(i/3+.95F)*(height-40)/2F,150);p.scale(54,54,-54);p.mulPose(Axis.YP.rotationDegrees(i/3==0?18:-18));p.translate(0,-1.501,0);
                    model.pose(role,phase,12+i*7,0,0);model.renderToBuffer(p,out.getBuffer(RenderType.entityCutoutNoCull(DareRenderers.sheet(role))),LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,-1);out.endBatch();p.popPose();
                    g.drawCenteredString(font,new String[]{"waiting","giggling","silent"}[phase],(int)((i%3+.5F)*width/3F),(int)((i/3+.95F)*(height-40)/2F)+8,0xffe9e2cf);}
                p.pushPose();p.translate(width-40,40,150);p.scale(2,2,1);g.renderItem(new ItemStack(DareRegistry.CANDLE.get()),0,0);p.popPose();
            }
            case 1->{
                var kinds=new ArrayList<BlockState>();
                for(var k:DareFittingBlock.Kind.values())for(int stage=0;stage<DareFittingBlock.stages(k);stage++)kinds.add(DareFittingBlock.of(k,Direction.NORTH,stage));
                for(int part=0;part<8;part++)kinds.add(DareMirrorBlock.of(Direction.NORTH,part%4,part/4));
                for(int part=0;part<8;part++)kinds.add(DareMirrorBlock.of(Direction.NORTH,part%4,part/4).setValue(DareMirrorBlock.SHATTERED,true));
                kinds.add(DareRegistry.BATHROOM_DOOR.get().defaultBlockState());kinds.add(DareRegistry.STAIR_DOOR.get().defaultBlockState());
                for(String name:DareRegistry.MATERIAL_NAMES)kinds.add(DareRegistry.material(name).defaultBlockState());
                int columns=12;float cell=Math.min((width-20)/(float)columns,(height-40)/(float)((kinds.size()+columns-1)/columns));
                for(int i=0;i<kinds.size();i++){p.pushPose();p.translate(10+(i%columns+.5F)*cell,30+(i/columns+.62F)*cell,150);float s=cell*.44F;p.scale(s,-s,s);p.mulPose(Axis.XP.rotationDegrees(20));p.mulPose(Axis.YP.rotationDegrees(200));p.translate(-.5,-.5,-.5);
                    mc.getBlockRenderer().renderSingleBlock(kinds.get(i),p,out,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);p.popPose();}
                out.endBatch();
            }
            case 2->{
                var faces=steve();DareReflection.face(faces[0],faces[1]);var body=new PlayerModel<AbstractClientPlayer>(mc.getEntityModels().bakeLayer(ModelLayers.PLAYER),false);
                int[][] stages={{0,0},{1,200},{2,300},{2,600},{3,100},{3,300}};String[] labels={"before","first: nothing yet","second: an eye drifting","second: the mouth paling","third: both eyes, mouth gone","third: it comes back wrong"};
                for(int i=0;i<stages.length;i++){
                    for(var part:new net.minecraft.client.model.geom.ModelPart[]{body.head,body.hat})part.resetPose();
                    p.pushPose();p.translate((i%3+.5F)*width/3F,(i/3+.7F)*(height-40)/2F+20,150);p.scale(150,150,-150);p.translate(0,.25,0);
                    body.head.render(p,out.getBuffer(RenderType.entityCutoutNoCull(DefaultPlayerSkin.getDefaultTexture())),LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);
                    body.hat.render(p,out.getBuffer(RenderType.entityTranslucent(DefaultPlayerSkin.getDefaultTexture())),LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);
                    if(stages[i][0]>0){DareReflection.frame(stages[i][0],stages[i][1]);DareReflection.drawFace(body.head,p,out,LightTexture.FULL_BRIGHT);}
                    out.endBatch();p.popPose();
                    g.drawCenteredString(font,labels[i],(int)((i%3+.5F)*width/3F),(int)((i/3+.7F)*(height-40)/2F)+44,0xffe9e2cf);
                }
            }
            case 4->{
                // 0.4.199, the first puzzle room: every working part in each of its states, then the room's one solution in miniature.
                var kinds=new ArrayList<BlockState>();
                for(var k:BorrowedLightBlock.Kind.values())for(int open=0;open<2;open++)for(int turned=0;turned<(k==BorrowedLightBlock.Kind.MIRROR?2:1);turned++)
                    if(open==0||k!=BorrowedLightBlock.Kind.MIRROR&&k!=BorrowedLightBlock.Kind.LAMP&&k!=BorrowedLightBlock.Kind.SCONCE)
                        kinds.add(BorrowedLightBlock.of(k,Direction.NORTH).setValue(BorrowedLightBlock.OPEN,open==1).setValue(BorrowedLightBlock.TURNED,turned==1));
                for(String name:new String[]{"wall","floor","ceiling"})kinds.add(PuzzleRegistry.material(name).defaultBlockState());
                float cell=Math.min((width-20)/(float)kinds.size(),46);
                for(int i=0;i<kinds.size();i++){p.pushPose();p.translate(10+(i+.5F)*cell,40,150);float s=cell*.4F;p.scale(s,-s,s);p.mulPose(Axis.XP.rotationDegrees(20));p.mulPose(Axis.YP.rotationDegrees(200));p.translate(-.5,-.5,-.5);
                    mc.getBlockRenderer().renderSingleBlock(kinds.get(i),p,out,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);p.popPose();}
                out.endBatch();
                // The lamp on the east, its shutter open; the mirror turned on its track; the beam turning onto the lit sun by the far door.
                p.pushPose();p.translate(width*.5F,height*.68F,150);float s=Math.min(width,height)/14F;p.scale(s,-s,s);p.mulPose(Axis.XP.rotationDegrees(28));p.mulPose(Axis.YP.rotationDegrees(35));
                Object[][] room={{BorrowedLightBlock.of(BorrowedLightBlock.Kind.LAMP,Direction.WEST),3,1,0},{BorrowedLightBlock.of(BorrowedLightBlock.Kind.SHUTTER,Direction.WEST).setValue(BorrowedLightBlock.OPEN,true),2,1,0},
                    {BorrowedLightBlock.of(BorrowedLightBlock.Kind.MIRROR,Direction.NORTH).setValue(BorrowedLightBlock.TURNED,true),-1,0,0},{BorrowedLightBlock.of(BorrowedLightBlock.Kind.PLATE,Direction.SOUTH).setValue(BorrowedLightBlock.OPEN,true),-1,1,-4}};
                for(int x=-3;x<=2;x++){p.pushPose();p.translate(x,-1,0);mc.getBlockRenderer().renderSingleBlock(BorrowedLightBlock.of(BorrowedLightBlock.Kind.TRACK,Direction.EAST).setValue(BorrowedLightBlock.OPEN,x==-1),p,out,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);p.popPose();}
                for(int x=-3;x<=3;x++)for(int y=0;y<=2;y++){p.pushPose();p.translate(x,y,-5);mc.getBlockRenderer().renderSingleBlock(PuzzleRegistry.material("wall").defaultBlockState(),p,out,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);p.popPose();}
                for(var part:room){p.pushPose();p.translate((int)part[1],(int)part[2],(int)part[3]);mc.getBlockRenderer().renderSingleBlock((BlockState)part[0],p,out,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);p.popPose();}
                out.endBatch();
                BorrowedLightRenderer.draw(p,out,List.of(new Vec3(3.5,1.5,.5),new Vec3(-.5,1.5,.5),new Vec3(-.5,1.5,-3.5)),0);out.endBatch();p.popPose();
                g.drawString(font,"Borrowed Light: every part and state, then the beam laid on the sun",10,height-18,0xffe9e2cf,false);
            }
            case 5->{
                // 0.4.337, the Sphinx's whole face over the first crossing's hall: face on, looking up at her from the middle of the hall
                // (her eyes turned down toward the reader, mid-blink on the right), and half risen out of the dark.
                var face=Sphinx.faceAt(Sphinx.FACE_PX_W/2.0,Sphinx.FACE_PX_H/2.0);
                // 0.4.340: the last two as she is in the hall, her stone in its dim light and only her eyes lit. 0.4.341: speaking in the second and fourth.
                float[][] gazes={{0,-.2F,0,-.2F},{-.5F,-.3F,-.4F,-.3F},{0,0,0,0},{.3F,-.3F,.3F,-.3F}};float[] closed={0,.6F,0,0};float[] risen={1,1,.5F,1};float[] tilt={0,50,0,40};int[] jaw={0,2,0,3};
                int dim=LightTexture.pack(SphinxFaceRenderer.STONE_LIGHT,0),eyes=LightTexture.pack(SphinxFaceRenderer.EYE_LIGHT,0);
                for(int view=0;view<4;view++){
                    float cx=(view+.5F)*width/4F,cy=height*.55F,s=width/4F/21F;boolean dark=view>=2;
                    g.fill((int)(cx-width/8F)+4,30,(int)(cx+width/8F)-4,height-26,0xff0c0a08);
                    p.pushPose();p.translate(cx,cy,150);p.scale(s,-s,s);p.mulPose(Axis.XP.rotationDegrees(tilt[view]));
                    SphinxFaceRenderer.draw(p,out,face.scale(-1),risen[view],gazes[view],closed[view],jaw[view],dark?dim:LightTexture.FULL_BRIGHT,dark?eyes:LightTexture.FULL_BRIGHT);
                    out.endBatch();p.popPose();
                    g.drawCenteredString(font,new String[]{"face on","from the hall below, speaking","rising out of the dark","in the hall's dark, speaking"}[view],(int)cx,height-20,0xffe9e2cf);
                }
            }
            case 6->{
                // 0.4.342, her whole body in the dream as the reader's own screen shows it (owner: "It should be so large as to occupy
                // most of the player's screen"): seventy-degree views from the reader's eye in the staged dream site, the house, yard and
                // fields drawn plainly around her. In the yard under her; from the gate; from the gate as she comes over it; and
                // from the doorstep, holding over the far field.
                var h=SphinxDream.house(null);float perch=h.ridgeY()+1;var paws=new Vec3((h.x0()+h.x1())/2.0+.5,perch,h.ridgeZ()+.5);
                int dismissive=SphinxCore.Conduct.DISMISSIVE.ordinal();
                // Over the yard twenty ticks after she rises, coming over the gate; and holding over the far field. In flight her pitch is
                // her heading's, held within thirty degrees as her entity holds it.
                var over=paws.add(SphinxDream.flight(dismissive,20));var far=paws.add(SphinxDream.flight(dismissive,150));
                var u=SphinxDream.flight(dismissive,21).subtract(SphinxDream.flight(dismissive,19));
                float overPitch=net.minecraft.util.Mth.clamp((float)-Math.toDegrees(Math.atan2(u.y,Math.hypot(u.x,u.z))),-30,30);
                var v=SphinxDream.flight(dismissive,151).subtract(SphinxDream.flight(dismissive,149));
                float flyYaw=(float)Math.toDegrees(Math.atan2(-v.x,v.z)),flyPitch=(float)-Math.toDegrees(Math.atan2(v.y,Math.hypot(v.x,v.z)));
                Object[][] views={
                    {new Vec3(.5,1.62,16),paws,0F,"in the yard, under her"},
                    {new Vec3(.5,1.62,23.5),paws,0F,"from the gate"},
                    {new Vec3(.5,1.62,23.5),over,1F,"coming over the gate: her face, her eyes ahead"},
                    {new Vec3(.5,1.62,2.6),far,1F,"over the far field, from the doorstep"}};
                int pw=(width-30)/2,ph=pw*9/16;if(ph>(height-44)/2){ph=(height-44)/2;pw=ph*16/9;}
                for(int view=0;view<4;view++){
                    int x=view%2==0?width/2-pw-5:width/2+5,y=24+(view/2)*(ph+8);var eye=(Vec3)views[view][0];var at=(Vec3)views[view][1];float fly=(float)views[view][2];
                    // Looking up at her face from the doorstep; from the gate, at the house and her over it; at her body when she flies.
                    // Coming over the gate, at her face, tipped down to the reader under her, her eyes on where she is going (her face's
                    // centre is 32 blocks over her forepaws and 4.5 ahead of them as she climbs).
                    var look=(fly>0?(view==2?at.add(0,32.1,4.5):at):view==0?at.add(0,35.8,13.9):new Vec3(.5,24,0)).subtract(eye);
                    float yaw=(float)Math.toDegrees(Math.atan2(-look.x,look.z)),pitch=(float)-Math.toDegrees(Math.atan2(look.y,Math.hypot(look.x,look.z)));
                    float herYaw=fly>0?(view==2?0:flyYaw):0,herPitch=fly>0?(view==2?overPitch:flyPitch):0;
                    g.fill(x,y,x+pw,y+ph,0xff9fc0dc);
                    perspective(g,x,y,pw,ph,eye,yaw,pitch,pose->{
                        dreamSite(pose,out,h);
                        pose.pushPose();pose.translate(at.x,at.y,at.z);
                        SphinxDreamRenderer.drawAt(pose,out,LightTexture.FULL_BRIGHT,36,fly,perch,herYaw,herPitch);
                        pose.popPose();
                    });
                    g.drawString(font,(String)views[view][3],x+4,y+ph-11,0xffffffff,true);
                }
            }
            case 7->{
                // Her whole body as a model: on the roof from the yard, from under her open wings, and from the side over the far field;
                // and the stela's pieces, each carving whole and chipped.
                com.mojang.blaze3d.platform.Lighting.setupForEntityInInventory();
                // {x0, x1, y0, y1} of each panel as parts of the page, then its turn, size, flight and ridge.
                float[][] panels={{0,.5F,0,.5F},{.5F,1,0,.5F},{0,.5F,.5F,1},{.5F,1,.5F,1}};
                float[][] views={{-14,-38,.95F,0,8},{-4,-90,.95F,0,8},{4,-90,.8F,1,8},{-60,0,.42F,1,8}};
                String[] labels={"on the roof, from the yard","on the roof, from the side","flying, from the side: neck forward, toes down","from under her open wings"};
                for(int view=0;view<4;view++){
                    var r=panels[view];int x0=(int)(4+r[0]*(width-8)),x1=(int)(4+r[1]*(width-8)),y0=(int)(24+r[2]*(height-76)),y1=(int)(24+r[3]*(height-76));var v=views[view];
                    g.fill(x0+2,y0+2,x1-2,y1-2,0xff8aa0b4);
                    g.enableScissor(x0+2,y0+2,x1-2,y1-2);
                    p.pushPose();p.translate((x0+x1)/2F+(view==1?24:view==2?30:0),(y0+y1)/2F+(view<2?30:view==2?8:0),150);p.scale(v[2],-v[2],v[2]);p.mulPose(Axis.XP.rotationDegrees(v[0]));p.mulPose(Axis.YP.rotationDegrees(v[1]));
                    SphinxDreamRenderer.draw(p,out,LightTexture.FULL_BRIGHT,40+view*9,v[3],v[4],false);out.endBatch();p.popPose();
                    g.disableScissor();
                    g.drawString(font,labels[view],x0+6,y1-13,0xffe9e2cf,true);
                }
                com.mojang.blaze3d.platform.Lighting.setupFor3DItems();
                var owner=java.util.UUID.nameUUIDFromBytes("dream".getBytes());int i=0;
                for(var carving:DreamStela.CARVINGS)for(boolean chipped:new boolean[]{false,true}){
                    var stone=DreamStela.make(owner,DreamStela.MEDIUM,"patient",carving,chipped);
                    int x=(int)(width/2F-5*22+i*22+3);p.pushPose();p.translate(x,height-34,0);p.scale(1.25F,1.25F,1);g.renderItem(stone,0,0);p.popPose();i++;
                }
            }
            default->{
                // As the glass shows it: the reflection facing the reader, the woman over its left shoulder (the image's right).
                var body=new PlayerModel<AbstractClientPlayer>(mc.getEntityModels().bakeLayer(ModelLayers.PLAYER),false);body.young=false;
                for(int view=0;view<2;view++){
                    p.pushPose();p.translate(width*(view==0?.3F:.72F),height*.84F,150);float s=view==0?118:96;p.scale(-s,s,-s);p.mulPose(Axis.ZP.rotationDegrees(180));
                    if(view==1)p.mulPose(Axis.YP.rotationDegrees(-55));
                    // The reader's reflection: arms at its sides, no candle.
                    p.pushPose();p.mulPose(Axis.YP.rotationDegrees(180-180));p.scale(-1,-1,1);p.scale(.9375F,.9375F,.9375F);p.translate(0,-1.501,0);
                    body.renderToBuffer(p,out.getBuffer(RenderType.entityCutoutNoCull(DefaultPlayerSkin.getDefaultTexture())),LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,-1);
                    if(view==0){var faces=steve();DareReflection.face(faces[0],faces[1]);DareReflection.frame(3,40);DareReflection.drawFace(body.head,p,out,LightTexture.FULL_BRIGHT);}
                    p.popPose();
                    DareReflection.draw(p,out,Vec3.ZERO,1.62F,180,new Vec3(0,1.62,-3),1,30,LightTexture.FULL_BRIGHT);
                    out.endBatch();p.popPose();
                }
                g.drawString(font,"Third count, as the glass shows it / front, and turned to show her at its back",10,height-18,0xffe9e2cf,false);
            }
        }}catch(Exception e){throw new IllegalStateException(e);}
    }
    /**
     * 0.4.342: a view as the reader's own screen shows it, into a panel of the proof: a seventy-degree perspective from {@code eye},
     * turned to {@code yaw} and {@code pitch} as a player's camera is, with the hazy fog of a ten-chunk view. {@code scene} draws in world
     * coordinates.
     */
    static void perspective(GuiGraphics g,int x,int y,int w,int h,Vec3 eye,float yaw,float pitch,java.util.function.Consumer<PoseStack> scene){
        var mc=Minecraft.getInstance();var win=mc.getWindow();double gs=win.getGuiScale();var out=mc.renderBuffers().bufferSource();
        g.flush();
        RenderSystem.backupProjectionMatrix();
        var mv=RenderSystem.getModelViewStack();mv.pushMatrix();mv.identity();RenderSystem.applyModelViewMatrix();
        RenderSystem.viewport((int)(x*gs),(int)(win.getHeight()-(y+h)*gs),(int)(w*gs),(int)(h*gs));
        RenderSystem.clear(org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT,Minecraft.ON_OSX);
        RenderSystem.setProjectionMatrix(new org.joml.Matrix4f().perspective((float)Math.toRadians(70),(float)w/h,.1F,1000F),com.mojang.blaze3d.vertex.VertexSorting.DISTANCE_TO_ORIGIN);
        RenderSystem.enableDepthTest();RenderSystem.depthMask(true);
        RenderSystem.setShaderFogStart(144);RenderSystem.setShaderFogEnd(160);RenderSystem.setShaderFogColor(.62F,.75F,.86F);RenderSystem.setShaderFogShape(com.mojang.blaze3d.shaders.FogShape.CYLINDER);
        // As the world is drawn: the camera's turn in the model-view matrix and the level's light, the pose only moved to the eye.
        mv.rotateX((float)Math.toRadians(pitch));mv.rotateY((float)Math.toRadians(yaw+180));RenderSystem.applyModelViewMatrix();
        com.mojang.blaze3d.platform.Lighting.setupLevel();
        var pose=new PoseStack();pose.translate(-eye.x,-eye.y,-eye.z);
        scene.accept(pose);
        out.endBatch();
        FogRenderer.setupNoFog();
        RenderSystem.viewport(0,0,win.getWidth(),win.getHeight());
        mv.popMatrix();RenderSystem.applyModelViewMatrix();RenderSystem.restoreProjectionMatrix();
        RenderSystem.clear(org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT,Minecraft.ON_OSX);
    }
    /** The staged dream site, plainly: wheat on every side, the fenced yard and its path, the little house with its gable roof. */
    static void dreamSite(PoseStack pose,MultiBufferSource out,SphinxDream.House h){
        var mc=Minecraft.getInstance();var q=out.getBuffer(RenderType.debugQuads());
        int yn=h.z0()-8,ys=22,fx=104,fn=yn-64,fs=158;
        flat(pose,q,-fx,fn,fx,yn-1,0,0xffc7ad58);flat(pose,q,-fx,ys+2,fx,fs,0,0xffc7ad58);flat(pose,q,-fx,yn-1,-23,ys+2,0,0xffc7ad58);flat(pose,q,24,yn-1,fx,ys+2,0,0xffc7ad58);
        flat(pose,q,-23,yn-1,24,ys+2,0,0xff6a9a3f);flat(pose,q,0,1,2,ys+2,.04F,0xff9c8058);
        for(int k=0;k<4;k++){float x0=k==1?fx:-fx,x1=k==0?-fx:fx,z0=k==2?fs:fn,z1=k==3?fn:fs;if(k<2){x1=x0;}else{z1=z0;}
            var m=pose.last().pose();int c=0xff3d5a2a;
            q.addVertex(m,x0,0,z0).setColor(c);q.addVertex(m,x0,9,z0).setColor(c);q.addVertex(m,x1,9,z1).setColor(c);q.addVertex(m,x1,0,z1).setColor(c);}
        var block=mc.getBlockRenderer();
        java.util.function.BiConsumer<net.minecraft.core.BlockPos,BlockState> put=(at,state)->{pose.pushPose();pose.translate(at.getX(),at.getY(),at.getZ());block.renderSingleBlock(state,pose,out,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);pose.popPose();};
        var wall=net.minecraft.world.level.block.Blocks.WHITE_TERRACOTTA.defaultBlockState();var plank=net.minecraft.world.level.block.Blocks.DARK_OAK_PLANKS.defaultBlockState();
        for(int x=h.x0();x<=h.x1();x++)for(int z=h.z0();z<=h.z1();z++)for(int y=0;y<=h.top();y++){
            boolean edge=x==h.x0()||x==h.x1()||z==h.z0()||z==h.z1();if(y==h.top()||edge&&!(x==0&&z==0&&y<2))put.accept(new net.minecraft.core.BlockPos(x,y,z),y==h.top()?net.minecraft.world.level.block.Blocks.SPRUCE_PLANKS.defaultBlockState():wall);}
        var door=net.minecraft.world.level.block.Blocks.SPRUCE_DOOR.defaultBlockState().setValue(net.minecraft.world.level.block.DoorBlock.FACING,Direction.NORTH);
        put.accept(net.minecraft.core.BlockPos.ZERO,door);put.accept(new net.minecraft.core.BlockPos(0,1,0),door.setValue(net.minecraft.world.level.block.DoorBlock.HALF,net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER));
        for(int z=h.z0()-1;z<=h.z1()+1;z++){int d=Math.min(z-(h.z0()-1),(h.z1()+1)-z);int y=h.top()+d;
            for(int x=h.x0()-1;x<=h.x1()+1;x++){
                put.accept(new net.minecraft.core.BlockPos(x,y,z),z==h.ridgeZ()?plank:net.minecraft.world.level.block.Blocks.DARK_OAK_STAIRS.defaultBlockState().setValue(net.minecraft.world.level.block.StairBlock.FACING,z<h.ridgeZ()?Direction.SOUTH:Direction.NORTH));
                if(x==h.x0()||x==h.x1())for(int yy=h.top()+1;yy<y;yy++)put.accept(new net.minecraft.core.BlockPos(x,yy,z),plank);}}
        var fence=net.minecraft.world.level.block.Blocks.SPRUCE_FENCE.defaultBlockState().setValue(net.minecraft.world.level.block.FenceBlock.NORTH,true).setValue(net.minecraft.world.level.block.FenceBlock.SOUTH,true);
        var rail=net.minecraft.world.level.block.Blocks.SPRUCE_FENCE.defaultBlockState().setValue(net.minecraft.world.level.block.FenceBlock.EAST,true).setValue(net.minecraft.world.level.block.FenceBlock.WEST,true);
        for(int x=-23;x<=23;x++){if(x!=0)put.accept(new net.minecraft.core.BlockPos(x,0,ys+1),rail);put.accept(new net.minecraft.core.BlockPos(x,0,yn-1),rail);}
        for(int z=yn;z<=ys;z++){put.accept(new net.minecraft.core.BlockPos(-23,0,z),fence);put.accept(new net.minecraft.core.BlockPos(23,0,z),fence);}
    }
    static void flat(PoseStack pose,com.mojang.blaze3d.vertex.VertexConsumer q,float x0,float z0,float x1,float z1,float y,int c){
        var m=pose.last().pose();q.addVertex(m,x0,y,z0).setColor(c);q.addVertex(m,x0,y,z1).setColor(c);q.addVertex(m,x1,y,z1).setColor(c);q.addVertex(m,x1,y,z0).setColor(c);
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post ignored)throws Exception{
        var mc=Minecraft.getInstance();if(!(mc.screen instanceof DareArtProof proof)||++proof.frames<20)return;Files.createDirectories(FOLDER);
        try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){
            image.writeToFile(FOLDER.resolve("native-dare-"+NAMES[proof.page]+".png"));
            var colors=new HashSet<Integer>();for(int y=image.getHeight()/6;y<image.getHeight()*5/6;y+=3)for(int x=image.getWidth()/6;x<image.getWidth()*5/6;x+=3)colors.add(image.getPixelRGBA(x,y));
            if(colors.size()<12)throw new IllegalStateException("Blank or untextured dare view "+NAMES[proof.page]+": "+colors.size());
        }
        if(++proof.page<NAMES.length){proof.frames=0;return;}
        Files.writeString(FOLDER.resolve("passed.txt"),"Four native views of the third floor: the sheeted kids waiting, giggling and silent; every fitting state, the glass whole and shattered, both doors and the seven materials; the reader's face at each count as the glass changes it; and the third count as the glass shows it. And one of the first puzzle room, Borrowed Light: every working part in each state, its three materials, and the beam turned by the mirror onto the lit sun. And the Sphinx's whole face: face on, from the hall below as she speaks, rising out of the dark, and speaking in the hall's dark. And her whole body in the dream as the reader's own screen shows it: stepping out of the door under her, from the gate, under her wing over the yard, and holding over the far field; and as a model, with the stela's ten pieces.\n");
        mc.stop();
    }
}

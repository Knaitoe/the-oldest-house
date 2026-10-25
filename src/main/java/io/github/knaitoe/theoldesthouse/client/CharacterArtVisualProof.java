package io.github.knaitoe.theoldesthouse.client;

import com.google.gson.*;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.nio.file.*;
import java.util.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.model.*;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;

/** Entire human cast on its production meshes, plus full-size native Harrigan/casket views. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class CharacterArtVisualProof extends Screen {
    private static boolean launched;
    private final List<JsonObject> cast=new ArrayList<>();
    private int page,frames;
    public CharacterArtVisualProof(){super(Component.literal("Character paint and native structure"));}
    private static ResourceLocation texture(String name){return ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/"+name+".png");}
    @Override protected void init(){
        cast.clear();
        var mc=Minecraft.getInstance();
        try(var reader=mc.getResourceManager().openAsReader(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"character_art.json"))){
            for(var row:JsonParser.parseReader(reader).getAsJsonArray())cast.add(row.getAsJsonObject());
        }catch(java.io.IOException e){throw new java.io.UncheckedIOException(e);}
        if(cast.size()<100)throw new IllegalStateException("Incomplete character art review: "+cast.size());
        for(var row:cast){String name=row.get("name").getAsString();
            try(var stream=mc.getResourceManager().open(texture(name));var image=NativeImage.read(stream)){
                int logical=row.get("logical").getAsInt();if(image.getWidth()!=logical*4||image.getHeight()!=256)throw new IllegalStateException("Character UV dimensions do not match its native layer: "+name);
            }catch(java.io.IOException e){throw new java.io.UncheckedIOException(e);}
        }
        verifyHarrigan();
    }
    private static void verifyHarrigan(){
        var mc=Minecraft.getInstance();var model=new HouseHumanRenderers.HarriganModel(mc.getEntityModels().bakeLayer(HouseHumanRenderers.HARRIGAN_LAYER));
        model.preview(3);model.hat.visible=model.jacket.visible=model.leftSleeve.visible=model.rightSleeve.visible=model.leftPants.visible=model.rightPants.visible=false;
        var p=new PoseStack();p.translate(.5,.9,.5);HouseHumanRenderers.casketPose(p);p.scale(-1,-1,1);p.translate(0,-1.501,0);
        var bounds=new Bounds();model.renderToBuffer(p,bounds,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,-1);
        if(bounds.x0<-.04F||bounds.x1>1.04F||bounds.z0<-.7F||bounds.z1>1.7F||bounds.y0<.54F)throw new IllegalStateException("Harrigan clips the native casket: "+bounds);
        var culling=HouseHumanRenderers.ActorRenderer.casketBounds(new net.minecraft.world.phys.AABB(.2,.9,.2,.8,1.4,.8));
        if(!culling.contains(bounds.x0,bounds.y0,bounds.z0)||!culling.contains(bounds.x1,bounds.y1,bounds.z1))throw new IllegalStateException("The native casket mesh exceeds its actual culling bounds: "+bounds);
        if(model.leftElbow.zRot<1||model.rightElbow.zRot> -1)throw new IllegalStateException("Casket hands have not folded across the torso");
        model.preview(1);if(model.head.z!=0||model.leftElbow.zRot!=0||Math.abs(model.leftKnee.xRot-(float)Math.PI/2)>.001F)throw new IllegalStateException("Casket pose leaked into the seated original");
        // 0.4.105: mourners stand with their hands clasped, so their elbows bend; the seated pose is knees up and elbows at +0.5.
        model.preview(4);if(model.leftKnee.xRot!=0||model.leftLeg.xRot!=0||Math.abs(model.rightElbow.xRot-.5F)<.001F)throw new IllegalStateException("Seated pose leaked into a standing mourner");
        // The caps are painted on the limb's own planes, so they are probed with the limbs at rest, not in a mourner's clasp.
        model.rightArm.getAllParts().forEach(net.minecraft.client.model.geom.ModelPart::resetPose);model.rightLeg.getAllParts().forEach(net.minecraft.client.model.geom.ModelPart::resetPose);
        jointCaps(model.rightArm,6/16F,0,8);jointCaps(model.rightLeg,18/16F,24,32);
    }
    private static void jointCaps(net.minecraft.client.model.geom.ModelPart part,float plane,int u0,int u1){
        var probe=new CapUvs(plane,u0/64F,u1/64F);part.render(new PoseStack(),probe,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,-1);
        if(probe.faces<2)throw new IllegalStateException("Native limb joint has an unpainted/open cap");
    }
    private static final class CapUvs implements VertexConsumer {
        final float plane,u0,u1;final float[][] points=new float[4][3];int at,faces;
        CapUvs(float plane,float u0,float u1){this.plane=plane;this.u0=u0;this.u1=u1;}
        public VertexConsumer addVertex(float x,float y,float z){points[at][0]=y;return this;}
        public VertexConsumer setUv(float u,float v){points[at][1]=u;points[at][2]=v;return this;}
        public VertexConsumer setNormal(float x,float y,float z){if(++at==4){at=0;if(java.util.Arrays.stream(points).allMatch(p->Math.abs(p[0]-plane)<.0001F)){faces++;for(var p:points)if(p[1]<u0-.00001F||p[1]>u1+.00001F||p[2]<0||p[2]>8/64F)throw new IllegalStateException("Native elbow/knee cap samples a hand or sole: "+java.util.Arrays.toString(p));}}return this;}
        public VertexConsumer setColor(int r,int g,int b,int a){return this;}public VertexConsumer setUv1(int u,int v){return this;}public VertexConsumer setUv2(int u,int v){return this;}
    }
    private static final class Bounds implements VertexConsumer {
        float x0=Float.POSITIVE_INFINITY,y0=x0,z0=x0,x1=Float.NEGATIVE_INFINITY,y1=x1,z1=x1;
        public VertexConsumer addVertex(float x,float y,float z){x0=Math.min(x0,x);y0=Math.min(y0,y);z0=Math.min(z0,z);x1=Math.max(x1,x);y1=Math.max(y1,y);z1=Math.max(z1,z);return this;}
        public VertexConsumer setColor(int r,int g,int b,int a){return this;}public VertexConsumer setUv(float u,float v){return this;}public VertexConsumer setUv1(int u,int v){return this;}public VertexConsumer setUv2(int u,int v){return this;}public VertexConsumer setNormal(float x,float y,float z){return this;}
        @Override public String toString(){return x0+","+y0+","+z0+" -> "+x1+","+y1+","+z1;}
    }
    private static int role(JsonObject row){try{return LiteraryActor.class.getField(row.get("role").getAsString()).getInt(null);}catch(ReflectiveOperationException e){throw new IllegalStateException("Unknown native cast role "+row,e);}}
    static EntityModel<?> model(JsonObject row){
        var mc=Minecraft.getInstance();int phase=row.get("phase").getAsInt();int number=row.get("number").getAsInt();
        return switch(row.get("model").getAsString()){
            case "borrasca"->{var m=new BorrascaNativeArt.CastModel(mc.getEntityModels().bakeLayer(BorrascaNativeArt.CAST));m.preview(number,0,0);yield m;}
            case "uncanny"->{var m=new UncannyActorRenderer.Model(mc.getEntityModels().bakeLayer(ModelLayers.PLAYER),false);m.pose(number,phase,false,0,0,0,0,0);yield m;}
            case "harrigan"->{var m=new HouseHumanRenderers.HarriganModel(mc.getEntityModels().bakeLayer(HouseHumanRenderers.HARRIGAN_LAYER));m.preview(number);yield m;}
            case "literary"->{if(PlainFigureModel.matches(role(row),phase))yield new PlainFigureModel(mc.getEntityModels().bakeLayer(PlainFigureModel.LAYER));var m=new LiteraryRenderers.CastModel(mc.getEntityModels().bakeLayer(LiteraryRenderers.CAST));m.preview(role(row),phase,0);yield m;}
            case "corpse"->{var m=new LiteraryRenderers.CastModel(mc.getEntityModels().bakeLayer(LiteraryRenderers.CAST));m.young=false;m.corpse(row.get("name").getAsString().contains("crew_"),number,0);yield m;}
            case "ghosts"->{var m=new GhostsCastModel(mc.getEntityModels().bakeLayer(GhostsCastModel.LAYER));m.preview(role(row),phase,0);yield m;}
            case "club"->{var m=new NightclubRenderers.CastModel(mc.getEntityModels().bakeLayer(NightclubRenderers.CAST));m.preview(number==8?Nightclub.DANCERS:number,0,phase);yield m;}
            case "hotel"->{var m=new HotelRenderers.CastModel(mc.getEntityModels().bakeLayer(HotelRenderers.CAST));m.preview(number,false,0,HotelActor.IDLE,0);yield m;}
            case "seance"->{var m=new SeanceRenderers.FamilyModel(mc.getEntityModels().bakeLayer(SeanceRenderers.LAYER));m.young=false;m.pose(number,false,false,0);yield m;}
            case "witch"->{var m=new LakeWitchModel(mc.getEntityModels().bakeLayer(LakeWitchModel.LAYER));m.young=false;if(phase==0)m.memoryPose(0,0,false,0,0);else m.huntPose(0,0,0,LakeWitchEntity.STALK,false);yield m;}
            case "cousin"->{var m=new GoatmanChildModel(mc.getEntityModels().bakeLayer(GoatmanChildModel.LAYER));m.pose(number,0,0,0,false,false,false);yield m;}
            default->{var m=new PlayerModel<>(mc.getEntityModels().bakeLayer(ModelLayers.PLAYER),false);m.young=false;yield m;}
        };
    }
    @Override public void renderBackground(GuiGraphics g,int x,int y,float partial){}
    @Override public void render(GuiGraphics g,int x,int y,float partial){
        g.fill(0,0,width,height,0xff2b3033);g.drawString(font,"Native character review / "+(page<4?"Harrigan / "+new String[]{"coffin, overhead","coffin, side","face and folded hands","seated original"}[page]:page==4?"funeral plaque and three mourners":"front and back / sheet "+(page-4)),10,9,0xffe9dfcd,false);g.flush();
        var mc=Minecraft.getInstance();var out=mc.renderBuffers().bufferSource();var p=g.pose();
        if(page>=5+(cast.size()+7)/8){creatures(g,page-(5+(cast.size()+7)/8));return;}
        if(page==4){funeral(g);return;}
        if(page<4){
            var m=new HouseHumanRenderers.HarriganModel(mc.getEntityModels().bakeLayer(HouseHumanRenderers.HARRIGAN_LAYER));m.preview(page==3?1:3);
            p.pushPose();p.translate(width*.5F,height*.53F,190);float scale=Math.min(width/3.8F,height/2.6F);float zoom=page==2?1.6F:1;p.scale(scale*zoom,-scale*zoom,scale*zoom);
            if(page<3){p.mulPose(Axis.XP.rotationDegrees(page==0?66:page==1?16:48));p.mulPose(Axis.YP.rotationDegrees(page==1?-70:-25));p.translate(-.5,page==2?-.75:-.6,page==2?-.1:-.5);
                mc.getBlockRenderer().renderSingleBlock(HarriganFinishBlock.of(HarriganFinishBlock.Kind.CASKET),p,out,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);
                p.translate(.5,.9,.5);HouseHumanRenderers.casketPose(p);p.scale(-1,-1,1);p.translate(0,-1.501,0);
            }else{p.scale(1,-1,1);p.mulPose(Axis.YP.rotationDegrees(155));p.translate(0,-.45,0);}
            m.renderToBuffer(p,out.getBuffer(RenderType.entityCutoutNoCull(page==3?HouseHumanRenderers.HARRIGAN:HouseHumanRenderers.HARRIGAN_DEAD)),LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,-1);out.endBatch();p.popPose();return;
        }
        float cw=(width-16F)/4,ch=(height-35F)/2;
        int first=(page-5)*8;
        for(int i=0;i<8&&first+i<cast.size();i++){
            var row=cast.get(first+i);String name=row.get("name").getAsString();float cx=8+(i%4+.5F)*cw,top=28+(i/4)*ch;
            float scale=Math.min(cw*.32F,ch*.42F);var label=font.split(Component.literal(name.replace("literary_","").replace("nightclub_","club_")),(int)cw-8);for(int line=0;line<Math.min(2,label.size());line++)g.drawString(font,label.get(line),(int)cx-font.width(label.get(line))/2,(int)(top+ch-20)+line*9,0xffe0dacb,false);g.flush();
            for(int view=0;view<2;view++){
                draw(g,model(row),texture(name),cx+(view==0?-.23F:.23F)*cw,top+ch*.47F,cw*.44F,ch*.85F,view==0?165:20,0);
            }
        }
    }
    private void funeral(GuiGraphics g){
        var mc=Minecraft.getInstance();var p=g.pose();var out=mc.renderBuffers().bufferSource();float column=width/4F;
        String[] labels={"Peabody & Sons","Clasped hands","Hand to face","Hand over heart"};
        for(int i=0;i<4;i++){float x=(i+.5F)*column;g.drawCenteredString(font,labels[i],(int)x,height-26,0xffe0dacb);g.flush();
            if(i==0){p.pushPose();p.translate(x,height*.77F,180);float scale=Math.min(column*.72F,height*.34F);p.scale(scale,-scale,scale);p.mulPose(Axis.YP.rotationDegrees(-12));p.translate(-.5,0,-.5);
                mc.getBlockRenderer().renderSingleBlock(HarriganFinishBlock.of(HarriganFinishBlock.Kind.PLAQUE),p,out,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);out.endBatch();p.popPose();}
            else{var model=new HouseHumanRenderers.HarriganModel(mc.getEntityModels().bakeLayer(HouseHumanRenderers.HARRIGAN_LAYER));model.preview(i+3);draw(g,model,HouseHumanRenderers.mourner(i-1),x,height*.47F,column*.86F,height*.74F,165,0);}
        }
    }
    static void draw(GuiGraphics g,EntityModel<?> model,ResourceLocation skin,float cx,float cy,float width,float height,float yaw,float pitch){
        var p=g.pose();var out=Minecraft.getInstance().renderBuffers().bufferSource();var sample=new PoseStack();sample.mulPose(Axis.YP.rotationDegrees(yaw));sample.mulPose(Axis.XP.rotationDegrees(pitch));
        var bounds=new Bounds();model.renderToBuffer(sample,bounds,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,-1);
        float scale=Math.min(width/Math.max(.01F,bounds.x1-bounds.x0),height/Math.max(.01F,bounds.y1-bounds.y0));
        p.pushPose();p.translate(cx,cy,180);p.scale(scale,scale,scale);p.translate(-(bounds.x0+bounds.x1)/2,-(bounds.y0+bounds.y1)/2,0);p.mulPose(Axis.YP.rotationDegrees(yaw));p.mulPose(Axis.XP.rotationDegrees(pitch));
        model.renderToBuffer(p,out.getBuffer(RenderType.entityCutoutNoCull(skin)),LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,-1);
        if(skin.getPath().endsWith("literary_snow_rabbit.png"))HolyRabbitRenderer.markings(Minecraft.getInstance().getEntityModels().bakeLayer(HolyRabbitRenderer.MARKS),false,0,0,p,out,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);
        out.endBatch();p.popPose();
    }
    private void creatures(GuiGraphics g,int sheet){
        var mc=Minecraft.getInstance();float cw=(width-16F)/4,ch=(height-35F)/2;
        String[] names={"Mother / 0","Mother / 7","Mother / 15","Pekingese","Clap girl","Goatman","Silent heave","Minotaur","Wounded","Released","Vulture / glide","Vulture / flap","Elk","Wounded elk","Skinned remains","Slaney"};
        for(int i=0;i<8;i++){
            int n=sheet*8+i;EntityModel<?> model;ResourceLocation skin;
            if(n<3){int stage=n==0?0:n==1?7:15;var m=new MotherModel(mc.getEntityModels().bakeLayer(MotherModel.LAYER));m.pose(stage/15F,false,0,0,0,0,0);model=m;skin=texture("mother_of_strays"+(stage==0?"":"_"+stage));}
            else if(n==3){model=new MotherPekingeseModel(mc.getEntityModels().bakeLayer(MotherPekingeseModel.LAYER));skin=texture("mother_pekingese");}
            else if(n==4){var m=new ClapGhostModel(mc.getEntityModels().bakeLayer(ClapGhostModel.LAYER));m.pose(0,0);model=m;skin=texture("clap_ghost_girl");}
            else if(n==5||n==6){var m=new GoatmanFigureModel<>(mc.getEntityModels().bakeLayer(GoatmanFigureModel.LAYER));m.pose(0,0,0,n==6,0,0);model=m;skin=texture("goatman");}
            else if(n<10){var m=new MinotaurModel(mc.getEntityModels().bakeLayer(MinotaurModel.LAYER));m.pose(n==7?MinotaurEntity.WATCHING:n==8?MinotaurEntity.WOUNDED:MinotaurEntity.RELEASED,0,0,0,0,0);model=m;skin=FinaleClientEvents.MATERIALS;}
            else if(n<12){var m=new NovelCreatureRenderers.VultureModel(mc.getEntityModels().bakeLayer(NovelCreatureRenderers.VULTURE));m.preview(n==10?70:3);model=m;skin=NovelCreatureRenderers.VULTURE_SKIN;}
            else if(n<15){var m=new LiteraryRenderers.ElkModel(mc.getEntityModels().bakeLayer(LiteraryRenderers.ELK));if(n==14)m.dead(2,4);model=m;skin=texture(n==12?"literary_elk":n==13?"literary_elk_wounded":"literary_elk_carcass_skinned");}
            else{var m=new RabbitModel<>(mc.getEntityModels().bakeLayer(ModelLayers.RABBIT));m.young=false;model=m;skin=texture("literary_snow_rabbit");}
            float cx=8+(i%4+.5F)*cw,top=28+(i/4)*ch;
            g.drawCenteredString(font,names[n],(int)cx,(int)(top+ch-10),0xffe0dacb);g.flush();draw(g,model,skin,cx,top+ch*.45F,cw*.86F,ch*.8F,155,-8);
        }
    }
    @SubscribeEvent public static void launch(ClientTickEvent.Post e){var mc=Minecraft.getInstance();if(Boolean.getBoolean("the_oldest_house.characterArtSmoke")&&!launched&&mc.screen!=null&&mc.getOverlay()==null){launched=true;mc.options.guiScale().set(2);mc.resizeDisplay();mc.setScreen(new CharacterArtVisualProof());}}
    @SubscribeEvent public static void frame(RenderFrameEvent.Post e)throws Exception{
        var mc=Minecraft.getInstance();if(!(mc.screen instanceof CharacterArtVisualProof proof)||++proof.frames<8)return;
        var dir=Path.of("../build/font-smoke");Files.createDirectories(dir);
        try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(dir.resolve(String.format(java.util.Locale.ROOT,"native-character-art-%02d.png",proof.page)));}
        if(++proof.page<7+(proof.cast.size()+7)/8){proof.frames=0;return;}
        Files.writeString(dir.resolve("character-art-passed.txt"),proof.cast.size()+" human skins on production layers, front and back; native coffin/pillow and three close views, seated original, bounded corpse vertices, joint and renderer-reuse reset checks. Existing character-phase views cover all sixteen Mother stages, witch attacks, cloth girl and earlier creature views. Drisking is included on its production humanoid layer. These are staged mesh/UV views, not uninterrupted ordinary playthroughs.\n");
        TheOldestHouse.LOGGER.info("CHARACTER ART CHECK PASSED: {} human sheets and native coffin bounds",proof.cast.size());
        if(Boolean.getBoolean("the_oldest_house.characterArtSmoke"))mc.stop();else mc.setScreen(new ShallowsVisualProof());
    }
}

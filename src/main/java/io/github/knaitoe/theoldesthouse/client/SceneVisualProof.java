package io.github.knaitoe.theoldesthouse.client;

import com.google.gson.*;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.math.Axis;
import com.mojang.serialization.JsonOps;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.UncannyRegistry;
import io.github.knaitoe.theoldesthouse.labyrinth.UncannyRooms;
import java.nio.file.*;
import java.util.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Cutaways of server-generated scenes, rendered with the shipped native block models and textures. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class SceneVisualProof extends Screen {
    private record Voxel(int x,int y,int z,BlockState state,float swing){}
    private final List<Path> files;
    private final boolean environment;
    private final boolean interiors;
    private final boolean foyer;
    private final boolean meal;
    private final boolean read;
    private record Prop(double x,double y,double z,net.minecraft.world.item.ItemStack item,org.joml.Vector3f scale,org.joml.Quaternionf rotation){}
    private final List<Prop> props=new ArrayList<>();
    private static boolean environmentLaunched;
    private final List<Voxel> voxels=new ArrayList<>();
    private int index,frames;private String name;private double cx,cz,cy,span;private float viewYaw=-45;
    private final LiteraryRenderers.CastModel body;private final LiteraryRenderers.ElkModel elk;
    private static boolean dead(BlockState s){if(!s.is(io.github.knaitoe.theoldesthouse.labyrinth.LiteraryRegistry.PROP.get()))return false;var k=s.getValue(io.github.knaitoe.theoldesthouse.labyrinth.LiteraryPropBlock.KIND);return k==io.github.knaitoe.theoldesthouse.labyrinth.LiteraryPropBlock.Kind.CARCASS||k==io.github.knaitoe.theoldesthouse.labyrinth.LiteraryPropBlock.Kind.GUEST_BODY||k==io.github.knaitoe.theoldesthouse.labyrinth.LiteraryPropBlock.Kind.CREW_BODY;}
    @SubscribeEvent public static void launch(ClientTickEvent.Post e)throws Exception{
        var mc=Minecraft.getInstance();
        if((Boolean.getBoolean("the_oldest_house.uncannyEnvironmentSmoke")||Boolean.getBoolean("the_oldest_house.interiorEnvironmentSmoke")||Boolean.getBoolean("the_oldest_house.foyerSmoke")||Boolean.getBoolean("the_oldest_house.goatmanMealSmoke")||Boolean.getBoolean("the_oldest_house.houseReadSmoke"))&&!environmentLaunched&&mc.screen!=null&&mc.getOverlay()==null){environmentLaunched=true;mc.options.guiScale().set(2);mc.resizeDisplay();mc.setScreen(new SceneVisualProof(true));}
    }
    public SceneVisualProof()throws Exception{this(false);}
    private SceneVisualProof(boolean environment)throws Exception{
        super(Component.literal("Native scene architecture"));
        this.environment=environment;
        interiors=environment&&Boolean.getBoolean("the_oldest_house.interiorEnvironmentSmoke");
        foyer=environment&&Boolean.getBoolean("the_oldest_house.foyerSmoke");
        meal=environment&&Boolean.getBoolean("the_oldest_house.goatmanMealSmoke");
        read=environment&&Boolean.getBoolean("the_oldest_house.houseReadSmoke");
        var models=Minecraft.getInstance().getEntityModels();body=new LiteraryRenderers.CastModel(models.bakeLayer(LiteraryRenderers.CAST));elk=new LiteraryRenderers.ElkModel(models.bakeLayer(LiteraryRenderers.ELK));
        try(var stream=Files.list(Path.of(read?"../build/house-read139-proof":meal?"../build/goatman137-proof":foyer?"../build/foyer136-proof":interiors?"../build/interior-environment-proof":environment?"../build/uncanny-environment-proof":"../build/architecture-proof"))){files=stream.filter(p->p.toString().endsWith(".json")).sorted().toList();}
        int expected=read?6:meal?3:foyer?4:interiors?io.github.knaitoe.theoldesthouse.labyrinth.VignetteInteriors.PLACES.size()+12:environment?11:99+UncannyRooms.PLACES.size();
        if(files.size()!=expected)throw new IllegalStateException("Expected "+expected+" generated architecture views (99 earlier plus every uncanny room), found "+files.size());
        if(!environment)for(var room:UncannyRooms.PLACES)if(files.stream().noneMatch(p->p.getFileName().toString().equals(room.id()+".json")))throw new IllegalStateException("Missing native architecture export for "+room.id());
        load();
    }
    private void load()throws Exception{
        var json=JsonParser.parseString(Files.readString(files.get(index))).getAsJsonObject();name=json.get("name").getAsString();viewYaw=json.has("viewYaw")?json.get("viewYaw").getAsFloat():-45;
        List<BlockState> palette=new ArrayList<>();for(var e:json.getAsJsonArray("palette"))palette.add(BlockState.CODEC.parse(JsonOps.INSTANCE,e).getOrThrow());
        voxels.clear();int x0=Integer.MAX_VALUE,x1=Integer.MIN_VALUE,z0=Integer.MAX_VALUE,z1=Integer.MIN_VALUE,y0=Integer.MAX_VALUE,y1=Integer.MIN_VALUE;
        for(var e:json.getAsJsonArray("blocks")){var a=e.getAsJsonArray();int x=a.get(0).getAsInt(),y=a.get(1).getAsInt(),z=a.get(2).getAsInt();
            voxels.add(new Voxel(x,y,z,palette.get(a.get(3).getAsInt()),a.size()>4?a.get(4).getAsFloat():0));x0=Math.min(x0,x);x1=Math.max(x1,x);z0=Math.min(z0,z);z1=Math.max(z1,z);y0=Math.min(y0,y);y1=Math.max(y1,y);
        }
        cx=(x0+x1+1)/2.;cz=(z0+z1+1)/2.;cy=(y0+y1+1)/2.;span=Math.max((x1-x0+z1-z0+2)*.72,(y1-y0+1)*1.8);if(foyer||meal)span*=1.2;frames=0;
        props.clear();if(json.has("items"))for(var entry:json.getAsJsonArray("items")){var item=entry.getAsJsonObject();var stack=new net.minecraft.world.item.ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(net.minecraft.resources.ResourceLocation.parse(item.get("item").getAsString())));if(item.has("model"))stack.set(net.minecraft.core.component.DataComponents.CUSTOM_MODEL_DATA,new net.minecraft.world.item.component.CustomModelData(item.get("model").getAsInt()));var scale=item.getAsJsonArray("scale");var rotation=item.getAsJsonArray("rotation");props.add(new Prop(item.get("x").getAsDouble(),item.get("y").getAsDouble(),item.get("z").getAsDouble(),stack,new org.joml.Vector3f(scale.get(0).getAsFloat(),scale.get(1).getAsFloat(),scale.get(2).getAsFloat()),new org.joml.Quaternionf(rotation.get(0).getAsFloat(),rotation.get(1).getAsFloat(),rotation.get(2).getAsFloat(),rotation.get(3).getAsFloat())));}
    }
    @Override public void renderBackground(GuiGraphics g,int x,int y,float delta){}
    @Override public void render(GuiGraphics g,int x,int y,float delta){
        var mc=Minecraft.getInstance();g.fill(0,0,width,height,0xFF292723);g.drawString(font,TheOldestHouse.displayName()+" / "+net.neoforged.fml.ModList.get().getModContainerById(TheOldestHouse.MOD_ID).orElseThrow().getModInfo().getVersion(),16,12,0xFFE0D5BF,false);
        g.drawString(font,name.replace('_',' '),16,29,0xFFF3EEE3,false);g.drawString(font,"Native geometry / full-bright cutaway / actors omitted",16,height-19,0xFFD4C8B3,false);g.flush();
        var pose=g.pose();pose.pushPose();pose.translate(width/2.,height/2.+12,500);float scale=(float)Math.min((width-40)/span,(height-85)/(span*.62));
        pose.scale(scale,-scale,scale);pose.mulPose(Axis.XP.rotationDegrees(32));pose.mulPose(Axis.YP.rotationDegrees(viewYaw));pose.translate(-cx,-cy,-cz);
        var buffers=mc.renderBuffers().bufferSource();
        for(var v:voxels){pose.pushPose();pose.translate(v.x,v.y,v.z);
            if(v.state.is(io.github.knaitoe.theoldesthouse.labyrinth.LiteraryRegistry.HANDLING.get()))VignetteHandlingRenderer.draw(v.state,io.github.knaitoe.theoldesthouse.labyrinth.VignetteHandlingGeometry.View.initial(),0,pose,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);
            else if(v.state.is(io.github.knaitoe.theoldesthouse.labyrinth.LiteraryRegistry.DETAIL.get()))ServiceFittingRenderer.draw(v.state,0,pose,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);
            else if(v.state.is(UncannyRegistry.PROP.get()))UncannyPropRenderer.draw(v.state,pose,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,v.swing);
            else if(v.state.is(io.github.knaitoe.theoldesthouse.labyrinth.NovelRegistry.WELL_COVER.get()))WellCoverRenderer.draw(0,false,pose,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);
            else if(v.state.is(net.minecraft.world.level.block.Blocks.WATER))water(pose,buffers,v.state);else if(v.state.getBlock() instanceof net.minecraft.world.level.block.BedBlock bed){drawBed(v.state,bed,pose,buffers);}else if(dead(v.state)){var kind=v.state.getValue(io.github.knaitoe.theoldesthouse.labyrinth.LiteraryPropBlock.KIND);long seed=net.minecraft.util.Mth.getSeed(v.x,v.y,v.z);
                // The carcasses and the dead are drawn by their block entity renderer in play; draw them the same way here.
                if(kind==io.github.knaitoe.theoldesthouse.labyrinth.LiteraryPropBlock.Kind.CARCASS)LiteraryRenderers.ModelRenderer.carcass(elk,v.state,seed,pose,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);
                else LiteraryRenderers.ModelRenderer.corpse(body,v.state,seed,kind==io.github.knaitoe.theoldesthouse.labyrinth.LiteraryPropBlock.Kind.CREW_BODY,pose,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);
            }else mc.getBlockRenderer().renderSingleBlock(v.state,pose,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);pose.popPose();}
        for(var prop:props){pose.pushPose();pose.translate(prop.x,prop.y,prop.z);pose.mulPose(prop.rotation);pose.scale(prop.scale.x,prop.scale.y,prop.scale.z);mc.getItemRenderer().renderStatic(prop.item,net.minecraft.world.item.ItemDisplayContext.FIXED,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,pose,buffers,null,0);pose.popPose();}
        buffers.endBatch();pose.popPose();
    }
    /** BedRenderer's world branch: draw each actual half with its saved facing, never its inventory fallback. */
    private static void drawBed(BlockState state,net.minecraft.world.level.block.BedBlock block,com.mojang.blaze3d.vertex.PoseStack pose,MultiBufferSource out){
        var mc=Minecraft.getInstance();var part=mc.getEntityModels().bakeLayer(state.getValue(net.minecraft.world.level.block.BedBlock.PART)==net.minecraft.world.level.block.state.properties.BedPart.HEAD?net.minecraft.client.model.geom.ModelLayers.BED_HEAD:net.minecraft.client.model.geom.ModelLayers.BED_FOOT);
        var material=Sheets.BED_TEXTURES[block.getColor().getId()];pose.pushPose();pose.translate(0,.5625F,0);pose.mulPose(Axis.XP.rotationDegrees(90));pose.translate(.5F,.5F,.5F);pose.mulPose(Axis.ZP.rotationDegrees(180+state.getValue(net.minecraft.world.level.block.BedBlock.FACING).toYRot()));pose.translate(-.5F,-.5F,-.5F);part.render(pose,material.buffer(out,RenderType::entitySolid),LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);pose.popPose();
    }
    private static void water(com.mojang.blaze3d.vertex.PoseStack pose,MultiBufferSource buffers,BlockState state){
        var sprite=Minecraft.getInstance().getTextureAtlas(net.minecraft.world.inventory.InventoryMenu.BLOCK_ATLAS).apply(net.minecraft.resources.ResourceLocation.withDefaultNamespace("block/water_still"));
        var out=buffers.getBuffer(RenderType.entityTranslucent(net.minecraft.world.inventory.InventoryMenu.BLOCK_ATLAS));var matrix=pose.last().pose();float y=state.getFluidState().getOwnHeight();
        float[][] points={{0,y,0,sprite.getU0(),sprite.getV0()},{0,y,1,sprite.getU0(),sprite.getV1()},{1,y,1,sprite.getU1(),sprite.getV1()},{1,y,0,sprite.getU1(),sprite.getV0()}};
        for(var p:points)out.addVertex(matrix,p[0],p[1],p[2]).setColor(63,118,228,185).setUv(p[3],p[4]).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(0,1,0);
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post e)throws Exception{
        var mc=Minecraft.getInstance();if(!(mc.screen instanceof SceneVisualProof s)||(!s.environment&&!Boolean.getBoolean("the_oldest_house.fontSmoke"))||++s.frames<3)return;
        Path folder=Path.of(s.read?"../build/house-read139-art":s.meal?"../build/goatman137-art":s.foyer?"../build/foyer136-art":s.interiors?"../build/font-smoke/interior-environments":s.environment?"../build/font-smoke/uncanny-environments":"../build/font-smoke/scenes");Files.createDirectories(folder);
        try(NativeImage image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(folder.resolve(s.name+".png"));if(s.foyer||s.meal){var colors=new HashSet<Integer>();for(int y=image.getHeight()/5;y<image.getHeight()*4/5;y+=3)for(int x=image.getWidth()/5;x<image.getWidth()*4/5;x+=3)colors.add(image.getPixelRGBA(x,y));if(colors.size()<15)throw new IllegalStateException("Blank native foyer view: "+s.name);}}
        if(++s.index<s.files.size())s.load();else{
            if(s.read){mc.setScreen(new HouseReadClothesProof());return;}
            if(s.meal){Files.writeString(folder.resolve("passed.txt"),"Three actual native exports: both inward-foot bed rows and the real supper table, including the original carried bottles and finite buns. Production block and item models; full-bright geometry/material review.\n");mc.stop();return;}
            if(s.foyer){Files.writeString(folder.resolve("passed.txt"),"Four actual native foyer exports: standoff, admitted glass door, hinged service exit and opened unhinged hallway. Production shared leaf/collision cuboids and saved pendulum angles. Full-bright geometry/material review; actual lighting, actors and client audio remain separate checks.\n");mc.stop();return;}
            if(s.interiors){Files.writeString(Path.of("../build/font-smoke/interior-environments-passed.txt"),"Fourteen actual native rooms, four floor-specific cutaways, four selected domestic extensions with finite household records, the quiet room, steel dumpster and two institute shelf/window close-ups. Full bright architecture views; not a lighting proof.\n");mc.setScreen(new DomesticAssetProof());return;}
            if(s.environment){Files.writeString(Path.of("../build/font-smoke/uncanny-environments-passed.txt"),"Eleven native room, kitchen, bathroom, exterior and floor-specific cutaways rendered with shipped textures and collision-authored models. Full-bright views; not a lighting proof.\n");mc.setScreen(new UncannyAssetProof());return;}
            Files.writeString(Path.of("../build/font-smoke/architecture-passed.txt"),"All "+s.files.size()+" native generated architecture views rendered successfully, including the 99 earlier views and all six uncanny rooms.\n");
            TheOldestHouse.LOGGER.info("HOUSE ARCHITECTURE CHECK PASSED: {} native architecture screenshots saved",s.files.size());mc.setScreen(new OutsideSkyProof());
        }
    }
}

package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.core.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.joml.Matrix4f;

/**
 * 0.4.161, the owner's playtest notes ("Mirror in closet doesn't provide accurate reflection."): a true plane mirror.
 * Each frame the room in front of the glass and the people in it are drawn reflected through the glass's plane into an
 * offscreen picture with the ordinary camera, and the glass shows that picture pixel for pixel. The reader is still the
 * House's version of them in it: the glasses an older read gives them, or no face where the glass did not agree.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class HouseReadReflection {
    /** The glass in the mirror's lower block, as the model draws it facing south: x and y in blocks, its face just in front. */
    static final float X0=1/16f,X1=15/16f,Y0=1/16f,Y1=31/16f,FACE=.75f/16f;
    /** How far in front of the glass the room is reflected, and to each side, above and below. */
    private static final int DEPTH=13,SIDE=8,BELOW=2,ABOVE=5;
    private static ShaderInstance shader;
    private static TextureTarget picture;
    private static VertexBuffer opaque,translucent;private static boolean hasOpaque,hasTranslucent;
    private static BlockPos baked;private static long bakedAt=Long.MIN_VALUE;
    private static List<Glass> seen=new ArrayList<>(),last=new ArrayList<>();
    private static Glass active;
    private static MultiBufferSource.BufferSource buffers;
    private static net.minecraft.client.model.geom.ModelPart standardCloth,slimCloth;
    private HouseReadReflection(){}

    record Glass(BlockPos at,Direction facing){
        /** A point in the mirror block's own model space (facing south), in the world. */
        Vec3 world(double x,double y,double z){return switch(facing){case WEST->new Vec3(at.getX()+1-z,at.getY()+y,at.getZ()+x);case NORTH->new Vec3(at.getX()+1-x,at.getY()+y,at.getZ()+1-z);case EAST->new Vec3(at.getX()+z,at.getY()+y,at.getZ()+1-x);default->new Vec3(at.getX()+x,at.getY()+y,at.getZ()+z);};}
        Vec3 normal(){return Vec3.atLowerCornerOf(facing.getNormal());}
        Vec3 plane(){return world(.5,1,FACE);}
    }

    /** The glass's renderer asks to be reflected; it is, from the next frame, if it is the nearest one in view. */
    static void want(BlockPos at,Direction facing){seen.add(new Glass(at.immutable(),facing));}
    static boolean showing(BlockPos at){return active!=null&&active.at.equals(at)&&picture!=null&&shader!=null;}

    /** The glass's surface: the reflected picture, sampled where each of its pixels falls on the screen. */
    static final RenderType GLASS=RenderType.create("the_oldest_house_read_mirror_glass",DefaultVertexFormat.POSITION,VertexFormat.Mode.QUADS,256,false,false,
        RenderType.CompositeState.builder().setShaderState(new RenderStateShard.ShaderStateShard(()->shader))
            .setTextureState(new RenderStateShard.EmptyTextureStateShard(()->{if(picture!=null)RenderSystem.setShaderTexture(0,picture.getColorTextureId());},()->{}))
            .setCullState(RenderStateShard.NO_CULL).createCompositeState(false));

    static void drawGlass(Glass glass,PoseStack pose,MultiBufferSource out){
        var m=pose.last().pose();var at=glass.at;var consumer=out.getBuffer(GLASS);
        for(double[] corner:new double[][]{{X0,Y0},{X1,Y0},{X1,Y1},{X0,Y1}}){var p=glass.world(corner[0],corner[1],FACE);consumer.addVertex(m,(float)(p.x-at.getX()),(float)(p.y-at.getY()),(float)(p.z-at.getZ()));}
    }

    @SubscribeEvent public static void stage(RenderLevelStageEvent e){
        if(e.getStage()==RenderLevelStageEvent.Stage.AFTER_LEVEL){last=seen;seen=new ArrayList<>();return;}
        if(e.getStage()!=RenderLevelStageEvent.Stage.AFTER_CUTOUT_BLOCKS)return;
        active=null;var mc=Minecraft.getInstance();if(mc.level==null||mc.player==null||shader==null)return;
        var camera=e.getCamera().getPosition();Glass best=null;double nearest=Double.MAX_VALUE;
        for(var glass:last){double front=camera.subtract(glass.plane()).dot(glass.normal()),d=camera.distanceTo(glass.plane());if(front>.05&&d<DEPTH&&d<nearest){nearest=d;best=glass;}}
        if(best==null)return;
        try{render(e,best,camera);active=best;}finally{mc.getMainRenderTarget().bindWrite(true);}
    }

    private static void render(RenderLevelStageEvent e,Glass glass,Vec3 camera){
        var mc=Minecraft.getInstance();var main=mc.getMainRenderTarget();
        if(picture==null)picture=new TextureTarget(main.width,main.height,true,Minecraft.ON_OSX);
        else if(picture.width!=main.width||picture.height!=main.height)picture.resize(main.width,main.height,Minecraft.ON_OSX);
        picture.setClearColor(.05f,.045f,.04f,1f);picture.clear(Minecraft.ON_OSX);picture.bindWrite(true);
        long now=mc.level.getGameTime();if(!glass.at.equals(baked)||now-bakedAt>=10||now<bakedAt)bake(glass);
        // The plane's reflection in camera space: u' = R u + t, R flipping the axis the glass faces.
        var n=glass.normal();double push=2*glass.plane().subtract(camera).dot(n);
        float sx=n.x!=0?-1:1,sz=n.z!=0?-1:1;
        var origin=Vec3.atLowerCornerOf(glass.at).subtract(camera);var reflected=new Vec3(origin.x*sx+n.x*push,origin.y,origin.z*sz+n.z*push);
        var view=new Matrix4f(e.getModelViewMatrix()).translate((float)reflected.x,(float)reflected.y,(float)reflected.z).scale(sx,1,sz);
        if(hasOpaque)draw(opaque,RenderType.cutout(),view,e.getProjectionMatrix());if(hasTranslucent)draw(translucent,RenderType.translucent(),view,e.getProjectionMatrix());
        VertexBuffer.unbind();
        // The people in front of the glass, at full size where the mirror puts them.
        if(buffers==null)buffers=MultiBufferSource.immediate(new ByteBufferBuilder(786432));
        Lighting.setupLevel();var dispatcher=mc.getEntityRenderDispatcher();float partial=e.getPartialTick().getGameTimeDeltaPartialTick(false);
        var pose=new PoseStack();pose.translate(n.x*push,0,n.z*push);pose.scale(sx,1,sz);
        // At this stage the level has not yet put the camera's turn on the shared model-view; entities need it there.
        var stack=RenderSystem.getModelViewStack();stack.pushMatrix();stack.mul(e.getModelViewMatrix());RenderSystem.applyModelViewMatrix();
        dispatcher.setRenderShadow(false);
        try{for(var player:mc.level.players()){
            var feet=player.getPosition(partial);if(player.isSpectator()||player.isInvisible()||feet.subtract(glass.plane()).dot(n)<.05||feet.distanceTo(glass.plane())>DEPTH)continue;
            var at=feet.subtract(camera);int light=dispatcher.getPackedLightCoords(player,partial);
            dispatcher.render(player,at.x,at.y,at.z,net.minecraft.util.Mth.lerp(partial,player.yRotO,player.getYRot()),partial,pose,buffers,light);
            if(player==mc.player&&dispatcher.getRenderer(player) instanceof PlayerRenderer renderer)house(renderer,player,at,partial,pose,light);
        }buffers.endBatch();}finally{dispatcher.setRenderShadow(true);stack.popMatrix();RenderSystem.applyModelViewMatrix();}
    }
    /** The House's version of the reader, on the same posed body the renderer just drew. */
    private static void house(PlayerRenderer renderer,AbstractClientPlayer player,Vec3 at,float partial,PoseStack pose,int light){
        var mc=Minecraft.getInstance();if(standardCloth==null){standardCloth=mc.getEntityModels().bakeLayer(HouseReadClothes.STANDARD);slimCloth=mc.getEntityModels().bakeLayer(HouseReadClothes.SLIM);}
        boolean narrow=player.getSkin().model()==net.minecraft.client.resources.PlayerSkin.Model.SLIM;var offset=renderer.getRenderOffset(player,partial);
        float bodyYaw=net.minecraft.util.Mth.rotLerp(partial,player.yBodyRotO,player.yBodyRot),scale=player.getScale();
        pose.pushPose();pose.translate(at.x+offset.x,at.y+offset.y,at.z+offset.z);pose.scale(scale,scale,scale);pose.mulPose(Axis.YP.rotationDegrees(180-bodyYaw));pose.scale(-1,-1,1);pose.scale(.9375f,.9375f,.9375f);pose.translate(0,-1.501f,0);
        HouseReadLookClient.dress(renderer.getModel(),narrow?slimCloth:standardCloth,"",pose,buffers,light);pose.popPose();
    }
    private static void draw(VertexBuffer mesh,RenderType type,Matrix4f view,Matrix4f projection){
        if(mesh==null)return;type.setupRenderState();RenderSystem.disableCull();var program=RenderSystem.getShader();
        if(program!=null){if(program.CHUNK_OFFSET!=null)program.CHUNK_OFFSET.set(0f,0f,0f);mesh.bind();mesh.drawWithShader(view,projection,program);}
        type.clearRenderState();
    }
    /** The room in front of the glass, as the level draws it, relative to the mirror block. Never the glass itself. */
    private static void bake(Glass glass){
        var mc=Minecraft.getInstance();var level=mc.level;var blocks=mc.getBlockRenderer();var random=RandomSource.create();
        var solidBytes=new ByteBufferBuilder(1<<20);var clearBytes=new ByteBufferBuilder(1<<16);
        var solid=new BufferBuilder(solidBytes,VertexFormat.Mode.QUADS,DefaultVertexFormat.BLOCK);var clear=new BufferBuilder(clearBytes,VertexFormat.Mode.QUADS,DefaultVertexFormat.BLOCK);
        var n=glass.normal();var plane=glass.plane();var pose=new PoseStack();
        var side=glass.facing.getClockWise();
        try{
            for(int d=0;d<=DEPTH;d++)for(int s=-SIDE;s<=SIDE;s++)for(int y=-BELOW;y<=ABOVE;y++){
                var pos=glass.at.relative(glass.facing,d).relative(side,s).above(y);if(pos.equals(glass.at)||pos.equals(glass.at.above()))continue;
                if(Vec3.atCenterOf(pos).subtract(plane).dot(n)<=0)continue;
                var state=level.getBlockState(pos);if(state.isAir()||state.getRenderShape()!=net.minecraft.world.level.block.RenderShape.MODEL)continue;
                var model=blocks.getBlockModel(state);long seed=state.getSeed(pos);random.setSeed(seed);
                pose.pushPose();pose.translate(pos.getX()-glass.at.getX(),pos.getY()-glass.at.getY(),pos.getZ()-glass.at.getZ());
                for(var type:model.getRenderTypes(state,random,ModelData.EMPTY)){random.setSeed(seed);blocks.renderBatched(state,pos,level,pose,type==RenderType.translucent()?clear:solid,true,random,ModelData.EMPTY,type);}
                pose.popPose();
            }
            if(opaque==null){opaque=new VertexBuffer(VertexBuffer.Usage.DYNAMIC);translucent=new VertexBuffer(VertexBuffer.Usage.DYNAMIC);}
            var mesh=solid.build();hasOpaque=mesh!=null;upload(opaque,mesh);mesh=clear.build();hasTranslucent=mesh!=null;upload(translucent,mesh);
        }finally{solidBytes.close();clearBytes.close();}
        baked=glass.at;bakedAt=level.getGameTime();
    }
    private static void upload(VertexBuffer buffer,MeshData mesh){if(mesh==null)return;buffer.bind();buffer.upload(mesh);VertexBuffer.unbind();}

    @EventBusSubscriber(modid=TheOldestHouse.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
    public static final class Registration {
        @SubscribeEvent public static void shaders(RegisterShadersEvent e)throws java.io.IOException{
            e.registerShader(new ShaderInstance(e.getResourceProvider(),ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"house_read_mirror"),DefaultVertexFormat.POSITION),loaded->shader=loaded);
        }
    }
}

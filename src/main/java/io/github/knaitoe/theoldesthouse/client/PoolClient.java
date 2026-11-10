package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.BarRegistry;
import io.github.knaitoe.theoldesthouse.labyrinth.PoolGame;
import io.github.knaitoe.theoldesthouse.labyrinth.PoolTableBlock;
import io.github.knaitoe.theoldesthouse.labyrinth.PoolTableBlockEntity;
import io.github.knaitoe.theoldesthouse.network.PoolShotPayload;
import io.github.knaitoe.theoldesthouse.network.PoolStatePayload;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/**
 * 0.4.414 (docs/BAR_GAMES_PROPOSAL.md): what this client knows of the pool tables near it. A frame as the server last sent it, and a
 * shot being played out: the client plays the same shot from the same table, a step at a time in real time, with its knocks and
 * drops heard where they happen, and then takes the server's table as it is. The balls are drawn on the table itself
 * ({@link Renderer}), so anyone near can watch; the shooter's screen is {@link PoolScreen}.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class PoolClient {
    private PoolClient(){}
    public static final class View {
        public final BlockPos foot;public PoolGame shown=new PoolGame();public CompoundTag info=new CompoundTag();
        @Nullable PoolGame playing;@Nullable CompoundTag after;double stepsDue;long lastNanos;int stepsDone;
        public View(BlockPos foot){this.foot=foot;}
        /** True while a shot is still rolling on this client. */
        public boolean rolling(){return playing!=null;}
        public PoolGame table(){return playing!=null?playing:shown;}
        public List<String> names(){var out=new ArrayList<String>();for(Tag t:info.getList("Names",Tag.TAG_STRING))out.add(t.getAsString());return out;}
        public List<UUID> seats(){var out=new ArrayList<UUID>();for(Tag t:info.getList("Seats",Tag.TAG_COMPOUND))out.add(((CompoundTag)t).getUUID("Id"));return out;}
        public int shooterSeat(){return info.getInt("Shooter");}
        public List<Integer> sides(){var out=new ArrayList<Integer>();for(Tag t:info.getList("Sides",Tag.TAG_INT))out.add(((net.minecraft.nbt.IntTag)t).getAsInt());return out;}
        /** True if the local player takes the next shot. */
        public boolean mine(){var p=Minecraft.getInstance().player;var s=seats();int k=shooterSeat();return p!=null&&k>=0&&k<s.size()&&s.get(k).equals(p.getUUID());}
    }
    static final Map<BlockPos,View> VIEWS=new HashMap<>();
    public static @Nullable View view(BlockPos foot){return VIEWS.get(foot);}

    public static void state(PoolStatePayload p){
        var v=VIEWS.computeIfAbsent(p.table().immutable(),View::new);
        if(!v.rolling())v.shown.load(p.game());else v.after=p.game().copy();
        v.info=p.info();
        if(p.open())Minecraft.getInstance().setScreen(new PoolScreen(p.table().immutable()));
    }
    public static void shot(PoolShotPayload p){
        var v=VIEWS.computeIfAbsent(p.table().immutable(),View::new);
        var g=new PoolGame();g.load(p.before());
        if(g.ballInHand){g.x[0]=p.placeX();g.y[0]=p.placeY();g.on[0]=true;g.ballInHand=false;}
        g.begin(p.angle(),p.power());
        v.playing=g;v.after=p.after();v.info=p.info();v.stepsDue=0;v.stepsDone=0;v.lastNanos=System.nanoTime();
        sound(v,BarRegistry.POOL_STRIKE.get(),.25F+(float)p.power()*.6F,1F);
    }
    /** Every shot being played out moves on by the time since the last frame, at the engine's own rate. */
    static void advance(View v){
        if(v.playing==null)return;
        long now=System.nanoTime();v.stepsDue+=(now-v.lastNanos)/1e9*240;v.lastNanos=now;
        int n=(int)Math.min(v.stepsDue,80);v.stepsDue-=n;
        for(int i=0;i<n;i++){
            var r=new PoolGame.Result();boolean moving=v.playing.step(r);v.stepsDone++;
            if(r.clacks>0)sound(v,BarRegistry.POOL_CLACK.get(),.5F,.9F+(float)Math.random()*.25F);
            if(r.cushions>0)sound(v,BarRegistry.POOL_CUSHION.get(),.35F,1F);
            if(!r.potted.isEmpty())sound(v,BarRegistry.POOL_POCKET.get(),.6F,1F);
            if(!moving||v.stepsDone>=PoolGame.MAX_STEPS){finish(v);return;}
        }
    }
    static void finish(View v){if(v.after!=null)v.shown.load(v.after);v.playing=null;v.after=null;}
    static void sound(View v,net.minecraft.sounds.SoundEvent s,float volume,float pitch){
        var l=Minecraft.getInstance().level;if(l==null)return;var c=Vec3.atCenterOf(v.foot);
        l.playLocalSound(c.x,c.y,c.z,s,SoundSource.BLOCKS,volume,pitch,false);
    }
    @SubscribeEvent public static void tick(ClientTickEvent.Post e){
        var mc=Minecraft.getInstance();
        if(mc.level==null||mc.player==null){VIEWS.clear();return;}
        VIEWS.values().removeIf(v->v.foot.distSqr(mc.player.blockPosition())>96*96||!PoolTableBlock.is(mc.level.getBlockState(v.foot)));
        advanceAll();
    }

    // ------------------------------------------------------------------------------------------------ where a ball is in the world
    /**
     * A point on the cloth, from table units, relative to the foot block's lower corner. The cloth runs from the foot's far rail to
     * the head's far rail (29 pixels) along the way the table faces, and 13 pixels across it.
     */
    public static Vec3 onCloth(Direction facing,double tx,double ty,double lift){
        double s=tx/PoolGame.W*29/16-6.5/16,t=ty/PoolGame.H*13/16-6.5/16;
        var cw=facing.getClockWise();
        return new Vec3(.5+facing.getStepX()*s+cw.getStepX()*t,12/16.0+lift,.5+facing.getStepZ()*s+cw.getStepZ()*t);
    }
    /** The balls' colours: the cue ball, then the seven solids, the eight, and the stripes on the same seven. */
    public static final int[] COLOURS={0xF4F0E4,0xE8C020,0x2850B8,0xC8301E,0x5A2C8C,0xE07020,0x1E7A3C,0x7A1E22,0x161616};
    public static int colour(int ball){return ball<=8?COLOURS[ball]:COLOURS[ball-8];}
    public static boolean striped(int ball){return ball>=9;}

    static final ResourceLocation WHITE=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/block/bar/ball.png");
    /** The balls on the cloth, for everyone near the table. */
    @EventBusSubscriber(modid=TheOldestHouse.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
    public static final class Renderer implements BlockEntityRenderer<PoolTableBlockEntity> {
        public Renderer(BlockEntityRendererProvider.Context ignored){}
        @SubscribeEvent public static void register(EntityRenderersEvent.RegisterRenderers e){e.registerBlockEntityRenderer(BarRegistry.POOL_TABLE_ENTITY.get(),Renderer::new);}
        @Override public void render(PoolTableBlockEntity be,float partial,PoseStack pose,MultiBufferSource out,int light,int overlay){
            var v=VIEWS.get(be.getBlockPos());var state=be.getBlockState();if(!PoolTableBlock.is(state))return;
            advanceIfNeeded(v);
            var g=v==null?null:v.table();
            var facing=state.getValue(PoolTableBlock.FACING);var buffer=out.getBuffer(RenderType.entitySolid(WHITE));var m=pose.last();
            double half=PoolGame.R/PoolGame.W*29/16*1.6;
            if(g==null){rack(buffer,m,facing,light,half);return;}
            for(int b=0;b<PoolGame.BALLS;b++){
                if(!g.on[b])continue;
                var c=onCloth(facing,g.x[b],g.y[b],half);int rgb=colour(b);
                box(buffer,m,c,half,rgb,light);
                if(striped(b))box(buffer,m,c.add(0,half*.55,0),half*.72,0xF4F0E4,light);
            }
        }
        /** No frame on: the balls racked at the foot spot, waiting. */
        static void rack(VertexConsumer buffer,PoseStack.Pose m,Direction facing,int light,double half){
            var g=PoolGame.rack(0);
            for(int b=0;b<PoolGame.BALLS;b++){var c=onCloth(facing,g.x[b],g.y[b],half);box(buffer,m,c,half,colour(b),light);if(striped(b))box(buffer,m,c.add(0,half*.55,0),half*.72,0xF4F0E4,light);}
        }
        static void box(VertexConsumer b,PoseStack.Pose m,Vec3 c,double h,int rgb,int light){
            float x0=(float)(c.x-h),x1=(float)(c.x+h),y0=(float)(c.y-h),y1=(float)(c.y+h),z0=(float)(c.z-h),z1=(float)(c.z+h);
            int r=rgb>>16&255,g=rgb>>8&255,bl=rgb&255;
            quad(b,m,x0,y1,z0,x0,y1,z1,x1,y1,z1,x1,y1,z0,0,1,0,r,g,bl,light);
            quad(b,m,x0,y0,z1,x0,y1,z1,x1,y1,z1,x1,y0,z1,0,0,1,r*9/10,g*9/10,bl*9/10,light);
            quad(b,m,x1,y0,z0,x1,y1,z0,x0,y1,z0,x0,y0,z0,0,0,-1,r*9/10,g*9/10,bl*9/10,light);
            quad(b,m,x0,y0,z0,x0,y1,z0,x0,y1,z1,x0,y0,z1,-1,0,0,r*8/10,g*8/10,bl*8/10,light);
            quad(b,m,x1,y0,z1,x1,y1,z1,x1,y1,z0,x1,y0,z0,1,0,0,r*8/10,g*8/10,bl*8/10,light);
        }
        static void quad(VertexConsumer b,PoseStack.Pose m,float ax,float ay,float az,float bx,float by,float bz,float cx,float cy,float cz,float dx,float dy,float dz,float nx,float ny,float nz,int r,int g,int bl,int light){
            b.addVertex(m,ax,ay,az).setColor(r,g,bl,255).setUv(0,0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(m,nx,ny,nz);
            b.addVertex(m,bx,by,bz).setColor(r,g,bl,255).setUv(0,1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(m,nx,ny,nz);
            b.addVertex(m,cx,cy,cz).setColor(r,g,bl,255).setUv(1,1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(m,nx,ny,nz);
            b.addVertex(m,dx,dy,dz).setColor(r,g,bl,255).setUv(1,0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(m,nx,ny,nz);
        }
        @Override public AABB getRenderBoundingBox(PoolTableBlockEntity be){return be.renderBox();}
    }
    /** A shot plays on at the frame rate whether or not a screen is open. */
    static void advanceIfNeeded(@Nullable View v){if(v!=null)advance(v);}
    public static void advanceAll(){for(var v:VIEWS.values())advance(v);}
}

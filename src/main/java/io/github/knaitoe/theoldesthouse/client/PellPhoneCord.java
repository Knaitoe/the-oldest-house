package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.LiteraryActor;
import io.github.knaitoe.theoldesthouse.labyrinth.PellPeople;
import io.github.knaitoe.theoldesthouse.labyrinth.PellRegistry;
import io.github.knaitoe.theoldesthouse.labyrinth.PellThing;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

/**
 * 0.4.566 (owner, of June on the telephone: "phone wire doesn't quite connect. It's VERY good so far, though!"). The kitchen telephone
 * off its hook showed a stub of cord hanging from the plate, and the receiver at June's ear had none. Now the cord is drawn whole, each
 * frame, from where it leaves the bottom of the plate on the wall (the wall set, tools/draw_pell_things.py: phone_1) to the foot of the
 * receiver's mouthpiece, wherever June has it: at her ear on the call (CastModel.receiver) or held out to whoever she is giving it to.
 * It hangs from both ends and sags between them, less as she stands further off; a coiled cord, a turn to each pixel along it, in the
 * telephone's own cord texture.
 */
final class PellPhoneCord {
    private PellPhoneCord(){}
    /** Where the cord leaves the plate, in the telephone's model as it faces north (the model is turned with the block's FACING). */
    private static final float SOCKET_X=8,SOCKET_Y=.5F,SOCKET_Z=14.6F;
    /** Half the cord's width: a pixel across. */
    private static final float HALF=.5F/16;
    /** How far the wall set is looked for, round whoever holds the receiver; a cord never reaches further. */
    private static final int REACH=4;
    private static final ResourceLocation CORD=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"block/pell/thing/phone_cord");

    /** After the cast renderer has drawn {@code a}: their receiver's cord, if they hold one by the telephone. {@code p} is at their feet. */
    static void draw(LiteraryRenderers.CastModel m,LiteraryActor a,float partial,PoseStack p,MultiBufferSource out,int light){
        if(a.role()!=LiteraryActor.STRANGER||!PellPeople.cast(a.phase())||!a.getMainHandItem().is(PellRegistry.RECEIVER.get()))return;
        var socket=socket(a);if(socket==null)return;
        var mouth=mouthpiece(m,a,partial);
        double ex=Mth.lerp(partial,a.xOld,a.getX()),ey=Mth.lerp(partial,a.yOld,a.getY()),ez=Mth.lerp(partial,a.zOld,a.getZ());
        var from=new Vector3f((float)(socket.x-ex),(float)(socket.y-ey),(float)(socket.z-ez));
        if(from.distance(mouth)>REACH)return;
        cord(p,out,light,from,mouth);
    }
    /** The wall set near {@code a}, and the point on it where the cord comes out, in the world. */
    static @Nullable Vec3 socket(LiteraryActor a){
        var l=a.level();var at=a.blockPosition();BlockPos found=null;double best=Double.MAX_VALUE;
        for(var q:BlockPos.betweenClosed(at.offset(-REACH,-1,-REACH),at.offset(REACH,3,REACH))){
            var s=l.getBlockState(q);if(!PellThing.is(s,PellThing.Kind.PHONE))continue;
            double d=q.distToCenterSqr(a.position());if(d<best){best=d;found=q.immutable();}
        }
        if(found==null)return null;
        return point(found,l.getBlockState(found).getValue(PellThing.FACING));
    }
    /** The model's socket turned as the block state turns the model: a quarter turn clockwise, seen from above, for each step from north. */
    static Vec3 point(BlockPos cell,Direction facing){
        float x=SOCKET_X,z=SOCKET_Z;
        int turns=switch(facing){case EAST->1;case SOUTH->2;case WEST->3;default->0;};
        for(int i=0;i<turns;i++){float t=x;x=16-z;z=t;}
        return new Vec3(cell.getX()+x/16,cell.getY()+SOCKET_Y/16,cell.getZ()+z/16);
    }
    /** The foot of the receiver's mouthpiece, from {@code a}'s feet, the world's way up: through the model as the cast renderer turns it. */
    static Vector3f mouthpiece(LiteraryRenderers.CastModel m,LiteraryActor a,float partial){
        return mouthpiece(m,a.getScale(),Mth.rotLerp(partial,a.yBodyRotO,a.yBodyRot),a.getMainHandItem(),a.level(),a);
    }
    /** As above, for a body of {@code scale} turned to {@code bodyYaw} holding {@code held} (the review renders draw it without an actor). */
    static Vector3f mouthpiece(LiteraryRenderers.CastModel m,float scale,float bodyYaw,net.minecraft.world.item.ItemStack held,@Nullable net.minecraft.world.level.Level level,@Nullable net.minecraft.world.entity.LivingEntity holder){
        var q=new PoseStack();
        q.scale(scale,scale,scale);q.mulPose(Axis.YP.rotationDegrees(180-bodyYaw));q.scale(-1,-1,1);q.translate(0,-1.501F,0);
        if(m.onPhone())m.receiver(q);
        else{
            m.translateToHand(HumanoidArm.RIGHT,q);q.mulPose(Axis.XP.rotationDegrees(-90));q.mulPose(Axis.YP.rotationDegrees(180));q.translate(1/16F,.125F,-.625F);
            Minecraft.getInstance().getItemRenderer().getModel(held,level,holder,0).applyTransform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,q,false);
        }
        // The receiver's model (tools/draw_pell_people.py, receiver_model) is drawn from its middle; its mouthpiece's foot is at y 1, z 8.25.
        return q.last().pose().transformPosition(new Vector3f(0,-7/16F,.25F/16),new Vector3f());
    }
    /** The cord from {@code from} to {@code to}, hanging down out of both and sagging between, a pixel thick, light and dark by turns. */
    static void cord(PoseStack p,MultiBufferSource out,int light,Vector3f from,Vector3f to){
        float d=from.distance(to),drop=Mth.clamp(.6F-.25F*d,.12F,.5F);
        var c1=new Vector3f(from).add(0,-drop,0);var c2=new Vector3f(to).add(0,-drop*.8F,0);
        // Long enough to sample a turn to each pixel along the curve's own length (its control polygon is longer than the curve, so it is a little over).
        float along=from.distance(c1)+c1.distance(c2)+c2.distance(to);int n=Mth.clamp((int)Math.ceil(along*16),4,160);
        var pts=new Vector3f[n+1];for(int i=0;i<=n;i++)pts[i]=bezier(from,c1,c2,to,i/(float)n);
        var sprite=Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(CORD);
        float u0=sprite.getU0(),u1=u0+(sprite.getU1()-sprite.getU0())/16,v0=sprite.getV0(),dv=(sprite.getV1()-sprite.getV0())/16;
        VertexConsumer vc=out.getBuffer(RenderType.entityCutoutNoCull(InventoryMenu.BLOCK_ATLAS));var pose=p.last();
        Vector3f[] sideA=frame(pts,0),sideB;
        for(int i=0;i<n;i++){
            sideB=frame(pts,i+1);float va=v0+dv*(i%2),vb=va+dv;
            for(int k=0;k<4;k++){
                // Each of the tube's four sides between the two rings; between corners k and k+1 is the side facing sides[k+1].
                int k2=(k+1)%4;var a1=corner(pts[i],sideA,k);var a2=corner(pts[i],sideA,k2);var b1=corner(pts[i+1],sideB,k);var b2=corner(pts[i+1],sideB,k2);
                var nrm=new Vector3f(sideA[k2]).add(sideB[k2]).normalize();
                vertex(vc,pose,a1,u0,va,light,nrm);vertex(vc,pose,b1,u0,vb,light,nrm);vertex(vc,pose,b2,u1,vb,light,nrm);vertex(vc,pose,a2,u1,va,light,nrm);
            }
            sideA=sideB;
        }
    }
    private static void vertex(VertexConsumer vc,PoseStack.Pose pose,Vector3f at,float u,float v,int light,Vector3f n){
        vc.addVertex(pose,at.x,at.y,at.z).setColor(-1).setUv(u,v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose,n.x,n.y,n.z);
    }
    private static Vector3f bezier(Vector3f a,Vector3f b,Vector3f c,Vector3f d,float t){
        float s=1-t;return new Vector3f(a).mul(s*s*s).add(new Vector3f(b).mul(3*s*s*t)).add(new Vector3f(c).mul(3*s*t*t)).add(new Vector3f(d).mul(t*t*t));
    }
    /** The ring of the cord's four sides at sample {@code i}: two directions across the curve there, each way. */
    private static Vector3f[] frame(Vector3f[] pts,int i){
        var tangent=new Vector3f(pts[Math.min(pts.length-1,i+1)]).sub(pts[Math.max(0,i-1)]);
        if(tangent.lengthSquared()<1e-8)tangent.set(0,-1,0);tangent.normalize();
        var across=new Vector3f(tangent).cross(0,1,0);if(across.lengthSquared()<1e-6)across=new Vector3f(tangent).cross(1,0,0);across.normalize();
        var other=new Vector3f(tangent).cross(across).normalize();
        return new Vector3f[]{across,other,new Vector3f(across).negate(),new Vector3f(other).negate()};
    }
    /** A corner of the ring at {@code at}: between side {@code k} and the side after it. */
    private static Vector3f corner(Vector3f at,Vector3f[] sides,int k){
        return new Vector3f(at).add(new Vector3f(sides[k]).add(sides[(k+1)%4]).mul(HALF));
    }
}

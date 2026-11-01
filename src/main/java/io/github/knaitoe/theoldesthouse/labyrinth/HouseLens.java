package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.203: the House's own camera. A real picture of the world, taken on the server from any eye at any target, ray by ray
 * through the actual blocks: each ray's first visual surface is shaded by its block's own colour, the light that falls on it
 * and the angle it is seen at, and fades with distance. People in the picture are figures, given by where they stood, and
 * come out as dark shapes. The result is one byte of brightness per pixel, row by row from the top left; the client gives it
 * its grain (Work print's tape, and later the Polaroid Trail's prints).
 */
public final class HouseLens {
    private HouseLens(){}
    /** Someone in the picture: where their feet were, and how tall they stood (1.8 upright, 1.5 crouched, 0.6 crawling). */
    public record Figure(Vec3 feet,double height){}
    public static final double REACH=40;

    public static byte[] shoot(ServerLevel l,Vec3 eye,Vec3 target,List<Figure> figures,int w,int h,double fovDegrees){
        var forward=target.subtract(eye).normalize();
        var up=new Vec3(0,1,0);
        var right=Math.abs(forward.y)>.98?new Vec3(1,0,0):forward.cross(up).normalize();
        var upward=right.cross(forward).normalize();
        double half=Math.tan(Math.toRadians(fovDegrees)/2),aspect=(double)w/h;
        byte[] out=new byte[w*h];
        for(int j=0;j<h;j++)for(int i=0;i<w;i++){
            double u=(2*(i+.5)/w-1)*half*aspect,v=(1-2*(j+.5)/h)*half;
            var dir=forward.add(right.scale(u)).add(upward.scale(v)).normalize();
            out[i+j*w]=(byte)Math.max(0,Math.min(255,(int)Math.round(ray(l,eye,dir,figures))));
        }
        return out;
    }

    /** How bright one ray's first surface is, 0 to 255. */
    static double ray(ServerLevel l,Vec3 eye,Vec3 dir,List<Figure> figures){
        var end=eye.add(dir.scale(REACH));
        BlockHitResult hit=l.clip(new ClipContext(eye,end,ClipContext.Block.VISUAL,ClipContext.Fluid.NONE,net.minecraft.world.phys.shapes.CollisionContext.empty()));
        double blockDistance=hit.getType()==HitResult.Type.MISS?Double.MAX_VALUE:hit.getLocation().distanceTo(eye);
        // A figure nearer than the wall behind it is a dark shape, a little lighter at its edges where the light catches it.
        for(var f:figures){
            for(var part:parts(f)){
                var at=part.clip(eye,end);
                if(at.isPresent()&&at.get().distanceTo(eye)<blockDistance){
                    var p=at.get();double edge=Math.min(Math.min(p.x-part.minX,part.maxX-p.x),Math.min(p.z-part.minZ,part.maxZ-p.z));
                    return edge<.04?38:14;
                }
            }
        }
        if(hit.getType()==HitResult.Type.MISS)return 0;
        BlockPos pos=hit.getBlockPos();BlockState state=l.getBlockState(pos);
        int colour=state.getMapColor(l,pos)==MapColor.NONE?0x808080:state.getMapColor(l,pos).col;
        double albedo=(.299*((colour>>16)&255)+.587*((colour>>8)&255)+.114*(colour&255))/255.0;
        Direction face=hit.getDirection();var normal=new Vec3(face.getStepX(),face.getStepY(),face.getStepZ());
        double facing=Math.abs(normal.dot(dir));
        int light=l.getMaxLocalRawBrightness(pos.relative(face));
        double lit=.18+.82*light/15.0;
        double fade=1/(1+blockDistance*blockDistance/600);
        return 255*albedo*lit*(.55+.45*facing)*fade;
    }

    /** A figure's body and head, as boxes. */
    private static AABB[] parts(Figure f){
        var p=f.feet();double h=f.height();
        if(h<1){return new AABB[]{new AABB(p.x-.45,p.y,p.z-.45,p.x+.45,p.y+h,p.z+.45)};}
        double head=.5*Math.min(1,h/1.8);
        return new AABB[]{new AABB(p.x-.3,p.y,p.z-.3,p.x+.3,p.y+h-head,p.z+.3),new AABB(p.x-.25,p.y+h-head,p.z-.25,p.x+.25,p.y+h,p.z+.25)};
    }

    /** A picture of someone from behind, from as far back as the room allows (up to four blocks), at their own eye height. */
    public static byte[] fromBehind(ServerLevel l,net.minecraft.server.level.ServerPlayer p,int w,int h){
        var eye=p.getEyePosition();var look=p.getLookAngle();var back=new Vec3(-look.x,0,-look.z);
        if(back.lengthSqr()<1e-4)back=new Vec3(0,0,1);back=back.normalize();
        var hit=l.clip(new ClipContext(eye,eye.add(back.scale(4.5)),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p));
        double room=hit.getType()==HitResult.Type.MISS?4:Math.max(1.2,hit.getLocation().distanceTo(eye)-.6);
        var lens=eye.add(back.scale(room)).add(0,.15,0);
        return shoot(l,lens,eye.add(look.scale(6)),List.of(new Figure(p.position(),p.getBbHeight())),w,h,62);
    }
}

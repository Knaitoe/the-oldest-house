package io.github.knaitoe.theoldesthouse.client;

import java.util.EnumSet;
import java.util.Set;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.core.Direction;

/** A shortened limb keeps its original side UVs; joint caps must not sample the original hand/sole. */
final class SegmentedSkin {
    private SegmentedSkin(){}
    static CubeListBuilder limb(int u,int v,int offset,float x,float y,float z,int w,int h,int d,boolean mirror,boolean arm){
        var faces=EnumSet.of(Direction.NORTH,Direction.SOUTH,Direction.EAST,Direction.WEST);
        // Native model Y points down: DOWN is the cube's minimum-Y surface.
        if(offset==0)faces.add(Direction.DOWN);
        var cubes=CubeListBuilder.create().texOffs(u,v+offset).mirror(mirror).addBox(x,y,z,w,h,d,faces);
        // Two unused head-net corners contain deliberately painted joint swatches.
        int swatch=arm?-4:20;
        if(offset>0){
            boolean wrist=arm&&offset>=10;
            cubes.texOffs(wrist?u+w:swatch,wrist?v:0).addBox(x,y,z,w,0,d,Set.of(Direction.DOWN));
        }
        boolean extremity=offset+h>=12||arm&&offset+h>=10;
        cubes.texOffs(extremity?u:swatch,extremity?v:0).addBox(x,y+h,z,w,0,d,Set.of(Direction.UP));
        return cubes;
    }
}

package io.github.knaitoe.theoldesthouse.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

/** Shared opt-in review viewpoints. Ordinary play never constrains a reader's camera. */
public final class GoatmanWindowViews {
    private GoatmanWindowViews(){}
    public static double window(boolean middle){return middle?-66.5:-58.5;}
    public static Vec3 feet(BlockPos base,String role,boolean middle){return new Vec3(base.getX()-3.2,base.getY()+1,base.getZ()+window(middle)+(role.equals("A")?1.3:-.9));}
}

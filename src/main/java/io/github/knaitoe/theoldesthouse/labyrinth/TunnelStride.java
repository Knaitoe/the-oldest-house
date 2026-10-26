package io.github.knaitoe.theoldesthouse.labyrinth;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.world.phys.Vec3;
/** Uneven, continuous native footfalls for the original private injured woman. */
public final class TunnelStride {
    private record Goal(Vec3 at,long until){}
    private static final Map<LiteraryActor,Goal> GOALS=new WeakHashMap<>();
    private TunnelStride(){}
    public static void seek(LiteraryActor a,Vec3 at){GOALS.put(a,new Goal(at,a.level().getGameTime()+10));}
    public static void tick(LiteraryActor a){
        if(a.role()!=LiteraryActor.UNCANNY_GIANT)return;
        var goal=GOALS.get(a);if(a.phase()!=2||goal==null||a.level().getGameTime()>goal.until){a.setDeltaMovement(Vec3.ZERO);return;}
        double step=.075+.065*(.5+.5*Math.sin(a.tickCount*.17));SceneFootpaths.walk(a,goal.at,step);
    }
}

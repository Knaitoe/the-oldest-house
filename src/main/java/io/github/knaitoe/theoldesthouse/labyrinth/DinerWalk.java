package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

/** The original human waitress walks every native tick; the story only requests destinations. */
public final class DinerWalk {
    private static final Map<LiteraryActor,Move> MOVES=new WeakHashMap<>();
    private static final class Move {Vec3 goal,look;long until;double speed=.13;}
    private DinerWalk(){}
    public static boolean toward(LiteraryActor a,Vec3 at){
        var m=MOVES.computeIfAbsent(a,k->new Move());m.goal=at;m.look=null;m.until=a.level().getGameTime()+10;
        return a.position().subtract(at).horizontalDistanceSqr()<.025&&Math.abs(a.getY()-at.y)<.7;
    }
    public static void look(LiteraryActor a,Vec3 at){var m=MOVES.computeIfAbsent(a,k->new Move());m.goal=null;m.look=at;}
    public static void follow(LiteraryActor a,Vec3 at,double speed){var m=MOVES.computeIfAbsent(a,k->new Move());m.goal=at;m.look=null;m.speed=speed;m.until=a.level().getGameTime()+10;}
    public static void register(LiteraryActor a,BlockPos b,boolean instant){
        var target=b.offset(UncannyRooms.source(LabyrinthPlace.SECOND_WAITRESS)).getCenter();look(a,target);
        if(instant)SceneFootpaths.face(a,target,180);
    }
    public static void tick(LiteraryActor a){
        if(!(a.level() instanceof ServerLevel l)||(a.role()!=LiteraryActor.UNCANNY_WAITRESS&&a.role()!=LiteraryActor.UNCANNY_FALSE_WAITRESS)||!a.scene().equals(LabyrinthPlace.SECOND_WAITRESS.id()))return;
        var m=MOVES.get(a);
        if(m!=null&&m.goal!=null&&l.getGameTime()<=m.until){SceneFootpaths.walk(a,m.goal,m.speed);return;}
        if(m!=null&&m.look!=null){SceneFootpaths.face(a,m.look,10);return;}
        if(a.role()==LiteraryActor.UNCANNY_FALSE_WAITRESS)return;
        var origin=HouseSavedData.get(l.getServer()).houseOrigin();if(origin!=null)register(a,LabyrinthPlaces.base(origin,LabyrinthPlace.SECOND_WAITRESS),false);
    }
}

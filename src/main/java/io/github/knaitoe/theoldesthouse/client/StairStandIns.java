package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.knaitoe.theoldesthouse.labyrinth.ClapGhostRegistry;
import io.github.knaitoe.theoldesthouse.labyrinth.ClassicsRegistry;
import io.github.knaitoe.theoldesthouse.labyrinth.DrownedTownRegistry;
import io.github.knaitoe.theoldesthouse.labyrinth.GoatmanRegistry;
import io.github.knaitoe.theoldesthouse.labyrinth.HotelRegistry;
import io.github.knaitoe.theoldesthouse.labyrinth.LiteraryActor;
import io.github.knaitoe.theoldesthouse.labyrinth.LiteraryRegistry;
import io.github.knaitoe.theoldesthouse.labyrinth.Shallows;
import io.github.knaitoe.theoldesthouse.labyrinth.StairStandIn;
import java.util.Map;
import java.util.WeakHashMap;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

/**
 * 0.4.655: one of a reader's people on the great staircase whose body is its own story's creature (labyrinth.StairStandIn,
 * docs/STAIR_STAND_INS_0_4_655.md), drawn by that story's own renderer where the body that stands for it stands, turned as it is.
 *
 * <p>Each figure is given its creature the first time it is drawn: made in the reader's own game and never added to the world, so
 * nothing ticks it, hears it or bumps into it, and it is let go with the figure. It is dressed as its story dresses it standing
 * still, and lit as its own renderer lights it (the girl from the wardrobe by her own light, as she was in that room). The cousin is the
 * one exception: in the trailer's window its renderer lights it at least as much as the trailer's spill would, and on the stair
 * there is no trailer, so it stands in the stair's own dark with only its eyes lit.
 */
public final class StairStandIns {
    private StairStandIns(){}
    private record Made(int phase,Entity body){}
    private static final Map<LiteraryActor,Made> MADE=new WeakHashMap<>();
    private static boolean drawing;
    /** True while a stand-in's creature is being drawn: a creature's renderer keeps what it does only in its own story out of it. */
    public static boolean drawing(){return drawing;}

    /** The creature a figure stands in for: made and dressed the first time it is drawn, and again if its dress changes. */
    static @Nullable Entity creature(LiteraryActor a){
        var made=MADE.get(a);int phase=a.phase();
        if(made!=null&&made.phase()==phase)return made.body();
        var body=make(a.level(),phase);
        if(body==null){MADE.remove(a);return null;}
        MADE.put(a,new Made(phase,body));
        return body;
    }
    /** Made and dressed as its own story dresses it standing still; whether it looks still depends on {@link #still}. */
    static @Nullable Entity make(Level level,int phase){
        int dress=StairStandIn.dress(phase);
        switch(StairStandIn.kind(phase)){
            case StairStandIn.GIRL:return ClapGhostRegistry.GIRL.get().create(level);
            // Out of the water and up on her hands and feet, as she comes for someone.
            case StairStandIn.STACEY:return DrownedTownRegistry.LAKE_WITCH.get().create(level);
            case StairStandIn.BEFORE_THE_LAKE:{var girl=DrownedTownRegistry.LAKE_WITCH.get().create(level);if(girl!=null)girl.memoryPhase(Shallows.STOOD);return girl;}
            case StairStandIn.PREACHER:{var preacher=DrownedTownRegistry.CONGREGANT.get().create(level);if(preacher!=null){preacher.pose(true,false);preacher.settle();}return preacher;}
            // A child's face it wore, its own; out of doors, where its eyes are lit.
            case StairStandIn.COUSIN:{var cousin=GoatmanRegistry.CHILD.get().create(level);if(cousin!=null){cousin.appearance(dress,0,null);cousin.disguise(dress);cousin.indoors(false);}return cousin;}
            case StairStandIn.MOTHER:{var mother=ClassicsRegistry.ACTOR.get().create(level);if(mother!=null)mother.appearance(StairStandIn.SEANCE_MOTHER,false,false);return mother;}
            case StairStandIn.BARTENDER:{var bartender=HotelRegistry.ACTOR.get().create(level);if(bartender!=null)bartender.appearance(StairStandIn.HOTEL_BARTENDER,false);return bartender;}
            // The reader's own, as the ones whose eyes are lit in the field beyond the Last Stop are.
            case StairStandIn.ELK:{var elk=LiteraryRegistry.ELK.get().create(level);var reader=Minecraft.getInstance().player;if(elk!=null&&reader!=null)elk.owner(reader.getUUID());return elk;}
            default:return null;
        }
    }
    /** Whether a creature is drawn without the passing of time: the girl from the wardrobe walks in place by it, and stands here. */
    static boolean still(int phase){return StairStandIn.kind(phase)==StairStandIn.GIRL;}

    /** Draws a figure's creature in its place: called by the cast's renderer for a body in the stand-in's role. */
    public static void render(LiteraryActor a,float partial,PoseStack pose,MultiBufferSource out,int light){
        var body=creature(a);if(body==null)return;
        boolean still=still(a.phase());float time=still?0:partial;
        // Where the figure is, as it is drawn now: the creature has no movement of its own to be drawn between.
        double x=Mth.lerp(partial,a.xo,a.getX()),y=Mth.lerp(partial,a.yo,a.getY()),z=Mth.lerp(partial,a.zo,a.getZ());
        body.setPos(x,y,z);body.xo=body.xOld=x;body.yo=body.yOld=y;body.zo=body.zOld=z;
        float yaw=Mth.rotLerp(partial,a.yRotO,a.getYRot());body.setYRot(yaw);body.yRotO=yaw;body.setXRot(0);body.xRotO=0;
        if(body instanceof LivingEntity living){
            float turned=Mth.rotLerp(partial,a.yBodyRotO,a.yBodyRot),head=Mth.rotLerp(partial,a.yHeadRotO,a.getYHeadRot());
            living.yBodyRot=living.yBodyRotO=turned;living.yHeadRot=living.yHeadRotO=head;
        }
        body.tickCount=still?0:a.tickCount;
        draw(body,yaw,time,pose,out,light);
    }
    private static <T extends Entity> void draw(T body,float yaw,float partial,PoseStack pose,MultiBufferSource out,int light){
        EntityRenderer<? super T> renderer=Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(body);
        // A shape on the way down is drawn pale and whole, whatever its renderer would light it by (StairShapes).
        int lit=StairShapes.drawing()?light:renderer.getPackedLightCoords(body,partial);
        drawing=true;
        try{renderer.render(body,yaw,partial,pose,out,lit);}
        finally{drawing=false;}
    }
}

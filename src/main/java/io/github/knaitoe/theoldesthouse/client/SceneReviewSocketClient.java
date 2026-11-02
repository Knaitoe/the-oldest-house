package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.gametest.LiveExpeditionProof;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.nio.file.*;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

/**
 * 0.4.240: the two clients of the pool and flat review (SceneReviewSocketProof). Native clicks, keys and movement; the camera turned to
 * whoever the scene is about; a screenshot at each moment the owner asked to see.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class SceneReviewSocketClient {
    private SceneReviewSocketClient(){}
    private static int last=-1,route,route2,clicks,shots,clicked=-100,since,mark=-1000;private static boolean flag,flag2;
    @Nullable private static String shot;
    private static final int GROUND=VacantSite.GROUND;
    private static void stop(Minecraft mc){mc.options.keyUp.setDown(false);mc.options.keyShift.setDown(false);mc.options.keyUse.setDown(false);mc.options.keyJump.setDown(false);mc.options.keySprint.setDown(false);}
    /** Toward a point, one axis at a time, as the other proofs walk. */
    private static boolean walk(Minecraft mc,Vec3 p,double tolerance){
        double dx=p.x-mc.player.getX(),dz=p.z-mc.player.getZ();if(Math.hypot(dx,dz)<tolerance){mc.options.keyUp.setDown(false);return true;}
        // Along one axis at a time, but a small sideways error (a door's open leaf narrows its gap) is put right first.
        if(Math.abs(dx)>.15&&Math.abs(dz)>.06&&Math.abs(dz)<.6)dx=0;else if(Math.abs(dx)>.15)dz=0;else dx=0;mc.player.setYRot((float)Math.toDegrees(Math.atan2(-dx,dz)));mc.player.setXRot(0);mc.options.keyUp.setDown(true);return false;
    }
    private static Vec3 at(BlockPos b,double x,double y,double z){return new Vec3(b.getX()+x,b.getY()+y,b.getZ()+z);}
    private static void look(Minecraft mc,Vec3 at){var eye=mc.player.getEyePosition();var d=at.subtract(eye);
        mc.player.setYRot((float)Math.toDegrees(Math.atan2(-d.x,d.z)));mc.player.setXRot((float)-Math.toDegrees(Math.atan2(d.y,Math.hypot(d.x,d.z))));}
    private static void click(Minecraft mc,BlockPos at,int age,int gap){if(age-clicked<gap)return;clicked=age;mc.gameMode.useItemOn(mc.player,InteractionHand.MAIN_HAND,new BlockHitResult(at.getCenter(),Direction.UP,at,false));}
    private static void shoot(String name){shot=name;shots++;}
    @Nullable private static LiteraryActor actor(Minecraft mc,int phase){
        LiteraryActor best=null;double d=Double.MAX_VALUE;
        for(var e:mc.level.entitiesForRendering())if(e instanceof LiteraryActor a&&a.phase()==phase&&!a.isInvisible()){double dd=a.distanceToSqr(mc.player);if(dd<d){d=dd;best=a;}}
        return best;
    }
    @Nullable private static Player other(Minecraft mc){for(var p:mc.level.players())if(p!=mc.player)return p;return null;}
    private static boolean carrying(Minecraft mc){return mc.player.getMainHandItem().is(VacantRegistry.PIECE.get());}

    public static boolean ready(Minecraft mc,int step,int age,String role,BlockPos b){
        if(last!=step){last=step;route=0;route2=0;clicks=0;shots=0;clicked=-100;since=0;mark=-1000;flag=false;flag2=false;stop(mc);}
        if(age%40==0)TheOldestHouse.LOGGER.info("LIVE SCENE REVIEW CLIENT {} step={} route={} clicks={} shots={} body={} ground={} carrying={} screen={}",role,step,route,clicks,shots,mc.player.position(),mc.player.onGround(),carrying(mc),mc.screen==null?"none":mc.screen.getClass().getSimpleName());
        if(mc.screen!=null&&!(mc.screen instanceof net.minecraft.client.gui.screens.ChatScreen)){mc.player.closeContainer();return false;}
        boolean a=role.equals("A");
        switch(step){
            case 63->{
                if(a){
                    // At the strap: the crank turned with real clicks, a little over the haul gap apart; one look out over the water partway.
                    stop(mc);var reel=b.offset(SwimmerSite.REEL_SOUTH);
                    if(age<120){look(mc,reel.getCenter());return false;}
                    if(age-clicked>=LastSwimmer.HAUL_GAP+3){click(mc,reel,age,LastSwimmer.HAUL_GAP+3);clicks++;}
                    if(clicks==5&&!flag){flag=true;mark=age;}
                    if(flag&&age-mark<6)look(mc,at(b,-2,0,-24));else look(mc,reel.getCenter());
                    if(flag&&age-mark==3)shoot("A-pool-hauling");
                    return clicks>=SwimmerSite.COLUMNS;
                }
                stop(mc);var swimmer=actor(mc,LastSwimmer.SWIMMER);
                if(swimmer!=null)look(mc,swimmer.position().add(0,.3,0));else look(mc,at(b,0,-1,-25));
                if(age==120)shoot("B-pool-swimmer");
                return age>=140;
            }
            case 64->{
                // The attendant, watched from where each reader stands, as they go round to the fire door and hold it.
                stop(mc);var keeper=actor(mc,LastSwimmer.ATTENDANT);
                if(keeper!=null)look(mc,keeper.position().add(0,1.4,0));
                if(age>20&&age%60==0&&shots<6)shoot(role+"-pool-attendant-"+(shots+1));
                return shots>=6&&!flagShotPending();
            }
            case 65->{
                if(!a){
                    // Downstairs, watching the stair for the reader coming down with the dresser.
                    // The stair runs between walls, so from the hall it shows only through its foot: watch the opening for the dresser.
                    stop(mc);var o=other(mc);look(mc,at(b,-4,GROUND+1.2,-5.5));
                    if(!flag&&o!=null&&o.getY()<b.getY()+GROUND+1.2&&o.getX()<b.getX()-2.5&&o.getZ()<b.getZ()-4.5){flag=true;shoot("B-flat-stair-watch");}
                    return flag||age>900;
                }
                if(age<60){stop(mc);return false;}
                return carry(mc,b,age,new BlockPos(-2,0,-9),"dresser");
            }
            case 66->{
                if(!a){
                    // Up the stair and into the living room, to watch the tenant and the last chair.
                    Vec3[] up={at(b,-3.5,GROUND,-7.5),at(b,-3.5,0,-.5),at(b,.5,0,-.5),at(b,.5,0,-6.5),at(b,1.5,0,-6.5)};
                    if(route<up.length){if(walk(mc,up[route],.3))route++;return false;}
                    stop(mc);var t=actor(mc,VacantPossession.TENANT);var chair=at(b,6.5,.8,-8.5);
                    look(mc,t!=null?t.position().add(0,1.2,0):chair);
                    boolean seated=t!=null&&flat(t.position(),at(b,6.5,0,-8.5))<.09,standing=t!=null&&flat(t.position(),at(b,6.5,0,-9.5))<.25;
                    if(!flag&&seated&&since++>10){flag=true;shoot("B-flat-tenant-seated");}
                    if(flag&&!flag2&&standing){flag2=true;mark=age;}
                    if(flag2&&age-mark==15)shoot("B-flat-tenant-stands");
                    if(flag2&&t==null&&age-mark>40&&shots<3){look(mc,chair);shoot("B-flat-empty");}
                    return shots>=3||age>2000;
                }
                return chairDown(mc,b,age);
            }
            default->{return false;}
        }
    }
    private static boolean flagShotPending(){return shot!=null;}

    /** The dresser lifted, carried out of the flat, along the landing, down the stair, along the hall and out to the van. */
    private static boolean carry(Minecraft mc,BlockPos b,int age,BlockPos piece,String name){
        var path=new Vec3[]{at(b,.5,0,-8.5),at(b,.5,0,-.5),at(b,-3.5,0,-.5),at(b,-3.5,GROUND,-6.5),at(b,6.5,GROUND,-6.4),at(b,9.5,GROUND,-6.4),at(b,10.4,GROUND,-6.4)};
        if(route==0){stop(mc);look(mc,b.offset(piece).getCenter());click(mc,b.offset(piece),age,20);if(carrying(mc))route=1;return false;}
        int leg=route-1;
        if(leg<path.length){
            if(leg==3&&!flag){stop(mc);mc.player.setYRot(180);mc.player.setXRot(35);if(mark<0)mark=age;if(age-mark==4)shoot("A-flat-stair-top");if(age-mark>8){flag=true;mark=-1000;}return false;}
            if(leg==3&&!flag2&&mc.player.getY()<b.getY()-2.4){flag2=true;shoot("A-flat-stair-middle");}
            if(walk(mc,path[leg],.3)){route++;if(leg==3){stop(mc);mc.player.setYRot(-90);mc.player.setXRot(5);mark=age;}}
            if(leg==4&&mark>0&&age-mark<6){mc.options.keyUp.setDown(false);mc.player.setYRot(-90);if(age-mark==3)shoot("A-flat-hall");}
            return false;
        }
        stop(mc);var van=b.offset(12,GROUND-1,-7);look(mc,van.getCenter().add(0,.5,0));
        if(carrying(mc)){click(mc,van,age,20);return false;}
        if(since==0){since=age;}if(age-since==6)shoot("A-flat-van");
        return age-since>12;
    }
    /** Back up to the tenant; the chair, when they stand; down to the van with it; and the window from the street. */
    private static boolean chairDown(Minecraft mc,BlockPos b,int age){
        if(age<260){stop(mc);return false;}
        var up=new Vec3[]{at(b,-3.5,GROUND,-6.5),at(b,-3.5,0,-.5),at(b,.5,0,-.5),at(b,.5,0,-8.5),at(b,4.5,0,-8.5)};
        if(route<up.length){if(walk(mc,up[route],.3))route++;return false;}
        var t=actor(mc,VacantPossession.TENANT);var chair=b.offset(VacantSite.CHAIR_PIECE.at());
        if(route==up.length){stop(mc);look(mc,t!=null?t.position().add(0,1.5,0):chair.getCenter());if(mark<0)mark=age;if(age-mark==25){shoot("A-flat-tenant-stands");route++;}return false;}
        if(route==up.length+1){stop(mc);look(mc,chair.getCenter());click(mc,chair,age,20);if(carrying(mc)){route++;mark=age;}return false;}
        if(route==up.length+2){stop(mc);look(mc,t!=null?t.position().add(0,1.5,0):chair.getCenter());if(age-mark==5){shoot("A-flat-chair-lifted");route++;}return false;}
        var down=new Vec3[]{at(b,.5,0,-8.5),at(b,.5,0,-.5),at(b,-3.5,0,-.5),at(b,-3.5,GROUND,-6.5),at(b,6.5,GROUND,-6.4),at(b,9.5,GROUND,-6.4),at(b,10.4,GROUND,-6.4)};
        int leg=route-(up.length+3);
        if(leg<down.length){if(leg==3&&!flag2&&mc.player.getY()<b.getY()-2.4){flag2=true;shoot("A-flat-chair-stair");}if(walk(mc,down[leg],.3))route++;return false;}
        stop(mc);var van=b.offset(12,GROUND-1,-7);
        if(carrying(mc)){look(mc,van.getCenter().add(0,.5,0));click(mc,van,age,20);mark=age;return false;}
        // Out from behind the van and across the street, to look back up at the living room's window past the van's end.
        var across=new Vec3[]{at(b,10.4,GROUND,-11.5),at(b,16.2,GROUND,-11.5)};
        if(!flag){if(walk(mc,across[route2],.3)){route2++;if(route2>=across.length){flag=true;mark=age;}}return false;}
        stop(mc);look(mc,at(b,8,1.5,-10.5));if(age-mark==30)shoot("A-flat-window-dark");
        return age-mark>40;
    }

    private static double flat(Vec3 a,Vec3 b){double dx=a.x-b.x,dz=a.z-b.z;return dx*dx+dz*dz;}

    @SubscribeEvent public static void frame(RenderFrameEvent.Post e)throws Exception{
        if(!LiveExpeditionProof.enabled()||shot==null)return;var mc=Minecraft.getInstance();Files.createDirectories(LiveExpeditionProof.folder());
        try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(LiveExpeditionProof.folder().resolve(shot+".png"));}
        Files.writeString(LiveExpeditionProof.folder().resolve(shot+".txt"),"Actual connected client in the production scene, with its actors and the world's light. Staging is described in scene-review-passed.txt.\n");shot=null;
    }
}

package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseSitting;
import io.github.knaitoe.theoldesthouse.house.SeatEntity;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

/** The original shared waitress walks a reader to an actual booth before service starts. */
public final class UncannyDiner {
    private static final LabyrinthPlace PLACE=LabyrinthPlace.SECOND_WAITRESS;
    public static final BlockPos SEAT=new BlockPos(5,0,-18);
    private static final List<Vec3> WALK=List.of(new Vec3(-7.5,0,-5.5),new Vec3(-3.5,0,-4.5),new Vec3(3.5,0,-5.5),new Vec3(3.5,0,-18.5));
    private UncannyDiner(){}
    public static LiteraryActor waitress(ServerPlayer p,BlockPos b){
        var w=UncannyStories.world(p.serverLevel(),b,PLACE);
        return w.hasUUID("Waitress")&&p.serverLevel().getEntity(w.getUUID("Waitress")) instanceof LiteraryActor a?a:null;
    }
    public static void greet(ServerPlayer p,BlockPos b,CompoundTag own){
        if(own.getBoolean("DinerGreeted127")||own.getBoolean("Completed")||UncannyStories.failed(own))return;
        if(waitress(p,b)!=null){own.putBoolean("DinerGreeted127",true);p.displayClientMessage(Component.literal("“Come in out of the rain. Just you?”"),false);}
    }
    private static boolean busy(ServerPlayer p,CompoundTag w){
        if(!w.hasUUID("Host127")||w.getUUID("Host127").equals(p.getUUID()))return false;
        var peer=p.server.getPlayerList().getPlayer(w.getUUID("Host127"));
        if(peer==null||!LiteraryVignettes.inside(peer,PLACE))return false;
        var theirs=LiteraryVignettes.personal(LabyrinthData.get(p.server),peer.getUUID(),PLACE);
        return !theirs.getBoolean("DinerHosted127")&&!theirs.getBoolean("Completed")&&!UncannyStories.failed(theirs);
    }
    public static void talk(ServerPlayer p,LiteraryActor a,BlockPos b,CompoundTag own){
        var fake=UncannyStories.actor(p,own,"CastFake");
        if(fake!=null&&fake.phase()>=2){
            own.putBoolean("AskedRota",true);
            if(own.getBoolean("MealServed")){own.putBoolean("Refused",true);p.displayClientMessage(Component.literal("“Leave it on the table. Staff door's beside the till.”"),false);}
            else p.displayClientMessage(Component.literal("“Keep your coffee. I'll see to the table.”"),false);
            return;
        }
        if(own.getBoolean("DinerHosted127")){p.displayClientMessage(Component.literal("“I'll be right with you.”"),false);return;}
        var w=UncannyStories.world(p.serverLevel(),b,PLACE);
        if(busy(p,w)){p.displayClientMessage(Component.literal("“Be with you in a moment.”"),false);return;}
        w.putUUID("Host127",p.getUUID());UncannyStories.world(p.serverLevel(),b,PLACE,w);
        if(!own.getBoolean("DinerSeating127")){own.putBoolean("DinerSeating127",true);p.displayClientMessage(Component.literal("“Booth by the window. This way.”"),false);}
        if(own.getInt("DinerHostLeg127")>=WALK.size()&&!own.getBoolean("DinerSeated127"))HouseSitting.sit(p,b.offset(SEAT));
    }
    public static boolean host(ServerPlayer p,BlockPos b,CompoundTag own){
        greet(p,b,own);var a=waitress(p,b);if(a==null)return false;
        // Already served old saves continue with their original plate and body.
        if(own.getBoolean("MealReleased")&&!own.getBoolean("DinerHosted127")){
            own.putBoolean("DinerHosted127",true);own.putInt("DinerHostedAt127",Math.max(0,own.getInt("UncannyAge")-600));
        }
        if(own.getBoolean("DinerHosted127"))return true;
        var w=UncannyStories.world(p.serverLevel(),b,PLACE);
        if(!w.hasUUID("Host127")||!w.getUUID("Host127").equals(p.getUUID())){
            if(!busy(p,w)){a.setYRot(270);a.setYHeadRot(270);a.yBodyRot=270;}
            return false;
        }
        if(!own.getBoolean("DinerSeating127"))return false;
        int leg=own.getInt("DinerHostLeg127");
        if(leg<WALK.size()){
            if(a.distanceToSqr(p)<14*14&&SceneFootpaths.step(a,WALK.get(leg).add(b.getX(),b.getY(),b.getZ()),.18))own.putInt("DinerHostLeg127",leg+1);
            return false;
        }
        if(!own.getBoolean("DinerSeated127")){
            if(!own.getBoolean("DinerSeatOffered127")){own.putBoolean("DinerSeatOffered127",true);p.displayClientMessage(Component.literal("“Here you are. I'll get the coffee.”"),false);}
            if(!(p.getVehicle() instanceof SeatEntity)||p.distanceToSqr(b.offset(SEAT).getCenter())>9)return false;
            own.putBoolean("DinerSeated127",true);
        }
        int back=own.getInt("DinerReturnLeg127");
        Vec3 target=back<WALK.size()?WALK.get(WALK.size()-1-back):new Vec3(-7.5,0,-8.5);
        if(SceneFootpaths.step(a,target.add(b.getX(),b.getY(),b.getZ()),.18)){
            if(back<WALK.size())own.putInt("DinerReturnLeg127",back+1);
            else{
                own.putBoolean("DinerHosted127",true);own.putInt("DinerHostedAt127",own.getInt("UncannyAge"));w.remove("Host127");
                UncannyStories.world(p.serverLevel(),b,PLACE,w);a.setYRot(270);a.setYHeadRot(270);a.yBodyRot=270;return true;
            }
        }
        return false;
    }
}

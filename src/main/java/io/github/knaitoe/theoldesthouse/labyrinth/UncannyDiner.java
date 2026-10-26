package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseSitting;
import io.github.knaitoe.theoldesthouse.house.SeatEntity;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** One shared original waitress hosts an actual seated order; the second serves its finite plate. */
public final class UncannyDiner {
    private static final LabyrinthPlace PLACE=LabyrinthPlace.SECOND_WAITRESS;
    public static final BlockPos SEAT=new BlockPos(5,0,-18);
    private static final List<Vec3> WALK=List.of(new Vec3(-7.5,0,-5.5),new Vec3(-3.5,0,-4.5),new Vec3(3.5,0,-5.5),new Vec3(3.5,0,-18.5));
    private static final List<Vec3> KITCHEN=List.of(new Vec3(3.5,0,-23.5),new Vec3(-7.5,0,-23.5),new Vec3(-7.5,0,-28.5));
    private static final List<Vec3> RETURN=List.of(new Vec3(-7.5,0,-23.5),new Vec3(-3.5,0,-23.5),new Vec3(-3.5,0,-5.5),new Vec3(-7.5,0,-8.5));
    private static final String[] PRESSURE={"Aren't you going to eat?","What's wrong, honey?","What's wrong?","What's wrong?","Eat up.","Eat eat eat."};
    private UncannyDiner(){}
    public static boolean seated(ServerPlayer p,BlockPos b){return p.getVehicle() instanceof SeatEntity&&p.distanceToSqr(b.offset(SEAT).getCenter())<9;}
    public static LiteraryActor waitress(ServerPlayer p,BlockPos b){var w=UncannyStories.world(p.serverLevel(),b,PLACE);return w.hasUUID("Waitress")&&p.serverLevel().getEntity(w.getUUID("Waitress")) instanceof LiteraryActor a?a:null;}
    public static void greet(ServerPlayer p,BlockPos b,CompoundTag own){
        if(own.getBoolean("DinerGreeted127")||own.getBoolean("Completed")||UncannyStories.failed(own))return;
        if(waitress(p,b)!=null){own.putBoolean("DinerGreeted127",true);p.displayClientMessage(Component.literal("“Come in out of the rain. Just you?”"),false);}
    }
    private static boolean busy(ServerPlayer p,CompoundTag w){
        if(!w.hasUUID("Host127")||w.getUUID("Host127").equals(p.getUUID()))return false;
        var peer=p.server.getPlayerList().getPlayer(w.getUUID("Host127"));if(peer==null||!LiteraryVignettes.inside(peer,PLACE))return false;
        var theirs=LiteraryVignettes.personal(LabyrinthData.get(p.server),peer.getUUID(),PLACE);
        return !theirs.getBoolean("DinerRealReturned130")&&!theirs.getBoolean("Completed")&&!UncannyStories.failed(theirs);
    }
    public static void talk(ServerPlayer p,LiteraryActor a,BlockPos b,CompoundTag own){
        if(b==null)return;var fake=UncannyStories.actor(p,own,"CastFake");
        if(fake!=null&&fake.phase()>=2){own.putBoolean("AskedRota",true);
            if(own.getBoolean("MealServed")){own.putBoolean("Refused",true);p.displayClientMessage(Component.literal("“Leave it on the table. Staff door's beside the till.”"),false);}
            else p.displayClientMessage(Component.literal("“Keep your coffee. I'll see to the table.”"),false);return;
        }
        if(own.getBoolean("DinerHosted127")){p.displayClientMessage(Component.literal("“I'll be right with you.”"),false);return;}
        var w=UncannyStories.world(p.serverLevel(),b,PLACE);if(busy(p,w)){p.displayClientMessage(Component.literal("“Be with you in a moment.”"),false);return;}
        w.putUUID("Host127",p.getUUID());UncannyStories.world(p.serverLevel(),b,PLACE,w);
        if(!own.getBoolean("DinerSeating127")){own.putBoolean("DinerSeating127",true);p.displayClientMessage(Component.literal("“Booth by the window. This way.”"),false);}
        if(own.getInt("DinerHostLeg127")>=WALK.size()&&!seated(p,b))HouseSitting.sit(p,b.offset(SEAT));
        if(own.getBoolean("DinerMenuGiven130")&&!own.contains("Breakfast130")&&seated(p,b))for(var item:p.getInventory().items)if(item.is(UncannyRegistry.MENU.get())){UncannyBreakfast.open(p,item);break;}
    }
    private static Vec3 point(BlockPos b,Vec3 local){return local.add(b.getX(),b.getY(),b.getZ());}
    private static void faceBooth(LiteraryActor a,BlockPos b){SceneFootpaths.face(a,Vec3.atCenterOf(b.offset(SEAT)),18);}
    private static void kitchenDoor(ServerPlayer p,LiteraryActor a,BlockPos b,boolean open){
        var at=b.offset(-8,0,-25);var l=p.serverLevel();var s=l.getBlockState(at);if(!(s.getBlock() instanceof DoorBlock door))return;
        if(!open&&!l.getEntitiesOfClass(LivingEntity.class,new AABB(at).expandTowards(0,2,0).inflate(.5),e->e.isAlive()).isEmpty())return;
        door.setOpen(a,l,s,at,open);
    }
    private static void handMenu(ServerPlayer p,BlockPos b,CompoundTag own){
        if(own.getBoolean("DinerMenuGiven130"))return;var menu=UncannyBreakfast.menu(p);own.putBoolean("DinerMenuGiven130",true);
        p.displayClientMessage(Component.literal("“Take your time. What can I get you?”"),false);LiteraryVignettes.give(p,menu.copy());
        LiteraryVignettes.save(LabyrinthData.get(p.server),p.getUUID(),PLACE,own);UncannyBreakfast.open(p,menu);
    }
    public static boolean host(ServerPlayer p,BlockPos b,CompoundTag own){
        greet(p,b,own);var a=waitress(p,b);if(a==null)return false;var w=UncannyStories.world(p.serverLevel(),b,PLACE);
        if(own.getBoolean("MealReleased")&&!own.getBoolean("DinerFlow130")){
            own.putBoolean("DinerHosted127",true);own.putBoolean("DinerRealReturned130",true);if(!own.contains("DinerHostedAt127"))own.putInt("DinerHostedAt127",Math.max(0,own.getInt("UncannyAge")-600));return true;
        }
        if(!own.getBoolean("DinerFlow130")){
            own.putBoolean("DinerFlow130",true);
            if(own.getBoolean("DinerHosted127")){
                own.remove("DinerHosted127");own.remove("DinerSeated127");own.putBoolean("DinerSeating127",true);own.putInt("DinerHostLeg127",0);
                if(!busy(p,w)){w.putUUID("Host127",p.getUUID());UncannyStories.world(p.serverLevel(),b,PLACE,w);}
            }
        }
        if(own.getBoolean("DinerHosted127")){
            if(w.hasUUID("Host127")&&w.getUUID("Host127").equals(p.getUUID())&&own.getInt("UncannyAge")-own.getInt("DinerHostedAt127")>=500){
                kitchenDoor(p,a,b,true);int leg=own.getInt("DinerReturnLeg130");
                if(leg<RETURN.size()&&SceneFootpaths.step(a,point(b,RETURN.get(leg)),.18))own.putInt("DinerReturnLeg130",++leg);
                if(leg>=RETURN.size()){own.putBoolean("DinerRealReturned130",true);w.remove("Host127");UncannyStories.world(p.serverLevel(),b,PLACE,w);SceneFootpaths.face(a,point(b,new Vec3(-9,0,-8.5)),15);}
            }return true;
        }
        if(!w.hasUUID("Host127")||!w.getUUID("Host127").equals(p.getUUID())){if(!busy(p,w))SceneFootpaths.face(a,point(b,new Vec3(-9,0,-8.5)),15);return false;}
        if(!own.getBoolean("DinerSeating127"))return false;
        int leg=own.getInt("DinerHostLeg127");if(leg<WALK.size()){
            if(a.distanceToSqr(p)<14*14&&SceneFootpaths.step(a,point(b,WALK.get(leg)),.18))own.putInt("DinerHostLeg127",leg+1);return false;
        }
        if(!own.contains("Breakfast130")){
            faceBooth(a,b);
            if(!own.getBoolean("DinerSeatOffered127")){own.putBoolean("DinerSeatOffered127",true);p.displayClientMessage(Component.literal("“Here you are.”"),false);}
            if(!seated(p,b))return false;own.putBoolean("DinerSeated127",true);
            var look=Vec3.atCenterOf(b.offset(SEAT)).subtract(a.position());float wanted=(float)Math.toDegrees(Math.atan2(-look.x,look.z));
            if(Math.abs(net.minecraft.util.Mth.wrapDegrees(a.getYRot()-wanted))<8)handMenu(p,b,own);return false;
        }
        kitchenDoor(p,a,b,true);int back=own.getInt("DinerKitchenLeg130");
        if(back<KITCHEN.size()&&SceneFootpaths.step(a,point(b,KITCHEN.get(back)),.18))own.putInt("DinerKitchenLeg130",++back);
        if(back>=KITCHEN.size()){
            kitchenDoor(p,a,b,false);own.putBoolean("DinerHosted127",true);own.putInt("DinerHostedAt127",own.getInt("UncannyAge"));SceneFootpaths.face(a,point(b,new Vec3(-8.5,0,-28.5)),18);return true;
        }return false;
    }
    /** Pressure is tied to the actual booth and its one private served plate, not a room-wide text loop. */
    public static boolean pressure(ServerPlayer p,LiteraryActor fake,BlockPos b,CompoundTag own){
        if(!seated(p,b)){own.remove("BoothPressureNext130");return false;}
        own.remove("LeftBoothAt130");SceneFootpaths.face(fake,p.position(),12);
        long age=own.getLong("BoothPressureClock130")+5;own.putLong("BoothPressureClock130",age);if(!own.contains("BoothPressureNext130")){own.putLong("BoothPressureNext130",age+160);return true;}
        if(age<own.getLong("BoothPressureNext130"))return true;
        int line=own.getInt("BoothPressureLine130");String text=PRESSURE[line<PRESSURE.length?line:2+(line-PRESSURE.length)%4];
        fake.say(text);p.displayClientMessage(Component.literal("“"+text+"”"),false);own.putInt("BoothPressureLine130",line>=100?6:line+1);own.putLong("BoothPressureNext130",age+160+(line%3)*40);return true;
    }
}

package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.phys.Vec3;

/** A private gift that Johnny tries to collect on at a later meeting. */
final class TruantHospitality {
    private TruantHospitality(){}

    private static final int DEPARTURE_WINDOW=600;
    private static final int GRIP_TICKS=30;
    private static final int GRIP_EFFECT_TICKS=3,GRIP_AMPLIFIER=5;
    private static final Vec3[] TO_DOOR={new Vec3(-4.5,0,-8.5),new Vec3(-9.5,0,-8.5),new Vec3(-9.5,0,-14.9)};
    private enum Beat{BREAD_QUESTION,BREAD_WALK,COFFEE_QUESTION,CUP,REACH,GRIP,DONE}
    record Departure(UUID companion,long route,Vec3 landing,long tick,BlockPos door){}
    static final class Encounter {
        final UUID reader;
        final @Nullable Departure departure;
        private Beat beat;
        private int ticks,leg;
        private Vec3 cupAt;
        private @Nullable MobEffectInstance gripEffect;
        Encounter(UUID reader,Beat beat,@Nullable Departure departure){this.reader=reader;this.beat=beat;this.departure=departure;}
    }

    static boolean farDoor(LabyrinthData.Door door){
        return door.dimension.equals(HouseDimensions.INTERIOR)
                &&door.id.equals(TruantPlaces.LAMP.id()+"/far");
    }

    static void departed(ServerPlayer p,LabyrinthData.Door door,long route){
        var sc=Truant.scene;
        if(sc==null||sc.place!=TruantPlaces.LAMP||!Truant.reader(p)||!Truant.veteran(p)||!farDoor(door))return;
        sc.mealDeparture=new Departure(p.getUUID(),route,p.position(),p.server.overworld().getGameTime(),door.lower);
    }

    static boolean offer(ServerPlayer p,LiteraryActor a){
        var sc=Truant.scene;
        if(sc==null||sc.place!=TruantPlaces.LAMP||sc.lamp!=3||sc.turn!=Truant.Turn.CLAIM||sc.hospitality!=null
                ||!p.getUUID().equals(sc.target))return false;
        var readers=readers(p.serverLevel(),sc);
        if(Truant.gated(readers))return false;
        var own=Truant.record(p.server,p.getUUID());
        var departure=sc.mealDeparture;
        var companion=departure==null?null:companion(p,departure);
        if(eligible(own,"Bread")&&departure!=null&&!p.getUUID().equals(departure.companion())
                &&readers.stream().anyMatch(q->q!=p&&q.distanceToSqr(a)<64)
                &&LabyrinthData.get(p.server).nodeKey(p.getUUID(),sc.place)==departure.route()
                &&companion!=null){
            sc.hospitality=new Encounter(p.getUUID(),Beat.BREAD_QUESTION,departure);
            consumed(p,own,"Bread");
            var ask="Which door did "+companion.getGameProfile().getName()+" take?";
            Truant.line(p,a,Truant.who(own),ask);
            heardByOthers(a,readers,p,ask);
            Talk.ask(p,Talk.reply("I'm not telling you.",q->refuseBread(q,a,sc)),
                    Talk.reply("You gave me bread. That's all.",q->refuseBread(q,a,sc)));
            return true;
        }
        if(!eligible(own,"Coffee"))return false;
        sc.hospitality=new Encounter(p.getUUID(),Beat.COFFEE_QUESTION,null);
        consumed(p,own,"Coffee");
        Truant.line(p,a,Truant.who(own),"I poured one for you. The cap's still hot.");
        Talk.ask(p,Talk.act("Take the cup.",q->takeCup(q,a,sc)),
                Talk.reply("I'm going.",q->{finish(sc.hospitality,a);Talk.end(q);}));
        return true;
    }

    private static int gift(CompoundTag own,String kind){
        if(own.contains(kind+"GiftAt"))return own.getInt(kind+"GiftAt");
        return own.getString("LastRefreshment").equalsIgnoreCase(kind)?-1:0;
    }

    private static boolean eligible(CompoundTag own,String kind){
        int at=gift(own,kind);
        return at!=0&&at<Truant.meeting(own)&&at!=own.getInt(kind+"CollectedAt");
    }

    private static void consumed(ServerPlayer p,CompoundTag own,String kind){
        own.putInt(kind+"CollectedAt",gift(own,kind));
        Truant.record(p.server,p.getUUID(),own);
    }

    private static List<ServerPlayer> readers(ServerLevel level,Truant.Scene sc){
        var origin=HouseSavedData.get(level.getServer()).houseOrigin();
        if(origin==null)return List.of();
        return level.players().stream().filter(p->Truant.reader(p)&&Truant.placeOf(p,origin)==sc.place).toList();
    }

    private static @Nullable ServerPlayer companion(ServerPlayer p,Departure route){
        if(p.server.overworld().getGameTime()-route.tick()>DEPARTURE_WINDOW)return null;
        var q=p.server.getPlayerList().getPlayer(route.companion());
        return q!=null&&q.level()==p.level()&&Truant.reader(q)&&q.distanceToSqr(route.landing())<144?q:null;
    }

    private static void heardByOthers(LiteraryActor a,List<ServerPlayer> readers,ServerPlayer target,String text){
        for(var q:readers)if(q!=target&&q.distanceToSqr(a)<100)
            q.displayClientMessage(Component.literal(text),true);
    }

    private static void refuseBread(ServerPlayer p,LiteraryActor a,Truant.Scene sc){
        var e=sc.hospitality;
        if(e==null||e.beat!=Beat.BREAD_QUESTION)return;
        var readers=readers(p.serverLevel(),sc);
        if(Truant.gated(readers)||e.departure==null||companion(p,e.departure)==null){finish(e,a);Talk.end(p);return;}
        String accusation="When you came to my lamp, did I ask why you were alone?";
        Truant.line(p,a,Truant.who(Truant.record(p.server,p.getUUID())),accusation);
        heardByOthers(a,readers,p,accusation);
        e.beat=Beat.BREAD_WALK;
        Talk.ask(p,Talk.act("Call out to warn them.",q->{
            if(e.beat==Beat.BREAD_WALK){
                var other=companion(q,e.departure);
                if(other!=null)other.displayClientMessage(Component.literal(q.getGameProfile().getName()+" calls from the hall. \"Johnny's coming.\""),true);
            }
            Talk.end(q);
        }),Talk.act("Get in his way.",Talk::close));
    }

    private static void takeCup(ServerPlayer p,LiteraryActor a,Truant.Scene sc){
        var e=sc.hospitality;
        if(e==null||e.beat!=Beat.COFFEE_QUESTION)return;
        if(Truant.gated(readers(p.serverLevel(),sc))){finish(e,a);Talk.end(p);return;}
        e.cupAt=p.position();
        e.beat=Beat.CUP;
        Truant.line(p,a,Truant.who(Truant.record(p.server,p.getUUID())),"Set it down if you need both hands. I'll wait.");
        Talk.ask(p,Talk.act("Set the cup down and go.",q->{reach(e,a,q);Talk.close(q);}),
                Talk.act("Stay a moment.",Talk::close));
    }

    private static void reach(Encounter e,LiteraryActor a,ServerPlayer p){
        if(e.beat!=Beat.CUP)return;
        e.beat=Beat.REACH;e.ticks=0;
        Truant.face(a,p.position());Truant.gesture(a,Truant.REACH);
        Truant.say(a,"You said you'd wait with me.");
    }

    static boolean interrupt(ServerPlayer p,LiteraryActor a,Truant.Scene sc){
        var e=sc.hospitality;
        if(e==null||e.reader.equals(p.getUUID())||p.distanceToSqr(a)>9
                ||e.beat!=Beat.BREAD_WALK&&e.beat!=Beat.REACH&&e.beat!=Beat.GRIP)return false;
        finish(e,a);
        Truant.say(a,"All right. Leave it.");
        return true;
    }

    static void struck(LiteraryActor a,Truant.Scene sc){
        var e=sc.hospitality;
        if(e!=null&&e.beat!=Beat.DONE)finish(e,a);
    }

    static boolean tick(ServerLevel l,BlockPos b,LiteraryActor a,List<ServerPlayer> readers,Truant.Scene sc){
        var e=sc.hospitality;
        if(e==null||e.beat==Beat.DONE)return false;
        var p=l.getServer().getPlayerList().getPlayer(e.reader);
        if(p==null||!readers.contains(p)||Truant.gated(readers)||Truant.withdrawn(p)){
            finish(e,a);
            if(p!=null&&Talk.key(p).equals(Truant.KEY))Talk.close(p);
            return false;
        }
        switch(e.beat){
            case BREAD_QUESTION,COFFEE_QUESTION->{return false;}
            case BREAD_WALK->{
                if(e.departure==null||companion(p,e.departure)==null){finish(e,a);return false;}
                if(++e.ticks==14)Truant.say(a,"All right. I'll try the doors myself.");
                if(e.ticks<28)return true;
                var origin=Vec3.atLowerCornerOf(b);
                var to=origin.add(TO_DOOR[e.leg]);
                var direction=to.subtract(a.position()).normalize();
                if(e.leg>0&&readers.stream().anyMatch(q->inHisWay(a,q,direction))){
                    finish(e,a);Truant.say(a,"All right. Leave it.");return true;
                }
                Truant.gesture(a,Truant.LAMP);Truant.face(a,to);Truant.walk(a,to,.10);
                if(a.distanceToSqr(to)<.015&&++e.leg>=TO_DOOR.length){
                    if(l.getBlockState(e.departure.door()).getBlock() instanceof DoorBlock)
                        l.playSound(null,e.departure.door(),SoundEvents.WOODEN_DOOR_CLOSE,SoundSource.BLOCKS,.8F,.72F);
                    finish(e,a);
                }
            }
            case CUP->{
                if(!a.getMainHandItem().is(LiteraryRegistry.GHOSTS_MUG.get())){
                    Truant.gesture(a,Truant.REACH);
                    a.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(LiteraryRegistry.GHOSTS_MUG.get()));
                }
                if(p.distanceToSqr(e.cupAt)>1.0)reach(e,a,p);
                else if(++e.ticks>=200)finish(e,a);
            }
            case REACH,GRIP->{
                if(p.distanceToSqr(a)>9||!p.hasLineOfSight(a)){finish(e,a);return true;}
                Truant.face(a,p.position());Truant.gesture(a,Truant.REACH);
                if(e.beat==Beat.REACH&&++e.ticks>=12){
                    if(p.distanceToSqr(a)>6.25){finish(e,a);return true;}
                    e.beat=Beat.GRIP;e.ticks=0;
                }
                if(e.beat==Beat.GRIP){
                    if(e.ticks==0){
                        Truant.harm(p,"coffee","CoffeeHeld");
                        Truant.say(a,"Just till I finish.");
                    }
                    hold(e,p);
                    if(++e.ticks>=GRIP_TICKS)finish(e,a);
                }
            }
            default->{return false;}
        }
        return true;
    }

    private static boolean inHisWay(LiteraryActor a,ServerPlayer p,Vec3 direction){
        var offset=p.position().subtract(a.position());
        double ahead=offset.dot(direction);
        return ahead>.45&&ahead<1.5&&offset.subtract(direction.scale(ahead)).lengthSqr()<.36;
    }

    private static void hold(Encounter e,ServerPlayer p){
        var current=p.getEffect(MobEffects.MOVEMENT_SLOWDOWN);
        if(current!=null&&!ownsGrip(e,current))return;
        p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,GRIP_EFFECT_TICKS,GRIP_AMPLIFIER,false,false,false));
        e.gripEffect=p.getEffect(MobEffects.MOVEMENT_SLOWDOWN);
    }

    private static boolean ownsGrip(Encounter e,MobEffectInstance effect){
        return effect!=null&&effect==e.gripEffect&&effect.getAmplifier()==GRIP_AMPLIFIER
                &&effect.getDuration()<=GRIP_EFFECT_TICKS;
    }

    private static void finish(Encounter e,LiteraryActor a){
        if(e.gripEffect!=null&&a.level() instanceof ServerLevel level){
            var p=level.getServer().getPlayerList().getPlayer(e.reader);
            if(p!=null&&ownsGrip(e,p.getEffect(MobEffects.MOVEMENT_SLOWDOWN)))p.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
            e.gripEffect=null;
        }
        e.beat=Beat.DONE;
        Truant.gesture(a,0);
    }
}

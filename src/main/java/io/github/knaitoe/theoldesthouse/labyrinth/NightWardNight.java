package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseWatchers;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.CrossCollisionBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * The night ward's long night (0.4.330). The chart says someone should come when the button is pressed. Something does answer
 * each press: footsteps, and they come a little further every quarter of the night. At first they stop outside the door, then
 * by the station, then behind a drawn curtain, and in the last quarter behind the reader. Nobody is ever there. Meanwhile the
 * ward closes in. The station light goes out, the open bay curtains are drawn one by one while the reader is looking elsewhere,
 * and from halfway through, the monitor on the empty incubator starts beeping in time with a heart, a little faster each quarter.
 * A call left unanswered late in the night brings the footsteps running. All of it belongs to one reader: the sounds and the
 * changed blocks are sent only to them, and the real ward is shown again at dawn or when they leave.
 * <p>
 * Nothing here changes the night's resolution, its timing, the chart or the Witness record.
 */
public final class NightWardNight {
    public static final int QUARTER=NovelVignettes.WARD_NIGHT/4;
    /** How long an alarm may ring unanswered in the last quarter before the footsteps run. */
    public static final int UNANSWERED=160;
    static final BlockPos STATION_LIGHT=new BlockPos(-6,NightWard.CEILING_Y,-3),INCUBATOR_LIGHT=new BlockPos(0,NightWard.CEILING_Y,-14),MONITOR=new BlockPos(3,1,-15);
    /** The bays in the order the night draws their open curtains: side (-1 west, 1 east) and the bed's z. */
    static final int[][] BAYS={{-1,-7},{-1,-19},{1,-17},{1,-11},{-1,-11}};
    private NightWardNight(){}

    static int quarter(int ticks){return Math.min(3,Math.max(0,ticks)/QUARTER);}

    /** A press while the monitor calls: the answer starts walking, unless one is already on its way. */
    static void answer(ServerPlayer p,BlockPos b,CompoundTag own){
        if(own.contains("WardWalk")){NovelVignettes.cue(p,own,"The button clicks.");return;}
        int q=quarter(own.getInt("WardTicks"));
        Vec3 here=p.position().subtract(Vec3.atLowerCornerOf(b));
        List<Vec3> path=switch(q){
            case 0->List.of(new Vec3(.5,0,9.5),new Vec3(.5,0,2.5));
            case 1->List.of(new Vec3(.5,0,5.5),new Vec3(.5,0,-.5),new Vec3(-3.5,0,-2.5));
            case 2->{int[] bay=bayBehind(own,here);Vec3 at=new Vec3(6*bay[0]+.5,0,bay[1]+1.5);yield List.of(new Vec3(.5,0,3.5),new Vec3(.5,0,at.z),at);}
            default->List.of(new Vec3(.5,0,-21.5),new Vec3(.5,0,here.z-2),behind(p,here));
        };
        walk(own,path,q,false);
        NovelVignettes.cue(p,own,q==0?"The button clicks. Somewhere past the door, footsteps.":"The button clicks. Footsteps.");
    }
    /** The bay nearest the reader whose curtain is drawn, or the nearest bay if none is yet. */
    private static int[] bayBehind(CompoundTag own,Vec3 here){
        int shut=own.getInt("WardShut");int[] best=null;double bestD=Double.MAX_VALUE;
        for(int pass=0;pass<2&&best==null;pass++)for(int i=0;i<BAYS.length;i++){
            if(pass==0&&(shut&(1<<i))==0)continue;
            double d=new Vec3(6*BAYS[i][0]+.5,0,BAYS[i][1]+1.5).distanceToSqr(here.x,0,here.z);
            if(d<bestD){bestD=d;best=BAYS[i];}
        }
        return best;
    }
    /** A point a step behind the reader, kept inside the ward. */
    private static Vec3 behind(ServerPlayer p,Vec3 here){
        var look=p.getLookAngle();var flat=new Vec3(look.x,0,look.z);if(flat.lengthSqr()<1e-4)flat=new Vec3(0,0,-1);flat=flat.normalize();
        var at=here.subtract(flat.scale(1.3));
        return new Vec3(Math.max(-7.5,Math.min(8.5,at.x)),0,Math.max(-21.5,Math.min(-.5,at.z)));
    }
    private static void walk(CompoundTag own,List<Vec3> path,int q,boolean run){
        var w=new CompoundTag();var pts=new net.minecraft.nbt.ListTag();
        for(var v:path){var t=new CompoundTag();t.putDouble("X",v.x);t.putDouble("Z",v.z);pts.add(t);}
        w.put("Path",pts);w.putInt("Q",q);w.putBoolean("Run",run);w.putInt("T",0);own.put("WardWalk",w);
    }
    /** Where the walk is going, relative to the ward: for tests and for the stop. */
    public static Vec3 walkTarget(CompoundTag own){
        var pts=own.getCompound("WardWalk").getList("Path",10);if(pts.isEmpty())return null;var t=pts.getCompound(pts.size()-1);return new Vec3(t.getDouble("X"),0,t.getDouble("Z"));
    }
    private static Vec3 along(net.minecraft.nbt.ListTag pts,double distance){
        Vec3 prev=null;
        for(int i=0;i<pts.size();i++){var t=pts.getCompound(i);var v=new Vec3(t.getDouble("X"),0,t.getDouble("Z"));
            if(prev!=null){double len=prev.distanceTo(v);if(distance<=len)return prev.lerp(v,len<1e-6?1:distance/len);distance-=len;}
            prev=v;}
        return prev;
    }
    private static double length(net.minecraft.nbt.ListTag pts){
        double sum=0;Vec3 prev=null;
        for(int i=0;i<pts.size();i++){var t=pts.getCompound(i);var v=new Vec3(t.getDouble("X"),0,t.getDouble("Z"));if(prev!=null)sum+=prev.distanceTo(v);prev=v;}
        return sum;
    }

    /** One ward tick for this reader, after the monitor's own call. */
    static void tick(ServerPlayer p,BlockPos b,CompoundTag own,int ticks){
        var l=p.serverLevel();
        // The answer walking.
        if(own.contains("WardWalk")){
            var w=own.getCompound("WardWalk");int t=w.getInt("T")+1;w.putInt("T",t);boolean run=w.getBoolean("Run");int q=w.getInt("Q");
            double pace=run?.45:q==3?.11:.16;int every=run?4:q==3?10:8;var pts=w.getList("Path",10);double total=length(pts),done=t*pace;
            if(t%every==0||done>=total){
                var at=along(pts,Math.min(done,total));
                float volume=run?.75F:switch(q){case 0->.3F;case 1->.45F;case 2->.55F;default->.6F;};
                LiteraryVignettes.heard(p,NovelRegistry.WARD_STEP.get(),b.offset((int)Math.floor(at.x),0,(int)Math.floor(at.z)),volume,.9F+p.getRandom().nextFloat()*.15F);
            }
            if(done>=total){
                own.remove("WardWalk");
                var end=along(pts,total);var at=b.offset((int)Math.floor(end.x),0,(int)Math.floor(end.z));
                if(run){flicker(p,b,own,ticks,30);NovelVignettes.cue(p,own,"They stop behind you.");}
                else switch(q){
                    case 0->NovelVignettes.cue(p,own,"The footsteps stop outside the door. Nobody comes in.");
                    case 1->NovelVignettes.cue(p,own,"The footsteps stop by the station. Nobody is there.");
                    case 2->{LiteraryVignettes.heard(p,SoundEvents.ARMOR_EQUIP_LEATHER.value(),at,.35F,1.25F);NovelVignettes.cue(p,own,"The footsteps stop behind a curtain. It moves a little.");}
                    default->{flicker(p,b,own,ticks,10);NovelVignettes.cue(p,own,"The footsteps stop behind you.");}
                }
                var last=new CompoundTag();last.putDouble("X",end.x);last.putDouble("Z",end.z);last.putInt("Q",q);last.putBoolean("Run",run);own.put("WardLastStop",last);
            }else own.put("WardWalk",w);
        }
        // Left ringing in the last quarter, the call is answered anyway.
        if(ticks>=3*QUARTER&&own.getBoolean("Alarm")&&!own.getBoolean("WardRan")&&!own.contains("WardWalk")&&ticks-own.getInt("AlarmSince")>=UNANSWERED){
            own.putBoolean("WardRan",true);Vec3 here=p.position().subtract(Vec3.atLowerCornerOf(b));
            var from=here.z>-11.5?new Vec3(.5,0,-21.5):new Vec3(.5,0,-1.5);
            walk(own,List.of(from,new Vec3(.5,0,here.z),behind(p,here)),3,true);
            NovelVignettes.cue(p,own,"Nobody answers the monitor. Footsteps run the length of the ward.");
        }
        // The station light goes out with the first quarter.
        if(ticks==QUARTER){send(p,b,STATION_LIGHT,NightWardBlock.of(NightWardBlock.Kind.LIGHT_OFF));LiteraryVignettes.heard(p,NovelRegistry.WARD_FLICKER.get(),b.offset(STATION_LIGHT),.6F,1);}
        // The open curtains are drawn, one bay at a time, never while the reader is looking at that bay.
        if(ticks>=QUARTER&&ticks%10==0){
            int due=Math.min(BAYS.length,1+(ticks-QUARTER)/360),shut=own.getInt("WardShut");
            for(int i=0;i<due;i++){if((shut&(1<<i))!=0)continue;
                var bay=BAYS[i];var front=Vec3.atLowerCornerOf(b).add(5*bay[0]+.5,1,bay[1]+1);
                if(HouseWatchers.sees(p,front))break;
                own.putInt("WardShut",shut|1<<i);for(var e:bayViews(bay).entrySet())send(p,b,e.getKey(),e.getValue());
                LiteraryVignettes.heard(p,NovelRegistry.WARD_CURTAIN.get(),BlockPos.containing(front),.55F,.95F+p.getRandom().nextFloat()*.1F);
                break;}
        }
        // From halfway through the night the empty incubator's monitor keeps time with a heart, quicker as dawn nears.
        if(ticks>=2*QUARTER){int every=ticks>=NovelVignettes.WARD_NIGHT-500?13:ticks>=3*QUARTER?17:23;
            if(ticks%every==0){float rise=(ticks-2*QUARTER)/(float)(2*QUARTER);LiteraryVignettes.heard(p,NovelRegistry.WARD_PULSE.get(),b.offset(MONITOR),.15F+.3F*rise,1);}}
        // The incubator's light coming back after a flicker.
        if(own.contains("WardFlicker")&&ticks>=own.getInt("WardFlicker")){own.remove("WardFlicker");send(p,b,INCUBATOR_LIGHT,l.getBlockState(b.offset(INCUBATOR_LIGHT)));}
        // Kept shown: chunks come and go on the client, and a reconnect forgets every private block.
        if(p.tickCount%100==0)for(var e:views(own,ticks).entrySet())send(p,b,e.getKey(),e.getValue());
    }
    private static void flicker(ServerPlayer p,BlockPos b,CompoundTag own,int ticks,int length){
        own.putInt("WardFlicker",ticks+length);send(p,b,INCUBATOR_LIGHT,NightWardBlock.of(NightWardBlock.Kind.LIGHT_OFF));
        LiteraryVignettes.heard(p,NovelRegistry.WARD_FLICKER.get(),b.offset(INCUBATOR_LIGHT),.7F,.9F);
    }
    /** The front half of a bay with its curtain drawn across, as the bay's other curtains are hung. */
    static Map<BlockPos,BlockState> bayViews(int[] bay){
        Map<BlockPos,BlockState> out=new LinkedHashMap<>();int x=5*bay[0];
        for(int dz=0;dz<=1;dz++){
            out.put(new BlockPos(x,0,bay[1]+dz),curtain(NightWardCurtainBlock.Part.LOWER));
            out.put(new BlockPos(x,1,bay[1]+dz),curtain(NightWardCurtainBlock.Part.UPPER));
        }
        return out;
    }
    private static BlockState curtain(NightWardCurtainBlock.Part part){
        return NightWardCurtainBlock.of(part).setValue(CrossCollisionBlock.NORTH,true).setValue(CrossCollisionBlock.SOUTH,true);
    }
    /** Every block this reader's night shows differently from the ward itself, relative to the ward. */
    public static Map<BlockPos,BlockState> views(CompoundTag own,int ticks){
        Map<BlockPos,BlockState> out=new LinkedHashMap<>();
        if(ticks>=QUARTER)out.put(STATION_LIGHT,NightWardBlock.of(NightWardBlock.Kind.LIGHT_OFF));
        if(own.contains("WardFlicker"))out.put(INCUBATOR_LIGHT,NightWardBlock.of(NightWardBlock.Kind.LIGHT_OFF));
        int shut=own.getInt("WardShut");for(int i=0;i<BAYS.length;i++)if((shut&(1<<i))!=0)out.putAll(bayViews(BAYS[i]));
        return out;
    }
    private static void send(ServerPlayer p,BlockPos b,BlockPos rel,BlockState state){p.connection.send(new ClientboundBlockUpdatePacket(b.offset(rel),state));}

    /** Dawn or leaving: the real ward again, and the night's own state forgotten. */
    static void end(ServerPlayer p,BlockPos b,CompoundTag own){
        if(b!=null&&p.level().dimension().equals(NovelRooms.dimension(LabyrinthPlace.HOSPITAL))){
            var l=p.serverLevel();List<BlockPos> touched=new java.util.ArrayList<>(List.of(STATION_LIGHT,INCUBATOR_LIGHT));for(var bay:BAYS)touched.addAll(bayViews(bay).keySet());
            for(var rel:touched)send(p,b,rel,l.getBlockState(b.offset(rel)));
        }
        reset(own);
    }
    static void reset(CompoundTag own){for(var key:List.of("WardWalk","WardShut","WardFlicker","WardRan","AlarmSince","WardLastStop"))own.remove(key);}
}

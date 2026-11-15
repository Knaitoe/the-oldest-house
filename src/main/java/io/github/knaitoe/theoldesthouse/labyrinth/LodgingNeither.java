package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.616, The Ones Already Inside, its sixteenth slice (docs/THE_ONES_ALREADY_INSIDE_PLAN.md, E4: "Neither Daniel. Ordinary violence
 * after the link is known. Both die, in two rooms or one. Vera starts putting the room back before anybody has moved. What happens to
 * her and the house after that is still the party's: tell Rae what she did, stop her preparing the next guest, leave her the house, or
 * go down and break the support.").
 *
 * <ul>
 * <li><b>Who.</b> Only a reader who has seen the cut come up on both men (0.4.606), or had the truth out of Vera (0.4.609), knows a blow
 * on one is a blow on the other. Anyone else's blow does what it always did: nothing. Not while another end is under way or done.</li>
 * <li><b>Blows.</b> Every blow from a reader who knows lands on both, wherever the other is: both double over, both are heard. Six,
 * from anyone who knows, a blow a hand every half second at most, and they die where they are, in two rooms or one. Daniel strapped in
 * his frame hangs in the straps; free, he goes down like Dan.</li>
 * <li><b>After.</b> Before anybody has moved, Vera comes down to the dining room and lays two places for the next arrivals. A blow on
 * her while she lays them stops her where she is. When both are laid, Rae comes in, knocks one of the chairs over, and cannot say which
 * place she meant. A reader can tell Rae what Vera did; one who knows the third way can still go down and work the ratchet and the
 * catch, and the house is let go under her.</li>
 * </ul>
 */
public final class LodgingNeither {
    private LodgingNeither(){}
    public static final int NONE=0,DEAD=1,LAYING=2,RAE=3,DONE=4,STOPPED=5,BROKEN=6;
    public static final String STATE="Neither0616";
    /** Each reader's own. */
    public static final String STRUCK="StruckThem0616",SAW_DIE="SawDie0616",SAW_PLACES="SawPlaces0616",TOLD="ToldRae0616",STOPPED_HER="StoppedVera0616",BROKE="BrokeSupport0616";
    public static final int BLOWS=6,GAP=10,LAY_EVERY=40,HURT_FOR=20;
    /** Lying where they fell; hanging in the straps. */
    public static final int DOWN=27,SLUMP=28;
    /** The two places laid, opposite each other across the middle of the table, and the chair Rae knocks over. */
    public static final BlockPos PLACE_WEST=new BlockPos(LodgingSite.TABLE_W,0,-5),PLACE_EAST=new BlockPos(LodgingSite.TABLE_E,0,-5),
        RAE_CHAIR=new BlockPos(LodgingSite.TABLE_W-1,0,-5);
    static final LodgingPeople.Spot VERA_TABLE=new LodgingPeople.Spot(6.4,LodgingSite.GROUND,-6.5,-90,LodgingPeople.WORK),
        RAE_TABLE=new LodgingPeople.Spot(6.4,LodgingSite.GROUND,-3.4,-120,LodgingPeople.WAIT);

    static CompoundTag state(CompoundTag house){if(!house.contains(STATE))house.put(STATE,new CompoundTag());return house.getCompound(STATE);}
    public static int phase(MinecraftServer s,BlockPos b){return state(LodgingStory.house(s,b)).getInt("Phase");}
    public static int blows(MinecraftServer s,BlockPos b){return state(LodgingStory.house(s,b)).getInt("Blows");}
    public static int laid(MinecraftServer s,BlockPos b){return state(LodgingStory.house(s,b)).getInt("Laid");}
    /** Whether this reader was told they died in the one room. */
    public static boolean oneRoom(CompoundTag own){return own.getBoolean("DiedOneRoom0616");}
    /** Both men dead: every other end is closed. */
    public static boolean dead(MinecraftServer s,BlockPos b){return phase(s,b)>=DEAD;}
    private static void go(CompoundTag in,int phase,int clock){in.putInt("Phase",phase);in.putInt("At",clock);}
    /** Whether this reader knows a blow on one lands on the other. */
    public static boolean knows(CompoundTag own){return own.getBoolean(LodgingInjury.SAW_DAN_CUT)&&own.getBoolean(LodgingInjury.SAW_DANIEL_CUT)||own.getBoolean(LodgingVera.TRUTH);}
    /** Nothing else under way or done, and the house settled. */
    static boolean open(MinecraftServer s,BlockPos b){
        return LodgingReturn.ready(s,b)&&LodgingReturn.phase(s,b)==LodgingReturn.NONE&&!LodgingComplete.begun(s,b)&&!LodgingSever.severed(s,b)&&!dead(s,b);
    }

    /** Where one of the four is kept by this slice, or null. */
    static @Nullable LodgingPeople.Spot spot(MinecraftServer s,BlockPos b,int who){
        var in=state(LodgingStory.house(s,b));if(in.getInt("Phase")<DEAD)return null;
        if(who==LodgingPeople.DAN||who==LodgingPeople.DANIEL){
            String k=who==LodgingPeople.DAN?"Dan":"Daniel";
            if(!in.contains(k+"X"))return null;
            return new LodgingPeople.Spot(in.getDouble(k+"X"),in.getDouble(k+"Y"),in.getDouble(k+"Z"),in.getFloat(k+"Yaw"),in.getInt(k+"Pose"));
        }
        if(who==LodgingPeople.VERA&&in.getBoolean("VeraThere"))return VERA_TABLE;
        if(who==LodgingPeople.RAE&&in.getBoolean("RaeThere"))return RAE_TABLE;
        return null;
    }
    /** The two dead men are not to turn their heads to anybody. */
    public static boolean still(MinecraftServer s,BlockPos b,int who){return (who==LodgingPeople.DAN||who==LodgingPeople.DANIEL)&&dead(s,b);}

    // ------------------------------------------------------------------------------------------------ blows
    /** A blow on Dan or Daniel. True if this slice took it. */
    public static boolean struck(ServerPlayer p,LiteraryActor a,int who){
        if(who!=LodgingPeople.DAN&&who!=LodgingPeople.DANIEL&&who!=LodgingPeople.VERA)return false;
        var b=LiteraryVignettes.base(p,LodgingPeople.PLACE);if(b==null)return false;
        var s=p.server;var l=p.serverLevel();var h=LodgingStory.house(s,b);var in=state(h);int clock=h.getInt("Clock");
        if(who==LodgingPeople.VERA)return stop(p,l,b,h,in,a,clock);
        var own=LodgingHouse.own(p);if(!knows(own)||!open(s,b))return false;
        if(own.contains("BlowAt0616")&&clock-own.getInt("BlowAt0616")<GAP)return true;
        own.putInt("BlowAt0616",clock);own.putBoolean(STRUCK,true);LodgingHouse.save(p,own);
        var dan=LodgingPeople.find(l,b,LodgingPeople.DAN);var daniel=LodgingPeople.find(l,b,LodgingPeople.DANIEL);
        int n=in.getInt("Blows")+1;in.putInt("Blows",n);in.putInt("HurtUntil",clock+HURT_FOR);
        // Both take it, wherever the other is; both are heard.
        for(var m:new LiteraryActor[]{dan,daniel}){if(m==null)continue;if(!LodgingKey.free(s,b)&&m==daniel)m.gesture(LodgingPeople.STRAIN);else m.gesture(LodgingPeople.HURT);
            l.playSound(null,m.blockPosition(),SoundEvents.PLAYER_HURT,SoundSource.NEUTRAL,.9F,.8F);
            l.sendParticles(ParticleTypes.DAMAGE_INDICATOR,m.getX(),m.getY()+1.2,m.getZ(),2,.2,.2,.2,0);}
        var other=a==dan?daniel:dan;
        boolean together=other!=null&&p.hasLineOfSight(other)&&p.distanceTo(other)<=12;
        LodgingHouse.cue(p,n==1?(together?LodgingTexts.BLOW_SAME:LodgingTexts.BLOW_FIRST):LodgingTexts.BLOW_AGAIN);
        if(n==1&&dan!=null)LodgingPeople.says(p,dan,"Dan",LodgingTexts.DAN_STOP);
        if(n==3&&daniel!=null)for(var r:LodgingHouse.readers(l,b))if(!r.isSpectator()&&r.distanceTo(daniel)<=LodgingDaniel.HEARD)Talk.line(r,"Daniel",LodgingTexts.DANIEL_WHO);
        if(n>=BLOWS)die(l,b,h,in,dan,daniel,clock);
        LodgingStory.save(s,b,h);
        return true;
    }
    private static void die(ServerLevel l,BlockPos b,CompoundTag h,CompoundTag in,@Nullable LiteraryActor dan,@Nullable LiteraryActor daniel,int clock){
        var s=l.getServer();go(in,DEAD,clock);
        boolean strapped=!LodgingKey.free(s,b);
        lay(b,in,"Dan",dan,DOWN);lay(b,in,"Daniel",daniel,strapped?SLUMP:DOWN);
        boolean oneRoom=dan!=null&&daniel!=null&&dan.distanceTo(daniel)<=6&&dan.hasLineOfSight(daniel);
        in.putBoolean("OneRoom",oneRoom);
        for(var p:LodgingHouse.readers(l,b)){if(p.isSpectator())continue;
            LodgingHouse.cue(p,oneRoom?LodgingTexts.DIED_ONE:LodgingTexts.DIED_TWO);
            var own=LodgingHouse.own(p);own.putBoolean(SAW_DIE,true);own.putBoolean("DiedOneRoom0616",oneRoom);LodgingHouse.save(p,own);}
        if(dan!=null)l.playSound(null,dan.blockPosition(),SoundEvents.PLAYER_BIG_FALL,SoundSource.NEUTRAL,1F,.9F);
        if(daniel!=null)l.playSound(null,daniel.blockPosition(),strapped?SoundEvents.CHAIN_STEP:SoundEvents.PLAYER_BIG_FALL,SoundSource.NEUTRAL,1F,.8F);
        // Vera, before anybody has moved.
        var vera=LodgingPeople.find(l,b,LodgingPeople.VERA);
        // Heard through the house: she says it on her way down.
        if(vera!=null){in.putString("VeraFrom",zone(b,vera));for(var p:LodgingHouse.readers(l,b))if(!p.isSpectator())Talk.line(p,"Vera",LodgingTexts.VERA_BACK);LodgingStory.speak(l,b,vera,"Vera",LodgingTexts.VERA_BACK);}
    }
    private static void lay(BlockPos b,CompoundTag in,String k,@Nullable LiteraryActor a,int pose){
        if(a==null)return;var r=LodgingInjury.rel(b,a);a.gesture(pose);a.setDeltaMovement(Vec3.ZERO);
        in.putDouble(k+"X",r.x);in.putDouble(k+"Y",r.y);in.putDouble(k+"Z",r.z);in.putFloat(k+"Yaw",a.getYRot());in.putInt(k+"Pose",pose);
    }
    /** A blow on Vera while she lays the places: she stops where she is, and lays no more. */
    private static boolean stop(ServerPlayer p,ServerLevel l,BlockPos b,CompoundTag h,CompoundTag in,LiteraryActor vera,int clock){
        int ph=in.getInt("Phase");if(ph!=DEAD&&ph!=LAYING)return false;
        var own=LodgingHouse.own(p);own.putBoolean(STOPPED_HER,true);LodgingHouse.save(p,own);
        go(in,STOPPED,clock);vera.gesture(LodgingPeople.WAIT);vera.setDeltaMovement(Vec3.ZERO);
        l.playSound(null,vera.blockPosition(),SoundEvents.PLAYER_ATTACK_WEAK,SoundSource.PLAYERS,.8F,.9F);
        for(var r:LodgingHouse.readers(l,b))if(!r.isSpectator()&&r.distanceTo(vera)<=12)LodgingHouse.cue(r,LodgingTexts.VERA_STOPS);
        aloud(l,b,vera,"Vera",LodgingTexts.VERA_STOPPED);
        LodgingStory.save(l.getServer(),b,h);return true;
    }

    // ------------------------------------------------------------------------------------------------ the next guests
    static void tick(ServerLevel l,BlockPos b,List<ServerPlayer> here){
        var s=l.getServer();var h=LodgingStory.house(s,b);var in=state(h);int ph=in.getInt("Phase");int clock=h.getInt("Clock");
        // Doubled over, then upright again, while they are alive.
        if(ph==NONE&&in.getInt("Blows")>0&&in.contains("HurtUntil")&&clock>=in.getInt("HurtUntil")){
            in.remove("HurtUntil");
            var dan=LodgingPeople.find(l,b,LodgingPeople.DAN);if(dan!=null&&dan.gesture()==LodgingPeople.HURT)dan.gesture(LodgingPeople.POCKET);
            var daniel=LodgingPeople.find(l,b,LodgingPeople.DANIEL);if(daniel!=null&&(daniel.gesture()==LodgingPeople.HURT||daniel.gesture()==LodgingPeople.STRAIN))daniel.gesture(LodgingKey.free(s,b)?LodgingPeople.WAIT:LodgingPeople.HELD);
            LodgingStory.save(s,b,h);return;
        }
        if(ph<DEAD||ph>=DONE)return;
        // The dead stay as they fell.
        for(var k:new String[]{"Dan","Daniel"}){var m=LodgingPeople.find(l,b,k.equals("Dan")?LodgingPeople.DAN:LodgingPeople.DANIEL);if(m!=null&&m.gesture()!=in.getInt(k+"Pose"))m.gesture(in.getInt(k+"Pose"));}
        if(ph==DEAD){
            var vera=LodgingPeople.find(l,b,LodgingPeople.VERA);
            if(vera==null){go(in,DONE,clock);LodgingStory.save(s,b,h);return;}
            if(walk(l,b,in,"VeraLeg",vera,in.getString("VeraFrom"),VERA_TABLE,.06)){
                in.putBoolean("VeraThere",true);seat(vera,VERA_TABLE);go(in,LAYING,clock);
            }
        }else if(ph==LAYING){
            int laid=in.getInt("Laid");
            if(clock-in.getInt("At")>=LAY_EVERY*(laid+1)&&laid<2){
                var cell=laid==0?PLACE_WEST:PLACE_EAST;var st=l.getBlockState(b.offset(cell));
                if(st.getBlock() instanceof LodgingThing)l.setBlock(b.offset(cell),st.setValue(LodgingThing.STAGE,st.getValue(LodgingThing.STAGE)|2),3);
                l.playSound(null,b.offset(cell),SoundEvents.CHAIN_PLACE,SoundSource.BLOCKS,.4F,1.6F);
                var vera=LodgingPeople.find(l,b,LodgingPeople.VERA);if(vera!=null)vera.swing(net.minecraft.world.InteractionHand.MAIN_HAND,true);
                in.putInt("Laid",++laid);
                if(laid==1)for(var p:here)if(!p.isSpectator()&&near(b,p,cell))LodgingHouse.cue(p,LodgingTexts.VERA_LAYS);
            }
            if(laid>=2){var rae=LodgingPeople.find(l,b,LodgingPeople.RAE);if(rae!=null)in.putString("RaeFrom",zone(b,rae));go(in,rae==null?DONE:RAE,clock);}
        }else if(ph==RAE){
            var rae=LodgingPeople.find(l,b,LodgingPeople.RAE);
            if(rae==null){go(in,DONE,clock);LodgingStory.save(s,b,h);return;}
            if(walk(l,b,in,"RaeLeg",rae,in.getString("RaeFrom"),RAE_TABLE,.07)){
                in.putBoolean("RaeThere",true);seat(rae,RAE_TABLE);
                var st=l.getBlockState(b.offset(RAE_CHAIR));
                if(st.getBlock() instanceof LodgingThing)l.setBlock(b.offset(RAE_CHAIR),st.setValue(LodgingThing.STAGE,1),3);
                l.playSound(null,b.offset(RAE_CHAIR),SoundEvents.WOOD_FALL,SoundSource.BLOCKS,1F,.7F);
                aloud(l,b,rae,"Rae",LodgingTexts.RAE_WHICH);
                for(var p:here){if(p.isSpectator()||!near(b,p,RAE_CHAIR))continue;LodgingHouse.cue(p,LodgingTexts.TWO_PLACES);var own=LodgingHouse.own(p);own.putBoolean(SAW_PLACES,true);LodgingHouse.save(p,own);}
                go(in,DONE,clock);
            }
        }
        LodgingStory.save(s,b,h);
    }
    /** In the dining room, or looking in at it from the hall door. */
    private static boolean near(BlockPos b,ServerPlayer p,BlockPos cell){var r=p.position().subtract(b.getX(),b.getY(),b.getZ());return p.distanceToSqr(Vec3.atCenterOf(b.offset(cell)))<=12*12&&r.x>=-3&&r.y<LodgingSite.UPPER-1&&r.y>LodgingSite.CELLAR+1;}

    // ------------------------------------------------------------------------------------------------ what the party does after
    /** Rae, after: a reader can tell her what Vera did. */
    static boolean talk(ServerPlayer p,BlockPos b,LiteraryActor a,int who,CompoundTag own){
        if(who!=LodgingPeople.RAE||!dead(p.server,b))return false;
        if(own.getBoolean(TOLD)){LodgingPeople.says(p,a,"Rae",LodgingTexts.RAE_AFTER);return true;}
        Talk.begin(p,LodgingPeople.TALK+"neither","Rae");
        var replies=new ArrayList<Talk.Reply>();
        replies.add(Talk.reply(LodgingTexts.R_TELL_RAE,LodgingNeither::tell));
        replies.add(Talk.act(LodgingTexts.R_NOTHING,Talk::close));Talk.ask(p,replies);
        return true;
    }
    private static void tell(ServerPlayer p){
        var b=LiteraryVignettes.base(p,LodgingPeople.PLACE);if(b==null){Talk.close(p);return;}
        var rae=LodgingPeople.find(p.serverLevel(),b,LodgingPeople.RAE);
        var own=LodgingHouse.own(p);own.putBoolean(TOLD,true);LodgingHouse.save(p,own);
        if(rae!=null)LodgingPeople.says(p,rae,"Rae",LodgingTexts.RAE_TOLD);
        Talk.end(p);
    }
    /** The ratchet and the catch both worked after the two are dead (LodgingSever): the house let go under her. */
    static void broken(ServerLevel l,BlockPos b,CompoundTag h,int clock){
        var in=state(h);int was=in.getInt("Phase");go(in,BROKEN,clock);in.putInt("Before",was);
        var vera=LodgingPeople.find(l,b,LodgingPeople.VERA);
        if(vera!=null){vera.gesture(LodgingSever.SAT);vera.setDeltaMovement(Vec3.ZERO);aloud(l,b,vera,"Vera",LodgingTexts.VERA_BROKEN);}
        for(var p:LodgingHouse.readers(l,b)){if(p.isSpectator())continue;if(vera!=null&&p.distanceTo(vera)<=16)LodgingHouse.cue(p,LodgingTexts.VERA_SITS);
            var own=LodgingHouse.own(p);if(own.getBoolean(LodgingSever.RATCHET)||own.getBoolean(LodgingSever.CATCH)){own.putBoolean(BROKE,true);LodgingHouse.save(p,own);}}
    }

    // ------------------------------------------------------------------------------------------------ walking
    private static String zone(BlockPos b,LiteraryActor a){var r=LodgingInjury.rel(b,a);return r.y>LodgingSite.UPPER-1.5?"upstairs":LodgingSite.inParlor(r)?"parlor":LodgingSite.inDaniels(r)?"daniels":r.x>4?"dining":"hall";}
    /** From where they are to the dining table, by the doors between. */
    private static boolean walk(ServerLevel l,BlockPos b,CompoundTag in,String key,LiteraryActor a,String from,LodgingPeople.Spot to,double pace){
        var end=new Vec3(to.x(),0,to.z());var hall=new Vec3(2.6,0,-4.5);var door=new Vec3(5.4,0,-4.5);
        List<Vec3> legs;Map<Integer,BlockPos> doors;
        switch(from){
            case "upstairs"->{legs=List.of(new Vec3(.5,5,-19.5),new Vec3(.5,5,-15),new Vec3(3.5,5,-12.5),new Vec3(3.5,0,-5.5),hall,door,end);doors=Map.of(1,LodgingSite.WORK_DOOR,5,LodgingSite.DINING_DOOR);}
            case "daniels"->{legs=List.of(new Vec3(-5.6,0,-24),new Vec3(-5.6,0,-20),new Vec3(-7.5,0,-18.5),new Vec3(-7.5,0,-14),new Vec3(0,0,-14),new Vec3(0,0,-5.5),hall,door,end);doors=Map.of(3,LodgingSite.DANIEL_DOOR,7,LodgingSite.DINING_DOOR);}
            case "parlor"->{legs=List.of(new Vec3(-5.6,0,-8.5),new Vec3(-2.6,0,-8.5),new Vec3(0,0,-8.5),new Vec3(0,0,-5.5),hall,door,end);doors=Map.of(1,LodgingSite.PARLOR_DOOR,5,LodgingSite.DINING_DOOR);}
            case "dining"->{legs=List.of(end);doors=Map.of();}
            default->{legs=List.of(hall,door,end);doors=Map.of(1,LodgingSite.DINING_DOOR);}
        }
        if(a.gesture()!=LodgingPeople.WAIT)a.gesture(LodgingPeople.WAIT);
        return LodgingTaken.walk(l,b,in,key,a,legs,doors,pace);
    }
    private static void seat(LiteraryActor a,LodgingPeople.Spot at){a.setYRot(at.look());a.setYBodyRot(at.look());a.setYHeadRot(at.look());a.gesture(at.gesture());a.setDeltaMovement(Vec3.ZERO);}
    private static void aloud(ServerLevel l,BlockPos b,LiteraryActor a,String name,String line){
        for(var p:LodgingHouse.readers(l,b))if(!p.isSpectator()&&p.distanceTo(a)<=LodgingDaniel.HEARD)Talk.line(p,name,line);
        LodgingStory.speak(l,b,a,name,line);
    }
    /** For a house built again. */
    public static void clear(MinecraftServer s,BlockPos b){LodgingStory.house(s,b).remove(STATE);}
}

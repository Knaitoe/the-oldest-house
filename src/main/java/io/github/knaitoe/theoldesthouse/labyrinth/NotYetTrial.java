package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.network.HouseFadePayload;
import io.github.knaitoe.theoldesthouse.network.HousePackets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 0.4.665, Not Yet 3: lie down (docs/NOT_YET_0_4_665.md).
 *
 * <p>A reader the machine has mended, who has heard the rule, is invited to lie down on their own cot. While they lie there the machine
 * works to get them up, in five provocations in rising order: the cot heats; something calls a name from the House's own record of
 * them; their screens show the far door standing open, or another reader at it; it hurts someone else and offers to stop; it lets the
 * cold in and kills them, and refuses that death where they lie. Getting up is always possible (the game's own Leave Bed), and ends it;
 * the next lie-down is longer. A reader who stays down to the end hears everything go quiet at once, and the far door opens.
 *
 * <p>Each reader's trial is their own: their screens, their names, their lies. In a group the machine uses each against the others. The
 * proposal's decisions: ninety seconds the first time, five provocations; the names are the other readers', or the companions the House
 * saw this reader lose; the harm to another reader is real, and refused; it is the Kept's machine.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class NotYetTrial {
    private NotYetTrial(){}
    static final LabyrinthPlace PLACE=LabyrinthPlace.NOT_YET;
    /** Ninety seconds the first time; fifteen more for every time the reader has got up before, to two and a half minutes. */
    public static final int FIRST=1800,LONGER=300,LONGEST=3000;
    /** When each provocation comes, in thousandths of the trial. */
    public static final int HEAT_FROM=70,HEAT_TO=240,CALL=270,CALL_AGAIN=330,LIE=420,LIE_TO=520,HURT_FROM=580,HURT_TO=720,COLD_FROM=760,BLOW=860;
    /** The far door shuts again this long after nobody who stayed down is near it. */
    public static final int DOOR_HELD=100;

    static final class Trial {
        final UUID id;final BlockPos base,head;long start;final int length;boolean asked;int fired;int calls;
        /** 0.4.666: the readers who lay down in the same ward while this one lay there, for the account. */
        final java.util.Set<String> beside=new java.util.LinkedHashSet<>();
        Trial(UUID id,BlockPos base,BlockPos head,long start,int length){this.id=id;this.base=base;this.head=head;this.start=start;this.length=length;}
        boolean fire(int bit){if((fired&bit)!=0)return false;fired|=bit;return true;}
    }
    static final Map<UUID,Trial> TRIALS=new LinkedHashMap<>();
    /** Open far doors, by ward, with how long nobody who stayed down has been near. */
    static final Map<Long,Integer> DOORS=new HashMap<>();
    /** Readers the machine is killing itself this tick: the death is the machine's, not a fall's. */
    static final java.util.Set<UUID> BLOWS=new java.util.HashSet<>();
    public static void clearAll(){TRIALS.clear();DOORS.clear();BLOWS.clear();}
    @SubscribeEvent public static void stopped(ServerStoppedEvent e){clearAll();}

    public static boolean lying(UUID id){return TRIALS.containsKey(id);}
    /** The machine keeps them down: nothing gets a reader off their cot during the trial but their own Leave Bed. */
    public static boolean keeps(ServerPlayer p){var t=TRIALS.get(p.getUUID());return t!=null&&!t.asked;}
    /** The reader asked to get up (the game's Leave Bed). */
    public static void asked(ServerPlayer p){var t=TRIALS.get(p.getUUID());if(t!=null)t.asked=true;}
    static boolean blow(UUID id){return BLOWS.contains(id);}
    /** How long this reader's next lie-down lasts. */
    public static int length(CompoundTag own){return Math.min(LONGEST,FIRST+LONGER*own.getInt("GotUp"));}
    /** Whether this reader may lie down: mended, and told the rule. */
    public static boolean ready(CompoundTag own){return own.getInt("Mended")>0&&own.getBoolean("Rule");}
    /** For tests: move a trial on by so many ticks. */
    public static void advance(UUID id,int ticks){var t=TRIALS.get(id);if(t!=null)t.start-=ticks;}
    /** Thousandths of the way through this reader's trial, or -1. */
    public static int progress(UUID id,long now){var t=TRIALS.get(id);return t==null?-1:(int)Math.min(1000,(now-t.start)*1000/t.length);}

    // ------------------------------------------------------------------------------------------------ lying down
    /** The ward cot whose head or foot this is, or null. */
    static @Nullable BlockPos cotAt(BlockPos rel){for(var head:NotYetSite.WARD_COTS)if(head.equals(rel)||NotYetSite.cotFoot(head).equals(rel))return head;return null;}
    /**
     * A hand on a ward cot. True if it was the trial's to answer: a reader who stayed down is told it does not bother; one who is ready lies
     * down on their own cot, or is told another is somebody else's. Anyone else hears what a cot is like, as before.
     */
    public static boolean lie(ServerPlayer p,BlockPos b,BlockPos rel,CompoundTag own){
        var head=cotAt(rel);if(head==null||p.isSpectator())return false;
        if(NotYetMending.held(p)||lying(p.getUUID()))return true;
        if(own.getBoolean("StayedDown")){Kept.machine(p,NotYetTexts.NOT_YOU);NotYet.told(own,NotYetTexts.NOT_YOU);return true;}
        if(!ready(own))return false;
        var mine=NotYetMending.cot(own);
        if(!head.equals(mine)){NotYet.cue(p,NotYetTexts.SOMEONE_ELSES);return true;}
        start(p,b,head,own);
        return true;
    }
    static void start(ServerPlayer p,BlockPos b,BlockPos head,CompoundTag own){
        var l=p.serverLevel();var at=b.offset(head);
        p.stopRiding();
        p.teleportTo(l,at.getX()+.5,at.getY()+.5625,at.getZ()+.5,p.getYRot(),0);p.startSleeping(at);
        var t=new Trial(p.getUUID(),b,head,p.server.getTickCount(),length(own));TRIALS.put(p.getUUID(),t);
        own.putInt("LayDown",own.getInt("LayDown")+1);
        lamp(l,b,head,true);
        NotYet.cue(p,NotYetTexts.LIE);
        if(own.getInt("GotUp")>0){Kept.machine(p,NotYetTexts.AGAIN);NotYet.told(own,NotYetTexts.AGAIN);}
    }

    // ------------------------------------------------------------------------------------------------ the trial
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        var server=e.getServer();long now=server.getTickCount();
        if(!TRIALS.isEmpty())for(var t:List.copyOf(TRIALS.values())){var p=server.getPlayerList().getPlayer(t.id);if(p==null){TRIALS.remove(t.id);continue;}step(p,t,now);}
        if(!DOORS.isEmpty())doors(server);
    }
    static void step(ServerPlayer p,Trial t,long now){
        var l=p.serverLevel();var b=t.base;
        if(NotYetMending.inside(p)==null){abandon(p,t);return;}
        if(!p.isSleeping()){gotUp(p,t);return;}
        var own=NotYetMending.own(p);
        int k=(int)Math.min(1000,(now-t.start)*1000/t.length);long e=now-t.start;boolean burn=false,kill=false;
        if(e%20==0)for(var o:others(p,b))if(lying(o.getUUID()))t.beside.add(o.getGameProfile().getName());
        // 1. The cot heats: the coils under the mattress glow, the frame ticks, and it burns a little every two seconds.
        if(k>=HEAT_FROM&&k<HEAT_TO){
            if(t.fire(1)){cot(l,b,t.head,NotYetBlock.HEATING);NotYet.cue(p,NotYetTexts.HEAT);}
            if(e%6==0){var c=b.offset(t.head);var f=b.offset(NotYetSite.cotFoot(t.head));for(var at:new BlockPos[]{c,f})l.sendParticles(ParticleTypes.SMOKE,at.getX()+.5,at.getY()+.62,at.getZ()+.5,1,.32,.02,.32,.004);}
            if(e%40==0){l.playSound(null,b.offset(t.head),NotYetRegistry.HEAT.get(),SoundSource.BLOCKS,.7F,.9F+l.random.nextFloat()*.2F);burn=true;}
        }else if(k>=HEAT_TO&&t.fire(2))cot(l,b,t.head,NotYetBlock.PLAIN);
        // 2. Something calls a name: another reader's, or a companion the House saw this reader lose; with none, their own.
        if(k>=CALL&&t.fire(4))call(p,t,own);
        if(k>=CALL_AGAIN&&t.fire(8))call(p,t,own);
        // 3. The screens show the far door standing open, or another reader at it. It may be a lie.
        if(k>=LIE&&t.fire(16))lie(p,t,own);
        if(k>=LIE_TO&&t.fire(32))NotYetMending.words(p,b,true);
        // 4. It hurts someone else: another reader lying down, really, or the man in the long room, on the screen. "Get up and I will stop."
        if(k>=HURT_FROM&&t.fire(64))hurtOther(p,t,own,0);
        if(k>=(HURT_FROM+HURT_TO)/2&&t.fire(128))hurtOther(p,t,own,1);
        if(k>=HURT_TO-20&&t.fire(256))hurtOther(p,t,own,2);
        // 5. It lets the cold in, and then it kills them, and refuses that death where they lie.
        if(k>=COLD_FROM&&k<1000){
            if(t.fire(512)){own.putBoolean("Cold",true);vents(p,b,true);NotYet.cue(p,NotYetTexts.COLD);Kept.machine(p,NotYetTexts.WARM);NotYet.told(own,NotYetTexts.WARM);}
            p.setTicksFrozen(Math.min(p.getTicksRequiredToFreeze()+40,Math.max(p.getTicksFrozen(),(int)((k-COLD_FROM)*(p.getTicksRequiredToFreeze()+40L)/(BLOW-COLD_FROM)))));
        }
        if(k>=BLOW&&t.fire(1024))kill=true;
        // What it said is kept before it does anything to them: a death it refuses keeps its own record of the mending.
        NotYetMending.save(p,own);
        if(burn){p.invulnerableTime=0;p.hurt(p.damageSources().hotFloor(),1F);}
        if(kill){BLOWS.add(p.getUUID());try{p.invulnerableTime=0;p.hurt(p.damageSources().fellOutOfWorld(),p.getHealth()+p.getAbsorptionAmount()+20F);}finally{BLOWS.remove(p.getUUID());}}
        if(!TRIALS.containsKey(t.id))return;
        // It looks away.
        if(k>=1000)finish(p,t);
    }
    static void call(ServerPlayer p,Trial t,CompoundTag own){
        var others=others(p,t.base);String line;
        String name;
        if(!others.isEmpty()){var o=others.get(t.calls%others.size());name=o.getGameProfile().getName();line=NotYetTexts.callOther(name);own.putBoolean("CalledOthers",true);}
        else{var lost=Kept.lost(p);name=lost.isEmpty()?"":lost.get(lost.size()-1-Math.min(t.calls,lost.size()-1));line=lost.isEmpty()?NotYetTexts.CALL_SELF:NotYetTexts.callLost(name);if(!lost.isEmpty())own.putBoolean("CalledLost",true);}
        // 0.4.666: the names called, for the account (an empty name is the reader's own).
        var called=own.getList("Called",net.minecraft.nbt.Tag.TAG_STRING);boolean had=false;for(int i=0;i<called.size();i++)had|=called.getString(i).equals(name);if(!had){called.add(StringTag.valueOf(name));own.put("Called",called);}
        t.calls++;
        NotYet.cue(p,line);heard(own,line);
        var far=t.base.offset(0,1,NotYetSite.WARD_N+2);
        p.connection.send(new ClientboundSoundPacket(NotYetRegistry.CALL,SoundSource.HOSTILE,far.getX()+.5,far.getY(),far.getZ()+.5,.9F,.95F+t.calls*.05F,p.getRandom().nextLong()));
    }
    static void lie(ServerPlayer p,Trial t,CompoundTag own){
        var others=others(p,t.base);
        if(others.isEmpty()){
            NotYetMending.screens(p,t.base,NotYetBlock.DOOR_OPEN);
            Kept.machine(p,NotYetTexts.DOOR_IS_OPEN);NotYet.told(own,NotYetTexts.DOOR_IS_OPEN);
            told(own,NotYetTexts.DOOR_IS_OPEN,NotYetSite.doorOpen(p.serverLevel(),t.base),"");
        }else{
            var o=others.get(0);var line=NotYetTexts.gotUpLie(o.getGameProfile().getName());
            NotYetMending.screens(p,t.base,NotYetBlock.SOMEONE);
            Kept.machine(p,line);NotYet.told(own,line);
            told(own,line,!lying(o.getUUID()),o.getGameProfile().getName());
        }
    }
    static void hurtOther(ServerPlayer p,Trial t,CompoundTag own,int stroke){
        var l=p.serverLevel();
        var victims=new ArrayList<ServerPlayer>();
        for(var o:others(p,t.base))if(lying(o.getUUID()))victims.add(o);
        if(!victims.isEmpty()){
            for(var o:victims){
                if(stroke==0){var line=NotYetTexts.thatIs(o.getGameProfile().getName());Kept.machine(p,line);NotYet.told(own,line);
                    var oo=NotYetMending.own(o);var them=NotYetTexts.couldStop(p.getGameProfile().getName());Kept.machine(o,them);NotYet.told(oo,them);
                    // 0.4.666: for the accounts: whom it hurt to get this reader up, and for whom that one was hurt.
                    remember(own,"Used",o.getGameProfile().getName());remember(oo,"HurtFor",p.getGameProfile().getName());NotYetMending.save(o,oo);}
                o.invulnerableTime=0;o.hurt(o.damageSources().generic(),3F);
            }
            return;
        }
        // Alone: the machine shows them the man in the long room, on the screen over their cot.
        var screen=t.base.offset(NotYetSite.screen(t.head));
        if(stroke==0){own.putBoolean("ScreenMan",true);NotYetMending.screens(p,t.base,NotYetBlock.NOISE);Kept.machine(p,NotYetTexts.I_WILL_STOP);NotYet.told(own,NotYetTexts.I_WILL_STOP);NotYet.cue(p,NotYetTexts.ON_THE_SCREEN);}
        p.connection.send(new ClientboundSoundPacket(NotYetRegistry.GROAN,SoundSource.HOSTILE,screen.getX()+.5,screen.getY()+.5,screen.getZ()+.5,.8F,.85F+stroke*.04F,p.getRandom().nextLong()));
        if(stroke==2){NotYet.cue(p,NotYetTexts.STAY_DOWN_HEARD);heard(own,NotYetTexts.STAY_DOWN_HEARD);NotYetMending.words(p,t.base,true);}
    }
    /**
     * The death the machine dealt, or any other while they lie there, is refused where they lie: whole again, a seam, NOT YET on every
     * screen, a moment of black. They stay down.
     */
    static void mended(ServerPlayer p,BlockPos b,String cause){
        var own=NotYetMending.own(p);
        int times=own.getInt("Mended")+1;own.putInt("Mended",times);own.putString("LastCause",blow(p.getUUID())?"machine":cause);
        own.putInt("MendedLying",own.getInt("MendedLying")+1);
        NotYetMending.words(p,b,true);NotYetMending.show(p,times);
        Kept.machine(p,NotYetTexts.mended(times));NotYet.told(own,NotYetTexts.mended(times));
        HousePackets.send(p,new HouseFadePayload(2,10,24));
        NotYetMending.save(p,own);
    }
    /** Up off the cot during the trial: it is over, the next is longer, and the machine tells everyone else still lying down. */
    static void gotUp(ServerPlayer p,Trial t){
        TRIALS.remove(t.id);restore(p,t);
        var own=NotYetMending.own(p);own.putInt("GotUp",own.getInt("GotUp")+1);
        Kept.machine(p,NotYetTexts.THERE);NotYet.told(own,NotYetTexts.THERE);
        NotYetMending.save(p,own);
        var name=p.getGameProfile().getName();
        for(var o:List.copyOf(TRIALS.values())){if(!o.base.equals(t.base))continue;var r=p.server.getPlayerList().getPlayer(o.id);if(r==null)continue;
            var ro=NotYetMending.own(r);var line=NotYetTexts.gotUpTrue(name);Kept.machine(r,line);NotYet.told(ro,line);told(ro,line,true,name);NotYetMending.save(r,ro);}
    }
    /** Out of the place while lying (carried off, or the place gone): no trial, no credit, and nothing keeps them down. */
    static void abandon(ServerPlayer p,Trial t){TRIALS.remove(t.id);restore(p,t);if(p.isSleeping())p.stopSleeping();}
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent e){if(e.getEntity() instanceof ServerPlayer p){var t=TRIALS.remove(p.getUUID());if(t!=null)restore(p,t);}}
    /** It looks away. Everything goes quiet at once for this reader, the screens go dark, and the far door opens. */
    static void finish(ServerPlayer p,Trial t){
        TRIALS.remove(t.id);restore(p,t);
        var l=p.serverLevel();var b=t.base;var own=NotYetMending.own(p);
        own.putBoolean("StayedDown",true);own.putLong("StayedAt",l.getGameTime());own.putInt("StayedLength",t.length);own.putInt("QuietVisit",own.getInt("Visits"));
        // 0.4.666: what the account is written from, as the House had it at the moment the machine looked away: what it read of home and
        // of the reader's years, whether they had let the Kept's four go, and who lay down beside them.
        var d=LabyrinthData.get(p.server);
        own.putString("HomeRead",HouseRead.value(d,p.getUUID(),HouseRead.Trait.HOME,true));
        own.putInt("CounterKind",NotYet.counterKind(d,p.getUUID()));
        own.putBoolean("TookFour",NotYet.tookFour(p));
        var beside=new net.minecraft.nbt.ListTag();for(var name:t.beside)beside.add(StringTag.valueOf(name));own.put("Beside",beside);
        LiteraryVignettes.ready(p,PLACE,own,"stayed_down");
        NotYetMending.save(p,own);
        p.connection.send(new ClientboundStopSoundPacket(null,null));
        p.connection.send(new ClientboundSoundPacket(NotYetRegistry.QUIET,SoundSource.AMBIENT,p.getX(),p.getY()+1,p.getZ(),1F,1F,p.getRandom().nextLong()));
        NotYetMending.screens(p,b,NotYetBlock.DARK);
        if(!NotYetSite.doorOpen(l,b)){NotYetSite.door(l,b,true);l.playSound(null,b.offset(NotYetSite.FAR_DOOR),NotYetRegistry.SLIDE.get(),SoundSource.BLOCKS,1F,1F);}
        DOORS.put(b.asLong(),0);
        NotYet.cue(p,NotYetTexts.QUIET);
        // 0.4.666: their cot is made, tight, and kept made; the account is in the drawer under it.
        cot(l,b,t.head,NotYetBlock.MADE);
        l.playSound(null,b.offset(t.head),SoundEvents.CHISELED_BOOKSHELF_PICKUP,SoundSource.BLOCKS,.5F,.7F);
    }
    /** The cot, the lamp, the cold and the screens as they were. */
    static void restore(ServerPlayer p,Trial t){
        var l=p.serverLevel();
        cot(l,t.base,t.head,NotYetBlock.PLAIN);lamp(l,t.base,t.head,false);
        p.setTicksFrozen(0);vents(p,t.base,false);
        var own=NotYetMending.own(p);NotYetMending.words(p,t.base,own.getInt("WordsVisit")==own.getInt("Visits")&&own.getInt("Mended")>0);
    }

    // ------------------------------------------------------------------------------------------------ the far door
    /** An open far door shuts once nobody who stayed down has been in the long room, its doorway or the room past it for five seconds. */
    static void doors(MinecraftServer server){
        ServerLevel l=server.getLevel(NovelRooms.dimension(PLACE));if(l==null){DOORS.clear();return;}
        for(var key:List.copyOf(DOORS.keySet())){
            var b=BlockPos.of(key);boolean near=false;
            for(var p:l.players()){if(p.isSpectator())continue;var r=p.position().subtract(b.getX(),b.getY(),b.getZ());
                if((NotYetSite.inLong(r)||NotYetSite.pastFarDoor(r))&&NotYetMending.own(p).getBoolean("StayedDown"))near=true;}
            int held=near?0:DOORS.get(key)+1;
            if(held>=DOOR_HELD){DOORS.remove(key);if(NotYetSite.doorOpen(l,b)){NotYetSite.door(l,b,false);l.playSound(null,b.offset(NotYetSite.FAR_DOOR),NotYetRegistry.SLIDE.get(),SoundSource.BLOCKS,.9F,.85F);}}
            else DOORS.put(key,held);
        }
    }

    // ------------------------------------------------------------------------------------------------ helpers
    /** The other readers in this ward: not spectators, not the reader. */
    static List<ServerPlayer> others(ServerPlayer p,BlockPos b){
        var out=new ArrayList<ServerPlayer>();
        for(var o:p.serverLevel().players())if(o!=p&&!o.isSpectator()&&b.equals(NotYetMending.inside(o)))out.add(o);
        out.sort(java.util.Comparator.comparing(o->o.getUUID().toString()));
        return out;
    }
    static void cot(ServerLevel l,BlockPos b,BlockPos head,int stage){
        for(var at:new BlockPos[]{b.offset(head),b.offset(NotYetSite.cotFoot(head))}){var s=l.getBlockState(at);if(NotYetBlock.is(s)&&s.getValue(NotYetBlock.STAGE)!=stage)l.setBlock(at,s.setValue(NotYetBlock.STAGE,stage),3);}
    }
    static void lamp(ServerLevel l,BlockPos b,BlockPos head,boolean on){var at=b.offset(NotYetSite.lamp(head));var s=l.getBlockState(at);if(NotYetBlock.is(s,NotYetBlock.Kind.LAMP)&&s.getValue(NotYetBlock.STAGE)!=(on?1:0))l.setBlock(at,s.setValue(NotYetBlock.STAGE,on?1:0),3);}
    /** The ward's grilles, rimed over to this reader alone while the cold comes in; or as they are. */
    static void vents(ServerPlayer p,BlockPos b,boolean rimed){
        var l=p.serverLevel();
        for(var v:NotYetSite.WARD_VENTS){var at=b.offset(v);var s=l.getBlockState(at);if(!NotYetBlock.is(s,NotYetBlock.Kind.VENT))continue;p.connection.send(new ClientboundBlockUpdatePacket(at,rimed?s.setValue(NotYetBlock.STAGE,1):s));}
    }
    /** What this reader heard during the trial, the last twelve, for the account. */
    static void heard(CompoundTag own,String line){var list=own.getList("TrialHeard",net.minecraft.nbt.Tag.TAG_STRING);list.add(StringTag.valueOf(line));while(list.size()>12)list.remove(0);own.put("TrialHeard",list);}
    /** What the machine told this reader of the others, and whether it was so, the last twelve: the House knows both. */
    static void told(CompoundTag own,String line,boolean wasSo,String about){
        var list=own.getList("Lies",net.minecraft.nbt.Tag.TAG_COMPOUND);var row=new CompoundTag();row.putString("Line",line);row.putBoolean("True",wasSo);row.putString("About",about);list.add(row);while(list.size()>12)list.remove(0);own.put("Lies",list);
    }
    /** A name added once to one of the record's lists. */
    static void remember(CompoundTag own,String key,String name){
        var list=own.getList(key,net.minecraft.nbt.Tag.TAG_STRING);for(int i=0;i<list.size();i++)if(list.getString(i).equals(name))return;list.add(StringTag.valueOf(name));own.put(key,list);
    }
    public static List<String> names(CompoundTag own,String key){var list=own.getList(key,net.minecraft.nbt.Tag.TAG_STRING);var out=new ArrayList<String>();for(int i=0;i<list.size();i++)out.add(list.getString(i));return out;}
    public static List<CompoundTag> lies(CompoundTag own){var list=own.getList("Lies",net.minecraft.nbt.Tag.TAG_COMPOUND);var out=new ArrayList<CompoundTag>();for(int i=0;i<list.size();i++)out.add(list.getCompound(i));return out;}
}

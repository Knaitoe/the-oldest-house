package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.journal.Quotes;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 0.4.755, the cold room, part 2 (docs/COLD_ROOM_0_4_755.md): the night in the house. One clock for each copy of the house, running
 * while a living reader is in it.
 *
 * <ul>
 * <li><b>What moves.</b> Every so often, one thing in the back bedroom moves: the bed rises off the boards or comes down again, the
 * drawers slide out, the cross turns on its nail. Each moves only while nobody can see it: no reader, and no spectator's camera, has it
 * in view. Everyone in the house hears it.
 * <li><b>The voice.</b> While a reader is in the bedroom, she speaks to one of them at a time, in turn. What she says is made only from
 * what the House has really kept of that reader: their name, the companions it saw them lose, their deaths and where, a line they wrote
 * in their own journal in the House (only where the House's read is on), and otherwise only what is true in the room. It never invents
 * a history. Each reader hears only their own lines.
 * </ul>
 *
 * When the house has been empty for half a minute, the room is put back: the bed down, the drawers shut, the cross upright.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class ColdNight {
    private ColdNight(){}
    /** Ticks: the first stir, and how often one is tried after it; how long the bed stays up before it may come down; the voice's turn. */
    public static final int FIRST_STIR=300,STIR=200,RISEN=60,VOICE=240,FIRST_VOICE=60,EMPTY=600,VOICE_RITE=160;
    /** Bits of what the voice has used for a reader, kept for the account: their name, a companion, a death, their own words. */
    public static final int NAME=1,COMPANION=2,DEATH=4,WROTE=8;

    public static final class Night {
        int t,empty,stirs,turn,lastStir=-1000,risenAt=-1000;long lastVoice=-1000;
        /** 0.4.756: the rite, one to the house. */
        final ColdRite.Rite rite=new ColdRite.Rite();
    }
    static final Map<Long,Night> NIGHTS=new ConcurrentHashMap<>();
    /** A house built afresh starts a fresh night. */
    public static void forget(BlockPos b){NIGHTS.remove(b.asLong());}
    public static void open(BlockPos b){NIGHTS.computeIfAbsent(b.asLong(),k->new Night());}
    public static @Nullable Night night(BlockPos b){return NIGHTS.get(b.asLong());}
    /** For the tests and the account: the rite in this house, if the house has a night. */
    public static @Nullable ColdRite.Rite rite(BlockPos b){var n=night(b);return n==null?null:n.rite;}
    /** Where the priest is: at the window, kneeling by the bed, or found in the chair. */
    /** 0.4.757: whether the night ended in the morning (her mother then waits on the landing). */
    public static boolean dawn(BlockPos b){var n=night(b);return n!=null&&ColdEnd.MORNING.equals(n.rite.end);}
    public static int priest(BlockPos b){var n=night(b);return n==null?ColdRite.AT_WINDOW:n.rite.priest;}
    /** For the tests: one second of the rite, now. */
    public static void riteNow(ServerLevel l,BlockPos b){var n=night(b);if(n!=null)ColdRite.tick(l,b,n,who(l,b).get(0));}
    public static int stirs(BlockPos b){var n=NIGHTS.get(b.asLong());return n==null?0:n.stirs;}
    @SubscribeEvent public static void stopped(ServerStoppedEvent e){NIGHTS.clear();}

    /** The copy of the house a player is in, if any. */
    public static @Nullable BlockPos site(ServerPlayer p){
        if(!p.level().dimension().equals(NovelRooms.dimension(ColdRoom.PLACE)))return null;
        for(long s:NIGHTS.keySet()){var b=BlockPos.of(s);if(ColdSite.inside(p.position().subtract(b.getX(),b.getY(),b.getZ())))return b;}
        return null;
    }
    static Vec3 rel(ServerPlayer p,BlockPos b){return p.position().subtract(b.getX(),b.getY(),b.getZ());}

    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        if(NIGHTS.isEmpty())return;
        var l=e.getServer().getLevel(NovelRooms.dimension(ColdRoom.PLACE));if(l==null)return;
        for(long s:List.copyOf(NIGHTS.keySet())){
            var b=BlockPos.of(s);var n=NIGHTS.get(s);if(n==null)continue;
            ColdEnd.taken(l,b,n);
            var w=who(l,b);var readers=w.get(0);var watchers=w.get(1);
            if(readers.isEmpty()){if(++n.empty>=EMPTY){putBack(l,b);NIGHTS.remove(s);ColdRoom.ensure(l,b);}continue;}
            n.empty=0;n.t++;
            if(n.t%20==10)ColdRite.tick(l,b,n,readers);
            if(ColdRite.quiet(n)||n.rite.over())continue;
            if(n.t>=FIRST_STIR&&n.t-n.lastStir>=STIR&&n.t%20==0)stir(l,b,n,readers,watchers);
            if(n.t>=FIRST_VOICE&&n.t%20==5)speak(l,b,n,readers);
        }
    }

    /** The house's players now: the readers (alive, not spectators) and everyone who can see (spectators' cameras too). */
    static List<List<ServerPlayer>> who(ServerLevel l,BlockPos b){
        var readers=new ArrayList<ServerPlayer>();var watchers=new ArrayList<ServerPlayer>();
        for(var p:l.players())if(p.isAlive()&&ColdSite.inside(rel(p,b))){watchers.add(p);if(!p.isSpectator())readers.add(p);}
        return List.of(readers,watchers);
    }
    /** For the tests: one stir tried now, whatever the clock says; true if something moved. */
    public static boolean stirNow(ServerLevel l,BlockPos b){open(b);var n=NIGHTS.get(b.asLong());int before=n.stirs;n.risenAt-=RISEN;var w=who(l,b);stir(l,b,n,w.get(0),w.get(1));return n.stirs>before;}
    /** For the tests: her next turn to speak, now. */
    public static void voiceNow(ServerLevel l,BlockPos b){open(b);var n=NIGHTS.get(b.asLong());n.lastVoice=-1000;speak(l,b,n,who(l,b).get(0));}

    // ------------------------------------------------------------------------------------------------ what moves
    /** Whether anyone in the house, a spectator's camera included, could see this point now. */
    public static boolean seen(ServerLevel l,List<ServerPlayer> watchers,Vec3 at){
        for(var p:watchers){
            var eye=p.getEyePosition();double d=eye.distanceTo(at);
            if(d<2)return true;
            if(d>32)continue;
            var to=at.subtract(eye).normalize();
            if(Math.toDegrees(Math.acos(Mth.clamp(p.getLookAngle().dot(to),-1,1)))>70)continue;
            var hit=l.clip(new ClipContext(eye,at,ClipContext.Block.VISUAL,ClipContext.Fluid.NONE,p));
            if(hit.getType()==HitResult.Type.MISS||hit.getLocation().distanceToSqr(at)<.5)return true;
        }
        return false;
    }
    /** One thing moves, if one can move unseen: the bed up, or down after a while up; the drawers out; the cross turned. */
    static void stir(ServerLevel l,BlockPos b,Night n,List<ServerPlayer> readers,List<ServerPlayer> watchers){
        if(n.rite.over())return;
        var head=b.offset(ColdSite.BED_HEAD);var hs=l.getBlockState(head);
        boolean risen=ColdBlock.is(hs,ColdBlock.Kind.BED)&&hs.getValue(ColdBlock.STAGE)>=2;
        var options=new ArrayList<Runnable>();
        var bedAt=Vec3.atCenterOf(head).add(-.5,0,0);
        if(!seen(l,watchers,bedAt)&&!seen(l,watchers,Vec3.atCenterOf(b.offset(ColdSite.BED_FOOT)))){
            if(!risen&&!ColdRite.held(n))options.add(()->{bed(l,b,true);n.risenAt=n.t;});
            else if(n.t-n.risenAt>=RISEN)options.add(()->bed(l,b,false));
        }
        var dresser=b.offset(ColdSite.DRESSER);var ds=l.getBlockState(dresser);
        if(ColdBlock.is(ds,ColdBlock.Kind.DRESSER)&&ds.getValue(ColdBlock.STAGE)==0&&!seen(l,watchers,Vec3.atCenterOf(dresser)))
            options.add(()->{l.setBlock(dresser,ds.setValue(ColdBlock.STAGE,1),Block.UPDATE_CLIENTS);l.playSound(null,dresser,ColdRegistry.DRAWER.get(),SoundSource.BLOCKS,.9F,.9F+l.random.nextFloat()*.2F);});
        var cross=b.offset(ColdSite.CRUCIFIX);var cs=l.getBlockState(cross);
        if(ColdBlock.is(cs,ColdBlock.Kind.CRUCIFIX)&&cs.getValue(ColdBlock.STAGE)==0&&!seen(l,watchers,Vec3.atCenterOf(cross)))
            options.add(()->{l.setBlock(cross,cs.setValue(ColdBlock.STAGE,1),Block.UPDATE_CLIENTS);l.playSound(null,cross,net.minecraft.sounds.SoundEvents.WOOD_HIT,SoundSource.BLOCKS,.6F,.6F);});
        if(options.isEmpty())return;
        options.get(l.random.nextInt(options.size())).run();
        n.lastStir=n.t;n.stirs++;
        var data=LabyrinthData.get(l.getServer());
        for(var p:readers){var own=LiteraryVignettes.personal(data,p.getUUID(),ColdRoom.PLACE);own.putInt("StirsHeard",own.getInt("StirsHeard")+1);LiteraryVignettes.save(data,p.getUUID(),ColdRoom.PLACE,own);}
    }
    /** The bed's head and foot up off the boards together, or down again, with the knock of its legs. */
    static void bed(ServerLevel l,BlockPos b,boolean up){
        boolean moved=false;
        for(var rel:new BlockPos[]{ColdSite.BED_HEAD,ColdSite.BED_FOOT}){
            var at=b.offset(rel);var st=l.getBlockState(at);if(!ColdBlock.is(st,ColdBlock.Kind.BED))continue;
            int stage=st.getValue(ColdBlock.STAGE)%2+(up?2:0);
            if(stage!=st.getValue(ColdBlock.STAGE)){l.setBlock(at,st.setValue(ColdBlock.STAGE,stage),Block.UPDATE_CLIENTS);moved=true;}
        }
        if(moved)l.playSound(null,b.offset(ColdSite.BED_HEAD),ColdRegistry.BED.get(),SoundSource.BLOCKS,1F,up?1F:.85F);
    }
    /** The bedroom as it was: the bed down, the drawers shut, the cross upright. Only where the house is loaded. */
    public static void putBack(ServerLevel l,BlockPos b){
        if(!l.isLoaded(b.offset(ColdSite.BED_HEAD)))return;
        bed(l,b,false);ColdRite.putBack(l,b);
        for(var rel:new BlockPos[]{ColdSite.DRESSER,ColdSite.CRUCIFIX}){var at=b.offset(rel);var st=l.getBlockState(at);if(ColdBlock.is(st)&&st.getValue(ColdBlock.STAGE)!=0)l.setBlock(at,st.setValue(ColdBlock.STAGE,0),Block.UPDATE_CLIENTS);}
    }

    // ------------------------------------------------------------------------------------------------ the voice
    /** Her turn to speak: to the next reader in the bedroom, in turn, the next thing she has for them. */
    static void speak(ServerLevel l,BlockPos b,Night n,List<ServerPlayer> readers){
        if(n.rite.over())return;
        if(n.t-n.lastVoice<(n.rite.on?VOICE_RITE:VOICE)&&n.lastVoice>=0)return;
        var here=new ArrayList<ServerPlayer>();for(var p:readers)if(ColdSite.inBedroom(rel(p,b)))here.add(p);
        if(here.isEmpty())return;
        var to=here.get(Math.floorMod(n.turn++,here.size()));n.lastVoice=n.t;
        say(to,b,here);ColdRite.lose(n,to);
    }
    /** One line to one reader, from what the House has kept of them; the breath under it is theirs alone to hear. */
    public static void say(ServerPlayer p,BlockPos b,List<ServerPlayer> here){
        var data=LabyrinthData.get(p.server);var own=LiteraryVignettes.personal(data,p.getUUID(),ColdRoom.PLACE);
        var lines=lines(p,here);int i=own.getInt("Voice");var line=lines.get(i%lines.size());
        own.putInt("Voice",i+1);own.putInt("VoiceUsed",own.getInt("VoiceUsed")|line.kind());own.putLong("VoiceAt",p.serverLevel().getGameTime());
        LiteraryVignettes.save(data,p.getUUID(),ColdRoom.PLACE,own);
        p.displayClientMessage(Component.literal((i==0?ColdTexts.VOICE_FIRST:ColdTexts.VOICE_AGAIN)+"\""+line.text()+"\"").withStyle(ChatFormatting.DARK_RED,ChatFormatting.ITALIC),false);
        LiteraryVignettes.heard(p,ColdRegistry.VOICE.get(),b.offset(ColdSite.BED_HEAD),.9F,.8F+p.getRandom().nextFloat()*.1F);
    }
    /** A line she has for a reader, and which of the House's records it came from (0 for what is only true in the room). */
    public record Line(String text,int kind){}
    /**
     * Everything she has for this reader, in the order she uses it: their name; the latest companion the House saw them lose; their
     * latest death and how many they have had in the House; a line of their own from their journal; then only what is true in the room.
     */
    public static List<Line> lines(ServerPlayer p,List<ServerPlayer> here){
        var out=new ArrayList<Line>();String name=p.getName().getString();
        out.add(new Line(ColdTexts.voiceName(name,0),NAME));
        var record=HotelVignette.personal(LabyrinthData.get(p.server),p.getUUID());
        var pets=record.getList("LostPets",Tag.TAG_COMPOUND);
        if(!pets.isEmpty()){var pet=pets.getCompound(pets.size()-1);String pname=pet.getString("Name");
            if(!pname.isBlank()){out.add(new Line(ColdTexts.voicePet(pname),COMPANION));if(!pet.getString("Message").isBlank())out.add(new Line(ColdTexts.voicePetDeath(pname,pet.getString("Message")),COMPANION));}}
        var deaths=record.getList("Deaths",Tag.TAG_COMPOUND);
        if(!deaths.isEmpty()){var death=deaths.getCompound(deaths.size()-1);if(!death.getString("Message").isBlank())out.add(new Line(ColdTexts.voiceDeath(death.getString("Message"),death.getString("Place")),DEATH));}
        long inside=record.getLong("HouseDeathsTotal");if(inside>1)out.add(new Line(ColdTexts.voiceDeaths(inside),DEATH));
        if(Quotes.allowed()){var wrote=Quotes.eligible(p.server,p.getUUID());if(!wrote.isEmpty())out.add(new Line(ColdTexts.voiceWrote(wrote.get(wrote.size()-1).text()),WROTE));}
        out.add(new Line(ColdTexts.voiceName(name,1),NAME));
        var others=new ArrayList<String>();for(var q:here)if(q!=p)others.add(q.getName().getString());
        out.add(new Line(others.isEmpty()?ColdTexts.VOICE_ALONE:ColdTexts.voiceOthers(others),0));
        for(String s:ColdTexts.VOICE_ROOM)out.add(new Line(s,0));
        return out;
    }
}

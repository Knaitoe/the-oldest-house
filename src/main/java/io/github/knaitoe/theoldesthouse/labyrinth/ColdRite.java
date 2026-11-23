package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.756, the cold room, part 3 (docs/COLD_ROOM_0_4_756.md): the rite. A hand on the book on the chest of drawers opens it, and the
 * priest kneels by the bed. From then on, for everyone in the house (one rite to a copy of the house):
 *
 * <ul>
 * <li><b>Reading.</b> A reader kneeling (crouched) in the back bedroom with their eyes on the open book reads. While anyone reads, the
 * pages turn, a little faster for each more reader. There are twelve; each page read is said to everyone in the room.
 * <li><b>Interruption.</b> When she speaks to a reader, that reader loses their place, and reads again only once they have looked back
 * at the page after a moment.
 * <li><b>The cold.</b> It rises while nobody reads and falls while someone does. Breath shows; the frost comes into the hands; at its
 * worst it hurts, but never takes the last three hearts and never freezes anyone through.
 * <li><b>The water.</b> The flask on the bedside table holds six. A sprinkle over the bed quiets her for five seconds: no voice, nothing
 * moves, the cold holds where it is.
 * <li><b>Holding her.</b> A reader crouched at the bedside holds her down; while anyone holds her the bed does not rise, and if it is up
 * it comes down under their hands. A reader alone reads and sprinkles in turn.
 * <li><b>The priest.</b> He kneels and reads with them. At the eleventh page his heart gives out; he is found in the chair by the bed and
 * does not answer. The readers read on alone. The twelfth page is blank (part 4 brings what is said there).
 * </ul>
 */
public final class ColdRite {
    private ColdRite(){}
    public static final int PAGES=12,PAGE=160,INTERRUPT=40,QUIET=100,WATER=6,PRIEST_GIVES_OUT=11;
    /** Where the priest kneels, between the bed's foot and the flask's table, facing the bed; and where he is found after. */
    public static final BlockPos KNEEL=new BlockPos(1,5,-18);
    /** The priest: at the window, kneeling by the bed, or found in the chair. */
    public static final int AT_WINDOW=0,KNEELING=1,DOWN=2;

    public static final class Rite {
        public boolean on;public double progress;public int page,water=WATER,priest=AT_WINDOW;public double cold;public int quietUntil=-1;
        final Map<UUID,Integer> lost=new HashMap<>();
        /** For the tests: this reader's place found, as if they had looked back. */
        public void lostClear(ServerPlayer p){lost.remove(p.getUUID());}final Set<UUID> holding=new HashSet<>();final Map<UUID,Integer> felt=new HashMap<>();
    }

    public static boolean quiet(ColdNight.Night n){return n.rite.on&&n.t<n.rite.quietUntil;}

    /** A hand on the book: the rite begins, or the reader is told where it has got to. */
    public static void book(ServerPlayer p,BlockPos b,net.minecraft.nbt.CompoundTag own){
        var l=p.serverLevel();ColdNight.open(b);var n=ColdNight.night(b);var r=n.rite;
        if(r.on){ColdRoom.cue(p,r.page>=PAGES?ColdTexts.LAST_PAGE:ColdTexts.bookAt(r.page+1));return;}
        r.on=true;
        var at=b.offset(ColdSite.BOOK);var st=l.getBlockState(at);
        if(ColdBlock.is(st,ColdBlock.Kind.BOOK))l.setBlock(at,st.setValue(ColdBlock.STAGE,1),Block.UPDATE_CLIENTS);
        l.playSound(null,at,SoundEvents.BOOK_PAGE_TURN,SoundSource.BLOCKS,1F,.8F);
        r.priest=KNEELING;var priest=ColdRoom.find(l,b,ColdRoom.PRIEST);if(priest!=null)ColdRoom.place(priest,b,ColdRoom.PRIEST);
        for(var q:ColdNight.who(l,b).get(0))q.displayClientMessage(Component.literal(ColdTexts.RITE_BEGINS).withStyle(ChatFormatting.GRAY),false);
        own.putInt("BeganRite",own.getInt("BeganRite")+1);
    }
    /** A hand on the flask's table during the rite: one sprinkle, while there is water. True if it was the rite's to answer. */
    public static boolean flask(ServerPlayer p,BlockPos b,net.minecraft.nbt.CompoundTag own){
        var n=ColdNight.night(b);if(n==null||!n.rite.on)return false;var r=n.rite;var l=p.serverLevel();
        if(r.water<=0){ColdRoom.cue(p,ColdTexts.FLASK_EMPTY);return true;}
        r.water--;r.quietUntil=n.t+QUIET;
        var bed=Vec3.atCenterOf(b.offset(ColdSite.BED_HEAD)).add(-.5,.3,0);
        l.sendParticles(ParticleTypes.SPLASH,bed.x,bed.y,bed.z,24,.6,.1,.4,.1);
        l.playSound(null,b.offset(ColdSite.FLASK_TABLE),SoundEvents.BOTTLE_EMPTY,SoundSource.PLAYERS,.8F,1.2F);
        ColdRoom.cue(p,ColdTexts.SPRINKLE);own.putInt("Sprinkled",own.getInt("Sprinkled")+1);
        return true;
    }
    /** She spoke to this reader: their place in the book is lost. */
    public static void lose(ColdNight.Night n,ServerPlayer p){
        if(!n.rite.on||n.rite.page>=PAGES)return;
        if(n.rite.lost.put(p.getUUID(),n.t)==null)ColdRoom.cue(p,ColdTexts.LOST_PLACE);
    }
    static boolean crouched(ServerPlayer p){return p.isShiftKeyDown()||p.isCrouching();}
    /** How far off where a reader looks something is, in degrees. */
    static double off(ServerPlayer p,Vec3 at){var to=at.subtract(p.getEyePosition());if(to.lengthSqr()<1e-6)return 0;return Math.toDegrees(Math.acos(Mth.clamp(p.getLookAngle().dot(to.normalize()),-1,1)));}
    static Vec3 bookAt(BlockPos b){return Vec3.atCenterOf(b.offset(ColdSite.BOOK)).add(0,-.4,0);}
    static Vec3 bedAt(BlockPos b){return Vec3.atBottomCenterOf(b.offset(ColdSite.BED_HEAD)).add(-.5,.6,0);}
    /** A reader kneeling at the bedside, within reach of her: holding her down. */
    public static boolean holds(ServerPlayer p,BlockPos b){
        if(p.isSpectator()||!crouched(p)||!ColdSite.inBedroom(ColdNight.rel(p,b)))return false;
        var bed=bedAt(b);return Math.abs(p.getX()-bed.x)<2.2&&Math.abs(p.getZ()-bed.z)<1.7;
    }
    /** A reader kneeling in the bedroom with their eyes on the open book, and their place not lost. */
    public static boolean reads(ColdNight.Night n,ServerPlayer p,BlockPos b){
        var r=n.rite;if(!r.on||r.page>=PAGES||p.isSpectator()||!crouched(p)||!ColdSite.inBedroom(ColdNight.rel(p,b)))return false;
        boolean looking=p.getEyePosition().distanceTo(bookAt(b))<7&&off(p,bookAt(b))<35;
        Integer lost=r.lost.get(p.getUUID());
        if(lost!=null){if(n.t-lost<INTERRUPT||!looking)return false;r.lost.remove(p.getUUID());ColdRoom.cue(p,ColdTexts.FOUND_PLACE);}
        return looking;
    }
    public static boolean held(ColdNight.Night n){return n.rite.on&&!n.rite.holding.isEmpty();}

    /** Every second of the rite: who reads and who holds her, the pages, the cold. */
    static void tick(ServerLevel l,BlockPos b,ColdNight.Night n,List<ServerPlayer> readers){
        var r=n.rite;if(!r.on)return;
        int reading=0;var nowHolding=new HashSet<UUID>();
        for(var p:readers){
            if(reads(n,p,b)){reading++;mark(p,"ReadSeconds",1);}
            if(holds(p,b)){nowHolding.add(p.getUUID());if(!r.holding.contains(p.getUUID()))ColdRoom.cue(p,ColdTexts.HOLD);mark(p,"HeldSeconds",1);}
        }
        r.holding.clear();r.holding.addAll(nowHolding);
        if(!r.holding.isEmpty()){var hs=l.getBlockState(b.offset(ColdSite.BED_HEAD));if(ColdBlock.is(hs,ColdBlock.Kind.BED)&&hs.getValue(ColdBlock.STAGE)>=2)ColdNight.bed(l,b,false);}
        if(r.page<PAGES&&reading>0){
            r.progress+=20*(1+.5*(reading-1));
            while(r.page<PAGES&&r.progress>=(r.page+1)*PAGE)turn(l,b,n,readers);
        }
        if(!quiet(n))r.cold=Mth.clamp(r.cold+(reading>0?-3:4),0,100);
        for(var p:readers)cold(l,n,p);
    }
    /** A page read: said to everyone in the room; at the eleventh the priest gives out; the twelfth is blank. */
    static void turn(ServerLevel l,BlockPos b,ColdNight.Night n,List<ServerPlayer> readers){
        var r=n.rite;r.page++;
        l.playSound(null,b.offset(ColdSite.BOOK),SoundEvents.BOOK_PAGE_TURN,SoundSource.BLOCKS,1F,1F);
        String line=r.page>=PAGES?ColdTexts.LAST_PAGE:ColdTexts.read(r.page,r.priest==KNEELING);
        for(var p:readers)if(ColdSite.inBedroom(ColdNight.rel(p,b))){
            p.displayClientMessage(Component.literal(line).withStyle(ChatFormatting.GRAY),false);
            var data=LabyrinthData.get(p.server);var own=LiteraryVignettes.personal(data,p.getUUID(),ColdRoom.PLACE);
            own.putInt("Pages",Math.max(own.getInt("Pages"),r.page));LiteraryVignettes.save(data,p.getUUID(),ColdRoom.PLACE,own);
        }
        if(r.page==PRIEST_GIVES_OUT&&r.priest!=DOWN){
            r.priest=DOWN;var priest=ColdRoom.find(l,b,ColdRoom.PRIEST);if(priest!=null)ColdRoom.place(priest,b,ColdRoom.PRIEST);
            for(var p:readers)if(ColdSite.inside(ColdNight.rel(p,b)))p.displayClientMessage(Component.literal(ColdTexts.PRIEST_DOWN).withStyle(ChatFormatting.GRAY),false);
        }
    }
    /** The cold on one reader: breath, frost in the hands, and at its worst a hurt that never takes the last three hearts. */
    static void cold(ServerLevel l,ColdNight.Night n,ServerPlayer p){
        double c=n.rite.cold;
        if(c>25){var eye=p.getEyePosition().add(p.getLookAngle().scale(.35));l.sendParticles(ParticleTypes.POOF,eye.x,eye.y-.1,eye.z,1,.03,.02,.03,.002);}
        // Native frost, held short of fully frozen (where the game's own freezing hurts).
        int frozen=(int)Math.min(p.getTicksRequiredToFreeze()-1,c*1.35);
        if(frozen>p.getTicksFrozen())p.setTicksFrozen(frozen);
        int was=n.rite.felt.getOrDefault(p.getUUID(),0),now=c>=75?2:c>=40?1:0;
        if(now>was){ColdRoom.cue(p,now==2?ColdTexts.COLD_HANDS:ColdTexts.COLD_BREATH);n.rite.felt.put(p.getUUID(),now);}
        if(now<was)n.rite.felt.put(p.getUUID(),now);
        if(c>=75&&n.t%40==10&&p.getHealth()>7)p.hurt(p.damageSources().freeze(),1);
    }
    /** The room as it was: the book shut. The rite itself goes with the night. */
    static void putBack(ServerLevel l,BlockPos b){var at=b.offset(ColdSite.BOOK);var st=l.getBlockState(at);if(ColdBlock.is(st,ColdBlock.Kind.BOOK)&&st.getValue(ColdBlock.STAGE)!=0)l.setBlock(at,st.setValue(ColdBlock.STAGE,0),Block.UPDATE_CLIENTS);}
    static void mark(ServerPlayer p,String key,int by){var data=LabyrinthData.get(p.server);var own=LiteraryVignettes.personal(data,p.getUUID(),ColdRoom.PLACE);own.putInt(key,own.getInt(key)+by);LiteraryVignettes.save(data,p.getUUID(),ColdRoom.PLACE,own);}
}

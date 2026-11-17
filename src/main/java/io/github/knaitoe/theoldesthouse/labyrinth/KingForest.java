package io.github.knaitoe.theoldesthouse.labyrinth;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.647, A sort of thing, part 7 (docs/SLEEPING_KING_0_4_647.md): the forest on the seventh square, where the land is ruled and a
 * reader in it is a line.
 *
 * <p>The Knight waits on his horse beside the square's peg and rides with the readers to the last brook. He is a drawing until somebody
 * looks at him; then he fills in, and while he is filled in he looks at them in turn, which makes a line a sort of thing (KingBreath's
 * notice). Nobody looking for a while, he thins, and falls off his horse into the path behind them, and lies there a drawing until
 * somebody comes back and looks at him long enough to get him up. So each keeps the other.
 *
 * <p>He sings his song if he is asked, and a reader who has heard it can sing it afterwards (crouching still, KingForest.reader). On the
 * last peg a reader he looks at, and who has eaten a bite of cake, is a thing and counted; he keeps a cake on his saddle for a reader
 * who has none. At the last brook he asks to be watched as far as the turn in the road and rides off along it: watched the whole way he
 * goes round it filled in; let go, he is a drawing before he gets there. Either way he is gone for the rest of the dream.
 */
public final class KingForest {
    private KingForest(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.SLEEPING_KING;
    /** Stranger phase: the Knight, on his horse or off it. */
    public static final int KNIGHT=227;
    /** Gestures, as bits: filled in (1), off his horse (2), moving (4). */
    public static final int FILLED=1,FALLEN=2,MOVING=4;
    /** Ticks nobody looks at him (riding, with a reader on the square) before he falls; ticks of being looked at to get him up again. */
    public static final int THINS=60,GETS_UP=40;
    /** Game ticks a reader who has the song crouches still before they sing it. */
    static final int STILL=12;
    /** How fast he rides beside a reader, and out along the turn. */
    static final double PACE=.2,OUT_PACE=.12;
    /** Keys in each reader's own record. */
    public static final String SONG="KingHeardSong",SANG="KingSangAt",TURN="KingKnightTurn",FELL="KingKnightFell",KNIGHT_CAKE="KingKnightCake",
        RAISED="KingKnightRaised",MET="KingKnightMet",SANG_TO_HIM="KingSangToHim";
    /** The turn, as kept: watched round it (1), went on (2). */
    public static final int WATCHED=1,WENT_ON=2;

    public static boolean is(LiteraryActor a){return a.role()==LiteraryActor.STRANGER&&a.phase()==KNIGHT;}
    public static boolean filled(LiteraryActor a){return (a.gesture()&FILLED)!=0;}
    public static boolean fallen(LiteraryActor a){return (a.gesture()&FALLEN)!=0;}
    public static @Nullable LiteraryActor find(ServerLevel l,BlockPos b){
        var at=b.offset(KingSite.KNIGHT_WAIT);
        for(var a:l.getEntitiesOfClass(LiteraryActor.class,new AABB(at).inflate(40,10,40),KingForest::is))if(a.isAlive())return a;
        return null;
    }
    /** The Knight on his horse at the peg, unless he has already gone round the turn in this dream. */
    static void ensure(ServerLevel l,BlockPos b,KingSleep.Sleep s){
        if(s.knightGone)return;
        var at=b.offset(KingSite.KNIGHT_WAIT);
        if(!l.isLoaded(at)||!l.areEntitiesLoaded(ChunkPos.asLong(at))||find(l,b)!=null)return;
        var a=LiteraryRegistry.ACTOR.get().create(l);if(a==null)return;
        a.bind(PLACE,null);a.appearance(LiteraryActor.STRANGER,KNIGHT);a.gesture(0);
        a.moveTo(at.getX()+.5,at.getY(),at.getZ()+.5,180,0);a.setYBodyRot(180);a.setYHeadRot(180);
        l.addFreshEntity(a);
    }
    /** A fresh dream: the Knight back at the peg, on his horse and a drawing. */
    static void reset(ServerLevel l,BlockPos b){
        var a=find(l,b);if(a==null)return;var at=KingSite.KNIGHT_WAIT;
        a.gesture(0);a.moveTo(b.getX()+at.getX()+.5,b.getY(),b.getZ()+at.getZ()+.5,180,0);
    }
    /** For tests: whether he has asked to be watched to the turn in this dream; whether he has gone round it; how often he has fallen. */
    public static boolean asked(BlockPos b){return KingSleep.get(b).knightAsked>0;}
    public static boolean gone(BlockPos b){return KingSleep.get(b).knightGone;}
    public static int falls(BlockPos b){return KingSleep.get(b).knightFalls;}
    /** For tests: the readers that the Knight would see now (those looking at him). */
    public static java.util.List<ServerPlayer> watchers(ServerLevel l,BlockPos b,LiteraryActor knight){
        var out=new java.util.ArrayList<ServerPlayer>();
        for(var p:KingSleep.readers(l,b))if(p.distanceTo(knight)<32&&KingBreath.looksAt(p,knight))out.add(p);
        return out;
    }

    // ------------------------------------------------------------------------------------------------ once a tick, while anybody is there
    static void tick(ServerLevel l,BlockPos b,KingSleep.Sleep s){
        ensure(l,b,s);var k=find(l,b);if(k==null)return;long now=l.getGameTime();
        var watching=watchers(l,b,k);boolean seen=!watching.isEmpty();
        if(seen){s.knightUnseen=0;s.knightSeen++;for(var p:watching)s.knightMet.add(p.getUUID());}else{s.knightUnseen++;s.knightSeen=0;}
        int g=k.gesture()&~(FILLED|MOVING);if(seen)g|=FILLED;
        // Filled in, he looks at the nearest of those looking at him: that is the look that counts (KingBreath.lookedAtByTheDream, which
        // takes a reader's middle). Lying in the path he looks at nobody; riding, he turns his head and the horse keeps its way.
        ServerPlayer near=null;
        if(seen&&!fallen(k)){double best=Double.MAX_VALUE;for(var p:watching){double d=p.distanceToSqr(k);if(d<best){best=d;near=p;}}}
        // Riding out along the turn in the road.
        if(s.knightRideFrom>0&&now>=s.knightRideFrom){
            if(!seen)s.knightThinned=true;
            var end=KingSite.TURN_END;var to=new Vec3(b.getX()+end.x,b.getY(),b.getZ()+end.z).subtract(k.position());
            if(to.horizontalDistance()<.3){gone(l,b,s,k);return;}
            var step=to.normalize().scale(Math.min(OUT_PACE,to.length()));
            float yaw=(float)(Math.toDegrees(Math.atan2(step.z,step.x))-90);
            k.moveTo(k.getX()+step.x,b.getY(),k.getZ()+step.z,yaw,0);k.setYBodyRot(yaw);if(!seen)k.setYHeadRot(yaw);
            if(near!=null)lookAt(k,near,true);
            k.gesture(g|MOVING);return;
        }
        if((k.gesture()&FALLEN)!=0){
            // Off his horse: a drawing in the path until somebody looks at him long enough.
            if(s.knightSeen>=GETS_UP){k.gesture(g&~FALLEN);s.knightUnseen=0;say(k,KingTexts.KNIGHT_UP);for(var p:watching)s.knightRaisedBy.add(p.getUUID());}else k.gesture(g);
            return;
        }
        // Riding with the readers on the square: beside and a little behind the one furthest down the road.
        ServerPlayer lead=null;
        for(var p:KingSleep.readers(l,b)){var r=p.position().subtract(b.getX(),b.getY(),b.getZ());if(KingSite.rank((int)Math.floor(r.z))!=7&&!(r.z<KingSite.rankNear(7)+1&&r.z>KingSite.rankFar(7)-2))continue;if(lead==null||p.getZ()<lead.getZ())lead=p;}
        if(lead==null){if(near!=null)lookAt(k,near,false);k.gesture(g);return;}
        // Nobody looking at him for long enough, with a reader on the square: he thins and falls off his horse.
        if(s.knightUnseen>=THINS&&s.knightAsked==0){
            k.gesture(g|FALLEN);s.knightFalls++;
            for(var p:KingSleep.readers(l,b)){if(p.distanceTo(k)<40){s.knightFellFor.add(p.getUUID());King.cue(p,KingTexts.KNIGHT_FELL);}}
            l.playSound(null,k.getX(),k.getY(),k.getZ(),KingRegistry.KNIGHT_FALL.get(),SoundSource.NEUTRAL,.8F,1);
            return;
        }
        var lr=lead.position().subtract(b.getX(),b.getY(),b.getZ());
        // A pace east of the road and a little behind the lead, kept on the square (never back over the brook behind it).
        double tz=Mth.clamp(lr.z+2.5,KingSite.TURN_Z+.5,KingSite.rankNear(7)-.5),tx=KingSite.KNIGHT_LANE;
        var to=new Vec3(b.getX()+tx,b.getY(),b.getZ()+tz).subtract(k.position());
        if(to.horizontalDistance()>.4){
            var step=to.normalize().scale(Math.min(PACE,to.horizontalDistance()));
            float yaw=(float)(Math.toDegrees(Math.atan2(step.z,step.x))-90);
            k.moveTo(k.getX()+step.x,b.getY(),k.getZ()+step.z,yaw,0);k.setYBodyRot(yaw);if(!seen)k.setYHeadRot(yaw);g|=MOVING;
        }
        if(near!=null)lookAt(k,near,(g&MOVING)!=0);
        k.gesture(g);
        // At the last brook, once: he asks to be watched as far as the turn, then rides off along it.
        if(s.knightAsked==0&&lr.z<=KingSite.TURN_Z+1.5&&k.distanceTo(lead)<8){
            s.knightAsked=now;s.knightRideFrom=now+40;s.knightThinned=false;
            say(k,KingTexts.KNIGHT_TURN_ASK);for(var p:KingSleep.readers(l,b))if(p.distanceTo(k)<24)s.knightAskedOf.add(p.getUUID());
        }
    }
    /** His look at a reader's middle: the whole of him turned when he stands, only his head while the horse is going somewhere. */
    static void lookAt(LiteraryActor k,ServerPlayer p,boolean headOnly){
        if(!headOnly){KingCourt.look(k,p.getBoundingBox().getCenter());return;}
        var to=p.getBoundingBox().getCenter().subtract(k.getEyePosition());
        k.setYHeadRot((float)(Math.toDegrees(Math.atan2(to.z,to.x))-90));k.setXRot(Mth.clamp((float)-Math.toDegrees(Math.atan2(to.y,Math.hypot(to.x,to.z))),-70,70));
    }
    /** Round the turn: gone for the rest of this dream, filled in if he was watched the whole way. */
    static void gone(ServerLevel l,BlockPos b,KingSleep.Sleep s,LiteraryActor k){
        s.knightGone=true;s.knightRideFrom=0;
        for(var id:s.knightAskedOf)(s.knightThinned?s.leftKnight:s.sawKnightGo).add(id);
        for(var p:KingSleep.readers(l,b))if(s.knightAskedOf.contains(p.getUUID()))King.cue(p,s.knightThinned?KingTexts.KNIGHT_THINNED:KingTexts.KNIGHT_ROUND);
        k.discard();
    }

    // ------------------------------------------------------------------------------------------------ each reader, in their own tick
    static void reader(ServerPlayer p,BlockPos b,CompoundTag own,int state){
        var s=KingSleep.get(b);var l=p.serverLevel();long now=l.getGameTime();
        if(s.knightFellFor.contains(p.getUUID()))own.putBoolean(FELL,true);
        if(s.knightRaisedBy.contains(p.getUUID()))own.putBoolean(RAISED,true);
        if(s.knightMet.contains(p.getUUID())&&!own.getBoolean(MET)){own.putBoolean(MET,true);King.cue(p,KingTexts.KNIGHT_LOOKED);}
        if(own.getInt(TURN)==0&&s.sawKnightGo.contains(p.getUUID()))own.putInt(TURN,WATCHED);
        if(own.getInt(TURN)==0&&s.leftKnight.contains(p.getUUID()))own.putInt(TURN,WENT_ON);
        // A reader who has heard his song sings it, crouching still: heard by everybody near, no oftener than every twenty seconds.
        // 0.4.648: by game time, a little over half a second (a reader's own part comes only every quarter second).
        if(own.getBoolean(SONG)&&p.isShiftKeyDown()&&p.getDeltaMovement().horizontalDistanceSqr()<1e-4){
            if(!own.contains("KingStillFrom"))own.putLong("KingStillFrom",now);
            if(now-own.getLong("KingStillFrom")>=STILL&&!own.getBoolean("KingStillSang")&&(!own.contains(SANG)||now-own.getLong(SANG)>=400)){
                own.putBoolean("KingStillSang",true);
                own.putLong(SANG,now);own.putBoolean("KingSang",true);
                l.playSound(null,p.getX(),p.getY(),p.getZ(),KingRegistry.KNIGHT_SONG.get(),SoundSource.PLAYERS,.7F,1.05F);
                King.cue(p,KingTexts.SANG);
                // Sung beside him, it settles him, once a dream. 0.4.648: sung by a reader in check who has taken the crown off, it is the
                // last song: he settles all the way, and the country fills out to its edges behind them as they go.
                boolean beside=KingBreath.beside(p.position().subtract(b.getX(),b.getY(),b.getZ()));
                if(beside&&own.getBoolean(KingSleep.CHECKED)&&!KingCrownItem.crowned(p)&&KingSleep.lastSong(l,b)){own.putBoolean(KingSleep.LAST_SONG,true);own.putBoolean(SANG_TO_HIM,true);}
                else if(beside&&KingSleep.settleBySong(l,b))own.putBoolean(SANG_TO_HIM,true);
            }
        }else{own.remove("KingStillFrom");own.remove("KingStillSang");}
    }

    // ------------------------------------------------------------------------------------------------ talking to him
    public static void talk(ServerPlayer p,LiteraryActor a,CompoundTag own){
        var b=LiteraryVignettes.base(p,PLACE);if(b==null)return;
        Talk.begin(p,"king_knight",KingTexts.KNIGHT_NAME);
        if(fallen(a)){line(p,a,KingTexts.KNIGHT_DOWN);Talk.end(p);return;}
        line(p,a,own.getBoolean(SONG)?KingTexts.KNIGHT_AGAIN:KingTexts.KNIGHT_HELLO);
        var asks=new java.util.ArrayList<Talk.Reply>();
        asks.add(Talk.reply(KingTexts.R_SING,KingForest::sing));
        if(!hasCake(p))asks.add(Talk.reply(KingTexts.R_CAKE,KingForest::cake));
        asks.add(Talk.reply(KingTexts.R_RIDE_ON,q->{var k=find(q.serverLevel(),b);if(k!=null)line(q,k,KingTexts.KNIGHT_RIDE_ON);Talk.end(q);}));
        Talk.ask(p,asks.toArray(Talk.Reply[]::new));
    }
    static boolean hasCake(ServerPlayer p){
        var inv=p.getInventory();
        for(int i=0;i<inv.getContainerSize();i++){var st=inv.getItem(i);if(st.getItem() instanceof KingDraughtItem d&&d.more())return true;}
        return false;
    }
    /** His song: the eight bars, and the words of it on the panel. Heard once, the reader has it. */
    public static void sing(ServerPlayer p){
        var b=LiteraryVignettes.base(p,PLACE);if(b==null){Talk.end(p);return;}
        var k=find(p.serverLevel(),b);if(k==null){Talk.end(p);return;}
        p.serverLevel().playSound(null,k.getX(),k.getY()+1.5,k.getZ(),KingRegistry.KNIGHT_SONG.get(),SoundSource.NEUTRAL,1,1);
        for(var verse:KingTexts.SONG_WORDS)line(p,k,verse);
        var d=LabyrinthData.get(p.server);var own=LiteraryVignettes.personal(d,p.getUUID(),PLACE);own.putBoolean(SONG,true);LiteraryVignettes.save(d,p.getUUID(),PLACE,own);
        Talk.end(p);
    }
    /** A cake from the box on his saddle, for a reader who has none. */
    public static void cake(ServerPlayer p){
        var b=LiteraryVignettes.base(p,PLACE);if(b==null){Talk.end(p);return;}
        var k=find(p.serverLevel(),b);
        if(!hasCake(p)){
            var cake=KingDraughtItem.own(true);if(!p.getInventory().add(cake))p.drop(cake,false);
            var d=LabyrinthData.get(p.server);var own=LiteraryVignettes.personal(d,p.getUUID(),PLACE);own.putBoolean(KNIGHT_CAKE,true);LiteraryVignettes.save(d,p.getUUID(),PLACE,own);
        }
        if(k!=null)line(p,k,KingTexts.KNIGHT_CAKE);
        Talk.end(p);
    }
    static void say(LiteraryActor a,String line){King.aloud(a,KingTexts.KNIGHT_NAME,line);}
    static void line(ServerPlayer p,LiteraryActor a,String line){Talk.line(p,KingTexts.KNIGHT_NAME,line);say(a,line);}
}

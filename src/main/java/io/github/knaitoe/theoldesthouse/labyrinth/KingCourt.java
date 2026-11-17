package io.github.knaitoe.theoldesthouse.labyrinth;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.646, A sort of thing, part 6 (docs/SLEEPING_KING_0_4_646.md): the court of cards on the sixth square.
 *
 * <p>The herald waits at the far bank of the drawn bridge and reads each reader's name off his scroll as they come on to the square.
 * Named, they are a thing for as long as the court attends to them (KingBreath), and a thing cannot get between the pack's guard, who
 * stand shoulder to shoulder across the middle file just short of the brook (KingCardBlock). The Queen of Hearts sentences them first;
 * nothing is ever carried out. There are three ways past the guard, and which a reader took is kept as {@link #WAY}:
 * <ul>
 * <li><b>The Cat's.</b> Its head is in the tree on the lawn, and it will come and be looked at instead. While the court argues over
 * whether a head with no body can be taken off, nobody attends to the reader, who is what the land makes of them and, a sort of thing,
 * goes out between the guard edge-on.</li>
 * <li><b>The bottle's.</b> A swallow takes them a step under what the court holds them to, and they go out the same way without the
 * Cat.</li>
 * <li><b>The cards'.</b> Too much of a thing (the cake), they say what the court is, and the pack goes up and comes down as cards on
 * the grass. The King stirs once, a long way off. After that nobody is held, and later readers find a pack of cards on the grass.</li>
 * </ul>
 */
public final class KingCourt {
    private KingCourt(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.SLEEPING_KING;
    /** Stranger phases: the King of Hearts, the Queen of Hearts, the herald, the Cat. */
    public static final int KING=223,QUEEN=224,HERALD=225,CAT=226;
    /** Gestures: standing (0); the herald with his scroll up, the Queen pointing at the dock (1); everybody looking up at the Cat (2). */
    public static final int STANDING=0,READING=1,LOOKING_UP=2;
    /** The ways out: by the Cat, by the bottle, by saying what the court is, or after another reader had. */
    public static final int BY_CAT=1,BY_BOTTLE=2,BY_CARDS=3,ALREADY_CARDS=4;
    static final String SUMMONED="KingSummoned",WAY="KingCourtWay",SENTENCED="KingSentenced",CALLED="KingCalledCat";
    /** How long the court attends to the Cat once it is over them, and the turns of their argument. */
    public static final int ATTEND=600;
    static final int[] ARGUMENT={30,130,230,330};

    public static boolean is(LiteraryActor a){return a.role()==LiteraryActor.STRANGER&&(a.phase()==KING||a.phase()==QUEEN||a.phase()==HERALD);}
    public static boolean cat(LiteraryActor a){return a.role()==LiteraryActor.STRANGER&&a.phase()==CAT;}
    public static @Nullable LiteraryActor find(ServerLevel l,BlockPos b,int phase){
        var at=phase==CAT?b.offset(KingSite.CAT_TREE):b.offset(KingSite.COURT_KING);
        for(var a:l.getEntitiesOfClass(LiteraryActor.class,new AABB(at).inflate(32,10,32),a->a.role()==LiteraryActor.STRANGER&&a.phase()==phase))if(a.isAlive())return a;
        return null;
    }
    static BlockPos spot(int phase){return phase==KING?KingSite.COURT_KING:phase==QUEEN?KingSite.COURT_QUEEN:KingSite.HERALD;}
    /** The court standing in their places (unless the pack has come down), and the Cat in its tree. */
    static void ensure(ServerLevel l,BlockPos b,KingSleep.Sleep s){
        // A dream that has forgotten the pack came down (a restart) stands it up again.
        var guard=b.offset(0,0,KingSite.GUARD_ROW);
        if(!s.courtFallen&&l.isLoaded(guard)&&KingCardBlock.is(l.getBlockState(guard))&&!KingCardBlock.standing(l.getBlockState(guard)))KingSite.court(l,b);
        if(!s.courtFallen)for(int phase:new int[]{KING,QUEEN,HERALD}){
            var at=b.offset(spot(phase));
            if(!l.isLoaded(at)||!l.areEntitiesLoaded(ChunkPos.asLong(at))||find(l,b,phase)!=null)continue;
            var a=LiteraryRegistry.ACTOR.get().create(l);if(a==null)continue;
            a.bind(PLACE,null);a.appearance(LiteraryActor.STRANGER,phase);
            // The herald's scroll, a sheet in his hand that he holds up to read the names from.
            if(phase==HERALD)a.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.PAPER));
            float yaw=phase==HERALD?-90:0;
            a.moveTo(at.getX()+.5,at.getY(),at.getZ()+.5,yaw,0);a.setYBodyRot(yaw);a.setYHeadRot(yaw);
            l.addFreshEntity(a);
        }
        var tree=b.offset(KingSite.CAT_TREE);
        if(l.isLoaded(tree)&&l.areEntitiesLoaded(ChunkPos.asLong(tree))&&find(l,b,CAT)==null){
            var a=LiteraryRegistry.ACTOR.get().create(l);
            if(a!=null){a.bind(PLACE,null);a.appearance(LiteraryActor.STRANGER,CAT);var at=KingSite.CAT_IN_TREE;a.moveTo(b.getX()+at.x,b.getY()+at.y,b.getZ()+at.z,90,0);a.setYBodyRot(90);a.setYHeadRot(90);l.addFreshEntity(a);}
        }
    }
    /** A fresh dream: the pack standing and the court in its places, the Cat in its tree. */
    static void reset(ServerLevel l,BlockPos b){
        if(l.isLoaded(b.offset(0,0,KingSite.GUARD_ROW)))KingSite.court(l,b);
        var cat=find(l,b,CAT);if(cat!=null){var at=KingSite.CAT_IN_TREE;cat.moveTo(b.getX()+at.x,b.getY()+at.y,b.getZ()+at.z,90,0);cat.setInvisible(false);}
    }

    /** For tests: the Cat goes back to its tree at once. */
    public static void forgetCat(BlockPos b){KingSleep.get(b).catUntil=0;}
    /** Whether the Cat is over the court now, and the court attending to it. */
    public static boolean attendingToCat(ServerLevel l,BlockPos b){var s=KingSleep.get(b);return !s.courtFallen&&s.catUntil>l.getGameTime();}
    /** Whether the court is holding this reader: named, not yet out, the pack still standing, and nobody looking at the Cat. */
    public static boolean holding(ServerPlayer p,BlockPos b,CompoundTag own){
        if(!own.getBoolean(SUMMONED)||own.getInt(WAY)!=0)return false;
        var s=KingSleep.get(b);if(s.courtFallen||s.catUntil>p.level().getGameTime())return false;
        return KingSite.rank((int)Math.floor(p.getZ()-b.getZ()))==6;
    }

    // ------------------------------------------------------------------------------------------------ once a tick, while anybody is there
    static void tick(ServerLevel l,BlockPos b,KingSleep.Sleep s){
        ensure(l,b,s);long now=l.getGameTime();
        var cat=find(l,b,CAT);
        boolean over=!s.courtFallen&&s.catUntil>now;
        if(cat!=null){
            var to=over?KingSite.CAT_OVER:KingSite.CAT_IN_TREE;
            var want=new Vec3(b.getX()+to.x,b.getY()+to.y,b.getZ()+to.z);
            // 0.4.685 (the two-reader cast checks saw it jump from the tree to the court in a tick): the Cat goes the way it goes. It
            // fades where it is, in a breath of smoke; a moment later it is somewhere else; a moment after that it can be seen there.
            boolean there=cat.position().distanceToSqr(want)<=.01;
            if(!there&&!cat.isInvisible()){cat.setInvisible(true);s.catFade=now;fade(l,cat.position());}
            else if(!there&&now-s.catFade>=FADE)cat.moveTo(want.x,want.y,want.z,cat.getYRot(),cat.getXRot());
            else if(there&&cat.isInvisible()&&now-s.catFade>=FADE*2){cat.setInvisible(false);fade(l,want);}
            cat.setDeltaMovement(Vec3.ZERO);
            var near=KingSleep.nearest(l,b,cat.position());
            if(over){cat.setYRot(0);cat.setYBodyRot(0);cat.setYHeadRot(0);cat.setXRot(25);}
            else if(near!=null&&near.distanceTo(cat)<14)KingCourt.look(cat,near.getEyePosition());
        }
        if(s.courtFallen){
            // 0.4.685: the court, gone into its cards, is cleared away once it has been out of sight a moment.
            if(now-s.courtFellAt>=5)for(int phase:new int[]{KING,QUEEN,HERALD}){var a=find(l,b,phase);if(a!=null)a.discard();}
            return;
        }
        for(int phase:new int[]{KING,QUEEN,HERALD}){
            var a=find(l,b,phase);if(a==null)continue;
            var home=b.offset(spot(phase));
            if(a.position().distanceToSqr(Vec3.atBottomCenterOf(home))>.04)a.moveTo(home.getX()+.5,home.getY(),home.getZ()+.5,a.getYRot(),0);
            if(over&&cat!=null){a.gesture(LOOKING_UP);look(a,cat.getEyePosition());continue;}
            // Otherwise each looks at the nearest reader on the square, and the herald keeps his scroll up for a while after reading.
            a.gesture(phase==HERALD&&now<s.heraldReadsUntil?READING:phase==QUEEN&&a.speaking()>0?READING:STANDING);
            ServerPlayer near=null;double best=Double.MAX_VALUE;
            for(var p:KingSleep.readers(l,b)){if(KingSite.rank((int)Math.floor(p.getZ()-b.getZ()))!=6)continue;double d=p.distanceToSqr(a);if(d<best){best=d;near=p;}}
            if(near!=null)look(a,near.getEyePosition());
            else{float yaw=phase==HERALD?-90:0;a.setYRot(yaw);a.setYBodyRot(yaw);a.setYHeadRot(yaw);a.setXRot(0);}
        }
        // The argument over the Cat's head, in turns while it is over them.
        if(over){
            long t=now-(s.catUntil-ATTEND);
            for(int i=0;i<ARGUMENT.length;i++)if(t==ARGUMENT[i]){
                int who=i==0||i==3?KING:i==1?QUEEN:HERALD;var a=find(l,b,who);
                String line=switch(i){case 0->KingTexts.ARGUE_KING;case 1->KingTexts.ARGUE_QUEEN;case 2->KingTexts.ARGUE_HERALD;default->KingTexts.ARGUE_KING_AGAIN;};
                if(a!=null)say(a,who,line);
            }
            if(t==ATTEND-60&&cat!=null)say(cat,CAT,KingTexts.CAT_GOING);
        }
    }
    /** 0.4.685: ticks the Cat takes to fade, and as long again to come back where it has gone. */
    static final int FADE=10;
    static void fade(ServerLevel l,Vec3 at){l.sendParticles(ParticleTypes.SMOKE,at.x,at.y+.6,at.z,10,.2,.25,.2,.01);}
    /** Turns an actor's head and body toward a point, pitch included, so its look (KingBreath) goes where it seems to. */
    static void look(LiteraryActor a,Vec3 at){
        var to=at.subtract(a.getEyePosition());
        float yaw=(float)(Math.toDegrees(Math.atan2(to.z,to.x))-90),pitch=(float)-Math.toDegrees(Math.atan2(to.y,Math.hypot(to.x,to.z)));
        a.setYRot(yaw);a.setYBodyRot(yaw);a.setYHeadRot(yaw);a.setXRot(Mth.clamp(pitch,-70,70));
    }

    // ------------------------------------------------------------------------------------------------ each reader, in their own tick
    static void reader(ServerPlayer p,BlockPos b,CompoundTag own,int state){
        var l=p.serverLevel();var s=KingSleep.get(b);long now=l.getGameTime();
        var r=p.position().subtract(b.getX(),b.getY(),b.getZ());
        int rank=KingSite.rank((int)Math.floor(r.z));
        // The summons: the first time they set foot on the square while the court stands, the herald reads their name.
        if(rank==6&&!own.getBoolean(SUMMONED)&&own.getInt(WAY)==0&&!s.courtFallen){
            own.putBoolean(SUMMONED,true);s.heraldReadsUntil=now+80;
            var herald=find(l,b,HERALD);
            if(herald!=null){say(herald,HERALD,p.getGameProfile().getName()+"!");say(herald,HERALD,KingTexts.HERALD_COME);}
            King.cue(p,KingTexts.SUMMONED);
        }
        // Sentence first, once, as they come up to the dock.
        if(own.getBoolean(SUMMONED)&&own.getInt(WAY)==0&&!own.getBoolean(SENTENCED)&&!s.courtFallen&&KingSite.inCourt(r)&&r.z<=KingSite.DOCK.z+2){
            own.putBoolean(SENTENCED,true);
            var queen=find(l,b,QUEEN);var king=find(l,b,KING);
            if(queen!=null)say(queen,QUEEN,KingTexts.SENTENCE_FIRST);
            if(king!=null)say(king,KING,KingTexts.SENTENCE_AFTER);
        }
        if(own.getInt(WAY)!=0||!KingSite.pastCourt(r))return;
        // Past the guard: by which way, or set back in the dock if by none (the court has not finished with them).
        int way;
        if(s.courtFallen)way=s.fellBy.contains(p.getUUID())?BY_CARDS:ALREADY_CARDS;
        else if(!own.getBoolean(SUMMONED))return;
        else if(s.catUntil>now)way=BY_CAT;
        else if(KingBreath.swallowedLess(p))way=BY_BOTTLE;
        else{
            var at=KingSite.DOCK;p.teleportTo(l,b.getX()+at.x,b.getY()+at.y,b.getZ()+at.z,180,p.getXRot());p.setDeltaMovement(Vec3.ZERO);
            King.cue(p,KingTexts.NOT_FINISHED);return;
        }
        own.putInt(WAY,way);
        if(way==BY_CAT||way==BY_BOTTLE)King.cue(p,KingTexts.EDGE_ON);
    }

    // ------------------------------------------------------------------------------------------------ talking to the court
    public static void talk(ServerPlayer p,LiteraryActor a,CompoundTag own){
        var b=LiteraryVignettes.base(p,PLACE);if(b==null)return;var s=KingSleep.get(b);
        if(own.getInt(WAY)!=0){King.cue(p,KingTexts.COURT_DONE);return;}
        int phase=a.phase();String name=name(phase);
        Talk.begin(p,"king_court",name);
        line(p,a,phase,phase==HERALD?KingTexts.HERALD_TALK:phase==KING?KingTexts.KING_TALK:KingTexts.SENTENCE_FIRST);
        var asks=new java.util.ArrayList<Talk.Reply>();
        asks.add(Talk.reply(KingTexts.R_WHAT_FOR,KingCourt::whatFor));
        // Too much of a thing can say what they are.
        if(!s.courtFallen&&KingBreath.of(p)==KingBreath.TOO_MUCH)asks.add(Talk.reply(KingTexts.R_PACK,KingCourt::pack));
        Talk.ask(p,asks.toArray(Talk.Reply[]::new));
    }
    static void whatFor(ServerPlayer p){
        var b=LiteraryVignettes.base(p,PLACE);if(b==null){Talk.end(p);return;}
        var king=find(p.serverLevel(),b,KING);
        Talk.line(p,name(KING),KingTexts.WHAT_FOR);if(king!=null)King.aloud(king,name(KING),KingTexts.WHAT_FOR);
        Talk.end(p);
    }
    /** "You're nothing but a pack of cards!": the pack goes up and comes down as cards, and the King stirs once, a long way off. */
    public static void pack(ServerPlayer p){
        Talk.end(p);
        var b=LiteraryVignettes.base(p,PLACE);if(b==null)return;
        var l=p.serverLevel();var s=KingSleep.get(b);if(s.courtFallen)return;
        s.courtFallen=true;s.courtFellAt=l.getGameTime();s.fellBy.add(p.getUUID());
        var mid=Vec3.atCenterOf(b.offset(0,1,-128));
        l.playSound(null,mid.x,mid.y,mid.z,KingRegistry.CARDS.get(),SoundSource.AMBIENT,1.2F,1);
        // The court come down as their own cards where they stood.
        for(int phase:new int[]{KING,QUEEN,HERALD}){
            var a=find(l,b,phase);
            int face=phase==KING?KingCardBlock.KING:phase==QUEEN?KingCardBlock.QUEEN:KingCardBlock.KNAVE;
            // 0.4.685: gone in the puff as the card lands, and cleared away a moment later (tick), never taken from sight.
            if(a!=null){l.sendParticles(ParticleTypes.POOF,a.getX(),a.getY()+1,a.getZ(),8,.3,.5,.3,.02);a.setInvisible(true);}
            KingSite.set(l,b,spot(phase),KingCardBlock.of(face,dir(phase*7),true));
        }
        // And the whole pack lies flat in the grass, every card turned some way of its own.
        for(int x=-8;x<=7;x++)fall(l,b,new BlockPos(x,0,KingSite.GUARD_ROW));
        for(int z=KingSite.JURY_FAR;z<=KingSite.JURY_NEAR;z++){fall(l,b,new BlockPos(KingSite.JURY_WEST,0,z));fall(l,b,new BlockPos(KingSite.JURY_EAST,0,z));}
        for(int i=0;i<40;i++)l.sendParticles(ParticleTypes.CLOUD,b.getX()+.5+(l.random.nextDouble()-.5)*16,b.getY()+2+l.random.nextDouble()*4,b.getZ()-128+(l.random.nextDouble()-.5)*12,1,0,-.1,0,.01);
        for(var q:KingSleep.readers(l,b))King.cue(q,KingTexts.PACK_FELL);
        KingSleep.stir(l,b,p,null,1);
    }
    static void fall(ServerLevel l,BlockPos b,BlockPos at){
        var st=l.getBlockState(b.offset(at));if(!KingCardBlock.standing(st))return;
        KingSite.set(l,b,at,st.setValue(KingCardBlock.FALLEN,true).setValue(KingCardBlock.FACING,dir(at.getX()*3+at.getZ())));
    }
    static Direction dir(int n){return Direction.from2DDataValue(Math.floorMod(n,4));}

    // ------------------------------------------------------------------------------------------------ the Cat
    public static void talkCat(ServerPlayer p,LiteraryActor a,CompoundTag own){
        var b=LiteraryVignettes.base(p,PLACE);if(b==null)return;
        Talk.begin(p,"king_cat",KingTexts.CAT_NAME);
        if(attendingToCat(p.serverLevel(),b)){line(p,a,CAT,KingTexts.CAT_THERE);Talk.end(p);return;}
        line(p,a,CAT,KingTexts.CAT_HEAD);line(p,a,CAT,KingTexts.CAT_OFFER);
        Talk.ask(p,Talk.reply(KingTexts.R_COME,KingCourt::call),Talk.reply(KingTexts.R_NOT_NOW,q->{var c=find(q.serverLevel(),b,CAT);if(c!=null)line(q,c,CAT,KingTexts.CAT_LATER);Talk.end(q);}));
    }
    /** The Cat goes over the court, and for a while the court looks at it and at nobody else. */
    public static void call(ServerPlayer p){
        Talk.end(p);
        var b=LiteraryVignettes.base(p,PLACE);if(b==null)return;
        var l=p.serverLevel();var s=KingSleep.get(b);if(s.courtFallen||s.catUntil>l.getGameTime())return;
        s.catUntil=l.getGameTime()+ATTEND;
        var d=LabyrinthData.get(p.server);var own=LiteraryVignettes.personal(d,p.getUUID(),PLACE);own.putBoolean(CALLED,true);LiteraryVignettes.save(d,p.getUUID(),PLACE,own);
        var cat=find(l,b,CAT);if(cat!=null)l.sendParticles(ParticleTypes.SMOKE,cat.getX(),cat.getY()+1.7,cat.getZ(),6,.15,.15,.15,.01);
        for(var q:KingSleep.readers(l,b))King.cue(q,KingTexts.CAT_OVER);
    }

    // ------------------------------------------------------------------------------------------------ words
    static String name(int phase){return switch(phase){case KING->KingTexts.HEARTS_KING_NAME;case QUEEN->KingTexts.HEARTS_QUEEN_NAME;case HERALD->KingTexts.HERALD_NAME;default->KingTexts.CAT_NAME;};}
    static void say(LiteraryActor a,int phase,String line){King.aloud(a,name(phase),line);}
    static void line(ServerPlayer p,LiteraryActor a,int phase,String line){Talk.line(p,name(phase),line);King.aloud(a,name(phase),line);}
}

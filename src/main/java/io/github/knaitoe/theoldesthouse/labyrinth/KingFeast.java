package io.github.knaitoe.theoldesthouse.labyrinth;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.648, A sort of thing, part 8 (docs/SLEEPING_KING_0_4_648.md): the feast on the eighth square, and the crown.
 *
 * <p><b>The feast.</b> A long table laid on the road, its dishes and its company all drawings: the Queen, the Guard, the Knight, the
 * herald and the King and Queen of Hearts drawn in their chairs, and one chair at the foot with nobody in it. Whatever a reader looks
 * at for a second is introduced: it fills in, and is somebody from then on, and cannot be cut or sat on. A guest introduced says so,
 * and looks back at the nearest reader on the square, which is the dream's notice (KingBreath.lookedAtByTheDream). The longer a
 * reader looks about them, the more the table fills. What has been introduced is the dream's, shared by everybody in that copy of the
 * country; who introduced what is each reader's own count.
 *
 * <p><b>The crown.</b> The square's peg stands at the head of the table. A reader counted on it, who is at least a sort of thing (a
 * line has no head for it), has a crown come down on them (KingCrownItem), which is their own and notice from then on.
 */
public final class KingFeast {
    private KingFeast(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.SLEEPING_KING;
    /** Stranger phases: the guests, each one of the dream's people drawn again at the table. */
    public static final int QUEEN=228,GUARD=229,KNIGHT=230,HERALD=231,HEARTS_KING=232,HEARTS_QUEEN=233;
    /** Who sits in each side chair (KingSite.FEAST_CHAIRS, west near to far, then east); the foot chair is empty. */
    static final int[] SEATED={GUARD,KNIGHT,HEARTS_KING,HERALD,QUEEN,HEARTS_QUEEN};
    /** What can be introduced, as bits: the six guests (each with their chair), the table, the mutton, the pudding, the candles, the empty chair. */
    public static final int TABLE=6,MUTTON=7,PUDDING=8,CANDLES=9,EMPTY_CHAIR=10,ALL=(1<<11)-1;
    /** Game ticks of looking that introduce a thing; how far a look reaches; how far a guest looks back. */
    public static final int LOOK=20;
    static final double SIGHT=24,LOOKS_BACK=20;
    /** How far round from the table a seated guest can turn their head. */
    static final float NECK=75;
    /** Keys in each reader's own record. */
    public static final String SAT_DOWN="KingFeastSeen",INTRODUCED="KingIntroduced",CROWNED="KingCrowned";

    public static boolean guest(int phase){return phase>=QUEEN&&phase<=HEARTS_QUEEN;}
    public static boolean is(LiteraryActor a){return a.role()==LiteraryActor.STRANGER&&guest(a.phase());}
    /** The one of the dream's people a guest is drawn as. */
    public static int original(int phase){
        return switch(phase){case QUEEN->KingQueen.PHASE;case GUARD->KingGuard.PHASE;case KNIGHT->KingForest.KNIGHT;case HERALD->KingCourt.HERALD;
            case HEARTS_KING->KingCourt.KING;case HEARTS_QUEEN->KingCourt.QUEEN;default->phase;};
    }
    /** Whether a guest has been introduced and filled in (drawn guests are faded right out, KingFeast.tick fills them). */
    public static boolean filled(LiteraryActor a){return is(a)&&a.fade()==0;}
    static int chair(int phase){for(int i=0;i<SEATED.length;i++)if(SEATED[i]==phase)return i;return -1;}
    /** The way a guest's chair faces: across the table. */
    static float tableYaw(int chair){return KingSite.FEAST_CHAIRS[chair].getX()<0?-90:90;}

    public static @Nullable LiteraryActor find(ServerLevel l,BlockPos b,int phase){
        var at=b.offset(KingSite.FEAST_HEAD);
        for(var a:l.getEntitiesOfClass(LiteraryActor.class,new AABB(at).inflate(16,6,16),a->a.role()==LiteraryActor.STRANGER&&a.phase()==phase))if(a.isAlive())return a;
        return null;
    }
    /** For tests: what this dream has introduced, as bits. */
    public static int introduced(BlockPos b){return KingSleep.get(b).introduced;}

    /** The guests in their chairs, drawn, wherever the square is loaded. */
    static void ensure(ServerLevel l,BlockPos b,KingSleep.Sleep s){
        for(int i=0;i<SEATED.length;i++){
            var at=b.offset(KingSite.FEAST_CHAIRS[i]);
            if(!l.isLoaded(at)||!l.areEntitiesLoaded(ChunkPos.asLong(at))||find(l,b,SEATED[i])!=null)continue;
            var a=LiteraryRegistry.ACTOR.get().create(l);if(a==null)continue;
            a.bind(PLACE,null);a.appearance(LiteraryActor.STRANGER,SEATED[i]);a.fade((s.introduced&1<<i)!=0?0:100);
            float yaw=tableYaw(i);a.moveTo(at.getX()+.5,at.getY(),at.getZ()+.5,yaw,0);a.setYBodyRot(yaw);a.setYHeadRot(yaw);
            l.addFreshEntity(a);
        }
    }
    /** A fresh dream: the feast drawn again, every guest a drawing in their chair. */
    static void reset(ServerLevel l,BlockPos b){
        if(l.isLoaded(b.offset(KingSite.FEAST_HEAD)))KingSite.feast(l,b,false);
        for(int i=0;i<SEATED.length;i++){var a=find(l,b,SEATED[i]);if(a!=null){a.fade(100);var at=KingSite.FEAST_CHAIRS[i];float yaw=tableYaw(i);a.moveTo(b.getX()+at.getX()+.5,b.getY(),b.getZ()+at.getZ()+.5,yaw,0);a.setYBodyRot(yaw);a.setYHeadRot(yaw);}}
    }

    // ------------------------------------------------------------------------------------------------ once a tick, while anybody is there
    static void tick(ServerLevel l,BlockPos b,KingSleep.Sleep s){
        ensure(l,b,s);
        // The laid feast shows what has been introduced, block for block.
        if(s.feastShown!=s.introduced&&l.isLoaded(b.offset(KingSite.FEAST_HEAD))){show(l,b,s.introduced);s.feastShown=s.introduced;}
        for(int i=0;i<SEATED.length;i++){
            var a=find(l,b,SEATED[i]);if(a==null)continue;
            var at=KingSite.FEAST_CHAIRS[i];float table=tableYaw(i);
            if(a.position().distanceToSqr(b.getX()+at.getX()+.5,b.getY()+at.getY(),b.getZ()+at.getZ()+.5)>.01)a.moveTo(b.getX()+at.getX()+.5,b.getY(),b.getZ()+at.getZ()+.5,table,0);
            a.setDeltaMovement(Vec3.ZERO);a.setYRot(table);a.setYBodyRot(table);
            // Filling in or thinning over a few ticks.
            boolean in=(s.introduced&1<<i)!=0;a.fade(in?a.fade()-14:100);
            // Filled in, they look round at the nearest reader on the square within reach of their neck; otherwise across the table.
            ServerPlayer near=null;
            if(in){double best=LOOKS_BACK*LOOKS_BACK;
                for(var p:KingSleep.readers(l,b)){if(KingSite.rank((int)Math.floor(p.getZ()-b.getZ()))!=KingSite.RANKS)continue;double d=p.distanceToSqr(a);if(d>=best)continue;
                    float want=yawTo(a,p.getBoundingBox().getCenter());if(Math.abs(Mth.wrapDegrees(want-table))>NECK)continue;best=d;near=p;}}
            if(near!=null){var to=near.getBoundingBox().getCenter().subtract(a.getEyePosition());a.setYHeadRot(yawTo(a,near.getBoundingBox().getCenter()));a.setXRot(Mth.clamp((float)-Math.toDegrees(Math.atan2(to.y,Math.hypot(to.x,to.z))),-60,60));}
            else{a.setYHeadRot(table);a.setXRot(8);}
        }
    }
    static float yawTo(LiteraryActor a,Vec3 at){return (float)(Math.toDegrees(Math.atan2(at.z-a.getZ(),at.x-a.getX()))-90);}
    /** The feast's blocks drawn or filled as introduced. */
    static void show(ServerLevel l,BlockPos b,int introduced){
        boolean table=(introduced&1<<TABLE)!=0;
        for(int z=KingSite.FEAST_NEAR;z>=KingSite.FEAST_FAR;z--)for(int x=-1;x<=0;x++){
            var at=b.offset(x,0,z);var st=l.getBlockState(at);if(!KingBlock.is(st,KingBlock.Kind.FEAST_TABLE))continue;
            int stage=table?1:0;if(st.getValue(KingBlock.STAGE)!=stage)l.setBlock(at,st.setValue(KingBlock.STAGE,stage),net.minecraft.world.level.block.Block.UPDATE_CLIENTS);
        }
        for(int i=0;i<KingSite.FEAST_DISHES.length;i++){
            var kind=KingSite.DISH_KINDS[i];int bit=kind==KingBlock.Kind.MUTTON?MUTTON:kind==KingBlock.Kind.PUDDING?PUDDING:CANDLES;
            set(l,b.offset(KingSite.FEAST_DISHES[i]),(introduced&1<<bit)!=0);
        }
        for(int i=0;i<KingSite.FEAST_CHAIRS.length;i++)set(l,b.offset(KingSite.FEAST_CHAIRS[i]),(introduced&1<<(i<SEATED.length?i:EMPTY_CHAIR))!=0);
    }
    private static void set(ServerLevel l,BlockPos at,boolean filled){
        var st=l.getBlockState(at);if(!KingBlock.feast(st))return;int stage=filled?1:0;
        if(st.getValue(KingBlock.STAGE)!=stage)l.setBlock(at,st.setValue(KingBlock.STAGE,stage),net.minecraft.world.level.block.Block.UPDATE_CLIENTS);
    }

    // ------------------------------------------------------------------------------------------------ each reader, in their own tick
    static void reader(ServerPlayer p,BlockPos b,CompoundTag own,int state){
        var s=KingSleep.get(b);var l=p.serverLevel();long now=l.getGameTime();
        var r=p.position().subtract(b.getX(),b.getY(),b.getZ());
        if(KingSite.rank((int)Math.floor(r.z))!=KingSite.RANKS){own.remove("KingFeastLook");return;}
        if(!own.getBoolean(SAT_DOWN)){own.putBoolean(SAT_DOWN,true);King.cue(p,KingTexts.FEAST_FIRST);}
        // What they are looking at, if it is something of the feast's not yet introduced; held long enough, it is.
        int what=lookingAt(p,b,s);
        if(what<0){own.remove("KingFeastLook");return;}
        if(!own.contains("KingFeastLook")||own.getInt("KingFeastLook")!=what){own.putInt("KingFeastLook",what);own.putLong("KingFeastLookFrom",now);return;}
        if(now-own.getLong("KingFeastLookFrom")<LOOK)return;
        own.remove("KingFeastLook");
        introduce(l,b,s,what);
        own.putInt(INTRODUCED,own.getInt(INTRODUCED)+1);
    }
    /** The feast's thing this reader has in the middle of their sight and that is not introduced yet, or -1. */
    static int lookingAt(ServerPlayer p,BlockPos b,KingSleep.Sleep s){
        if(KingBreath.of(p)<=KingBreath.NOWHERE)return -1;
        for(int i=0;i<SEATED.length;i++){
            if((s.introduced&1<<i)!=0)continue;var a=find(p.serverLevel(),b,SEATED[i]);
            if(a!=null&&p.distanceTo(a)<SIGHT&&KingBreath.looksAt(p,a)&&centred(p,a.getBoundingBox().getCenter()))return i;
        }
        var hit=p.pick(SIGHT,1,false);
        if(hit.getType()!=HitResult.Type.BLOCK)return -1;
        var at=((BlockHitResult)hit).getBlockPos();var st=p.level().getBlockState(at);if(!KingBlock.feast(st))return -1;
        var rel=at.subtract(b);int what;
        switch(st.getValue(KingBlock.KIND)){
            case FEAST_TABLE->what=TABLE;
            case MUTTON->what=MUTTON;
            case PUDDING->what=PUDDING;
            case CANDLES->what=CANDLES;
            default->{int i=-1;for(int c=0;c<KingSite.FEAST_CHAIRS.length;c++)if(KingSite.FEAST_CHAIRS[c].equals(rel))i=c;
                if(i<0)return -1;what=i<SEATED.length?i:EMPTY_CHAIR;}
        }
        return (s.introduced&1<<what)!=0?-1:what;
    }
    /** Within twelve degrees of the middle of their sight: a look at somebody, not a glance past them. */
    static boolean centred(ServerPlayer p,Vec3 at){var to=at.subtract(p.getEyePosition());double d=to.length();return d<1e-3||p.getViewVector(1).dot(to.scale(1/d))>Math.cos(Math.toRadians(12));}

    /** One thing at the feast introduced: it fills in, says so if it is somebody, and is the dream's from then on. */
    public static void introduce(ServerLevel l,BlockPos b,KingSleep.Sleep s,int what){
        if((s.introduced&1<<what)!=0)return;
        s.introduced|=1<<what;
        var at=what<SEATED.length?KingSite.FEAST_CHAIRS[what]:what==EMPTY_CHAIR?KingSite.FEAST_FOOT:what==TABLE?new BlockPos(0,0,(KingSite.FEAST_NEAR+KingSite.FEAST_FAR)/2):KingSite.FEAST_DISHES[what==MUTTON?3:what==PUDDING?1:0];
        var c=Vec3.atCenterOf(b.offset(at));
        l.playSound(null,c.x,c.y,c.z,KingRegistry.INTRODUCED.get(),SoundSource.AMBIENT,.7F,.9F+l.random.nextFloat()*.2F);
        if(what<SEATED.length){var a=find(l,b,SEATED[what]);if(a!=null)say(a,SEATED[what],KingTexts.introduced(SEATED[what]));}
        else for(var p:KingSleep.readers(l,b))if(KingSite.rank((int)Math.floor(p.getZ()-b.getZ()))==KingSite.RANKS)King.cue(p,KingTexts.introducedThing(what));
    }

    // ------------------------------------------------------------------------------------------------ the crown
    /**
     * Counted on the eighth square's peg, at the head of the table: a crown comes down on them, unless they are a line, which has no head
     * for it, or already have their own. Whatever they wore on their head goes back into their pack. True if it came down.
     */
    static boolean crown(ServerPlayer p,BlockPos b,CompoundTag own,int state){
        if(state<KingBreath.SORT||KingCrownItem.has(p))return false;
        var worn=p.getItemBySlot(EquipmentSlot.HEAD);
        if(!worn.isEmpty()){p.setItemSlot(EquipmentSlot.HEAD,net.minecraft.world.item.ItemStack.EMPTY);if(!p.getInventory().add(worn))p.drop(worn,false);}
        p.setItemSlot(EquipmentSlot.HEAD,KingCrownItem.of(p.getUUID()));
        own.putBoolean(CROWNED,true);
        var at=p.getEyePosition();p.serverLevel().playSound(null,at.x,at.y,at.z,KingRegistry.CROWNED.get(),SoundSource.PLAYERS,1,1);
        King.cue(p,KingTexts.CROWN_CAME);
        var queen=find(p.serverLevel(),b,QUEEN);if(queen!=null&&filled(queen))say(queen,QUEEN,KingTexts.queenCrowned(p));
        return true;
    }

    // ------------------------------------------------------------------------------------------------ talking to the guests
    public static void talk(ServerPlayer p,LiteraryActor a,CompoundTag own){
        if(!filled(a)){King.cue(p,KingTexts.GUEST_DRAWN);return;}
        Talk.begin(p,"king_feast",name(a.phase()));
        Talk.line(p,name(a.phase()),KingTexts.atTable(a.phase()));a.say(name(a.phase()),KingTexts.atTable(a.phase()));a.speaks(KingTexts.atTable(a.phase()));
        Talk.end(p);
    }
    /** A hand on something at the feast: drawn, it is only drawn; introduced, it is somebody, and not to be cut or sat on. */
    static void click(ServerPlayer p,KingBlock.Kind kind,boolean filled){
        if(!filled){King.cue(p,KingTexts.FEAST_DRAWN);return;}
        King.cue(p,kind==KingBlock.Kind.FEAST_CHAIR?KingTexts.NOT_SAT_ON:kind==KingBlock.Kind.FEAST_TABLE?KingTexts.TABLE_SOMEBODY:KingTexts.NOT_CUT);
    }
    static String name(int phase){
        return switch(phase){case QUEEN->KingTexts.QUEEN_NAME;case GUARD->KingTexts.GUARD_NAME;case KNIGHT->KingTexts.KNIGHT_NAME;case HERALD->KingTexts.HERALD_NAME;
            case HEARTS_KING->KingTexts.HEARTS_KING_NAME;default->KingTexts.HEARTS_QUEEN_NAME;};
    }
    static void say(LiteraryActor a,int phase,String line){a.say(name(phase),line);a.speaks(line);}
    /** For the look review and tests: which way the foot chair faces. */
    static Direction footFacing(){return Direction.NORTH;}
}

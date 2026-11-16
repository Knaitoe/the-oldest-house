package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.641, A sort of thing, the first part (docs/SLEEPING_KING_0_4_641.md), from the owner's proposal (docs/SLEEPING_KING_PROPOSAL.md)
 * by the plan (docs/SLEEPING_KING_PLAN.md). The place alone: the drawing-room, the room behind the glass, the board in its three kinds
 * of land, the wood and the King asleep under his tree. The glass lets a reader through by a hand on it, either way. Nothing else
 * happens yet, and the House does not deal it until part 3.
 */
public final class King {
    private King(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.SLEEPING_KING;
    /** The King, on stranger phase 217 (literary_king). */
    public static final int PHASE=217;
    /**
     * 0.4.654: the King's country's people (the King, the two brothers, the Queen, the guard, the Caterpillar, the court, the Knight and
     * the feast's guests), every one of whom has gestures of its own, so none of them is ever taken for the crawl or the plain reach
     * (LiteraryActor.ownGestures).
     */
    public static boolean cast(int phase){return phase>=PHASE&&phase<=KingFeast.HEARTS_QUEEN;}
    static void cue(ServerPlayer p,String line){p.displayClientMessage(Component.literal(line).withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),true);}
    /**
     * 0.4.643: dealt among the understanding stories from depth twelve (the owner's decision 11 of 15 November), once the quiet rooms
     * are done as every story is, and never again after a reader's own ending.
     */
    public static final int DEPTH=12;
    public static boolean dealable(LabyrinthData d,UUID reader){return d.returnDepth(reader)>=DEPTH&&!LiteraryVignettes.personal(d,reader,PLACE).getBoolean("Completed");}
    public static boolean is(LiteraryActor a){return a.role()==LiteraryActor.STRANGER&&a.phase()==PHASE;}
    static AABB area(BlockPos b){var k=b.offset(KingSite.KING);return new AABB(k).inflate(8);}
    public static void arrive(ServerPlayer p,BlockPos b,CompoundTag own){
        KingAfter.arrive(own);
        // 0.4.649: a country standing as its aftermath is not peopled for a reader who has had their ending (KingAfter).
        if(KingSleep.after(b)!=KingAfter.NONE&&KingAfter.done(own))return;
        people(p.serverLevel(),b);
    }
    /** The King and all the dream's people, each where a dream has them, wherever their ground is loaded. */
    static void people(ServerLevel l,BlockPos b){ensure(l,b);KingFeast.ensure(l,b,KingSleep.get(b));KingBrothers.ensure(l,b,KingSleep.get(b));KingQueen.ensure(l,b);KingGuard.ensure(l,b);KingCaterpillar.ensure(l,b);KingCourt.ensure(l,b,KingSleep.get(b));KingForest.ensure(l,b,KingSleep.get(b));}

    /**
     * 0.4.643: a hand on the King. A line's hand goes into him; before the brothers have said his name he is only an old man asleep;
     * after, beside him, the reader may say the whole of it, which wakes him (KingSleep), or let him sleep.
     */
    public static void talk(ServerPlayer p,LiteraryActor a,CompoundTag own){
        if(!is(a))return;var b=LiteraryVignettes.base(p,PLACE);if(b==null)return;
        if(KingBreath.of(p)<=KingBreath.LINE){cue(p,KingTexts.THROUGH_HIM);return;}
        if(KingSleep.waking(b)){cue(p,KingTexts.EYES_OPEN);return;}
        if(!own.getBoolean(KingSleep.HEARD)||!KingBreath.beside(p.position().subtract(b.getX(),b.getY(),b.getZ()))){cue(p,KingTexts.KING);return;}
        // 0.4.648: the last move is a queen's. A pawn beside him has none.
        if(!KingCrownItem.crowned(p)){cue(p,KingTexts.PAWN_NO_MOVE);return;}
        Talk.begin(p,"king_name",KingTexts.THE_KING);
        Talk.line(p,"",KingTexts.ASLEEP_HERE);
        Talk.ask(p,Talk.reply(KingTexts.SAY_HIS_NAME,King::sayName),Talk.act(KingTexts.LET_HIM_SLEEP,Talk::close));
    }
    public static void sayName(ServerPlayer p){
        var b=LiteraryVignettes.base(p,PLACE);if(b==null)return;
        var d=LabyrinthData.get(p.server);var own=LiteraryVignettes.personal(d,p.getUUID(),PLACE);own.putBoolean("KingNamed",true);LiteraryVignettes.save(d,p.getUUID(),PLACE,own);
        Talk.close(p);KingSleep.wake(p.serverLevel(),b,p,false);
    }

    /** The King under his tree, if his body is there. */
    public static @Nullable LiteraryActor find(ServerLevel l,BlockPos b){
        for(var a:l.getEntitiesOfClass(LiteraryActor.class,area(b),King::is))if(a.isAlive())return a;
        return null;
    }
    /** He is made lying under his tree if he is not there; nothing is made while the wood's entities are unloaded. */
    public static void ensure(ServerLevel l,BlockPos b){
        var at=b.offset(KingSite.KING);
        if(!l.isLoaded(at)||!l.areEntitiesLoaded(ChunkPos.asLong(at)))return;
        var a=find(l,b);
        if(a==null){
            a=LiteraryRegistry.ACTOR.get().create(l);if(a==null)return;
            a.bind(PLACE,null);a.appearance(LiteraryActor.STRANGER,PHASE);
            if(!l.addFreshEntity(a))return;
        }
        lie(a,at);
    }
    /** Asleep in a heap at the foot of the tree, his head toward the trunk. */
    static void lie(LiteraryActor a,BlockPos at){lie(a,at,180);}
    /** 0.4.643: turned a little one way or the other, as he has turned over in his sleep (KingSleep). */
    static void lie(LiteraryActor a,BlockPos at,float yaw){
        a.moveTo(at.getX()+.5,at.getY(),at.getZ()+.5,yaw,0);a.setYBodyRot(yaw);a.setYHeadRot(yaw);
        a.setSleepingPos(at);a.setPose(Pose.SLEEPING);a.gesture(0);a.setDeltaMovement(Vec3.ZERO);
    }

    /** A hand on something of the room's or the country's. True if it was theirs to answer. */
    public static boolean click(ServerPlayer p,BlockPos b,BlockPos rel,CompoundTag own){
        var l=p.serverLevel();var at=b.offset(rel);var st=l.getBlockState(at);
        var r=p.position().subtract(b.getX(),b.getY(),b.getZ());
        if(KingRegistry.ruled(st)){cue(p,KingTexts.RULED);return true;}
        // 0.4.646: one of the court's pack, standing or fallen.
        if(KingCardBlock.is(st)){cue(p,st.getValue(KingCardBlock.FALLEN)?KingTexts.CARD_FALLEN:KingTexts.CARD_STANDING);return true;}
        if(!st.is(KingRegistry.FITTING.get()))return false;
        boolean behind=rel.getZ()<KingSite.MIRROR_Z;
        switch(st.getValue(KingBlock.KIND)){
            case MANTEL->cue(p,KingTexts.MANTEL);
            case GLASS->{
                // Through the glass: from the hearth into the same place on the other side of it, facing on.
                var from=BlockPos.containing(r);
                if(Math.abs(rel.getZ()-r.z)>3.2){cue(p,behind?KingTexts.GLASS_BACK:KingTexts.GLASS);return true;}
                double tz=behind?-10.5:-15.5;float yaw=behind?0:180;
                l.playSound(null,at,SoundEvents.AMETHYST_BLOCK_CHIME,SoundSource.BLOCKS,.5F,.6F);
                p.teleportTo(l,b.getX()+Math.max(-1,Math.min(1,from.getX()))+.5,b.getY(),b.getZ()+tz,yaw,p.getXRot());
                cue(p,behind?KingTexts.GLASS_BACK:KingTexts.GLASS);
            }
            case RUG->cue(p,KingTexts.RUG);
            case CHESSMEN->cue(p,KingTexts.CHESSMEN);
            case TABLE->cue(p,KingTexts.TABLE);
            // The chess problem on the drawing-room table is the first paper: the House's own reading answers it. Behind the glass it reads.
            case PROBLEM->{if(!behind)return false;cue(p,KingTexts.PROBLEM);}
            case ARMCHAIR->cue(p,KingTexts.ARMCHAIR);
            case WINDOW->cue(p,KingTexts.WINDOW);
            // 0.4.643: the rattle on his square, lifted only by a thing; a sort of thing's or a line's hand goes through it.
            case RATTLE->{
                if(KingBreath.of(p)<KingBreath.THING){cue(p,KingTexts.RATTLE_THROUGH);return true;}
                var s=KingSleep.get(b);if(s.rattleTaken)return true;
                l.setBlock(at,net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),net.minecraft.world.level.block.Block.UPDATE_CLIENTS);
                s.rattleTaken=true;s.rattleBy=p.getUUID();own.putBoolean("KingRattle",true);cue(p,KingTexts.RATTLE_TAKEN);
            }
            // 0.4.644: the peg says what it is; standing on it is what counts (KingSleep.reader).
            case PEG->cue(p,KingTexts.PEG);
            // The glass table: only a thing can lift the bottle and the cake, and each reader takes their own.
            case GLASS_TABLE->{
                if(KingBreath.of(p)<KingBreath.THING){cue(p,KingTexts.TABLE_THROUGH);return true;}
                if(own.getBoolean("KingTookDraughts")){cue(p,KingTexts.TABLE_TAKEN);return true;}
                p.getInventory().placeItemBackInInventory(KingDraughtItem.own(false));p.getInventory().placeItemBackInInventory(KingDraughtItem.own(true));
                own.putBoolean("KingTookDraughts",true);cue(p,KingTexts.TABLE_TAKE);
            }
            case SEAT->io.github.knaitoe.theoldesthouse.house.HouseSitting.sit(p,at);
            case WHEEL->cue(p,KingTexts.WHEEL);
            // 0.4.648: the feast, drawn or introduced.
            case FEAST_TABLE,MUTTON,PUDDING,CANDLES,FEAST_CHAIR->KingFeast.click(p,st.getValue(KingBlock.KIND),KingBlock.filled(st));
            // 0.4.649: his night-cap on a bough, in the country he woke in.
            case NIGHTCAP->cue(p,KingTexts.NIGHTCAP);
            // 0.4.645: the mushroom. The side a reader stands on is the side they break from: the west side more, the east side
            // less. A piece fills the bottle or the cake again (or gives one, if they have none of that side's).
            case STALK->cue(p,KingTexts.STALK);
            case CAP->{
                boolean more=p.getX()<at.getX()+.5;
                var inv=p.getInventory();boolean had=false;
                for(int i=0;i<inv.getContainerSize();i++){var it=inv.getItem(i);if(KingDraughtItem.is(it)&&((KingDraughtItem)it.getItem()).more()==more){it.setDamageValue(0);had=true;}}
                if(!had)inv.placeItemBackInInventory(KingDraughtItem.own(more));
                own.putInt("KingMushroom",own.getInt("KingMushroom")+1);
                l.playSound(null,at,SoundEvents.CROP_BREAK,SoundSource.BLOCKS,.6F,1.3F);
                cue(p,more?KingTexts.MUSHROOM_MORE:KingTexts.MUSHROOM_LESS);
            }
        }
        return true;
    }
}

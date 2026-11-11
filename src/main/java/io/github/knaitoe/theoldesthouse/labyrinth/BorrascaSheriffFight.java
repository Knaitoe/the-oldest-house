package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.457, the owner, note 24 of the playtest of 10 November: "Combat with the sheriff shouldn't' feel like a vanilla Minecraft
 * fight. Punches should be sluggish with real weight. The sheriff should use his baton, then drop it after being hit a few times
 * and start swinging his own fists. He swings with one fist then the other."
 * <p>
 * The fight a grown reader starts by striking the Sheriff (BorrascaStory.sheriffFight, 0.4.427). What it does is unchanged: beaten,
 * he goes down in the dirt and talks, and a blow after that kills him. How it goes is this.
 * <ul>
 * <li><b>His baton.</b> He puts the flashlight away and has his baton out. A swing is a thing with a beginning: he raises it
 * (BATON_RAISE), turning with the reader until the last moment, then is committed and steps in, and it comes down. It lands on a
 * reader who is still within its reach and in front of him, hard: it knocks them back and their legs are slow for a moment. A
 * reader who has stepped out of it is missed, and he is longer getting his balance. Then he rests his arm (BATON_REST).</li>
 * <li><b>His fists.</b> After DISARM_HITS blows have landed on him the baton is out of his hand and lies where it fell, and he
 * puts his fists up. He throws the left, and then the right, each drawn back first (FIST_DRAW), each a hook that pushes the
 * reader across as well as back; and rests.</li>
 * <li><b>The reader's arms.</b> While the fight is on a blow has to be brought all the way back (ARM_SPEED blows a second, whatever
 * is in the hand). A blow thrown before that glances: it does nothing, and he does not feel it.</li>
 * <li><b>A blow that lands on him</b> knocks him back a step and he reels from it (REEL_TICKS), his own swing broken if he had
 * only begun to raise it. He cannot be kept reeling: having reeled, he is steady for a while (STEADY) and takes the next blow
 * standing, and a swing he is committed to comes down whatever lands on him.</li>
 * </ul>
 * The story's own clock runs each fifth tick, so every length here is a multiple of five.
 */
@net.neoforged.fml.common.EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class BorrascaSheriffFight {
    private BorrascaSheriffFight(){}
    /** His poses: fists or baton up between blows; the baton raised, and brought down; each fist drawn back, and thrown; reeling. */
    public static final int GUARD=21,BATON_UP=22,BATON_DOWN=23,LEFT_BACK=24,LEFT_OUT=25,RIGHT_BACK=26,RIGHT_OUT=27,REEL=28;
    /** What he is in the middle of. */
    public static final int NONE=0,BATON=1,LEFT=2,RIGHT=3;
    public static final int DISARM_HITS=3;
    public static final int BATON_RAISE=15,BATON_REST=30,FIST_DRAW=10,FIST_REST=30,FOLLOW=5,COMMIT=5,MISS_EXTRA=10,REEL_TICKS=15,STEADY=30;
    public static final float BATON_DAMAGE=4,FIST_DAMAGE=2;
    public static final double BATON_REACH=3.1,FIST_REACH=2.7,CLOSE=2.2;
    /** How much of a blow has to be there for it to be a blow: the arm at least this far back (of 1). */
    public static final float FULL_BLOW=.85F;
    /** A reader's blows a second while the fight is on, whatever they hold. */
    public static final double ARM_SPEED=1.1;
    public static final ResourceLocation HEAVY=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"borrasca_heavy_arms");

    /** Whether the fight is on: he has been struck, or struck at, this visit, and is on his feet. */
    public static boolean on(CompoundTag own){return (own.getInt("SheriffHits")>0||own.getBoolean("SheriffRoused"))&&!own.getBoolean("SheriffDown")&&!own.getBoolean("SheriffDead");}
    /** Whether a blow of this size from this reader had the arm all the way back. The game has already made a part-drawn blow smaller. */
    public static boolean full(ServerPlayer p,float amount){
        double whole=p.getAttributeValue(Attributes.ATTACK_DAMAGE);
        return whole<=0||amount>=whole*(.2+.8*FULL_BLOW*FULL_BLOW)-1e-4;
    }
    /** A blow that glanced: it rouses him, and that is all. */
    public static void glanced(ServerPlayer p,LiteraryActor sheriff,CompoundTag own){
        if(!own.getBoolean("SheriffRoused")){own.putBoolean("SheriffRoused",true);own.putBoolean("Escorting",false);own.putInt("Suspicion",0);BorrascaStory.save(p,own);}
        BorrascaStory.heard(p,SoundEvents.PLAYER_ATTACK_WEAK,sheriff.blockPosition(),.7F);
    }
    /** The reader's arms: heavy while the fight is on, their own again after. */
    public static void heavy(ServerPlayer p,boolean on){
        var speed=p.getAttribute(Attributes.ATTACK_SPEED);if(speed==null)return;
        var mine=speed.getModifier(HEAVY);
        if(!on){if(mine!=null)speed.removeModifier(HEAVY);return;}
        double without=speed.getValue()/(1+(mine==null?0:mine.amount())),want=Math.min(0,ARM_SPEED/Math.max(.05,without)-1);
        if(mine!=null&&Math.abs(mine.amount()-want)<.01)return;
        speed.removeModifier(HEAVY);
        if(want<0)speed.addTransientModifier(new AttributeModifier(HEAVY,want,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    }
    /** A new visit, or the reader gone: he has his baton again and nothing is in the middle of happening. */
    public static void rest(ServerPlayer p,CompoundTag own,boolean newVisit){
        for(var key:new String[]{"SheriffSwing","SheriffSwingAt","SheriffSwung","SheriffMissed","SheriffNextAt","SheriffReelUntil","SheriffSteadyUntil","SheriffRoused"})own.remove(key);
        if(newVisit&&!own.getBoolean("SheriffDead")){
            if(own.contains("SheriffBatonAt")){var at=BlockPos.of(own.getLong("SheriffBatonAt"));if(p.serverLevel().hasChunkAt(at)&&BorrascaShopBlock.is(p.serverLevel().getBlockState(at),BorrascaShopBlock.Kind.BATON))p.serverLevel().setBlock(at,Blocks.AIR.defaultBlockState(),3);}
            own.remove("SheriffBatonAt");own.remove("SheriffDisarmed");
        }
        heavy(p,false);
    }

    /** A blow that landed on him, after the story has counted it (hits is how many have landed this visit). */
    public static void struck(ServerPlayer p,LiteraryActor sheriff,CompoundTag own,int hits){
        long now=p.serverLevel().getGameTime();own.putBoolean("SheriffRoused",true);
        var from=sheriff.position().subtract(p.position());sheriff.knockback(.45,-from.x,-from.z);sheriff.hurtMarked=true;
        BorrascaStory.heard(p,SoundEvents.PLAYER_ATTACK_KNOCKBACK,sheriff.blockPosition(),1F);
        if(hits>=DISARM_HITS&&!own.getBoolean("SheriffDisarmed"))disarm(p,sheriff,own);
        int swing=own.getInt("SheriffSwing");long t=now-own.getLong("SheriffSwingAt");
        boolean committed=swing!=NONE&&t>=draw(swing)-COMMIT;
        if(now>=own.getLong("SheriffSteadyUntil")&&!committed){
            own.putLong("SheriffReelUntil",now+REEL_TICKS);own.putLong("SheriffSteadyUntil",now+REEL_TICKS+STEADY);
            own.putInt("SheriffSwing",NONE);own.remove("SheriffSwung");own.remove("SheriffMissed");own.putLong("SheriffNextAt",Math.max(own.getLong("SheriffNextAt"),now+REEL_TICKS+5));
            sheriff.gesture(REEL);BorrascaCast.stop(sheriff);
        }
    }
    /** The baton out of his hand: it lies where it fell, by his feet, for the rest of the visit. */
    private static void disarm(ServerPlayer p,LiteraryActor sheriff,CompoundTag own){
        own.putBoolean("SheriffDisarmed",true);sheriff.setItemSlot(EquipmentSlot.MAINHAND,ItemStack.EMPTY);
        var l=p.serverLevel();var feet=sheriff.blockPosition();var facing=Direction.fromYRot(sheriff.getYRot());
        for(var at:new BlockPos[]{feet.relative(facing.getClockWise()),feet.relative(facing.getCounterClockWise()),feet.relative(facing.getOpposite()),feet}){
            if(!l.getBlockState(at).isAir()||!l.getBlockState(at.below()).isFaceSturdy(l,at.below(),Direction.UP))continue;
            l.setBlock(at,BorrascaShopBlock.of(BorrascaShopBlock.Kind.BATON,facing.getClockWise()),3);own.putLong("SheriffBatonAt",at.asLong());break;
        }
        BorrascaStory.heard(p,SoundEvents.WOOD_HIT,feet,1F);
    }

    private static int draw(int swing){return swing==BATON?BATON_RAISE:FIST_DRAW;}
    private static double reach(int swing){return swing==BATON?BATON_REACH:FIST_REACH;}
    private static void face(LiteraryActor sheriff,ServerPlayer p){float yaw=BorrascaStory.toward(sheriff,p);sheriff.setYRot(yaw);sheriff.setYBodyRot(yaw);sheriff.setYHeadRot(yaw);}
    /** In front of him, within a wide arc of the way he faces. */
    private static boolean before(LiteraryActor sheriff,ServerPlayer p){
        var to=p.position().subtract(sheriff.position()).multiply(1,0,1);if(to.lengthSqr()<1e-4)return true;
        var facing=Vec3.directionFromRotation(0,sheriff.getYRot());return to.normalize().dot(facing)>.45;
    }
    private static void begin(CompoundTag own,LiteraryActor sheriff,long now,int swing){
        own.putInt("SheriffSwing",swing);own.putLong("SheriffSwingAt",now);own.remove("SheriffSwung");own.remove("SheriffMissed");
        BorrascaCast.stop(sheriff);sheriff.gesture(swing==BATON?BATON_UP:swing==LEFT?LEFT_BACK:RIGHT_BACK);
    }

    /** A reader whose arms are heavy and who is no longer in it (gone from the town, or the fight over some other way): they are their own again. */
    @net.neoforged.bus.api.SubscribeEvent public static void after(net.neoforged.neoforge.event.tick.PlayerTickEvent.Post e){
        if(!(e.getEntity() instanceof ServerPlayer p)||p.tickCount%20!=0)return;
        var speed=p.getAttribute(Attributes.ATTACK_SPEED);if(speed==null||speed.getModifier(HEAVY)==null)return;
        if(!BorrascaSites.inside(p)||!on(LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),LabyrinthPlace.BORRASCA)))heavy(p,false);
    }
    /** The fight, each fifth tick, while he is on his feet: he closes, raises, is committed, lands or misses, and rests. */
    public static void tick(ServerPlayer p,CompoundTag own,LiteraryActor sheriff){
        long now=p.serverLevel().getGameTime();boolean armed=!own.getBoolean("SheriffDisarmed");
        heavy(p,true);
        if(armed){if(!sheriff.getMainHandItem().is(BorrascaRegistry.BATON.get()))sheriff.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(BorrascaRegistry.BATON.get()));}
        else if(!sheriff.getMainHandItem().isEmpty())sheriff.setItemSlot(EquipmentSlot.MAINHAND,ItemStack.EMPTY);
        if(now<own.getLong("SheriffReelUntil")){BorrascaCast.stop(sheriff);sheriff.gesture(REEL);return;}
        int swing=own.getInt("SheriffSwing");double d=Math.sqrt(sheriff.distanceToSqr(p));
        if(swing==NONE){
            face(sheriff,p);sheriff.gesture(GUARD);
            int next=armed?BATON:LEFT;
            if(d>reach(next)-.5){BorrascaCast.walk(sheriff,p.position(),.85);return;}
            BorrascaCast.stop(sheriff);
            if(now>=own.getLong("SheriffNextAt")&&BorrascaStory.sees(sheriff,p.getEyePosition()))begin(own,sheriff,now,next);
            return;
        }
        long t=now-own.getLong("SheriffSwingAt");int draw=draw(swing);
        if(t<draw){
            // Raising it: he turns with the reader until the last moment, then is committed and steps in behind it.
            if(t<draw-COMMIT){face(sheriff,p);BorrascaCast.stop(sheriff);}
            else BorrascaCast.walk(sheriff,sheriff.position().add(Vec3.directionFromRotation(0,sheriff.getYRot()).scale(.9)),.9);
            sheriff.gesture(swing==BATON?BATON_UP:swing==LEFT?LEFT_BACK:RIGHT_BACK);return;
        }
        if(!own.getBoolean("SheriffSwung")){
            own.putBoolean("SheriffSwung",true);BorrascaCast.stop(sheriff);sheriff.gesture(swing==BATON?BATON_DOWN:swing==LEFT?LEFT_OUT:RIGHT_OUT);
            if(d<=reach(swing)&&before(sheriff,p)&&BorrascaStory.sees(sheriff,p.getEyePosition()))land(p,sheriff,swing);
            else{own.putBoolean("SheriffMissed",true);BorrascaStory.heard(p,SoundEvents.PLAYER_ATTACK_SWEEP,sheriff.blockPosition(),.6F);}
            return;
        }
        if(t<draw+FOLLOW)return;
        boolean missed=own.getBoolean("SheriffMissed");
        // One fist and then the other, whether or not the first found anything.
        if(swing==LEFT){begin(own,sheriff,now,RIGHT);return;}
        own.putInt("SheriffSwing",NONE);own.remove("SheriffSwung");own.remove("SheriffMissed");
        own.putLong("SheriffNextAt",now+(swing==BATON?BATON_REST:FIST_REST)+(missed?MISS_EXTRA:0));sheriff.gesture(GUARD);
    }
    /** His blow, landed: the baton knocks the reader back and slows their legs; a fist is a hook, and pushes them across as well. */
    private static void land(ServerPlayer p,LiteraryActor sheriff,int swing){
        boolean baton=swing==BATON;
        if(!p.hurt(p.damageSources().mobAttack(sheriff),baton?BATON_DAMAGE:FIST_DAMAGE))return;
        var away=p.position().subtract(sheriff.position()).multiply(1,0,1);away=away.lengthSqr()<1e-4?Vec3.directionFromRotation(0,sheriff.getYRot()):away.normalize();
        if(!baton)away=away.add(new Vec3(-away.z,0,away.x).scale(swing==LEFT?.6:-.6)).normalize();
        p.knockback(baton?.9:.5,-away.x,-away.z);p.hurtMarked=true;
        if(baton)p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,25,1,false,false,false));
        BorrascaStory.heard(p,baton?SoundEvents.ZOMBIE_ATTACK_WOODEN_DOOR:SoundEvents.PLAYER_ATTACK_STRONG,p.blockPosition(),baton?.5F:1F);
    }
}

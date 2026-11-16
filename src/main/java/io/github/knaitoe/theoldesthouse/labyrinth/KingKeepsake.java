package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * 0.4.649, A sort of thing, part 9 (docs/SLEEPING_KING_0_4_649.md; the proposal's "Afterwards"): the keepsake, the crown.
 *
 * <p>In the King's country, while his dream goes on, a crown is real and filled in. Anywhere else it is only drawn, an outline with
 * nothing in it, and it fills in for as long as something is looking at its wearer: another reader, or one of the House's people. It
 * does what the country taught: notice makes a thing of it. In the stories that have a watcher, it tells the wearer they are watched:
 * the wearer hears it fill in (their own sound, nobody else's), and is told once what that means.
 *
 * <p><b>Looking</b> is as it is in the country (KingBreath): the looker's head turned to within twenty-five degrees of the wearer, with
 * nothing between them. A reader looks from up to forty-eight blocks, one of the House's people from up to thirty-two. The House's
 * people are whatever the House has made that is alive, awake and to be seen by this reader: nobody asleep, nobody who is only
 * another reader's to see, and nothing that came from outside the House. A look that has just left holds the crown for a second, so that
 * it does not flicker at the edge of a look.
 *
 * <p>Only a reader's own crown, worn, fills for being looked at. One carried, or another reader's on a head, is drawn outside the
 * country and filled within it.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class KingKeepsake {
    private KingKeepsake(){}
    /** How often a crown is seen to, in ticks; how long a look that has left still holds it; how far each kind of looker sees. */
    public static final int EVERY=5,LINGER=20;
    static final double READER_SEES=48,PEOPLE_SEE=32;
    /** At least this long between two soundings of the crown filling in. */
    static final int SOUND_GAP=60;
    private static final double COS=Math.cos(Math.toRadians(25));
    /** When each wearer was last looked at, and last heard their crown fill in (game time). */
    private static final Map<UUID,Long> LOOKED=new HashMap<>(),SOUNDED=new HashMap<>();

    @SubscribeEvent public static void tick(PlayerTickEvent.Post e){
        if(!(e.getEntity() instanceof ServerPlayer p)||p.tickCount%EVERY!=0||p.isSpectator())return;
        see(p);
    }
    @SubscribeEvent public static void left(PlayerEvent.PlayerLoggedOutEvent e){LOOKED.remove(e.getEntity().getUUID());SOUNDED.remove(e.getEntity().getUUID());}

    /** Sees to every crown this reader has about them. Public for tests. */
    public static void see(ServerPlayer p){
        var head=p.getItemBySlot(EquipmentSlot.HEAD);boolean live=dreaming(p);
        var inv=p.getInventory();
        for(int i=0;i<inv.getContainerSize();i++){var stack=inv.getItem(i);if(stack!=head&&KingCrownItem.is(stack))KingCrownItem.draw(stack,!live);}
        if(!KingCrownItem.is(head))return;
        if(!KingCrownItem.crowned(p)){KingCrownItem.draw(head,!live);return;}
        long now=p.level().getGameTime();
        if(!live&&watched(p))LOOKED.put(p.getUUID(),now);
        boolean filled=live||now-LOOKED.getOrDefault(p.getUUID(),Long.MIN_VALUE/2)<=LINGER;
        if(!KingCrownItem.draw(head,!filled)||live)return;
        if(!filled){if(KingCrownItem.tell(head,KingCrownItem.TOLD_DRAWN))King.cue(p,KingTexts.CROWN_ONLY_DRAWN);return;}
        if(now-SOUNDED.getOrDefault(p.getUUID(),Long.MIN_VALUE/2)>=SOUND_GAP){SOUNDED.put(p.getUUID(),now);p.playNotifySound(KingRegistry.CROWN_FILLS.get(),SoundSource.PLAYERS,.7F,1F);}
        if(KingCrownItem.tell(head,KingCrownItem.TOLD_FILLED))King.cue(p,KingTexts.CROWN_FILLED_IN);
    }

    /** Whether this reader is in the King's country while his dream goes on: the one place a crown is real of itself. */
    static boolean dreaming(ServerPlayer p){
        if(LiteraryVignettes.current(p)!=KingSleep.PLACE)return false;
        var b=LiteraryVignettes.base(p,KingSleep.PLACE);
        return b!=null&&KingSleep.after(b)==KingAfter.NONE&&KingSite.inside(p.position().subtract(b.getX(),b.getY(),b.getZ()));
    }

    /** Whether something is looking at this reader now: another reader, or one of the House's people. Public for tests. */
    public static boolean watched(ServerPlayer p){
        var l=p.serverLevel();
        for(var q:l.players()){
            if(q==p||q.isSpectator()||!q.isAlive()||q.distanceToSqr(p)>READER_SEES*READER_SEES)continue;
            if(KingBreath.looksAt(q,p))return true;
        }
        for(var e:l.getEntitiesOfClass(LivingEntity.class,p.getBoundingBox().inflate(PEOPLE_SEE),e->person(e,p)))if(looks(e,p))return true;
        return false;
    }
    /** One of the House's people, awake and to be seen by this reader. */
    static boolean person(LivingEntity e,ServerPlayer p){
        if(e instanceof Player||!e.isAlive()||e.isInvisible()||e.isSleeping())return false;
        if(!BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).getNamespace().equals(TheOldestHouse.MOD_ID))return false;
        if(e instanceof LiteraryActor a){
            // Somebody only another reader can see is not looking at this one; somebody faded right out is not there to look.
            var owner=a.owner();if(owner.isPresent()&&!owner.get().equals(p.getUUID()))return false;
            if(a.fade()>=100||a.waitingUnseen())return false;
        }
        return true;
    }
    static boolean looks(LivingEntity e,ServerPlayer p){
        var eye=e.getEyePosition();var at=p.getBoundingBox().getCenter();var to=at.subtract(eye);double d=to.length();if(d<1e-3)return true;
        if(d>PEOPLE_SEE)return false;
        if(Vec3.directionFromRotation(e.getXRot(),e.getYHeadRot()).dot(to.scale(1/d))<COS)return false;
        var hit=e.level().clip(new ClipContext(eye,at,ClipContext.Block.VISUAL,ClipContext.Fluid.NONE,e));
        return hit.getType()==HitResult.Type.MISS||hit.getLocation().distanceTo(eye)>=d-.6;
    }
    /** For tests: forget who was looked at when. */
    public static void forget(){LOOKED.clear();SOUNDED.clear();}
}

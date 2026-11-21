package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.DoorBlock;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

/**
 * 0.4.730, The list, part 8b (docs/THE_LIST_0_4_730.md): more of Amber's side, for readers in town who are not the one who went in, during
 * a hunt.
 * <ul>
 * <li><b>Barricading.</b> A hand on a shut door the slasher could strike (a neighbor's locked house door, or a steel door the five have
 * bolted) braces it: {@link #BRACE} more blows before it gives, for each reader who braces it and stays by it, up to {@link #MOST}. Kept per door until
 * the door gives or the hunt ends.</li>
 * <li><b>Knocking the slasher down.</b> A blow from such a reader on the one who went in puts the body down, as a blow from one of the five
 * does; then {@link #AGAIN} ticks before that reader can do it again.</li>
 * <li><b>Keeping a heart quiet.</b> Crouched within {@link #HUSH} of one of the five who is hidden or cornered, such a reader keeps that
 * heart from the slasher, and the pull with it (TheListHunt keeps who is hushed, each tick).</li>
 * </ul>
 * Each reader's record keeps how many doors they braced, how many times they put the slasher down and whose hearts they kept quiet.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class TheListAmber{
    private TheListAmber(){}
    public static final int BRACE=3,MOST=2,AGAIN=600;
    public static final double HUSH=2.5,REACH=4;
    /** Who has braced each door, by where its lower half stands in the level. */
    static final Map<Long,Set<UUID>> BRACED=new HashMap<>();
    /** When each reader last put the slasher down. */
    static final Map<UUID,Long> KNOCKED=new HashMap<>();

    /** Whether this reader is on Amber's side now: in town, a participant, without the body, a hunt on. */
    public static boolean side(ServerPlayer p,BlockPos b){
        return LiteraryVignettes.participant(p)&&!TheListBody.LIVE.containsKey(p.getUUID())&&TheListHunt.on(b);
    }
    /** How many more blows a door takes for its bracing: three for each who braced it and is still there, alive, holding it. */
    public static int extra(ServerLevel l,BlockPos at){
        var s=BRACED.get(at.asLong());if(s==null)return 0;int holding=0;
        for(var id:s)if(l.getPlayerByUUID(id) instanceof ServerPlayer q&&q.isAlive()&&!q.isSpectator()&&q.distanceToSqr(at.getX()+.5,at.getY()+.5,at.getZ()+.5)<=REACH*REACH)holding++;
        return Math.min(MOST,holding)*BRACE;
    }
    public static int braces(BlockPos at){var s=BRACED.get(at.asLong());return s==null?0:s.size();}

    /** A hand on a shut door the slasher could strike, from a reader on Amber's side: braced. True if it was theirs to brace. */
    static boolean brace(ServerPlayer p,BlockPos b,BlockPos lower,net.minecraft.nbt.CompoundTag own){
        if(!side(p,b))return false;
        var l=p.serverLevel();var at=b.offset(lower);var st=l.getBlockState(at);
        if(!(st.getBlock() instanceof DoorBlock)||st.getValue(DoorBlock.OPEN))return false;
        if(p.distanceToSqr(at.getX()+.5,at.getY()+.5,at.getZ()+.5)>REACH*REACH)return false;
        var by=BRACED.computeIfAbsent(at.asLong(),k->new HashSet<>());
        if(by.contains(p.getUUID())){TheList.cue(p,TheListTexts.BRACED_ALREADY);return true;}
        if(by.size()>=MOST){TheList.cue(p,TheListTexts.BRACED_FULL);return true;}
        by.add(p.getUUID());
        l.playSound(null,at.getX()+.5,at.getY()+.5,at.getZ()+.5,SoundEvents.WOOD_PLACE,SoundSource.BLOCKS,1F,.7F);
        TheList.cue(p,TheListTexts.BRACED);
        // The record in hand is the caller's, saved after the hand (TheList.click).
        own.putInt("Braced",own.getInt("Braced")+1);
        return true;
    }
    /** A door given or put back: its bracing goes with it. */
    static void unbrace(BlockPos at){BRACED.remove(at.asLong());}
    /** The hunt over in a town: every door in it unbraced. */
    static void clear(BlockPos b){var box=TheListBody.bounds(b);BRACED.keySet().removeIf(k->box.contains(BlockPos.of(k).getCenter()));}

    /** A blow from a reader on Amber's side on the one who went in: the body goes down, unless it is too soon. True if it was theirs. */
    public static boolean knock(ServerPlayer by,ServerPlayer slasher){
        var b=TheListBody.town(slasher);if(b==null||!TheListBody.LIVE.containsKey(slasher.getUUID())||!side(by,b))return false;
        if(TheListBody.down(slasher)>0)return true;
        long now=by.level().getGameTime();Long last=KNOCKED.get(by.getUUID());
        if(last!=null&&now-last<AGAIN){TheList.cue(by,TheListTexts.KNOCK_SOON);return true;}
        KNOCKED.put(by.getUUID(),now);
        by.swing(net.minecraft.world.InteractionHand.MAIN_HAND,true);
        by.level().playSound(null,slasher.getX(),slasher.getEyeY(),slasher.getZ(),SoundEvents.PLAYER_ATTACK_KNOCKBACK,SoundSource.PLAYERS,1F,.85F);
        TheListBody.felled(slasher,TheListTexts.KNOCKED_DOWN,by.position());
        TheList.cue(by,TheListTexts.KNOCKED_IT);
        var d=LabyrinthData.get(by.server);var own=LiteraryVignettes.personal(d,by.getUUID(),TheList.PLACE);own.putInt("Knocked",own.getInt("Knocked")+1);LiteraryVignettes.save(d,by.getUUID(),TheList.PLACE,own);
        return true;
    }
    @SubscribeEvent public static void attack(AttackEntityEvent e){
        if(!(e.getEntity() instanceof ServerPlayer by)||!(e.getTarget() instanceof ServerPlayer slasher))return;
        if(knock(by,slasher))e.setCanceled(true);
    }
    /** Whether a reader is crouched near enough one of the five to keep that heart quiet. */
    static boolean hushes(ServerPlayer q,LiteraryActor a){
        if(q.isSpectator()||!q.isCrouching()||!LiteraryVignettes.participant(q)||TheListBody.LIVE.containsKey(q.getUUID()))return false;
        return q.distanceTo(a)<=HUSH;
    }
    /** A heart kept quiet for the first time this hunt by this reader: they are told, and their record keeps whose. */
    static void hushed(ServerPlayer q,int who){
        TheList.cue(q,String.format(TheListTexts.HUSHED,TheListTexts.NAMES[who]));
        var d=LabyrinthData.get(q.server);var own=LiteraryVignettes.personal(d,q.getUUID(),TheList.PLACE);own.putInt("Hushed",own.getInt("Hushed")|1<<who);LiteraryVignettes.save(d,q.getUUID(),TheList.PLACE,own);
    }
    /** For a check: when this reader can put the slasher down again, forgotten. */
    public static void rest(UUID id){KNOCKED.remove(id);}
    @SubscribeEvent public static void stopped(ServerStoppedEvent e){clearAll();}
    static void clearAll(){BRACED.clear();KNOCKED.clear();}
}

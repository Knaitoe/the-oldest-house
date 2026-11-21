package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.732, The list, part 8c (docs/THE_LIST_0_4_732.md): readers on Amber's side testing the rules from the tapes for real, during a
 * hunt. Each rule is tested once by each reader and kept in their own record ({@code Tried}, a bit for each):
 * <ul>
 * <li>{@link #LIGHT}, "Light keeps him back": wrong. A light held up in either hand, the one who went in in front of them and in the
 * light, within {@link #REACH}, for {@link #HOLD} ticks while it does not go back.</li>
 * <li>{@link #ASKED}, "He can't come in unless you ask him in": wrong. A door the one who went in breaks down, a reader within
 * {@link #REACH} of it.</li>
 * <li>{@link #NIGHT}, "Only at night": right. A reader in town when first light ends a hunt (not the one who went in lying down).</li>
 * </ul>
 * Never the one who went in, never a spectator.
 */
public final class TheListRules{
    private TheListRules(){}
    public static final int LIGHT=1,ASKED=2,NIGHT=4;
    public static final double REACH=8;
    /** How long the light must be held on it, in ticks; and how wide the reader's look and the light's cone are, in degrees. */
    public static final int HOLD=60;
    public static final double CONE=25;
    /** How long each reader has held a light on it so far, unbroken. */
    static final Map<UUID,Integer> HELD=new HashMap<>();

    public static boolean light(ItemStack s){return s.is(Items.TORCH)||s.is(Items.SOUL_TORCH)||s.is(Items.LANTERN)||s.is(Items.SOUL_LANTERN);}
    public static boolean holdsLight(ServerPlayer p){return light(p.getItemInHand(InteractionHand.MAIN_HAND))||light(p.getItemInHand(InteractionHand.OFF_HAND));}
    /** Whether this reader has tried this rule. */
    public static boolean tried(net.minecraft.nbt.CompoundTag own,int rule){return (own.getInt("Tried")&rule)!=0;}
    static boolean onIt(ServerPlayer q,ServerPlayer slasher){
        if(q.distanceTo(slasher)>REACH)return false;
        var to=slasher.getEyePosition().subtract(q.getEyePosition()).normalize();
        if(Math.toDegrees(Math.acos(Math.max(-1,Math.min(1,to.dot(q.getLookAngle())))))>CONE)return false;
        return q.level().clip(new ClipContext(q.getEyePosition(),slasher.getEyePosition(),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,q)).getType()==HitResult.Type.MISS;
    }
    /** Mark a rule tried for a reader, once: told, and kept. */
    static void tried(ServerPlayer q,int rule,String cue){
        var d=LabyrinthData.get(q.server);var own=LiteraryVignettes.personal(d,q.getUUID(),TheList.PLACE);
        if(tried(own,rule))return;
        own.putInt("Tried",own.getInt("Tried")|rule);LiteraryVignettes.save(d,q.getUUID(),TheList.PLACE,own);
        TheList.cue(q,cue);
    }
    /** One tick of a hunt: the light held on the one who went in. */
    static void tick(ServerLevel l,TheListNight.Night n,List<ServerPlayer> here){
        if(n.slasher==null||!(l.getPlayerByUUID(n.slasher) instanceof ServerPlayer slasher)||!TheListBody.LIVE.containsKey(slasher.getUUID()))return;
        for(var q:here){
            if(q==slasher||!TheListAmber.side(q,n.base)){HELD.remove(q.getUUID());continue;}
            if(holdsLight(q)&&TheListBody.down(slasher)==0&&onIt(q,slasher)){
                int t=HELD.merge(q.getUUID(),1,Integer::sum);
                if(t>=HOLD)tried(q,LIGHT,TheListTexts.RULE_LIGHT);
            }else HELD.remove(q.getUUID());
        }
    }
    /** A door broken down: anyone on Amber's side by it has seen it come in unasked. */
    static void cameIn(ServerLevel l,BlockPos b,BlockPos at){
        var c=Vec3.atCenterOf(at);
        for(var q:TheList.readers(l,b))if(TheListAmber.side(q,b)&&q.distanceToSqr(c)<=REACH*REACH)tried(q,ASKED,TheListTexts.RULE_ASKED);
    }
    /** First light on a hunt: everyone on Amber's side in town has seen it. */
    static void dawn(ServerLevel l,TheListNight.Night n){
        for(var q:TheListNight.inTown(l,n.base))if(!q.getUUID().equals(n.slasher)&&LiteraryVignettes.participant(q))tried(q,NIGHT,TheListTexts.RULE_NIGHT);
    }
    public static void clearAll(){HELD.clear();}
}

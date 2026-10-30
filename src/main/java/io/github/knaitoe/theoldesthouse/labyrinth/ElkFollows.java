package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import java.util.*;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.CanPlayerSleepEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * The Last Stop after <i>The Only Good Indians</i> (0.4.187, owner: "The elk entity in that story punished its protagonist
 * for harming animals. That might be better?"). The men's accusations read the reader's real record of animals killed. A
 * reader who strikes the elk fails the night: it resolves nothing, and she follows them home and answers what they do to
 * animals. The meat and hide of what they kill spoil as they fall; for each animal they kill, one of their own penned animals
 * is gone in the morning, its gate open; killing a young animal costs them the night: they cannot sleep, and out on open
 * ground the field brings them back to where they stood. It ends on a later night at the Last Stop when they let her pass
 * without raising a hand, having killed nothing since the night before. It never wounds the reader; everything here is theirs
 * alone.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class ElkFollows {
    private ElkFollows(){}
    static final LabyrinthPlace PLACE=LabyrinthPlace.ELK_LOT;
    public static final String FOLLOWS="Follows0187",STRUCK_VISIT="StruckVisit0187",WOKE_VISIT="WokeVisit0187",BASE="KillBase0187",IN_KIND="InKind0187",IN_KIND_DAY="InKindDay0187",CALF_UNTIL="CalfUntil0187";
    /** How far the field lets a reader walk from where the night found them before it brings them back. */
    public static final int FIELD_REACH=24;
    /** The animals the men count, in the order they name them, with their plurals. */
    static final List<Map.Entry<EntityType<?>,String>> COUNTED=List.of(
        Map.entry(EntityType.COW,"cows"),Map.entry(EntityType.PIG,"pigs"),Map.entry(EntityType.SHEEP,"sheep"),Map.entry(EntityType.CHICKEN,"chickens"),
        Map.entry(EntityType.RABBIT,"rabbits"),Map.entry(EntityType.GOAT,"goats"),Map.entry(EntityType.MOOSHROOM,"mooshrooms"),Map.entry(EntityType.HORSE,"horses"),
        Map.entry(EntityType.DONKEY,"donkeys"),Map.entry(EntityType.MULE,"mules"),Map.entry(EntityType.LLAMA,"llamas"),Map.entry(EntityType.CAMEL,"camels"),
        Map.entry(EntityType.FOX,"foxes"),Map.entry(EntityType.POLAR_BEAR,"polar bears"),Map.entry(EntityType.PANDA,"pandas"),Map.entry(EntityType.TURTLE,"turtles"),
        Map.entry(EntityType.ARMADILLO,"armadillos"),Map.entry(EntityType.SNIFFER,"sniffers"),Map.entry(EntityType.WOLF,"wolves"),Map.entry(EntityType.CAT,"cats"),
        Map.entry(EntityType.OCELOT,"ocelots"),Map.entry(EntityType.PARROT,"parrots"),Map.entry(EntityType.FROG,"frogs"));
    /** Where the field found each reader on a restless night. */
    private static final Map<UUID,Vec3> ANCHOR=new HashMap<>();

    private static CompoundTag own(ServerPlayer p){return LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),PLACE);}
    private static void save(ServerPlayer p,CompoundTag own){LiteraryVignettes.save(LabyrinthData.get(p.server),p.getUUID(),PLACE,own);}
    public static boolean follows(ServerPlayer p){return own(p).getBoolean(FOLLOWS);}
    private static void hint(ServerPlayer p,String line){p.displayClientMessage(Component.literal(line).withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),false);}

    // ------------------------------------------------------------------------------------------------ the record
    /** Every animal the reader has killed in their world, by the game's own count. */
    public static int kills(ServerPlayer p){int n=0;for(var e:COUNTED)n+=p.getStats().getValue(Stats.ENTITY_KILLED.get(e.getKey()));return n;}
    /**
     * What the men say of the reader's record: the two kinds they have killed most, named back to them with their name in
     * between; or, for a reader who has killed none, the accusation faltering.
     */
    public static String accusation(ServerPlayer p){
        var counts=new ArrayList<Map.Entry<String,Integer>>();
        for(var e:COUNTED){int n=p.getStats().getValue(Stats.ENTITY_KILLED.get(e.getKey()));if(n>0)counts.add(Map.entry(e.getValue(),n));}
        if(counts.isEmpty())return "Never even shot a deer, %s? Then why'd it look at you?";
        counts.sort((a,b)->b.getValue()-a.getValue());
        String first=capital(words(counts.get(0).getValue()))+" "+(counts.get(0).getValue()==1?single(counts.get(0).getKey()):counts.get(0).getKey());
        if(counts.size()==1)return first+", %s. We counted.";
        return first+", %s. "+capital(words(counts.get(1).getValue()))+" "+(counts.get(1).getValue()==1?single(counts.get(1).getKey()):counts.get(1).getKey())+".";
    }
    static String single(String plural){return switch(plural){case "sheep"->"sheep";case "foxes"->"fox";case "wolves"->"wolf";default->plural.endsWith("s")?plural.substring(0,plural.length()-1):plural;};}
    static String capital(String s){return s.isEmpty()?s:Character.toUpperCase(s.charAt(0))+s.substring(1);}
    /** A count as the men would say it, in words up to nine hundred and ninety-nine. */
    static String words(int n){
        String[] ones={"zero","one","two","three","four","five","six","seven","eight","nine","ten","eleven","twelve","thirteen","fourteen","fifteen","sixteen","seventeen","eighteen","nineteen"};
        String[] tens={"","","twenty","thirty","forty","fifty","sixty","seventy","eighty","ninety"};
        if(n<0||n>999)return Integer.toString(n);
        if(n<20)return ones[n];
        if(n<100)return tens[n/10]+(n%10==0?"":"-"+ones[n%10]);
        return ones[n/100]+" hundred"+(n%100==0?"":" and "+words(n%100));
    }

    // ------------------------------------------------------------------------------------------------ the night
    /** Striking her, in the lot or the field, is the failure; the record keeps which night it was. */
    public static void struck(ServerPlayer p,CompoundTag own){own.putInt(STRUCK_VISIT,own.getInt("Visit"));}
    public static boolean struckTonight(CompoundTag own){return own.contains(STRUCK_VISIT)&&own.getInt(STRUCK_VISIT)==own.getInt("Visit");}
    /** After waking on the porch from a struck night, the night is over for this reader. */
    public static boolean overTonight(CompoundTag own){return own.contains(WOKE_VISIT)&&own.getInt(WOKE_VISIT)==own.getInt("Visit");}
    /**
     * The capture after a struck night: the reader wakes on the porch as any capture does, but the clipping is not about them
     * and nothing is resolved; she follows them home.
     */
    public static void wokeStruck(ServerPlayer p,CompoundTag own){
        own.putInt(WOKE_VISIT,own.getInt("Visit"));own.putInt("Beat",own.getInt("Visit"));own.remove("EndingSide0492");
        boolean already=own.getBoolean(FOLLOWS);own.putBoolean(FOLLOWS,true);own.putInt(BASE,kills(p));
        hint(p,"The noticeboard is bare. Nobody wrote about you tonight.");
        hint(p,already?"She is still following you.":"Out past the fence something stands and watches you go. It will follow you home.");
    }
    /**
     * A later night that ends without the reader raising a hand to her: she passes. If they have killed nothing since their last
     * night here she lets them go; otherwise she looks back at them, and the count starts again from tonight.
     */
    public static void passed(ServerPlayer p,CompoundTag own){
        if(!own.getBoolean(FOLLOWS)||struckTonight(own))return;
        int now=kills(p);
        if(now<=own.getInt(BASE)){own.putBoolean(FOLLOWS,false);own.remove(IN_KIND);own.remove(CALF_UNTIL);ANCHOR.remove(p.getUUID());hint(p,"She passes you in the dark and does not look back. She does not follow you now.");}
        else{own.putInt(BASE,now);hint(p,"She passes you in the dark, and looks back at you. There is more blood on you than there was.");}
    }

    // ------------------------------------------------------------------------------------------------ the win
    /** The cap from the broken post, for a reader who came through the night: theirs to wear. */
    public static ItemStack cap(){
        var s=new ItemStack(Items.LEATHER_HELMET);
        s.set(DataComponents.CUSTOM_NAME,Component.literal("The cap from the broken post").withStyle(s2->s2.withItalic(false)));
        s.set(DataComponents.DYED_COLOR,new DyedItemColor(0x7A3324,false));
        s.set(DataComponents.LORE,new ItemLore(List.of(Component.literal("It lay by the broken post after the first time round.").withStyle(ChatFormatting.GRAY))));
        return s;
    }
    /** What winning costs: the tab comes due, an emerald a drink, as far as the reader's pack goes. */
    public static void payTab(ServerPlayer p,CompoundTag own){
        int owed=own.getInt("Drinks")-own.getInt("TabPaid0187");if(owed<=0)return;
        int paid=0;var inv=p.getInventory();
        for(int i=0;i<inv.getContainerSize()&&paid<owed;i++){var s=inv.getItem(i);if(!s.is(Items.EMERALD)||s.has(DataComponents.CUSTOM_DATA))continue;int take=Math.min(owed-paid,s.getCount());s.shrink(take);paid+=take;}
        own.putInt("TabPaid0187",own.getInt("TabPaid0187")+paid);save(p,own);inv.setChanged();
        hint(p,paid==0?"Marla's tab is still open. She has your name.":paid<owed?"The tab came due. You paid what you could: "+paid+(paid==1?" emerald.":" emeralds."):"The tab came due: "+paid+(paid==1?" emerald.":" emeralds."));
    }

    // ------------------------------------------------------------------------------------------------ what she answers
    private static boolean outside(net.minecraft.world.level.Level l){return !HouseDimensions.isHouseDimension(l.dimension());}
    private static ServerPlayer killer(net.minecraft.world.damagesource.DamageSource s){return s.getEntity() instanceof ServerPlayer p&&!p.isSpectator()?p:null;}
    /** The meat and the hide spoil as they fall. */
    @SubscribeEvent public static void drops(LivingDropsEvent e){
        if(!(e.getEntity() instanceof Animal)||!outside(e.getEntity().level()))return;var p=killer(e.getSource());if(p==null||!follows(p))return;
        boolean spoiled=false;
        for(var it=e.getDrops().iterator();it.hasNext();){ItemEntity drop=it.next();var s=drop.getItem();
            if(s.is(Items.LEATHER)||s.is(Items.RABBIT_HIDE)){it.remove();spoiled=true;}
            else if(s.has(DataComponents.FOOD)&&!s.is(Items.ROTTEN_FLESH)){drop.setItem(new ItemStack(Items.ROTTEN_FLESH,s.getCount()));spoiled=true;}}
        if(spoiled)p.displayClientMessage(Component.literal("It spoiled as it fell.").withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),true);
    }
    /** Each animal they kill is counted against them; a young one costs them the night. */
    @SubscribeEvent public static void death(LivingDeathEvent e){
        if(!(e.getEntity() instanceof Animal a)||!outside(a.level()))return;var p=killer(e.getSource());if(p==null)return;var own=own(p);if(!own.getBoolean(FOLLOWS))return;
        own.putInt(IN_KIND,own.getInt(IN_KIND)+1);
        if(a.isBaby()){long now=p.server.overworld().getDayTime();own.putLong(CALF_UNTIL,(now/24000L+1)*24000L);hint(p,"That one was young. She will come for your night.");}
        save(p,own);
    }
    /** A reader whose night she has taken. */
    public static boolean restless(ServerPlayer p){var own=own(p);return own.getBoolean(FOLLOWS)&&own.contains(CALF_UNTIL)&&p.server.overworld().getDayTime()<own.getLong(CALF_UNTIL);}
    @SubscribeEvent public static void sleep(CanPlayerSleepEvent e){
        if(e.getProblem()!=null||!restless(e.getEntity()))return;
        e.setProblem(Player.BedSleepingProblem.OTHER_PROBLEM);hint(e.getEntity(),"You can't sleep. Something is standing out in the field, waiting.");
    }
    /**
     * In kind: one of the reader's own penned animals near where they sleep is gone in the morning, the gate left open. Never a
     * named, leashed or tamed animal; never one outside a pen. One a morning, for as many as they have killed.
     */
    public static boolean morning(ServerPlayer p){
        var own=own(p);if(own.getInt(IN_KIND)<=0)return false;
        var l=p.server.getLevel(p.getRespawnDimension());if(l==null)return false;
        var home=p.getRespawnPosition()!=null?p.getRespawnPosition():l.getSharedSpawnPos();
        Animal taken=null;
        for(var a:l.getEntitiesOfClass(Animal.class,new AABB(home).inflate(32),a->a.isAlive()&&!a.hasCustomName()&&!a.isLeashed()&&!(a instanceof TamableAnimal t&&t.isTame())&&!(a instanceof AbstractHorse h&&h.isTamed())&&penned(l,a.blockPosition())))
            if(taken==null||a.distanceToSqr(Vec3.atCenterOf(home))<taken.distanceToSqr(Vec3.atCenterOf(home)))taken=a;
        if(taken==null)return false;
        var gate=gate(l,taken.blockPosition());
        if(gate!=null)l.setBlock(gate,l.getBlockState(gate).setValue(FenceGateBlock.OPEN,true),3);
        taken.discard();own.putInt(IN_KIND,own.getInt(IN_KIND)-1);save(p,own);
        hint(p,gate!=null?"A gate stands open this morning, and one of your animals is gone. The prints by the gate are not a cow's.":"One of your animals is gone this morning. The prints in the mud are not a cow's.");
        return true;
    }
    static boolean penned(ServerLevel l,BlockPos at){
        for(var c:BlockPos.betweenClosed(at.offset(-4,-1,-4),at.offset(4,1,4))){var s=l.getBlockState(c);if(s.is(BlockTags.FENCES)||s.is(BlockTags.FENCE_GATES))return true;}return false;
    }
    static BlockPos gate(ServerLevel l,BlockPos at){
        BlockPos best=null;for(var c:BlockPos.betweenClosed(at.offset(-6,-1,-6),at.offset(6,1,6))){if(l.getBlockState(c).getBlock() instanceof FenceGateBlock&&(best==null||c.distSqr(at)<best.distSqr(at)))best=c.immutable();}return best;
    }
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        var server=e.getServer();if(server.getTickCount()%5!=0)return;
        long time=server.overworld().getDayTime();int day=(int)(time/24000L);boolean dawn=time%24000L<1000;
        for(var p:server.getPlayerList().getPlayers()){
            if(!p.isAlive()||p.isSpectator()){ANCHOR.remove(p.getUUID());continue;}
            var own=own(p);if(!own.getBoolean(FOLLOWS)){ANCHOR.remove(p.getUUID());continue;}
            if(dawn&&own.getInt(IN_KIND)>0&&own.getInt(IN_KIND_DAY)!=day){own.putInt(IN_KIND_DAY,day);save(p,own);morning(p);}
            field(p);
        }
    }
    /**
     * The calf's night: out on open ground in their own world, the reader who walks too far from where the night found them is
     * back where they stood, the herd's eyes in the dark on either side.
     */
    static void field(ServerPlayer p){
        var l=p.serverLevel();
        if(!restless(p)||!outside(l)||!l.isNight()||!l.canSeeSky(p.blockPosition().above())){if(!restless(p))ANCHOR.remove(p.getUUID());return;}
        var at=ANCHOR.computeIfAbsent(p.getUUID(),id->{hint(p,"The field again.");return p.position();});
        double dx=p.getX()-at.x,dz=p.getZ()-at.z;
        if(dx*dx+dz*dz>FIELD_REACH*FIELD_REACH){p.teleportTo(l,at.x,at.y,at.z,p.getYRot(),p.getXRot());p.fallDistance=0;}
        if(p.tickCount%40==0)for(int side:new int[]{-1,1}){var eye=p.position().add(p.getLookAngle().yRot((float)(side*Math.PI/2.6)).multiply(7,0,7)).add(0,1.4,0);l.sendParticles(p,ParticleTypes.GLOW,true,eye.x,eye.y,eye.z,2,.12,.02,0,0);}
    }
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent e){ANCHOR.remove(e.getEntity().getUUID());}
}

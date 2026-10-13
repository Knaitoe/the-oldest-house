package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * One occupied cooking clock; every package, raw frank and cooked serving retains its round. 0.4.77: a cooked frank left on
 * the griddle keeps cooking on the same occupied clock: it gets hot at {@link #HOT_TICKS} and burns at {@link #BURN_TICKS},
 * each with its own cue, and a burnt one stays burnt on the plate, in a cousin's hand and in the reader's. Burnt franks still
 * count for supper; the cousins notice.
 */
public final class GoatmanSupper {
    public static final int COOK_TICKS=120,PAN_CAPACITY=4,HOT_TICKS=160,BURN_TICKS=300;
    public static final String BURNT="Burnt0476",NOT_YET="Not right now. Supper goes on the plates first.";
    private static final String[] BURNT_LINES={
        "A cousin: Mine's all black. ...It's fine. I like them like this.",
        "A cousin: Did you even turn these over? I told you to turn them over.",
        "A cousin: It tastes like the smoke smells."};
    private GoatmanSupper(){}
    public static boolean burnt(ItemStack item){var custom=item.get(DataComponents.CUSTOM_DATA);return custom!=null&&custom.copyTag().getBoolean(BURNT);}
    /** The round's own frank: burnt ones carry their char in their name and model, and none can be eaten before supper. */
    static ItemStack frank(CompoundTag r,boolean burnt){
        var item=new ItemStack(GoatmanRegistry.BRAT.get());CustomData.update(DataComponents.CUSTOM_DATA,item,t->{t.putUUID(GoatmanVignette.ROUND,r.getUUID("Id"));if(burnt)t.putBoolean(BURNT,true);});
        if(burnt)blacken(item);return item;
    }
    private static void blacken(ItemStack item){item.set(DataComponents.CUSTOM_MODEL_DATA,new CustomModelData(1));item.set(DataComponents.ITEM_NAME,Component.literal("Burnt brat"));}
    public static boolean ready(CompoundTag r){
        int expected=GoatmanVignette.expectedCount(r);
        return r.getBoolean("MealReady0465")||expected>0&&Integer.bitCount(r.getInt("Plated0464")&~r.getInt("EmptyPlates0465"))>=expected;
    }
    private static boolean belongs(ItemStack item,CompoundTag r){
        var custom=item.get(DataComponents.CUSTOM_DATA);var tag=custom==null?null:custom.copyTag();
        return tag!=null&&tag.hasUUID(GoatmanVignette.ROUND)&&r.hasUUID("Id")&&tag.getUUID(GoatmanVignette.ROUND).equals(r.getUUID("Id"));
    }
    private static void give(ServerPlayer p,ItemStack item){if(!p.getInventory().add(item))p.drop(item,false);}
    private static boolean early(ServerPlayer p,ItemStack item){
        if(!GoatmanVignette.inside(p))return false;var r=GoatmanVignette.run(LabyrinthData.get(p.server));return GoatmanVignette.fresh(r)&&!ready(r)&&belongs(item,r);
    }
    /** The refusal to eat before supper is on the plates, now with a line to say so. */
    public static void onFood(net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent.Start e){
        if(!(e.getEntity() instanceof ServerPlayer p)||!e.getItem().is(GoatmanRegistry.BRAT.get()))return;
        if(early(p,e.getItem())){e.setCanceled(true);p.displayClientMessage(Component.literal(NOT_YET),true);}
    }
    /**
     * A plate can be visible above the slab even when the native ray misses that slab. Serve it before the food-use
     * fallback, then answer actual eating attempts, including a full reader who never starts eating.
     */
    public static void onUse(PlayerInteractEvent.RightClickItem e){
        if(!(e.getEntity() instanceof ServerPlayer p)||!e.getItemStack().is(GoatmanRegistry.BRAT.get()))return;
        if(serveAimed(p,e.getHand())){e.setCanceled(true);e.setCancellationResult(InteractionResult.SUCCESS);return;}
        if(!early(p,e.getItemStack()))return;
        if(p.serverLevel().getGameTime()-p.getPersistentData().getLong("GoatmanClick0476")>1)p.displayClientMessage(Component.literal(NOT_YET),true);
    }
    /** Native entity selection can intercept the block below a visible plate. It must not swallow serving. */
    public static void onEntity(PlayerInteractEvent.EntityInteract e){
        if(e.getEntity() instanceof ServerPlayer p&&tableEntity(e.getTarget())&&(serveAimed(p,e.getHand())||GoatmanConversations.talk(p,e.getTarget(),e.getHand()))){e.setCanceled(true);e.setCancellationResult(InteractionResult.SUCCESS);}
    }
    public static void onEntitySpecific(PlayerInteractEvent.EntityInteractSpecific e){
        if(e.getEntity() instanceof ServerPlayer p&&tableEntity(e.getTarget())&&(serveAimed(p,e.getHand())||GoatmanConversations.talk(p,e.getTarget(),e.getHand()))){e.setCanceled(true);e.setCancellationResult(InteractionResult.SUCCESS);}
    }
    private static boolean tableEntity(Entity target){return target instanceof GoatmanChild&&target.getTags().contains(GoatmanVignette.ACTOR)||target.getTags().contains(GoatmanWoods.PLATE);}
    private static boolean serving(ServerPlayer p,CompoundTag run){
        if(!run.getBoolean("PlayerServes0464")||run.getInt("Phase")!=GoatmanVignette.GATHERING)return false;
        var member=run.getCompound("Cohort").getCompound(p.getUUID().toString());return member.getBoolean("Active")&&!member.getBoolean("Failed");
    }
    /** Aim at the displayed plate, with native reach and block occlusion, rather than wherever its ray ends. */
    private static int aimedSeat(ServerPlayer p,BlockPos base,CompoundTag run){
        var eye=p.getEyePosition();var end=eye.add(p.getLookAngle().scale(p.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE)));int seat=-1;double nearest=Double.MAX_VALUE;
        for(int i=0;i<GoatmanVignette.expectedCount(run);i++){
            var at=GoatmanWoods.plate(i).add(base.getX(),base.getY(),base.getZ());var hit=new AABB(at.x-.32,at.y-.1,at.z-.32,at.x+.32,at.y+.1,at.z+.32).clip(eye,end);if(hit.isEmpty())continue;
            double distance=eye.distanceToSqr(hit.get());if(distance>=nearest)continue;
            var block=p.serverLevel().clip(new ClipContext(eye,at,ClipContext.Block.OUTLINE,ClipContext.Fluid.NONE,p));
            if(block.getType()!=HitResult.Type.MISS&&!(block.getBlockPos().equals(BlockPos.containing(at).below())&&GoatmanWoods.tableSupport(p.serverLevel().getBlockState(block.getBlockPos()))))continue;
            nearest=distance;seat=i;
        }
        return seat;
    }
    private static boolean serveAimed(ServerPlayer p,InteractionHand hand){
        if(!GoatmanVignette.inside(p))return false;var data=LabyrinthData.get(p.server);var run=GoatmanVignette.run(data);var held=p.getItemInHand(hand);
        if(!serving(p,run)||!held.is(GoatmanRegistry.BRAT.get()))return false;
        var base=GoatmanVignette.base(p.server);int seat=aimedSeat(p,base,run);if(seat<0)return false;
        plate(p,held,base,run,seat);saveClick(p,data,run);return true;
    }
    private static void plate(ServerPlayer player,ItemStack held,BlockPos base,CompoundTag run,int seat){
        int mask=run.getInt("Plated0464"),bit=1<<seat,empty=run.getInt("EmptyPlates0465");
        var at=BlockPos.containing(GoatmanWoods.plate(seat).add(base.getX(),base.getY(),base.getZ()));var level=player.serverLevel();
        if(!level.hasChunkAt(at)||!level.areEntitiesLoaded(net.minecraft.world.level.ChunkPos.asLong(at.getX()>>4,at.getZ()>>4))){player.displayClientMessage(Component.literal("Wait a moment."),true);return;}
        var shown=GoatmanWoods.serving(level,base,seat);var original=GoatmanWoods.food(level,shown);
        // A bookkeeping bit cannot make a physically empty, loaded plate occupied.
        // Conversely an existing original must never be overwritten by another serving.
        boolean occupied=original.is(GoatmanRegistry.BRAT.get());
        if(occupied){mask|=bit;empty&=~bit;GoatmanWoods.served(level,base,seat,original);}else empty|=bit;
        run.putInt("Plated0464",mask);run.putInt("EmptyPlates0465",empty);
        if(held.is(GoatmanRegistry.BRAT.get())&&belongs(held,run)){
            if(!occupied){boolean charred=burnt(held);if(GoatmanWoods.served(level,base,seat,held.copyWithCount(1))==null)return;held.shrink(1);run.putInt("Plated0464",mask|bit);run.putInt("EmptyPlates0465",empty&~bit);
                run.putInt("BurntPlates0476",charred?run.getInt("BurntPlates0476")|bit:run.getInt("BurntPlates0476")&~bit);
                if(ready(run)){run.putBoolean("MealReady0465",true);run.putBoolean("MealFrozen0465",true);}}
            else player.displayClientMessage(Component.literal("There's already one here."),true);
        }else if(held.isEmpty()&&occupied){
            run.putInt("BurntPlates0476",run.getInt("BurntPlates0476")&~bit);
            run.putInt("EmptyPlates0465",empty|bit);shown.discard();give(player,original.copyWithCount(1));
        }else player.displayClientMessage(Component.literal(held.is(GoatmanRegistry.BRAT.get())?"That wasn't in this supper's pack.":"Put a cooked frank on the plate."),true);
    }
    private static void saveClick(ServerPlayer player,LabyrinthData data,CompoundTag run){player.getPersistentData().putLong("GoatmanClick0476",player.serverLevel().getGameTime());GoatmanVignette.saveRun(data,run);}
    /** What the next real cousin to bite into a burnt one says, the first three times. */
    static String burntLine(CompoundTag r){int n=r.getInt("BurntEaten0476");r.putInt("BurntEaten0476",n+1);return n<BURNT_LINES.length?BURNT_LINES[n]:null;}
    /**
     * Cousins eat only food the player actually put down. The impostor uses the absent runner's place, and eats a burnt one
     * without a word. Returns 0 for nothing to say, 1 for the missing supper, 2 for a real cousin's burnt one.
     */
    static int eat(ServerLevel l,BlockPos b,CompoundTag r,GoatmanChild c,int index,int clock){
        if(c.getPersistentData().contains("EatingUntil0465")&&clock>=c.getPersistentData().getInt("EatingUntil0465")&&c.getMainHandItem().is(GoatmanRegistry.BRAT.get())){c.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,ItemStack.EMPTY);c.getPersistentData().remove("EatingUntil0465");}
        if(!GoatmanVignette.fresh(r)||!ready(r)||!c.seated()||(r.getInt("EatenCousins0465")&(1<<index))!=0)return 0;
        int real=index==r.getInt("Wrong")?r.getInt("Runner"):index,seat=real<r.getInt("Wrong")?real:real-1,bit=1<<seat;
        if((r.getInt("Plated0464")&~r.getInt("EmptyPlates0465")&bit)==0){
            if(index==r.getInt("Runner")&&!r.getBoolean("MissingSupper0465")){r.putBoolean("MissingSupper0465",true);return 1;}return 0;
        }
        var plate=BlockPos.containing(GoatmanWoods.plate(seat).add(b.getX(),b.getY(),b.getZ()));
        if(!l.hasChunkAt(plate)||!l.areEntitiesLoaded(net.minecraft.world.level.ChunkPos.asLong(plate.getX()>>4,plate.getZ()>>4)))return 0;
        var shown=GoatmanWoods.serving(l,b,seat);var original=GoatmanWoods.food(l,shown);
        if(!original.is(GoatmanRegistry.BRAT.get())){r.putInt("EmptyPlates0465",r.getInt("EmptyPlates0465")|bit);return 0;}
        boolean charred=burnt(original);r.putInt("BurntPlates0476",r.getInt("BurntPlates0476")&~bit);
        r.putInt("EmptyPlates0465",r.getInt("EmptyPlates0465")|bit);r.putInt("EatenCousins0465",r.getInt("EatenCousins0465")|(1<<index));shown.discard();
        c.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,original.copyWithCount(1));c.getPersistentData().putInt("EatingUntil0465",clock+80);return charred&&index!=r.getInt("Wrong")?2:0;
    }
    public static void tick(ServerLevel l,BlockPos b,CompoundTag r,List<ServerPlayer> readers){
        if(!r.getBoolean("PlayerServes0464"))return;
        if(l.getGameTime()%20==0)GoatmanWoods.fitPlaceSettings(l,b);
        if(!r.getBoolean("PanPositioned0468")){GoatmanWoods.cookingPan(l,b,r);r.putBoolean("PanPositioned0468",true);}
        var cooking=r.getList("Cooking0465",Tag.TAG_COMPOUND);var cooked=r.getList("CookedPan0465",Tag.TAG_COMPOUND);boolean changed=false,done=false,hot=false,burned=false;
        // Franks cooked before 0.4.77 carry no heat and never burn.
        for(int i=0;i<cooked.size();i++){
            var t=cooked.getCompound(i);if(!t.contains("Heat")||t.getBoolean(BURNT))continue;int heat=t.getInt("Heat")+1;t.putInt("Heat",heat);
            if(heat==HOT_TICKS)hot=true;
            if(heat>=BURN_TICKS){
                var item=ItemStack.parseOptional(l.registryAccess(),t.getCompound("Item"));CustomData.update(DataComponents.CUSTOM_DATA,item,c->c.putBoolean(BURNT,true));blacken(item);
                t.put("Item",item.save(l.registryAccess()));t.putBoolean(BURNT,true);burned=changed=true;
            }
        }
        for(int i=cooking.size()-1;i>=0;i--){
            var t=cooking.getCompound(i);int left=t.getInt("Left")-1;t.putInt("Left",left);
            if(left<=0){
                var raw=ItemStack.parseOptional(l.registryAccess(),t.getCompound("Item"));
                var frank=new ItemStack(GoatmanRegistry.BRAT.get());var metadata=raw.get(DataComponents.CUSTOM_DATA);if(metadata!=null)frank.set(DataComponents.CUSTOM_DATA,metadata);
                var entry=new CompoundTag();entry.put("Item",frank.save(l.registryAccess()));entry.putInt("Heat",0);cooked.add(entry);cooking.remove(i);changed=done=true;
            }
        }
        r.put("Cooking0465",cooking);r.put("CookedPan0465",cooked);
        if(changed)GoatmanWoods.cookingPan(l,b,r);
        cues(l,b,r,readers,cooked,done,hot,burned);
        var stove=l.getBlockState(b.offset(GoatmanWoods.STOVE));
        if(stove.is(Blocks.SMOKER)&&stove.getValue(SmokerBlock.LIT)!=!cooking.isEmpty())l.setBlock(b.offset(GoatmanWoods.STOVE),stove.setValue(SmokerBlock.LIT,!cooking.isEmpty()),Block.UPDATE_CLIENTS);
    }
    /** What the griddle sounds and smells like: done, too hot, burnt. Smoke rises from whatever is still on it. */
    private static void cues(ServerLevel l,BlockPos b,CompoundTag r,List<ServerPlayer> readers,ListTag cooked,boolean done,boolean hot,boolean burned){
        Vec3 pan=Vec3.atCenterOf(b.offset(GoatmanWoods.PAN)).add(0,.6,0);long now=l.getGameTime();
        var near=readers.stream().filter(p->p.position().distanceTo(pan)<12).toList();
        if(done&&now-r.getLong("ReadyCue0476")>60){r.putLong("ReadyCue0476",now);l.playSound(null,pan.x,pan.y,pan.z,SoundEvents.CAMPFIRE_CRACKLE,SoundSource.BLOCKS,1.2F,1.4F);
            for(var p:near)p.displayClientMessage(Component.literal("The franks on the griddle look done. Take them off."),true);}
        if(hot&&now-r.getLong("HotCue0476")>60){r.putLong("HotCue0476",now);l.playSound(null,pan.x,pan.y,pan.z,SoundEvents.FIRE_AMBIENT,SoundSource.BLOCKS,1.4F,1.6F);
            for(var p:near)p.displayClientMessage(Component.literal("The franks are spitting. They're getting too hot."),true);}
        if(burned&&now-r.getLong("BurntAt0476")>100){r.putLong("BurntAt0476",now);l.playSound(null,pan.x,pan.y,pan.z,SoundEvents.FIRE_EXTINGUISH,SoundSource.BLOCKS,.8F,.7F);
            for(var p:readers)p.displayClientMessage(Component.literal("There's burnt flesh in the air."),true);
            int said=r.getInt("BurntSaid0476");
            if(said<2){r.putInt("BurntSaid0476",said+1);GoatmanVignette.say(readers,said==0?"A cousin: Something's burning! Get them off the griddle!":"A cousin: Open a window or something. It stinks in here.");}}
        if(now%8!=0)return;
        for(int i=0;i<cooked.size();i++){var t=cooked.getCompound(i);if(!t.contains("Heat"))continue;Vec3 at=GoatmanWoods.panSpot(i).add(b.getX(),b.getY()+.1,b.getZ());
            if(t.getBoolean(BURNT))l.sendParticles(ParticleTypes.LARGE_SMOKE,at.x,at.y,at.z,2,.06,.02,.06,.01);
            else if(t.getInt("Heat")>=HOT_TICKS)l.sendParticles(ParticleTypes.SMOKE,at.x,at.y,at.z,2,.05,.02,.05,.01);}
    }
    /** The cousins turn toward the smell for five seconds after something burns. */
    public static boolean smelled(CompoundTag r,long now){return r.contains("BurntAt0476")&&now-r.getLong("BurntAt0476")<100;}
    static boolean interact(ServerPlayer player,PlayerInteractEvent.RightClickBlock event){
        var data=LabyrinthData.get(player.server);var run=GoatmanVignette.run(data);
        if(!serving(player,run))return false;
        var base=GoatmanVignette.base(player.server);var rel=event.getPos().subtract(base);var held=player.getItemInHand(event.getHand());
        boolean stove=rel.equals(GoatmanWoods.STOVE)||rel.equals(GoatmanWoods.PAN);
        if(rel.equals(GoatmanWoods.COOLER)||stove&&!player.serverLevel().getBlockState(base.offset(GoatmanWoods.COOLER)).is(GoatmanRegistry.COOLER.get())&&!held.is(GoatmanRegistry.FRANKS.get())&&!held.is(GoatmanRegistry.RAW_FRANK.get())&&run.getList("Cooking0465",Tag.TAG_COMPOUND).isEmpty()&&run.getList("CookedPan0465",Tag.TAG_COMPOUND).isEmpty()){
            int issued=run.getInt("PacketsIssued0464"),limit=(GoatmanVignette.expectedCount(run)+3)/4;
            if(issued<limit){
                var packet=new ItemStack(GoatmanRegistry.FRANKS.get());CustomData.update(DataComponents.CUSTOM_DATA,packet,t->t.putUUID(GoatmanVignette.ROUND,run.getUUID("Id")));
                run.putInt("PacketsIssued0464",issued+1);give(player,packet);
            }else player.displayClientMessage(Component.literal("The cooler is empty. We counted enough for supper."),true);
        }else if(stove){
            var cooking=run.getList("Cooking0465",Tag.TAG_COMPOUND);var cooked=run.getList("CookedPan0465",Tag.TAG_COMPOUND);
            if(held.is(GoatmanRegistry.FRANKS.get()))player.setItemInHand(event.getHand(),held.getItem().use(player.level(),player,event.getHand()).getObject());
            else if(held.is(GoatmanRegistry.RAW_FRANK.get())){
                if(!belongs(held,run))player.displayClientMessage(Component.literal("That wasn't in this supper's pack."),true);
                else if(cooking.size()+cooked.size()>=PAN_CAPACITY)player.displayClientMessage(Component.literal("The pan is full. Take the cooked ones out first."),true);
                else {var entry=new CompoundTag();entry.put("Item",held.copyWithCount(1).save(player.registryAccess()));entry.putInt("Left",COOK_TICKS);held.shrink(1);cooking.add(entry);run.put("Cooking0465",cooking);GoatmanWoods.cookingPan(player.serverLevel(),base,run);}
            }else if(!cooked.isEmpty()){
                var item=ItemStack.parseOptional(player.registryAccess(),cooked.getCompound(0).getCompound("Item"));cooked.remove(0);run.put("CookedPan0465",cooked);give(player,item);GoatmanWoods.cookingPan(player.serverLevel(),base,run);
            }else{
                player.displayClientMessage(Component.literal(cooking.isEmpty()?"Grab a pack from the cooler beside the kitchen counter.":"They're still cooking."),true);
            }
        }else{
            // The visible plate under the crosshair wins. A later slab hit can be nearer a different place setting.
            int seat=aimedSeat(player,base,run);if(seat<0)return false;plate(player,held,base,run,seat);
        }
        saveClick(player,data,run);event.setCanceled(true);event.setCancellationResult(InteractionResult.SUCCESS);return true;
    }
}

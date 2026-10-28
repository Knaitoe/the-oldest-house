package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.*;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;

/** The first plate starts the gas trip; the ordinary meal fills the interval before its return. */
public final class GoatmanMeal {
    public static final String TAG="TrailerProvision137";
    public static final int MEAL_TICKS=260;
    /** 0.4.150: with several readers, the cousins give a reader this long to fetch the bottles before they go themselves. */
    public static final int READER_WAIT=200;
    /** The pairs of bottles (ketchup and mustard, the jars) a reader took from the cooler. */
    public static final String READER_PAIRS="ReaderPairs0150";
    private static final String[] NAMES={"ketchup","mustard","mayonnaise","relish"};
    private GoatmanMeal(){}
    private static List<GoatmanChild> cast(ServerLevel l,BlockPos b){return l.getEntitiesOfClass(GoatmanChild.class,IndianLakeRooms.bounds(b,LabyrinthPlace.GOATMAN),c->c.getTags().contains(GoatmanVignette.ACTOR)&&!c.girl());}
    public static List<Integer> carriers(CompoundTag r){var result=new ArrayList<Integer>();for(int i=0;i<GoatmanVignette.count(r)&&result.size()<2;i++)if(i!=r.getInt("Wrong")&&i!=r.getInt("Runner"))result.add(i);return result;}
    private static Item bottle(int i){return switch(i){case 0->GoatmanRegistry.KETCHUP.get();case 1->GoatmanRegistry.MUSTARD.get();case 2->GoatmanRegistry.MAYO.get();default->GoatmanRegistry.RELISH.get();};}
    public static Vec3 spot(int i){return i==4?new Vec3(.5,2.08,-61.1):new Vec3(i%2==0?.15:.85,2.04,-62.5-(i/2)*2);}
    public static boolean supplied(CompoundTag r){return !r.getBoolean("MealStarted137")||(r.getInt("CondimentsPlaced137")&15)==15;}
    public static void firstPlate(ServerLevel l,BlockPos b,CompoundTag r,List<ServerPlayer> readers){
        if(!GoatmanVignette.brisk(r)||r.getBoolean("MealStarted137")||r.contains("SupperAt04123")||(r.getInt("Plated0464")&~r.getInt("EmptyPlates0465"))==0)return;
        r.putBoolean("MealStarted137",true);r.putInt("MealStartedAt0150",r.getInt("Clock"));r.putInt("BunsLeft137",GoatmanVignette.expectedCount(r));
        if(r.getInt("RunnerState")==GoatmanVignette.R_HOME&&!GoatmanVignette.sendForGas(l,b,r,readers)){r.putInt("RunnerState",GoatmanVignette.R_LEAVING);r.putInt("RunnerLeftAt134",r.getInt("Clock"));GoatmanVignette.say(readers,"The older cousin: Generator's out of gas. I'll get the can from the truck. Save mine.");}
        r.putBoolean("CalledIn04123",true);GoatmanWoods.door(l,b,true);
        GoatmanVignette.say(readers,readers.size()>1?"A cousin: Somebody grab the ketchup and mustard from the cooler. And the jars.":"A cousin: I'll get the ketchup and mustard. You get the jars.");
    }
    public static boolean cousin(ServerLevel l,BlockPos b,CompoundTag r,GoatmanChild c,List<ServerPlayer> readers){
        if(r.getInt("Phase")!=GoatmanVignette.GATHERING||!r.getBoolean("MealStarted137"))return false;
        int index=c.getPersistentData().getInt(GoatmanVignette.INDEX),carrier=carriers(r).indexOf(index);if(carrier<0)return false;
        String key="ProvisionCarrier137_"+carrier;int stage=r.getInt(key);if(stage>=3)return false;
        var t=c.getPersistentData();
        if(stage==0){
            // 0.4.150: a reader fetched this pair; with several readers, the cousins give them a while to.
            if((r.getInt(READER_PAIRS)&(1<<carrier))!=0)return false;
            if(readers.size()>1&&r.getInt("Clock")-r.getInt("MealStartedAt0150")<READER_WAIT)return false;
            // Wait on the clear side of the porch before the first carrier leaves the cooler.
            if(carrier==1&&r.getInt("ProvisionCarrier137_0")<2&&(r.getInt(READER_PAIRS)&1)==0)return true;
            stash(c);GoatmanVignette.supplyRoute(c,b,true,carrier);r.putInt(key,stage=1);
        }
        if(stage==1){
            if(!GoatmanVignette.advance(c,b,.13,l,true))return true;
            c.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(bottle(carrier*2)));c.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(bottle(carrier*2+1)));
            l.playSound(null,b.offset(GoatmanWoods.COOLER),SoundEvents.CHEST_OPEN,SoundSource.BLOCKS,.45F,.8F);
            GoatmanVignette.supplyRoute(c,b,false,carrier);r.putInt(key,2);return true;
        }
        if(!GoatmanVignette.advance(c,b,.13,l,true))return true;
        int hold=t.getInt("SetBottles137")+1;t.putInt("SetBottles137",hold);c.pose(false);c.setYRot(-90);c.setYHeadRot(-90);c.yBodyRot=-90;
        if(hold<16)return true;
        for(int j=carrier*2;j<carrier*2+2;j++){
            var held=c.getItemInHand(j%2==0?InteractionHand.MAIN_HAND:InteractionHand.OFF_HAND);
            if(!held.is(bottle(j)))return true;
            if(display(l,b,r,j,held.copyWithCount(1))==null)return true;
            r.putInt("CondimentsPlaced137",r.getInt("CondimentsPlaced137")|(1<<j));
        }
        restore(c);c.getPersistentData().putString("At","condiments"+carrier);c.getPersistentData().putString("Goal","condiments"+carrier);r.putInt(key,3);l.playSound(null,c.blockPosition(),SoundEvents.WOOD_PLACE,SoundSource.BLOCKS,.3F,1.3F);return true;
    }
    private static void stash(GoatmanChild c){var t=c.getPersistentData();if(t.getBoolean("SuppliesStashed137"))return;t.putBoolean("SuppliesStashed137",true);for(var hand:InteractionHand.values())t.put("SupplyOriginal137_"+hand,c.getItemInHand(hand).saveOptional(c.registryAccess()));}
    private static void restore(GoatmanChild c){var t=c.getPersistentData();for(var hand:InteractionHand.values()){c.setItemInHand(hand,ItemStack.parseOptional(c.registryAccess(),t.getCompound("SupplyOriginal137_"+hand)));t.remove("SupplyOriginal137_"+hand);}t.remove("SuppliesStashed137");}
    public static void seated(ServerLevel l,BlockPos b,CompoundTag r,List<ServerPlayer> readers,int clock){
        if(!r.getBoolean("MealStarted137"))return;
        if(!r.contains("MealSeatedAt137")){
            var diners=cast(l,b).stream().filter(c->{int i=c.getPersistentData().getInt(GoatmanVignette.INDEX);return i!=r.getInt("Runner")&&i!=r.getInt("Wrong");}).toList();
            if(!supplied(r)||diners.size()!=GoatmanVignette.count(r)-2-GoatmanVignette.played(r)||diners.stream().anyMatch(c->!c.seated()))return;
            r.putInt("MealSeatedAt137",clock);GoatmanVignette.say(readers,"A cousin: Can you pass the buns down? They're at your end.");
        }
        int age=clock-r.getInt("MealSeatedAt137"),line=r.getInt("MealTalk137");
        String[] lines={"A cousin: You put the whole jar on that?","A cousin: It's relish. That's what it's for.","A cousin: Who brought mayonnaise?","A cousin: Me. Give it here if you don't want it."};
        if(line<lines.length&&age>=45+line*50){GoatmanVignette.say(readers,lines[line]);r.putInt("MealTalk137",line+1);}
    }
    public static void tick(ServerLevel l,BlockPos b,CompoundTag r,List<ServerPlayer> readers){
        if(!r.getBoolean("MealStarted137")||r.getInt("Phase")!=GoatmanVignette.GATHERING)return;
        if(r.getInt("BunsLeft137")>0)display(l,b,r,4,new ItemStack(GoatmanRegistry.BUNS.get()));
        else find(l,b,4).ifPresent(Entity::discard);
    }
    private static Optional<Display.ItemDisplay> find(ServerLevel l,BlockPos b,int i){return l.getEntitiesOfClass(Display.ItemDisplay.class,IndianLakeRooms.bounds(b,LabyrinthPlace.GOATMAN),e->e.getTags().contains(TAG)&&e.getPersistentData().getInt("Supply137")==i).stream().findFirst();}
    private static Display.ItemDisplay display(ServerLevel l,BlockPos b,CompoundTag r,int i,ItemStack item){
        var old=find(l,b,i);if(old.isPresent())return old.get();var d=EntityType.ITEM_DISPLAY.create(l);if(d==null)return null;
        var t=d.saveWithoutId(new CompoundTag());t.put("item",item.save(l.registryAccess()));t.putString("item_display","fixed");var transform=new CompoundTag();
        transform.put("scale",floats(i==4?.8F:.55F,i==4?.8F:.55F,i==4?.8F:.55F));transform.put("translation",floats(0,0,0));transform.put("left_rotation",floats(0,0,0,1));transform.put("right_rotation",floats(0,0,0,1));t.put("transformation",transform);d.load(t);
        d.addTag(TAG);d.getPersistentData().putInt("Supply137",i);d.getPersistentData().putUUID(GoatmanVignette.ROUND,r.getUUID("Id"));d.moveTo(spot(i).add(b.getX(),b.getY(),b.getZ()));return l.addFreshEntity(d)?d:null;
    }
    private static ListTag floats(float... values){var l=new ListTag();for(float value:values)l.add(FloatTag.valueOf(value));return l;}
    public static void reset(ServerLevel l,BlockPos b){l.getEntitiesOfClass(Display.ItemDisplay.class,IndianLakeRooms.bounds(b,LabyrinthPlace.GOATMAN),e->e.getTags().contains(TAG)).forEach(Entity::discard);}
    /** A reader at the cooler takes a pair of bottles nobody has gone for yet, to set out on the table themselves (0.4.150). */
    public static boolean takePair(ServerPlayer p,CompoundTag r){
        if(!r.getBoolean("MealStarted137")||r.getInt("Phase")!=GoatmanVignette.GATHERING)return false;
        for(int pair=0;pair<2;pair++){
            if((r.getInt(READER_PAIRS)&(1<<pair))!=0||r.getInt("ProvisionCarrier137_"+pair)>0||(r.getInt("CondimentsPlaced137")&(3<<pair*2))!=0)continue;
            r.putInt(READER_PAIRS,r.getInt(READER_PAIRS)|1<<pair);
            for(int j=pair*2;j<pair*2+2;j++){var item=new ItemStack(bottle(j));CustomData.update(DataComponents.CUSTOM_DATA,item,t->t.putUUID(GoatmanVignette.ROUND,r.getUUID("Id")));if(!p.getInventory().add(item))p.drop(item,false);}
            p.displayClientMessage(Component.literal(pair==0?"The ketchup and mustard. They go out on the table.":"The jars. They go out on the table."),true);return true;
        }
        return false;
    }
    private static int bottleIndex(ItemStack item){for(int j=0;j<4;j++)if(item.is(bottle(j)))return j;return -1;}
    public static boolean use(ServerPlayer p,InteractionHand hand){
        if(hand!=InteractionHand.MAIN_HAND||!GoatmanVignette.inside(p)||!LiteraryVignettes.participant(p))return false;
        var d=LabyrinthData.get(p.server);var r=GoatmanVignette.run(d);if(!r.getBoolean("MealStarted137")||r.getInt("Phase")!=GoatmanVignette.GATHERING)return false;
        var member=r.getCompound("Cohort").getCompound(p.getUUID().toString());if(!member.getBoolean("Active")||member.getBoolean("Failed"))return false;
        var b=GoatmanVignette.base(p.server);var eye=p.getEyePosition();var ray=eye.add(p.getLookAngle().scale(p.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE)));
        // A bottle a reader fetched goes to its own place on the table, within reach.
        var carried=p.getItemInHand(hand);int bottle=bottleIndex(carried);
        if(bottle>=0&&GoatmanSupper.belongs(carried,r)&&find(p.serverLevel(),b,bottle).isEmpty()&&eye.distanceTo(spot(bottle).add(b.getX(),b.getY(),b.getZ()))<=p.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE)){
            if(display(p.serverLevel(),b,r,bottle,carried.copyWithCount(1))==null)return true;
            carried.shrink(1);r.putInt("CondimentsPlaced137",r.getInt("CondimentsPlaced137")|(1<<bottle));p.serverLevel().playSound(null,p.blockPosition(),SoundEvents.WOOD_PLACE,SoundSource.BLOCKS,.3F,1.3F);
            GoatmanVignette.saveRun(d,r);return true;
        }
        int chosen=-1;double nearest=GoatmanSupper.aimedPlateDistance(p,b,r);
        for(int i=0;i<5;i++){
            var at=spot(i).add(b.getX(),b.getY(),b.getZ());var hit=new AABB(at.x-.22,at.y-.08,at.z-.22,at.x+.22,at.y+.5,at.z+.22).clip(eye,ray);if(hit.isEmpty()||find(p.serverLevel(),b,i).isEmpty())continue;
            double distance=eye.distanceToSqr(hit.get());if(distance>=nearest-1e-6)continue;
            var block=p.serverLevel().clip(new ClipContext(eye,hit.get(),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p));if(block.getType()!=HitResult.Type.MISS&&eye.distanceToSqr(block.getLocation())<distance-1e-6)continue;
            nearest=distance;chosen=i;
        }
        if(chosen>=0){int i=chosen;
            long now=p.serverLevel().getGameTime();if(now-p.getPersistentData().getLong("ProvisionUse137")<3)return true;p.getPersistentData().putLong("ProvisionUse137",now);
            if(i==4){int left=r.getInt("BunsLeft137");if(left>0){var buns=new ItemStack(GoatmanRegistry.BUN.get(),left);CustomData.update(DataComponents.CUSTOM_DATA,buns,t->t.putUUID(GoatmanVignette.ROUND,r.getUUID("Id")));r.putInt("BunsLeft137",0);if(!p.getInventory().add(buns))p.drop(buns,false);find(p.serverLevel(),b,4).ifPresent(Entity::discard);}}
            else {var food=p.getItemInHand(hand);if(food.is(GoatmanRegistry.BRAT.get())){final int sauce=i;CustomData.update(DataComponents.CUSTOM_DATA,food,t->t.putString("GoatmanSauce137",NAMES[sauce]));p.serverLevel().playSound(null,p.blockPosition(),SoundEvents.HONEY_BLOCK_SLIDE,SoundSource.BLOCKS,.25F,1.3F);}else p.displayClientMessage(Component.literal("The "+NAMES[i]+" bottle is still cold from the cooler."),true);}
            GoatmanVignette.saveRun(d,r);return true;
        }return false;
    }
    public static void inBun(ItemStack food){CustomData.update(DataComponents.CUSTOM_DATA,food,t->t.putBoolean("GoatmanBun137",true));food.set(DataComponents.CUSTOM_MODEL_DATA,new CustomModelData(GoatmanSupper.burnt(food)?3:2));}
    public static boolean plateBun(ServerPlayer p,ItemStack held,BlockPos b,CompoundTag r,int seat){
        if(!GoatmanSupper.belongs(held,r))return false;var shown=GoatmanWoods.serving(p.serverLevel(),b,seat);var food=GoatmanWoods.food(p.serverLevel(),shown);if(!food.is(GoatmanRegistry.BRAT.get()))return false;
        var data=food.get(DataComponents.CUSTOM_DATA);if(data!=null&&data.copyTag().getBoolean("GoatmanBun137"))return true;inBun(food);var t=shown.saveWithoutId(new CompoundTag());t.put("item",food.save(p.registryAccess()));shown.load(t);held.shrink(1);r.putInt("BunsServed137",r.getInt("BunsServed137")|1<<seat);return true;
    }
    public static boolean handBun(ServerPlayer p,Entity entity,InteractionHand hand){
        if(hand!=InteractionHand.MAIN_HAND||!(entity instanceof GoatmanChild c)||!GoatmanVignette.inside(p)||!p.getItemInHand(hand).is(GoatmanRegistry.BUN.get()))return false;
        var d=LabyrinthData.get(p.server);var r=GoatmanVignette.run(d);var member=r.getCompound("Cohort").getCompound(p.getUUID().toString());if(r.getInt("Phase")!=GoatmanVignette.GATHERING||!r.getBoolean("MealStarted137")||!member.getBoolean("Active")||member.getBoolean("Failed")||c.girl())return false;var food=p.getItemInHand(hand);int i=c.getPersistentData().getInt(GoatmanVignette.INDEX);
        if(!LiteraryVignettes.participant(p)||!GoatmanSupper.belongs(food,r)||i<0||!c.getPersistentData().hasUUID(GoatmanVignette.ROUND)||!c.getPersistentData().getUUID(GoatmanVignette.ROUND).equals(r.getUUID("Id"))||p.distanceToSqr(c)>9||!p.hasLineOfSight(c))return false;
        if((r.getInt("BunsCousins137")&(1<<i))!=0)return true;r.putInt("BunsCousins137",r.getInt("BunsCousins137")|1<<i);food.shrink(1);if(c.getMainHandItem().is(GoatmanRegistry.BRAT.get()))inBun(c.getMainHandItem());GoatmanVignette.saveRun(d,r);return true;
    }
}

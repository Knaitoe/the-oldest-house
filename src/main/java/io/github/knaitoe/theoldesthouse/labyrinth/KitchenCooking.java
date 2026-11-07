package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.ItemLore;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;

/**
 * 0.4.331: cooking in the familiar kitchen (docs/KITCHEN_0_4_331.md). The owner: "Cooking is such a core part of domestic life and can
 * impart so much of a person's history and culture... a cooking system that offers slight buffs in the house, OR maybe NPCs that accept
 * food and respond to specific dishes in specific ways. This wouldn't gauge the ethnic nature of the food but the broad strokes of it –
 * texture, seasoning, cook?"
 * <p>
 * More than one thing in the stove's pan makes a dish, and a dish is three broad strokes and nothing else:
 * <ul>
 *   <li><b>Texture</b>: crisp if it was fried dry, soft if water went in, creamy if milk did.</li>
 *   <li><b>Seasoning</b>: what most of the seasoning things in it taste of (salt from dried kelp, sweet from sugar, honey, fruit or
 *       beetroot, sharp from berries, earthy from mushrooms, bitter from cocoa, fiery from blaze powder); plain with none; muddled
 *       with three or more kinds.</li>
 *   <li><b>Cook</b>: how long it was left on the ring: underdone, cooked through, well done, burnt.</li>
 * </ul>
 * Eaten inside the House, a dish cooked through or well done gives a slight lift by its strokes. Visitors to the kitchen
 * ({@link KitchenVisitors}) take dishes and answer to the same strokes.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class KitchenCooking {
    private KitchenCooking(){}
    public static final String TAG="KitchenDish";
    public enum Texture{CRISP("crisp","fry"),SOFT("soft","stew"),CREAMY("creamy","soup");
        public final String word,noun;Texture(String word,String noun){this.word=word;this.noun=noun;}}
    public enum Seasoning{PLAIN("plain"),SALTY("salty"),SWEET("sweet"),SHARP("sharp"),EARTHY("earthy"),BITTER("bitter"),FIERY("fiery"),MUDDLED("muddled");
        public final String word;Seasoning(String word){this.word=word;}}
    public enum Cook{UNDERDONE("underdone in the middle"),THROUGH("cooked through"),WELL_DONE("well done"),BURNT("burnt");
        public final String words;Cook(String words){this.words=words;}}
    /** Ties between two seasonings go to the stronger taste. */
    static final List<Seasoning> STRENGTH=List.of(Seasoning.FIERY,Seasoning.SHARP,Seasoning.SALTY,Seasoning.SWEET,Seasoning.EARTHY,Seasoning.BITTER);

    /** One thing that can go in the pan: how much it fills, what it tastes of, whether it is food in itself, and whether it is raw flesh. */
    public record Ingredient(int nutrition,@Nullable Seasoning taste,boolean main,boolean rawFlesh,int liquid){}
    public static final int WATER=1,MILK=2;
    private static final Map<Item,Ingredient> TABLE=new HashMap<>();
    private static void put(Item item,int nutrition,@Nullable Seasoning taste,boolean main,boolean rawFlesh){TABLE.put(item,new Ingredient(nutrition,taste,main,rawFlesh,0));}
    static{
        for(var raw:List.of(Items.BEEF,Items.PORKCHOP))put(raw,8,null,true,true);
        for(var raw:List.of(Items.MUTTON,Items.CHICKEN,Items.SALMON))put(raw,6,null,true,true);
        for(var raw:List.of(Items.RABBIT,Items.COD))put(raw,5,null,true,true);
        for(var done:List.of(Items.COOKED_BEEF,Items.COOKED_PORKCHOP))put(done,8,null,true,false);
        for(var done:List.of(Items.COOKED_MUTTON,Items.COOKED_CHICKEN,Items.COOKED_SALMON))put(done,6,null,true,false);
        for(var done:List.of(Items.COOKED_RABBIT,Items.COOKED_COD,Items.BAKED_POTATO,Items.BREAD))put(done,5,null,true,false);
        put(Items.POTATO,5,null,true,false);put(Items.CARROT,3,null,true,false);put(Items.EGG,3,null,true,false);put(Items.WHEAT,2,null,true,false);
        put(Items.BEETROOT,2,Seasoning.SWEET,true,false);put(Items.APPLE,4,Seasoning.SWEET,true,false);put(Items.PUMPKIN,3,Seasoning.SWEET,true,false);put(Items.MELON_SLICE,2,Seasoning.SWEET,true,false);
        put(Items.SWEET_BERRIES,2,Seasoning.SHARP,true,false);put(Items.GLOW_BERRIES,2,Seasoning.SHARP,true,false);
        put(Items.BROWN_MUSHROOM,3,Seasoning.EARTHY,true,false);put(Items.RED_MUSHROOM,3,Seasoning.EARTHY,true,false);
        put(Items.KELP,1,Seasoning.SALTY,true,false);
        // Seasonings, which are not a meal on their own.
        put(Items.DRIED_KELP,1,Seasoning.SALTY,false,false);put(Items.SUGAR,0,Seasoning.SWEET,false,false);put(Items.HONEY_BOTTLE,2,Seasoning.SWEET,false,false);
        put(Items.COCOA_BEANS,0,Seasoning.BITTER,false,false);put(Items.BLAZE_POWDER,0,Seasoning.FIERY,false,false);
        TABLE.put(Items.MILK_BUCKET,new Ingredient(0,null,false,false,MILK));
    }
    /** What a stack is in the pan, if it can go in at all: a bottle of water counts, other potions do not. */
    public static @Nullable Ingredient ingredient(ItemStack s){
        if(s.isEmpty())return null;
        if(s.is(Items.POTION)){var c=s.get(DataComponents.POTION_CONTENTS);return c!=null&&c.is(Potions.WATER)?new Ingredient(0,null,false,false,WATER):null;}
        return TABLE.get(s.getItem());
    }
    /** What is left in the hand once a thing has gone in the pan: the bottle or the bucket. */
    public static ItemStack leftover(ItemStack s){return s.is(Items.POTION)||s.is(Items.HONEY_BOTTLE)?new ItemStack(Items.GLASS_BOTTLE):s.is(Items.MILK_BUCKET)?new ItemStack(Items.BUCKET):ItemStack.EMPTY;}

    /** A dish, by its strokes. Nutrition is what it fills; by is who took it off the stove. */
    public record Dish(Texture texture,Seasoning seasoning,@Nullable Seasoning second,Cook cook,int nutrition,boolean rawFlesh,String by){
        public String name(){return cap(seasoning==Seasoning.PLAIN?"plain "+texture.noun:seasoning.word+" "+texture.noun);}
        /** "something crisp and salty, cooked through" */
        public String strokes(){return "something "+texture.word+(seasoning==Seasoning.PLAIN?"":" and "+(seasoning==Seasoning.MUDDLED?"muddled":seasoning.word))+", "+cook.words;}
        public CompoundTag save(){var t=new CompoundTag();t.putString("Texture",texture.name());t.putString("Seasoning",seasoning.name());if(second!=null)t.putString("Second",second.name());t.putString("Cook",cook.name());t.putInt("Nutrition",nutrition);t.putBoolean("RawFlesh",rawFlesh);t.putString("By",by);return t;}
        static Dish load(CompoundTag t){return new Dish(valueOf(Texture.class,t.getString("Texture"),Texture.CRISP),valueOf(Seasoning.class,t.getString("Seasoning"),Seasoning.PLAIN),t.contains("Second")?valueOf(Seasoning.class,t.getString("Second"),null):null,valueOf(Cook.class,t.getString("Cook"),Cook.THROUGH),t.getInt("Nutrition"),t.getBoolean("RawFlesh"),t.getString("By"));}
    }
    static String cap(String s){return s.isEmpty()?s:Character.toUpperCase(s.charAt(0))+s.substring(1);}
    private static <E extends Enum<E>> E valueOf(Class<E> type,String name,E fallback){try{return Enum.valueOf(type,name);}catch(IllegalArgumentException e){return fallback;}}

    /** How long a pan needs before it is cooked through: longer with water or milk in it. */
    public static int ready(Texture texture){return texture==Texture.CRISP?200:300;}
    public static Texture texture(List<ItemStack> pan){
        boolean water=false;for(var s:pan){var i=ingredient(s);if(i==null)continue;if(i.liquid()==MILK)return Texture.CREAMY;if(i.liquid()==WATER)water=true;}
        return water?Texture.SOFT:Texture.CRISP;
    }
    public static Cook cook(Texture texture,int ticks){double r=ticks/(double)ready(texture);return r<.6?Cook.UNDERDONE:r<1.5?Cook.THROUGH:r<2.3?Cook.WELL_DONE:Cook.BURNT;}
    /** When a pan left on the ring has burnt, and the ring clicks off by itself. */
    public static int burnsAt(Texture texture){return (int)Math.ceil(ready(texture)*2.3);}
    public static boolean hasMain(List<ItemStack> pan){for(var s:pan){var i=ingredient(s);if(i!=null&&i.main())return true;}return false;}

    /** What a pan makes after so many ticks on the ring. */
    public static Dish dish(List<ItemStack> pan,int ticks,String by){
        var texture=texture(pan);var cook=cook(texture,ticks);
        Map<Seasoning,Integer> tastes=new EnumMap<>(Seasoning.class);int fill=0,seasonings=0;boolean flesh=false;
        for(var s:pan){var i=ingredient(s);if(i==null)continue;
            if(i.taste()!=null)tastes.merge(i.taste(),1,Integer::sum);
            if(i.main())fill+=i.nutrition();else if(i.taste()!=null&&seasonings<2){fill++;seasonings++;}
            flesh|=i.rawFlesh();}
        Seasoning top=Seasoning.PLAIN,second=null;
        if(tastes.size()>=3)top=Seasoning.MUDDLED;
        else if(!tastes.isEmpty()){
            var order=new ArrayList<>(tastes.keySet());order.sort((a,b)->tastes.get(a).equals(tastes.get(b))?STRENGTH.indexOf(a)-STRENGTH.indexOf(b):tastes.get(b)-tastes.get(a));
            top=order.get(0);if(order.size()>1)second=order.get(1);
        }
        double factor=switch(cook){case UNDERDONE->.6;case THROUGH->1;case WELL_DONE->.85;case BURNT->.4;};
        int nutrition=Math.max(2,Math.min(20,(int)Math.round(fill*factor)));
        return new Dish(texture,top,second,cook,nutrition,flesh&&cook==Cook.UNDERDONE,by);
    }
    /** The dish as something to carry, eat or give: its picture, its name, its strokes in grey under it. */
    public static ItemStack stack(Dish d){
        var s=new ItemStack(KitchenRegistry.DISH.get());
        s.set(DataComponents.ITEM_NAME,Component.literal(d.name()));
        var lore=new ArrayList<Component>();lore.add(Component.literal(cap(d.cook().words)+".").withStyle(ChatFormatting.GRAY));
        if(!d.by().isEmpty())lore.add(Component.literal("Made in the kitchen by "+d.by()+".").withStyle(ChatFormatting.DARK_GRAY,ChatFormatting.ITALIC));
        s.set(DataComponents.LORE,new ItemLore(lore));
        s.set(DataComponents.CUSTOM_MODEL_DATA,new CustomModelData(model(d.texture(),d.cook())));
        float saturation=switch(d.texture()){case CRISP->.6F;case SOFT->.7F;case CREAMY->.8F;}+(d.seasoning()==Seasoning.SALTY?.2F:0);
        var food=new FoodProperties.Builder().nutrition(d.nutrition()).saturationModifier(saturation);
        if(d.texture()!=Texture.CRISP)food.usingConvertsTo(Items.BOWL);
        s.set(DataComponents.FOOD,food.build());
        var tag=new CompoundTag();tag.put(TAG,d.save());s.set(DataComponents.CUSTOM_DATA,CustomData.of(tag));
        return s;
    }
    /** The picture: a plate, a bowl or a pale bowl, and the plate or a bowl again when it burnt. */
    public static int model(Texture texture,Cook cook){
        if(cook==Cook.BURNT)return texture==Texture.CRISP?3:4;
        return switch(texture){case CRISP->0;case SOFT->1;case CREAMY->2;};
    }
    public static @Nullable Dish read(ItemStack s){
        if(!s.is(KitchenRegistry.DISH.get()))return null;var c=s.get(DataComponents.CUSTOM_DATA);
        return c==null||!c.copyTag().contains(TAG)?null:Dish.load(c.copyTag().getCompound(TAG));
    }

    /**
     * What anything edible comes to in broad strokes, for someone it is offered to. A dish is what it is; ordinary food is read the
     * plain way (roast meat and bread crisp and cooked through, stews soft, raw meat underdone, fruit and sweets sweet). Nothing for
     * what nobody should be handed to eat.
     */
    public static @Nullable Dish strokes(ItemStack s){
        var dish=read(s);if(dish!=null)return dish;
        if(s.isEmpty()||s.get(DataComponents.FOOD)==null)return null;
        if(s.is(Items.ROTTEN_FLESH)||s.is(Items.SPIDER_EYE)||s.is(Items.POISONOUS_POTATO)||s.is(Items.PUFFERFISH)||s.is(Items.CHORUS_FRUIT)||s.is(Items.SUSPICIOUS_STEW))return null;
        var food=s.get(DataComponents.FOOD);int n=food.nutrition();
        if(s.is(Items.MUSHROOM_STEW)||s.is(Items.RABBIT_STEW))return plain(Texture.SOFT,Seasoning.EARTHY,Cook.THROUGH,n,false);
        if(s.is(Items.BEETROOT_SOUP))return plain(Texture.SOFT,Seasoning.SWEET,Cook.THROUGH,n,false);
        if(s.is(Items.PUMPKIN_PIE))return plain(Texture.SOFT,Seasoning.SWEET,Cook.THROUGH,n,false);
        var i=ingredient(s);
        if(i!=null&&i.rawFlesh())return plain(Texture.CRISP,Seasoning.PLAIN,Cook.UNDERDONE,n,true);
        if(i!=null&&i.taste()!=null)return plain(Texture.CRISP,i.taste(),Cook.THROUGH,n,false);
        if(s.is(Items.COOKIE)||s.is(Items.GOLDEN_CARROT)||s.is(Items.GOLDEN_APPLE)||s.is(Items.ENCHANTED_GOLDEN_APPLE))return plain(Texture.CRISP,Seasoning.SWEET,Cook.THROUGH,n,false);
        return plain(Texture.CRISP,Seasoning.PLAIN,s.is(Items.POTATO)?Cook.UNDERDONE:Cook.THROUGH,n,false);
    }
    private static Dish plain(Texture t,Seasoning s,Cook c,int n,boolean flesh){return new Dish(t,s,null,c,n,flesh,"");}

    // ------------------------------------------------------------------ eating one
    /** The slight lift a dish gives inside the House, by its strokes. Only cooked through or well done; outside the House it is food. */
    @SubscribeEvent public static void eaten(LivingEntityUseItemEvent.Finish e){
        if(!(e.getEntity() instanceof ServerPlayer p))return;var dish=read(e.getItem());if(dish==null)return;
        lift(p,dish,HouseDimensions.isHouseDimension(p.level().dimension()));
    }
    /** The lift, and the line that goes with it. Returns the line, for tests. */
    public static String lift(ServerPlayer p,Dish d,boolean inHouse){
        if(d.rawFlesh())p.addEffect(new MobEffectInstance(MobEffects.HUNGER,400,0));
        String line;
        if(d.cook()==Cook.UNDERDONE)line="Not quite done in the middle.";
        else if(d.cook()==Cook.BURNT)line="It tastes of the pan.";
        else{
            double keep=d.cook()==Cook.WELL_DONE?.75:1;var parts=new ArrayList<String>();
            if(inHouse){
                switch(d.texture()){
                    case SOFT->{p.addEffect(new MobEffectInstance(MobEffects.REGENERATION,(int)(120*keep),0));parts.add("Warm all the way down.");}
                    case CREAMY->{p.addEffect(new MobEffectInstance(MobEffects.ABSORPTION,(int)(600*keep),0));parts.add("Filling.");}
                    case CRISP->{}
                }
                switch(d.seasoning()){
                    case SALTY->{p.getFoodData().eat(0,3);parts.add("It stays with you.");}
                    case SWEET->{p.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED,(int)(1200*keep),0));parts.add("Your hands feel quick.");}
                    case SHARP->{p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,(int)(400*keep),0));parts.add("Awake, all at once.");}
                    case EARTHY->{p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,(int)(400*keep),0));parts.add("Heavy, in a good way.");}
                    case BITTER->{p.removeEffect(MobEffects.POISON);p.removeEffect(MobEffects.HUNGER);parts.add("It clears your head.");}
                    case FIERY->{p.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE,(int)(1200*keep),0));parts.add("Warm right through.");}
                    case MUDDLED->parts.add("Too much going on in it.");
                    case PLAIN->parts.add("Plain, and enough.");
                }
            }else parts.add(d.seasoning()==Seasoning.PLAIN?"Plain, and enough.":"It tastes like the kitchen.");
            line=String.join(" ",parts);
        }
        p.displayClientMessage(Component.literal(line).withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),true);
        return line;
    }
    static String id(Dish d){return (d.texture().name()+"/"+d.seasoning().name()+"/"+d.cook().name()).toLowerCase(Locale.ROOT);}
}

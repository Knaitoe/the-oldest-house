package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.*;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.*;

public final class GoatmanRegistry {
    private static final DeferredRegister<EntityType<?>> TYPES=DeferredRegister.create(Registries.ENTITY_TYPE,TheOldestHouse.MOD_ID);
    private static final DeferredRegister.Items ITEMS=DeferredRegister.createItems(TheOldestHouse.MOD_ID);
    private static final DeferredRegister<ParticleType<?>> PARTICLES=DeferredRegister.create(Registries.PARTICLE_TYPE,TheOldestHouse.MOD_ID);
    public static final DeferredItem<Item> PLATE=ITEMS.registerSimpleItem("trailer_plate",new Item.Properties().stacksTo(1));
    public static final DeferredItem<FranksPackageItem> FRANKS=ITEMS.registerItem("franks_package",FranksPackageItem::new,new Item.Properties().stacksTo(1));
    /** 0.4.53: one each, from the pan on the stove. */
    public static final DeferredItem<Item> BRAT=ITEMS.registerSimpleItem("goatman_brat",new Item.Properties().food(new FoodProperties.Builder().nutrition(6).saturationModifier(.6F).build()));
    /** 0.4.53: what the reader who counted right takes home. */
    public static final DeferredItem<TallyCounterItem> COUNTER=ITEMS.registerItem("tally_counter",TallyCounterItem::new,new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    private static final DeferredRegister<SoundEvent> SOUNDS=DeferredRegister.create(Registries.SOUND_EVENT,TheOldestHouse.MOD_ID);
    public static final DeferredHolder<EntityType<?>,EntityType<GoatmanChild>> CHILD=TYPES.register("trailer_child",()->EntityType.Builder.<GoatmanChild>of(GoatmanChild::new,MobCategory.MISC).sized(.6F,1.8F).eyeHeight(1.62F).clientTrackingRange(12).updateInterval(1).build("trailer_child"));
    /** 0.4.53: the thing itself, always private to whoever sees it. */
    public static final DeferredHolder<EntityType<?>,EntityType<GoatmanFigure>> FIGURE=TYPES.register("goatman",()->EntityType.Builder.<GoatmanFigure>of(GoatmanFigure::new,MobCategory.MISC).sized(.6F,2.2F).eyeHeight(1.95F).clientTrackingRange(10).updateInterval(1).build("goatman"));
    public static final DeferredHolder<SoundEvent,SoundEvent> HAMMER=sound("goatman.hammer"),LAUGH=sound("goatman.laugh"),WOODS=sound("goatman.woods"),
        CLICK=sound("goatman.click"),CLAW=sound("goatman.claw"),CRICKETS=sound("goatman.crickets"),KEEN=sound("goatman.keen");
    /** The smell, where it is strongest. */
    public static final DeferredHolder<ParticleType<?>,SimpleParticleType> COPPER=PARTICLES.register("goatman_copper",()->new SimpleParticleType(false));
    private static DeferredHolder<SoundEvent,SoundEvent> sound(String id){return SOUNDS.register(id,()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,id)));}
    private GoatmanRegistry(){}
    public static void register(IEventBus bus){TYPES.register(bus);SOUNDS.register(bus);ITEMS.register(bus);PARTICLES.register(bus);bus.addListener(GoatmanRegistry::attributes);}
    private static void attributes(EntityAttributeCreationEvent e){e.put(CHILD.get(),GoatmanChild.attributes().build());e.put(FIGURE.get(),GoatmanFigure.attributes().build());}
}

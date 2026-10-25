package io.github.knaitoe.theoldesthouse.labyrinth;
import com.mojang.serialization.MapCodec;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.core.registries.*;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;
/** Physical fittings and finite artifacts for the six uncanny rooms. */
public final class UncannyRegistry {
    private static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(TheOldestHouse.MOD_ID);
    private static final DeferredRegister.Items ITEMS=DeferredRegister.createItems(TheOldestHouse.MOD_ID);
    private static final DeferredRegister<MobEffect> EFFECTS=DeferredRegister.create(Registries.MOB_EFFECT,TheOldestHouse.MOD_ID);
    private static final DeferredRegister<MapCodec<? extends Block>> CODECS=DeferredRegister.create(BuiltInRegistries.BLOCK_TYPE,TheOldestHouse.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BES=DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE,TheOldestHouse.MOD_ID);
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<UncannyPropBlock>> PROP_TYPE=CODECS.register("uncanny_fitting",()->UncannyPropBlock.CODEC);
    public static final DeferredBlock<UncannyPropBlock> PROP=BLOCKS.registerBlock("uncanny_fitting",UncannyPropBlock::new,BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).strength(.8F).noOcclusion().noLootTable().lightLevel(UncannyPropBlock::light));
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<UncannyPropBlock.Fitting>> FITTING=BES.register("uncanny_fitting",()->BlockEntityType.Builder.of(UncannyPropBlock.Fitting::new,PROP.get()).build(null));
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<UncannyFurnitureBlock>> FURNITURE_TYPE=CODECS.register("uncanny_furniture",()->UncannyFurnitureBlock.CODEC);
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<UncannyFleshBlock>> FLESH_TYPE=CODECS.register("cellar_flesh",()->UncannyFleshBlock.CODEC);
    public static final DeferredBlock<UncannyFleshBlock> FLESH=BLOCKS.registerBlock("cellar_flesh",UncannyFleshBlock::new,BlockBehaviour.Properties.ofFullCopy(Blocks.MUD).strength(.35F).sound(SoundType.SLIME_BLOCK).noLootTable());
    public static final DeferredHolder<MobEffect,EngulfedEffect> ENGULFED=EFFECTS.register("engulfed",EngulfedEffect::new);
    public static final DeferredBlock<UncannyFurnitureBlock> FURNITURE=BLOCKS.registerBlock("uncanny_furniture",UncannyFurnitureBlock::new,BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).strength(.8F).noOcclusion().noLootTable().lightLevel(UncannyFurnitureBlock::light));
    public static final DeferredBlock<DoorBlock> DINER_DOOR=BLOCKS.register("uncanny_diner_door",()->new DoorBlock(net.minecraft.world.level.block.state.properties.BlockSetType.COPPER,BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_DOOR).noOcclusion().noLootTable()));
    public static final DeferredBlock<CarpetBlock> PARLOR_RUG=BLOCKS.registerBlock("uncanny_parlor_rug",CarpetBlock::new,BlockBehaviour.Properties.ofFullCopy(Blocks.GREEN_CARPET).noLootTable());
    private static final java.util.Map<String,DeferredBlock<Block>> MATERIALS=new java.util.LinkedHashMap<>();
    static {
        for(String name:new String[]{"diner_wall","diner_dado","diner_tile","roof","asphalt","pavement","parlor_paper","dark_wood","bedroom_paper","workshop_wall","workshop_floor","lobby_wall","courtyard_wall","cellar_brick","cellar_floor","cellar_plaster"})
            MATERIALS.put(name,BLOCKS.registerSimpleBlock("uncanny_"+name,BlockBehaviour.Properties.ofFullCopy(Blocks.WHITE_TERRACOTTA).strength(1.2F).noLootTable()));
    }
    public static Block material(String name){var block=MATERIALS.get(name);if(block==null)throw new IllegalArgumentException(name);return block.get();}
    public static final DeferredItem<UncannyArtifactItem> BONE_RING=ITEMS.registerItem("bone_ring",p->new UncannyArtifactItem(p,false),new Item.Properties().stacksTo(1).rarity(Rarity.RARE));
    public static final DeferredItem<UncannyArtifactItem> SMOKY_MIRROR=ITEMS.registerItem("smoky_mirror",p->new UncannyArtifactItem(p,true),new Item.Properties().stacksTo(1).rarity(Rarity.RARE));
    public static final DeferredItem<Item> BAT=ITEMS.registerSimpleItem("hallway_bat",new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> CLEAVER=ITEMS.registerSimpleItem("cellar_cleaver",new Item.Properties().stacksTo(1).durability(96));
    public static final DeferredItem<Item> CELLAR_KEY=ITEMS.registerSimpleItem("cellar_key",new Item.Properties().stacksTo(1));
    public static final DeferredItem<UncannyMealItem> MEAL=ITEMS.registerItem("second_waitress_meal",UncannyMealItem::new,new Item.Properties().stacksTo(1).food(new FoodProperties.Builder().nutrition(1).saturationModifier(0).alwaysEdible().build()));
    private UncannyRegistry(){}
    public static void register(IEventBus bus){CODECS.register(bus);BLOCKS.register(bus);BES.register(bus);ITEMS.register(bus);EFFECTS.register(bus);}
}

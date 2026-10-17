package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Everything Movie Night (0.4.102) registers: the lake's props, the screen, the shark and the film's sounds. */
public final class MovieNightRegistry {
    private static final DeferredRegister<EntityType<?>> TYPES=DeferredRegister.create(Registries.ENTITY_TYPE,TheOldestHouse.MOD_ID);
    private static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(TheOldestHouse.MOD_ID);
    private static final DeferredRegister<MapCodec<? extends Block>> CODECS=DeferredRegister.create(BuiltInRegistries.BLOCK_TYPE,TheOldestHouse.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BES=DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE,TheOldestHouse.MOD_ID);
    private static final DeferredRegister<SoundEvent> SOUNDS=DeferredRegister.create(Registries.SOUND_EVENT,TheOldestHouse.MOD_ID);

    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<MoviePropBlock>> PROP_TYPE=CODECS.register("movie_prop",()->MoviePropBlock.CODEC);
    /** Speakers, the projector and the barge's mortars. Scenery: no item, no drops, not to be broken. */
    public static final DeferredBlock<MoviePropBlock> PROP=BLOCKS.registerBlock("movie_prop",MoviePropBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).strength(-1,3600000).noOcclusion().noLootTable());
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<MovieScreenBlock>> SCREEN_TYPE=CODECS.register("movie_screen",()->MovieScreenBlock.CODEC);
    /** One block in the screen's black wall carries the film, its beam and the shadows thrown across it. */
    public static final DeferredBlock<MovieScreenBlock> SCREEN=BLOCKS.registerBlock("movie_screen",MovieScreenBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.BLACK_CONCRETE).strength(-1,3600000).noLootTable());
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<MovieScreenBlock.Screen>> SCREEN_ENTITY=BES.register("movie_screen",
            ()->BlockEntityType.Builder.of(MovieScreenBlock.Screen::new,SCREEN.get()).build(null));
    /** The shark only one reader sees. It has no body to touch and never saves. */
    public static final DeferredHolder<EntityType<?>,EntityType<LakeSharkEntity>> SHARK=TYPES.register("lake_shark",
            ()->EntityType.Builder.<LakeSharkEntity>of(LakeSharkEntity::new,MobCategory.MISC).sized(2.0F,1.2F).noSave().clientTrackingRange(8).updateInterval(1).build("lake_shark"));

    /** The film's score and the lake around it, each subtitled. */
    public static final DeferredHolder<SoundEvent,SoundEvent> CALM=sound("movie.calm"),DREAD=sound("movie.dread"),STING=sound("movie.sting"),FINALE=sound("movie.finale"),
            BELL=sound("movie.bell"),PROJECTOR=sound("movie.projector"),REEL=sound("movie.reel"),CUT=sound("movie.cut"),BUMP=sound("movie.bump");

    private MovieNightRegistry(){}
    private static DeferredHolder<SoundEvent,SoundEvent> sound(String id){return SOUNDS.register(id,()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,id)));}
    public static void register(IEventBus bus){CODECS.register(bus);BLOCKS.register(bus);BES.register(bus);TYPES.register(bus);SOUNDS.register(bus);}
}

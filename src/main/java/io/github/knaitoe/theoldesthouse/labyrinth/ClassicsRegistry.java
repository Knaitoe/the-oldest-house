package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.core.registries.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.*;

/** Authored surfaces, three-dimensional keepsakes and the native séance cast. */
public final class ClassicsRegistry {
    private static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(TheOldestHouse.MOD_ID);
    private static final DeferredRegister.Items ITEMS=DeferredRegister.createItems(TheOldestHouse.MOD_ID);
    private static final DeferredRegister<MapCodec<? extends Block>> CODECS=DeferredRegister.create(BuiltInRegistries.BLOCK_TYPE,TheOldestHouse.MOD_ID);
    private static final DeferredRegister<EntityType<?>> TYPES=DeferredRegister.create(Registries.ENTITY_TYPE,TheOldestHouse.MOD_ID);
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<WallpaperPanelBlock>> PANEL_TYPE=CODECS.register("nursery_wallpaper",()->WallpaperPanelBlock.CODEC);
    public static final DeferredBlock<WallpaperPanelBlock> WALLPAPER=BLOCKS.registerBlock("nursery_wallpaper",WallpaperPanelBlock::new,BlockBehaviour.Properties.ofFullCopy(Blocks.YELLOW_TERRACOTTA).noLootTable());
    public static final DeferredBlock<Block> WAINSCOT=BLOCKS.registerSimpleBlock("seance_wainscot",BlockBehaviour.Properties.ofFullCopy(Blocks.DARK_OAK_PLANKS).noLootTable());
    public static final DeferredBlock<Block> SEANCE_WALL=BLOCKS.registerSimpleBlock("seance_wallpaper",BlockBehaviour.Properties.ofFullCopy(Blocks.BROWN_TERRACOTTA).noLootTable());
    public static final DeferredBlock<Block> TABLE=BLOCKS.registerBlock("seance_table",p->new Block(p){
        @Override protected net.minecraft.world.phys.shapes.VoxelShape getShape(net.minecraft.world.level.block.state.BlockState s,net.minecraft.world.level.BlockGetter l,net.minecraft.core.BlockPos b,net.minecraft.world.phys.shapes.CollisionContext c){return Block.box(0,0,0,16,16,16);}
    },BlockBehaviour.Properties.ofFullCopy(Blocks.DARK_OAK_PLANKS).noOcclusion().noLootTable());
    public static final DeferredItem<Item> PLANCHETTE=ITEMS.registerSimpleItem("seance_planchette",new Item.Properties().stacksTo(1));
    public static final DeferredItem<ClassicsFolioItem> FOLIO=ITEMS.register("wallpaper_folio",()->new ClassicsFolioItem(new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<EntityType<?>,EntityType<SeanceActor>> ACTOR=TYPES.register("seance_actor",()->EntityType.Builder.<SeanceActor>of(SeanceActor::new,MobCategory.MISC).sized(.6F,1.8F).clientTrackingRange(16).updateInterval(2).build("seance_actor"));
    private ClassicsRegistry(){}
    public static void register(IEventBus bus){CODECS.register(bus);BLOCKS.register(bus);ITEMS.register(bus);TYPES.register(bus);bus.addListener(ClassicsRegistry::attributes);}
    private static void attributes(EntityAttributeCreationEvent e){e.put(ACTOR.get(),SeanceActor.attributes().build());}
}

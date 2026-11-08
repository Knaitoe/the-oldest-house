package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.*;

/**
 * 0.4.351, The Last Crossing (docs/LAST_CROSSING_0_4_351.md): the flood station's fittings, the things the crew carries about the station
 * (the handprint plate, the mooring line, the station lamp, the intake worker's wrench, from Day Two the cable tools, and from Day Three the
 * launch pin and the locking bar), and the station's
 * sounds. Drawn by
 * tools/draw_last_crossing_art.py; heard from tools/generate_last_crossing_audio.py. From 0.4.352, Night One: the things that come up out
 * of the water (Undertow) and the night's sounds.
 */
public final class CrossingRegistry {
    private static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(TheOldestHouse.MOD_ID);
    private static final DeferredRegister.Items ITEMS=DeferredRegister.createItems(TheOldestHouse.MOD_ID);
    private static final DeferredRegister<MapCodec<? extends Block>> CODECS=DeferredRegister.create(Registries.BLOCK_TYPE,TheOldestHouse.MOD_ID);
    private static final DeferredRegister<SoundEvent> SOUNDS=DeferredRegister.create(Registries.SOUND_EVENT,TheOldestHouse.MOD_ID);
    private static final DeferredRegister<EntityType<?>> TYPES=DeferredRegister.create(Registries.ENTITY_TYPE,TheOldestHouse.MOD_ID);
    public static final DeferredHolder<EntityType<?>,EntityType<Undertow>> UNDERTOW_TYPE=TYPES.register("crossing_undertow",
        ()->EntityType.Builder.<Undertow>of(Undertow::new,MobCategory.MONSTER).sized(.6F,1.95F).eyeHeight(1.74F).clientTrackingRange(8).build("crossing_undertow"));
    private static BlockBehaviour.Properties fixed(Block like){return BlockBehaviour.Properties.ofFullCopy(like).strength(-1,3600000).noLootTable();}
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<CrossingBlock>> FITTING_TYPE=CODECS.register("crossing_fitting",()->CrossingBlock.CODEC);
    public static final DeferredBlock<CrossingBlock> FITTING=BLOCKS.registerBlock("crossing_fitting",CrossingBlock::new,fixed(Blocks.IRON_BLOCK).noOcclusion().lightLevel(CrossingBlock::light));
    /** The handprint plate, lifted out of its bracket: it takes both hands, and is set down on a floor or in a bracket. */
    public static final DeferredItem<Plate> PLATE=ITEMS.registerItem("crossing_plate",Plate::new,new Item.Properties().stacksTo(1));
    /** The ferry's loose mooring line, the station lamp, and the wrench that hangs by the intake. */
    public static final DeferredItem<Item> LINE=ITEMS.registerSimpleItem("crossing_line",new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> LAMP=ITEMS.registerSimpleItem("crossing_lamp",new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> WRENCH=ITEMS.registerSimpleItem("crossing_wrench",new Item.Properties().stacksTo(1));
    /** Day Two: the cable tools Vera lays on the desk for whoever takes the far end of the winch: a scraper, a marlinspike, rags. */
    public static final DeferredItem<Item> TOOLS=ITEMS.registerSimpleItem("crossing_tools",new Item.Properties().stacksTo(1));
    /** 0.4.355, Day Three: the launch pin, carried down from the porch rack to its sleeve on the upper ramp; the locking bar, carried from the
     * intake down to the feed gate in the lower gallery. */
    public static final DeferredItem<Item> PIN=ITEMS.registerSimpleItem("crossing_pin",new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> LOCKBAR=ITEMS.registerSimpleItem("crossing_lockbar",new Item.Properties().stacksTo(1));
    /** 0.4.357: a piece of Cordelia's brass nameplate, broken along its bolt holes on a crew's victory, or the spare that broke away at one
     * bolt. Tapped, it knocks softly through matching pieces nearby; the spare can be handed to a reader who turned. */
    public static final DeferredItem<Fragment> FRAGMENT=ITEMS.registerItem("crossing_fragment",Fragment::new,new Item.Properties().stacksTo(1));
    private static DeferredHolder<SoundEvent,SoundEvent> sound(String id){return SOUNDS.register(id,()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,id)));}
    /** A knock in the speaking pipe; the pump taking its load; something tapping back under the grate; the bleed screw letting go. */
    public static final DeferredHolder<SoundEvent,SoundEvent> KNOCK=sound("crossing.knock"),PUMP=sound("crossing.pump"),TAP=sound("crossing.tap"),BLEED=sound("crossing.bleed");
    /** Night One: the surge through the intake; the second breath an infected reader hears under their own; the things from the water; the
     * ferry straining at the first rise; the line snapping. */
    public static final DeferredHolder<SoundEvent,SoundEvent> SURGE=sound("crossing.surge"),BREATH=sound("crossing.breath"),UNDERTOW=sound("crossing.undertow"),RISE=sound("crossing.rise"),SNAP=sound("crossing.snap");
    /** Day Two: the winch drum ratcheting a notch; something answering through the pipes; water running behind a grate in a flooded branch. */
    public static final DeferredHolder<SoundEvent,SoundEvent> RATCHET=sound("crossing.ratchet"),SCRAPE=sound("crossing.scrape"),BRANCH=sound("crossing.branch");
    /** 0.4.354, Night Two: a reader turning at sunset; a claw raking cloth; a throat closing on a breath. */
    public static final DeferredHolder<SoundEvent,SoundEvent> TURN=sound("crossing.turn"),CLAW=sound("crossing.claw"),CHOKE=sound("crossing.choke");
    private CrossingRegistry(){}
    public static void register(IEventBus bus){CODECS.register(bus);BLOCKS.register(bus);ITEMS.register(bus);SOUNDS.register(bus);TYPES.register(bus);bus.addListener(CrossingRegistry::attributes);}
    private static void attributes(EntityAttributeCreationEvent e){e.put(UNDERTOW_TYPE.get(),Undertow.attributes().build());}

    /** A piece of the nameplate: tapped in the air, it knocks; used on someone, the spare is handed over. */
    public static final class Fragment extends Item {
        public Fragment(Properties p){super(p);}
        @Override public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand){
            var stack=player.getItemInHand(hand);
            if(player instanceof ServerPlayer p)LastCrossingNameplate.tap(p,stack);
            return InteractionResultHolder.sidedSuccess(stack,level.isClientSide);
        }
        @Override public InteractionResult interactLivingEntity(ItemStack stack,Player player,LivingEntity target,InteractionHand hand){
            if(player.level().isClientSide)return InteractionResult.SUCCESS;
            if(!(player instanceof ServerPlayer p)||!(target instanceof ServerPlayer to))return InteractionResult.PASS;
            return LastCrossingNameplate.hand(p,to,hand)?InteractionResult.CONSUME:InteractionResult.PASS;
        }
        @Override public Component getName(ItemStack s){return Component.literal(LastCrossingNameplate.spare(s)?"The spare piece of a nameplate":"A piece of a nameplate");}
        @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> lines,TooltipFlag f){
            lines.add(Component.literal(LastCrossingNameplate.shown(s)).withStyle(ChatFormatting.GRAY));
            lines.add(Component.literal(LastCrossingNameplate.spare(s)?"Cast brass, a single bolt hole. No letters on it.":"Cast brass, broken along its bolt holes.").withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC));
        }
    }
    /** The plate set down where its carrier points: on a floor, the station decides whether it can stand there. */
    public static final class Plate extends Item {
        public Plate(Properties p){super(p);}
        @Override public InteractionResult useOn(UseOnContext c){
            if(c.getLevel().isClientSide)return InteractionResult.SUCCESS;
            if(!(c.getPlayer() instanceof ServerPlayer p))return InteractionResult.PASS;
            return LastCrossing.setDownOn(p,c.getClickedPos(),c.getClickedFace())?InteractionResult.CONSUME:InteractionResult.FAIL;
        }
    }
}

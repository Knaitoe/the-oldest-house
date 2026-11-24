package io.github.knaitoe.theoldesthouse.labyrinth;

import com.google.gson.*;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.BiPredicate;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

/**
 * 0.4.758 (docs/BUILDING_OUTSIDE_0_4_758.md): the stories' block sets, for building outside the House. Each story in
 * the_oldest_house/story_pieces.json owns the blocks whose ids start with its prefixes; every block gets an item, every kind of a
 * fitting is its own piece (the kind carried on the stack as its block state), and each piece is cut at a stonecutter from what it is
 * made of. Finishing a story ({@link WitnessAccount}) teaches its reader the set and gives them its keepsake, once. A stonecutter shows
 * a reader only the pieces they know. A piece set down outside the House is furniture: it breaks like an ordinary block and drops
 * itself, and using it does nothing. Nothing places a piece inside the House.
 */
public final class StoryPieces {
    private StoryPieces(){}
    /** One piece: a block, the kind it is set to (or none), the recipe that cuts it, the story it belongs to. */
    public record Piece(Block block,@Nullable String kind,ResourceLocation recipe,WitnessAccount.Story story){
        public ItemStack stack(){
            var s=new ItemStack(block.asItem());
            if(kind!=null)s.set(DataComponents.BLOCK_STATE,new BlockItemStateProperties(Map.of("kind",kind)));
            return s;
        }
    }
    private record Set(WitnessAccount.Story story,List<String> prefixes,java.util.Set<String> skip,String keepsake,String line){}
    private static final List<Set> SETS=load();
    private static final Map<WitnessAccount.Story,List<Piece>> BY_STORY=new EnumMap<>(WitnessAccount.Story.class);
    private static final Map<Block,WitnessAccount.Story> STORY_OF=new IdentityHashMap<>();
    private static final Map<ResourceLocation,Piece> BY_RECIPE=new HashMap<>();
    /** Set on the client: whether the local reader's recipe book knows a recipe. */
    public static BiPredicate<Player,ResourceLocation> clientKnows=(p,id)->false;

    private static List<Set> load(){
        var out=new ArrayList<Set>();
        try(var in=StoryPieces.class.getResourceAsStream("/the_oldest_house/story_pieces.json")){
            if(in==null)return out;
            var root=JsonParser.parseReader(new InputStreamReader(in,StandardCharsets.UTF_8)).getAsJsonObject();
            for(var e:root.getAsJsonArray("stories")){
                var o=e.getAsJsonObject();var id=o.get("story").getAsString();
                WitnessAccount.Story story=null;for(var s:WitnessAccount.Story.values())if(s.id.equals(id))story=s;
                if(story==null)throw new IllegalStateException("story_pieces.json names no story "+id);
                var prefixes=new ArrayList<String>();for(var p:o.getAsJsonArray("prefixes"))prefixes.add(p.getAsString());
                var skip=new HashSet<String>();for(var p:o.getAsJsonArray("skip"))skip.add(p.getAsString());
                out.add(new Set(story,List.copyOf(prefixes),skip,o.get("keepsake").getAsString(),o.get("line").getAsString()));
            }
        }catch(java.io.IOException ex){throw new IllegalStateException(ex);}
        return List.copyOf(out);
    }
    private static @Nullable Set setFor(String path){
        for(var s:SETS)for(var p:s.prefixes)if(path.startsWith(p))return s;
        return null;
    }
    private static @Nullable Property<?> kindOf(Block b){return b.getStateDefinition().getProperty("kind");}
    @SuppressWarnings({"unchecked","rawtypes"})
    private static String name(Property p,Comparable v){return p.getName(v);}

    public static void register(IEventBus bus){
        bus.addListener(EventPriority.LOWEST,false,RegisterEvent.class,StoryPieces::items);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST,StoryPieces::broken);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGH,StoryPieces::used);
    }
    /** Every story block without an item of its own gets one, after every other item is in. */
    private static void items(RegisterEvent e){
        if(!e.getRegistryKey().equals(Registries.ITEM))return;
        var blocks=new TreeMap<String,Block>();
        for(var entry:BuiltInRegistries.BLOCK.entrySet()){
            var id=entry.getKey().location();
            if(id.getNamespace().equals(TheOldestHouse.MOD_ID)&&setFor(id.getPath())!=null)blocks.put(id.getPath(),entry.getValue());
        }
        e.<Item>register(Registries.ITEM,helper->{
            for(var entry:blocks.entrySet()){
                var path=entry.getKey();var block=entry.getValue();var set=setFor(path);
                if(set.skip.contains(path)||Item.BY_BLOCK.containsKey(block))continue;
                var id=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,path);
                if(BuiltInRegistries.ITEM.containsKey(id))continue;
                helper.register(id,new PieceItem(block,new Item.Properties()));
                STORY_OF.put(block,set.story);
                var pieces=BY_STORY.computeIfAbsent(set.story,k->new ArrayList<>());
                var kind=kindOf(block);
                if(kind==null)add(pieces,new Piece(block,null,recipeId(path,null),set.story));
                else for(Comparable<?> v:kind.getPossibleValues()){
                    var k=name(kind,v);
                    if(!set.skip.contains(path+"["+k+"]"))add(pieces,new Piece(block,k,recipeId(path,k),set.story));
                }
            }
        });
    }
    private static void add(List<Piece> pieces,Piece p){pieces.add(p);BY_RECIPE.put(p.recipe,p);}
    private static ResourceLocation recipeId(String path,@Nullable String kind){
        return ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"piece/"+path+(kind==null?"":"/"+kind));
    }

    public static boolean isPiece(Block b){return STORY_OF.containsKey(b);}
    /** Every piece item, in block order. */
    public static List<Item> items(){var out=new ArrayList<Item>();for(var b:STORY_OF.keySet())out.add(b.asItem());out.sort(Comparator.comparing(i->BuiltInRegistries.ITEM.getKey(i).getPath()));return out;}
    public static @Nullable WitnessAccount.Story story(Block b){return STORY_OF.get(b);}
    public static List<Piece> pieces(WitnessAccount.Story story){return BY_STORY.getOrDefault(story,List.of());}
    public static boolean isPieceRecipe(ResourceLocation id){return BY_RECIPE.containsKey(id);}
    public static @Nullable Piece keepsake(WitnessAccount.Story story){
        for(var s:SETS)if(s.story==story){
            var k=s.keepsake;var at=k.indexOf('[');var path=at<0?k:k.substring(0,at);var kind=at<0?null:k.substring(at+1,k.length()-1);
            for(var p:pieces(story))if(BuiltInRegistries.BLOCK.getKey(p.block).getPath().equals(path)&&Objects.equals(p.kind,kind))return p;
        }
        return null;
    }
    private static String line(WitnessAccount.Story story){for(var s:SETS)if(s.story==story)return s.line;return "";}
    /** The kind a piece stack is set to, if any. */
    public static @Nullable String kind(ItemStack s){var props=s.get(DataComponents.BLOCK_STATE);return props==null?null:props.properties().get("kind");}
    /** The block state a piece stack places (facing aside), for its look in the hand and the inventory. */
    public static BlockState state(ItemStack s){
        if(!(s.getItem() instanceof BlockItem b))return net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
        var props=s.get(DataComponents.BLOCK_STATE);var st=b.getBlock().defaultBlockState();
        return props==null?st:props.apply(st);
    }
    /** A piece's name: its own line in the language file if it has one, else its kind (or its block's name after the story's prefix). */
    public static Component name(Block b,@Nullable String kind){
        var path=BuiltInRegistries.BLOCK.getKey(b).getPath();
        if(kind==null)return Component.translatableWithFallback(b.getDescriptionId(),words(trimPrefix(path)));
        return Component.translatableWithFallback("piece."+TheOldestHouse.MOD_ID+"."+path+"."+kind,words(kind));
    }
    private static String trimPrefix(String path){
        var set=setFor(path);if(set!=null)for(var p:set.prefixes)if(path.startsWith(p)&&path.length()>p.length())return path.substring(p.length());
        return path;
    }
    private static String words(String id){var s=id.replace('_',' ');return s.isEmpty()?s:Character.toUpperCase(s.charAt(0))+s.substring(1);}

    /** What a piece is cut from: by the sound of the block it was made like, wood from planks, cloth from wool, and so on. */
    private static JsonObject ingredient(Block b){
        var o=new JsonObject();var sound=b.defaultBlockState().getSoundType();
        if(sound==SoundType.WOOD||sound==SoundType.CHERRY_WOOD||sound==SoundType.BAMBOO_WOOD||sound==SoundType.NETHER_WOOD||sound==SoundType.LADDER
                ||sound==SoundType.SCAFFOLDING||sound==SoundType.BAMBOO||sound==SoundType.CHISELED_BOOKSHELF)o.addProperty("tag","minecraft:planks");
        else if(sound==SoundType.WOOL)o.addProperty("tag","minecraft:wool");
        else if(sound==SoundType.GLASS)o.addProperty("item","minecraft:glass");
        else if(sound==SoundType.METAL||sound==SoundType.ANVIL||sound==SoundType.CHAIN||sound==SoundType.LANTERN||sound==SoundType.COPPER
                ||sound==SoundType.COPPER_GRATE||sound==SoundType.COPPER_BULB||sound==SoundType.HEAVY_CORE)o.addProperty("item","minecraft:iron_ingot");
        else if(sound==SoundType.GRAVEL||sound==SoundType.SAND||sound==SoundType.ROOTED_DIRT||sound==SoundType.MUD||sound==SoundType.GRASS)o.addProperty("item","minecraft:dirt");
        else o.addProperty("tag","minecraft:stone_crafting_materials");
        return o;
    }
    /** Written into the recipe manager's input as it loads (mixin.RecipeManagerPiecesMixin); a data pack's own recipe of the same id wins. */
    public static void addRecipes(Map<ResourceLocation,JsonElement> recipes){
        for(var piece:BY_RECIPE.values()){
            if(recipes.containsKey(piece.recipe))continue;
            var o=new JsonObject();o.addProperty("type","minecraft:stonecutting");o.add("ingredient",ingredient(piece.block));
            var result=new JsonObject();result.addProperty("id",BuiltInRegistries.ITEM.getKey(piece.block.asItem()).toString());result.addProperty("count",1);
            if(piece.kind!=null){JsonObject comps=new JsonObject(),state=new JsonObject();state.addProperty("kind",piece.kind);comps.add("minecraft:block_state",state);result.add("components",comps);}
            o.add("result",result);recipes.put(piece.recipe,o);
        }
    }
    /** What a stonecutter shows this reader: every ordinary recipe, and of the stories' pieces only those they know. */
    public static <T extends net.minecraft.world.item.crafting.Recipe<?>> List<RecipeHolder<T>> known(Player who,List<RecipeHolder<T>> recipes){
        var out=new ArrayList<RecipeHolder<T>>(recipes.size());
        for(var r:recipes)if(!isPieceRecipe(r.id())||knows(who,r.id()))out.add(r);
        return out;
    }
    public static boolean knows(Player who,ResourceLocation id){
        return who instanceof ServerPlayer sp?sp.getRecipeBook().contains(id):clientKnows.test(who,id);
    }

    /** A story is finished: its reader learns its pieces and, the first time, is given its keepsake. */
    public static void ended(ServerPlayer p,WitnessAccount.Story story){
        var pieces=pieces(story);if(pieces.isEmpty()||p.isSpectator())return;
        var keep=keepsake(story);
        boolean first=keep!=null&&!p.getRecipeBook().contains(keep.recipe);
        var holders=new ArrayList<RecipeHolder<?>>();
        for(var piece:pieces)p.server.getRecipeManager().byKey(piece.recipe).ifPresent(holders::add);
        if(p.awardRecipes(holders)>0)p.displayClientMessage(Component.literal("The pieces of “"+story.title+"” can be cut at a stonecutter now.").withStyle(ChatFormatting.GRAY),false);
        if(first){
            var s=keepsake(story,keep);
            if(!p.getInventory().add(s))p.drop(s,false);
        }
    }
    /** The keepsake as it is given: the piece, with the story's line under its name. */
    public static ItemStack keepsake(WitnessAccount.Story story,Piece keep){
        var s=keep.stack();
        s.set(DataComponents.LORE,new ItemLore(List.of(Component.literal(line(story)).withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC))));
        return s;
    }

    /** Whether a piece here breaks like an ordinary block: only one a reader set down outside the House. */
    public static boolean breakable(Block b,BlockGetter level,BlockPos pos){
        if(!isPiece(b)||!(level instanceof Level l)||HouseDimensions.isHouseDimension(l.dimension()))return false;
        return l instanceof ServerLevel sl?PlacedPieces.has(sl,pos):l.isClientSide();
    }
    /** The piece a placed block gives back. */
    public static ItemStack stackOf(BlockState s){
        var stack=new ItemStack(s.getBlock().asItem());
        var kind=kindOf(s.getBlock());
        if(kind!=null)stack.set(DataComponents.BLOCK_STATE,new BlockItemStateProperties(Map.of("kind",name(kind,s.getValue(kind)))));
        return stack;
    }
    private static void broken(BlockEvent.BreakEvent e){
        if(e.isCanceled()||!(e.getLevel() instanceof ServerLevel l)||!isPiece(e.getState().getBlock())||!PlacedPieces.has(l,e.getPos()))return;
        PlacedPieces.remove(l,e.getPos());
        if(!e.getPlayer().isCreative())Block.popResource(l,e.getPos(),stackOf(e.getState()));
    }
    /** A piece outside the House is furniture: using it does nothing, though a held block is still set against it. */
    private static void used(PlayerInteractEvent.RightClickBlock e){
        var l=e.getLevel();var s=l.getBlockState(e.getPos());
        if(breakable(s.getBlock(),l,e.getPos()))e.setUseBlock(TriState.FALSE);
    }
}

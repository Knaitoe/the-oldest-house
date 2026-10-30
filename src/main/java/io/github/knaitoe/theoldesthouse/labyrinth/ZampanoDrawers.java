package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.journal.ZampanoLeafItem;
import io.github.knaitoe.theoldesthouse.journal.ZampanoLeaves;
import io.github.knaitoe.theoldesthouse.network.ZampanoDrawerActionPayload;
import io.github.knaitoe.theoldesthouse.network.ZampanoFolderPayload;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Zampanò's three filing cabinets against the courtyard archive's north wall, under his scribbles (JOURNAL_DESIGN.md, step
 * 2). A drawer opens onto a folder of two leaves drawn by the journal's renderer. Which drawer stands open is the one thing
 * the readers share; each reader pulls their own copy of a leaf, once, and the folder still holds it for everyone else.
 * The archive's finite originals (the readers' files, the chest's book, the survey on its lectern) are never touched.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class ZampanoDrawers {
    public static final String STATE="zampano_cabinets0178",PULLED="ZampanoLeaves0178";
    /** The left cabinet; the others stand at x 0 and 1, backs to the paper wall. */
    public static final BlockPos FIRST=new BlockPos(-1,0,-38);
    static final double REACH=6;
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    record Open(ResourceKey<Level> level,BlockPos pos,int folder,int drawer,long order){}
    private static final Map<UUID,Open> OPEN=new HashMap<>();
    private static final Set<GlobalPos> SHOWN=new HashSet<>();
    private static long order;
    public enum Pull{PULLED,NOT_OPEN,NOT_IN_THIS_FOLDER,ALREADY}
    private ZampanoDrawers(){}

    public static BlockState cabinet(int i){return NovelRegistry.ZAMPANO_CABINET.get().defaultBlockState().setValue(ZampanoCabinetBlock.FACING,Direction.SOUTH).setValue(ZampanoCabinetBlock.CABINET,i);}
    public static BlockPos cell(int i){return FIRST.offset(i,0,0);}
    /** New courtyards stand the cabinets as they are built. */
    public static void build(ServerLevel l,BlockPos b){
        for(int i=0;i<ZampanoLeaves.CABINETS;i++)BuildBlocks.set(l,b.offset(cell(i)),cabinet(i),F);
        BuildBlocks.after(l,()->done(l.getServer(),b));
    }
    private static void done(MinecraftServer s,BlockPos b){var d=LabyrinthData.get(s);var saved=d.state(STATE);saved.putBoolean(Long.toString(b.asLong()),true);d.setState(STATE,saved);}
    public static boolean placed(MinecraftServer s,BlockPos b){return LabyrinthData.get(s).state(STATE).getBoolean(Long.toString(b.asLong()));}
    /**
     * Saved courtyards get theirs once, while the archive is loaded, unwatched and empty, and only where the three cells and
     * the floor before them are still bare: a reader's own building there is kept, and the courtyard is never given them twice.
     */
    public static boolean place(ServerLevel l,BlockPos b){
        if(placed(l.getServer(),b))return true;
        var area=new AABB(Vec3.atLowerCornerOf(b.offset(FIRST)),Vec3.atLowerCornerOf(b.offset(cell(ZampanoLeaves.CABINETS-1)).offset(1,2,2)));
        for(int i=0;i<ZampanoLeaves.CABINETS;i++){var at=b.offset(cell(i));if(!l.hasChunkAt(at)||!l.areEntitiesLoaded(net.minecraft.world.level.ChunkPos.asLong(at.getX()>>4,at.getZ()>>4)))return false;}
        if(l.players().stream().anyMatch(p->area.inflate(24).intersects(p.getCamera().getBoundingBox()))||!l.getEntitiesOfClass(LivingEntity.class,area.inflate(.05),LivingEntity::isAlive).isEmpty())return false;
        boolean bare=true;
        for(int i=0;i<ZampanoLeaves.CABINETS;i++){var at=b.offset(cell(i));if(!l.getBlockState(at).isAir()||!l.getBlockState(at.south()).isAir()||l.getBlockState(at.below()).isAir())bare=false;}
        if(bare)for(int i=0;i<ZampanoLeaves.CABINETS;i++)l.setBlock(b.offset(cell(i)),cabinet(i),F);
        done(l.getServer(),b);return true;
    }

    /** The reader's hand on a drawer front: that drawer slides out and its folder opens on their screen. */
    public static void open(ServerPlayer p,BlockPos pos,int cabinet,int drawer){
        if(!LiteraryVignettes.participant(p)||cabinet<0||cabinet>=ZampanoLeaves.CABINETS||drawer<0||drawer>=ZampanoLeaves.DRAWERS)return;
        int folder=cabinet*ZampanoLeaves.DRAWERS+drawer;var level=p.serverLevel();
        if(!(level.getBlockState(pos).getBlock() instanceof ZampanoCabinetBlock))return;
        boolean wasShut=level.getBlockState(pos).getValue(ZampanoCabinetBlock.DRAWER)==0;
        var before=OPEN.put(p.getUUID(),new Open(level.dimension(),pos.immutable(),folder,drawer,++order));SHOWN.add(GlobalPos.of(level.dimension(),pos.immutable()));
        // A hand moved to another cabinet lets go of the first at once.
        if(before!=null&&!(before.level().equals(level.dimension())&&before.pos().equals(pos)))settle(p.server,GlobalPos.of(before.level(),before.pos()));
        settle(p.server,GlobalPos.of(level.dimension(),pos.immutable()));
        if(wasShut)level.playSound(null,pos,SoundEvents.CHISELED_BOOKSHELF_PICKUP,SoundSource.BLOCKS,.7F,.75F);
        level.playSound(null,pos,SoundEvents.BOOK_PAGE_TURN,SoundSource.BLOCKS,.6F,1.1F);
        io.github.knaitoe.theoldesthouse.network.HousePackets.send(p,new ZampanoFolderPayload(folder,-1,pulled(p)));
    }
    public static void act(ServerPlayer p,int leaf){if(leaf==ZampanoDrawerActionPayload.CLOSE)close(p);else pull(p,leaf);}
    /** The reader closes the folder; the drawer goes back in unless someone else still has a folder from it open. */
    public static void close(ServerPlayer p){var open=OPEN.remove(p.getUUID());if(open!=null)settle(p.server,GlobalPos.of(open.level(),open.pos()));}
    public static boolean reading(ServerPlayer p){return OPEN.containsKey(p.getUUID());}
    public static int folderOpen(ServerPlayer p){var o=OPEN.get(p.getUUID());return o==null?-1:o.folder();}

    /** Leaves this reader has pulled, as a mask of leaf indices. */
    public static int pulled(ServerPlayer p){return LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),LabyrinthPlace.ZAMPANO_COURTYARD).getInt(PULLED);}
    /** Pulls the reader's own copy of a leaf from the folder they have open. Nothing leaves the folder for anyone else. */
    public static Pull pull(ServerPlayer p,int leaf){
        var open=OPEN.get(p.getUUID());
        if(open==null||!LiteraryVignettes.participant(p))return Pull.NOT_OPEN;
        if(!ZampanoLeaves.valid(leaf)||ZampanoLeaves.folderOf(leaf)!=open.folder())return Pull.NOT_IN_THIS_FOLDER;
        var data=LabyrinthData.get(p.server);var own=LiteraryVignettes.personal(data,p.getUUID(),LabyrinthPlace.ZAMPANO_COURTYARD);int mask=own.getInt(PULLED);
        if((mask&1<<leaf)!=0)return Pull.ALREADY;
        own.putInt(PULLED,mask|1<<leaf);LiteraryVignettes.save(data,p.getUUID(),LabyrinthPlace.ZAMPANO_COURTYARD,own);
        var page=ZampanoLeafItem.of(leaf);if(!p.getInventory().add(page))p.drop(page,false);
        p.serverLevel().playSound(null,open.pos(),SoundEvents.BOOK_PAGE_TURN,SoundSource.PLAYERS,.8F,.8F);
        io.github.knaitoe.theoldesthouse.network.HousePackets.send(p,new ZampanoFolderPayload(open.folder(),-1,mask|1<<leaf));
        return Pull.PULLED;
    }

    /** Shows the newest open folder's drawer on a cabinet, or shuts it when nobody has one open. */
    static void settle(MinecraftServer s,GlobalPos at){
        var level=s.getLevel(at.dimension());
        if(level==null||!level.isLoaded(at.pos())){SHOWN.remove(at);return;}
        var state=level.getBlockState(at.pos());
        if(!(state.getBlock() instanceof ZampanoCabinetBlock)){SHOWN.remove(at);OPEN.values().removeIf(o->o.level().equals(at.dimension())&&o.pos().equals(at.pos()));return;}
        Open newest=null;for(var o:OPEN.values())if(o.level().equals(at.dimension())&&o.pos().equals(at.pos())&&(newest==null||o.order()>newest.order()))newest=o;
        int drawer=newest==null?0:newest.drawer()+1,was=state.getValue(ZampanoCabinetBlock.DRAWER);
        if(drawer!=was)level.setBlock(at.pos(),state.setValue(ZampanoCabinetBlock.DRAWER,drawer),Block.UPDATE_CLIENTS);
        if(newest==null){SHOWN.remove(at);if(was!=0)level.playSound(null,at.pos(),SoundEvents.CHISELED_BOOKSHELF_INSERT,SoundSource.BLOCKS,.7F,.75F);}
    }
    /** A reader who walks off, dies, changes world or leaves has let go of the folder. */
    public static void sweep(MinecraftServer s){
        OPEN.entrySet().removeIf(e->{var p=s.getPlayerList().getPlayer(e.getKey());var o=e.getValue();
            return p==null||!LiteraryVignettes.participant(p)||!p.level().dimension().equals(o.level())||p.getEyePosition().distanceToSqr(Vec3.atCenterOf(o.pos()))>REACH*REACH;});
        for(var at:List.copyOf(SHOWN))settle(s,at);
    }
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){if(e.getServer().getTickCount()%10==7&&!SHOWN.isEmpty())sweep(e.getServer());}
    @SubscribeEvent public static void loggedOut(PlayerEvent.PlayerLoggedOutEvent e){OPEN.remove(e.getEntity().getUUID());}
    @SubscribeEvent public static void stopped(ServerStoppedEvent e){OPEN.clear();SHOWN.clear();}
}

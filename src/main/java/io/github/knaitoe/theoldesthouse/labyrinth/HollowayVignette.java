package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.*;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Shared native pursuit, personal theft/visits/escape. No scene-wide reset or shared credit. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class HollowayVignette {
    public static final String ID="holloway_camp",REWARD_OWNER="HollowayRewardOwner";
    public static final int ARENA_TICKS=80;
    private static final Set<UUID> PRESENT=new HashSet<>();
    private static final Map<UUID,Long> MISSING=new HashMap<>();
    private static final Map<UUID,HouseHuman> LIVE=new HashMap<>();
    private HollowayVignette(){}
    public static CompoundTag personal(LabyrinthData data,UUID id){return data.state(ID).getCompound("Players").getCompound(id.toString()).copy();}
    private static void save(LabyrinthData data,UUID id,CompoundTag own){var all=data.state(ID);var players=all.getCompound("Players");players.put(id.toString(),own);all.put("Players",players);data.setState(ID,all);}
    public static @Nullable BlockPos base(MinecraftServer s){return IndianLakeRooms.base(s,LabyrinthPlace.HOLLOWAY_CAMP);}
    public static boolean inside(ServerPlayer p){return p.gameMode.getGameModeForPlayer()!=net.minecraft.world.level.GameType.SPECTATOR&&IndianLakeRooms.inside(p,LabyrinthPlace.HOLLOWAY_CAMP);}
    public static boolean canDeal(LabyrinthData data,UUID id){return !personal(data,id).getBoolean("Escaped")&&!WitnessAccount.has(data,id,WitnessAccount.Story.HOLLOWAY);}
    public static boolean pursued(ServerPlayer p){var own=personal(LabyrinthData.get(p.server),p.getUUID());return inside(p)&&(own.getBoolean("Run")||own.getBoolean("CampAnger"));}
    public static void provoke(ServerPlayer p,boolean theft){
        if(!inside(p))return;enter(p);var data=LabyrinthData.get(p.server);var own=personal(data,p.getUUID());
        long now=p.serverLevel().getGameTime();if(!theft&&own.contains("TouchedAt")&&now-own.getLong("TouchedAt")<20)return;
        int suspicion=Math.min(3,own.getInt("Suspicion")+1);own.putInt("Suspicion",suspicion);own.putLong("TouchedAt",now);
        if(theft||suspicion>=3)own.putBoolean("CampAnger",true);save(data,p.getUUID(),own);
        p.displayClientMessage(Component.literal(theft?"Holloway: Put it back. You think I didn't see?":suspicion>=3?"Holloway: I told you. Get away from my things.":"Holloway: Don't touch that. I counted everything."),false);
    }
    public static void onArrive(ServerPlayer p,LabyrinthPlace place){if(place==LabyrinthPlace.HOLLOWAY_CAMP)enter(p);}
    public static void enter(ServerPlayer p){
        if(!inside(p)||!PRESENT.add(p.getUUID()))return;
        var b=base(p.server);var l=p.serverLevel();var box=IndianLakeRooms.bounds(b,LabyrinthPlace.HOLLOWAY_CAMP);
        IndianLakeRooms.keepLoaded(l,b,LabyrinthPlace.HOLLOWAY_CAMP);
        for(int x=((int)box.minX)>>4;x<=((int)box.maxX)>>4;x++)for(int z=((int)box.minZ)>>4;z<=((int)box.maxZ)>>4;z++)l.getChunk(x,z);
        var data=LabyrinthData.get(p.server);var own=personal(data,p.getUUID());
        // A saved in-room run resumes after logout/restart; leaving physically ends that run.
        boolean resume=own.getBoolean("InRoom");
        if(!resume){int visit=own.getInt("Visits")+1;own.putInt("Visits",visit);
            own.putBoolean("Run",own.getBoolean("Looted")&&visit>own.getInt("LootVisit")&&!own.getBoolean("Escaped"));
            own.putInt("Arena",0);own.putInt("ArenaTicks",0);own.putBoolean("Seen",false);
        }
        own.putBoolean("InRoom",true);own.putLong("LastTick",-1);save(data,p.getUUID(),own);
        ensureActor(l,b);
        p.displayClientMessage(Component.literal(own.getBoolean("Run")?"Boots scrape beyond the hut. Three rooms north; the service latch is on the left.":"A dirt hut. A field survey by the bed. Someone has counted the supplies."),false);
    }
    /** Called only by the native source slot's onTake, never by opening or receiving a borrowed item. */
    private static void looted(ServerPlayer p){
        if(!inside(p))return;enter(p);var data=LabyrinthData.get(p.server);var own=personal(data,p.getUUID());
        provoke(p,true);own=personal(data,p.getUUID());if(own.getBoolean("Looted"))return;own.putBoolean("Looted",true);own.putInt("LootVisit",own.getInt("Visits"));save(data,p.getUUID(),own);
        p.displayClientMessage(Component.literal("The empty space in the barrel looks deliberate."),false);
    }
    public static void depart(ServerPlayer p){
        PRESENT.remove(p.getUUID());var data=LabyrinthData.get(p.server);var own=personal(data,p.getUUID());
        if(!own.getBoolean("InRoom"))return;own.putBoolean("InRoom",false);own.putBoolean("Run",false);own.putBoolean("CampAnger",false);own.putInt("Arena",0);own.putInt("ArenaTicks",0);save(data,p.getUUID(),own);
    }
    public static void clearAll(){PRESENT.clear();MISSING.clear();LIVE.clear();}
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent e){PRESENT.remove(e.getEntity().getUUID());}
    @SubscribeEvent public static void death(LivingDeathEvent e){if(e.getEntity() instanceof ServerPlayer p)depart(p);}
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        var s=e.getServer();var b=base(s);var l=s.getLevel(HouseDimensions.INTERIOR);if(b==null||l==null)return;
        for(var world:s.getAllLevels())for(var p:List.copyOf(world.players()))playerTick(p);
        var visitors=IndianLakeRooms.visitors(l,b,LabyrinthPlace.HOLLOWAY_CAMP);if(visitors.isEmpty()){
            var all=LabyrinthData.get(s).state(ID);if(all.hasUUID("Actor")&&l.getEntity(all.getUUID("Actor")) instanceof HouseHuman actor){actor.pursue(null);actor.getNavigation().stop();}return;
        }
        IndianLakeRooms.keepLoaded(l,b,LabyrinthPlace.HOLLOWAY_CAMP);HollowayCamp.repairSigns(l,b);var actor=ensureActor(l,b);if(actor!=null)hunt(actor,visitors,b);
    }
    public static void playerTick(ServerPlayer p){
        if(!inside(p)){if(PRESENT.contains(p.getUUID())||personal(LabyrinthData.get(p.server),p.getUUID()).getBoolean("InRoom"))depart(p);return;}
        enter(p);var data=LabyrinthData.get(p.server);var own=personal(data,p.getUUID());if(!own.getBoolean("Run")||own.getBoolean("Escaped"))return;
        long now=p.serverLevel().getGameTime();if(own.getLong("LastTick")==now)return;own.putLong("LastTick",now);
        var r=p.position().subtract(base(p.server).getX(),base(p.server).getY(),base(p.server).getZ());
        int arena=own.getInt("Arena"),ticks=own.getInt("ArenaTicks");
        if(arena==0&&r.z< -14&&r.z> -24){arena=1;ticks=0;}
        boolean present=arena==1&&r.z< -14&&r.z>=-25||arena==2&&r.z< -25&&r.z>=-47||arena==3&&r.z< -47&&r.z>=-69;
        if(present&&r.y>=-.25&&r.y<3)ticks=Math.min(ARENA_TICKS,ticks+1);
        if(ticks>=ARENA_TICKS&&arena==1&&r.z< -25&&r.z> -46){arena=2;ticks=0;}
        else if(ticks>=ARENA_TICKS&&arena==2&&r.z< -47&&r.z> -65){arena=3;ticks=0;}
        own.putInt("Arena",arena);own.putInt("ArenaTicks",ticks);save(data,p.getUUID(),own);
    }
    public static @Nullable HouseHuman ensureActor(ServerLevel l,BlockPos b){
        var data=LabyrinthData.get(l.getServer());var all=data.state(ID);var bounds=IndianLakeRooms.bounds(b,LabyrinthPlace.HOLLOWAY_CAMP);
        if(all.hasUUID("Actor")){
            UUID id=all.getUUID("Actor");var entity=l.getEntity(id);if(entity instanceof HouseHuman actor){LIVE.put(id,actor);MISSING.remove(id);return actor;}
            var queued=LIVE.get(id);if(queued!=null&&!queued.isRemoved()&&queued.level()==l){MISSING.remove(id);return queued;}
            // Native entity chunks can arrive after block chunks on a saved-world join.
            long first=MISSING.computeIfAbsent(id,key->l.getGameTime());if(l.getGameTime()-first<100)return null;
        }
        var existing=l.getEntitiesOfClass(HouseHuman.class,bounds);
        HouseHuman actor=existing.isEmpty()?NovelRegistry.HUMAN.get().create(l):existing.getFirst();if(actor==null)return null;
        if(existing.isEmpty()){actor.moveTo(b.getX()-3.5,b.getY(),b.getZ()-15.5,0,0);if(!l.addFreshEntity(actor))return null;}
        LIVE.put(actor.getUUID(),actor);
        all.putUUID("Actor",actor.getUUID());data.setState(ID,all);return actor;
    }
    private static void hunt(HouseHuman actor,List<ServerPlayer> visitors,BlockPos b){
        var l=(ServerLevel)actor.level();var target=visitors.stream().filter(HollowayVignette::pursued)
                .min(Comparator.comparingDouble(actor::distanceToSqr)).orElse(null);actor.pursue(target);
        if(target==null){actor.patrol(b,visitors);return;}
        var data=LabyrinthData.get(target.server);
        for(var visitor:visitors)if(pursued(visitor)&&actor.distanceToSqr(visitor)<1024&&actor.hasLineOfSight(visitor)){
            var own=personal(data,visitor.getUUID());if(!own.getBoolean("Seen")){own.putBoolean("Seen",true);save(data,visitor.getUUID(),own);}
        }
        boolean sight=actor.hasLineOfSight(target);
        if(actor.staggered()){actor.getNavigation().stop();return;}
        actor.getLookControl().setLookAt(target,40,40);
        if(l.getGameTime()%10==0){var torch=sight?null:torchTarget(l,target);
            if(torch==null)actor.getNavigation().moveTo(target,1.08);else actor.getNavigation().moveTo(torch.getX()+.5,torch.getY(),torch.getZ()+.5,1.08);}
        // A shot needs native sight and a short range; walls and innocent visitors never become targets.
        double distance=actor.distanceToSqr(target);
        if(actor.attackReady()&&sight&&distance<100){
            actor.attacked();l.playSound(null,actor.blockPosition(),SoundEvents.CROSSBOW_SHOOT,SoundSource.HOSTILE,.65F,.6F);
            var bolt=net.minecraft.world.entity.EntityType.ARROW.create(l);
            if(bolt!=null){bolt.setOwner(actor);bolt.setPos(actor.getX(),actor.getEyeY()-.1,actor.getZ());bolt.setBaseDamage(1.5);bolt.pickup=net.minecraft.world.entity.projectile.AbstractArrow.Pickup.DISALLOWED;
                Vec3 aim=target.position().add(0,target.getBbHeight()*.55,0).subtract(bolt.position());
                bolt.shoot(aim.x,aim.y+aim.horizontalDistance()*.06,aim.z,1.6F,3);l.addFreshEntity(bolt);}
        }
    }
    /** Only the pursued explorer's actual, still-present native torch is a search marker. */
    public static @Nullable BlockPos torchTarget(ServerLevel l,ServerPlayer p){
        var own=personal(LabyrinthData.get(p.server),p.getUUID());if(!own.contains("Torch"))return null;
        var at=BlockPos.of(own.getLong("Torch"));return isTorch(l.getBlockState(at))?at:null;
    }
    private static boolean isTorch(BlockState state){return state.is(Blocks.TORCH)||state.is(Blocks.WALL_TORCH)||state.is(Blocks.SOUL_TORCH)||state.is(Blocks.SOUL_WALL_TORCH);}
    public static boolean allowsPlacing(ServerLevel l,BlockPos at,BlockState state){var b=base(l.getServer());return b!=null&&l.dimension().equals(HouseDimensions.INTERIOR)&&IndianLakeRooms.bounds(b,LabyrinthPlace.HOLLOWAY_CAMP).contains(at.getCenter())&&isTorch(state);}
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void placed(BlockEvent.EntityPlaceEvent e){
        if(e.isCanceled()||!(e.getEntity() instanceof ServerPlayer p)||!(e.getLevel() instanceof ServerLevel l)||!inside(p)||!allowsPlacing(l,e.getPos(),e.getPlacedBlock()))return;
        var data=LabyrinthData.get(p.server);var own=personal(data,p.getUUID());own.putLong("Torch",e.getPos().asLong());save(data,p.getUUID(),own);
    }
    public static boolean finish(ServerPlayer p,BlockPos at){
        if(!inside(p)||!at.equals(base(p.server).offset(HollowayCamp.LATCH))||p.distanceToSqr(at.getCenter())>16)return false;
        var data=LabyrinthData.get(p.server);var own=personal(data,p.getUUID());
        if(own.getBoolean("Escaped"))return true;
        if(!own.getBoolean("Run")||own.getInt("Arena")!=3||own.getInt("ArenaTicks")<ARENA_TICKS||!own.getBoolean("Seen")){
            p.displayClientMessage(Component.literal("The service latch is stiff. The survey describes three rooms on the way here."),true);return true;
        }
        if(!p.isShiftKeyDown()){p.displayClientMessage(Component.literal("Crouch and pull the service latch."),true);return true;}
        own.putBoolean("Escaped",true);own.putBoolean("Run",false);own.putBoolean("CampAnger",false);own.putBoolean("Rewarded",true);save(data,p.getUUID(),own);
        var shield=owned(VignetteYields.mark(new ItemStack(Items.SHIELD),ID),p.getUUID());shield.set(DataComponents.CUSTOM_NAME,Component.literal("Holloway's battered shield"));
        give(p,shield);give(p,owned(wrongMap(p.serverLevel()),p.getUUID()));
        WitnessAccount.resolve(p,WitnessAccount.Story.HOLLOWAY,"three_rooms_and_service_latch");
        p.displayClientMessage(Component.literal("The latch yields. A shield and a folded survey were wedged behind it. The service door leads back."),false);return true;
    }
    private static ItemStack owned(ItemStack stack,UUID id){CustomData.update(DataComponents.CUSTOM_DATA,stack,tag->tag.putUUID(REWARD_OWNER,id));return stack;}
    private static void give(ServerPlayer p,ItemStack stack){if(!p.getInventory().add(stack))p.drop(stack,false);p.inventoryMenu.broadcastChanges();}
    private static ItemStack wrongMap(ServerLevel l){
        ItemStack map=MapItem.create(l,0,0,(byte)0,false,false);var data=MapItem.getSavedData(map,l);
        if(data!=null){Arrays.fill(data.colors,(byte)0);for(int y=15;y<65;y++)for(int x=18;x<108;x++){
            boolean mark=(x%18==0&&y%16<12)||(y%16==0&&x%18<14);if(mark)data.setColor(x,y,(byte)116);
        }l.setMapData(map.get(DataComponents.MAP_ID),data.locked());}
        map=VignetteYields.mark(map,ID);map.set(DataComponents.CUSTOM_NAME,Component.literal("Holloway's unfinished survey — conjecture"));return map;
    }
    /** Shared supplies may be gone; every explorer can take one personal survey copy. */
    public static final class SurveyMenu extends LecternMenu {
        private final ServerPlayer owner;private final BlockPos at;
        public SurveyMenu(int id,ServerPlayer owner,BlockPos at){this(id,owner,at,new net.minecraft.world.SimpleContainer(1));}
        private SurveyMenu(int id,ServerPlayer owner,BlockPos at,net.minecraft.world.SimpleContainer book){
            super(id,book,new SimpleContainerData(1));this.owner=owner;this.at=at.immutable();book.setItem(0,HollowayCamp.journal());
        }
        @Override public boolean stillValid(net.minecraft.world.entity.player.Player p){return p==owner&&inside(owner)&&owner.distanceToSqr(at.getCenter())<=36&&owner.level().getBlockState(at).is(Blocks.LECTERN);}
        @Override public boolean clickMenuButton(net.minecraft.world.entity.player.Player p,int button){
            if(!stillValid(p))return false;
            if(button==3){var data=LabyrinthData.get(owner.server);var own=personal(data,owner.getUUID());if(own.getBoolean("SurveyTaken"))return false;
                own.putBoolean("SurveyTaken",true);save(data,owner.getUUID(),own);give(owner,owned(VignetteYields.mark(HollowayCamp.journal(),ID),owner.getUUID()));looted(owner);return true;}
            if(button>=100){if(button-100>=4)return false;}else if(button==1){if(getPage()<=0)return false;}else if(button==2){if(getPage()>=3)return false;}else return false;
            return super.clickMenuButton(p,button);
        }
    }
    public static final class CacheMenu extends AbstractContainerMenu {
        private final ServerPlayer owner;private final net.minecraft.world.Container cache;private final BlockPos at;
        public CacheMenu(int id,ServerPlayer owner,net.minecraft.world.Container cache,BlockPos at){
            super(MenuType.GENERIC_9x3,id);this.owner=owner;this.cache=cache;this.at=at.immutable();cache.startOpen(owner);
            for(int row=0;row<3;row++)for(int col=0;col<9;col++)addSlot(new Slot(cache,col+row*9,8+col*18,18+row*18){
                @Override public void onTake(net.minecraft.world.entity.player.Player p,ItemStack stack){super.onTake(p,stack);if(p==owner&&!stack.isEmpty())looted(owner);}
            });
            for(int row=0;row<3;row++)for(int col=0;col<9;col++)addSlot(new Slot(owner.getInventory(),col+row*9+9,8+col*18,85+row*18));
            for(int col=0;col<9;col++)addSlot(new Slot(owner.getInventory(),col,8+col*18,143));
        }
        @Override public boolean stillValid(net.minecraft.world.entity.player.Player p){return p==owner&&inside(owner)&&owner.distanceToSqr(at.getCenter())<=64&&owner.level().getBlockEntity(at)==cache;}
        @Override public ItemStack quickMoveStack(net.minecraft.world.entity.player.Player p,int index){
            if(!stillValid(p)||index<0||index>=slots.size())return ItemStack.EMPTY;var slot=slots.get(index);if(!slot.hasItem())return ItemStack.EMPTY;
            var stack=slot.getItem();var original=stack.copy();if(index<27){if(!moveItemStackTo(stack,27,slots.size(),true))return ItemStack.EMPTY;}
            else if(!moveItemStackTo(stack,0,27,false))return ItemStack.EMPTY;
            if(stack.isEmpty())slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();
            if(stack.getCount()==original.getCount())return ItemStack.EMPTY;slot.onTake(p,original);return original;
        }
        @Override public void removed(net.minecraft.world.entity.player.Player p){super.removed(p);cache.stopOpen(p);}
    }
    @SubscribeEvent(priority=EventPriority.HIGH) public static void block(PlayerInteractEvent.RightClickBlock e){
        if(!(e.getEntity() instanceof ServerPlayer p)||e.getHand()!=InteractionHand.MAIN_HAND||!inside(p))return;
        var at=e.getPos();if(p.distanceToSqr(at.getCenter())>36)return;
        if(at.equals(base(p.server).offset(HollowayCamp.CACHE))&&p.level().getBlockEntity(at) instanceof net.minecraft.world.Container cache){
            provoke(p,false);
            e.setCanceled(true);e.setCancellationResult(InteractionResult.SUCCESS);p.openMenu(new SimpleMenuProvider((id,inventory,player)->new CacheMenu(id,p,cache,at),Component.literal("Counted supplies")));
        }else if(at.equals(base(p.server).offset(HollowayCamp.JOURNAL))){
            provoke(p,false);
            e.setCanceled(true);e.setCancellationResult(InteractionResult.SUCCESS);p.openMenu(new SimpleMenuProvider((id,inventory,player)->new SurveyMenu(id,p,at),Component.literal("Holloway's survey")));
        }else if(finish(p,at)){e.setCanceled(true);e.setCancellationResult(InteractionResult.SUCCESS);}
    }
}

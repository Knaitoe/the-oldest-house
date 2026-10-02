package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.opening.CompanionOrders;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.world.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.*;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.*;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.*;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** The cell is the commitment point. Everything consequential is stored in world data. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class FinaleController {
    private static final int FLAGS=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    public static final String GUIDE="HouseFinaleGuide";
    private static final Map<UUID,Long> LAST_WORDS=new HashMap<>();
    private static final Map<UUID,Long> MISSING_CREATURES=new HashMap<>();
    private static final Set<TamableAnimal> GUIDE_GOALS=Collections.newSetFromMap(new WeakHashMap<>());
    private FinaleController(){}
    public static @Nullable BlockPos companionTarget(ServerPlayer player,boolean exit) {
        BlockPos origin=HouseSavedData.get(player.server).houseOrigin();if(origin==null)return null;
        var phase=FinaleProgress.phase(player.server,player.getUUID());
        if(FinaleProgress.committed(phase)&&phase!=FinaleProgress.Phase.HOMEWARD)return player.blockPosition();
        if(player.getY()<FinaleArchitecture.ARENA+3)return exit?FinaleArchitecture.base(origin).offset(0,FinaleArchitecture.ARENA,31):FinaleArchitecture.cell(origin).north(4);
        List<BlockPos> route=FinaleArchitecture.staircaseRoute(origin);int nearest=0;double distance=Double.MAX_VALUE;
        for(int i=0;i<route.size();i++){double d=route.get(i).distToCenterSqr(player.position());if(d<distance){distance=d;nearest=i;}}
        int step=Math.max(0,Math.min(route.size()-1,nearest+(exit?-5:5)));
        return step==0&&exit?FinaleArchitecture.entry(origin):route.get(step);
    }
    public static @Nullable Vec3 cellCenter(MinecraftServer server){BlockPos origin=HouseSavedData.get(server).houseOrigin();return origin==null?null:Vec3.atBottomCenterOf(FinaleArchitecture.cell(origin).south(5));}
    public static boolean lockedOut(ServerPlayer player){return FinaleProgress.terminal(FinaleProgress.phase(player.server,player.getUUID()));}
    public static boolean canOffer(LabyrinthData data,UUID player){
        CompoundTag world=data.state(FinaleProgress.STATE),record=world.getCompound(player.toString());
        return !world.getBoolean("Ended")&&!FinaleProgress.terminal(FinaleProgress.phase(record))
                &&(record.getBoolean("Discovered")||FinaleProgress.eligible(data,player));
    }
    public static void enter(ServerPlayer player,LabyrinthData.Door from){
        if(lockedOut(player)||!FinaleArchitecture.ready(player.server))return;
        LabyrinthData data=LabyrinthData.get(player.server);LabyrinthData.Door entry=data.door(FinaleArchitecture.ENTRY);
        ServerLevel target=player.server.getLevel(HouseDimensions.INTERIOR);if(entry==null||target==null)return;
        var turn=LabyrinthDoors.rotationFrom(from.facing,entry.facing);
        LabyrinthDoors.copyVestibule(player.serverLevel(),from,target,entry,turn);
        Vec3 destination=LabyrinthDoors.shifted(player.position(),from.lower,entry.lower,turn);
        data.pushReturn(player.getUUID(),new LabyrinthData.Waypoint(from.dimension,Vec3.atBottomCenterOf(from.lower),from.facing.toYRot(),true));
        CompoundTag record=FinaleProgress.player(player.server,player.getUUID());record.putString("Phase",FinaleProgress.Phase.STAIRCASE.name());
        record.putBoolean("Discovered",true);record.putBoolean("Inside",false);FinaleProgress.save(player.server,player.getUUID(),record);
        if(player.serverLevel()==target){HouseInternalTeleport.shift(player,destination,player.getYRot()+LabyrinthDoors.angle(turn));LabyrinthDoors.setDoorOpen(target,entry.lower,true,player);}
        else HouseTransitionEvents.beginDoorTransition(player,HouseDimensions.INTERIOR,null,p->LabyrinthDoors.setDoorOpen(target,entry.lower,true,p),destination,player.getYRot()+LabyrinthDoors.angle(turn));
        ensureWitness(target,HouseSavedData.get(player.server).houseOrigin());
    }
    /** Must run before manor auto-entry, including a pending transition saved by another controller. */
    public static boolean enforceExclusion(ServerPlayer player){
        FinaleProgress.Phase phase = FinaleProgress.phase(player.server, player.getUUID());
        if (FinaleProgress.committed(phase) && !player.isDeadOrDying() && !player.serverLevel().dimension().equals(HouseDimensions.INTERIOR)) {
            BlockPos origin = HouseSavedData.get(player.server).houseOrigin(); ServerLevel interior = player.server.getLevel(HouseDimensions.INTERIOR);
            if (origin != null && interior != null) {
                HouseTransitionEvents.cancelPending(player, "the opened cell cannot be left this way");
                BlockPos at = phase == FinaleProgress.Phase.ESCAPE ? FinaleArchitecture.bottomStart(origin) : FinaleArchitecture.base(origin).offset(0,FinaleArchitecture.ARENA,34);
                interior.getChunkAt(at); player.teleportTo(interior,at.getX()+.5,at.getY(),at.getZ()+.5,0,0); player.resetFallDistance(); return true;
            }
        }
        if(!lockedOut(player))return false;
        if(HouseTransitionEvents.isPending(player))HouseTransitionEvents.cancelPending(player,"the House no longer admits this player");
        BlockPos origin=HouseSavedData.get(player.server).housePosition().orElse(null);
        if(origin!=null&&(HouseDimensions.isHouseDimension(player.serverLevel().dimension())
                ||player.serverLevel().dimension().equals(Level.OVERWORLD)&&HouseLayout.isInsideDomesticVolume(player.getX()-origin.getX(),player.getY()-origin.getY(),player.getZ()-origin.getZ())))outside(player,origin);
        return true;
    }
    public static boolean tickPlayer(ServerPlayer player,BlockPos origin){
        if (LabyrinthData.get(player.server).returnDepth(player.getUUID()) >= 9) FinaleArchitecture.request(player.server);
        if(!player.serverLevel().dimension().equals(HouseDimensions.INTERIOR)||!FinaleArchitecture.contains(origin,player.blockPosition()))return false;
        if(!player.isAlive()||player.gameMode.getGameModeForPlayer()==net.minecraft.world.level.GameType.SPECTATOR)return true;
        CompoundTag record=FinaleProgress.player(player.server,player.getUUID());FinaleProgress.Phase phase=FinaleProgress.phase(record);
        if(phase==FinaleProgress.Phase.UNSEEN){record.putString("Phase",FinaleProgress.Phase.STAIRCASE.name());record.putBoolean("Discovered",true);}
        BlockPos b=FinaleArchitecture.base(origin);long now=player.serverLevel().getGameTime();
        if(phase==FinaleProgress.Phase.STAIRCASE||phase==FinaleProgress.Phase.UNSEEN){
            double z=player.getZ()-b.getZ();
            if(player.getY()>FinaleArchitecture.TOP-2&&z<13)record.putBoolean("Inside",true);
            if(player.getY()>FinaleArchitecture.TOP-2&&z>15&&record.getBoolean("Inside")){FinaleProgress.save(player.server,player.getUUID(),record);returnFromStaircase(player);return true;}
            if(player.tickCount%40==0)ensureWitness(player.serverLevel(),origin);
            if(player.getY()<FinaleArchitecture.ARENA+4&&z>31&&!record.getBoolean("Warned")){
                record.putBoolean("Warned",true);words(player,b.offset(0,FinaleArchitecture.ARENA+2,35),"The door behind you still leads back. The cell does not.");
            }
        }else if(phase==FinaleProgress.Phase.HOMEWARD){
            WitnessEnding.returnWeapon(player,record);
            if(player.getY()>FinaleArchitecture.TOP-2&&player.getZ()>b.getZ()+15){WitnessEnding.finish(player,origin,record);return true;}
        }else if(phase==FinaleProgress.Phase.FIGHT||phase==FinaleProgress.Phase.COLLAPSE||phase==FinaleProgress.Phase.RELEASE){
            // Commands and outside teleports cannot turn the commitment into a free exit.
            if(phase!=FinaleProgress.Phase.COLLAPSE&&(player.getY()<FinaleArchitecture.ARENA-2||player.getZ()<b.getZ()+30||Math.abs(player.getX()-b.getX())>16))
                HouseInternalTeleport.shift(player,Vec3.atBottomCenterOf(b.offset(0,FinaleArchitecture.ARENA,34)),0);
            if(player.tickCount%20==0){
                if(phase==FinaleProgress.Phase.RELEASE)WitnessEnding.keepSceneLoaded(player,origin);
                ensureMinotaur(player,origin,record);
            }
            if(phase==FinaleProgress.Phase.COLLAPSE){FinaleCollapse.tickRetreat(player,origin,record);FinaleProgress.save(player.server,player.getUUID(),record);}
        }else if(phase==FinaleProgress.Phase.ESCAPE){
            if(player.getY()<0){player.hurt(player.damageSources().genericKill(),Float.MAX_VALUE);return true;}
            FinaleCollapse.tickEscape(player,origin,record);
            boolean lit=now<record.getLong("LightUntil");
            if(lit)player.removeEffect(MobEffects.DARKNESS);else if(player.tickCount%20==0)player.addEffect(new MobEffectInstance(MobEffects.DARKNESS,60,0,false,false));
            if(record.getBoolean("Descended")){createGuide(player,origin,record);tickGuide(player,origin,record);}
            FinaleProgress.save(player.server,player.getUUID(),record);
        }
        if(player.tickCount%20==0)FinaleProgress.save(player.server,player.getUUID(),record);
        return true;
    }
    private static void returnFromStaircase(ServerPlayer player){
        LabyrinthData data=LabyrinthData.get(player.server);var back=data.popReturn(player.getUUID());
        if(back==null){BlockPos origin=HouseSavedData.get(player.server).houseOrigin();if(origin!=null)HouseInternalTeleport.shift(player,HideAndClap.manorRespawn(origin),180);}
        else {ServerLevel level=player.server.getLevel(back.dimension());if(level!=null){Vec3 to=back.door()?back.pos().add(Direction.fromYRot(back.yaw()).getStepX()*1.2,0,Direction.fromYRot(back.yaw()).getStepZ()*1.2):back.pos();
            if(level==player.serverLevel())HouseInternalTeleport.shift(player,to,back.yaw());else HouseTransitionEvents.beginDoorTransition(player,back.dimension(),null,null,to,back.yaw());}}
        FinaleProgress.phase(player.server,player.getUUID(),FinaleProgress.Phase.UNSEEN);
    }
    @SubscribeEvent(priority=EventPriority.HIGHEST) public static void interact(PlayerInteractEvent.RightClickBlock event){
        if(!(event.getEntity() instanceof ServerPlayer player)||event.getHand()!=InteractionHand.MAIN_HAND)return;
        BlockPos origin=HouseSavedData.get(player.server).housePosition().orElse(null);if(origin==null)return;
        if(lockedOut(player)&&player.serverLevel().dimension().equals(Level.OVERWORLD)&&event.getPos().distSqr(origin.offset(HouseLayout.AXIS_X,1,HouseLayout.FRONT_DOOR_Z))<12){
            event.setCanceled(true);event.setCancellationResult(InteractionResult.SUCCESS);
            if(FinaleProgress.phase(player.server,player.getUUID())!=FinaleProgress.Phase.WITNESSED)
                player.serverLevel().playSound(null,event.getPos(),SoundEvents.ZOMBIE_ATTACK_WOODEN_DOOR,SoundSource.BLOCKS,.35F,.75F);
            words(player,event.getPos().above(),FinaleProgress.phase(player.server,player.getUUID())==FinaleProgress.Phase.WITNESSED?"The door stays quiet.":"Someone knocks back.");return;
        }
        if(!player.serverLevel().dimension().equals(HouseDimensions.INTERIOR)||!FinaleArchitecture.contains(origin,player.blockPosition()))return;
        var phase=FinaleProgress.phase(player.server,player.getUUID());
        if(phase==FinaleProgress.Phase.ESCAPE&&player.getMainHandItem().is(Items.FLINT_AND_STEEL)&&burnable(player.getOffhandItem())){
            event.setCanceled(true);event.setCancellationResult(InteractionResult.SUCCESS);burn(player);return;
        }
        if(phase==FinaleProgress.Phase.ESCAPE&&event.getPos().distManhattan(FinaleArchitecture.exit(origin))<=1&&player.distanceToSqr(event.getPos().getCenter())<20){
            event.setCanceled(true);event.setCancellationResult(InteractionResult.SUCCESS);var record=FinaleProgress.player(player.server,player.getUUID());boolean open=FinaleCollapse.pull(player,origin,record);FinaleProgress.save(player.server,player.getUUID(),record);if(open)finish(player,record.getBoolean("Guided"));return;
        }
        BlockPos cell=FinaleArchitecture.cell(origin);
        if(event.getPos().getZ()==cell.getZ()&&Math.abs(event.getPos().getX()-cell.getX())<=1&&Math.abs(event.getPos().getY()-cell.getY())<=3){
            event.setCanceled(true);event.setCancellationResult(InteractionResult.SUCCESS);
            if(player.isShiftKeyDown()&&WitnessAccount.ready(LabyrinthData.get(player.server),player.getUUID()))WitnessEnding.begin(player);
            else start(player);
        }
    }
    @SubscribeEvent public static void burnAir(PlayerInteractEvent.RightClickItem event){
        if(event.getEntity() instanceof ServerPlayer player&&event.getHand()==InteractionHand.MAIN_HAND
                &&FinaleProgress.phase(player.server,player.getUUID())==FinaleProgress.Phase.ESCAPE&&event.getItemStack().is(Items.FLINT_AND_STEEL)&&burnable(player.getOffhandItem())){
            event.setCanceled(true);event.setCancellationResult(InteractionResult.SUCCESS);burn(player);
        }
    }
    private static boolean burnable(ItemStack stack){return stack.is(Items.PAPER)||stack.has(DataComponents.WRITTEN_BOOK_CONTENT)||VignetteYields.of(stack)!=null;}
    private static void burn(ServerPlayer player){
        CompoundTag record=FinaleProgress.player(player.server,player.getUUID());long now=player.serverLevel().getGameTime();if(now<record.getLong("LightUntil"))return;
        BlockPos pos=player.blockPosition().above();if(!player.serverLevel().getBlockState(pos).isAir())return;
        player.getOffhandItem().shrink(1);player.getMainHandItem().hurtAndBreak(1,player,EquipmentSlot.MAINHAND);
        player.serverLevel().setBlock(pos,Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL,12),FLAGS);
        record.putLong("Light",pos.asLong());record.putLong("LightUntil",now+120);FinaleProgress.save(player.server,player.getUUID(),record);
        player.serverLevel().sendParticles(net.minecraft.core.particles.ParticleTypes.FLAME,player.getX(),player.getY()+1,player.getZ(),14,.1,.2,.1,.01);
        player.serverLevel().playSound(null,pos,SoundEvents.FIRECHARGE_USE,SoundSource.PLAYERS,.6F,.8F);player.removeEffect(MobEffects.DARKNESS);
    }
    public static boolean start(ServerPlayer player){
        BlockPos origin=HouseSavedData.get(player.server).houseOrigin();if(origin==null||!FinaleArchitecture.ready(player.server)||lockedOut(player)||!player.isAlive()||player.gameMode.getGameModeForPlayer()==net.minecraft.world.level.GameType.SPECTATOR)return false;
        CompoundTag world=FinaleProgress.world(player.server);if(world.hasUUID("Owner")&&!world.getUUID("Owner").equals(player.getUUID())){words(player,FinaleArchitecture.cell(origin),"There is already someone on the other side.");return false;}
        if(FinaleProgress.committed(FinaleProgress.phase(player.server,player.getUUID())))return false;
        UUID weapon=WeaponHistory.favorite(player);
        if(weapon==null){ItemStack fallback=new ItemStack(Items.STONE_SWORD);WeaponHistory.record(player,fallback,1);player.getInventory().add(fallback);weapon=WeaponHistory.favorite(player);}
        world.putUUID("Owner",player.getUUID());LabyrinthData.get(player.server).setState(FinaleProgress.STATE,world);
        CompoundTag record=FinaleProgress.player(player.server,player.getUUID());record.putString("Phase",(world.getBoolean("MinotaurWounded")?FinaleProgress.Phase.COLLAPSE:FinaleProgress.Phase.FIGHT).name());record.putUUID("Weapon",weapon);
        if(world.hasUUID("WoundedCreature"))record.putUUID("Creature",world.getUUID("WoundedCreature"));
        if(world.getBoolean("MinotaurWounded"))FinaleCollapse.wounded(player,origin,record);
        if(player.getRespawnPosition()!=null)record.putLong("Base",player.getRespawnPosition().asLong());record.putString("BaseDimension",player.getRespawnDimension().location().toString());
        FinaleArchitecture.seal(player.serverLevel(),origin,true);FinaleArchitecture.openCell(player.serverLevel(),origin);
        ensureMinotaur(player,origin,record);FinaleProgress.save(player.server,player.getUUID(),record);
        LabyrinthData.get(player.server).clearReturns(player.getUUID());return true;
    }
    private static void ensureMinotaur(ServerPlayer player,BlockPos origin,CompoundTag record){
        ServerLevel level=player.serverLevel();BlockPos cell=FinaleArchitecture.cell(origin);level.getChunkAt(cell);
        // Entity lookup is only meaningful after the cell chunk is loaded.
        if(record.hasUUID("Creature")&&level.getEntity(record.getUUID("Creature")) instanceof MinotaurEntity){MISSING_CREATURES.remove(player.getUUID());return;}
        if(record.hasUUID("Creature")){
            long first=MISSING_CREATURES.computeIfAbsent(player.getUUID(),ignored->level.getGameTime());
            if(level.getGameTime()-first<40)return;
        }
        var existing=level.getEntitiesOfClass(MinotaurEntity.class,new AABB(FinaleArchitecture.base(origin)).inflate(100),e->player.getUUID().equals(e.owner()));
        if(!existing.isEmpty()){record.putUUID("Creature",existing.get(0).getUUID());MISSING_CREATURES.remove(player.getUUID());return;}
        MinotaurEntity creature=FinaleRegistry.MINOTAUR.get().create(level);if(creature==null)return;
        creature.moveTo(cell.getX()+.5,cell.getY(),cell.getZ()+5,180,0);creature.owner(player.getUUID());
        if(FinaleProgress.phase(record)==FinaleProgress.Phase.RELEASE)creature.released();
        if(FinaleProgress.phase(record)==FinaleProgress.Phase.COLLAPSE)creature.wounded();level.addFreshEntity(creature);record.putUUID("Creature",creature.getUUID());
    }
    public static void wound(ServerPlayer player,MinotaurEntity creature){
        CompoundTag record=FinaleProgress.player(player.server,player.getUUID());record.putString("Phase",FinaleProgress.Phase.COLLAPSE.name());
        BlockPos origin=HouseSavedData.get(player.server).houseOrigin();if(origin!=null)FinaleCollapse.wounded(player,origin,record);FinaleProgress.save(player.server,player.getUUID(),record);
        creature.wounded();words(player,creature.blockPosition().above(),"It crawls back to the cell. The walls begin to move.");
        player.serverLevel().playSound(null,player.blockPosition(),SoundEvents.GENERIC_EXPLODE.value(),SoundSource.BLOCKS,.55F,.45F);
    }
    public static void beginEscape(ServerPlayer player){
        BlockPos origin=HouseSavedData.get(player.server).houseOrigin();if(origin==null)return;
        CompoundTag record=FinaleProgress.player(player.server,player.getUUID());if(FinaleProgress.phase(record)!=FinaleProgress.Phase.COLLAPSE)return;
        record.putString("Phase",FinaleProgress.Phase.ESCAPE.name());record.putInt("GuideStep",0);
        // The opened side wall leads to a real shaft and a water landing. No transfer replaces the fall.
        words(player,player.blockPosition().above(2),"The west wall is open. Water is falling into the dark.");
        player.serverLevel().playSound(null,player.blockPosition(),SoundEvents.STONE_BREAK,SoundSource.BLOCKS,1,.5F);
        FinaleProgress.save(player.server,player.getUUID(),record);
    }
    private static void createGuide(ServerPlayer player,BlockPos origin,CompoundTag record){
        MotherCollection collection=MotherCollection.get(player.server);
        if(record.getBoolean("RescueAttempted")||!collection.canGuide(player.getUUID()))return;
        record.putBoolean("RescueAttempted",true);
        for(var entry:collection.all())if(entry.pet&&!entry.sealed&&player.getUUID().equals(entry.owner)){
            Entity entity=MotherOfStrays.restorePetEntity(player.serverLevel(),entry);
            if(!(entity instanceof TamableAnimal pet))continue;
            pet.removeTag(MotherOfStrays.PET);pet.setOwnerUUID(null);pet.setTame(true,false);pet.setOrderedToSit(false);pet.setInSittingPose(false);
            pet.setNoAi(false);pet.setInvulnerable(false);pet.setHealth(pet.getMaxHealth());pet.setPersistenceRequired();pet.getPersistentData().putBoolean(GUIDE,true);
            pet.moveTo(Vec3.atBottomCenterOf(FinaleArchitecture.bottomStart(origin).south(2)));installGuideGoal(pet);player.serverLevel().addFreshEntity(pet);
            record.putUUID("Guide",pet.getUUID());record.putUUID("GuideEntry",entry.id);record.putBoolean("Guided",true);
            words(player,pet.blockPosition().above(),"You know that collar.");return;
        }
    }
    private static void tickGuide(ServerPlayer player,BlockPos origin,CompoundTag record){
        if(!record.hasUUID("Guide"))return;
        List<BlockPos> path=FinaleArchitecture.escapeRoute(origin);int step=Math.min(path.size()-1,record.getInt("GuideStep"));
        player.serverLevel().getChunkAt(path.get(step));
        Entity entity=player.serverLevel().getEntity(record.getUUID("Guide"));if(!(entity instanceof TamableAnimal pet))return;
        if(!pet.isAlive()){record.putBoolean("Guided",false);return;}installGuideGoal(pet);
        if(pet.position().distanceToSqr(Vec3.atBottomCenterOf(path.get(step)))<2&&step<path.size()-1){step++;record.putInt("GuideStep",step);}
        if(pet.distanceToSqr(player)>100){pet.getNavigation().stop();pet.getLookControl().setLookAt(player);return;}
        BlockPos next=path.get(step);if(player.tickCount%5==0)pet.getNavigation().moveTo(next.getX()+.5,next.getY(),next.getZ()+.5,.8);
    }
    private static void ensureWitness(ServerLevel level,@Nullable BlockPos origin){
        if(origin==null)return;BlockPos at=FinaleArchitecture.base(origin).offset(-12,FinaleArchitecture.ARENA,43);
        if(!level.getEntitiesOfClass(FinaleWitness.class,new AABB(at).inflate(5)).isEmpty())return;
        FinaleWitness witness=FinaleRegistry.WITNESS.get().create(level);if(witness!=null){witness.moveTo(Vec3.atBottomCenterOf(at));level.addFreshEntity(witness);}
    }
    public static void inspectWeapon(ServerPlayer player,FinaleWitness witness){
        LabyrinthData data=LabyrinthData.get(player.server);
        if(player.isShiftKeyDown()&&WitnessAccount.count(data,player.getUUID())>0){
            WitnessAccount.updateBook(player,true);words(player,witness.blockPosition().above(2),WitnessAccount.ready(data,player.getUUID())
                    ?"The play has another passage. Read it before you decide what to carry through that door."
                    :"You have written part of it. Other rooms have endings you have not heard yet.");return;
        }
        UUID original=WeaponHistory.favorite(player);
        if(original==null){words(player,witness.blockPosition().above(2),"Bring the weapon your hand remembers. The shield alone will not finish it.");return;}
        ItemStack sample=WeaponHistory.sample(player,original);String name=sample.getHoverName().getString();
        boolean kept=MotherCollection.get(player.server).all().stream().filter(e->!e.pet).anyMatch(e->original.equals(WeaponHistory.identity(e.item(player.registryAccess()))));
        String answer=WeaponHistory.wounds(player.getMainHandItem(),original)?"That is the one. Face its rush with the shield. Strike when it stops.":kept?"Your "+name+" is on her shelf. Recover it before opening that cell.":"The "+name+" you used most. Bring the original. The House can copy an edge, not its history.";
        words(player,witness.blockPosition().above(2),answer);
    }
    public static void words(ServerPlayer player,BlockPos pos,String text){
        long now=player.serverLevel().getGameTime();if(now-LAST_WORDS.getOrDefault(player.getUUID(),-100L)<30)return;LAST_WORDS.put(player.getUUID(),now);
        Display.TextDisplay display=EntityType.TEXT_DISPLAY.create(player.serverLevel());if(display==null)return;
        CompoundTag tag=new CompoundTag();display.saveWithoutId(tag);tag.putString("text",Component.Serializer.toJson(Component.literal(text),player.registryAccess()));
        tag.putString("billboard","center");tag.putInt("line_width",240);tag.putInt("background",0x44000000);tag.putBoolean("see_through",false);
        display.load(tag);display.moveTo(pos.getX()+.5,pos.getY()+.4,pos.getZ()+.5);display.getPersistentData().putLong("FinaleWordsUntil",now+140);display.addTag("HouseFinaleWords");player.serverLevel().addFreshEntity(display);
    }
    @SubscribeEvent(priority=EventPriority.HIGHEST) public static void death(LivingDeathEvent event){
        if(!(event.getEntity() instanceof ServerPlayer player)||!FinaleProgress.committed(FinaleProgress.phase(player.server,player.getUUID())))return;
        CompoundTag record=FinaleProgress.player(player.server,player.getUUID());record.putString("Phase",FinaleProgress.Phase.LOCKED_OUT.name());record.putBoolean("NeedsRespawn",true);
        WitnessEnding.keepWeaponOnDeath(player,record);
        for(int i=0;i<player.getInventory().getContainerSize();i++)keep(player,player.getInventory().removeItemNoUpdate(i));
        keep(player,player.containerMenu.getCarried());player.containerMenu.setCarried(ItemStack.EMPTY);
        java.util.List<TamableAnimal> keptPets=new java.util.ArrayList<>();
        for(Entity entity:player.serverLevel().getAllEntities())if(entity instanceof TamableAnimal pet&&pet.isAlive()&&player.getUUID().equals(CompanionOrders.owner(pet)))keptPets.add(pet);
        for(var pet:keptPets){
            CompoundTag contents=new CompoundTag();pet.saveWithoutId(contents);
            contents.putString("id",net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(pet.getType()).toString());
            var entry=MotherCollection.get(player.server).keepPet(pet.getUUID(),contents,player.getUUID(),pet.getName().getString());
            if(entry!=null){entry.sealed=true;MotherCollection.get(player.server).setDirty();}pet.discard();
        }
        FinaleProgress.save(player.server,player.getUUID(),record);LabyrinthData.get(player.server).clearReturns(player.getUUID());
        if(record.hasUUID("Creature")&&player.serverLevel().getEntity(record.getUUID("Creature")) instanceof MinotaurEntity creature&&creature.motion()!=MinotaurEntity.WOUNDED)creature.discard();
        CompoundTag world=FinaleProgress.world(player.server);world.remove("Owner");LabyrinthData.get(player.server).setState(FinaleProgress.STATE,world);
        BlockPos origin=HouseSavedData.get(player.server).houseOrigin();if(origin!=null){
            FinaleArchitecture.seal(player.serverLevel(),origin,false);FinaleArchitecture.closeCell(player.serverLevel(),origin);
        }
    }
    private static void keep(ServerPlayer player,ItemStack stack){if(!stack.isEmpty())MotherCollection.get(player.server).keepFinaleItem(stack,player.registryAccess(),player.getUUID(),player.serverLevel().getGameTime());}
    @SubscribeEvent(priority=EventPriority.HIGHEST) public static void drops(LivingDropsEvent event){
        if(event.getEntity() instanceof ServerPlayer player&&FinaleProgress.player(player.server,player.getUUID()).getBoolean("NeedsRespawn")){
            for(ItemEntity item:event.getDrops())keep(player,item.getItem());event.getDrops().clear();
        }
    }
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void respawnPosition(PlayerRespawnPositionEvent event){
        if(event.getEntity() instanceof ServerPlayer player&&!event.isFromEndFight()&&FinaleProgress.player(player.server,player.getUUID()).getBoolean("NeedsRespawn")){
            BlockPos origin=HouseSavedData.get(player.server).housePosition().orElse(null);if(origin==null)return;
            Vec3 at=HouseProxyEntityEvacuation.frontDoorExit(player.server.overworld(),origin);if(at==null)return;player.server.overworld().getChunkAt(BlockPos.containing(at));
            event.setDimensionTransition(new DimensionTransition(player.server.overworld(),at,Vec3.ZERO,180,0,DimensionTransition.DO_NOTHING));
        }
    }
    @SubscribeEvent public static void respawned(PlayerEvent.PlayerRespawnEvent event){
        if(event.getEntity() instanceof ServerPlayer player){CompoundTag record=FinaleProgress.player(player.server,player.getUUID());if(record.getBoolean("NeedsRespawn")){
            player.getInventory().clearContent();record.remove("NeedsRespawn");FinaleProgress.save(player.server,player.getUUID(),record);}}
    }
    @SubscribeEvent public static void projectile(EntityJoinLevelEvent event){
        if(!event.getLevel().isClientSide&&event.getEntity() instanceof TamableAnimal pet&&pet.getPersistentData().getBoolean(GUIDE))installGuideGoal(pet);
        if(event.getLevel().isClientSide||!(event.getEntity() instanceof Projectile projectile)||!(projectile.getOwner() instanceof ServerPlayer player))return;
        ItemStack used=player.getUseItem();if(!WeaponHistory.weapon(used))used=WeaponHistory.weapon(player.getMainHandItem())?player.getMainHandItem():player.getOffhandItem();
        UUID original=WeaponHistory.stamp(used);if(original!=null)projectile.getPersistentData().putUUID(WeaponHistory.ORIGINAL,original);
    }
    private static void outside(ServerPlayer player,BlockPos origin){
        Vec3 at=HouseProxyEntityEvacuation.frontDoorExit(player.server.overworld(),origin);if(at!=null)outside(player,at);
    }
    private static void outside(ServerPlayer player,Vec3 at){
        var companions=CompanionOrders.followingAll(player);
        player.server.overworld().getChunkAt(BlockPos.containing(at));player.stopRiding();player.teleportTo(player.server.overworld(),at.x,at.y,at.z,180,0);player.resetFallDistance();
        for(var pet:companions)CompanionOrders.followAcross(pet,player);
    }
    public static void finish(ServerPlayer player,boolean guided){
        BlockPos origin=HouseSavedData.get(player.server).houseOrigin();if(origin==null)return;
        // Player edits can obstruct the exterior. Keep the ending retryable until every resident can land safely.
        Vec3 landing=HouseProxyEntityEvacuation.frontDoorExit(player.server.overworld(),origin);if(landing==null)return;
        CompoundTag record=FinaleProgress.player(player.server,player.getUUID());
        if(record.hasUUID("Guide")&&player.serverLevel().getEntity(record.getUUID("Guide")) instanceof TamableAnimal pet){
            pet.getPersistentData().remove(GUIDE);pet.setOwnerUUID(player.getUUID());pet.setTame(true,false);pet.setInvulnerable(false);
            if(record.hasUUID("GuideEntry"))MotherCollection.get(player.server).releaseFinalePet(record.getUUID("GuideEntry"));
            // Transfer after the owner reaches the overworld; the guide has been waiting here.
            record.putUUID("RecoveredGuide",pet.getUUID());
        }
        record.putString("Phase",FinaleProgress.Phase.ESCAPED.name());record.putBoolean("Guided",guided);record.putLong("EpilogueDue",player.server.overworld().getGameTime()+24000);
        FinaleProgress.save(player.server,player.getUUID(),record);player.removeEffect(MobEffects.DARKNESS);
        // Evacuate every resident before disabling the House's origin; no peer can be stranded.
        for(ServerPlayer other:player.server.getPlayerList().getPlayers())if(HouseDimensions.isHouseDimension(other.serverLevel().dimension())){HouseTransitionEvents.cancelPending(other,"the House is collapsing");outside(other,landing);}
        if(!player.serverLevel().dimension().equals(Level.OVERWORLD))outside(player,landing);
        if(record.hasUUID("RecoveredGuide")){
            ServerLevel interior=player.server.getLevel(HouseDimensions.INTERIOR);
            if(interior!=null&&interior.getEntity(record.getUUID("RecoveredGuide")) instanceof TamableAnimal pet)CompanionOrders.followAcross(pet,player);
        }
        if(!guided){player.server.overworld().setDayTime(player.server.overworld().getDayTime()+60*24000L);overgrowBase(player,record);}
        CompoundTag world=FinaleProgress.world(player.server);world.putBoolean("Ended",true);world.putLong("CollapsedOrigin",origin.asLong());world.putInt("Demolition",0);world.remove("Owner");
        LabyrinthData.get(player.server).setState(FinaleProgress.STATE,world);HouseSavedData.get(player.server).collapse();
        io.github.knaitoe.theoldesthouse.network.HousePackets.sendToAll(player.server,new io.github.knaitoe.theoldesthouse.network.HouseSightlineStatePayload(BlockPos.ZERO,false));
        io.github.knaitoe.theoldesthouse.network.HousePackets.sendToAll(player.server,HouseBetweenRoom.doorPayload(HouseSavedData.get(player.server)));
        words(player,player.blockPosition().above(2),guided?"An empty lot. The collar is still warm.":"An empty lot. Sixty mornings have passed.");
    }
    @SubscribeEvent public static void tick(ServerTickEvent.Post event){
        MinecraftServer server=event.getServer();FinaleArchitecture.tick(server);
        List<Entity> expiredWords=new ArrayList<>();for(ServerLevel level:server.getAllLevels())if(level.getGameTime()%20==0)
            for(Entity entity:level.getAllEntities())if(entity!=null&&entity.getTags().contains("HouseFinaleWords")&&level.getGameTime()>=entity.getPersistentData().getLong("FinaleWordsUntil"))expiredWords.add(entity);
        expiredWords.forEach(Entity::discard);
        CompoundTag world=FinaleProgress.world(server);boolean changed=false;ServerLevel interior=server.getLevel(HouseDimensions.INTERIOR);
        BlockPos activeOrigin=HouseSavedData.get(server).houseOrigin();
        if(world.getBoolean("CellReleased")&&!world.hasUUID("Owner")&&interior!=null&&activeOrigin!=null
                &&server.getPlayerList().getPlayers().stream().noneMatch(p->p.serverLevel()==interior&&FinaleArchitecture.contains(activeOrigin,p.blockPosition()))){
            FinaleArchitecture.closeCell(interior,activeOrigin);world.remove("CellReleased");changed=true;
        }
        for(String key:new ArrayList<>(world.getAllKeys())){CompoundTag record=world.getCompound(key);
            if(record.contains("Light")&&interior!=null&&interior.getGameTime()>=record.getLong("LightUntil")){
                BlockPos light=BlockPos.of(record.getLong("Light"));if(interior.getBlockState(light).is(Blocks.LIGHT))interior.setBlock(light,Blocks.AIR.defaultBlockState(),FLAGS);
                record.remove("Light");world.put(key,record);changed=true;
            }
            if((FinaleProgress.phase(record)==FinaleProgress.Phase.ESCAPED||FinaleProgress.phase(record)==FinaleProgress.Phase.WITNESSED)&&!record.getBoolean("EpilogueDelivered")&&server.overworld().getGameTime()>=record.getLong("EpilogueDue")){
                UUID id;try{id=UUID.fromString(key);}catch(IllegalArgumentException ignored){continue;}
                ServerPlayer player=server.getPlayerList().getPlayer(id);if(player!=null&&epilogue(player,record)){record.putBoolean("EpilogueDelivered",true);world.put(key,record);changed=true;}
            }
        }
        if(world.getBoolean("Ended")&&world.contains("CollapsedOrigin")){
            BlockPos origin=BlockPos.of(world.getLong("CollapsedOrigin"));int cursor=world.getInt("Demolition"),nx=HouseLayout.MAX_X-HouseLayout.MIN_X+1,nz=HouseLayout.MAX_Z-HouseLayout.MIN_Z+1;
            int total=nx*nz*(HouseLayout.MAX_Y-HouseLayout.MIN_Y+1),end=Math.min(total,cursor+1536);
            for(;cursor<end;cursor++){int x=HouseLayout.MIN_X+cursor%nx,z=HouseLayout.MIN_Z+(cursor/nx)%nz,y=HouseLayout.MIN_Y+cursor/(nx*nz);BlockPos pos=origin.offset(x,y,z);
                var replacement=y<0?Blocks.DIRT.defaultBlockState():y==0?Blocks.GRASS_BLOCK.defaultBlockState():Blocks.AIR.defaultBlockState();server.overworld().setBlock(pos,replacement,FLAGS);
                if(interior!=null)interior.setBlock(pos,Blocks.AIR.defaultBlockState(),FLAGS);}
            if(cursor!=world.getInt("Demolition")){world.putInt("Demolition",cursor);changed=true;}
        }
        if(changed)LabyrinthData.get(server).setState(FinaleProgress.STATE,world);
    }
    private static boolean epilogue(ServerPlayer player,CompoundTag record){
        ServerLevel level=player.server.overworld();BlockPos home=record.contains("Base")&&record.getString("BaseDimension").equals(Level.OVERWORLD.location().toString())?BlockPos.of(record.getLong("Base")):player.blockPosition();
        level.getChunkAt(home);
        for(BlockPos candidate:BlockPos.betweenClosed(home.offset(-4,0,-4),home.offset(4,2,4)))if(level.getBlockState(candidate).isAir()&&level.getBlockState(candidate.below()).isSolidRender(level,candidate.below())){
            level.setBlock(candidate,Blocks.CHEST.defaultBlockState(),FLAGS);
            if(level.getBlockEntity(candidate) instanceof net.minecraft.world.level.block.entity.ChestBlockEntity chest){
                if(FinaleProgress.phase(record)==FinaleProgress.Phase.WITNESSED){chest.setItem(0,WitnessEnding.epilogue(player));chest.setChanged();return true;}
                chest.setItem(0,HouseWriting.book("Loose pages","?",HouseWriting.WritingStyle.WILL,List.of("I have been reading your account.\n\nThis page was not there when you left. Neither was the handwriting beneath it.")));
                String unseen=Arrays.stream(LabyrinthPlace.values()).filter(p->p.isVignette()&&!LabyrinthData.get(player.server).visited(player.getUUID()).contains(p.id())).map(LabyrinthPlace::id).findFirst().orElse("an unmeasured room");
                chest.setItem(1,HouseWriting.book("Unnumbered page","?",HouseWriting.WritingStyle.ZAMPANO,List.of("A room outside the survey: "+unseen.replace('_',' ')+".\n\nThe inventory is incomplete. It always was.")));chest.setChanged();return true;
            }
        }
        return false;
    }
    private static void overgrowBase(ServerPlayer player,CompoundTag record){
        if(!record.contains("Base")||!record.getString("BaseDimension").equals(Level.OVERWORLD.location().toString()))return;
        ServerLevel level=player.server.overworld();BlockPos home=BlockPos.of(record.getLong("Base"));
        // Age only crops and add plants to vacant, supported cells; built blocks stay intact.
        for(BlockPos p:BlockPos.betweenClosed(home.offset(-8,-3,-8),home.offset(8,5,8))){
            var state=level.getBlockState(p);
            if(state.getBlock() instanceof CropBlock crop){level.setBlock(p,crop.getStateForAge(crop.getMaxAge()),FLAGS);continue;}
            if(!state.isAir()||Math.floorMod(p.asLong(),11)!=0)continue;
            if(level.canSeeSky(p)&&level.getBlockState(p.below()).is(Blocks.GRASS_BLOCK))level.setBlock(p,Blocks.SHORT_GRASS.defaultBlockState(),FLAGS);
            else if(level.canSeeSky(p))for(Direction direction:Direction.Plane.HORIZONTAL){
                BlockPos support=p.relative(direction);if(!level.getBlockState(support).isFaceSturdy(level,support,direction.getOpposite()))continue;
                var vine=Blocks.VINE.defaultBlockState().setValue(VineBlock.getPropertyForFace(direction),true);
                if(vine.canSurvive(level,p))level.setBlock(p,vine,FLAGS);break;
            }
        }
    }
    private static void installGuideGoal(TamableAnimal pet){
        if(!GUIDE_GOALS.add(pet))return;
        pet.goalSelector.addGoal(0,new net.minecraft.world.entity.ai.goal.Goal(){
            {setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK));}
            @Override public boolean canUse(){return pet.getPersistentData().getBoolean(GUIDE);}
            @Override public boolean canContinueToUse(){return canUse();}
        });
    }
    public static void clearAll(){LAST_WORDS.clear();MISSING_CREATURES.clear();GUIDE_GOALS.clear();FinaleArchitecture.clearAll();FinaleCollapse.clearAll();}
}

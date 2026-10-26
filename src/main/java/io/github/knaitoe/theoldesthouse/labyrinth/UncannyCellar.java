package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.*;
import net.minecraft.server.level.*;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;

/** An occupied cellar grows a few real cells at a time; cut tissue keeps a saved regrowth deadline. */
public final class UncannyCellar {
    private static final LabyrinthPlace PLACE=LabyrinthPlace.LAYERED_MAN;
    private static final int F=UncannyRooms.FLAGS;
    private UncannyCellar(){}
    public static boolean authored(int x,int y,int z){
        if(x==0&&y==UncannyRooms.KEY_CAVITY.getY()&&z==UncannyRooms.KEY_CAVITY.getZ())return false;
        // Reclined, swollen human anatomy: distinct feet and shins, abdomen,
        // shoulders, two lowered arms and a face above the ribs. The original
        // key cavity stays inside his abdomen, approached through real tissue.
        boolean legs=y>=-5&&y<=-4&&Math.abs(x)==1&&z>=-20&&z<=-17;
        boolean belly=y>=-5&&y<=-3&&Math.abs(x)<=2&&z>=-23&&z<=-19&&!(x==0&&y==-3&&z>=-20);
        boolean shoulders=y>=-4&&y<=-3&&Math.abs(x)==2&&z>=-24&&z<=-21;
        boolean arms=Math.abs(x)==3&&y>=-5&&y<=-3&&z>=-23&&z<=-20;
        boolean head=Math.abs(x)<=1&&y>=-3&&y<=-2&&z>=-23&&z<=-21;
        boolean nose=x==0&&y==-2&&z==-20;
        return legs||belly||shoulders||arms||head||nose;
    }
    private static boolean inMass(BlockPos r){return Math.abs(r.getX())<=6&&r.getY()>=-5&&r.getY()<=-2&&r.getZ()>=-28&&r.getZ()<=-10;}
    private static boolean protectedCell(BlockPos r){return r.equals(UncannyRooms.KEY_CAVITY)||Math.abs(r.getX())<=1&&r.getZ()>=-12||r.getX()==-6&&r.getZ()==-10;}
    public static void cut(ServerLevel l,BlockPos b,BlockPos r,CompoundTag shared){
        var queue=shared.getList("Regrow",Tag.TAG_COMPOUND);
        for(var t:queue)if(((CompoundTag)t).getLong("At")==r.asLong())return;
        if(queue.size()<320){var entry=new CompoundTag();entry.putLong("At",r.asLong());entry.putLong("Due",l.getGameTime()+360);queue.add(entry);shared.put("Regrow",queue);}
    }
    private static boolean body(ServerLevel l,BlockPos at){return !l.getEntitiesOfClass(LivingEntity.class,new AABB(at).inflate(.04),LivingEntity::isAlive).isEmpty();}
    private static boolean neighbor(ServerLevel l,BlockPos at){for(var d:Direction.values())if(UncannyFleshBlock.is(l.getBlockState(at.relative(d))))return true;return false;}
    public static boolean remains(ServerLevel l,BlockPos b){for(int x=-6;x<=6;x++)for(int y=-5;y<=-2;y++)for(int z=-28;z<=-10;z++)if(UncannyFleshBlock.is(l.getBlockState(b.offset(x,y,z))))return true;return false;}
    private static boolean restingCut(CompoundTag shared,BlockPos r,long now){for(var t:shared.getList("Regrow",Tag.TAG_COMPOUND)){var e=(CompoundTag)t;if(e.getLong("At")==r.asLong()&&e.getLong("Due")>now)return true;}return false;}
    private static void grow(ServerLevel l,BlockPos at,BlockPos r){if(l.setBlock(at,UncannyFleshBlock.at(r),F))l.playSound(null,at,UncannyRegistry.FLESH_GROW.get(),net.minecraft.sounds.SoundSource.BLOCKS,.7F,.85F+l.random.nextFloat()*.2F);}
    private static void regrow(ServerLevel l,BlockPos b,CompoundTag shared){
        var queue=shared.getList("Regrow",Tag.TAG_COMPOUND);int changed=0;
        if(!shared.getBoolean("RegrowthBudget130")){for(var row:queue){var e=(CompoundTag)row;e.putLong("Due",Math.max(e.getLong("Due"),l.getGameTime()+360));}shared.putBoolean("RegrowthBudget130",true);}
        if(l.getGameTime()%100!=0)return;
        for(int i=queue.size()-1;i>=0&&changed<1;i--){var e=queue.getCompound(i);if(e.getLong("Due")>l.getGameTime())continue;var r=BlockPos.of(e.getLong("At"));var at=b.offset(r);
            if(!inMass(r)||protectedCell(r)){queue.remove(i);continue;}
            if(body(l,at)){e.putLong("Due",l.getGameTime()+30);continue;}
            if(l.isEmptyBlock(at)&&neighbor(l,at)){grow(l,at,r);changed++;}
            else if(l.isEmptyBlock(at))continue;
            queue.remove(i);
        }
        shared.put("Regrow",queue);
    }
    private static void spread(ServerLevel l,BlockPos b,CompoundTag shared,int age){
        boolean worked=shared.contains("LastPlayerPeel")&&l.getGameTime()-shared.getLong("LastPlayerPeel")<240;
        if(age%(worked?180:age<2400?100:60)!=0)return;
        var candidates=new ArrayList<BlockPos>();
        for(int x=-6;x<=6;x++)for(int y=-5;y<=-2;y++)for(int z=-28;z<=-10;z++){
            var r=new BlockPos(x,y,z);var at=b.offset(r);if(protectedCell(r)||!l.isEmptyBlock(at)||restingCut(shared,r,l.getGameTime()))continue;
            for(var d:Direction.values())if(UncannyFleshBlock.is(l.getBlockState(at.relative(d)))){if(!body(l,at))candidates.add(r);break;}
        }
        // The frontier favours the stairs but grows along floors/walls too. No volume is filled in one tick.
        candidates.sort(Comparator.comparingInt(r->Math.floorMod((r.asLong()^age*7919L)*31,997)));
        int limit=1;
        for(int i=0;i<Math.min(limit,candidates.size());i++){var r=candidates.get(i);grow(l,b.offset(r),r);shared.putInt("Layers",shared.getInt("Layers")+1);}
        if(!candidates.isEmpty()){shared.putInt("Pulse",shared.getInt("Pulse")+1);shared.putInt("Growth",Math.min(9,1+shared.getInt("Layers")/15));}
    }
    /** Actual tissue reaching the cellar's inner wall/approach is a lost containment attempt. */
    private static void containment(ServerLevel l,BlockPos b,CompoundTag shared){
        if(shared.getBoolean("ContainmentFailed127"))return;
        for(int x=-6;x<=6;x++)for(int z=-28;z<=-13;z++){
            if(Math.abs(x)!=6&&z!=-28&&z!=-13)continue;
            for(int y=-5;y<=-2;y++)if(UncannyFleshBlock.is(l.getBlockState(b.offset(x,y,z)))){
                shared.putBoolean("ContainmentFailed127",true);shared.putLong("ContainmentAt127",l.getGameTime());return;
            }
        }
    }
    private static void lamps(ServerLevel l,BlockPos b,int age){
        boolean brownout=age%190<10||age%430>=395&&age%430<415||age>1200&&age%275>=255;
        for(var r:List.of(new BlockPos(-3,-2,-14),new BlockPos(3,-2,-14),new BlockPos(3,-2,-25))){var at=b.offset(r);var s=l.getBlockState(at);
            if(s.is(UncannyRegistry.FURNITURE.get())&&s.getValue(UncannyFurnitureBlock.KIND)==UncannyFurnitureBlock.Kind.FLUORESCENT&&s.getValue(UncannyFurnitureBlock.OPEN)!=brownout)l.setBlock(at,s.setValue(UncannyFurnitureBlock.OPEN,brownout),F);
        }
    }
    private static ItemStack key(ItemStack old){
        var s=new ItemStack(UncannyRegistry.CELLAR_KEY.get(),old.getCount());s.applyComponents(old.getComponentsPatch());return s;
    }
    private static boolean ownedKey(ServerPlayer p,ItemStack s){var c=s.get(DataComponents.CUSTOM_DATA);return c!=null&&c.copyTag().hasUUID("UncannyKeyOwner")&&c.copyTag().getUUID("UncannyKeyOwner").equals(p.getUUID());}
    public static boolean carriesKey(ServerPlayer p){for(int i=0;i<p.getInventory().getContainerSize();i++)if(ownedKey(p,p.getInventory().getItem(i)))return true;return false;}
    public static boolean askToLeave(ServerPlayer p,BlockPos b,CompoundTag own){
        if(b==null||!carriesKey(p))return false;
        var shared=UncannyStories.world(p.serverLevel(),b,PLACE);if(shared.getBoolean("Rescued"))return false;
        if(shared.hasUUID("EscortOwner")&&!shared.getUUID("EscortOwner").equals(p.getUUID())){
            var other=p.server.getPlayerList().getPlayer(shared.getUUID("EscortOwner"));if(other!=null&&LiteraryVignettes.inside(other,PLACE)&&!UncannyStories.failed(LiteraryVignettes.personal(LabyrinthData.get(p.server),other.getUUID(),PLACE)))return false;
        }
        shared.putUUID("EscortOwner",p.getUUID());own.putBoolean("Escorting",true);UncannyStories.world(p.serverLevel(),b,PLACE,shared);
        p.displayClientMessage(net.minecraft.network.chat.Component.literal("“I can walk. Keep the way clear.”"),false);return true;
    }
    /** The original worker follows supported floor and the real stairs. No flag substitutes for his arrival. */
    private static void escort(ServerLevel l,BlockPos b,CompoundTag shared,LiteraryActor worker){
        if(worker==null||!shared.hasUUID("EscortOwner")||shared.getBoolean("Rescued"))return;
        var owner=l.getServer().getPlayerList().getPlayer(shared.getUUID("EscortOwner"));
        if(owner==null||!LiteraryVignettes.inside(owner,PLACE)||UncannyStories.failed(LiteraryVignettes.personal(LabyrinthData.get(l.getServer()),owner.getUUID(),PLACE)))return;
        var points=List.of(new Vec3(-2.5,-5,-13.5),new Vec3(.5,-5,-11.5),new Vec3(.5,0,-4.5),new Vec3(-2.5,0,-3.5));
        int leg=Math.min(points.size()-1,shared.getInt("EscortLeg"));var target=points.get(leg).add(b.getX(),b.getY(),b.getZ());
        worker.appearance(LiteraryActor.UNCANNY_PEELER,3);worker.gesture(0);if(SceneFootpaths.step(worker,target,.28)){
            if(leg<points.size()-1)shared.putInt("EscortLeg",leg+1);
            else if(worker.getY()>=b.getY()-.1&&worker.getZ()>b.getZ()-5){shared.putBoolean("Rescued",true);shared.putUUID("RescuedBy",owner.getUUID());shared.remove("EscortOwner");}
        }
    }
    /** Completed readers can carry an old original away from the cellar; this is independent of story ticking. */
    public static void migrateKeys(ServerPlayer p){
        for(int i=0;i<p.getInventory().getContainerSize();i++){var s=p.getInventory().getItem(i);if(s.is(LiteraryRegistry.KEY.get())&&ownedKey(p,s))p.getInventory().setItem(i,key(s));
            if(s.is(UncannyRegistry.CLEAVER.get())){s.set(DataComponents.UNBREAKABLE,new net.minecraft.world.item.component.Unbreakable(false));if(s.getDamageValue()!=0)s.setDamageValue(0);}
        }
    }
    private static void contact(ServerPlayer p,BlockPos b,CompoundTag own){
        int pressure=own.getInt("Engulfment");boolean touching=false;var box=p.getBoundingBox().inflate(.14,.06,.14);
        if(p.getY()<b.getY()-1)for(var at:BlockPos.betweenClosed(BlockPos.containing(box.minX,box.minY,box.minZ),BlockPos.containing(box.maxX,box.maxY,box.maxZ)))if(UncannyFleshBlock.is(p.serverLevel().getBlockState(at))){touching=true;break;}
        pressure=Math.max(0,Math.min(100,pressure+(touching?2:-2)));own.putInt("Engulfment",pressure);
        if(pressure>0)p.addEffect(new MobEffectInstance(UncannyRegistry.ENGULFED,40,Math.min(3,pressure/25),false,true,true));else p.removeEffect(UncannyRegistry.ENGULFED);
        if(pressure>=100){p.removeEffect(UncannyRegistry.ENGULFED);own.putInt("Engulfment",0);UncannyStories.caught(p,b,PLACE,own,"overtaken_by_the_cellar");}
    }
    public static void arrive(ServerPlayer p,BlockPos b){
        var l=p.serverLevel();if(!UncannyStories.actorsLoaded(l,b,PLACE))return;var shared=UncannyStories.world(l,b,PLACE);long now=l.getGameTime();
        // Departure never restores tissue or resets cutting/eradication history.
        shared.putLong("PresentAt",now);UncannyStories.world(l,b,PLACE,shared);
    }
    public static CompoundTag physical(ServerPlayer p,BlockPos b,CompoundTag own){
        var l=p.serverLevel();if(!UncannyStories.actorsLoaded(l,b,PLACE))return null;long now=l.getGameTime();var shared=UncannyStories.world(l,b,PLACE);
        UncannyStories.prepare(l,b,PLACE);shared=UncannyStories.world(l,b,PLACE);
        if(shared.getLong("Tick")!=now){
            shared.putLong("Tick",now);int age=shared.getInt("Age")+5;shared.putInt("Age",age);int stage=age<400?0:age<800?1:2;
            var worker=shared.hasUUID("Peeler")&&l.getEntity(shared.getUUID("Peeler")) instanceof LiteraryActor a?a:null;
            if(worker!=null&&!shared.getBoolean("Rescued")&&!shared.hasUUID("EscortOwner")){
                worker.appearance(LiteraryActor.UNCANNY_PEELER,stage);worker.gesture(1);
                if(stage>=1)worker.setItemSlot(EquipmentSlot.MAINHAND,ItemStack.EMPTY);
                CellarWorker.mine(l,b,shared,worker);
                if(age%20==0)l.sendParticles(ParticleTypes.DRIPPING_WATER,worker.getX(),worker.getY()+1.5,worker.getZ(),1,.12,.05,.12,0);
            }
            escort(l,b,shared,worker);
            if(!remains(l,b)){shared.putBoolean("Eradicated131",true);shared.remove("Regrow");shared.remove("ContainmentFailed127");shared.remove("ContainmentAt127");}
            if(!shared.getBoolean("Eradicated131")){regrow(l,b,shared);spread(l,b,shared,age);containment(l,b,shared);}
            lamps(l,b,age);UncannyStories.world(l,b,PLACE,shared);
        }
        CellarDialogue.actions(p,b,own,shared);return shared;
    }
    public static void tick(ServerPlayer p,BlockPos b,CompoundTag own){
        var l=p.serverLevel();var shared=physical(p,b,own);if(shared==null)return;
        if(shared.getBoolean("ContainmentFailed127")){
            p.removeEffect(UncannyRegistry.ENGULFED);own.putInt("Engulfment",0);
            UncannyStories.caught(p,b,PLACE,own,"basement_overrun");return;
        }
        own.putInt("GrowthCue",shared.getInt("Pulse"));
        CellarDialogue.tick(p,b,own,shared);
        int age=shared.getInt("Age");
        if(age%(age>=400?60:100)==0){var worker=shared.hasUUID("Peeler")&&l.getEntity(shared.getUUID("Peeler")) instanceof LiteraryActor a?a:null;if(worker!=null)UncannyStories.heard(p,NovelRegistry.WELL_BREATH.get(),worker.getEyePosition(),age<400?.28F:.5F,.72F);}
        if(own.getInt("LastGrowth")!=shared.getInt("Pulse")){own.putInt("LastGrowth",shared.getInt("Pulse"));if(age%80==0)UncannyStories.heard(p,net.minecraft.sounds.SoundEvents.WARDEN_HEARTBEAT,Vec3.atCenterOf(b.offset(UncannyRooms.MASS)),.28F,.55F);}
        boolean exposed=true;for(int z=-18;z>=UncannyRooms.KEY_CAVITY.getZ();z--)if(!l.isEmptyBlock(b.offset(0,-4,z))){exposed=false;break;}
        if(!own.getBoolean("KeyReleased")&&exposed){
            if(!own.contains("OriginalKey")){var original=new ItemStack(UncannyRegistry.CELLAR_KEY.get());CustomData.update(DataComponents.CUSTOM_DATA,original,t->{t.putUUID("UncannyKeyOwner",p.getUUID());t.putUUID("UncannyKey",UUID.randomUUID());});own.put("OriginalKey",original.save(p.registryAccess()));}
            var item=key(ItemStack.parseOptional(p.registryAccess(),own.getCompound("OriginalKey")));var at=Vec3.atCenterOf(b.offset(UncannyRooms.KEY_CAVITY));var e=new ItemEntity(l,at.x,at.y,at.z,item);e.setTarget(p.getUUID());
            if(l.addFreshEntity(e)){own.remove("OriginalKey");own.putBoolean("KeyReleased",true);own.putUUID("KeyEntity",e.getUUID());}
        }
        boolean hasKey=false;for(int i=0;i<p.getInventory().getContainerSize();i++){var s=p.getInventory().getItem(i);if(ownedKey(p,s)){hasKey=true;if(!s.is(UncannyRegistry.CELLAR_KEY.get()))p.getInventory().setItem(i,key(s));}}
        if(own.hasUUID("KeyEntity")&&l.getEntity(own.getUUID("KeyEntity")) instanceof ItemEntity e&&ownedKey(p,e.getItem())&&!e.getItem().is(UncannyRegistry.CELLAR_KEY.get()))e.setItem(key(e.getItem()));
        if(hasKey){
            own.putBoolean("Helped",true);boolean rescued=own.getBoolean("Escorting")&&shared.getBoolean("Rescued")&&shared.hasUUID("RescuedBy")&&shared.getUUID("RescuedBy").equals(p.getUUID());own.putBoolean("WorkerRescued",rescued);own.putBoolean("WorkerAlreadySafe",shared.getBoolean("Rescued")&&!rescued);
            UncannyStories.ready(p,PLACE,own,"retrieved_the_cellar_key");return;
        }contact(p,b,own);
    }
}

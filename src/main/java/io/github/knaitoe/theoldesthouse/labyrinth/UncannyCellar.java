package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
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
        if(y==-2)return Math.abs(x)<=1&&z>=-22&&z<=-20;
        if(y==-3)return Math.abs(x)<=1&&z>=-23&&z<=-19;
        return y>=-5&&y<=-4&&Math.abs(x)<=2&&z>=-23&&z<=-18&&(Math.abs(x)<2||z>=-22&&z<=-19);
    }
    private static boolean inMass(BlockPos r){return Math.abs(r.getX())<=6&&r.getY()>=-5&&r.getY()<=-2&&r.getZ()>=-28&&r.getZ()<=-10;}
    private static boolean protectedCell(BlockPos r){return r.equals(UncannyRooms.KEY_CAVITY)||Math.abs(r.getX())<=1&&r.getZ()>=-12||r.getX()==-6&&r.getZ()==-10;}
    public static void cut(ServerLevel l,BlockPos b,BlockPos r,CompoundTag shared){
        var queue=shared.getList("Regrow",Tag.TAG_COMPOUND);
        for(var t:queue)if(((CompoundTag)t).getLong("At")==r.asLong())return;
        if(queue.size()<320){var entry=new CompoundTag();entry.putLong("At",r.asLong());entry.putLong("Due",l.getGameTime()+80);queue.add(entry);shared.put("Regrow",queue);}
    }
    private static boolean body(ServerLevel l,BlockPos at){return !l.getEntitiesOfClass(LivingEntity.class,new AABB(at).inflate(.04),LivingEntity::isAlive).isEmpty();}
    private static void regrow(ServerLevel l,BlockPos b,CompoundTag shared){
        var queue=shared.getList("Regrow",Tag.TAG_COMPOUND);int changed=0;
        for(int i=queue.size()-1;i>=0&&changed<2;i--){var e=queue.getCompound(i);if(e.getLong("Due")>l.getGameTime())continue;var r=BlockPos.of(e.getLong("At"));var at=b.offset(r);
            if(!inMass(r)||protectedCell(r)){queue.remove(i);continue;}
            if(body(l,at)){e.putLong("Due",l.getGameTime()+30);continue;}
            if(l.isEmptyBlock(at)){l.setBlock(at,UncannyFleshBlock.at(r),F);changed++;}
            queue.remove(i);
        }
        shared.put("Regrow",queue);
    }
    private static void spread(ServerLevel l,BlockPos b,CompoundTag shared,int age){
        boolean worked=shared.contains("LastPlayerPeel")&&l.getGameTime()-shared.getLong("LastPlayerPeel")<100;
        if(age%(worked?40:20)!=0)return;
        var candidates=new ArrayList<BlockPos>();
        for(int x=-6;x<=6;x++)for(int y=-5;y<=-2;y++)for(int z=-28;z<=-10;z++){
            var r=new BlockPos(x,y,z);var at=b.offset(r);if(protectedCell(r)||!l.isEmptyBlock(at))continue;
            for(var d:Direction.values())if(UncannyFleshBlock.is(l.getBlockState(at.relative(d)))){if(!body(l,at))candidates.add(r);break;}
        }
        // The frontier favours the stairs but grows along floors/walls too. No volume is filled in one tick.
        candidates.sort(Comparator.comparingInt(r->Math.floorMod((r.asLong()^age*7919L)*31,997)));
        int limit=worked?1:age<800?1:age<1800?2:3;
        for(int i=0;i<Math.min(limit,candidates.size());i++){var r=candidates.get(i);l.setBlock(b.offset(r),UncannyFleshBlock.at(r),F);shared.putInt("Layers",shared.getInt("Layers")+1);}
        if(!candidates.isEmpty()){shared.putInt("Pulse",shared.getInt("Pulse")+1);shared.putInt("Growth",Math.min(9,1+shared.getInt("Layers")/15));}
    }
    private static void lamps(ServerLevel l,BlockPos b,int age){
        boolean brownout=age%190<10||age%430>=395&&age%430<415||age>1200&&age%275>=255;
        for(var r:List.of(new BlockPos(-3,-2,-14),new BlockPos(3,-2,-14),new BlockPos(3,-2,-25))){var at=b.offset(r);var s=l.getBlockState(at);
            if(s.is(UncannyRegistry.FURNITURE.get())&&s.getValue(UncannyFurnitureBlock.KIND)==UncannyFurnitureBlock.Kind.FLUORESCENT&&s.getValue(UncannyFurnitureBlock.OPEN)!=brownout)l.setBlock(at,s.setValue(UncannyFurnitureBlock.OPEN,brownout),F);
        }
    }
    private static ItemStack key(ItemStack old){
        var s=new ItemStack(UncannyRegistry.CELLAR_KEY.get(),old.getCount());s.applyComponents(old.getComponentsPatch());s.set(DataComponents.CUSTOM_NAME,Component.translatable("item.the_oldest_house.cellar_key"));return s;
    }
    private static boolean ownedKey(ServerPlayer p,ItemStack s){var c=s.get(DataComponents.CUSTOM_DATA);return c!=null&&c.copyTag().hasUUID("UncannyKeyOwner")&&c.copyTag().getUUID("UncannyKeyOwner").equals(p.getUUID());}
    private static void contact(ServerPlayer p,BlockPos b,CompoundTag own){
        int pressure=own.getInt("Engulfment");boolean touching=false;var box=p.getBoundingBox().inflate(.14,.06,.14);
        if(p.getY()<b.getY()-1)for(var at:BlockPos.betweenClosed(BlockPos.containing(box.minX,box.minY,box.minZ),BlockPos.containing(box.maxX,box.maxY,box.maxZ)))if(UncannyFleshBlock.is(p.serverLevel().getBlockState(at))){touching=true;break;}
        pressure=Math.max(0,Math.min(100,pressure+(touching?2:-2)));own.putInt("Engulfment",pressure);
        if(pressure>0)p.addEffect(new MobEffectInstance(UncannyRegistry.ENGULFED,40,Math.min(3,pressure/25),false,true,true));else p.removeEffect(UncannyRegistry.ENGULFED);
        if(pressure>=100){p.removeEffect(UncannyRegistry.ENGULFED);own.putInt("Engulfment",0);UncannyStories.caught(p,b,PLACE,own,"overtaken_by_the_cellar");}
    }
    private static void reset(ServerLevel l,BlockPos b,CompoundTag shared){
        for(int x=-6;x<=6;x++)for(int y=-5;y<=-2;y++)for(int z=-28;z<=-10;z++){var r=new BlockPos(x,y,z);var at=b.offset(r);var s=l.getBlockState(at);
            if(authored(x,y,z)){if(s.isAir())l.setBlock(at,UncannyFleshBlock.at(r),F);}
            else if(UncannyFleshBlock.is(s))l.setBlock(at,Blocks.AIR.defaultBlockState(),F);
        }
        for(String k:List.of("Age","Layers","Growth","Beats","LastPeel","LastPlayerPeel","Regrow"))shared.remove(k);shared.putBoolean("ToolGiven",false);
    }
    public static void arrive(ServerPlayer p,BlockPos b){
        var l=p.serverLevel();if(!UncannyStories.actorsLoaded(l,b,PLACE))return;var shared=UncannyStories.world(l,b,PLACE);long now=l.getGameTime();
        if(shared.contains("PresentAt")&&now-shared.getLong("PresentAt")>200&&l.players().stream().noneMatch(o->o!=p&&o.isAlive()&&LiteraryVignettes.inside(o,PLACE))){reset(l,b,shared);shared.remove("Tick");}
        shared.putLong("PresentAt",now);UncannyStories.world(l,b,PLACE,shared);
    }
    public static void tick(ServerPlayer p,BlockPos b,CompoundTag own){
        var l=p.serverLevel();if(!UncannyStories.actorsLoaded(l,b,PLACE))return;long now=l.getGameTime();var shared=UncannyStories.world(l,b,PLACE);
        UncannyStories.prepare(l,b,PLACE);shared=UncannyStories.world(l,b,PLACE);
        if(shared.getLong("Tick")!=now){
            shared.putLong("Tick",now);int age=shared.getInt("Age")+5;shared.putInt("Age",age);int stage=age<400?0:age<800?1:2;
            var worker=shared.hasUUID("Peeler")&&l.getEntity(shared.getUUID("Peeler")) instanceof LiteraryActor a?a:null;
            if(worker!=null){
                worker.appearance(LiteraryActor.UNCANNY_PEELER,stage);worker.gesture(1);
                var to=Vec3.atCenterOf(b.offset(0,-3,-20));float yaw=(float)(Math.toDegrees(Math.atan2(to.z-worker.getZ(),to.x-worker.getX()))-90);
                worker.setYRot(yaw);worker.setYHeadRot(yaw);worker.yBodyRot=yaw;
                if(stage>=1)worker.setItemSlot(EquipmentSlot.MAINHAND,ItemStack.EMPTY);
                if(age%40==0&&stage<2){var r=new BlockPos(-1,-3,-19);var at=b.offset(r);if(UncannyFleshBlock.is(l.getBlockState(at))){l.setBlock(at,Blocks.AIR.defaultBlockState(),F);cut(l,b,r,shared);}if(stage==0&&!worker.getMainHandItem().isEmpty())worker.getMainHandItem().hurtAndBreak(1,worker,EquipmentSlot.MAINHAND);shared.putLong("LastPeel",now);}
                if(age%20==0)l.sendParticles(ParticleTypes.DRIPPING_WATER,worker.getX(),worker.getY()+1.5,worker.getZ(),1,.12,.05,.12,0);
            }
            regrow(l,b,shared);spread(l,b,shared,age);lamps(l,b,age);UncannyStories.world(l,b,PLACE,shared);
        }
        own.putInt("GrowthCue",shared.getInt("Pulse"));
        int age=shared.getInt("Age");
        if(age%(age>=400?60:100)==0){var worker=shared.hasUUID("Peeler")&&l.getEntity(shared.getUUID("Peeler")) instanceof LiteraryActor a?a:null;if(worker!=null)UncannyStories.heard(p,NovelRegistry.WELL_BREATH.get(),worker.getEyePosition(),age<400?.28F:.5F,.72F);}
        if(own.getInt("LastGrowth")!=shared.getInt("Pulse")){own.putInt("LastGrowth",shared.getInt("Pulse"));if(age%80==0)UncannyStories.heard(p,net.minecraft.sounds.SoundEvents.WARDEN_HEARTBEAT,Vec3.atCenterOf(b.offset(UncannyRooms.MASS)),.28F,.55F);}
        boolean exposed=true;for(int z=-18;z>=UncannyRooms.KEY_CAVITY.getZ();z--)if(!l.isEmptyBlock(b.offset(0,-4,z))){exposed=false;break;}
        if(own.getInt("Peeled")>=6&&!own.getBoolean("KeyReleased")&&exposed){
            if(!own.contains("OriginalKey")){var original=new ItemStack(UncannyRegistry.CELLAR_KEY.get());CustomData.update(DataComponents.CUSTOM_DATA,original,t->{t.putUUID("UncannyKeyOwner",p.getUUID());t.putUUID("UncannyKey",UUID.randomUUID());});own.put("OriginalKey",original.save(p.registryAccess()));}
            var item=key(ItemStack.parseOptional(p.registryAccess(),own.getCompound("OriginalKey")));var at=Vec3.atCenterOf(b.offset(UncannyRooms.KEY_CAVITY));var e=new ItemEntity(l,at.x,at.y,at.z,item);e.setTarget(p.getUUID());
            if(l.addFreshEntity(e)){own.remove("OriginalKey");own.putBoolean("KeyReleased",true);own.putUUID("KeyEntity",e.getUUID());}
        }
        boolean hasKey=false;for(int i=0;i<p.getInventory().getContainerSize();i++){var s=p.getInventory().getItem(i);if(ownedKey(p,s)){hasKey=true;if(!s.is(UncannyRegistry.CELLAR_KEY.get()))p.getInventory().setItem(i,key(s));}}
        if(own.hasUUID("KeyEntity")&&l.getEntity(own.getUUID("KeyEntity")) instanceof ItemEntity e&&ownedKey(p,e.getItem())&&!e.getItem().is(UncannyRegistry.CELLAR_KEY.get()))e.setItem(key(e.getItem()));
        contact(p,b,own);
        if(hasKey&&own.getInt("Peeled")>=6&&p.getY()>=b.getY()&&p.getZ()>b.getZ()-7){own.putBoolean("Helped",true);UncannyStories.ready(p,PLACE,own,"brought_the_cellar_key_up");}
    }
}

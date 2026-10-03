package io.github.knaitoe.theoldesthouse.labyrinth;
import io.github.knaitoe.theoldesthouse.house.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.damagesource.DamageSource;
/** A low, segmented native creature. Ground navigation respects closed room doors. */
public final class HotelHose extends PathfinderMob {
    public HotelHose(EntityType<? extends HotelHose> t,Level l){super(t,l);setPersistenceRequired();setCanPickUpLoot(false);}
    public static AttributeSupplier.Builder attributes(){return createMobAttributes().add(Attributes.MAX_HEALTH,20).add(Attributes.MOVEMENT_SPEED,.16).add(Attributes.FOLLOW_RANGE,12);}
    @Override public void tick(){super.tick();if(!(level() instanceof ServerLevel l)||tickCount%20!=0)return;
        if(tickCount%80==0)l.playSound(null,blockPosition(),HotelRegistry.RUBBER.get(),net.minecraft.sounds.SoundSource.HOSTILE,.4F,1);
        var target=l.players().stream().filter(p->HotelVignette.inside(p,LabyrinthPlace.HOTEL)&&p.getY()>getY()-1&&p.getY()<getY()+2&&distanceToSqr(p)<100).min(java.util.Comparator.comparingDouble(this::distanceToSqr)).orElse(null);
        if(target!=null&&l.clip(new net.minecraft.world.level.ClipContext(position().add(0,.15,0),target.position().add(0,.15,0),net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,this)).getType()==net.minecraft.world.phys.HitResult.Type.MISS)getNavigation().moveTo(target,.65);else getNavigation().stop();
    }
    @Override public boolean hurt(DamageSource s,float a){return false;}
    @Override public boolean removeWhenFarAway(double d){return false;}
}

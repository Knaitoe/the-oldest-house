package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.*;

/**
 * The night ward's bed curtains (0.4.199): pleated cloth hung two blocks high from a ceiling track on drop rods, joining the
 * walls and each other like panes. Where a curtain is pulled back the track runs on alone. They are cloth: anyone walks
 * through them, with a rustle, and nobody is ever shut in by one.
 */
public final class NightWardCurtainBlock extends IronBarsBlock {
    public static final MapCodec<NightWardCurtainBlock> CODEC=simpleCodec(NightWardCurtainBlock::new);
    public enum Part implements StringRepresentable {
        LOWER,UPPER,RAIL;
        public String getSerializedName(){return name().toLowerCase(Locale.ROOT);}
    }
    public static final EnumProperty<Part> PART=EnumProperty.create("part",Part.class);
    public NightWardCurtainBlock(Properties p){super(p);registerDefaultState(defaultBlockState().setValue(PART,Part.LOWER));}
    @Override public MapCodec<? extends IronBarsBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){super.createBlockStateDefinition(b);b.add(PART);}
    public static BlockState of(Part part){return NovelRegistry.NIGHT_WARD_CURTAIN.get().defaultBlockState().setValue(PART,part);}
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return Shapes.empty();}
    @Override protected void entityInside(BlockState s,Level l,BlockPos pos,Entity e){
        if(!l.isClientSide&&s.getValue(PART)==Part.LOWER&&e instanceof Player p&&!p.isSpectator()&&p.tickCount%12==0)
            l.playSound(null,pos,SoundEvents.ARMOR_EQUIP_LEATHER.value(),SoundSource.BLOCKS,.3F,1.3F);
    }
}

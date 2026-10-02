package io.github.knaitoe.theoldesthouse.house;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.EnumProperty;
/** A continuous thin rug over its floor, including the floor beneath furniture legs. */
public final class RugFloorBlock extends Block {
    public static final MapCodec<RugFloorBlock> CODEC=simpleCodec(RugFloorBlock::new);
    public enum Tone implements StringRepresentable{GRAY,BROWN,RED,SLATE;public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}}
    public enum Rim implements StringRepresentable{CENTER,NORTH,SOUTH,EAST,WEST,NORTH_EAST,NORTH_WEST,SOUTH_EAST,SOUTH_WEST;public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}}
    public static final EnumProperty<Tone> TONE=EnumProperty.create("tone",Tone.class);public static final EnumProperty<Rim> RIM=EnumProperty.create("rim",Rim.class);
    public RugFloorBlock(BlockBehaviour.Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(TONE,Tone.GRAY).setValue(RIM,Rim.CENTER));}
    @Override protected MapCodec<? extends Block> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(TONE,RIM);}
    public static void patch(ServerLevel level,BlockPos b,int x0,int x1,int z0,int z1,Block edge,Block centre){
        Tone tone=centre==Blocks.RED_CARPET?Tone.RED:edge==Blocks.BLACK_CARPET?Tone.SLATE:edge==Blocks.BROWN_CARPET?Tone.BROWN:Tone.GRAY;
        for(int x=x0;x<=x1;x++)for(int z=z0;z<=z1;z++){
            var floor=b.offset(x,-1,z);var old=level.getBlockState(floor);if(level.getBlockEntity(floor)!=null)continue;
            if(!old.is(BlockTags.PLANKS)&&!old.is(Blocks.SMOOTH_STONE)&&!old.is(Blocks.STONE)&&!old.is(Blocks.STONE_BRICKS)&&!old.is(HouseBlocks.RUG_FLOOR.get()))continue;
            String ns=z==z0?"north":z==z1?"south":"",ew=x==x0?"west":x==x1?"east":"";
            Rim rim=ns.isEmpty()?(ew.isEmpty()?Rim.CENTER:Rim.valueOf(ew.toUpperCase())):Rim.valueOf((ns+(ew.isEmpty()?"":"_"+ew)).toUpperCase());
            level.setBlock(floor,HouseBlocks.RUG_FLOOR.get().defaultBlockState().setValue(TONE,tone).setValue(RIM,rim),Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE);
            var at=floor.above();if(level.getBlockState(at).is(edge)||level.getBlockState(at).is(centre))level.setBlock(at,Blocks.AIR.defaultBlockState(),Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE);
        }
    }
}

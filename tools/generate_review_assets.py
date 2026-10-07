"""Package the generated material sheet into native models and selection shapes.

The sheet is original built-in image-generation output. Cropping/downsampling here
is the UV preparation step; this script does not invent replacement raster art.
"""
from pathlib import Path
import json, sys
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
ASSET = ROOT / 'src/main/resources/assets/the_oldest_house'
NAMES = ['well_initials','reading_wood','chess','notice','bear','train','toy_blocks','doll',
         'scarecrow_face','scarecrow_cloth','straw','pier','missing_notice','diary','lake_map','child_wall']
def box(material, *bounds): return material, list(bounds)
PROPS = {
 'bear':[box('bear',4,0,4,12,8,12),box('bear',3,8,3,13,14,13),box('bear',2,12,4,5,16,7),box('bear',11,12,4,14,16,7),box('bear',1,1,5,4,7,10),box('bear',12,1,5,15,7,10)],
 'train':[box('train',1,2,3,15,6,12),box('train',1,6,4,6,11,11),box('train',9,6,5,12,9,10),box('train',10,9,6,12,13,8),box('reading_wood',2,0,2,5,3,13),box('reading_wood',11,0,2,14,3,13)],
 'toy_blocks':[box('toy_blocks',1,0,2,6,5,7),box('toy_blocks',7,0,6,12,5,11),box('toy_blocks',3,5,4,8,10,9),box('toy_blocks',10,0,1,15,5,6)],
 'doll':[box('doll',4,0,5,7,5,8),box('doll',9,0,5,12,5,8),box('doll',4,5,4,12,11,10),box('doll',5,11,4,11,16,10),box('doll',1,6,5,4,10,8),box('doll',12,6,5,15,10,8)],
 'chess':[box('reading_wood',1,0,1,15,1,15),box('chess',1,1,1,15,1.2,15),box('reading_wood',3,1.2,3,4,3,4),box('reading_wood',11,1.2,11,12,4,12)],
 'notice_board':[box('reading_wood',7,0,7,9,14,9),box('reading_wood',1,10,7,15,25,9),box('notice',2,11,6.8,14,24,7)],
 'map_board':[box('reading_wood',7,0,7,9,14,9),box('reading_wood',1,10,7,15,25,9),box('lake_map',2,11,6.8,14,24,7)],
 'missing_notice':[box('reading_wood',1,0,13,15,16,16),box('missing_notice',2,1,12.8,14,15,13)],
 'diary_stack':[box('diary',1,0,2,14,1,14),box('diary',3,1,1,15,2,12),box('reading_wood',2,2,4,8,2.7,4.5)],
 'field_notebook':[box('reading_wood',2,0,2,14,2,13),box('diary',3,2,3,13,2.3,12)],
 'flashlight':[box('pier',2,0,5,12,3,8),box('reading_wood',12,0,4,15,4,9)],
 'cassette':[box('pier',2,0,3,14,2,11),box('missing_notice',3,2,4,13,2.2,10)],
 'sealed_box':[box('reading_wood',1,0,1,15,11,15),box('diary',6,11,1,10,11.2,15)],
 'book_tray':[box('reading_wood',1,14,1,15,16,15),box('reading_wood',2,0,2,4,14,4),box('reading_wood',12,0,2,14,14,4),box('reading_wood',2,0,12,4,14,14),box('reading_wood',12,0,12,14,14,14)],
 'shadow':[box('black',6,0,13,8,9,15),box('black',9,0,13,11,9,15),box('black',5,9,13,12,22,15),box('black',6,22,13,11,28,15),box('black',2,12,13,5,21,15),box('black',12,12,13,15,21,15)],
 'child_wall':[box('child_wall',0,0,15.8,16,16,16)],
}

def run(sheet):
 im=Image.open(sheet).convert('RGBA')
 texture=ASSET/'textures/block';texture.mkdir(parents=True,exist_ok=True)
 tiles={}
 for i,name in enumerate(NAMES):
  x,y=i%4,i//4
  tile=im.crop((round(im.width*x/4),round(im.height*y/4),round(im.width*(x+1)/4),round(im.height*(y+1)/4))).resize((64,64),Image.Resampling.NEAREST)
  tile.save(texture/f'review_{name}.png',optimize=True);tiles[name]=tile
 variants={}
 for name,cubes in PROPS.items():
  textures={m:('minecraft:block/black_concrete' if m=='black' else 'the_oldest_house:block/review_'+m) for m,_ in cubes}
  elements=[{'from':b[:3],'to':b[3:],'faces':{f:{'uv':[0,0,16,16],'texture':'#'+m} for f in ['north','south','east','west','up','down']}} for m,b in cubes]
  model={'ambientocclusion':True,'textures':dict(textures,particle=next(iter(textures.values()))),'elements':elements}
  (ASSET/f'models/block/review_{name}.json').write_text(json.dumps(model,indent=2)+'\n')
  for face,turn in [('north',0),('east',90),('south',180),('west',270)]: variants[f'facing={face},kind={name}']={'model':'the_oldest_house:block/review_'+name,'y':turn}
 (ASSET/'blockstates/vignette_detail.json').write_text(json.dumps({'variants':variants},indent=2)+'\n')
 # Keep the existing desk's exact native geometry, with its own new material.
 desk=json.loads((ASSET/'models/block/furniture_walnut_desk.json').read_text())
 desk['textures']={k:'the_oldest_house:block/review_reading_wood' for k in desk['textures']}
 (ASSET/'models/block/furniture_reading_desk.json').write_text(json.dumps(desk,indent=2)+'\n')
 p=ASSET/'blockstates/household_furniture.json';j=json.loads(p.read_text())
 for key,v in list(j['variants'].items()):
  if 'kind=walnut_desk' in key: j['variants'][key.replace('kind=walnut_desk','kind=reading_desk')]=dict(v,model='the_oldest_house:block/furniture_reading_desk')
 p.write_text(json.dumps(j,indent=2)+'\n')
 # The carving is the same full wall block at its old interaction coordinate.
 p=ASSET/'models/block/well_carvings.json';j=json.loads(p.read_text())
 j['textures']={k:'the_oldest_house:block/review_well_initials' for k in j['textures']};p.write_text(json.dumps(j,indent=2)+'\n')
 # Native player-model UV skin for the original ArmorStand, not a new actor.
 skin=Image.new('RGBA',(64,64),(0,0,0,0))
 for rect,material in [((0,0,32,16),'scarecrow_face'),((16,16,40,32),'scarecrow_cloth'),((40,16,56,32),'straw'),((0,16,16,32),'pier'),((16,48,32,64),'pier'),((32,48,48,64),'straw')]:
  x0,y0,x1,y1=rect;skin.paste(tiles[material].resize((x1-x0,y1-y0),Image.Resampling.NEAREST),(x0,y0))
 (ASSET/'textures/entity/costume_scarecrow.png').parent.mkdir(parents=True,exist_ok=True);skin.save(ASSET/'textures/entity/costume_scarecrow.png',optimize=True)
 shapes='\n'.join('            case '+name.upper()+' -> new double[][]{'+','.join('{'+','.join(str(v) for v in b)+'}' for _,b in cubes)+'};' for name,cubes in PROPS.items())
 source='''package io.github.knaitoe.theoldesthouse.house;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.shapes.*;
/** Custom scene objects; mesh and rotated selection share exact dimensions. */
public final class VignetteDetailBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<VignetteDetailBlock> CODEC=simpleCodec(VignetteDetailBlock::new);
    public enum Kind implements StringRepresentable { ENUMS;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}}
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public VignetteDetailBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(KIND,Kind.BEAR));}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,KIND);}
    public static BlockState state(Kind kind,Direction facing){return HouseBlocks.VIGNETTE_DETAIL.get().defaultBlockState().setValue(KIND,kind).setValue(FACING,facing);}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        double[][] boxes=switch(s.getValue(KIND)){
SHAPES
        };VoxelShape shape=Shapes.empty();int turns=switch(s.getValue(FACING)){case EAST->1;case SOUTH->2;case WEST->3;default->0;};
        for(var b:boxes){double x0=b[0],z0=b[2],x1=b[3],z1=b[5];for(int t=0;t<turns;t++){double nx0=16-z1,nx1=16-z0;z0=x0;z1=x1;x0=nx0;x1=nx1;}shape=Shapes.or(shape,Block.box(x0,b[1],z0,x1,b[4],z1));}return shape.optimize();
    }
    @Override protected BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    @Override protected BlockState mirror(BlockState s,Mirror m){return s.rotate(m.getRotation(s.getValue(FACING)));}
}
'''.replace('ENUMS',', '.join(n.upper() for n in PROPS)).replace('SHAPES',shapes)
 (ROOT/'src/main/java/io/github/knaitoe/theoldesthouse/house/VignetteDetailBlock.java').write_text(source)
 lang=ASSET/'lang/en_us.json';j=json.loads(lang.read_text());j['block.the_oldest_house.vignette_detail']='Vignette detail';lang.write_text(json.dumps(j,indent=2,ensure_ascii=False)+'\n')
 print(f'Packed {len(NAMES)} generated texture tiles, {len(PROPS)} native prop models, reading desk and native costume skin.')
if __name__=='__main__': run(Path(sys.argv[1]) if len(sys.argv)>1 else ROOT/'art/review_0454/material-atlas.png')

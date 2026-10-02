"""Small scene furnishings: exact native meshes, matching selection boxes and material UVs."""
from pathlib import Path
import json, random
from PIL import Image

ROOT=Path(__file__).resolve().parents[1]
ASSET=ROOT/'src/main/resources/assets/the_oldest_house'
MATERIALS={
 'wood':(84,53,31),'leather':(77,56,43),'brass':(153,119,62),'paper':(207,195,161),
 'blue':(63,84,103),'red':(119,53,46),'green':(69,86,61),'cream':(184,170,140),
 'glass':(79,111,101),'black':(35,32,29),'white':(209,211,201),'cloth':(131,108,81),
 'rope':(159,133,89),'steel':(111,116,116),'ink':(57,54,47),'floral':(91,113,65),
}
# Material plus x/y/z bounds in sixteenths. Objects remain in their own block.
def c(material,*bounds): return (material,list(bounds))
PROPS={
 'books':[c('blue',2,0,3,14,2,12),c('paper',3,2,4,13,4,11),c('red',1,4,2,13,6,11),c('paper',2,6,3,12,8,10),c('green',3,8,4,14,10,12)],
 'tea_set':[c('wood',1,0,2,15,1,14),c('cream',3,1,4,7,5,8),c('brass',7,2,5,8,4,7),c('cream',10,1,8,14,5,12),c('brass',9,2,9,10,4,11)],
 'shoes':[c('leather',2,0,3,6,3,13),c('leather',9,0,1,13,3,11),c('black',2,0,3,6,1,13),c('black',9,0,1,13,1,11)],
 'blanket':[c('cloth',2,0,2,14,3,13),c('cream',3,3,3,13,5,12),c('cloth',2,5,2,14,7,13)],
 'satchel':[c('leather',3,0,3,13,9,12),c('leather',5,9,5,11,12,7),c('brass',7,4,2,9,6,3)],
 'tools':[c('wood',1,0,2,15,3,13),c('steel',3,3,5,12,4,7),c('steel',10,4,4,13,6,8),c('wood',3,3,9,13,5,10)],
 'bottles':[c('glass',2,0,4,6,8,8),c('brass',3,8,5,5,10,7),c('glass',9,0,7,14,7,12),c('brass',10,7,8,13,9,11)],
 'vase':[c('cream',5,0,5,11,7,11),c('green',7,7,7,9,14,9),c('floral',4,10,6,7,14,9),c('floral',9,9,5,12,12,8),c('white',7,12,7,11,15,11)],
 'feed_sack':[c('cloth',2,0,2,14,9,14),c('rope',5,9,5,11,11,11),c('cream',4,2,1,12,6,2)],
 'file_tray':[c('steel',1,0,2,15,1,14),c('paper',2,1,3,14,3,13),c('steel',1,1,2,2,5,14),c('steel',14,1,2,15,5,14),c('blue',3,3,4,12,4,12)],
 'towels':[c('white',2,0,2,14,3,13),c('blue',3,3,3,13,5,12),c('white',2,5,2,14,7,13)],
 'coat':[c('brass',7,12,14,9,15,16),c('blue',4,2,13,12,13,16),c('blue',2,6,12,4,12,15),c('blue',12,6,12,14,12,15)],
 'crate':[c('wood',1,0,1,15,9,15),c('paper',3,9,3,10,11,12),c('green',10,9,5,13,13,10)],
 'rope_coil':[c('rope',2,0,3,14,2,5),c('rope',2,0,11,14,2,13),c('rope',2,0,5,4,2,11),c('rope',12,0,5,14,2,11),c('rope',4,1,5,12,3,7),c('rope',4,1,9,12,3,11)],
 'crock':[c('cream',4,0,4,12,9,12),c('wood',3,9,3,13,10,13),c('wood',7,10,7,9,12,9)],
 'table_lamp':[c('brass',3,0,3,13,1,13),c('brass',7,1,7,9,9,9),c('cream',3,9,3,13,14,13),c('cream',5,14,5,11,15,11)],
 'clock':[c('wood',3,3,13,13,14,16),c('cream',4,4,12,12,13,13),c('black',7,8,11.5,8,12,12),c('black',8,7,11.5,11,8,12)],
 'toys':[c('red',2,0,3,6,4,7),c('blue',6,0,5,10,4,9),c('green',10,0,2,14,4,6),c('cream',4,4,4,8,7,8)],
 'dish_rack':[c('steel',1,0,1,15,1,15),c('steel',1,1,1,2,5,15),c('steel',14,1,1,15,5,15),*[c('cream',x,1,3,x+1,8,12) for x in (4,7,10)]],
 'frame':[c('wood',2,2,13,14,14,16),c('paper',3,3,12,13,13,13),c('ink',5,4,11.5,8,9,12),c('blue',8,6,11.5,11,12,12)],
 'ink_papers':[c('paper',1,0,2,13,.5,14),c('paper',3,.5,1,15,1,12),c('ink',3,1,4,9,1.2,4.5),c('black',11,1,7,14,4,10),c('brass',4,1,10,12,1.5,11)],
 'medical_tray':[c('steel',1,0,2,15,1,14),c('white',2,1,3,7,3,8),c('glass',10,1,4,13,6,7),c('cream',3,1,10,12,2,12)],
 'dustpan':[c('steel',1,0,4,9,1,13),c('wood',9,0,7,15,2,9),c('black',2,1,5,8,2,6)],
 'hymnals':[c('wood',2,0,3,14,3,12),c('paper',3,1,4,13,2,11),c('black',2,3,3,14,4,12),c('brass',7,4,5,9,4.3,10)],
}

def run():
 rng=random.Random(427)
 for name,base in MATERIALS.items():
  im=Image.new('RGBA',(16,16))
  for y in range(16):
   for x in range(16):
    grain=(3 if x%4==0 else 0) if name in {'wood','leather'} else (2 if (x+y)%3==0 else 0)
    delta=rng.randrange(-7,8)+grain
    im.putpixel((x,y),tuple(max(0,min(255,n+delta)) for n in base)+(255,))
  path=ASSET/f'textures/block/detail_{name}.png';path.parent.mkdir(parents=True,exist_ok=True);im.save(path,optimize=True)
 variants={}
 for name,cubes in PROPS.items():
  elements=[]
  for material,b in cubes:
   elements.append({'from':b[:3],'to':b[3:],'faces':{face:{'uv':[0,0,16,16],'texture':'#'+material} for face in ['north','south','east','west','up','down']}})
  model={'ambientocclusion':True,'textures':{**{m:'the_oldest_house:block/detail_'+m for m in MATERIALS},'particle':'the_oldest_house:block/detail_'+cubes[0][0]},'elements':elements}
  (ASSET/f'models/block/detail_{name}.json').write_text(json.dumps(model,indent=2)+'\n')
  for facing,rotation in [('north',0),('east',90),('south',180),('west',270)]:
   variants[f'facing={facing},kind={name}']={'model':'the_oldest_house:block/detail_'+name,'y':rotation}
 (ASSET/'blockstates/scene_detail.json').write_text(json.dumps({'variants':variants},indent=2)+'\n')
 lang=json.loads((ASSET/'lang/en_us.json').read_text());lang['block.the_oldest_house.scene_detail']='Household detail';(ASSET/'lang/en_us.json').write_text(json.dumps(lang,indent=2,ensure_ascii=False)+'\n')
 enums=', '.join(name.upper() for name in PROPS)
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

/** Small authored clutter. Selection follows the mesh; it never obstructs a story route. */
public final class SceneDetailBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<SceneDetailBlock> CODEC=simpleCodec(SceneDetailBlock::new);
    public enum Kind implements StringRepresentable { ENUMS;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
        public boolean wall(){return this==COAT||this==CLOCK||this==FRAME;}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public SceneDetailBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(KIND,Kind.BOOKS));}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,KIND);}
    public static BlockState state(Kind kind,Direction facing){return HouseBlocks.SCENE_DETAIL.get().defaultBlockState().setValue(KIND,kind).setValue(FACING,facing);}
    public static boolean supported(BlockGetter level,BlockPos at,BlockState state){
        var facing=state.getValue(FACING);var kind=state.getValue(KIND);
        BlockPos support=kind.wall()?at.relative(facing.getOpposite()):at.below();
        return level.getBlockState(support).isFaceSturdy(level,support,kind.wall()?facing:Direction.UP);
    }
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        double[][] boxes=switch(s.getValue(KIND)) {
SHAPES
        };
        int turns=switch(s.getValue(FACING)){case EAST->1;case SOUTH->2;case WEST->3;default->0;};
        VoxelShape shape=Shapes.empty();
        for(var box:boxes){double x0=box[0],z0=box[2],x1=box[3],z1=box[5];
            for(int i=0;i<turns;i++){double a=16-z1,b=16-z0;z0=x0;z1=x1;x0=a;x1=b;}
            shape=Shapes.or(shape,Block.box(x0,box[1],z0,x1,box[4],z1));
        }
        return shape.optimize();
    }
    @Override protected BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    @Override protected BlockState mirror(BlockState s,Mirror m){return s.rotate(m.getRotation(s.getValue(FACING)));}
}
'''.replace('ENUMS',enums).replace('SHAPES',shapes)
 (ROOT/'src/main/java/io/github/knaitoe/theoldesthouse/house/SceneDetailBlock.java').write_text(source)
 print(f'Generated {len(PROPS)} small native props, matching selection shapes, {len(variants)} facings and {len(MATERIALS)} materials.')

if __name__=='__main__':run()

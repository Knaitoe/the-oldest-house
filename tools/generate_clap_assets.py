"""Author exact Minecraft cuboids/UVs for the girl and woven blindfold. No runtime dependencies."""
from pathlib import Path
import json, math
import numpy as np
from PIL import Image, ImageDraw, ImageFont

ROOT=Path(__file__).resolve().parents[1]
ASSETS=ROOT/'src/main/resources/assets/the_oldest_house/textures'
ART=ROOT/'art'
JAVA=ROOT/'src/main/java/io/github/knaitoe/theoldesthouse/client'
for p in (ASSETS/'entity',ASSETS/'item',ASSETS/'gui',ART,JAVA): p.mkdir(parents=True,exist_ok=True)

def cube(name,origin,size,material): return dict(name=name,origin=origin,size=size,material=material)
def part(name,parent='root',pivot=(0,0,0),cubes=()): return dict(name=name,parent=parent,pivot=pivot,cubes=list(cubes))
girl=[
    part('root',None,(0,24,0)),
    part('body',pivot=(0,-15,0),cubes=[cube('cotton_nightdress',(-3.5,0,-2.5),(7,10,5),'cloth')]),
    part('head',pivot=(0,-16,0),cubes=[cube('face',(-3,-6,-3),(6,6,6),'face')]),
    part('hair','head',cubes=[cube('dark_crown',(-3.5,-7,-3.5),(7,2,7),'hair'),
        cube('dark_back',(-3.5,-5,2.5),(7,6,1),'hair'),
        cube('left_hair',(-3.5,-5,-2.5),(1,5,5),'hair'),
        cube('right_hair',(2.5,-5,-2.5),(1,5,5),'hair')]),
    part('left_arm',pivot=(4,-15,0),cubes=[cube('left_sleeve',(-1,0,-1),(2,3,2),'cloth'),cube('left_hand',(-1,3,-1),(2,5,2),'skin')]),
    part('right_arm',pivot=(-4,-15,0),cubes=[cube('right_sleeve',(-1,0,-1),(2,3,2),'cloth'),cube('right_hand',(-1,3,-1),(2,5,2),'skin')]),
    part('left_leg',pivot=(1.6,-6,0),cubes=[cube('left_ankle',(-1,0,-1),(2,5,2),'skin'),cube('left_bare_foot',(-1,5,-2),(2,1,3),'foot')]),
    part('right_leg',pivot=(-1.6,-6,0),cubes=[cube('right_ankle',(-1,0,-1),(2,5,2),'skin'),cube('right_bare_foot',(-1,5,-2),(2,1,3),'foot')]),
]
x=y=row=0
for p in girl:
    for c in p['cubes']:
        w,h,d=c['size'];rw=2*(w+d)+1;rh=d+h+1
        if x+rw>128:x=0;y+=row;row=0
        assert y+rh<=128
        c['uv']=(x,y);x+=rw;row=max(row,rh)

palette=dict(cloth=(187,192,189),skin=(185,182,173),face=(193,188,178),foot=(181,177,167),hair=(35,31,36))
tex=Image.new('RGBA',(512,512));draw=ImageDraw.Draw(tex)
for p in girl:
    for c in p['cubes']:
        u,v=(n*4 for n in c['uv']);w,h,d=(n*4 for n in c['size']);mat=c['material'];base=palette[mat]
        for yy in range(v,v+d+h):
            for xx in range(u,u+2*(w+d)):
                shade=((xx*7+yy*13)%5)-2
                if mat=='cloth':shade-=7 if (xx-u)%12<2 else 0
                if mat=='hair':shade-=9 if (xx-u)%8<2 else 0
                if mat in ('skin','foot'):shade-=5 if yy>v+d+h-4 else 0
                draw.point((xx,yy),tuple(max(0,min(255,n+shade)) for n in base)+(255,))
        fx,fy=u+d,v+d
        if mat=='face':
            for ex in (3,15):
                draw.line((fx+ex,fy+9,fx+ex+4,fy+9),fill=(77,73,77,255),width=2)
                draw.line((fx+ex,fy+12,fx+ex+5,fy+12),fill=(145,139,140,255))
            draw.line((fx+9,fy+19,fx+14,fy+19),fill=(118,105,104,255))
        if mat=='cloth':
            draw.line((u,v+d+h-4,u+2*(w+d)-1,v+d+h-4),fill=(146,154,152,255))
            for px in range(u+2,u+2*(w+d),4):draw.point((px,v+d+h-2),fill=(220,220,205,255))
        if mat=='foot':
            # Small toe divisions on the front and top, not a boot texture.
            for px in (fx+2,fx+5):
                draw.line((px,fy,px,fy+2),fill=(143,137,131,255))
                draw.line((px,v+d-3,px,v+d-1),fill=(143,137,131,255))
            draw.line((fx+1,v+d-3,fx+w-2,v+d-3),fill=(202,196,184,255))
tex.save(ASSETS/'entity/clap_ghost_girl.png')

item=Image.new('RGBA',(32,32));di=ImageDraw.Draw(item)
di.polygon([(3,12),(6,8),(23,8),(25,12),(23,19),(7,19),(4,16)],fill=(39,35,43,255))
for yy in range(9,19):
    for xx in range(6,24):
        if item.getpixel((xx,yy))[3]:di.point((xx,yy),fill=(49 if (xx+yy)%3==0 else 34,39 if yy%2==0 else 31,46 if xx%2==0 else 38,255))
di.line((5,12,2,9),fill=(74,62,75,255),width=2);di.line((24,12,29,8),fill=(74,62,75,255),width=2)
di.polygon([(25,12),(29,17),(27,23),(24,16)],fill=(45,38,49,255))
di.polygon([(24,15),(22,23),(24,27),(26,20)],fill=(59,47,61,255))
di.line((7,9,21,9),fill=(81,68,82,255));di.line((6,18,23,18),fill=(21,18,24,255))
item.save(ASSETS/'item/blindfold.png')

edge=Image.new('RGBA',(64,16));de=ImageDraw.Draw(edge)
for xx in range(64):
    hem=10+(xx*13%5)
    for yy in range(hem):
        shade=8+(xx+yy)%3*3
        de.point((xx,yy),fill=(shade,shade-1,shade+2,255))
    if xx%7==0:de.line((xx,hem-1,xx,15),fill=(24,20,27,145))
edge.save(ASSETS/'gui/blindfold_edge.png')

def ff(n):return str(float(n))+'F'
lines=['package io.github.knaitoe.theoldesthouse.client;','',
'import io.github.knaitoe.theoldesthouse.TheOldestHouse;',
'import io.github.knaitoe.theoldesthouse.labyrinth.ClapGhostEntity;',
'import net.minecraft.client.model.HierarchicalModel;',
'import net.minecraft.client.model.geom.ModelLayerLocation;',
'import net.minecraft.client.model.geom.ModelPart;',
'import net.minecraft.client.model.geom.PartPose;',
'import net.minecraft.client.model.geom.builders.CubeListBuilder;',
'import net.minecraft.client.model.geom.builders.LayerDefinition;',
'import net.minecraft.client.model.geom.builders.MeshDefinition;',
'import net.minecraft.client.model.geom.builders.PartDefinition;',
'import net.minecraft.resources.ResourceLocation;',
'import net.minecraft.util.Mth;','',
'/** Regenerate exact geometry/UVs with tools/generate_clap_assets.py. */',
'public final class ClapGhostModel extends HierarchicalModel<ClapGhostEntity> {',
'    public static final ModelLayerLocation LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID, "clap_ghost_girl"), "main");',
'    private final ModelPart root;',
'    public ClapGhostModel(ModelPart baked) { root = baked.getChild("root"); }',
'    @Override public ModelPart root() { return root; }',
'    public static LayerDefinition createBodyLayer() {',
'        MeshDefinition mesh = new MeshDefinition();']
for p in girl:
    builder='CubeListBuilder.create()'
    for c in p['cubes']:builder+='.texOffs('+', '.join(map(str,c['uv']))+').addBox('+', '.join(ff(n) for n in (*c['origin'],*c['size']))+')'
    parent='mesh.getRoot()' if p['parent'] is None else 'p_'+p['parent']
    lines.append('        PartDefinition p_'+p['name']+' = '+parent+'.addOrReplaceChild("'+p['name']+'", '+builder+', PartPose.offset('+', '.join(ff(n) for n in p['pivot'])+'));')
lines+=['        return LayerDefinition.create(mesh, 128, 128);','    }',
'    @Override public void setupAnim(ClapGhostEntity entity, float walk, float amount, float age, float yaw, float pitch) {',
'        root.getAllParts().forEach(ModelPart::resetPose);',
'        root.xScale = root.yScale = root.zScale = 0.80F;',
'        root.getChild("head").yRot = yaw * Mth.DEG_TO_RAD;',
'        root.getChild("left_leg").xRot = Mth.sin(age * 0.42F) * 0.18F;',
'        root.getChild("right_leg").xRot = -Mth.sin(age * 0.42F) * 0.18F;',
'        root.getChild("left_arm").zRot = -0.045F;',
'        root.getChild("right_arm").zRot = 0.045F;',
'    }','}']
(JAVA/'ClapGhostModel.java').write_text('\n'.join(lines)+'\n')
(ART/'clap_ghost_girl.model.json').write_text(json.dumps(dict(format='oldest-house-cuboid-model-v1',texture=dict(width=512,height=512,uv_width=128,uv_height=128,path='clap_ghost_girl.png'),scale=.8,parts=girl),indent=2)+'\n')

# Orthographic preview of the actual cuboids and their exact UVs.
def render():
    canvas=Image.new('RGBA',(400,500),(24,23,28,255));faces=[];transforms={}
    angle=-.35;ca,sa=math.cos(angle),math.sin(angle)
    camera=np.array([[ca,0,sa],[0,1,0],[-sa,0,ca]])
    for p in girl:
        local=np.eye(4);local[:3,3]=p['pivot']
        if p['name']=='root':local[:3,:3]*=.8
        transform=transforms[p['parent']]@local if p['parent'] else local;transforms[p['name']]=transform
        for c in p['cubes']:
            x,y,z=c['origin'];w,h,d=c['size'];u,v=c['uv']
            corners=np.array([[x,y,z,1],[x+w,y,z,1],[x+w,y+h,z,1],[x,y+h,z,1],[x,y,z+d,1],[x+w,y,z+d,1],[x+w,y+h,z+d,1],[x,y+h,z+d,1]])
            world=(transform@corners.T).T[:,:3];world[:,1]=24-world[:,1];cam=(camera@world.T).T
            points=[(200+a[0]*18,454-a[1]*18) for a in cam]
            quads=[([0,1,2,3],(u+d,v+d,u+d+w,v+d+h),1),([1,5,6,2],(u+d+w,v+d,u+d+w+d,v+d+h),.75),([4,0,3,7],(u,v+d,u+d,v+d+h),.75),([5,4,7,6],(u+d+w+d,v+d,u+2*(w+d),v+d+h),.8),([4,5,1,0],(u+d,v,u+d+w,v+d),1.1)]
            for ids,uv,shade in quads:
                face=[points[i] for i in ids]
                cross=(face[1][0]-face[0][0])*(face[2][1]-face[0][1])-(face[1][1]-face[0][1])*(face[2][0]-face[0][0])
                if cross>0:faces.append((sum(cam[i][2] for i in ids)/4,face,uv,shade))
    for depth,face,uv,shade in sorted(faces,key=lambda a:-a[0]):
        sample=tex.crop(tuple(n*4 for n in uv));arr=np.asarray(sample).copy();arr[:,:,:3]=(arr[:,:,:3]*shade).clip(0,255).astype('uint8');sample=Image.fromarray(arr)
        source=[(0,0),(sample.width,0),(sample.width,sample.height),(0,sample.height)];matrix=[];target=[]
        for (xx,yy),(sx,sy) in zip(face,source):
            matrix.extend(([xx,yy,1,0,0,0,-sx*xx,-sx*yy],[0,0,0,xx,yy,1,-sy*xx,-sy*yy]));target.extend((sx,sy))
        try:coeff=np.linalg.solve(np.array(matrix),np.array(target))
        except np.linalg.LinAlgError:continue
        warped=sample.transform(canvas.size,Image.Transform.PERSPECTIVE,coeff,Image.Resampling.NEAREST)
        mask=Image.new('L',canvas.size);ImageDraw.Draw(mask).polygon(face,fill=255)
        warped.putalpha(Image.fromarray(np.minimum(np.asarray(mask),np.asarray(warped.getchannel('A')))));canvas.alpha_composite(warped)
    return canvas
model=render();preview=Image.new('RGBA',(1000,650),(24,23,28,255));preview.alpha_composite(model,(0,70));pd=ImageDraw.Draw(preview)
font=ImageFont.truetype('/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf',20);small=ImageFont.truetype('/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf',15)
pd.text((25,24),'Clap-and-seek: textured girl and bare feet',font=font,fill=(215,207,190,255))
pd.text((470,85),'Woven blindfold item',font=font,fill=(215,207,190,255));preview.alpha_composite(item.resize((192,192),Image.Resampling.NEAREST),(520,115))
pd.text((470,325),'Bare feet: same model, enlarged',font=font,fill=(215,207,190,255))
preview.alpha_composite(model.crop((140,360,260,462)).resize((240,204),Image.Resampling.NEAREST),(500,365))
pd.text((25,590),'Actual model and UV textures. In play the cloth hides her face; only the gap below it reveals her feet.',font=small,fill=(165,156,148,255))
preview.convert('RGB').save(ART/'clap_seek_preview.png')
print(json.dumps(dict(parts=len(girl),cubes=sum(len(p['cubes']) for p in girl),texture=tex.size,preview=str(ART/'clap_seek_preview.png'))))

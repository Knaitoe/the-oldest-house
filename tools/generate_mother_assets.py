"""Author and preview the exact cuboid models used by the Java renderers.

No runtime dependencies are added to the mod. Pillow and numpy are only for authoring.
Run from any directory; the model, UV atlas and model source are deterministic.
"""
from pathlib import Path
import json
import math
import numpy as np
from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / 'src/main/java/io/github/knaitoe/theoldesthouse/client'
ASSETS = ROOT / 'src/main/resources/assets/the_oldest_house/textures/entity'
ART = ROOT / 'art'
for directory in (JAVA, ASSETS, ART):
    directory.mkdir(parents=True, exist_ok=True)


def part(name, parent='root', pivot=(0, 0, 0), cubes=(), rotation=(0, 0, 0)):
    return dict(name=name, parent=parent, pivot=pivot, cubes=list(cubes), rotation=rotation)


def cube(name, origin, size, material):
    return dict(name=name, origin=origin, size=size, material=material)


mother = [
    part('root', None, (0, 24, 0)),
    part('body', pivot=(0, -23, 0), cubes=[cube('bodice', (-4, 0, -2.5), (8, 11, 5), 'dress'),
        cube('left_bodice_fold', (-3.5, 3, -4), (3, 4, 3), 'dress'),
        cube('right_bodice_fold', (.5, 3, -4), (3, 4, 3), 'dress')]),
    part('neck', pivot=(0, -23, 0), cubes=[cube('neck', (-1.5, -3, -1.5), (3, 3, 3), 'skin')]),
    part('head', pivot=(0, -26, -.1), cubes=[cube('face', (-3, -6, -3), (6, 7, 6), 'face'),
        cube('nose', (-.5, -3, -4), (1, 2, 1), 'skin')]),
    part('hair', 'head', (0, 0, .2), cubes=[cube('coiffed_hair', (-3.5, -7, -2.6), (7, 3, 6), 'hair'),
        cube('bun', (-2, -5, 3), (4, 4, 3), 'hair')]),
    part('jaw', 'head', (0, .2, -.2), cubes=[cube('unhinged_jaw', (-3, 0, -3), (6, 3, 6), 'jaw')]),
    part('shawl', pivot=(0, -22.5, .2), cubes=[cube('shawl', (-5, -.5, -3), (10, 5, 7), 'shawl')]),
    part('collar', pivot=(0, -22, -3.2), cubes=[cube('lace_collar', (-2.5, 0, -.5), (5, 2, 1), 'lace')]),
    part('skirt_upper', pivot=(0, -13, 0), cubes=[cube('upper_skirt', (-5, -1, -3), (10, 6, 6), 'dress')]),
    part('skirt_lower', pivot=(0, -8, 0), cubes=[cube('lower_skirt', (-6, 0, -4), (12, 8, 8), 'hem')]),
    part('left_leg', pivot=(2, -8, 0), cubes=[cube('left_shoe', (-1.5, 6, -2.8), (3, 2, 5), 'shoe')]),
    part('right_leg', pivot=(-2, -8, 0), cubes=[cube('right_shoe', (-1.5, 6, -2.8), (3, 2, 5), 'shoe')]),
]
for side, sign in (('left', 1), ('right', -1)):
    mother += [
        part(side + '_arm', pivot=(sign * 4.5, -22, 0), rotation=(0, 0, -sign * .06),
             cubes=[cube(side + '_sleeve', (-1.5, -.5, -1.5), (3, 8, 3), 'shawl')]),
        part(side + '_forearm', side + '_arm', (0, 7.5, 0), rotation=(-.14, 0, 0),
             cubes=[cube(side + '_forearm', (-1, 0, -1), (2, 7, 2), 'skin')]),
        part(side + '_hand', side + '_forearm', (0, 6.5, 0),
             cubes=[cube(side + '_palm', (-1.5, 0, -1), (3, 3, 2), 'skin')]),
    ]
    for finger, length in enumerate((3, 5, 4)):
        mother.append(part(side + '_finger_' + str(finger), side + '_hand',
                           (-1 + finger, 2.5, -.2), rotation=(-.18, 0, 0), cubes=[
                               cube(side + '_finger_' + str(finger), (-.35, 0, -.35), (.7, length, .7), 'skin'),
                               cube(side + '_nail_' + str(finger), (-.38, length - .5, -.5), (.76, 2, .6), 'nail')]))

dog = [
    part('root', None, (0, 24, 0)),
    part('body', pivot=(0, -5, 0), cubes=[cube('long_coat', (-4, -3, -5), (8, 6, 10), 'fur')]),
    part('mane', pivot=(0, -5, -3), cubes=[cube('lion_mane', (-4.5, -3.5, -2.5), (9, 7, 5), 'mane')]),
    part('head', pivot=(0, -6, -5), cubes=[cube('flat_face', (-3, -3, -3), (6, 5, 5), 'dog_face'),
        cube('short_muzzle', (-2, 0, -4), (4, 2, 1), 'muzzle'),
        cube('nose', (-1, -.4, -4.5), (2, 1, 1), 'shoe')]),
    part('left_ear', 'head', (3, -.8, .3), cubes=[cube('left_floppy_ear', (0, -1, -1.5), (2, 5, 3), 'ear')]),
    part('right_ear', 'head', (-3, -.8, .3), cubes=[cube('right_floppy_ear', (-2, -1, -1.5), (2, 5, 3), 'ear')]),
    part('bandage', 'head', (0, 0, 0), cubes=[cube('head_bandage', (-3.08, -3.1, -3.08), (6.16, 1.2, 5.16), 'bandage')]),
    part('tail', pivot=(0, -7, 4), rotation=(-.7, 0, 0), cubes=[cube('curled_tail', (-2, -2, -1), (4, 4, 3), 'mane')]),
]
for name, x, z in [('left_front', 2.8, -3), ('right_front', -2.8, -3),
                   ('left_back', 2.8, 3), ('right_back', -2.8, 3)]:
    dog.append(part(name, pivot=(x, -3, z), cubes=[cube(name, (-1, 0, -1), (2, 3, 2), 'fur')]))

PALETTE = dict(dress=(64, 38, 51), shawl=(34, 29, 36), hem=(46, 28, 37),
               skin=(176, 148, 132), face=(181, 151, 135), jaw=(136, 105, 103),
               hair=(119, 113, 111), lace=(177, 161, 146), nail=(91, 29, 43), shoe=(32, 24, 23),
               fur=(161, 109, 56), mane=(184, 134, 75), ear=(115, 72, 42),
               dog_face=(128, 84, 49), muzzle=(73, 49, 34), bandage=(202, 193, 167))


def pack(parts, width=128, height=256):
    x = y = row = 0
    for p in parts:
        for c in p['cubes']:
            w, h, d = [math.ceil(v) for v in c['size']]
            rw, rh = 2 * (w + d) + 1, h + d + 1
            if x + rw > width:
                x = 0
                y += row
                row = 0
            if y + rh > height:
                raise ValueError('UV atlas overflow')
            c['uv'] = (x, y)
            c['uv_size'] = (w, h, d)
            x += rw
            row = max(row, rh)
    return width, height


def atlas(parts, size, corruption=0):
    im = Image.new('RGBA', size, (0, 0, 0, 0))
    draw = ImageDraw.Draw(im)
    for p in parts:
        for c in p['cubes']:
            u, v = c['uv']
            w, h, d = c['uv_size']
            mat = c['material']
            rgb = PALETTE[mat]
            for yy in range(v, v + h + d):
                for xx in range(u, u + 2 * (w + d)):
                    shade = ((xx * 19 + yy * 7) % 5 - 2) * 2
                    if mat in ('dress', 'hem', 'shawl'):
                        shade += -9 if (xx - u) % 4 == 0 else 1
                    elif mat in ('hair', 'fur', 'mane', 'ear'):
                        shade += -12 if (xx + yy) % 4 == 0 else 0
                    if corruption and mat in ('skin', 'face', 'jaw'):
                        shade -= int(18 * corruption)
                    draw.point((xx, yy), tuple(max(0, min(255, n + shade)) for n in rgb) + (255,))
            # North face in the Java cuboid UV net.
            fx, fy = u + d, v + d
            if mat == 'face':
                draw.line((fx, fy + 2, fx + 1, fy + 2), fill=(39, 29, 33, 255))
                draw.line((fx + 4, fy + 2, fx + 5, fy + 2), fill=(39, 29, 33, 255))
                draw.point((fx + 1, fy + 3), fill=(204, 194, 180, 255))
                draw.point((fx + 4, fy + 3), fill=(204, 194, 180, 255))
                draw.line((fx + 2, fy + 5, fx + 4, fy + 5), fill=(111, 31, 48, 255))
                if corruption >= .33:
                    draw.line((fx, fy + 3, fx, fy + h - 1), fill=(91, 64, 70, 255))
                    draw.line((fx + w - 1, fy + 3, fx + w - 1, fy + h - 1), fill=(91, 64, 70, 255))
                if corruption >= .66:
                    draw.rectangle((fx, fy + 2, fx + 1, fy + 4), fill=(22, 17, 24, 255))
                    draw.rectangle((fx + 4, fy + 2, fx + 5, fy + 4), fill=(22, 17, 24, 255))
                    draw.line((fx + 1, fy + 6, fx + 4, fy + 6), fill=(49, 20, 30, 255))
            elif mat == 'jaw':
                draw.rectangle((fx, fy, fx + w - 1, fy + h - 1), fill=(49, 22, 32, 255))
                for tooth in range(0, w, 2):
                    draw.line((fx + tooth, fy, fx + tooth, fy + 1), fill=(193, 179, 153, 255))
            elif mat == 'dog_face':
                draw.rectangle((fx + 1, fy + 1, fx + 1, fy + 2), fill=(20, 14, 11, 255))
                draw.rectangle((fx + 4, fy + 1, fx + 4, fy + 2), fill=(20, 14, 11, 255))
                draw.point((fx + 1, fy + 1), fill=(227, 209, 174, 255))
                draw.point((fx + 4, fy + 1), fill=(227, 209, 174, 255))
            elif mat == 'bandage':
                for xx in range(u, u + 2 * (w + d)):
                    if xx % 3 == 0:
                        draw.line((xx, v, xx, v + h + d - 1), fill=(157, 149, 130, 255))
    return im


def f(value):
    return str(round(float(value), 4)) + 'F'


def java_model(parts, class_name, entity_name, file_id, size):
    named = [p for p in parts if p['name'] != 'root']
    lines = [
        'package io.github.knaitoe.theoldesthouse.client;',
        '', 'import io.github.knaitoe.theoldesthouse.TheOldestHouse;',
        'import io.github.knaitoe.theoldesthouse.labyrinth.' + entity_name + ';',
        'import net.minecraft.client.model.HierarchicalModel;',
        'import net.minecraft.client.model.geom.ModelLayerLocation;',
        'import net.minecraft.client.model.geom.ModelPart;',
        'import net.minecraft.client.model.geom.PartPose;',
        'import net.minecraft.client.model.geom.builders.CubeListBuilder;',
        'import net.minecraft.client.model.geom.builders.LayerDefinition;',
        'import net.minecraft.client.model.geom.builders.MeshDefinition;',
        'import net.minecraft.client.model.geom.builders.PartDefinition;',
        'import net.minecraft.resources.ResourceLocation;',
        'import net.minecraft.util.Mth;', '',
        '/** Authored cuboid geometry. Regenerate with tools/generate_mother_assets.py. */',
        'public final class ' + class_name + ' extends HierarchicalModel<' + entity_name + '> {',
        '    public static final ModelLayerLocation LAYER = new ModelLayerLocation(',
        '            ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID, "' + file_id + '"), "main");',
        '    private final ModelPart root;',
        '    public ' + class_name + '(ModelPart baked) { root = baked.getChild("root"); }',
        '    @Override public ModelPart root() { return root; }',
        '    public static LayerDefinition createBodyLayer() {',
        '        MeshDefinition mesh = new MeshDefinition();',
    ]
    for p in parts:
        builder = 'CubeListBuilder.create()'
        for c in p['cubes']:
            builder += '.texOffs(' + ', '.join(map(str, c['uv'])) + ').addBox(' + ', '.join(f(n) for n in [*c['origin'], *c['size']]) + ')'
        parent = 'mesh.getRoot()' if p['parent'] is None else 'p_' + p['parent']
        pose = 'PartPose.offsetAndRotation(' + ', '.join(f(n) for n in [*p['pivot'], *p['rotation']]) + ')'
        lines.append('        PartDefinition p_' + p['name'] + ' = ' + parent + '.addOrReplaceChild("' + p['name'] + '", ' + builder + ', ' + pose + ');')
    lines += ['        return LayerDefinition.create(mesh, ' + str(size[0]) + ', ' + str(size[1]) + ');', '    }',
              '    @Override public void setupAnim(' + entity_name + ' entity, float walk, float amount, float age, float yaw, float pitch) {',
              '        root.getAllParts().forEach(ModelPart::resetPose);',
              '        ModelPart head = root.getChild("head");',
              '        head.yRot = yaw * Mth.DEG_TO_RAD;',
              '        head.xRot = pitch * Mth.DEG_TO_RAD;']
    if entity_name == 'MotherEntity':
        lines += [
            '        float c = entity.shownCorruption();',
            '        root.getChild("body").zScale = 1.0F + c * 0.28F;',
            '        root.getChild("body").xRot = c * 0.18F;',
            '        head.xScale = 1.0F + c * 0.22F;',
            '        head.yScale = 1.0F + c * 0.40F;',
            '        head.xRot += c * 0.28F;',
            '        root.getChild("neck").yScale = 1.0F + c * 0.45F;',
            '        ModelPart jaw = head.getChild("jaw");',
            '        jaw.visible = c > 0.20F;',
            '        jaw.yScale = Math.max(0.1F, c * 1.55F);',
            '        jaw.xRot = c * 0.30F;',
            '        for (String side : new String[]{"left", "right"}) {',
            '            float sign = side.equals("left") ? 1.0F : -1.0F;',
            '            ModelPart arm = root.getChild(side + "_arm");',
            '            arm.xRot = Mth.cos(walk * 0.38F + (sign > 0 ? 0 : Mth.PI)) * amount * 0.25F;',
            '            arm.zRot = -sign * (0.06F + c * 0.18F);',
            '            ModelPart forearm = arm.getChild(side + "_forearm");',
            '            forearm.xRot = entity.isCarrying() ? -1.30F : -0.14F - c * 0.35F;',
            '            ModelPart hand = forearm.getChild(side + "_hand");',
            '            hand.yScale = 1.0F + c * 0.25F;',
            '            for (int i = 0; i < 3; i++) {',
            '                ModelPart finger = hand.getChild(side + "_finger_" + i);',
            '                finger.yScale = 1.0F + c * 0.28F;',
            '                finger.xRot = -0.18F - c * 0.65F + Mth.sin(age * 0.035F + i) * 0.045F;',
            '            }',
            '        }',
            '        root.getChild("shawl").zScale = 1.0F + c * 0.15F;',
            '        root.getChild("skirt_lower").xRot = Mth.sin(walk * 0.25F) * amount * 0.025F;',
            '        root.getChild("body").y += Mth.sin(age * 0.028F) * (0.05F + c * 0.10F);',
        ]
    else:
        lines += [
            '        root.getChild("left_front").xRot = Mth.cos(walk * 0.8F) * amount;',
            '        root.getChild("right_back").xRot = Mth.cos(walk * 0.8F) * amount;',
            '        root.getChild("right_front").xRot = Mth.cos(walk * 0.8F + Mth.PI) * amount;',
            '        root.getChild("left_back").xRot = Mth.cos(walk * 0.8F + Mth.PI) * amount;',
            '        root.getChild("tail").yRot = Mth.sin(age * 0.12F) * 0.16F;',
        ]
    lines += ['    }', '}']
    (JAVA / (class_name + '.java')).write_text('\n'.join(lines) + '\n')


def model_source(parts, texture, name):
    output = dict(format='oldest-house-cuboid-model-v1',
                  coordinates='Minecraft ModelPart: pixels, local parent pivots, positive Y down',
                  texture=dict(width=texture.width, height=texture.height, path=name + '.png'),
                  parts=parts)
    (ART / (name + '.model.json')).write_text(json.dumps(output, indent=2) + '\n')


def rot(rx, ry, rz):
    sx, cx, sy, cy, sz, cz = math.sin(rx), math.cos(rx), math.sin(ry), math.cos(ry), math.sin(rz), math.cos(rz)
    return np.array([[cz,-sz,0],[sz,cz,0],[0,0,1]]) @ np.array([[cy,0,sy],[0,1,0],[-sy,0,cy]]) @ np.array([[1,0,0],[0,cx,-sx],[0,sx,cx]])


def render(parts, texture, corruption=0, width=360, height=660, scale=17, offset=(180,590)):
    canvas = Image.new('RGBA', (width,height), (24,23,28,255))
    transforms, faces = {}, []
    view = rot(-.08, -.38, 0)
    for p in parts:
        pivot = np.array(p['pivot'],dtype=float)
        angles = list(p['rotation']); axes = np.ones(3)
        if p['name']=='head':
            axes = np.array([1+corruption*.22,1+corruption*.4,1])
            angles[0] += corruption*.28
        if p['name']=='jaw':
            if corruption<=.2: continue
            axes[1] = max(.1, corruption*1.55); angles[0] += corruption*.3
        if p['name']=='neck': axes[1] += corruption*.45
        if p['name']=='body': axes[2] += corruption*.28; angles[0] += corruption*.18
        if p['name'].endswith('_arm'): angles[2] *= 1+corruption*3
        if p['name'].endswith('_forearm'): angles[0] -= corruption*.35
        if '_finger_' in p['name']: axes[1] += corruption*.28; angles[0] -= corruption*.65
        local = np.eye(4); local[:3,:3] = rot(*angles) @ np.diag(axes); local[:3,3] = pivot
        transform = (transforms[p['parent']] @ local) if p['parent'] else local
        transforms[p['name']] = transform
        for c in p['cubes']:
            x,y,z = c['origin']; w,h,d = c['size']
            corners = np.array([[x,y,z,1],[x+w,y,z,1],[x+w,y+h,z,1],[x,y+h,z,1],
                                [x,y,z+d,1],[x+w,y,z+d,1],[x+w,y+h,z+d,1],[x,y+h,z+d,1]])
            world = (transform @ corners.T).T[:,:3]
            world[:,1] = 24-world[:,1]
            camera = (view @ world.T).T
            points = [(offset[0]+p[0]*scale,offset[1]-p[1]*scale) for p in camera]
            u,v=c['uv']; uw,uh,ud=c['uv_size']
            quads=[([0,1,2,3],(u+ud,v+ud,u+ud+uw,v+ud+uh),1.0),
                   ([1,5,6,2],(u+ud+uw,v+ud,u+ud+uw+ud,v+ud+uh),.7),
                   ([4,0,3,7],(u,v+ud,u+ud,v+ud+uh),.7),
                   ([5,4,7,6],(u+ud+uw+ud,v+ud,u+2*(ud+uw),v+ud+uh),.8),
                   ([4,5,1,0],(u+ud,v,u+ud+uw,v+ud),1.12),
                   ([3,2,6,7],(u+ud+uw,v,u+ud+2*uw,v+ud),.6)]
            for indices,uv,shade in quads:
                face=[points[i] for i in indices]
                cross=(face[1][0]-face[0][0])*(face[2][1]-face[0][1])-(face[1][1]-face[0][1])*(face[2][0]-face[0][0])
                if cross<=0: continue
                faces.append((sum(camera[i][2] for i in indices)/4,face,uv,shade))
    for depth,face,uv,shade in sorted(faces, key=lambda x:-x[0]):
        tex=texture.crop(uv)
        arr=np.asarray(tex).copy(); arr[:,:,:3]=(arr[:,:,:3]*shade).clip(0,255).astype(np.uint8); tex=Image.fromarray(arr)
        # Inverse project the source rectangle into this exact face quadrilateral.
        source=[(0,0),(tex.width,0),(tex.width,tex.height),(0,tex.height)]
        matrix=[]; target=[]
        for (xx,yy),(sx,sy) in zip(face,source):
            matrix.append([xx,yy,1,0,0,0,-sx*xx,-sx*yy]); target.append(sx)
            matrix.append([0,0,0,xx,yy,1,-sy*xx,-sy*yy]); target.append(sy)
        try: coeff=np.linalg.solve(np.array(matrix),np.array(target))
        except np.linalg.LinAlgError: continue
        warped=tex.transform(canvas.size,Image.Transform.PERSPECTIVE,coeff,Image.Resampling.NEAREST)
        mask=Image.new('L',canvas.size); ImageDraw.Draw(mask).polygon(face,fill=255)
        alpha=np.minimum(np.array(warped.getchannel('A')),np.array(mask)); warped.putalpha(Image.fromarray(alpha))
        canvas.alpha_composite(warped)
    return canvas


msize=pack(mother); dsize=pack(dog)
textures=[]
for stage in range(4):
    texture=atlas(mother,msize,stage/3)
    texture.save(ASSETS / ('mother_of_strays' + ('' if stage==0 else '_' + str(stage)) + '.png'))
    textures.append(texture)
dog_texture=atlas(dog,dsize)
dog_texture.save(ASSETS / 'mother_pekingese.png')
java_model(mother,'MotherModel','MotherEntity','mother_of_strays',msize)
java_model(dog,'MotherPekingeseModel','MotherPekingese','mother_pekingese',dsize)
model_source(mother,textures[0],'mother_of_strays')
model_source(dog,dog_texture,'mother_pekingese')
preview=Image.new('RGBA',(1320,800),(24,23,28,255))
draw=ImageDraw.Draw(preview)
font=ImageFont.truetype('/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf',19)
small=ImageFont.truetype('/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf',14)
for i,stage in enumerate((0,2,3)):
    preview.alpha_composite(render(mother,textures[stage],stage/3,width=440,height=680,scale=14,offset=(220,610)), (i*440,44))
    draw.text((i*440+24,20),['Ordinary appearance','Growing distortion','Before reclamation'][i],font=font,fill=(211,197,180,255))
preview.alpha_composite(render(dog,dog_texture,width=250,height=180,scale=8,offset=(130,145)),(1060,600))
draw.text((24,760),'Exact model geometry and UV texture preview. In-game lighting will vary.',font=small,fill=(157,145,140,255))
preview.convert('RGB').save(ART / 'mother_model_preview.png')
print(json.dumps(dict(mother_parts=len(mother),mother_cubes=sum(len(p['cubes']) for p in mother),
                      dog_parts=len(dog),atlas=msize,preview=str(ART / 'mother_model_preview.png'))))

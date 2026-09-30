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
MOTHER_STAGES = 16
MOTHER_RESOLUTION = 4
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
    part('hair', 'head', (0, 0, .2), cubes=[cube('platinum_crown', (-3.5, -7, -2.6), (7, 2, 6), 'hair'),
        cube('platinum_back', (-3.5, -5, 2.5), (7, 6, 1), 'hair'),
        cube('left_bob', (-3.6, -5, -2.4), (1, 5, 5), 'hair'),
        cube('right_bob', (2.8, -5, -2.4), (1, 5, 5), 'hair')]),
    part('jaw', 'head', (0, .2, -.2), cubes=[cube('lengthening_chin', (-3, 0, -3), (6, 3, 6), 'jaw')]),
    part('shawl', pivot=(0, -22.5, .2), cubes=[cube('shawl', (-5, -.5, -3), (10, 5, 7), 'shawl')]),
    part('collar', pivot=(0, -22, -3.2), cubes=[cube('open_neckline', (-2.5, 0, -.5), (5, 2, 1), 'skin')]),
    part('skirt_upper', pivot=(0, -13, 0), cubes=[cube('upper_skirt', (-5, -1, -3), (10, 6, 6), 'dress')]),
    part('skirt_lower', pivot=(0, -8, 0), cubes=[cube('lower_skirt', (-6, 0, -4), (12, 7, 8), 'hem')]),
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

PALETTE = dict(dress=(38, 32, 42), shawl=(69, 35, 48), hem=(29, 24, 34),
               skin=(202, 172, 151), face=(212, 182, 162), jaw=(206, 176, 156),
               hair=(232, 231, 221), lace=(103, 66, 74), nail=(112, 37, 56), shoe=(32, 24, 23),
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


def dog_atlas(parts, size):
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
                    draw.point((xx, yy), tuple(max(0, min(255, n + shade)) for n in rgb) + (255,))
            # North face in the Java cuboid UV net.
            fx, fy = u + d, v + d
            if mat == 'dog_face':
                draw.rectangle((fx + 1, fy + 1, fx + 1, fy + 2), fill=(20, 14, 11, 255))
                draw.rectangle((fx + 4, fy + 1, fx + 4, fy + 2), fill=(20, 14, 11, 255))
                draw.point((fx + 1, fy + 1), fill=(227, 209, 174, 255))
                draw.point((fx + 4, fy + 1), fill=(227, 209, 174, 255))
            elif mat == 'bandage':
                for xx in range(u, u + 2 * (w + d)):
                    if xx % 3 == 0:
                        draw.line((xx, v, xx, v + h + d - 1), fill=(157, 149, 130, 255))
    return im


def blend(start, end, amount):
    return tuple(round(a + (b - a) * amount) for a, b in zip(start, end))


def ease(value, start=0, end=1):
    t = max(0, min(1, (value - start) / (end - start)))
    return t * t * (3 - 2 * t)


def mother_atlas(parts, size, corruption=0):
    """Paint the authored UV net at 4x resolution; Java still uses logical UV units.

    Johnnie's platinum hair, heavy eyeliner, petite figure and full bust are
    grounded in House of Leaves, footnote 249, pp. 266-267. Costume, bob, makeup
    colours and the supernatural progression are this mod's interpretation.
    """
    resolution = MOTHER_RESOLUTION
    im = Image.new('RGBA', tuple(n * resolution for n in size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(im)
    fatigue = ease(corruption, .08, .82)
    strain = ease(corruption, .3, 1)
    late = ease(corruption, .62, 1)
    for p in parts:
        for c in p['cubes']:
            u, v = [n * resolution for n in c['uv']]
            w, h, d = [n * resolution for n in c['uv_size']]
            mat = c['material']
            rgb = PALETTE[mat]
            if mat in ('skin', 'face', 'jaw'):
                rgb = blend(rgb, (180, 176, 166), corruption * .6)
            elif mat == 'hair':
                rgb = blend(rgb, (217, 220, 215), corruption * .55)
            elif mat == 'nail':
                rgb = blend(rgb, (75, 46, 55), strain * .65)
            for yy in range(v, v + h + d):
                for xx in range(u, u + 2 * (w + d)):
                    shade = ((xx * 19 + yy * 7) % 5 - 2)
                    if mat in ('dress', 'hem', 'shawl'):
                        shade += -7 if (xx - u) % (4 * resolution) < 2 else 1
                        shade += -2 if (yy - v) % 3 == 0 else 0
                    elif mat == 'hair':
                        shade += -10 if (xx - u) % (2 * resolution) < 2 else 1
                    if mat in ('skin', 'face', 'jaw'):
                        shade -= round(2 * fatigue * ((xx + yy) % 3))
                    draw.point((xx, yy), tuple(max(0, min(255, n + shade)) for n in rgb) + (255,))
            fx, fy = u + d, v + d
            if mat == 'face':
                # 24 x 28 px face. Retain small visible eyes throughout all stages.
                # Author facial strokes at subpixel precision so an eyelid or lip
                # does not jump a whole texel when crossing an integer position.
                detail_scale = 4
                front = im.crop((fx, fy, fx+w, fy+h)).resize((w*detail_scale,h*detail_scale),Image.Resampling.NEAREST)
                face_draw = ImageDraw.Draw(front)
                def line(coords, colour, width=1):
                    face_draw.line([(round(x*detail_scale),round(y*detail_scale)) for x,y in coords],
                                   fill=(*colour,255),width=width*detail_scale)

                shadow = blend(rgb, (132, 119, 123), .08 + fatigue * .42)
                crease = blend(rgb, (112, 105, 110), strain * .54)
                liner = blend((38, 29, 38), (46, 43, 49), corruption)
                eye_white = blend((228, 222, 208), (184, 187, 178), fatigue * .8)
                iris = blend((75, 83, 84), (145, 155, 149), late * .7)
                for ex, drift in ((3, 0), (15, strain * 2)):
                    line([(ex, 14 + drift), (ex + 5, 14 + drift)], shadow)
                    line([(ex, 6), (ex + 5, 5)], (111, 91, 85))
                    line([(ex - 1, 9 + drift), (ex + 6, 9 + drift)], liner, 2)
                    line([(ex, 10 + drift), (ex + 5, 10 + drift)], eye_white, 2)
                    line([(ex + 2, 10 + drift), (ex + 2, 11 + drift)], iris)
                    line([(ex, 12 + drift), (ex + 5, 12 + drift)], liner)
                line([(1, 8), (2, 9)], liner)
                line([(22, 8 + strain * 2), (21, 9 + strain * 2)], liner)
                blush = blend(rgb, (194, 118, 116), .2 * (1 - fatigue))
                line([(3, 18), (5, 18)], blush, 2)
                line([(18, 18), (20, 18)], blush, 2)
                line([(2, 16), (3, 19), (5, 22)], crease)
                line([(21, 16), (20, 20), (19, 23)], crease)
                line([(5, 3), (7, 4), (8, 6)], blend(rgb, (139, 128, 126), strain * .33))
                extension = late * 3
                droop = strain * 2
                lip = blend((132, 52, 69), (100, 65, 72), fatigue * .7)
                line([(8 - extension, 22), (12, 21), (16 + extension, 22 + droop)], lip, 2)
                line([(9 - extension, 23), (14, 23), (16 + extension, 22 + droop)],
                     blend((158, 79, 89), (128, 98, 99), fatigue * .7))
                line([(8 - extension, 22), (12, 22), (16 + extension, 22 + droop)],
                     blend((76, 37, 47), (64, 52, 59), fatigue * .5))
                # Fine makeup fissures emerge by colour, rather than a sudden scar.
                line([(5, 12), (6, 15), (5, 16)], blend(rgb, (106, 107, 112), late * .45))
                line([(18, 14 + droop), (17, 18), (18, 20)], blend(rgb, (106, 107, 112), late * .45))
                im.paste(front.resize((w,h),Image.Resampling.BOX),(fx,fy))
            elif mat == 'jaw':
                chin = blend(rgb, (151, 145, 144), strain * .3)
                draw.rectangle((fx, fy, fx + w - 1, fy + h - 1), fill=(*chin, 255))
                draw.line((fx + 3, fy + h - 3, fx + w - 4, fy + h - 3),
                          fill=(*blend(chin, (109, 101, 106), late * .3), 255))
            elif mat == 'nail':
                draw.line((fx, fy, fx, fy + h - 1), fill=(*blend(rgb, (202, 137, 141), .4), 255))
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
            '        float raw = Mth.clamp(entity.shownCorruption(), 0.0F, 1.0F);',
            '        float t = Mth.clamp((raw - 0.20F) / 0.80F, 0.0F, 1.0F);',
            '        float c = t * t * (3.0F - 2.0F * t);',
            '        float jt = Mth.clamp((raw - 0.74F) / 0.26F, 0.0F, 1.0F);',
            '        float j = jt * jt * (3.0F - 2.0F * jt);',
            '        root.xScale = root.zScale = 0.90F;',
            '        root.yScale = 0.86F;',
            '        root.getChild("body").zScale = 1.0F + c * 0.08F;',
            '        root.getChild("body").xRot = c * 0.08F;',
            '        head.xScale = 1.0F + c * 0.08F;',
            '        head.yScale = 1.0F + c * 0.14F;',
            '        head.xRot += c * 0.13F;',
            '        root.getChild("neck").yScale = 1.0F + c * 0.15F;',
            '        ModelPart jaw = head.getChild("jaw");',
            '        jaw.visible = raw > 0.74F;',
            '        jaw.yScale = 0.18F + j * 0.32F;',
            '        jaw.xRot = j * 0.08F;',
            '        for (String side : new String[]{"left", "right"}) {',
            '            float sign = side.equals("left") ? 1.0F : -1.0F;',
            '            ModelPart arm = root.getChild(side + "_arm");',
            '            arm.xRot = Mth.cos(walk * 0.38F + (sign > 0 ? 0 : Mth.PI)) * amount * 0.25F;',
            '            arm.zRot = -sign * (0.06F + c * 0.09F);',
            '            ModelPart forearm = arm.getChild(side + "_forearm");',
            '            forearm.xRot = entity.isCarrying() ? -1.90F : -0.14F - c * 0.16F;',
            '            ModelPart hand = forearm.getChild(side + "_hand");',
            '            hand.yScale = 1.0F + c * 0.08F;',
            '            for (int i = 0; i < 3; i++) {',
            '                ModelPart finger = hand.getChild(side + "_finger_" + i);',
            '                finger.yScale = 1.0F + c * 0.14F;',
            '                finger.xRot = -0.18F - c * 0.30F + Mth.sin(age * 0.035F + i) * 0.045F;',
            '            }',
            '        }',
            '        root.getChild("shawl").zScale = 1.0F + c * 0.05F;',
            '        root.getChild("skirt_lower").xRot = Mth.sin(walk * 0.25F) * amount * 0.025F;',
            '        root.getChild("body").y += Mth.sin(age * 0.028F) * (0.05F + raw * 0.035F);',
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


def model_source(parts, texture, name, logical_size=None):
    output = dict(format='oldest-house-cuboid-model-v1',
                  coordinates='Minecraft ModelPart: pixels, local parent pivots, positive Y down',
                  texture=dict(width=texture.width, height=texture.height, path=name + '.png'),
                  parts=parts)
    if logical_size:
        output['texture'].update(uv_width=logical_size[0], uv_height=logical_size[1])
        output['texture']['stages'] = [dict(stage=i, corruption=i / (MOTHER_STAGES - 1),
            path=name + ('' if i == 0 else '_' + str(i)) + '.png') for i in range(MOTHER_STAGES)]
    (ART / (name + '.model.json')).write_text(json.dumps(output, indent=2) + '\n')


def rot(rx, ry, rz):
    sx, cx, sy, cy, sz, cz = math.sin(rx), math.cos(rx), math.sin(ry), math.cos(ry), math.sin(rz), math.cos(rz)
    return np.array([[cz,-sz,0],[sz,cz,0],[0,0,1]]) @ np.array([[cy,0,sy],[0,1,0],[-sy,0,cy]]) @ np.array([[1,0,0],[0,cx,-sx],[0,sx,cx]])


def render(parts, texture, corruption=0, width=360, height=660, scale=17, offset=(180,590)):
    canvas = Image.new('RGBA', (width,height), (24,23,28,255))
    transforms, faces = {}, []
    view = rot(-.08, -.38, 0)
    deformation = ease(corruption, .2, 1)
    j = ease(corruption, .74, 1)
    texture_scale = MOTHER_RESOLUTION if parts is mother else 1
    for p in parts:
        pivot = np.array(p['pivot'],dtype=float)
        angles = list(p['rotation']); axes = np.ones(3)
        if parts is mother:
            if p['name']=='root': axes = np.array([.9,.86,.9])
            if p['name']=='head':
                axes = np.array([1+deformation*.08,1+deformation*.14,1])
                angles[0] += deformation*.13
            if p['name']=='jaw':
                if corruption<=.74: continue
                axes[1] = .18+j*.32; angles[0] += j*.08
            if p['name']=='neck': axes[1] += deformation*.15
            if p['name']=='body': axes[2] += deformation*.08; angles[0] += deformation*.08
            if p['name']=='shawl': axes[2] += deformation*.05
            if p['name'].endswith('_arm'): angles[2] *= 1+deformation*1.5
            if p['name'].endswith('_forearm'): angles[0] -= deformation*.16
            if p['name'].endswith('_hand'): axes[1] += deformation*.08
            if '_finger_' in p['name']: axes[1] += deformation*.14; angles[0] -= deformation*.30
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
        tex=texture.crop(tuple(value * texture_scale for value in uv))
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
for stage in range(MOTHER_STAGES):
    texture=mother_atlas(mother,msize,stage/(MOTHER_STAGES-1))
    texture.save(ASSETS / ('mother_of_strays' + ('' if stage==0 else '_' + str(stage)) + '.png'))
    textures.append(texture)
dog_texture=dog_atlas(dog,dsize)
dog_texture.save(ASSETS / 'mother_pekingese.png')
java_model(mother,'MotherModel','MotherEntity','mother_of_strays',msize)
java_model(dog,'MotherPekingeseModel','MotherPekingese','mother_pekingese',dsize)
model_source(mother,textures[0],'mother_of_strays',msize)
model_source(dog,dog_texture,'mother_pekingese')
preview=Image.new('RGBA',(1320,800),(24,23,28,255))
draw=ImageDraw.Draw(preview)
font=ImageFont.truetype('/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf',19)
small=ImageFont.truetype('/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf',14)
for i,stage in enumerate((0,5,10,15)):
    preview.alpha_composite(render(mother,textures[stage],stage/15,width=330,height=680,scale=16,offset=(150,610)), (i*330,44))
    draw.text((i*330+20,20),['Ordinary appearance','First unease','Growing strain','Before reclamation'][i],font=font,fill=(211,197,180,255))
    draw.text((i*330+20,718),f'Stage {stage+1} / {MOTHER_STAGES}',font=small,fill=(174,160,155,255))
draw.text((24,760),'Exact model geometry and UV texture preview. In-game lighting will vary.',font=small,fill=(157,145,140,255))
preview.convert('RGB').save(ART / 'mother_model_preview.png')

# Full progression, including magnified samples of the actual face UVs.
sheet = Image.new('RGBA', (1280, 1720), (24, 23, 28, 255))
sheet_draw = ImageDraw.Draw(sheet)
face_cube = next(c for p in mother for c in p['cubes'] if c['name'] == 'face')
u, v = face_cube['uv']; w, h, d = face_cube['uv_size']
face_box = tuple(n * MOTHER_RESOLUTION for n in (u+d, v+d, u+d+w, v+d+h))
for stage, texture in enumerate(textures):
    x, y = (stage % 4) * 320, (stage // 4) * 420
    sheet.alpha_composite(render(mother,texture,stage/15,width=320,height=374,scale=10.5,offset=(205,344)),(x,y+34))
    face = texture.crop(face_box).resize((96,112),Image.Resampling.NEAREST)
    sheet.alpha_composite(face,(x+14,y+115))
    sheet_draw.text((x+16,y+10),f'{stage+1:02d} / {MOTHER_STAGES}',font=font,fill=(211,197,180,255))
    sheet_draw.text((x+14,y+237),'Face UV',font=small,fill=(157,145,140,255))
sheet_draw.text((16,1690),'All sixteen textures with continuous model deformation. Same ordinary form returns after reclamation.',font=small,fill=(157,145,140,255))
sheet.convert('RGB').save(ART / 'mother_texture_stages.png')
print(json.dumps(dict(mother_parts=len(mother),mother_cubes=sum(len(p['cubes']) for p in mother),
                      dog_parts=len(dog),atlas=textures[0].size,logical_uv=msize,stages=MOTHER_STAGES,
                      preview=str(ART / 'mother_model_preview.png'))))

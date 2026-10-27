"""Native pixel painting fitted to the Backseat creature's asymmetric, non-player mesh."""
from PIL import Image, ImageDraw

NETS = {
    'head': (0, 0, 6, 10, 5), 'neck': (24, 0, 3, 6, 3),
    'upper_neck': (38, 0, 2, 5, 2), 'left_shoulder': (0, 18, 7, 3, 5),
    'right_shoulder': (26, 18, 6, 3, 5), 'chest': (0, 29, 9, 7, 4),
    'belly': (28, 29, 5, 7, 3), 'arm': (48, 18, 3, 10, 3),
    'forearm': (48, 33, 2, 11, 2), 'thigh': (0, 43, 3, 8, 3),
    'shin': (14, 43, 2, 9, 2), 'jaw': (24, 43, 4, 3, 4),
    'finger': (42, 48, 1, 4, 1),
}


def net(u, v, w, h, d):
    return {'top': (u+d,v,u+d+w,v+d), 'bottom': (u+d+w,v,u+d+2*w,v+d),
            'right': (u,v+d,u+d,v+d+h), 'front': (u+d,v+d,u+d+w,v+d+h),
            'left': (u+d+w,v+d,u+2*d+w,v+d+h), 'back': (u+2*d+w,v+d,u+2*d+2*w,v+d+h)}


def paint():
    atlas = Image.new('RGBA', (64, 64))
    for name, dims in NETS.items():
        for side, box in net(*dims).items():
            w, h = box[2]-box[0], box[3]-box[1]
            skin = Image.new('RGBA', (w, h), '#959c86')
            d = ImageDraw.Draw(skin)
            # Broad dry hide folds and mineral-coloured patches, without clothes or hair.
            if h > 3:
                d.line((0, 1, 0, h-2), fill='#5d665d')
                d.line((max(0,w-2), 2, max(0,w-2), min(h-1,5)), fill='#b7b99b')
                for y in range(3, h, 4):
                    d.line((1 if w>2 else 0,y,min(w-1,3),y), fill='#788171')
            if name in ('left_shoulder','right_shoulder','chest','belly'):
                d.line((0, h//2, w-1, max(0,h//2-1)), fill='#5d665d')
                if w>3:d.line((1,max(0,h//2-1),w-2,max(0,h//2-2)), fill='#d0c9a7')
            if name == 'head':
                d.rectangle((0,0,w-1,max(0,h//3)), fill='#b7b99b')
                if side == 'front':
                    d.rectangle((0,0,w-1,h-1), fill='#b7b99b')
                    d.rectangle((0,0,1,2), fill='#959c86')
                    d.point((4,3), fill='#d0c9a7')
                    d.rectangle((0,5,1,6), fill='#252c27')
                    d.rectangle((4,6,5,7), fill='#171b19')
                    d.line((2,8,2,9), fill='#252c27')
                    d.point((3,9), fill='#5d665d')
                elif side == 'back':
                    d.line((w//2,2,w//2,h-1),fill='#788171')
            if name == 'jaw' and side == 'front':
                d.line((1,0,1,h-1),fill='#252c27')
                d.point((2,h-1),fill='#5d665d')
            if name == 'finger':d.line((0,max(0,h-2),w-1,h-1),fill='#5d665d')
            atlas.paste(skin, box[:2])
    return atlas.resize((256,256),Image.Resampling.NEAREST)

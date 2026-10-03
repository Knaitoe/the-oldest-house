"""Integrate generated cloth materials into the standard 64x64 Minecraft skin UV net."""
from pathlib import Path
from PIL import Image,ImageDraw
ROOT=Path(__file__).resolve().parents[1]
material=Image.open(ROOT/'art/prisoner/materials.png').convert('RGB')
tiles=[material.crop((x*128+16,y*128+16,x*128+112,y*128+112)).resize((16,16),Image.Resampling.BOX) for y in range(2) for x in range(2)]
skin=Image.new('RGBA',(64,64));draw=ImageDraw.Draw(skin)
def fill(box,tile):
    for y in range(box[1],box[3]):
        for x in range(box[0],box[2]):skin.putpixel((x,y),tiles[tile].getpixel((x%16,y%16))+(255,))
fill((0,0,32,16),0)
for box in [(16,16,40,32),(40,16,56,32),(32,48,48,64)]:fill(box,1)
for box in [(0,16,16,32),(16,48,32,64)]:fill(box,2)
for box in [(0,28,16,32),(16,60,32,64)]:fill(box,3)
# Faceless by design: folded, stitched cloth, rather than an unfinished beige square.
draw.line((8,8,11,12,8,15),fill=(104,105,100,255));draw.line((15,8,12,12,15,15),fill=(147,146,134,255))
draw.line((8,8,15,8),fill=(191,190,173,255));draw.line((9,14,14,14),fill=(134,135,127,255))
for y in (20,23,26):draw.point((23,y),fill=(49,61,66,255))
draw.line((22,19,22,29),fill=(98,110,112,255));draw.line((20,29,27,29),fill=(77,91,97,255))
for box in [(44,28,47,31),(36,60,39,63)]:draw.rectangle(box,fill=(139,137,124,255))
draw.line((5,28,5,30),fill=(49,36,29,255));draw.line((21,60,21,62),fill=(49,36,29,255))
path=ROOT/'src/main/resources/assets/the_oldest_house/textures/entity/caged_child.png';skin.save(path,optimize=True)

"""0.4.764: the porch's shapes (HouseReadPorch). Only ever a shape: the silhouette's own pixel map, every opaque pixel dark,
with a slightly lighter rim so the shape reads against a lit room. Run from the repository root."""
from PIL import Image
SRC='src/main/resources/assets/the_oldest_house/textures/entity/literary_silhouette.png'
OUT='src/main/resources/assets/the_oldest_house/textures/entity/literary_read_cook.png'
im=Image.open(SRC).convert('RGBA');w,h=im.size;px=im.load()
out=Image.new('RGBA',(w,h),(0,0,0,0));o=out.load()
for y in range(h):
    for x in range(w):
        r,g,b,a=px[x,y]
        if a==0:continue
        edge=any(not(0<=x+dx<w and 0<=y+dy<h) or px[x+dx,y+dy][3]==0 for dx,dy in((1,0),(-1,0),(0,1),(0,-1)))
        n=(x*7+y*13)%5
        v=24+n if edge else 15+n//2
        o[x,y]=(v,v,v+3,255)
out.save(OUT)

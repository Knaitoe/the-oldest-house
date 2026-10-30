"""Quiet shelter strangers on the mod's shared, explicit Minecraft character grid."""
from pathlib import Path
from dataclasses import replace
from PIL import ImageDraw
import argparse,io
from paint_character_skins import Look,paint
ROOT=Path(__file__).resolve().parents[1];OUT=ROOT/'src/main/resources/assets/the_oldest_house/textures/entity'
def face_px(d,x,y,fill):
 """A pixel of the face as the painter draws it (16 to the face) on the shipped sheet (four to a texel, the face at 32,32)."""
 d.rectangle((32+2*x,32+2*y,33+2*x,33+2*y),fill=fill)
def feminine(im,look):
 """Lashes, a rose mouth and a gold clip on the face; long hair down the back (the body's back at 128,80) and a lock forward
 over each shoulder (its front at 80,80), four pixels to a texel."""
 d=ImageDraw.Draw(im);hair=look.hair
 for x in (2,13):face_px(d,x,6,'#241e1c')
 for x in range(6,10):face_px(d,x,13,'#7f3f45');face_px(d,x,14,'#a35a5e')
 for x in (2,3,4):face_px(d,x,3,'#f0d68a' if x==3 else '#d9b65a')
 d.polygon([(130,80),(157,80),(157,99),(151,105),(136,105),(130,99)],fill=hair)
 for x in (80,108):d.rectangle((x,80,x+3,95),fill=hair)
def outputs():
 out={}
 looks={'man':Look(skin='#bd9479',hair='#443b30',cut='part',cloth='#506c80',pants='#4f5048',style='jacket',accent='#c4bea4',eyes='#69745f',sleeve=10), 'woman':Look(skin='#ae7e61',hair='#322d29',cut='long',cloth='#8c3f4c',pants='#3a2e2c',style='dress',accent='#e2d2b4',eyes='#5e6756',sleeve=10)}
 # 0.4.172 (owner: "The girl ... should look more traditionally feminine for easier gender recognition"): a wine-red dress with a
 # cream sash and a long skirt (the cast model's gown), dark tights, long hair down over her shoulders and back, a gold clip,
 # lashes at the outer corners and a rose mouth.
 # 0.4.159: the same two strangers around forty and older, for readers the waiting room read as older; clothes unchanged.
 grey={'man':{1:'#5b5248',2:'#9a958c'},'woman':{1:'#4a4039',2:'#a29a92'}}
 for who,look in list(looks.items()):
  for age,suffix in [(1,'_40'),(2,'_old')]:looks[who+suffix]=replace(look,hair=grey[who][age],age=age)
 for who,look in looks.items():
  for smile in [False,True]:
   im=paint(look)
   if who.startswith('woman'):feminine(im,look)
   if smile:
    # A single raised mouth corner on the real front-face UV; never a painted portrait. 0.4.172: drawn at the sheet's own scale;
    # it had landed off the face, in an unused corner of the sheet.
    d=ImageDraw.Draw(im);face_px(d,10,12,'#7f3f45' if who.startswith('woman') else '#84634f')
   buf=io.BytesIO();im.save(buf,format='PNG');out['literary_read_'+who+('_smile' if smile else '')+'.png']=buf.getvalue()
 return out
if __name__=='__main__':
 p=argparse.ArgumentParser(description=__doc__);p.add_argument('--check',action='store_true');args=p.parse_args()
 for name,raw in outputs().items():
  if args.check:assert (OUT/name).read_bytes()==raw,name
  else:(OUT/name).write_bytes(raw)
 print('Twelve shelter sheets match the shared native character painter.' if args.check else 'Painted twelve native shelter sheets.')

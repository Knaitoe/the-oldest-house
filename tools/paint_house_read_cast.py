"""Quiet shelter strangers on the mod's shared, explicit Minecraft character grid."""
from pathlib import Path
from dataclasses import replace
from PIL import ImageDraw
import argparse,io
from paint_character_skins import Look,paint
ROOT=Path(__file__).resolve().parents[1];OUT=ROOT/'src/main/resources/assets/the_oldest_house/textures/entity'
def outputs():
 out={}
 looks={'man':Look(skin='#bd9479',hair='#443b30',cut='part',cloth='#506c80',pants='#4f5048',style='jacket',accent='#c4bea4',eyes='#69745f',sleeve=10), 'woman':Look(skin='#ae7e61',hair='#322d29',cut='braids',cloth='#90967e',pants='#4d6070',style='knit',accent='#d5c4a5',eyes='#5e6756',sleeve=10)}
 # 0.4.159: the same two strangers around forty and older, for readers the waiting room read as older; clothes unchanged.
 grey={'man':{1:'#5b5248',2:'#9a958c'},'woman':{1:'#4a4039',2:'#a29a92'}}
 for who,look in list(looks.items()):
  for age,suffix in [(1,'_40'),(2,'_old')]:looks[who+suffix]=replace(look,hair=grey[who][age],age=age)
 for who,look in looks.items():
  for smile in [False,True]:
   im=paint(look)
   if smile:
    # A single raised mouth corner on the real front-face UV; never a painted portrait.
    d=ImageDraw.Draw(im);d.point((26,28),fill='#84634f');d.point((26,29),fill=look.skin)
   buf=io.BytesIO();im.save(buf,format='PNG');out['literary_read_'+who+('_smile' if smile else '')+'.png']=buf.getvalue()
 return out
if __name__=='__main__':
 p=argparse.ArgumentParser(description=__doc__);p.add_argument('--check',action='store_true');args=p.parse_args()
 for name,raw in outputs().items():
  if args.check:assert (OUT/name).read_bytes()==raw,name
  else:(OUT/name).write_bytes(raw)
 print('Twelve shelter sheets match the shared native character painter.' if args.check else 'Painted twelve native shelter sheets.')

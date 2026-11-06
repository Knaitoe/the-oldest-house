"""The bedside exorcism's actual writing desk: wood, a drawer recess, and four legs. Existing pixel materials only."""
from pathlib import Path
import argparse,json
ROOT=Path(__file__).resolve().parents[1]
A=ROOT/'src/main/resources/assets/the_oldest_house'
def box(a,b):return {'from':a,'to':b,'faces':{f:{'texture':'#wood','uv':[0,0,16,16]} for f in ['north','south','east','west','up','down']}}
def main():
 parser=argparse.ArgumentParser();parser.add_argument('--check',action='store_true');args=parser.parse_args()
 elements=[box([0,13,1],[16,15,15]),box([1,11,12],[15,13,14])]
 elements += [box([x,0,z],[x+2,13,z+2]) for x in [1,13] for z in [2,12]]
 model={'textures':{'particle':'the_oldest_house:block/ghosts/wood','wood':'the_oldest_house:block/ghosts/wood'},'elements':elements,'ambientocclusion':True}
 target=A/'models/block/ghosts_writing_desk.json';states=A/'blockstates/ghosts_set.json';state=json.loads(states.read_text())
 expected={f'kind=writing_desk,facing={f}':{'model':'the_oldest_house:block/ghosts_writing_desk','y':y} for f,y in [('north',0),('east',90),('south',180),('west',270)]}
 if args.check:
  assert json.loads(target.read_text())==model
  for key,value in expected.items():assert state['variants'].get(key)==value,key
 else:
  target.write_text(json.dumps(model,indent=2)+'\n');state['variants'].update(expected);states.write_text(json.dumps(state,indent=2)+'\n')
 print(('Verified' if args.check else 'Drew')+' the Ghosts writing desk and its four native states.')
if __name__=='__main__':main()

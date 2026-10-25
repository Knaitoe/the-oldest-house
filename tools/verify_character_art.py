"""Check the historical baseline, shared-painter skins and native production mesh catalog."""
from pathlib import Path
from io import BytesIO
import hashlib
import json
import argparse
import zipfile
from PIL import Image
from paint_character_skins import LOOKS, net
from character_art_catalog import entries

ROOT=Path(__file__).resolve().parents[1]
CATALOG=ROOT/'art/inventory/character_review_04100.json'


def main():
    parser=argparse.ArgumentParser();parser.add_argument('--jar',action='store_true');args=parser.parse_args()
    review=json.loads(CATALOG.read_text());changed={r['path']:r for r in review['repainted']}
    # Preserve the historical 0.4.100 inventory, including its original machine atlas.
    # The five later Drisking human sheets are now explicitly included in LOOKS below.
    assert review['excluded']==['Borrasca']
    assert set(changed).isdisjoint(review['unchanged_png_sha256'])
    assert len(changed)+len(review['unchanged_png_sha256'])==review['png_count']==769
    packaged=None
    if args.jar:
        jars=[p for p in (ROOT/'build/libs').glob('this_house_has_ghosts_in_it-*.jar') if not p.stem.endswith(('-sources','-javadoc'))]
        assert len(jars)==1,jars
        packaged=zipfile.ZipFile(jars[0]);assert packaged.testzip() is None
    def read(path):
        return packaged.read(path.removeprefix('src/main/resources/')) if packaged else (ROOT/path).read_bytes()
    for path,digest in review['unchanged_png_sha256'].items():
        assert hashlib.sha256(read(path)).hexdigest()==digest,('Unreviewed/excluded painting changed',path)
    for path,row in changed.items():
        raw=read(path);assert hashlib.sha256(raw).hexdigest()==row['sha256'],path
        im=Image.open(BytesIO(raw)).convert('RGBA');assert list(im.size)==row['size'],path
        assert hashlib.sha256(im.getchannel('A').tobytes()).hexdigest()==row['alpha_sha256'],path
    for name,look in LOOKS.items():
        path='src/main/resources/assets/the_oldest_house/textures/entity/'+name+'.png'
        im=Image.open(BytesIO(read(path))).convert('RGBA')
        # Every authored pixel exports as an exact 2x2 square. No antialiased portrait detail survives.
        half=im.resize((im.width//2,im.height//2),Image.Resampling.NEAREST)
        assert half.resize(im.size,Image.Resampling.NEAREST).tobytes()==im.tobytes(),('Off-grid art',name)
        for box in [(0,0,8,8),(24,0,32,8)]:
            swatch=im.crop(tuple(v*4 for v in box))
            assert len(swatch.getcolors(swatch.width*swatch.height))==1 and swatch.getchannel('A').getextrema()==(255,255),('Broken elbow/knee swatch',name)
        arms=3 if name.startswith('lake_witch') else 4
        for cube in [net(0,0,8,8,8),net(16,16,8,12,4),net(40,16,arms,12,4),net(0,16,4,12,4),net(32,48,arms,12,4),net(16,48,4,12,4)]:
            for face,box in cube.items():
                rect=tuple(round(v*4) for v in box);alpha=im.getchannel('A').crop(rect)
                assert alpha.getextrema()==(255,255),('Unpainted native base face',name,face,box)
    runtime=json.loads(read('src/main/resources/assets/the_oldest_house/character_art.json'))
    assert runtime==entries(),'Native review catalog differs from the actual production mesh selection'
    assert {r['name'] for r in runtime if r['model']=='borrasca'}=={'borrasca_'+p for p in ('kyle','kimber','sheriff','prescott','worker')}
    print(f"Verified {len(changed)} historical paintings, {len(runtime)} human model selections including Drisking, all opaque native base faces and {len(review['unchanged_png_sha256'])} unchanged historical PNGs.")
    if packaged:packaged.close()


if __name__=='__main__':main()

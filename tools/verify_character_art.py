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
PROP_REVIEW=ROOT/'art/inventory/devils_rock_props_0241.json'
FAMILY_REVIEW=ROOT/'art/inventory/painted_family_04123.json'
PORTRAIT_REVIEW=ROOT/'art/inventory/manor_portraits_04223.json'
MERRY_REVIEW=ROOT/'art/inventory/ghosts_merry_04242.json'
MERRY_PATH='src/main/resources/assets/the_oldest_house/textures/entity/ghosts_merry.png'
MARJORIE_REVIEW=ROOT/'art/inventory/ghosts_marjorie_04318.json'
MARJORIE_PATHS={'src/main/resources/assets/the_oldest_house/textures/entity/ghosts_marjorie'+phase+'.png' for phase in ('','_dinner')}
MARJORIE_HAIR_NETS=((80,0,8,6,1),(80,8,8,10,1),(101,0,2,10,1),(111,0,2,9,1))
SISTERS_REVIEW=ROOT/'art/inventory/ghosts_sisters_04320.json'
FRECKLES_REVIEW=ROOT/'art/inventory/ghosts_merry_04321.json'
MERRY_HAIR_NETS=((80,0,3,7,3),(96,0,2,3,2),(110,0,4,2,3))
FAMILY_PATHS={'src/main/resources/assets/the_oldest_house/textures/entity/literary_family_'+role+phase+'.png'
              for role in ('father','mother','child') for phase in ('','_changed')}


def reviewed_repaints(review):
    """Current approved paintings, shared by source and actual playable-JAR checks."""
    changed={r['path']:r for r in review['repainted']}
    # Only the six explicitly requested family sheets supersede their historical hashes.
    family={r['path']:r for r in json.loads(FAMILY_REVIEW.read_text())['repainted']}
    assert set(family)==FAMILY_PATHS
    for path,row in family.items():
        assert row['review_04100_sha256']==changed[path]['sha256'],('Historical family review changed',path)
        assert row['size']==changed[path]['size'] and row['alpha_sha256']==changed[path]['alpha_sha256'],('Family UV/alpha changed',path)
    # 0.4.242: Merry's sheet, painted again with longer hair at the owner's request; it supersedes only her own 0.4.100 hash.
    merry={r['path']:r for r in json.loads(MERRY_REVIEW.read_text())['repainted']}
    assert set(merry)=={MERRY_PATH}
    for path,row in merry.items():
        assert row['review_04100_sha256']==changed[path]['sha256'],('Historical Merry review changed',path)
        assert row['size']==changed[path]['size'] and row['alpha_sha256']==changed[path]['alpha_sha256'],('Merry UV/alpha changed',path)
    # The requested younger face and long hair supersede only Marjorie's normal and dinner paintings.
    marjorie={r['path']:r for r in json.loads(MARJORIE_REVIEW.read_text())['repainted']}
    assert set(marjorie)==MARJORIE_PATHS
    for path,row in marjorie.items():
        assert row['review_04100_sha256']==changed[path]['sha256'],('Historical Marjorie review changed',path)
        assert row['size']==changed[path]['size'] and row['review_04100_alpha_sha256']==changed[path]['alpha_sha256'],('Marjorie baseline changed',path)
    previous={**changed,**family,**merry,**marjorie}
    sisters={r['path']:r for r in json.loads(SISTERS_REVIEW.read_text())['repainted']}
    assert set(sisters)==MARJORIE_PATHS|{MERRY_PATH}
    for path,row in sisters.items():
        assert row['previous_sha256']==previous[path]['sha256'] and row['previous_alpha_sha256']==previous[path]['alpha_sha256'],('Previous sisters review changed',path)
        assert row['size']==previous[path]['size'],('Sisters atlas size changed',path)
        if path in MARJORIE_PATHS:assert row['alpha_sha256']==previous[path]['alpha_sha256'],('Marjorie alpha changed',path)
    current={**previous,**sisters}
    freckles={r['path']:r for r in json.loads(FRECKLES_REVIEW.read_text())['repainted']}
    assert set(freckles)=={MERRY_PATH}
    row=freckles[MERRY_PATH];prior=current[MERRY_PATH]
    assert row['review_04320_sha256']==prior['sha256'] and row['review_04320_alpha_sha256']==prior['alpha_sha256'], 'Previous Merry review changed'
    assert row['size']==prior['size'] and row['alpha_sha256']==prior['alpha_sha256'], 'Merry UV/alpha changed with freckles'
    return {**current,**freckles}


def main():
    parser=argparse.ArgumentParser();parser.add_argument('--jar',action='store_true');args=parser.parse_args()
    review=json.loads(CATALOG.read_text());changed=reviewed_repaints(review)
    # Preserve the historical 0.4.100 inventory, including its original machine atlas.
    # The later Drisking human sheets are explicitly included in LOOKS.
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
    # 0.4.223: the manor's five family portraits, repainted at the owner's request; each supersedes only its own historical hash.
    portraits={r['path']:r for r in json.loads(PORTRAIT_REVIEW.read_text())['repainted']}
    assert len(portraits)==5 and all(path.endswith('_portrait.png') for path in portraits)
    # 0.4.241: Devil's Rock's generated prop crops, redrawn as pixel art at the owner's request (tools/draw_devils_rock_props.py).
    props={r['path']:r for r in json.loads(PROP_REVIEW.read_text())['repainted']}
    assert len(props)==4 and all('/review_' in path for path in props)
    for path,digest in review['unchanged_png_sha256'].items():
        if path in props:
            row=props[path];assert row['review_04100_sha256']==digest,('Historical prop review changed',path)
            raw=read(path);assert hashlib.sha256(raw).hexdigest()==row['sha256'],('Devil\'s Rock prop changed since its redrawing',path)
            assert list(Image.open(BytesIO(raw)).size)==row['size'],path
            continue
        if path in portraits:
            row=portraits[path];assert row['review_04100_sha256']==digest,('Historical portrait review changed',path)
            raw=read(path);assert hashlib.sha256(raw).hexdigest()==row['sha256'],('Manor portrait changed since its repaint',path)
            im=Image.open(BytesIO(raw)).convert('RGBA');assert list(im.size)==row['size'] and hashlib.sha256(im.getchannel('A').tobytes()).hexdigest()==row['alpha_sha256'],path
            continue
        assert hashlib.sha256(read(path)).hexdigest()==digest,('Unreviewed/excluded painting changed',path)
    for path,row in changed.items():
        raw=read(path);assert hashlib.sha256(raw).hexdigest()==row['sha256'],path
        im=Image.open(BytesIO(raw)).convert('RGBA');assert list(im.size)==row['size'],path
        assert hashlib.sha256(im.getchannel('A').tobytes()).hexdigest()==row['alpha_sha256'],path
        if path in MARJORIE_PATHS:
            # Only the newly occupied hair nets may extend the historical opacity mask.
            alpha=im.getchannel('A')
            for dims in MARJORIE_HAIR_NETS:
                for box in net(*dims).values():alpha.paste(0,tuple(round(v*4) for v in box))
            assert hashlib.sha256(alpha.tobytes()).hexdigest()==row['review_04100_alpha_sha256'],('Marjorie original UV/alpha changed',path)
        if path==MERRY_PATH:
            alpha=im.getchannel('A')
            for dims in MERRY_HAIR_NETS:
                for box in net(*dims).values():alpha.paste(0,tuple(round(v*4) for v in box))
            assert hashlib.sha256(alpha.tobytes()).hexdigest()==row['previous_alpha_sha256'],('Merry original UV/alpha changed',path)
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
    from draw_backseat_stowaway import NETS, net as creature_net
    neck=Image.open(BytesIO(read('src/main/resources/assets/the_oldest_house/textures/entity/uncanny_bent_neck.png'))).convert('RGBA')
    for box in net(0,32,2,4,2).values():assert neck.crop(tuple(v*4 for v in box)).getchannel('A').getextrema()==(255,255),'injured neck has an opaque dedicated net'
    creature=Image.open(BytesIO(read('src/main/resources/assets/the_oldest_house/textures/entity/backseat_stowaway.png'))).convert('RGBA')
    assert creature.size==(256,256)
    native=creature.resize((64,64),Image.Resampling.NEAREST)
    assert native.resize(creature.size,Image.Resampling.NEAREST).tobytes()==creature.tobytes(),'Off-grid Backseat creature'
    for name,dims in NETS.items():
        for face,box in creature_net(*dims).items():
            assert native.getchannel('A').crop(box).getextrema()==(255,255),('Unpainted creature face',name,face)
    runtime=json.loads(read('src/main/resources/assets/the_oldest_house/character_art.json'))
    assert runtime==entries(),'Native review catalog differs from the actual production mesh selection'
    assert {r['name'] for r in runtime if r['model']=='borrasca'}=={'borrasca_'+p for p in ('kyle','kimber','sheriff','prescott','worker','worker_two','eli','wren')}
    print(f"Verified {len(changed)} catalogued paintings including {len(FAMILY_PATHS)} refreshed family sheets, {len(runtime)} human model selections including Drisking, all opaque native base faces and {len(review['unchanged_png_sha256'])} unchanged historical PNGs.")
    if packaged:packaged.close()


if __name__=='__main__':main()

"""The Barretts' beds stand on four corner legs, and each pile of books in the house is its own pile (0.4.326).

The owner's notes (7 November): "Improperly placed leg for bed" (each half of a bed had two diagonal legs, so one stood in the
middle of the bed) and "Give different text and textures to the piles of books in merry's room" (every pile in the house was the
same denim-and-red slab). The head half now has its two legs at the head end and the foot half its two at the foot. Four piles are
drawn at native pixel scale: Merry's picture books, the overdue library books on her floor, Marjorie's paperbacks and Sarah's
cookbooks with their binder. Each book is a box: its cover on top and bottom, its spine on one side, page edges on the other three.
The basement keeps the old box of books."""
from pathlib import Path
from PIL import Image, ImageDraw
import argparse, io, json

ROOT=Path(__file__).resolve().parents[1]
A=ROOT/'src/main/resources/assets/the_oldest_house'
ATLAS='the_oldest_house:block/ghosts/materials_atlas'
FACES=('north','south','east','west','up','down')

def bed(head):
    """The existing mesh, materials-atlas UVs unchanged, with the legs at the right end."""
    def cell(i):x=i%4*4;y=i//4*4;return [float(x),float(y),float(x+4),float(y+4)]
    def b(lo,hi,uv,tex):return {'from':lo,'to':hi,'faces':{f:{'texture':'#'+tex,'uv':uv} for f in FACES}}
    cream,cloth,wood,white=cell(8),cell(13),cell(5),cell(14)
    z=1 if head else 13
    elements=[b([0,6,0],[16,10,16],cream,'cream'),b([0,10,0],[16,12,16],cloth,'cloth'),b([1,0,z],[3,6,z+2],wood,'wood'),b([13,0,z],[15,6,z+2],wood,'wood')]
    textures={'cloth':ATLAS,'cream':ATLAS,'wood':ATLAS,'particle':ATLAS}
    if head:elements.append(b([1,12,1],[15,14,5],white,'white'));textures={'cloth':ATLAS,'white':ATLAS,'cream':ATLAS,'wood':ATLAS,'particle':ATLAS}
    return {'textures':textures,'elements':elements,'ambientocclusion':True}

def cover(base,dark,light,art=None,spine=None,label=False):
    """Rows 0-13 are the cover, rows 14-15 the spine (a thin book shows only row 15)."""
    im=Image.new('RGBA',(16,16),base);d=ImageDraw.Draw(im)
    d.rectangle((0,0,15,0),fill=light);d.rectangle((0,13,15,13),fill=dark);d.rectangle((15,0,15,13),fill=dark)
    if art:art(d)
    d.rectangle((0,14,15,15),fill=spine or dark)
    for x in range(2,14,4):d.point((x,15),fill=light)
    if label:d.rectangle((1,14,3,15),fill='#e8e4d6');d.point((2,15),fill='#4b4f55')
    return im

def pages():
    im=Image.new('RGBA',(16,16),'#e3dcc4');d=ImageDraw.Draw(im)
    for y in range(0,16,2):d.line((0,y,15,y),fill='#cfc6aa')
    for x,y in ((3,1),(9,5),(12,9),(5,13)):d.point((x,y),fill='#bdb293')
    return im

def paintings():
    out={'pages':pages()}
    # Merry's picture books: big, thin and bright.
    out['picture_sun']=cover('#d9b23c','#a8842a','#f0d777',lambda d:(d.ellipse((4,3,10,9),fill='#e7742f'),d.rectangle((2,10,13,11),fill='#5e8a4a')),'#c28f2c')
    out['picture_whale']=cover('#3f8c95','#2c6870','#7cc0c5',lambda d:(d.ellipse((3,5,11,9),fill='#e6ece8'),d.polygon([(11,7),(14,4),(14,10)],fill='#e6ece8'),d.point((5,6),fill='#2c3438')),'#2f7279')
    out['picture_ants']=cover('#c7493b','#962f26','#e98a6f',lambda d:([d.rectangle((x,7,x+1,8),fill='#2b2422') for x in (3,6,9,12)],d.line((2,10,13,10),fill='#f1d9a7')),'#a83a2f')
    # The school library's hardbacks: plastic jackets, a white label on every spine, a slip sticking out of the top one.
    out['library_green']=cover('#3e6b4c','#2b4c36','#86ad8f',lambda d:(d.line((1,1,5,12),fill='#7fa889'),d.rectangle((4,4,11,6),fill='#d8cfa9')),label=True)
    out['library_navy']=cover('#34466a','#232f4a','#7a8db3',lambda d:(d.line((1,1,5,12),fill='#7286ad'),d.ellipse((5,5,10,10),outline='#c9c1a0')),label=True)
    out['library_maroon']=cover('#6e2f3a','#4b1f27','#ad6c77',lambda d:(d.line((1,1,5,12),fill='#a86a74'),d.rectangle((3,8,12,9),fill='#cdb98f')),label=True)
    slip=Image.new('RGBA',(16,16),'#e9e3cf');d=ImageDraw.Draw(slip)
    for y in (3,6,9,12):d.line((1,y,14,y),fill='#b7aa8d')
    d.rectangle((2,4,7,5),fill='#7a3f39');out['due_slip']=slip
    # Marjorie's paperbacks: cracked white spines, a cover torn off one.
    def cracks(d):[d.line((x,14,x,15),fill='#e8e1cd') for x in (3,7,8,12)]
    out['paper_black']=cover('#29272b','#151417','#545058',lambda d:(d.rectangle((3,2,12,5),fill='#9d2a2a'),d.ellipse((5,7,10,12),fill='#c9c3b2'),cracks(d)))
    out['paper_cream']=cover('#d9cfb2','#ada283','#efe8d4',lambda d:(d.rectangle((2,3,13,4),fill='#4a3f6b'),d.rectangle((4,7,11,8),fill='#4a3f6b'),cracks(d)),'#b9ad8d')
    out['paper_torn']=cover('#e3dcc4','#b8ad8e','#efe9d6',lambda d:(d.polygon([(0,0),(9,0),(4,6),(0,9)],fill='#c07a34'),[d.line((3,y,13,y),fill='#c9bfa3') for y in (7,9,11)],cracks(d)),'#c07a34')
    # Sarah's cookbooks, swollen with steam, and the ring binder of cut-out recipes.
    out['cook_gingham']=cover('#e8e0cb','#bcb196','#f6f0e1',lambda d:([d.rectangle((x,y,x+1,y+1),fill='#c55b4f') for x in range(0,14,4) for y in range(0,12,4)],d.rectangle((3,5,12,8),fill='#f6f0e1'),d.line((4,6,11,6),fill='#7b5a3a')),'#c55b4f')
    out['cook_green']=cover('#5f7d4a','#455c35','#9cb585',lambda d:(d.ellipse((4,3,11,10),fill='#e9dfc2'),d.ellipse((6,5,9,8),fill='#c4562f')),'#4d6a3b')
    out['binder_red']=cover('#a63b33','#7a2923','#d1756a',lambda d:(d.rectangle((1,2,2,11),fill='#8d8f92'),d.point((1,4),fill='#c9cbcd'),d.point((1,9),fill='#c9cbcd'),d.rectangle((5,4,13,9),fill='#e6dcc2'),d.point((8,6),fill='#b4392d'),d.point((11,7),fill='#b4392d')),'#8f322b')
    return out

def book(x0,z0,x1,z1,y0,h,face,spine='west'):
    """A book lying flat: its cover up and down, its spine on one side, page edges on the others. UVs at native pixel scale."""
    w,dz=x1-x0,z1-z0;faces={}
    for f in FACES:
        if f in ('up','down'):faces[f]={'texture':'#'+face,'uv':[0,0,w,dz]}
        elif f==spine:faces[f]={'texture':'#'+face,'uv':[0,16-h,dz if f in ('east','west') else w,16]}
        else:faces[f]={'texture':'#pages','uv':[0,0,dz if f in ('east','west') else w,h]}
    return {'from':[x0,y0,z0],'to':[x1,y0+h,z1],'faces':faces}

def models():
    names=list(paintings())
    textures={n:'the_oldest_house:block/ghosts_books/'+n for n in names}
    def model(elements):
        used={v['texture'][1:] for e in elements for v in e['faces'].values()}
        tex={k:v for k,v in textures.items() if k in used};tex['particle']=next(iter(tex.values()))
        return {'textures':tex,'elements':elements,'ambientocclusion':True}
    picture=[book(2,2,14,14,0,1,'picture_sun','south'),book(3,1.5,15,13.5,1,1,'picture_whale','west'),book(1.5,3,13.5,15,2,1,'picture_ants','north')]
    library=[book(3,2,12,14,0,2,'library_navy'),book(4,2.5,13,14.5,2,2,'library_maroon','east'),book(3.5,1.5,12.5,13.5,4,2,'library_green'),
             {'from':[6,5.2,13.5],'to':[9,5.2,15.5],'faces':{f:{'texture':'#due_slip','uv':[0,0,3,2]} for f in ('up','down')}}]
    paperback=[book(4,3,11,13,0,1.5,'paper_cream'),book(3.5,4,10.5,14,1.5,1.5,'paper_black','east'),book(5,3.5,12,13.5,3,1.5,'paper_torn')]
    cook=[book(2,2,12,15,0,2,'cook_green','south'),book(3,1,13,14,2,2,'cook_gingham','west'),book(2.5,3,14,15,4,2.5,'binder_red','east')]
    return {'books_picture':model(picture),'books_library':model(library),'books_paperback':model(paperback),'books_cook':model(cook)}

def main():
    parser=argparse.ArgumentParser();parser.add_argument('--check',action='store_true');args=parser.parse_args();files={}
    for name,im in paintings().items():
        buf=io.BytesIO();im.save(buf,format='PNG');files[A/('textures/block/ghosts_books/'+name+'.png')]=buf.getvalue()
    meshes=dict(models());meshes['bed_head']=bed(True);meshes['bed_foot']=bed(False)
    states=A/'blockstates/ghosts_set.json';current=json.loads(states.read_text());variants={}
    for name,model in meshes.items():
        files[A/('models/block/ghosts_'+name+'.json')]=(json.dumps(model,indent=2)+'\n').encode()
        for facing,y in [('north',0),('east',90),('south',180),('west',270)]:variants['kind='+name+',facing='+facing]={'model':'the_oldest_house:block/ghosts_'+name,'y':y}
    if args.check:
        for path,raw in files.items():assert path.read_bytes()==raw,path
        for key,value in variants.items():assert current['variants'].get(key)==value,key
    else:
        for path,raw in files.items():path.parent.mkdir(parents=True,exist_ok=True);path.write_bytes(raw)
        current['variants'].update(variants);states.write_text(json.dumps(current,indent=2)+'\n')
    print(('Verified' if args.check else 'Drew')+' the Barretts\' four-legged beds and their four piles of books.')
if __name__=='__main__':main()

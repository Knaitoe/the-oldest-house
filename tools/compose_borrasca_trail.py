"""New original material/contact recordings. The same triad is heard on the trail and at the rig."""
import math
from compose_borrasca import compose


def chime(t, noise, low):
    value = 0.0
    for start in (0,.55,1.10):
        d = t-start
        if d >= 0:
            value += math.exp(-d*2.4)*(.22*math.sin(math.tau*431*d)+.17*math.sin(math.tau*697*d)+.10*math.sin(math.tau*1121*d))
            value += .16*noise*math.exp(-d*24)
    return value


if __name__ == '__main__':
    compose('triple_chime',3.5,chime)
    compose('carve',1.2,lambda t,n,low: .18*(n-low)*math.sin(math.pi*t/1.2)**2*(.35+.65*max(0,math.sin(math.tau*4*t))))
    compose('breaker',.7,lambda t,n,low: (.3*n+.15*math.sin(math.tau*83*t))*math.exp(-t*13))
    compose('chain',1.1,lambda t,n,low: (.12*n+.2*math.sin(math.tau*1373*t))*math.exp(-t*5)*(.3+.7*max(0,math.sin(math.tau*13*t))))

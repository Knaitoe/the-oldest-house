#!/usr/bin/env python3
"""Summarize a playtest log into the measures in docs/PLAYER_EXPERIENCE_PLAN.md.

Usage: python3 tools/summarize_playtest_log.py <world>/the_oldest_house/playtest-log.jsonl

The log is written by the server only when `playtestLog = true` in the
the_oldest_house-house-server.toml [playtest] section. Each line is one event.
Times here are wall-clock minutes of play, counted only between session_start
and session_end, so time spent logged out does not inflate them.
"""
import json
import sys
from collections import Counter, defaultdict


def load(path):
    events = []
    with open(path, encoding="utf-8") as f:
        for number, line in enumerate(f, 1):
            line = line.strip()
            if not line:
                continue
            try:
                events.append(json.loads(line))
            except json.JSONDecodeError:
                print(f"line {number}: not JSON, skipped", file=sys.stderr)
    events.sort(key=lambda e: e.get("time", 0))
    return events


class Player:
    def __init__(self):
        self.played_ms = 0
        self.session_started = None
        self.last_time = None
        self.firsts = {}
        self.events = []

    def clock(self, event):
        """Minutes of play before this event, counting only time inside sessions."""
        t = event["time"]
        if self.session_started is not None:
            return (self.played_ms + t - self.session_started) / 60000
        return self.played_ms / 60000


def summarize(events):
    players = defaultdict(Player)
    for e in events:
        p = players[e.get("player", "?")]
        name = e.get("event")
        if name == "session_start":
            p.session_started = e["time"]
        minute = p.clock(e)
        e["_minute"] = minute
        p.events.append(e)
        key = None
        if name == "opening_stage":
            key = "opening: " + e.get("stage", "?")
        elif name == "arrive":
            key = "first labyrinth arrival"
            if e.get("story"):
                p.firsts.setdefault("first story room", minute)
        elif name == "story_resolve":
            key = "first story resolved"
        elif name == "phase":
            key = "finale: " + e.get("phase", "?")
        if key:
            p.firsts.setdefault(key, minute)
        if name == "session_end" and p.session_started is not None:
            p.played_ms += e["time"] - p.session_started
            p.session_started = None
    for p in players.values():
        if p.session_started is not None and p.events:
            p.played_ms += p.events[-1]["time"] - p.session_started
            p.session_started = None
    return players


def report(players):
    for pid, p in players.items():
        print(f"\n== player {pid}: {p.played_ms / 60000:.0f} minutes of play")
        for key, minute in sorted(p.firsts.items(), key=lambda kv: kv[1]):
            print(f"  {minute:7.1f} min  {key}")

        arrivals = [e for e in p.events if e["event"] == "arrive"]
        stories = [e for e in arrivals if e.get("story")]
        resolves = [e for e in p.events if e["event"] == "story_resolve"]
        if arrivals:
            span = arrivals[-1]["_minute"] - arrivals[0]["_minute"]
            hours = max(span / 60, 1e-9)
            print(f"  arrivals {len(arrivals)}, story rooms entered {len(stories)}, stories resolved {len(resolves)}"
                  + (f" ({len(resolves) / hours:.1f} per hour of labyrinth play)" if span > 1 else ""))
            beats = sorted(e["_minute"] for e in p.events
                           if e["event"] in ("story_resolve", "note_scene_enter") or (e["event"] == "arrive" and e.get("story")))
            gaps = [b - a for a, b in zip(beats, beats[1:])]
            if gaps:
                print(f"  longest stretch without a story or note scene: {max(gaps):.1f} min")
            depths = [e.get("depth", 0) for e in arrivals]
            print(f"  deepest: {max(depths)}")

        leaves = [e for e in p.events if e["event"] == "story_leave"]
        unresolved = [e for e in leaves if not e.get("resolved")]
        if leaves:
            print(f"  left story rooms {len(leaves)} times, {len(unresolved)} unresolved (retreats)")
            for place, n in Counter(e["place"] for e in unresolved).most_common(5):
                print(f"    retreated from {place} x{n}")

        deaths = [e for e in p.events if e["event"] == "death"]
        if deaths:
            print(f"  deaths {len(deaths)}")
            for (place, cause), n in Counter((e.get("place"), e.get("cause")) for e in deaths).most_common(5):
                print(f"    {place} ({cause}) x{n}")

        refusals = [e for e in p.events if e["event"] == "refused"]
        if refusals:
            print(f"  refusals {len(refusals)}: " + ", ".join(f"{g} x{n}" for g, n in Counter(e["gate"] for e in refusals).most_common(6)))
            loops = 0
            for gate in {e["gate"] for e in refusals}:
                times = [e["_minute"] for e in refusals if e["gate"] == gate]
                for i in range(len(times) - 2):
                    if times[i + 2] - times[i] <= 1.0:
                        loops += 1
                        break
            print(f"  repeated-refusal loops (same gate 3+ times within a minute): {loops}")

        scenes = [e for e in p.events if e["event"] == "note_scene_leave"]
        if scenes:
            done = sum(1 for e in scenes if e.get("finished"))
            print(f"  note scenes {len(scenes)}, finished {done}")

        ends = [e for e in p.events if e["event"] == "session_end"]
        if ends:
            print("  sessions ended at: " + ", ".join(f"{place} x{n}" for place, n in Counter(e.get("place") for e in ends).most_common()))


def main():
    if len(sys.argv) != 2:
        print(__doc__)
        sys.exit(2)
    report(summarize(load(sys.argv[1])))


if __name__ == "__main__":
    main()

#!/usr/bin/env python3
"""Summarize opt-in local logs described in docs/PLAYER_EXPERIENCE_PLAN.md.

Usage: python3 tools/summarize_playtest_log.py <world>/the_oldest_house/playtest-log.jsonl [--json]

Schema 2 records actual occupancy, dimensions and connection identities. Older
logs are accepted with inferred occupancy, explicitly labelled as an estimate.
Incomplete sessions stop at their last observed event, never at the next login.
Quiet stretches exclude time inside an unfinished story or personal note scene.
"""
import argparse
import json
import sys
from collections import Counter, defaultdict, deque
from dataclasses import dataclass, field

MINUTE = 60_000
REFUGES = {"manor", "overworld", "outside", "other"}


def load(path):
    events = []
    with open(path, encoding="utf-8") as source:
        for number, line in enumerate(source, 1):
            if not line.strip():
                continue
            try:
                event = json.loads(line)
                if not isinstance(event, dict) or not isinstance(event.get("time"), (int, float)) or not isinstance(event.get("event"), str):
                    raise ValueError("missing event/time")
                events.append(event)
            except (json.JSONDecodeError, ValueError):
                print(f"line {number}: invalid event, skipped", file=sys.stderr)
    return sorted(events, key=lambda event: event["time"])


@dataclass
class Session:
    events: list = field(default_factory=list)
    complete: bool = False


def sessions(events):
    """Split connections before measuring anything; keep interrupted evidence."""
    result, active = defaultdict(list), {}
    for original in sorted(events, key=lambda event: event["time"]):
        event = dict(original)
        pid = event.get("player", "?")
        current = active.get(pid)
        token = event.get("session")
        changed = current and token and current.events[-1].get("session") and token != current.events[-1]["session"]
        if event["event"] == "session_start" or changed:
            if current:
                result[pid].append(current)
            current = Session()
            active[pid] = current
        if current is None:
            current = Session()
            active[pid] = current
        current.events.append(event)
        if event["event"] == "session_end":
            current.complete = current.events[0]["event"] == "session_start"
            result[pid].append(current)
            active.pop(pid, None)
    for pid, current in active.items():
        result[pid].append(current)
    return result


def context(event, previous):
    inside, activity = previous
    name, place = event["event"], event.get("place")
    if "inLabyrinth" in event:
        inside = bool(event["inLabyrinth"])
    elif name == "arrive" or name == "note_scene_enter":
        inside = True
    elif place in REFUGES:
        inside = False
    elif place:
        inside = True
    if event.get("spectator"):
        inside = False
    if "activity" in event:
        activity = event["activity"]
    elif name == "note_scene_enter":
        activity = "note_scene"
    elif name == "note_scene_leave":
        activity = "staircase"
    elif name == "arrive":
        activity = "story" if event.get("story") else "ordinary"
    elif name == "story_resolve":
        activity = "ordinary"
    elif not inside:
        activity = "refuge"
    return inside, activity


def refusal_episodes(events):
    """One episode per continuing burst, keyed by actual site and connection."""
    groups = defaultdict(list)
    for event in events:
        if event["event"] == "refused" and not event.get("spectator"):
            site = event.get("site", event.get("place", "?"))
            groups[(event.get("gate", "?"), event.get("dimension", "?"), site)].append(event["time"])
    count = 0
    for times in groups.values():
        window, counted, last = deque(), False, None
        for time in times:
            if last is not None and time - last > MINUTE:
                window.clear()
                counted = False
            while window and time - window[0] > MINUTE:
                window.popleft()
            window.append(time)
            if len(window) >= 3 and not counted:
                count += 1
                counted = True
            last = time
    return count


def summarize(events):
    players = {}
    for pid, visits in sessions(events).items():
        played_ms = labyrinth_ms = longest_ms = 0
        arrivals = stories = resolves = loops = 0
        firsts, annotated, by_session = {}, [], []
        for index, session in enumerate(visits):
            rows = session.events
            start, end = rows[0]["time"], rows[-1]["time"]
            previous_time, current = start, (False, "refuge")
            quiet = session_labyrinth = 0
            session_resolves = 0
            for event in rows:
                time = max(previous_time, event["time"])
                span = time - previous_time
                if current[0]:
                    labyrinth_ms += span
                    session_labyrinth += span
                    if current[1] not in {"story", "note_scene"}:
                        quiet += span
                        longest_ms = max(longest_ms, quiet)
                    else:
                        quiet = 0
                else:
                    quiet = 0
                before = current
                current = context(event, current)
                if before[0] != current[0] or current[1] in {"story", "note_scene"}:
                    quiet = 0
                name = event["event"]
                if name in {"story_resolve", "note_scene_enter", "strangeness"} or name == "arrive" and event.get("story"):
                    quiet = 0
                minute = (played_ms + time - start) / MINUTE
                row = dict(event, _minute=minute, _session=index, _inside=current[0])
                annotated.append(row)
                key = None
                if name == "opening_stage":
                    key = "opening: " + event.get("stage", "?")
                elif name == "arrive" and current[0]:
                    arrivals += 1
                    key = "first labyrinth arrival"
                    if event.get("story"):
                        stories += 1
                        firsts.setdefault("first story room", minute)
                elif name == "story_resolve" and current[0]:
                    resolves += 1
                    session_resolves += 1
                    key = "first story resolved"
                elif name == "phase":
                    key = "finale: " + event.get("phase", "?")
                if key:
                    firsts.setdefault(key, minute)
                previous_time = time
            duration = max(0, end - start)
            played_ms += duration
            episodes = refusal_episodes(rows)
            loops += episodes
            by_session.append({
                "minutes": duration / MINUTE,
                "labyrinth_minutes": session_labyrinth / MINUTE,
                "resolutions": session_resolves,
                "refusal_episodes": episodes,
                "complete": session.complete,
                "ended_at": rows[-1].get("place", "?"),
            })
        hours = labyrinth_ms / (60 * MINUTE)
        players[pid] = {
            "minutes": played_ms / MINUTE,
            "labyrinth_minutes": labyrinth_ms / MINUTE,
            "arrivals": arrivals, "story_arrivals": stories, "resolutions": resolves,
            "resolutions_per_hour": resolves / hours if hours > 0 else None,
            "longest_quiet_minutes": longest_ms / MINUTE if labyrinth_ms else None,
            "refusal_episodes": loops,
            "incomplete_sessions": sum(not session.complete for session in visits),
            "estimated_occupancy": any("inLabyrinth" not in event for session in visits for event in session.events),
            "firsts": firsts, "sessions": by_session, "events": annotated,
        }
    return players


def report(players):
    for pid, player in players.items():
        print(f"\n== player {pid}: {player['minutes']:.1f} minutes of play; {player['labyrinth_minutes']:.1f} in the labyrinth")
        for key, minute in sorted(player["firsts"].items(), key=lambda pair: pair[1]):
            print(f"  {minute:7.1f} min  {key}")
        print(f"  arrivals {player['arrivals']}, Witness story rooms entered {player['story_arrivals']}, stories resolved {player['resolutions']}")
        if player["resolutions_per_hour"] is not None:
            print(f"  stories resolved per hour of labyrinth play: {player['resolutions_per_hour']:.2f}")
            print(f"  longest quiet stretch outside unfinished stories/note scenes: {player['longest_quiet_minutes']:.1f} min")
        if player["estimated_occupancy"]:
            print("  legacy occupancy estimate: this log lacks explicit dimension/occupancy observations")
        if player["incomplete_sessions"]:
            print(f"  incomplete sessions: {player['incomplete_sessions']} (counted only through their last observed event)")
        rows = player["events"]
        leaves = [event for event in rows if event["event"] == "story_leave"]
        retreats = [event for event in leaves if not event.get("resolved")]
        if leaves:
            print(f"  left Witness story rooms {len(leaves)} times, {len(retreats)} unresolved (retreats)")
            for place, count in Counter(event.get("place", "?") for event in retreats).most_common(5):
                print(f"    retreated from {place} x{count}")
        deaths = [event for event in rows if event["event"] == "death"]
        if deaths:
            print(f"  deaths {len(deaths)}")
            for (place, cause), count in Counter((event.get("place"), event.get("cause")) for event in deaths).most_common(5):
                print(f"    {place} ({cause}) x{count}")
        refusals = [event for event in rows if event["event"] == "refused"]
        if refusals:
            print("  refusals: " + ", ".join(f"{gate} x{count}" for gate, count in Counter(event.get("gate") for event in refusals).most_common(6)))
            print(f"  repeated-refusal episodes (same physical gate 3+ times within 60 seconds): {player['refusal_episodes']}")
        scenes = [event for event in rows if event["event"] == "note_scene_leave"]
        if scenes:
            print(f"  note scenes {len(scenes)}, finished {sum(bool(event.get('finished')) for event in scenes)}")
        for index, session in enumerate(player["sessions"], 1):
            status = "ended" if session["complete"] else "last observed"
            print(f"  session {index}: {session['minutes']:.1f} min; {session['labyrinth_minutes']:.1f} labyrinth; {status} at {session['ended_at']}")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("path")
    parser.add_argument("--json", action="store_true", help="print the same measures as JSON")
    args = parser.parse_args()
    players = summarize(load(args.path))
    if args.json:
        print(json.dumps(players, indent=2))
    else:
        report(players)


if __name__ == "__main__":
    main()

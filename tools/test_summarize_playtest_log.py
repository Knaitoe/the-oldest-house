"""Regressions for real journey measurements, including the five review failures."""
import contextlib
import io
import json
import tempfile
import unittest
from pathlib import Path
from summarize_playtest_log import load, summarize


def event(minute, name, **fields):
    return dict(time=minute * 60000, player="reader", event=name, **fields)


def metrics(*events):
    return summarize(events)["reader"]


class JourneyMeasures(unittest.TestCase):
    def test_final_room_and_tail_are_in_rate_denominator(self):
        p = metrics(event(0, "session_start", place="cross_hall"),
                    event(0, "arrive", story=False), event(2, "arrive", story=True),
                    event(32, "story_resolve"), event(60, "session_end"))
        self.assertEqual(p["labyrinth_minutes"], 60)
        self.assertEqual(p["resolutions_per_hour"], 1)

    def test_hour_with_no_story_is_a_full_quiet_stretch(self):
        p = metrics(event(0, "session_start", inLabyrinth=True, activity="ordinary"),
                    event(60, "session_end", inLabyrinth=True, activity="ordinary"))
        self.assertEqual(p["longest_quiet_minutes"], 60)

    def test_leading_and_trailing_gaps_are_measured(self):
        p = metrics(event(0, "session_start", inLabyrinth=True, activity="ordinary"),
                    event(20, "strangeness", inLabyrinth=True, activity="ordinary"),
                    event(60, "session_end", inLabyrinth=True, activity="ordinary"))
        self.assertEqual(p["longest_quiet_minutes"], 40)

    def test_story_presence_is_not_empty_hallway_time(self):
        p = metrics(event(0, "session_start", inLabyrinth=True, activity="ordinary"),
                    event(10, "arrive", inLabyrinth=True, activity="story", story=True),
                    event(55, "story_resolve", inLabyrinth=True, activity="ordinary"),
                    event(60, "session_end", inLabyrinth=True, activity="ordinary"))
        self.assertEqual(p["longest_quiet_minutes"], 10)
        self.assertEqual(p["labyrinth_minutes"], 60)

    def test_manor_break_is_excluded_from_labyrinth_hours(self):
        p = metrics(event(0, "session_start", inLabyrinth=True, activity="ordinary"),
                    event(10, "location", inLabyrinth=False, place="manor"),
                    event(100, "location", inLabyrinth=True, activity="story"),
                    event(110, "story_resolve", inLabyrinth=True, activity="ordinary"),
                    event(120, "session_end", inLabyrinth=True, activity="ordinary"))
        self.assertEqual(p["minutes"], 120)
        self.assertEqual(p["labyrinth_minutes"], 30)
        self.assertEqual(p["resolutions_per_hour"], 2)
        self.assertEqual(p["longest_quiet_minutes"], 10)

    def test_two_distinct_refusal_bursts_count_twice(self):
        p = metrics(event(0, "session_start"),
                    *(event(t, "refused", gate="door_sticks", site="a") for t in (0, .1, .2, 10, 10.1, 10.2)),
                    event(11, "session_end"))
        self.assertEqual(p["refusal_episodes"], 2)

    def test_offline_time_does_not_join_refusal_bursts(self):
        p = metrics(event(0, "session_start"), event(.1, "refused", gate="g"),
                    event(.2, "refused", gate="g"), event(.3, "session_end"),
                    event(1440, "session_start"), event(1440.1, "refused", gate="g"),
                    event(1440.2, "session_end"))
        self.assertEqual(p["refusal_episodes"], 0)
        self.assertAlmostEqual(p["minutes"], .5)

    def test_different_physical_doors_do_not_form_one_loop(self):
        p = metrics(event(0, "session_start"),
                    event(.1, "refused", gate="door_sticks", site="a"),
                    event(.2, "refused", gate="door_sticks", site="b"),
                    event(.3, "refused", gate="door_sticks", site="a"),
                    event(1, "session_end"))
        self.assertEqual(p["refusal_episodes"], 0)

    def test_missing_logout_retains_known_previous_minutes(self):
        p = metrics(event(0, "session_start"), event(10, "location"),
                    event(1440, "session_start"), event(1445, "session_end"))
        self.assertEqual(p["minutes"], 15)
        self.assertEqual(p["incomplete_sessions"], 1)

    def test_incomplete_tail_ends_at_last_observation(self):
        p = metrics(event(0, "session_start", inLabyrinth=True, activity="ordinary"),
                    event(15, "location", inLabyrinth=True, activity="ordinary"))
        self.assertEqual(p["minutes"], 15)
        self.assertEqual(p["longest_quiet_minutes"], 15)
        self.assertEqual(p["incomplete_sessions"], 1)

    def test_reconnect_token_splits_even_without_start_or_end(self):
        p = metrics(event(0, "location", session="a", inLabyrinth=True),
                    event(10, "location", session="a", inLabyrinth=True),
                    event(1440, "location", session="b", inLabyrinth=True),
                    event(1445, "location", session="b", inLabyrinth=True))
        self.assertEqual(p["minutes"], 15)
        self.assertEqual(len(p["sessions"]), 2)

    def test_non_witness_room_is_not_a_story_arrival(self):
        p = metrics(event(0, "session_start", inLabyrinth=True),
                    event(1, "arrive", place="karen_room", story=False, inLabyrinth=True),
                    event(2, "session_end", inLabyrinth=True))
        self.assertEqual(p["story_arrivals"], 0)
        self.assertEqual(p["longest_quiet_minutes"], 2)

    def test_spectator_is_excluded_from_participation(self):
        p = metrics(event(0, "session_start", inLabyrinth=True, spectator=True),
                    event(10, "arrive", inLabyrinth=True, story=True, spectator=True),
                    event(15, "story_resolve", inLabyrinth=True, spectator=True),
                    event(20, "session_end", inLabyrinth=True, spectator=True))
        self.assertEqual(p["labyrinth_minutes"], 0)
        self.assertEqual(p["resolutions"], 0)
        self.assertEqual(p["story_arrivals"], 0)

    def test_resolutions_outside_expedition_are_not_in_its_rate(self):
        p = metrics(event(0, "session_start", inLabyrinth=False),
                    event(10, "story_resolve", inLabyrinth=False),
                    event(20, "arrive", inLabyrinth=True, story=False),
                    event(30, "session_end", inLabyrinth=True))
        self.assertEqual(p["resolutions"], 0)
        self.assertEqual(p["resolutions_per_hour"], 0)

    def test_note_scene_breaks_quiet_stretch(self):
        p = metrics(event(0, "session_start", inLabyrinth=True, activity="ordinary"),
                    event(10, "note_scene_enter", inLabyrinth=True, activity="note_scene"),
                    event(30, "note_scene_leave", inLabyrinth=True, activity="staircase"),
                    event(40, "session_end", inLabyrinth=True, activity="staircase"))
        self.assertEqual(p["labyrinth_minutes"], 40)
        self.assertEqual(p["longest_quiet_minutes"], 10)

    def test_simultaneous_players_remain_independent(self):
        events = [event(0, "session_start", inLabyrinth=True),
                  event(10, "story_resolve", inLabyrinth=True),
                  event(60, "session_end", inLabyrinth=True)]
        events += [dict(event(0, "session_start", inLabyrinth=True), player="peer"),
                   dict(event(60, "session_end", inLabyrinth=True), player="peer")]
        p = summarize(events)
        self.assertEqual(p["reader"]["resolutions"], 1)
        self.assertEqual(p["peer"]["resolutions"], 0)

    def test_loader_handles_truncated_last_line_without_losing_evidence(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "log.jsonl"
            path.write_text(json.dumps(event(0, "session_start")) + "\n" +
                            json.dumps(event(10, "location")) + "\n{" + "\n")
            with contextlib.redirect_stderr(io.StringIO()):
                p = summarize(load(path))["reader"]
            self.assertEqual(p["minutes"], 10)
            self.assertEqual(p["incomplete_sessions"], 1)


if __name__ == "__main__":
    unittest.main()

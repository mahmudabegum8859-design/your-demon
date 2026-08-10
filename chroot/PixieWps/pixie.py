#!/usr/bin/env python3
# =============================================================================
#  pixie.py — compatibility wrapper for the Your Demon app
#
#  The app invokes this script as:
#      python3 -u /CORE/PixieWps/pixie.py -i <iface> [--iface-down] -B|-p <pin>|-K -F -b <bssid>
#
#  It simply forwards every argument to OneShot-Extended (ose.py) after
#  translating the legacy original-OneShot flag "-K/--pixie-dust" into the
#  OneShot-Extended equivalent "-P/--pixie-dust".
# =============================================================================
import os
import sys

_DIR = os.path.dirname(os.path.realpath(__file__))
sys.path.insert(0, _DIR)
os.chdir(_DIR)

args = []
for arg in sys.argv[1:]:
    if arg in ("-K", "--pixie-dust"):
        args.append("-P")  # OneShot-Extended flag for the pixie dust attack
    else:
        args.append(arg)

os.execv(sys.executable, [sys.executable, "-u", os.path.join(_DIR, "ose.py")] + args)

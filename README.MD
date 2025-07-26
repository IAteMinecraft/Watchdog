# Watchdog: Save my Server!

---
Does your Server keep running out of memory and becoming sluggish?  
Do you need a mod that automatically shuts your Server down when memory gets too high  
*and is **highly configurable***?

### Well look no further!

---
Watchdog is a mod that allows you to automatically stop your Server.  
Once the given statistic has been reached, Watchdog will start a countdown (or quit immediately if `Start Time` is set to 0)

Configuration options:
- Timings
    - Start Time
        - How long the countdown timer will take before stopping the server
        - Default: 60, min: 0
        - Unit: Seconds
    - Averaging Window
        - How long the Server must stay at a given percent/amount before starting the countdown
        - Default: 30, min: 1
        - Unit: Seconds
- Memory Values
    - Type
        - Whether to use Max Memory Percent or Amount
        - Default: PERCENT, other: AMOUNT
        - Unit: None
    - Max Memory Usage (Percent)
        - The maximum amount of memory the Server process is allowed to use before shutting down
        - Default: 90, min: 0, max: 100
        - Unit: Percent
    - Max Memory Usage (Amount)
        - The maximum amount of memory the Server process is allowed to use before shutting down
        - Default: 0, min: 0
        - Unit: Gigabytes

---

This mod is entirely made by me, Icon and All!
You may use this mod in modpacks (although probably only put it in server packs)

---

If you are wondering why Watchdog does not restart the server, it is because (from what I can figure out), It just is not feasable to restart the server from within the mod.

This mod is meant for situations where your wrapper script restarts the server, or when you are using a service like Pterodactyl
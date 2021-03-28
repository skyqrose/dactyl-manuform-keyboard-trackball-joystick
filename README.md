Work in progress. Do not use, fork, or consider anything in this README to be accurate until this notice is removed.

# Dactyl Manuform Keyboard with Trackball and Joystick

(picture goes here)

## Features

* Trackball and joystick mounts
* Modular thumb cluster, for easier swap between trackball, joystick, and key-only versions
* Wrist rest
* Adjustable tenting system
* Removed unused features, files, configuration options, etc

## Acknowledgements

### Inspirations
* `davekincade`'s [Track Beast](https://github.com/davekincade/track_beast) and [build log](https://medium.com/@kincade/track-beast-build-log-a-trackball-dactyl-manuform-19eaa0880222)
* `noahprince22`'s [Tractyl Manuform and build log](https://github.com/noahprince22/tractyl-manuform-keyboard), where I got ideas for the wrist rest and tenting system, and most of my parts list, and from which I might also copy some [firmware](https://github.com/noahprince22/qmk_firmware/compare/noah...noahprince22:trackball).
* `/u/qqurn`'s [trackball writeup](https://www.reddit.com/r/MechanicalKeyboards/comments/g3aue6/the_dactylmanuformrtrack_with_qmk_features_on_a/) and an [earlier version including a joystick](https://www.reddit.com/r/ErgoMechKeyboards/comments/fcsjj7/dactyl_manuform_joystick_and_tracking_ball/)
* `brickbots`'s [aball](https://github.com/brickbots/aball), which is just a trackball, no keyboard, from which I'll might copy some of my QMK firmware.
* `/u/sabborello`'s [reddit post](https://www.reddit.com/r/ErgoMechKeyboards/comments/fqudmp/update_on_split_arcade_aka_the_dactyl_with_a/) which contains a link to some firmware I might copy, and [`/u/qqurn`'s comment](https://www.reddit.com/r/ErgoMechKeyboards/comments/fqudmp/update_on_split_arcade_aka_the_dactyl_with_a/fltf282) with more firmware links.
* [`veikman`](https://github.com/veikman)'s [Conertina build guide](https://viktor.eikman.se/article/concertina-v060-build-guide/) which inspired using a separate piece for the thumbs.

### Resources
* [Dactyl build guide](https://medium.com/swlh/complete-idiot-guide-for-building-a-dactyl-manuform-keyboard-53454845b065)

### Upstream Code
* Forked from [FSund](https://github.com/FSund/dactyl-manuform-keyboard)
* Large amounts of code copied from [dereknheily/dactyl-manuform-tight](https://github.com/dereknheiley/dactyl-manuform-tight/blob/master/src/dactyl_keyboard/dactyl.clj)

## Generating a custom model

* `apt install clojure leiningen openscad`
* `lein generate`

Models will be put in the [`things/`](things/) directory.

* [Some other ways to evaluate the clojure design file](http://stackoverflow.com/a/28213489)
* [Example designing with clojure](http://adereth.github.io/blog/2014/04/09/3d-printing-with-clojure/)


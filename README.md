Work in progress. Do not use, fork, or consider anything in this README to be accurate until this notice is removed.

# Dactyl Manuform Keyboard with Trackball and Joystick

(picture goes here)

## Changes

* Added trackball and joystick mounts.
* Removed unused features, files, etc. Having a powerful configuration system makes it harder to change things in code.

Forked from [FSund](https://github.com/FSund/dactyl-manuform-keyboard). The changes they made from [tshort/Dactyl-ManuForm](https://github.com/tshort/dactyl-keyboard):

* Removed the side-nubs in the switch holes to make the case compatible with Kailh (and similar switches) that don't have the notch for the nubs
* Removed the teensy holder and wire posts, since they supposedly increase printing time by a lot (and I don't have any use for them)
* Included screw hole-, rj9 connector-, and usb hole-fixes as suggested by [jmg123 over at Geekhack](https://geekhack.org/index.php?topic=88576.msg2578041#msg2578041)

## Generating a custom model

* `apt install clojure leiningen openscad`
* `lein generate`

Models will be put in the [`things/`](things/) directory.

* [Some other ways to evaluate the clojure design file](http://stackoverflow.com/a/28213489)
* [Example designing with clojure](http://adereth.github.io/blog/2014/04/09/3d-printing-with-clojure/)


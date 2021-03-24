(defproject dactyl-manuform-keyboard-trackball-joystick "0.1.0-SNAPSHOT"
  :description "Dactyl Manuform Keyboard with Trackball and Joystick"
  :url "https://github.com/skyqrose/dactyl-manuform-keyboard-trackball-joystick"
  :main dactyl-keyboard.dactyl
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :plugins [[lein-auto "0.1.3"]
            [lein-exec "0.3.7"]]
  :aliases {"generate" ["exec" "-p" "src/dactyl_keyboard/dactyl.clj"]}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [scad-clj "0.5.3"]])

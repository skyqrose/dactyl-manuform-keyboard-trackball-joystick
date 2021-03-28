(ns dactyl-keyboard.dactyl
  (:refer-clojure :exclude [use import])
  (:require [clojure.core.matrix :refer [array matrix mmul]]
            [scad-clj.scad :refer :all]
            [scad-clj.model :refer :all]))


(defn deg2rad [degrees]
  (* (/ degrees 180) pi))

;;;;;;;;;;;;;;;;;;;;;;
;; Shape parameters ;;
;;;;;;;;;;;;;;;;;;;;;;

(def nrows 4)
(def ncols 6)

(def column-curvature (deg2rad 17))
(def row-curvature (deg2rad (case nrows
  6 1
  5 2
  4 4)))
(def centerrow (case nrows
  6 3.1
  5 2.1
  4 1.75)) ; controls front-back tilt
(def centercol 3)                       ; controls left-right tilt / tenting (higher number is more tenting)
(def tenting-angle (deg2rad 18))        ; or, change this for more precise tenting control

(defn column-offset [column] (cond
  (= column 2) [0 2.8 -6.5]
  (= column 3) [0 0 -0.5]
  (>= column 4) [0 -16 5]
  :else [0 0 0]))

(def keyboard-z-offset (case nrows
    6 20
    5 10.5
    4 9))                               ; controls overall height

(def extra-width 2)                     ; extra space between the base of keys; original= 2
(def extra-height 1.7)                  ; original= 0.5

(def wall-z-offset -7)                  ; length of the first downward-sloping part of the wall (negative)
(def wall-xy-offset 1)                  ; offset in the x and/or y direction for the first downward-sloping part of the wall (negative)
(def wall-thickness 3)                  ; wall thickness parameter; originally 5

;;;;;;;;;;;;;;;;;;;;;;;
;; General variables ;;
;;;;;;;;;;;;;;;;;;;;;;;

(def lastrow (dec nrows))
(def cornerrow (dec lastrow))
(def lastcol (dec ncols))

;;;;;;;;;;;;;;;;;
;; Switch Hole ;;
;;;;;;;;;;;;;;;;;

(def keyswitch-height 13.8)                                   ;; Was 14.1, then 14.25
(def keyswitch-width 13.9)
(def plate-thickness 5)
(def use_hotswap true)

(def retention-tab-thickness 1.5)
(def retention-tab-hole-thickness (- plate-thickness retention-tab-thickness))
(def edge-width 1.5)
(def edge-height 1.5)
(def mount-width (+ keyswitch-width (* 2 edge-width)))
(def mount-height (+ keyswitch-height (* 2 edge-height)))

(def holder-x mount-width)
(def holder-thickness    (/ (- holder-x keyswitch-width) 2))
(def holder-y            (+ keyswitch-height (* holder-thickness 2)))
(def swap-z              3)
(def web-thickness (+ plate-thickness swap-z))
(def keyswitch-below-plate (- 8 web-thickness))           ; approx space needed below keyswitch
(def LED-holder true)
(def square-led-size     6)

(def switch-teeth-cutout
  (let [
        ; cherry, gateron, kailh switches all have a pair of tiny "teeth" that stick out
        ; on the top and bottom, this gives those teeth somewhere to press into
        teeth-x        4.5
        teeth-y        0.75
        teeth-z        1.75
        teeth-x-offset 0
        teeth-y-offset (+ (/ keyswitch-height 2) (/ teeth-y 2.01))
        teeth-z-offset (- plate-thickness 1.95)
       ]
      (->> (cube teeth-x teeth-y teeth-z)
           (translate [teeth-x-offset teeth-y-offset teeth-z-offset])
      )
  )
)

(def hotswap-y1 4.3) ;first y-size of kailh hotswap holder
(def hotswap-y2 6.2) ;second y-size of kailh hotswap holder
(def hotswap-z (+ swap-z 0.2)) ;thickness of kailn hotswap holder + some margin of printing error (0.2mm)
(def hotswap-cutout-z-offset -2.6)
(def hotswap-cutout-1-y-offset 4.95)
(def hotswap-holder
        ;irregularly shaped hot swap holder
        ; ___________
        ;|_|_______| |  hotswap offset from out edge of holder with room to solder
        ;|_|_O__  \ _|  hotswap pin
        ;|      \O_|_|  hotswap pin
        ;|  o  O  o  |  fully supported friction holes
        ;|    ___    |
        ;|    |_|    |  space for LED under SMD or transparent switches
        ;
        ; can be described as having two sizes in the y dimension depending on the x coordinate
  (let [
        swap-x              holder-x
        swap-y              (if (or (> 11.5 holder-y) LED-holder) holder-y 11.5) ; should be less than or equal to holder-y
        swap-offset-x       0
        swap-offset-y       (/ (- holder-y swap-y) 2)
        swap-offset-z       (* (/ swap-z 2) -1) ; the bottom of the hole.
        swap-holder         (->> (cube swap-x swap-y swap-z)
                                 (translate [swap-offset-x
                                             swap-offset-y
                                             swap-offset-z]))
        hotswap-x           holder-x ;cutout full width of holder instead of only 14.5mm
        hotswap-x2          (* (/ holder-x 3) 1.95)
        hotswap-x3          (/ holder-x 4)
        hotswap-cutout-1-x-offset 0.01
        hotswap-cutout-2-x-offset (* (/ holder-x 4.5) -1)
        hotswap-cutout-3-x-offset (- (/ holder-x 2) (/ hotswap-x3 2))
        hotswap-cutout-4-x-offset (- (/ hotswap-x3 2) (/ holder-x 2))
        hotswap-cutout-led-x-offset 0
        hotswap-cutout-1-y-offset 4.95
        hotswap-cutout-2-y-offset 4
        hotswap-cutout-3-y-offset (/ holder-y 2)
        hotswap-cutout-led-y-offset -6
        hotswap-cutout-1    (->> (cube hotswap-x hotswap-y1 hotswap-z)
                                 (translate [hotswap-cutout-1-x-offset
                                             hotswap-cutout-1-y-offset
                                             hotswap-cutout-z-offset]))
        hotswap-cutout-2    (->> (cube hotswap-x2 hotswap-y2 hotswap-z)
                                 (translate [hotswap-cutout-2-x-offset
                                             hotswap-cutout-2-y-offset
                                             hotswap-cutout-z-offset]))
        hotswap-cutout-3    (->> (cube hotswap-x3 hotswap-y1 hotswap-z)
                                 (translate [ hotswap-cutout-3-x-offset
                                              hotswap-cutout-3-y-offset
                                              hotswap-cutout-z-offset]))
        hotswap-cutout-4    (->> (cube hotswap-x3 hotswap-y1 hotswap-z)
                                 (translate [ hotswap-cutout-4-x-offset
                                              hotswap-cutout-3-y-offset
                                              hotswap-cutout-z-offset]))
        hotswap-led-cutout  (->> (cube square-led-size square-led-size 10)
                                 (translate [ hotswap-cutout-led-x-offset
                                              hotswap-cutout-led-y-offset
                                              hotswap-cutout-z-offset]))
        ; for the main axis
        main-axis-hole      (->> (cylinder (/ 4.1 2) 10)
                                 (with-fn 12))
        plus-hole           (->> (cylinder (/ 3.3 2) 10)
                                 (with-fn 8)
                                 (translate [-3.81 2.54 0]))
        minus-hole          (->> (cylinder (/ 3.3 2) 10)
                                 (with-fn 8)
                                 (translate [2.54 5.08 0]))
        friction-hole       (->> (cylinder (/ 1.95 2) 10)
                                 (with-fn 8))
        friction-hole-right (translate [5 0 0] friction-hole)
        friction-hole-left  (translate [-5 0 0] friction-hole)
       ]
      (difference swap-holder
                  main-axis-hole
                  plus-hole
                  minus-hole
                  friction-hole-left
                  friction-hole-right
                  hotswap-cutout-1
                  hotswap-cutout-2
                  hotswap-cutout-3
                  hotswap-cutout-4
                  hotswap-led-cutout)
  )
)

(defn single-plate [is-left-side]
  (let [
        top-edge-center (/ (+ edge-height keyswitch-height) 2)
        left-edge-center (/ (+ edge-width keyswitch-width) 2)
        top-wall (->>
          (cube mount-width edge-height plate-thickness)
          (translate [0 top-edge-center (/ plate-thickness 2)]))
        left-wall (->>
          (cube edge-width mount-height plate-thickness)
          (translate [left-edge-center 0 (/ plate-thickness 2)]))
        half-plate (difference (union top-wall left-wall) switch-teeth-cutout)
       ]
    (union
      half-plate
      (->> half-plate (mirror [0 1 0]) (mirror [1 0 0]))
      (mirror [(if is-left-side 1 0) 0 0] hotswap-holder))
    ))

;;;;;;;;;;;;;;;;
;; SA Keycaps ;;
;;;;;;;;;;;;;;;;

(def sa-length 18.25)
(def sa-height 12.5)

(def sa-key-height-from-plate 7.39)
(def sa-cap-bottom-height (+ sa-key-height-from-plate plate-thickness))
(def sa-cap-bottom-height-pressed (+ 3 plate-thickness))

(def sa-double-length 37.5)
(def sa-cap
  (let [bl2 (/ sa-length 2)
                     m 8.5
                     key-cap (hull (->> (polygon [[bl2 bl2] [bl2 (- bl2)] [(- bl2) (- bl2)] [(- bl2) bl2]])
                                        (extrude-linear {:height 0.1 :twist 0 :convexity 0})
                                        (translate [0 0 0.05]))
                                   (->> (polygon [[m m] [m (- m)] [(- m) (- m)] [(- m) m]])
                                        (extrude-linear {:height 0.1 :twist 0 :convexity 0})
                                        (translate [0 0 6]))
                                   (->> (polygon [[6 6] [6 -6] [-6 -6] [-6 6]])
                                        (extrude-linear {:height 0.1 :twist 0 :convexity 0})
                                        (translate [0 0 sa-height])))]
  (color [220/255 163/255 163/255 1] (union
    (translate [0 0 sa-cap-bottom-height] key-cap)
    (translate [0 0 sa-cap-bottom-height-pressed] key-cap)
  ))))

;;;;;;;;;;;;;;;;;;;;;;;;;
;; Placement Functions ;;
;;;;;;;;;;;;;;;;;;;;;;;;;

(def sa-profile-key-height 12.7)

(def cap-top-height (+ plate-thickness sa-profile-key-height))
(def row-radius (+ (/ (/ (+ mount-height extra-height) 2)
                      (Math/sin (/ column-curvature 2)))
                   cap-top-height))
(def column-radius (+ (/ (/ (+ mount-width extra-width) 2)
                         (Math/sin (/ row-curvature 2)))
                      cap-top-height))

(defn apply-key-geometry [translate-fn rotate-x-fn rotate-y-fn column row shape]
  (let [column-angle (* row-curvature (- centercol column))
        placed-shape (->> shape
                          (translate-fn [0 0 (- row-radius)])
                          (rotate-x-fn  (* column-curvature (- centerrow row)))
                          (translate-fn [0 0 row-radius])
                          (translate-fn [0 0 (- column-radius)])
                          (rotate-y-fn  column-angle)
                          (translate-fn [0 0 column-radius])
                          (translate-fn (column-offset column)))
       ]
    (->> placed-shape
         (rotate-y-fn  tenting-angle)
         (translate-fn [0 0 keyboard-z-offset]))))

(defn key-place [column row shape]
  (apply-key-geometry translate 
    (fn [angle obj] (rotate angle [1 0 0] obj)) 
    (fn [angle obj] (rotate angle [0 1 0] obj)) 
    column row shape))

(defn rotate-around-x [angle position] 
  (mmul 
   [[1 0 0]
    [0 (Math/cos angle) (- (Math/sin angle))]
    [0 (Math/sin angle)    (Math/cos angle)]]
   position))

(defn rotate-around-y [angle position] 
  (mmul 
   [[(Math/cos angle)     0 (Math/sin angle)]
    [0                    1 0]
    [(- (Math/sin angle)) 0 (Math/cos angle)]]
   position))

(defn key-position [column row position]
  (apply-key-geometry (partial map +) rotate-around-x rotate-around-y column row position))

(defn key-places [shape]
  (apply union
         (for [column (range 0 ncols)
               row (range 0 nrows)
               :when (or (.contains [2 3] column)
                         (not= row lastrow))]
           (->> shape
                (key-place column row)))))

(defn key-holes [is-left-side]
  (key-places (single-plate is-left-side)))

(def caps
  (key-places sa-cap))

;;;;;;;;;;;;;;;;;;;;
;; Web Connectors ;;
;;;;;;;;;;;;;;;;;;;;

(def web-thickness plate-thickness)
(def post-size 1.2)
(def web-post (->> (cube post-size post-size web-thickness)
                   (translate [0 0 (+ (/ web-thickness -2)
                                      plate-thickness)])))

(def post-adj (/ post-size 2))
(def web-post-tr (translate [(- (/ mount-width 2) post-adj) (- (/ mount-height 2) post-adj) 0] web-post))
(def web-post-tl (translate [(+ (/ mount-width -2) post-adj) (- (/ mount-height 2) post-adj) 0] web-post))
(def web-post-bl (translate [(+ (/ mount-width -2) post-adj) (+ (/ mount-height -2) post-adj) 0] web-post))
(def web-post-br (translate [(- (/ mount-width 2) post-adj) (+ (/ mount-height -2) post-adj) 0] web-post))

(def connectors
  (apply union
         (concat
          ;; Row connections
          (for [column (range 0 (dec ncols))
                row (range 0 nrows)
                :when (or (not= row lastrow) (= 2 column))]
            (hull
             (key-place (inc column) row web-post-tl)
             (key-place column row web-post-tr)
             (key-place (inc column) row web-post-bl)
             (key-place column row web-post-br)))

          ;; Column connections
          (for [column (range 0 ncols)
                row (range 0 (dec nrows))
                :when (or (not= row (dec lastrow))
                          (= column 2)
                          (= column 3))]
            (hull
             (key-place column row web-post-bl)
             (key-place column row web-post-br)
             (key-place column (inc row) web-post-tl)
             (key-place column (inc row) web-post-tr)))

          ;; Diagonal connections
          (for [column (range 0 (dec ncols))
                row (range 0 (dec nrows))
                :when (or (not= row (dec lastrow)) (= column 2))]
            (hull
             (key-place column row web-post-br)
             (key-place column (inc row) web-post-tr)
             (key-place (inc column) row web-post-bl)
             (key-place (inc column) (inc row) web-post-tl))))))

;;;;;;;;;;;;
;; Thumbs ;;
;;;;;;;;;;;;

(def thumb-offsets (if (> nrows 4) [8 -5 8] [9 -5 4]))

(def thumborigin
  (map + (key-position 1 cornerrow [(/ mount-width 2) (- (/ mount-height 2)) 0])
       thumb-offsets))

"need to account for plate thickness which is baked into thumb-_-place rotation & move values
plate-thickness was 2
need to adjust for difference for thumb-z only"
(def thumb-design-z 2)
(def thumb-z-adjustment (- (if (> plate-thickness thumb-design-z)
                                 (- thumb-design-z plate-thickness)
                                 (if (< plate-thickness thumb-design-z)
                                       (- thumb-design-z plate-thickness)
                                       0))
                            1.1))
(def thumb-x-rotation-adjustment (if (> nrows 4) -12 -8))
(defn thumb-place [rot move shape]
  (->> shape

       (translate [0 0 thumb-z-adjustment])                   ;adapt thumb positions for increased plate
       (rotate (deg2rad thumb-x-rotation-adjustment) [1 0 0]) ;adjust angle of all thumbs to be less angled down towards user since key is taller

       (rotate (deg2rad (nth rot 0)) [1 0 0])
       (rotate (deg2rad (nth rot 1)) [0 1 0])
       (rotate (deg2rad (nth rot 2)) [0 0 1])
       (translate thumborigin)
       (translate move)))

; convexer
(defn thumb-r-place [shape] (thumb-place [14 -40 10] [-15 -10 5] shape)) ; right
(defn thumb-m-place [shape] (thumb-place [10 -23 20] [-33 -15 -6] shape)) ; middle
(defn thumb-l-place [shape] (thumb-place [6 -5 35] [-52.5 -25.5 -11.5] shape)) ; left

(defn thumb-layout [shape]
  (union
    (thumb-r-place shape)
    (thumb-m-place shape)
    (thumb-l-place shape)
    ))

(def thumbcaps (thumb-layout sa-cap))
(defn thumb [is-left-side] (thumb-layout (single-plate is-left-side)))

;;;;;;;;;;
;; Case ;;
;;;;;;;;;;

(defn bottom [height p]
  (->> (project p)
       (extrude-linear {:height height :twist 0 :convexity 0})
       (translate [0 0 (- (/ height 2) 10)])))

(defn bottom-hull [& p]
  (hull p (bottom 0.001 p)))


(defn wall-locate1 [dx dy] [(* dx wall-thickness)                    (* dy wall-thickness)                    0])
(defn wall-locate2 [dx dy] [(* dx wall-xy-offset)                    (* dy wall-xy-offset)                    wall-z-offset])
(defn wall-locate3 [dx dy] [(* dx (+ wall-xy-offset wall-thickness)) (* dy (+ wall-xy-offset wall-thickness)) wall-z-offset])

; dx1, dy1, dx2, dy2 = direction of the wall. '1' for front, '-1' for back, '0' for 'not in this direction'.
; place1, place2 = function that places an object at a location, typically refers to the center of a key position.
; post1, post2 = the shape that should be rendered
(defn wall-brace [place1 dx1 dy1 post1 place2 dx2 dy2 post2]
  (union
    (->> (hull
      (place1 post1)
      (place1 (translate (wall-locate1 dx1 dy1) post1))
      (place1 (translate (wall-locate2 dx1 dy1) post1))
      (place2 post2)
      (place2 (translate (wall-locate1 dx2 dy2) post2))
      (place2 (translate (wall-locate2 dx2 dy2) post2))
      ))
    (->> (bottom-hull
      (place1 (translate (wall-locate2 dx1 dy1) post1))
      (place2 (translate (wall-locate2 dx2 dy2) post2))
      ))
  ))

(defn key-wall-brace [x1 y1 dx1 dy1 post1 x2 y2 dx2 dy2 post2]
  (wall-brace (partial key-place x1 y1) dx1 dy1 post1
              (partial key-place x2 y2) dx2 dy2 post2))

(defn key-corner [x y loc]
  (case loc
    :tl (key-wall-brace x y 0  1 web-post-tl x y -1 0 web-post-tl)
    :tr (key-wall-brace x y 0  1 web-post-tr x y  1 0 web-post-tr)
    :bl (key-wall-brace x y 0 -1 web-post-bl x y -1 0 web-post-bl)
    :br (key-wall-brace x y 0 -1 web-post-br x y  1 0 web-post-br)))

(def case-walls
  (union
    ; corners
    (key-corner lastcol 0 :tr) ; top right
    (key-corner lastcol cornerrow :br) ; bottom right
    (key-corner 0 0 :tl) ; left back
    (key-corner 0 cornerrow :bl) ; bottom left
    ; right-wall
    (for [y (range 0 (dec nrows))] (key-wall-brace lastcol      y  1 0 web-post-tr lastcol y 1 0 web-post-br))
    (for [y (range 1 (dec nrows))] (key-wall-brace lastcol (dec y) 1 0 web-post-br lastcol y 1 0 web-post-tr))
    ; back wall
    (for [x (range 0 ncols)] (key-wall-brace x 0 0 1 web-post-tl      x  0 0 1 web-post-tr))
    (for [x (range 1 ncols)] (key-wall-brace x 0 0 1 web-post-tl (dec x) 0 0 1 web-post-tr))
    ; left wall
    (for [y (range 0 (dec nrows))] (key-wall-brace 0      y  -1 0 web-post-tl 0 y -1 0 web-post-bl))
    (for [y (range 1 (dec nrows))] (key-wall-brace 0 (dec y) -1 0 web-post-bl 0 y -1 0 web-post-tl))
    ; front wall, left to right
    (key-wall-brace 0 cornerrow 0 -1 web-post-bl 0 cornerrow 0 -1 web-post-br)
    (key-wall-brace 0 cornerrow 0 -1 web-post-br 1 cornerrow 0 -1 web-post-bl)
    (key-wall-brace 1 cornerrow 0 -1 web-post-bl 1 cornerrow 0 -1 web-post-br)
    (key-wall-brace 1 cornerrow 0 -1 web-post-br 2 lastrow -1 0 web-post-tl)
    (key-wall-brace 2 lastrow -1 0 web-post-tl 2 lastrow -1 0 web-post-bl)
    (key-corner 2 lastrow :bl)
    (key-wall-brace 2 lastrow 0 -1 web-post-bl 2 lastrow 0 -1 web-post-br)
    (key-wall-brace 2 lastrow 0 -1 web-post-br 3 lastrow 0 -1 web-post-bl)
    (key-wall-brace 3 lastrow 0 -1 web-post-bl 3 lastrow 0.5 -1 web-post-br)
    (key-wall-brace 3 lastrow 0.5 -1 web-post-br 4 cornerrow 0.5 -1 web-post-bl)
    (for [x (range 4 ncols)] (key-wall-brace x cornerrow 0 -1 web-post-bl      x  cornerrow 0 -1 web-post-br))
    (for [x (range 5 ncols)] (key-wall-brace x cornerrow 0 -1 web-post-bl (dec x) cornerrow 0 -1 web-post-br))
    ; fill gaps left by concave corners on front
    (->> (hull
      (key-place 3 cornerrow web-post-br)
      (key-place 3 lastrow web-post-tr)
      (key-place 4 cornerrow web-post-bl)))
    (->> (hull
      (key-place 3 lastrow web-post-tr)
      (key-place 3 lastrow web-post-br)
      (key-place 4 cornerrow web-post-bl)))
    (->> (hull
      (key-place 2 lastrow web-post-tl)
      (key-place 2 cornerrow web-post-bl)
      (key-place 1 cornerrow web-post-br)))
    ))

;;;;;;;;;;;
;; Ports ;;
;;;;;;;;;;;

(def rj9-start  (map + [0 -3  0] (key-position 0 0 (map + (wall-locate3 0 1) [0 (/ mount-height  2) 0]))))
(def rj9-position  [(first rj9-start) (second rj9-start) 11])
(def rj9-shell (translate rj9-position (cube 14.78 13 22.38)))
(def rj9-void (translate rj9-position (union
   (translate [0 2 0] (cube 10.78  10 18.38))
   (translate [0 -0.01 5] (cube 10.78 14  5))
)))

(defn screw-insert-shape [bottom-radius top-radius height]
   (union (translate [0 0 (/ height 2)] (cylinder [bottom-radius top-radius] height))
          (translate [0 0 height] (sphere top-radius))))
(def screw-insert-void (screw-insert-shape (/ 5.31 2) (/ 5.1 2) 3.8))
(def screw-insert-shell (screw-insert-shape 4.255 4.15 5.3))
(def screw-plate-hole (screw-insert-shape 1.7 1.7 350))
(defn screw-position [column row xoffset yoffset]
  (let [position (key-position column row [0 0 0])]
  [(+ (first position) xoffset) (+ (second position) yoffset) 0]))
(def screw-positions (if (> nrows 4) [
  (screw-position 2 0 0 5.5) ;bc top middle
  (screw-position 0 1 -9.5 -8) ;ml left
  (screw-position 0 lastrow -29.5 -17.5) ;thmb thumb
  (screw-position (- lastcol 1) 0 20.1 5.1) ;br top right
  (screw-position 0 (+ lastrow 1) 16.7 7) ;fc bottom middle
] [
  (screw-position 2 0 -3.7 7) ;bc top middle
  (screw-position 0 1 -8 -8) ;ml left
  (screw-position 0 lastrow -7.5 -3.9) ;thmb thumb
  (screw-position (- lastrow 1) 0 23.7 7) ;br top right
  (screw-position 0 (+ lastrow 1) 21 9.5) ;fc bottom middle
]))

(defn copy-to-positions [positions shape]
  (->> positions
       (map (fn [position]
              (translate [(first position) (second position) 0] shape)))
       union))
(def screw-insert-voids (copy-to-positions screw-positions screw-insert-void))
(def screw-insert-shells (copy-to-positions screw-positions screw-insert-shell))
(def screw-plate-holes (copy-to-positions screw-positions screw-plate-hole))

;;;;;;;;;;;;;;
;; Assembly ;;
;;;;;;;;;;;;;;

(def model-right
  (difference
    (union
      (key-holes false)
      connectors
      (thumb false)
      case-walls
      screw-insert-shells
      rj9-shell
      thumbcaps
      caps
    )
    rj9-void
    screw-insert-voids
    (translate [0 0 -20] (cube 350 350 40))
  )
)

(def bottom-plate-thickness 3)
(def bottom-plate
  (let
    [
      bottom-outline (cut (translate [0 0 -0.1] case-walls))
      inner-thing (->>
        (union
          (extrude-linear {:height 99 :scale  0.1 :center true} bottom-outline)
          (cube 50 50 bottom-plate-thickness)
        )
        project
        (translate [0 0 -0.1])
      )
      bottom-plate-blank (extrude-linear {:height bottom-plate-thickness} inner-thing)
    ]
    (difference
      bottom-plate-blank
      screw-plate-holes
    )
  )
)

(spit "things/test.scad"
      (write-scad (single-plate false)))

(spit "things/right.scad"
      (write-scad model-right))

(spit "things/bottom-plate.scad"
      (write-scad bottom-plate))

(defn -main [dum] 1)  ; dummy to make it easier to batch

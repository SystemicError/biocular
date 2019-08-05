(ns biocular.core
  (:require [clojure.string :as str]
            [clojure.edn :as edn]))

(println "Program started.")

(defn load-photo [id-str]
  "Loads the appropriate file into the appropriate image."
  (let [photo (.getElementById js/document id-str)
        filepath (.-value (.getElementById js/document (str id-str "-file")))]
    (set! (.-src photo) filepath)))

(defn get-coord-pairs []
  "Returns a list of all coordinate pairs."
  (let [coord-pairs (.-value (.getElementById js/document "coord-pairs"))]
    (edn/read-string coord-pairs)))

(defn add-coord-pair []
  (let [coord-pairs (get-coord-pairs)
        left-pair (edn/read-string (.-value (.getElementById js/document "left-photo-text")))
        right-pair (edn/read-string (.-value (.getElementById js/document "right-photo-text")))
        new-pair {:y (/ (+ (nth left-pair 1) (nth right-pair 1)) 2)
                  :x-left (first left-pair)
                  :x-right (first right-pair)}]
    (set! (.-value (.getElementById js/document "coord-pairs")) (str (cons new-pair 
                                                                           (if coord-pairs
                                                                             coord-pairs
                                                                             []))))))

(defn set-coords [id-str event]
  "Sets a pair of coordinates for the specified photo."
  (let [coord-text (.getElementById js/document (str id-str "-text"))
        photo (.getElementById js/document id-str)
        bounding-rect (.getBoundingClientRect photo)
        x (- (.-pageX event) (.-left bounding-rect))
        y (- (.-pageY event) (.-top bounding-rect))
        x-from-center (- x (/ (.-width photo) 2.0))
        y-from-center (- y (/ (.-height photo) 2.0))]
    (set! (.-value coord-text) (str [x-from-center y-from-center]))
    ))

(defn triangulate [pair focal-length separation]
  "Takes in :y :x-left :x-right  focal-length (in pixels) and triangulates location with respect to left camera."
  (let [left-x (:x-left pair)
        right-x (:x-right pair)
        scale (/ separation (- left-x right-x))
        dummy (js/console.log (str "fl,sep,scale = " focal-length ", " separation ", " scale))
        x (* scale left-x) ; with respect to left camera
        z (* scale focal-length) ; depth in front of camera
        y (* (:y pair) scale)]
    [x y z]))

(defn get-focal-length []
  (edn/read-string (.-value (.getElementById js/document "focal-length"))))

(defn get-separation []
  (edn/read-string (.-value (.getElementById js/document "separation"))))

(defn draw-points [ctx points center]
  "Draws points on the canvas"
  (if (> (count points) 0)
    (let [coord (first points)
          x (first coord)
          y (nth coord 1)
          z (nth coord 2)
          cx (:x center)
          cy (:y center)]
      (.beginPath ctx)
      (.arc ctx (+ cx x) (+ cy y) (/ 1000.0 z) 0 6.28)
      (js/console.log (str "x,y" x ", " y "\n"
                           "cx,cy" cx ", " cy "\n"
                           "cx+x,cy+y" (+ cx x) ", " (+ cy y)))
      (.fill ctx)
      (recur ctx (rest points) center))))

(defn draw-model [model]
  "Draws the 3d model on the canvas."
  (let [canvas (.getElementById js/document "model-canvas")
        ctx (.getContext canvas "2d")
        w (.-width canvas)
        h (.-height canvas)]
    (.clearRect ctx 0 0 w h)
    (draw-points ctx model {:x (/ w 2.0) :y (/ h 2.0)})))

(defn generate-model []
  "Generate a model based on coord pairs."
  (let [coord-pairs (get-coord-pairs)
        focal-length (get-focal-length)
        separation (get-separation)
        dummy (js/console.log (str "fl,s = " focal-length ", " separation))
        model (map #(triangulate % focal-length separation) coord-pairs)
        model-coords (.getElementById js/document "model-coords")]
    (set! (.-value model-coords) model)
    (draw-model model)))


(set! (.-onclick (.getElementById js/document "generate-model")) generate-model)
(set! (.-onclick (.getElementById js/document "add-coord-pair")) add-coord-pair)

(set! (.-onclick (.getElementById js/document "left-photo")) (partial set-coords "left-photo"))
(set! (.-onclick (.getElementById js/document "right-photo")) (partial set-coords "right-photo"))

(set! (.-oninput (.getElementById js/document "left-photo-file")) (partial load-photo "left-photo"))
(set! (.-oninput (.getElementById js/document "right-photo-file")) (partial load-photo "right-photo"))


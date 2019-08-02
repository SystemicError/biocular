(ns biocular.core
  (:require [clojure.string :as str]
            [clojure.edn :as edn]))

(println "Program started.")

(defn load-photo [id-str]
  "Loads the appropriate file into the appropriate image."
  (let [photo (.getElementById js/document id-str)
        filepath (.-value (.getElementById js/document (str id-str "-file")))]
    (set! (.-src photo) filepath)))

(defn read-coord-pairs []
  "Returns a list of all coordinate pairs."
  (let [coord-pairs (.-value (.getElementById js/document "coord-pairs"))]
    (edn/read-string coord-pairs)))

(defn add-coord-pair []
  (let [coord-pairs (read-coord-pairs)
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
        dummy (js/console.log (str id-str "offsetleft" (.-offsetLeft photo)))
        bounding-rect (.getBoundingClientRect photo)
        x (- (.-pageX event) (.-left bounding-rect))
        y (- (.-pageY event) (.-top bounding-rect))
        x-from-center (- x (/ (.-width photo) 2.0))
        y-from-center (- y (/ (.-height photo) 2.0))
        dummy (js/console.log (str x-from-center ", " y-from-center))]
    (set! (.-value coord-text) (str [x-from-center y-from-center]))
    ))

(defn triangulate [pair focal-length]
  "Takes in :y :left-x :right-x  focal-length (in pixels) and triangulates location with respect to left camera."
  (let [azimuth-left nil
        azimuth-right nil
        elevation nil
        distance-left nil]
  ))

(set! (.-onclick (.getElementById js/document "add-coord-pair")) add-coord-pair)

(set! (.-onclick (.getElementById js/document "left-photo")) (partial set-coords "left-photo"))
(set! (.-onclick (.getElementById js/document "right-photo")) (partial set-coords "right-photo"))

(set! (.-oninput (.getElementById js/document "left-photo-file")) (partial load-photo "left-photo"))
(set! (.-oninput (.getElementById js/document "right-photo-file")) (partial load-photo "right-photo"))


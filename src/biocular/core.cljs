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
        right-pair (edn/read-string (.-value (.getElementById js/document "right-photo-text")))]
    (set! (.-value (.getElementById js/document "coord-pairs")) (str (cons [left-pair right-pair] 
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
        dummy (js/console.log (str x ", " y))]
    (set! (.-value coord-text) (str [x y]))
    ))

(set! (.-onclick (.getElementById js/document "add-coord-pair")) add-coord-pair)

(set! (.-onclick (.getElementById js/document "left-photo")) (partial set-coords "left-photo"))
(set! (.-onclick (.getElementById js/document "right-photo")) (partial set-coords "right-photo"))

(set! (.-oninput (.getElementById js/document "left-photo-file")) (partial load-photo "left-photo"))
(set! (.-oninput (.getElementById js/document "right-photo-file")) (partial load-photo "right-photo"))


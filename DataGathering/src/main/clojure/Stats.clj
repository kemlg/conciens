(ns Stats
  (:use (incanter core stats charts datasets io)))

;(def pregs (concat (range 2 24) (range 26 43)))
(def data (read-dataset "/Users/sergio/Downloads/pregs.csv" :header true :delim \;))
(def pregs (range (count (:column-names data))))
(def mat (to-matrix data))
(def pca (principal-components (sel mat :cols pregs)))

;; extract the first two principal components
(def pc1 (sel (:rotation pca) :cols 0))
(def pc2 (sel (:rotation pca) :cols 1))
;(def pc3 (sel (:rotation pca) :cols 2))

;; project the first four dimension of the iris data onto the first
;; two principal components
(def x1 (mmult (sel mat :cols pregs) pc1))
(def x2 (mmult (sel mat :cols pregs) pc2))
;(def x3 (mmult (sel mat :cols pregs) pc3))

;; now plot the transformed data, coloring each species a different color
(doto
  (scatter-plot
    (sel x1 :rows (range 50))
    (sel x2 :rows (range 50))
    ;(sel x3 :rows (range 50))
    :x-label "PC1" :y-label "PC2" :title "Iris PCA")
  (add-points
    (sel x1 :rows (range 50 100))
    (sel x2 :rows (range 50 100))
    ;(sel x3 :rows (range 50 100))
    )
  (add-points
    (sel x1 :rows (range 100 150))
    (sel x2 :rows (range 100 150))
    ;(sel x3 :rows (range 100 150))
    )
  view)

(:rotation pca)
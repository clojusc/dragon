(ns dragon.blog.tags
  (:require [clojure.set :refer [union]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Utility Functions   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn unique
  [data]
  (->> data
       (map :tags)
       (apply union)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Core Functions   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn get-freqs
  [data]
  (->> data
       ;; XXX use mapcat here instead
       (map (comp vec :tags))
       (flatten)
       (frequencies)))

(defn add-percents
  [total highest tag-data]
  (let [percent (float (/ (:count tag-data) highest))]
    (assoc tag-data
           :total total
           :highest highest
           :percent percent
           :five-star (Math/round (* 5 percent))
           :hundred (Math/round (* 100 percent)))))

(defn add-all-percents
  [total highest tags-data]
  (map #(add-percents total highest %) tags-data))

(defn get-stats
  [data]
  (let [freqs (get-freqs data)
        total (reduce + (vals freqs))
        highest (apply max (vals freqs))]
    (->> freqs
         vec
         (map #(zipmap [:name :count] %))
         (add-all-percents total highest)
         (sort-by :name))))

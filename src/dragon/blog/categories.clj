(ns dragon.blog.categories)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Utility Functions   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn unique
  [data]
  (set (map :category data)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Core Functions   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn get-freqs
  [data]
  (frequencies (map :category data)))

(defn add-percents
  [total highest category-data]
  (let [percent (float (/ (:count category-data) highest))]
    (assoc category-data
           :total total
           :highest highest
           :percent percent
           :five-star (Math/round (* 5 percent))
           :hundred (Math/round (* 100 percent)))))

(defn add-all-percents
  [total highest categories-data]
  (map #(add-percents total highest %) categories-data))

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

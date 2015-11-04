(ns cheryl.core
  (:gen-class)
  (:refer-clojure :exclude [==])
  (use clojure.core.logic))

(def all-days [[:May 15] [:May 16] [:May 19] [:June 17] [:June 18] [:July 14]
               [:July 16] [:August 14] [:August 15] [:August 17]]) 

(defn days-for-month [days m]
  (get (group-by first days) m))

(defn months-for-day [days d]
  (get (group-by second days) d))

(defn unique-month-for-day? [[days d]]
  (= 1 (count (months-for-day days d))))

(defn unique-day-for-month? [[days m]]
  (= 1 (count (days-for-month days m))))

(defn some-unique-days? [[days m]]
  (boolean 
    (some
      (fn [[month d]] 
        (unique-month-for-day? [days d])) 
      (days-for-month days m))))

(defn first-rule [days]
  (run* [m d]
        (membero [m d] days)
        (pred [days m] (complement unique-day-for-month?))
        (pred [days m] (complement some-unique-days?))))

(defn second-rule [days]
  (run* [m d]
        (membero [m d] days)
        (pred [days d] unique-month-for-day?)))

(defn third-rule [days]
  (run* [m d]
        (membero [m d] days)
        (pred [days m] unique-day-for-month?)))

(defn solve []
  (-> all-days
      first-rule
      second-rule
      third-rule))

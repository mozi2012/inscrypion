(ns inscrypion.foo
  (:require [clojure.string :as str]))

(defn my-function
  [arg1 arg2]
  (let [result (+ arg1 arg2)]
    (println "Result:" result)

    result))

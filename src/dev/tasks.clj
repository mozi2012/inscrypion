(ns tasks
  (:require
    [clojure.java.io :as io]
    [clojure.java.shell :as sh]
    [clojure.string :as str]))


(defn- emacs-temp-file?
  [^java.io.File f]
  (let [name (.getName f)]
    (or (str/ends-with? name "~")
        (and (str/starts-with? name "#") (str/ends-with? name "#")))))


(defn- emacs-temp-files
  [root]
  (->> (file-seq (io/file root))
       (filter #(.isFile ^java.io.File %))
       (filter emacs-temp-file?)))


(defn clean
  [args]
  (let [paths [".cpcache" "target" ".shadow-cljs" "public/js" "lib/cljd-out"
               ".clojuredart"]
        existing-paths (filter #(.exists (io/file %)) paths)
        emacs-files (emacs-temp-files ".")
        emacs-paths (map #(.getPath ^java.io.File %) emacs-files)]
    (cond (or (seq existing-paths) (seq emacs-paths))
          (do (when (seq existing-paths)
                (println "Cleaning:" (str/join ", " existing-paths))
                (apply sh/sh "rm" "-rf" existing-paths))
              (when (seq emacs-paths)
                (println "Cleaning emacs temp files:"
                         (str/join ", " emacs-paths))
                (apply sh/sh "rm" "-f" emacs-paths))
              (println "Clean complete."))
          :else (println "Nothing to clean."))))


(defn- run-cljstyle
  []
  (let [{:keys [exit out err]} (sh/sh "mise" "exec" "--" "cljstyle" "fix")]
    (if (not= 0 exit)
      (do (binding [*out* *err*]
            (println "cljstyle error:")
            (println err)
            (println out))
          exit)
      (do (when (seq (str/trim out))
            (print out)
            (flush))
          0))))


(defn cljstyle-fix
  [_]
  (System/exit (run-cljstyle)))


(defn format-codebase
  [_]
  (System/exit (run-cljstyle)))

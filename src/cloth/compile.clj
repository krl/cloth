(ns cloth.compile
  (:use [cloth.instructions :only [INSTRUCTIONS]]
        [clojure.java.shell :only [sh]]))

(defn hex-to-instructions [hex]
  (let [data (atom 0)]
    (vec
     (map (fn [instr]
            ;; don't try to make instructions of things to be pushed as data
            (cond (not (zero? @data))
                  (do
                    (swap! data dec)
                    (Integer/parseInt instr 16))

                  :else
                  (do
                    (when (or (= (subs instr 0 1) "6")
                              (= (subs instr 0 1) "7"))
                      (reset! data (- (Integer/parseInt instr 16) 95)))
                    (get INSTRUCTIONS instr))))
          (map #(apply str %) (partition 2 hex))))))

(defn compile-lll-file [filename]
  (hex-to-instructions
   (:out (sh "lllc" :in (slurp filename)))))

(defn compile-lll-string [string]
  (hex-to-instructions
   (:out (sh "lllc" :in string))))

(compile-lll-string "(return 888)")

(defn compile-serpent-string [string]
  (hex-to-instructions
   (:out (sh "sc" "compile" string))))

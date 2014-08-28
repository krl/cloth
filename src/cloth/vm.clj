(ns cloth.vm
  (:require [clojure.pprint :as pp]))

(defmacro dbg [x] `(let [x# ~x] (println "dbg:" '~x "=" x#) x#))

(def *instructions* (atom {}))

(defmacro def-special-instruction [name args & body]
  `(do
     (swap! *instructions*
          assoc ~name
          (fn ~args
            ~@body))
     ~name))

(defmacro definstruction
  "Special macro to define simple operators, i.e those who does not have
to be aware of surrounding contracts and returns the environment untouched"
  [name args & body]
  (let [env   (gensym "env")
        topop (vec (next args))
        count (count topop)]
    `(do (swap! *instructions*
            assoc ~name
            (fn ~(conj [env '_#] (first args))
              (let [[~(first args) ~topop] (stack-pop ~(first args) ~count)]
                [~env
                 (inc-pointer (do ~@body) 1)])))
         ~name)))

(defn instruction-at
  ([state] (instruction-at state 0))
  ([state ofs]
     (get (:code state)
          (+ (:pointer state) ofs))))

(defn get-return-value [state]
  (let [[adr _] (:stack state)]
    (get (:mem state) adr)))

(defn inc-pointer [state by]
  (assoc state :pointer (+ (:pointer state) by)))

(defn stack-push [state value]
  (assoc state :stack (conj (:stack state) value)))

(defn stack-pop [state num]
  (let [popped (take num (:stack state))
        remain (nthnext (:stack state) num)]
    [(assoc state :stack remain) popped]))

;; arithmetic

(definstruction :ADD [state a b]
  (stack-push state (+ a b)))

(definstruction :SUB [state a b]
  (stack-push state (- a b)))

(definstruction :MUL [state a b]
  (stack-push state (* a b)))

(definstruction :DIV [state a b]
  (stack-push state (/ a b)))


(defn bytes-to-int [bytes]
  (if (= (count bytes) 1)
    (first bytes)
    (reduce (fn [acc new]
              (+ (* acc 256) new))
            bytes)))

(doseq [i (range 1 33)] ;; PUSH1 - PUSH32
  (def-special-instruction (keyword (str "PUSH" i)) [env to state]
    [env 
     (-> state
         (inc-pointer (inc i))
         (stack-push (bytes-to-int 
                      (subvec (:code state)
                              (inc (:pointer state))
                              (+ (:pointer state) i 1)))))]))

(def-special-instruction :GAS [env to state]
  (-> state
      (inc-pointer 1)
      (stack-push 100000)))

(definstruction :MSTORE [state addr value]
  (assoc-in state [:mem addr] value))

(definstruction :CALLDATALOAD [state addr]  
  (stack-push state (get (:args state) addr)))

(definstruction :RETURN [state]
  (assoc state :terminated true))

(definstruction :STOP [state]
  (assoc state :terminated true))

(def-special-instruction :CALL [env to state])

(definstruction :CALLER [state]
  (stack-push state :TESTCALLER))

(definstruction :DUP [state value]
  (stack-push state value))

(definstruction :CODECOPY [state addr offset size]
  (assoc-in state [:mem addr] 
            (subvec (:code state) offset (+ offset size))))

(def-special-instruction :SSTORE [env to state]
  (let [[state [addr value]] (stack-pop state 2)]
    [(assoc-in env [:contracts to :storage addr] value)
     (inc-pointer state 1)]))

(def-special-instruction :SLOAD [env to state]
  [env
   (let [[state [addr]] (stack-pop state 1)]
    (-> state
        (inc-pointer 1)
        (stack-push (or (get-in env [:contracts to :storage addr])
                        0))))])

(definstruction :MSTORE [state addr value]
  (assoc-in state [:mem addr] value))

(definstruction :MLOAD [state addr]
  (stack-push state (or (get-in state [:mem addr])
                        0)))
   
(defn step [env to state]
  (let [instruction (get @*instructions* (instruction-at state))]
    (cond (nil? instruction)
          [env (assoc state :terminated true)]

          (nil? instruction)
          (throw (Exception. (str "Unknown instruction " instruction)))

          :else (instruction env to state))))

(defn run [env to state]
  (println "execution state")
  (pp/pprint (-> state
                 (assoc :instruction (instruction-at state))
                 (dissoc :code)))
  (let [[env state] (step env to state)]
    (if (:terminated state)
      [env state]
      (recur env to state))))

(defn init-state [code & args]
  {:args    (vec args)
   :pointer 0
   :code    code
   :mem     {}
   :stack   (list)})

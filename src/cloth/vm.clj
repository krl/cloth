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
                (fn ~(conj [env] (first args))
                  (let [[~(first args) ~topop] (stack-pop ~(first args) ~count)]
                    [~env
                     (inc-pointer (do ~@body) 1)])))
         ~name)))

(defn storage-get [env state addr]
  (or (get-in env [(:context state) :storage addr]) 0))

(defn storage-put [env state addr value]
  (assoc-in env [(:context state) :storage addr] value))

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

(defn step [env state]
  (let [instruction (get @*instructions* (instruction-at state))]
    (cond (nil? instruction)
          [env (assoc state :terminated true)]

          (nil? instruction)
          (throw (Exception. (str "Unknown instruction " instruction)))

          :else (instruction env state))))

(defn run [env state]
  (println "execution state")
  (pp/pprint (-> state
                 (assoc :instruction (instruction-at state))
                 (dissoc :code)))
  (let [[env state] (step env state)]
    (if (:terminated state)
      [env state]
      (recur env state))))

(defn init-state [context code & args]
  {:args    (vec args)
   :code    code
   :pointer 0
   :context context
   :mem     {}
   :stack   (list)})

(defn bytes-to-int [bytes]
  (if (= (count bytes) 1)
    (first bytes)
    (reduce (fn [acc new]
              (+ (* acc 256) new))
            bytes)))

(doseq [i (range 1 33)] ;; PUSH1 - PUSH32
  (def-special-instruction (keyword (str "PUSH" i)) [env state]
    [env 
     (-> state
         (inc-pointer (inc i))
         (stack-push (bytes-to-int 
                      (subvec (:code state)
                              (inc (:pointer state))
                              (+ (:pointer state) i 1)))))]))

;; arithmetic

(definstruction :ADD [state a b]
  (stack-push state (+ a b)))

(definstruction :SUB [state a b]
  (stack-push state (- a b)))

(definstruction :MUL [state a b]
  (stack-push state (* a b)))

(definstruction :DIV [state a b]
  (stack-push state (/ a b)))

;; logic

(definstruction :NOT [state in]
  (if (= in 0) 1 0))

(definstruction :LT [state a b]
  (stack-push state
              (if (< a b) 1 0)))

(definstruction :SLT [state a b]
  (stack-push state
              (if (< a b) 1 0)))

(definstruction :GT [state a b]
  (stack-push state
              (if (> a b) 1 0)))

(definstruction :SGT [state a b]
  (stack-push state
              (if (> a b) 1 0)))

(definstruction :EQ [state a b]
  (stack-push state
              (if (= a b) 1 0)))

(definstruction :NOT [state val]
  (stack-push state
              (if (zero? val) 1 0)))                

;; stuff

(definstruction :GAS [state]
  ;; dummy for now
  (stack-push state 100000));

(definstruction :ADDRESS [state]
  (stack-push state (:context state)))

;; storage

(definstruction :MSTORE [state addr value]
  (assoc-in state [:mem addr] value))

(definstruction :CALLDATALOAD [state addr]  
  (stack-push state (get (:args state) addr)))

(definstruction :RETURN [state]
  (assoc state :terminated true))

(definstruction :STOP [state]
  (assoc state :terminated true))

(def-special-instruction :JUMP [env state]
  (let [[state [addr]] (stack-pop state 1)]    
    [env
     (assoc state :pointer addr)]))

(def-special-instruction :JUMPI [env state]
  (let [[state [addr value]] (stack-pop state 2)]
    [env
     (if-not (zero? value)
       (assoc state :pointer addr)
       (inc-pointer state 1))]))

(def-special-instruction :CALL [env state]
  (println "GOTHERE MFCRS")
  (let [[state [gas addr value in-off in-size out-off out-size]] 
        (stack-pop state 7)
        
        [env callstate]
        (run env 
             (init-state addr
                         (get-in env [addr :code])
                         (get-in state [:mem in-off])))
        return (get-return-value callstate)]
    (println "subcall returned: " return)
    [env
     (-> state
         (assoc-in [:mem out-off] return)
         (inc-pointer 1))]))

(definstruction :CALLER [state]
  (stack-push (:context state)))

(definstruction :DUP [state value]
  (stack-push state value))

(definstruction :CODECOPY [state addr offset size]
  (assoc-in state [:mem addr] 
            (subvec (:code state) offset (+ offset size))))

(def-special-instruction :SSTORE [env state]
  (let [[state [addr value]] (stack-pop state 2)]
    [(storage-put env state addr value)
     (inc-pointer state 1)]))

(def-special-instruction :SLOAD [env state]
  [env
   (let [[state [addr]] (stack-pop state 1)]
    (-> state
        (inc-pointer 1)
        (stack-push (storage-get env state addr))))])

(definstruction :MSTORE [state addr value]
  (assoc-in state [:mem addr] value))

(definstruction :MLOAD [state addr]
  (stack-push state (or (get-in state [:mem addr])
                        0)))



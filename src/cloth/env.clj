(ns cloth.env
  (require [cloth.vm :as vm]
           [cloth.compile :as c]))

(defn create-contract [env code]
  (let [addr           (hash code)
        [env executed] (vm/run env (vm/init-state addr code))
        new-env        (-> env 
                           (assoc-in [addr :code]
                                     (vm/get-return-value executed)))]
    (println "executed")
    (println executed)
    [new-env addr]))

(defn transaction [env to & args]
  (println "Transaction")
  (let [state          (apply vm/init-state                              
                              to
                              (get-in env [to :code])
                              args)
        [env executed] (vm/run env state)]
    [env (vm/get-return-value executed)]))

(defn contract-helper
  "Takes code, or an environment and body code
and returns an environment and the contract address"
  ([code] (contract-helper {} code))
  ([env code]
     (create-contract env
                      (c/compile-lll-string 
                       (str "(return 0 (lll " code " 0))")))))

(defn contract-helper-serpent
  "Takes code, or an environment and body code
and returns an environment and the contract address"
  ([code] (contract-helper-serpent {} code))
  ([env code]
     (create-contract env
                      (c/compile-serpent-string code))))

(defn contract-helper-full
  "Takes code, or an environment and full code
and returns an environment and the contract address"
  ([code] (contract-helper-full {} code))
  ([env code]
     (create-contract env
                      (c/compile-lll-string code))))

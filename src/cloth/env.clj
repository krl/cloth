(ns cloth.env
  (require [cloth.vm :as vm]))

(defn create-contract [env code]
  (let [address        (hash code)
        [env executed] (vm/run env address 
                               (vm/init-state code))        
        new-env        (-> env 
                           (assoc-in [:contracts address :code]
                                     (vm/get-return-value executed)))]
    [new-env address]))

(defn transaction [env to & args]
  (let [state          (apply vm/init-state
                              (get-in env [:contracts to :code])
                              args)
        [env executed] (vm/run env to state)]
    [env (vm/get-return-value executed)]))

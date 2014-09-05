(ns cloth.env-test
  (:require [clojure.test :refer :all]
            [cloth.compile :as c]
            [cloth.vm :as vm]
            [cloth.env :as e]
            [cloth.env :refer :all]))

(testing "contracts calling contracts"
  (let [[env addr]
        (e/contract-helper "(return (+ $0 1))")
          
        [env addr2]
        (e/contract-helper env
                         (str "(return (call 1337 " addr " 0 0 7 0 0))"))

        [env return] 
        (transaction env addr2)]
    return))

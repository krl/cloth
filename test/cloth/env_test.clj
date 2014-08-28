(ns cloth.env-test
  (:require [clojure.test :refer :all]
            [cloth.compile :as c]
            [cloth.vm :as vm]
            [cloth.env :refer :all]))

;; (testing "contracts calling contracts"
;;   (let [[addr env] 
;;         (create-contract
;;          {} (c/compile-lll-string "(return 0 (lll (return (+ $0 1)) 0))"))
          
;;         [addr2 env]
;;         (create-contract {}
;;                          (c/compile-lll-string
;;                           (str "(call (- (gas) 100) " addr " 0 0 7 0 0)")))]
;;     env)

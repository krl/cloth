(ns cloth.env-test
  (:require [clojure.test :refer :all]
            [cloth.compile :as c]
            [cloth.vm :as vm]
            [cloth.env :as e]
            [cloth.env :refer :all]))

;; (testing "contracts calling contracts"
;;   (let [[env addr]
;;         (e/contract-helper "(return (+ $0 1))")
          
;;         [env addr2]
;;         (e/contract-helper env
;;                            (str "(return (call 1337 " addr " 0 0 2 0 0))"))

;;         [env return] 
;;         (transaction env addr2)]
;;     return))

(deftest recursive-factorial
  (let [[env addr] (e/contract-helper-serpent "

if msg.data[0] <= 1
  return(1)
else:
  return(msg.data[0] * call(contract.address, msg.data[0] - 1))

")]
    (testing "recursion"
      (let [[env return0] (transaction env addr 0)
            [env return1] (transaction env addr 1)
            [env return2] (transaction env addr 2)
            [env return3] (transaction env addr 3)
            [env return4] (transaction env addr 4)
            [env return5] (transaction env addr 5)
            [env return6] (transaction env addr 6)
            [env return7] (transaction env addr 7)]
        (is (= return0 1))
        (is (= return1 1))
        (is (= return2 2))
        (is (= return3 6))
        (is (= return4 24))
        (is (= return5 120))
        (is (= return6 720))
        (is (= return7 5040))))))

(comment (run-tests))

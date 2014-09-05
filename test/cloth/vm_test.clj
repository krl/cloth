(ns cloth.vm-test
  (:require [clojure.test :refer :all]
            [cloth.env :as e]
            [cloth.vm :refer :all]))

(deftest test-return-args
  (testing "return values" 
    (let [[env address] (e/contract-helper "(return 42)")
          [env return]  (e/transaction env address)]
      (is (= return 42))))

  (testing "arguments"
    (let [[env address] (e/contract-helper "(return (+ $0 $1))")
          [env return]  (e/transaction env address 100 21)]
      (is (= return 121)))))

(deftest arithmetic
  (testing "addition"
    (let [[env address] (e/contract-helper "(return (+ (+ $0 38) (+ $0 32)))")
          [env return]  (e/transaction env address 4)]
      (is (= return 78))))

  (testing "subtraction"
    (let [[env address] (e/contract-helper "(return (- (- $0 32) (- $0 38)))")
          [env return]  (e/transaction env address 10)]
      (is (= return 6))))

  (testing "multiplication"
    (let [[env address] (e/contract-helper "(return (* (* $0 38) (* $0 32)))")
          [env return]  (e/transaction env address 3)]
      (is (= return 10944))))

  (testing "division"
    (let [[env address] (e/contract-helper "(return (/ (/ $0 10) (/ 10 $0)))")
          [env return]  (e/transaction env address 20)]
      (is (= return 4)))))

(deftest sstore-sload
  (testing "sstore in init code"
    (let [[env address] (e/contract-helper-full 
                         "{ [[1312]] 1997 (return 0 (lll (return @@1312) 0))}")
          [env return]  (e/transaction env address)]
      (is (= return 1997))))
  (testing "sstore in body code"
    (let [[env address] (e/contract-helper
                         "{[[1312]] 9000 
                           (return @@1312)}")
          [env return]  (e/transaction env address)]
      (is (= return 9000))))
  (testing "sload of undefined"
    (let [[env address] (e/contract-helper
                         "(return @@1312)")
          [env return]  (e/transaction env address)]
      (is (= return 0)))))

(deftest mstore-mload
  (testing "mstore"
    (let [[env address] (e/contract-helper
                         "{ [33] 33 (return @33) }")
          [env return]  (e/transaction env address)]
      (is (= return 33))))
  (testing "mload of undefined"
    (let [[env address] (e/contract-helper
                         "(return @33)")
          [env return]  (e/transaction env address)]
      (is (= return 0)))))

;; (deftest lll
;;   (testing "keyvalue-example"
;;     (let [[env address] (e/contract-helper-full
;; " {[[69]] (caller) 
;;    (return 0 (lll
;;      (when (= (caller) @@69)
;;               (for {} (< @i (calldatasize)) [i](+ @i 64)
;;                 [[ (calldataload @i) ]] (calldataload (+ @i 32))))
;;               0))}")]
;;       env)))

(comment (run-tests))

# cloth

Implementation of the Ethereum Virtual Machine in clojure (ethereum.org)

## Usage

Example test case:

```clojure
(testing "sstore in init code"
  (let [[env address] (contract-helper-full 
                       "{ [[1312]] 1997 (return 0 (lll (return @@1312) 0))}")
        [env return]  (e/transaction env address)]
    (is (= return 1997))))
```

## Necessary dependancies

You need to have lllc in your $PATH to make the tests run.

## License

Copyright © 2014 Kristoffer Ström

Distributed under the Eclipse Public License, the same as Clojure.

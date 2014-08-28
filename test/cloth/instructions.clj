(ns cloth.instructions)

(def INSTRUCTIONS
  {
   ;; Stop and Arithmetic Operations
   "00" :STOP
   "01" :ADD
   "02" :MUL
   "03" :SUB
   "04" :DIV
   "05" :SDIV
   "06" :MOD
   "07" :SMOD
   "08" :EXP
   "09" :NEG
   "0a" :LT
   "0b" :GT
   "0c" :SLT
   "0d" :SGT
   "0e" :EQ
   "0f" :NOT
   "10" :AND
   "11" :OR
   "12" :XOR
   "13" :BYTE

   ;; SHA3
   "20" :SHA3

   ;; Environmental Information
   "30" :ADDRESS
   "31" :BALANCE
   "32" :ORIGIN
   "33" :CALLER
   "34" :CALLVALUE
   "35" :CALLDATALOAD
   "36" :CALLDATASIZE
   "37" :CALLDATACOPY
   "38" :CODESIZE
   "39" :CODECOPY
   "3a" :GASPRICE

   ;; Block Information
   "40" :PREVHASH
   "41" :COINBASE
   "42" :TIMESTAMP
   "43" :NUMBER
   "44" :DIFFICULTY
   "45" :GASLIMIT
   
   ;; Stack, Memory, Storage and Flow Operations
   "50" :POP
   "51" :DUP
   "52" :SWAP
   "53" :MLOAD
   "54" :MSTORE
   "55" :MSTORE8
   "56" :SLOAD
   "57" :SSTORE
   "58" :JUMP
   "59" :JUMPI
   "5a" :PC
   "5b" :MSIZE
   "5c" :GAS
   
   ;; Push operations

   "60" :PUSH1
   "61" :PUSH2
   "62" :PUSH3
   "63" :PUSH4
   "64" :PUSH5
   "65" :PUSH6
   "66" :PUSH7
   "67" :PUSH8
   "68" :PUSH9
   "69" :PUSH10
   "6a" :PUSH11
   "6b" :PUSH12
   "6c" :PUSH13
   "6d" :PUSH14
   "6e" :PUSH15
   "6f" :PUSH16
   "70" :PUSH17
   "71" :PUSH18
   "72" :PUSH19
   "73" :PUSH20
   "74" :PUSH21
   "75" :PUSH22
   "76" :PUSH23
   "77" :PUSH24
   "78" :PUSH25
   "79" :PUSH26
   "7a" :PUSH27
   "7b" :PUSH28
   "7c" :PUSH29
   "7d" :PUSH30
   "7e" :PUSH31
   "7f" :PUSH32

   ;; System operations
   "f0" :CREATE
   "f1" :CALL
   "f2" :RETURN
   "ff" :SUICIDE
   })


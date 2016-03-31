
leran [1,2,3,5] to class 1, learn [1,5] to class 0
$ curl -s -XGET -d '{"query":[{"whichClass":1, "words":[1,2,3,5]}, {"whichClass":0, "words":[1,5]}], "classifierId":"test"}' http://localhost:8888/learn | json_xs 
{
   "took" : 2,
   "result" : true
}

query for [1,2] and [3,4]
$ curl -s -XGET -d '{"query":[{"words":[1,2]},{"words":[3,4]},{"words":[1,2,3,4,5]}], "classifierId":"test"}' http://localhost:8888/query | json_xs
{
   "took" : 1,
   "result" : [
      [
         3.99999999984e-11,
         0.99999999996
      ],
      [
         1.99999999996e-11,
         0.99999999998
      ],
      [
         3.2e-21,
         1
      ]
   ]
}


index 0 is class GOOD
index 1 is class BAD

(if no session id is provided, it uses _default_)


learn "hello(1) world(2)" for class 0 for session abcdef
    $ curl 'http://localhost:8888/learn?class=0&words=1&words=2&id=abcdef'
    true

learn "hello(1)" for class 1 for session abcdef
    $ curl 'http://localhost:8888/learn?class=1&words=1&id=abcdef'
    true

get prob scores for "hello(1) world(2)" for session abcdef
    $ curl 'http://localhost:8888/query?words=1&words=2&id=abcdef'
    [0.99999999999,9.9999999999E-12]

index 0 is class GOOD
index 1 is class BAD

(if no session id is provided, it uses _default_)

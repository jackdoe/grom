
learn "hello(1) world(2)" for class 0 for session abcdef
    http://localhost:8888/learn?class=0&words=1&words=2&id=abcdef

learn "hello(1)" for class 1 for session abcdef
    http://localhost:8888/learn?class=1&words=1&id=abcdef

get prob scores for "hello(1) world(2)" for session abcdef
    http://localhost:8888/query?words=1&words=2&id=abcdef

(if no session id is provided, it uses _default_)

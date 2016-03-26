
learn "hello world" for class 0 for session abcdef
    http://localhost:8888/learn?class=0&words=hello&words=world&id=abcdef

learn "hello" for class 1 for session abcdef
    http://localhost:8888/learn?class=1&words=hello&id=abcdef

get prob scores for "hello world" for session abcdef
    http://localhost:8888/query?words=hello&words=world&id=abcdef

(if no session id is provided, it uses _default_)

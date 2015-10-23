@ECHO OFF

start runserver -n 1

timeout 3 > NUL

start compile

timeout 2 > NUL

start runclient

exit
#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

echo "starting subprocess from $DIR"
bash -c ""${DIR}/SubProcess.sh"" &

while true
do
    echo "PING"
    echo "PONG" >&2
    sleep 0.1
done

#!/usr/bin/env bash

template=$1
shift
query=""
# IFS='' (or IFS=) prevents leading/trailing whitespace from being trimmed.
# -r prevents backslash escapes from being interpreted.
# || [[ -n $line ]] prevents the last line from being ignored if it doesn't end with a \n (since read returns a non-zero exit code when it encounters EOF).
while IFS='' read -r line || [ -n "$line" ];
    do
        line="$(echo "$line" | sed 's/\([;(<"*">)]\)/\\\1/g')" # escape [ ; ( < " * " > ) ]
        query+=$(eval echo "$line") # replace variables in template with values
        query+="\n" # append a new line
    done < "$template"
echo "$query"

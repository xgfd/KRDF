#sh 3varQgen.sh movie.rq movie_1.txt movie_2.txt movie_3.txt
declare -a arr1
declare -a arr2

template="$1"
arr1=(`cat $2`)
arr2=(`cat $3`)
arr3=(`cat $4`)

mkdir queries

echo ${#arr1[@]}

for ((i = 0; i < ${#arr1[@]}; i++))
    do
        varA="${arr1[$i]}"
        varB="${arr2[$i]}"
        varC="${arr3[$i]}"
        echo "Processing pair $varA - $varB"
        sh gen.sh "$template" "<$varA>" "<$varB>" "<$varC>" > "./queries/${template%%.*}-$(printf "%03d" $(($i + 1))).rq"
    done

echo Finished!
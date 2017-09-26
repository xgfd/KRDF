# arr1 arr2 template.rq
#sh qGen.sh q1_1.txt q1_2.txt q1.rq
declare -a arr1
declare -a arr2

template="$1"
arr1=(`cat $2`)
arr2=(`cat $3`)

mkdir queries

echo ${#arr1[@]}

for ((i = 0; i < ${#arr1[@]}; i++))
    do
        varA="${arr1[$i]}"
        varB="${arr2[$i]}"
        echo "Processing pair $varA - $varB"
        sh gen.sh "$template" "<$varA>" "<$varB>" > "./queries/${template%%.*}-$(printf "%03d" $(($i + 1))).rq"
    done

echo Finished!

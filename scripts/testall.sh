#! /bin/sh

curl -O https://engineering.purdue.edu/EE468/project/step5/testcases_step5.tar.gz
tar -xf testcases_step5.tar.gz
mv testcases_step5 testcases

for file in `ls testcases/input`
do
    if [ "$1" = "-v" ]; then
        echo "$file run:"
    fi

    FILE=$(echo "$file" | cut -d"." -f1)
    make run FILE=$FILE
    if [ "$1" = "-v" ]; then
        echo "$file check:"
    fi
    make run-tiny FILE=$FILE < testcases/input/$FILE.input
    make check-tiny FILE=$FILE
done

rm testcases_step5.tar.gz
rm -rf testcases
rm *.test
rm *.tinyout

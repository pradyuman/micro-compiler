#! /bin/sh

curl -O https://engineering.purdue.edu/EE468/project/step3/testcases.tar.gz
tar -xf testcases.tar.gz

for file in `ls testcases/input`
do
    if [ "$1" = "-v" ]; then
        echo "$file run:"
    fi
    make run FILE=$(echo "$file" | cut -d"." -f1)
    if [ "$1" = "-v" ]; then
        echo "$file check:"
    fi
    make check FILE=$(echo "$file" | cut -d"." -f1)
done

rm testcases.tar.gz
rm -rf testcases
rm *.scanner

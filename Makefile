LIB_ANTLR := lib/antlr.jar
ANTLR_SCRIPT := Micro.g4
CLASS_PATH := classes/

all: group compiler
group:
	@echo "Pradyuman Vig (pvig)  Tiger Cheng (tigerc)"
compiler:
	rm -rf build
	mkdir build
	java -cp $(LIB_ANTLR) org.antlr.v4.Tool -o build $(ANTLR_SCRIPT)
	rm -rf classes
	mkdir classes
	javac -cp $(LIB_ANTLR) -d classes src/*.java build/*.java
lexer:
	@java -cp "$(LIB_ANTLR):$(CLASS_PATH)" \
	org.antlr.v4.gui.TestRig Micro tokens -tokens
run:
	@java -cp "$(LIB_ANTLR):$(CLASS_PATH)" \
	Micro testcases/input/$(FILE).micro > $(FILE).test
check:
	diff -b -B testcases/output/$(FILE).out $(FILE).test
download:
	curl -O https://engineering.purdue.edu/EE468/project/step$(s)/testcases.tar.gz
	tar -xvzf testcases.tar.gz
run-tiny:
	lib/tiny $(FILE)
test: run check
testall:
	./scripts/testall.sh
clean:
	rm -rf classes build

.PHONY: all group compiler clean

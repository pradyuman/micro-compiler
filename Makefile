LIB_ANTLR := lib/antlr.jar
ANTLR_SCRIPT := MicroLexer.g4
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
test:
	@java -cp "$(LIB_ANTLR):$(CLASS_PATH)" \
	org.antlr.v4.gui.TestRig Micro ${ARGS}
lexer:
	@java -cp "$(LIB_ANTLR):$(CLASS_PATH)" \
	org.antlr.v4.gui.TestRig Micro tokens -tokens
run:
	@java -cp "$(LIB_ANTLR):$(CLASS_PATH)" \
	Micro ${FILE}.micro > ${FILE}.scanner
check:
	diff -b -B ${FILE}.out ${FILE}.scanner
clean:
	rm -rf classes build

.PHONY: all group compiler clean

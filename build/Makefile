############################################################################
#
#	Makefile for GATE2
#
#	Hamish Cunningham, 8/Jan/99
#
############################################################################
#
#	$Id$
#
############################################################################

PKGS=	gate gate.util gate.annotation gate.corpora gate.jape \
	gate.jape.parser gate.db
#	gate.fsm
CP=	-classpath \
../classes:../lib/junit.jar:../lib/jgl3.1.0.jar:../lib/orajdbc2.zip:../lib/jacl.jar:../lib/oro.jar
SP=	-sourcepath ../src
#COMPILE=./jdk.sh -s -g:none javac -Xdepend -Xstdout -O
COMPILE=./jdk.sh -s -g:none javac -O \
-d ../classes $(SP) $(CP)

# "all" builds the .class files. 
# having the main test class as first target, and -Xdepend in the (1.2)
# javac options, means that most classes get compiled by a single call
# to javac
all: ../classes/gate/TestGate.class classes

# build the whole thing
everything: depend all jar docs internaldocs test

# the .class/.java dependencies are all stored Makefile.dep
include Makefile.dep

# regenerate Makefile.dep
depend:
	./makejdepend.sh ../src/gate >Makefile.dep

# run the test suite (swap the last line for one of the commented ones to get
# the JUnit GUI test runner)
test:
	./jdk.sh java -Djava.compiler=none -classpath gate.jar \
	  junit.textui.TestRunner gate.TestGate
#	  junit.ui.TestRunner gate.TestGate
#	  junit.ui.LoadingTestRunner gate.TestGate

# build gate.jar. recursively includes all the .jar and .zip from lib too
jar: tcl
	rm -f gate.jar
	cd ../classes && ../build/jdk.sh jar cvf ../build/gate.jar gate
	./addjars.sh gate.jar ../lib/*.jar ../lib/*.zip

# refresh the jar with all the class files
jarupdate:
	cd ../classes && ../build/jdk.sh jar uvf ../build/gate.jar gate

# build the Tcl script library:
# get gate.util.Jacl to update itself; rebuild it; copy all the Tcl files
tcl:
	./jdk.sh java $(CP) gate.util.Jacl
	make ../classes/gate/util/Jacl.class
	cd ../src && for f in `find . -name '*.tcl' -print`; \
	do cp $$f ../classes/$$f; done

# run the Jacl Tcl shell
jaclsh:
	./jdk.sh java -Djava.compiler=none $(CP) tcl.lang.Shell

# make the javadoc documentation
docs:
	./jdk.sh \
	  -s '-link http://java.sun.com/products/jdk/1.3/docs/api/' \
	  javadoc $(SP) $(CP) -public \
	  -d ../doc/javadoc $(PKGS)
	cp ../src/gate/CookBook.java ../doc/javadoc/gate/CookBook.txt
	cp ../src/gate/CookBook.java ../doc/javadoc/internal/gate/CookBook.txt

# make the developer Javadocs (inc. private members)
internaldocs:
	./jdk.sh \
	  -s '-link http://bradfield.dcs.shef.ac.uk/Java2_docs/api/' \
	  javadoc $(SP) $(CP) -private -use \
	  -d ../doc/javadoc/internal $(PKGS)

# delete all the class files etc. (doesn't delete gate.jar)
clean:
	rm -rf `find ../classes/* -print |grep -v CVS` \
	  `find .. -name '*.~*' -print`

# make a pretty list of the gate source, missing class files etc.
list:
	@ ./list.sh


############################################################################

############################################################################
#
#	Makefile for METER
#
#	___AUTHOR___, ___DATE___
#
############################################################################
#
#	$Id$
#
############################################################################

PKGS=	template
CP=	-classpath \
../classes:../../Template/classes:../../Template/build/gate.jar:../classes:../lib/junit.jar:../classes:../lib/gate.jar
SP=	-sourcepath ../src

#COMPILE=./jdk.sh -s -g:none javac -Xdepend -Xstdout -O
COMPILE=./jdk.sh -s -g:none javac -O -d ../classes $(SP) $(CP)

# "all" builds the .class files. 
# having the main test class as first target, and -Xdepend in the (1.2)
# javac options, means that most classes get compiled by a single call
# to javac
all: ../classes/template/TestTemplate.class classes resources

# build the whole thing
everything: depend all jar docs internaldocs test

# the .class/.java dependencies are all stored Makefile.dep
include Makefile.dep

# regenerate Makefile.dep
depend:
	./makejdepend.sh ../src/template >Makefile.dep

# run the test suite (swap the last line for one of the commented ones to get
# the JUnit GUI test runner)
test:
	./jdk.sh java -Djava.compiler=none \
	  -classpath template.jar:../lib/gate.jar \
	  junit.textui.TestRunner template.TestTemplate

# build template.jar
jar: resources classes.jar
	rm -f template.jar
	./jdk.sh java -cp ../../Template/lib/gate.jar gate.util.JarFiles \
	  template.jar classes.jar ../../Template/lib/gate.jar

# just the classes in a jar
classes.jar:
	cd ../classes && ../build/jdk.sh jar cvf ../build/classes.jar template

# copy the resources directory into "classes"
resources:
	cd ../src/template && cp -R resources ../../classes/template

# refresh the jar with all the class files
jarupdate:
	cd ../classes && ../build/jdk.sh jar uvf ../build/template.jar template

# run the gui
rungui:
	./jdk.sh java -cp template.jar -Xmx200m template.TestTemplate
runguisrc:
	./jdk.sh java \
	  -cp ../classes:../../gate2/classes:../../gate2/build/gate.jar \
	  -Xmx200m template.TestTemplate

# make the javadoc documentation
docs:
	./jdk.sh \
	  -s '-link http://java.sun.com/products/jdk/1.3/docs/api/' \
	  javadoc $(SP) $(CP) -public \
	  -d ../doc/javadoc $(PKGS)

# make the developer Javadocs (inc. private members)
internaldocs:
	./jdk.sh \
	  -s '-link http://redmires.dcs.shef.ac.uk/Java2_docs/api/' \
	  javadoc $(SP) $(CP) -private -use \
	  -d ../doc/javadoc/internal $(PKGS)

# delete all the class files etc. (doesn't delete template.jar)
clean:
	rm -f classes.jar
	rm -rf `find ../classes/* -print |grep -v CVS` \
	  `find .. -name '*.~*' -print`

# make a pretty list of the Template source, missing class files etc.
list:
	@ ./list.sh

# make a delivery .zip with the jar and docs
delivery: template.jar
	DELNAME="Template-`cat version.txt`"; \
	rm -rf $$DELNAME; \
	mkdir $$DELNAME; \
	cp template.jar template.sh template.bat $$DELNAME; \
	cp -R ../src $$DELNAME; \
	cp `cat version.txt`-readme.html $$DELNAME/README.html; \
	cp `cat version.txt`-readme.html $$DELNAME/index.html; \
	cd ../doc/wp1; \
	cp techdoc.html techprog.html finprog.html ../../build/$$DELNAME; \
	cp techdoc.ps techprog.ps finprog.ps ../../build/$$DELNAME; \
	cd ..; cp -R javadoc ../build/$$DELNAME; cd ../build; \
	./jdk.sh jar cvf $$DELNAME.zip $$DELNAME

# make a list of packages
packages:
	cd ../src; \
	for f in `find Template -print`; do \
	  [ -d $$f ] || continue; \
	  case $$f in \
	    *CVS) continue;; \
	    template/resources*) continue;; \
	  esac; \
	  echo $$f |sed 's,/,.,g'; \
	done

############################################################################

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

PKGS=	gate gate.util gate.annotation gate.corpora
CP=	-classpath \
../classes:../lib/junit.jar
SP=	-sourcepath ../src
COMPILE=./jdk.sh -s -g:none javac -Xdepend -Xstdout -O \
-d ../classes $(SP) $(CP)

# having the main test class as first target, and -Xdepend in the
# javac options, means that most classes get compiled by a single call
# to javac
all: ../classes/gate/TestGate.class classes

include Makefile.dep

depend:
	./makejdepend.sh ../src/gate >Makefile.dep

test:
	./jdk.sh java -Djava.compiler=none -classpath gate.jar \
	  junit.textui.TestRunner gate.TestGate

jar:
	rm -f gate.jar
	cd ../classes && ../build/jdk.sh jar cvf ../build/gate.jar gate
	./addjars.sh gate.jar ../lib/*.jar ../lib/*.zip

docs:
	./jdk.sh \
	  -s '-link http://bradfield.dcs.shef.ac.uk/Java2_docs/api/' \
	  javadoc $(SP) $(CP) -private -use \
	  -d ../doc/javadoc/internal $(PKGS)
	./jdk.sh javadoc $(SP) $(CP) -public \
	  -d ../doc/javadoc $(PKGS)
	cp ../src/gate/CookBook.java ../doc/javadoc/gate/CookBook.txt
	cp ../src/gate/CookBook.java ../doc/javadoc/internal/gate/CookBook.txt

clean:
	rm -rf ../classes/gate `find .. -name '*.~*' -print`

list:
	@ ./list.sh


############################################################################
#
# $Log$
# Revision 1.18  2000/02/21 15:32:58  hamish
# gate.jar now includes lib/*.jar/zip; make tests uses gate.jar
#
# Revision 1.17  2000/02/18 16:04:00  hamish
# made cookbook source a .txt
#
# Revision 1.16  2000/02/18 15:56:36  hamish
# added copy of CookBook source code to docs
#
# Revision 1.15  2000/02/18 15:33:04  hamish
# added classes; added -use to internal javadoc
#
# Revision 1.14  2000/02/16 17:26:12  hamish
# found a better way to do the -link for docs
#
# Revision 1.13  2000/02/16 17:11:22  hamish
# added some more foul mess to jdk.sh to allow Makefile
# to link to JDK docs when doing the internal javadoc run
#
# Revision 1.12  2000/02/16 13:43:38  hamish
# added jar target
#
# Revision 1.11  2000/02/15 13:52:46  hamish
# added -Xdepend and TestGate.class target
#
# Revision 1.10  2000/02/11 14:59:37  hamish
# added corpora pkg
#
# Revision 1.9  2000/02/10 16:01:42  hamish
# removed impl pkg
#
# Revision 1.8  2000/02/09 15:55:44  hamish
# no jit on test run
#
# Revision 1.7  2000/02/07 16:48:12  hamish
# added annotation pkg
#
# Revision 1.6  2000/01/28 17:08:18  hamish
# added impl/ag package
#
# Revision 1.5  2000/01/24 14:40:59  hamish
# made test use textual UI, so can use in cron job
#
# Revision 1.4  2000/01/21 17:08:17  hamish
# changed test runner to autoloading version
#
# Revision 1.3  2000/01/21 17:01:28  hamish
# added tests target and junit.jar
#
# Revision 1.2  2000/01/19 16:19:31  hamish
# added
#
# Revision 1.1  2000/01/19 16:00:26  hamish
# added
#
############################################################################

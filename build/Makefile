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

PKGS=	gate gate.util
CP=	-classpath \
../classes:../lib/junit.jar
SP=	-sourcepath ../src
COMPILE=./jdk.sh -s -g:none javac -Xstdout -O -d ../classes $(SP) $(CP)

all: classes

include Makefile.dep

depend:
	./makejdepend.sh ../src/gate >Makefile.dep

test:
	./jdk.sh java ${CP} junit.ui.LoadingTestRunner gate.TestGate
docs:
	./jdk.sh javadoc $(SP) $(CP) -public \
	  -d ../doc/javadoc $(PKGS)
	./jdk.sh javadoc $(SP) $(CP) -private \
	  -d ../doc/javadoc/internal $(PKGS)

clean:
	rm -rf ../classes/gate `find .. -name '*.~*' -print`

list:
	@ ./list.sh


############################################################################
#
# $Log$
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

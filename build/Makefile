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

PKGS=	gate gate.util gate.impl.ag gate.annotation
CP=	-classpath \
../classes:../lib/junit.jar
SP=	-sourcepath ../src
COMPILE=./jdk.sh -s -g:none javac -Xstdout -O -d ../classes $(SP) $(CP)

all: classes

include Makefile.dep

depend:
	./makejdepend.sh ../src/gate >Makefile.dep

test:
	./jdk.sh java -Djava.compiler=none ${CP} junit.textui.TestRunner gate.TestGate
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

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

PKGS=	gate 
CP=	-classpath \
../classes:
SP=	-sourcepath ../src
COMPILE=./jdk.sh -s -g:none javac -Xstdout -O -d ../classes $(SP) $(CP)

all: classes

include Makefile.dep

depend:
	./makejdepend.sh ../src/gate >Makefile.dep

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
# Revision 1.1  2000/01/19 16:00:26  hamish
# added
#
############################################################################

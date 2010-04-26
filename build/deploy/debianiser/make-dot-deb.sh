#!/usr/bin/env bash
#
# make-dot-deb.sh -- hamish -- apr-22-2010

# prevent cd-related errors
unset CDPATH

# some constants
VERSION=`cat ../../version.txt`-`cat ../../build.txt`-0ubuntu1
DEBFILE=gate-developer-${VERSION}_all.deb
DEBELS=deb-elements
SUMS=control/md5sums
STARTSCRIPT=data/usr/bin/gate-developer
CHANGELOG=usr/share/doc/gate-developer/changelog.Debian

# dump the previous one if it exists
[ -f ${DEBFILE} ] && rm ${DEBFILE}

# the startup script
sed 's,GATE_HOME=`dirname.*$,GATE_HOME=/usr/share/gate-developer,' \
  ../../../bin/gate.sh >${STARTSCRIPT}
chmod 755 ${STARTSCRIPT}

# the changelog
cd data
[ -f ${CHANGELOG} ] && gzip ${CHANGELOG}

# the control file
cd ../control
sed 's,^Version:.*$,Version: '${VERSION}',' control >$$
mv $$ control

# the checksums
>../${SUMS}
for f in `find . -type f |egrep -v '\.svn|\..*.swp' |sed 's,^\./,,'`
do
  md5sum $f >> ../${SUMS}
done
cd ..

# the control and data .tgzs and the .deb
cd control && fakeroot tar czf ../${DEBELS}/control.tar.gz *
cd ../data && fakeroot tar czf ../${DEBELS}/data.tar.gz *
cd ../${DEBELS} && ar -cr ../${DEBFILE} debian-binary control.tar.gz data.tar.gz
cd ..

# all done
echo; echo we seem to have a new .deb in ${DEBFILE}; echo

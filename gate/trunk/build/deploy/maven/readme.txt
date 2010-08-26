25 August 2010
Pushing GATE to Maven Central

This directory contains the tools needed to push GATE releases to central.

Since sourceforge.net does not act as a Maven forge, these tools are set up to use the oss.sonatype.org staging repository.

To push a JAR file to Maven central via this path requires a procedure documented at https://docs.sonatype.org/display/Repository/Sonatype+OSS+Maven+Repository+Usage+Guide.

There are a number of requirements; the most complex is the requirement to have GPG signatures.

The present author thinks that the simplest way to make this work is to run Maven, itself, and use the 
maven-gpg-plugin to sign the pieces and push them to Sonatype. Once that is done, a human can log
into oss.sonatype.org and go through the promotion process described on the page referenced above.

The POM file sign-and-deploy.pom.xml contains the Maven configuration for this process. However,
this cannot be 'burned into' a fixed configuration usable by anyone.

To run this process, you must have:

1. an account, as described on the page above, at Sonatype. That account has to be associated with the GATE groupId via a ticket 
on their JIRA system.

2. a strong GPG key registered in the public repository.

Once you have those things, you can set up the file ~/.m2/settings.xml. Here is mine, with the password redacted.

<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                      http://maven.apache.org/xsd/settings-1.0.0.xsd">

  <profiles>

    <profile>
      <id>gate_release</id>
      <properties>
	<gate.gpg.keyname>bimargulies@apache.org</gate.gpg.keyname>
        <gate.gpg.passphrase>-redacted-</gate.gpg.passphrase>
      </properties>
    </profile>
 </profiles>

  <servers>
    <server>
      <id>gate-oss</id>
      <username>bimargulies</username>
      <!-- password at sonatype -->
      <password>-redacted-</password>
    </server>
 </servers>
</settings>

Given all of this, and a copy of maven version 2.2.1 or newer, you can run:

    mvn -f sign-and-deploy.pom.xml -Pgate_release,WHATEVER

WHATEVER gets replaced by a profile name that specifies what you are trying to do. As of this writing, the only thing filled in
is publish-gate-asm, which pushes version 3.1 of gate-asm.

(Note that there is a requirement to provide javadoc and sources. There is a build.xml in here that creates those from the rest 
of the gate build tree.)




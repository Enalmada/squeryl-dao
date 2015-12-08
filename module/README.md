# Deploy
To get this running on your local machine: `activator publishLocal`

## Publishing Your Artifact

The general steps for publishing your artifact to the Central Repository are as follows: 

 * `publishSigned` to deploy your artifact to staging repository at Sonatype.
 * `sonatypeRelease` do `sonatypeClose` and `sonatypePromote` in one step.
   * `sonatypeClose` closes your staging repository at Sonatype. This step verifies Maven central sync requirement, GPG-signature, javadoc
   and source code presence, pom.xml settings, etc.
   * `sonatypePromote` command verifies the closed repository so that it can be synchronized with Maven central.

# Initial Keystuff (so I remember when I come back to this)
Follow these directions to create your key...
    http://www.scala-sbt.org/sbt-pgp/usage.html

upload public key
    pgp-cmd send-key <email> hkp://pgp.mit.edu
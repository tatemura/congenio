Congenio: Configuration Generation Language
========================================================

** This is a preview version (0.0.1) ** The current implementation
is just enough for our specific purposes. We will add some more missing
pieces (with potentially incompatible changes) as well as documentation.
We target Version 0.1.0 for public use.

The Congenio Configuration Generation Language is a tool
to maintain and generate configuration values (primarily) for experiments.

 * inheritance (@extends attribute): combining and modifying component
 documents.
 * document unfolding (foreach element): generating multiple configuration
 documents (each of which is used for one experiment setting).
 * reference resolution (@ref attribute)
 * value expression (@exp attribute)

Motivation
-----
Configuration management utility, such as Puppet, enables us
to deploy and configure a complex system (that consists of multiple
components such as DBMS and application servers) automatically. Once
we describe configuration, we can repeat the same deployment many times.
But what if we need to repeat with many different combination of configurations?

In our case, we needed to conduct experiments on the performance of
complex systems (performance evaluation, tuning, capacity planning,...), which
involve a large number of configuration options to explore. Writing scripts
was one approach, which tend to bury important parameters into the script code,
quickly making experiments no longer manageable and repeatable.

The configuration generation language was developed to bring manageability
and reproducibility to such experiments with complex systems. [TBC...]

Usage
-----
TODO

### Embedded (use as a library)
We have developed the language as a library that is embedded to
our experiment platform (which runs various workloads on the system to be tested).

The library provides Java API to consume the generated configuration parameters.

### Command-line (use in a script)

Before getting ready to public use, a preview version will include command line
tools to generate configurations in XML and JSON forms.

### Executor (run as a script)

We plan to add additional language features to describe a simple executor
that invokes external scripts (e.g. Puppet) with generated parameters.

Language Features
-----------------
TODO: Wiki will explain more on language features.

### Inheritance (@extends)
Suppose you have a file jpa-mysql.xml:

	<Database>
	  <Name>test</Name>
	  <Params>
	    <property name="javax.persistence.jdbc.driver">
	       com.mysql.jdbc.Driver
	    </property>
	    <property name="javax.persistence.jdbc.url">
	       jdbc:mysql://localhost:3306
	    </property>
	  </Params>
	</Database>
The following XML will extend the jpa-mysql.xml with a new
name:

	<Database extends="jpa-mysql">
	    <Name>mydb</Name>
	</Database>
and resolved as

	<Database>
	  <Name>mydb</Name>
	  <Params>
	    <property name="javax.persistence.jdbc.driver">
	       com.mysql.jdbc.Driver
	    </property>
	    <property name="javax.persistence.jdbc.url">
	       jdbc:mysql://localhost:3306
	    </property>
	  </Params>
	</Database>

You can extend values that are not direct children:

	<Database extends="jpa-mysql">
	    <Name>mydb</Name>
	    <Params extends=".">
	       <property name="javax.persistence.jdbc.url">
	         jdbc:mysql://myserver.example.com:3306
	       </property>
	    </Params>
	</Database>
### Reference (@ref)

	<Experiment>
	   <dbUrl>jdbc:mysql://myserver.example.com:3306</dbUrl>
	   <Database extends="jpa-mysql">
	    <Name>mydb</Name>
	    <Params extends=".">
	       <property name="javax.persistence.jdbc.url"
	          ref="dbUrl"/>
	    </Params>
      </Database>
      <Workload extends="my-workload">
        <Database ref="Database"/>
      </Workload>
      <Startup extends="start-config">
        <db ref="dbUrl"/>
      </Startup>
    </Experiment>

### Value expression (@exp)

	<property name="javax.persistence.jdbc.url"
	          exp="concat('')">
	   <h>jdbc:mysql://</h>
	   <h ref="host"/>
	   <h>:3306</h>
	</property>

Requirements
------------

Java >= 1.6

License
-------
Code licensed under the Apache License Version 2.0.

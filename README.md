Congenio: Configuration Generation Language
========================================================


The Congenio Configuration Generation Language is a tool
to maintain and generate configuration values (primarily) for experiments.

 * inheritance (@extends attribute): combining and modifying component
 documents.
 * document unfolding (foreach element): generating multiple configuration
 documents (each of which is used for one experiment setting).
 * reference resolution (@ref attribute)
 * value expression (@exp attribute)

See [User's guide](https://github.com/tatemura/congenio/wiki/UserGuide) for detils.

** NOTE: this is a preview version (0.0.X) ** The current implementation
is just enough for our specific purposes. We will add some more missing
pieces (with potentially incompatible changes) as well as documentation.
We target Version 0.1.0 for public use.

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
and reproducibility to such experiments with complex systems.


Usage
-----
It can be used either a stand alone command or a library embedded to
other programs.

### Command-line (use in a script)

A command line tool "congen" converts a config document into a
set of resolved documents (in either XML or JSON format). A script can use
this tool to generate and use resolved documents as input of experiments.

### Embedded (use as a library)
The library provides Java API to consume the generated configuration parameters.

In fact, the Congenio language is originally developed as a library within
our experiment platform [Strudel](https://github.com/tatemura/strudel)
(which runs various workloads on the system to be tested).


### Executor (run as a script)

[TODO] We plan to add additional language features to describe a simple executor
that invokes external scripts (e.g. Puppet) with generated parameters.

Language Features
-----------------
With @extends attribute, an experiment document can refer to existing templates
(that define various components such as benchmarks and database configuration
parameters) and customize the default values of these templates. With foreach
elements, an experiment document can generate a set of documents, each of
which corresponds to one execution run with a specific set of parameters.

See [wiki](https://github.com/tatemura/congenio/wiki/LanguageFeatures) for details.


### Inheritance (@extends)
Suppose you have a file jpa-mysql.xml:

	<database>
	  <name>test</name>
	  <params>
	    <property name="javax.persistence.jdbc.driver">
	       com.mysql.jdbc.Driver
	    </property>
	    <property name="javax.persistence.jdbc.url">
	       jdbc:mysql://localhost:3306
	    </property>
	  </params>
	</database>
Another document can refer to this document with an "extend"
attribute to generate a modified version of the original document.
For example, the following XML will extend the jpa-mysql.xml with a new
name:

	<database extends="jpa-mysql">
	    <name>mydb</name>
	</database>
After resolution of inheritance the document will become as follows:

	<database>
	  <name>mydb</name>
	  <params>
	    <property name="javax.persistence.jdbc.driver">
	       com.mysql.jdbc.Driver
	    </property>
	    <property name="javax.persistence.jdbc.url">
	       jdbc:mysql://localhost:3306
	    </property>
	  </params>
	</database>

A special extend reference "." can be used to
extend values that are not direct children:

	<database extends="jpa-mysql">
	    <name>mydb</name>
	    <params extends=".">
	       <property name="javax.persistence.jdbc.url">
	         jdbc:mysql://myserver.example.com:3306
	       </property>
	    </params>
	</database>
### Reference (@ref)
A reference ("ref" attribute) is provided to refer to a value
(subtree) within the same document. It is useful to describe
configuration that comprise of multiple components that share
the same values.

The following example is a configuration of an experiment that
uses three components (database, workload, startup). Configuration
of these components require the URL of the same database. The value
of URL is described at one place (dbUrl) and these components
use "ref" attributes that refer to it.

	<experiment>
	   <dbUrl>jdbc:mysql://localhost:3306</dbUrl>
	   <database extends="jpa-mysql">
	    <name>mydb</name>
	    <params extends=".">
	       <property name="javax.persistence.jdbc.url"
	          ref="dbUrl"/>
	    </params>
      </database>
      <workload extends="my-workload">
        <database ref="Database"/>
      </workload>
      <startup extends="start-config">
        <db ref="dbUrl"/>
      </startup>
    </experiment>

Now, in order to run a modified experiment with a new
database URL, a document can extend only one element:

    <experiment extends="experiment-templ">
       <dbUrl>jdbc:mysql://myserver.example.com:3306</dbUrl>
    </experiment>

### Value expression (@exp)
A reference let components share the exactly same values. But sometimes,
a components need a slightly different value. For example, we just want
to share the host name of the server used by multiple components.

    <experiment>
        <host>localhost</host>
        ...
    </experiment>

But each component may need a special string derived from a host
name. For example, by using a value expression (concat), an URL of
JDBC can be generated from a reference:

	<property name="javax.persistence.jdbc.url"
	          exp="concat('')">
	   <h>jdbc:mysql://</h>
	   <h ref="host"/>
	   <h>:3306</h>
	</property>

### Document unfolding (foreach)
A foreach element is used
to create a series of experiments with slightly different
parameters from one document. 

    <benchmarkParam>
      <foreach name="scale">
        <s><clients>10</clients><users>100000</users></s>
        <s><clients>20</clients><users>200000</users></s>
        <s><clients>40</clients><users>400000</users></s>
        <s><clients>80</clients><users>800000</users></s>
      </foreach>
      <foreach name="servers" sep=",">5,10</foreach>
    <benchmarkParam>

The above document is unfoleded into 8 documents (4 x 2), the one
of which is as follows:

    <benchmarkParam>
      <scale><clients>10</clients><users>100000</users></scale>
      <servers>5</servers>
    </benchmarkParam>

Requirements
------------

Java >= 1.6

License
-------
Code licensed under the Apache License Version 2.0.

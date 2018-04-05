
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.pitest/pitest/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/org.pitest/pitest)
[![Build Status](https://travis-ci.org/hcoles/pitest.png?branch=master)](https://travis-ci.org/hcoles/pitest)
# CS6367 - Pitest Improvement Project

## Project Summary

This project aims at adding more mutations to Pitest and use it to help fixing errors. The first phase involves implementing the AOD, ROR and AOR mutations. The second phase will implement even more mutations and add code fixing rules. (treated as mutators) 

## How to run

 1. Please clone our project and compile it in maven: `mvn install -DskipTests` (doing it without -Dskiptests is fine as well.
 2. Location of the compiled jar files: `C:\Users\\[User Account Name\]\.m2\repository\org\pitest\pitest\`
 3. As of this implementation, the pitest version is 1.4.0-SNAPSHOT.
 4. Then add the following section to pom.xml and test using this command:
 
    mvn org.pitest:pitest-maven:mutationCoverage
    

    	<build>
	    	<plugins>
		    	<plugin>
					<groupId>org.pitest</groupId>
					<artifactId>pitest-maven</artifactId>
					<version>1.4.0-SNAPSHOT</version>
					<configuration>
						<mutators>
							<mutator>DEFAULTS</mutator>
							<mutator>AOD_FIRST</mutator>
							<mutator>AOD_LAST</mutator>
							<mutator>ARITHMETIC_OPERATOR_REPLACEMENT_MUTATOR</mutator>
						</mutators>
				</configuration>
			</plugin>
		</plugins>
	</build>


## Members
 - Joseph LaFreniere 
 - Leeja James 
 - Keith Nguyen

## Progress
**First Phase**
 Problem encountered: we have to use different bytecode operation for single-word operator/operands (int, float) compared to double-word operator/operands (long, double).
 
*AOD mutator*
 - Replaces two operands and the operator with each of the operands, meaning there are two mutators for each operation.
 - Example: a + b is replaced with mutant a and mutant b. 


*ROR mutator*
 - Replaces each relational operators with each of the other ones.
 - Some ROR mutators have already been implemented in the Conditional Boundary Mutator and Negate Conditional Mutator.
 - Example: < is replaced with >=, <=, !=, ==.

 *AOR mutator*
 - Replaces each arithmetic operator with each of the other ones.
 - Some AOR mutators have been implemented in Math Mutator. 
 - Example: + is replaced with -, *, /, %.


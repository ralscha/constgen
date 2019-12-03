## constgen

![Test Status](https://github.com/ralscha/constgen/workflows/test/badge.svg)

## Overview

*constgen* is a Java Annotation Processor that scans for classes annotated with ```org.springframework.data.mongodb.core.mapping.Document``` or
```org.mongodb.morphia.annotations.Entity```. For every annotated class *constgen* creates an additional Java class with the name **C***classname*. This new class contains class variables for every field from the origin class.


## Example
*Source class*
```
@Document
public class User {
	private String id;
	private String email;
	private String passwordResetToken;
	private Date passwordResetTokenValidUntil;
	private boolean deleted;
}
```

*Generated class*
```
public final class CUser {
  public static final String deleted = "deleted";  
  public static final String email = "email";
  public static final String id = "id";   
  public static final String passwordResetToken = "passwordResetToken";
  public static final String passwordResetTokenValidUntil = "passwordResetTokenValidUntil";   
}
```

## Use Case

When you write queries with Morphia or Spring Data MongoDB you need to specify the fields with String parameters.
If you have a typo in a field name MongoDB will not complain because it is schemaless. 
The C classes help you to make this process a little less error prone. In an IDE you will have code completion and it is more refactor friendly. When somebody removes or renames a field you will immediately see compiler errors. 

*With String*
```
	User user = mongoTemplate.findAndModify(
			Query.query(Criteria.where("email").is("test@test.com")
			                    .and("deleted").is(false)),
			Update.update("passwordResetTokenValidUntil", new Date())
			      .set("passwordResetToken", "test_token"),
			FindAndModifyOptions.options().returnNew(true), User.class);
```		

*With C class*		
```		
	User user = this.mongoTemplate.findAndModify(
			Query.query(Criteria.where(CUser.email).is("test@test.com")
			                    .and(CUser.deleted).is(false)),
			Update.update(CUser.passwordResetTokenValidUntil, new Date())
				  .set(CUser.passwordResetToken, token),
			FindAndModifyOptions.options().returnNew(true), User.class);
```


## Features

*constgen* ...
  * requires Java 8 to run.
  * scans for classes annotated with ```org.springframework.data.mongodb.core.mapping.Document``` or ```org.mongodb.morphia.annotations.Entity```.
  * ignores ```transient``` fields.
  * ignores ```static``` fields.
  * ignores fields annotated with ```org.springframework.data.annotation.Transient``` or ```org.mongodb.morphia.annotations.Transient```.
  * takes into account the annotations ```org.springframework.data.mongodb.core.mapping.Field``` and ```org.mongodb.morphia.annotations.Property``` and uses the value of the annotation as value for the String constant. 


## Maven

To activate the annotation processor you add the library as a dependency to your pom.xml. 
constgen does not need to present at runtime so the dependency can be marked as optional 
and will not be included in an jar or war. 
```
	<dependency>
		<groupId>ch.rasc</groupId>
		<artifactId>constgen</artifactId>
		<version>1.0.3</version>
		<optional>true</optional>
	</dependency>
```
Instead of ```<optional>true</optional>``` the provided scope ```<scope>provided</scope>``` can be specified. 
It has the same effect and marks the library as a compile-time only dependency. 

The [immutable](http://immutables.github.io) project has a good description 
on how to [use annotation processors in Eclipse and IntelliJ](http://immutables.github.io/apt.html).

The Spring Boot Maven plugin includes dependencies that are optional or scope provided in the final jar. 
To exclude constgen you need to add an exclude configuration.
```
	<plugin>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-maven-plugin</artifactId>
		<configuration>
			<excludes>
				<exclude>
					<groupId>ch.rasc</groupId>
					<artifactId>constgen</artifactId>
				</exclude>
			</excludes>
		</configuration>
	</plugin>
```

## Changelog

### 1.0.3 - January 30, 2016
  * Resolves issue [#1](https://github.com/ralscha/constgen/issues/1)

### 1.0.2 - November 22, 2015
  * Do not ignore transient fields. Spring Data Mongo stores these fields.
  * Support annotations (Field and Transient) from [bsoncodec-apt](https://github.com/ralscha/bsoncodec-apt)

### 1.0.1 - October 29, 2015
  * Ignore static fields
  * Generate code the correct way with JavaFileObject

### 1.0.0 - September 26, 2015
  * Initial release


## License
Code released under [the Apache license](http://www.apache.org/licenses/).


## Links
  * [Spring Data MongoDB](http://projects.spring.io/spring-data-mongodb/)
  * [Morphia](https://github.com/mongodb/morphia)
  * [bsoncodec-apt](https://github.com/ralscha/bsoncodec-apt)
  * [Mongo Java Driver](https://github.com/mongodb/mongo-java-driver)
  * [MongoDB](https://www.mongodb.org/)



##constgen

[![Build Status](https://api.travis-ci.org/ralscha/constgen.png)](https://travis-ci.org/ralscha/constgen)

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
  * ignores fields annotated with ```org.springframework.data.annotation.Transient``` or ```org.mongodb.morphia.annotations.Transient```.
  * takes into account the annotations ```org.springframework.data.mongodb.core.mapping.Field``` and ```org.mongodb.morphia.annotations.Property``` and uses the value of the annotation as value for the String constant. 


## Maven

One way to call *constgen* from a maven build is by using the apt-maven-plugin from [Mysema](http://www.mysema.com/).
This configuration will write the C classes to the directory ```target/generated-sources/java```

```
			<plugin>
				<groupId>com.mysema.maven</groupId>
				<artifactId>apt-maven-plugin</artifactId>
				<version>1.1.3</version>
				<executions>
				    <execution>
						<id>constantgen</id>
						<goals>
							<goal>process</goal>
						</goals>
						<configuration>
							<processor>ch.rasc.constgen.ConstAnnotationProcessor</processor>
							<outputDirectory>${project.basedir}/target/generated-sources/java</outputDirectory>
						</configuration>				    
				    </execution>					
				</executions>
				<dependencies>
					<dependency>
						<groupId>ch.rasc</groupId>
						<artifactId>constgen</artifactId>
						<version>1.0.0</version>
					</dependency>				
				</dependencies>
			</plugin>
```


## Changelog

### 1.0.0 - September 26, 2015
  * Initial release

## License

Code released under [the Apache license](http://www.apache.org/licenses/).

## Links
  * [Spring Data MongoDB](http://projects.spring.io/spring-data-mongodb/)
  * [Morphia](https://github.com/mongodb/morphia)
  * [Mongo Java Driver](https://github.com/mongodb/mongo-java-driver)
  * [MongoDB](https://www.mongodb.org/)



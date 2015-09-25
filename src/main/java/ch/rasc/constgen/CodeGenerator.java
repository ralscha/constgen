/**
 * Copyright 2015-2015 Ralph Schaer <ralphschaer@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ch.rasc.constgen;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.lang.model.element.Modifier;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeSpec.Builder;

public class CodeGenerator {

	private final Class<?> clazz;

	private String packageName;

	private String className;

	public CodeGenerator(Class<?> clazz) {
		this.clazz = clazz;
		this.packageName = clazz.getPackage().getName();
		this.className = "C" + clazz.getSimpleName();
	}

	public CodeGenerator(String qualifiedName) throws ClassNotFoundException {
		this(Class.forName(qualifiedName));
	}

	public String getPackageName() {
		return this.packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getClassName() {
		return this.className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public void generate(Appendable appendable) throws IOException {
		Builder classBuilder = TypeSpec.classBuilder(this.className);
		classBuilder.addModifiers(Modifier.PUBLIC, Modifier.FINAL);

		for (Field field : collectFields()) {
			FieldSpec fieldSpec = FieldSpec.builder(String.class, field.getName())
					.addModifiers(Modifier.PUBLIC, Modifier.FINAL, Modifier.STATIC)
					.initializer("$S", getKeyName(field)).build();

			classBuilder.addField(fieldSpec);
		}

		JavaFile javaFile = JavaFile.builder(this.packageName, classBuilder.build())
				.build();
		javaFile.writeTo(appendable);
	}

	private static String getKeyName(Field field) {
		for (Annotation annotation : field.getAnnotations()) {
			Class<? extends Annotation> annotationType = annotation.annotationType();
			String name = annotationType.getName();
			if (name.equals("org.springframework.data.mongodb.core.mapping.Field")
					|| name.equals("org.mongodb.morphia.annotations.Property")) {
				String value = (String) getValue(annotation, "value");
				if (value != null && !".".equals(value) && !"".equals(value.trim())) {
					return value;
				}
			}
		}

		return field.getName();
	}

	private List<Field> collectFields() {
		List<Field> fields = new ArrayList<>();
		Field[] allFields = this.clazz.getDeclaredFields();
		for (Field field : allFields) {
			if (!isTransient(field)) {
				fields.add(field);
			}
		}

		fields.sort(Comparator.comparing(Field::getName));
		return fields;
	}

	private static boolean isTransient(Field field) {
		if (java.lang.reflect.Modifier.isTransient(field.getModifiers())) {
			return true;
		}

		for (Annotation annotation : field.getAnnotations()) {
			Class<? extends Annotation> annotationType = annotation.annotationType();

			String name = annotationType.getName();
			if (name.equals("org.springframework.data.annotation.Transient")
					|| name.equals("org.mongodb.morphia.annotations.Transient")) {
				return true;
			}
		}

		return false;
	}

	public static Object getValue(Annotation annotation, String attributeName) {
		try {
			Method method = annotation.annotationType().getDeclaredMethod(attributeName);
			if ((!java.lang.reflect.Modifier.isPublic(method.getModifiers())
					|| !java.lang.reflect.Modifier
							.isPublic(method.getDeclaringClass().getModifiers())
					|| java.lang.reflect.Modifier.isFinal(method.getModifiers()))
					&& !method.isAccessible()) {
				method.setAccessible(true);
			}
			return method.invoke(annotation);
		}
		catch (Exception ex) {
			return null;
		}
	}

}

/*
 * Copyright the original author or authors.
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeSpec.Builder;

public class CodeGenerator {

	private final TypeElement typeElement;

	private String packageName;

	private String className;

	private final Elements elements;

	private final boolean bsoncodecProject;

	public CodeGenerator(TypeElement typeElement, Elements elements,
			boolean bsoncodecProject) {
		this.typeElement = typeElement;
		this.packageName = elements.getPackageOf(typeElement).getQualifiedName()
				.toString();
		this.className = "C" + typeElement.getSimpleName();
		this.elements = elements;
		this.bsoncodecProject = bsoncodecProject;
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

		for (Constant constant : collectFields()) {
			FieldSpec fieldSpec = FieldSpec.builder(String.class, constant.getName())
					.addModifiers(Modifier.PUBLIC, Modifier.FINAL, Modifier.STATIC)
					.initializer("$S", constant.getValue()).build();

			classBuilder.addField(fieldSpec);
		}

		JavaFile javaFile = JavaFile.builder(this.packageName, classBuilder.build())
				.build();
		javaFile.writeTo(appendable);
	}

	private List<Constant> collectFields() {
		List<Constant> fields = new ArrayList<>();

		for (Element el : this.typeElement.getEnclosedElements()) {
			if (el.getKind() == ElementKind.FIELD) {

				VariableElement varEl = (VariableElement) el;
				if (!isTransient(varEl) && !isStatic(varEl)) {
					String value = getValue(varEl);
					fields.add(new Constant(el.getSimpleName().toString(), value));
				}

			}
		}

		Collections.sort(fields);
		return fields;
	}

	private static boolean isStatic(VariableElement el) {
		if (el.getModifiers().contains(Modifier.STATIC)) {
			return true;
		}
		return false;
	}

	private boolean isTransient(VariableElement el) {
		for (AnnotationMirror am : this.elements.getAllAnnotationMirrors(el)) {
			Name qualifiedName = ((TypeElement) am.getAnnotationType().asElement())
					.getQualifiedName();
			if (qualifiedName
					.contentEquals("org.springframework.data.annotation.Transient")
					|| qualifiedName
							.contentEquals("org.mongodb.morphia.annotations.Transient")
					|| qualifiedName
							.contentEquals("ch.rasc.bsoncodec.annotation.Transient")) {
				return true;
			}
		}

		return false;
	}

	private String getValue(VariableElement el) {
		String alternateValue = null;
		for (AnnotationMirror am : this.elements.getAllAnnotationMirrors(el)) {
			Name qualifiedName = ((TypeElement) am.getAnnotationType().asElement())
					.getQualifiedName();
			if (qualifiedName
					.contentEquals("org.springframework.data.mongodb.core.mapping.Field")
					|| qualifiedName
							.contentEquals("org.mongodb.morphia.annotations.Property")
					|| qualifiedName
							.contentEquals("ch.rasc.bsoncodec.annotation.Field")) {

				alternateValue = am.getElementValues().entrySet().stream()
						.filter(e -> e.getKey().getSimpleName().toString()
								.equals("value"))
						.map(e -> (String) e.getValue().getValue())
						.filter(s -> !".".equals(s) && !"".equals(s.trim())).findAny()
						.orElse(null);
			}
			else if (qualifiedName.contentEquals("ch.rasc.bsoncodec.annotation.Id")) {
				alternateValue = "_id";
			}
		}
		if (alternateValue == null) {
			String simpleName = el.getSimpleName().toString();
			if (this.bsoncodecProject) {
				if ("id".equals(simpleName)) {
					return "_id";
				}
			}
			return simpleName;
		}
		return alternateValue;
	}

}

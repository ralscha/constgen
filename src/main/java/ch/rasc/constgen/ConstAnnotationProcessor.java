/**
 * Copyright 2015-2018 the original author or authors.
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

import java.io.Writer;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import com.google.auto.service.AutoService;

@SupportedAnnotationTypes({ "org.springframework.data.mongodb.core.mapping.Document",
		"org.mongodb.morphia.annotations.Entity",
		"ch.rasc.bsoncodec.annotation.BsonDocument" })
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class ConstAnnotationProcessor extends AbstractProcessor {

	private static final boolean ALLOW_OTHER_PROCESSORS_TO_CLAIM_ANNOTATIONS = false;

	@Override
	public boolean process(Set<? extends TypeElement> annotations,
			RoundEnvironment roundEnv) {

		this.processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
				"Running " + getClass().getSimpleName());

		if (roundEnv.processingOver() || annotations.size() == 0) {
			return ALLOW_OTHER_PROCESSORS_TO_CLAIM_ANNOTATIONS;
		}

		if (roundEnv.getRootElements() == null || roundEnv.getRootElements().isEmpty()) {
			this.processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
					"No sources to process");
			return ALLOW_OTHER_PROCESSORS_TO_CLAIM_ANNOTATIONS;
		}

		for (TypeElement annotation : annotations) {
			Set<? extends Element> elements = roundEnv
					.getElementsAnnotatedWith(annotation);
			boolean bsoncodecProject = annotation.getQualifiedName()
					.contentEquals("ch.rasc.bsoncodec.annotation.BsonDocument");
			for (Element element : elements) {

				try {
					TypeElement typeElement = (TypeElement) element;

					CodeGenerator codeGen = new CodeGenerator(typeElement,
							this.processingEnv.getElementUtils(), bsoncodecProject);

					JavaFileObject jfo = this.processingEnv.getFiler().createSourceFile(
							codeGen.getPackageName() + "." + codeGen.getClassName());
					try (Writer writer = jfo.openWriter()) {
						codeGen.generate(writer);
					}

				}
				catch (Exception e) {
					this.processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
							e.getMessage());
				}

			}
		}

		return ALLOW_OTHER_PROCESSORS_TO_CLAIM_ANNOTATIONS;
	}

}

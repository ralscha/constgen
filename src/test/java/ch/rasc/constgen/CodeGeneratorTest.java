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

import org.junit.Test;

import com.google.common.truth.Truth;
import com.google.testing.compile.JavaFileObjects;
import com.google.testing.compile.JavaSourceSubjectFactory;

public class CodeGeneratorTest {

	@Test
	public void verifySpringData() {
		Truth.assert_().about(JavaSourceSubjectFactory.javaSource())
				.that(JavaFileObjects.forResource("UserSD.java"))
				.processedWith(new ConstAnnotationProcessor()).compilesWithoutError()
				.and().generatesSources(JavaFileObjects.forResource("CUserSD.java"));
	}

	@Test
	public void verifyMorphia() {
		Truth.assert_().about(JavaSourceSubjectFactory.javaSource())
				.that(JavaFileObjects.forResource("UserMorphia.java"))
				.processedWith(new ConstAnnotationProcessor()).compilesWithoutError()
				.and().generatesSources(JavaFileObjects.forResource("CUserMorphia.java"));
	}

}

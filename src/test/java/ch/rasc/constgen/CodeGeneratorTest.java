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
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.util.StreamUtils;

public class CodeGeneratorTest {

	@Test
	public void verifySpringData() {
		CodeGenerator cd = new CodeGenerator(UserSD.class);

		StringBuilder sb = new StringBuilder();
		try {
			cd.generate(sb);

			String code = sb.toString();

			InputStream is = getClass().getResourceAsStream("/CUserSD.txt");
			String expected = new String(StreamUtils.copyToByteArray(is),
					StandardCharsets.UTF_8);

			code = code.replace("\r", "").trim();
			expected = expected.replace("\r", "").trim();

			Assert.assertEquals(expected, code);

		}
		catch (IOException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void verifyMorphia() {
		CodeGenerator cd = new CodeGenerator(UserMorphia.class);

		StringBuilder sb = new StringBuilder();
		try {
			cd.generate(sb);

			String code = sb.toString();

			InputStream is = getClass().getResourceAsStream("/CUserMorphia.txt");
			String expected = new String(StreamUtils.copyToByteArray(is),
					StandardCharsets.UTF_8);

			code = code.replace("\r", "").trim();
			expected = expected.replace("\r", "").trim();

			Assert.assertEquals(expected, code);

		}
		catch (IOException e) {
			Assert.fail(e.getMessage());
		}
	}

}

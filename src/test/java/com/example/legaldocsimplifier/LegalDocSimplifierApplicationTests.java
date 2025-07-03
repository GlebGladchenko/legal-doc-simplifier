package com.example.legaldocsimplifier;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestMailConfig.class)
class LegalDocSimplifierApplicationTests {

	@Test
	void contextLoads() {
	}

}

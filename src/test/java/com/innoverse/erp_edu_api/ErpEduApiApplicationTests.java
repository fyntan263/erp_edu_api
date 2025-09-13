package com.innoverse.erp_edu_api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.modulith.core.ApplicationModules;

@SpringBootTest
class ErpEduApiApplicationTests {
    ApplicationModules modules = ApplicationModules.of(ErpEduApiApplication.class);
	@Test
	void contextLoads() {
        modules.verify();
	}

}

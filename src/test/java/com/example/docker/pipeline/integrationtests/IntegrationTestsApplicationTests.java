package com.example.docker.pipeline.integrationtests;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class IntegrationTestsApplicationTests {

	static String containerId;
	
	static PortMapper ports = PortMapper.builder()
			.hostPort(8081)
			.containerPort(8080)
			.build();
	static ImageConfig image = ImageConfig.builder()
			.name("pulgupta/hostping")
			.ports(Collections.singletonList(ports))
			.healthCheckEndPoint("http://localhost:8081/ping")
			.timeout(20000)
			.build();
	
	@Autowired
	MockMvc mockmvc;
	
	@BeforeClass
	public static void startContainers() {
		containerId = ContainerManager.startContainer(image);
	}
	
	@Test
	public void FIndUserByLoginSuccess() throws Exception {
		mockmvc.perform(get("/hostping-client").contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message", is("pong - From client"))).andReturn();
	}
	
	@AfterClass
	public static void stopContainers() {
		ContainerManager.stopContainer(containerId);
	}

}

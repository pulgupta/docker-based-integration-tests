package com.example.docker.pipeline.integrationtests;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.example.docker.pipeline.integrationtests.model.HostService;

/**
 * A sample rest API which in turn calls our server API
 * @author pulgupta
 */
@RestController
public class HostpingClient {

	@RequestMapping("hostping-client")
	public HostService getHostDetails() {
		RestTemplate template = new RestTemplate();
		// Call the back-end service which will be dockerized for integration testing
		HostService data =  template.getForObject("http://localhost:8081/ping", HostService.class);
		data.setMessage(data.getMessage().concat(" - From client"));
		return data;
	}
}

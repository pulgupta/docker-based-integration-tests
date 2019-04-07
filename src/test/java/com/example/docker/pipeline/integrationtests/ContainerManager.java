package com.example.docker.pipeline.integrationtests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.example.docker.pipeline.integrationtests.model.HostService;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.PortBinding;

import lombok.extern.slf4j.Slf4j;

/**
 * A generic utility to start and stop containers.
 * ContainerManger hides all the complexity of container configuration and 
 * can easily be used to start any docker container 
 * @author pulgupta
 */
@Slf4j
public final class ContainerManager {
	
	private static DockerClient dockerClient;

	private enum Status{
		STARTED,
		UNKNOWN
	}
	
	static {
		try {
			dockerClient = DefaultDockerClient.fromEnv().build();
		} catch (DockerCertificateException e) {
			log.error(e.getMessage());
		}
	}
	
	private ContainerManager() {
		throw new AssertionError();
	}
	
	/**
	 * Start container just by passing the image details 
	 * @param image Image details like name, port to map, status end point etc
	 * @return containerId of the successfully started container
	 */
	public static String startContainer(ImageConfig image) {
		try {
			Map<String, List<PortBinding>> bindings = new HashMap<>();
			Set<String> exposedPorts = new HashSet<>();
			
			// Map the host and container ports
			for(PortMapper mapper: image.getPorts()) {
				List<PortBinding> hostPorts = new ArrayList<>();
				hostPorts.add(PortBinding.create("0.0.0.0", Integer.toString(mapper.getHostPort())));
				bindings.put(Integer.toString(mapper.getContainerPort()), hostPorts);
				exposedPorts.add(Integer.toString(mapper.getContainerPort()));
			}
			
			HostConfig config = HostConfig.builder()
					.portBindings(bindings)
					.build();
			ContainerConfig containerConfig = ContainerConfig.builder()
					.image(image.getName())
					.hostConfig(config)
					.exposedPorts(exposedPorts)
					.build();
			
			ContainerCreation creation = dockerClient.createContainer(containerConfig);
			String id = creation.id();
			// pull the docker image if not available locally
			dockerClient.pull(image.getName());
			dockerClient.startContainer(id);
			// check if the application within the container has started
			Status status = getContainerStatus(image.getName(), image.getHealthCheckEndPoint(), image.getTimeout());
			if(status != Status.STARTED) {
				throw new DockerException("Not able to communicate with the docker service");
			}
			return id;
		
		} catch (DockerException | InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Stop any running container
	 * @param id
	 */
	public static void stopContainer(String id) {
		try {
			dockerClient.stopContainer(id, 0);
		} catch (DockerException | InterruptedException e) {
			log.warn("Error in stopping container {}. Please stop it manually", id);
		}
	}
	
	/**
	 * To check if the application inside the container is actually up
	 * @param imageName Name of the image
	 * @param healthEndpoint The end point to check for application status 
	 * @param timeout
	 * @return status of the contaniorized application
	 * @throws InterruptedException
	 */
	private static Status getContainerStatus(String imageName, String healthEndpoint, long timeout) throws InterruptedException {
		long slice  = 0;
		while(slice < timeout) {
			RestTemplate template = new RestTemplate();
			try {
				ResponseEntity<HostService> response =  template.exchange(healthEndpoint, HttpMethod.GET, null, HostService.class);
				if(response.getStatusCode() == HttpStatus.OK)
					return Status.STARTED;
			} catch (Exception e) {
				log.warn("Container not fully started for {}. Waiting....", imageName);
			}
			Thread.sleep(2000);
			slice+=2000;
		}
		return Status.UNKNOWN;
	}
}

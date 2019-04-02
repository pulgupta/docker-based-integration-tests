package com.example.docker.pipeline.integrationtests;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerCertificateException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ContainerUtil {
	
	private static final int WAIT_TIMEOUT = 2000;
	private static DockerClient docker;

	static {
		try {
			docker = DefaultDockerClient.fromEnv().build();
		} catch (DockerCertificateException e) {
			log.error(e.getMessage());
		}
	}
	
	private ContainerUtil() { }
	
	
	
}

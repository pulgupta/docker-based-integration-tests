package com.example.docker.pipeline.integrationtests;

import java.util.List;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class ImageConfig {

	String name;
	String healthCheckEndPoint;
	List<PortMapper> ports;
	long timeout;
	
}
@Builder
@Value
class PortMapper {
	int hostPort;
	int containerPort;
}

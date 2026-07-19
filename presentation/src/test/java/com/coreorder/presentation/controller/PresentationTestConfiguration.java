package com.coreorder.presentation.controller;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

/**
 * Marker @SpringBootConfiguration that lets @WebMvcTest slice tests in this
 * module find a configuration root by walking the package tree upwards.
 * <p>
 * Each presentation test that needs a Spring context must be discovered under
 * this class. The class itself is empty; the slice is narrowed by the
 * @WebMvcTest annotation on each individual test.
 */
@SpringBootConfiguration
@EnableAutoConfiguration
public class PresentationTestConfiguration {
}

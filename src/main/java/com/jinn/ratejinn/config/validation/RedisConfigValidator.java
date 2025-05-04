package com.jinn.ratejinn.config.validation;

import com.jinn.ratejinn.config.RedisConfig;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class RedisConfigValidator implements ConstraintValidator<ValidRedisConfig, RedisConfig> {

    @Override
    public boolean isValid(RedisConfig config, ConstraintValidatorContext context) {
        boolean valid = true;
        context.disableDefaultConstraintViolation();

        if ("standalone".equals(config.getMode())) {
            if (config.getStandalone() == null) {
                context.buildConstraintViolationWithTemplate("Standalone configuration is required when mode is 'standalone'")
                        .addConstraintViolation();
                valid = false;
            }

            if (config.getCluster() != null) {
                context.buildConstraintViolationWithTemplate("Cluster configuration should not be set when mode is 'standalone'")
                        .addConstraintViolation();
                valid = false;
            }
        } else if ("cluster".equals(config.getMode())) {
            if (config.getCluster() == null || config.getCluster().getNodes() == null || config.getCluster().getNodes().isEmpty()) {
                context.buildConstraintViolationWithTemplate("Cluster configuration with nodes is required when mode is 'cluster'")
                        .addConstraintViolation();
                valid = false;
            }

            if (config.getStandalone() != null) {
                context.buildConstraintViolationWithTemplate("Standalone configuration should not be set when mode is 'cluster'")
                        .addConstraintViolation();
                valid = false;
            }
        }

        return valid;
    }
}

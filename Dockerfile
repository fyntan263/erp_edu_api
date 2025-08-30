# Use official OpenJDK 21 image
FROM openjdk:21-jdk-slim

# Argument for jar path
ARG JAR_FILE=build/libs/erp_edu_api-0.0.1-SNAPSHOT.jar

# Set working directory
WORKDIR /app

# Copy the built jar into the container
COPY ${JAR_FILE} erp_edu_api.jar

# Expose port for Spring Boot
EXPOSE 8080

# Optional: volume for temp files/logs
VOLUME /tmp

# Environment variables for JVM options and Spring profile
ENV JAVA_OPTS="-Xms512m -Xmx1024m"
#ENV SPRING_PROFILES_ACTIVE=prod

## Optional healthcheck
#HEALTHCHECK --interval=30s --timeout=5s --start-period=30s \
#  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the Spring Boot application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/erp_edu_api.jar"]

# Use official OpenJDK 18 image
FROM openjdk:18

WORKDIR /app

# Copy JAR to container and rename
COPY target/*.jar loan.jar

# Expose port Spring Boot runs on
EXPOSE 8083

# Run the JAR
ENTRYPOINT ["java", "-jar", "loan.jar"]

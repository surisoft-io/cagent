FROM openjdk:21

# Set the working directory in the container
WORKDIR /app

# Copy the JAR file into the container
COPY target/cagent-1.0.0.jar /app/

# Run the application when the container starts
CMD ["java", "-jar", "cagent-1.0.0.jar"]
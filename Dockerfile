FROM openjdk:21-slim-bullseye

# Set the working directory in the container
#WORKDIR /app

# Copy the JAR file into the container
COPY target/cagent-1.0.0-jar-with-dependencies.jar app.jar

# Run the application when the container starts
CMD ["java", "-jar", "app.jar"]
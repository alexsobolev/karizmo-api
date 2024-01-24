# Use the official maven/Java 11 image to create a build artifact.
# https://hub.docker.com/_/maven
FROM openjdk:17-jdk-slim AS build-env
ENV APP_HOME=/usr/app/
WORKDIR $APP_HOME
COPY build.gradle settings.gradle gradlew $APP_HOME
COPY gradle $APP_HOME/gradle
RUN ./gradlew build || return 0
COPY . .
RUN ./gradlew build

# Use OpenJDK for base image.
# https://hub.docker.com/_/openjdk
# https://docs.docker.com/develop/develop-images/multistage-build/#use-multi-stage-builds
FROM openjdk:17-jdk-slim
ENV ARTIFACT_NAME=./karizmo-0.0.1-SNAPSHOT.jar
ENV APP_HOME=/usr/app/
WORKDIR $APP_HOME
COPY --from=build-env $APP_HOME/build/libs/karizmo-0.0.1-SNAPSHOT.jar .
EXPOSE 8080
CMD ["java","-jar", "./karizmo-0.0.1-SNAPSHOT.jar"]

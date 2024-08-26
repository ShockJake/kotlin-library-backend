# Base image
FROM ubuntu:24.10 AS base

ARG DEBIAN_FRONTEND=noninteractive

RUN rm /bin/sh && ln -s /bin/bash /bin/sh
RUN apt-get update

RUN apt install -y curl unzip zip
RUN curl -Lo /tmp/sdkman.sh "https://get.sdkman.io"
RUN chmod 777 /tmp/sdkman.sh
RUN /tmp/sdkman.sh

# Building image
FROM base AS build

RUN source "$HOME/.sdkman/bin/sdkman-init.sh"; sdk install java 21.0.4-tem; sdk install kotlin 2.0.0; sdk install gradle 8.9
ENV JAVA_HOME=/root/.sdkman/candidates/java/21.0.4-tem
ENV GRADLE_HOME=/root/.sdkman/candidates/gradle/8.9

RUN mkdir -p "/tmp/app/src/"
COPY src/ /tmp/app/src/
COPY build.gradle.kts /tmp/app/build.gradle.kts
COPY settings.gradle.kts /tmp/app/settings.gradle.kts
COPY gradle.properties /tmp/app/gradle.properties

WORKDIR "/tmp/app/"
RUN ${GRADLE_HOME}/bin/gradle buildFatJar

# Main image
FROM base AS runner

RUN source "$HOME/.sdkman/bin/sdkman-init.sh"; sdk install java 21.0.4-tem; sdk install kotlin 2.0.0; sdk install gradle 8.9
ENV JAVA_HOME=/root/.sdkman/candidates/java/21.0.4-tem
EXPOSE 8080
RUN mkdir -p "/tmp/app"
COPY --from=build /tmp/app/build/libs/kotlin-library.jar /tmp/app/kotlin-library.jar
CMD ${JAVA_HOME}/bin/java -jar /tmp/app/kotlin-library.jar >> library_backend.log

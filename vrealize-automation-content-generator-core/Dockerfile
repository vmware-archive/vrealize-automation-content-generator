FROM openjdk:11

RUN mkdir /vrealize-automation-content-generator
COPY target/vrealize-automation-content-generator-core-1.1.jar /vrealize-automation-content-generator/vrealize-automation-content-generator.jar
COPY target/lib /vrealize-automation-content-generator/

VOLUME /resources

WORKDIR /resources
ENTRYPOINT ["java", "-cp", "/vrealize-automation-content-generator/*:/vrealize-automation-content-generator/lib/*", "com.vmware.devops.cli.Generate", "--path", "/resources"]
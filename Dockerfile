# Use a base image with Java 21
#FROM eclipse-temurin:21-jdk-jammy
#
## Set environment variables
#ENV CATALINA_HOME=/opt/tomcat
#ENV PATH="$CATALINA_HOME/bin:$PATH"
#
## Install curl and Tomcat 9.0.106
#RUN apt-get update && \
#    apt-get install -y curl && \
#    curl -fsSL https://downloads.apache.org/tomcat/tomcat-9/v9.0.106/bin/apache-tomcat-9.0.106.tar.gz | tar xz -C /opt && \
#    mv /opt/apache-tomcat-9.0.106 /opt/tomcat && \
#    rm -rf /opt/tomcat/webapps/*
FROM tomcat:9
RUN rm -rf /usr/local/tomcat/webapps/*
# Copy WAR file into Tomcat
#COPY target/hms.war $CATALINA_HOME/webapps/hms.war
COPY target/hms.war /usr/local/tomcat/webapps/ROOT.war

# Expose Tomcat port
EXPOSE 8080

# Start Tomcat
#CMD ["catalina.sh", "run"]

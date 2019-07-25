#!/bin/sh

CP=patch.jar
CP=$CP:./lib/booking.jar
CP=$CP:./lib/spring-aop-2.5.4.jar
CP=$CP:./lib/xercesImpl-2.8.1.jar
CP=$CP:./lib/spring-ws-core-1.5.2.jar
CP=$CP:./lib/spring-beans-2.5.4.jar
CP=$CP:./lib/spring-webmvc-2.5.4.jar
CP=$CP:./lib/commons-logging-1.1.1.jar
CP=$CP:./lib/XmlSchema-1.3.2.jar
CP=$CP:./lib/xalan-2.7.0.jar
CP=$CP:./lib/spring-core-2.5.4.jar
CP=$CP:./lib/spring-context-2.5.4.jar
CP=$CP:./lib/spring-oxm-1.5.2.jar
CP=$CP:./lib/wsdl4j-1.6.1.jar
CP=$CP:./lib/log4j-1.2.15.jar
CP=$CP:./lib/aopalliance-1.0.jar
CP=$CP:./lib/spring-context-support-2.5.4.jar
CP=$CP:./lib/spring-xml-1.5.2.jar
CP=$CP:./lib/xml-apis-1.3.04.jar
CP=$CP:./lib/spring-web-2.5.4.jar

PROC="com.tamageta.financial.booking.AppMain http://localhost/edsweb/quote/services"

echo $CP
echo $PROC

$JAVA_HOME/bin/java -cp $CP $PROC

FROM ubuntu:16.04

MAINTAINER Andrey Babkov


# Обвновление списка пакетов
RUN apt-get -y update

# Установка JDK
RUN apt-get install -y openjdk-9-jdk-headless

USER root

# Копируем исходный код в Docker-контейнер
ENV WORK /opt/api
ADD / $WORK/


# Собираем и устанавливаем пакет
WORKDIR $WORK/
RUN ./mvnw clean package -DskipTests

# Объявлем порт сервера
EXPOSE 5000

#
# Запускаем PostgreSQL и сервер
#
CMD java -Xmx300M -Xmx300M -jar $WORK/target/forum.jar
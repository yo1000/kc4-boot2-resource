# kc4-boot2-resource

Keycloak 4.0 x Spring Boot 2.0 examples

```
$ git clone https://github.com/yo1000/kc4-boot2-resource.git
$ wget https://downloads.jboss.org/keycloak/4.0.0.Final/keycloak-4.0.0.Final.tar.gz
$ tar -zxvf keycloak-4.0.0.Final.tar.gz

$ cd keycloak-4.0.0.Final
$ ../kc4-boot2-resource/kc4.setup.sh

$ cd ../kc4-boot2-resource
$ ./mvnw clean spring-boot:run -pl kc4-boot2-resource-server &
$ ./mvnw clean spring-boot:run -pl kc4-boot2-resource-client &
```
